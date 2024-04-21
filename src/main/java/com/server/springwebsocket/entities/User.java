package com.server.springwebsocket.entities;
import com.server.springwebsocket.utils.RandomNumberGenerator;
import java.util.UUID;

public class User {
    private String uuid;
    private String ws_ID;
    private String username;
    private String token;
    private int[] encryptor;
    private int[] decryptor;

    public User() {

    }

    public User(String ws_ID, String username) {
        this.uuid = UUID.randomUUID().toString();
        this.ws_ID = ws_ID;
        this.username = username;
        this.token = "sc_" + UUID.randomUUID().toString() + UUID.randomUUID().toString();

        RandomNumberGenerator generator = new RandomNumberGenerator(this.token);
        int[] range = new int[256];
        for (int i = 0; i < 256; i++) {
            range[i] = i;
        }
        for (int i = 0; i < 1000; i++) {
            int pos_1 = generator.nextRange(1, 256);
            int pos_2 = generator.nextRange(1, 256);
            
            int int_1 = range[pos_1];
            int int_2 = range[pos_2];
            
            range[pos_2] = int_1;
            range[pos_1] = int_2;
        }
        this.encryptor = range;
        this.decryptor = new int[256];
        for (int i = 0; i < 256; i++) {
            int num = range[i];
            this.decryptor[num] = i;
        }
    }

    public String getUUID() {
        return this.uuid;
    }

    public String getWSID() {
        return this.ws_ID;
    }

    public String getToken() {
        return this.token;
    }



    public void setUsername(String newUsername) {
        this.username = newUsername;
    }

    public String getUsername() {
        return this.username;
    }

    public boolean authenticate(String token) {
        return (token.equals(this.token));
    }

    public byte[] encrypt_packet(byte[] array) {
        byte[] encrypted = array.clone();
        for (int i = 1; i < array.length; i++) {
            encrypted[i] = (byte)this.encryptor[(int)array[i] & 0xFF];
        }
        return encrypted;
    }

    public byte[] decrypt_packet(byte[] array) {
        byte[] decrypted = array.clone();
        for (int i = 1; i < array.length; i++) {
            decrypted[i] = (byte)this.decryptor[(int)array[i] & 0xFF];
        }
        return decrypted;
    }



}