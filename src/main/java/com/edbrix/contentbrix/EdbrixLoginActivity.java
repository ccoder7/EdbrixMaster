package com.edbrix.contentbrix;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Bundle;
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
import com.edbrix.contentbrix.data.OrganizationsData;
import com.edbrix.contentbrix.data.UserData;
import com.edbrix.contentbrix.utils.SessionManager;
import com.edbrix.contentbrix.volley.VolleySingleton;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class EdbrixLoginActivity extends BaseActivity {

    private EditText edtEmail;
    private EditText edtPwd;
    private Button btnLogin;
    private TextView btnRegister;
    private TextView glLogin;
    private TextView schlLogin;
    private TextView loginTitle;
    private List<OrganizationsData> organizationsDataList;
    private SessionManager sessionManager;
    private String orgId;
    private boolean isForgotPwd;
    private LinearLayout loginTypeLayout;
    private ImageView backBtn;
    private ImageView eyeIcon;
    private LinearLayout pwdLayout;
    private boolean isPasswordVisible;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!isTablet()) {
            // stop screen rotation on phones
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
        setContentView(R.layout.activity_edbrix_login);
        sessionManager = new SessionManager(this);
        //initialize component
        initComponent();
        //set listeners to component
        setListeners();
    }

    /*
   initialise component
    */
    private void initComponent() {
        edtEmail = (EditText) findViewById(R.id.edtEmail);
        edtPwd = (EditText) findViewById(R.id.edtPwd);
        btnLogin = (Button) findViewById(R.id.btnLogin);
        btnRegister = (TextView) findViewById(R.id.txtRegister);
        glLogin = (TextView) findViewById(R.id.glLogin);
        schlLogin = (TextView) findViewById(R.id.schlLogin);
        loginTitle = (TextView) findViewById(R.id.loginTitle);
        backBtn = (ImageView) findViewById(R.id.backBtn);
        loginTypeLayout = (LinearLayout) findViewById(R.id.loginTypeLayout);
        eyeIcon = (ImageView) findViewById(R.id.eyeIcon);
        pwdLayout = (LinearLayout) findViewById(R.id.pwdLayout);
        getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN
        );
        isPasswordVisible = false;
        edtPwd.setTransformationMethod(new PasswordTransformationMethod());
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
                        authenticateInstructor(email, password);
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
                if (validate()) {
                    edtEmail.setBackgroundResource(R.drawable.black_border);
                    pwdLayout.setBackgroundResource(R.drawable.black_border);
                    String email = edtEmail.getText().toString().trim();
                    String password = edtPwd.getText().toString().trim();
                    authenticateInstructor(email, password);
                }
            }
        });

        this.btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = "http://foundation.flipclass.com/page/instructorregister?otherpage=1&public=1";
                Intent registerIntent = new Intent(Intent.ACTION_VIEW);
                registerIntent.setData(Uri.parse(url));
                startActivity(registerIntent);
                finish();
            }
        });

        this.eyeIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setPasswordVisible(isPasswordVisible);
                isPasswordVisible = !isPasswordVisible;
            }
        });

    }

    private void setPasswordVisible(boolean isVisible) {
        if (isVisible) {
            eyeIcon.setImageDrawable(ContextCompat.getDrawable(EdbrixLoginActivity.this, R.drawable.eye_invisible));
            edtPwd.setTransformationMethod(new PasswordTransformationMethod());
        } else {
            eyeIcon.setImageDrawable(ContextCompat.getDrawable(EdbrixLoginActivity.this, R.drawable.eye_visible));
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
            edtEmail.setHint(getResources().getString(R.string.username_blank));
            edtEmail.setBackgroundResource(R.drawable.red_border);
            //getAlertDialogManager().Dialog(getResources().getString(R.string.error), getResources().getString(R.string.username_blank), true, null).show();
            bln = false;
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            edtEmail.setBackgroundResource(R.drawable.red_border);
            getAlertDialogManager().Dialog(getResources().getString(R.string.error), getResources().getString(R.string.invalid_email), true, null).show();
            bln = false;
        } else if (!isForgotPwd && password.length() == 0) {
            pwdLayout.setBackgroundResource(R.drawable.red_border);
            edtPwd.setHint(getResources().getString(R.string.password_blank));
            // getAlertDialogManager().Dialog(getResources().getString(R.string.error), getResources().getString(R.string.password_blank), true, null).show();
            bln = false;
        }
        return bln;
    }

    private void authenticateInstructor(final String email, final String password) {

        Map<String, String> requestMap = new HashMap<String, String>();
        requestMap.put("Email", email);
        requestMap.put("Password", password);

        try {
            showBusyProgress();
            JsonObjectRequest authenticateInstructorRequest = new JsonObjectRequest(Request.Method.POST, VolleySingleton.getEdbrixServerUrl() + "authenticateinstructor.php", new JSONObject(requestMap), new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    hideBusyProgress();
                    Log.v("Volley Response", response.toString());
                    try {
                        if (response != null) {
                            if (response.has("Success")) {
                                JSONObject successObj = response.getJSONObject("Success");
                                if (successObj.has("User")) {
                                    UserData userData = getGlobalMethods().toObjects(successObj.get("User").toString(), UserData.class);
                                    if (userData != null) {
                                        sessionManager.updateLoggedUserData(userData);
                                        sessionManager.updateSessionUsername(email);
                                        sessionManager.updateSessionPassword(password);
                                        sessionManager.updateSessionOrgID(userData.getOrganizationId());
                                    }
                                }
                                if (successObj.has("AccessToken")) {
                                    sessionManager.updateSessionProfileToken(successObj.getString("AccessToken"));
                                }

                                showToast("Login Successfully..");
                                setResult(RecordPreviewActivity.RESULT_OK);
                                finish();
                            } else if (response.has("Error")) {
                                //{"Error":[{"ErrorCode":"E1003","ErrorMessage":"Invalid Username or Password."}]}
                                JSONArray errorArrayObj = response.getJSONArray("Error");
                                JSONObject errorObj = errorArrayObj.getJSONObject(0);
                                showToast(errorObj.getString("ErrorMessage"));
                            }
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

            VolleySingleton.getInstance().addToRequestQueue(authenticateInstructorRequest);
        } catch (Exception e) {
            Crashlytics.logException(e);
            hideBusyProgress();
            Log.v("Volley Excep", e.getMessage());
            showToast("Something went wrong. Please try again later.");
        }
    }


}
