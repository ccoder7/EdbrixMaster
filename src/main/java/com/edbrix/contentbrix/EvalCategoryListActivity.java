package com.edbrix.contentbrix;

import android.content.Context;
import android.content.Intent;
import androidx.annotation.NonNull;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.os.Bundle;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.crashlytics.android.Crashlytics;
import com.edbrix.contentbrix.adapters.CategoryListAdapter;
import com.edbrix.contentbrix.baseclasses.BaseActivity;
import com.edbrix.contentbrix.data.CategoryData;
import com.edbrix.contentbrix.data.CategoryListData;
import com.edbrix.contentbrix.data.FileData;
import com.edbrix.contentbrix.data.KnowledgeBrixUserData;
import com.edbrix.contentbrix.utils.SessionManager;
import com.edbrix.contentbrix.volley.GsonRequest;
import com.edbrix.contentbrix.volley.VolleySingleton;

import org.json.JSONObject;

import java.util.ArrayList;

public class EvalCategoryListActivity extends BaseActivity {

    public  final static int requestCode=205;
    private Context context;
    private RecyclerView categoryListRecyclerView;
    private LinearLayout noResourceLayout;
    private SwipeRefreshLayout swipeRefreshLayout;
    private CategoryListAdapter categoryListAdapter;
    private ArrayList<CategoryListData> categoryArrayList;
    private SessionManager sessionManager;
    private int pageNo;
    private FileData fileData;
    private String dataType;
    private KnowledgeBrixUserData userData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_eval_category_list);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        sessionManager = new SessionManager(EvalCategoryListActivity.this);
        userData = sessionManager.getLoggedKnowledgeBrixUserData();

        context = EvalCategoryListActivity.this;
        categoryArrayList = new ArrayList<>();
        categoryListRecyclerView = (RecyclerView)findViewById(R.id.categoryListRecyclerView);
        noResourceLayout = (LinearLayout)findViewById(R.id.noResourceLayout);
        swipeRefreshLayout = (SwipeRefreshLayout)findViewById(R.id.swipeRefreshLayout);
        fileData = (FileData) getIntent().getSerializableExtra("FileData");
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                swipeRefreshLayout.setRefreshing(true);
                categoryArrayList.clear();
                pageNo = 0;
                getCategoryList(userData.getId(),userData.getAccessToken(),userData.getUserType(),"mob", dataType, pageNo);
            }
        });

        categoryListAdapter = new CategoryListAdapter(context, categoryListRecyclerView, categoryArrayList);
        setCategoryListAdapter();
        categoryListAdapter.setCategoryListActionListener(new CategoryListAdapter.CategoryListActionListener() {
            @Override
            public void onCategoryItemSelected(CategoryListData categoryListData) {
                Intent intent = new Intent(EvalCategoryListActivity.this,CourseListEvalActivity.class);
                intent.putExtra("CategoryId", categoryListData.getId());
                intent.putExtra("FileData", fileData);
                startActivity(intent);
            }
        });
        getCategoryList(userData.getId(),userData.getAccessToken(),userData.getUserType(),"mob", dataType, pageNo);
    }

    private void setCategoryListAdapter() {
        categoryListRecyclerView.setAdapter(categoryListAdapter);
        categoryListRecyclerView.setVisibility(View.VISIBLE);
    }

    private void getCategoryList(String id, String accessToken, String userType, String mob, String dataType, int pageNo) {

            try{
                JSONObject jo = new JSONObject();
                jo.put("UserId", id);
                jo.put("AccessToken", accessToken);

                String tempUrl = "https://services.edbrix.net/";
                GsonRequest<CategoryData> getCategoryListRequest = new GsonRequest<>(Request.Method.POST,
                        tempUrl+"contentbrix/getevalbrixcoursecategorieslist",
                        jo.toString(),
                        CategoryData.class,
                        new Response.Listener<CategoryData>()
                        {
                            @Override
                            public void onResponse(@NonNull CategoryData response) {
                                swipeRefreshLayout.setRefreshing(false);
                                Log.e(EvalCategoryListActivity.class.getName(),""+response.toString());
                                Log.e(EvalCategoryListActivity.class.getName(),""+response.getCategoryList().size());

                                if (response.getCategoryList() != null && response.getCategoryList().size() >=0 )
                                {
//                                    //Remove loading item
                                    categoryListRecyclerView.setVisibility(View.VISIBLE);
                                    if (categoryArrayList.size() > 0) {
                                        categoryArrayList.remove(categoryArrayList.size() - 1);
                                        categoryListAdapter.notifyItemRemoved(categoryArrayList.size());
                                    }
                                    categoryArrayList.addAll(response.getCategoryList());
                                    categoryListAdapter.notifyDataSetChanged();
                                    categoryListAdapter.setLoaded();

                                    if(categoryArrayList.isEmpty()){
                                        categoryListRecyclerView.setVisibility(View.GONE);
                                    }else{
                                        categoryListRecyclerView.setVisibility(View.VISIBLE);
                                    }
                                }else{
//                                    showToast("No courses found.");
                                    categoryListRecyclerView.setVisibility(View.GONE);
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
                getCategoryListRequest.setRetryPolicy(VolleySingleton.getDefaultRetryPolice());
                getCategoryListRequest.setShouldCache(false);
                VolleySingleton.getInstance().addToRequestQueue(getCategoryListRequest, "getCategoryListRequest");


            }catch (Exception e){
                Crashlytics.logException(e);
                swipeRefreshLayout.setRefreshing(false);
                showToast("Something went wrong. Please try again later.");
            }

    }
}
