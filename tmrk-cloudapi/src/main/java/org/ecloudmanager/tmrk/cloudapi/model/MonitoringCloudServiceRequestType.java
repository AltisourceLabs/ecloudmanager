//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911
// .1802
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2015.07.16 at 03:24:22 PM EEST 
//


package org.ecloudmanager.tmrk.cloudapi.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for MonitoringCloudServiceRequestType complex type.
 * <p/>
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p/>
 * <pre>
 * &lt;complexType name="MonitoringCloudServiceRequestType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="IsEnabled" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *         &lt;element name="CloudServicePackage" type="{}ReferenceType"/>
 *         &lt;element name="MonitoringCloudServiceDeviceConfiguration"
 *         type="{}MonitoringCloudServiceDeviceConfigurationType"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "MonitoringCloudServiceRequestType", propOrder = {
    "isEnabled",
    "cloudServicePackage",
    "monitoringCloudServiceDeviceConfiguration"
})
public class MonitoringCloudServiceRequestType {

    @XmlElement(name = "IsEnabled")
    protected boolean isEnabled;
    @XmlElement(name = "CloudServicePackage", required = true, nillable = true)
    protected ReferenceType cloudServicePackage;
    @XmlElement(name = "MonitoringCloudServiceDeviceConfiguration", required = true, nillable = true)
    protected MonitoringCloudServiceDeviceConfigurationType monitoringCloudServiceDeviceConfiguration;

    /**
     * Gets the value of the isEnabled property.
     */
    public boolean isIsEnabled() {
        return isEnabled;
    }

    /**
     * Sets the value of the isEnabled property.
     */
    public void setIsEnabled(boolean value) {
        this.isEnabled = value;
    }

    /**
     * Gets the value of the cloudServicePackage property.
     *
     * @return possible object is
     * {@link ReferenceType }
     */
    public ReferenceType getCloudServicePackage() {
        return cloudServicePackage;
    }

    /**
     * Sets the value of the cloudServicePackage property.
     *
     * @param value allowed object is
     *              {@link ReferenceType }
     */
    public void setCloudServicePackage(ReferenceType value) {
        this.cloudServicePackage = value;
    }

    /**
     * Gets the value of the monitoringCloudServiceDeviceConfiguration property.
     *
     * @return possible object is
     * {@link MonitoringCloudServiceDeviceConfigurationType }
     */
    public MonitoringCloudServiceDeviceConfigurationType getMonitoringCloudServiceDeviceConfiguration() {
        return monitoringCloudServiceDeviceConfiguration;
    }

    /**
     * Sets the value of the monitoringCloudServiceDeviceConfiguration property.
     *
     * @param value allowed object is
     *              {@link MonitoringCloudServiceDeviceConfigurationType }
     */
    public void setMonitoringCloudServiceDeviceConfiguration(MonitoringCloudServiceDeviceConfigurationType value) {
        this.monitoringCloudServiceDeviceConfiguration = value;
    }

}
