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
 * <p>Java class for NetworkHostIpAddressType complex type.
 * <p/>
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p/>
 * <pre>
 * &lt;complexType name="NetworkHostIpAddressType">
 *   &lt;complexContent>
 *     &lt;extension base="{}ReferenceType">
 *       &lt;sequence>
 *         &lt;element name="NodeServices" type="{}ArrayOfNodeServiceType" minOccurs="0"/>
 *         &lt;element name="FirewallAcls" type="{}ArrayOfFirewallAclType" minOccurs="0"/>
 *         &lt;element name="FirewallLog" type="{}ReferenceType" minOccurs="0"/>
 *         &lt;element name="Rnat" type="{}ReferenceType" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "NetworkHostIpAddressType", propOrder = {
    "nodeServices",
    "firewallAcls",
    "firewallLog",
    "rnat"
})
public class NetworkHostIpAddressType
    extends ReferenceType {

    /**
     *
     */
    private static final long serialVersionUID = -7367679477752498226L;
    @XmlElementRef(name = "NodeServices", type = JAXBElement.class, required = false)
    protected JAXBElement<ArrayOfNodeServiceType> nodeServices;
    @XmlElementRef(name = "FirewallAcls", type = JAXBElement.class, required = false)
    protected JAXBElement<ArrayOfFirewallAclType> firewallAcls;
    @XmlElementRef(name = "FirewallLog", type = JAXBElement.class, required = false)
    protected JAXBElement<ReferenceType> firewallLog;
    @XmlElementRef(name = "Rnat", type = JAXBElement.class, required = false)
    protected JAXBElement<ReferenceType> rnat;

    /**
     * Gets the value of the nodeServices property.
     *
     * @return possible object is
     * {@link JAXBElement }{@code <}{@link ArrayOfNodeServiceType }{@code >}
     */
    public JAXBElement<ArrayOfNodeServiceType> getNodeServices() {
        return nodeServices;
    }

    /**
     * Sets the value of the nodeServices property.
     *
     * @param value allowed object is
     *              {@link JAXBElement }{@code <}{@link ArrayOfNodeServiceType }{@code >}
     */
    public void setNodeServices(JAXBElement<ArrayOfNodeServiceType> value) {
        this.nodeServices = value;
    }

    /**
     * Gets the value of the firewallAcls property.
     *
     * @return possible object is
     * {@link JAXBElement }{@code <}{@link ArrayOfFirewallAclType }{@code >}
     */
    public JAXBElement<ArrayOfFirewallAclType> getFirewallAcls() {
        return firewallAcls;
    }

    /**
     * Sets the value of the firewallAcls property.
     *
     * @param value allowed object is
     *              {@link JAXBElement }{@code <}{@link ArrayOfFirewallAclType }{@code >}
     */
    public void setFirewallAcls(JAXBElement<ArrayOfFirewallAclType> value) {
        this.firewallAcls = value;
    }

    /**
     * Gets the value of the firewallLog property.
     *
     * @return possible object is
     * {@link JAXBElement }{@code <}{@link ReferenceType }{@code >}
     */
    public JAXBElement<ReferenceType> getFirewallLog() {
        return firewallLog;
    }

    /**
     * Sets the value of the firewallLog property.
     *
     * @param value allowed object is
     *              {@link JAXBElement }{@code <}{@link ReferenceType }{@code >}
     */
    public void setFirewallLog(JAXBElement<ReferenceType> value) {
        this.firewallLog = value;
    }

    /**
     * Gets the value of the rnat property.
     *
     * @return possible object is
     * {@link JAXBElement }{@code <}{@link ReferenceType }{@code >}
     */
    public JAXBElement<ReferenceType> getRnat() {
        return rnat;
    }

    /**
     * Sets the value of the rnat property.
     *
     * @param value allowed object is
     *              {@link JAXBElement }{@code <}{@link ReferenceType }{@code >}
     */
    public void setRnat(JAXBElement<ReferenceType> value) {
        this.rnat = value;
    }

}
