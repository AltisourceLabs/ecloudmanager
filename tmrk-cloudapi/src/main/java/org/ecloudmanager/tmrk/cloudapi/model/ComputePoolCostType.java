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
 * <p>Java class for ComputePoolCostType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ComputePoolCostType">
 *   &lt;complexContent>
 *     &lt;extension base="{}ReferenceType">
 *       &lt;sequence>
 *         &lt;element name="Total" type="{}CostType" minOccurs="0"/>
 *         &lt;element name="InstanceBased" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="BurstingCost" type="{}BurstingCostType" minOccurs="0"/>
 *         &lt;element name="Servers" type="{}DeviceCostListType" minOccurs="0"/>
 *         &lt;element name="DetachedDisks" type="{}DetachedDiskCostListType" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ComputePoolCostType", propOrder = {
    "total",
    "instanceBased",
    "burstingCost",
    "servers",
    "detachedDisks"
})
public class ComputePoolCostType
    extends ReferenceType
{

    private static final long serialVersionUID = -4631989299233564939L;

    @XmlElementRef(name = "Total", type = JAXBElement.class, required = false)
    protected JAXBElement<CostType> total;
    @XmlElement(name = "InstanceBased")
    protected Boolean instanceBased;
    @XmlElementRef(name = "BurstingCost", type = JAXBElement.class, required = false)
    protected JAXBElement<BurstingCostType> burstingCost;
    @XmlElementRef(name = "Servers", type = JAXBElement.class, required = false)
    protected JAXBElement<DeviceCostListType> servers;
    @XmlElementRef(name = "DetachedDisks", type = JAXBElement.class, required = false)
    protected JAXBElement<DetachedDiskCostListType> detachedDisks;

    /**
     * Gets the value of the total property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link CostType }{@code >}
     *     
     */
    public JAXBElement<CostType> getTotal() {
        return total;
    }

    /**
     * Sets the value of the total property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link CostType }{@code >}
     *     
     */
    public void setTotal(JAXBElement<CostType> value) {
        this.total = value;
    }

    /**
     * Gets the value of the instanceBased property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isInstanceBased() {
        return instanceBased;
    }

    /**
     * Sets the value of the instanceBased property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setInstanceBased(Boolean value) {
        this.instanceBased = value;
    }

    /**
     * Gets the value of the burstingCost property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link BurstingCostType }{@code >}
     *     
     */
    public JAXBElement<BurstingCostType> getBurstingCost() {
        return burstingCost;
    }

    /**
     * Sets the value of the burstingCost property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link BurstingCostType }{@code >}
     *     
     */
    public void setBurstingCost(JAXBElement<BurstingCostType> value) {
        this.burstingCost = value;
    }

    /**
     * Gets the value of the servers property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link DeviceCostListType }{@code >}
     *     
     */
    public JAXBElement<DeviceCostListType> getServers() {
        return servers;
    }

    /**
     * Sets the value of the servers property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link DeviceCostListType }{@code >}
     *     
     */
    public void setServers(JAXBElement<DeviceCostListType> value) {
        this.servers = value;
    }

    /**
     * Gets the value of the detachedDisks property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link DetachedDiskCostListType }{@code >}
     *     
     */
    public JAXBElement<DetachedDiskCostListType> getDetachedDisks() {
        return detachedDisks;
    }

    /**
     * Sets the value of the detachedDisks property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link DetachedDiskCostListType }{@code >}
     *     
     */
    public void setDetachedDisks(JAXBElement<DetachedDiskCostListType> value) {
        this.detachedDisks = value;
    }

}
