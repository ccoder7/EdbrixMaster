package com.edbrix.contentbrix;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import androidx.appcompat.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.crashlytics.android.Crashlytics;
import com.dropbox.core.InvalidAccessTokenException;
import com.dropbox.core.android.Auth;
import com.dropbox.core.v2.files.FileMetadata;
import com.edbrix.contentbrix.adapters.RecordedVideoRecyclerViewAdapter;
import com.edbrix.contentbrix.baseclasses.BaseActivity;
import com.edbrix.contentbrix.commons.AlertDialogManager;
import com.edbrix.contentbrix.commons.GlobalMethods;
import com.edbrix.contentbrix.customview.CustomDialogSlideFromBottom;
import com.edbrix.contentbrix.data.FileData;
import com.edbrix.contentbrix.data.KnowledgeBrixUserData;
import com.edbrix.contentbrix.data.ProductAccessList;
import com.edbrix.contentbrix.data.Userproductaccessresponse;
import com.edbrix.contentbrix.dropbox.DropboxClient;
import com.edbrix.contentbrix.dropbox.UploadFileToDropboxTask;
import com.edbrix.contentbrix.utils.SessionManager;
import com.edbrix.contentbrix.volley.VolleySingleton;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi;
import com.google.android.gms.drive.MetadataChangeSet;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.ipaulpro.afilechooser.utils.FileUtils;
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.edbrix.contentbrix.RecordPreviewActivity.REQUEST_CODE_RESOURCE_LIST;

public class MainActivity extends BaseActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, UploadFileToDropboxTask.Callback{
    private CardView card_view;

    // Camera activity request codes
    private static final int CAMERA_CAPTURE_VIDEO_REQUEST_CODE = 200;
    private static final int SETTINGS_REQUEST_CODE = 1585;

    // LogCat tag
    private static final String TAG = MainActivity.class.getSimpleName();

    private RecyclerView recordedVideoRecyclerView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private LinearLayout btnRecordWhiteBoard;
    private LinearLayout btnRecordPDF;
    private LinearLayout btnRecordScreen;
    private LinearLayout btnRecordVideo;
    private GlobalMethods globalMethods;
    private List<FileData> fileDataList;
    private SessionManager sessionManager;
    private EditText searchBox;
    private ImageView rideIcon;
    //private ImageView deleteFiles;
    private Uri fileUri;
    private MenuItem deleteAllMenuItem;
    //private KnowledgeBrixUserData vData;

    private RecordedVideoRecyclerViewAdapter.OnButtonActionListener buttonActionListener;
    private RecordedVideoRecyclerViewAdapter recyclerViewAdapter;

    private GoogleApiClient mGoogleApiClient;
    private boolean isUploadDPBoxResultPending;
    private KnowledgeBrixUserData vData;
    private ISaver mSaver;

    private static final int REQUEST_CODE_CREATOR = 2;
    private static final int REQUEST_CODE_RESOLUTION = 3;

