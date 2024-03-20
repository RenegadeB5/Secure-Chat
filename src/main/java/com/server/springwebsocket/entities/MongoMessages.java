package com.server.springwebsocket.entities;

import lombok.Data;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.MongoId;

import java.util.List;

@Data
@Document(collection = "messages")
public class MongoMessages {
    @MongoId
    private String id;

    @DBRef
    private MongoUser sender;

    @DBRef
    private MongoConversation conversation;

    private String contents;

    public MongoMessages(MongoUser sender, MongoConversation conversation, String contents) {
        this.sender = sender;
        this.conversation = conversation;
        this.contents = contents;
    }
}
