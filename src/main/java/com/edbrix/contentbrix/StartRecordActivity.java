package com.edbrix.contentbrix;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.media.MediaRouter;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.edbrix.contentbrix.baseclasses.BaseActivity;
import com.edbrix.contentbrix.commons.AlertDialogManager;
import com.edbrix.contentbrix.commons.GlobalMethods;
import com.edbrix.contentbrix.utils.SessionManager;
import com.edbrix.contentbrix.volley.VolleySingleton;
import com.edbrix.contentbrix.walkthrough.WalkThroughActivity;

import java.io.File;
import java.util.List;

import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;

public class StartRecordActivity extends BaseActivity implements EasyPermissions.PermissionCallbacks {
    // LogCat tag
    private static final String TAG = StartRecordActivity.class.getSimpleName();

    // Camera activity request codes
    private static final int CAMERA_CAPTURE_VIDEO_REQUEST_CODE = 200;

    public final static int PERM_REQUEST_CODE_DRAW_OVERLAYS = 1234;
    private static final int REQUEST_CAST = 1111;

    private LinearLayout btnhelp;
    private LinearLayout btnRecordVideo;
    private LinearLayout btnRecordedVideo;
    private LinearLayout btnMyExports;
    private LinearLayout btnBrodcast;
    private LinearLayout btnRecordWhiteBoard;
    private LinearLayout btnAdvRecordWhiteBoard;
    private LinearLayout btnRecordPDF;
    private LinearLayout btnSettings;
    private GlobalMethods globalMethods;
    private TextView versionName;
    private Uri fileUri;
    private SessionManager sessionManager;
    private ImageView screenCastBtn;
    private boolean isCastingOn = false;
    //private ImageView imgDashboardBackground;
    private final String[] PERMISSIONS = {
            android.Manifest.permission.VIBRATE,
            android.Manifest.permission.RECORD_AUDIO,
            android.Manifest.permission.CAMERA,
            android.Manifest.permission.READ_EXTERNAL_STORAGE};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (isTablet()) {
            // stop screen rotation on phones
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }
        // set View
        setContentView(R.layout.activity_start_record);

        // Initialize components
        /* versionName = (TextView) findViewById(R.id.versionName);*/ //commented by pranav 02082019
        btnhelp = (LinearLayout) findViewById(R.id.btnhelp);
        btnRecordVideo = (LinearLayout) findViewById(R.id.btnRecordVideo);
        btnBrodcast = (LinearLayout) findViewById(R.id.btnBrodcast);
        btnRecordedVideo = (LinearLayout) findViewById(R.id.btnRecordedVideo);
        btnMyExports = (LinearLayout) findViewById(R.id.btnMyExports);
        btnSettings = (LinearLayout) findViewById(R.id.btnSettings);
        btnRecordWhiteBoard = (LinearLayout) findViewById(R.id.btnRecordWhiteBoard);
        btnAdvRecordWhiteBoard = (LinearLayout) findViewById(R.id.btnAdvRecordWhiteBoard);
        btnRecordPDF = (LinearLayout) findViewById(R.id.btnRecordPDF);
        screenCastBtn = findViewById(R.id.castBtn);

        screenCastBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isCastingOn) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(StartRecordActivity.this);
                    builder.setMessage("Would you like to finish screen casting ?")
                            .setCancelable(false)
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    isCastingOn = false;
                                    sessionManager.updateSessionAppIsScreenCastActive(false);
                                    try {
                                        MediaRouter mMediaRouter = (MediaRouter) getSystemService(Context.MEDIA_ROUTER_SERVICE);
                                        mMediaRouter.selectRoute(MediaRouter.ROUTE_TYPE_LIVE_VIDEO, mMediaRouter.getDefaultRoute());
                                        //Log.e("Route Count", "" + mMediaRouter.getRouteCount());
                                        screenCastBtn.setImageDrawable(getDrawable(R.drawable.screencast));
                                    } catch (Exception e) {
                                        //Log.e("Stop Casting", "pressed " + e.getMessage());
                                        e.printStackTrace();
                                    }
                                }
                            })
                            .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();
                                }
                            });
                    //Creating dialog box
                    AlertDialog alert = builder.create();
                    //Setting the title manually
                    alert.show();
                } else {
                    try {
                        startActivityForResult(new Intent("android.settings.CAST_SETTINGS"), REQUEST_CAST);
                    } catch (Exception exception1) {
                        exception1.printStackTrace();
                        Toast.makeText(getApplicationContext(), "Device not supported", Toast.LENGTH_LONG).show();
                    }
                }
            }
        });
        //imgDashboardBackground=(ImageView)findViewById(R.id.imgDashboardBackground);

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int height = displayMetrics.heightPixels;
        int width = displayMetrics.widthPixels;
        Log.e("Width",""+displayMetrics.widthPixels);
        Log.e("height",""+displayMetrics.heightPixels);

       /* if (!isTablet()) {
            imgDashboardBackground.requestLayout();
            imgDashboardBackground.getLayoutParams().height = height;
            imgDashboardBackground.getLayoutParams().width = width;
            imgDashboardBackground.setScaleType(ImageView.ScaleType.FIT_XY);
        }*/


        globalMethods = new GlobalMethods();
        sessionManager = new SessionManager(StartRecordActivity.this);
       /* try {
            versionName.setText("Ver " + getPackageManager().getPackageInfo(getPackageName(), 0).versionName);
        } catch (Exception e) {
            Crashlytics.logException(e);

        }*/// commented by pranav 02082019
        // set Listeners
        setListeners();

        // showToast("FCM Token: "+ FirebaseInstanceId.getInstance().getToken());
        checkAllPermissions();

        getProductVersion();
    }

    /**
     * Defined all components listeners
     */
    private void setListeners() {

        btnRecordWhiteBoard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TypedValue outValue = new TypedValue();
                StartRecordActivity.this.getTheme().resolveAttribute(android.R.attr.selectableItemBackground, outValue, true);
                btnRecordWhiteBoard.setBackgroundResource(outValue.resourceId);

                if (globalMethods.checkCameraAudioRecordPermission(StartRecordActivity.this)) {
                    recordWhiteBoard();
                } else {
                    String msg = globalMethods.requestCameraAudioWithReadWriteExternalStoragePermission(StartRecordActivity.this);
                    showToast(msg);
                }
            }
        });
        btnAdvRecordWhiteBoard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TypedValue outValue = new TypedValue();
                StartRecordActivity.this.getTheme().resolveAttribute(android.R.attr.selectableItemBackground, outValue, true);
                btnAdvRecordWhiteBoard.setBackgroundResource(outValue.resourceId);

                if (globalMethods.checkCameraAudioRecordPermissionForAdvWhiteBoard(StartRecordActivity.this)) {
                    recordAdvWhiteBoard();
                } else {
                    String msg = globalMethods.requestCameraAudioWithReadWriteExternalStoragePermissionForAdvWhiteBoard(StartRecordActivity.this);
                    showToast(msg);
                }
            }
        });

        btnRecordPDF.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TypedValue outValue = new TypedValue();
                StartRecordActivity.this.getTheme().resolveAttribute(android.R.attr.selectableItemBackground, outValue, true);
                btnRecordPDF.setBackgroundResource(outValue.resourceId);

                if (globalMethods.checkAudioRecordPermission(StartRecordActivity.this)) {
//                    recordPDFWithWhiteBoard();
//                    showBusyProgress();
//                    openDocumentFolder();
                    alertToConfirmPDFUploadFrom();
                } else {
                    String msg = globalMethods.requestAudioWithReadWriteExternalStoragePermission(StartRecordActivity.this);
                    showToast(msg);
                }
            }
        });

        btnRecordVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TypedValue outValue = new TypedValue();
                StartRecordActivity.this.getTheme().resolveAttribute(android.R.attr.selectableItemBackground, outValue, true);
                btnRecordVideo.setBackgroundResource(outValue.resourceId);
                if (globalMethods.isCameraHardwareAvailable(StartRecordActivity.this)) {
                    if (globalMethods.checkCameraRecordPermission(StartRecordActivity.this)) {
                        goToRecordVideo();
                    } else {
                        String msg = globalMethods.requestCameraWithReadWriteExternalStoragePermission(StartRecordActivity.this);
                        showToast(msg);
                    }
                } else {
                    showToast(getResources().getString(R.string.camera_not_found_error));
                }
            }
        });

        btnRecordedVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TypedValue outValue = new TypedValue();
                StartRecordActivity.this.getTheme().resolveAttribute(android.R.attr.selectableItemBackground, outValue, true);
                btnRecordedVideo.setBackgroundResource(outValue.resourceId);
                if (globalMethods.checkReadWriteExternalStorage(StartRecordActivity.this)) {
                    showRecordedVideo();
                } else {
                    String msg = globalMethods.requestReadWriteExternalStoragePermission(StartRecordActivity.this);
                    showToast(msg);
                }

            }
        });
        btnMyExports.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TypedValue outValue = new TypedValue();
                StartRecordActivity.this.getTheme().resolveAttribute(android.R.attr.selectableItemBackground, outValue, true);
                btnRecordedVideo.setBackgroundResource(outValue.resourceId);
                if (globalMethods.checkReadWriteExternalStorage(StartRecordActivity.this)) {
                    showExportedFiles();
                } else {
                    String msg = globalMethods.requestReadWriteExternalStoragePermission(StartRecordActivity.this);
                    showToast(msg);
                }

            }
        });

        btnSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TypedValue outValue = new TypedValue();
                StartRecordActivity.this.getTheme().resolveAttribute(android.R.attr.selectableItemBackground, outValue, true);
                btnSettings.setBackgroundResource(outValue.resourceId);
                goToSettings();
            }
        });

        btnhelp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent settings = new Intent(StartRecordActivity.this, WalkThroughActivity.class);
                startActivity(settings);
            }
        });

        btnBrodcast.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent broadcast = new Intent(StartRecordActivity.this, BroadcastListActivity.class);
                startActivity(broadcast);
            }
        });
    }

    private void goToRecordVideo() {
        new SplashTimeoutTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, 1500);
    }

    private void recordWhiteBoard() {
        Intent recordIntent = new Intent(StartRecordActivity.this, RecordWhiteBoardActivity.class);
        startActivityForResult(recordIntent, RecordWhiteBoardActivity.RESULT_CODE);
    }

    private void recordAdvWhiteBoard() {
        Intent recordIntent = new Intent(StartRecordActivity.this, RecordAdvWhiteBoardActivity.class);
        startActivityForResult(recordIntent, RecordAdvWhiteBoardActivity.RESULT_CODE);
    }

    private void recordPDFWithWhiteBoard() {
        Intent pdfIntent = new Intent(StartRecordActivity.this, PDFListActivity.class);
        startActivityForResult(pdfIntent, PICKFILE_RESULT_CODE);
    }

    private void showRecordedVideo() {
        if (!getGlobalMethods().isInternetConnectivity(StartRecordActivity.this)) {
            showToast("No internet connectivity found. Please check settings for internet connectivity.");
        }
        Intent recordVideoIntent = new Intent(StartRecordActivity.this, MainActivity.class);
        startActivity(recordVideoIntent);
    }

    private void showExportedFiles() {
        if (!getGlobalMethods().isInternetConnectivity(StartRecordActivity.this)) {
            showToast("No internet connectivity found. Please check settings for internet connectivity.");
        }
        Intent recordVideoIntent = new Intent(StartRecordActivity.this, MyExportedFilesActivity.class);
        startActivity(recordVideoIntent);
    }

    private void goToSettings() {
        Intent settings = new Intent(StartRecordActivity.this, SettingsActivity.class);
        startActivity(settings);
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
            case GlobalMethods.camera_audio_external_storage_permission_request_code:
                recordWhiteBoard();
                break;
            case GlobalMethods.camera_audio_external_storage_permission_request_code_for_adv_whiteboard:
                recordAdvWhiteBoard();
                break;
            case GlobalMethods.external_storage_permission_request_code:
                showRecordedVideo();
                break;
            case GlobalMethods.audio_external_storage_permission_request_code:
//                recordPDFWithWhiteBoard();
//                openDocumentFolder();
                alertToConfirmPDFUploadFrom();
                break;
        }
    }

    /**
     * Launching camera app to record video
     */
    private void recordVideo() {

        Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);

        fileUri = FileProvider.getUriForFile(StartRecordActivity.this, BuildConfig.APPLICATION_ID + ".provider", globalMethods.getOutputMediaFile(StartRecordActivity.this));

