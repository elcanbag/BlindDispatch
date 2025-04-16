package com.blinddispatch.controller;

import com.blinddispatch.model.User;
import com.blinddispatch.service.UserService;
import com.blinddispatch.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody User user) {
        userService.registerUser(user);
        return new ResponseEntity<>("Verification code sent to your email.", HttpStatus.CREATED);
    }

    @PostMapping("/verify")
    public ResponseEntity<String> verify(@RequestParam("username") String username,
                                         @RequestParam("code") String code) {
        String result = authService.verifyCode(username, code);
        return ResponseEntity.ok(result);
    }
}
