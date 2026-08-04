package com.fitness.app.utils;

import android.os.Handler;
import android.os.Looper;
import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.fitness.app.data.network.AICallback;
import com.fitness.app.models.ChatMessage;
import com.fitness.app.models.User;
import com.fitness.app.repositories.AIRepository;
import com.fitness.app.repositories.ChatRepository;

import java.util.List;
import java.util.UUID;

public class ChatManager {

    private final ChatRepository chatRepo;
    private final AIRepository aiRepo;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private final MutableLiveData<Boolean> isLoadingLive = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorLive = new MutableLiveData<>(null);

    public ChatManager(Context context) {
        this.chatRepo = new ChatRepository(context);
        this.aiRepo = new AIRepository(context);
    }

    public LiveData<List<ChatMessage>> getMessagesLive() {
        return chatRepo.getAllMessagesLive();
    }

    public LiveData<Boolean> getIsLoadingLive() {
        return isLoadingLive;
    }

    public LiveData<String> getErrorLive() {
        return errorLive;
    }

    public void sendMessage(String text, User user) {
        sendMessage(text, null, null, user);
    }

    public void sendMessage(String text, String attachmentUri, String attachmentType, User user) {
        if ((text == null || text.trim().isEmpty()) && attachmentUri == null) return;

        // 1. Create and save User message with "SENT" status (Single Tick)
        ChatMessage userMsg = new ChatMessage(
                UUID.randomUUID().toString(),
                "USER",
                text != null ? text.trim() : "",
                System.currentTimeMillis(),
                attachmentUri,
                attachmentType
        );
        userMsg.setStatus("SENT");
        userMsg.setDelivered(false);
        userMsg.setRead(false);
        chatRepo.saveMessage(userMsg);

        // 2. Set loading state
        isLoadingLive.postValue(true);
        errorLive.postValue(null);

        // 3. Simulated network delay (400ms) to transition to "DELIVERED" status (Double Tick)
        mainHandler.postDelayed(() -> {
            userMsg.setStatus("DELIVERED");
            userMsg.setDelivered(true);
            chatRepo.saveMessage(userMsg);
        }, 400);

        // 4. Query AI
        String query = text != null ? text.trim() : "";
        if (attachmentType != null) {
            query = String.format("[Sent %s attachment] %s", attachmentType, query);
        }

        aiRepo.queryAIWithUser(query, user, new AICallback() {
            @Override
            public void onSuccess(String responseText) {
                // Update User message to "READ" status (Blue Double Tick)
                userMsg.setStatus("READ");
                userMsg.setRead(true);
                chatRepo.saveMessage(userMsg);

                isLoadingLive.postValue(false);
                
                // Save AI response
                ChatMessage aiMsg = new ChatMessage(
                        UUID.randomUUID().toString(),
                        "AI",
                        responseText,
                        System.currentTimeMillis()
                );
                chatRepo.saveMessage(aiMsg);
            }

            @Override
            public void onFailure(Throwable t) {
                isLoadingLive.postValue(false);
                errorLive.postValue("Network error: " + t.getMessage());
                // Update User message status to "FAILED" to allow user to retry
                userMsg.setStatus("FAILED");
                chatRepo.saveMessage(userMsg);
            }
        });
    }

    public void retryMessage(ChatMessage userMsg, User user) {
        if (userMsg == null) return;

        userMsg.setStatus("SENT");
        userMsg.setDelivered(false);
        userMsg.setRead(false);
        chatRepo.saveMessage(userMsg);

        isLoadingLive.postValue(true);
        errorLive.postValue(null);

        mainHandler.postDelayed(() -> {
            userMsg.setStatus("DELIVERED");
            userMsg.setDelivered(true);
            chatRepo.saveMessage(userMsg);
        }, 400);

        String query = userMsg.getText();
        if (userMsg.getAttachmentType() != null) {
            query = String.format("[Sent %s attachment] %s", userMsg.getAttachmentType(), query);
        }

        aiRepo.queryAIWithUser(query, user, new AICallback() {
            @Override
            public void onSuccess(String responseText) {
                userMsg.setStatus("READ");
                userMsg.setRead(true);
                chatRepo.saveMessage(userMsg);

                isLoadingLive.postValue(false);

                ChatMessage aiMsg = new ChatMessage(
                        UUID.randomUUID().toString(),
                        "AI",
                        responseText,
                        System.currentTimeMillis()
                );
                chatRepo.saveMessage(aiMsg);
            }

            @Override
            public void onFailure(Throwable t) {
                isLoadingLive.postValue(false);
                errorLive.postValue("Network error: " + t.getMessage());
                userMsg.setStatus("FAILED");
                chatRepo.saveMessage(userMsg);
            }
        });
    }

    public void regenerateResponse(User user) {
        new Thread(() -> {
            List<ChatMessage> history = chatRepo.getAllMessagesLive().getValue();
            if (history == null || history.isEmpty()) return;

            // Find last user message and last AI message
            ChatMessage lastUser = null;
            ChatMessage lastAI = null;
            for (int i = history.size() - 1; i >= 0; i--) {
                ChatMessage m = history.get(i);
                if (lastAI == null && "AI".equals(m.getSender())) {
                    lastAI = m;
                }
                if (lastUser == null && "USER".equals(m.getSender())) {
                    lastUser = m;
                }
            }

            if (lastUser == null) return;

            // Delete last AI message from Database
            if (lastAI != null) {
                chatRepo.deleteMessage(lastAI);
            }

            final ChatMessage finalUser = lastUser;
            mainHandler.post(() -> {
                isLoadingLive.postValue(true);
                errorLive.postValue(null);

                String query = finalUser.getText();
                if (finalUser.getAttachmentType() != null) {
                    query = String.format("[Sent %s attachment] %s", finalUser.getAttachmentType(), query);
                }

                aiRepo.queryAIWithUser(query, user, new AICallback() {
                    @Override
                    public void onSuccess(String responseText) {
                        isLoadingLive.postValue(false);

                        ChatMessage aiMsg = new ChatMessage(
                                UUID.randomUUID().toString(),
                                "AI",
                                responseText,
                                System.currentTimeMillis()
                        );
                        chatRepo.saveMessage(aiMsg);
                    }

                    @Override
                    public void onFailure(Throwable t) {
                        isLoadingLive.postValue(false);
                        errorLive.postValue("Network error: " + t.getMessage());
                        finalUser.setStatus("FAILED");
                        chatRepo.saveMessage(finalUser);
                    }
                });
            });
        }).start();
    }

    public void deleteMessage(ChatMessage msg) {
        chatRepo.deleteMessage(msg);
    }

    public void clearConversation() {
        chatRepo.clearAllMessages();
    }

    public List<ChatMessage> getMessagesPaged(int limit, int offset) {
        return chatRepo.getMessagesPaged(limit, offset);
    }
}
