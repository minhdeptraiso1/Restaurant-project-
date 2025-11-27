package com.hoabanrestaurant.backend.service.impl;

import com.hoabanrestaurant.backend.dto.request.ChatReq;
import com.hoabanrestaurant.backend.dto.response.ChatRes;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;


@Service
@RequiredArgsConstructor
public class AIClient {
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${chatbot.url}")
    private String aiServiceBaseUrl;

    @Value("${chatbot.internal-secret}")
    private String internalSecret;

    public ChatRes send(ChatReq req) {

        HttpHeaders headers = new HttpHeaders();
        headers.set("X-INTERNAL-SECRET", internalSecret);
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<ChatReq> entity = new HttpEntity<>(req, headers);
        return restTemplate.postForObject(aiServiceBaseUrl, entity, ChatRes.class);
    }
}
