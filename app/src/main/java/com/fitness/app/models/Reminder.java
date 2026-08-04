package com.fitness.app.models;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

public class Reminder implements Serializable {
    private String id;
    private String type; // "WORKOUT", "MEAL", "WATER", "SLEEP"
    private int hour;
    private int minute;
    private boolean enabled;
    private String message;

    public Reminder() {}

    public Reminder(String id, String type, int hour, int minute, boolean enabled, String message) {
        this.id = id;
        this.type = type;
        this.hour = hour;
        this.minute = minute;
        this.enabled = enabled;
        this.message = message;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public int getHour() { return hour; }
    public void setHour(int hour) { this.hour = hour; }

    public int getMinute() { return minute; }
    public void setMinute(int minute) { this.minute = minute; }

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public JSONObject toJsonObject() throws JSONException {
        JSONObject obj = new JSONObject();
        obj.put("id", id);
        obj.put("type", type);
        obj.put("hour", hour);
        obj.put("minute", minute);
        obj.put("enabled", enabled);
        obj.put("message", message);
        return obj;
    }

    public static Reminder fromJsonObject(JSONObject obj) throws JSONException {
        return new Reminder(
            obj.optString("id", ""),
            obj.optString("type", ""),
            obj.optInt("hour", 0),
            obj.optInt("minute", 0),
            obj.optBoolean("enabled", false),
            obj.optString("message", "")
        );
    }
}
