package com.edbrix.contentbrix;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import androidx.annotation.NonNull;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.os.Bundle;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.crashlytics.android.Crashlytics;
import com.edbrix.contentbrix.adapters.CourseListAdapter;
import com.edbrix.contentbrix.baseclasses.BaseActivity;
import com.edbrix.contentbrix.data.CourseListResponseData;
import com.edbrix.contentbrix.data.CoursesData;
import com.edbrix.contentbrix.data.FileData;
import com.edbrix.contentbrix.data.KnowledgeBrixUserData;
import com.edbrix.contentbrix.utils.SessionManager;
import com.edbrix.contentbrix.volley.GsonRequest;
import com.edbrix.contentbrix.volley.VolleySingleton;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class CourseListEvalActivity extends BaseActivity {

    public  final static int requestCode=205;
    private Context context;
    private RecyclerView courseListRecyclerView;
    private LinearLayout noResourceLayout;
    private SwipeRefreshLayout swipeRefreshLayout;
    private CourseListAdapter courseListAdapter;
    private ArrayList<CoursesData> coursesArrayList;
    private SessionManager sessionManager;
    private int pageNo;
    private String dataType;
    private FileData fileData;
    private KnowledgeBrixUserData userData;
    private String categoryId = "";

    private FirebaseStorage storage;
    private StorageReference storageRef;
    private UploadTask uploadTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course_list_eval);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        Intent intent = getIntent();
        categoryId = intent.getStringExtra("CategoryId");
        Log.e(CourseListEvalActivity.class.getName(),"CategoryId : "+intent.getStringExtra("CategoryId"));
        sessionManager = new SessionManager(CourseListEvalActivity.this);
        fileData = (FileData) getIntent().getSerializableExtra("FileData");
        userData = sessionManager.getLoggedKnowledgeBrixUserData();

        context = CourseListEvalActivity.this;
        storage = FirebaseStorage.getInstance("gs://edbrix-school-api-storage");
        storageRef = storage.getReferenceFromUrl("gs://edbrix-school-api-storage/");

        coursesArrayList = new ArrayList<>();
        noResourceLayout = (LinearLayout) findViewById(R.id.noResourceLayout);
        courseListRecyclerView = (RecyclerView) findViewById(R.id.courseListRecyclerView);
        courseListRecyclerView.setLayoutManager(new LinearLayoutManager(context));
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                swipeRefreshLayout.setRefreshing(true);
                coursesArrayList.clear();
                pageNo = 0;
                getCourseList(userData.getId(),userData.getAccessToken(),userData.getUserType(),"mob", dataType, pageNo);
            }
        });
        courseListAdapter = new CourseListAdapter(context, courseListRecyclerView, coursesArrayList);
        setCourseListAdapter();
        courseListAdapter.setCourseListActionListener(new CourseListAdapter.CourseListActionListener()
        {
            @Override
            public void onCourseItemSelected(CoursesData courses)
            {
                /*Intent createCourse = new Intent(CourseListActivity.this, CreateEdbrixCourseActivity.class);
                createCourse.putExtra("FileData", fileData);
                createCourse.putExtra("CourseData", courses);
                createCourse.putExtra("createNewCourse", false);
                startActivityForResult(createCourse,requestCode);*/
                showConformationDialog(fileData,courses);
            }
        });
        getCourseList(userData.getId(),userData.getAccessToken(),"I","mob", dataType, pageNo);

    }
    public void showConformationDialog(final FileData fileData, final CoursesData courses) {
        final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);

        LayoutInflater inflater = this.getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.custom_createcoursecontent_dialog, null);
        dialogBuilder.setView(dialogView);
        final AlertDialog alertDialog  = dialogBuilder.create();

        final TextView confirmTextviewView = (TextView)dialogView.findViewById(R.id.dig_txtconformation);
        final EditText edtContentTitle = (EditText) dialogView.findViewById(R.id.dig_edtContentTitle);
        final EditText edtContentDescription = (EditText) dialogView.findViewById(R.id.dig_edtContentDescription);
        final Button btnCreateCourseContent = (Button) dialogView.findViewById(R.id.btnCreateCourseContent);

        confirmTextviewView.setText("Do you want to upload the content to "+courses.getTitle()+" Course?");
        btnCreateCourseContent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //createevalbrixcoursecontent(String userId,int courseId,String accessCode,String courseTitle,String fileName)
                //uploadToEdbrix();
                if(edtContentTitle.getText().toString().equals("")){
                    showToast("Please Enter The Title");
                }else{
                    showBusyProgress();
                    uploadToEdbrix(userData.getId(),
                            courses.getId(),
                            userData.getAccessToken(),
                            edtContentTitle.getText().toString(),
                            edtContentDescription.getText().toString(),
                            fileData.getFileName(),
                            courses.getTitle());
                    alertDialog.dismiss();
                }

                //finalAlertDialog.dismiss();
               // CourseListEvalActivity.this.finish();
            }
        });
        /*dialogBuilder.setTitle("Custom dialog");
        dialogBuilder.setMessage("Enter text below");
        dialogBuilder.setPositiveButton("Done", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                //do something with edt.getText().toString();
            }
        });
        dialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                //pass
            }
        });*/

        alertDialog.show();



    }

    private void setCourseListAdapter() {
        courseListRecyclerView.setAdapter(courseListAdapter);
        courseListRecyclerView.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // if the result is capturing Image
        if (requestCode == this.requestCode) {
            if (resultCode == RESULT_OK) {
               /* swipeRefreshLayout.setRefreshing(true);
                coursesArrayList.clear();
                pageNo = 0;
                getCourseList(userData.getEdbrixUserId(),userData.getEdbrixUserAccessToken(),"I","mob", dataType, pageNo);*/
                setResult(RESULT_OK);
                finish();
            }
        }
    }

    /**
     * Get course list from server and load data
     *
     * @param userId user ID
     * @param accessToken accessToken
     * @param userType user Type
     * @param deviceType Device type i.e. mob and tablet
     * @param dataType   Data type i.e. course, meeting and all
     * @param pageNo     page no i.e. 1,2,..etc. for pagination
     */
    private void getCourseList(String userId,String accessToken,String userType, String deviceType, String dataType, int pageNo) {
        try {
            JSONObject jo = new JSONObject();

            jo.put("UserId", userId);
            jo.put("AccessToken", accessToken);
            jo.put("CategoryId",categoryId);
            /*jo.put("UserType", userType);
            jo.put("DeviceType", deviceType);
            jo.put("DataType", dataType);
            jo.put("isResource", "1");
            jo.put("Page", pageNo);
            Log.e("123",""+jo.toString());*/

            String tempUrl = "https://services.edbrix.net/";
            //GsonRequest<CourseListResponseData> getDashboardCourseSchedulesRequest = new GsonRequest<>(Request.Method.POST, "http://enterpriseservices.edbrix.net/app/student/getdashboardcourseandschedules.php", jo.toString(), CourseListResponseData.class,
            //GsonRequest<CourseListResponseData> getDashboardCourseSchedulesRequest = new GsonRequest<>(Request.Method.POST, tempUrl+"knowledgebrix/getcourseslist", jo.toString(), CourseListResponseData.class,
            GsonRequest<CourseListResponseData> getDashboardCourseSchedulesRequest = new GsonRequest<>(Request.Method.POST,
                    tempUrl+"contentbrix/getevalbrixcourselist",
                    jo.toString(),
                    CourseListResponseData.class,
                    new Response.Listener<CourseListResponseData>()
                    {
                        @Override
                        public void onResponse(@NonNull CourseListResponseData response) {
                            swipeRefreshLayout.setRefreshing(false);
                            Log.e(CourseListEvalActivity.class.getName(),""+response.toString());
                            Log.e(CourseListEvalActivity.class.getName(),""+response.getCoursesList().size());

                           /* if (response.getCode() != null && response.getCode().length() > 0) {
                                showToast(response.getMessage());
                            } else {*/
                            if (response.getCoursesList() != null && response.getCoursesList().size() >=0 )
                            {
//                                    //Remove loading item
                                courseListRecyclerView.setVisibility(View.VISIBLE);
                                if (coursesArrayList.size() > 0) {
                                    coursesArrayList.remove(coursesArrayList.size() - 1);
                                    courseListAdapter.notifyItemRemoved(coursesArrayList.size());
                                }
                                coursesArrayList.addAll(response.getCoursesList());
                                courseListAdapter.notifyDataSetChanged();
                                courseListAdapter.setLoaded();

                                if(coursesArrayList.isEmpty()){
                                    courseListRecyclerView.setVisibility(View.GONE);
                                }else{
                                    courseListRecyclerView.setVisibility(View.VISIBLE);
                                }
                            }else{
//                                    showToast("No courses found.");
                                courseListRecyclerView.setVisibility(View.GONE);
                            }
                            //}
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    swipeRefreshLayout.setRefreshing(false);
                    showToast(VolleySingleton.getErrorMessage(error));
                }
            });
            getDashboardCourseSchedulesRequest.setRetryPolicy(VolleySingleton.getDefaultRetryPolice());
            getDashboardCourseSchedulesRequest.setShouldCache(false);
            VolleySingleton.getInstance().addToRequestQueue(getDashboardCourseSchedulesRequest, "getdashboardcourseandschedules");
        } catch (JSONException e) {
            Crashlytics.logException(e);
            swipeRefreshLayout.setRefreshing(false);
            showToast("Something went wrong. Please try again later.");
        }
    }
   /* createevalbrixcoursecontent(userData.getId(),
                                    courses.getId(),
                                    userData.getAccessToken(),
                                    edtContentTitle.getText().toString(),
                                    edtContentDescription.getText().toString(),
                                    fileData.getFileName());*/
   private void createevalbrixcoursecontent(String userId, String courseId, String accessToken, String ContentTitle,
                                            String ContentDescription, String fileName, final String CourseTitle) {
       final Map<String, Object> requestMap = new HashMap<String, Object>();
       requestMap.put("AccessToken", accessToken);
       requestMap.put("UserId", userId);
       requestMap.put("CourseId", courseId);
       requestMap.put("Title", ContentTitle);
       requestMap.put("Description", ContentDescription);
       requestMap.put("Type", "VC");
       requestMap.put("Content", fileName);
       requestMap.put("ContentType", "file");
       Log.e("123", "" + requestMap);

       try {

           String tempUrl = "https://services.edbrix.net/";
           JsonObjectRequest createCourseRequest = new JsonObjectRequest(Request.Method.POST, tempUrl + "contentbrix/createevalbrixcoursecontent", new JSONObject(requestMap), new Response.Listener<JSONObject>() {
               @Override
               public void onResponse(JSONObject response) {
                   hideBusyProgress();
                   Log.v(CourseListEvalActivity.class.getName(), "Create createcoursematerial : " + response.toString());
                   try {
                       if (response != null) {
                           if (response.has("Error")) {
                               showToast(response.getString("ErrorMessage"));
                               finish();

                           } else if (response.has("Success")) {
                               int success = response.getInt("Success");
                               if (success == 1) {
                                   showToast("Video uploaded successfully in "+CourseTitle+" library.");
                                   sessionManager.updateVideoUploadCount(sessionManager.getVideoUploadCount() + 1);
                                   setResult(RESULT_OK);
                                   finish();
                               }
                           } else {
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

    private void uploadToEdbrix(final String id, final String coursesId, final String accessToken, final String ContentTitle,
                                final String ContentDescription, final String fileName, final String CourseTitle) {
        try {
            Uri fileUri = Uri.fromFile(fileData.getFileObject());
            if (fileUri != null) {

                /*edtCourseTitle.setEnabled(false);
                edtContentTitle.setEnabled(false);
                btnCreateCourse.setEnabled(false);
                edtSubject.setEnabled(false);
                edtBoard.setEnabled(false);
                btnCreateCourse.setText("Please wait..");
                mProgressBar.setVisibility(View.VISIBLE);
                textPercentage.setVisibility(View.VISIBLE);
                textPercentage.setText("");*/

                StorageReference childRef = storageRef.child("knowledgebrix/" + userData.getId() + "/" + fileData.getFileName());
                uploadTask = childRef.putFile(fileUri);

                uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        createevalbrixcoursecontent(id,coursesId,accessToken,ContentTitle,ContentDescription,fileName,CourseTitle);
                       /* mProgressBar.setVisibility(View.GONE);
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
                        }*/


                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.v("Upload", "Fail Exception :" + e.getMessage());
                        /*mProgressBar.setVisibility(View.GONE);
                        edtCourseTitle.setEnabled(true);
                        edtContentTitle.setEnabled(true);
                        btnCreateCourse.setEnabled(true);

                        edtSubject.setEnabled(true);
                        edtBoard.setEnabled(true);

                        if (isNewCourseCreate) {
                            btnCreateCourse.setText(getString(R.string.create));
                        } else {
                            btnCreateCourse.setText(getString(R.string.add_to_course));
                        }*/
                        //textPercentage.setText("Upload Failed.");
                        showToast("Upload Failed");
                    }
                }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                        double progress = (100 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                        //mProgressBar.setProgress((int) progress);
                        //textPercentage.setText("Uploading completed " + (int) progress + "%");
                    }
                });

            } else {
                showToast(getResources().getString(R.string.no_file_found));
            }
        } catch (Exception e) {
            Crashlytics.logException(e);
            Log.v(CourseListEvalActivity.class.getName(), e.getMessage());
        }

    }

}
