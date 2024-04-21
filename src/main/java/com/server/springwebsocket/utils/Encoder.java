package com.server.springwebsocket.utils;
import java.nio.*;

public class Encoder {
    private byte[] buffer;
    private int at;

    public Encoder() {
        this.buffer = new byte[1500];
        this.at = 0;
    }

    public int getPosition() {
        return this.at;
    }

    public void addInt(int i) {
        this.buffer[this.at++] = (byte)i;
    }

    public void addString(String s) {
        this.addInt(s.length());
        for (int i = 0; i < s.length(); i++) {
            this.addInt((int)s.charAt(i));
        }
    }

    public byte[] finish() {
        int length = this.at;
        byte[] new_buf = new byte[length];
        for (int i = 0; i < length; i++) {
            new_buf[i] = this.buffer[i];
        }
        return new_buf;
    }
}