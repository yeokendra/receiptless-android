package com.ocr.receiptless.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.google.gson.Gson;
import com.ocr.receiptless.LoginActivity;
import com.ocr.receiptless.model.User;

import java.text.NumberFormat;
import java.util.Locale;

public class Util {
    public static final String LOG = "receiptless";
    public static final String IMAGE_URI_PATH = "image_uri_path";
    public static final String FROM_CAMERA = "from_camera";
    public static final String API_URL_PREFIX = "http://192.168.240.3/ocr_api";
    public static final String API_URL_PREFIX_BACKUP = "http://192.168.240.3/ocr_api";
    public static final String PREF_USER = "user_pref";

    public static void saveUser(Context context, User user){
        SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor prefsEditor = mPrefs.edit();
        if(user!=null) {
            Gson gson = new Gson();
            String json = gson.toJson(user);
            prefsEditor.putString(Util.PREF_USER, json);
            prefsEditor.apply();
        }else{
            prefsEditor.putString(Util.PREF_USER, null);
            prefsEditor.apply();
        }
    }

    public static User getUser(Context context){
        SharedPreferences mPrefs =  PreferenceManager.getDefaultSharedPreferences(context);
        Gson gson = new Gson();
        String json = mPrefs.getString(Util.PREF_USER, "");
        return gson.fromJson(json, User.class);
    }

    public static String formatCurrency(double number){
        return NumberFormat.getNumberInstance(Locale.US).format(number);
    }
}
