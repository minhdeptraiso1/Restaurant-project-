package com.hoabanrestaurant.backendchat.nlu;

import org.springframework.stereotype.Component;

@Component
public class IntentDetector {

    public String detect(String text) {

        if (text == null || text.isEmpty()) {
            return "UNKNOWN";
        }

        // Intent đặt bàn
        if (text.contains("dat ban") ||
                text.contains("dat cho") ||
                text.contains("ban") && text.contains("nguoi") ||
                text.matches(".*\\d+h.*")) {
            return "BOOK_TABLE";
        }

        // Intent gọi món
        if (text.contains("goi") ||
                text.contains("muon") ||
                text.contains("an") ||
                text.contains("lau") ||
                text.contains("nuong") ||
                text.contains("mon")) {
            return "ORDER_DISH";
        }

        // Intent hỏi thông tin (ví dụ: “món nào ngon”, “món hot”)
        if (text.contains("mon nao") ||
                text.contains("goi y") ||
                text.contains("recommend") ||
                text.contains("hot") ||
                text.contains("ngon")) {
            return "ASK_RECOMMENDATION";
        }

        // Intent hỏi giá
        if (text.contains("gia") ||
                text.contains("bao nhieu") ||
                text.contains("tien")) {
            return "ASK_PRICE";
        }

        // Mặc định
        return "SMALL_TALK";
    }
}
