package com.hoabanrestaurant.backendchat.nlu;

import org.springframework.stereotype.Component;

@Component
public class IntentDetector {

    public String detect(String text) {

        if (text == null || text.isEmpty()) {
            System.out.println("[DEBUG] Text is null or empty");
            return "UNKNOWN";
        }

        System.out.println("[DEBUG] Original text: " + text);

        text = normalize(text);

        System.out.println("[DEBUG] Normalized text: " + text);

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

        System.out.println("[DEBUG] hasTime: " + hasTime);
        System.out.println("[DEBUG] hasPeople: " + hasPeople);
        System.out.println("[DEBUG] hasBookKeyword: " + hasBookKeyword);

        if ((hasPeople && hasTime) || hasBookKeyword) {
            System.out.println("[DEBUG] ✓ Returning BOOK_TABLE");
            return "BOOK_TABLE";
        }

        /* ================================
         * 2) ASK_RECOMMENDATION
         * ================================ */
        boolean hasRecommendKeyword =
                text.contains("tu van") ||
                        text.contains("goi y") ||
                        text.contains("de xuat") ||
                        text.contains("mon nao ngon") ||
                        text.contains("an gi") ||
                        text.contains("nen an") ||
                        text.contains("khong cay") ||
                        text.contains("it cay") ||
                        text.contains("cay vua") ||
                        text.contains("mon chay") ||
                        text.contains("chay") ||
                        text.contains("healthy") ||
                        text.contains("thanh dam") ||
                        text.contains("tot cho suc khoe") ||
                        text.contains("an gi cho tre em") ||
                        text.contains("an gi cho nguoi gia") ||
                        text.contains("mon tot cho suc khoe");

        System.out.println("[DEBUG] hasRecommendKeyword: " + hasRecommendKeyword);
        System.out.println("[DEBUG] - contains 'tu van': " + text.contains("tu van"));
        System.out.println("[DEBUG] - contains 'goi y': " + text.contains("goi y"));
        System.out.println("[DEBUG] - contains 'de xuat': " + text.contains("de xuat"));

        if (hasRecommendKeyword) {
            System.out.println("[DEBUG] ✓ Returning ASK_RECOMMENDATION");
            return "ASK_RECOMMENDATION";
        }

        /* ================================
         * 3) ASK_PRICE
         * ================================ */
        boolean hasPriceKeyword =
                text.contains("gia") ||
                        text.contains("bao nhieu") ||
                        text.contains("gia tien") ||
                        text.contains("gia ca");

        System.out.println("[DEBUG] hasPriceKeyword: " + hasPriceKeyword);

        if (hasPriceKeyword) {
            System.out.println("[DEBUG] ✓ Returning ASK_PRICE");
            return "ASK_PRICE";
        }

        System.out.println("[DEBUG] ✗ Returning SMALL_TALK (default)");
        return "SMALL_TALK";
    }



    /* ---------------------------------------------
     * Hàm normalize tiếng Việt thành không dấu
     * --------------------------------------------- */
    private String normalize(String text) {
        String normalized = java.text.Normalizer
                .normalize(text.toLowerCase(), java.text.Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "")
                .trim();

        System.out.println("[DEBUG] normalize() input: '" + text + "'");
        System.out.println("[DEBUG] normalize() output: '" + normalized + "'");

        return normalized;
    }
}