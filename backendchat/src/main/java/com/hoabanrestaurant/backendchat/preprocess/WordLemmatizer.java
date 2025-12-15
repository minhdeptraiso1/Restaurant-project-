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

        // QUAN TRỌNG: Chỉ xóa "an" và "mon" khi chúng là từ RIÊNG BIỆT
        // Sử dụng word boundary \b để tránh xóa "an" trong "van", "ban"
        // CHÚ Ý: Không thể dùng \b trong replace trực tiếp
        // Nên KHÔNG nên xóa "an" và "mon" ở đây
        // Thay vào đó, để StopWordRemover xử lý

        // DICT.put("an", "");  // ← XÓA DÒNG NÀY!
        // DICT.put("mon", ""); // ← XÓA DÒNG NÀY!

        DICT.put("cho minh", "");
        DICT.put("toi muon", "muon");
    }

    public String lemmatize(String text) {

        System.out.println("[LEMMATIZER-DEBUG] Input: '" + text + "'");

        // chuẩn hoá: lower + remove dấu
        text = normalize(text);

        System.out.println("[LEMMATIZER-DEBUG] After normalize: '" + text + "'");

        // thay thế theo dictionary
        for (Map.Entry<String, String> e : DICT.entrySet()) {
            String before = text;
            text = text.replace(e.getKey(), e.getValue());
            if (!before.equals(text)) {
                System.out.println("[LEMMATIZER-DEBUG] Replaced '" + e.getKey() + "' -> '" + e.getValue() + "'");
                System.out.println("[LEMMATIZER-DEBUG] Result: '" + text + "'");
            }
        }

        // xoá space thừa
        text = text.replaceAll("\\s{2,}", " ").trim();

        System.out.println("[LEMMATIZER-DEBUG] Final output: '" + text + "'");

        return text;
    }

    private String normalize(String s) {
        return Normalizer.normalize(s.toLowerCase(), Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "")
                .trim();
    }
}