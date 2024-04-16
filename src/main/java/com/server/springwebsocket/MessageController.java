package com.server.springwebsocket;

import java.security.Principal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.MessageMapping;

import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import com.server.springwebsocket.services.*;

import org.springframework.messaging.simp.SimpMessageType;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;

@Controller
public class MessageController {
	
	@Autowired 
	private final MessageServiceInterface message_service;
	
	private final SimpMessagingTemplate simpMessagingTemplate;	

	
	public MessageController(MessageServiceInterface message_service, SimpMessagingTemplate simpMessagingTemplate) {
		this.message_service = message_service;
		this.simpMessagingTemplate = simpMessagingTemplate;
	}

    @MessageMapping("/endpoint")
	//@SendToUser
    public void event(@Header("simpSessionId") String sessionId, byte[] message, Principal principle) throws Exception {
		System.out.println(sessionId);
		this.message_service.parse_packet(sessionId, message);
		/*SimpMessageHeaderAccessor headerAccessor = SimpMessageHeaderAccessor.create(SimpMessageType.MESSAGE);
        headerAccessor.setSessionId(sessionId);
        headerAccessor.setLeaveMutable(true);
		simpMessagingTemplate.convertAndSendToUser(
            sessionId, 
            "/queue/listen", 
            new byte[10],
			headerAccessor.getMessageHeaders()
        ); */
		//return new byte[10];

    }

	
}

