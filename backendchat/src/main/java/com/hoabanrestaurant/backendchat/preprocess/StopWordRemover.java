package com.hoabanrestaurant.backendchat.preprocess;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class StopWordRemover {

    private static final Set<String> STOP_WORDS = new HashSet<>(Arrays.asList(
            "la", "thi", "ma", "va", "và", "nha", "nhe", "nhé", "ạ",
            "em", "anh", "chi", "cho", "voi", "day", "di", "ke",
            "minh", "toi", "ban", "ay", "nhà", "co", "cai"
    ));

    public String remove(String text) {
        StringBuilder sb = new StringBuilder();
        for (String w : text.split(" ")) {
            if (!STOP_WORDS.contains(w)) {
                sb.append(w).append(" ");
            }
        }
        return sb.toString().trim();
    }
}
