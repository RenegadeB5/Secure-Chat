package com.server.springwebsocket.repository;
import com.server.springwebsocket.entities.Group;

public interface GroupRepositoryInterface {
    public void addGroup(Group group);
    public void removeGroup(String groupID);
    public Group getGroup(String groupID);
    public boolean hasGroup(String groupID);
}