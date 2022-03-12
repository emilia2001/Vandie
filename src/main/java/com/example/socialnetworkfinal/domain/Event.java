package com.example.socialnetworkfinal.domain;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Objects;
import java.util.stream.Collectors;

public class Event extends Entity<Long> {
    private String name;
    private LocalDate startDate;
    private LocalDate endDate;
    private String location;
    private String category;
    private String description;
    private HashMap<Long, Integer> notifications;
    private String message;
    private String eventPictureUrl;

    public Event(String name, LocalDate startDate, LocalDate endDate, String location, String category, String description) {
        this.name = name;
        this.startDate = startDate;
        this.endDate = endDate;
        this.location = location;
        this.category = category;
        this.description = description;

    }

    public void setMessage() {
        if(this.getStartDate().equals(LocalDate.now()))
            this.message = "Event " + this.getName() + " is today!";
        else if(this.getStartDate().isBefore(LocalDate.now()))
            this.message = "Event " + this.getName() + " is ongoing!";
        else
            this.message = "Event " + this.getName() + " starts in " + String.valueOf(ChronoUnit.DAYS.between( LocalDate.now().atStartOfDay(),this.getStartDate().atStartOfDay())) + " days";
    }

    public String getName() {
        return name;
    }

    public String getMessage() {
        return message;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public HashMap<Long, Integer> getNotifications() {
        return notifications;
    }

    public void setNotifications(HashMap<Long, Integer> notifications) {
        this.notifications = notifications;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getEventPictureUrl() {
        return eventPictureUrl;
    }

    public void setEventPictureUrl(String eventPictureUrl) {
        this.eventPictureUrl = eventPictureUrl;
    }

    public void setNotifications(Long user_id, Integer value) {
        this.notifications.put(user_id,value);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Event)) return false;
        if (!super.equals(o)) return false;
        Event event = (Event) o;
        return Objects.equals(getName(), event.getName()) && Objects.equals(getStartDate(), event.getStartDate()) && Objects.equals(getEndDate(), event.getEndDate()) && Objects.equals(getLocation(), event.getLocation()) && Objects.equals(getCategory(), event.getCategory()) && Objects.equals(getDescription(), event.getDescription());
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), getName(), getStartDate(), getEndDate(), getLocation(), getCategory(), getDescription());
    }

    @Override
    public String toString() {
        return "Event{" +
                "name='" + name + '\'' +
                ", notifications=" + notifications +
                '}';
    }
}