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
 * <p>Java class for ThresholdType complex type.
 * <p/>
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p/>
 * <pre>
 * &lt;complexType name="ThresholdType">
 *   &lt;complexContent>
 *     &lt;extension base="{}ResourceUnitType">
 *       &lt;sequence>
 *         &lt;element name="CompareType" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ThresholdType", propOrder = {
    "compareType"
})
public class ThresholdType
    extends ResourceUnitType {

    /**
     *
     */
    private static final long serialVersionUID = -608917011580686406L;
    @XmlElementRef(name = "CompareType", type = JAXBElement.class, required = false)
    protected JAXBElement<String> compareType;

    /**
     * Gets the value of the compareType property.
     *
     * @return possible object is
     * {@link JAXBElement }{@code <}{@link String }{@code >}
     */
    public JAXBElement<String> getCompareType() {
        return compareType;
    }

    /**
     * Sets the value of the compareType property.
     *
     * @param value allowed object is
     *              {@link JAXBElement }{@code <}{@link String }{@code >}
     */
    public void setCompareType(JAXBElement<String> value) {
        this.compareType = value;
    }

}
