package com.example.appchat.viewmodel;

import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;
import android.content.Context;
import android.support.annotation.NonNull;

import java.util.HashMap;
import java.util.Map;

public class ChatViewModelFactory implements ViewModelProvider.Factory {

    private Context context;
    private final String TAG = ChatViewModelFactory.class.getSimpleName();

    private static ChatViewModelFactory chatViewModelFactory;
    private Map<String, ViewModel> map = new HashMap<>();

    private ChatViewModelFactory(Context context) {
        this.context = context;
    }

    public static ChatViewModelFactory getInstance(Context context) {
        if (chatViewModelFactory == null) {
            chatViewModelFactory = new ChatViewModelFactory(context);
        }
        return chatViewModelFactory;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(ChatViewModel.class)) {
            if (map.containsKey(TAG)) {
                return (T) getViewModel();
            } else {
                addViewModel(new ChatViewModel(context));
                return (T) getViewModel();
            }
        }
        throw new IllegalArgumentException("Unknown ViewModel class");
    }

    private void addViewModel(ViewModel viewModel) {
        map.put(TAG, viewModel);
    }

    public void deleteViewModel() {
        if (map.containsKey(TAG)) {
            map.remove(TAG);
        }

    }

    private ViewModel getViewModel() {
        return map.get(TAG);
    }
}
