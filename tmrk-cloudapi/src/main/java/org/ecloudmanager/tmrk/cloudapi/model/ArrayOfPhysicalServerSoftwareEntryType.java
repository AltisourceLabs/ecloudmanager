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
import java.util.ArrayList;
import java.util.List;


/**
 * <p>Java class for ArrayOfPhysicalServerSoftwareEntryType complex type.
 * <p/>
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p/>
 * <pre>
 * &lt;complexType name="ArrayOfPhysicalServerSoftwareEntryType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="PhysicalServerSoftwareEntry" type="{}PhysicalServerSoftwareEntryType"
 *         maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ArrayOfPhysicalServerSoftwareEntryType", propOrder = {
    "physicalServerSoftwareEntry"
})
public class ArrayOfPhysicalServerSoftwareEntryType {

    @XmlElement(name = "PhysicalServerSoftwareEntry", nillable = true)
    protected List<PhysicalServerSoftwareEntryType> physicalServerSoftwareEntry;

    /**
     * Gets the value of the physicalServerSoftwareEntry property.
     * <p/>
     * <p/>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the physicalServerSoftwareEntry property.
     * <p/>
     * <p/>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getPhysicalServerSoftwareEntry().add(newItem);
     * </pre>
     * <p/>
     * <p/>
     * <p/>
     * Objects of the following type(s) are allowed in the list
     * {@link PhysicalServerSoftwareEntryType }
     */
    public List<PhysicalServerSoftwareEntryType> getPhysicalServerSoftwareEntry() {
        if (physicalServerSoftwareEntry == null) {
            physicalServerSoftwareEntry = new ArrayList<PhysicalServerSoftwareEntryType>();
        }
        return this.physicalServerSoftwareEntry;
    }

}
