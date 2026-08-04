package com.fitness.app.repositories;

import android.content.Context;

import androidx.lifecycle.LiveData;

import com.fitness.app.data.room.AppDatabase;
import com.fitness.app.data.room.ChatDao;
import com.fitness.app.models.ChatMessage;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ChatRepository {

    private final ChatDao dao;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public ChatRepository(Context context) {
        AppDatabase db = AppDatabase.getInstance(context);
        dao = db.chatDao();
    }

    public LiveData<List<ChatMessage>> getAllMessagesLive() {
        return dao.getAllMessagesLive();
    }

    public void saveMessage(ChatMessage message) {
        executor.execute(() -> dao.insertMessage(message));
    }

    public void deleteMessage(ChatMessage message) {
        executor.execute(() -> dao.deleteMessage(message));
    }

    public void clearAllMessages() {
        executor.execute(() -> dao.clearAllMessages());
    }

    public List<ChatMessage> getMessagesPaged(int limit, int offset) {
        return dao.getMessagesPaged(limit, offset);
    }
}
