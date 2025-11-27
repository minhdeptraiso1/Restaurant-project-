package com.hoabanrestaurant.backendchat.preprocess;

import java.util.HashMap;
import java.util.Map;

public class WordLemmatizer {

    private static final Map<String, String> DICT = new HashMap<>();

    static {
        // Menu mapping (có thể mở rộng sau)
        DICT.put("lau ga", "lau ga");
        DICT.put("ga nuong", "ga nuong");
        DICT.put("ca nuong", "ca nuong");

        // General
        DICT.put("muon", "muon");
        DICT.put("dat", "dat");
        DICT.put("goi", "goi");
        DICT.put("an", "");
        DICT.put("mon", "");
    }

    public String lemmatize(String text) {
        for (Map.Entry<String, String> e : DICT.entrySet()) {
            text = text.replace(e.getKey(), e.getValue());
        }
        return text;
    }
}
