package com.server.springwebsocket.repository;
import com.server.springwebsocket.entities.User;
import org.springframework.stereotype.Repository; 
import java.util.HashMap; 
import java.util.Map; 

@Repository
public class UserRepository implements UserRepositoryInterface {
    private Map<String, User> repository;

    public UserRepository() {
        this.repository = new HashMap<String, User>();
    }

    @Override
    public void addUser(User user) {
        this.repository.put(user.getUUID(), user);
    }

    @Override
    public void removeUser(String uuid) {
        this.repository.remove(uuid);
    }

    @Override
    public User getUser(String uuid) {
        return this.repository.get(uuid);
    }


}