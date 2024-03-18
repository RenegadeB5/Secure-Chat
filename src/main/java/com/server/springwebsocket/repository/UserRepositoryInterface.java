package com.server.springwebsocket.repository;
import com.server.springwebsocket.entities.User;

public interface UserRepositoryInterface {
    public void addUser(User user);
    public void removeUser(String uuid);
    public User getUser(String uuid);
}