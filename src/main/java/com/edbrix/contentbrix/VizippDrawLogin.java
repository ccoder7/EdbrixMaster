package com.edbrix.contentbrix;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.crashlytics.android.Crashlytics;
import com.edbrix.contentbrix.baseclasses.BaseActivity;
import com.edbrix.contentbrix.commons.AlertDialogManager;
import com.edbrix.contentbrix.data.AuthenticateUserOrganisationResponse;
import com.edbrix.contentbrix.data.KnowledgeBrixUserData;
import com.edbrix.contentbrix.data.ProductAccessList;
import com.edbrix.contentbrix.data.UserOrganizationList;
import com.edbrix.contentbrix.data.Userproductaccessresponse;
import com.edbrix.contentbrix.utils.SessionManager;
import com.edbrix.contentbrix.volley.GsonRequest;
import com.edbrix.contentbrix.volley.VolleySingleton;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;

public class VizippDrawLogin extends BaseActivity {

    private EditText edtEmail;
    private EditText edtPwd;
    private Button btnLogin;
    private TextView btnRegister;
    private TextView btnForgotPwd;
    private TextView btnChangePwd;
    private TextView loginTitle;
    private SessionManager sessionManager;
    private ImageView backBtn;
    private ImageView eyeIcon;
    private LinearLayout pwdLayout;
    private LinearLayout signUpLayout;
    private boolean isPasswordVisible;
    private boolean isLoginTextVisible;

    public static final int REQUEST_CODE_FORGOT_PWD = 161;
    public static final int REQUEST_CODE_CHANGE_PWD = 162;
    public static final int REQUEST_CODE_REGISTRATION = 163;
    public static final int REQUEST_CODE_VIZIPP_LOGIN = 164;
    public static final int REQUEST_CODE_VIZIPP_PAY = 165;
    public static final String LOGIN_TEXT_VISIBLE = "loginTextVisible";
    public static final int RESULT_LOGIN = 5;
    private GoogleSignInClient googleSignInClient;
    private SignInButton googleSignInButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (isTablet()) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE); // stop screen rotation on phones
        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }
        setContentView(R.layout.activity_vizipp_draw_login);

        sessionManager = new SessionManager(this);
        //initialize component
        initComponent();
        //set listeners to component
        setListeners();
        getProductVersion();
    }

    /*
    initialise component
    */
    private void initComponent() {
        edtEmail = (EditText) findViewById(R.id.edtEmail);
        edtPwd = (EditText) findViewById(R.id.edtPwd);
        btnLogin = (Button) findViewById(R.id.btnLogin);
        btnRegister = (TextView) findViewById(R.id.txtRegister);
        btnForgotPwd = (TextView) findViewById(R.id.txtForgotPwd);
        btnChangePwd = (TextView) findViewById(R.id.txtChangePwd);
        loginTitle = (TextView) findViewById(R.id.loginTitle);
        backBtn = (ImageView) findViewById(R.id.backBtn);
        eyeIcon = (ImageView) findViewById(R.id.eyeIcon);
        pwdLayout = (LinearLayout) findViewById(R.id.pwdLayout);
        signUpLayout = (LinearLayout) findViewById(R.id.signUpLayout);
        googleSignInButton = findViewById(R.id.sign_in_button);
        googleSignInButton.setColorScheme(0);

        // tempory code on 5 march
        signUpLayout.setVisibility(View.VISIBLE);
        // tempory code

        getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN
        );
        isPasswordVisible = false;
        edtPwd.setTransformationMethod(new PasswordTransformationMethod());

        isLoginTextVisible = getIntent().getBooleanExtra(LOGIN_TEXT_VISIBLE, true);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getResources().getString(R.string.web_client_id))
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(VizippDrawLogin.this, gso);

