package com.server.springwebsocket.entities;
import java.util.UUID;

public class User {
    private String uuid;
    private String ws_ID;
    private String username;
    private String token;

    public User() {

    }

    public User(String ws_ID, String username) {
        this.uuid = UUID.randomUUID().toString();
        this.ws_ID = ws_ID;
        this.username = username;
        this.token = "sc_" + UUID.randomUUID().toString() + UUID.randomUUID().toString();
    }

    public String getUUID() {
        return this.uuid;
    }

    public String getWSID() {
        return this.ws_ID;
    }

    public String getToken() {
        return this.token;
    }

    public void setUsername(String newUsername) {
        this.username = newUsername;
    }

    public String getUsername() {
        return this.username;
    }

    public boolean authenticate(String token) {
        return (token.equals(this.token));
    }



}