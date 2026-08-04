package com.fitness.app.data.network;

public interface AICallback {
    void onSuccess(String response);
    void onFailure(Throwable t);
}
