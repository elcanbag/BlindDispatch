package com.blinddispatch.service;

import com.blinddispatch.dto.MessageDto;
import com.blinddispatch.dto.UserDto;
import com.blinddispatch.model.Message;
import com.blinddispatch.model.User;
import com.blinddispatch.repository.MessageRepository;
import com.blinddispatch.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MessageService {

    private final UserRepository userRepository;
    private final MessageRepository messageRepository;

    public Message sendMessage(String senderUsername, com.blinddispatch.dto.MessageRequest messageRequest) {
        User sender = userRepository.findByUsername(senderUsername)
                .orElseThrow(() -> new RuntimeException("Sender not found"));
        User recipient = userRepository.findByUsername(messageRequest.getRecipientUsername())
                .orElseThrow(() -> new RuntimeException("Recipient not found"));
        Message message = Message.builder()
                .sender(sender)
                .recipient(recipient)
                .content(messageRequest.getContent())
                .sentAt(LocalDateTime.now())
                .build();
        return messageRepository.save(message);
    }

    public List<MessageDto> getConversation(String user1, String user2) {
        User u1 = userRepository.findByUsername(user1)
                .orElseThrow(() -> new RuntimeException("User not found"));
        User u2 = userRepository.findByUsername(user2)
                .orElseThrow(() -> new RuntimeException("User not found"));
        List<Message> messages = messageRepository.findBySenderAndRecipientOrSenderAndRecipient(u1, u2, u2, u1);
        return messages.stream().map(message -> {
            UserDto senderDto = new UserDto();
            senderDto.setId(message.getSender().getId());
            senderDto.setUsername(message.getSender().getUsername());
            senderDto.setActive(message.getSender().isActive());
            senderDto.setVerified(message.getSender().isVerified());
            UserDto recipientDto = new UserDto();
            recipientDto.setId(message.getRecipient().getId());
            recipientDto.setUsername(message.getRecipient().getUsername());
            recipientDto.setActive(message.getRecipient().isActive());
            recipientDto.setVerified(message.getRecipient().isVerified());
            MessageDto dto = new MessageDto();
            dto.setId(message.getId());
            dto.setSender(senderDto);
            dto.setRecipient(recipientDto);
            dto.setContent(message.getContent());
            dto.setSentAt(message.getSentAt());
            return dto;
        }).collect(Collectors.toList());
    }
}
