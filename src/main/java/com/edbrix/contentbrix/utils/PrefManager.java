package com.edbrix.contentbrix.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.edbrix.contentbrix.Constants;


public class PrefManager {
    public static SharedPreferences getPreferences(Context context) {
        return context.getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE);
    }
}
