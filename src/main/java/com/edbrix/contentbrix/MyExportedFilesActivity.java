package com.edbrix.contentbrix;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.os.AsyncTask;
import android.os.Environment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.os.Bundle;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import androidx.appcompat.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.edbrix.contentbrix.adapters.ExportedListRecyclerViewAdapter;
import com.edbrix.contentbrix.baseclasses.BaseActivity;
import com.edbrix.contentbrix.commons.AlertDialogManager;
import com.edbrix.contentbrix.commons.GlobalMethods;
import com.edbrix.contentbrix.data.FileData;
import com.edbrix.contentbrix.utils.SessionManager;
import com.ipaulpro.afilechooser.utils.FileUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class MyExportedFilesActivity extends BaseActivity {

    private MenuItem deleteAllMenuItem;
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
    private CardView card_view;
    private boolean isExportPDF;

    private ExportedListRecyclerViewAdapter.OnButtonActionListener buttonActionListener;
    private static final String TAG = MyExportedFilesActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (isTablet()) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE); // stop screen rotation on phones
        }else{
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }
        setContentView(R.layout.activity_my_exported_files);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        isExportPDF = getIntent().getBooleanExtra("isExport",false);

        File yourAppStorageDir = new File(Environment.getExternalStorageDirectory(), "/" + getResources().getString(R.string.app_name) + "/exports/");
        if (!yourAppStorageDir.exists()) {
            boolean isDirCreated = yourAppStorageDir.mkdirs();
            Log.d(MyExportedFilesActivity.class.getName(), "App mediaStorageDirectory created :" + isDirCreated);
        }
        card_view = (CardView) findViewById(R.id.card_view);
        globalMethods = new GlobalMethods();
        sessionManager = new SessionManager(MyExportedFilesActivity.this);
        searchBox = (EditText) findViewById(R.id.searchBox);
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeRefreshLayout);
        btnRecordWhiteBoard = (LinearLayout) findViewById(R.id.btnRecordWhiteBoard);
        btnRecordPDF = (LinearLayout) findViewById(R.id.btnRecordPDF);
        btnRecordScreen = (LinearLayout) findViewById(R.id.btnRecordScreen);
        btnRecordVideo = (LinearLayout) findViewById(R.id.btnRecordVideo);
        recordedVideoRecyclerView = (RecyclerView) findViewById(R.id.recordedVideoRecyclerView);
        rideIcon = (ImageView)findViewById(R.id.rideIcon);
        swipeRefreshLayout.setRefreshing(true);
        getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN
        );

        //set Listeners
        setListeners();

        if (globalMethods.checkAllPermission(MyExportedFilesActivity.this)) {
            // get Video List
            getExportedFilesListFromLocalStorage();
            if(fileDataList.size()>10)
            {
                AlertDialog.Builder builder = new AlertDialog.Builder(MyExportedFilesActivity.this);
                builder.setMessage("Please delete some recordings for better performance")
                        .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.dismiss();
                            }
                        });
                builder.create();
                builder.show();
            }
        } else {
            String msg = globalMethods.requestAllPermission(MyExportedFilesActivity.this);
            showToast(msg+".");
        }
    }

    private void setListeners() {
        buttonActionListener = new ExportedListRecyclerViewAdapter.OnButtonActionListener() {
            @Override
            public void onDeleteButtonPressed(FileData fileData, int position) {
                deleteFile(fileData, position);
            }
        };
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                swipeRefreshLayout.setRefreshing(true);
                getExportedFilesListFromLocalStorage();

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

    private void getExportedFilesListFromLocalStorage()
    {
        fileDataList = new ArrayList<>();
        File yourDir = new File(Environment.getExternalStorageDirectory(), "/" + mContext.getResources().getString(R.string.app_name) + "/exports/");//VolleySingleton.getAppStorageDirectory();
        for (File file : yourDir.listFiles())
        {
            Log.v(TAG, "File MIME Type:" + FileUtils.getMimeType(file));
            Log.v(TAG, "File MIME Ext:" + FileUtils.getExtension(file.getPath()));
            if (file.isFile()) {
                /*if (FileUtils.getMimeType(file).equals("video/mp4") && FileUtils.getExtension(file.getPath()).equals(".mp4")) {
                    fileDataList.add(new FileData(file));
                }*/
                if(isExportPDF ){
                    if(FileUtils.getMimeType(file).equals("application/pdf")) {
                        fileDataList.add(new FileData(file));
                    }
                }else {
                    fileDataList.add(new FileData(file));
                }

            }
        }
        ///shashank
        ////VideoSorting
        Collections.sort(fileDataList, new Comparator<FileData>(){
            public int compare(FileData f1, FileData f2) {
                return Long.compare(f1.getFileObject().lastModified(), f2.getFileObject().lastModified());
            }
        });
        Collections.reverse(fileDataList);

        if (fileDataList != null && fileDataList.size() > 0)
        {
            if (getScreenOrientation() == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
                recordedVideoRecyclerView.setLayoutManager(new StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL));
            } else if (getScreenOrientation() == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
                if (isTablet()) {
                    recordedVideoRecyclerView.setLayoutManager(new StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL));
                } else {
                    recordedVideoRecyclerView.setLayoutManager(new StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL));
                }
            }
            if(deleteAllMenuItem != null) {
                deleteAllMenuItem.setVisible(true);
                searchBox.setVisibility(View.VISIBLE);
                rideIcon.setVisibility(View.VISIBLE);
                if(isExportPDF){
                    deleteAllMenuItem.setVisible(false);
                }
            }
            // set Adapter
            ExportedListRecyclerViewAdapter recyclerViewAdapter = new ExportedListRecyclerViewAdapter(MyExportedFilesActivity.this, isExportPDF,fileDataList, buttonActionListener);
            recordedVideoRecyclerView.setAdapter(recyclerViewAdapter);
            recordedVideoRecyclerView.setVisibility(View.VISIBLE);
            swipeRefreshLayout.setRefreshing(false);

        } else {
            if(deleteAllMenuItem != null) {
                deleteAllMenuItem.setVisible(false);
                searchBox.setVisibility(View.GONE);
                rideIcon.setVisibility(View.GONE);
            }
            recordedVideoRecyclerView.setVisibility(View.GONE);
            swipeRefreshLayout.setRefreshing(false);
            showToast("No files found.", Toast.LENGTH_SHORT);
        }
    }

    /**
     * Delete Video file by intimating user through alert box
     */
    private void deleteFile(final FileData fileDataObj, final int pos) {
        getAlertDialogManager().Dialog(fileDataObj.getFileName().replaceAll(".mp4", ""), getString(R.string.delete_exports_msg), true, new AlertDialogManager.onTwoButtonClickListner() {
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
                showToast("File(s) deleted successfully.", Toast.LENGTH_SHORT);
                ((ExportedListRecyclerViewAdapter) recordedVideoRecyclerView.getAdapter()).removeItem(position);
//                getRecordedVideoListFromLocalStorage();
            } else {
                showToast("Something went wrong. Please try again later..!");
            }


        }
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
                ((ExportedListRecyclerViewAdapter) recordedVideoRecyclerView.getAdapter()).updateList(temp);
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
            if(isExportPDF){
                deleteAllMenuItem.setVisible(false);
            }
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
                        File dir = new File(Environment.getExternalStorageDirectory(), "/" + getResources().getString(R.string.app_name) + "/exports/");
                        if (dir != null) {
                            // so we can list all files
                            File[] filenames = dir.listFiles();
                            // loop through each file and delete
                            for (File tmpf : filenames) {
                                tmpf.delete();
                            }
                            getExportedFilesListFromLocalStorage();
                            showToast("All files are deleted.");
                        }
                    }
                }).show();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void checkIsExportFileListEmpty(){
        if (fileDataList != null && fileDataList.size() > 0) {
            deleteAllMenuItem.setVisible(true);
            searchBox.setVisibility(View.VISIBLE);
            rideIcon.setVisibility(View.VISIBLE);
            if(isExportPDF){
                deleteAllMenuItem.setVisible(false);
            }
        }
        else {
            deleteAllMenuItem.setVisible(false);
            searchBox.setVisibility(View.GONE);
            rideIcon.setVisibility(View.GONE);
        }
    }
}
