package com.edbrix.contentbrix;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.crashlytics.android.Crashlytics;
/*import com.edbrix.connectbrix.Application;
import com.edbrix.connectbrix.data.ForgotPasswordResponseData;
import com.edbrix.connectbrix.data.SaveDeviceTokenResponseData;
import com.edbrix.connectbrix.data.UserLoginResponseData;
import com.edbrix.connectbrix.data.UserOrganizationList;
import com.edbrix.connectbrix.utils.Constants;
import com.edbrix.connectbrix.volley.GsonRequest;
import com.edbrix.connectbrix.volley.SettingsMy;*/
import com.edbrix.contentbrix.adapters.OrgnizationListAdapter;
import com.edbrix.contentbrix.baseclasses.BaseActivity;
import com.edbrix.contentbrix.commons.AlertDialogManager;
import com.edbrix.contentbrix.data.KnowledgeBrixUserData;
import com.edbrix.contentbrix.data.ProductAccessList;
import com.edbrix.contentbrix.data.UserOrganizationList;
import com.edbrix.contentbrix.data.Userproductaccessresponse;
import com.edbrix.contentbrix.utils.SessionManager;
import com.edbrix.contentbrix.volley.VolleySingleton;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
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

public class OrgnizationListActivity extends BaseActivity {

    private static final String TAG = OrgnizationListActivity.class.getName();
    private ListView mOrgnizationList;
    OrgnizationListAdapter orgnizationListAdapter;
    ArrayList<UserOrganizationList> userOrganizationListData;
    SessionManager sessionManager;

