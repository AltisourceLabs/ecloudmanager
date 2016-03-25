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
 * <p>Java class for RnatType complex type.
 * <p/>
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p/>
 * <pre>
 * &lt;complexType name="RnatType">
 *   &lt;complexContent>
 *     &lt;extension base="{}ResourceType">
 *       &lt;sequence>
 *         &lt;element name="Default" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="PublicIp" type="{}ReferenceType" minOccurs="0"/>
 *         &lt;element name="Networks" type="{}NetworkReferencesType" minOccurs="0"/>
 *         &lt;element name="Associations" type="{}RnatHostAssociationsType" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "RnatType", propOrder = {
    "_default",
    "publicIp",
    "networks",
    "associations"
})
public class RnatType
    extends ResourceType {

    @XmlElement(name = "Default")
    protected Boolean _default;
    @XmlElementRef(name = "PublicIp", type = JAXBElement.class, required = false)
    protected JAXBElement<ReferenceType> publicIp;
    @XmlElementRef(name = "Networks", type = JAXBElement.class, required = false)
    protected JAXBElement<NetworkReferencesType> networks;
    @XmlElementRef(name = "Associations", type = JAXBElement.class, required = false)
    protected JAXBElement<RnatHostAssociationsType> associations;

    /**
     * Gets the value of the default property.
     *
     * @return possible object is
     * {@link Boolean }
     */
    public Boolean isDefault() {
        return _default;
    }

    /**
     * Sets the value of the default property.
     *
     * @param value allowed object is
     *              {@link Boolean }
     */
    public void setDefault(Boolean value) {
        this._default = value;
    }

    /**
     * Gets the value of the publicIp property.
     *
     * @return possible object is
     * {@link JAXBElement }{@code <}{@link ReferenceType }{@code >}
     */
    public JAXBElement<ReferenceType> getPublicIp() {
        return publicIp;
    }

    /**
     * Sets the value of the publicIp property.
     *
     * @param value allowed object is
     *              {@link JAXBElement }{@code <}{@link ReferenceType }{@code >}
     */
    public void setPublicIp(JAXBElement<ReferenceType> value) {
        this.publicIp = value;
    }

    /**
     * Gets the value of the networks property.
     *
     * @return possible object is
     * {@link JAXBElement }{@code <}{@link NetworkReferencesType }{@code >}
     */
    public JAXBElement<NetworkReferencesType> getNetworks() {
        return networks;
    }

    /**
     * Sets the value of the networks property.
     *
     * @param value allowed object is
     *              {@link JAXBElement }{@code <}{@link NetworkReferencesType }{@code >}
     */
    public void setNetworks(JAXBElement<NetworkReferencesType> value) {
        this.networks = value;
    }

    /**
     * Gets the value of the associations property.
     *
     * @return possible object is
     * {@link JAXBElement }{@code <}{@link RnatHostAssociationsType }{@code >}
     */
    public JAXBElement<RnatHostAssociationsType> getAssociations() {
        return associations;
    }

    /**
     * Sets the value of the associations property.
     *
     * @param value allowed object is
     *              {@link JAXBElement }{@code <}{@link RnatHostAssociationsType }{@code >}
     */
    public void setAssociations(JAXBElement<RnatHostAssociationsType> value) {
        this.associations = value;
    }

}
