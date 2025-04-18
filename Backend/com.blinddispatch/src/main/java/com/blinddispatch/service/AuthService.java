package com.blinddispatch.service;

import com.blinddispatch.model.User;
import com.blinddispatch.model.VerificationCode;
import com.blinddispatch.repository.UserRepository;
import com.blinddispatch.repository.VerificationCodeRepository;
import com.blinddispatch.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final VerificationCodeRepository codeRepository;
    private final JwtUtil jwtUtil;

    public String verifyCode(String username, String code) {
        VerificationCode verificationCode = codeRepository.findByCode(code)
                .orElseThrow(() -> new RuntimeException("Invalid verification code"));

        if (!verificationCode.getUser().getUsername().equals(username))
            throw new RuntimeException("Verification code does not belong to the provided username");

        if (verificationCode.isUsed())
            throw new RuntimeException("Verification code has already been used");

        if (verificationCode.getExpiresAt().isBefore(LocalDateTime.now()))
            throw new RuntimeException("Verification code has expired");

        User user = verificationCode.getUser();
        user.setVerified(true);
        userRepository.save(user);

        verificationCode.setUsed(true);
        codeRepository.save(verificationCode);

        return "Email " + user.getEmail() + " has been successfully verified.";
    }

    public String login(String username, String password) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Username not found"));

        if (!user.getPassword().equals(password))
            throw new RuntimeException("Incorrect password");

        if (!user.isVerified())
            throw new RuntimeException("User not verified");


        return jwtUtil.generateToken(user.getId(), user.getUsername());
    }

}
