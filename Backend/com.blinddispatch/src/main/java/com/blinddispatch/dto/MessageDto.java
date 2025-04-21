package com.blinddispatch.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class MessageDto {
    private Long id;
    private UserDto sender;
    private UserDto recipient;
    private String content;
    private LocalDateTime sentAt;
    private boolean read;
}
