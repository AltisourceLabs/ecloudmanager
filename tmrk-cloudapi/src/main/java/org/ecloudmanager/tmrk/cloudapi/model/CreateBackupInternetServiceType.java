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
 * <p>Java class for CreateBackupInternetServiceType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CreateBackupInternetServiceType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="Protocol" type="{}ProtocolTypeEnum"/>
 *         &lt;element name="Enabled" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *         &lt;element name="Description" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="Persistence" type="{}InternetServicePersistenceType" minOccurs="0"/>
 *         &lt;element name="RedirectUrl" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="LoadBalancingMethod" type="{}LoadBalancingMethod" minOccurs="0"/>
 *         &lt;element name="IsIPv4" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="name" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CreateBackupInternetServiceType", propOrder = {
    "protocol",
    "enabled",
    "description",
    "persistence",
    "redirectUrl",
    "loadBalancingMethod",
    "isIPv4"
})
public class CreateBackupInternetServiceType {

    @XmlElement(name = "Protocol", required = true)
    @XmlSchemaType(name = "string")
    protected ProtocolTypeEnum protocol;
    @XmlElement(name = "Enabled")
    protected boolean enabled;
    @XmlElementRef(name = "Description", type = JAXBElement.class, required = false)
    protected JAXBElement<String> description;
    @XmlElementRef(name = "Persistence", type = JAXBElement.class, required = false)
    protected JAXBElement<InternetServicePersistenceType> persistence;
    @XmlElementRef(name = "RedirectUrl", type = JAXBElement.class, required = false)
    protected JAXBElement<String> redirectUrl;
    @XmlElementRef(name = "LoadBalancingMethod", type = JAXBElement.class, required = false)
    protected JAXBElement<LoadBalancingMethod> loadBalancingMethod;
    @XmlElementRef(name = "IsIPv4", type = JAXBElement.class, required = false)
    protected JAXBElement<Boolean> isIPv4;
    @XmlAttribute(name = "name", required = true)
    protected String name;

    /**
     * Gets the value of the protocol property.
     * 
     * @return
     *     possible object is
     *     {@link ProtocolTypeEnum }
     *     
     */
    public ProtocolTypeEnum getProtocol() {
        return protocol;
    }

    /**
     * Sets the value of the protocol property.
     * 
     * @param value
     *     allowed object is
     *     {@link ProtocolTypeEnum }
     *     
     */
    public void setProtocol(ProtocolTypeEnum value) {
        this.protocol = value;
    }

    /**
     * Gets the value of the enabled property.
     * 
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Sets the value of the enabled property.
     * 
     */
    public void setEnabled(boolean value) {
        this.enabled = value;
    }

    /**
     * Gets the value of the description property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public JAXBElement<String> getDescription() {
        return description;
    }

    /**
     * Sets the value of the description property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public void setDescription(JAXBElement<String> value) {
        this.description = value;
    }

    /**
     * Gets the value of the persistence property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link InternetServicePersistenceType }{@code >}
     *     
     */
    public JAXBElement<InternetServicePersistenceType> getPersistence() {
        return persistence;
    }

    /**
     * Sets the value of the persistence property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link InternetServicePersistenceType }{@code >}
     *     
     */
    public void setPersistence(JAXBElement<InternetServicePersistenceType> value) {
        this.persistence = value;
    }

    /**
     * Gets the value of the redirectUrl property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public JAXBElement<String> getRedirectUrl() {
        return redirectUrl;
    }

    /**
     * Sets the value of the redirectUrl property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public void setRedirectUrl(JAXBElement<String> value) {
        this.redirectUrl = value;
    }

    /**
     * Gets the value of the loadBalancingMethod property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link LoadBalancingMethod }{@code >}
     *     
     */
    public JAXBElement<LoadBalancingMethod> getLoadBalancingMethod() {
        return loadBalancingMethod;
    }

    /**
     * Sets the value of the loadBalancingMethod property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link LoadBalancingMethod }{@code >}
     *     
     */
    public void setLoadBalancingMethod(JAXBElement<LoadBalancingMethod> value) {
        this.loadBalancingMethod = value;
    }

    /**
     * Gets the value of the isIPv4 property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link Boolean }{@code >}
     *     
     */
    public JAXBElement<Boolean> getIsIPv4() {
        return isIPv4;
    }

    /**
     * Sets the value of the isIPv4 property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link Boolean }{@code >}
     *     
     */
    public void setIsIPv4(JAXBElement<Boolean> value) {
        this.isIPv4 = value;
    }

    /**
     * Gets the value of the name property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the value of the name property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setName(String value) {
        this.name = value;
    }

}
