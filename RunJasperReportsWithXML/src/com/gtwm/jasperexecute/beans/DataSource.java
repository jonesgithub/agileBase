//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.0.3-b24-fcs 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2008.01.08 at 08:37:26 PM CET 
//


package com.gtwm.jasperexecute.beans;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for DataSource complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="DataSource">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="name" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="type" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="dbhostname" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="dbname" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="dbPort" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="dbuser" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="dbpassword" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="dbprovider" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "DataSource", propOrder = {
    "name",
    "type",
    "dbhostname",
    "dbname",
    "dbPort",
    "dbuser",
    "dbpassword",
    "dbprovider"
})
public class DataSource {

    @XmlElement(required = true)
    protected String name;
    @XmlElement(required = true)
    protected String type;
    @XmlElement(required = true)
    protected String dbhostname;
    @XmlElement(required = true)
    protected String dbname;
    @XmlElement(required = true)
    protected String dbPort;
    @XmlElement(required = true)
    protected String dbuser;
    @XmlElement(required = true)
    protected String dbpassword;
    @XmlElement(required = true)
    protected String dbprovider;

    /**
     * Gets the value of the name property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the value of the name property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setName(String value) {
        this.name = value;
    }

    /**
     * Gets the value of the type property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getType() {
        return type;
    }

    /**
     * Sets the value of the type property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setType(String value) {
        this.type = value;
    }

    /**
     * Gets the value of the dbhostname property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDbhostname() {
        return dbhostname;
    }

    /**
     * Sets the value of the dbhostname property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDbhostname(String value) {
        this.dbhostname = value;
    }

    /**
     * Gets the value of the dbname property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDbname() {
        return dbname;
    }

    /**
     * Sets the value of the dbname property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDbname(String value) {
        this.dbname = value;
    }

    /**
     * Gets the value of the dbPort property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDbPort() {
        return dbPort;
    }

    /**
     * Sets the value of the dbPort property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDbPort(String value) {
        this.dbPort = value;
    }

    /**
     * Gets the value of the dbuser property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDbuser() {
        return dbuser;
    }

    /**
     * Sets the value of the dbuser property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDbuser(String value) {
        this.dbuser = value;
    }

    /**
     * Gets the value of the dbpassword property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDbpassword() {
        return dbpassword;
    }

    /**
     * Sets the value of the dbpassword property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDbpassword(String value) {
        this.dbpassword = value;
    }

    /**
     * Gets the value of the dbprovider property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDbprovider() {
        return dbprovider;
    }

    /**
     * Sets the value of the dbprovider property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDbprovider(String value) {
        this.dbprovider = value;
    }

}
