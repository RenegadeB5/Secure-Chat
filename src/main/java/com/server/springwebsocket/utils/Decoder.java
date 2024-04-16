package com.server.springwebsocket.utils;
import java.nio.*;

public class Decoder {
    private byte[] buffer;
    private int at;

    public Decoder(byte[] buffer) {
        this.buffer = buffer;
    }

    public int getInt() {
        return (int)(this.buffer[this.at++] & 0xFF); 
    }

    public String getString() {
        int length = this.getInt();
        String s = "";
        for (int i = 0; i < length; i++) {
            s += (char)this.getInt();
        }
        return s;
    }
}