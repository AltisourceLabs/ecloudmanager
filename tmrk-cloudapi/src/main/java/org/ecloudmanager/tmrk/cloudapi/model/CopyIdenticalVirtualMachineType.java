//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911.1802 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2015.07.16 at 03:24:22 PM EEST 
//


package org.ecloudmanager.tmrk.cloudapi.model;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.*;


/**
 * <p>Java class for CopyIdenticalVirtualMachineType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CopyIdenticalVirtualMachineType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="Source" type="{}ReferenceType"/>
 *         &lt;element name="DestinationComputePool" type="{}ReferenceType" minOccurs="0"/>
 *         &lt;element name="Layout" type="{}LayoutRequestType" minOccurs="0"/>
 *         &lt;element name="Description" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="name" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CopyIdenticalVirtualMachineType", propOrder = {
    "source",
    "destinationComputePool",
    "layout",
    "description"
})
public class CopyIdenticalVirtualMachineType {

    @XmlElement(name = "Source", required = true, nillable = true)
    protected ReferenceType source;
    @XmlElementRef(name = "DestinationComputePool", type = JAXBElement.class, required = false)
    protected JAXBElement<ReferenceType> destinationComputePool;
    @XmlElementRef(name = "Layout", type = JAXBElement.class, required = false)
    protected JAXBElement<LayoutRequestType> layout;
    @XmlElementRef(name = "Description", type = JAXBElement.class, required = false)
    protected JAXBElement<String> description;
    @XmlAttribute(name = "name", required = true)
    protected String name;

    /**
     * Gets the value of the source property.
     * 
     * @return
     *     possible object is
     *     {@link ReferenceType }
     *     
     */
    public ReferenceType getSource() {
        return source;
    }

    /**
     * Sets the value of the source property.
     * 
     * @param value
     *     allowed object is
     *     {@link ReferenceType }
     *     
     */
    public void setSource(ReferenceType value) {
        this.source = value;
    }

    /**
     * Gets the value of the destinationComputePool property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link ReferenceType }{@code >}
     *     
     */
    public JAXBElement<ReferenceType> getDestinationComputePool() {
        return destinationComputePool;
    }

    /**
     * Sets the value of the destinationComputePool property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link ReferenceType }{@code >}
     *     
     */
    public void setDestinationComputePool(JAXBElement<ReferenceType> value) {
        this.destinationComputePool = value;
    }

    /**
     * Gets the value of the layout property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link LayoutRequestType }{@code >}
     *     
     */
    public JAXBElement<LayoutRequestType> getLayout() {
        return layout;
    }

    /**
     * Sets the value of the layout property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link LayoutRequestType }{@code >}
     *     
     */
    public void setLayout(JAXBElement<LayoutRequestType> value) {
        this.layout = value;
    }

    /**
     * Gets the value of the description property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public JAXBElement<String> getDescription() {
        return description;
    }

    /**
     * Sets the value of the description property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public void setDescription(JAXBElement<String> value) {
        this.description = value;
    }

    /**
     * Gets the value of the name property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the value of the name property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setName(String value) {
        this.name = value;
    }

}
