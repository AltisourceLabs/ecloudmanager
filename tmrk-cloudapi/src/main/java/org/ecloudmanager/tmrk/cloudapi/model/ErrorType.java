//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911
// .1802
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2015.07.15 at 11:53:24 PM EEST 
//


package org.ecloudmanager.tmrk.cloudapi.model;

import javax.xml.bind.annotation.*;
import java.io.Serializable;


/**
 * <p>Java class for anonymous complex type.
 * <p/>
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p/>
 * <pre>
 * &lt;complexType>
 *   &lt;simpleContent>
 *     &lt;extension base="&lt;http://www.w3.org/2001/XMLSchema>string">
 *       &lt;attribute name="message" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="majorErrorCode" type="{http://www.w3.org/2001/XMLSchema}short" />
 *       &lt;attribute name="minorErrorCode" type="{http://www.w3.org/2001/XMLSchema}string" />
 *     &lt;/extension>
 *   &lt;/simpleContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "value"
})
@XmlRootElement(name = "Error")
public class ErrorType implements Serializable {

    private static final long serialVersionUID = -7602659524305908875L;

    @XmlValue
    protected String value;
    @XmlAttribute(name = "message")
    protected String message;
    @XmlAttribute(name = "majorErrorCode")
    protected Short majorErrorCode;
    @XmlAttribute(name = "minorErrorCode")
    protected String minorErrorCode;

    /**
     * Gets the value of the value property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getValue() {
        return value;
    }

    /**
     * Sets the value of the value property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setValue(String value) {
        this.value = value;
    }

    /**
     * Gets the value of the message property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getMessage() {
        return message;
    }

    /**
     * Sets the value of the message property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setMessage(String value) {
        this.message = value;
    }

    /**
     * Gets the value of the majorErrorCode property.
     *
     * @return possible object is
     * {@link Short }
     */
    public Short getMajorErrorCode() {
        return majorErrorCode;
    }

    /**
     * Sets the value of the majorErrorCode property.
     *
     * @param value allowed object is
     *              {@link Short }
     */
    public void setMajorErrorCode(Short value) {
        this.majorErrorCode = value;
    }

    /**
     * Gets the value of the minorErrorCode property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getMinorErrorCode() {
        return minorErrorCode;
    }

    /**
     * Sets the value of the minorErrorCode property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setMinorErrorCode(String value) {
        this.minorErrorCode = value;
    }

}
