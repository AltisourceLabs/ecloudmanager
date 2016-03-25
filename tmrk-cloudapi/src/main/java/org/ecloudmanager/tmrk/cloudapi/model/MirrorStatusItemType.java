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
 * <p>Java class for MirrorStatusItemType complex type.
 * <p/>
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p/>
 * <pre>
 * &lt;complexType name="MirrorStatusItemType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="ComputePool" type="{}ReferenceType" minOccurs="0"/>
 *         &lt;element name="SourceLocation" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="DestinationLocation" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="LagTime" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="MirrorTimestamp" type="{http://www.w3.org/2001/XMLSchema}dateTime" minOccurs="0"/>
 *         &lt;element name="Status" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="State" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "MirrorStatusItemType", propOrder = {
    "computePool",
    "sourceLocation",
    "destinationLocation",
    "lagTime",
    "mirrorTimestamp",
    "status",
    "state"
})
public class MirrorStatusItemType {

    @XmlElementRef(name = "ComputePool", type = JAXBElement.class, required = false)
    protected JAXBElement<ReferenceType> computePool;
    @XmlElementRef(name = "SourceLocation", type = JAXBElement.class, required = false)
    protected JAXBElement<String> sourceLocation;
    @XmlElementRef(name = "DestinationLocation", type = JAXBElement.class, required = false)
    protected JAXBElement<String> destinationLocation;
    @XmlElementRef(name = "LagTime", type = JAXBElement.class, required = false)
    protected JAXBElement<String> lagTime;
    @XmlElement(name = "MirrorTimestamp")
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar mirrorTimestamp;
    @XmlElementRef(name = "Status", type = JAXBElement.class, required = false)
    protected JAXBElement<String> status;
    @XmlElementRef(name = "State", type = JAXBElement.class, required = false)
    protected JAXBElement<String> state;

    /**
     * Gets the value of the computePool property.
     *
     * @return possible object is
     * {@link JAXBElement }{@code <}{@link ReferenceType }{@code >}
     */
    public JAXBElement<ReferenceType> getComputePool() {
        return computePool;
    }

    /**
     * Sets the value of the computePool property.
     *
     * @param value allowed object is
     *              {@link JAXBElement }{@code <}{@link ReferenceType }{@code >}
     */
    public void setComputePool(JAXBElement<ReferenceType> value) {
        this.computePool = value;
    }

    /**
     * Gets the value of the sourceLocation property.
     *
     * @return possible object is
     * {@link JAXBElement }{@code <}{@link String }{@code >}
     */
    public JAXBElement<String> getSourceLocation() {
        return sourceLocation;
    }

    /**
     * Sets the value of the sourceLocation property.
     *
     * @param value allowed object is
     *              {@link JAXBElement }{@code <}{@link String }{@code >}
     */
    public void setSourceLocation(JAXBElement<String> value) {
        this.sourceLocation = value;
    }

    /**
     * Gets the value of the destinationLocation property.
     *
     * @return possible object is
     * {@link JAXBElement }{@code <}{@link String }{@code >}
     */
    public JAXBElement<String> getDestinationLocation() {
        return destinationLocation;
    }

    /**
     * Sets the value of the destinationLocation property.
     *
     * @param value allowed object is
     *              {@link JAXBElement }{@code <}{@link String }{@code >}
     */
    public void setDestinationLocation(JAXBElement<String> value) {
        this.destinationLocation = value;
    }

    /**
     * Gets the value of the lagTime property.
     *
     * @return possible object is
     * {@link JAXBElement }{@code <}{@link String }{@code >}
     */
    public JAXBElement<String> getLagTime() {
        return lagTime;
    }

    /**
     * Sets the value of the lagTime property.
     *
     * @param value allowed object is
     *              {@link JAXBElement }{@code <}{@link String }{@code >}
     */
    public void setLagTime(JAXBElement<String> value) {
        this.lagTime = value;
    }

    /**
     * Gets the value of the mirrorTimestamp property.
     *
     * @return possible object is
     * {@link XMLGregorianCalendar }
     */
    public XMLGregorianCalendar getMirrorTimestamp() {
        return mirrorTimestamp;
    }

    /**
     * Sets the value of the mirrorTimestamp property.
     *
     * @param value allowed object is
     *              {@link XMLGregorianCalendar }
     */
    public void setMirrorTimestamp(XMLGregorianCalendar value) {
        this.mirrorTimestamp = value;
    }

    /**
     * Gets the value of the status property.
     *
     * @return possible object is
     * {@link JAXBElement }{@code <}{@link String }{@code >}
     */
    public JAXBElement<String> getStatus() {
        return status;
    }

    /**
     * Sets the value of the status property.
     *
     * @param value allowed object is
     *              {@link JAXBElement }{@code <}{@link String }{@code >}
     */
    public void setStatus(JAXBElement<String> value) {
        this.status = value;
    }

    /**
     * Gets the value of the state property.
     *
     * @return possible object is
     * {@link JAXBElement }{@code <}{@link String }{@code >}
     */
    public JAXBElement<String> getState() {
        return state;
    }

    /**
     * Sets the value of the state property.
     *
     * @param value allowed object is
     *              {@link JAXBElement }{@code <}{@link String }{@code >}
     */
    public void setState(JAXBElement<String> value) {
        this.state = value;
    }

}
