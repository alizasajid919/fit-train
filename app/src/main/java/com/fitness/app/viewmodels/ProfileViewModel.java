package com.fitness.app.viewmodels;

import android.net.Uri;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.fitness.app.models.User;
import com.fitness.app.repositories.UserRepository;

public class ProfileViewModel extends ViewModel {
    private final UserRepository userRepository;
    private final MutableLiveData<User> userProfileLiveData = new MutableLiveData<>();

    public ProfileViewModel() {
        userRepository = new UserRepository();
    }

    public LiveData<UserRepository.Resource<User>> getUserProfile(String uid) {
        return userRepository.getUserProfile(uid);
    }

    public LiveData<UserRepository.Resource<Void>> updateUserProfile(User user) {
        return userRepository.updateUserProfile(user);
    }

    public LiveData<UserRepository.Resource<String>> uploadProfileImage(String uid, Uri imageUri) {
        return userRepository.uploadProfileImage(uid, imageUri);
    }

    public void setUser(User user) {
        userProfileLiveData.setValue(user);
    }

    public LiveData<User> getUser() {
        return userProfileLiveData;
    }

    public void logout() {
        userRepository.logout();
    }
}
