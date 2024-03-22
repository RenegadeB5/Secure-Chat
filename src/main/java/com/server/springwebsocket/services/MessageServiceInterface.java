package com.server.springwebsocket.services;

public interface MessageServiceInterface {
    public void parse_packet(String origin_id, byte[] array);
}