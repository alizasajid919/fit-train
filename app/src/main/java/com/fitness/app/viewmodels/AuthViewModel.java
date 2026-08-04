package com.fitness.app.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.fitness.app.data.local.LocalDataManager;
import com.fitness.app.repositories.UserRepository;
import com.google.firebase.auth.FirebaseUser;

public class AuthViewModel extends ViewModel {
    private final UserRepository userRepository;

    public AuthViewModel() {
        userRepository = new UserRepository();
    }

    public LiveData<UserRepository.Resource<FirebaseUser>> signInAnonymously(LocalDataManager localDb) {
        return userRepository.signInAnonymously(localDb);
    }

    public LiveData<UserRepository.Resource<FirebaseUser>> register(String firstName, String lastName, String email, String password, LocalDataManager localDb) {
        return userRepository.registerUser(firstName, lastName, email, password, localDb);
    }

    public LiveData<UserRepository.Resource<FirebaseUser>> login(String email, String password, LocalDataManager localDb) {
        return userRepository.loginUser(email, password, localDb);
    }

    public LiveData<UserRepository.Resource<String>> resetPassword(String email) {
        return userRepository.resetPassword(email);
    }

    public FirebaseUser getCurrentUser() {
        return userRepository.getCurrentUser();
    }
}
