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
 * <p>Java class for AlertRuleGroup.
 * <p/>
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p/>
 * <pre>
 * &lt;simpleType name="AlertRuleGroup">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="None"/>
 *     &lt;enumeration value="Resources"/>
 *     &lt;enumeration value="Cloud"/>
 *     &lt;enumeration value="Billing"/>
 *     &lt;enumeration value="Devices"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 */
@XmlType(name = "AlertRuleGroup")
@XmlEnum
public enum AlertRuleGroup {

    @XmlEnumValue("None")
    NONE("None"),
    @XmlEnumValue("Resources")
    RESOURCES("Resources"),
    @XmlEnumValue("Cloud")
    CLOUD("Cloud"),
    @XmlEnumValue("Billing")
    BILLING("Billing"),
    @XmlEnumValue("Devices")
    DEVICES("Devices");
    private final String value;

    AlertRuleGroup(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static AlertRuleGroup fromValue(String v) {
        for (AlertRuleGroup c : AlertRuleGroup.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
