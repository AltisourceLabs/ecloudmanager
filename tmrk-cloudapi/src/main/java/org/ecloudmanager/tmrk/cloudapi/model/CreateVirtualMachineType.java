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


/**
 * <p>Java class for CreateVirtualMachineType complex type.
 * <p/>
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p/>
 * <pre>
 * &lt;complexType name="CreateVirtualMachineType">
 *   &lt;complexContent>
 *     &lt;extension base="{}CreateOsTemplateVirtualMachineRequestType">
 *       &lt;sequence>
 *         &lt;element name="Template" type="{}ReferenceType"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CreateVirtualMachineType", propOrder = {
    "template"
})
public class CreateVirtualMachineType
    extends CreateOsTemplateVirtualMachineRequestType {

    @XmlElement(name = "Template", required = true, nillable = true)
    protected ReferenceType template;

    /**
     * Gets the value of the template property.
     *
     * @return possible object is
     * {@link ReferenceType }
     */
    public ReferenceType getTemplate() {
        return template;
    }

    /**
     * Sets the value of the template property.
     *
     * @param value allowed object is
     *              {@link ReferenceType }
     */
    public void setTemplate(ReferenceType value) {
        this.template = value;
    }

}
