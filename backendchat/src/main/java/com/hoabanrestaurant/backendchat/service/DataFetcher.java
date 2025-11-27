package com.hoabanrestaurant.backendchat.service;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
public class DataFetcher {

    private final RestTemplate rest = new RestTemplate();
    private final String BASE = "http://localhost:8080/api/internal/ai";

    public List<Map<String,Object>> fetchDishes() {
        ResponseEntity<List<Map<String,Object>>> res =
                rest.exchange(
                        BASE + "/dishes",
                        HttpMethod.GET,
                        null,
                        new ParameterizedTypeReference<List<Map<String,Object>>>() {}
                );
        return res.getBody();
    }

    public List<Map<String,Object>> fetchCombos() {
        ResponseEntity<List<Map<String,Object>>> res =
                rest.exchange(
                        BASE + "/combos",
                        HttpMethod.GET,
                        null,
                        new ParameterizedTypeReference<List<Map<String,Object>>>() {}
                );
        return res.getBody();
    }

    public List<Map<String,Object>> fetchTables() {
        ResponseEntity<List<Map<String,Object>>> res =
                rest.exchange(
                        BASE + "/tables/status",
                        HttpMethod.GET,
                        null,
                        new ParameterizedTypeReference<List<Map<String,Object>>>() {}
                );
        return res.getBody();
    }

    public List<Map<String,Object>> fetchSlots() {
        ResponseEntity<List<Map<String,Object>>> res =
                rest.exchange(
                        BASE + "/reservation/slots",
                        HttpMethod.GET,
                        null,
                        new ParameterizedTypeReference<List<Map<String,Object>>>() {}
                );
        return res.getBody();
    }
}
