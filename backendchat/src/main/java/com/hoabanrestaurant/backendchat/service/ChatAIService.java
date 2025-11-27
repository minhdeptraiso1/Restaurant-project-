package com.hoabanrestaurant.backendchat.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hoabanrestaurant.backendchat.dto.ChatReq;
import com.hoabanrestaurant.backendchat.dto.ChatRes;
import com.hoabanrestaurant.backendchat.memory.ChatMemoryStore;
import com.hoabanrestaurant.backendchat.nlu.IntentDetector;
import com.hoabanrestaurant.backendchat.preprocess.MessagePreprocessor;
import com.hoabanrestaurant.backendchat.preprocess.SemanticEncoder;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ChatAIService {

    private final MessagePreprocessor preprocessor;
    private final SemanticEncoder encoder;
    private final IntentDetector intentDetector;
    private final ChatMemoryStore memory;
    private final DataFetcher dataFetcher;
    private final DataCompressor dataCompressor;
    private final PromptBuilder promptBuilder;
    private final JsonValidator jsonValidator;
    private final ChatClient chatClient;
    private final MenuMatcher menuMatcher;
    private final ComboRecommender comboRecommender;

    private final ObjectMapper mapper = new ObjectMapper();

    public ChatRes process(ChatReq req) {

        // 1. Preprocess input
        String clean = preprocessor.process(req.message());
        String intent = intentDetector.detect(clean);

        // RULE 1: ORDER_DISH → match món trước (không cần gọi AI)
        if (intent.equals("ORDER_DISH")) {
            List<Map<String,Object>> allDishes = dataFetcher.fetchDishes();
            Map<String,Object> dish = menuMatcher.matchDish(req.message(), allDishes);

            if (dish != null) {
                return new ChatRes(
                        "Bạn muốn gọi món: " + dish.get("name") +
                                " (giá " + dish.get("price") + "đ). Xác nhận nhé?",
                        "ORDER_DISH"
                );
            }
        }

        // RULE 2: BOOK_TABLE → gợi combo theo số người
        if (intent.equals("BOOK_TABLE")) {
            int partySize = extractPartySize(clean);
            List<Map<String,Object>> combos = dataFetcher.fetchCombos();
            Map<String,Object> combo = comboRecommender.recommendCombo(partySize, combos);

            if (combo != null) {
                return new ChatRes(
                        "Combo phù hợp cho " + partySize + " người là: " + combo.get("name"),
                        "RECOMMEND_COMBO"
                );
            }
        }

        String encoded = encoder.encode(clean, intent);

        // 2. Save memory
        memory.addUserMessage(req.sessionId(), req.message());
        List<String> history = memory.getHistory(req.sessionId());

        // 3. Fetch REAL DATA
        List<Map<String,Object>> dishes = dataFetcher.fetchDishes();
        List<Map<String,Object>> combos = dataFetcher.fetchCombos();
        List<Map<String,Object>> tables = dataFetcher.fetchTables();
        List<Map<String,Object>> slots = dataFetcher.fetchSlots();

        // 4. COMPRESS DATA
        String cd = dataCompressor.compressList(dishes);
        String cc = dataCompressor.compressList(combos);
        String ct = dataCompressor.compressList(tables);
        String cs = dataCompressor.compressList(slots);

        // 5. Build prompt gọn
        String prompt = promptBuilder.buildPrompt(encoded, history, cd, cc, ct, cs);

        // 6. Call Gemini
        String aiResult = chatClient
                .prompt()
                .system("Bạn là hệ thống AI của nhà hàng Hoa Ban.")
                .user(prompt)
                .call()
                .content();

        // 7. Clean malformed JSON
        String cleaned = cleanJson(aiResult);

        // 8. Validate JSON
        JsonNode json = jsonValidator.validateOrFallback(cleaned);

        String reply = json.get("reply").asText();
        String finalIntent = json.get("intent").asText();

        // 9. Save AI reply into memory
        memory.addAiMessage(req.sessionId(), reply);

        return new ChatRes(reply, finalIntent);
    }


    private String cleanJson(String raw) {
        raw = raw.replace("```json","")
                .replace("```","")
                .replace("`","")
                .trim();

        int s = raw.indexOf("{");
        int e = raw.lastIndexOf("}");

        if (s != -1 && e != -1 && e > s)
            return raw.substring(s, e + 1);

        return raw;
    }

    private int extractPartySize(String clean) {
        for (String w : clean.split(" ")) {
            try {
                int n = Integer.parseInt(w);
                if (n >= 1 && n <= 30) return n;
            } catch (Exception ignore){}
        }
        return 2; // default
    }
}
