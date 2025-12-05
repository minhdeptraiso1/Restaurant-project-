package com.hoabanrestaurant.backendchat.nlu;

import org.springframework.stereotype.Component;

@Component
public class IntentDetector {

    public String detect(String text) {

        if (text == null || text.isEmpty()) {
            return "UNKNOWN";
        }

        // Chuẩn hoá chữ tiếng Việt → không dấu
        text = normalize(text);
        
        

        /* ================================
         * 1) Intent ĐẶT BÀN (ưu tiên cao nhất)
         * ================================ */
        boolean hasTime =
                text.matches(".*\\b\\d{1,2}h\\b.*") ||     // 7h, 19h
                        text.matches(".*\\b\\d{1,2}:\\d{2}\\b.*") || // 19:00
                        text.contains("toi nay") ||
                        text.contains("chieu nay") ||
                        text.contains("sang nay") ||
                        text.contains("trua nay") ||
                        text.contains("toi mai") ||
                        text.contains("ngay mai") ||
                        text.contains("cuoi tuan") ||
                        text.contains("thu") ||
                        text.contains("chu nhat");

        boolean hasPeople =
                text.matches(".*\\b\\d+ nguoi\\b.*") ||
                        text.matches(".*\\b\\d+ khach\\b.*") ||
                        text.contains("so nguoi") ||
                        text.contains("so khach") ||
                        text.contains("bao nhieu nguoi");

        boolean hasBookKeyword =
                text.contains("dat ban") ||
                        text.contains("dat cho") ||
                        text.contains("dat cho ngoi") ||
                        text.contains("book ban") ||
                        text.contains("booking") ||
                        text.contains("dat phong") ||
                        text.contains("dat cho") ||
                        text.contains("can dat") ||
                        text.contains("muon dat") ||
                        text.contains("tim ban") ||
                        text.contains("con ban") ||
                        text.contains("co ban") ||
                        text.contains("co cho") ||
                        text.contains("cho ngoi") ||
                        text.contains("cho trong") ||
                        text.contains("di an") ||
                        text.contains("hen di an") ||
                        text.contains("an toi") ||
                        text.contains("an trua") ||
                        text.contains("an sang");

        // Nếu có số người + thời gian → chắc chắn là đặt bàn
        if (hasPeople && hasTime) return "BOOK_TABLE";

        // Nếu có từ khóa đặt bàn → BOOK_TABLE
        if (hasBookKeyword) return "BOOK_TABLE";


        /* ================================
         * 2) Intent GỌI MÓN
         * ================================ */
        boolean hasFoodKeyword =
                text.contains("goi mon") ||
                        text.contains("cho toi mon") ||
                        text.contains("goi") ||
                        text.contains("muon an") ||
                        text.contains("an mon") ||
                        text.contains("lau") ||
                        text.contains("nuong") ||
                        text.contains("order") ||
                        text.contains("cho toi") ||
                        text.contains("lay cho") ||
                        text.contains("them") ||
                        text.contains("them mon") ||
                        text.contains("muon goi") ||
                        text.contains("muon dat") ||
                        text.contains("muon thu") ||
                        text.contains("thu mon") ||
                        text.contains("xin mon") ||
                        text.contains("can mon") ||
                        text.contains("cho xin") ||
                        text.contains("cho mot") ||
                        text.contains("cho hai") ||
                        text.contains("cho ba") ||
                        text.contains("mang den") ||
                        text.contains("mang cho") ||
                        text.contains("phuc vu");

        if (hasFoodKeyword) return "ORDER_DISH";


        /* =================================
         * 3) Intent hỏi gợi ý
         * ================================= */
        if (text.contains("mon nao ngon") ||
                text.contains("mon gi ngon") ||
                text.contains("an gi") ||
                text.contains("nen an") ||
                text.contains("goi y") ||
                text.contains("de xuat") ||
                text.contains("tu van") ||
                text.contains("mon hot") ||
                text.contains("mon dac biet") ||
                text.contains("mon noi tieng") ||
                text.contains("mon mac dinh") ||
                text.contains("mon pho bien") ||
                text.contains("recommend") ||
                text.contains("suggestion") ||
                text.contains("co mon nao") ||
                text.contains("co gi") ||
                text.contains("an thu") ||
                text.contains("thu cai gi")) {

            return "ASK_RECOMMENDATION";
        }

        /* =================================
         * 4) Intent hỏi giá
         * ================================= */
        if (text.contains("gia") ||
                text.contains("bao nhieu") ||
                text.contains("bao nhieu tien") ||
                text.contains("gia ca") ||
                text.contains("don gia") ||
                text.contains("tien") ||
                text.contains("het bao nhieu") ||
                text.contains("chi phi") ||
                text.contains("gia tien") ||
                text.contains("price") ||
                text.contains("cost") ||
                text.contains("tan nao") ||
                text.contains("mat tien") ||
                text.contains("phi")) {

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