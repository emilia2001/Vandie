package com.example.socialnetworkfinal.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


public class User extends Entity<Long>{
    private String firstName;
    private String lastName;
    private String email;
    private String password;
    private List<User> friends;
    private String profilePictureUrl;
    private String coverPictureUrl;
    private String fullName;
    /**
     * @param email - String, the email of a user
     * @param firstName - String, name
     * @param lastName - String, name
     */
    public User(String email, String firstName, String lastName, String password) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.fullName = firstName + " " + lastName;
        this.email = email;
        this.password = password;
        friends = new ArrayList<>();
    }

    /**
     * @return String - the first name of a user
     */
    public String getFirstName() {
        return firstName;
    }

    /**
     * @return String - the last name of a user
     */
    public String getLastName() {
        return lastName;
    }


    /**
     * @return String - the email of a user
     */
    public String getEmail() {
        return email;
    }

    /**
     * @return List - the friends of a user
     */
    public List<User> getFriends() {
        return friends;
    }

    /**
     * @param friends - List<User>, the new friends of the user
     */
    public void setFriends(List<User> friends) {
        this.friends = friends;
    }

    public String getPassword() {
        return password;
    }

    public String getFullName() {
        return fullName;
    }


    /**
     * @return String - to be printed for a user
     */
    @Override
    public String toString() {
        return firstName + " " + lastName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User)) return false;
        User that = (User) o;
        return this.getId().equals(that.getId());
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    public String getProfilePictureUrl() {
        return profilePictureUrl;
    }

    public void setProfilePictureUrl(String profilePictureUrl) {
        this.profilePictureUrl = profilePictureUrl;
    }

    public String getCoverPictureUrl() {
        return coverPictureUrl;
    }

    public void setCoverPictureUrl(String coverPictureUrl) {
        this.coverPictureUrl = coverPictureUrl;
    }
}
