package com.edbrix.contentbrix;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.os.AsyncTask;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.crashlytics.android.Crashlytics;
import com.dropbox.core.android.Auth;
import com.dropbox.core.v2.users.FullAccount;
import com.edbrix.contentbrix.baseclasses.BaseActivity;
import com.edbrix.contentbrix.commons.AlertDialogManager;
import com.edbrix.contentbrix.data.KnowledgeBrixUserData;
import com.edbrix.contentbrix.dropbox.DropboxClient;
import com.edbrix.contentbrix.dropbox.GetDropboxCurrentAccountTask;
import com.edbrix.contentbrix.utils.SessionManager;
import com.edbrix.contentbrix.volley.VolleySingleton;
import com.edbrix.contentbrix.walkthrough.WalkThroughActivity;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.ipaulpro.afilechooser.utils.FileUtils;

import org.json.JSONObject;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class SettingsActivity extends BaseActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private static final int REQUEST_CODE_EDBRIX_LOGIN = 151;
    private static final int REQUEST_CODE_RESOLUTION = 3;
    private static final String TAG = SettingsActivity.class.getSimpleName();

    private ImageView removeAddVizippAC;
    private ImageView removeAddEdbrixAC;
    private ImageView removeAddDPBoxAC;
    private ImageView removeAddDriveAC;
    private TextView edbrixUName;
    private TextView vizippDUName;
    private TextView vizippUsername;
    private TextView dropBoxUName;
    private TextView driveUName;
    private TextView textManageSubs;

    private boolean isVizippLogged;
    private boolean isEdbrixLogged;
    private boolean isDropBoxLogged;
    private Switch switchVibrateOnOFF;
    private LinearLayout factoryResetLayout;
    private LinearLayout appTourLayout;
    private LinearLayout signOutBtn;

    private LinearLayout updateCardLayout;
    private LinearLayout manageSubsLayout;

    private SessionManager sessionManager;

    private GoogleApiClient sGoogleApiClient;

    private ConnectionResult driveConnectionResult;

    private boolean isDropBoxUserDetailFetchingPending;

    private GoogleSignInClient googleSignInClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setContentView(R.layout.activity_settings);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        sessionManager = new SessionManager(SettingsActivity.this);

        isVizippLogged = false;
        isDropBoxLogged = false;
        isEdbrixLogged = false;

        try {
            ((TextView) findViewById(R.id.textViewVer)).setText("Ver " + getPackageManager().getPackageInfo(getPackageName(), 0).versionName+"_"+getPackageManager().getPackageInfo(getPackageName(), 0).versionCode);
        } catch (Exception e) { Crashlytics.logException(e);}

        //init component
        initComponent();

        //set Listeners
        setListeners();

        //set Values
        setValues();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // update dropbox token after authentication and fetch user details
        updateDropBoxAccessToken();

        if (sGoogleApiClient == null) {
            /**
             * Create the API client and bind it to an instance variable.
             * We use this instance as the callback for connection and connection failures.
             * Since no account name is passed, the user is prompted to choose.
             */
            sGoogleApiClient = startGoogleClientAuthentication(this, this);
            sGoogleApiClient.connect();
//            boolean googleClientConnected = sGoogleApiClient.hasConnectedApi(Drive.API);
//            Toast.makeText(SettingsActivity.this,"DriveAPI session connected :"+googleClientConnected,Toast.LENGTH_LONG).show();
        }

    }

    private void initComponent() {
        removeAddVizippAC = (ImageView) findViewById(R.id.removeAddVizippAC);
        removeAddEdbrixAC = (ImageView) findViewById(R.id.removeAddEdbrixAC);
        removeAddDPBoxAC = (ImageView) findViewById(R.id.removeAddDPBoxAC);
        removeAddDriveAC = (ImageView) findViewById(R.id.removeAddDriveAC);

        edbrixUName = (TextView) findViewById(R.id.edbrixUName);
        vizippDUName = (TextView) findViewById(R.id.vizippDUName);
        vizippUsername = (TextView) findViewById(R.id.vizippUsername);
        dropBoxUName = (TextView) findViewById(R.id.dropBoxUName);
        driveUName = (TextView) findViewById(R.id.driveUName);
        textManageSubs = (TextView) findViewById(R.id.textManageSubs);

        switchVibrateOnOFF = (Switch) findViewById(R.id.switchVibrateOnOFF);
        factoryResetLayout = (LinearLayout) findViewById(R.id.factoryResetLayout);
        appTourLayout = (LinearLayout) findViewById(R.id.appTourLayout);
        signOutBtn = (LinearLayout) findViewById(R.id.signOutBtn);
        manageSubsLayout = (LinearLayout) findViewById(R.id.manageSubsLayout);
        updateCardLayout = (LinearLayout) findViewById(R.id.updateCardLayout);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getResources().getString(R.string.web_client_id))
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(this, gso);

    }

    private void setListeners() {
        switchVibrateOnOFF.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                sessionManager.updateSessionVibratePhoneState(isChecked);
            }
        });
        removeAddVizippAC.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToVizippLogin(false);
            }
        });

        removeAddEdbrixAC.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isEdbrixLogged) {
                    getAlertDialogManager().Dialog(getResources().getString(R.string.app_name), getString(R.string.edbrix_remove_continue_msg), true, new AlertDialogManager.onTwoButtonClickListner() {
                        @Override
                        public void onNegativeClick() { }

                        @Override
                        public void onPositiveClick() {
                            sessionManager.clearSessionCredentials();
                            setValues();
                        }
                    }).show();

                } else {
                    Intent loginEdbrixIntent = new Intent(SettingsActivity.this, EdbrixLoginActivity.class);
                    startActivityForResult(loginEdbrixIntent, REQUEST_CODE_EDBRIX_LOGIN);
                }
            }
        });

        removeAddDPBoxAC.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isDropBoxLogged) {
                    getAlertDialogManager().Dialog(getResources().getString(R.string.app_name), getString(R.string.dropbox_remove_continue_msg), true, new AlertDialogManager.onTwoButtonClickListner() {
                        @Override
                        public void onNegativeClick() {

                        }

                        @Override
                        public void onPositiveClick() {
                            sessionManager.updateSessionDropBoxToken(null);
                            sessionManager.updateSessionDropBoxDisplayName(null);
                            setValues();
                        }
                    }).show();
                } else {
                    isDropBoxUserDetailFetchingPending = true;
                    Auth.startOAuth2Authentication(SettingsActivity.this, getString(R.string.DROPBOX_APP_KEY));
                }
            }
        });

        removeAddDriveAC.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (sessionManager.getDriveConnected()) {
                    getAlertDialogManager().Dialog(getResources().getString(R.string.app_name), getString(R.string.google_drive_remove_continue_msg), true, new AlertDialogManager.onTwoButtonClickListner() {
                        @Override
                        public void onNegativeClick() {

                        }

                        @Override
                        public void onPositiveClick() {
                            clearGoogleDriveSession();
                        }
                    }).show();

                } else {
                    startToConnectDrive();
                }
            }
        });

        factoryResetLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getAlertDialogManager().Dialog(getResources().getString(R.string.app_name),
                        "Are you sure want to restore default app settings?\n" +
                                "Once you click OK, your synced accounts and recorded videos will be deleted from device.",
                        true,
                        new AlertDialogManager.onTwoButtonClickListner() {
                            @Override
                            public void onNegativeClick() {

                            }

                            @Override
                            public void onPositiveClick() {
                                File yourDir = VolleySingleton.getAppStorageDirectory();
                                new DeleteAllFileAsyncTask().execute(yourDir.listFiles());
                            }
                        }).show();
            }
        });

        manageSubsLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (sessionManager.getLoggedKnowledgeBrixUserData() != null && sessionManager.getLoggedKnowledgeBrixUserData().getId() != null && sessionManager.getLoggedKnowledgeBrixUserData().getId().length() > 0) {
                    moveForwardToPayment();
                } else {
                    goToVizippLogin(true);
                }
            }
        });

        updateCardLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent updateIntent = new Intent(SettingsActivity.this, UpdateCardDetailActivity.class);
                startActivity(updateIntent);
            }
        });

        signOutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               /* getAlertDialogManager().Dialog(getResources().getString(R.string.app_name), getString(R.string.sign_out_continue_msg), true, new AlertDialogManager.onTwoButtonClickListner() {
                    @Override
                    public void onNegativeClick() {

                    }

                    @Override
                    public void onPositiveClick() {
                        sessionManager.clearSessionVizippCredentials();
                        sessionManager.clearSessionCredentials();
                        sessionManager.updateSessionDropBoxToken(null);
                        sessionManager.updateSessionDropBoxDisplayName(null);
                        sessionManager.updateSessionOrgName("");
                        clearGoogleDriveSession();
                        callLoginActivity();
                    }
                }).show();*/

                AlertDialog.Builder alertDialog = new AlertDialog.Builder(SettingsActivity.this);
                alertDialog.setTitle("Contentbrix");
                alertDialog.setMessage(getString(R.string.sign_out_continue_msg));
                alertDialog.setIcon(R.drawable.app_logo_round_new24);
                alertDialog.setPositiveButton("yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        sessionManager.clearSessionVizippCredentials();
                        sessionManager.clearSessionCredentials();
                        sessionManager.updateSessionDropBoxToken(null);
                        sessionManager.updateSessionDropBoxDisplayName(null);
                        sessionManager.updateSessionOrgName("");
                        googleSignInClient.signOut().addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                Log.e(TAG,"Loggedout from google account");
                            }
                        });
                        clearGoogleDriveSession();
                        callLoginActivity();
                    }
                });
                alertDialog.setNegativeButton("no", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {    }
                });
                alertDialog.show();
            }
        });

        appTourLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showWalkThrough();
            }
        });

    }

    private void callLoginActivity() {
        Intent mainIntent = new Intent();
        mainIntent.setClass(SettingsActivity.this, VizippDrawLogin.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(mainIntent);
        finish();
    }

    private void setValues() {
        if (sessionManager.hasSessionVizippCredentials()) {
            vizippUsername.setText(sessionManager.getLoggedKnowledgeBrixUserData().getFirstName()+" "+sessionManager.getLoggedKnowledgeBrixUserData().getLastName());
            vizippDUName.setText(sessionManager.getLoggedKnowledgeBrixUserData().getEmail()+"\n"
                                +sessionManager.getSessionOrgName());
//            removeAddVizippAC.setImageDrawable(ContextCompat.getDrawable(SettingsActivity.this, R.drawable.sign_out_grey));
            removeAddVizippAC.setVisibility(View.INVISIBLE);
            signOutBtn.setVisibility(View.VISIBLE);
            isVizippLogged = true;
        } else {
            vizippDUName.setText(getResources().getString(R.string.sign_in));
//            removeAddVizippAC.setImageDrawable(ContextCompat.getDrawable(SettingsActivity.this, R.drawable.add_user_d_grey));
            removeAddVizippAC.setVisibility(View.VISIBLE);
            signOutBtn.setVisibility(View.GONE);
            isVizippLogged = false;
        }
        if (sessionManager.hasSessionCredentials()) {
            edbrixUName.setText(sessionManager.getSessionUsername());
            removeAddEdbrixAC.setImageDrawable(ContextCompat.getDrawable(SettingsActivity.this, R.drawable.delete_grey));
            isEdbrixLogged = true;
        } else {
            edbrixUName.setText(getResources().getString(R.string.my_files));
            removeAddEdbrixAC.setImageDrawable(ContextCompat.getDrawable(SettingsActivity.this, R.drawable.add_user_d_grey));
            isEdbrixLogged = false;
        }

        if (sessionManager.getSessionDropBoxToken().length() > 0 && sessionManager.getSessionDropBoxDisplayName().length() > 0) {
            dropBoxUName.setText(sessionManager.getSessionDropBoxDisplayName());
            removeAddDPBoxAC.setImageDrawable(ContextCompat.getDrawable(SettingsActivity.this, R.drawable.delete_grey));
            isDropBoxLogged = true;
        } else if (sessionManager.getSessionDropBoxToken().length() > 0 && sessionManager.getSessionDropBoxDisplayName().length() == 0) {
            getDropBoxUserAccount(sessionManager.getSessionDropBoxToken());
        } else {
            dropBoxUName.setText(getResources().getString(R.string.dropbox));
            removeAddDPBoxAC.setImageDrawable(ContextCompat.getDrawable(SettingsActivity.this, R.drawable.add_user_d_grey));
            isDropBoxLogged = false;
        }

        if (sessionManager.getLoggedKnowledgeBrixUserData() != null && sessionManager.getLoggedKnowledgeBrixUserData().getId() != null && sessionManager.getLoggedKnowledgeBrixUserData().getId().length() > 0) {
            textManageSubs.setText(getResources().getString(R.string.upgrade_pro));
            updateCardLayout.setVisibility(View.GONE);
        } else {
            textManageSubs.setText(getResources().getString(R.string.upgrade_pro));
            updateCardLayout.setVisibility(View.GONE);
        }
        switchVibrateOnOFF.setChecked(sessionManager.getSessionVibratePhoneState());
    }

    /**
     * Update Access Token in app session after authentication process
     */
    public void updateDropBoxAccessToken() {
        String dropBoxToken = Auth.getOAuth2Token(); //generate Access Token
        if (dropBoxToken != null) {
            //Store dropBoxToken in SharedPreferences
            sessionManager.updateSessionDropBoxToken(dropBoxToken);
            if (isDropBoxUserDetailFetchingPending)
                getDropBoxUserAccount(dropBoxToken);
        }
    }

    /**
     * Receiving activity result
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // if the result is capturing Image
        if (requestCode == REQUEST_CODE_EDBRIX_LOGIN) {
            if (resultCode == RESULT_OK) {
                setValues();
            }
        } else if (requestCode == REQUEST_CODE_RESOLUTION) {
            if (resultCode == RESULT_OK) {
                sGoogleApiClient.connect();
            }
        } else if (requestCode == VizippDrawLogin.REQUEST_CODE_VIZIPP_LOGIN || requestCode == VizippDrawLogin.REQUEST_CODE_VIZIPP_PAY) {
            if (resultCode == RESULT_OK) {
                setValues();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.settings_menu, menu);
//        menu.findItem(R.id.homeOption).setVisible(false);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.homeOption:
                Intent startRecordIntent = new Intent();
                startRecordIntent.setClass(SettingsActivity.this, StartRecordActivity.class);
                startRecordIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startRecordIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startRecordIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(startRecordIntent);
                finish();
                return true;

        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * AsyncTask for Delete Video file from selecting delete menu.
     */
    private class DeleteAllFileAsyncTask extends AsyncTask<File, Void, Boolean> {
        @Override
        protected Boolean doInBackground(File... params) {
            for (File file : params) {
                if (file.isFile()) {
                    if (FileUtils.getMimeType(file).equals("video/mp4") && FileUtils.getExtension(file.getPath()).equals(".mp4")) {
                        file.delete();
                    }
                }
            }
            return true;
        }

        @Override
        protected void onPreExecute() {
            showBusyProgress();
        }

        @Override
        protected void onPostExecute(Boolean fileDeleted) {
            hideBusyProgress();
            showToast(getResources().getString(R.string.app_reset_msg));
            clearGoogleDriveSession();
            sessionManager.updateSessionVibratePhoneState(false);
            sessionManager.updateSessionDropBoxToken(null);
            sessionManager.clearSessionCredentials();
            setValues();
            setResult(RESULT_OK);
        }
    }

//    private void showWalkThrough() {
//        Intent intent = new Intent(this, WalkThroughActivity.class);
//        intent.putExtra("ismain", false);
//        startActivity(intent);
//    }

    @Override
    protected void onStop() {
        super.onStop();
        if (sGoogleApiClient != null) {
            // disconnect Google Android Drive API connection.
            sGoogleApiClient.disconnect();
        }
        super.onPause();
    }

    /**
     * It invoked when Google API client connected
     *
     * @param connectionHint
     */
    @Override
    public void onConnected(@Nullable Bundle connectionHint) {
        sessionManager.updateDriveConnectedFlag(true);
        removeAddDriveAC.setImageDrawable(ContextCompat.getDrawable(SettingsActivity.this, R.drawable.delete_grey));
        Log.i(TAG, "GoogleApiClient connection connected :");
    }

    /**
     * It invoked when connection suspended
     *
     * @param cause
     */
    @Override
    public void onConnectionSuspended(int cause) {
        Log.i(TAG, "GoogleApiClient connection suspended :" + cause);
    }

    /**
     * It invoked when connection failed
     *
     * @param result
     */
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult result) {
        driveConnectionResult = result;
        sessionManager.updateDriveConnectedFlag(false);
        removeAddDriveAC.setImageDrawable(ContextCompat.getDrawable(SettingsActivity.this, R.drawable.add_user_d_grey));
        // Called whenever the API client fails to connect.
        Log.i(TAG, "GoogleApiClient connection failed: " + result.toString());

        if (!result.hasResolution()) {

            // show the localized error dialog.
            GoogleApiAvailability.getInstance().getErrorDialog(this, result.getErrorCode(), 0).show();
            return;
        }

        /**
         *  The failure has a resolution. Resolve it.
         *  Called typically when the app is not yet authorized, and an  authorization
         *  dialog is displayed to the user.
         */

       /* try {

            result.startResolutionForResult(this, REQUEST_CODE_RESOLUTION);

        } catch (IntentSender.SendIntentException e) {

            Log.e(TAG, "Exception while starting resolution activity", e);
        }*/

    }

    private void startToConnectDrive() {
        if (driveConnectionResult != null) {
            try {
                driveConnectionResult.startResolutionForResult(this, REQUEST_CODE_RESOLUTION);

            } catch (IntentSender.SendIntentException e) {
                Crashlytics.logException(e);
                Log.e(TAG, "Exception while starting resolution activity", e);
            }
        } else {
            showToast("Unable to connect Google Drive. Please try again later.");
        }
    }

    private void clearGoogleDriveSession() {
        if (sGoogleApiClient != null && sGoogleApiClient.isConnected()) {
            sGoogleApiClient.clearDefaultAccountAndReconnect().setResultCallback(new ResultCallback<Status>() {

                @Override
                public void onResult(Status status) {
                    boolean isCleared = status.isSuccess();
                    if (isCleared) {
                        sessionManager.updateDriveConnectedFlag(false);
                        removeAddDriveAC.setImageDrawable(ContextCompat.getDrawable(SettingsActivity.this, R.drawable.add_user_d_grey));
                        if (sGoogleApiClient != null && sGoogleApiClient.isConnected()) {
                            sGoogleApiClient.disconnect();
                        }
                    }

                }
            });
        }
    }


    private void cancelSubscription(final String userId, final String apiToken) {

        Map<String, String> requestMap = new HashMap<String, String>();
        requestMap.put("APITOKEN", apiToken);
        requestMap.put("userId", userId);

        try {
            showBusyProgress();
            JsonObjectRequest cancelSubscriptionRequest = new JsonObjectRequest(Request.Method.POST, VolleySingleton.getWsBaseUrl() + "stripegateway/cancelsubscription.php", new JSONObject(requestMap), new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    hideBusyProgress();
                    Log.v("Volley Response", response.toString());
                    try {
                        if (response != null) {
                            if (response.has("status") && response.getString("status").equals("1")) {
                                if (sessionManager.getLoggedKnowledgeBrixUserData() != null && sessionManager.getLoggedKnowledgeBrixUserData().getId() != null && sessionManager.getLoggedKnowledgeBrixUserData().getId().length() > 0) {
                                    //VizippUserData tempUserData = sessionManager.getLoggedVizippUserData();
                                    KnowledgeBrixUserData tempUserData = sessionManager.getLoggedKnowledgeBrixUserData();
                                    //tempUserData.setSubscribed(false);
                                    sessionManager.updateLoggedKnoledgebrixUserData(tempUserData);
                                }
                                textManageSubs.setText(getResources().getString(R.string.upgrade_pro));
                                updateCardLayout.setVisibility(View.GONE);
                            } else if (response.has("status") && response.getString("status").equals("0")) {
                                getAlertDialogManager().Dialog("ErrorCode : " + response.getString("errorCode"), response.getString("message"), false, null).show();
                            }
                        } else {
                            showToast("Something went wrong. Please try again later.");
                            finish();
                        }
                    } catch (Exception e) {
                        Crashlytics.logException(e);
                        e.printStackTrace();
                        Log.v("Volley Excep", e.getMessage());
                        showToast("Something went wrong. Please try again later.");
                    }
                }

            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    hideBusyProgress();
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

            VolleySingleton.getInstance().addToRequestQueue(cancelSubscriptionRequest);
        } catch (Exception e) {
            Crashlytics.logException(e);
            hideBusyProgress();
            Log.v("Volley Excep", e.getMessage());
            showToast("Something went wrong. Please try again later.");
        }

    }

    private void moveForwardToPayment() {
        // move toward payment
        Intent paymentIntent = new Intent(SettingsActivity.this, PaymentActivity.class);
        startActivityForResult(paymentIntent, VizippDrawLogin.REQUEST_CODE_VIZIPP_PAY);
    }

    private void goToVizippLogin(boolean loginTextVisible) {
        Intent loginIntent = new Intent(SettingsActivity.this, VizippDrawLogin.class);
        loginIntent.putExtra(VizippDrawLogin.LOGIN_TEXT_VISIBLE,loginTextVisible);
        startActivityForResult(loginIntent, VizippDrawLogin.REQUEST_CODE_VIZIPP_LOGIN);
    }

    private void getDropBoxUserAccount(String ACCESS_TOKEN) {
        showBusyProgress("Fetching Dropbox account details");
        new GetDropboxCurrentAccountTask(DropboxClient.getClient(ACCESS_TOKEN), new GetDropboxCurrentAccountTask.Callback() {
            @Override
            public void onAccountReceived(FullAccount account) {
                sessionManager.updateSessionDropBoxDisplayName(account.getEmail());
                setValues();
                hideBusyProgress();
                isDropBoxUserDetailFetchingPending = false;
            }

            @Override
            public void onError(Exception error) {
                hideBusyProgress();
                Log.d("Dropbox User", "Error receiving account details.");
                isDropBoxUserDetailFetchingPending = false;
            }
        }).execute();
    }

    private void showWalkThrough() {
        Intent intent = new Intent(this, WalkThroughActivity.class);
        intent.putExtra("ismain", false);
        startActivity(intent);
    }
}
