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
 * <p>Java class for BandwidthUsageType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="BandwidthUsageType">
 *   &lt;complexContent>
 *     &lt;extension base="{}ResourceType">
 *       &lt;sequence>
 *         &lt;element name="TotalUsage" type="{}BandwidthTotalUsageType" minOccurs="0"/>
 *         &lt;element name="IpStatistics" type="{}BandwidthIpAddressUsagesType" minOccurs="0"/>
 *         &lt;element name="HistoricalUsages" type="{}ArrayOfHistoricalUsageType" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "BandwidthUsageType", propOrder = {
    "totalUsage",
    "ipStatistics",
    "historicalUsages"
})
public class BandwidthUsageType
    extends ResourceType
{

    @XmlElementRef(name = "TotalUsage", type = JAXBElement.class, required = false)
    protected JAXBElement<BandwidthTotalUsageType> totalUsage;
    @XmlElementRef(name = "IpStatistics", type = JAXBElement.class, required = false)
    protected JAXBElement<BandwidthIpAddressUsagesType> ipStatistics;
    @XmlElementRef(name = "HistoricalUsages", type = JAXBElement.class, required = false)
    protected JAXBElement<ArrayOfHistoricalUsageType> historicalUsages;

    /**
     * Gets the value of the totalUsage property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link BandwidthTotalUsageType }{@code >}
     *     
     */
    public JAXBElement<BandwidthTotalUsageType> getTotalUsage() {
        return totalUsage;
    }

    /**
     * Sets the value of the totalUsage property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link BandwidthTotalUsageType }{@code >}
     *     
     */
    public void setTotalUsage(JAXBElement<BandwidthTotalUsageType> value) {
        this.totalUsage = value;
    }

    /**
     * Gets the value of the ipStatistics property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link BandwidthIpAddressUsagesType }{@code >}
     *     
     */
    public JAXBElement<BandwidthIpAddressUsagesType> getIpStatistics() {
        return ipStatistics;
    }

    /**
     * Sets the value of the ipStatistics property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link BandwidthIpAddressUsagesType }{@code >}
     *     
     */
    public void setIpStatistics(JAXBElement<BandwidthIpAddressUsagesType> value) {
        this.ipStatistics = value;
    }

    /**
     * Gets the value of the historicalUsages property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link ArrayOfHistoricalUsageType }{@code >}
     *     
     */
    public JAXBElement<ArrayOfHistoricalUsageType> getHistoricalUsages() {
        return historicalUsages;
    }

    /**
     * Sets the value of the historicalUsages property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link ArrayOfHistoricalUsageType }{@code >}
     *     
     */
    public void setHistoricalUsages(JAXBElement<ArrayOfHistoricalUsageType> value) {
        this.historicalUsages = value;
    }

}
