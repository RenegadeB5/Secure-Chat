package com.server.springwebsocket.entities;
import java.util.*; 

public class Group {
    private String groupID;
    private String groupName;
    private List<String> members;

    public Group() {

    }

    public Group(String groupID, String groupName) {
        this.groupID = groupID;
        this.groupName = groupName;
        this.members = new ArrayList<String>();
    }

    public Group(String groupID, String groupName, ArrayList<String> members) {
        this.groupID = groupID;
        this.groupName = groupName;
        for (String uuid : members) {
            this.members.add(uuid);
        }
    }

    public String getGroupID() {
        return this.groupID;
    }

    public void setGroupName(String newName) {
        this.groupName = newName;
    }

    public String getGroupName() {
        return this.groupName;
    }

    public void addMember(String uuid) {
        this.members.add(uuid);
    }

    public void removeMember(String uuid) {
        int index = this.members.indexOf(uuid);
        this.members.remove(index);
    }

    public List<String> getUsers() {
        return this.members;
    }

}