//        if (isLoginTextVisible) {
//            loginTitle.setVisibility(View.VISIBLE);
//        } else {
//            loginTitle.setVisibility(View.GONE);
//        }
    }

    /*
    set Listeners
     */
    private void setListeners() {
        this.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        this.edtEmail.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                edtEmail.setCursorVisible(true);
                return false;
            }
        });
        this.edtPwd.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                edtPwd.setCursorVisible(true);
                return false;
            }
        });
        this.edtPwd.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    // hide virtual keyboard
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(edtPwd.getWindowToken(), 0);
                    //doLogin
                    if (validate()) {
                        //doLogin
                        String email = edtEmail.getText().toString().trim();
                        String password = edtPwd.getText().toString().trim();

                        if (sessionManager.getSessionAppIsUserActive()) {//temporary code added
                            authenticateUserOrganisation(email, password);
                        } else {
                            showToast("User is deactivated.");
                        }
                    }
                    return true;
                }
                return false;
            }
        });

        this.btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(edtPwd.getWindowToken(), 0);

                edtEmail.setBackgroundResource(R.drawable.black_border);
                pwdLayout.setBackgroundResource(R.drawable.black_border);
                if (validate()) {
                    edtEmail.setBackgroundResource(R.drawable.black_border);
                    pwdLayout.setBackgroundResource(R.drawable.black_border);
                    String email = edtEmail.getText().toString().trim();
                    String password = edtPwd.getText().toString().trim();

                    if (sessionManager.getSessionAppIsUserActive()) {//temporary code added
                        authenticateUserOrganisation(email, password);
                    } else {
                        showToast("User is deactivated.");
                    }
                }
            }
        });

        this.btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*Intent registrationIntent = new Intent(VizippDrawLogin.this, VizippDrawRegistration.class);
                startActivityForResult(registrationIntent, REQUEST_CODE_REGISTRATION);*/
                Intent registrationIntent = new Intent(VizippDrawLogin.this, ContentBrixRegistrationActivity.class);
                startActivity(registrationIntent);
            }
        });

        this.btnForgotPwd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent forgotPwdIntent = new Intent(VizippDrawLogin.this, VizippDrawForgotPassword.class);
                startActivityForResult(forgotPwdIntent, REQUEST_CODE_FORGOT_PWD);
            }
        });

        this.btnChangePwd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Intent forgotPwdIntent = new Intent(VizippDrawLogin.this, VizippDrawForgotPassword.class);
