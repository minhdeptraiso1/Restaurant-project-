package com.hoabanrestaurant.backendchat.memory.ChatMemoryStoreIpml;

import com.hoabanrestaurant.backendchat.memory.ChatMemoryStore;


import java.util.*;


public class InMemoryMemoryStore implements ChatMemoryStore {

    private final Map<String, List<String>> memory = new HashMap<>();
    private final Map<String, String> pendingBooking = new HashMap<>();
    private final Map<String, String> confirmKeyword = new HashMap<>();

    @Override
    public void addUserMessage(String sessionId, String msg) {
        memory.computeIfAbsent(sessionId, k -> new ArrayList<>())
                .add("USER: " + msg);
    }

    @Override
    public void addAiMessage(String sessionId, String msg) {
        memory.computeIfAbsent(sessionId, k -> new ArrayList<>())
                .add("AI: " + msg);
    }

    @Override
    public List<String> getHistory(String sessionId) {
        return memory.getOrDefault(sessionId, List.of());
    }

    @Override
    public void clear(String sessionId) {
        memory.remove(sessionId);
    }

    @Override
    public void savePendingBooking(String sessionId, String json) {
        pendingBooking.put(sessionId, json);
    }

    @Override
    public String getPendingBooking(String sessionId) {
        return pendingBooking.get(sessionId);
    }

    @Override
    public void saveConfirmKeyword(String sessionId, String keyword) {
        confirmKeyword.put(sessionId, keyword.toLowerCase());
    }

    @Override
    public String getConfirmKeyword(String sessionId) {
        return confirmKeyword.get(sessionId);
    }

    @Override
    public void clearConfirmKeyword(String sessionId) {
        confirmKeyword.remove(sessionId);
    }
}