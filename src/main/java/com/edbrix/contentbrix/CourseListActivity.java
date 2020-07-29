package com.edbrix.contentbrix;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;

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
import com.getbase.floatingactionbutton.FloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionsMenu;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class CourseListActivity extends BaseActivity
{
    public  final static int requestCode=205;
    private Context context;
    private RecyclerView courseListRecyclerView;
    private LinearLayout noResourceLayout;
    private SwipeRefreshLayout swipeRefreshLayout;
    private CourseListAdapter courseListAdapter;
    private ArrayList<CoursesData> coursesArrayList;
    private SessionManager sessionManager;
    private FloatingActionsMenu fabEditMenu;
    private FloatingActionButton fabCreateCourse,fabAddToMyfiles;
    private int pageNo;
    private String dataType;
    private FileData fileData;
    private KnowledgeBrixUserData userData;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course_list);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        sessionManager = new SessionManager(CourseListActivity.this);
        fileData = (FileData) getIntent().getSerializableExtra("FileData");
        userData = sessionManager.getLoggedKnowledgeBrixUserData();

        /*pageNo = 1;
        dataType = "course";*/
        context = CourseListActivity.this;

        coursesArrayList = new ArrayList<>();
        fabEditMenu = (FloatingActionsMenu) findViewById(R.id.fabEditMenu);
        fabCreateCourse = (FloatingActionButton) findViewById(R.id.fab_create_course);
        fabAddToMyfiles = (FloatingActionButton) findViewById(R.id.fab_upload_myfiles);

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

        fabCreateCourse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fabEditMenu.collapse();
                Intent createCourse = new Intent(CourseListActivity.this, CreateEdbrixCourseActivity.class);
                createCourse.putExtra("FileData", fileData);
                createCourse.putExtra("createNewCourse", true);
                startActivityForResult(createCourse,requestCode);
            }
        });
        fabAddToMyfiles.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fabEditMenu.collapse();
                Long tsLong = System.currentTimeMillis()/1000;
                String ts = tsLong.toString();
                String encodedVideo=null;
                try{
                    showBusyProgress();
                    encodedVideo = encodeFileToBase64Binary(fileData.getFileObject().getPath().toString());
                    Log.e(CourseListActivity.class.getName(),"base64Video : \n"+encodedVideo);
                }catch (Exception ex){showToast(" FileToBase64 Exp \n"+ex.getMessage().toString());}

                uploadToMyfiles(userData.getAccessToken(), Integer.parseInt(userData.getId()),"MyFile_"+ts,"My File",0,"V","MyFile_"+ts+".mp4",encodedVideo);
            }
        });

        //swipeRefreshLayout.setRefreshing(true);
        courseListAdapter = new CourseListAdapter(context, courseListRecyclerView, coursesArrayList);
        setCourseListAdapter();
        courseListAdapter.setCourseListActionListener(new CourseListAdapter.CourseListActionListener()
        {
            @Override
            public void onCourseItemSelected(CoursesData courses)
            {
                Intent createCourse = new Intent(CourseListActivity.this, CreateEdbrixCourseActivity.class);
                createCourse.putExtra("FileData", fileData);
                createCourse.putExtra("CourseData", courses);
                createCourse.putExtra("createNewCourse", false);
                startActivityForResult(createCourse,requestCode);
            }
        });
        getCourseList(userData.getId(),userData.getAccessToken(),"I","mob", dataType, pageNo);
    }

    private void setCourseListAdapter() {

       /* courseListAdapter.setCourseListActionListener(new CourseListAdapter.CourseListActionListener() {
            @Override
            public void onCourseItemSelected(CoursesData courseItem) {

                if (courseItem != null) {
//                    Intent courseDetail = new Intent(context, CourseDetailActivity.class);
//                    courseDetail.putExtra(CourseDetailActivity.courseDetailBundleKey, courseItem);
//                    startActivityForResult(courseDetail,205);
                } else {

                }
            }
        });*/
        /*courseListAdapter.setOnLoadMoreListener(new CourseListAdapter.OnLoadMoreListener() {
            @Override
            public void onLoadMore() {
                coursesArrayList.add(null);
                courseListAdapter.notifyItemInserted(coursesArrayList.size() - 1);
                pageNo ++;//= pageNo + 1;
                Log.e(CourseListActivity.class.getName(),"pageNo : "+pageNo);
                getCourseList(userData.getId(), userData.getAccessToken(), "I", "mob", dataType, pageNo);
            }
        });*/
        courseListRecyclerView.setAdapter(courseListAdapter);
        courseListRecyclerView.setVisibility(View.VISIBLE);
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
            /*jo.put("UserType", userType);
            jo.put("DeviceType", deviceType);
            jo.put("DataType", dataType);
            jo.put("isResource", "1");
            jo.put("Page", pageNo);
            Log.e("123",""+jo.toString());*/

            String tempUrl = "https://services.edbrix.net/";
            //GsonRequest<CourseListResponseData> getDashboardCourseSchedulesRequest = new GsonRequest<>(Request.Method.POST, "http://enterpriseservices.edbrix.net/app/student/getdashboardcourseandschedules.php", jo.toString(), CourseListResponseData.class,
            //GsonRequest<CourseListResponseData> getDashboardCourseSchedulesRequest = new GsonRequest<>(Request.Method.POST, tempUrl+"knowledgebrix/getcourseslist", jo.toString(), CourseListResponseData.class,
                GsonRequest<CourseListResponseData> getDashboardCourseSchedulesRequest = new GsonRequest<>(Request.Method.POST, tempUrl+"contentbrix/getcourseslist", jo.toString(), CourseListResponseData.class,
                    new Response.Listener<CourseListResponseData>()
                    {
                        @Override
                        public void onResponse(@NonNull CourseListResponseData response) {
                            swipeRefreshLayout.setRefreshing(false);
                            Log.e(CourseListActivity.class.getName(),""+response.toString());
                            Log.e(CourseListActivity.class.getName(),""+response.getCoursesList().size());

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

    private void uploadToMyfiles(String AccessToken,int UserId,String Title,String Description,int CategoryId,String MediaType,String FileName,String Content){
        try {
            JSONObject requestMap = new JSONObject();
            requestMap.put("AccessToken", AccessToken);
            requestMap.put("UserId", UserId);
            requestMap.put("Title", Title);
            requestMap.put("Description", Description);
            requestMap.put("CategoryId", 0);
            requestMap.put("MediaType", "V");
            requestMap.put("FileName", FileName);
            requestMap.put("Content", Content);

            Log.e(CourseListActivity.class.getName(),requestMap.toString());

            String tempUrl = "https://services.edbrix.net/";
            JsonObjectRequest uploadVideo = new JsonObjectRequest(Request.Method.POST, tempUrl + "common/createusermedia", requestMap, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    Log.e(CourseListEvalActivity.class.getName(), response.toString());
                    showToast("File Uploaded successfully.");
                    /*
                    sessionManager.updateVideoUploadCount(sessionManager.getVideoUploadCount() + 1);
                    Log.e(CourseListEvalActivity.class.getName(), response.toString());
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
                        Log.e(CourseListEvalActivity.class.getName(), e.getMessage());
                        //textPercentage.setText(getResources().getString(R.string.error_message));
                    }*/
                }

            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    /*btnUpload.setEnabled(true);
                    btnUpload.setVisibility(View.VISIBLE);
                    btnCancel.setVisibility(View.GONE);*/

                    //mProgressBar.setVisibility(View.GONE);
                    Log.e(CourseListEvalActivity.class.getName(), VolleySingleton.getErrorMessage(error));
                    //textPercentage.setText(VolleySingleton.getErrorMessage(error));
                }

            }) {
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    HashMap<String, String> headers = new HashMap<String, String>();
                    headers.put("Content-Type", "application/json");
                    return headers;
                }

            };

            uploadVideo.setRetryPolicy(VolleySingleton.getDefaultRetryPolice());
            uploadVideo.setShouldCache(false);
            VolleySingleton.getInstance().addToRequestQueue(uploadVideo, "uploadToMyfilesCourseListActivity");
            //VolleySingleton.getInstance().addToRequestQueue(uploadVideo);
        } catch (Exception e) {
            Crashlytics.logException(e);
            /*btnUpload.setEnabled(true);
            btnUpload.setVisibility(View.VISIBLE);
            btnCancel.setVisibility(View.GONE);*/

            //mProgressBar.setVisibility(View.GONE);
            Log.e(CourseListEvalActivity.class.getName(), e.getMessage());
            //textPercentage.setText(getResources().getString(R.string.error_message));
        }
    }

    /**
     * Receiving activity result
     */
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

    private String encodeFileToBase64Binary(String fileName) throws IOException {
        File file = new File(fileName);
        byte[] bytes = loadFile(file);
        //byte[] encoded = Base64.encodeBase64(bytes);
        String encodedString = Base64.encodeToString(bytes, Base64.DEFAULT);//new String(encoded);
        hideBusyProgress();
        return encodedString;
    }

    private static byte[] loadFile(File file) throws IOException {
        InputStream is = new FileInputStream(file);
        long length = file.length();
        if (length > Integer.MAX_VALUE) {
            // File is too large
        }
        byte[] bytes = new byte[(int) length];
        int offset = 0;
        int numRead = 0;
        while (offset < bytes.length && (numRead = is.read(bytes, offset, bytes.length - offset)) >= 0) { offset += numRead;      }
        if (offset < bytes.length) { throw new IOException("Could not completely read file " + file.getName());     }
        is.close();
        return bytes;
    }
}
