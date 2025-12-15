package com.hoabanrestaurant.backendchat.preprocess;

import java.text.Normalizer;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class StopWordRemover {

    private static final Set<String> STOP_WORDS = new HashSet<>(Arrays.asList(
            // Đại từ
            "anh", "em", "chi", "chị", "tôi", "toi", "tao", "minh", "mình",
            // "ban", "bạn", // ← CHÚ Ý: đã xử lý riêng ở dưới

            // Từ đệm
            "à", "a", "ạ", "ơ", "ờ", "vậy", "thế", "nhỉ", "nhé", "nha", "nhen",
            "với", "voi", "với", "va", "và", "ma", "mà", "thì", "thi", "kiểu",

            // Từ filler
            "ơi", "ha", "hả", "hử", "he", "dạ", "ạ", "ạk"
    ));

    // ← THÊM MỚI: Các từ KHÔNG ĐƯỢC xóa trong context nhất định
    private static final Set<String> PROTECTED_WORDS = new HashSet<>(Arrays.asList(
            "tu",      // tư (trong "tư vấn")
            "van",     // vấn (trong "tư vấn")
            "goi",     // gợi (trong "gợi ý")
            "y",       // ý (trong "gợi ý")
            "de",      // đề (trong "đề xuất")
            "xuat",    // xuất (trong "đề xuất")
            "dat",     // đặt (trong "đặt bàn")
            "ban",     // bàn (trong "đặt bàn")
            "nguoi",   // người
            "gia",     // giá
            "mon",     // món
            "chay",    // chay
            "ngon"     // ngon
    ));

    public String remove(String text) {

        // 1) Remove emoji, ký tự đặc biệt
        text = text.replaceAll("[^\\p{L}\\p{N}\\s]", " ");

        // 2) Chuẩn hóa Unicode
        text = Normalizer.normalize(text, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "") // xoá dấu
                .toLowerCase()
                .replaceAll("\\s+", " ")
                .trim();

        // 3) Xử lý stop-words
        StringBuilder sb = new StringBuilder();
        for (String w : text.split(" ")) {

            // Giữ lại từ nếu nó nằm trong danh sách bảo vệ
            if (PROTECTED_WORDS.contains(w)) {
                sb.append(w).append(" ");
                continue;
            }

            // KHÔNG xoá "ban" nếu user đang nói về BÀN (detect)
            if (w.equals("ban") && text.contains("dat") && !text.contains("ban be")) {
                sb.append("ban ");
                continue;
            }

            // Xóa stop words thông thường
            if (!STOP_WORDS.contains(w)) {
                sb.append(w).append(" ");
            }
        }

        return sb.toString().trim();
    }
}