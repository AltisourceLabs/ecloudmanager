//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911
// .1802
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2015.07.16 at 03:24:22 PM EEST 
//


package org.ecloudmanager.tmrk.cloudapi.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for MirrorStateCountSummaryDataType complex type.
 * <p/>
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p/>
 * <pre>
 * &lt;complexType name="MirrorStateCountSummaryDataType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="StateUnitializedCount" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="StateSnapMirroredCount" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="StateBrokenOffCount" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="StateQuiescedCount" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="StateSourceCount" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="StateUnknownCount" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "MirrorStateCountSummaryDataType", propOrder = {
    "stateUnitializedCount",
    "stateSnapMirroredCount",
    "stateBrokenOffCount",
    "stateQuiescedCount",
    "stateSourceCount",
    "stateUnknownCount"
})
public class MirrorStateCountSummaryDataType {

    @XmlElement(name = "StateUnitializedCount")
    protected Integer stateUnitializedCount;
    @XmlElement(name = "StateSnapMirroredCount")
    protected Integer stateSnapMirroredCount;
    @XmlElement(name = "StateBrokenOffCount")
    protected Integer stateBrokenOffCount;
    @XmlElement(name = "StateQuiescedCount")
    protected Integer stateQuiescedCount;
    @XmlElement(name = "StateSourceCount")
    protected Integer stateSourceCount;
    @XmlElement(name = "StateUnknownCount")
    protected Integer stateUnknownCount;

    /**
     * Gets the value of the stateUnitializedCount property.
     *
     * @return possible object is
     * {@link Integer }
     */
    public Integer getStateUnitializedCount() {
        return stateUnitializedCount;
    }

    /**
     * Sets the value of the stateUnitializedCount property.
     *
     * @param value allowed object is
     *              {@link Integer }
     */
    public void setStateUnitializedCount(Integer value) {
        this.stateUnitializedCount = value;
    }

    /**
     * Gets the value of the stateSnapMirroredCount property.
     *
     * @return possible object is
     * {@link Integer }
     */
    public Integer getStateSnapMirroredCount() {
        return stateSnapMirroredCount;
    }

    /**
     * Sets the value of the stateSnapMirroredCount property.
     *
     * @param value allowed object is
     *              {@link Integer }
     */
    public void setStateSnapMirroredCount(Integer value) {
        this.stateSnapMirroredCount = value;
    }

    /**
     * Gets the value of the stateBrokenOffCount property.
     *
     * @return possible object is
     * {@link Integer }
     */
    public Integer getStateBrokenOffCount() {
        return stateBrokenOffCount;
    }

    /**
     * Sets the value of the stateBrokenOffCount property.
     *
     * @param value allowed object is
     *              {@link Integer }
     */
    public void setStateBrokenOffCount(Integer value) {
        this.stateBrokenOffCount = value;
    }

    /**
     * Gets the value of the stateQuiescedCount property.
     *
     * @return possible object is
     * {@link Integer }
     */
    public Integer getStateQuiescedCount() {
        return stateQuiescedCount;
    }

    /**
     * Sets the value of the stateQuiescedCount property.
     *
     * @param value allowed object is
     *              {@link Integer }
     */
    public void setStateQuiescedCount(Integer value) {
        this.stateQuiescedCount = value;
    }

    /**
     * Gets the value of the stateSourceCount property.
     *
     * @return possible object is
     * {@link Integer }
     */
    public Integer getStateSourceCount() {
        return stateSourceCount;
    }

    /**
     * Sets the value of the stateSourceCount property.
     *
     * @param value allowed object is
     *              {@link Integer }
     */
    public void setStateSourceCount(Integer value) {
        this.stateSourceCount = value;
    }

    /**
     * Gets the value of the stateUnknownCount property.
     *
     * @return possible object is
     * {@link Integer }
     */
    public Integer getStateUnknownCount() {
        return stateUnknownCount;
    }

    /**
     * Sets the value of the stateUnknownCount property.
     *
     * @param value allowed object is
     *              {@link Integer }
     */
    public void setStateUnknownCount(Integer value) {
        this.stateUnknownCount = value;
    }

}
