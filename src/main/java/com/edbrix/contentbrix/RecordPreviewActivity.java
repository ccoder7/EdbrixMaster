package com.edbrix.contentbrix;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.crashlytics.android.Crashlytics;
import com.dropbox.core.InvalidAccessTokenException;
import com.dropbox.core.android.Auth;
import com.dropbox.core.v2.files.FileMetadata;
import com.edbrix.contentbrix.data.KnowledgeBrixUserData;
import com.edbrix.contentbrix.data.ProductAccessList;
import com.edbrix.contentbrix.data.Userproductaccessresponse;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi;
import com.google.android.gms.drive.MetadataChangeSet;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.edbrix.contentbrix.baseclasses.BaseActivity;
import com.edbrix.contentbrix.commons.GlobalMethods;
import com.edbrix.contentbrix.customview.CustomDialogSlideFromBottom;
import com.edbrix.contentbrix.customview.InformatoryDialog;
import com.edbrix.contentbrix.data.FileData;
import com.edbrix.contentbrix.data.VizippUserData;
import com.edbrix.contentbrix.dropbox.DropboxClient;
import com.edbrix.contentbrix.dropbox.UploadFileToDropboxTask;
import com.edbrix.contentbrix.utils.SessionManager;
import com.edbrix.contentbrix.volley.VolleySingleton;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.microsoft.onedrivesdk.saver.ISaver;
import com.microsoft.onedrivesdk.saver.Saver;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.android.volley.Request.*;

public class RecordPreviewActivity extends BaseActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, UploadFileToDropboxTask.Callback {

    // LogCat tag
    private static final String TAG = RecordPreviewActivity.class.getSimpleName();
    public static final int REQUEST_CODE = 5120;
    private static final int REQUEST_CODE_RESOLUTION = 3;
    private static final int UPLOAD_LIMIT = 5;
    private static final int REQUEST_CODE_CREATOR = 2;
    public static final int REQUEST_CODE_EDBRIX_LOGIN = 151;
    public static final int REQUEST_CODE_EDBRIX_CHOOSE_ACCOUNT = 152;
    public static final int REQUEST_CODE_RESOURCE_LIST = 153;

    private Button btnSave;
    private LinearLayout btnUpload;
    private LinearLayout btnCancel;
    private EditText edtFileName;
    private ProgressBar vProgressBar;
    private ProgressBar mProgressBar;
    private ImageView playBtn;
    //    private CustomVideoView videoPreview;
    private ImageView videoPreview;
    private TextView noContentText;
    private FileData fileData;
    private TextView textPercentage;
    //    private TextView title;
    private SessionManager sessionManager;

    private String userId;
    private String accessToken;

    private FirebaseStorage storage;
    private StorageReference storageRef;
    private UploadTask uploadTask;

    private GoogleApiClient mGoogleApiClient;
    private boolean isUploadDPBoxResultPending;
    private KnowledgeBrixUserData vData;
    private ISaver mSaver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_preview);
        if (isTablet()) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE); // stop screen rotation on phones
        }else{
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }
        sessionManager = new SessionManager(RecordPreviewActivity.this);

        storage = FirebaseStorage.getInstance("gs://edbrix-school-api-storage");
        storageRef = storage.getReferenceFromUrl("gs://edbrix-school-api-storage");

        mSaver = Saver.createSaver("8dc3bd17-8417-4083-9f37-d28d80986105"); //App Id From Microsoft

        Bundle bundle = getIntent().getExtras();
