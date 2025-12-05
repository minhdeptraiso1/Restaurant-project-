package com.hoabanrestaurant.backendchat.nlu;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.hoabanrestaurant.backendchat.dto.BookingCommand;

@Component
public class BookingCommandExtractor {

    private static final ZoneId VN_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");

    /**
     * =====================================================================
     *  extract(): Chuyển đổi JSON từ AI → BookingCommand hoàn chỉnh
     * =====================================================================
     */
    public BookingCommand extract(JsonNode json) {

        /* ------------------------- RAW TEXT ------------------------- */
        String raw = json.has("originalText")
                ? json.get("originalText").asText().toLowerCase(Locale.ROOT)
                : "";

        /* ------------------------- PARTY SIZE ------------------------ */
        int party = json.has("partySize") ? json.get("partySize").asInt() : 5;

        /* --------------------------- NOTE ---------------------------- */
        String note = json.has("note") ? json.get("note").asText() : "";

        /* ------------------------- DATE PARSING ---------------------- */
        LocalDate today = LocalDate.now(VN_ZONE);
        LocalTime nowTime = LocalTime.now(VN_ZONE);
        LocalDate date = today;

        if (json.has("date")) {
            try {
                LocalDate parsed = LocalDate.parse(json.get("date").asText());

                if (parsed.getYear() < today.getYear()) {
                    parsed = parsed.withYear(today.getYear());
                }

                date = parsed;

            } catch (Exception ignored) {}
        } else {
            if (raw.contains("toi nay") || raw.contains("dem nay")) {
                date = nowTime.isBefore(LocalTime.of(5, 0))
                        ? today.minusDays(1)
                        : today;
            } else if (raw.contains("hom nay")) {
                date = today;
            } else if (raw.contains("mai") && !raw.contains("may")) {
                date = today.plusDays(1);
            } else if (raw.contains("mot")) {
                date = today.plusDays(2);
            }
        }

        /* ------------------------- TIME PARSING ---------------------- */
        LocalTime startTime = LocalTime.of(18, 0);

        if (json.has("time")) {

            try {
                String t = json.get("time").asText().trim().toLowerCase();
                int hour = -1;
                int minute = 0;

                if (t.matches("\\d{1,2}:\\d{2}")) {
                    startTime = LocalTime.parse(t);
                } else if (t.matches("\\d{1,2}:\\d{2}:\\d{2}")) {
                    startTime = LocalTime.parse(t.substring(0, 5));
                } else if (t.matches("\\d{1,2}h")) {
                    hour = Integer.parseInt(t.replace("h", ""));
                } else if (t.matches("\\d{1,2}h\\d{1,2}")) {
                    String[] p = t.split("h");
                    hour = Integer.parseInt(p[0]);
                    minute = Integer.parseInt(p[1]);
                } else if (t.matches("\\d{1,2}")) {
                    hour = Integer.parseInt(t);
                }

                if (hour != -1) {

                    if (raw.contains("toi") || raw.contains("dem")) {
                        if (hour < 12) hour += 12;
                    } else if (raw.contains("sang")) {
                        if (hour > 12) hour -= 12;
                    } else if (raw.contains("trua")) {
                        if (hour < 12) hour = 12;
                    } else if (raw.contains("chieu")) {
                        if (hour < 12) hour += 12;
                    }

                    startTime = LocalTime.of(hour, minute);
                }

            } catch (Exception ignored) {}

        } else {
            // Nhận dạng thời gian từ câu gốc như "7 tối", "9h đêm"
            Pattern p = Pattern.compile(".*\\b(\\d{1,2})h?\\s*(giờ\\s*)?(tối|đêm)\\b.*");
            Matcher m = p.matcher(raw);

            if (m.matches()) {
                try {
                    int hour = Integer.parseInt(m.group(1));
                    if (hour < 12) hour += 12;
                    startTime = LocalTime.of(hour, 0);
                } catch (Exception ignored) {}
            }
        }

        /* ------------------------ RELATIVE TIME ---------------------- */
        boolean isRelative = false;
        ZonedDateTime relative = null;

        Matcher h = Pattern.compile("(\\d+)\\s*(tieng|gio|h)\\s*nua").matcher(raw);
        Matcher mi = Pattern.compile("(\\d+)\\s*(phut|p)\\s*nua").matcher(raw);

        if (h.find()) {
            relative = ZonedDateTime.now(VN_ZONE).plusHours(Integer.parseInt(h.group(1)));
            isRelative = true;
        } else if (mi.find()) {
            relative = ZonedDateTime.now(VN_ZONE).plusMinutes(Integer.parseInt(mi.group(1)));
            isRelative = true;
        }

        if (isRelative && relative != null) {
            int minute = relative.getMinute();
            int remainder = minute % 5;
            if (remainder != 0) {
                relative = relative.plusMinutes(5 - remainder);
            }

            relative = relative.withSecond(0).withNano(0);
            date = relative.toLocalDate();
            startTime = relative.toLocalTime();
        }

        /* ------------------------- FINAL INSTANT --------------------- */
        ZonedDateTime startZoned = ZonedDateTime.of(date, startTime, VN_ZONE);
        ZonedDateTime now = ZonedDateTime.now(VN_ZONE);

        if (startZoned.isBefore(now)) {
            startZoned = startZoned.plusDays(1);
        }

        Instant startInstant = startZoned.toInstant();
        Instant endInstant = startInstant.plus(5, ChronoUnit.HOURS);

        return new BookingCommand(startInstant, endInstant, party, note);
    }
}
