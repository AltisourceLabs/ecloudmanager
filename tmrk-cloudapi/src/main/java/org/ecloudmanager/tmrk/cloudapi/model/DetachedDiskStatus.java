//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911
// .1802
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2015.07.16 at 03:24:22 PM EEST 
//


package org.ecloudmanager.tmrk.cloudapi.model;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for DetachedDiskStatus.
 * <p/>
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p/>
 * <pre>
 * &lt;simpleType name="DetachedDiskStatus">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="Available"/>
 *     &lt;enumeration value="AttachInProgress"/>
 *     &lt;enumeration value="DetachInProgress"/>
 *     &lt;enumeration value="DeleteInProgress"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 */
@XmlType(name = "DetachedDiskStatus")
@XmlEnum
public enum DetachedDiskStatus {

    @XmlEnumValue("Available")
    AVAILABLE("Available"),
    @XmlEnumValue("AttachInProgress")
    ATTACH_IN_PROGRESS("AttachInProgress"),
    @XmlEnumValue("DetachInProgress")
    DETACH_IN_PROGRESS("DetachInProgress"),
    @XmlEnumValue("DeleteInProgress")
    DELETE_IN_PROGRESS("DeleteInProgress");
    private final String value;

    DetachedDiskStatus(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static DetachedDiskStatus fromValue(String v) {
        for (DetachedDiskStatus c : DetachedDiskStatus.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
