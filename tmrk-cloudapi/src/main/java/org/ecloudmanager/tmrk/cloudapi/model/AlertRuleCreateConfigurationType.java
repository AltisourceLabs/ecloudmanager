//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911
// .1802
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2015.07.16 at 03:24:22 PM EEST 
//


package org.ecloudmanager.tmrk.cloudapi.model;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.*;
import java.math.BigDecimal;


/**
 * <p>Java class for AlertRuleCreateConfigurationType complex type.
 * <p/>
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p/>
 * <pre>
 * &lt;complexType name="AlertRuleCreateConfigurationType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="AlertRuleTemplate" type="{}ReferenceType" minOccurs="0"/>
 *         &lt;element name="RuleGroup" type="{}AlertRuleGroup" minOccurs="0"/>
 *         &lt;element name="Threshold" type="{}ThresholdType" minOccurs="0"/>
 *         &lt;element name="RuleAction" type="{}AlertRuleAction" minOccurs="0"/>
 *         &lt;element name="Configuration" type="{}ConfigurationType" minOccurs="0"/>
 *         &lt;element name="AlertFrequency" type="{}AlertFrequency" minOccurs="0"/>
 *         &lt;element name="BudgetAmount" type="{http://www.w3.org/2001/XMLSchema}decimal" minOccurs="0"/>
 *         &lt;element name="Environment" type="{}ReferenceType" minOccurs="0"/>
 *         &lt;element name="ComputePool" type="{}ReferenceType" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "AlertRuleCreateConfigurationType", propOrder = {
    "alertRuleTemplate",
    "ruleGroup",
    "threshold",
    "ruleAction",
    "configuration",
    "alertFrequency",
    "budgetAmount",
    "environment",
    "computePool"
})
public class AlertRuleCreateConfigurationType {

    @XmlElementRef(name = "AlertRuleTemplate", type = JAXBElement.class, required = false)
    protected JAXBElement<ReferenceType> alertRuleTemplate;
    @XmlElement(name = "RuleGroup")
    @XmlSchemaType(name = "string")
    protected AlertRuleGroup ruleGroup;
    @XmlElementRef(name = "Threshold", type = JAXBElement.class, required = false)
    protected JAXBElement<ThresholdType> threshold;
    @XmlElement(name = "RuleAction")
    @XmlSchemaType(name = "string")
    protected AlertRuleAction ruleAction;
    @XmlElementRef(name = "Configuration", type = JAXBElement.class, required = false)
    protected JAXBElement<ConfigurationType> configuration;
    @XmlElement(name = "AlertFrequency")
    @XmlSchemaType(name = "string")
    protected AlertFrequency alertFrequency;
    @XmlElementRef(name = "BudgetAmount", type = JAXBElement.class, required = false)
    protected JAXBElement<BigDecimal> budgetAmount;
    @XmlElementRef(name = "Environment", type = JAXBElement.class, required = false)
    protected JAXBElement<ReferenceType> environment;
    @XmlElementRef(name = "ComputePool", type = JAXBElement.class, required = false)
    protected JAXBElement<ReferenceType> computePool;

    /**
     * Gets the value of the alertRuleTemplate property.
     *
     * @return possible object is
     * {@link JAXBElement }{@code <}{@link ReferenceType }{@code >}
     */
    public JAXBElement<ReferenceType> getAlertRuleTemplate() {
        return alertRuleTemplate;
    }

    /**
     * Sets the value of the alertRuleTemplate property.
     *
     * @param value allowed object is
     *              {@link JAXBElement }{@code <}{@link ReferenceType }{@code >}
     */
    public void setAlertRuleTemplate(JAXBElement<ReferenceType> value) {
        this.alertRuleTemplate = value;
    }

    /**
     * Gets the value of the ruleGroup property.
     *
     * @return possible object is
     * {@link AlertRuleGroup }
     */
    public AlertRuleGroup getRuleGroup() {
        return ruleGroup;
    }

    /**
     * Sets the value of the ruleGroup property.
     *
     * @param value allowed object is
     *              {@link AlertRuleGroup }
     */
    public void setRuleGroup(AlertRuleGroup value) {
        this.ruleGroup = value;
    }

    /**
     * Gets the value of the threshold property.
     *
     * @return possible object is
     * {@link JAXBElement }{@code <}{@link ThresholdType }{@code >}
     */
    public JAXBElement<ThresholdType> getThreshold() {
        return threshold;
    }

    /**
     * Sets the value of the threshold property.
     *
     * @param value allowed object is
     *              {@link JAXBElement }{@code <}{@link ThresholdType }{@code >}
     */
    public void setThreshold(JAXBElement<ThresholdType> value) {
        this.threshold = value;
    }

    /**
     * Gets the value of the ruleAction property.
     *
     * @return possible object is
     * {@link AlertRuleAction }
     */
    public AlertRuleAction getRuleAction() {
        return ruleAction;
    }

    /**
     * Sets the value of the ruleAction property.
     *
     * @param value allowed object is
     *              {@link AlertRuleAction }
     */
    public void setRuleAction(AlertRuleAction value) {
        this.ruleAction = value;
    }

    /**
     * Gets the value of the configuration property.
     *
     * @return possible object is
     * {@link JAXBElement }{@code <}{@link ConfigurationType }{@code >}
     */
    public JAXBElement<ConfigurationType> getConfiguration() {
        return configuration;
    }

    /**
     * Sets the value of the configuration property.
     *
     * @param value allowed object is
     *              {@link JAXBElement }{@code <}{@link ConfigurationType }{@code >}
     */
    public void setConfiguration(JAXBElement<ConfigurationType> value) {
        this.configuration = value;
    }

    /**
     * Gets the value of the alertFrequency property.
     *
     * @return possible object is
     * {@link AlertFrequency }
     */
    public AlertFrequency getAlertFrequency() {
        return alertFrequency;
    }

    /**
     * Sets the value of the alertFrequency property.
     *
     * @param value allowed object is
     *              {@link AlertFrequency }
     */
    public void setAlertFrequency(AlertFrequency value) {
        this.alertFrequency = value;
    }

    /**
     * Gets the value of the budgetAmount property.
     *
     * @return possible object is
     * {@link JAXBElement }{@code <}{@link BigDecimal }{@code >}
     */
    public JAXBElement<BigDecimal> getBudgetAmount() {
        return budgetAmount;
    }

    /**
     * Sets the value of the budgetAmount property.
     *
     * @param value allowed object is
     *              {@link JAXBElement }{@code <}{@link BigDecimal }{@code >}
     */
    public void setBudgetAmount(JAXBElement<BigDecimal> value) {
        this.budgetAmount = value;
    }

    /**
     * Gets the value of the environment property.
     *
     * @return possible object is
     * {@link JAXBElement }{@code <}{@link ReferenceType }{@code >}
     */
    public JAXBElement<ReferenceType> getEnvironment() {
        return environment;
    }

    /**
     * Sets the value of the environment property.
     *
     * @param value allowed object is
     *              {@link JAXBElement }{@code <}{@link ReferenceType }{@code >}
     */
    public void setEnvironment(JAXBElement<ReferenceType> value) {
        this.environment = value;
    }

    /**
     * Gets the value of the computePool property.
     *
     * @return possible object is
     * {@link JAXBElement }{@code <}{@link ReferenceType }{@code >}
     */
    public JAXBElement<ReferenceType> getComputePool() {
        return computePool;
    }

    /**
     * Sets the value of the computePool property.
     *
     * @param value allowed object is
     *              {@link JAXBElement }{@code <}{@link ReferenceType }{@code >}
     */
    public void setComputePool(JAXBElement<ReferenceType> value) {
        this.computePool = value;
    }

}
