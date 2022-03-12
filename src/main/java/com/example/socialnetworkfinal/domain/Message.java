package com.example.socialnetworkfinal.domain;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;

public class Message extends Entity<Long> {
    private User from;
    private List<User> to;
    private String message;
    private LocalDateTime date;
    private Message reply = null;

    /**
     * @param from - User, the user who sends the message
     * @param to - List<User>, the users that the message is sent to
     * @param message - String, the text of a message
     */
    public Message(User from, List<User> to, String message) {
        this.from = from;
        this.to = to;
        this.message = message;
    }

    /**
     * @return User - the user who sent the message
     */
    public User getFrom() {
        return from;
    }

    /**
     * @return List<User> - the recipients of the message
     */
    public List<User> getTo() {
        return to;
    }

    /**
     * @return String, the text of the message
     */
    public String getMessage() {
        return message;
    }

    /**
     * @return LocalDateTime - the date and time when the message was sent
     */
    public LocalDateTime getDate() {
        return date;
    }

    /**
     * @param date LocalDateTime - the new date and time of the message
     */
    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    /**
     * @return Message- the original message to which it was replied to
     */
    public Message getReply() {
        return reply;
    }

    /**
     * @param reply - Message, set an original message for which this is a reply to
     */
    public void setReply(Message reply) {
        this.reply = reply;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Message)) return false;
        if (!super.equals(o)) return false;
        Message message1 = (Message) o;
        return this.getId().equals(message1.getId());    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), getFrom(), getTo(), getMessage(), getDate());
    }

    @Override
    public String toString() {
        final String[] stringTo = {""};
        to.forEach(t -> stringTo[0] += t.getLastName() + " " + t.getFirstName() + ", ");
        String message = "\nMessage\n";
        message += "From : " + from.getLastName() + " " + from.getFirstName() + "\n";
        message += "To : " + stringTo[0] + "\n";
        message += "Message Text : " + this.message + "\n";
        message += "Date : " + date.format(DateTimeFormatter.ofPattern("yyyy-MMM-dd HH:mm:ss")) + "\n";
        if(reply != null)
            message += "Reply to : " + reply.getId() + "\n";
        return message;
    }
}
