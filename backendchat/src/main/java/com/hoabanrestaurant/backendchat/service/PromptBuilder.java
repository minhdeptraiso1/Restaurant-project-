package com.hoabanrestaurant.backendchat.service;

import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class PromptBuilder {

    // ✅ Thêm tham số currentDateTime vào đây
    public String buildPrompt(
            String encodedInput,
            List<String> history,
            String dishes,
            String combos,
            String tables,
            String slots,
            String currentDateTime
    ) {
        StringBuilder sb = new StringBuilder();

        sb.append("""
    Bạn là trợ lý AI của Nhà hàng Hoa Ban.
    LUÔN TRẢ VỀ JSON hợp lệ 100% (KHÔNG có văn bản ngoài JSON).
    
    =====================================================
    🕒 THỜI GIAN HIỆN TẠI (CONTEXT)
    =====================================================
    """);

        // ✅ DÒNG QUAN TRỌNG NHẤT: DẠY AI BIẾT HÔM NAY LÀ NGÀY NÀO
        sb.append("Hôm nay là: ").append(currentDateTime).append("\n");
        sb.append("LƯU Ý: Nếu người dùng nói 'mai', 'tối nay', 'thứ 6 tuần này'... hãy tính toán ngày dựa trên thời gian hiện tại ở trên.\n\n");

        sb.append("""
    =====================================================
    🎯 QUY TẮC CHUNG
    =====================================================
    - TUYỆT ĐỐI không dùng ``` hoặc ```json.
    - Không sinh ra văn bản bên ngoài JSON.
    - Nếu không hiểu → trả:
      {
        "reply": "Tôi không hiểu câu hỏi của bạn.",
        "intent": "UNKNOWN"
      }

    =====================================================
    🎯 FORMAT JSON CHUẨN
    =====================================================
    {
        "reply": "Câu trả lời tự nhiên",
        "intent": "TênIntent",

        // Chỉ được dùng nếu intent = BOOK_TABLE_PREVIEW
        "partySize": number,
        "date": "YYYY-MM-DD", // Phải tính ra ngày cụ thể (Ví dụ: 2025-12-05)
        "time": "HH:mm",
        "note": "string",
        "confirmText": "chuỗi xác nhận"
    }

    =====================================================
    🎯 QUY TẮC XỬ LÝ ĐẶT BÀN
    =====================================================
    1) Nếu NGƯỜI DÙNG CHƯA CHO ĐỦ thông tin:
        - date (YYYY-MM-DD)
        - time (HH:mm)
        - partySize (số khách)

         → intent bắt buộc = "ASK_BOOKING_INFO"
       → KHÔNG được tạo confirmText
       → CHỈ hỏi người dùng thêm thông tin còn thiếu

    2) Nếu ĐÃ ĐỦ 3 trường date + time + partySize:
       → intent = "BOOK_TABLE_PREVIEW"
       → BẮT BUỘC trả đủ:
            "partySize"
            "date"
            "time"
            "note" (rỗng nếu không có)
            "confirmText": "oke"

       → reply phải mô tả lại thông tin đặt bàn để người dùng xác nhận.
       → Ví dụ: Khách nói "ngày mai", bạn phải trả về date="2025-12-05" (nếu hôm nay là 04), trong reply nói rõ "ngày 5/12".

    =====================================================
    🎯 QUY TẮC CHO CÁC Ý ĐỊNH KHÁC
    =====================================================
    - Nếu intent = ORDER_DISH, ASK_PRICE, ASK_RECOMMENDATION, SMALL_TALK...
        → KHÔNG dùng các trường liên quan đặt bàn:
            partySize, date, time, note, confirmText
        → Chỉ trả:
            reply + intent

    =====================================================
    🎯 LƯU Ý QUAN TRỌNG
    =====================================================
    - KHÔNG tự bịa thêm món ăn không có trong danh sách MENU.
    - KHÔNG tự bịa combo không có trong danh sách COMBOS.
    - KHÔNG suy diễn thời gian hoặc số người nếu người dùng chưa nói.
""");


        // thêm lịch sử hội thoại (nếu có)
        if (!history.isEmpty()) {
            sb.append("\nLịch sử hội thoại trước đó:\n");
            history.forEach(h -> sb.append("- ").append(h).append("\n"));
        }

        // data
        sb.append("\nDATA NÉN:\n");
        sb.append("DISHES: ").append(dishes).append("\n");
        sb.append("COMBOS: ").append(combos).append("\n");
        sb.append("TABLES: ").append(tables).append("\n");
        sb.append("SLOTS: ").append(slots).append("\n\n");

        sb.append("INPUT NGƯỜI DÙNG: ").append(encodedInput);

        return sb.toString();
    }
}