//        fileUri = Uri.fromFile(globalMethods.getOutputMediaFile(StartRecordActivity.this));

        // set video quality
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);
        //intent.putExtra(MediaStore.EXTRA_DURATION_LIMIT, 3600);
        intent.putExtra(MediaStore.EXTRA_SCREEN_ORIENTATION, ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri); // set the image file

        // start the video capture Intent
        startActivityForResult(intent, CAMERA_CAPTURE_VIDEO_REQUEST_CODE);
    }

    /**
     * Receiving activity result method will be called after closing the camera
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        // if the result is capturing Image
        super.onActivityResult(requestCode, resultCode, intent);
        if (requestCode == CAMERA_CAPTURE_VIDEO_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {

                // video successfully recorded
                if (intent != null && intent.getData() != null) {

                    showToast("Reccord Video successfully.", Toast.LENGTH_SHORT);

                    /*String fileName = new File(intent.getData().getPath()).getName();
                    File mediaStorageDir = VolleySingleton.getAppStorageDirectory();

                    FileData fileData = new FileData(new File(mediaStorageDir.getPath() + File.separator + fileName));
                    Intent recordPreviewIntent = new Intent(StartRecordActivity.this, RecordPreviewActivity.class);
                    recordPreviewIntent.putExtra("FileUri", intent.getData());
                    Bundle bundle = new Bundle();
                    bundle.putSerializable("FileData", fileData);
                    recordPreviewIntent.putExtras(bundle);

                    startActivityForResult(recordPreviewIntent, RecordPreviewActivity.REQUEST_CODE);*/
                    startActivity(new Intent(StartRecordActivity.this, MainActivity.class));


                } else {
                    // failed to record video
                    showToast("Sorry! Unable to fetch recorded video data.");
                }
            } else if (resultCode == RESULT_CANCELED) {
                // user cancelled recording
                //showToast("User cancelled video recording");

            } else {
                // failed to record video
                showToast("Sorry! Failed to record video");
            }
        } else if (requestCode == PICKFILE_RESULT_CODE) {
            if (resultCode == RESULT_OK) {
                File tempPDFFile = VolleySingleton.getTempPDFFile();
                if (intent != null && intent.getData() != null && tempPDFFile != null) {
//                    openPDFRecordingScreen(intent.getData());
                    if (copyInputStreamToFile(intent.getData(), tempPDFFile)) {
                        openPDFRecordingScreen(tempPDFFile);
                    } else {
                        hideBusyProgress();
                        showToast("Something went wrong. Please try again later..!");
                    }
                } else {
                    showToast("Something went wrong. Please try again later..!");
                }
            } else {
//                showToast("PDF recording cancelled.");
                hideBusyProgress();
            }
        } else if (requestCode == REQUEST_CAST)
            if (((MediaRouter) getSystemService(Context.MEDIA_ROUTER_SERVICE)).getRouteCount() > 1) {
                isCastingOn = true;
                sessionManager.updateSessionAppIsScreenCastActive(true);
                screenCastBtn.setImageDrawable(getDrawable(R.drawable.ic_cast_connected));
            }
