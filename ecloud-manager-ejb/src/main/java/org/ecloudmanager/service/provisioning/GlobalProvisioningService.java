/*
 * MIT License
 *
 * Copyright (c) 2016  Altisource
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

package org.ecloudmanager.service.provisioning;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import org.apache.logging.log4j.Logger;
import org.ecloudmanager.deployment.vm.VMDeployer;
import org.ecloudmanager.deployment.vm.VMDeployment;
import org.ecloudmanager.deployment.vm.infrastructure.InfrastructureDeployer;
import org.ecloudmanager.deployment.vm.provisioning.ChefEnvironment;
import org.ecloudmanager.deployment.vm.provisioning.ChefEnvironmentDeployer;
import org.ecloudmanager.domain.chef.ChefConfiguration;
import org.ecloudmanager.domain.template.SshConfiguration;
import org.ecloudmanager.jeecore.service.Service;
import org.ecloudmanager.node.AsyncNodeAPI;
import org.ecloudmanager.node.model.Command;
import org.ecloudmanager.node.model.Credentials;
import org.ecloudmanager.node.model.SSHCredentials;
import org.ecloudmanager.repository.SshConfigurationRepository;
import org.ecloudmanager.repository.deployment.LoggingEventRepository;
import org.ecloudmanager.service.chef.ChefGenerationService;

import javax.inject.Inject;
import java.io.*;
import java.util.HashMap;

import static org.ecloudmanager.node.LoggableFuture.waitFor;

@Service
public class GlobalProvisioningService {

    @Inject
    private SshConfigurationRepository sshConfigurationRepository;

    @Inject
    private ChefGenerationService chefGenerationService;

    @Inject
    private Logger log;
    @Inject
    private LoggingEventRepository loggingEventRepository;

    public void provisionVm(String actionId, VMDeployment deployment, AsyncNodeAPI api, Credentials credentials, boolean firstRun) {
        LoggingEventRepository.ActionLogger actionLog = loggingEventRepository.createActionLogger(GlobalProvisioningService.class, actionId);
        try {
            provisionVmImpl(actionLog, deployment, api, credentials, firstRun);
        } catch (Exception e) {
            actionLog.error("Failed to provision VM " + deployment.getConfigValue(VMDeployer.VM_NAME), e);
            throw new RuntimeException(e);
        }
    }

    private void provisionVmImpl(LoggingEventRepository.ActionLogger actionLog, VMDeployment deployment, AsyncNodeAPI api, Credentials credentials, boolean firstRun) throws Exception {
        if (deployment == null) {
            throw new IllegalArgumentException("Provisioning without deployment object is not supported, exiting...");
        }

        String nodeName = deployment.getConfigValue(VMDeployer.VM_NAME);
        String nodeId = InfrastructureDeployer.getVmId(deployment);
        String sshConfigurationName = VMDeployer.getSshConfiguration(deployment);
        SshConfiguration c = sshConfigurationRepository.find(sshConfigurationName);
        if (c == null) {
            throw new IllegalArgumentException("SSH configuration not found: " + sshConfigurationName);
        }
        SSHCredentials sshCredentials = new SSHCredentials().jumpHost1Username(c.getJumpHost1Username())
                .jumpHost1(c.getJumpHost1()).jumpHost1PrivateKey(c.getJumpHost1PrivateKey()).jumpHost1PrivateKeyPassphrase(c.getJumpHost1PrivateKeyPassphrase()).jumpHost1Username(c.getJumpHost1Username())
                .jumpHost2(c.getJumpHost2()).jumpHost2PrivateKey(c.getJumpHost2PrivateKey()).jumpHost2PrivateKeyPassphrase(c.getJumpHost2PrivateKeyPassphrase()).jumpHost2Username(c.getJumpHost2Username())
                .username(c.getUsername()).privateKey(c.getPrivateKey()).privateKeyPassphrase(c.getPrivateKeyPassphrase());

        actionLog.info("Started provisioning of vm: " + nodeName);
        if (firstRun) {
            InputStream clientRbTemplateStream = this.getClass().getResourceAsStream("/client.rb.mustache");
            InputStream clientRbStream = generateClientRb(clientRbTemplateStream, deployment.getChefEnvironment(),
                nodeName);
            waitFor(api.uploadFile(credentials, sshCredentials, nodeId, clientRbStream, "client.rb"), actionLog);

            ChefConfiguration chefConfiguration = ChefEnvironmentDeployer.getChefConfiguration(deployment.getChefEnvironment());
            waitFor(api.uploadFile(credentials, sshCredentials, nodeId, new ByteArrayInputStream(chefConfiguration.getChefValidationClientSecret().getBytes()), "chef-validator.pem"), actionLog);

        }

        waitFor(api.uploadFile(credentials, sshCredentials, nodeId, generateNodeJson(deployment, "\"recipe[hostnames]\""), "node-config.json"), actionLog);
        waitFor(api.uploadFile(credentials, sshCredentials, nodeId, generateNodeJson(deployment), "node.json"), actionLog);

        Command cmd = new Command().credentials(sshCredentials);
        if (firstRun) {
            cmd
                    .addCommandItem("sudo yum install -y wget")
                    .addCommandItem("curl -L https://omnitruck.chef.io/install.sh | sudo bash -s -- -v 12.8.1")
                    .addCommandItem("sudo mkdir /etc/chef")
                    .addCommandItem("sudo chmod 777 /etc/chef")
                    .addCommandItem("mv -f client.rb /etc/chef")
                    .addCommandItem("mv -f chef-validator.pem /etc/chef");
        }
        cmd
                .addCommandItem("mv -f node-config.json /etc/chef")
                .addCommandItem("mv -f node.json /etc/chef")
                .addCommandItem("sudo chef-client -j /etc/chef/node-config.json")
                .addCommandItem("sudo chef-client -j /etc/chef/node.json");

        actionLog.info("Issuing the following command to " + nodeName + ": " + cmd.getCommand());
        int exitCode = waitFor(api.executeScript(credentials, sshCredentials, nodeId, cmd), actionLog);

        actionLog.info("Provisioning process finished with exit status: " + exitCode);

        if (exitCode != 0) {
            throw new Exception("Error provisioning VM " + nodeName + ", exit status: " + exitCode);
        }
        log.info("Finished provisioning of vm: " + nodeName);
    }


    private InputStream generateClientRb(InputStream clientRbTemplate, ChefEnvironment env, String
            nodeName) {
        MustacheFactory mf = new UnescapedMustacheFactory();
        Mustache mustache = mf.compile(new InputStreamReader(clientRbTemplate), "client.rb");

        ChefConfiguration chefConfiguration = ChefEnvironmentDeployer.getChefConfiguration(env);
        HashMap<String, String> scopes = new HashMap<>();
        scopes.put("node_name", nodeName);
        scopes.put("chef_server_url", chefConfiguration.getChefServerAddress());
        scopes.put("environment", env.getTop().getName());
        scopes.put("validation_client_name", chefConfiguration.getChefValidationClientName());

        StringWriter writer = new StringWriter();
        mustache.execute(writer, scopes);
        writer.flush();
        return new ByteArrayInputStream(writer.toString().getBytes());
    }

    private InputStream generateNodeJson(VMDeployment deployment) {
        Writer sw = chefGenerationService.generateChefNodeEnv(new StringWriter(), deployment);
        return getInputStreamFromWriter(sw);
    }

    private InputStream generateNodeJson(VMDeployment deployment, String runlist) {
        Writer sw = chefGenerationService.generateChefNodeEnv(new StringWriter(), deployment, runlist);
        return getInputStreamFromWriter(sw);
    }

    private InputStream getInputStreamFromWriter(Writer writer) {
        String jsonString;
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        try {
            Object o = mapper.readValue(writer.toString(), Object.class);
            jsonString = mapper.writeValueAsString(o);
        } catch (IOException e) {
            jsonString = e.toString();
        }

        return new ByteArrayInputStream(jsonString.getBytes());
    }

    public boolean needUpdateProvisioning(VMDeployment before, VMDeployment after) {
        String oldNodeJson = chefGenerationService.generateChefNodeEnv(new StringWriter(), before).toString();
        String newNodeJson = chefGenerationService.generateChefNodeEnv(new StringWriter(), after).toString();
        return !oldNodeJson.equals(newNodeJson);
    }
}
