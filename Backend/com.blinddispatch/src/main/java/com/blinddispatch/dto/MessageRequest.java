package com.blinddispatch.dto;

import lombok.Data;

@Data
public class MessageRequest {
    private String recipientUsername;
    private String content;
}
