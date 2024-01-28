package com.example.lab7.domain;



import java.time.LocalDateTime;
import java.util.List;

public class Message extends Entity<Long> {
    private Utilizator from;
    private Utilizator to;
    private LocalDateTime date;
    private String message;
    private Message reply;

    public Message(Utilizator from, Utilizator to, LocalDateTime date, String message) {
        this.from = from;
        this.to = to;
        this.date = date;
        this.message = message;
        this.reply = null;
    }

    public Message(Utilizator from, Utilizator to, String message) {
        this.from = from;
        this.to = to;
        this.message = message;
        this.date = LocalDateTime.now();
    }

    public Utilizator getFrom() {
        return from;
    }

    public void setFrom(Utilizator from) {
        this.from = from;
    }

    public Utilizator getTo() {
        return to;
    }

    public void setTo(Utilizator to) {
        this.to = to;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Message getReply() {
        return reply;
    }

    public void setReply(Message reply) {
        this.reply = reply;
    }

    @Override
    public String toString() {
        return from.getFirstName() +
                ": " + message + "\nDATE:" +
                date;
    }
}