/*
        if (requestCode == PERM_REQUEST_CODE_DRAW_OVERLAYS) {
            if (android.os.Build.VERSION.SDK_INT >= 23) {   //Android M Or Over
                if (!Settings.canDrawOverlays(this)) {
                    // ADD UI FOR USER TO KNOW THAT UI for SYSTEM_ALERT_WINDOW permission was not granted earlier...
                }
            }
        }*/
    }

    private void openPDFRecordingScreen(Uri pdfUriData) {
        Intent pdfRecordingIntent = new Intent(StartRecordActivity.this, RecordPDFOnBoardActivity.class);
        pdfRecordingIntent.setData(pdfUriData);
        startActivityForResult(pdfRecordingIntent, RecordPDFOnBoardActivity.RESULT_CODE);
    }

    private void openPDFRecordingScreen(File pdfFile) {
        Intent pdfRecordingIntent = new Intent(StartRecordActivity.this, RecordPDFOnBoardActivity.class);
        pdfRecordingIntent.putExtra("pdf", pdfFile);
        startActivityForResult(pdfRecordingIntent, RecordPDFOnBoardActivity.RESULT_CODE);
        hideBusyProgress();
    }

    private void alertToConfirmPDFUploadFrom(){
        getAlertDialogManager().Dialog(getResources().getString(R.string.record_presentation), getString(R.string.upload_file_from),  "SD Card", "My Exports", false, new AlertDialogManager.onTwoButtonClickListner() {
            @Override
            public void onNegativeClick() {
                Intent recordVideoIntent = new Intent(StartRecordActivity.this, MyExportedFilesActivity.class);
                recordVideoIntent.putExtra("isExport",true);
                startActivity(recordVideoIntent);
            }

            @Override
            public void onPositiveClick() {
                showBusyProgress();
                openDocumentFolder();
            }
        }).show();

    }


    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {
        Log.d(TAG, "onPermissionsGranted:" + requestCode + ":" + perms.size());
    }

    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {
        Log.d(TAG, "onPermissionsDenied:" + requestCode + ":" + perms.size());

        // (Optional) Check whether the user denied any permissions and checked "NEVER ASK AGAIN."
        // This will display a dialog directing them to enable the permission in app settings.
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            new AppSettingsDialog.Builder(this).build().show();
        }
    }

    private class SplashTimeoutTask extends AsyncTask<Integer, Void, Void> {

        private static final int TimeoutSleepInterval = 200;

        @Override
        protected void onPreExecute() {
            //showToast("You can record video upto 60 min only.", Toast.LENGTH_SHORT);
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
                    Crashlytics.logException(e);
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
            //Toast.makeText(StartRecordActivity.this,"Vib : "+sessionManager.getSessionVibratePhoneState(),Toast.LENGTH_LONG).show();
//            cancelToast();
            if (sessionManager.getSessionVibratePhoneState()) {
                globalMethods.vibratePhone(StartRecordActivity.this, 500);
            }

            recordVideo();
        }
    }

    public void checkAllPermissions() {

        if (EasyPermissions.hasPermissions(this, PERMISSIONS)) {
            // Have permissions, do the thing!
            //Toast.makeText(this, "TODO: Location and Contacts things", Toast.LENGTH_LONG).show();
        } else {
            // Ask for both permissions
            EasyPermissions.requestPermissions(
                    this,
                    getString(R.string.permissions),
                    123,
                    PERMISSIONS);
        }
    }

