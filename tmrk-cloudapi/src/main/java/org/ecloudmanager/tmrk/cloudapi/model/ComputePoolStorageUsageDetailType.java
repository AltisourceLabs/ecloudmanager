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
 * <p>Java class for ComputePoolStorageUsageDetailType complex type.
 * <p/>
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p/>
 * <pre>
 * &lt;complexType name="ComputePoolStorageUsageDetailType">
 *   &lt;complexContent>
 *     &lt;extension base="{}ResourceType">
 *       &lt;sequence>
 *         &lt;element name="Allocated" type="{}ResourceUnitType" minOccurs="0"/>
 *         &lt;element name="VirtualMachines" type="{}StorageDetails_VirtualMachinesType" minOccurs="0"/>
 *         &lt;element name="DetachedDisks" type="{}DetachedDiskReferenceListType" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ComputePoolStorageUsageDetailType", propOrder = {
    "allocated",
    "virtualMachines",
    "detachedDisks"
})
public class ComputePoolStorageUsageDetailType
    extends ResourceType {

    @XmlElementRef(name = "Allocated", type = JAXBElement.class, required = false)
    protected JAXBElement<ResourceUnitType> allocated;
    @XmlElementRef(name = "VirtualMachines", type = JAXBElement.class, required = false)
    protected JAXBElement<StorageDetailsVirtualMachinesType> virtualMachines;
    @XmlElementRef(name = "DetachedDisks", type = JAXBElement.class, required = false)
    protected JAXBElement<DetachedDiskReferenceListType> detachedDisks;

    /**
     * Gets the value of the allocated property.
     *
     * @return possible object is
     * {@link JAXBElement }{@code <}{@link ResourceUnitType }{@code >}
     */
    public JAXBElement<ResourceUnitType> getAllocated() {
        return allocated;
    }

    /**
     * Sets the value of the allocated property.
     *
     * @param value allowed object is
     *              {@link JAXBElement }{@code <}{@link ResourceUnitType }{@code >}
     */
    public void setAllocated(JAXBElement<ResourceUnitType> value) {
        this.allocated = value;
    }

    /**
     * Gets the value of the virtualMachines property.
     *
     * @return possible object is
     * {@link JAXBElement }{@code <}{@link StorageDetailsVirtualMachinesType }{@code >}
     */
    public JAXBElement<StorageDetailsVirtualMachinesType> getVirtualMachines() {
        return virtualMachines;
    }

    /**
     * Sets the value of the virtualMachines property.
     *
     * @param value allowed object is
     *              {@link JAXBElement }{@code <}{@link StorageDetailsVirtualMachinesType }{@code >}
     */
    public void setVirtualMachines(JAXBElement<StorageDetailsVirtualMachinesType> value) {
        this.virtualMachines = value;
    }

    /**
     * Gets the value of the detachedDisks property.
     *
     * @return possible object is
     * {@link JAXBElement }{@code <}{@link DetachedDiskReferenceListType }{@code >}
     */
    public JAXBElement<DetachedDiskReferenceListType> getDetachedDisks() {
        return detachedDisks;
    }

    /**
     * Sets the value of the detachedDisks property.
     *
     * @param value allowed object is
     *              {@link JAXBElement }{@code <}{@link DetachedDiskReferenceListType }{@code >}
     */
    public void setDetachedDisks(JAXBElement<DetachedDiskReferenceListType> value) {
        this.detachedDisks = value;
    }

}
