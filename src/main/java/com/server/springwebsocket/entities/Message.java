package com.server.springwebsocket.entities;

import java.nio.ByteBuffer;

public class Message {
    
    private byte[] buffer;

    public Message() {
        this.buffer = new byte[2048];
    }

    public Message(byte[] buffer) {
        this.buffer = buffer;
    }

    public ByteBuffer getBuffer() {
        return ByteBuffer.wrap(this.buffer);
    }
}