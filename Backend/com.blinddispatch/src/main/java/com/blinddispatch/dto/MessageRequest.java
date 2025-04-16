package com.blinddispatch.dto;

import lombok.Data;

@Data
public class MessageRequest {
    private String recipientUsername;
    private Long recipientPublicId;
    private String content;
}
