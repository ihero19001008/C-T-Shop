package com.app.ecommerce.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class PageViewModel extends ViewModel {
    private MutableLiveData<String> mNewQuantity;
    public MutableLiveData<String> getCurrentName() {
        if (mNewQuantity == null) {
            mNewQuantity = new MutableLiveData<String>();
        }
        return mNewQuantity;
    }

}
