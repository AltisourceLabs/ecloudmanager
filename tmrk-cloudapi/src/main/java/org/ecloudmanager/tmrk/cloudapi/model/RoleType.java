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


/**
 * <p>Java class for RoleType complex type.
 * <p/>
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p/>
 * <pre>
 * &lt;complexType name="RoleType">
 *   &lt;complexContent>
 *     &lt;extension base="{}ResourceType">
 *       &lt;sequence>
 *         &lt;element name="RoleType" type="{}RoleTypeEnum" minOccurs="0"/>
 *         &lt;element name="Active" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="Category" type="{}RoleCategory" minOccurs="0"/>
 *         &lt;element name="IsAdminRole" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="BusinessOperations" type="{}BusinessOperationReferencesType" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "RoleType", propOrder = {
    "roleType",
    "active",
    "category",
    "isAdminRole",
    "businessOperations"
})
public class RoleType
    extends ResourceType {

    @XmlElement(name = "RoleType")
    @XmlSchemaType(name = "string")
    protected RoleTypeEnum roleType;
    @XmlElement(name = "Active")
    protected Boolean active;
    @XmlElement(name = "Category")
    @XmlSchemaType(name = "string")
    protected RoleCategory category;
    @XmlElement(name = "IsAdminRole")
    protected Boolean isAdminRole;
    @XmlElementRef(name = "BusinessOperations", type = JAXBElement.class, required = false)
    protected JAXBElement<BusinessOperationReferencesType> businessOperations;

    /**
     * Gets the value of the roleType property.
     *
     * @return possible object is
     * {@link RoleTypeEnum }
     */
    public RoleTypeEnum getRoleType() {
        return roleType;
    }

    /**
     * Sets the value of the roleType property.
     *
     * @param value allowed object is
     *              {@link RoleTypeEnum }
     */
    public void setRoleType(RoleTypeEnum value) {
        this.roleType = value;
    }

    /**
     * Gets the value of the active property.
     *
     * @return possible object is
     * {@link Boolean }
     */
    public Boolean isActive() {
        return active;
    }

    /**
     * Sets the value of the active property.
     *
     * @param value allowed object is
     *              {@link Boolean }
     */
    public void setActive(Boolean value) {
        this.active = value;
    }

    /**
     * Gets the value of the category property.
     *
     * @return possible object is
     * {@link RoleCategory }
     */
    public RoleCategory getCategory() {
        return category;
    }

    /**
     * Sets the value of the category property.
     *
     * @param value allowed object is
     *              {@link RoleCategory }
     */
    public void setCategory(RoleCategory value) {
        this.category = value;
    }

    /**
     * Gets the value of the isAdminRole property.
     *
     * @return possible object is
     * {@link Boolean }
     */
    public Boolean isIsAdminRole() {
        return isAdminRole;
    }

    /**
     * Sets the value of the isAdminRole property.
     *
     * @param value allowed object is
     *              {@link Boolean }
     */
    public void setIsAdminRole(Boolean value) {
        this.isAdminRole = value;
    }

    /**
     * Gets the value of the businessOperations property.
     *
     * @return possible object is
     * {@link JAXBElement }{@code <}{@link BusinessOperationReferencesType }{@code >}
     */
    public JAXBElement<BusinessOperationReferencesType> getBusinessOperations() {
        return businessOperations;
    }

    /**
     * Sets the value of the businessOperations property.
     *
     * @param value allowed object is
     *              {@link JAXBElement }{@code <}{@link BusinessOperationReferencesType }{@code >}
     */
    public void setBusinessOperations(JAXBElement<BusinessOperationReferencesType> value) {
        this.businessOperations = value;
    }

}
