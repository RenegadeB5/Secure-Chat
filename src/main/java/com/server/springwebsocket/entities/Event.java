package com.server.springwebsocket.entities;

public class Event {
    private Message message;

    public Event() {

    }

    public Event(Message message) {
        this.message = message;
    }

    public Message getContent() {
        return this.message;
    }
}