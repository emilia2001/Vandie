package com.example.socialnetworkfinal.domain;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class Conversation extends Entity<Long>{
    private String name;
    private List<User> users;
    private LocalDateTime lastMessageDate;
    private String lastMessage;

    public Conversation(String name, List<User> users) {
        this.name = name;
        Collections.sort(users, new UserIdComparator());
        this.users = users;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<User> getUsers() {
        return users;
    }

    public void setUsers(List<User> users) {
        this.users = users;
    }

    public LocalDateTime getLastMessageDate() {
        return lastMessageDate;
    }

    public void setLastMessageDate(LocalDateTime lastMessageDate) {
        this.lastMessageDate = lastMessageDate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Conversation)) return false;
        if (!super.equals(o)) return false;
        Conversation that = (Conversation) o;
        return Objects.equals(getUsers(), that.getUsers());
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), getUsers());
    }

    @Override
    public String toString() {
        return "Conversation{" +
                "name='" + name + '\'' +
                '}';
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(String lastMessage) {
        if(lastMessage.split("\n").length > 1)
            this.lastMessage = lastMessage.split("\n")[0] + "...";
        else
            this.lastMessage = lastMessage;
    }
}