//        title = (TextView) findViewById(R.id.title);
        fileData = (FileData) bundle.getSerializable("FileData");

        if (sessionManager.hasSessionVizippCredentials())
            vData = sessionManager.getLoggedKnowledgeBrixUserData();

        //initialize components
        btnSave = (Button) findViewById(R.id.btnSave);
        btnUpload = (LinearLayout) findViewById(R.id.btnUpload);
        btnCancel = (LinearLayout) findViewById(R.id.btnCancel);

        noContentText = (TextView) findViewById(R.id.noContentText);
        textPercentage = (TextView) findViewById(R.id.txtPercentage);
        edtFileName = (EditText) findViewById(R.id.edtFileName);
        vProgressBar = (ProgressBar) findViewById(R.id.vProgressBar);
        mProgressBar = (ProgressBar) findViewById(R.id.mProgressBar);
        playBtn = (ImageView) findViewById(R.id.playBtn);
        videoPreview = (ImageView) findViewById(R.id.videoPreview);

        // set listeners
        setListeners();

        //set video uri to video view
        if (fileData != null && fileData.getFileObject() != null) {
            String fName = fileData.getFileName().replaceAll(".mp4", "");
//            title.setText(fName);
            edtFileName.setText(fName);
            noContentText.setVisibility(View.GONE);
            Bitmap thumb = GlobalMethods.getVideoThumbnailFromPath(fileData.getFileObject().getPath());
            BitmapDrawable bitmapDrawable = new BitmapDrawable(thumb);
            videoPreview.setImageBitmap(thumb);
            videoPreview.setScaleType(ImageView.ScaleType.CENTER_CROP);
            playBtn.setVisibility(View.VISIBLE);
        } else {
            noContentText.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Receiving activity result
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // if the result is capturing Image
        if (requestCode == REQUEST_CODE_EDBRIX_LOGIN || requestCode == REQUEST_CODE_EDBRIX_CHOOSE_ACCOUNT) {
            if (resultCode == RESULT_OK) {
                if (sessionManager.hasSessionCredentials()) {
                    if (getGlobalMethods().isInternetConnectivity(RecordPreviewActivity.this)) {
                        uploadToEdbrix();
                    } else {
                        showToast(getResources().getString(R.string.no_internet_msg));
                    }
                }
            }
        } else if (requestCode == REQUEST_CODE_RESOLUTION) {
            if (resultCode == RESULT_OK) {
                mGoogleApiClient.connect();
            }
        } else if (requestCode == VizippDrawLogin.REQUEST_CODE_VIZIPP_LOGIN) {
            if (resultCode == RESULT_OK) {
                btnUpload.callOnClick();
            }
        } else if (requestCode == VizippDrawLogin.REQUEST_CODE_VIZIPP_PAY) {
            if (resultCode == RESULT_OK) {
                showUploadOptionDialog();
            }
        } else if (requestCode == REQUEST_CODE_RESOURCE_LIST) {
            if (resultCode == RESULT_OK) {
                finish();
            }
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        //Get Dropbox token and upload file to dropbox account
        getAccessToken_UploadToDropbox();
    }

    /**
     * Get Access Token after authentication process and if token!=null then upload file dropbox account
     */
    public void getAccessToken_UploadToDropbox() {
        String dropBoxToken = Auth.getOAuth2Token(); //generate Access Token
        if (dropBoxToken != null) {
            //Store dropBoxToken in SharedPreferences
            sessionManager.updateSessionDropBoxToken(dropBoxToken);
            if (isUploadDPBoxResultPending) {
                showBusyProgress(" Upload to Dropbox in progress...");
                new UploadFileToDropboxTask(DropboxClient.getClient(dropBoxToken), fileData.getFileObject(), this).execute();
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mGoogleApiClient != null) {
            // disconnect Google Android Drive API connection.
            mGoogleApiClient.disconnect();
        }
        super.onPause();
    }

    /**
     * Defined listeners to components
     */
    private void setListeners() {

        playBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openVideoPlayer(fileData);
            }
        });

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(edtFileName.getWindowToken(), 0);
                String fileName = edtFileName.getText().toString().trim();
                if (fileName.length() > 0) {
                    renameVideoFile(fileName);
                } else {

                }
            }
        });

        btnUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (sessionManager.hasSessionVizippCredentials())
                {
                    if (getGlobalMethods().isInternetConnectivity(RecordPreviewActivity.this))
                    {
                        showUploadOptionDialog();
                        //getUserSubscriptionDetails(sessionManager.getLoggedKnowledgeBrixUserData().getId(), sessionManager.getPrefsSessionVizippToken());
                    } else {
                        showToast(getResources().getString(R.string.no_internet_msg));
                    }
                } else {
                    goToKnowledgeBrixLogin();
                }
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                cancelUploadFile();
            }
        });

    }

    /**
     * go to Knowledgebrix Login Screen
     */
    private void goToKnowledgeBrixLogin() {
        Intent mainIntent = new Intent();
        mainIntent.setClass(RecordPreviewActivity.this, VizippDrawLogin.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(mainIntent);
        finish();
    }

    /**
     * go to Login Screen
     */
    private void goToLogin() {
        Intent registerIntent = new Intent(RecordPreviewActivity.this, VizippDrawLogin.class);
        startActivityForResult(registerIntent, VizippDrawLogin.REQUEST_CODE_VIZIPP_LOGIN);
    }

    /**
     * Go to payment activity
     */
    private void moveForwardToPayment() {
        // move toward payment
    /* On 5 March 2018
    code commented for demo build, once demo completed uncomment it
       Intent paymentIntent = new Intent(RecordPreviewActivity.this, PaymentActivity.class);
        startActivityForResult(paymentIntent, VizippDrawLogin.REQUEST_CODE_VIZIPP_PAY);*/
    //temparory code --- remove it later
        showToast("Not available now");
        //temparory code
    }

    /**
     * show Upload option Dialog
     */
    private void showUploadOptionDialog() {
        if (getGlobalMethods().isInternetConnectivity(RecordPreviewActivity.this))
        {
            CustomDialogSlideFromBottom customDialogSlideFromBottom = new CustomDialogSlideFromBottom(RecordPreviewActivity.this, R.style.DialogAnimation,"Choose option to upload ");
            customDialogSlideFromBottom.setOnActionButtonListener(new CustomDialogSlideFromBottom.OnActionButtonListener() {
                @Override
                public void onOptionPressed(String optionType) {
                    switch (optionType) {
                        case CustomDialogSlideFromBottom.OPT_EDBRIX:
                            if (fileData != null) {
                                getUserProducts(fileData,vData.getId(),vData.getAccessToken());
                                //showCourseList(fileData);
                            }
                            break;
                        case CustomDialogSlideFromBottom.OPT_DRIVE:
                            authenticateGoogleClient();
                            break;
                        case CustomDialogSlideFromBottom.OPT_DROPBOX:
                            if (sessionManager.getSessionDropBoxToken() != null && sessionManager.getSessionDropBoxToken().length() > 0) {
                                showBusyProgress(" Upload to Dropbox in progress...");
                                new UploadFileToDropboxTask(DropboxClient.getClient(sessionManager.getSessionDropBoxToken()), fileData.getFileObject(), RecordPreviewActivity.this).execute();
                            } else {
                                isUploadDPBoxResultPending = true;
                                Auth.startOAuth2Authentication(getApplicationContext(), getString(R.string.DROPBOX_APP_KEY));
                            }
                            break;
                        case CustomDialogSlideFromBottom.OPT_ONE_DRIVE:
                            Uri orgUri = Uri.fromFile(fileData.getFileObject());
                            Log.e(RecordPreviewActivity.class.getName(),orgUri.toString());
                            mSaver.startSaving(RecordPreviewActivity.this, fileData.getFileName(),orgUri);
                            break;
                    }
                }
            });
            customDialogSlideFromBottom.showMe();
        } else {
            showToast(getResources().getString(R.string.no_internet_msg));
        }

    }

    private void showUpgradeProDialog(final boolean islimitOvered) {
        String message;
        String negBtnText;
        if (islimitOvered) {
            message = getString(R.string.upload_limit_finished_message);
            negBtnText = getString(R.string.cancel);
        } else {
            message = getString(R.string.login_upgrade_pro_message);
            negBtnText = getString(R.string.skip_it);
        }
        final InformatoryDialog informatoryDialog = new InformatoryDialog(RecordPreviewActivity.this, R.layout.upgrade_account_dialog, null, message, true,
                getString(R.string.upgrade_now), negBtnText, new InformatoryDialog.OnActionButtonListener() {
            @Override
            public void onPositiveButtonPressed() {
                if (sessionManager.getLoggedKnowledgeBrixUserData().getId() != null && sessionManager.getLoggedKnowledgeBrixUserData().getId().length() > 0) {
                    moveForwardToPayment();
                } else {
                    goToKnowledgeBrixLogin();
                }
            }

            @Override
            public void onNegativeButtonPressed() {
                if (!islimitOvered) {
                    showUploadOptionDialog();
                }

            }
        });
        informatoryDialog.showMe();
    }


    /**
     * Rename video file
     *
     * @param newFileName new file name without extension.
     */

    private void renameVideoFile(String newFileName)
    {
        File dir = VolleySingleton.getAppStorageDirectory();
        if (dir.exists()) {
            File fromObj = fileData.getFileObject();
//            File from = new File(videoFileUri.getPath());
            File toObj = new File(dir, newFileName+ ".mp4");
            if (fromObj.exists()) {
                boolean isRenamed = fromObj.renameTo(toObj);
                if (isRenamed) {
//                    title.setText(newFileName);
                    fileData = new FileData(toObj);
//                    fileData.setFileName(newFileName);
                    //showToast(getResources().getString(R.string.file_rename_success_msg));
                    showToast("Saved");
                }
            }
        }
    }

    /**
     * Open Video Player
     *
     * @param fileData FileData obj
     */
    private void openVideoPlayer(FileData fileData) {
        Intent videoPlayer = new Intent(RecordPreviewActivity.this, VideoPlayerActivity.class);
        videoPlayer.putExtra("FileData", fileData);
        startActivity(videoPlayer);
    }

    /**
     * call Login Page for Edbrix Enterprise Login
     */
    private void callLoginActivity() {
        Intent loginEdbrixIntent = new Intent(RecordPreviewActivity.this, EdbrixLoginActivity.class);
        startActivityForResult(loginEdbrixIntent, REQUEST_CODE_EDBRIX_LOGIN);
    }

    /**
     * call ChooseAccountActivity for account selection to upload
     */
    private void callChooseAccountActivity() {
        Intent chooseAcIntent = new Intent(RecordPreviewActivity.this, ChooseAccountActivity.class);
        startActivityForResult(chooseAcIntent, REQUEST_CODE_EDBRIX_CHOOSE_ACCOUNT);
    }

    private void getUserProducts(final FileData fData, String userId, String accessCode){
        final Map<String, Object> requestMap = new HashMap<String, Object>();
        requestMap.put("AccessToken", accessCode);
        requestMap.put("UserId", userId);
        Log.e("123",""+requestMap);

        try {
            showBusyProgress();
            String tempUrl = "https://services.edbrix.net/";
            JsonObjectRequest getuserproductaccessRequest = new JsonObjectRequest(Method.POST, tempUrl + "common/getuserproductaccess", new JSONObject(requestMap),
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            hideBusyProgress();
                            try {
                                Log.e(RecordPreviewActivity.class.getName(),""+response.getJSONArray("ProductAccessList").toString());
                                Userproductaccessresponse userproductaccessresponse = new Userproductaccessresponse();
                                userproductaccessresponse.setProductAccessList((List<ProductAccessList>) new Gson().fromJson(response.getJSONArray("ProductAccessList").toString(),new TypeToken<List<ProductAccessList>>(){}.getType()));
                                Log.e(RecordPreviewActivity.class.getName(),""+userproductaccessresponse.getProductAccessList().get(0).getTitle());

                                AlertDialog.Builder builderSingle = new AlertDialog.Builder(RecordPreviewActivity.this);
                                builderSingle.setIcon(R.drawable.app_logo_round_new72);
                                builderSingle.setTitle("Choose below:-");

                                final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(RecordPreviewActivity.this, android.R.layout.select_dialog_singlechoice);
                                for(int i=0;i<userproductaccessresponse.getProductAccessList().size();i++)
                                {  if(vData.getUserType().equals("T"))
                                        {
                                            if(userproductaccessresponse.getProductAccessList().get(i).getProductId().equals("6") && userproductaccessresponse.getProductAccessList().get(i).getHasAccess()==1){
                                                arrayAdapter.add(userproductaccessresponse.getProductAccessList().get(i).getTitle());
                                            }else if(userproductaccessresponse.getProductAccessList().get(i).getProductId().equals("4") && userproductaccessresponse.getProductAccessList().get(i).getHasAccess()==1){
                                                arrayAdapter.add(userproductaccessresponse.getProductAccessList().get(i).getTitle());
                                            }
                                        }else if(vData.getUserType().equals("A")) {
                                            if(userproductaccessresponse.getProductAccessList().get(i).getProductId().equals("8") && userproductaccessresponse.getProductAccessList().get(i).getHasAccess()==1){
                                                arrayAdapter.add(userproductaccessresponse.getProductAccessList().get(i).getTitle());
                                            }
                                        }
                                }
                                builderSingle.setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                });

                                builderSingle.setAdapter(arrayAdapter, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        String strName = arrayAdapter.getItem(which);
                                        showToast("Selected : "+strName);
                                        if(strName.equals("Knowledgebrix")){
                                            showKnowledgeBrixCourseList(fData);
                                        }else if(strName.equals("LMSbrix")){
                                            showLmsBrixCourseList(fData);
                                        }else if(strName.equals("Eval Brix")){
                                            showEvalBrixCourseList(fData);
                                        }

                                       /* AlertDialog.Builder builderInner = new AlertDialog.Builder(RecordPreviewActivity.this);
                                        builderInner.setMessage(strName);
                                        builderInner.setTitle("Your Selected Item is");
                                        builderInner.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog,int which) {
                                                dialog.dismiss();
                                            }
                                        });
                                        builderInner.show();*/
                                    }
                                });
                                builderSingle.show();

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

    private void showLmsBrixCourseList(FileData fData) {
        Intent courseList = new Intent(RecordPreviewActivity.this, CourseListActivity.class);
        courseList.putExtra("FileData", fData);
        startActivityForResult(courseList, REQUEST_CODE_RESOURCE_LIST);
    }
    private void showEvalBrixCourseList(FileData fData) {
        Intent courseList = new Intent(RecordPreviewActivity.this, CourseListEvalActivity.class);
        courseList.putExtra("FileData", fData);
        startActivityForResult(courseList, REQUEST_CODE_RESOURCE_LIST);
    }
    private void showKnowledgeBrixCourseList (FileData fData) {
        Intent courseList = new Intent(RecordPreviewActivity.this, CourseListKnowledgeBrixActivity.class);
        courseList.putExtra("FileData", fData);
        startActivityForResult(courseList, REQUEST_CODE_RESOURCE_LIST);
    }


    /**
     * upload file to Edbrix cloud storage
     */
    private void uploadToEdbrix() {
        try {

            Uri fileUri = Uri.fromFile(fileData.getFileObject());
            if (fileUri != null) {
                btnUpload.setVisibility(View.GONE);
                btnCancel.setVisibility(View.VISIBLE);

                mProgressBar.setVisibility(View.VISIBLE);
                textPercentage.setVisibility(View.VISIBLE);
                textPercentage.setText("");
                userId = sessionManager.getLoggedUserData().getId();
                accessToken = sessionManager.getSessionProfileToken();

                StorageReference childRef = storageRef.child("media/" + userId + "/" + fileData.getFileName());
                //uploading the image
                uploadTask = childRef.putFile(fileUri);

                uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        btnUpload.setVisibility(View.VISIBLE);
                        btnUpload.setEnabled(false);
                        btnCancel.setVisibility(View.GONE);

                        mProgressBar.setVisibility(View.GONE);
                        //uploadVideoToMyFiles(userId, accessToken, fileData.getFileName(),"18");
//                        RecordPreviewActivity
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.v("Upload", "Fail Exception :" + e.getMessage());
                        btnUpload.setEnabled(true);
                        btnUpload.setVisibility(View.VISIBLE);
                        btnCancel.setVisibility(View.GONE);

                        mProgressBar.setVisibility(View.GONE);
                        textPercentage.setText("Upload Failed.");
                    }
                }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                        double progress = (100 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                        mProgressBar.setProgress((int) progress);
                        textPercentage.setText("Uploading completed " + (int) progress + "%");
                    }
                });

            } else {
                showToast(getResources().getString(R.string.no_file_found));
            }
        } catch (Exception e) {
            Crashlytics.logException(e);
            Log.v(TAG, e.getMessage());
        }

    }

    /**
     * Upload files to Edbrix Instructor's  my files
     *
     * @param// userId
     * @param //accessToken
     * @param ///fileName
     */
   /* private void uploadVideoToMyFiles(final String userId, final String accessToken, final String fileName,final String courseId) {

        Map<String, String> requestMap = new HashMap<String, String>();
        requestMap.put("UserId", userId);
        requestMap.put("AccessToken", accessToken);
        requestMap.put("Content", fileName);
        requestMap.put("Type", "V");
        requestMap.put("CourseId", courseId);


        try {
            String tempUrl = "https://services.edbrix.net/";
            JsonObjectRequest uploadVideo = new JsonObjectRequest(Request.Method.POST, tempUrl + "course/createcoursematerial", new JSONObject(requestMap), new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    btnUpload.setEnabled(true);
                    btnUpload.setVisibility(View.VISIBLE);
                    btnCancel.setVisibility(View.GONE);

                    mProgressBar.setVisibility(View.GONE);
                    textPercentage.setText("File Uploaded successfully.");
                    sessionManager.updateVideoUploadCount(sessionManager.getVideoUploadCount() + 1);
                    Log.v("Volley Response", response.toString());
                    try {
                        if (response != null) {
                            if (response.has("Success")) {
//                                textPercentage.setText("Upload successful");
                            } else if (response.has("Error")) {
//                                textPercentage.setText("Error in web service.");
                            }
                        }
                    } catch (Exception e) {
                        Crashlytics.logException(e);
                        e.printStackTrace();
                        Log.v("Volley Excep", e.getMessage());
                        textPercentage.setText(getResources().getString(R.string.error_message));
                    }
                }

            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    btnUpload.setEnabled(true);
                    btnUpload.setVisibility(View.VISIBLE);
                    btnCancel.setVisibility(View.GONE);

                    mProgressBar.setVisibility(View.GONE);
                    Log.v("Volley VError", VolleySingleton.getErrorMessage(error));
                    textPercentage.setText(VolleySingleton.getErrorMessage(error));
                }

            }) {
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    HashMap<String, String> headers = new HashMap<String, String>();
                    headers.put("Content-Type", "application/json");
                    return headers;
                }

            };

            VolleySingleton.getInstance().addToRequestQueue(uploadVideo);
        } catch (Exception e) {
            Crashlytics.logException(e);
            btnUpload.setEnabled(true);
            btnUpload.setVisibility(View.VISIBLE);
            btnCancel.setVisibility(View.GONE);

            mProgressBar.setVisibility(View.GONE);
            Log.v("Volley Excep", e.getMessage());
            textPercentage.setText(getResources().getString(R.string.error_message));
        }
    }*/

    private void authenticateGoogleClient() {
        if (mGoogleApiClient == null) {
            mGoogleApiClient = startGoogleClientAuthentication(this, this);
            mGoogleApiClient.connect();
        } else {
            if (mGoogleApiClient.isConnected()) {
                saveFileToDrive();
            } else if (mGoogleApiClient.isConnecting()) {

            } else {
                mGoogleApiClient.connect();
            }
        }


    }

    private void saveFileToDrive() {
        // Start by creating a new contents, and setting a callback.
        Log.i(TAG, "Creating new contents.");

        Drive.DriveApi.newDriveContents(mGoogleApiClient)
                .setResultCallback(new ResultCallback<DriveApi.DriveContentsResult>() {

                    @Override
                    public void onResult(DriveApi.DriveContentsResult result) {
                        // If the operation was not successful, we cannot do anything
                        // and must
                        // fail.
                        if (!result.getStatus().isSuccess()) {
                            Log.i(TAG, "Failed to create new contents.");
                            return;
                        }
                        // Otherwise, we can write our data to the new contents.
                        Log.i(TAG, "New contents created.");
                        // Get an output stream for the contents.
                        OutputStream outputStream = result.getDriveContents().getOutputStream();
                        // Write the bitmap data from it.
//                        ByteArrayOutputStream bitmapStream = new ByteArrayOutputStream();
                        InputStream inputStream = null;
                        try {
                            inputStream = new FileInputStream(fileData.getFileObject());
                        } catch (FileNotFoundException e) {
                            Crashlytics.logException(e);

                        }

                        try {
                            byte[] buffer = new byte[8192];
                            int bytesRead;
                            while ((bytesRead = inputStream.read(buffer)) != -1) {
                                outputStream.write(buffer, 0, bytesRead);
                            }
//                            outputStream.write(bitmapStream.toByteArray());
                        } catch (IOException e1) {
                            Crashlytics.logException(e1);
                            Log.i(TAG, "Unable to write file contents.");
                        }
                        // Create the initial metadata - MIME type and title.
                        // Note that the user will be able to change the title later.
                        MetadataChangeSet metadataChangeSet = new MetadataChangeSet.Builder()
                                .setMimeType("video/mp4").setTitle(fileData.getFileName()).build();
                        // Create an intent for the file chooser, and start it.
                        IntentSender intentSender = Drive.DriveApi
                                .newCreateFileActivityBuilder()
                                .setInitialMetadata(metadataChangeSet)
                                .setInitialDriveContents(result.getDriveContents())
                                .build(mGoogleApiClient);
                        sessionManager.updateVideoUploadCount(sessionManager.getVideoUploadCount() + 1);
                        try {
                            startIntentSenderForResult(
                                    intentSender, REQUEST_CODE_CREATOR, null, 0, 0, 0);
                        } catch (IntentSender.SendIntentException e) {
                            Crashlytics.logException(e);
                            Log.i(TAG, "Failed to launch file chooser.");
                        }
                    }
                });
    }

    /**
     * It invoked when Google API client connected
     *
     * @param connectionHint
     */
    @Override
    public void onConnected(@Nullable Bundle connectionHint) {
        saveFileToDrive();
    }

    /**
     * It invoked when connection suspended
     *
     * @param cause
     */
    @Override
    public void onConnectionSuspended(int cause) {
        Log.i(TAG, "GoogleApiClient connection suspended");
    }

    /**
     * It invoked when connection failed
     *
     * @param result
     */
    @Override
    public void onConnectionFailed(ConnectionResult result) {

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

        try {

            result.startResolutionForResult(this, REQUEST_CODE_RESOLUTION);

        } catch (IntentSender.SendIntentException e) {
            Crashlytics.logException(e);
            Log.e(TAG, "Exception while starting resolution activity", e);
        }
    }

    /**
     * get UserSubscriptionDetails by calling web service
     */
    private void getUserSubscriptionDetails(final String userId, final String apiToken) {

        Map<String, String> requestMap = new HashMap<String, String>();
        requestMap.put("APITOKEN", apiToken);
        requestMap.put("userId", userId);

        try {
            showBusyProgress();
            JsonObjectRequest getUserSubscriptionDetailsRequest = new JsonObjectRequest(Method.POST, VolleySingleton.getWsBaseUrl() + "getusersubscriptiondetails.php", new JSONObject(requestMap), new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    hideBusyProgress();
                    Log.v("Volley Response", response.toString());
                    try {
                        if (response != null) {
                            if (response.has("status") && response.getString("status").equals("1")) {
                                if (response.has("userData")) {
                                    VizippUserData vizippUserData = getGlobalMethods().toObjects(response.getJSONObject("userData").toString(), VizippUserData.class);

                                    showUploadOptionDialog();// Added for v1.0.10 Demo version

                                   /*  Commented for v1.0.10 Demo version
                                   if (vizippUserData.isActive() && vizippUserData.isVerified() && vizippUserData.isSubscribed()) {
                                        showUploadOptionDialog();
                                    } else if (vizippUserData.isActive() && vizippUserData.isVerified() && !vizippUserData.isSubscribed()) {
                                        if (sessionManager.getVideoUploadCount() < UPLOAD_LIMIT) {
                                            showUpgradeProDialog(false);
                                        } else {
                                            showUpgradeProDialog(true);
                                        }
                                    }
                                    Commented for v1.0.10 Demo version
                                    */
                                }
                            } else if (response.has("status") && response.getString("status").equals("0")) {
                                getAlertDialogManager().Dialog("Error Code: " + response.getString("errorCode"), response.getString("message"), false, null).show();
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

            VolleySingleton.getInstance().addToRequestQueue(getUserSubscriptionDetailsRequest);
        } catch (Exception e) {
            Crashlytics.logException(e);
            hideBusyProgress();
            Log.v("Volley Excep", e.getMessage());
            showToast("Something went wrong. Please try again later.");
        }

    }

    @Override
    public void onUploadFileToDropboxComplete(FileMetadata result) {
        hideBusyProgress();
        sessionManager.updateVideoUploadCount(sessionManager.getVideoUploadCount() + 1);
        showToast("File uploaded successfully to your Dropbox Account.");
        isUploadDPBoxResultPending = false;
    }

    @Override
    public void onUploadFileToDropboxError(Exception e) {
        hideBusyProgress();
        if (e instanceof InvalidAccessTokenException) {
            showToast("Invalid Access Token. Do Authentication again");
            isUploadDPBoxResultPending = true;
            Auth.startOAuth2Authentication(getApplicationContext(), getString(R.string.DROPBOX_APP_KEY));
        } else {
            showToast("Dropbox Server Error :Unable to upload file.");
            isUploadDPBoxResultPending = false;
        }
    }
}
