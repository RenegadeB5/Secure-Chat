package com.server.springwebsocket.repository;
import com.server.springwebsocket.entities.User;

public interface UserRepositoryInterface {
    public void addUser(User user);
    public void removeUser(String uuid);
    public User getUserByUserId(String uuid);
    public User getUserByWsId(String uuid);
    public boolean hasUser(String uuid);
    
}