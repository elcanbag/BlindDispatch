package com.blinddispatch.controller;

import com.blinddispatch.dto.ContactDto;
import com.blinddispatch.dto.InboxItemDto;
import com.blinddispatch.dto.MessageDto;
import com.blinddispatch.dto.MessageRequest;
import com.blinddispatch.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
    public ResponseEntity<MessageDto> sendMessage(@RequestBody MessageRequest messageRequest) {
        String senderUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        MessageDto messageDto = messageService.sendMessage(senderUsername, messageRequest);
        return ResponseEntity.ok(messageDto);
    }

    @GetMapping("/conversation")
    public ResponseEntity<List<MessageDto>> getConversation(@RequestParam("user2") String user2,
                                                            @RequestParam(value = "type", defaultValue = "username") String type) {
        String user1 = SecurityContextHolder.getContext().getAuthentication().getName();
        List<MessageDto> conversation = messageService.getConversation(user1, user2, type);
        return ResponseEntity.ok(conversation);
    }

    @GetMapping("/contacts")
    public ResponseEntity<List<ContactDto>> getContacts(@AuthenticationPrincipal String username) {
        List<ContactDto> contacts = messageService.getContacts(username);
        return ResponseEntity.ok(contacts);
    }

    @PatchMapping("/mark-read/{id}")
    public ResponseEntity<Void> markAsRead(@PathVariable Long id) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        messageService.markMessageAsRead(id, username);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/search")
    public ResponseEntity<List<MessageDto>> searchMessages(@RequestParam("user2") String user2,
                                                           @RequestParam("query") String query,
                                                           @RequestParam(value = "type", defaultValue = "username") String type) {
        String user1 = SecurityContextHolder.getContext().getAuthentication().getName();
        List<MessageDto> results = messageService.searchMessages(user1, user2, type, query);
        return ResponseEntity.ok(results);
    }

    @GetMapping("/inbox")
    public ResponseEntity<List<InboxItemDto>> getInbox() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        List<InboxItemDto> inbox = messageService.getInbox(username);
        return ResponseEntity.ok(inbox);
    }

    @PatchMapping("/delete/{id}")
    public ResponseEntity<Void> deleteMessage(@PathVariable Long id,
                                              @RequestParam("type") String type) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        messageService.deleteMessage(id, username, type);
        return ResponseEntity.ok().build();
    }

}
