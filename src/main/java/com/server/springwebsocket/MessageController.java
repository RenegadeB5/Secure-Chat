package com.server.springwebsocket;

import java.security.Principal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import com.server.springwebsocket.entities.*;
import com.server.springwebsocket.repository.*;
import com.server.springwebsocket.utils.*;

@Controller
public class MessageController {
	private final GroupRepositoryInterface groups;
	private final UserRepositoryInterface users;

	@Autowired
	public MessageController(GroupRepositoryInterface groups, UserRepositoryInterface users) {
		this.groups = groups;
		this.users = users;
	}


	/*
	packet structure
	serverBound: 
		1: register 
			string username
		2: authenticate

		3: send message 
			(int) dm or gm
			1: 
				(string) user token | (string) recipient ID | (string) message
			2:
				(string) user token | (string) group ID | (string) message

		4: join/leave group
			(int) join/leave
			1: join
				(string) user token | (string) group ID
			2: leave
				(string) user token | (string) group ID














	 */
    @MessageMapping("/endpoint")
    public void event(byte[] message, Principal principle) throws Exception {
		System.out.println(principle.getName());
		System.out.println(message[0]);

    }

}