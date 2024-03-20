package com.server.springwebsocket.repository;

import com.server.springwebsocket.entities.MongoMessages;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface MongoMessageRepository extends MongoRepository<MongoMessages, String> {
}
