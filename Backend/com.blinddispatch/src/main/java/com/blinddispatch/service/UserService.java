package com.blinddispatch.service;

import com.blinddispatch.model.User;
import com.blinddispatch.model.VerificationCode;
import com.blinddispatch.repository.UserRepository;
import com.blinddispatch.repository.VerificationCodeRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final VerificationCodeRepository codeRepository;
    private final JavaMailSender mailSender;

    public void registerUser(User user) {
        user.setActive(true);
        user.setVerified(false);
        userRepository.save(user);

        String code = generateVerificationCode();
        VerificationCode verificationCode = VerificationCode.builder()
                .code(code)
                .createdAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusMinutes(10))
                .used(false)
                .user(user)
                .build();

        codeRepository.save(verificationCode);
        sendVerificationEmail(user.getEmail(), code);
    }

    private String generateVerificationCode() {
        Random random = new Random();
        int number = 100000 + random.nextInt(900000);
        return String.valueOf(number);
    }

    private void sendVerificationEmail(String recipientEmail, String code) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setTo(recipientEmail);
            helper.setSubject("Your BlindDispatch Verification Code");
            helper.setText("Your verification code is: " + code);

            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send verification email.");
        }
    }
}
