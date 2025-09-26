package com.chronos.jobscheduler.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;

@Service
public class NotificationService {
    @Autowired private JavaMailSender mailSender;
    @Value("${spring.mail.username:}")
    private String from;

    public void sendFailureNotification(String to, String subject, String body) {
        try {
            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setFrom(from);
            msg.setTo(to);
            msg.setSubject(subject);
            msg.setText(body);
            mailSender.send(msg);
        } catch (Exception e) {
            // log, but don't fail job execution because of notification problems
            System.err.println("Failed to send email: " + e.getMessage());
        }
    }
}
