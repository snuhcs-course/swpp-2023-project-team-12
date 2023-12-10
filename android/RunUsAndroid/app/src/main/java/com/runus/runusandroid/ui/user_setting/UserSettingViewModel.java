package com.runus.runusandroid.ui.user_setting;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class UserSettingViewModel extends ViewModel {

    private final MutableLiveData<String> mText;

    public UserSettingViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is UserSetting fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }

    public void setText(String text) {
        mText.setValue(text);
    }
}