package com.runus.runusandroid.ui.multi_mode;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class
MultiModeViewModel extends ViewModel {

    private final MutableLiveData<String> mText;

    public MultiModeViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is multi mode fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}