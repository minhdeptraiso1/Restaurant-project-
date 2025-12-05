package com.hoabanrestaurant.backendchat.controller;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.hoabanrestaurant.backendchat.dto.ChatReq;
import com.hoabanrestaurant.backendchat.dto.ChatRes;
import com.hoabanrestaurant.backendchat.service.ChatAIService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
@CrossOrigin("*")
public class ChatAiController {

    private final ChatAIService chatAIService;

    @PostMapping("/chat")
    public ResponseEntity<ChatRes> chat(@RequestBody ChatReq req) throws JsonProcessingException {
        return ResponseEntity.ok(chatAIService.process(req));
    }


}