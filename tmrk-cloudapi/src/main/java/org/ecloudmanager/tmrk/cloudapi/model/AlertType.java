//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911.1802 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2015.07.16 at 03:24:22 PM EEST 
//


package org.ecloudmanager.tmrk.cloudapi.model;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.*;
import javax.xml.datatype.XMLGregorianCalendar;


/**
 * <p>Java class for AlertType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="AlertType">
 *   &lt;complexContent>
 *     &lt;extension base="{}ResourceType">
 *       &lt;sequence>
 *         &lt;element name="AlertRuleTemplate" type="{}ReferenceType" minOccurs="0"/>
 *         &lt;element name="AlertSource" type="{}ReferenceType" minOccurs="0"/>
 *         &lt;element name="DurationInMinutes" type="{http://www.w3.org/2001/XMLSchema}long" minOccurs="0"/>
 *         &lt;element name="StartTime" type="{http://www.w3.org/2001/XMLSchema}dateTime" minOccurs="0"/>
 *         &lt;element name="EndTime" type="{http://www.w3.org/2001/XMLSchema}dateTime" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "AlertType", propOrder = {
    "alertRuleTemplate",
    "alertSource",
    "durationInMinutes",
    "startTime",
    "endTime"
})
public class AlertType
    extends ResourceType
{

    @XmlElementRef(name = "AlertRuleTemplate", type = JAXBElement.class, required = false)
    protected JAXBElement<ReferenceType> alertRuleTemplate;
    @XmlElementRef(name = "AlertSource", type = JAXBElement.class, required = false)
    protected JAXBElement<ReferenceType> alertSource;
    @XmlElementRef(name = "DurationInMinutes", type = JAXBElement.class, required = false)
    protected JAXBElement<Long> durationInMinutes;
    @XmlElement(name = "StartTime")
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar startTime;
    @XmlElementRef(name = "EndTime", type = JAXBElement.class, required = false)
    protected JAXBElement<XMLGregorianCalendar> endTime;

    /**
     * Gets the value of the alertRuleTemplate property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link ReferenceType }{@code >}
     *     
     */
    public JAXBElement<ReferenceType> getAlertRuleTemplate() {
        return alertRuleTemplate;
    }

    /**
     * Sets the value of the alertRuleTemplate property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link ReferenceType }{@code >}
     *     
     */
    public void setAlertRuleTemplate(JAXBElement<ReferenceType> value) {
        this.alertRuleTemplate = value;
    }

    /**
     * Gets the value of the alertSource property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link ReferenceType }{@code >}
     *     
     */
    public JAXBElement<ReferenceType> getAlertSource() {
        return alertSource;
    }

    /**
     * Sets the value of the alertSource property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link ReferenceType }{@code >}
     *     
     */
    public void setAlertSource(JAXBElement<ReferenceType> value) {
        this.alertSource = value;
    }

    /**
     * Gets the value of the durationInMinutes property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link Long }{@code >}
     *     
     */
    public JAXBElement<Long> getDurationInMinutes() {
        return durationInMinutes;
    }

    /**
     * Sets the value of the durationInMinutes property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link Long }{@code >}
     *     
     */
    public void setDurationInMinutes(JAXBElement<Long> value) {
        this.durationInMinutes = value;
    }

    /**
     * Gets the value of the startTime property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getStartTime() {
        return startTime;
    }

    /**
     * Sets the value of the startTime property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setStartTime(XMLGregorianCalendar value) {
        this.startTime = value;
    }

    /**
     * Gets the value of the endTime property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link XMLGregorianCalendar }{@code >}
     *     
     */
    public JAXBElement<XMLGregorianCalendar> getEndTime() {
        return endTime;
    }

    /**
     * Sets the value of the endTime property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link XMLGregorianCalendar }{@code >}
     *     
     */
    public void setEndTime(JAXBElement<XMLGregorianCalendar> value) {
        this.endTime = value;
    }

}
