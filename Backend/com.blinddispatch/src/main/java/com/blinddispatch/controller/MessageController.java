package com.blinddispatch.controller;

import com.blinddispatch.dto.MessageRequest;
import com.blinddispatch.dto.MessageDto;
import com.blinddispatch.model.Message;
import com.blinddispatch.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/messages")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;

    @PostMapping("/send")
    public ResponseEntity<Message> sendMessage(@RequestBody MessageRequest messageRequest) {
        String senderUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        Message message = messageService.sendMessage(senderUsername, messageRequest);
        return ResponseEntity.ok(message);
    }

    @GetMapping("/conversation")
    public ResponseEntity<List<MessageDto>> getConversation(@RequestParam("user2") String user2) {
        String user1 = SecurityContextHolder.getContext().getAuthentication().getName();
        List<MessageDto> conversation = messageService.getConversation(user1, user2);
        return ResponseEntity.ok(conversation);
    }
}
