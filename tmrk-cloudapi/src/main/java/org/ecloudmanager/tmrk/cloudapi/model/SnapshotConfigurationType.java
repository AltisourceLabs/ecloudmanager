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
 * <p>Java class for SnapshotConfigurationType complex type.
 * <p/>
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p/>
 * <pre>
 * &lt;complexType name="SnapshotConfigurationType">
 *   &lt;complexContent>
 *     &lt;extension base="{}ResourceType">
 *       &lt;sequence>
 *         &lt;element name="SnapshotSchedule" type="{}SnapshotScheduleType" minOccurs="0"/>
 *         &lt;element name="SnapshotOverheadStorage" type="{}ResourceUnitType" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SnapshotConfigurationType", propOrder = {
    "snapshotSchedule",
    "snapshotOverheadStorage"
})
public class SnapshotConfigurationType
    extends ResourceType {

    @XmlElementRef(name = "SnapshotSchedule", type = JAXBElement.class, required = false)
    protected JAXBElement<SnapshotScheduleType> snapshotSchedule;
    @XmlElementRef(name = "SnapshotOverheadStorage", type = JAXBElement.class, required = false)
    protected JAXBElement<ResourceUnitType> snapshotOverheadStorage;

    /**
     * Gets the value of the snapshotSchedule property.
     *
     * @return possible object is
     * {@link JAXBElement }{@code <}{@link SnapshotScheduleType }{@code >}
     */
    public JAXBElement<SnapshotScheduleType> getSnapshotSchedule() {
        return snapshotSchedule;
    }

    /**
     * Sets the value of the snapshotSchedule property.
     *
     * @param value allowed object is
     *              {@link JAXBElement }{@code <}{@link SnapshotScheduleType }{@code >}
     */
    public void setSnapshotSchedule(JAXBElement<SnapshotScheduleType> value) {
        this.snapshotSchedule = value;
    }

    /**
     * Gets the value of the snapshotOverheadStorage property.
     *
     * @return possible object is
     * {@link JAXBElement }{@code <}{@link ResourceUnitType }{@code >}
     */
    public JAXBElement<ResourceUnitType> getSnapshotOverheadStorage() {
        return snapshotOverheadStorage;
    }

    /**
     * Sets the value of the snapshotOverheadStorage property.
     *
     * @param value allowed object is
     *              {@link JAXBElement }{@code <}{@link ResourceUnitType }{@code >}
     */
    public void setSnapshotOverheadStorage(JAXBElement<ResourceUnitType> value) {
        this.snapshotOverheadStorage = value;
    }

}
