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
 * <p>Java class for NetworkHostNetworkType complex type.
 * <p/>
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p/>
 * <pre>
 * &lt;complexType name="NetworkHostNetworkType">
 *   &lt;complexContent>
 *     &lt;extension base="{}NetworkReferenceType">
 *       &lt;sequence>
 *         &lt;element name="IpAddresses" type="{}NetworkHostIpAddressesType" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "NetworkHostNetworkType", propOrder = {
    "ipAddresses"
})
public class NetworkHostNetworkType
    extends NetworkReferenceType {

    /**
     *
     */
    private static final long serialVersionUID = 762127307230752761L;
    @XmlElementRef(name = "IpAddresses", type = JAXBElement.class, required = false)
    protected JAXBElement<NetworkHostIpAddressesType> ipAddresses;

    /**
     * Gets the value of the ipAddresses property.
     *
     * @return possible object is
     * {@link JAXBElement }{@code <}{@link NetworkHostIpAddressesType }{@code >}
     */
    public JAXBElement<NetworkHostIpAddressesType> getIpAddresses() {
        return ipAddresses;
    }

    /**
     * Sets the value of the ipAddresses property.
     *
     * @param value allowed object is
     *              {@link JAXBElement }{@code <}{@link NetworkHostIpAddressesType }{@code >}
     */
    public void setIpAddresses(JAXBElement<NetworkHostIpAddressesType> value) {
        this.ipAddresses = value;
    }

}
