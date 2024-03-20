package com.server.springwebsocket.repository;

import com.server.springwebsocket.entities.MongoUser;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

public interface MongoUserRepository extends MongoRepository<MongoUser, String> {
    @Query("{username:'?0'}")
    MongoUser findUserByName(String name);
}
