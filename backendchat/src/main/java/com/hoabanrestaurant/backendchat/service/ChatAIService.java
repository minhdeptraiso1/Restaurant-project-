package com.hoabanrestaurant.backendchat.service;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.hoabanrestaurant.backendchat.dto.ChatReq;
import com.hoabanrestaurant.backendchat.dto.ChatRes;
import com.hoabanrestaurant.backendchat.memory.ChatMemoryStore;
import com.hoabanrestaurant.backendchat.nlu.BookingCommandExtractor;
import com.hoabanrestaurant.backendchat.nlu.IntentDetector;
import com.hoabanrestaurant.backendchat.preprocess.MessagePreprocessor;
import com.hoabanrestaurant.backendchat.preprocess.SemanticEncoder;

import lombok.RequiredArgsConstructor;

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

    private final BookingCommandExtractor bookingExtractor;
    private final BookingClient bookingClient;

    private final ObjectMapper mapper = new ObjectMapper();



    /**
     * ============================================================
     * LUỒNG XỬ LÝ CHÍNH CHO MỖI TIN NHẮN
     * ============================================================
     */
    public ChatRes process(ChatReq req) {

        // Tiền xử lý message (lowercase, bỏ dấu, chuẩn hoá...)
        String clean = preprocessor.process(req.message());

        // Bước 0: Kiểm tra user đang trong trạng thái "Xác nhận đặt bàn"
        String confirmKey = memory.getConfirmKeyword(req.sessionId());
        if (confirmKey != null && clean.equalsIgnoreCase(confirmKey)) {
            return handleConfirm(req);
        }

        // Bước 1: Nhận diện intent người dùng
        String intent = intentDetector.detect(clean);


        if (intent.equals("BOOK_TABLE")) {
            int partySize = extractPartySize(clean);
            var combos = dataFetcher.fetchCombos();
            var combo = comboRecommender.recommendCombo(partySize, combos);

            if (combo != null)
                return new ChatRes(
                        "Combo phù hợp cho " + partySize + " người là: " + combo.get("name"),
                        "RECOMMEND_COMBO"
                );
        }

        // Lưu lịch sử hội thoại
        memory.addUserMessage(req.sessionId(), req.message());
        List<String> fullHistory = memory.getHistory(req.sessionId());
        int limit = 6;

        List<String> history = fullHistory.size() > limit
                ? fullHistory.subList(fullHistory.size() - limit, fullHistory.size())
                : fullHistory;

        // Lấy dữ liệu thực tế để đưa vào prompt
        var dishes = dataFetcher.fetchDishes();
        var combos = dataFetcher.fetchCombos();
        var tables = dataFetcher.fetchTables();
        var slots  = dataFetcher.fetchSlots();

        String cd = dataCompressor.compressList(dishes);
        String cc = dataCompressor.compressList(combos);
        String ct = dataCompressor.compressList(tables);
        String cs = dataCompressor.compressList(slots);

        // Mã hóa message + ý định để AI dễ hiểu
        String encoded = encoder.encode(clean, intent);
        String nowStr = ZonedDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh"))
                .format(DateTimeFormatter.ofPattern("EEEE, dd/MM/yyyy HH:mm:ss", new Locale("vi", "VN")));

        // Tạo prompt gửi vào LLM
        String prompt = promptBuilder.buildPrompt(encoded, history, cd, cc, ct, cs, nowStr);

        // Gọi mô hình AI
        String aiResult = chatClient
                .prompt()
                .system("Bạn là hệ thống AI của nhà hàng Hoa Ban.")
                .user(prompt)
                .call()
                .content();

        // Làm sạch chuỗi JSON AI trả về
        String cleaned = cleanJson(aiResult);

        // Validate hoặc fallback JSON
        JsonNode json = jsonValidator.validateOrFallback(cleaned);

        String reply = json.get("reply").asText();
        String finalIntent = json.get("intent").asText();


        if (finalIntent.equals("BOOK_TABLE_PREVIEW")) {

            try {
                JsonNode bookingJson = mapper.readTree(cleaned);

                // Lưu nguyên văn câu người dùng gửi vào JSON preview
                if (bookingJson instanceof ObjectNode) {
                    ((ObjectNode) bookingJson).put("originalText", req.message());
                }

                memory.savePendingBooking(req.sessionId(), mapper.writeValueAsString(bookingJson));

            } catch (Exception e) {
                memory.savePendingBooking(req.sessionId(), cleaned);
            }

            String confirmText = json.has("confirmText")
                    ? json.get("confirmText").asText()
                    : "xac nhan dat ban";

            memory.saveConfirmKeyword(req.sessionId(), confirmText.toLowerCase());

            String preview = reply + "\n👉 Để xác nhận, vui lòng gõ: \"" + confirmText + "\"";
            memory.addAiMessage(req.sessionId(), preview);

            return new ChatRes(preview, "BOOK_TABLE_PREVIEW");
        }

        // Trường hợp bình thường: trả lời AI
        memory.addAiMessage(req.sessionId(), reply);
        return new ChatRes(reply, finalIntent);
    }

    /**
     * ============================================================
     * CASE B — handleConfirm()
     * Xác nhận đặt bàn sau khi AI preview thành công
     * ============================================================
     */
    private ChatRes handleConfirm(ChatReq req) {
        String raw = memory.getPendingBooking(req.sessionId());
        if (raw == null)
            return new ChatRes("❌ Không tìm thấy yêu cầu đặt bàn.", "ERROR");

        try {
            JsonNode json = mapper.readTree(raw);

            // Trích xuất lệnh đặt bàn từ JSON
            var cmd = bookingExtractor.extract(json);

            // Tạo đơn đặt bàn thật sự
            Map<String,Object> created = bookingClient.createReservation(
                    req.sessionId(),
                    req.userId(),
                    cmd
            );

            String done =
                    "🎉 Đặt bàn thành công!\n"
                            + "🕒 " + cmd.startTime() + "\n"
                            + "👥 Số người: " + cmd.partySize() + "\n"
                            + "📝 " + (cmd.note() == null ? "Không có" : cmd.note()) + "\n"
                            + "🔑 Mã đơn: " + created.get("id");

            // Reset lại memory
            memory.clearConfirmKeyword(req.sessionId());
            memory.clear(req.sessionId());
            memory.addAiMessage(req.sessionId(), done);

            return new ChatRes(done, "BOOK_TABLE");

        } catch (Exception e) {
            return new ChatRes("⚠ Lỗi xử lý dữ liệu đặt bàn.", "ERROR");
        }
    }

    /**
     * Loại bỏ dấu thừa của JSON do AI trả về
     */
    private String cleanJson(String raw) {
        raw = raw.replace("```json", "")
                .replace("```", "")
                .replace("`", "")
                .trim();

        int s = raw.indexOf("{");
        int e = raw.lastIndexOf("}");

        return (s != -1 && e != -1 && e > s) ? raw.substring(s, e + 1) : raw;
    }

    /**
     * Lấy số lượng người từ tin nhắn người dùng
     */
    private int extractPartySize(String clean) {
        for (String w : clean.split(" ")) {
            try {
                int n = Integer.parseInt(w);
                if (n > 0 && n <= 30) return n;
            } catch (Exception ignore) {}
        }
        return 2;
    }
}
