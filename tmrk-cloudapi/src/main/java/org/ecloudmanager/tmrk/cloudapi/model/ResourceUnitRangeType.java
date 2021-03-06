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
 * <p>Java class for ResourceUnitRangeType complex type.
 * <p/>
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p/>
 * <pre>
 * &lt;complexType name="ResourceUnitRangeType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="MinimumSize" type="{}ResourceUnitType" minOccurs="0"/>
 *         &lt;element name="MaximumSize" type="{}ResourceUnitType" minOccurs="0"/>
 *         &lt;element name="StepFactor" type="{}ResourceUnitType" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ResourceUnitRangeType", propOrder = {
    "minimumSize",
    "maximumSize",
    "stepFactor"
})
public class ResourceUnitRangeType {

    @XmlElementRef(name = "MinimumSize", type = JAXBElement.class, required = false)
    protected JAXBElement<ResourceUnitType> minimumSize;
    @XmlElementRef(name = "MaximumSize", type = JAXBElement.class, required = false)
    protected JAXBElement<ResourceUnitType> maximumSize;
    @XmlElementRef(name = "StepFactor", type = JAXBElement.class, required = false)
    protected JAXBElement<ResourceUnitType> stepFactor;

    /**
     * Gets the value of the minimumSize property.
     *
     * @return possible object is
     * {@link JAXBElement }{@code <}{@link ResourceUnitType }{@code >}
     */
    public JAXBElement<ResourceUnitType> getMinimumSize() {
        return minimumSize;
    }

    /**
     * Sets the value of the minimumSize property.
     *
     * @param value allowed object is
     *              {@link JAXBElement }{@code <}{@link ResourceUnitType }{@code >}
     */
    public void setMinimumSize(JAXBElement<ResourceUnitType> value) {
        this.minimumSize = value;
    }

    /**
     * Gets the value of the maximumSize property.
     *
     * @return possible object is
     * {@link JAXBElement }{@code <}{@link ResourceUnitType }{@code >}
     */
    public JAXBElement<ResourceUnitType> getMaximumSize() {
        return maximumSize;
    }

    /**
     * Sets the value of the maximumSize property.
     *
     * @param value allowed object is
     *              {@link JAXBElement }{@code <}{@link ResourceUnitType }{@code >}
     */
    public void setMaximumSize(JAXBElement<ResourceUnitType> value) {
        this.maximumSize = value;
    }

    /**
     * Gets the value of the stepFactor property.
     *
     * @return possible object is
     * {@link JAXBElement }{@code <}{@link ResourceUnitType }{@code >}
     */
    public JAXBElement<ResourceUnitType> getStepFactor() {
        return stepFactor;
    }

    /**
     * Sets the value of the stepFactor property.
     *
     * @param value allowed object is
     *              {@link JAXBElement }{@code <}{@link ResourceUnitType }{@code >}
     */
    public void setStepFactor(JAXBElement<ResourceUnitType> value) {
        this.stepFactor = value;
    }

}
