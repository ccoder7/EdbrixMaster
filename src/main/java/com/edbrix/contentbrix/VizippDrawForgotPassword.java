package com.edbrix.contentbrix;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import androidx.annotation.NonNull;
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
import com.edbrix.contentbrix.data.AuthenticateUserOrganisationResponse;
import com.edbrix.contentbrix.data.OrganizationsData;
import com.edbrix.contentbrix.data.UserOrganizationList;
import com.edbrix.contentbrix.utils.SessionManager;
import com.edbrix.contentbrix.volley.GsonRequest;
import com.edbrix.contentbrix.volley.VolleySingleton;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VizippDrawForgotPassword extends BaseActivity {

    private EditText edtEmail;
    private Button btnSubmit;
    private TextView btnRegister;
    private TextView btnLogin;
    private TextView glLogin;
    private TextView schlLogin;
    private TextView loginTitle;
    private List<OrganizationsData> organizationsDataList;
    private SessionManager sessionManager;
    private String orgId;
    //    private boolean isForgotPwd;
    private LinearLayout loginTypeLayout;
    private ImageView backBtn;
    public static final int RESULT_FORGOT_PASSWORD = 4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);
        if (isTablet()) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE); // stop screen rotation on phones
        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
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
        edtEmail = (EditText) findViewById(R.id.edtEmail);

        btnSubmit = (Button) findViewById(R.id.btnSubmit);
        btnRegister = (TextView) findViewById(R.id.txtRegister);
        btnLogin = (TextView) findViewById(R.id.txtLogin);
        glLogin = (TextView) findViewById(R.id.glLogin);
        schlLogin = (TextView) findViewById(R.id.schlLogin);
        loginTitle = (TextView) findViewById(R.id.loginTitle);
        backBtn = (ImageView) findViewById(R.id.backBtn);
        loginTypeLayout = (LinearLayout) findViewById(R.id.loginTypeLayout);
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
        this.edtEmail.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                edtEmail.setCursorVisible(true);
                return false;
            }
        });


        this.edtEmail.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    // hide virtual keyboard
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(edtEmail.getWindowToken(), 0);
                    //doLogin
                    if (validate()) {
                        //doLogin
                        String email = edtEmail.getText().toString().trim();
                        //forgotPassword(email);
                        authenticateUserOrganisation(email);
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
                imm.hideSoftInputFromWindow(edtEmail.getWindowToken(), 0);
                if (validate()) {
                    edtEmail.setBackgroundResource(R.drawable.black_border);
                    String email = edtEmail.getText().toString().trim();
                    //forgotPassword(email);
                    authenticateUserOrganisation(email);
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
        String email = this.edtEmail.getText().toString().trim();

        if (email.length() == 0) {
            edtEmail.setHint(getResources().getString(R.string.username_blank));
            edtEmail.setError(getResources().getString(R.string.username_blank));
            edtEmail.setBackgroundResource(R.drawable.red_border);
            //getAlertDialogManager().Dialog(getResources().getString(R.string.error), getResources().getString(R.string.username_blank), true, null).show();
            bln = false;
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            edtEmail.setBackgroundResource(R.drawable.red_border);
            edtEmail.setError(getResources().getString(R.string.invalid_email));
//            getAlertDialogManager().Dialog(getResources().getString(R.string.error), getResources().getString(R.string.invalid_email), false, null).show();
            bln = false;
        }
        return bln;
    }

    private void forgotPassword(final String email, final String apiKey, final String secretKey) {

        Map<String, String> requestMap = new HashMap<String, String>();

        requestMap.put("Email", email);
        requestMap.put("APIKEY", apiKey);
        requestMap.put("SECRETKEY", secretKey);

        /*requestMap.put("APIKEY", "QVBAMTIjMllIRC1TREFTNUQtNUFTRksyMjEx");
        requestMap.put("SECRETKEY", "MjQ1QDEyIzJZSEQtODVEQTJTM0RFQTg1Mz1JRTVCNEE1MQ==");*/
//        requestMap.put("APITOKEN", VolleySingleton.getVizipp_APITOKEN());

        try {
            showBusyProgress();
            String tempUrl = "https://services.edbrix.net/";
            JsonObjectRequest forgotPasswordRequest = new JsonObjectRequest(Request.Method.POST, tempUrl + "auth/resetpassword", new JSONObject(requestMap), new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    hideBusyProgress();
                    Log.v(VizippDrawForgotPassword.class.getName(), response.toString());
                    try {
                        if (response != null && response.has("Success")) {
                            if (response.getString("Success").equals("1")) {
                                showToast(response.getString("Message"));
                                setResult(RESULT_OK);
                                finish();
                            } else {
                                showToast(response.getString("Message"));
                            }
                        }
                    } catch (Exception e) {
                        Crashlytics.logException(e);
                        e.printStackTrace();
                        Log.v("Volley Excep", e.getMessage());
                        showToast("Something went wrong. Please try again later.");
                    }
//                    {"status":"1","errorCode":"0","message":"Password recovery successful. Please check your email."}
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

            VolleySingleton.getInstance().addToRequestQueue(forgotPasswordRequest);
        } catch (Exception e) {
            Crashlytics.logException(e);
            hideBusyProgress();
            Log.v("Volley Excep", e.getMessage());
            showToast("Something went wrong. Please try again later.");
        }

    }

    private void authenticateUserOrganisation(final String email) {
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
                            Log.e(VizippDrawLogin.class.getName(), "" + response.getUserOrganizationList().size());

                            if (response != null) {
                                if (response.getSuccess() != null && response.getSuccess() == 1) {

                                    sessionManager.updateSessionVizippUsername(email);
                                    ArrayList<UserOrganizationList> listData = new ArrayList<UserOrganizationList>();
                                    for (int i = 0; i < response.getUserOrganizationList().size(); i++) {
                                        listData.add(response.getUserOrganizationList().get(i));
                                    }

                                    Intent intent = new Intent(VizippDrawForgotPassword.this, OrgnizationListActivity.class);
                                    intent.putExtra("organizationList", listData);
                                    intent.putExtra("email", email);
                                    intent.putExtra("comesFrom", "forgotPasswordActivity");
                                    startActivityForResult(intent, RESULT_FORGOT_PASSWORD);

                                    /*if (response.getUserOrganizationList().size() == 1) {
                                        forgotPassword(email, response.getUserOrganizationList().get(0).getApiKey(), response.getUserOrganizationList().get(0).getSecretekey());
                                    } else {
                                        final Dialog dialog = new Dialog(VizippDrawForgotPassword.this);
                                        View view = getLayoutInflater().inflate(R.layout.dialog_main, null);
                                        ListView lv = (ListView) view.findViewById(R.id.custom_list);

                                        ArrayList<UserOrganizationList> listData = new ArrayList<UserOrganizationList>();
                                        for (int i = 0; i < response.getUserOrganizationList().size(); i++) {
                                            listData.add(response.getUserOrganizationList().get(i));
                                        }
                                        CustomListAdapterDialog clad = new CustomListAdapterDialog(VizippDrawForgotPassword.this, listData);
                                        lv.setAdapter(clad);
                                        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                            @Override
                                            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                                //authenticateUser(email,password,response.getUserOrganizationList().get(position).getApiKey(),response.getUserOrganizationList().get(position).getSecretekey());
                                                forgotPassword(email, response.getUserOrganizationList().get(position).getApiKey(), response.getUserOrganizationList().get(position).getSecretekey());
                                            }
                                        });
                                        dialog.setContentView(view);
                                        dialog.show();

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
