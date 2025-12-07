package com.hoabanrestaurant.backendchat.nlu;

import org.springframework.stereotype.Component;

@Component
public class IntentDetector {

    public String detect(String text) {

        if (text == null || text.isEmpty()) return "UNKNOWN";

        text = normalize(text);

        /* ================================
         * 1) BOOK_TABLE
         * ================================ */
        boolean hasTime =
                text.matches(".*\\b\\d{1,2}h\\b.*") ||
                        text.matches(".*\\b\\d{1,2}:\\d{2}\\b.*") ||
                        text.contains("toi nay") ||
                        text.contains("chieu nay") ||
                        text.contains("sang nay") ||
                        text.contains("trua nay") ||
                        text.contains("toi mai") ||
                        text.contains("ngay mai") ||
                        text.contains("cuoi tuan") ||
                        text.matches(".*thu [2-7].*") ||
                        text.contains("chu nhat");

        boolean hasPeople =
                text.matches(".*\\b\\d+ nguoi\\b.*") ||
                        text.matches(".*\\b\\d+ khach\\b.*") ||
                        text.contains("so nguoi") ||
                        text.contains("bao nhieu nguoi");

        boolean hasBookKeyword =
                text.contains("dat ban") ||
                        text.contains("dat cho") ||
                        text.contains("book ban") ||
                        text.contains("booking");

        if ((hasPeople && hasTime) || hasBookKeyword) {
            return "BOOK_TABLE";
        }

        /* ================================
         * 2) ASK_RECOMMENDATION
         * ================================ */
        if (text.contains("tu van") ||
                text.contains("goi y") ||
                text.contains("de xuat") ||
                text.contains("mon nao ngon") ||
                text.contains("an gi") ||
                text.contains("nen an") ||
                text.contains("mon khong cay") ||
                text.contains("mon it cay") ||
                text.contains("mon chay") ||
                text.contains("mon healthy")) {
            return "ASK_RECOMMENDATION";
        }

        /* ================================
         * 3) ORDER_DISH (CHỈ MATCH KHI USER ĐẶC BIỆT YÊU CẦU)
         * ================================ */
        if (text.contains("goi mon") ||
                text.startsWith("goi ") ||
                text.startsWith("cho toi ") ||
                text.contains("lay them") ||
                text.contains("them mon") ||
                text.contains("toi muon an")) {
            return "ORDER_DISH";
        }

        /* ================================
         * 4) ASK_PRICE
         * ================================ */
        if (text.contains("gia") ||
                text.contains("bao nhieu") ||
                text.contains("gia tien") ||
                text.contains("gia ca")) {
            return "ASK_PRICE";
        }

        return "SMALL_TALK";
    }



    /* ---------------------------------------------
     * Hàm normalize tiếng Việt thành không dấu
     * --------------------------------------------- */
    private String normalize(String text) {
        return java.text.Normalizer
                .normalize(text.toLowerCase(), java.text.Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "")
                .trim();
    }
}