package com.commentarium.services;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import jakarta.mail.internet.MimeMessage;
import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class EmailService {
    private final JavaMailSender javaMailSender;

    @Async
    public void sendSimpleMessage(SimpleMailMessage email) {
        javaMailSender.send(email);
    }

    @Async
    public void sendMimeMessage(MimeMessage message){
        javaMailSender.send(message);
    }
}
