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


/**
 * <p>Java class for SecurityGroupReferenceType complex type.
 * <p/>
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p/>
 * <pre>
 * &lt;complexType name="SecurityGroupReferenceType">
 *   &lt;complexContent>
 *     &lt;extension base="{}ReferenceType">
 *       &lt;sequence>
 *         &lt;element name="Environment" type="{}ReferenceType" minOccurs="0"/>
 *         &lt;element name="Active" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SecurityGroupReferenceType", propOrder = {
    "environment",
    "active"
})
public class SecurityGroupReferenceType
    extends ReferenceType {

    /**
     *
     */
    private static final long serialVersionUID = 8581325347874931978L;
    @XmlElementRef(name = "Environment", type = JAXBElement.class, required = false)
    protected JAXBElement<ReferenceType> environment;
    @XmlElement(name = "Active")
    protected Boolean active;

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
     * Gets the value of the active property.
     *
     * @return possible object is
     * {@link Boolean }
     */
    public Boolean isActive() {
        return active;
    }

    /**
     * Sets the value of the active property.
     *
     * @param value allowed object is
     *              {@link Boolean }
     */
    public void setActive(Boolean value) {
        this.active = value;
    }

}
