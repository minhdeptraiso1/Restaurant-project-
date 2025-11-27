package com.hoabanrestaurant.backendchat.service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

@Component
public class DataCompressor {

    public String compressList(List<?> list) {
        if (list == null || list.isEmpty()) return "[]";

        List<Map<String, Object>> output = new ArrayList<>();

        for (Object item : list) {
            if (item instanceof Map<?, ?> mapItem) {
                // Nếu là map -> xử lý chuẩn
                Map<String, Object> clean = new LinkedHashMap<>();
                clean.put("id", safe(mapItem.get("id")));
                clean.put("name", safe(mapItem.get("name")));
                clean.put("price", safe(mapItem.get("price")));
                output.add(clean);
            } 
            else if (item instanceof String str) {
                // Nếu backend trả về string -> convert thành map đơn giản
                Map<String, Object> clean = new LinkedHashMap<>();
                clean.put("value", str);
                output.add(clean);
            }
            else {
                // fallback cho object không rõ kiểu
                Map<String, Object> clean = new LinkedHashMap<>();
                clean.put("value", item.toString());
                output.add(clean);
            }
        }

        String json = output.toString();

        // cắt giảm token
        if (json.length() > 500) {
            return json.substring(0, 500) + "...";
        }

        return json;
    }

    private Object safe(Object v) {
        return v == null ? "" : v;
    }
}
