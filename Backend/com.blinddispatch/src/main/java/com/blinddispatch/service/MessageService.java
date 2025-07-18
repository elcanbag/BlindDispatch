package com.blinddispatch.service;

import com.blinddispatch.dto.*;
import com.blinddispatch.model.Message;
import com.blinddispatch.model.User;
import com.blinddispatch.repository.MessageRepository;
import com.blinddispatch.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MessageService {

    private final UserRepository userRepository;
    private final MessageRepository messageRepository;

    public MessageDto sendMessage(String senderUsername, MessageRequest messageRequest) {
        User sender = userRepository.findByUsername(senderUsername)
                .orElseThrow(() -> new RuntimeException("Sender not found"));
        User recipient;
        boolean hideRecipientUsername = false;
        if (messageRequest.getRecipientPublicId() != null) {
            recipient = userRepository.findByPublicId(messageRequest.getRecipientPublicId())
                    .orElseThrow(() -> new RuntimeException("Recipient not found"));
            hideRecipientUsername = true;
        } else {
            recipient = userRepository.findByUsername(messageRequest.getRecipientUsername())
                    .orElseThrow(() -> new RuntimeException("Recipient not found"));
        }
        Message message = Message.builder()
                .sender(sender)
                .recipient(recipient)
                .content(messageRequest.getContent())
                .sentAt(LocalDateTime.now())
                .read(false)
                .build();
        Message savedMessage = messageRepository.save(message);
        return mapToDto(savedMessage, hideRecipientUsername);
    }

    public List<MessageDto> getConversation(String user1, String user2, String identifierType) {
        User u1 = userRepository.findByUsername(user1)
                .orElseThrow(() -> new RuntimeException("User not found"));
        User u2;
        if ("public".equalsIgnoreCase(identifierType)) {
            try {
                Long publicId = Long.parseLong(user2);
                u2 = userRepository.findByPublicId(publicId)
                        .orElseThrow(() -> new RuntimeException("User not found"));
            } catch (NumberFormatException e) {
                throw new RuntimeException("Invalid public ID format");
            }
        } else {
            u2 = userRepository.findByUsername(user2)
                    .orElseThrow(() -> new RuntimeException("User not found"));
        }
        List<Message> messages = messageRepository.findBySenderAndRecipientOrSenderAndRecipient(u1, u2, u2, u1);

        return messages.stream()
                .filter(m -> {
                    if (m.getSender().equals(u1) && m.isDeletedBySender()) return false;
                    if (m.getRecipient().equals(u1) && m.isDeletedByRecipient()) return false;
                    return true;
                })
                .map(message -> mapToDto(message, "public".equalsIgnoreCase(identifierType)))
                .collect(Collectors.toList());
    }


    private MessageDto mapToDto(Message message, boolean hideRecipientUsername) {
        UserDto senderDto = new UserDto();
        senderDto.setId(message.getSender().getId());
        senderDto.setUsername(message.getSender().getUsername());
        senderDto.setPublicId(message.getSender().getPublicId());
        senderDto.setActive(message.getSender().isActive());
        senderDto.setVerified(message.getSender().isVerified());

        UserDto recipientDto = new UserDto();
        recipientDto.setId(message.getRecipient().getId());
        recipientDto.setPublicId(message.getRecipient().getPublicId());
        recipientDto.setActive(message.getRecipient().isActive());
        recipientDto.setVerified(message.getRecipient().isVerified());
        if (hideRecipientUsername) {
            recipientDto.setUsername(null);
        } else {
            recipientDto.setUsername(message.getRecipient().getUsername());
        }

        MessageDto dto = new MessageDto();
        dto.setId(message.getId());
        dto.setSender(senderDto);
        dto.setRecipient(recipientDto);
        dto.setContent(message.getContent());
        dto.setSentAt(message.getSentAt());
        dto.setRead(message.isRead());
        return dto;
    }

    public List<ContactDto> getContacts(String currentUsername) {
        User me = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<User> senders = messageRepository.findDistinctSendersByRecipient(me);
        List<User> recipients = messageRepository.findDistinctRecipientsBySender(me);


        Set<User> uniqueContacts = new HashSet<>();
        uniqueContacts.addAll(senders);
        uniqueContacts.addAll(recipients);

        return uniqueContacts.stream()
                .map(u -> new ContactDto(u.getId(), u.getUsername(), u.getPublicId()))
                .collect(Collectors.toList());
    }


    public void markMessageAsRead(Long messageId, String username) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new RuntimeException("Message not found"));
        if (!message.getRecipient().getUsername().equals(username)) {
            throw new RuntimeException("Unauthorized");
        }
        message.setRead(true);
        messageRepository.save(message);
    }

    public List<MessageDto> searchMessages(String user1Name, String user2Name, String type, String keyword) {
        User user1 = userRepository.findByUsername(user1Name)
                .orElseThrow(() -> new RuntimeException("User not found"));
        User user2;

        if ("public".equalsIgnoreCase(type)) {
            try {
                Long publicId = Long.parseLong(user2Name);
                user2 = userRepository.findByPublicId(publicId)
                        .orElseThrow(() -> new RuntimeException("User not found"));
            } catch (NumberFormatException e) {
                throw new RuntimeException("Invalid public ID format");
            }
        } else {
            user2 = userRepository.findByUsername(user2Name)
                    .orElseThrow(() -> new RuntimeException("User not found"));
        }

        List<Message> messages = messageRepository.searchMessagesBetweenUsers(user1, user2, keyword);

        return messages.stream()
                .filter(m -> {
                    if (m.getSender().equals(user1) && m.isDeletedBySender()) return false;
                    if (m.getRecipient().equals(user1) && m.isDeletedByRecipient()) return false;
                    return true;
                })
                .map(m -> mapToDto(m, "public".equalsIgnoreCase(type)))
                .collect(Collectors.toList());
    }


    public List<InboxItemDto> getInbox(String username) {
        User me = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<Message> messages = messageRepository.findAllMessagesInvolvingUser(me);

        Map<Long, InboxItemDto> map = new LinkedHashMap<>();
        for (Message m : messages) {
            boolean senderDeleted = m.getSender().equals(me) && m.isDeletedBySender();
            boolean recipientDeleted = m.getRecipient().equals(me) && m.isDeletedByRecipient();
            if (senderDeleted || recipientDeleted) continue;

            User other = m.getSender().equals(me) ? m.getRecipient() : m.getSender();
            if (!map.containsKey(other.getId())) {
                map.put(other.getId(), new InboxItemDto(
                        other.getId(),
                        other.getUsername(),
                        other.getPublicId(),
                        m.getSentAt()
                ));
            }
        }

        return new ArrayList<>(map.values());
    }


    public void deleteMessage(Long messageId, String username, String type) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new RuntimeException("Message not found"));

        boolean isSender = message.getSender().getUsername().equals(username);
        boolean isRecipient = message.getRecipient().getUsername().equals(username);

        if (!isSender && !isRecipient) {
            throw new RuntimeException("Unauthorized");
        }

        if ("double".equalsIgnoreCase(type)) {
            if (!isSender) {
                throw new RuntimeException("Only sender can delete for both sides");
            }
            LocalDateTime now = LocalDateTime.now();
            if (message.getSentAt().plusMinutes(2).isBefore(now)) {
                throw new RuntimeException("Too late to delete for both sides");
            }
            message.setContent("This message deleted by USER");
            message.setDeletedBySender(true);
            message.setDeletedByRecipient(true);
            messageRepository.save(message);
        } else if ("one".equalsIgnoreCase(type)) {
            if (isSender) {
                message.setDeletedBySender(true);
            } else if (isRecipient) {
                message.setDeletedByRecipient(true);
            }
            if (message.isDeletedBySender() && message.isDeletedByRecipient()) {
                messageRepository.delete(message);
            } else {
                messageRepository.save(message);
            }
        } else {
            throw new RuntimeException("Invalid delete type");
        }
    }

}
