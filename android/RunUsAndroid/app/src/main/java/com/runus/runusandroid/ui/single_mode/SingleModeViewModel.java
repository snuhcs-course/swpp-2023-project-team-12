package com.runus.runusandroid.ui.single_mode;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class SingleModeViewModel extends ViewModel {

    private final MutableLiveData<String> mText;

    public SingleModeViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is single mode fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}