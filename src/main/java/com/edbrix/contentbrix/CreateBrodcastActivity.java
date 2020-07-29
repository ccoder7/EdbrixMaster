package com.edbrix.contentbrix;

import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TimePicker;

import androidx.appcompat.widget.Toolbar;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.crashlytics.android.Crashlytics;
import com.edbrix.contentbrix.baseclasses.BaseActivity;
import com.edbrix.contentbrix.utils.SessionManager;
import com.edbrix.contentbrix.volley.VolleySingleton;
import com.google.android.material.appbar.AppBarLayout;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class CreateBrodcastActivity extends BaseActivity {


    private AppBarLayout mAppBarLayout;
    private Toolbar mToolbar;
    private RelativeLayout mRelativeLayoutLogo;
    private TextView mTitle;
    private EditText mBroadcastName;
    private EditText mBroadcastDiscription;
    private EditText mBroadcastDate;
    private Button mBtnSaveBroadcast;
    private int mYear, mMonth, mDay, mHour, mMinute;
    private SessionManager sessionManager;
    String dateTime;
    String tempDateTime;
    TimePickerDialog timePickerDialog;

    private void assignViews() {
        mAppBarLayout = (AppBarLayout) findViewById(R.id.appBarLayout);
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mRelativeLayoutLogo = (RelativeLayout) findViewById(R.id.relativeLayoutLogo);
        mTitle = (TextView) findViewById(R.id.title);
        mBroadcastName = (EditText) findViewById(R.id.broadcastName);
        mBroadcastDiscription = (EditText) findViewById(R.id.broadcastDiscription);
        mBroadcastDate = (EditText) findViewById(R.id.broadcastDate);
        mBtnSaveBroadcast = (Button) findViewById(R.id.btnSaveBroadcast);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (isTablet()) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE); // stop screen rotation on phones
        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }
        setContentView(R.layout.activity_create_brodcast);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        assignViews();
        setListners();
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        sessionManager = new SessionManager(this);


    }

    private void setListners() {
        mBtnSaveBroadcast.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validation() == true) {
                    createBroadcast();
                }
            }
        });

        mBroadcastDate.setFocusable(false);
        mBroadcastDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get Current Date
                final Calendar c = Calendar.getInstance();
                mYear = c.get(Calendar.YEAR);
                mMonth = c.get(Calendar.MONTH);
                mDay = c.get(Calendar.DAY_OF_MONTH);

                mHour = c.get(Calendar.HOUR_OF_DAY);
                mMinute = c.get(Calendar.MINUTE);

                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                    DatePickerDialog datePickerDialog = new DatePickerDialog(CreateBrodcastActivity.this, new DatePickerDialog.OnDateSetListener() {
                        @Override
                        public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                            dateTime = year + "-" + (month + 1) + "-" + dayOfMonth;
                            timePickerDialog.show();
                        }
                    }, mYear, mMonth, mDay);
                    datePickerDialog.show();

                    // Launch Time Picker Dialog
                    timePickerDialog = new TimePickerDialog(CreateBrodcastActivity.this,
                            new TimePickerDialog.OnTimeSetListener() {

                                @Override
                                public void onTimeSet(TimePicker view, int hourOfDay,
                                                      int minute) {

                                    String AM_PM;
                                    if (hourOfDay < 12) {
                                        AM_PM = "AM";
                                    } else {
                                        AM_PM = "PM";
                                    }

                                    String tempDateTime1 = dateTime + " " + hourOfDay + ":" + minute+" "+AM_PM;
                                    mBroadcastDate.setText(tempDateTime1);

                                    String inputPattern = "yyyy-MM-dd HH:mm:ss";
                                    String outputPattern = "yyyy-MM-dd h:mm a";
                                    SimpleDateFormat inputFormat = new SimpleDateFormat(inputPattern);
                                    SimpleDateFormat outputFormat = new SimpleDateFormat(outputPattern);

                                    Date date = null;
                                    String str = null;

                                    try {
                                        date = outputFormat.parse(tempDateTime1);
                                        tempDateTime = inputFormat.format(date);
                                    } catch (ParseException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }, mHour, mMinute, false);
                }
            }
        });
    }

    private boolean validation() {
        String broadcastName = mBroadcastName.getText().toString();
        String broadcastDiscription = mBroadcastDiscription.getText().toString();
        String broadcastDate = mBroadcastDate.getText().toString();

        if (broadcastName.isEmpty()) {
            mBroadcastName.setError("Broadcast name can't be blank");
            return false;
        } else if (broadcastDiscription.isEmpty()) {
            mBroadcastDiscription.setError("Broadcast description can't be blank");
            return false;
        } else if (broadcastDate.isEmpty()) {
            mBroadcastDate.setError("Broadcast date can't be blank");
            return false;
        }

        return true;


    }

    private void createBroadcast() {

        Map<String, String> requestMap = new HashMap<String, String>();
        requestMap.put("AccessToken", sessionManager.getPrefsSessionVizippToken());
        requestMap.put("UserId", sessionManager.getUserId());
        requestMap.put("Title", mBroadcastName.getText().toString());
        requestMap.put("Description", mBroadcastDiscription.getText().toString());
        requestMap.put("ChannelToken", "");
        requestMap.put("ChatUserToken", "");
        requestMap.put("CreatedDate", tempDateTime);

        Log.e("Log", requestMap.toString());


        try {
            showBusyProgress();
            String tempUrl = "https://services.edbrix.net/";
            JsonObjectRequest createBroadcastRequest = new JsonObjectRequest(Request.Method.POST, tempUrl + "chatbrix/createboradcast", new JSONObject(requestMap), new Response.Listener<JSONObject>() {

                @Override
                public void onResponse(JSONObject response) {
                    hideBusyProgress();
                    Log.v(CreateBrodcastActivity.class.getName(), response.toString());

                    try {
                        if (response != null && response.has("Success")) {
                            if (response.getString("Success").equals("1")) {
                                showToast("Broadcast created successfully");//response.getString("Message")
                            } else {
                                showToast(response.getString("Message"));
                            }
                        }
                    } catch (JSONException e) {
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

            createBroadcastRequest.setRetryPolicy(VolleySingleton.getDefaultRetryPolice());
            createBroadcastRequest.setShouldCache(false);
            VolleySingleton.getInstance().addToRequestQueue(createBroadcastRequest, "createBroadcastRequest");

        } catch (Exception e) {
            Crashlytics.logException(e);
            hideBusyProgress();
            Log.v("Volley Excep", e.getMessage());
            showToast("Something went wrong. Please try again later.");
        }


    }

    protected boolean isTablet() {
        boolean xlarge = ((this.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) >=
                Configuration.SCREENLAYOUT_SIZE_XLARGE);
        boolean large = ((this.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) >=
                Configuration.SCREENLAYOUT_SIZE_LARGE);
        return (xlarge || large);
    }
}