package com.edbrix.contentbrix;

import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.crashlytics.android.Crashlytics;
import com.edbrix.contentbrix.baseclasses.BaseActivity;
import com.edbrix.contentbrix.commons.AlertDialogManager;
import com.edbrix.contentbrix.volley.VolleySingleton;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class ContentBrixRegistrationActivity extends BaseActivity {
    private EditText mEditTextSchoolCode, mEditTextFirstNm, mEditTextLastNm, mEditTextEMail, mEditTextPhone;
    private Button mButtonReg;
    private TextView mTextViewLogIn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (isTablet()) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE); // stop screen rotation on phones
        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }
        setContentView(R.layout.activity_content_brix_registration);

        mEditTextSchoolCode = findViewById(R.id.edtSchoolCode);
        mEditTextFirstNm = findViewById(R.id.edtFirstName);
        mEditTextLastNm = findViewById(R.id.edtLastName);
        mEditTextEMail = findViewById(R.id.edtEmail);
        mEditTextPhone = findViewById(R.id.edtPhone);
        mButtonReg = findViewById(R.id.btnRegister);
        mTextViewLogIn = findViewById(R.id.textViewLogIn);

        mButtonReg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!(TextUtils.isEmpty(mEditTextSchoolCode.getText().toString().trim()) | TextUtils.isEmpty(mEditTextFirstNm.getText().toString().trim()) | TextUtils.isEmpty(mEditTextLastNm.getText().toString().trim()) | TextUtils.isEmpty(mEditTextEMail.getText().toString().trim()) | TextUtils.isEmpty(mEditTextPhone.getText().toString().trim()))) {
                    if (validate()) {
                        registerUser(mEditTextSchoolCode.getText().toString(), mEditTextFirstNm.getText().toString(), mEditTextLastNm.getText().toString(), mEditTextEMail.getText().toString(), mEditTextPhone.getText().toString());
                    }
                } else {
                    showToast("Please Fill All Fields..!");
                }
            }
        });

        mTextViewLogIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    protected boolean isTablet() {
        boolean xlarge = ((this.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) >=
                Configuration.SCREENLAYOUT_SIZE_XLARGE);
        boolean large = ((this.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) >=
                Configuration.SCREENLAYOUT_SIZE_LARGE);
        return (xlarge || large);
    }


    private void registerUser(String schoolCode, final String firstName, final String lastName, final String email, final String phone) {

        try {
            showBusyProgress();
            JSONObject jo = new JSONObject();
            jo.put("SchoolCode", schoolCode);
            jo.put("FirstName", firstName);
            jo.put("LastName", lastName);
            jo.put("Email", email);
            jo.put("Phone", phone);

            String tempUrl = "https://services.edbrix.net/";
            JsonObjectRequest userRegistrationRequest = new JsonObjectRequest(Request.Method.POST, tempUrl + "contentbrix/registerrequest", jo, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(final JSONObject response) {
                    hideBusyProgress();
                    Log.v("Volley Response", response.toString());
                    try {
                        if (response != null) {
                            if (response.has("Success") && response.getString("Success").equals("1")) {

                                getAlertDialogManager().Dialog("Information", response.getString("Message"), new AlertDialogManager.onSingleButtonClickListner() {
                                    @Override
                                    public void onPositiveClick() {
                                        finish();
                                    }
                                }).show();
                            } else if (response.has("Error")) {
                                JSONObject ErrorjsonObject = response.getJSONObject("Error");
                                getAlertDialogManager().Dialog("Information", ErrorjsonObject.getString("ErrorMessage"), new AlertDialogManager.onSingleButtonClickListner() {
                                    @Override
                                    public void onPositiveClick() {
                                        //finish();
                                    }
                                }).show();
                            } else {
                                showToast("Something went wrong..!");
                            }
                        } else {
                            showToast("Something went wrong..!");
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

    private boolean validate() {
        boolean bln = true;
        String email = this.mEditTextEMail.getText().toString().trim();
        String phone = this.mEditTextPhone.getText().toString().trim();
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            mEditTextEMail.setBackgroundResource(R.drawable.red_border);
            mEditTextEMail.requestFocus();
            mEditTextEMail.setError(getResources().getString(R.string.invalid_email));
            bln = false;
        }
        if (phone.length() != 10) {
            mEditTextPhone.setBackgroundResource(R.drawable.red_border);
            mEditTextPhone.requestFocus();
            mEditTextPhone.setError("Enter Valid Phone Number");
            bln = false;
        }
        if (mEditTextSchoolCode.getText().toString().trim().length() == 0) {
            showToast("please enter school code..!");
        }
        if (mEditTextFirstNm.getText().toString().trim().length() == 0) {
            showToast("please enter first name..!");
        }
        if (mEditTextLastNm.getText().toString().trim().length() == 0) {
            showToast("please enter last name..!");
        }
        if (mEditTextEMail.getText().toString().trim().length() == 0) {
            showToast("please enter email..!");
        }
        if (mEditTextPhone.getText().toString().trim().length() == 0) {
            showToast("please enter mobile number..!");
        }
        return bln;
    }
}
