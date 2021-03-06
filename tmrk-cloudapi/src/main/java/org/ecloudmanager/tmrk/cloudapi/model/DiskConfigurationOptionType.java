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
 * <p>Java class for DiskConfigurationOptionType complex type.
 * <p/>
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p/>
 * <pre>
 * &lt;complexType name="DiskConfigurationOptionType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="Minimum" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="Maximum" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="SystemDisk" type="{}DiskConfigurationOptionRangeType" minOccurs="0"/>
 *         &lt;element name="DataDisk" type="{}DiskConfigurationOptionRangeType" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "DiskConfigurationOptionType", propOrder = {
    "minimum",
    "maximum",
    "systemDisk",
    "dataDisk"
})
public class DiskConfigurationOptionType {

    @XmlElement(name = "Minimum")
    protected int minimum;
    @XmlElement(name = "Maximum")
    protected int maximum;
    @XmlElementRef(name = "SystemDisk", type = JAXBElement.class, required = false)
    protected JAXBElement<DiskConfigurationOptionRangeType> systemDisk;
    @XmlElementRef(name = "DataDisk", type = JAXBElement.class, required = false)
    protected JAXBElement<DiskConfigurationOptionRangeType> dataDisk;

    /**
     * Gets the value of the minimum property.
     */
    public int getMinimum() {
        return minimum;
    }

    /**
     * Sets the value of the minimum property.
     */
    public void setMinimum(int value) {
        this.minimum = value;
    }

    /**
     * Gets the value of the maximum property.
     */
    public int getMaximum() {
        return maximum;
    }

    /**
     * Sets the value of the maximum property.
     */
    public void setMaximum(int value) {
        this.maximum = value;
    }

    /**
     * Gets the value of the systemDisk property.
     *
     * @return possible object is
     * {@link JAXBElement }{@code <}{@link DiskConfigurationOptionRangeType }{@code >}
     */
    public JAXBElement<DiskConfigurationOptionRangeType> getSystemDisk() {
        return systemDisk;
    }

    /**
     * Sets the value of the systemDisk property.
     *
     * @param value allowed object is
     *              {@link JAXBElement }{@code <}{@link DiskConfigurationOptionRangeType }{@code >}
     */
    public void setSystemDisk(JAXBElement<DiskConfigurationOptionRangeType> value) {
        this.systemDisk = value;
    }

    /**
     * Gets the value of the dataDisk property.
     *
     * @return possible object is
     * {@link JAXBElement }{@code <}{@link DiskConfigurationOptionRangeType }{@code >}
     */
    public JAXBElement<DiskConfigurationOptionRangeType> getDataDisk() {
        return dataDisk;
    }

    /**
     * Sets the value of the dataDisk property.
     *
     * @param value allowed object is
     *              {@link JAXBElement }{@code <}{@link DiskConfigurationOptionRangeType }{@code >}
     */
    public void setDataDisk(JAXBElement<DiskConfigurationOptionRangeType> value) {
        this.dataDisk = value;
    }

}
