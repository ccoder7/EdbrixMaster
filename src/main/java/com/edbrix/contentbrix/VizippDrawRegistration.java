package com.edbrix.contentbrix;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import androidx.core.content.ContextCompat;
import android.text.Html;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
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
import com.edbrix.contentbrix.data.OrganizationsData;
import com.edbrix.contentbrix.data.VizippUserData;
import com.edbrix.contentbrix.utils.SessionManager;
import com.edbrix.contentbrix.volley.VolleySingleton;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VizippDrawRegistration extends BaseActivity {

    private EditText edtEmailUNM;
    private EditText edtConfirmEmail;
    private EditText edtLastName;
    private EditText edtFirstName;
    private EditText edtCreatePwd;
    private EditText edtCoupon;
    private EditText edtPhone;
    private Button btnRegister;
    private TextView btnLogin;
    private TextView glLogin;
    private TextView schlLogin;
    private TextView loginTitle;
    private TextView textTrmConditions;
    private List<OrganizationsData> organizationsDataList;
    private SessionManager sessionManager;
    private String orgId;
    //    private boolean isForgotPwd;
    private LinearLayout loginTypeLayout;
    private LinearLayout pwdLayout;
    private ImageView backBtn;
    private ImageView eyeIcon;
    private boolean isPasswordVisible;
    private CheckBox agreedTerms;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vizipp_draw_registration);
        if (!isTablet()) {
            // stop screen rotation on phones
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
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
        edtEmailUNM = (EditText) findViewById(R.id.edtEmail);
        edtConfirmEmail = (EditText) findViewById(R.id.edtConfirmEmail);
        edtFirstName = (EditText) findViewById(R.id.edtFirstName);
        edtLastName = (EditText) findViewById(R.id.edtLastName);
        edtCreatePwd = (EditText) findViewById(R.id.edtCreatePwd);
        edtCoupon = (EditText) findViewById(R.id.edtCoupon);
        edtPhone = (EditText) findViewById(R.id.edtPhone);
        btnRegister = (Button) findViewById(R.id.btnRegister);
        btnLogin = (TextView) findViewById(R.id.txtLogin);
        glLogin = (TextView) findViewById(R.id.glLogin);
        schlLogin = (TextView) findViewById(R.id.schlLogin);
        loginTitle = (TextView) findViewById(R.id.loginTitle);
        backBtn = (ImageView) findViewById(R.id.backBtn);
        eyeIcon = (ImageView) findViewById(R.id.eyeIcon);
        loginTypeLayout = (LinearLayout) findViewById(R.id.loginTypeLayout);
        pwdLayout = (LinearLayout) findViewById(R.id.pwdLayout);
        agreedTerms = (CheckBox) findViewById(R.id.checkBxAgreed);
        textTrmConditions = (TextView) findViewById(R.id.textTrmConditions);
        String htmlString = "I accept <font color='blue'><u>Terms & Conditions</u></font>.";
        textTrmConditions.setText(Html.fromHtml(htmlString));
        getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN
        );
        isPasswordVisible = false;
        edtCreatePwd.setTransformationMethod(new PasswordTransformationMethod());
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
        this.edtEmailUNM.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                edtEmailUNM.setCursorVisible(true);
                return false;
            }
        });
        this.edtFirstName.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                edtFirstName.setCursorVisible(true);
                return false;
            }
        });

        this.edtLastName.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                edtLastName.setCursorVisible(true);
                return false;
            }
        });

        this.edtCreatePwd.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                edtCreatePwd.setCursorVisible(true);
                return false;
            }
        });
        this.edtConfirmEmail.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                edtConfirmEmail.setCursorVisible(true);
                return false;
            }
        });
        this.edtCoupon.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                edtCoupon.setCursorVisible(true);
                return false;
            }
        });
        this.edtPhone.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                edtPhone.setCursorVisible(true);
                return false;
            }
        });


        this.edtCreatePwd.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    // hide virtual keyboard
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(edtCreatePwd.getWindowToken(), 0);
                    //doLogin
                    if (validate()) {
                        //doLogin
                        String email = edtEmailUNM.getText().toString().trim();
                        String firstName = edtFirstName.getText().toString().trim();
                        String lastName = edtLastName.getText().toString().trim();
                        String pwd = edtCreatePwd.getText().toString().trim();
                        String phone = edtPhone.getText().toString().trim();
                        String referralCode = edtCoupon.getText().toString().trim();
                        registerUser(firstName, lastName, email, pwd, phone, referralCode);
                    }
                    return true;
                }
                return false;
            }
        });

        this.btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(edtCreatePwd.getWindowToken(), 0);
                edtEmailUNM.setBackgroundResource(R.drawable.black_border);
                edtConfirmEmail.setBackgroundResource(R.drawable.black_border);
                edtFirstName.setBackgroundResource(R.drawable.black_border);
                edtLastName.setBackgroundResource(R.drawable.black_border);
                pwdLayout.setBackgroundResource(R.drawable.black_border);
                edtCoupon.setBackgroundResource(R.drawable.black_border);
                edtPhone.setBackgroundResource(R.drawable.black_border);
                if (validate()) {
                    edtEmailUNM.setBackgroundResource(R.drawable.black_border);
                    edtConfirmEmail.setBackgroundResource(R.drawable.black_border);
                    edtFirstName.setBackgroundResource(R.drawable.black_border);
                    edtLastName.setBackgroundResource(R.drawable.black_border);
                    pwdLayout.setBackgroundResource(R.drawable.black_border);
                    edtCoupon.setBackgroundResource(R.drawable.black_border);
                    edtPhone.setBackgroundResource(R.drawable.black_border);
                    String email = edtEmailUNM.getText().toString().trim();
                    String firstName = edtFirstName.getText().toString().trim();
                    String lastName = edtLastName.getText().toString().trim();
                    String pwd = edtCreatePwd.getText().toString().trim();
                    String phone = edtPhone.getText().toString().trim();
                    String referralCode = edtCoupon.getText().toString().trim();
                    registerUser(firstName, lastName, email, pwd, phone, referralCode);
                }
            }
        });

        this.btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent loginIntent = new Intent(VizippDrawRegistration.this, VizippDrawLogin.class);
                startActivity(loginIntent);
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

        this.textTrmConditions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent termsConditionIntent = new Intent(VizippDrawRegistration.this, TermsConditionPolicyViewActivity.class);
                termsConditionIntent.putExtra("pagelink", "file:///android_asset/terms_condition.html");
                startActivity(termsConditionIntent);
            }
        });
    }

    private void setPasswordVisible(boolean isVisible) {
        if (isVisible) {
            eyeIcon.setImageDrawable(ContextCompat.getDrawable(VizippDrawRegistration.this, R.drawable.eye_invisible));
            edtCreatePwd.setTransformationMethod(new PasswordTransformationMethod());
        } else {
            eyeIcon.setImageDrawable(ContextCompat.getDrawable(VizippDrawRegistration.this, R.drawable.eye_visible));

            edtCreatePwd.setTransformationMethod(null);
        }
    }

    /*
    do validation
     */
    private boolean validate() {
        boolean bln = true;
        String email = this.edtEmailUNM.getText().toString().trim();
        String confirmEmail = this.edtConfirmEmail.getText().toString().trim();
        String firstNm = this.edtFirstName.getText().toString().trim();
        String lastNm = this.edtLastName.getText().toString().trim();
        String pwd = this.edtCreatePwd.getText().toString().trim();
        String phone = this.edtPhone.getText().toString().trim();

        if (firstNm.length() == 0) {
            edtFirstName.setBackgroundResource(R.drawable.red_border);
            edtFirstName.setHint(getResources().getString(R.string.firstname_blank));
            // getAlertDialogManager().Dialog(getResources().getString(R.string.error), getResources().getString(R.string.password_blank), true, null).show();
            bln = false;
        } else if (lastNm.length() == 0) {
            edtLastName.setBackgroundResource(R.drawable.red_border);
            edtLastName.setHint(getResources().getString(R.string.lastname_blank));
            // getAlertDialogManager().Dialog(getResources().getString(R.string.error), getResources().getString(R.string.password_blank), true, null).show();
            bln = false;
        } else if (phone.length() == 0) {
            edtPhone.setBackgroundResource(R.drawable.red_border);
            edtPhone.setHint(getResources().getString(R.string.mobile_no_blank));
            // getAlertDialogManager().Dialog(getResources().getString(R.string.error), getResources().getString(R.string.password_blank), true, null).show();
            bln = false;
        }else if (email.length() == 0) {
            edtEmailUNM.setHint(getResources().getString(R.string.username_blank));
            edtEmailUNM.setBackgroundResource(R.drawable.red_border);
            //getAlertDialogManager().Dialog(getResources().getString(R.string.error), getResources().getString(R.string.username_blank), true, null).show();
            bln = false;
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            edtEmailUNM.setError(getResources().getString(R.string.invalid_email));
            edtEmailUNM.setBackgroundResource(R.drawable.red_border);
//            getAlertDialogManager().Dialog(getResources().getString(R.string.error), getResources().getString(R.string.invalid_email), true, null).show();
            bln = false;
        } else if (confirmEmail.length() == 0) {
            edtConfirmEmail.setHint(getResources().getString(R.string.username_blank));
            edtConfirmEmail.setBackgroundResource(R.drawable.red_border);
            //getAlertDialogManager().Dialog(getResources().getString(R.string.error), getResources().getString(R.string.username_blank), true, null).show();
            bln = false;
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(confirmEmail).matches()) {
            edtConfirmEmail.setError(getResources().getString(R.string.invalid_email));
            edtConfirmEmail.setBackgroundResource(R.drawable.red_border);
//            getAlertDialogManager().Dialog(getResources().getString(R.string.error), getResources().getString(R.string.invalid_email), true, null).show();
            bln = false;
        } else if (!email.contentEquals(confirmEmail)) {
            edtEmailUNM.setBackgroundResource(R.drawable.red_border);
            edtConfirmEmail.setBackgroundResource(R.drawable.red_border);
            getAlertDialogManager().Dialog(getResources().getString(R.string.error), getResources().getString(R.string.emails_not_match), true, null).show();
            bln = false;
        } else if (pwd.length() == 0) {
            edtCreatePwd.setHint(getResources().getString(R.string.password_blank));
            pwdLayout.setBackgroundResource(R.drawable.red_border);
            //getAlertDialogManager().Dialog(getResources().getString(R.string.error), getResources().getString(R.string.username_blank), true, null).show();
            bln = false;
        } else if (pwd.length() < 6) {
            pwdLayout.setBackgroundResource(R.drawable.red_border);
            getAlertDialogManager().Dialog(getResources().getString(R.string.error), getResources().getString(R.string.password_min_char), true, null).show();
            bln = false;
        } else if (!agreedTerms.isChecked()) {
            agreedTerms.setTextColor(ContextCompat.getColor(VizippDrawRegistration.this, R.color.ColorRed));
            agreedTerms.setFocusable(true);
            getAlertDialogManager().Dialog(getResources().getString(R.string.app_name), getResources().getString(R.string.please_accept_terms), true, null).show();
            bln = false;
        }
        return bln;
    }

    private void registerUser(final String firstName, final String lastName, final String email, final String password, final String phone, final String referralCode) {

        Map<String, String> requestMap = new HashMap<String, String>();
        requestMap.put("email", email);
        requestMap.put("password", password);
        requestMap.put("firstName", firstName);
        requestMap.put("lastName", lastName);
        requestMap.put("phone", phone);
        requestMap.put("referralCode", referralCode);
//        requestMap.put("APITOKEN", VolleySingleton.getVizipp_APITOKEN());

        try {
            showBusyProgress();
            JsonObjectRequest userRegistrationRequest = new JsonObjectRequest(Request.Method.POST, VolleySingleton.getWsBaseUrl() + "registeruser.php", new JSONObject(requestMap), new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(final JSONObject response) {
                    hideBusyProgress();
//                    Log.v("Volley Response", response.toString());
                    try {
                        if (response != null) {
                            if (response.has("status") && response.getString("status").equals("1")) {
                                if (response.has("userData")) {
                                    VizippUserData vizippUserData = getGlobalMethods().toObjects(response.getJSONObject("userData").toString(), VizippUserData.class);
                                    //sm//sessionManager.updateLoggedVizippUserData(vizippUserData);
                                    sessionManager.updateSessionVizippUsername(email);
                                    sessionManager.updateSessionVizippPassword(password);
                                    sessionManager.updateSessionVizippToken(vizippUserData.getAPITOKEN());
//                                    showToast(vizippUserData.getFirstName());
                                    if (vizippUserData.isPasswordReset()) {
                                        //Go to change password later successfully change password update password and isPasswordReset=0 in app session's user object
                                        Intent changePwdIntent = new Intent(VizippDrawRegistration.this, VizippDrawChangePassword.class);
                                        startActivityForResult(changePwdIntent, VizippDrawLogin.REQUEST_CODE_CHANGE_PWD);
                                        showToast("Need to change password..");
                                    } else if (!vizippUserData.isSubscribed()) {
                                        //Login successfull...
                                        showToast(response.getString("message"));
                                        moveForwardToPayment();
                                    } else {
                                        setResult(RecordPreviewActivity.RESULT_OK);
                                        finish();
                                    }
                                }
//                                showToast(response.getString("message"));
//                                setResult(RecordPreviewActivity.RESULT_OK);
//                                finish();
                            } else if (response.has("status") && response.getString("status").equals("0")) {

                                if (response.has("errorCode") && response.getString("errorCode").equals("103")) {
                                    if (response.has("userId")) {
                                        final String userId = response.getString("userId");
                                        getAlertDialogManager().Dialog("Error Code: " + response.getString("errorCode"), response.getString("message"), "OK", "Resend Email", false, new AlertDialogManager.onTwoButtonClickListner() {
                                            @Override
                                            public void onNegativeClick() {
                                                resendVerificationEmail(userId, sessionManager.getPrefsSessionVizippToken());
                                            }

                                            @Override
                                            public void onPositiveClick() {
//                                                finish();
                                            }
                                        }).show();
                                    }
                                } else {
                                    getAlertDialogManager().Dialog("Error Code: " + response.getString("errorCode"), response.getString("message"), new AlertDialogManager.onSingleButtonClickListner() {
                                        @Override
                                        public void onPositiveClick() {
//                                            finish();
                                        }
                                    }).show();
                                }
                            }
                        } else {
                            showToast("Response : Null");
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

            VolleySingleton.getInstance().addToRequestQueue(userRegistrationRequest);
        } catch (Exception e) {
            Crashlytics.logException(e);
            hideBusyProgress();
            Log.v("Volley Excep", e.getMessage());
            showToast("Something went wrong. Please try again later.");
        }

    }


    private void resendVerificationEmail(final String userId, final String apiToken) {

        Map<String, String> requestMap = new HashMap<String, String>();
        requestMap.put("APITOKEN", apiToken);
        requestMap.put("userId", userId);


        try {
            showBusyProgress();
            JsonObjectRequest resendVerificationEmailRequest = new JsonObjectRequest(Request.Method.POST, VolleySingleton.getWsBaseUrl() + "resenduserverificationemail.php", new JSONObject(requestMap), new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    hideBusyProgress();
//                    Log.v("Volley Response", response.toString());
                    try {
                        if (response != null) {
                            if (response.has("status") && response.getString("status").equals("1")) {
//                                showToast(response.getString("message"));
//                                setResult(RecordPreviewActivity.RESULT_OK);
//                                finish();
                                getAlertDialogManager().Dialog(response.getString("message"), new AlertDialogManager.onSingleButtonClickListner() {
                                    @Override
                                    public void onPositiveClick() {
                                        finish();
                                    }
                                }).show();
                            } else if (response.has("status") && response.getString("status").equals("0")) {
                                getAlertDialogManager().Dialog("Error Code: " + response.getString("errorCode"), response.getString("message"), new AlertDialogManager.onSingleButtonClickListner() {
                                    @Override
                                    public void onPositiveClick() {
                                        finish();
                                    }
                                }).show();
                            }

                        } else {
                            showToast("Response : Null");
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

            VolleySingleton.getInstance().addToRequestQueue(resendVerificationEmailRequest);
        } catch (Exception e) {
            Crashlytics.logException(e);
            hideBusyProgress();
            Log.v("Volley Excep", e.getMessage());
            showToast("Something went wrong. Please try again later.");
        }

    }

    /**
     * Receiving activity result
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case VizippDrawLogin.REQUEST_CODE_VIZIPP_PAY:
                setResult(RESULT_OK);
                finish();
                break;
            case VizippDrawLogin.REQUEST_CODE_CHANGE_PWD:
                if (resultCode == RESULT_OK)
                {
                    if (sessionManager.getLoggedKnowledgeBrixUserData() != null)
                    {
                        setResult(RESULT_OK);
                        finish();
                    }
                } else {

                }

                break;
        }
    }

    private void moveForwardToPayment() {
        // move toward payment
        Intent paymentIntent = new Intent(VizippDrawRegistration.this, PaymentActivity.class);
        startActivityForResult(paymentIntent, VizippDrawLogin.REQUEST_CODE_VIZIPP_PAY);
    }
}
