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
 * <p>Java class for ReservedBillingType complex type.
 * <p/>
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p/>
 * <pre>
 * &lt;complexType name="ReservedBillingType">
 *   &lt;complexContent>
 *     &lt;extension base="{}BillingCostType">
 *       &lt;sequence>
 *         &lt;element name="BurstingCost" type="{}BurstingCostType" minOccurs="0"/>
 *         &lt;element name="LicenseCost" type="{}LicenseCostType" minOccurs="0"/>
 *         &lt;element name="CloudServiceCost" type="{}CloudServiceCostType" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ReservedBillingType", propOrder = {
    "burstingCost",
    "licenseCost",
    "cloudServiceCost"
})
public class ReservedBillingType
    extends BillingCostType {

    @XmlElementRef(name = "BurstingCost", type = JAXBElement.class, required = false)
    protected JAXBElement<BurstingCostType> burstingCost;
    @XmlElementRef(name = "LicenseCost", type = JAXBElement.class, required = false)
    protected JAXBElement<LicenseCostType> licenseCost;
    @XmlElementRef(name = "CloudServiceCost", type = JAXBElement.class, required = false)
    protected JAXBElement<CloudServiceCostType> cloudServiceCost;

    /**
     * Gets the value of the burstingCost property.
     *
     * @return possible object is
     * {@link JAXBElement }{@code <}{@link BurstingCostType }{@code >}
     */
    public JAXBElement<BurstingCostType> getBurstingCost() {
        return burstingCost;
    }

    /**
     * Sets the value of the burstingCost property.
     *
     * @param value allowed object is
     *              {@link JAXBElement }{@code <}{@link BurstingCostType }{@code >}
     */
    public void setBurstingCost(JAXBElement<BurstingCostType> value) {
        this.burstingCost = value;
    }

    /**
     * Gets the value of the licenseCost property.
     *
     * @return possible object is
     * {@link JAXBElement }{@code <}{@link LicenseCostType }{@code >}
     */
    public JAXBElement<LicenseCostType> getLicenseCost() {
        return licenseCost;
    }

    /**
     * Sets the value of the licenseCost property.
     *
     * @param value allowed object is
     *              {@link JAXBElement }{@code <}{@link LicenseCostType }{@code >}
     */
    public void setLicenseCost(JAXBElement<LicenseCostType> value) {
        this.licenseCost = value;
    }

    /**
     * Gets the value of the cloudServiceCost property.
     *
     * @return possible object is
     * {@link JAXBElement }{@code <}{@link CloudServiceCostType }{@code >}
     */
    public JAXBElement<CloudServiceCostType> getCloudServiceCost() {
        return cloudServiceCost;
    }

    /**
     * Sets the value of the cloudServiceCost property.
     *
     * @param value allowed object is
     *              {@link JAXBElement }{@code <}{@link CloudServiceCostType }{@code >}
     */
    public void setCloudServiceCost(JAXBElement<CloudServiceCostType> value) {
        this.cloudServiceCost = value;
    }

}
