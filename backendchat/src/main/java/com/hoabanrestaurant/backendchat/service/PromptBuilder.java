package com.hoabanrestaurant.backendchat.service;

import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PromptBuilder {

    public String buildPrompt(
            String encodedInput,
            List<String> history,
            String dishes,
            String combos,
            String tables,
            String slots
    ) {

        StringBuilder sb = new StringBuilder();

        sb.append("""
            Bạn là trợ lý AI của Nhà hàng Hoa Ban.
            Bạn phải trả JSON hợp lệ 100%.

            Format JSON:
            {
              "reply": "Nội dung trả lời tự nhiên",
              "intent": "IntentName"
            }

            Quy tắc:
            - Không trả text ngoài JSON
            - Không dùng ```json hoặc ``` 
            - Không giải thích dài dòng
            - Nếu không hiểu thì reply UNKNOWN

            """);

        if (!history.isEmpty()) {
            sb.append("Lịch sử hội thoại:\n");
            history.forEach(h -> sb.append("- ").append(h).append("\n"));
            sb.append("\n");
        }

        sb.append("DỮ LIỆU ĐÃ NÉN (AI-FRIENDLY):\n");
        sb.append("MENU: ").append(dishes).append("\n");
        sb.append("COMBOS: ").append(combos).append("\n");
        sb.append("TABLES: ").append(tables).append("\n");
        sb.append("SLOTS: ").append(slots).append("\n\n");

        sb.append("INPUT: ").append(encodedInput);

        return sb.toString();
    }
}
