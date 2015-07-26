package se.ivankrizsan.mule.jerseylogging.entities;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * Customer entity class.
 *
 * @author Ivan krizsan
 */
@XmlRootElement
public class Customer {
    /* Constant(s): */

    /* Instance variable(s): */
    protected Long id;
    protected String firstName;
    protected String lastName;
    protected String streetName;
    protected String city;

    public Long getId() {
        return id;
    }

    public void setId(final Long inId) {
        id = inId;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(final String inFirstName) {
        firstName = inFirstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(final String inLastName) {
        lastName = inLastName;
    }

    public String getStreetName() {
        return streetName;
    }

    public void setStreetName(final String inStreetName) {
        streetName = inStreetName;
    }

    public String getCity() {
        return city;
    }

    public void setCity(final String inCity) {
        city = inCity;
    }
}
