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
 * <p>Java class for LayoutReferenceType complex type.
 * <p/>
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p/>
 * <pre>
 * &lt;complexType name="LayoutReferenceType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="Row" type="{}ReferenceType" minOccurs="0"/>
 *         &lt;element name="Group" type="{}ReferenceType" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "LayoutReferenceType", propOrder = {
    "row",
    "group"
})
public class LayoutReferenceType {

    @XmlElementRef(name = "Row", type = JAXBElement.class, required = false)
    protected JAXBElement<ReferenceType> row;
    @XmlElementRef(name = "Group", type = JAXBElement.class, required = false)
    protected JAXBElement<ReferenceType> group;

    /**
     * Gets the value of the row property.
     *
     * @return possible object is
     * {@link JAXBElement }{@code <}{@link ReferenceType }{@code >}
     */
    public JAXBElement<ReferenceType> getRow() {
        return row;
    }

    /**
     * Sets the value of the row property.
     *
     * @param value allowed object is
     *              {@link JAXBElement }{@code <}{@link ReferenceType }{@code >}
     */
    public void setRow(JAXBElement<ReferenceType> value) {
        this.row = value;
    }

    /**
     * Gets the value of the group property.
     *
     * @return possible object is
     * {@link JAXBElement }{@code <}{@link ReferenceType }{@code >}
     */
    public JAXBElement<ReferenceType> getGroup() {
        return group;
    }

    /**
     * Sets the value of the group property.
     *
     * @param value allowed object is
     *              {@link JAXBElement }{@code <}{@link ReferenceType }{@code >}
     */
    public void setGroup(JAXBElement<ReferenceType> value) {
        this.group = value;
    }

}