    String userComesFrom;
    String isPasswordSkip = "";
    private GoogleSignInClient mGoogleSignInClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_orgnization_list);
        //getSupportActionBar().setTitle("Select your School");
        assignViews();

        /* mGoogleSignInClient = GoogleSignIn.getClient(this, gso);*/

        sessionManager = new SessionManager(this);
        Intent intent = getIntent();
        userComesFrom = intent.getStringExtra("comesFrom");
        userOrganizationListData = new ArrayList<>();
        userOrganizationListData = (ArrayList<UserOrganizationList>) intent.getSerializableExtra("organizationList");
        setOrgnizationListAdapter(userOrganizationListData);
        clickListners();

    }

    private void clickListners() {

        mOrgnizationList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {

                if (userComesFrom.equals("loginActivity")) {
                    doLogin(position);
                } else if (userComesFrom.equals("forgotPasswordActivity")) {
                    resetPassword(position);
                }


            }
        });
    }

    private void assignViews() {
        mOrgnizationList = (ListView) findViewById(R.id.orgnizationList);
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getResources().getString(R.string.web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
    }

    private void setOrgnizationListAdapter(ArrayList<UserOrganizationList> userOrganizationListData) {
        orgnizationListAdapter = new OrgnizationListAdapter(OrgnizationListActivity.this, userOrganizationListData);
        mOrgnizationList.setAdapter(orgnizationListAdapter);
    }

    private void doLogin(final int position) {
        try {
            final String email = sessionManager.getSessionVizippUsername();
            final String password = sessionManager.getSessionVizippPassword();
            final String apiKey = userOrganizationListData.get(position).getApiKey();
            final String secretekey = userOrganizationListData.get(position).getSecretekey();
            final String OrganizationName = userOrganizationListData.get(position).getOrganizationName();
            Map<String, String> requestMap = new HashMap<String, String>();
            requestMap.put("Email", email);
            requestMap.put("Password", password);
            requestMap.put("APIKEY", apiKey);
            requestMap.put("SECRETKEY", secretekey);
            if (IsSkipPassword) {
                requestMap.put("IsSkipPassword", "1");
            }

            Log.v("loggin", requestMap.toString());

            //////////////
            showBusyProgress();
            String tempUrl = "https://services.edbrix.net/";

            JsonObjectRequest authenticateUserRequest = new JsonObjectRequest(Request.Method.POST, tempUrl + "auth/login", new JSONObject(requestMap), new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    hideBusyProgress();
                    Log.v(OrgnizationListActivity.class.getName().toString(), "authenticateUserResp: \n" + response.toString());
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

                                        Log.v(OrgnizationListActivity.class.getName().toString(), " AccessToken : " + knowledgeBrixUserData.getAccessToken());
                                        Log.v(OrgnizationListActivity.class.getName().toString(), " ApiKey : " + knowledgeBrixUserData.getApiKey());
                                        Log.v(OrgnizationListActivity.class.getName().toString(), " Secretekey : " + knowledgeBrixUserData.getSecretekey());

                                        verifyProduct(
                                                response.getString("AccessToken"),
                                                knowledgeBrixUserData.getId(),
                                                knowledgeBrixUserData, email, password, OrganizationName,knowledgeBrixUserData.getUserType());


                                    } else {
                                        mGoogleSignInClient.signOut().addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                Log.e(OrgnizationListActivity.class.getName().toString(), "Loggedout from google account");
                                            }
                                        });

                                        AlertDialog.Builder alertDialog = new AlertDialog.Builder(OrgnizationListActivity.this);
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
                            } else if (response.has("Error")) {
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

            VolleySingleton.getInstance().addToRequestQueue(authenticateUserRequest);
        } catch (Exception e) {
            Crashlytics.logException(e);
            hideBusyProgress();
            Log.v("Volley Excep", e.getMessage());
            showToast("Something went wrong. Please try again later.");
        }
    }

    private void verifyProduct(String accessToken, final String id,
                               final KnowledgeBrixUserData knowledgeBrixUserData,
                               final String email,
                               final String password,
                               final String organizationName,final String userType) {

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
                                        if (userproductaccessresponse.getProductAccessList().get(i).getProductId().equals("9") &&
                                                userproductaccessresponse.getProductAccessList().get(i).getHasAccess() == 1) {
                                            isActive = true;
                                            break;
                                        } else {
                                            isActive = false;
                                        }
                                    }
                                    if (isActive) {
                                        sessionManager.updateUserId(id);
                                        sessionManager.updateUserType(userType);
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
                                        mGoogleSignInClient.signOut().addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                Log.e(OrgnizationListActivity.class.getName().toString(), "Loggedout from google account");
                                            }
                                        });
                                        showToast("This user is not activated for this school.");
                                    }
                                } else {
                                    mGoogleSignInClient.signOut().addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            Log.e(OrgnizationListActivity.class.getName().toString(), "Loggedout from google account");
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

    private void startRecordActivity() {
        Intent mainIntent = new Intent();
        mainIntent.setClass(OrgnizationListActivity.this, StartRecordActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(mainIntent);
        finish();
    }


    private void resetPassword(int position) {

        final String email = sessionManager.getSessionVizippUsername();
        final String apiKey = userOrganizationListData.get(position).getApiKey();
        final String secretKey = userOrganizationListData.get(position).getSecretekey();

        Map<String, String> requestMap = new HashMap<String, String>();

        requestMap.put("Email", email);
        requestMap.put("APIKEY", apiKey);
        requestMap.put("SECRETKEY", secretKey);

        try {
            showBusyProgress();
            String tempUrl = "https://services.edbrix.net/";
            JsonObjectRequest forgotPasswordRequest = new JsonObjectRequest(Request.Method.POST, tempUrl + "auth/resetpassword", new JSONObject(requestMap), new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    hideBusyProgress();
                    Log.v(OrgnizationListActivity.class.getName(), response.toString());
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

    @Override
    public void onBackPressed() {
        finish();
        //startActivity(new Intent(OrgnizationListActivity.this, VizippDrawLogin.class));

    }

    private void signOut() {
        // Firebase sign out
        /* mAuth.signOut();*/

        // Google sign out
        mGoogleSignInClient.signOut().addOnCompleteListener(this,
                new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        IsSkipPassword = false;
                        Log.e(TAG, "Loggedout from google account");
                        //sessionManager.updateIsPasswordSkip("0");
                        //sessionManager.updateGoogleAccount("");
                    }
                });
    }
}
