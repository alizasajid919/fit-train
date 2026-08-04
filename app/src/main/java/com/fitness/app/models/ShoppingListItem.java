package com.fitness.app.models;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.io.Serializable;

@Entity(tableName = "shopping_list_items")
public class ShoppingListItem implements Serializable {

    @PrimaryKey
    @NonNull
    private String id;
    private String name;
    private String category;
    private String quantity;
    private boolean isPurchased;
    private String explanation;
    private long timestamp;

    public ShoppingListItem() {
    }

    public ShoppingListItem(@NonNull String id, String name, String category, String quantity, boolean isPurchased, String explanation, long timestamp) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.quantity = quantity;
        this.isPurchased = isPurchased;
        this.explanation = explanation;
        this.timestamp = timestamp;
    }

    @NonNull
    public String getId() { return id; }
    public void setId(@NonNull String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getQuantity() { return quantity; }
    public void setQuantity(String quantity) { this.quantity = quantity; }

    public boolean isPurchased() { return isPurchased; }
    public void setPurchased(boolean purchased) { isPurchased = purchased; }

    public String getExplanation() { return explanation; }
    public void setExplanation(String explanation) { this.explanation = explanation; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
}
