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
 * <p>Java class for ComputePoolCapacityType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ComputePoolCapacityType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="Cpu" type="{}ResourceUnitType" minOccurs="0"/>
 *         &lt;element name="Memory" type="{}ResourceUnitType" minOccurs="0"/>
 *         &lt;element name="Storage" type="{}ResourceUnitType" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ComputePoolCapacityType", propOrder = {
    "cpu",
    "memory",
    "storage"
})
public class ComputePoolCapacityType {

    @XmlElementRef(name = "Cpu", type = JAXBElement.class, required = false)
    protected JAXBElement<ResourceUnitType> cpu;
    @XmlElementRef(name = "Memory", type = JAXBElement.class, required = false)
    protected JAXBElement<ResourceUnitType> memory;
    @XmlElementRef(name = "Storage", type = JAXBElement.class, required = false)
    protected JAXBElement<ResourceUnitType> storage;

    /**
     * Gets the value of the cpu property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link ResourceUnitType }{@code >}
     *     
     */
    public JAXBElement<ResourceUnitType> getCpu() {
        return cpu;
    }

    /**
     * Sets the value of the cpu property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link ResourceUnitType }{@code >}
     *     
     */
    public void setCpu(JAXBElement<ResourceUnitType> value) {
        this.cpu = value;
    }

    /**
     * Gets the value of the memory property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link ResourceUnitType }{@code >}
     *     
     */
    public JAXBElement<ResourceUnitType> getMemory() {
        return memory;
    }

    /**
     * Sets the value of the memory property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link ResourceUnitType }{@code >}
     *     
     */
    public void setMemory(JAXBElement<ResourceUnitType> value) {
        this.memory = value;
    }

    /**
     * Gets the value of the storage property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link ResourceUnitType }{@code >}
     *     
     */
    public JAXBElement<ResourceUnitType> getStorage() {
        return storage;
    }

    /**
     * Sets the value of the storage property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link ResourceUnitType }{@code >}
     *     
     */
    public void setStorage(JAXBElement<ResourceUnitType> value) {
        this.storage = value;
    }

}
