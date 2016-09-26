/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2016 Altisource Labs
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package org.ecloudmanager;

import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.AbstractProject;
import hudson.model.Result;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.tasks.*;
import hudson.util.FormValidation;
import jenkins.tasks.SimpleBuildStep;
import net.jodah.failsafe.Failsafe;
import net.jodah.failsafe.RetryPolicy;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.ecloudmanager.web.model.LoggingEvent;
import org.ecloudmanager.web.model.TaskInfo;
import org.ecloudmanager.web.rest.ApiClient;
import org.ecloudmanager.web.rest.ApiException;
import org.ecloudmanager.web.rest.client.DeploymentApi;
import org.ecloudmanager.web.rest.client.TasksApi;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;

import javax.servlet.ServletException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

/**
 * Sample {@link Builder}.
 *
 * <p>
 * When the user configures the project and enables this builder,
 * {@link DescriptorImpl#newInstance(StaplerRequest)} is invoked
 * and a new {@link EcloudManagerBuilder} is created. The created
 * instance is persisted to the project configuration XML by using
 * XStream, so this allows you to use instance fields (like {@link #apiUrl})
 * to remember the configuration.
 *
 * <p>
 * When a build is performed, the {@link #perform} method will be invoked. 
 *
 * @author Kohsuke Kawaguchi
 */
public class EcloudManagerBuilder extends Notifier implements SimpleBuildStep {

    private final String apiUrl;
    private final String apiUser;
    private final String apiPassword;
    private final String deploymentName;

    // Fields in config.jelly must match the parameter names in the "DataBoundConstructor"
    @DataBoundConstructor
    public EcloudManagerBuilder(String apiUrl, String apiUser, String apiPassword, String deploymentName) {
        this.apiUrl = apiUrl;
        this.apiUser = apiUser;
        this.apiPassword = apiPassword;
        this.deploymentName = deploymentName;
    }

    /**
     * We'll use this from the <tt>config.jelly</tt>.
     */
    public String getApiUrl() {
        return apiUrl;
    }

    public String getApiUser() {
        return apiUser;
    }

    public String getApiPassword() {
        return apiPassword;
    }

    public String getDeploymentName() {
        return deploymentName;
    }

    @Override
    public void perform(Run<?,?> build, FilePath workspace, Launcher launcher, TaskListener listener) {
        if (build.getResult().equals(Result.SUCCESS)) {
            // This is where you 'build' the project.
            listener.getLogger().println("Starting Ecloud Manager deployment...");

            ApiClient apiClient = new ApiClient();
            apiClient.setBasePath(apiUrl);
            apiClient.setUsername(apiUser);
            apiClient.setPassword(apiPassword);
            DeploymentApi deploymentApi = new DeploymentApi(apiClient);
            try {
                List<String> deployments = deploymentApi.getDeployments(deploymentName);
                if (deployments.size() == 0) {
                    listener.getLogger().println("Error: application deployment '" + deploymentName + "' not found.");
                    build.setResult(Result.FAILURE);
                    return;
                } else if (deployments.size() > 1) {
                    listener.getLogger().println("Warning: found multiple application deployments with name '" + deploymentName + "'.");
                    listener.getLogger().println(StringUtils.join(deployments, "\n"));
                }

                String deploymentId = deployments.get(0);
                String taskId = deploymentApi.deploy(deploymentId);
                TasksApi tasksApi = new TasksApi(apiClient);

                RetryPolicy untilTaskDone = new RetryPolicy()
                        .<TaskInfo>retryIf(t -> !t.getDone())
                        .withDelay(3, TimeUnit.SECONDS)
                        .withMaxDuration(10000, TimeUnit.SECONDS);
                Failsafe.with(untilTaskDone).get(() -> {
                    List<LoggingEvent> loggingEvents = tasksApi.pollLog(taskId);
                    loggingEvents.forEach(e -> {
                        listener.getLogger().println(convertLoggingEventToString(e));
                    });
                    return tasksApi.getTask(taskId);
                });

                LoggingEvent exception = tasksApi.getTask(taskId).getException();
                if (exception != null) {
                    build.setResult(Result.FAILURE);
                    listener.getLogger().println("Ecloud manager deployment failed: ");
                    listener.getLogger().println(convertLoggingEventToString(exception));
                } else {
                    listener.getLogger().println("Ecloud manager deployment successfully completed.");
                }
            } catch (ApiException e) {
                listener.getLogger().println("Error: unable to use ecloud manager API: " + e);
                listener.getLogger().println(StringUtils.join(e.getStackTrace(), "\n"));
                build.setResult(Result.FAILURE);
            }
        }
    }

