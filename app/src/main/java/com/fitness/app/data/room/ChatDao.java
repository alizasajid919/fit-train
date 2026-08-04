package com.fitness.app.data.room;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.fitness.app.models.ChatMessage;

import java.util.List;

@Dao
public interface ChatDao {

    @Query("SELECT * FROM chat_messages ORDER BY timestamp ASC")
    List<ChatMessage> getAllMessages();

    @Query("SELECT * FROM chat_messages ORDER BY timestamp ASC")
    LiveData<List<ChatMessage>> getAllMessagesLive();

    @Query("SELECT * FROM chat_messages ORDER BY timestamp DESC LIMIT :limit OFFSET :offset")
    List<ChatMessage> getMessagesPaged(int limit, int offset);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertMessage(ChatMessage message);

    @androidx.room.Delete
    void deleteMessage(ChatMessage message);

    @Query("DELETE FROM chat_messages")
    void clearAllMessages();
}
