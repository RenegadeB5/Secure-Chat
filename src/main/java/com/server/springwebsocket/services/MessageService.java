package com.server.springwebsocket.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageType;
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
    
    private int[] global_encryptor;
    private int[] global_decryptor;
    private final SimpMessagingTemplate simpMessagingTemplate;
    private final GroupRepositoryInterface group_repo;
	private final UserRepositoryInterface user_repo;

    @Autowired
    public MessageService(SimpMessagingTemplate messagingTemplate, GroupRepositoryInterface groups, UserRepositoryInterface users) {
        this.simpMessagingTemplate = messagingTemplate;
        this.group_repo = groups;
        this.user_repo = users;

        this.global_decryptor = new int[256];
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
        this.global_encryptor = range;
        String str = "";
        for (int i = 0; i < 256; i++) {
            str += this.global_encryptor[i];
            int num = range[i];
            this.global_decryptor[num] = i;
        }
        System.out.println(str);
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
        1: recieve user token
            (string) user token 

        2: recieve alert
            (string) alert message
        
        3: recieve message
			(int) private or group message:
				1:
					(string) sender ID | (string) sender name | (string) message
				2: 
            		(string) group ID | (string) sender ID | (string) message
            
		4: group updates
				(string) group ID | (string)  group name | (int) # of updates | repeat 
					(int) add or remove 
						0: add	
							(string) user ID | (string) user name
						1: remove 
							(string) user ID


    */



    @Override
    public void parse_packet(String origin_ws_id, byte[] array) {
        
        array = array.clone();
        User origin_user = this.user_repo.getUserByWsId(origin_ws_id);
        Decoder decoder = new Decoder(this.global_decrypt_packet(array));
        int private_ = decoder.getInt();
        if (private_ == 1) {
            decoder.set_buffer(origin_user.decrypt_packet(array));
        }
        int header = decoder.getInt();
        String token = null;
        User user = null;
        Group group = null;
        String group_name = null;
        String group_password = null;

        Encoder encoder = new Encoder();
        
        System.out.println("Header: " + private_ + " " + header);
        switch (header) {
            case 1: // register
                String username = decoder.getString();
                token = this.create_user(origin_ws_id, username);
                encoder.addInt(0);
                encoder.addInt(1);
                encoder.addString(token);
                
                this.send_packet(origin_ws_id, encoder.finish());
                break;

            case 2: // authenticate
                break;

            case 3: // send message
                token = decoder.getString();
                user = this.user_repo.getUserByWsId(origin_ws_id);
                if (!user.authenticate(token)) break;

                int message_type = decoder.getInt();
                String recipient_id = decoder.getString();
                String message = decoder.getString();
                if (message_type == 1) {
                    this.send_direct_message(origin_user.getUUID(), recipient_id, message);
                }

                if (message_type == 2) {
                    this.send_group_message(origin_user.getUUID(), recipient_id, message);
                }

                break;

            case 4: // create group
                token = decoder.getString();
                user = this.user_repo.getUserByWsId(origin_ws_id);
                
                System.out.println("Token: " + user.getToken() + " " + this.group_repo.test());
                if (!user.authenticate(token)) break;
                
                System.out.println(user.getToken());
                group_name = decoder.getString();
                group_password = decoder.getString();
                group = new Group(group_name, group_password);
                group.addMember(user.getUUID());
                this.group_repo.addGroup(group);
                this.broadcast_group_join(group.getGroupID(), user.getUUID());
                
                this.send_group_members(group.getGroupID(), user.getUUID());
                break;

                
            case 5: // join/leave group
                token = decoder.getString();
                user = this.user_repo.getUserByWsId(origin_ws_id);
                if (!user.authenticate(token)) break;
                String group_id = decoder.getString();
                System.out.println("Group ID: " + group_id + " " + this.group_repo.test());
                if (!this.group_repo.hasGroup(group_id)) break;
                group = this.group_repo.getGroup(group_id);
                int action = decoder.getInt();
                switch (action) {
                    case 1: 
                        group_password = decoder.getString();
                        if (!group.authenticate(group_password)) break;
                        group.addMember(user.getUUID());
                        this.broadcast_group_join(group.getGroupID(), user.getUUID());
                        this.send_group_members(group_id, user.getUUID());
                        break;
                    case 2:
                        group.removeMember(user.getUUID());    
                        this.broadcast_group_leave(group.getGroupID(), user.getUUID());
                        break;
                }
                


        }

    }

    private void send_packet(String recipient_WSID, byte[] packet) {
        packet = packet.clone();
        System.out.println("Sending to: " + recipient_WSID);
        
        System.out.println((int)(packet[0] & 0xFF) + " " + (int)(packet[1] & 0xFF));
        byte[] new_packet = this.global_encrypt_packet(packet);
        System.out.println((int)(new_packet[0] & 0xFF) + " " + (int)(new_packet[1] & 0xFF));

        SimpMessageHeaderAccessor headerAccessor = SimpMessageHeaderAccessor.create(SimpMessageType.MESSAGE);
        headerAccessor.setSessionId(recipient_WSID);
        headerAccessor.setLeaveMutable(true);
        this.simpMessagingTemplate.convertAndSendToUser(
            recipient_WSID, 
            "/queue/listen", 
            new_packet,
            headerAccessor.getMessageHeaders()
        ); 
    }

    private String create_user(String ws_id, String username) {
        
        User new_user = new User(ws_id, username);
        this.user_repo.addUser(new_user);
        return new_user.getToken();
        
    }

    private void send_direct_message(String sender_ID, String recipient_ID, String message) {
        User sender = this.user_repo.getUserByUserId(sender_ID);
        User recipient = this.user_repo.getUserByUserId(recipient_ID);
        Encoder encoder = new Encoder();
        encoder.addInt(1);
        encoder.addInt(3);
        encoder.addInt(1);
        encoder.addString(sender_ID);
        encoder.addString(sender.getUsername());
        encoder.addString(message);
        this.send_packet(recipient.getWSID(), recipient.encrypt_packet(encoder.finish()));
    }

    private void send_group_message(String sender_ID, String group_ID, String message) {
        if (!this.group_repo.hasGroup(group_ID)) return;
        Group group = this.group_repo.getGroup(group_ID);
        List<String> member_ids = group.getMembers();

        Encoder encoder = new Encoder();
        encoder.addInt(1);
        encoder.addInt(3);
        encoder.addInt(2);
        encoder.addString(group_ID);
        encoder.addString(sender_ID);
        encoder.addString(message);
        byte[] packet = encoder.finish();
        for (String id: member_ids) {
            if (id.equals(sender_ID)) continue;
            User user = this.user_repo.getUserByUserId(id);
            String ws_id = user.getWSID();
            this.send_packet(ws_id, user.encrypt_packet(packet));
        }

    }

    private void broadcast_group_join(String group_ID, String user_ID) {
        Group group = this.group_repo.getGroup(group_ID);
        User user = this.user_repo.getUserByUserId(user_ID);
        
        List<String> member_ids = group.getMembers();
        Encoder encoder = new Encoder();
        encoder.addInt(1);
        encoder.addInt(4);
        encoder.addString(group_ID);
        encoder.addString(group.getGroupName());
        encoder.addInt(1);
        encoder.addInt(0);
        encoder.addString(user_ID);
        encoder.addString(user.getUsername());
        byte[] packet = encoder.finish();
        for (String id: member_ids) {
            User user_ = this.user_repo.getUserByUserId(id);
            if (user_ID.equals(id)) continue;
            String ws_id = user_.getWSID();
            this.send_packet(ws_id, user_.encrypt_packet(packet));
        }
    }

    private void send_group_members(String group_ID, String user_ID) {
        Group group = this.group_repo.getGroup(group_ID);
        User user = this.user_repo.getUserByUserId(user_ID);
        
        List<String> member_ids = group.getMembers();
        Encoder encoder = new Encoder();
        encoder.addInt(1);
        encoder.addInt(4);
        encoder.addString(group_ID);
        encoder.addString(group.getGroupName());
        encoder.addInt(member_ids.size());
        System.out.println("Size: " + member_ids.size());
        
        for (String id: member_ids) {
            User user_ = this.user_repo.getUserByUserId(id);
            encoder.addInt(0);
            System.out.println(user_.getUUID());
            encoder.addString(user_.getUUID());
            encoder.addString(user_.getUsername());
        }
        byte[] packet = encoder.finish();
        this.send_packet(user.getWSID(), user.encrypt_packet(packet));
    }

    private void broadcast_group_leave(String group_ID, String user_ID) {
        Group group = this.group_repo.getGroup(group_ID);
        User user = this.user_repo.getUserByUserId(user_ID);
        
        List<String> member_ids = group.getMembers();
        Encoder encoder = new Encoder();
        encoder.addInt(1);
        encoder.addInt(4);
        encoder.addString(group_ID);
        encoder.addString(group.getGroupName());
        encoder.addInt(1);
        encoder.addString(user_ID);
        byte[] packet = encoder.finish();
        for (String id: member_ids) {
            User user_ = this.user_repo.getUserByUserId(id);
            String ws_id = user_.getWSID();
            this.send_packet(ws_id, user_.encrypt_packet(packet));
        }
    }

    private byte[] global_encrypt_packet(byte[] array) {
        for (int i = 1; i < array.length; i++) {
            array[i] = (byte)this.global_encryptor[(int)array[i] & 0xFF];
        }
        return array;
    }

    private byte[] global_decrypt_packet(byte[] array) {
        for (int i = 1; i < array.length; i++) {
            array[i] = (byte)this.global_decryptor[(int)array[i] & 0xFF];
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

 
