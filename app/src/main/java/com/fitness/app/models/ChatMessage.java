package com.fitness.app.models;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

@Entity(tableName = "chat_messages")
public class ChatMessage implements Serializable {
    
    @PrimaryKey
    @NonNull
    private String id;
    
    private String sender; // "USER" or "AI"
    private String text;
    private long timestamp;
    
    private String attachmentUri;  // Local URI of the file attachment
    private String attachmentType; // "IMAGE", "PDF", "TEXT", or null

    private String status;         // "SENT", "DELIVERED", "READ"
    private boolean isDelivered;
    private boolean isRead;

    public ChatMessage() {
        this.id = "";
        this.status = "SENT";
    }

    @Ignore
    public ChatMessage(@NonNull String id, String sender, String text, long timestamp) {
        this.id = id;
        this.sender = sender;
        this.text = text;
        this.timestamp = timestamp;
        this.attachmentUri = null;
        this.attachmentType = null;
        this.status = "SENT";
        this.isDelivered = false;
        this.isRead = false;
    }

    @Ignore
    public ChatMessage(@NonNull String id, String sender, String text, long timestamp, String attachmentUri, String attachmentType) {
        this.id = id;
        this.sender = sender;
        this.text = text;
        this.timestamp = timestamp;
        this.attachmentUri = attachmentUri;
        this.attachmentType = attachmentType;
        this.status = "SENT";
        this.isDelivered = false;
        this.isRead = false;
    }

    @NonNull
    public String getId() { return id; }
    public void setId(@NonNull String id) { this.id = id; }

    public String getSender() { return sender; }
    public void setSender(String sender) { this.sender = sender; }

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public String getAttachmentUri() { return attachmentUri; }
    public void setAttachmentUri(String attachmentUri) { this.attachmentUri = attachmentUri; }

    public String getAttachmentType() { return attachmentType; }
    public void setAttachmentType(String attachmentType) { this.attachmentType = attachmentType; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public boolean isDelivered() { return isDelivered; }
    public void setDelivered(boolean delivered) { isDelivered = delivered; }

    public boolean isRead() { return isRead; }
    public void setRead(boolean read) { isRead = read; }

    public JSONObject toJsonObject() throws JSONException {
        JSONObject obj = new JSONObject();
        obj.put("id", id);
        obj.put("sender", sender);
        obj.put("text", text);
        obj.put("timestamp", timestamp);
        if (attachmentUri != null) obj.put("attachmentUri", attachmentUri);
        if (attachmentType != null) obj.put("attachmentType", attachmentType);
        obj.put("status", status);
        obj.put("isDelivered", isDelivered);
        obj.put("isRead", isRead);
        return obj;
    }

    public static ChatMessage fromJsonObject(JSONObject obj) throws JSONException {
        ChatMessage msg = new ChatMessage(
            obj.optString("id", ""),
            obj.optString("sender", ""),
            obj.optString("text", ""),
            obj.optLong("timestamp", 0),
            obj.isNull("attachmentUri") ? null : obj.optString("attachmentUri", null),
            obj.isNull("attachmentType") ? null : obj.optString("attachmentType", null)
        );
        msg.setStatus(obj.optString("status", "SENT"));
        msg.setDelivered(obj.optBoolean("isDelivered", false));
        msg.setRead(obj.optBoolean("isRead", false));
        return msg;
    }
}
