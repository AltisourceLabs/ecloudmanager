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
 * <p>Java class for DisasterRecoveryMirrorStatusType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="DisasterRecoveryMirrorStatusType">
 *   &lt;complexContent>
 *     &lt;extension base="{}EntityType">
 *       &lt;sequence>
 *         &lt;element name="MirrorStatusItems" type="{}ArrayOfMirrorStatusItemType" minOccurs="0"/>
 *         &lt;element name="StateCountSummary" type="{}MirrorStateCountSummaryDataType" minOccurs="0"/>
 *         &lt;element name="MirrorErrors" type="{}ArrayOfMirrorErrorDataType" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "DisasterRecoveryMirrorStatusType", propOrder = {
    "mirrorStatusItems",
    "stateCountSummary",
    "mirrorErrors"
})
public class DisasterRecoveryMirrorStatusType
    extends EntityType
{

    @XmlElementRef(name = "MirrorStatusItems", type = JAXBElement.class, required = false)
    protected JAXBElement<ArrayOfMirrorStatusItemType> mirrorStatusItems;
    @XmlElementRef(name = "StateCountSummary", type = JAXBElement.class, required = false)
    protected JAXBElement<MirrorStateCountSummaryDataType> stateCountSummary;
    @XmlElementRef(name = "MirrorErrors", type = JAXBElement.class, required = false)
    protected JAXBElement<ArrayOfMirrorErrorDataType> mirrorErrors;

    /**
     * Gets the value of the mirrorStatusItems property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link ArrayOfMirrorStatusItemType }{@code >}
     *     
     */
    public JAXBElement<ArrayOfMirrorStatusItemType> getMirrorStatusItems() {
        return mirrorStatusItems;
    }

    /**
     * Sets the value of the mirrorStatusItems property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link ArrayOfMirrorStatusItemType }{@code >}
     *     
     */
    public void setMirrorStatusItems(JAXBElement<ArrayOfMirrorStatusItemType> value) {
        this.mirrorStatusItems = value;
    }

    /**
     * Gets the value of the stateCountSummary property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link MirrorStateCountSummaryDataType }{@code >}
     *     
     */
    public JAXBElement<MirrorStateCountSummaryDataType> getStateCountSummary() {
        return stateCountSummary;
    }

    /**
     * Sets the value of the stateCountSummary property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link MirrorStateCountSummaryDataType }{@code >}
     *     
     */
    public void setStateCountSummary(JAXBElement<MirrorStateCountSummaryDataType> value) {
        this.stateCountSummary = value;
    }

    /**
     * Gets the value of the mirrorErrors property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link ArrayOfMirrorErrorDataType }{@code >}
     *     
     */
    public JAXBElement<ArrayOfMirrorErrorDataType> getMirrorErrors() {
        return mirrorErrors;
    }

    /**
     * Sets the value of the mirrorErrors property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link ArrayOfMirrorErrorDataType }{@code >}
     *     
     */
    public void setMirrorErrors(JAXBElement<ArrayOfMirrorErrorDataType> value) {
        this.mirrorErrors = value;
    }

}
