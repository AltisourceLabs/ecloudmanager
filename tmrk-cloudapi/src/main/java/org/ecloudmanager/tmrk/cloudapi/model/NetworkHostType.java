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
 * <p>Java class for NetworkHostType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="NetworkHostType">
 *   &lt;complexContent>
 *     &lt;extension base="{}ResourceType">
 *       &lt;sequence>
 *         &lt;element name="Device" type="{}ReferenceType" minOccurs="0"/>
 *         &lt;element name="Networks" type="{}NetworkHostNetworksType" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "NetworkHostType", propOrder = {
    "device",
    "networks"
})
public class NetworkHostType
    extends ResourceType
{

    @XmlElementRef(name = "Device", type = JAXBElement.class, required = false)
    protected JAXBElement<ReferenceType> device;
    @XmlElementRef(name = "Networks", type = JAXBElement.class, required = false)
    protected JAXBElement<NetworkHostNetworksType> networks;

    /**
     * Gets the value of the device property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link ReferenceType }{@code >}
     *     
     */
    public JAXBElement<ReferenceType> getDevice() {
        return device;
    }

    /**
     * Sets the value of the device property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link ReferenceType }{@code >}
     *     
     */
    public void setDevice(JAXBElement<ReferenceType> value) {
        this.device = value;
    }

    /**
     * Gets the value of the networks property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link NetworkHostNetworksType }{@code >}
     *     
     */
    public JAXBElement<NetworkHostNetworksType> getNetworks() {
        return networks;
    }

    /**
     * Sets the value of the networks property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link NetworkHostNetworksType }{@code >}
     *     
     */
    public void setNetworks(JAXBElement<NetworkHostNetworksType> value) {
        this.networks = value;
    }

}
