package com.blinddispatch.dto;

import lombok.Data;

@Data
public class UserDto {
    private Long id;
    private String username;
    private boolean active;
    private boolean verified;
}