//                startActivity(forgotPwdIntent);
//                finish();
            }
        });

        this.eyeIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setPasswordVisible(isPasswordVisible);
                isPasswordVisible = !isPasswordVisible;
            }
        });

        googleSignInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent signInIntent = googleSignInClient.getSignInIntent();
                startActivityForResult(signInIntent, 101);
            }
        });

    }

    private void setPasswordVisible(boolean isVisible) {
        if (isVisible) {
            eyeIcon.setImageDrawable(ContextCompat.getDrawable(VizippDrawLogin.this, R.drawable.eye_invisible));
            edtPwd.setTransformationMethod(new PasswordTransformationMethod());
        } else {
            eyeIcon.setImageDrawable(ContextCompat.getDrawable(VizippDrawLogin.this, R.drawable.eye_visible));
            edtPwd.setTransformationMethod(null);
        }
    }

    /*
    do validation
     */
    private boolean validate() {
        boolean bln = true;
        String email = this.edtEmail.getText().toString().trim();
        String password = this.edtPwd.getText().toString().trim();
        if (email.length() == 0) {
            edtEmail.setError(getResources().getString(R.string.username_blank));
            edtEmail.setBackgroundResource(R.drawable.red_border);
            //getAlertDialogManager().Dialog(getResources().getString(R.string.error), getResources().getString(R.string.username_blank), true, null).show();
            bln = false;
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            edtEmail.setBackgroundResource(R.drawable.red_border);
            edtEmail.setError(getResources().getString(R.string.invalid_email));
//            getAlertDialogManager().Dialog(getResources().getString(R.string.error), getResources().getString(R.string.invalid_email), false, null).show();
            bln = false;
        } else if (password.length() == 0) {
            pwdLayout.setBackgroundResource(R.drawable.red_border);
            edtPwd.setError(getResources().getString(R.string.password_blank));
            edtPwd.setHint(getResources().getString(R.string.password_blank));
            // getAlertDialogManager().Dialog(getResources().getString(R.string.error), getResources().getString(R.string.password_blank), true, null).show();
            bln = false;
        }
        return bln;
    }

    /**
     * Receiving activity result
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        /*if (resultCode == Activity.RESULT_OK)
            switch (requestCode) {

            }*/

        switch (requestCode) {
            case 101://009
                try {
                    Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
                    GoogleSignInAccount account = task.getResult(ApiException.class);
                    //onLoggedIn(account);
                    IsSkipPassword = true;
                    authenticateUserOrganisation(account.getEmail(), "");
                    //showToast("LoggedInWithGoogle");
                } catch (ApiException e) {
                    // The ApiException status code indicates the detailed failure reason.
                    Log.w(VizippDrawLogin.class.getName(), "signInResult:failed code=" + e.getStatusCode());
                }
                break;

            case REQUEST_CODE_REGISTRATION:
                if (resultCode == RESULT_OK)
                    startRecordActivity();
                break;
            case REQUEST_CODE_FORGOT_PWD:
                break;
            case REQUEST_CODE_VIZIPP_PAY:
                startRecordActivity();
                break;
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

    private void startRecordActivity() {
        Intent mainIntent = new Intent();
        mainIntent.setClass(VizippDrawLogin.this, StartRecordActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(mainIntent);
        finish();
    }

    private void moveForwardToPayment() {
        // move toward payment
        Intent paymentIntent = new Intent(VizippDrawLogin.this, PaymentActivity.class);
        startActivityForResult(paymentIntent, REQUEST_CODE_VIZIPP_PAY);
    }


    private void authenticateUser(final String email, final String password, final String apiKey, final String secretekey, final String OrganizationName) {

        Map<String, String> requestMap = new HashMap<String, String>();
//        requestMap.put("APITOKEN", VolleySingleton.getVizipp_APITOKEN());

        /*String SECRETKEY = "MjQ1QDEyIzJZSEQtODVEQTJTM0RFQTg1Mz1JRTVCNEE1MQ==";
        String APIKEY = "QVBAMTIjMllIRC1TREFTNUQtNUFTRksyMjEx";*/

        requestMap.put("Email", email);
        requestMap.put("Password", password);
        requestMap.put("APIKEY", apiKey);
        requestMap.put("SECRETKEY", secretekey);
        if (IsSkipPassword) {
            requestMap.put("IsSkipPassword", "1");
        }

        Log.v("loggin", requestMap.toString());
        try {
            showBusyProgress();
            String tempUrl = "https://services.edbrix.net/";
            JsonObjectRequest authenticateUserRequest = new JsonObjectRequest(Request.Method.POST, tempUrl + "auth/login", new JSONObject(requestMap), new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    hideBusyProgress();
                    Log.v(VizippDrawLogin.class.getName().toString(), "authenticateUserResp: \n" + response.toString());
                    try {
                        if (response != null) {
                            if (response.has("Success") && response.getString("Success").equals("1")) {
                                if (response.has("User")) {
                                    KnowledgeBrixUserData knowledgeBrixUserData = getGlobalMethods().toObjects(response.getJSONObject("User").toString(), KnowledgeBrixUserData.class);
                                    Log.e("1212", knowledgeBrixUserData.getUserType() + "userId" + knowledgeBrixUserData.getId());

                                    if (knowledgeBrixUserData.getUserType().equals("T") || knowledgeBrixUserData.getUserType().equals("A")) {
                                        knowledgeBrixUserData.setAccessToken(response.getString("AccessToken"));
                                        knowledgeBrixUserData.setApiKey(apiKey);
                                        knowledgeBrixUserData.setSecretekey(secretekey);

                                        Log.v(VizippDrawLogin.class.getName().toString(), " AccessToken : " + knowledgeBrixUserData.getAccessToken());
                                        Log.v(VizippDrawLogin.class.getName().toString(), " ApiKey : " + knowledgeBrixUserData.getApiKey());
                                        Log.v(VizippDrawLogin.class.getName().toString(), " Secretekey : " + knowledgeBrixUserData.getSecretekey());

                                        verifyProduct(
                                                response.getString("AccessToken"),
                                                knowledgeBrixUserData.getId(),
                                                knowledgeBrixUserData, email, password, OrganizationName);


                                    } else {
                                        googleSignInClient.signOut().addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                Log.e(VizippDrawLogin.class.getName().toString(), "Loggedout from google account");
                                            }
                                        });

                                        AlertDialog.Builder alertDialog = new AlertDialog.Builder(VizippDrawLogin.this);
                                        alertDialog.setTitle(getString(R.string.app_name));
                                        alertDialog.setMessage("Access Denied for this ISD.\nIt have access only to teacher or admin");
                                        alertDialog.setIcon(R.drawable.app_logo_round_new24);
                                        alertDialog.setPositiveButton("ok", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                            }
                                        });
                                        alertDialog.show();
                                    }

                                }
//                                setResult(RecordPreviewActivity.RESULT_OK);
//                                finish();
                            } else if (response.has("Error")) {
                                JSONObject ErrorjsonObject = response.getJSONObject("Error");
                                getAlertDialogManager().Dialog("Error Code: " + ErrorjsonObject.getString("ErrorCode"), ErrorjsonObject.getString("ErrorMessage"), new AlertDialogManager.onSingleButtonClickListner() {
                                    @Override
                                    public void onPositiveClick() {
//                                        finish();
                                    }
                                }).show();
                            }/*else if (response.has("Success") && response.getString("Success").equals("0")) {
                                getAlertDialogManager().Dialog("Error Code: " + response.getString("Code"), response.getString("Message"), new AlertDialogManager.onSingleButtonClickListner() {
                                    @Override
                                    public void onPositiveClick() {
//                                        finish();
                                    }
                                }).show();
                            }*/
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

            VolleySingleton.getInstance().addToRequestQueue(authenticateUserRequest);
        } catch (Exception e) {
            Crashlytics.logException(e);
            hideBusyProgress();
            Log.v("Volley Excep", e.getMessage());
            showToast("Something went wrong. Please try again later.");
        }

    }

    private void verifyProduct(String accessToken, String id,
                               final KnowledgeBrixUserData knowledgeBrixUserData,
                               final String email,
                               final String password,
                               final String organizationName) {

        final Map<String, Object> requestMap = new HashMap<String, Object>();
        requestMap.put("AccessToken", accessToken);
        requestMap.put("UserId", id);
        Log.e("123", "" + requestMap);

        try {
            showBusyProgress();
            String tempUrl = "https://services.edbrix.net/";
            JsonObjectRequest getuserproductaccessRequest = new JsonObjectRequest(Request.Method.POST, tempUrl + "common/getuserproductaccess", new JSONObject(requestMap),
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            hideBusyProgress();
                            try {
                                Log.e(RecordPreviewActivity.class.getName(), "" + response.getJSONArray("ProductAccessList").toString());
                                Userproductaccessresponse userproductaccessresponse = new Userproductaccessresponse();
                                userproductaccessresponse.setProductAccessList((List<ProductAccessList>) new Gson().fromJson(response.getJSONArray("ProductAccessList").toString(), new TypeToken<List<ProductAccessList>>() {
                                }.getType()));
                                Log.e(RecordPreviewActivity.class.getName(), "" + userproductaccessresponse.getProductAccessList().get(0).getTitle());
                                boolean isActive = false;
                                if (userproductaccessresponse.getProductAccessList().size() > 0) {
                                    for (int i = 0; i < userproductaccessresponse.getProductAccessList().size(); i++) {
                                        if (userproductaccessresponse.getProductAccessList().get(i).getTitle().equals("Contentbrix") &&
                                                userproductaccessresponse.getProductAccessList().get(i).getHasAccess() == 1) {
                                            isActive = true;
                                            break;
                                        } else {
                                            isActive = false;
                                        }
                                    }
                                    if (isActive) {
                                        sessionManager.updateLoggedKnoledgebrixUserData(knowledgeBrixUserData);
                                        sessionManager.updateSessionVizippUsername(email);
                                        sessionManager.updateSessionVizippPassword(password);
                                        sessionManager.updateSessionVizippToken(knowledgeBrixUserData.getAccessToken());
                                        if (!sessionManager.getSessionLastLoggedVizippUsername().isEmpty()) {
                                            if (!sessionManager.getSessionLastLoggedVizippUsername().equalsIgnoreCase(sessionManager.getSessionVizippUsername())) {
                                                sessionManager.updateVideoUploadCount(0);
                                            }
                                        }
                                        sessionManager.updateSessionLastLoggedVizippUsername(email);
                                        sessionManager.updateSessionOrgName(organizationName);
                                        startRecordActivity();
                                        ///009 Ethan
                                    } else {
                                        googleSignInClient.signOut().addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                Log.e(VizippDrawLogin.class.getName().toString(), "Loggedout from google account");
                                            }
                                        });
                                        showToast("This user is not activated for this school.");
                                    }
                                } else {
                                    googleSignInClient.signOut().addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            Log.e(VizippDrawLogin.class.getName().toString(), "Loggedout from google account");
                                        }
                                    });
                                    showToast("something went wrong");
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
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
            getuserproductaccessRequest.setRetryPolicy(VolleySingleton.getDefaultRetryPolice());
            getuserproductaccessRequest.setShouldCache(false);
            VolleySingleton.getInstance().addToRequestQueue(getuserproductaccessRequest, "getuserproductaccessRequest");
        } catch (Exception e) {
            Crashlytics.logException(e);
            hideBusyProgress();
            Log.v("Volley Excep", e.getMessage());
            showToast("Something went wrong. Please try again later.");
        }
    }

    private void authenticateUserOrganisation(final String email, final String password) {
        showBusyProgress();
        try {
            JSONObject jo = new JSONObject();
            jo.put("APIKEY", "QVBAMTIjMllIRC1TREFTNUQtNUFTRksyMjEhZWRicml4QDE4");
            jo.put("SECRETKEY", "MjQ1QDEyIzJZSEQtODVEQTJTM0RFQTg1Mz1JRTVCNEE1IWVkYnJpeEAxOA==");
            jo.put("Email", email);

            String tempUrl = "https://services.edbrix.net/";
            GsonRequest<AuthenticateUserOrganisationResponse> getDashboardCourseSchedulesRequest = new GsonRequest<>(Request.Method.POST, tempUrl + "auth/authorganizationlistbyuseremail", jo.toString(), AuthenticateUserOrganisationResponse.class,
                    new Response.Listener<AuthenticateUserOrganisationResponse>() {
                        @Override
                        public void onResponse(@NonNull final AuthenticateUserOrganisationResponse response) {
                            hideBusyProgress();
                            Log.e(VizippDrawLogin.class.getName(), "" + response.toString());
                            //Log.e(VizippDrawLogin.class.getName(), "" + response.getUserOrganizationList().size());

                            if (response != null) {
                                if (response.getSuccess() != null && response.getSuccess() == 1) {

                                    sessionManager.updateSessionVizippUsername(email);
                                    sessionManager.updateSessionVizippPassword(password);

                                    ArrayList<UserOrganizationList> listData = new ArrayList<UserOrganizationList>();
                                    for (int i = 0; i < response.getUserOrganizationList().size(); i++) {
                                        listData.add(response.getUserOrganizationList().get(i));
                                    }
                                    finish();
                                    Intent intent = new Intent(VizippDrawLogin.this, OrgnizationListActivity.class);
                                    intent.putExtra("organizationList", listData);
                                    intent.putExtra("comesFrom", "loginActivity");
                                    startActivityForResult(intent, RESULT_LOGIN);

                                    /*by 0008
                                    if (response.getUserOrganizationList().size() == 1) {
                                        authenticateUser(email,
                                                password,
                                                response.getUserOrganizationList().get(0).getApiKey(),
                                                response.getUserOrganizationList().get(0).getSecretekey(),
                                                response.getUserOrganizationList().get(0).getOrganizationName());
                                    } else {
                                        final Dialog dialog = new Dialog(VizippDrawLogin.this);
                                        View view = getLayoutInflater().inflate(R.layout.dialog_main, null);
                                        ImageView btn_close = (ImageView) view.findViewById(R.id.button_Close);
                                        ListView lv = (ListView) view.findViewById(R.id.custom_list);

                                        ArrayList<UserOrganizationList> listData = new ArrayList<UserOrganizationList>();
                                        for (int i = 0; i < response.getUserOrganizationList().size(); i++) {
                                            listData.add(response.getUserOrganizationList().get(i));
                                        }
                                        CustomListAdapterDialog clad = new CustomListAdapterDialog(VizippDrawLogin.this, listData);
                                        lv.setAdapter(clad);
                                        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                            @Override
                                            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                                authenticateUser(email,
                                                        password,
                                                        response.getUserOrganizationList().get(position).getApiKey(),
                                                        response.getUserOrganizationList().get(position).getSecretekey(),
                                                        response.getUserOrganizationList().get(position).getOrganizationName());
                                            }
                                        });
                                        dialog.setContentView(view);
                                        dialog.show();
                                        btn_close.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                dialog.cancel();
                                            }
                                        });
                                    }*/
                                } else {
                                    showToast("Invalid User");
                                }
                            }

                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    hideBusyProgress();
                    showToast(VolleySingleton.getErrorMessage(error));
                }
            });
            getDashboardCourseSchedulesRequest.setRetryPolicy(VolleySingleton.getDefaultRetryPolice());
            getDashboardCourseSchedulesRequest.setShouldCache(false);
            VolleySingleton.getInstance().addToRequestQueue(getDashboardCourseSchedulesRequest, "getdashboardcourseandschedules");
        } catch (JSONException e) {
            Crashlytics.logException(e);
            hideBusyProgress();
            showToast("Something went wrong. Please try again later.");
        }
    }
}
