package com.hoabanrestaurant.backendchat.preprocess;

import java.text.Normalizer;
import java.util.LinkedHashMap;
import java.util.Map;

public class WordLemmatizer {

    private static final Map<String, String> DICT = new LinkedHashMap<>();

    static {
        // Món ăn (normalize không dấu)
        DICT.put("lau ga", "lau ga");
        DICT.put("lau hai san", "lau hai san");
        DICT.put("lau bo", "lau bo");

        DICT.put("ga nuong", "ga nuong");
        DICT.put("ga hap", "ga hap");
        DICT.put("ga xao", "ga xao");

        DICT.put("ca nuong", "ca nuong");
        DICT.put("ca hap", "ca hap");

        // Gom nhóm từ chung vào base form
        DICT.put("goi mon", "goi");
        DICT.put("dat ban", "dat ban");
        DICT.put("dat cho", "dat ban");

        // Stopwords xoá
        DICT.put("an", "");
        DICT.put("mon", "");
        DICT.put("cho minh", "");
        DICT.put("toi muon", "muon");
    }

    public String lemmatize(String text) {

        // chuẩn hoá: lower + remove dấu
        text = normalize(text);

        // thay thế theo dictionary
        for (Map.Entry<String, String> e : DICT.entrySet()) {
            text = text.replace(e.getKey(), e.getValue());
        }

        // xoá space thừa
        return text.replaceAll("\\s{2,}", " ").trim();
    }

    private String normalize(String s) {
        return Normalizer.normalize(s.toLowerCase(), Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "")
                .trim();
    }
}
