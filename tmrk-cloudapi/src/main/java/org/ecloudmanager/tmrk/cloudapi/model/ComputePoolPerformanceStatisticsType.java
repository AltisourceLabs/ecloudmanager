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
 * <p>Java class for ComputePoolPerformanceStatisticsType complex type.
 * <p/>
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p/>
 * <pre>
 * &lt;complexType name="ComputePoolPerformanceStatisticsType">
 *   &lt;complexContent>
 *     &lt;extension base="{}ResourceType">
 *       &lt;sequence>
 *         &lt;element name="Hourly" type="{}ComputePoolPerformanceStatisticType" minOccurs="0"/>
 *         &lt;element name="Daily" type="{}ComputePoolPerformanceStatisticType" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ComputePoolPerformanceStatisticsType", propOrder = {
    "hourly",
    "daily"
})
public class ComputePoolPerformanceStatisticsType
    extends ResourceType {

    @XmlElementRef(name = "Hourly", type = JAXBElement.class, required = false)
    protected JAXBElement<ComputePoolPerformanceStatisticType> hourly;
    @XmlElementRef(name = "Daily", type = JAXBElement.class, required = false)
    protected JAXBElement<ComputePoolPerformanceStatisticType> daily;

    /**
     * Gets the value of the hourly property.
     *
     * @return possible object is
     * {@link JAXBElement }{@code <}{@link ComputePoolPerformanceStatisticType }{@code >}
     */
    public JAXBElement<ComputePoolPerformanceStatisticType> getHourly() {
        return hourly;
    }

    /**
     * Sets the value of the hourly property.
     *
     * @param value allowed object is
     *              {@link JAXBElement }{@code <}{@link ComputePoolPerformanceStatisticType }{@code >}
     */
    public void setHourly(JAXBElement<ComputePoolPerformanceStatisticType> value) {
        this.hourly = value;
    }

    /**
     * Gets the value of the daily property.
     *
     * @return possible object is
     * {@link JAXBElement }{@code <}{@link ComputePoolPerformanceStatisticType }{@code >}
     */
    public JAXBElement<ComputePoolPerformanceStatisticType> getDaily() {
        return daily;
    }

    /**
     * Sets the value of the daily property.
     *
     * @param value allowed object is
     *              {@link JAXBElement }{@code <}{@link ComputePoolPerformanceStatisticType }{@code >}
     */
    public void setDaily(JAXBElement<ComputePoolPerformanceStatisticType> value) {
        this.daily = value;
    }

}
