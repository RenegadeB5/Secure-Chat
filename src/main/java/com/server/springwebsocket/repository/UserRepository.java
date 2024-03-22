package com.server.springwebsocket.repository;
import com.server.springwebsocket.entities.User;
import org.springframework.stereotype.Repository; 
import java.util.HashMap; 
import java.util.Map; 

@Repository
public class UserRepository implements UserRepositoryInterface {
    private Map<String, User> repository;
    private Map<String,String> ws_to_id_lookup;

    public UserRepository() {
        this.repository = new HashMap<String, User>();
        this.ws_to_id_lookup = new HashMap<String,String>();
    }

    @Override
    public void addUser(User user) {
        this.repository.put(user.getUUID(), user);
        this.ws_to_id_lookup.put(user.getWSID(), user.getUUID());
    }

    @Override
    public void removeUser(String uuid) {
        this.repository.remove(uuid);
    }

    @Override
    public User getUserByUserId(String uuid) {
        return this.repository.get(uuid);
    }

    @Override
    public User getUserByWsId(String uuid) {
        String user_id = this.ws_to_id_lookup.get(uuid);
        return this.repository.get(user_id);
    }

    @Override
    public boolean hasUser(String uuid) {
        return this.repository.containsKey(uuid);
    }

    


}