package com.edbrix.contentbrix;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.crashlytics.android.Crashlytics;
import com.edbrix.contentbrix.baseclasses.BaseActivity;
import com.edbrix.contentbrix.commons.AlertDialogManager;
import com.edbrix.contentbrix.data.KnowledgeBrixUserData;
import com.edbrix.contentbrix.utils.SessionManager;
import com.edbrix.contentbrix.volley.VolleySingleton;
import com.edbrix.contentbrix.walkthrough.WalkThroughActivity;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import static com.edbrix.contentbrix.VizippDrawLogin.REQUEST_CODE_CHANGE_PWD;


public class SplashActivity extends BaseActivity
{
    private static final int SplashTimeoutNormal = 2500;
    private static final int SplashTimeoutAutologin = 1500;

    private SessionManager sessionManager;
    private Boolean endSplash;

    private SplashTimeoutTask splashTimeoutTask;

    private ViewGroup infoContainerLayout;
    private ProgressBar infoProgressBar;
    private TextView infoTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (isTablet()) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE); // stop screen rotation on phones
        }else{
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }
        setContentView(R.layout.activity_splash);
        this.sessionManager = new SessionManager(this);


        try {
            ((TextView) findViewById(R.id.textView_version)).setText("Ver " + getPackageManager().getPackageInfo(getPackageName(), 0).versionName);
        } catch (Exception e) { Crashlytics.logException(e);}

       // setContentView(R.layout.activity_splash);

        //new code
        if (!isTaskRoot()) {
            final Intent intent = getIntent();
            final String intentAction = intent.getAction();
            if (intent.hasCategory(Intent.CATEGORY_LAUNCHER) && intentAction != null && intentAction.equals(Intent.ACTION_MAIN)) {
                finish();
                return;
            }
        }

        View rootView = findViewById(R.id.layoutSplash);
        rootView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                SplashActivity.this.endSplash = true;

            }
        });
        this.infoContainerLayout = (ViewGroup) this.findViewById(R.id.infoContainerLayout);
        this.infoProgressBar = (ProgressBar) this.findViewById(R.id.infoProgressBar);
        this.infoTextView = (TextView) this.findViewById(R.id.infoTextView);
        this.infoProgressBar.setVisibility(View.GONE);
        this.sessionManager = new SessionManager(this);

        this.endSplash = false;

        this.splashTimeoutTask = new SplashTimeoutTask();
        this.splashTimeoutTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, SplashTimeoutAutologin);
    }

    private class SplashTimeoutTask extends AsyncTask<Integer, Void, Void> {

        private static final int TimeoutSleepInterval = 200;

        @Override
        protected Void doInBackground(Integer... params) {
            int count = 0;
            int maxCount = params[0] / TimeoutSleepInterval;

            while (count < maxCount) {
                try {
                    Thread.sleep(TimeoutSleepInterval);
                } catch (InterruptedException e) {
                    ; // Ignore
                    Crashlytics.logException(e);
                }
                count++;
                if (this.isCancelled()) {
                    break;
                }
                if (SplashActivity.this.endSplash) {
                    break;
                }
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            /*if (SplashActivity.this.sessionManager.getWalkthroughSkipValue()) {
                if (SplashActivity.this.sessionManager.hasSessionVizippCredentials()) {
//                    startRecordActivity();
                    authenticateUser(sessionManager.getSessionVizippUsername(), sessionManager.getSessionVizippPassword());
                } else {
                    callLoginActivity();
                }
            } else {
                showWalkthrough();
            }*////change shashank for demo

            if (SplashActivity.this.sessionManager.hasSessionVizippCredentials())
            {
                String apiKey = sessionManager.getLoggedKnowledgeBrixUserData().getApiKey();
                String secretKey = sessionManager.getLoggedKnowledgeBrixUserData().getSecretekey();
                startRecordActivity();
//                authenticateUser(sessionManager.getSessionVizippUsername(), sessionManager.getSessionVizippPassword(),apiKey,secretKey);
            } else {
                callLoginActivity();
            }
        }
    }

    private void startRecordActivity() {
        Intent mainIntent = new Intent();
        mainIntent.setClass(SplashActivity.this, StartRecordActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(mainIntent);
        finish();
    }

    private void callLoginActivity() {
        Intent mainIntent = new Intent();
        mainIntent.setClass(SplashActivity.this, VizippDrawLogin.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(mainIntent);
        finish();
    }

    private void showWalkthrough()
    {
        if (!sessionManager.getWalkthroughSkipValue())
        {
            Intent intent = new Intent(this, WalkThroughActivity.class);
            intent.putExtra("ismain", true);
            startActivity(intent);
            finish();
        }
    }

 /*   private void authenticateUser(final String email, final String password,final String apiKey, final String secretekey) {

        Map<String, String> requestMap = new HashMap<String, String>();
        String SECRETKEY = "MjQ1QDEyIzJZSEQtODVEQTJTM0RFQTg1Mz1JRTVCNEE1MQ==";
        String APIKEY = "QVBAMTIjMllIRC1TREFTNUQtNUFTRksyMjEx";

        requestMap.put("Email", email);
        requestMap.put("Password", password);
        requestMap.put("APIKEY", apiKey);
        requestMap.put("SECRETKEY",secretekey );

        try {
            infoProgressBar.setVisibility(View.VISIBLE);
            infoTextView.setVisibility(View.VISIBLE);
            infoTextView.setText("Logging in..");
            String tempUrl = "https://services.edbrix.net/";
            JsonObjectRequest authenticateUserRequest = new JsonObjectRequest(Request.Method.POST, tempUrl + "auth/login", new JSONObject(requestMap), new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response)
                {
                    infoProgressBar.setVisibility(View.GONE);
//                    hideBusyProgress();
//                    Log.v("Volley Response", response.toString());
                    try
                    {
                        if (response != null)
                        {
                            Log.v(SplashActivity.class.getName().toString(), "authenticateUserResp: \n"+response.toString());
                            if (response.has("Success") && response.getString("Success").equals("1"))
                            {
                                if (response.has("User")) {
                                    //VizippUserData vizippUserData = getGlobalMethods().toObjects(response.getJSONObject("userData").toString(), VizippUserData.class);
                                    KnowledgeBrixUserData knowledgeBrixUserData = getGlobalMethods().toObjects(response.getJSONObject("User").toString(), KnowledgeBrixUserData.class);
                                    knowledgeBrixUserData.setAccessToken(response.getString("AccessToken"));
                                    knowledgeBrixUserData.setApiKey(apiKey);
                                    knowledgeBrixUserData.setSecretekey(secretekey);
                                    Log.v(VizippDrawLogin.class.getName().toString(), " AccessToken : "+knowledgeBrixUserData.getAccessToken());

                                    sessionManager.updateLoggedKnoledgebrixUserData(knowledgeBrixUserData);
                                    sessionManager.updateSessionVizippUsername(email);
                                    sessionManager.updateSessionVizippPassword(password);
                                    sessionManager.updateSessionVizippToken(knowledgeBrixUserData.getAccessToken());
//                                    showToast(vizippUserData.getFirstName());
                                    if (!sessionManager.getSessionLastLoggedVizippUsername().isEmpty()) {
                                        if (!sessionManager.getSessionLastLoggedVizippUsername().equalsIgnoreCase(sessionManager.getSessionVizippUsername())) {
                                            sessionManager.updateVideoUploadCount(0);
                                        }
                                    }
                                    sessionManager.updateSessionLastLoggedVizippUsername(email);
                                    *//*if (vizippUserData.isPasswordReset()) {
                                        //Go to change password later successfully change password update password and isPasswordReset=0 in app session's user object
                                        Intent changePwdIntent = new Intent(SplashActivity.this, VizippDrawChangePassword.class);
                                        startActivityForResult(changePwdIntent, REQUEST_CODE_CHANGE_PWD);
                                        showToast("Need to change password..");
                                    }*//*
                                    infoTextView.setText("Logged in successfully..");
                                    startRecordActivity();

                                  *//*  else if (!vizippUserData.isSubscribed()) {
                                        //Login successfull...
                                        showToast(response.getString("message"));
                                        moveForwardToPayment();
                                    } *//*

                                   *//* else {
                                        infoTextView.setText("Logged in successfully..");
                                        startRecordActivity();
                                    }*//*
                                }
//                                setResult(RecordPreviewActivity.RESULT_OK);
//                                finish();
                            } else if(response.has("Error"))
                            {
                                JSONObject ErrorjsonObject = response.getJSONObject("Error");
                                getAlertDialogManager().Dialog("Error Code: " + ErrorjsonObject.getString("ErrorCode"), ErrorjsonObject.getString("ErrorMessage"), new AlertDialogManager.onSingleButtonClickListner() {
                                    @Override
                                    public void onPositiveClick() {
//                                        finish();
                                    }
                                }).show();
                            }
                        }
                    } catch (Exception e) {
                        Crashlytics.logException(e);
                        e.printStackTrace();
                        Log.v("Volley Excep", e.getMessage());
                        infoTextView.setText("Something went wrong. Please try again later.");
                        showToast("Something went wrong. Please try again later.");
                    }
                }

            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    infoProgressBar.setVisibility(View.GONE);
                    infoTextView.setText(VolleySingleton.getErrorMessage(error));
//                    hideBusyProgress();
                    Log.v("Volley VError", VolleySingleton.getErrorMessage(error));
                    showToast(VolleySingleton.getErrorMessage(error));
                }

            }) {
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    HashMap<String, String> headers = new HashMap<String, String>();
                    headers.put("Content-Type", "application/json");
                    return headers;
                }

            };

            VolleySingleton.getInstance().addToRequestQueue(authenticateUserRequest);
        } catch (Exception e) {
            Crashlytics.logException(e);
            infoProgressBar.setVisibility(View.GONE);
            infoTextView.setText("Something went wrong. Please try again later.");
//            infoTextView.setVisibility(View.GONE);
//            hideBusyProgress();
            Log.v("Volley Excep", e.getMessage());
            showToast("Something went wrong. Please try again later.");
        }
    }*/

    /**
     * Receiving activity result
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CODE_CHANGE_PWD:
                if (resultCode == RESULT_OK) {
                    if (sessionManager.getLoggedKnowledgeBrixUserData() != null) {
                        startRecordActivity();
                       /* if (!sessionManager.getLoggedVizippUserData().isSubscribed()) {
                            moveForwardToPayment();
                        } else {
                            startRecordActivity();
                        }*/
                    }
                } else {
                }
                break;
        }
    }
}
