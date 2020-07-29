package com.edbrix.contentbrix;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
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
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.crashlytics.android.Crashlytics;
import com.edbrix.contentbrix.baseclasses.BaseActivity;
import com.edbrix.contentbrix.commons.AlertDialogManager;
import com.edbrix.contentbrix.utils.SessionManager;
import com.edbrix.contentbrix.volley.VolleySingleton;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class VizippDrawChangePassword extends BaseActivity {

    private EditText edtNewPwd;
    private EditText edtConfmPwd;
    private Button btnSubmit;
    private TextView btnRegister;
    private TextView btnLogin;
    private SessionManager sessionManager;
    private ImageView backBtn;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);
        if (!isTablet()) {
            // stop screen rotation on phones
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
        sessionManager = new SessionManager(this);
        if (sessionManager.getLoggedKnowledgeBrixUserData() != null && sessionManager.getLoggedKnowledgeBrixUserData().getId() != null) {
            userId = sessionManager.getLoggedKnowledgeBrixUserData().getId();
        } else {
            showToast("Something went wrong. Please try again later.");
            finish();
        }

        //initialize component
        initComponent();
        //set listeners to component
        setListeners();
    }

    /*
        initialise component
    */
    private void initComponent() {
        edtNewPwd = (EditText) findViewById(R.id.edtNewPwd);
        edtConfmPwd = (EditText) findViewById(R.id.edtConfmPwd);
        btnSubmit = (Button) findViewById(R.id.btnSubmit);
        btnRegister = (TextView) findViewById(R.id.txtRegister);
        btnLogin = (TextView) findViewById(R.id.txtLogin);
        backBtn = (ImageView) findViewById(R.id.backBtn);
        getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN
        );
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

        this.edtNewPwd.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                edtNewPwd.setCursorVisible(true);
                return false;
            }
        });

        this.edtConfmPwd.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                edtConfmPwd.setCursorVisible(true);
                return false;
            }
        });

        this.edtConfmPwd.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    // hide virtual keyboard
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(edtConfmPwd.getWindowToken(), 0);
                    edtNewPwd.setBackgroundResource(R.drawable.black_border);
                    edtConfmPwd.setBackgroundResource(R.drawable.black_border);
                    //doLogin
                    if (validate()) {
                        //doLogin
                        edtNewPwd.setBackgroundResource(R.drawable.black_border);
                        edtConfmPwd.setBackgroundResource(R.drawable.black_border);
                        String password = edtNewPwd.getText().toString().trim();
                        String confirmPwd = edtConfmPwd.getText().toString().trim();
                        changePassword(userId, password, confirmPwd, sessionManager.getPrefsSessionVizippToken());
                    }
                    return true;
                }
                return false;
            }
        });

        this.btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(edtConfmPwd.getWindowToken(), 0);
                edtNewPwd.setBackgroundResource(R.drawable.black_border);
                edtConfmPwd.setBackgroundResource(R.drawable.black_border);
                if (validate()) {
                    edtNewPwd.setBackgroundResource(R.drawable.black_border);
                    edtConfmPwd.setBackgroundResource(R.drawable.black_border);
                    String password = edtNewPwd.getText().toString().trim();
                    String confirmPwd = edtConfmPwd.getText().toString().trim();
                    changePassword(userId, password, confirmPwd, sessionManager.getPrefsSessionVizippToken());
                }
            }
        });

        this.btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*String url = "http://foundation.flipclass.com/page/instructorregister?otherpage=1&public=1";
                Intent registerIntent = new Intent(Intent.ACTION_VIEW);
                registerIntent.setData(Uri.parse(url));
                startActivity(registerIntent);
                finish();*/
            }
        });

        this.btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*String url = "http://foundation.flipclass.com/page/instructorregister?otherpage=1&public=1";
                Intent registerIntent = new Intent(Intent.ACTION_VIEW);
                registerIntent.setData(Uri.parse(url));
                startActivity(registerIntent);
                finish();*/
            }
        });

    }

    /*
    do validation
     */
    private boolean validate() {
        boolean bln = true;
        String password = this.edtNewPwd.getText().toString().trim();
        String confirmPwd = this.edtConfmPwd.getText().toString().trim();
        if (password.length() == 0) {
            edtNewPwd.setBackgroundResource(R.drawable.red_border);
            edtNewPwd.setHint(getResources().getString(R.string.password_blank));
            // getAlertDialogManager().Dialog(getResources().getString(R.string.error), getResources().getString(R.string.password_blank), true, null).show();
            bln = false;
        } else if (confirmPwd.length() == 0) {
            edtConfmPwd.setBackgroundResource(R.drawable.red_border);
            edtConfmPwd.setHint(getResources().getString(R.string.confirm_password_blank));
            // getAlertDialogManager().Dialog(getResources().getString(R.string.error), getResources().getString(R.string.password_blank), true, null).show();
            bln = false;
        } else if (!password.equals(confirmPwd)) {
            edtNewPwd.setBackgroundResource(R.drawable.red_border);
            edtConfmPwd.setBackgroundResource(R.drawable.red_border);
//            edtRetypePwd.setHint(getResources().getString(R.string.password_not_match));
            getAlertDialogManager().Dialog(getResources().getString(R.string.error), getResources().getString(R.string.password_not_match), false, null).show();
            bln = false;
        }
        return bln;
    }

    private void changePassword(final String userId, final String password, final String confirmPwd, final String apiToken) {

        Map<String, String> requestMap = new HashMap<String, String>();
        requestMap.put("UserId", userId);
        requestMap.put("NewPassword", password);
        requestMap.put("ConfirmPassword", confirmPwd);
        requestMap.put("AccessToken", apiToken);

        try {
            showBusyProgress();
            String tempUrl = "https://services.edbrix.net/";
            JsonObjectRequest changePasswordRequest = new JsonObjectRequest(Request.Method.POST, tempUrl+"auth/changepassword", new JSONObject(requestMap), new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    hideBusyProgress();
//                    Log.v("Volley Response", response.toString());
                    try {
                        if (response != null)
                        {
                            if (response.has("Success") && response.getString("Success").equals("1")) {
                                if (sessionManager.getLoggedKnowledgeBrixUserData() != null)
                                {
                                    //sessionManager.getLoggedKnowledgeBrixUserData().setPasswordReset(false);
                                    sessionManager.updateSessionVizippPassword(confirmPwd);
                                    //set result back and go for further
                                    setResult(RESULT_OK);
                                    finish();
                                }
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

            VolleySingleton.getInstance().addToRequestQueue(changePasswordRequest);
        } catch (Exception e) {
            Crashlytics.logException(e);
            hideBusyProgress();
            Log.v("Volley Excep", e.getMessage());
            showToast("Something went wrong. Please try again later.");
        }

    }


}
