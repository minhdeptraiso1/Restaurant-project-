package com.hoabanrestaurant.backend.service.impl;

import com.hoabanrestaurant.backend.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.util.Locale;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {
    private final JavaMailSender mailSender;
    private final SpringTemplateEngine templateEngine;
    @Value("${app.mail.from:Hoa Ban <no-reply@hoaban.vn>}")
    private String from;

    @Override
    public void send(String to, String subject, String html) {
        try {
            var mime = mailSender.createMimeMessage();
            var helper = new MimeMessageHelper(mime, "UTF-8");
            helper.setFrom(from);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(html, true);
            mailSender.send(mime);
        } catch (Exception e) {
            throw new RuntimeException("Gửi email thất bại", e);
        }
    }

    @Override
    public void sendTemplate(String to, String subject, String template, Map<String, Object> model) {
        String html = templateEngine.process(template, new Context(Locale.forLanguageTag("vi"), model));
        send(to, subject, html);
    }
}
