package com.hoabanrestaurant.backend.controller;


import com.hoabanrestaurant.backend.dto.request.ChatReq;
import com.hoabanrestaurant.backend.dto.response.ChatRes;
import com.hoabanrestaurant.backend.service.impl.AIClient;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/chat")
@RequiredArgsConstructor

public class ChatProxyController {

    private final AIClient aiClient;

    @PostMapping
    public ChatRes chat(@RequestBody ChatReq req) {
        return aiClient.send(req);
    }
}