    private FileData tempFiledata;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (isTablet()) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE); // stop screen rotation on phones
        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        File yourAppStorageDir = new File(Environment.getExternalStorageDirectory(), "/" + getResources().getString(R.string.app_name) + "/");
        if (!yourAppStorageDir.exists()) {
            boolean isDirCreated = yourAppStorageDir.mkdirs();
            Log.d("ContentBrixApp", "App mediaStorageDirectory created :" + isDirCreated);
        }
        card_view = (CardView) findViewById(R.id.card_view);

        //initialize component
        globalMethods = new GlobalMethods();
        sessionManager = new SessionManager(MainActivity.this);
        searchBox = (EditText) findViewById(R.id.searchBox);
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeRefreshLayout);
        btnRecordWhiteBoard = (LinearLayout) findViewById(R.id.btnRecordWhiteBoard);
        btnRecordPDF = (LinearLayout) findViewById(R.id.btnRecordPDF);
        btnRecordScreen = (LinearLayout) findViewById(R.id.btnRecordScreen);
        btnRecordVideo = (LinearLayout) findViewById(R.id.btnRecordVideo);
        recordedVideoRecyclerView = (RecyclerView) findViewById(R.id.recordedVideoRecyclerView);
        rideIcon = (ImageView) findViewById(R.id.rideIcon);

        swipeRefreshLayout.setRefreshing(true);
        if (sessionManager.hasSessionVizippCredentials())
            vData = sessionManager.getLoggedKnowledgeBrixUserData();

        //set Listeners
        setListeners();

        mSaver = Saver.createSaver("8dc3bd17-8417-4083-9f37-d28d80986105"); //App Id From Microsoft

        if (globalMethods.checkAllPermission(MainActivity.this)) {
            // get Video List
            getRecordedVideoListFromLocalStorage();
            if (fileDataList.size() > 10) {
               /* AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setMessage("Please delete some recordings for better performance.")
                        .setNegativeButton("Ok", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.dismiss();
                            }
                        });
                builder.create();
                builder.show();*/
            }
        } else {
            String msg = globalMethods.requestAllPermission(MainActivity.this);
            showToast(msg);
        }
    }

    private void renameVideoFile(FileData fileData, String newFileName) {
        File dir = VolleySingleton.getAppStorageDirectory();
        if (dir.exists()) {
            File fromObj = fileData.getFileObject();
//            File from = new File(videoFileUri.getPath());
            File toObj = new File(dir, newFileName );//+ ".mp4");
            if (fromObj.exists()) {
                boolean isRenamed = fromObj.renameTo(toObj);
                if (isRenamed) {
//                    title.setText(newFileName);
                    fileData = new FileData(toObj);
//                    fileData.setFileName(newFileName);
                    //showToast(getResources().getString(R.string.file_rename_success_msg));
                    showToast("Saved");
                    getRecordedVideoListFromLocalStorage();
                }
            }
        }
    }

    /**
     * Defined all components listener
     */
    private void setListeners() {
        buttonActionListener = new RecordedVideoRecyclerViewAdapter.OnButtonActionListener() {
            @Override
            public void onDeleteButtonPressed(FileData fileData, int position) {
                deleteFile(fileData, position);
            }

            @Override
            public void onCardViewClicked(FileData fileData, int position) {
                /*Intent recordPreviewIntent = new Intent(MainActivity.this, RecordPreviewActivity.class);
                Bundle bundle = new Bundle();
                bundle.putSerializable("FileData", fileData);
                recordPreviewIntent.putExtras(bundle);
                startActivityForResult(recordPreviewIntent, RecordPreviewActivity.REQUEST_CODE);*/
                tempFiledata = fileData;
                showUploadOptionDialog();
            }

            @Override
            public void onRenameViewClicked(final FileData fileData, int position) {

                /// added code by pranav
                final AlertDialog dialogBuilder = new AlertDialog.Builder(MainActivity.this).create();
                LayoutInflater inflater = MainActivity.this.getLayoutInflater();
                final View dialogView = inflater.inflate(R.layout.activity_rename_file_dailog, null);

                final TextView txtViewRename = (TextView) dialogView.findViewById(R.id.txtViewRename);
                final EditText edtxtFileName = (EditText)dialogView.findViewById(R.id.edtxtFileName);
                final Button btnCancel = (Button)dialogView.findViewById(R.id.btnCancel);
                final Button btnOk = (Button)dialogView.findViewById(R.id.btnOk);

                edtxtFileName.setText(fileData.getFileName().substring(0, fileData.getFileName().lastIndexOf(".")));

                btnCancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialogBuilder.dismiss();
                    }
                });

                btnOk.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        String m_Text = edtxtFileName.getText().toString()+".mp4";
                        if(!edtxtFileName.getText().toString().equals("")){
                            renameVideoFile(fileData, m_Text);
                            fileData.setFileName(m_Text);
                            recyclerViewAdapter.notifyDataSetChanged();

                            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                            imm.hideSoftInputFromWindow(edtxtFileName.getWindowToken(), 0);
                            dialogBuilder.dismiss();
                        } else {
                            showToast("Please enter file name.");
                            edtxtFileName.setFocusable(true);
                        }

                    }
                });

                dialogBuilder.setView(dialogView);
                dialogBuilder.setCancelable(false);
                dialogBuilder.show();

                //-----------------------------------------------------------

                // commented by pranav 02082019
                /*//final String m_Text = "";
                final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("Rename");

                // Set up the input
                final EditText input = new EditText(MainActivity.this);
                // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
                //input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);

                //input.setText(fileData.getFileName().toString());
                input.setText(fileData.getFileName().substring(0, fileData.getFileName().lastIndexOf(".")));
                builder.setView(input);

                // Set up the buttons
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        String m_Text = input.getText().toString()+".mp4";
                        if(!input.getText().toString().equals("")){
                            renameVideoFile(fileData, m_Text);
                            fileData.setFileName(m_Text);
                            recyclerViewAdapter.notifyDataSetChanged();

                            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                            imm.hideSoftInputFromWindow(input.getWindowToken(), 0);

                        } else {
                            showToast("Please enter file name.");
                        }
//                        recordedVideoRecyclerView.notify();
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                builder.setCancelable(false);
                builder.create().show();*/
            }
        };

        btnRecordVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TypedValue outValue = new TypedValue();
                MainActivity.this.getTheme().resolveAttribute(android.R.attr.selectableItemBackground, outValue, true);
                btnRecordVideo.setBackgroundResource(outValue.resourceId);

                if (globalMethods.checkCameraAudioRecordPermission(MainActivity.this)) {
                    goToRecordVideo();
                } else {
                    String msg = globalMethods.requestCameraAudioWithReadWriteExternalStoragePermission(MainActivity.this);
                    showToast(msg);
                }
            }
        });

        btnRecordWhiteBoard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TypedValue outValue = new TypedValue();
                MainActivity.this.getTheme().resolveAttribute(android.R.attr.selectableItemBackground, outValue, true);
                btnRecordWhiteBoard.setBackgroundResource(outValue.resourceId);

                if (globalMethods.checkCameraAudioRecordPermission(MainActivity.this)) {
                    recordWhiteBoard();
                } else {
                    String msg = globalMethods.requestCameraAudioWithReadWriteExternalStoragePermission(MainActivity.this);
                    showToast(msg);
                }
            }
        });
        btnRecordPDF.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                TypedValue outValue = new TypedValue();
                MainActivity.this.getTheme().resolveAttribute(android.R.attr.selectableItemBackground, outValue, true);
                btnRecordPDF.setBackgroundResource(outValue.resourceId);

                if (globalMethods.checkAudioRecordPermission(MainActivity.this)) {
//                    recordPDFWithWhiteBoard();
                    showBusyProgress();
                    openDocumentFolder();
                } else {
                    String msg = globalMethods.requestAudioWithReadWriteExternalStoragePermission(MainActivity.this);
                    showToast(msg);
                }
            }
        });
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                swipeRefreshLayout.setRefreshing(true);
                getRecordedVideoListFromLocalStorage();
            }
        });
        searchBox.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                // TODO Auto-generated method stub
            }

            @Override
            public void afterTextChanged(Editable s) {

                // filter your list from your input
                filter(s.toString().toLowerCase());
                //you can use runnable postDelayed like 500 ms to delay search text
            }
        });
    }

    /**
     * show Upload option Dialog
     * @param tempFiledata
     */
    private void showUploadOptionDialog() {
        if (getGlobalMethods().isInternetConnectivity(MainActivity.this))
        {
            CustomDialogSlideFromBottom customDialogSlideFromBottom = new CustomDialogSlideFromBottom(MainActivity.this, R.style.DialogAnimation,"Choose option to upload "+tempFiledata.getFileName()+" video.");
            customDialogSlideFromBottom.setOnActionButtonListener(new CustomDialogSlideFromBottom.OnActionButtonListener() {
                @Override
                public void onOptionPressed(String optionType) {
                    switch (optionType) {
                        case CustomDialogSlideFromBottom.OPT_EDBRIX:
                            if (tempFiledata != null) {
                                getUserProducts(tempFiledata,vData.getId(),vData.getAccessToken());
                                //showCourseList(fileData);
                            }
                            break;
                        case CustomDialogSlideFromBottom.OPT_DRIVE:
                            authenticateGoogleClient(tempFiledata);
                            break;
                        case CustomDialogSlideFromBottom.OPT_DROPBOX:
                            if (sessionManager.getSessionDropBoxToken() != null && sessionManager.getSessionDropBoxToken().length() > 0) {
                                showBusyProgress(" Upload to Dropbox in progress...");
                                new UploadFileToDropboxTask(DropboxClient.getClient(sessionManager.getSessionDropBoxToken()), tempFiledata.getFileObject(), MainActivity.this).execute();
                            } else {
                                isUploadDPBoxResultPending = true;
                                Auth.startOAuth2Authentication(getApplicationContext(), getString(R.string.DROPBOX_APP_KEY));
                            }
                            break;
                        case CustomDialogSlideFromBottom.OPT_ONE_DRIVE:
                            Uri orgUri = Uri.fromFile(tempFiledata.getFileObject());
                            Log.e(RecordPreviewActivity.class.getName(),orgUri.toString());
                            mSaver.startSaving(MainActivity.this, tempFiledata.getFileName(),orgUri);
                            break;
                    }
                }
            });
            customDialogSlideFromBottom.showMe();
        } else {
            showToast(getResources().getString(R.string.no_internet_msg));
        }

    }

    private void authenticateGoogleClient(FileData fileData) {
        if (mGoogleApiClient == null) {
            mGoogleApiClient = startGoogleClientAuthentication(MainActivity.this, MainActivity.this);
            mGoogleApiClient.connect();
        } else {
            if (mGoogleApiClient.isConnected()) {
                tempFiledata = fileData;
                saveFileToDrive();
            } else if (mGoogleApiClient.isConnecting()) {

            } else {
                mGoogleApiClient.connect();
            }
        }
    }
    private void getUserProducts(final FileData fData, String userId, String accessCode){
        final Map<String, Object> requestMap = new HashMap<String, Object>();
        requestMap.put("AccessToken", accessCode);
        requestMap.put("UserId", userId);
        Log.e("123",""+requestMap);

        try {
            showBusyProgress();
            String tempUrl = "https://services.edbrix.net/";
            JsonObjectRequest getuserproductaccessRequest = new JsonObjectRequest(Request.Method.POST, tempUrl + "common/getuserproductaccess", new JSONObject(requestMap),
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            hideBusyProgress();
                            try {
                                Log.e(RecordPreviewActivity.class.getName(),""+response.getJSONArray("ProductAccessList").toString());
                                Userproductaccessresponse userproductaccessresponse = new Userproductaccessresponse();
                                userproductaccessresponse.setProductAccessList((List<ProductAccessList>) new Gson().fromJson(response.getJSONArray("ProductAccessList").toString(),new TypeToken<List<ProductAccessList>>(){}.getType()));
                                Log.e(RecordPreviewActivity.class.getName(),""+userproductaccessresponse.getProductAccessList().get(0).getTitle());

                                AlertDialog.Builder builderSingle = new AlertDialog.Builder(MainActivity.this);
                                builderSingle.setIcon(R.drawable.app_logo_round_new72);
                                builderSingle.setTitle("Choose below");

                                final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(MainActivity.this, android.R.layout.select_dialog_singlechoice);
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
                                        //showToast("Selected : "+strName);
                                        if(strName.equals("Knowledgebrix")){
                                            showKnowledgeBrixCourseList(fData);
                                        }else if(strName.equals("Assignbrix")){
                                            showLmsBrixCourseList(fData);
                                        }else if(strName.equals("Evalbrix")){
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
        Intent courseList = new Intent(MainActivity.this, CourseListActivity.class);
        courseList.putExtra("FileData", fData);
        startActivityForResult(courseList, REQUEST_CODE_RESOURCE_LIST);
    }
    private void showEvalBrixCourseList(FileData fData) {
        /*Intent courseList = new Intent(MainActivity.this, CourseListEvalActivity.class);
        courseList.putExtra("FileData", fData);
        startActivityForResult(courseList, REQUEST_CODE_RESOURCE_LIST);*/ //commented by pranav 01082019

        Intent categoryList = new Intent(MainActivity.this,EvalCategoryListActivity.class);
        categoryList.putExtra("FileData", fData);
        startActivityForResult(categoryList, REQUEST_CODE_RESOURCE_LIST);

    }
    private void showKnowledgeBrixCourseList (FileData fData) {
        Intent courseList = new Intent(MainActivity.this, CourseListKnowledgeBrixActivity.class);
        courseList.putExtra("FileData", fData);
        startActivityForResult(courseList, REQUEST_CODE_RESOURCE_LIST);
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
                            inputStream = new FileInputStream(tempFiledata.getFileObject());
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
                                .setMimeType("video/mp4").setTitle(tempFiledata.getFileName()).build();
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

    private void recordWhiteBoard() {
        Intent recordIntent = new Intent(MainActivity.this, RecordWhiteBoardActivity.class);
        startActivityForResult(recordIntent, RecordWhiteBoardActivity.RESULT_CODE);
    }

    private void recordPDFWithWhiteBoard() {
        Intent pdfIntent = new Intent(MainActivity.this, PDFListActivity.class);
        startActivityForResult(pdfIntent, PICKFILE_RESULT_CODE);
    }

    /**
     * Get recorded video list from apps local storage
     */
    private void getRecordedVideoListFromLocalStorage() {
        fileDataList = new ArrayList<>();
        File yourDir = VolleySingleton.getAppStorageDirectory();
        for (File file : yourDir.listFiles()) {
            Log.v(TAG, "File MIME Type:" + FileUtils.getMimeType(file));
            Log.v(TAG, "File MIME Ext:" + FileUtils.getExtension(file.getPath()));
            if (file.isFile()) {
                if (FileUtils.getMimeType(file).equals("video/mp4") && FileUtils.getExtension(file.getPath()).equals(".mp4")) {
                    fileDataList.add(new FileData(file));
                }
            }
        }
        ///shashank
        ////VideoSorting
        Collections.sort(fileDataList, new Comparator<FileData>() {
            public int compare(FileData f1, FileData f2) {
                return Long.compare(f1.getFileObject().lastModified(), f2.getFileObject().lastModified());
            }
        });
        Collections.reverse(fileDataList);

        if (fileDataList != null && fileDataList.size() > 0) {
            if (getScreenOrientation() == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
                recordedVideoRecyclerView.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));
            } else if (getScreenOrientation() == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
                if (isTablet()) {
                    recordedVideoRecyclerView.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));
                } else {
                    recordedVideoRecyclerView.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));
                }
            }
            if (deleteAllMenuItem != null) {
                deleteAllMenuItem.setVisible(true);
                searchBox.setVisibility(View.VISIBLE);
                rideIcon.setVisibility(View.VISIBLE);
            }
            // set Adapter
            recyclerViewAdapter = new RecordedVideoRecyclerViewAdapter(MainActivity.this, fileDataList, buttonActionListener);
            recordedVideoRecyclerView.setAdapter(recyclerViewAdapter);
            recordedVideoRecyclerView.setVisibility(View.VISIBLE);
            swipeRefreshLayout.setRefreshing(false);

        } else {
            if (deleteAllMenuItem != null) {
                deleteAllMenuItem.setVisible(false);
                searchBox.setVisibility(View.GONE);
                rideIcon.setVisibility(View.GONE);
            }
            recordedVideoRecyclerView.setVisibility(View.GONE);
            swipeRefreshLayout.setRefreshing(false);
            showToast("No recorded video found", Toast.LENGTH_SHORT);
        }
    }

    /**
     * Receiving activity result method will be called after closing the camera
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {

        if (requestCode == RecordWhiteBoardActivity.RESULT_CODE || requestCode == RecordPreviewActivity.REQUEST_CODE || requestCode == SETTINGS_REQUEST_CODE) {
            swipeRefreshLayout.setRefreshing(true);
            getRecordedVideoListFromLocalStorage();
        } else if (requestCode == PICKFILE_RESULT_CODE) {
            if (resultCode == RESULT_OK) {
                if (intent != null && intent.getData() != null) {
//                    openPDFRecordingScreen(intent.getData());
                    if (copyInputStreamToFile(intent.getData(), VolleySingleton.getTempPDFFile())) {
                        openPDFRecordingScreen(VolleySingleton.getTempPDFFile());
                    } else {
                        hideBusyProgress();
                        showToast("Something went wrong. Please try again later..!");
                    }
                } else {
//                    showToast("Something went wrong. Please try again later..!");
                }
            } else {
                hideBusyProgress();
                showToast("PDF recording cancelled.");
            }
        } else if (requestCode == CAMERA_CAPTURE_VIDEO_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {

                // video successfully recorded
                if (intent != null && intent.getData() != null) {

                    showToast("Video is recorded successfully.", Toast.LENGTH_SHORT);

                    String fileName = new File(intent.getData().getPath()).getName();
                    File mediaStorageDir = VolleySingleton.getAppStorageDirectory();

                    FileData fileData = new FileData(new File(mediaStorageDir.getPath() + File.separator + fileName));
                    Intent recordPreviewIntent = new Intent(MainActivity.this, RecordPreviewActivity.class);
                    recordPreviewIntent.putExtra("FileUri", intent.getData());
                    Bundle bundle = new Bundle();
                    bundle.putSerializable("FileData", fileData);
                    recordPreviewIntent.putExtras(bundle);

                    startActivityForResult(recordPreviewIntent, RecordPreviewActivity.REQUEST_CODE);

                } else {
                    // failed to record video
                    showToast("Sorry! Unable to fetch recorded video data.");
                }
            } else if (resultCode == RESULT_CANCELED) {
                // user cancelled recording
                //showToast("User cancelled video recording");

            }else if (requestCode == REQUEST_CODE_RESOLUTION) {
                if (resultCode == RESULT_OK) {
                    mGoogleApiClient.connect();
                }
            } else {
                // failed to record video
                showToast("Sorry! Failed to record video");
            }
        }else if(requestCode == REQUEST_CODE_CREATOR){
            showToast("Video uploaded on google drive.");
        }
    }

    private void openPDFRecordingScreen(Uri pdfUriData) {
        Intent pdfRecordingIntent = new Intent(MainActivity.this, RecordPDFOnBoardActivity.class);
        pdfRecordingIntent.setData(pdfUriData);
        startActivityForResult(pdfRecordingIntent, RecordPDFOnBoardActivity.RESULT_CODE);
    }

    private void openPDFRecordingScreen(File pdfFile) {
        Intent pdfRecordingIntent = new Intent(MainActivity.this, RecordPDFOnBoardActivity.class);
        pdfRecordingIntent.putExtra("pdf", pdfFile);
        startActivityForResult(pdfRecordingIntent, RecordPDFOnBoardActivity.RESULT_CODE);
        hideBusyProgress();
    }

    /**
     * Delete Video file by intimating user through alert box
     */
    private void deleteFile(final FileData fileDataObj, final int pos) {
        //fileDataObj.getFileName().replaceAll(".mp4", "")
        getAlertDialogManager().Dialog("Confirmation", getString(R.string.delete_video_msg), false, new AlertDialogManager.onTwoButtonClickListner() {
            @Override
            public void onNegativeClick() {

            }

            @Override
            public void onPositiveClick() {
                DeleteFileAsyncTask deleteFileAsyncTask = new DeleteFileAsyncTask();
                deleteFileAsyncTask.position = pos;
                deleteFileAsyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, fileDataObj.getFileObject());
            }
        }).show();
    }



    /**
     * AsyncTask for Delete Video file from selecting delete menu.
     */
    private class DeleteFileAsyncTask extends AsyncTask<File, Void, Boolean> {
        private int position;

        @Override
        protected Boolean doInBackground(File... params) {
            boolean isDeleted = false;
            if (params[0].exists()) {
                isDeleted = params[0].delete();
            }
            return isDeleted;
        }

        @Override
        protected void onPreExecute() {
            showBusyProgress();
        }

        @Override
        protected void onPostExecute(Boolean fileDeleted) {
            hideBusyProgress();
            if (fileDeleted) {
                showToast("Recording deleted successfully.", Toast.LENGTH_SHORT);
                ((RecordedVideoRecyclerViewAdapter) recordedVideoRecyclerView.getAdapter()).removeItem(position);
//                getRecordedVideoListFromLocalStorage();
            } else {
                showToast("Something went wrong. Please try again later");
            }


        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length > 0) {
            boolean grantedAll = false;
            for (int i = 0; i < grantResults.length; i++) {
                if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                    String msg = "Sorry now you can not perform this operation by denying any permission. Please try again later..!";
                    showToast(msg);
                    grantedAll = false;
                    break;
                } else {
                    grantedAll = true;
                }
            }
            if (grantedAll) {
                showResultRequestWise(requestCode);
            }
        } else {
            String msg = "Sorry now you can not perform this operation by denying permission. Please try again later..!";
            showToast(msg);
        }

    }

    private void showResultRequestWise(int requestCode) {
        switch (requestCode) {
            case GlobalMethods.camera_external_storage_permission_request_code:
                goToRecordVideo();
                break;
            case GlobalMethods.all_permission_request_code:
                // get Video List
                getRecordedVideoListFromLocalStorage();
                break;
            case GlobalMethods.camera_audio_external_storage_permission_request_code:
                // open white board
                recordWhiteBoard();
                break;
            case GlobalMethods.audio_external_storage_permission_request_code:
                //open pdf record white board
//                recordPDFWithWhiteBoard();
                openDocumentFolder();
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        deleteAllMenuItem = menu.findItem(R.id.deleteAllFilesOption);
        if (fileDataList != null && fileDataList.size() > 0) {
            deleteAllMenuItem.setVisible(true);
            searchBox.setVisibility(View.VISIBLE);
            rideIcon.setVisibility(View.VISIBLE);
        }
        else {
            deleteAllMenuItem.setVisible(false);
            searchBox.setVisibility(View.GONE);
            rideIcon.setVisibility(View.GONE);
        }

        return super.onCreateOptionsMenu(menu);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.deleteAllFilesOption:
                getAlertDialogManager().Dialog("Clear All", "Do you want to clear all files?", new AlertDialogManager.onTwoButtonClickListner() {
                    @Override
                    public void onNegativeClick() {
                    }

                    @Override
                    public void onPositiveClick() {
                        File dir = new File(Environment.getExternalStorageDirectory(), "/" + getResources().getString(R.string.app_name) + "/");
                        if (dir != null) {
                            // so we can list all files
                            File[] filenames = dir.listFiles();
                            // loop through each file and delete
                            for (File tmpf : filenames) {
                                tmpf.delete();
                            }
                            getRecordedVideoListFromLocalStorage();
                            showToast("All files are deleted.");
                        }
                    }
                }).show();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    void filter(String text) {
        if (fileDataList != null && fileDataList.size() > 0) {
            List<FileData> temp = new ArrayList();
            for (FileData fileData : fileDataList) {
                //or use .equal(text) with you want equal match
                //use .toLowerCase() for better matches
                if (fileData.getFileName().toLowerCase().contains(text)) {
                    temp.add(fileData);
                }
            }
            //update recyclerview
            if (recordedVideoRecyclerView.getAdapter() != null && recordedVideoRecyclerView.getAdapter().getItemCount() > 0)
                ((RecordedVideoRecyclerViewAdapter) recordedVideoRecyclerView.getAdapter()).updateList(temp);
        }
    }

    private void goToRecordVideo() {
        new SplashTimeoutTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, 1500);
    }

    /**
     * Launching camera app to record video
     */
    private void recordVideo() {

        Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);

        fileUri = FileProvider.getUriForFile(MainActivity.this, BuildConfig.APPLICATION_ID + ".provider", globalMethods.getOutputMediaFile(MainActivity.this));

//        fileUri = Uri.fromFile(globalMethods.getOutputMediaFile(MainActivity.this));

        // set video quality
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);
        intent.putExtra(MediaStore.EXTRA_DURATION_LIMIT, 3600);
        intent.putExtra(MediaStore.EXTRA_SCREEN_ORIENTATION, ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri); // set the image file
        // name

        // start the video capture Intent
        startActivityForResult(intent, CAMERA_CAPTURE_VIDEO_REQUEST_CODE);
    }

    private class SplashTimeoutTask extends AsyncTask<Integer, Void, Void> {

        private static final int TimeoutSleepInterval = 200;

        @Override
        protected void onPreExecute() {
            showToast("You can record video upto 60 min only.", Toast.LENGTH_SHORT);
        }

        @Override
        protected Void doInBackground(Integer... params) {
            int count = 0;
            int maxCount = params[0] / TimeoutSleepInterval;

            while (count < maxCount) {
                try {
                    Thread.sleep(TimeoutSleepInterval);
                } catch (InterruptedException e) {
                    ; // Ignore
                }
                count++;
                Log.v("Splash", "Counter :" + count);
                if (this.isCancelled()) {
                    break;
                }

            }

            return null;
        }


        @Override
        protected void onPostExecute(Void result) {
//            cancelToast();
            if (sessionManager.getSessionVibratePhoneState())
                globalMethods.vibratePhone(MainActivity.this, 500);

            recordVideo();

        }
    }

    public void checkIsVedioListEmpty(){
        if (fileDataList != null && fileDataList.size() > 0) {
            deleteAllMenuItem.setVisible(true);
            searchBox.setVisibility(View.VISIBLE);
            rideIcon.setVisibility(View.VISIBLE);
        }
        else {
            deleteAllMenuItem.setVisible(false);
            searchBox.setVisibility(View.GONE);
            rideIcon.setVisibility(View.GONE);
        }
    }
}
