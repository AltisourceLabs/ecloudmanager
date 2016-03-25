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
 * <p>Java class for VirtualNicType complex type.
 * <p/>
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p/>
 * <pre>
 * &lt;complexType name="VirtualNicType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="UnitNumber" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="Name" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="MacAddress" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="Network" type="{}NetworkReferenceType"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "VirtualNicType", propOrder = {
    "unitNumber",
    "name",
    "macAddress",
    "network"
})
public class VirtualNicType {

    @XmlElementRef(name = "UnitNumber", type = JAXBElement.class, required = false)
    protected JAXBElement<Integer> unitNumber;
    @XmlElementRef(name = "Name", type = JAXBElement.class, required = false)
    protected JAXBElement<String> name;
    @XmlElementRef(name = "MacAddress", type = JAXBElement.class, required = false)
    protected JAXBElement<String> macAddress;
    @XmlElement(name = "Network", required = true, nillable = true)
    protected NetworkReferenceType network;

    /**
     * Gets the value of the unitNumber property.
     *
     * @return possible object is
     * {@link JAXBElement }{@code <}{@link Integer }{@code >}
     */
    public JAXBElement<Integer> getUnitNumber() {
        return unitNumber;
    }

    /**
     * Sets the value of the unitNumber property.
     *
     * @param value allowed object is
     *              {@link JAXBElement }{@code <}{@link Integer }{@code >}
     */
    public void setUnitNumber(JAXBElement<Integer> value) {
        this.unitNumber = value;
    }

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
     * Gets the value of the macAddress property.
     *
     * @return possible object is
     * {@link JAXBElement }{@code <}{@link String }{@code >}
     */
    public JAXBElement<String> getMacAddress() {
        return macAddress;
    }

    /**
     * Sets the value of the macAddress property.
     *
     * @param value allowed object is
     *              {@link JAXBElement }{@code <}{@link String }{@code >}
     */
    public void setMacAddress(JAXBElement<String> value) {
        this.macAddress = value;
    }

    /**
     * Gets the value of the network property.
     *
     * @return possible object is
     * {@link NetworkReferenceType }
     */
    public NetworkReferenceType getNetwork() {
        return network;
    }

    /**
     * Sets the value of the network property.
     *
     * @param value allowed object is
     *              {@link NetworkReferenceType }
     */
    public void setNetwork(NetworkReferenceType value) {
        this.network = value;
    }

}