/*    public void permissionToDrawOverlays() {
        if (android.os.Build.VERSION.SDK_INT >= 23) {   //Android M Or Over
            if (!Settings.canDrawOverlays(this)) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent, PERM_REQUEST_CODE_DRAW_OVERLAYS);
            }
        }
    }*/


    @Override
    protected void onStart() {
        super.onStart();
        if (((MediaRouter) getSystemService(Context.MEDIA_ROUTER_SERVICE)).getRouteCount() > 1) {
            isCastingOn = true;
            sessionManager.updateSessionAppIsScreenCastActive(true);
            screenCastBtn.setImageDrawable(getDrawable(R.drawable.ic_cast_connected));
        } else {
            isCastingOn = false;
            sessionManager.updateSessionAppIsScreenCastActive(false);
            screenCastBtn.setImageDrawable(getDrawable(R.drawable.screencast));
        }
    }

    @Override
    public void onBackPressed() {

        /*if (isCastingOn) {
            isCastingOn = false;
            try {
                MediaRouter mMediaRouter = (MediaRouter) getSystemService(Context.MEDIA_ROUTER_SERVICE);
                mMediaRouter.selectRoute(MediaRouter.ROUTE_TYPE_LIVE_VIDEO, mMediaRouter.getDefaultRoute());
                MediaRouter.RouteInfo routeInfo=mMediaRouter.getSelectedRoute(MediaRouter.ROUTE_TYPE_LIVE_VIDEO);

                //Log.e("Route Count",""+mMediaRouter.getRouteCount());
                screenCastBtn.setImageDrawable(getDrawable(R.drawable.screencast));
                super.onBackPressed();
            } catch (Exception e) {
                //Log.e("Stop Casting","pressed "+e.getMessage());
                e.printStackTrace();
            }
        }*/
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
       /* if (isCastingOn) {
            isCastingOn = false;
            try {
                MediaRouter mMediaRouter = (MediaRouter) getSystemService(Context.MEDIA_ROUTER_SERVICE);
                mMediaRouter.selectRoute(MediaRouter.ROUTE_TYPE_LIVE_VIDEO, mMediaRouter.getDefaultRoute());
                Log.e("Route Count", "" + mMediaRouter.getRouteCount());
                screenCastBtn.setImageDrawable(getDrawable(R.drawable.screencast));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }*/
        super.onDestroy();
    }
}
