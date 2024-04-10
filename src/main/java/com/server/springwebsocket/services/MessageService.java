package com.server.springwebsocket.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import com.server.springwebsocket.entities.*;
import com.server.springwebsocket.repository.*;
import com.server.springwebsocket.utils.*;
import java.io.*;
import java.nio.*;

import java.util.*; 

@Service
public class MessageService implements MessageServiceInterface {
    
    private int[] encryptor;
    private int[] decryptor;
    @Autowired
    private SimpMessagingTemplate simpMessagingTemplate;
    private final GroupRepositoryInterface group_repo;
	private final UserRepositoryInterface user_repo;

    @Autowired
    public MessageService(GroupRepositoryInterface groups, UserRepositoryInterface users) {
        this.group_repo = groups;
        this.user_repo = users;

        this.decryptor = new int[256];
        System.out.println("new: " + System.getProperty("user.dir") + "\\src\\main\\java\\com\\server\\springwebsocket\\build");
        File file = new File(System.getProperty("user.dir") + "\\src\\main\\java\\com\\server\\springwebsocket\\build");
        String seed = "";
		try {
            
            FileReader fr = new FileReader(file);
            BufferedReader br = new BufferedReader(fr, 1024);
            seed = br.readLine();
            br.close();
        }
        catch (Exception e) {}
        System.out.println(seed);
        

        RandomNumberGenerator generator = new RandomNumberGenerator(seed);
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
        for (int i = 0; i < 256; i++) {
            System.out.println(this.encryptor[i]);
            int num = range[i];
            this.decryptor[num] = i;
        }
    }


    /*
	packet structure
	serverbound (int): 
		1: register 
			string username
		2: authenticate

		3: send message 
			(string) user token | (int) dm(1) or gm(2)  | (string) recipient ID | (string) message

		4: create group
            (string) user token | (string) name | (string) group password
		
		5: join/leave group
			(string) user token | (string) group ID | (int) join/leave
			1: join
				 (string) group password
			2: leave


    clientbound (int):
        1: recieve user info
            (string) user token |
            (int) # of groups (for each) | (string group ID) (String group name) |
            (int) # of dms (for each) | (string user ID) (String user name) |
            * maybe send messages for each *

        2: recieve alert
            (string) alert message
        
        3: recieve message
            (string) sender ID | (string) group ID | (string) message
            

    */



    @Override
    public void parse_packet(String origin_ws_id, byte[] array) {

        ByteBuffer buffer = ByteBuffer.wrap(this.decrypt_packet(array));
        Decoder decoder = new Decoder(buffer);

        int header = decoder.getInt();
        boolean user_exists = user_repo.hasUser(origin_ws_id);
        String token = null;
        User user = null;
        Group group = null;
        String group_name = null;
        String group_password = null;
        switch (header) {
            case 1: // register
                if (user_exists) break;
                String username = decoder.getString();
                this.create_user(origin_ws_id, username);
                break;

            case 2: // authenticate
                break;

            case 3: // send message
                if (!user_exists) break;
                token = decoder.getString();
                user = this.user_repo.getUserByWsId(origin_ws_id);
                if (!user.authenticate(token)) break;

                int message_type = decoder.getInt();
                String recipient_id = decoder.getString();
                String message = decoder.getString();
                if (message_type == 1) {
                    this.send_direct_message(recipient_id, message);
                }

                if (message_type == 2) {
                    this.send_group_message(recipient_id, message);
                }

                break;

            case 4: // create group
                if (!user_exists) break;
                token = decoder.getString();
                user = this.user_repo.getUserByWsId(origin_ws_id);
                if (!user.authenticate(token)) break;
                group_name = decoder.getString();
                group_password = decoder.getString();
                group = new Group(group_name, group_password);
                group.addMember(user.getUUID());
                this.group_repo.addGroup(group);
                break;

                
            case 5: // join/leave group
                if (!user_exists) break;
                token = decoder.getString();
                user = this.user_repo.getUserByWsId(origin_ws_id);
                if (!user.authenticate(token)) break;
                String group_id = decoder.getString();
                if (this.group_repo.hasGroup(group_id)) break;
                group = this.group_repo.getGroup(group_id);
                int action = decoder.getInt();
                switch (action) {
                    case 1: 
                        group_password = decoder.getString();
                        if (!group.authenticate(group_password)) break;
                        group.addMember(user.getUUID());
                    case 2:
                        group.removeMember(user.getUUID());    
                }
                


        }

    }

    private void create_user(String ws_id, String username) {
        
        User new_user = new User(ws_id, username);
        this.user_repo.addUser(new_user);
        
    }

    private void send_direct_message(String recipient_ID, String message) {
        User recipient = user_repo.getUserByUserId(recipient_ID);
        simpMessagingTemplate.convertAndSendToUser(
            recipient.getWSID(), 
            "/app/listen", 
            message
        ); 
    }

    private void send_group_message(String group_ID, String message) {
        if (!this.group_repo.hasGroup(group_ID)) return;
        Group group = this.group_repo.getGroup(group_ID);
        List<String> member_ids = group.getMembers();
        for (String id: member_ids) {
            User user = this.user_repo.getUserByUserId(id);
            String ws_id = user.getWSID();
            simpMessagingTemplate.convertAndSendToUser(
                ws_id, 
                "/app/listen", 
                message
            ); 
        }

    }

    private byte[] encrypt_packet(byte[] array) {
        for (int i = 0; i < array.length; i++) {
            array[i] = (byte)this.encryptor[(int)array[i]];
        }
        return array;
    }

    private byte[] decrypt_packet(byte[] array) {
        for (int i = 0; i < array.length; i++) {
            array[i] = (byte)this.decryptor[(int)array[i]];
        }
        return array;
    }
}



/* sbox test code
 * import java.io.*;
import java.util.*;
public class MyClass {
     public static void print_arr(int[] arr) {
        for (int i = 0; i < arr.length; i++) {
            System.out.print(arr[i]);
            System.out.print(" ");
        }
        System.out.println();
    }
    public static void main(String args[]) {
      int[] sbox = new int[256];
      int[] inv_sbox = new int[256];
      Random generator = new Random();
      int[] range = new int[256];
      for (int i = 0; i < 256; i++) {
          range[i] = i;
      }
      for (int i = 0; i < 1000; i++) {
          int pos_1 = generator.nextRange(256);
          int pos_2 = generator.nextRange(256);
          int int_1 = range[pos_1];
          int int_2 = range[pos_2];
          
          range[pos_2] = int_1;
          range[pos_1] = int_2;
      }
      sbox = range;
      for (int i = 0; i < 256; i++) {
          int num = range[i];
          inv_sbox[num] = i;
          
      }
      
      
      int test_num = 235;
      int enc = sbox[test_num];
      System.out.println(enc);
      System.out.println(inv_sbox[enc]);
      
      print_arr(range);
      print_arr(sbox);
      print_arr(inv_sbox);
      System.out.println(163 & 0xF);
    }
    
   
}
 */

 
