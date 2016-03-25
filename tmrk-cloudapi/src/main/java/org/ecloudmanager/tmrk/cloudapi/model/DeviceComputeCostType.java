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
 * <p>Java class for DeviceComputeCostType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="DeviceComputeCostType">
 *   &lt;complexContent>
 *     &lt;extension base="{}BillingCostType">
 *       &lt;sequence>
 *         &lt;element name="Configurations" type="{}ResourceUsagesType" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "DeviceComputeCostType", propOrder = {
    "configurations"
})
public class DeviceComputeCostType
    extends BillingCostType
{

    @XmlElementRef(name = "Configurations", type = JAXBElement.class, required = false)
    protected JAXBElement<ResourceUsagesType> configurations;

    /**
     * Gets the value of the configurations property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link ResourceUsagesType }{@code >}
     *     
     */
    public JAXBElement<ResourceUsagesType> getConfigurations() {
        return configurations;
    }

    /**
     * Sets the value of the configurations property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link ResourceUsagesType }{@code >}
     *     
     */
    public void setConfigurations(JAXBElement<ResourceUsagesType> value) {
        this.configurations = value;
    }

}
