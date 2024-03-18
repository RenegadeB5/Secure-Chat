package com.server.springwebsocket.entities;

public class User {
    private String uuid;
    private String ws_ID;
    private String username;
    private String token;

    public User() {

    }

    public User(String uuid, String ws_ID, String username, String token) {
        this.uuid = uuid;
        this.ws_ID = ws_ID;
        this.username = username;
        this.token = token;
    }

    public String getUUID() {
        return this.uuid;
    }

    public String getWSID() {
        return this.ws_ID;
    }

    public void setUsername(String newUsername) {
        this.username = newUsername;
    }

    public String getUsername() {
        return this.username;
    }

    public boolean authenticate(String token) {
        return (token == this.token);
    }



}