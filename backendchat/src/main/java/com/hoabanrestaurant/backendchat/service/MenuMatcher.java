package com.hoabanrestaurant.backendchat.service;

import org.springframework.stereotype.Component;

import java.text.Normalizer;
import java.util.List;
import java.util.Map;

@Component
public class MenuMatcher {

    // Chuẩn hóa text
    private String normalize(String s) {
        s = s.toLowerCase();
        s = Normalizer.normalize(s, Normalizer.Form.NFD);
        return s.replaceAll("\\p{M}", "");
    }

    // Fuzzy match
    private int similarity(String a, String b) {
        int max = Math.max(a.length(), b.length());
        int dist = levenshtein(a, b);
        return (int)((1.0 - (double)dist / max) * 100);
    }

    // Levenshtein distance
    private int levenshtein(String a, String b) {
        int[][] dp = new int[a.length()+1][b.length()+1];
        for (int i = 0; i <= a.length(); i++) dp[i][0] = i;
        for (int j = 0; j <= b.length(); j++) dp[0][j] = j;

        for (int i = 1; i <= a.length(); i++) {
            for (int j = 1; j <= b.length(); j++) {
                int cost = (a.charAt(i-1) == b.charAt(j-1)) ? 0 : 1;
                dp[i][j] = Math.min(
                        Math.min(dp[i-1][j] + 1, dp[i][j-1] + 1),
                        dp[i-1][j-1] + cost);
            }
        }
        return dp[a.length()][b.length()];
    }

    // tìm món gần nhất
    public Map<String,Object> matchDish(String text, List<Map<String,Object>> dishes) {
        text = normalize(text);

        Map<String,Object> best = null;
        int bestScore = 0;

        for (Map<String,Object> d : dishes) {
            String name = normalize(d.get("name").toString());
            int score = similarity(text, name);

            if (score > bestScore) {
                bestScore = score;
                best = d;
            }
        }

        // Nếu match trên 70% → coi món hợp lệ
        if (bestScore >= 70) return best;

        return null;
    }
}
