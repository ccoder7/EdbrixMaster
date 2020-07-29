package com.edbrix.contentbrix.services;


import android.content.Intent;
import android.util.Log;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.edbrix.contentbrix.app.Config;
import com.edbrix.contentbrix.utils.SessionManager;
import com.google.firebase.iid.FirebaseInstanceId;
//import com.google.firebase.iid.FirebaseInstanceIdService;

/**
 * Created by rajk on 28/10/17.
 */

public class MyFirebaseInstanceIDService {
//        extends FirebaseInstanceIdService {
//
//    private static final String TAG = MyFirebaseInstanceIDService.class.getSimpleName();
//
//    @Override
//    public void onTokenRefresh() {
//        super.onTokenRefresh();
//        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
//
//        // Saving reg id to shared preferences
//        storeRegIdInPref(refreshedToken);
//
//        // sending reg id to your server
//        sendRegistrationToServer(refreshedToken);
//
//        // Notify UI that registration has completed, so the progress indicator can be hidden.
//        Intent registrationComplete = new Intent(Config.REGISTRATION_COMPLETE);
//        registrationComplete.putExtra("token", refreshedToken);
//        LocalBroadcastManager.getInstance(this).sendBroadcast(registrationComplete);
//    }
//
//    private void sendRegistrationToServer(final String token) {
//        // sending gcm token to server
//        Log.v(TAG, "sendRegistrationToServer: " + token);
//    }
//
//    private void storeRegIdInPref(String token) {
//        SessionManager sessionManager = new SessionManager(getApplicationContext());
//        sessionManager.updateSessionFCMToken(token);
//    }
}
