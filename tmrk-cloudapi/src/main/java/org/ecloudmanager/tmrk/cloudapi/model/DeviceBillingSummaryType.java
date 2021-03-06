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
import javax.xml.datatype.XMLGregorianCalendar;


/**
 * <p>Java class for DeviceBillingSummaryType complex type.
 * <p/>
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p/>
 * <pre>
 * &lt;complexType name="DeviceBillingSummaryType">
 *   &lt;complexContent>
 *     &lt;extension base="{}ResourceType">
 *       &lt;sequence>
 *         &lt;element name="StartDate" type="{http://www.w3.org/2001/XMLSchema}dateTime" minOccurs="0"/>
 *         &lt;element name="EndDate" type="{http://www.w3.org/2001/XMLSchema}dateTime" minOccurs="0"/>
 *         &lt;element name="Total" type="{}CostType" minOccurs="0"/>
 *         &lt;element name="MeteredBilling" type="{}DeviceMeteredBillingType" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "DeviceBillingSummaryType", propOrder = {
    "startDate",
    "endDate",
    "total",
    "meteredBilling"
})
public class DeviceBillingSummaryType
    extends ResourceType {

    @XmlElement(name = "StartDate")
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar startDate;
    @XmlElement(name = "EndDate")
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar endDate;
    @XmlElementRef(name = "Total", type = JAXBElement.class, required = false)
    protected JAXBElement<CostType> total;
    @XmlElementRef(name = "MeteredBilling", type = JAXBElement.class, required = false)
    protected JAXBElement<DeviceMeteredBillingType> meteredBilling;

    /**
     * Gets the value of the startDate property.
     *
     * @return possible object is
     * {@link XMLGregorianCalendar }
     */
    public XMLGregorianCalendar getStartDate() {
        return startDate;
    }

    /**
     * Sets the value of the startDate property.
     *
     * @param value allowed object is
     *              {@link XMLGregorianCalendar }
     */
    public void setStartDate(XMLGregorianCalendar value) {
        this.startDate = value;
    }

    /**
     * Gets the value of the endDate property.
     *
     * @return possible object is
     * {@link XMLGregorianCalendar }
     */
    public XMLGregorianCalendar getEndDate() {
        return endDate;
    }

    /**
     * Sets the value of the endDate property.
     *
     * @param value allowed object is
     *              {@link XMLGregorianCalendar }
     */
    public void setEndDate(XMLGregorianCalendar value) {
        this.endDate = value;
    }

    /**
     * Gets the value of the total property.
     *
     * @return possible object is
     * {@link JAXBElement }{@code <}{@link CostType }{@code >}
     */
    public JAXBElement<CostType> getTotal() {
        return total;
    }

    /**
     * Sets the value of the total property.
     *
     * @param value allowed object is
     *              {@link JAXBElement }{@code <}{@link CostType }{@code >}
     */
    public void setTotal(JAXBElement<CostType> value) {
        this.total = value;
    }

    /**
     * Gets the value of the meteredBilling property.
     *
     * @return possible object is
     * {@link JAXBElement }{@code <}{@link DeviceMeteredBillingType }{@code >}
     */
    public JAXBElement<DeviceMeteredBillingType> getMeteredBilling() {
        return meteredBilling;
    }

    /**
     * Sets the value of the meteredBilling property.
     *
     * @param value allowed object is
     *              {@link JAXBElement }{@code <}{@link DeviceMeteredBillingType }{@code >}
     */
    public void setMeteredBilling(JAXBElement<DeviceMeteredBillingType> value) {
        this.meteredBilling = value;
    }

}
