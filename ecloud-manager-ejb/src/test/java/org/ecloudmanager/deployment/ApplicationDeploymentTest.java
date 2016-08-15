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
//package org.ecloudmanager.deployment;
//
//import org.ecloudmanager.deployment.app.ApplicationDeployment;
//import org.ecloudmanager.deployment.app.ApplicationTemplate;
//import org.ecloudmanager.deployment.core.ConstraintField;
//import org.ecloudmanager.deployment.core.ConstraintValue;
//import org.ecloudmanager.deployment.es.ExternalServiceTemplate;
//import org.ecloudmanager.deployment.ps.ProducedServiceTemplate;
//import org.junit.Before;
//import org.junit.Ignore;
//import org.junit.Test;
//
//import static org.junit.Assert.*;
//
//@SuppressWarnings("ALL")
//public class ApplicationDeploymentTest {
//    ApplicationDeployment applicationDeployment;
//
//    @Before
//    public final void setUp() {
//        ApplicationTemplate app = new ApplicationTemplate("test-app");
//        app.addChild(new ProducedServiceTemplate("iamapp"));
//        app.addChild(new ProducedServiceTemplate("iammysql"));
//        app.addChild(new ExternalServiceTemplate("realsearch"));
//
//        applicationDeployment = app.toDeployment();
//        applicationDeployment.addField(ConstraintField.builder().name("field1").description("First Field").build());
//
//        applicationDeployment.getChildByName("iamapp").addField(ConstraintField.builder().name("urls").description
//            ("Service urls").build());
//        applicationDeployment.createIfMissingAndGetConfig("cfg-ad")
//            .createIfMissingAndGetConfig("cfg-child")
//            .addField(ConstraintField.builder().name("cfgfield").description("First Field").build());
//        applicationDeployment.getChildByName("iamapp").createIfMissingAndGetConfig("cfg-iam").setExtendedConfig
//            (applicationDeployment
//                .createIfMissingAndGetConfig("cfg-ad"));
//        applicationDeployment.getChildByName("iamapp").createIfMissingAndGetConfig("cfg-iam")
//            .createIfMissingAndGetConfig("cfg-child");
//
//        applicationDeployment.getChildByName("iamapp").addField(ConstraintField.builder().name("urls").description
//            ("Service urls").build());
//
//        applicationDeployment.getChildByName("iammysql").addField(ConstraintField.builder().name("property")
//            .description("property").build());
//        applicationDeployment.getChildByName("realsearch").addField(ConstraintField.builder().name("property")
//            .description("property").build());
//        applicationDeployment.addField(ConstraintField.builder().name("/some/property").description("iam app2 " +
//            "property").build());
//    }
//
//    @Test
//    @Ignore
//    public final void testConstraintLocation() throws Exception {
//        assertNotNull(applicationDeployment.getConstraintField("field1"));
//        assertNotNull(applicationDeployment.getConstraintField("some/property"));
//        assertNotNull(applicationDeployment.getChildByName("iamapp").getConstraintField("urls"));
//        assertNotNull(applicationDeployment.getChildByName("realsearch").getConstraintField("property"));
//        assertNull(applicationDeployment.getConstraintField("urls"));
//        assertNull(applicationDeployment.getConstraintField("/iamapp/urls"));
//    }
//
//    @Test
//    public final void testConstraintValue() throws Exception {
//        applicationDeployment.setValue("field1", ConstraintValue.value("value1"));
//        assertEquals("value1", applicationDeployment.getConfigValue("field1"));
//
//        assertEquals("value1", applicationDeployment.getChildByName("iamapp").getConfigValue("../field1"));
//
//        applicationDeployment.setValue("some/property", ConstraintValue.value("value2"));
//        assertEquals("value2", applicationDeployment.getConfigValue("some/property"));
//
//
//        applicationDeployment.getChildByName("iamapp").setValue("urls", ConstraintValue.value("value3"));
//        assertEquals("value3", applicationDeployment.getConfigValue("/iamapp/urls"));
//
//        applicationDeployment.getChildByName("realsearch")
//            .setValue("property", ConstraintValue.reference("/iamapp/urls"));
//        assertEquals("value3", applicationDeployment.getConfigValue("/realsearch/property"));
//
//        applicationDeployment.getChildByName("iamapp").setValue("urls", ConstraintValue.value("value4"));
//        assertEquals("value4", applicationDeployment.getConfigValue("/realsearch/property"));
//
//    }
//
//    @Test
//    public final void testRelativePath() throws Exception {
//        assertEquals("", applicationDeployment.relativePathTo(applicationDeployment));
//        assertEquals("iamapp/", applicationDeployment.relativePathTo(applicationDeployment.getChildByName("iamapp")));
//        assertEquals(null, applicationDeployment.relativePathTo(applicationDeployment.getChildByName("iamapp")
//            .getChildByName("iamapp")));
//        assertEquals("iamapp/cfg-iam/", applicationDeployment.relativePathTo(applicationDeployment.getChildByName
//            ("iamapp").getChildByName("cfg-iam")));
//        assertEquals("../../", applicationDeployment.getChildByName("iamapp").getChildByName("cfg-iam")
//            .relativePathTo(applicationDeployment));
//        assertEquals("../realsearch/", applicationDeployment.getChildByName("iamapp").relativePathTo
//            (applicationDeployment.getChildByName("realsearch")));
//
//    }
//
//    @Test
//    public final void testConstraintInheritance() throws Exception {
//        applicationDeployment.getChildByName("cfg-ad").getChildByName("cfg-child").setValue("cfgfield",
//            ConstraintValue.value("foo"));
//        applicationDeployment.getChildByName("cfg-ad").addField("xyz", "");
//        applicationDeployment.getChildByName("cfg-ad").setValue("xyz", ConstraintValue.value("zyx"));
//        assertEquals("zyx", applicationDeployment.getChildByName("cfg-ad").getConfigValue("xyz"));
//        assertEquals("zyx", applicationDeployment.getChildByName("iamapp").getChildByName("cfg-iam").getConfigValue
//            ("xyz"));
//
//        applicationDeployment.getChildByName("cfg-ad").setValue("xyz", ConstraintValue.value("zyx"));
//        assertEquals("zyx", applicationDeployment.getChildByName("cfg-ad").getConfigValue("xyz"));
//        assertEquals("zyx", applicationDeployment.getChildByName("iamapp").getChildByName("cfg-iam").getConfigValue
//            ("xyz"));
//
//
//        assertEquals("foo", applicationDeployment.getChildByName("cfg-ad").getChildByName("cfg-child").getConfigValue
//            ("cfgfield"));
////        assertEquals("foo", applicationDeployment.getChildByName("iamapp").getChildByName("cfg-iam")
////            .getChildByName("cfg-child").getConfigValue("cfgfield"));
//
//        assertEquals("foo", applicationDeployment.getConfigValue("/cfg-ad/cfg-child/cfgfield"));
//        assertEquals("zyx", applicationDeployment.getConfigValue("/cfg-ad/xyz"));
//        assertEquals("zyx", applicationDeployment.getConfigValue("cfg-ad/xyz"));
//        assertEquals("zyx", applicationDeployment.getChildByName("iamapp").getConfigValue("cfg-iam/xyz"));
//        assertEquals("zyx", applicationDeployment.getChildByName("iamapp").getConfigValue("cfg-iam/xyz"));
//
//
//    }
//
//}