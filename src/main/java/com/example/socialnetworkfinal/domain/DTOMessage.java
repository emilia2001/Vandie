package com.example.socialnetworkfinal.domain;

import java.time.LocalDateTime;

public class DTOMessage {
    private String from;
    private String text;
    LocalDateTime date;

    public DTOMessage(String from, String text, LocalDateTime date) {
        this.from = from;
        this.text = text;
        this.date = date;
    }

    public String getFrom() {
        return from;
    }

    public String getText() {
        return text;
    }

    public LocalDateTime getDate() {
        return date;
    }
}
