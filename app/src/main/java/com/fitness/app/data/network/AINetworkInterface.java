package com.fitness.app.data.network;

public interface AINetworkInterface {
    void queryAI(String query, String userProfileContext, AICallback callback);
}
