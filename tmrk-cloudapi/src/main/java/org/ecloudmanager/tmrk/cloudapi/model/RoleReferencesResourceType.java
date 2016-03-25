//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911
// .1802
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2015.07.16 at 03:24:22 PM EEST 
//


package org.ecloudmanager.tmrk.cloudapi.model;

import javax.xml.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;


/**
 * <p>Java class for RoleReferencesResourceType complex type.
 * <p/>
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p/>
 * <pre>
 * &lt;complexType name="RoleReferencesResourceType">
 *   &lt;complexContent>
 *     &lt;extension base="{}ResourceType">
 *       &lt;sequence>
 *         &lt;element name="Role" type="{}RoleReferenceType" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "RoleReferencesResourceType", propOrder = {
    "role"
})
@XmlSeeAlso({
    RolesType.class
})
public class RoleReferencesResourceType
    extends ResourceType {

    @XmlElement(name = "Role", nillable = true)
    protected List<RoleReferenceType> role;

    /**
     * Gets the value of the role property.
     * <p/>
     * <p/>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the role property.
     * <p/>
     * <p/>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getRole().add(newItem);
     * </pre>
     * <p/>
     * <p/>
     * <p/>
     * Objects of the following type(s) are allowed in the list
     * {@link RoleReferenceType }
     */
    public List<RoleReferenceType> getRole() {
        if (role == null) {
            role = new ArrayList<RoleReferenceType>();
        }
        return this.role;
    }

}
