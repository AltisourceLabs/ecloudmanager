//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911.1802 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2015.07.16 at 03:24:22 PM EEST 
//


package org.ecloudmanager.tmrk.cloudapi.model;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.*;
import javax.xml.datatype.XMLGregorianCalendar;


/**
 * <p>Java class for UserType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="UserType">
 *   &lt;complexContent>
 *     &lt;extension base="{}EntityType">
 *       &lt;sequence>
 *         &lt;element name="FirstName" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="LastName" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="Email" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="Status" type="{}UserStatus" minOccurs="0"/>
 *         &lt;element name="LoginStatus" type="{}UserLoginStatus" minOccurs="0"/>
 *         &lt;element name="LastLogin" type="{http://www.w3.org/2001/XMLSchema}dateTime" minOccurs="0"/>
 *         &lt;element name="UserRole" type="{}UserRoleType" minOccurs="0"/>
 *         &lt;element name="MultifactorAuthentication" type="{}MultifactorAuthenticationType" minOccurs="0"/>
 *         &lt;element name="IsAdministrator" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="IsApiUser" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="IsAlertNotificationEnabled" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="IsMultifactorAuthenticationEnabled" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="ApiKeys" type="{}ApiKeysType" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "UserType", propOrder = {
    "firstName",
    "lastName",
    "email",
    "status",
    "loginStatus",
    "lastLogin",
    "userRole",
    "multifactorAuthentication",
    "isAdministrator",
    "isApiUser",
    "isAlertNotificationEnabled",
    "isMultifactorAuthenticationEnabled",
    "apiKeys"
})
public class UserType
    extends EntityType
{

    @XmlElementRef(name = "FirstName", type = JAXBElement.class, required = false)
    protected JAXBElement<String> firstName;
    @XmlElementRef(name = "LastName", type = JAXBElement.class, required = false)
    protected JAXBElement<String> lastName;
    @XmlElementRef(name = "Email", type = JAXBElement.class, required = false)
    protected JAXBElement<String> email;
    @XmlElement(name = "Status")
    @XmlSchemaType(name = "string")
    protected UserStatus status;
    @XmlElement(name = "LoginStatus")
    @XmlSchemaType(name = "string")
    protected UserLoginStatus loginStatus;
    @XmlElementRef(name = "LastLogin", type = JAXBElement.class, required = false)
    protected JAXBElement<XMLGregorianCalendar> lastLogin;
    @XmlElementRef(name = "UserRole", type = JAXBElement.class, required = false)
    protected JAXBElement<UserRoleType> userRole;
    @XmlElementRef(name = "MultifactorAuthentication", type = JAXBElement.class, required = false)
    protected JAXBElement<MultifactorAuthenticationType> multifactorAuthentication;
    @XmlElement(name = "IsAdministrator")
    protected Boolean isAdministrator;
    @XmlElement(name = "IsApiUser")
    protected Boolean isApiUser;
    @XmlElement(name = "IsAlertNotificationEnabled")
    protected Boolean isAlertNotificationEnabled;
    @XmlElement(name = "IsMultifactorAuthenticationEnabled")
    protected Boolean isMultifactorAuthenticationEnabled;
    @XmlElementRef(name = "ApiKeys", type = JAXBElement.class, required = false)
    protected JAXBElement<ApiKeysType> apiKeys;

    /**
     * Gets the value of the firstName property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public JAXBElement<String> getFirstName() {
        return firstName;
    }

    /**
     * Sets the value of the firstName property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public void setFirstName(JAXBElement<String> value) {
        this.firstName = value;
    }

    /**
     * Gets the value of the lastName property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public JAXBElement<String> getLastName() {
        return lastName;
    }

    /**
     * Sets the value of the lastName property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public void setLastName(JAXBElement<String> value) {
        this.lastName = value;
    }

    /**
     * Gets the value of the email property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public JAXBElement<String> getEmail() {
        return email;
    }

    /**
     * Sets the value of the email property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public void setEmail(JAXBElement<String> value) {
        this.email = value;
    }

    /**
     * Gets the value of the status property.
     * 
     * @return
     *     possible object is
     *     {@link UserStatus }
     *     
     */
    public UserStatus getStatus() {
        return status;
    }

    /**
     * Sets the value of the status property.
     * 
     * @param value
     *     allowed object is
     *     {@link UserStatus }
     *     
     */
    public void setStatus(UserStatus value) {
        this.status = value;
    }

    /**
     * Gets the value of the loginStatus property.
     * 
     * @return
     *     possible object is
     *     {@link UserLoginStatus }
     *     
     */
    public UserLoginStatus getLoginStatus() {
        return loginStatus;
    }

    /**
     * Sets the value of the loginStatus property.
     * 
     * @param value
     *     allowed object is
     *     {@link UserLoginStatus }
     *     
     */
    public void setLoginStatus(UserLoginStatus value) {
        this.loginStatus = value;
    }

    /**
     * Gets the value of the lastLogin property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link XMLGregorianCalendar }{@code >}
     *     
     */
    public JAXBElement<XMLGregorianCalendar> getLastLogin() {
        return lastLogin;
    }

    /**
     * Sets the value of the lastLogin property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link XMLGregorianCalendar }{@code >}
     *     
     */
    public void setLastLogin(JAXBElement<XMLGregorianCalendar> value) {
        this.lastLogin = value;
    }

    /**
     * Gets the value of the userRole property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link UserRoleType }{@code >}
     *     
     */
    public JAXBElement<UserRoleType> getUserRole() {
        return userRole;
    }

    /**
     * Sets the value of the userRole property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link UserRoleType }{@code >}
     *     
     */
    public void setUserRole(JAXBElement<UserRoleType> value) {
        this.userRole = value;
    }

    /**
     * Gets the value of the multifactorAuthentication property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link MultifactorAuthenticationType }{@code >}
     *     
     */
    public JAXBElement<MultifactorAuthenticationType> getMultifactorAuthentication() {
        return multifactorAuthentication;
    }

    /**
     * Sets the value of the multifactorAuthentication property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link MultifactorAuthenticationType }{@code >}
     *     
     */
    public void setMultifactorAuthentication(JAXBElement<MultifactorAuthenticationType> value) {
        this.multifactorAuthentication = value;
    }

    /**
     * Gets the value of the isAdministrator property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isIsAdministrator() {
        return isAdministrator;
    }

    /**
     * Sets the value of the isAdministrator property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setIsAdministrator(Boolean value) {
        this.isAdministrator = value;
    }

    /**
     * Gets the value of the isApiUser property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isIsApiUser() {
        return isApiUser;
    }

    /**
     * Sets the value of the isApiUser property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setIsApiUser(Boolean value) {
        this.isApiUser = value;
    }

    /**
     * Gets the value of the isAlertNotificationEnabled property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isIsAlertNotificationEnabled() {
        return isAlertNotificationEnabled;
    }

    /**
     * Sets the value of the isAlertNotificationEnabled property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setIsAlertNotificationEnabled(Boolean value) {
        this.isAlertNotificationEnabled = value;
    }

    /**
     * Gets the value of the isMultifactorAuthenticationEnabled property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isIsMultifactorAuthenticationEnabled() {
        return isMultifactorAuthenticationEnabled;
    }

    /**
     * Sets the value of the isMultifactorAuthenticationEnabled property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setIsMultifactorAuthenticationEnabled(Boolean value) {
        this.isMultifactorAuthenticationEnabled = value;
    }

    /**
     * Gets the value of the apiKeys property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link ApiKeysType }{@code >}
     *     
     */
    public JAXBElement<ApiKeysType> getApiKeys() {
        return apiKeys;
    }

    /**
     * Sets the value of the apiKeys property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link ApiKeysType }{@code >}
     *     
     */
    public void setApiKeys(JAXBElement<ApiKeysType> value) {
        this.apiKeys = value;
    }

}
