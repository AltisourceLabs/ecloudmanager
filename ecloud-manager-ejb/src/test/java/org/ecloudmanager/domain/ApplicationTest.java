///*
// * MIT License
// *
// * Copyright (c) 2016  Altisource
// *
// * Permission is hereby granted, free of charge, to any person obtaining a copy
// * of this software and associated documentation files (the "Software"), to deal
// * in the Software without restriction, including without limitation the rights
// * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
// * copies of the Software, and to permit persons to whom the Software is
// * furnished to do so, subject to the following conditions:
// *
// * The above copyright notice and this permission notice shall be included in all
// * copies or substantial portions of the Software.
// *
// * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
// * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
// * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
// * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
// * SOFTWARE.
// */
//
//package org.ecloudmanager.domain;
//
//import org.bson.types.ObjectId;
//import org.ecloudmanager.deployment.app.ApplicationDeployment;
//import org.ecloudmanager.deployment.app.ApplicationTemplate;
//import org.ecloudmanager.deployment.core.DeploymentObject;
//import org.ecloudmanager.deployment.core.Endpoint;
//import org.ecloudmanager.deployment.vm.infrastructure.Infrastructure;
//import org.ecloudmanager.deployment.vm.provisioning.Recipe;
//import org.ecloudmanager.repository.deployment.ApplicationDeploymentRepository;
//import org.ecloudmanager.repository.template.ApplicationTemplateRepository;
//import org.ecloudmanager.repository.template.RecipeRepository;
//import org.ecloudmanager.service.deployment.ApplicationDeploymentService;
//import org.ecloudmanager.service.template.ApplicationTemplateService;
//import org.ecloudmanager.service.template.RecipeService;
//import org.jboss.arquillian.container.test.api.Deployment;
//import org.jboss.arquillian.junit.Arquillian;
//import org.jboss.shrinkwrap.api.ShrinkWrap;
//import org.jboss.shrinkwrap.api.asset.EmptyAsset;
//import org.jboss.shrinkwrap.api.spec.WebArchive;
//import org.jboss.shrinkwrap.resolver.api.maven.Maven;
//import org.junit.Test;
//import org.junit.runner.RunWith;
//
//import javax.inject.Inject;
//import java.io.File;
//
//import static org.junit.Assert.assertFalse;
//import static org.junit.Assert.assertNotNull;
//
//@RunWith(Arquillian.class)
//public class ApplicationTest {
//
//    @Inject
//    private ApplicationTemplateRepository applicationRepository;
//
//    @Inject
//    private ApplicationDeploymentRepository applicationDeploymentRepository;
//
//    @Inject
//    private ApplicationTemplateService testBean;
//
//    @Inject
//    private ApplicationDeploymentService applicationDeploymentService;
//    @Inject
//    private RecipeRepository recipeRepository;
//    @Inject
//    private RecipeService recipeService;
//
//    @Deployment
//    public static WebArchive createDeployment() {
//        File[] pomLibs = Maven.resolver().loadPomFromFile("pom.xml").resolve(
//            "org.apache.commons:commons-lang3",
//            "org.mongodb.morphia:morphia",
//            "com.github.fakemongo:fongo",
//            "com.google.guava:guava",
//            "com.github.spullara.mustache.java:compiler",
//            "org.apache.jclouds.api:chef",
//            "org.ecloudmanager:tmrk-cloudapi",
//            "uk.com.robust-it:cloning",
//            "com.jcraft:jsch",
//                "org.picketlink:picketlink",
//                "org.picketlink:picketlink-idm-simple-schema",
//
//            "org.apache.logging.log4j:log4j-api",
//            "org.apache.logging.log4j:log4j-core",
//            "org.apache.logging.log4j:log4j-nosql",
//
//            "com.amazonaws:aws-java-sdk-ec2",
//            "com.amazonaws:aws-java-sdk-s3",
//            "com.amazonaws:aws-java-sdk-iam",
//            "com.amazonaws:aws-java-sdk-iam",
//            "com.amazonaws:aws-java-sdk-route53",
//            "com.fasterxml.jackson.dataformat:jackson-dataformat-yaml")
//            .withTransitivity().as(File.class);
//
//        return ShrinkWrap.create(WebArchive.class, "test.war").addPackages(true,
//            "org.ecloudmanager")
//                .addAsWebInfResource("persistence.xml")
//            .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml").addAsLibraries(pomLibs);
//    }
//
//    @Test
//    public final void testAppSave() throws Exception {
//        ApplicationTemplate app = new ApplicationTemplate();
//        testBean.saveApp(app);
//        ObjectId id = app.getId();
//        ApplicationTemplate fromDB = applicationRepository.get(id);
//        assertNotNull(fromDB);
//    }
//
//    @Test
//    public final void testAppDeploymentSave() throws Exception {
//        ApplicationTemplate app = new ApplicationTemplate();
//        ApplicationDeployment applicationDeployment = applicationDeploymentService.create(app,
//            Infrastructure.VERIZON.name());
//        applicationDeploymentService.save(applicationDeployment);
//        ObjectId id = applicationDeployment.getId();
//        DeploymentObject fromDB = applicationDeploymentRepository.get(id);
//        assertNotNull(fromDB);
//    }
//
//    @Test
//    public final void testRecipeSave() throws Exception {
//        String LS = System.getProperty("line.separator");
//        Recipe iam_app = new Recipe("rf-iam-app");
//        iam_app.setVersion("= 2.9.0");
//        iam_app.addEndpoint(new Endpoint("IAM_APP"));
//
//        iam_app.addEnvironmentOverrideAttribute("iam_public_hostname", "${SHIB:fqdn}");
//        iam_app.addEnvironmentOverrideAttribute("iam_app_host", "${:fqdn}");
//        iam_app.addEnvironmentOverrideAttribute("iam_opendj_host", "${OPEN_DJ:fqdn}");
//        iam_app.addEnvironmentOverrideAttribute("iam_mysql_host", "${IAM_MYSQL:fqdn}");
//        iam_app.addEnvironmentOverrideAttribute("rf_search_host", "${RF_SEARCH:fqdn}");
//        iam_app.addEnvironmentOverrideAttribute("rf_indexing_host", "${RF_INDEXING:fqdn}");
//        iam_app.addEnvironmentOverrideAttribute("rf_smtp_server_host", "${SMTP:fqdn}");
//        iam_app.addEnvironmentOverrideAttribute("rf_smtp_server_port", "${SMTP:port}");
//        iam_app.addEnvironmentOverrideAttribute("tomcat", "{" + LS +
//            "      \"home\": \"/opt/tomcat\"" + LS +
//            "    }");
//        iam_app.addEnvironmentOverrideAttribute("iam", "{" + LS +
//            "      \"rpm\": {" + LS +
//            "       \"version\": \"${iam_rpm_version}\" " + LS +
//            "       }" + LS +
//            "    }");
//
//
//        recipeService.save(iam_app);
//
//        Recipe fromDB = recipeRepository.get("rf-iam-app");
//        assertNotNull(fromDB);
//        assertFalse(fromDB.isNew());
//    }
//
//
//}
//
