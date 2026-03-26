package entities.fmt;

import java.util.ArrayList;
import java.util.List;

/**
 * This class represents a person in the FarMart system
 * 
 * @author lucyb
 *
 */
public class Person implements Comparable<Person> {

    private int personId;
    private String personCode;
    private String firstName;
    private String lastName;
    private Address address;
    private List<String> emails;

    /**
     * Constructor for Person without personId (used for CSV parsing)
     * 
     * @param personCode
     * @param firstName
     * @param lastName
     * @param address
     */
    public Person(String personCode, String firstName, String lastName, Address address) {
        this.personCode = personCode;
        this.firstName = firstName;
        this.lastName = lastName;
        this.address = address;
        this.emails = new ArrayList<>();
    }

    /**
     * Constructor for Person with personId (used for database loading)
     * 
     * @param personId
     * @param personCode
     * @param lastName
     * @param firstName
     * @param address
     */
    public Person(int personId, String personCode, String lastName, String firstName, Address address) {
        this.personId = personId;
        this.personCode = personCode;
        this.firstName = firstName;
        this.lastName = lastName;
        this.address = address;
        this.emails = new ArrayList<>();
    }

    public int getPersonId() {
        return personId;
    }

    public String getPersonCode() {
        return personCode;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public Address getAddress() {
        return address;
    }

    public List<String> getEmails() {
        return emails;
    }

    /**
     * Adds an email to the person's email list
     * 
     * @param email
     */
    public void addEmail(String email) {
        this.emails.add(email);
    }

    /**
     * Returns the full name of the person in "LastName, FirstName" format
     * 
     * @return formatted full name
     */
    public String getWholeName() {
        return lastName + ", " + firstName;
    }

    @Override
    public String toString() {
        return getWholeName();
    }

    /**
     * Compares persons by last name, then by first name if last names are equal
     */
    @Override
    public int compareTo(Person other) {
        int lastNameComparison = this.lastName.compareTo(other.lastName);
        if (lastNameComparison != 0) {
            return lastNameComparison;
        }
        return this.firstName.compareTo(other.firstName);
    }
}
