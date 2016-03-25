//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911
// .1802
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2015.07.16 at 03:24:22 PM EEST 
//


package org.ecloudmanager.tmrk.cloudapi.model;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for OperatingSystemFamilyType complex type.
 * <p/>
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p/>
 * <pre>
 * &lt;complexType name="OperatingSystemFamilyType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="Name" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="OperatingSystems" type="{}OperatingSystemReferencesType" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "OperatingSystemFamilyType", propOrder = {
    "name",
    "operatingSystems"
})
public class OperatingSystemFamilyType {

    @XmlElementRef(name = "Name", type = JAXBElement.class, required = false)
    protected JAXBElement<String> name;
    @XmlElementRef(name = "OperatingSystems", type = JAXBElement.class, required = false)
    protected JAXBElement<OperatingSystemReferencesType> operatingSystems;

    /**
     * Gets the value of the name property.
     *
     * @return possible object is
     * {@link JAXBElement }{@code <}{@link String }{@code >}
     */
    public JAXBElement<String> getName() {
        return name;
    }

    /**
     * Sets the value of the name property.
     *
     * @param value allowed object is
     *              {@link JAXBElement }{@code <}{@link String }{@code >}
     */
    public void setName(JAXBElement<String> value) {
        this.name = value;
    }

    /**
     * Gets the value of the operatingSystems property.
     *
     * @return possible object is
     * {@link JAXBElement }{@code <}{@link OperatingSystemReferencesType }{@code >}
     */
    public JAXBElement<OperatingSystemReferencesType> getOperatingSystems() {
        return operatingSystems;
    }

    /**
     * Sets the value of the operatingSystems property.
     *
     * @param value allowed object is
     *              {@link JAXBElement }{@code <}{@link OperatingSystemReferencesType }{@code >}
     */
    public void setOperatingSystems(JAXBElement<OperatingSystemReferencesType> value) {
        this.operatingSystems = value;
    }

}
