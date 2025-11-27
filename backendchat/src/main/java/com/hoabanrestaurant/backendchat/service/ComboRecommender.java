package com.hoabanrestaurant.backendchat.service;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class ComboRecommender {

    public Map<String,Object> recommendCombo(int partySize, List<Map<String,Object>> combos) {
        Map<String,Object> best = null;

        for (Map<String,Object> combo : combos) {
            // lấy suggestedSum hoặc description để đoán
            try {
                List<Map<String,Object>> items = (List<Map<String,Object>>) combo.get("items");

                int totalServes = items.stream()
                        .mapToInt(i -> (int)i.get("quantity"))
                        .sum();

                if (totalServes >= partySize) {
                    best = combo;
                    break;
                }
            } catch (Exception ignore){}
        }
        return best;
    }
}
