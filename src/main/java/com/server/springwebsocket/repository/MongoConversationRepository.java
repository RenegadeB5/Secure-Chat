package com.server.springwebsocket.repository;

import com.server.springwebsocket.entities.MongoConversation;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface MongoConversationRepository extends MongoRepository<MongoConversation, String> {
}
