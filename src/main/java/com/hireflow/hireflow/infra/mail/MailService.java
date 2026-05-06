package com.hireflow.hireflow.infra.mail;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class MailService {

    private final JavaMailSender mailSender;

    public void sendDeadlineReminder(String to, String jobTitle, LocalDate deadline) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("[HireFlow] " + jobTitle + " 마감 3일 전입니다.");
        message.setText(jobTitle + " 공고 마감일이 " + deadline + " 입니다. 서류 준비 잊지 마세요!");
        mailSender.send(message);
    }

    public void sendInterviewReminder(String to, String jobTitle, LocalDate interviewDate) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("[HireFlow] " + jobTitle + " 면접 D-1");
        message.setText(jobTitle + " 면접이 내일(" + interviewDate + ")입니다. 파이팅!");
        mailSender.send(message);
    }
}
