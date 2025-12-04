package com.hoabanrestaurant.backend.service.impl;

import com.hoabanrestaurant.backend.dto.request.ChatAiReq;
import com.hoabanrestaurant.backend.dto.request.ChatReq;
import com.hoabanrestaurant.backend.dto.response.ChatRes;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.UUID;


@Service
@RequiredArgsConstructor
public class AIClient {
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${chatbot.url}")
    private String aiServiceBaseUrl;

    @Value("${chatbot.internal-secret}")
    private String internalSecret;

    public ChatRes send(ChatReq req, Jwt jwt) {

        UUID uid = UUID.fromString(jwt.getClaimAsString("uid"));

        HttpHeaders headers = new HttpHeaders();
        headers.set("X-INTERNAL-SECRET", internalSecret);
        headers.setContentType(MediaType.APPLICATION_JSON);

        ChatAiReq chatAiReq = new ChatAiReq(req.sessionId(), uid, req.message());

        HttpEntity<ChatAiReq> entity = new HttpEntity<>(chatAiReq, headers);
        return restTemplate.postForObject(aiServiceBaseUrl, entity, ChatRes.class);
    }
}
