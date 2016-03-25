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

package org.ecloudmanager.service;

import org.apache.logging.log4j.Logger;
import org.ecloudmanager.deployment.app.ApplicationTemplate;
import org.ecloudmanager.deployment.core.EndpointTemplate;
import org.ecloudmanager.deployment.es.ExternalServiceTemplate;
import org.ecloudmanager.deployment.ps.HAProxyFrontendConfig;
import org.ecloudmanager.deployment.ps.ProducedServiceTemplate;
import org.ecloudmanager.deployment.ps.cg.ComponentGroupTemplate;
import org.ecloudmanager.deployment.ps.cg.HAProxyBackendConfig;
import org.ecloudmanager.deployment.vm.VirtualMachineTemplate;
import org.ecloudmanager.deployment.vm.infrastructure.Infrastructure;
import org.ecloudmanager.deployment.vm.provisioning.Recipe;
import org.ecloudmanager.repository.template.VirtualMachineTemplateRepository;
import org.ecloudmanager.service.deployment.ApplicationDeploymentService;
import org.ecloudmanager.service.template.ApplicationTemplateService;
import org.ecloudmanager.service.template.RecipeService;
import org.ecloudmanager.service.template.VirtualMachineTemplateService;

import javax.annotation.PostConstruct;
import javax.ejb.LocalBean;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.inject.Inject;
import java.util.Arrays;


@Singleton
@LocalBean
@Startup
public class BootstrapSeedingService {
    @Inject
    private RecipeService recipeService;

    @Inject
    private VirtualMachineTemplateRepository virtualMachineTemplateRepository;

    @Inject
    private VirtualMachineTemplateService virtualMachineTemplateService;

    @Inject
    private ApplicationTemplateService applicationTemplateService;

    @Inject
    private ApplicationDeploymentService appDeploymentService;

    @Inject
    private Logger log;

    @PostConstruct
    public void init() {
        if (virtualMachineTemplateRepository.getAll().isEmpty()) {
            log.info("Database is empty, creating example data");
            populateDatabaseWithExampleData();
        }
    }

    private void populateDatabaseWithExampleData() {
        Recipe tomcat = new Recipe("tomcat");
        recipeService.save(tomcat);
        Recipe iam_app = new Recipe("iam-app");
        iam_app.addEndpoint(new EndpointTemplate("IAM_APP"));
        iam_app.addEnvironmentOverrideAttribute("iam_public_hostname", "\"{{SHIB:fqdn}}\"");
        iam_app.addEnvironmentOverrideAttribute("tomcat", "{\n" +
            "      \"home\": \"{{tomcat_home}}\"\n" +
            "    }");
        recipeService.save(iam_app);
        Recipe iam_web = new Recipe("iam-web");
        iam_web.addEndpoint(new EndpointTemplate("SHIB"));

        Recipe iam_opendj = new Recipe("iam-opendj");
        iam_opendj.addEndpoint(new EndpointTemplate("OPEN_DJ"));
        recipeService.save(iam_opendj);
        Recipe iam_mysql = new Recipe("iam-mysql");
        iam_mysql.addEndpoint(new EndpointTemplate("IAM_MYSQL"));
        recipeService.save(iam_mysql);
        Recipe rf_search_app = new Recipe("rf-search-app");
        rf_search_app.addEndpoint(new EndpointTemplate("RF_SEARCH"));
        recipeService.save(rf_search_app);
/*        "realsearch": {
            "ha_installation": true,
                    "iam": {
                "host": "rfiqa-iam-app.ngnpr.den.vz.altidev.net",
                        "search_query": "machinename:rfiqa-iam-app.ngnpr.den.vz.altidev.net",
                        "protocol": "http",
                        "port": "8080",
                        "api": {
                    "version": "v3"
                },
                "authz": {
                    "method": "uid"
                }
            },
            "drservice": {
                "scheduledBackup": false,
                        "backupCronSchedule": "0 0 * * * *",
                        "scheduledRestore": false,
                        "restoreCronSchedule": "0 0 * * * *",
                        "repositoryLocation": "/mnt/es-backup"
            }
        }
        */
        VirtualMachineTemplate t1 = createTemplate("IAM-APP", tomcat, iam_app);

        VirtualMachineTemplate t2 = createTemplate("IAM-MySQL", iam_mysql);
//        t2.setFrom(t1);
        VirtualMachineTemplate t3 = createTemplate("IAM-OpenDJ", iam_opendj);
        virtualMachineTemplateRepository.saveAll(Arrays.asList(
            t1, t2, t3));

        ApplicationTemplate app = new ApplicationTemplate();
        app.setName("app1");
        ExternalServiceTemplate externalService = new ExternalServiceTemplate();
        externalService.setName("external");
        externalService.setDescription("http://example.com:7777");
        ExternalServiceTemplate externalService1 = new ExternalServiceTemplate();
        externalService1.setName("other service");
        externalService1.setDescription("http://acme.com:3306");
        app.addChild(externalService);
        app.addChild(externalService1);

        ComponentGroupTemplate cg = new ComponentGroupTemplate();
        cg.setVirtualMachineTemplate(t1);
        cg.setName("group1");
        HAProxyBackendConfig hpbc = new HAProxyBackendConfig();
        hpbc.setName("backend1");
        hpbc.setConfig(Arrays.asList("str1", "str2"));
        cg.setHaProxyBackendConfig(hpbc);
        ProducedServiceTemplate srv = new ProducedServiceTemplate();
        srv.setComponentGroups(Arrays.asList(cg));
        srv.setName("service1");
        HAProxyFrontendConfig hpfc = new HAProxyFrontendConfig();
        hpfc.setName("frontend1");
        hpfc.setConfig(Arrays.asList("str1", "str2"));
        srv.setHaProxyFrontendConfig(hpfc);
        app.addChild(srv);
        applicationTemplateService.saveApp(app);
        appDeploymentService.save(appDeploymentService.create(app, Infrastructure.VERIZON.name()));
    }

    private VirtualMachineTemplate createTemplate(String name, Recipe... recipes) {
        VirtualMachineTemplate t = new VirtualMachineTemplate();
        t.setName(name);
        for (Recipe r : recipes) {
            t.addRecipe(r);
        }
        return t;
    }
}