    private String convertLoggingEventToString(LoggingEvent loggingEvent) {
        return new Date(loggingEvent.getTimeStamp()) + " " +
               loggingEvent.getLevel() + ": " + loggingEvent.getMessage() + " " + loggingEvent.getThrowable();
    }

    // Overridden for better type safety.
    // If your plugin doesn't really define any property on Descriptor,
    // you don't have to do this.
    @Override
    public DescriptorImpl getDescriptor() {
        return (DescriptorImpl)super.getDescriptor();
    }

    @Override
    public BuildStepMonitor getRequiredMonitorService() {
        return BuildStepMonitor.STEP;
    }

    /**
     * Descriptor for {@link EcloudManagerBuilder}. Used as a singleton.
     * The class is marked as public so that it can be accessed from views.
     *
     * <p>
     * See <tt>src/main/resources/hudson/plugins/hello_world/EcloudManagerBuilder/*.jelly</tt>
     * for the actual HTML fragment for the configuration screen.
     */
    @Extension // This indicates to Jenkins that this is an implementation of an extension point.
    public static final class DescriptorImpl extends BuildStepDescriptor<Publisher> {
        /**
         * To persist global configuration information,
         * simply store it in a field and call save().
         *
         * <p>
         * If you don't want fields to be persisted, use <tt>transient</tt>.
         */

        /**
         * In order to load the persisted global configuration, you have to 
         * call load() in the constructor.
         */
        public DescriptorImpl() {
            load();
        }

        /**
         * Performs on-the-fly validation of the form field 'apiUrl'.
         *
         * @param value
         *      This parameter receives the value that the user has typed.
         * @return
         *      Indicates the outcome of the validation. This is sent to the browser.
         *      <p>
         *      Note that returning {@link FormValidation#error(String)} does not
         *      prevent the form from being saved. It just means that a message
         *      will be displayed to the user. 
         */
        public FormValidation doCheckApiUrl(@QueryParameter String value)
                throws IOException, ServletException {
            if (value.length() == 0)
                return FormValidation.error("Please set a valid API URL");
            try {
                new URL(value);
            } catch (MalformedURLException e) {
                return FormValidation.warning("URL is not valid");
            }

            return FormValidation.ok();
        }

        public FormValidation doCheckDeploymentName(@QueryParameter String value)
                throws IOException, ServletException {
            if (value.length() == 0) {
                return FormValidation.error("Please set a valid application deployment name");
            }

            return FormValidation.ok();
        }

        public boolean isApplicable(Class<? extends AbstractProject> aClass) {
            // Indicates that this builder can be used with all kinds of project types 
            return true;
        }

        /**
         * This human readable name is used in the configuration screen.
         */
        public String getDisplayName() {
            return "Deploy/update Ecloud Manager application deployment";
        }

        @Override
        public boolean configure(StaplerRequest req, JSONObject formData) throws FormException {
            // To persist global configuration information,
            // set that to properties and call save().

            // ^Can also use req.bindJSON(this, formData);
            //  (easier when there are many fields; need set* methods for this, like setUseFrench)
            save();
            return super.configure(req,formData);
        }
    }
}

