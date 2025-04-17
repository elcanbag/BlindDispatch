package com.blinddispatch.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ContactDto {
    private Long id;
    private String username;
    private Long publicId;
}
