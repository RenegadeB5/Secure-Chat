package com.server.springwebsocket.entities;

import lombok.Data;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.MongoId;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Document(collection = "conversations")
public class MongoConversation {
    @MongoId
    private String id;

    @DBRef
    private List<MongoUser> participants;

    private LocalDateTime created;
}
