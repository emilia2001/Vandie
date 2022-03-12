package com.example.socialnetworkfinal.domain;


import java.time.LocalDate;
import java.util.Objects;

/**
 * DTO for a friendship, contains the name of a friend and the date of the friendship
 */
public class DTOFriendship {
    private String lastName;
    private String firstName;
    private LocalDate date;
    private String state;
    private String picture_url;

    public String getPicture_url() {
        return picture_url;
    }

    /**
     * @param lastName - String, the last name of a friend
     * @param firstName String, the first name for a friend
     * @param date - LocalDate, the date when the friendship was created
     */
    public DTOFriendship(String lastName, String firstName, LocalDate date, String state, String picture_url) {
        this.lastName = lastName;
        this.firstName = firstName;
        this.date = date;
        this.state = state;
        this.picture_url = picture_url;
    }

    public String getLastName() {
        return lastName;
    }

    public String getFirstName() {
        return firstName;
    }

    public LocalDate getDate() {
        return date;
    }

    public String getState() {
        return state;
    }

    @Override
    public String toString() {
        String string = "\n";
        string += "Last name : " + lastName + "\n";
        string += "First name : " + firstName + "\n";
        string += "Friend since : " + date + "\n";
        return string;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DTOFriendship)) return false;
        DTOFriendship that = (DTOFriendship) o;
        return Objects.equals(getLastName(), that.getLastName()) && Objects.equals(getFirstName(), that.getFirstName()) && Objects.equals(getDate(), that.getDate()) && Objects.equals(getState(), that.getState());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getLastName(), getFirstName(), getDate(), getState());
    }
}
