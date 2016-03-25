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
 * <p>Java class for BandwidthTotalUsageType complex type.
 * <p/>
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p/>
 * <pre>
 * &lt;complexType name="BandwidthTotalUsageType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="Billable" type="{}ResourceUnitType" minOccurs="0"/>
 *         &lt;element name="Burst" type="{}ResourceUnitType" minOccurs="0"/>
 *         &lt;element name="In" type="{}ResourceUnitType" minOccurs="0"/>
 *         &lt;element name="Out" type="{}ResourceUnitType" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "BandwidthTotalUsageType", propOrder = {
    "billable",
    "burst",
    "in",
    "out"
})
public class BandwidthTotalUsageType {

    @XmlElementRef(name = "Billable", type = JAXBElement.class, required = false)
    protected JAXBElement<ResourceUnitType> billable;
    @XmlElementRef(name = "Burst", type = JAXBElement.class, required = false)
    protected JAXBElement<ResourceUnitType> burst;
    @XmlElementRef(name = "In", type = JAXBElement.class, required = false)
    protected JAXBElement<ResourceUnitType> in;
    @XmlElementRef(name = "Out", type = JAXBElement.class, required = false)
    protected JAXBElement<ResourceUnitType> out;

    /**
     * Gets the value of the billable property.
     *
     * @return possible object is
     * {@link JAXBElement }{@code <}{@link ResourceUnitType }{@code >}
     */
    public JAXBElement<ResourceUnitType> getBillable() {
        return billable;
    }

    /**
     * Sets the value of the billable property.
     *
     * @param value allowed object is
     *              {@link JAXBElement }{@code <}{@link ResourceUnitType }{@code >}
     */
    public void setBillable(JAXBElement<ResourceUnitType> value) {
        this.billable = value;
    }

    /**
     * Gets the value of the burst property.
     *
     * @return possible object is
     * {@link JAXBElement }{@code <}{@link ResourceUnitType }{@code >}
     */
    public JAXBElement<ResourceUnitType> getBurst() {
        return burst;
    }

    /**
     * Sets the value of the burst property.
     *
     * @param value allowed object is
     *              {@link JAXBElement }{@code <}{@link ResourceUnitType }{@code >}
     */
    public void setBurst(JAXBElement<ResourceUnitType> value) {
        this.burst = value;
    }

    /**
     * Gets the value of the in property.
     *
     * @return possible object is
     * {@link JAXBElement }{@code <}{@link ResourceUnitType }{@code >}
     */
    public JAXBElement<ResourceUnitType> getIn() {
        return in;
    }

    /**
     * Sets the value of the in property.
     *
     * @param value allowed object is
     *              {@link JAXBElement }{@code <}{@link ResourceUnitType }{@code >}
     */
    public void setIn(JAXBElement<ResourceUnitType> value) {
        this.in = value;
    }

    /**
     * Gets the value of the out property.
     *
     * @return possible object is
     * {@link JAXBElement }{@code <}{@link ResourceUnitType }{@code >}
     */
    public JAXBElement<ResourceUnitType> getOut() {
        return out;
    }

    /**
     * Sets the value of the out property.
     *
     * @param value allowed object is
     *              {@link JAXBElement }{@code <}{@link ResourceUnitType }{@code >}
     */
    public void setOut(JAXBElement<ResourceUnitType> value) {
        this.out = value;
    }

}
