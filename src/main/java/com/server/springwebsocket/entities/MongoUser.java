package com.server.springwebsocket.entities;


import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.MongoId;

import java.time.LocalDateTime;

@Data
@Document(collection = "users")
public class MongoUser {

    @MongoId
    private String Id;

    private String username;

    private String password;
    private LocalDateTime created;

    public MongoUser(){}

    public MongoUser(String username, String password, LocalDateTime created){
        super();
        this.username = username;
        this.password = password;
        this.created = created;
    }


}
