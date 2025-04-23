package com.blinddispatch.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class InboxItemDto {
    private Long id;
    private String username;
    private Long publicId;
    private LocalDateTime lastMessageTime;
}
