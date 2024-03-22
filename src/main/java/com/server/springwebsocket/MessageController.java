package com.server.springwebsocket;

import java.security.Principal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import com.server.springwebsocket.services.*;

import org.springframework.stereotype.Controller;

@Controller
public class MessageController {
	
	
	private final MessageServiceInterface message_service;

	@Autowired 
	public MessageController(MessageServiceInterface message_service) {
		this.message_service = message_service;
	}

    @MessageMapping("/endpoint")
    public void event(byte[] message, Principal principle) throws Exception {
		this.message_service.parse_packet(principle.getName(), message);

    }

}