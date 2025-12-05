package com.hoabanrestaurant.backendchat.memory;

import java.util.List;

public interface ChatMemoryStore {

    void addUserMessage(String sessionId, String msg);

    void addAiMessage(String sessionId, String msg);

    List<String> getHistory(String sessionId);

    void clear(String sessionId);

    void savePendingBooking(String sessionId, String json);

    String getPendingBooking(String sessionId);

    void saveConfirmKeyword(String sessionId, String keyword);

    String getConfirmKeyword(String sessionId);

    void clearConfirmKeyword(String sessionId);
}
