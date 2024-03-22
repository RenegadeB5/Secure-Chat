package com.server.springwebsocket.repository;
import com.server.springwebsocket.entities.Group;
import org.springframework.stereotype.Repository; 
import java.util.HashMap;
import java.util.Map;


@Repository
public class GroupRepository implements GroupRepositoryInterface {
    private Map<String, Group> repository;

    public GroupRepository() {
        this.repository = new HashMap<String, Group>();
    }

    @Override
    public void addGroup(Group group) {
        this.repository.put(group.getGroupID(), group);
    }

    @Override
    public void removeGroup(String groupID) {
        this.repository.remove(groupID);
    }

    @Override
    public Group getGroup(String groupID) {
        return this.repository.get(groupID);
    }

    @Override
    public boolean hasGroup(String groupID) {
        return this.repository.containsKey(groupID);
    }
}