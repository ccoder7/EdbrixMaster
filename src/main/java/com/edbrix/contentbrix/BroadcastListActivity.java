package com.edbrix.contentbrix;

import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.crashlytics.android.Crashlytics;
import com.edbrix.contentbrix.baseclasses.AgoraBaseActivity;
import com.edbrix.contentbrix.baseclasses.BaseActivity;
import com.edbrix.contentbrix.data.GetUserBroadCastListData;
import com.edbrix.contentbrix.data.GetUserBroadCastListParentData;
import com.edbrix.contentbrix.utils.SessionManager;
import com.edbrix.contentbrix.volley.VolleySingleton;
import com.google.android.material.appbar.AppBarLayout;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BroadcastListActivity extends BaseActivity {

    private AppBarLayout mAppBarLayout;
    private Toolbar mToolbar;
    private RelativeLayout mRelativeLayoutLogo;
    private TextView mTitle;
    private Button mBtnCreateBroadcast;
    private Button mBtnRefresh;
    private TableLayout mBroadcastListTable;
    private TextView mTextViewDay;
    private TextView mTextViewStartTime;
    private SessionManager sessionManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (isTablet()) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE); // stop screen rotation on phones
        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }
        setContentView(R.layout.activity_broadcast_list);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        sessionManager = new SessionManager(this);

        if (sessionManager.getUserType().equals("T")) {
            getUserBroadcastList();
        } else {

        }

        assignViews();
        setClickListners();


    }

    private void setClickListners() {
        mBtnCreateBroadcast.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent broadcast = new Intent(BroadcastListActivity.this, CreateBrodcastActivity.class);
                startActivity(broadcast);
            }
        });

        mBtnRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mBroadcastListTable.getChildCount()>0){
                    mBroadcastListTable.removeAllViews();
                }
                getUserBroadcastList();
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

    private void assignViews() {
        mAppBarLayout = (AppBarLayout) findViewById(R.id.appBarLayout);
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mRelativeLayoutLogo = (RelativeLayout) findViewById(R.id.relativeLayoutLogo);
        mTitle = (TextView) findViewById(R.id.title);
        mBtnCreateBroadcast = (Button) findViewById(R.id.btnCreateBroadcast);
        mBtnRefresh = (Button) findViewById(R.id.btnRefresh);
        mBroadcastListTable = (TableLayout) findViewById(R.id.broadcastListTable);
        //mTextViewDay = (TextView) findViewById(R.id.textViewDay);
        //mTextViewStartTime = (TextView) findViewById(R.id.textViewStartTime);
    }

    private void getUserBroadcastList() {

        Map<String, String> requestMap = new HashMap<String, String>();
        requestMap.put("AccessToken", sessionManager.getPrefsSessionVizippToken());
        requestMap.put("UserId", sessionManager.getUserId());


        Log.e("Log", requestMap.toString());

        try {
            showBusyProgress();
            String tempUrl = "https://services.edbrix.net/";
            JsonObjectRequest createBroadcastRequest = new JsonObjectRequest(Request.Method.POST, tempUrl + "chatbrix/getuserbroadcastlist", new JSONObject(requestMap), new Response.Listener<JSONObject>() {

                @Override
                public void onResponse(JSONObject response) {
                    hideBusyProgress();
                    Log.v(BroadcastListActivity.class.getName(), response.toString());


                    try {
                        final GetUserBroadCastListParentData parentData = new GetUserBroadCastListParentData();
                        parentData.setUserBroadcast((List<GetUserBroadCastListData>) new Gson().fromJson(response.getJSONArray("UserBroadcast").toString(), new TypeToken<List<GetUserBroadCastListData>>() {
                        }.getType()));

                        if (response != null && response.has("Success")) {
                            if (response.getString("Success").equals("1")) {
                                TableRow row1 = new TableRow(BroadcastListActivity.this);
                                row1.setPadding(5, 5, 5, 5);
                                TableRow.LayoutParams lparams1 = new TableRow.LayoutParams(
                                        TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT, 1.0f);
                                lparams1.setMargins(5, 5, 5, 5);

                                TextView textView_day = new TextView(BroadcastListActivity.this);
                                textView_day.setLayoutParams(lparams1);
                                textView_day.setTextColor(getResources().getColor(R.color.black));
                                textView_day.setText("Day");

                                TextView textView_Start_Time = new TextView(BroadcastListActivity.this);
                                textView_Start_Time.setLayoutParams(lparams1);
                                textView_Start_Time.setTextColor(getResources().getColor(R.color.black));
                                textView_Start_Time.setText("Start Time");

                                TextView textView_Start_Broadcast = new TextView(BroadcastListActivity.this);
                                textView_Start_Broadcast.setLayoutParams(lparams1);
                                textView_Start_Broadcast.setTextColor(getResources().getColor(R.color.black));
                                textView_Start_Broadcast.setText("Start Broadcast");

                                TextView textView_action = new TextView(BroadcastListActivity.this);
                                textView_action.setLayoutParams(lparams1);
                                textView_action.setTextColor(getResources().getColor(R.color.black));
                                textView_action.setText("Action");

                                row1.addView(textView_day);
                                row1.addView(textView_Start_Time);
                                row1.addView(textView_Start_Broadcast);
                                row1.addView(textView_action);
                                row1.setBackground(getResources().getDrawable(R.drawable.no_border_grey_background));
                                mBroadcastListTable.addView(row1,0);


                                if (parentData.getUserBroadcast().size() > 0) {
                                    for (int i = 0; i < parentData.getUserBroadcast().size(); i++) {
                                        TableRow row = new TableRow(BroadcastListActivity.this);
                                        row.setPadding(5, 5, 5, 5);
                                        TableRow.LayoutParams lparams = new TableRow.LayoutParams(
                                                TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT, 1.0f);
                                        lparams.setMargins(5, 5, 5, 5);

                                        TextView textView_day_i = new TextView(BroadcastListActivity.this);
                                        textView_day_i.setLayoutParams(lparams);
                                        textView_day_i.setText(parentData.getUserBroadcast().get(i).getDateName());

                                        TextView textView_start_i = new TextView(BroadcastListActivity.this);
                                        textView_start_i.setLayoutParams(lparams);
                                        textView_start_i.setText(parentData.getUserBroadcast().get(i).getCreatedDate());

                                        Button button_start_i = new Button(BroadcastListActivity.this);
                                        button_start_i.setLayoutParams(lparams);
                                        button_start_i.setText("Start");
                                        button_start_i.setId(i);
                                        button_start_i.setAllCaps(false);
                                        button_start_i.setTextColor(getResources().getColor(R.color.button_text));
                                        button_start_i.setBackgroundResource(R.drawable.blue_border_no_background);
                                        button_start_i.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                config().setChannelName(parentData.getUserBroadcast().get(v.getId()).getTitle());//  "withpranva"
                                                config().setmChannelToken(parentData.getUserBroadcast().get(v.getId()).getChannelToken());//"0063f07a97f774a42c88ea526f99e508373IABuyAU6sP2Xmme4PLil8keie6uyCTuHdo8pVh3N1pVnAxwbr52n1rw8IgDRzQdXvzkRXwQAAQA/Fx1fAgA/Fx1fAwA/Fx1fBAA/Fx1f"
                                                config().setmMuxStreamKey(parentData.getUserBroadcast().get(v.getId()).getMuxStreamKey());
                                                Intent intent = new Intent(getIntent());
                                                intent.putExtra(com.edbrix.contentbrix.Constants.KEY_CLIENT_ROLE, io.agora.rtc.Constants.CLIENT_ROLE_BROADCASTER);
                                                intent.setClass(getApplicationContext(), StartBroadcastActivity.class);
                                                startActivity(intent);
                                            }
                                        });

                                        Button button_delete_i = new Button(BroadcastListActivity.this);
                                        button_delete_i.setLayoutParams(lparams);
                                        button_delete_i.setText("Delete");
                                        button_delete_i.setId(i);
                                        button_delete_i.setAllCaps(false);
                                        button_delete_i.setTextColor(getResources().getColor(R.color.ColorErrorRed));
                                        button_delete_i.setBackgroundResource(R.drawable.delete_button_background);
                                        button_delete_i.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                deleteBroadcast(Integer.parseInt(parentData.getUserBroadcast().get(v.getId()).getBroadcastId()));
                                                //Toast.makeText(BroadcastListActivity.this,"HIi "+v.getId(),Toast.LENGTH_LONG).show();
                                            }
                                        });

                                        row.addView(textView_day_i);
                                        row.addView(textView_start_i);
                                        row.addView(button_start_i);
                                        row.addView(button_delete_i);

                                        mBroadcastListTable.addView(row, i+1);


                                    }
                                }
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

    private void deleteBroadcast(int broadcastId){
        Map<String, String> requestMap = new HashMap<>();
        requestMap.put("AccessToken", sessionManager.getPrefsSessionVizippToken());
        requestMap.put("UserId", sessionManager.getUserId());
        requestMap.put("BroadcastId", ""+broadcastId);
        Log.e("Log", requestMap.toString());

        try {
            showBusyProgress();
            String tempUrl = "https://services.edbrix.net/";

            JsonObjectRequest deleteBroadcastRequest = new JsonObjectRequest(Request.Method.POST, tempUrl + "chatbrix/deleteuserboradcast", new JSONObject(requestMap), new Response.Listener<JSONObject>() {

                @Override
                public void onResponse(JSONObject response) {
                    hideBusyProgress();
                    Log.v(BroadcastListActivity.class.getName(), response.toString());


                    try {

                        if (response != null && response.has("Success")) {
                            if (response.getString("Success").equals("1")) {

                                showToast("Broadcast deleted");
                                mBroadcastListTable.removeAllViews();
                                getUserBroadcastList();
                            } else {
                                showToast(response.getString("Message"));
                            }
                        }else{
                            showToast("Something went wrong. Please try again later.");
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

            deleteBroadcastRequest.setRetryPolicy(VolleySingleton.getDefaultRetryPolice());
            deleteBroadcastRequest.setShouldCache(false);
            VolleySingleton.getInstance().addToRequestQueue(deleteBroadcastRequest, "deleteBroadcastRequest");
        }catch (Exception e){
            Crashlytics.logException(e);
            hideBusyProgress();
            Log.v("Volley Excep", e.getMessage());
            showToast("Something went wrong. Please try again later.");
        }

    }
}