package com.hoabanrestaurant.backendchat.service;

import com.hoabanrestaurant.backendchat.dto.BookingCommand;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class BookingClient {

    private final RestTemplate rest = new RestTemplate();

    private final String BASE = "http://localhost:8080/api/internal/ai";

    public Map<String,Object> createReservation(
            String sessionId,
            UUID userId,
            BookingCommand cmd
    ) {
        Map<String,Object> req = Map.of(
                "startTime", cmd.startTime().toString(),
                "endTime", cmd.endTime().toString(),
                "partySize", cmd.partySize(),
                "note", cmd.note()
        );

        // Gửi sessionId + userId sang backend chính
        String url = BASE + "/reservation/create"
                + "?sessionId=" + sessionId
                + "&userId=" + userId;

        return rest.postForObject(url, req, Map.class);
    }
}
