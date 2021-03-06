//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911.1802 
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
 * <p>Java class for SnapshotScheduleOptionsType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="SnapshotScheduleOptionsType">
 *   &lt;complexContent>
 *     &lt;extension base="{}ResourceType">
 *       &lt;sequence>
 *         &lt;element name="Interval" type="{}SnapshotConfigurationRangeType" minOccurs="0"/>
 *         &lt;element name="SnapshotsToCapture" type="{}ConfigurationOptionRangeType" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SnapshotScheduleOptionsType", propOrder = {
    "interval",
    "snapshotsToCapture"
})
public class SnapshotScheduleOptionsType
    extends ResourceType
{

    @XmlElementRef(name = "Interval", type = JAXBElement.class, required = false)
    protected JAXBElement<SnapshotConfigurationRangeType> interval;
    @XmlElementRef(name = "SnapshotsToCapture", type = JAXBElement.class, required = false)
    protected JAXBElement<ConfigurationOptionRangeType> snapshotsToCapture;

    /**
     * Gets the value of the interval property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link SnapshotConfigurationRangeType }{@code >}
     *     
     */
    public JAXBElement<SnapshotConfigurationRangeType> getInterval() {
        return interval;
    }

    /**
     * Sets the value of the interval property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link SnapshotConfigurationRangeType }{@code >}
     *     
     */
    public void setInterval(JAXBElement<SnapshotConfigurationRangeType> value) {
        this.interval = value;
    }

    /**
     * Gets the value of the snapshotsToCapture property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link ConfigurationOptionRangeType }{@code >}
     *     
     */
    public JAXBElement<ConfigurationOptionRangeType> getSnapshotsToCapture() {
        return snapshotsToCapture;
    }

    /**
     * Sets the value of the snapshotsToCapture property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link ConfigurationOptionRangeType }{@code >}
     *     
     */
    public void setSnapshotsToCapture(JAXBElement<ConfigurationOptionRangeType> value) {
        this.snapshotsToCapture = value;
    }

}
