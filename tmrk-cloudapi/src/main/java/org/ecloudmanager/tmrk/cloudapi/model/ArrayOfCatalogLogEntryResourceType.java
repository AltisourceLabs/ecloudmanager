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
 * <p>Java class for ArrayOfCatalogLogEntryResourceType complex type.
 * <p/>
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p/>
 * <pre>
 * &lt;complexType name="ArrayOfCatalogLogEntryResourceType">
 *   &lt;complexContent>
 *     &lt;extension base="{}ResourceType">
 *       &lt;sequence>
 *         &lt;element name="CatalogLogEntry" type="{}CatalogLogEntryType" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ArrayOfCatalogLogEntryResourceType", propOrder = {
    "catalogLogEntry"
})
@XmlSeeAlso({
    CatalogLogType.class
})
public class ArrayOfCatalogLogEntryResourceType
    extends ResourceType {

    @XmlElement(name = "CatalogLogEntry", nillable = true)
    protected List<CatalogLogEntryType> catalogLogEntry;

    /**
     * Gets the value of the catalogLogEntry property.
     * <p/>
     * <p/>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the catalogLogEntry property.
     * <p/>
     * <p/>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getCatalogLogEntry().add(newItem);
     * </pre>
     * <p/>
     * <p/>
     * <p/>
     * Objects of the following type(s) are allowed in the list
     * {@link CatalogLogEntryType }
     */
    public List<CatalogLogEntryType> getCatalogLogEntry() {
        if (catalogLogEntry == null) {
            catalogLogEntry = new ArrayList<CatalogLogEntryType>();
        }
        return this.catalogLogEntry;
    }

}
