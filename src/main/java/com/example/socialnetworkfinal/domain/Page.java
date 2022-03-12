package com.example.socialnetworkfinal.domain;

import java.util.List;

public class Page {
    String email;
    String lastName;
    String firstName;
    List<User> friends;
    List<Message> messages;
    //le tin sub forma de Friendship ca sa fac filtrarea in controller pt received si sent
    List<Friendship> friendshipsRequests;

    public Page(String email, String lastName, String firstName, List<User> friends, List<Message> messages, List<Friendship> friendshipsRequests) {
        this.email = email;
        this.lastName = lastName;
        this.firstName = firstName;
        this.friends = friends;
        this.messages = messages;
        this.friendshipsRequests = friendshipsRequests;
    }

    public String getEmail() {
        return email;
    }

    public String getLastName() {
        return lastName;
    }

    public String getFirstName() {
        return firstName;
    }

    public List<User> getFriends() {
        return friends;
    }

    public List<Message> getMessages() {
        return messages;
    }

    public List<Friendship> getFriendshipsRequests() {
        return friendshipsRequests;
    }

    public void setFriends(List<User> friends) {
        this.friends = friends;
    }
}
