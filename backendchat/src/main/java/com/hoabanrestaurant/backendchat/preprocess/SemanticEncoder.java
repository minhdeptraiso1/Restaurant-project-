package com.hoabanrestaurant.backendchat.preprocess;

import org.springframework.stereotype.Component;

@Component
public class SemanticEncoder {

    public String encode(String text, String intent) {
        return intent + "|" + text.hashCode();
    }

}

