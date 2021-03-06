//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911.1802 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2015.07.16 at 03:24:22 PM EEST 
//


package org.ecloudmanager.tmrk.cloudapi.model;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.*;


/**
 * <p>Java class for LinuxCustomizationType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="LinuxCustomizationType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="NetworkSettings" type="{}NetworkSettingsType"/>
 *         &lt;element name="SshKey" type="{}ReferenceType" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "LinuxCustomizationType", propOrder = {
    "networkSettings",
    "sshKey"
})
public class LinuxCustomizationType {

    @XmlElement(name = "NetworkSettings", required = true, nillable = true)
    protected NetworkSettingsType networkSettings;
    @XmlElementRef(name = "SshKey", type = JAXBElement.class, required = false)
    protected JAXBElement<ReferenceType> sshKey;

    /**
     * Gets the value of the networkSettings property.
     * 
     * @return
     *     possible object is
     *     {@link NetworkSettingsType }
     *     
     */
    public NetworkSettingsType getNetworkSettings() {
        return networkSettings;
    }

    /**
     * Sets the value of the networkSettings property.
     * 
     * @param value
     *     allowed object is
     *     {@link NetworkSettingsType }
     *     
     */
    public void setNetworkSettings(NetworkSettingsType value) {
        this.networkSettings = value;
    }

    /**
     * Gets the value of the sshKey property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link ReferenceType }{@code >}
     *     
     */
    public JAXBElement<ReferenceType> getSshKey() {
        return sshKey;
    }

    /**
     * Sets the value of the sshKey property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link ReferenceType }{@code >}
     *     
     */
    public void setSshKey(JAXBElement<ReferenceType> value) {
        this.sshKey = value;
    }

}
