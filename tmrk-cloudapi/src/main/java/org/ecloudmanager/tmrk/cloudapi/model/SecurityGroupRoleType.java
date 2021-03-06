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
 * <p>Java class for SecurityGroupRoleType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="SecurityGroupRoleType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="SecurityGroup" type="{}ReferenceType"/>
 *         &lt;element name="Role" type="{}ReferenceType" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SecurityGroupRoleType", propOrder = {
    "securityGroup",
    "role"
})
public class SecurityGroupRoleType {

    @XmlElement(name = "SecurityGroup", required = true, nillable = true)
    protected ReferenceType securityGroup;
    @XmlElementRef(name = "Role", type = JAXBElement.class, required = false)
    protected JAXBElement<ReferenceType> role;

    /**
     * Gets the value of the securityGroup property.
     * 
     * @return
     *     possible object is
     *     {@link ReferenceType }
     *     
     */
    public ReferenceType getSecurityGroup() {
        return securityGroup;
    }

    /**
     * Sets the value of the securityGroup property.
     * 
     * @param value
     *     allowed object is
     *     {@link ReferenceType }
     *     
     */
    public void setSecurityGroup(ReferenceType value) {
        this.securityGroup = value;
    }

    /**
     * Gets the value of the role property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link ReferenceType }{@code >}
     *     
     */
    public JAXBElement<ReferenceType> getRole() {
        return role;
    }

    /**
     * Sets the value of the role property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link ReferenceType }{@code >}
     *     
     */
    public void setRole(JAXBElement<ReferenceType> value) {
        this.role = value;
    }

}
