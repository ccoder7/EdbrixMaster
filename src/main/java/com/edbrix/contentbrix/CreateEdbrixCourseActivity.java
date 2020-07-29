package com.edbrix.contentbrix;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.crashlytics.android.Crashlytics;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.edbrix.contentbrix.baseclasses.BaseActivity;
import com.edbrix.contentbrix.data.CoursesData;
import com.edbrix.contentbrix.data.FileData;
import com.edbrix.contentbrix.data.KnowledgeBrixUserData;
import com.edbrix.contentbrix.utils.SessionManager;
import com.edbrix.contentbrix.volley.VolleySingleton;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class CreateEdbrixCourseActivity extends BaseActivity
{
    // LogCat tag
    private static final String TAG = CreateEdbrixCourseActivity.class.getSimpleName();
    private Button btnCreateCourse;
    private EditText edtCourseTitle;
    private EditText edtSubject;
    private EditText edtBoard;
    private EditText edtContentTitle;

    private ProgressBar mProgressBar;
    private TextView textPercentage;
    private TextView txtFileName;
    private SessionManager sessionManager;

    private FirebaseStorage storage;
    private StorageReference storageRef;
    private UploadTask uploadTask;

    private FileData fileData;

    //private VizippUserData vData;
    private KnowledgeBrixUserData vData;

    private CoursesData courseData;

    private boolean isNewCourseCreate;

    private int courseId = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_edbrix_course);

        sessionManager = new SessionManager(CreateEdbrixCourseActivity.this);

        isNewCourseCreate = getIntent().getBooleanExtra("createNewCourse", false);
        fileData = (FileData) getIntent().getSerializableExtra("FileData");

        if (!isNewCourseCreate)
            courseData = (CoursesData) getIntent().getSerializableExtra("CourseData");

        if (sessionManager.hasSessionVizippCredentials())
            vData = sessionManager.getLoggedKnowledgeBrixUserData();

        storage = FirebaseStorage.getInstance("gs://edbrix-school-api-storage");
        storageRef = storage.getReferenceFromUrl("gs://edbrix-school-api-storage/");

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        TextView title = toolbar.findViewById(R.id.title);
        title.setText(getResources().getString(R.string.create_course));

        edtCourseTitle = (EditText) findViewById(R.id.edtCourseTitle);
        edtSubject = (EditText) findViewById(R.id.edtSubject);
        edtBoard = (EditText) findViewById(R.id.edtBoard);
        edtContentTitle = (EditText) findViewById(R.id.edtContentTitle);

        txtFileName = (TextView) findViewById(R.id.txtFileName);
        txtFileName.setText(fileData.getFileName());
        textPercentage = (TextView) findViewById(R.id.txtPercentage);
        mProgressBar = (ProgressBar) findViewById(R.id.mProgressBar);

        btnCreateCourse = (Button) findViewById(R.id.btnCreateCourse);
        btnCreateCourse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(edtContentTitle.getWindowToken(), 0);
                if (validate()) {
                    uploadToEdbrix();
                }
            }
        });

        edtCourseTitle.setEnabled(true);
        if (!isNewCourseCreate) {
            if (courseData != null) {
                courseId = Integer.parseInt(courseData.getId());
                edtCourseTitle.setText(courseData.getTitle());
                edtCourseTitle.setVisibility(View.GONE);
                edtSubject.setVisibility(View.GONE);
                edtBoard.setVisibility(View.GONE);
                title.setText(courseData.getTitle());
                btnCreateCourse.setText(getResources().getString(R.string.add_to_course));
            }
        }
    }

    /**
     * upload file to Edbrix cloud storage
     */
    private void uploadToEdbrix()
    {
        try {

            Uri fileUri = Uri.fromFile(fileData.getFileObject());
            if (fileUri != null) {

                edtCourseTitle.setEnabled(false);
                edtContentTitle.setEnabled(false);
                btnCreateCourse.setEnabled(false);
                edtSubject.setEnabled(false);
                edtBoard.setEnabled(false);

                btnCreateCourse.setText("Please wait...");
                mProgressBar.setVisibility(View.VISIBLE);
                textPercentage.setVisibility(View.VISIBLE);
                textPercentage.setText("");

                StorageReference childRef = storageRef.child("knowledgebrix/" + vData.getId() + "/" + fileData.getFileName());
                //uploading the image
                uploadTask = childRef.putFile(fileUri);

                uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        mProgressBar.setVisibility(View.GONE);
                        edtCourseTitle.setEnabled(true);
                        edtContentTitle.setEnabled(true);
                        btnCreateCourse.setEnabled(true);

                        edtSubject.setEnabled(true);
                        edtBoard.setEnabled(true);
                        //09
                        if (isNewCourseCreate) {
                            btnCreateCourse.setText(getString(R.string.create));
                        } else {
                            btnCreateCourse.setText(getString(R.string.add_to_course));
                        }
                        createCourseResourse();

                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.v("Upload", "Fail Exception :" + e.getMessage());
                        mProgressBar.setVisibility(View.GONE);
                        edtCourseTitle.setEnabled(true);
                        edtContentTitle.setEnabled(true);
                        btnCreateCourse.setEnabled(true);

                        edtSubject.setEnabled(true);
                        edtBoard.setEnabled(true);

                        if (isNewCourseCreate) {
                            btnCreateCourse.setText(getString(R.string.create));
                        } else {
                            btnCreateCourse.setText(getString(R.string.add_to_course));
                        }
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

    /*
    validation
   */
    private boolean validate() {
        boolean bln = true;
        String courseName = this.edtCourseTitle.getText().toString().trim();
        String contentName = this.edtContentTitle.getText().toString().trim();

        if (courseName.length() == 0) {
            edtCourseTitle.setBackgroundResource(R.drawable.red_border);
            edtCourseTitle.setHint(getResources().getString(R.string.resource_name_blank));
            showToast(getResources().getString(R.string.resource_name_blank));
            bln = false;
        } else if (contentName.length() == 0) {
            edtContentTitle.setBackgroundResource(R.drawable.red_border);
            edtContentTitle.setHint(getResources().getString(R.string.content_name_blank));
            showToast(getResources().getString(R.string.content_name_blank));
            bln = false;
        }
        return bln;
    }

    private void createCourseResourse() {
        String courseName = this.edtCourseTitle.getText().toString().trim();
        String contentName = this.edtContentTitle.getText().toString().trim();
        String subject = this.edtSubject.getText().toString().trim();
        String board = this.edtBoard.getText().toString().trim();
        String tags = "";
        if (!subject.isEmpty() && board.isEmpty()) {
            tags = subject;
        } else if (subject.isEmpty() && !board.isEmpty()) {
            tags = board;
        } else if (!subject.isEmpty() && !board.isEmpty()) {
            tags = subject + "," + board;
        }
        if (vData != null) {
            //createVideoCourse(vData.getEdbrixUserId(), vData.getEdbrixUserAccessToken(), courseId, courseName, contentName, fileData.getFileName(), "1", tags);
            if(isNewCourseCreate) {
                createVideoCourse(vData.getId(), vData.getAccessToken(), courseName, contentName, fileData.getFileName(),"");
            }else{
                updateCourseVideo(vData.getId(),courseId,vData.getAccessToken(),contentName,fileData.getFileName());
            }
        }
    }

    private void createVideoCourse(String userId, String accessCode, String courseTitle, String contentTitle, String fileName, String course_Access_code )
    {
        final Map<String, Object> requestMap = new HashMap<String, Object>();
        /*requestMap.put("userid", userId);
        requestMap.put("accesstoken", accessCode);
        requestMap.put("courseid", courseId);
        requestMap.put("course_title", courseTitle);
        requestMap.put("content_title", contentTitle);
        requestMap.put("filename", fileName);
        requestMap.put("isResource", isResource);
        requestMap.put("tags", tags);*/

        requestMap.put("AccessToken", accessCode);
        requestMap.put("UserId", userId);
        requestMap.put("Title", courseTitle);
        requestMap.put("Content", fileName);
        requestMap.put("ContentTitle", contentTitle);
        requestMap.put("AccessCode", course_Access_code);

        Log.e("123",""+requestMap);

        try {
            showBusyProgress();
            String tempUrl = "https://services.edbrix.net/";
            JsonObjectRequest createCourseRequest = new JsonObjectRequest(Request.Method.POST, tempUrl+"contentbrix/createcourse", new JSONObject(requestMap),
                    new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response)
                {
                    hideBusyProgress();
                    Log.v(CreateEdbrixCourseActivity.class.getName(), "Create Course Res : "+response.toString());
                    try
                    {
                        if (response != null)
                        {
                            if (response.has("Error"))
                            {
                                showToast(response.getString("ErrorMessage"));
                                finish();
                                /*int error = response.getInt("error");
                                if (error == 0)
                                {

                                    if (isNewCourseCreate) {
                                        showToast("Resource created & content added successfully.");
                                    } else {
                                        showToast("Content added successfully.");
                                    }
                                    sessionManager.updateVideoUploadCount(sessionManager.getVideoUploadCount() + 1);
                                    setResult(RESULT_OK);
                                    finish();
                                } else {
                                    showToast(response.getString("ErrorMessage"));
                                    finish();
                                }*/

                            }else if(response.has("Success")){
                                int success = response.getInt("Success");
                                if(success ==1){
                                    if (isNewCourseCreate)
                                    {
                                        showToast("Course created & content added successfully.");
                                    } else {
                                        showToast("Content added successfully.");
                                    }
                                    sessionManager.updateVideoUploadCount(sessionManager.getVideoUploadCount() + 1);
                                    setResult(RESULT_OK);
                                    finish();
                                }
                            }else{
                                showToast("Something went wrong. Please try again later.");
                            }
                        }
                    } catch (JSONException e) {
                        Crashlytics.logException(e);
                        showToast("Exception :" + e.getMessage());
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

            createCourseRequest.setRetryPolicy(VolleySingleton.getDefaultRetryPolice());
            createCourseRequest.setShouldCache(false);
            VolleySingleton.getInstance().addToRequestQueue(createCourseRequest, "create_resource_request");
        } catch (Exception e) {
            Crashlytics.logException(e);
            hideBusyProgress();
            Log.v("Volley Excep", e.getMessage());
            showToast("Something went wrong. Please try again later.");
        }
    }

    private void updateCourseVideo(String userId,int courseId,String accessCode,String courseTitle,String fileName){
        final Map<String, Object> requestMap = new HashMap<String, Object>();
        /*requestMap.put("userid", userId);
        requestMap.put("accesstoken", accessCode);
        requestMap.put("courseid", courseId);
        requestMap.put("course_title", courseTitle);
        requestMap.put("content_title", contentTitle);
        requestMap.put("filename", fileName);
        requestMap.put("isResource", isResource);
        requestMap.put("tags", tags);*/

        requestMap.put("AccessToken", accessCode);
        requestMap.put("UserId", userId);
        requestMap.put("CourseId", courseId);
        requestMap.put("Title", courseTitle);
        requestMap.put("Type", "V");
        requestMap.put("Content", fileName);
        requestMap.put("ContentType", "file");

        //requestMap.put("ContentTitle", contentTitle);
        //requestMap.put("AccessCode", course_Access_code);

        Log.e("123",""+requestMap);

        try {
            showBusyProgress();
            String tempUrl = "https://services.edbrix.net/";
            JsonObjectRequest createCourseRequest = new JsonObjectRequest(Request.Method.POST, tempUrl+"contentbrix/createcoursematerial", new JSONObject(requestMap), new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response)
                {
                    hideBusyProgress();
                    Log.v(CreateEdbrixCourseActivity.class.getName(), "Create createcoursematerial : "+response.toString());
                    try
                    {
                        if (response != null)
                        {
                            if (response.has("Error"))
                            {
                                showToast(response.getString("ErrorMessage"));
                                finish();

                            }else if(response.has("Success")){
                                int success = response.getInt("Success");
                                if(success ==1){
                                    if (isNewCourseCreate)
                                    {
                                        showToast("Course created & content added successfully.");
                                    } else {
                                        showToast("Content added successfully.");
                                    }
                                    sessionManager.updateVideoUploadCount(sessionManager.getVideoUploadCount() + 1);
                                    setResult(RESULT_OK);
                                    finish();
                                }
                            }else{
                                showToast("Something went wrong. Please try again later.");
                            }
                        }
                    } catch (JSONException e) {
                        Crashlytics.logException(e);
                        showToast("Exception :" + e.getMessage());
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

            createCourseRequest.setRetryPolicy(VolleySingleton.getDefaultRetryPolice());
            createCourseRequest.setShouldCache(false);
            VolleySingleton.getInstance().addToRequestQueue(createCourseRequest, "create_resource_request");
        } catch (Exception e) {
            Crashlytics.logException(e);
            hideBusyProgress();
            Log.v("Volley Excep", e.getMessage());
            showToast("Something went wrong. Please try again later.");
        }
    }
}

