package com.fitness.app.models;

public class Goal {
    private String title;
    private String description;
    private int illustrationResId;

    public Goal(String title, String description, int illustrationResId) {
        this.title = title;
        this.description = description;
        this.illustrationResId = illustrationResId;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public int getIllustrationResId() {
        return illustrationResId;
    }
}
