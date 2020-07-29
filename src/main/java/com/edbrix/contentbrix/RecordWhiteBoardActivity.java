package com.edbrix.contentbrix;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.MediaRecorder;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.provider.Settings;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import com.edbrix.contentbrix.encoder.EncoderConfig;
import com.edbrix.contentbrix.encoder.FrameEncoder;
import com.edbrix.contentbrix.encoder.HevcEncoderConfig;
import com.edbrix.contentbrix.utils.Screenshots;
import com.google.android.material.snackbar.Snackbar;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.viewpager.widget.ViewPager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.SparseIntArray;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.Surface;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.crashlytics.android.Crashlytics;
import com.edbrix.contentbrix.adapters.TextureListRecyclerViewAdapter;
import com.edbrix.contentbrix.adapters.WhiteBoardViewPagerAdapter;
import com.edbrix.contentbrix.baseclasses.BaseActivity;
import com.edbrix.contentbrix.commons.GlobalMethods;
import com.edbrix.contentbrix.customview.CustomViewPager;
import com.edbrix.contentbrix.customview.DrawingView;
import com.edbrix.contentbrix.customview.InformatoryDialog;
import com.edbrix.contentbrix.customview.TextEditorDialog;
import com.edbrix.contentbrix.data.FileData;
import com.edbrix.contentbrix.data.KnowledgeBrixUserData;
import com.edbrix.contentbrix.data.ProductAccessList;
import com.edbrix.contentbrix.data.Userproductaccessresponse;
import com.edbrix.contentbrix.fragments.WhiteBoardPageFragment;
import com.edbrix.contentbrix.utils.SessionManager;
import com.edbrix.contentbrix.volley.VolleySingleton;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.github.douglasjunior.androidSimpleTooltip.SimpleTooltip;

public class RecordWhiteBoardActivity extends BaseActivity implements ViewPager.OnPageChangeListener, BaseActivity.OnVideoProcessingListener {
    private static final String TAG = "RecordWhiteBoardActivity";
    private static final int REQUEST_CODE = 1000;
    public static final int RESULT_CODE = 1234;
    public static final int PICK_IMAGE = 102;
    private int mScreenDensity;
    private MediaProjectionManager mProjectionManager;
    private int DISPLAY_WIDTH = 1920;
    private int DISPLAY_HEIGHT = 1080;
    private MediaProjection mMediaProjection;
    private VirtualDisplay mVirtualDisplay;
    private MediaProjectionCallback mMediaProjectionCallback;
    private MediaRecorder mMediaRecorder;
    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();
    private static final int REQUEST_PERMISSIONS = 10;
    private static final int REQUEST_PERMISSIONS_PDF_EXPORT = 11;
    private CustomViewPager whiteBoardViewPager;
    private TextView countDownTimerText;
    private MyCountDownTimer myCountDownTimer;
    private WhiteBoardViewPagerAdapter whiteBoardViewPagerAdapter;
    private int countDownTime;
    private int countDownInterval;
    private int selectedPencilColor = Color.BLACK;
    //    private DrawingView drawingView;
    private DrawingView drawingViewObj;
    private RelativeLayout mainContainer;
    private ImageView stopBtn;
    private Button colorsPaletBtn;
    private Button pencilStrokesBtn;
    private Button shapesBtn;
    private Button fullscreenBtn;

    private ImageView prevBtn;
    private ImageView nextBtn;

    private ImageView prevBtn1;
    private ImageView nextBtn1;

    private ImageView resumeBtnOut;
    private ImageView pauseBtnOut;
    private ImageView stopBtnOut;
    private ImageView startBtn;

    private Button eraseBtn;
    private Button removeBtn;
    private Button trashImagesBtn;
    private Button doneImagesBtn;
    private ImageView showHideBtn;
    private Button addBGGraphicBtn;
    private Button addImageBtn;
    private Button exportPDFBtn;
    //private Button addShapesBtn;
    //private Button addMathsBtn;
    private ImageView homeBtn;
    private ImageView endBtn;
    private Button undoBtn;
    private Button redoBtn;
    private Button addTextBtn;
    private Button btnCloseRightDrawer;
    private LinearLayout upperNavigationLayout;
    private LinearLayout colorPaletLayout;
    private RelativeLayout rightTextureListDrawer;
    private ScrollView toolsLayout;
    private TextView pageIndex;
    private TextView pageIndex1;

    private ArrayList<Bitmap> imageScreenshotList;

    private boolean isRecordingRunning = false;

    private boolean isToolBarHidden = false;

    private boolean isResumed = false;

    private boolean isPaused = false;

    private boolean isImgeScaleDone = false;

    private ArrayList<String> sourceFilePath;

    private Uri fileUri;

    private int whiteBoardPagesCount = 20;

    private RecyclerView backTextureListView;

    private SessionManager sessionManager;

    private FileData fileData;

    private boolean isTextAddedOnBoard;

    private int counterInd;

    private int recordTime;
    private int tempRecordTime;
    private Handler handler;
    private TextView currentTimeText;
    private GlobalMethods globalMethods;
    private KnowledgeBrixUserData vData;
    private Userproductaccessresponse userproductaccessresponse;

    private boolean isTextureOpen = false;
    private boolean isFullScreen = false;

    //23Jan2020 - Raj K


    private EncoderConfig mEncoderConfig;
    private FrameEncoder frameEncoder;
    private  int DEFAULT_WIDTH = 320;
    private  int DEFAULT_HEIGHT = 240;
    private static final float DEFAULT_FRAME_RATE_PER_SECOND = 3;
    private static final int DEFAULT_BIT_RATE = 1000000;

    private String AudioSavePathInDevice = null;
    private String VideoSavePathInDevice = null;

    // public static SimpleTooltip tooltip = null;

    static {
        ORIENTATIONS.append(Surface.ROTATION_0, 90);
        ORIENTATIONS.append(Surface.ROTATION_90, 0);
        ORIENTATIONS.append(Surface.ROTATION_180, 270);
        ORIENTATIONS.append(Surface.ROTATION_270, 180);
    }

    @SuppressLint("WrongConstant")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_white_board);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        RecordWhiteBoardActivity.this.getPackageManager().hasSystemFeature("org.chromium.arc.device_management");

        /*View bView = getWindow().getDecorView();
        bView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);//Hiding hardware button of Android Devices*/

        sessionManager = new SessionManager(RecordWhiteBoardActivity.this);
        globalMethods = new GlobalMethods();
//        drawingView = (DrawingView) findViewById(R.id.drawing);

//        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide navigation bar
//                | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
//                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);

        imageScreenshotList = new ArrayList<Bitmap>();
        sourceFilePath = new ArrayList<>();
        // screen recording
        handler = new Handler();

        prevBtn = (ImageView) findViewById(R.id.prevBtn);
        nextBtn = (ImageView) findViewById(R.id.nextBtn);

        prevBtn1 = (ImageView) findViewById(R.id.prevBtn1);
        nextBtn1 = (ImageView) findViewById(R.id.nextBtn1);

        fullscreenBtn = (Button) findViewById(R.id.fullscreenBtn);


        colorsPaletBtn = (Button) findViewById(R.id.colorsPaletBtn);
        pencilStrokesBtn = (Button) findViewById(R.id.pencilStrokesBtn);
        shapesBtn = (Button) findViewById(R.id.shapesBtn);
        eraseBtn = (Button) findViewById(R.id.eraserBtn);
        removeBtn = (Button) findViewById(R.id.removeBtn);
        trashImagesBtn = (Button) findViewById(R.id.trashBtn);
        doneImagesBtn = (Button) findViewById(R.id.doneImagesBtn);
        addImageBtn = (Button) findViewById(R.id.addImageBtn);
        exportPDFBtn = (Button) findViewById(R.id.exportPDFBtn);
//        addShapesBtn = (Button) findViewById(R.id.addShapesBtn);
//        addMathsBtn = (Button) findViewById(R.id.addMathsBtn);
        addBGGraphicBtn = (Button) findViewById(R.id.addBGGraphicBtn);

        homeBtn = (ImageView) findViewById(R.id.homeBtn);
        endBtn = (ImageView) findViewById(R.id.endBtn);
        undoBtn = (Button) findViewById(R.id.undoBtn);
        redoBtn = (Button) findViewById(R.id.redoBtn);
        btnCloseRightDrawer = (Button) findViewById(R.id.btnCloseRightDrawer);
        showHideBtn = (ImageView) findViewById(R.id.showHideBtn);
        pageIndex = (TextView) findViewById(R.id.pageIndex);
        pageIndex1 = (TextView) findViewById(R.id.pageIndex1);
        currentTimeText = (TextView) findViewById(R.id.currentTimeText);
        addTextBtn = (Button) findViewById(R.id.addTextBtn);

        countDownTimerText = (TextView) findViewById(R.id.countDownTimerText);

        mainContainer = (RelativeLayout) findViewById(R.id.mainContainer);
        toolsLayout = (ScrollView) findViewById(R.id.toolsLayout);
        rightTextureListDrawer = (RelativeLayout) findViewById(R.id.rightTextureListDrawer);
        colorPaletLayout = (LinearLayout) findViewById(R.id.colorPaletLayout);
        upperNavigationLayout = (LinearLayout) findViewById(R.id.upperNavigationLayout);
//        colorPaletLayout.setVisibility(View.GONE);
//        undoBtn.setVisibility(View.GONE);
//        redoBtn.setVisibility(View.GONE);
        upperNavigationLayout.setVisibility(View.GONE);

        whiteBoardViewPager = (CustomViewPager) findViewById(R.id.whiteBoardViewPager);
        whiteBoardViewPager.setPagingEnabled(false);
        whiteBoardViewPagerAdapter = new WhiteBoardViewPagerAdapter(getSupportFragmentManager(), whiteBoardPagesCount);
        whiteBoardViewPager.setAdapter(whiteBoardViewPagerAdapter);
        whiteBoardViewPager.setOffscreenPageLimit(whiteBoardPagesCount);

        startBtn = (ImageView) findViewById(R.id.startBtn);
        stopBtn = (ImageView) findViewById(R.id.stopBtn);

        resumeBtnOut = (ImageView) findViewById(R.id.resumeBtnOut);
        pauseBtnOut = (ImageView) findViewById(R.id.pauseBtnOut);
        stopBtnOut = (ImageView) findViewById(R.id.stopBtnOut);

        backTextureListView = (RecyclerView) findViewById(R.id.backTextureListView);

        stopBtn.setVisibility(View.GONE);

        resumeBtnOut.setVisibility(View.GONE);
        pauseBtnOut.setVisibility(View.GONE);
        stopBtnOut.setVisibility(View.GONE);
        ////////////////////////

//        showToast("You can add and rescale image on whiteboard before start recording.");

        if (!sessionManager.getInfoDialogSeenStatus()) {
            InformatoryDialog informatoryDialog = new InformatoryDialog(RecordWhiteBoardActivity.this, R.layout.image_scale_info_dialog, null, null);
//            informatoryDialog.showMe(); commented for demo
            sessionManager.updateInfoDialogSeenStatus(true);
        }

//        drawingView.setEnabled(false);

        // count down time
        countDownTime = 5; // in sec
        countDownInterval = 1000; // in sec

        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);

        mScreenDensity = metrics.densityDpi;

        if (sessionManager.hasSessionVizippCredentials()) {
            vData = sessionManager.getLoggedKnowledgeBrixUserData();
        }
        getUserProducts(fileData, vData.getId(), vData.getAccessToken());

//        DISPLAY_HEIGHT = metrics.heightPixels / mScreenDensity;
//        DISPLAY_WIDTH = metrics.widthPixels / mScreenDensity;

        mMediaRecorder = new MediaRecorder();
        mProjectionManager = (MediaProjectionManager) getSystemService
                (Context.MEDIA_PROJECTION_SERVICE);

//        countDownTimerText.setVisibility(View.VISIBLE);
        countDownTimerText.setText("" + countDownTime);
        myCountDownTimer = new MyCountDownTimer(countDownTime * 1000, countDownInterval);
//        myCountDownTimer.start();

        showHideBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showHideToolbar();
            }
        });

        fullscreenBtn.setOnClickListener(new View.OnClickListener() {// added by pranav in 30/09/2019
            @Override
            public void onClick(View v) {
                View decorView = getWindow().getDecorView();
                if (isFullScreen != true) {
                    decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_IMMERSIVE
                            | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            // Hide the nav bar and status bar
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN);
                    isFullScreen = true;
                    fullscreenBtn.setBackgroundResource(R.drawable.normalscreen);
                } else {
                    decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
                    isFullScreen = false;
                    fullscreenBtn.setBackgroundResource(R.drawable.fullscreen);
                }

            }
        });

        startBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TypedValue outValue = new TypedValue();
                RecordWhiteBoardActivity.this.getTheme().resolveAttribute(android.R.attr.selectableItemBackground, outValue, true);
                startBtn.setBackgroundResource(outValue.resourceId);

                view.setVisibility(View.GONE);


                showHideBtn.setVisibility(View.VISIBLE);//change shashank for demo
//                addBGGraphicBtn.setVisibility(View.INVISIBLE);

               /* showHideBtn.setImageDrawable(ContextCompat.getDrawable(RecordWhiteBoardActivity.this, R.drawable.upward_grey));
                upperNavigationLayout.setVisibility(View.VISIBLE);
                toolsLayout.setVisibility(View.GONE);
                isToolBarHidden = true;*/

                rightTextureListDrawer.setVisibility(View.GONE);
                trashImagesBtn.setVisibility(View.GONE);
                doneImagesBtn.setVisibility(View.GONE);
                colorPaletLayout.setVisibility(View.VISIBLE);
                exportPDFBtn.setVisibility(View.VISIBLE);
                undoBtn.setVisibility(View.VISIBLE);
                redoBtn.setVisibility(View.VISIBLE);
                //blackBtn.callOnClick();//09
                //setPenColor(Color.BLACK);
                showHideBtn.callOnClick();

                setImageMovable(false);
                whiteBoardViewPager.setCurrentItem(0);
                countDownTimerText.setVisibility(View.VISIBLE);
                myCountDownTimer.start();
                getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide navigation bar
                        | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);

                if (sessionManager.getSessionVibratePhoneState()) {
                    globalMethods.vibratePhone(RecordWhiteBoardActivity.this, 500);
                }

            }
        });

        resumeBtnOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*TypedValue outValue = new TypedValue();
                RecordWhiteBoardActivity.this.getTheme().resolveAttribute(android.R.attr.selectableItemBackground, outValue, true);
                resumeBtnOut.setBackgroundResource(outValue.resourceId);*/

                /*pauseBtnOut.setVisibility(View.VISIBLE);///demo change shashank
                resumeBtnOut.setVisibility(View.GONE);
                rightTextureListDrawer.setVisibility(View.GONE);
                isResumed = true;
                isPaused = false;
                resumeScreenSharing();*///origin


                ///demo change shashank
                resumeVedio();
                //handler.post(UpdateRecordTime);

            }
        });

        pauseBtnOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*TypedValue outValue = new TypedValue();
                RecordWhiteBoardActivity.this.getTheme().resolveAttribute(android.R.attr.selectableItemBackground, outValue, true);
                pauseBtnOut.setBackgroundResource(outValue.resourceId);*/

                pauseVedio();

            }
        });

        stopBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                addBGGraphicBtn.setVisibility(View.VISIBLE);
                isRecordingRunning = false;
                if (isResumed) {
                    if(sessionManager.getSessionAppIsScreenCastActive()){
                        stopScreenCastRecording();
                    }else{
                        onToggleScreenShare(false);
                    }
                } else if (isPaused) {
                    if(sessionManager.getSessionAppIsScreenCastActive()){
                        stopScreenCastRecording();
                    }else {
                        destroyMediaProjection();
                    }
                    setResult(REQUEST_CODE);
                    finish();
                    showToast("Video recorded successfully.", Toast.LENGTH_SHORT);
                    recordTime = 0;
                    currentTimeText.setVisibility(View.GONE);
                    startActivity(new Intent(RecordWhiteBoardActivity.this, MainActivity.class));
                } else {
                    if(sessionManager.getSessionAppIsScreenCastActive()){
                        stopScreenCastRecording();
                    }else{
                        onToggleScreenShare(false);
                    }
                    setResult(REQUEST_CODE);
                    finish();
                    showToast("Video recorded successfully.", Toast.LENGTH_SHORT);
                    recordTime = 0;
                    currentTimeText.setVisibility(View.GONE);
                    startActivity(new Intent(RecordWhiteBoardActivity.this, MainActivity.class));
                }
            }
        });

        stopBtnOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                TypedValue outValue = new TypedValue();
                RecordWhiteBoardActivity.this.getTheme().resolveAttribute(android.R.attr.selectableItemBackground, outValue, true);
                stopBtnOut.setBackgroundResource(outValue.resourceId);

                addBGGraphicBtn.setVisibility(View.VISIBLE);

                isRecordingRunning = false;
                if (isResumed) {
                    if(sessionManager.getSessionAppIsScreenCastActive()){
                        stopScreenCastRecording();
                    }else{
                        onToggleScreenShare(false);
                    }
                } else if (isPaused) {
                    if(sessionManager.getSessionAppIsScreenCastActive()){
                        stopScreenCastRecording();
                    }else {
                        destroyMediaProjection();
                    }
                    setResult(REQUEST_CODE);
                    finish();
                    showToast("Video recorded successfully.", Toast.LENGTH_SHORT);
                    recordTime = 0;
                    currentTimeText.setVisibility(View.GONE);
                    startActivity(new Intent(RecordWhiteBoardActivity.this, MainActivity.class));
                } else {
                    if(sessionManager.getSessionAppIsScreenCastActive()){
                        stopScreenCastRecording();
                    }else{
                        onToggleScreenShare(false);
                    }
                    setResult(REQUEST_CODE);
                    finish();
                    showToast("Video recorded successfully.", Toast.LENGTH_SHORT);
                    recordTime = 0;
                    currentTimeText.setVisibility(View.GONE);
                    startActivity(new Intent(RecordWhiteBoardActivity.this, MainActivity.class));
                }
            }
        });

        prevBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //previous page
                addViewScreenshotInList(whiteBoardViewPager.getCurrentItem());
                if (whiteBoardViewPager.getCurrentItem() > 0) {
                    whiteBoardViewPager.setCurrentItem(whiteBoardViewPager.getCurrentItem() - 1);
                }
            }
        });

        nextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //next page
                addViewScreenshotInList(whiteBoardViewPager.getCurrentItem());
                if (whiteBoardViewPager.getCurrentItem() < whiteBoardViewPager.getAdapter().getCount() - 1) {
                    whiteBoardViewPager.setCurrentItem(whiteBoardViewPager.getCurrentItem() + 1);
                }
                /*if (counterInd == whiteBoardViewPager.getCurrentItem()) {
                    counterInd++;
                    if (whiteBoardViewPager.getCurrentItem() < whiteBoardViewPager.getAdapter().getCount() - 1) {
                        int cItm = whiteBoardViewPager.getCurrentItem() + 1;
                        int cLimit = whiteBoardViewPager.getOffscreenPageLimit();
                        whiteBoardViewPager.setCurrentItem(cItm);
                        int rItm = whiteBoardViewPager.getCurrentItem();
                        whiteBoardViewPager.setOffscreenPageLimit(rItm + 1);
                    }
                } else {

                    if (whiteBoardViewPager.getCurrentItem() < whiteBoardViewPager.getAdapter().getCount() - 1) {
                        whiteBoardViewPager.setCurrentItem(whiteBoardViewPager.getCurrentItem() + 1);
                    }
                }*/
            }
        });

        prevBtn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //previous page
                if (whiteBoardViewPager.getCurrentItem() > 0) {
                    whiteBoardViewPager.setCurrentItem(whiteBoardViewPager.getCurrentItem() - 1);
                }
            }
        });

        nextBtn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //next page
                if (whiteBoardViewPager.getCurrentItem() < whiteBoardViewPager.getAdapter().getCount() - 1) {
                    whiteBoardViewPager.setCurrentItem(whiteBoardViewPager.getCurrentItem() + 1);
                }
               /* if (counterInd == whiteBoardViewPager.getCurrentItem()) {
                    counterInd++;
                    //next page
                    if (whiteBoardViewPager.getCurrentItem() < whiteBoardViewPager.getAdapter().getCount() - 1) {
                        int cItm = whiteBoardViewPager.getCurrentItem() + 1;
                        int cLimit = whiteBoardViewPager.getOffscreenPageLimit();
                        whiteBoardViewPager.setCurrentItem(cItm);
                        int rItm = whiteBoardViewPager.getCurrentItem();
                        whiteBoardViewPager.setOffscreenPageLimit(rItm + 1);

                    }
                } else {
                    if (whiteBoardViewPager.getCurrentItem() < whiteBoardViewPager.getAdapter().getCount() - 1) {
                        whiteBoardViewPager.setCurrentItem(whiteBoardViewPager.getCurrentItem() + 1);
                    }
                }*/
            }
        });

        /*addMathsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (userproductaccessresponse.getProductAccessList().size() < 0) {
                    showToast("Unsubscribed - user");
                } else {
                    for (int i = 0; i < whiteBoardViewPagerAdapter.getFragmentsList().size(); i++) {
                        WhiteBoardPageFragment temp = (WhiteBoardPageFragment) whiteBoardViewPagerAdapter.getFragmentsList().get(i);
                        if (temp != null){
                            temp.setMathRecognationViewVisibility(true);
                        }
                            *//*if (temp.getFragmentId() == whiteBoardViewPager.getCurrentItem()) {
                                temp.setMathRecognationViewVisibility(true);
                                break;
                            }*//*

                    }
                }
            }
        });

        addShapesBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (userproductaccessresponse.getProductAccessList().size() < 0) {
                    showToast("Unsubscribed - user");
                } else {
                    for (int i = 0; i < whiteBoardViewPagerAdapter.getFragmentsList().size(); i++) {
                        WhiteBoardPageFragment temp = (WhiteBoardPageFragment) whiteBoardViewPagerAdapter.getFragmentsList().get(i);
                        if (temp != null){
                            temp.setShapeRecognationViewVisibility(true);
                        }
                    }
                }
                *//*final SimpleTooltip tooltip = new SimpleTooltip.Builder(RecordWhiteBoardActivity.this)
                        .anchorView(v)
                        .gravity(Gravity.END)
                        .dismissOnOutsideTouch(true)
                        .dismissOnInsideTouch(false)
                        .modal(true)
                        .animated(true)
                        .animationDuration(1000)
                        .contentView(R.layout.item_select_shapes)
                        .focusable(true)
                        .build();
                final Button digramRecogBtn = tooltip.findViewById(R.id.digramRecogBtn);
                final Button mathRecogBtn = tooltip.findViewById(R.id.mathRecogBtn);

                digramRecogBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        tooltip.dismiss();
                    }
                });
                mathRecogBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        tooltip.dismiss();
                    }
                });
                tooltip.show();*//*
            }
        });*/

        shapesBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final SimpleTooltip tooltip = new SimpleTooltip.Builder(RecordWhiteBoardActivity.this)
                        .anchorView(v)
                        .gravity(Gravity.END)
                        .dismissOnOutsideTouch(true)
                        .dismissOnInsideTouch(false)
                        .modal(true)
                        .animated(true)
                        .animationDuration(1000)
                        .contentView(R.layout.item_shapes_palet)
                        .focusable(true)
                        .build();
                final Button circleShapeBtn = tooltip.findViewById(R.id.circleShapeBtn);
                final Button rectShapeBtn = tooltip.findViewById(R.id.rectShapeBtn);
                final Button squareShapeBtn = tooltip.findViewById(R.id.squareShapeBtn);
                final Button triangleShapeBtn = tooltip.findViewById(R.id.triangleShapeBtn);

                circleShapeBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        for (int i = 0; i < whiteBoardViewPagerAdapter.getFragmentsList().size(); i++) {
                            WhiteBoardPageFragment temp = (WhiteBoardPageFragment) whiteBoardViewPagerAdapter.getFragmentsList().get(i);
                            temp.enableShapeDrawingView(true);
                            temp.setShapeDrawingId(0);
                        }
                        tooltip.dismiss();
                        showHideBtn.callOnClick();
                    }
                });

                rectShapeBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        for (int i = 0; i < whiteBoardViewPagerAdapter.getFragmentsList().size(); i++) {
                            WhiteBoardPageFragment temp = (WhiteBoardPageFragment) whiteBoardViewPagerAdapter.getFragmentsList().get(i);
                            temp.enableShapeDrawingView(true);
                            temp.setShapeDrawingId(1);
                        }
                        tooltip.dismiss();
                        showHideBtn.callOnClick();
                    }
                });

                squareShapeBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        for (int i = 0; i < whiteBoardViewPagerAdapter.getFragmentsList().size(); i++) {
                            WhiteBoardPageFragment temp = (WhiteBoardPageFragment) whiteBoardViewPagerAdapter.getFragmentsList().get(i);
                            temp.enableShapeDrawingView(true);
                            temp.setShapeDrawingId(3);
                        }
                        tooltip.dismiss();
                        showHideBtn.callOnClick();
                    }
                });

                triangleShapeBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        for (int i = 0; i < whiteBoardViewPagerAdapter.getFragmentsList().size(); i++) {
                            WhiteBoardPageFragment temp = (WhiteBoardPageFragment) whiteBoardViewPagerAdapter.getFragmentsList().get(i);
                            temp.enableShapeDrawingView(true);
                            temp.setShapeDrawingId(2);
                        }
                        tooltip.dismiss();
                        showHideBtn.callOnClick();
                    }
                });
                tooltip.show();
            }
        });

        pencilStrokesBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final SimpleTooltip tooltip = new SimpleTooltip.Builder(RecordWhiteBoardActivity.this)
                        .anchorView(v)
                        .gravity(Gravity.END)
                        .dismissOnOutsideTouch(true)
                        .dismissOnInsideTouch(false)
                        .modal(true)
                        .animated(true)
                        .animationDuration(1000)
                        .contentView(R.layout.item_pencile_strokes_palet)
                        .focusable(true)
                        .build();
                final Button smallPencilSizeBtn = tooltip.findViewById(R.id.smallPencilSizeBtn);
                final Button mediumPencilSizeBtn = tooltip.findViewById(R.id.mediumPencilSizeBtn);
                final Button largePencilSizeBtn = tooltip.findViewById(R.id.largePencilSizeBtn);

//.backgroundColor(selectedPencilColor)
                if (tooltip.isShowing())
                    tooltip.dismiss();

                smallPencilSizeBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                /*TypedValue outValue = new TypedValue();
                RecordWhiteBoardActivity.this.getTheme().resolveAttribute(android.R.attr.selectableItemBackground, outValue, true);
                smallPencilSizeBtn.setBackgroundResource(outValue.resourceId);*/
                        //smallPencilSizeBtn.setBackground(ContextCompat.getDrawable(RecordWhiteBoardActivity.this, R.drawable.pencil_small_blue));
                        eraseBtn.setBackground(ContextCompat.getDrawable(RecordWhiteBoardActivity.this, R.drawable.icon_eraser_gray));
                        setEraser(false);
                        pencilStrokesBtn.setBackground(ContextCompat.getDrawable(RecordWhiteBoardActivity.this, R.drawable.pencil_small));
                        mediumPencilSizeBtn.setBackground(ContextCompat.getDrawable(RecordWhiteBoardActivity.this, R.drawable.pencil_medium));
                        largePencilSizeBtn.setBackground(ContextCompat.getDrawable(RecordWhiteBoardActivity.this, R.drawable.pencil_large));
                        for (int i = 0; i < whiteBoardViewPagerAdapter.getFragmentsList().size(); i++) {
                            WhiteBoardPageFragment temp = (WhiteBoardPageFragment) whiteBoardViewPagerAdapter.getFragmentsList().get(i);
                            temp.setPenStrokeForDrawing(2);
                        }
                        tooltip.dismiss();
                        showHideBtn.callOnClick();

                    }
                });
                //pencilStrokesBtn.setBackground(ContextCompat.getDrawable(RecordWhiteBoardActivity.this, R.drawable.pencil_medium_blue));
                for (int i = 0; i < whiteBoardViewPagerAdapter.getFragmentsList().size(); i++) {
                    WhiteBoardPageFragment temp = (WhiteBoardPageFragment) whiteBoardViewPagerAdapter.getFragmentsList().get(i);
                    temp.setPenStrokeForDrawing(6);
                    temp.enableShapeDrawingView(false);
                }
                mediumPencilSizeBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                /*TypedValue outValue = new TypedValue();
                RecordWhiteBoardActivity.this.getTheme().resolveAttribute(android.R.attr.selectableItemBackground, outValue, true);
                mediumPencilSizeBtn.setBackgroundResource(outValue.resourceId);*/
                        eraseBtn.setBackground(ContextCompat.getDrawable(RecordWhiteBoardActivity.this, R.drawable.icon_eraser_gray));
                        setEraser(false);
                        smallPencilSizeBtn.setBackground(ContextCompat.getDrawable(RecordWhiteBoardActivity.this, R.drawable.pencil_small));
                        // mediumPencilSizeBtn.setBackground(ContextCompat.getDrawable(RecordWhiteBoardActivity.this, R.drawable.pencil_medium_blue));
                        mediumPencilSizeBtn.setBackground(ContextCompat.getDrawable(RecordWhiteBoardActivity.this, R.drawable.pencil_medium));
                        largePencilSizeBtn.setBackground(ContextCompat.getDrawable(RecordWhiteBoardActivity.this, R.drawable.pencil_large));
                        for (int i = 0; i < whiteBoardViewPagerAdapter.getFragmentsList().size(); i++) {
                            WhiteBoardPageFragment temp = (WhiteBoardPageFragment) whiteBoardViewPagerAdapter.getFragmentsList().get(i);
                            temp.setPenStrokeForDrawing(6);
                        }
                        tooltip.dismiss();
                        showHideBtn.callOnClick();

                    }
                });
                largePencilSizeBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                /*TypedValue outValue = new TypedValue();
                RecordWhiteBoardActivity.this.getTheme().resolveAttribute(android.R.attr.selectableItemBackground, outValue, true);
                largePencilSizeBtn.setBackgroundResource(outValue.resourceId);*/
                        eraseBtn.setBackground(ContextCompat.getDrawable(RecordWhiteBoardActivity.this, R.drawable.icon_eraser_gray));
                        setEraser(false);
                        smallPencilSizeBtn.setBackground(ContextCompat.getDrawable(RecordWhiteBoardActivity.this, R.drawable.pencil_small));
                        mediumPencilSizeBtn.setBackground(ContextCompat.getDrawable(RecordWhiteBoardActivity.this, R.drawable.pencil_medium));
                        //largePencilSizeBtn.setBackground(ContextCompat.getDrawable(RecordWhiteBoardActivity.this, R.drawable.pencil_large_blue));
                        largePencilSizeBtn.setBackground(ContextCompat.getDrawable(RecordWhiteBoardActivity.this, R.drawable.pencil_large));
                        for (int i = 0; i < whiteBoardViewPagerAdapter.getFragmentsList().size(); i++) {
                            WhiteBoardPageFragment temp = (WhiteBoardPageFragment) whiteBoardViewPagerAdapter.getFragmentsList().get(i);
                            temp.setPenStrokeForDrawing(10);
                        }
                        tooltip.dismiss();
                        showHideBtn.callOnClick();

                    }
                });
                tooltip.show();

            }
        });

        colorsPaletBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                eraseBtn.setBackground(ContextCompat.getDrawable(RecordWhiteBoardActivity.this, R.drawable.icon_eraser_gray));
                setEraser(false);
                final SimpleTooltip tooltip = new SimpleTooltip.Builder(RecordWhiteBoardActivity.this)
                        .anchorView(v)
                        .gravity(Gravity.END)
                        .dismissOnOutsideTouch(true)
                        .dismissOnInsideTouch(false)
                        .modal(true)
                        .animated(true)
                        .animationDuration(1000)
                        .contentView(R.layout.item_colors_palet)
                        .focusable(true)
                        .build();
                final Button blueBtn = tooltip.findViewById(R.id.blueBtn);
                final Button redBtn = tooltip.findViewById(R.id.redBtn);
                final Button greenBtn = tooltip.findViewById(R.id.greenBtn);
                final Button blackBtn = tooltip.findViewById(R.id.blackBtn);

                if (tooltip.isShowing())
                    tooltip.dismiss();

                blackBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
//                eraseBtn.setBackgroundTintList(ContextCompat.getColorStateList(RecordWhiteBoardActivity.this, R.color.ColorsWhite));
                        eraseBtn.setBackground(ContextCompat.getDrawable(RecordWhiteBoardActivity.this, R.drawable.icon_eraser_gray));
                        setEraser(false);
                        redBtn.setBackground(ContextCompat.getDrawable(RecordWhiteBoardActivity.this, R.drawable.round_image_red_whiteborder));
                        greenBtn.setBackground(ContextCompat.getDrawable(RecordWhiteBoardActivity.this, R.drawable.round_image_green_whiteborder));
                        blueBtn.setBackground(ContextCompat.getDrawable(RecordWhiteBoardActivity.this, R.drawable.round_image_blue_whiteborder));

                        //blackBtn.setBackground(ContextCompat.getDrawable(RecordWhiteBoardActivity.this, R.drawable.icon_pencil));
                        colorsPaletBtn.setBackground(ContextCompat.getDrawable(RecordWhiteBoardActivity.this, R.drawable.round_image_black_whiteborder));

                        setPenColor(Color.BLACK);
                        tooltip.dismiss();
                        showHideBtn.callOnClick();
                    }
                });

                redBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
//                eraseBtn.setBackgroundTintList(ContextCompat.getColorStateList(RecordWhiteBoardActivity.this, R.color.ColorsWhite));
                        eraseBtn.setBackground(ContextCompat.getDrawable(RecordWhiteBoardActivity.this, R.drawable.icon_eraser_gray));
                        setEraser(false);
                        //redBtn.setBackground(ContextCompat.getDrawable(RecordWhiteBoardActivity.this, R.drawable.icon_pencil));
                        colorsPaletBtn.setBackground(ContextCompat.getDrawable(RecordWhiteBoardActivity.this, R.drawable.round_image_red_whiteborder));

                        blackBtn.setBackground(ContextCompat.getDrawable(RecordWhiteBoardActivity.this, R.drawable.round_image_black_whiteborder));
                        greenBtn.setBackground(ContextCompat.getDrawable(RecordWhiteBoardActivity.this, R.drawable.round_image_green_whiteborder));
                        blueBtn.setBackground(ContextCompat.getDrawable(RecordWhiteBoardActivity.this, R.drawable.round_image_blue_whiteborder));

                        setPenColor(Color.RED);
                        tooltip.dismiss();
                        showHideBtn.callOnClick();
                    }
                });

                greenBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
//                      eraseBtn.setBackgroundTintList(ContextCompat.getColorStateList(RecordWhiteBoardActivity.this, R.color.ColorsWhite));
                        eraseBtn.setBackground(ContextCompat.getDrawable(RecordWhiteBoardActivity.this, R.drawable.icon_eraser_gray));
                        setEraser(false);
                        //greenBtn.setBackground(ContextCompat.getDrawable(RecordWhiteBoardActivity.this, R.drawable.icon_pencil));
                        colorsPaletBtn.setBackground(ContextCompat.getDrawable(RecordWhiteBoardActivity.this, R.drawable.round_image_green_whiteborder));

                        blackBtn.setBackground(ContextCompat.getDrawable(RecordWhiteBoardActivity.this, R.drawable.round_image_black_whiteborder));
                        redBtn.setBackground(ContextCompat.getDrawable(RecordWhiteBoardActivity.this, R.drawable.round_image_red_whiteborder));
                        blueBtn.setBackground(ContextCompat.getDrawable(RecordWhiteBoardActivity.this, R.drawable.round_image_blue_whiteborder));

                        setPenColor(Color.GREEN);
                        tooltip.dismiss();
                        showHideBtn.callOnClick();
                    }
                });

                blueBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
//                eraseBtn.setBackgroundTintList(ContextCompat.getColorStateList(RecordWhiteBoardActivity.this, R.color.ColorsWhite));
                        eraseBtn.setBackground(ContextCompat.getDrawable(RecordWhiteBoardActivity.this, R.drawable.icon_eraser_gray));
                        setEraser(false);
                        //blueBtn.setBackground(ContextCompat.getDrawable(RecordWhiteBoardActivity.this, R.drawable.icon_pencil));
                        colorsPaletBtn.setBackground(ContextCompat.getDrawable(RecordWhiteBoardActivity.this, R.drawable.round_image_blue_whiteborder));

                        redBtn.setBackground(ContextCompat.getDrawable(RecordWhiteBoardActivity.this, R.drawable.round_image_red_whiteborder));
                        greenBtn.setBackground(ContextCompat.getDrawable(RecordWhiteBoardActivity.this, R.drawable.round_image_green_whiteborder));
                        blackBtn.setBackground(ContextCompat.getDrawable(RecordWhiteBoardActivity.this, R.drawable.round_image_black_whiteborder));

                        setPenColor(Color.BLUE);
                        tooltip.dismiss();
                        showHideBtn.callOnClick();
                    }
                });
                tooltip.show();


            }
        });

        eraseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                eraseBtn.setBackgroundTintList(ContextCompat.getColorStateList(RecordWhiteBoardActivity.this, R.color.SubTextColor));
//                blackBtn.setBackgroundColor(ContextCompat.getColor(RecordWhiteBoardActivity.this, R.color.ColorsBlack));
//                redBtn.setBackgroundColor(ContextCompat.getColor(RecordWhiteBoardActivity.this, R.color.ColorRed));
//                blueBtn.setBackgroundColor(ContextCompat.getColor(RecordWhiteBoardActivity.this, R.color.ColorsBlue));
//                greenBtn.setBackgroundColor(ContextCompat.getColor(RecordWhiteBoardActivity.this, R.color.ColorsGreen));

                /*blackBtn.setBackground(ContextCompat.getDrawable(RecordWhiteBoardActivity.this, R.drawable.round_image_black_whiteborder));
                redBtn.setBackground(ContextCompat.getDrawable(RecordWhiteBoardActivity.this, R.drawable.round_image_red_whiteborder));
                greenBtn.setBackground(ContextCompat.getDrawable(RecordWhiteBoardActivity.this, R.drawable.round_image_green_whiteborder));
                blueBtn.setBackground(ContextCompat.getDrawable(RecordWhiteBoardActivity.this, R.drawable.round_image_blue_whiteborder));*/

                eraseBtn.setBackground(ContextCompat.getDrawable(RecordWhiteBoardActivity.this, R.drawable.icon_eraser_blue));
                setEraser(true);
                showHideBtn.callOnClick();
            }
        });

        removeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                eraseBtn.setBackgroundTintList(ContextCompat.getColorStateList(RecordWhiteBoardActivity.this, R.color.ColorsWhite));
                eraseBtn.setBackground(ContextCompat.getDrawable(RecordWhiteBoardActivity.this, R.drawable.icon_eraser_gray));
                setEraser(false);
                clearDrawingBoard();
                //blackBtn.callOnClick();//09
                //setPenColor(Color.BLACK);
                //blackBtn.setBackground(ContextCompat.getDrawable(RecordWhiteBoardActivity.this, R.drawable.icon_pencil));
                //colorsPaletBtn.setBackground(ContextCompat.getDrawable(RecordWhiteBoardActivity.this, R.drawable.round_image_black_whiteborder));
                showHideBtn.callOnClick();
            }
        });

        exportPDFBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ContextCompat.checkSelfPermission(RecordWhiteBoardActivity.this,Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(RecordWhiteBoardActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

                        Snackbar.make(findViewById(android.R.id.content), R.string.label_permissions_storage,
                                Snackbar.LENGTH_INDEFINITE).setAction("ENABLE",
                                new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        ActivityCompat.requestPermissions(RecordWhiteBoardActivity.this,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},REQUEST_PERMISSIONS_PDF_EXPORT);
                                    }
                                }).show();
                    } else {
                        ActivityCompat.requestPermissions(RecordWhiteBoardActivity.this,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},REQUEST_PERMISSIONS_PDF_EXPORT);
                    }
                }else {
                    addViewScreenshotInList(whiteBoardViewPager.getCurrentItem());
                    exportPDFBtn.setEnabled(false);
                    exportAsPdfFile();
                }
            }
        });

        trashImagesBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clearGraphicsImages();
            }
        });


      /*  addShapesBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (int i = 0; i < whiteBoardViewPagerAdapter.getFragmentsList().size(); i++) {
                    WhiteBoardPageFragment temp = (WhiteBoardPageFragment) whiteBoardViewPagerAdapter.getFragmentsList().get(i);
                    temp.setPenStrokeForDrawing(10);
                }
            }
        });*/

        addImageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                eraseBtn.setBackground(ContextCompat.getDrawable(RecordWhiteBoardActivity.this, R.drawable.icon_eraser_gray));
                setEraser(false);
                /*if (isRecordingRunning && !isPaused) {
                    pauseBtnOut.callOnClick();
                }
                 if (getGlobalMethods().checkCameraAudioRecordPermission(RecordWhiteBoardActivity.this)) {
                        //openPictureGallery();
                        openImagesFolder();
                    } else {
                        String msg = getGlobalMethods().requestCameraAudioWithReadWriteExternalStoragePermission(RecordWhiteBoardActivity.this);
                        showToast(msg);
                    }*///origin
                final SimpleTooltip tooltip = new SimpleTooltip.Builder(RecordWhiteBoardActivity.this)
                        .anchorView(view)
                        .gravity(Gravity.END)
                        .dismissOnOutsideTouch(true)
                        .dismissOnInsideTouch(false)
                        .modal(true)
                        .animated(true)
                        .animationDuration(1000)
                        .contentView(R.layout.item_select_imgsource)
                        .focusable(true)
                        .build();
                final Button cameraBtn = tooltip.findViewById(R.id.cameraBtn);
                final Button galleryBtn = tooltip.findViewById(R.id.galleryBtn);
                galleryBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (isChromebook()) {
                            if (getGlobalMethods().checkCameraAudioRecordPermission(RecordWhiteBoardActivity.this)) {
                                //openPictureGallery();
                                openImagesFolder();
                            } else {
                                String msg = getGlobalMethods().requestCameraAudioWithReadWriteExternalStoragePermission(RecordWhiteBoardActivity.this);
                                showToast(msg);
                            }
                        } else {
                            if (isRecordingRunning && !isPaused) {
                                pauseBtnOut.callOnClick();
//                                showToast("Please pause the recording to add image !!");
                            } else {
                                if (getGlobalMethods().checkCameraAudioRecordPermission(RecordWhiteBoardActivity.this)) {
                                    //openPictureGallery();
                                    openImagesFolder();
                                } else {
                                    String msg = getGlobalMethods().requestCameraAudioWithReadWriteExternalStoragePermission(RecordWhiteBoardActivity.this);
                                    showToast(msg);
                                }
                            }
                        }
                        rightTextureListDrawer.setVisibility(View.GONE);
                    }
                });
                cameraBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (isChromebook()) {
                            if (getGlobalMethods().checkCameraAudioRecordPermission(RecordWhiteBoardActivity.this)) {
                                //openPictureGallery();
                                opencameraIntent();//6666
                            } else {
                                String msg = getGlobalMethods().requestCameraAudioWithReadWriteExternalStoragePermission(RecordWhiteBoardActivity.this);
                                showToast(msg);
                            }
                        } else {
                            if (isRecordingRunning && !isPaused) {
                                //pauseBtnOut.callOnClick();
                                showToast("Please pause the recording to add image !!");
                            } else {
                                if (getGlobalMethods().checkCameraAudioRecordPermission(RecordWhiteBoardActivity.this)) {
                                    // openPictureGallery();
                                    opencameraIntent();
                                } else {
                                    String msg = getGlobalMethods().requestCameraAudioWithReadWriteExternalStoragePermission(RecordWhiteBoardActivity.this);
                                    showToast(msg);
                                }
                            }
                        }
                        rightTextureListDrawer.setVisibility(View.GONE);
                    }
                });
                tooltip.show();


            }
        });

        btnCloseRightDrawer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                rightTextureListDrawer.setVisibility(View.GONE);
                if (isRecordingRunning && isPaused) {
                    resumeBtnOut.callOnClick();
                }
            }
        });

        addBGGraphicBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isRecordingRunning && !isPaused) {
                    pauseBtnOut.callOnClick();
                }
                showTextureList();

            }
        });

        addTextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*if (isRecordingRunning && !isPaused) {
                    pauseBtnOut.callOnClick();
                }*/
                TextEditorDialog editorDialog = new TextEditorDialog(RecordWhiteBoardActivity.this, new TextEditorDialog.ActionButtonListener() {
                    @Override
                    public void onDoneText(String textString, int textStyle, int textSize, boolean isUnderlined) {
                        isTextAddedOnBoard = true;
                        trashImagesBtn.setVisibility(View.VISIBLE);
                        doneImagesBtn.setVisibility(View.VISIBLE);
                        colorPaletLayout.setVisibility(View.GONE);
                        exportPDFBtn.setVisibility(View.GONE);
                        //addImageBtn.setVisibility(View.GONE);
                        addBGGraphicBtn.setVisibility(View.GONE);
                        WhiteBoardPageFragment temp = (WhiteBoardPageFragment) whiteBoardViewPagerAdapter.getFragmentsList().get(whiteBoardViewPager.getCurrentItem());
                        temp.addDraggableText(textString, textStyle, textSize, isUnderlined);

                    }
                });
                editorDialog.showMe();
                rightTextureListDrawer.setVisibility(View.GONE);
            }
        });

        doneImagesBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //TO DO
                trashImagesBtn.setVisibility(View.GONE);
                doneImagesBtn.setVisibility(View.GONE);
                //addTextBtn.setVisibility(View.VISIBLE); by 008
                addTextBtn.setVisibility(View.GONE);//by 008
                exportPDFBtn.setVisibility(View.VISIBLE);
                colorPaletLayout.setVisibility(View.VISIBLE);
                isTextAddedOnBoard = false;
                setImageMovable(false);
                //resumeBtnOut.callOnClick();

                if (isRecordingRunning && isPaused) {
                    resumeBtnOut.callOnClick();//origin
//                    showToast("click on resume button to continue recording");
                    isImgeScaleDone = true;//change
                }
            }
        });

        homeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addViewScreenshotInList(whiteBoardViewPager.getCurrentItem());
                whiteBoardViewPager.setCurrentItem(0);
            }
        });

        endBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addViewScreenshotInList(whiteBoardViewPager.getCurrentItem());
                int index = whiteBoardViewPager.getAdapter().getCount();
                whiteBoardViewPager.setCurrentItem(index);
            }
        });

        undoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                eraseBtn.setBackground(ContextCompat.getDrawable(RecordWhiteBoardActivity.this, R.drawable.icon_eraser_gray));
                setEraser(false);
                WhiteBoardPageFragment temp = (WhiteBoardPageFragment) whiteBoardViewPagerAdapter.getFragmentsList().get(whiteBoardViewPager.getCurrentItem());
                temp.undoDrawing();
            }
        });

        redoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                eraseBtn.setBackground(ContextCompat.getDrawable(RecordWhiteBoardActivity.this, R.drawable.icon_eraser_gray));
                setEraser(false);
                WhiteBoardPageFragment temp = (WhiteBoardPageFragment) whiteBoardViewPagerAdapter.getFragmentsList().get(whiteBoardViewPager.getCurrentItem());
                temp.redoDrawing();
            }
        });

        whiteBoardViewPager.addOnPageChangeListener(this);
        setOnVideoProcessingListener(this); // set Listener for video processing
//        blackBtn.callOnClick();
    }

    //for pause the vedio
    protected void pauseVedio() {

        pauseBtnOut.setVisibility(View.GONE);
        //resumeBtnOut.setVisibility(View.VISIBLE);
        if (isChromebook()) {
            resumeBtnOut.setVisibility(View.GONE);
        } else {
            resumeBtnOut.setVisibility(View.VISIBLE);
        }
        isResumed = false;
        isPaused = true;
        isToolBarHidden = true;
        showHideToolbar();
        tempRecordTime = recordTime;

        if(sessionManager.getSessionAppIsScreenCastActive()){
            stopScreenCastRecording();
        }else {
            pauseScreenSharing();
            handler.removeCallbacks(UpdateRecordTime);
        }
    }

    // for resume vedio
    protected void resumeVedio() {
        if (isChromebook()) {
            pauseBtnOut.setVisibility(View.GONE);
        } else {
            pauseBtnOut.setVisibility(View.VISIBLE);
        }
        resumeBtnOut.setVisibility(View.GONE);
        rightTextureListDrawer.setVisibility(View.GONE);

        isResumed = true;
        isPaused = false;
        recordTime = tempRecordTime;
        if(sessionManager.getSessionAppIsScreenCastActive()){
            startScreenCastRecording();
        }else {
            resumeScreenSharing();
        }

    }

    protected void opencameraIntent() {
        fileUri = getImageUriForImageChooserIntent();
        Intent imageIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        imageIntent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
        startActivityForResult(imageIntent, REQUEST_CAMERA);
        // imageIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[]{imageIntent});

        /*Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, REQUEST_CAMERA);*/
    }

    @Override
    public void onBackPressed() {

        if (isRecordingRunning) {
            //onToggleScreenShare(false);
            if (!isPaused) {
                pauseBtnOut.callOnClick();
            }
            final boolean[] noBtnClicked = {false};
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(RecordWhiteBoardActivity.this);
            alertDialog.setTitle("Confirmation");
            alertDialog.setMessage("Do you want to stop and save?");
            alertDialog.setIcon(R.drawable.app_logo_round_new24);
            alertDialog.setPositiveButton("yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    isRecordingRunning = false;
                    if(sessionManager.getSessionAppIsScreenCastActive()){
                        stopScreenCastRecording();
                    }else {
                        destroyMediaProjection();
                    }
                    setResult(REQUEST_CODE);
                    recordTime = 0;
                    currentTimeText.setVisibility(View.GONE);
                }
            });
            alertDialog.setNegativeButton("no", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    noBtnClicked[0] = true;
                    return;
                }
            });
            alertDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    if (noBtnClicked[0]) {
                        resumeBtnOut.callOnClick();
                    }
                }
            });
            alertDialog.show();
        } else if (isPaused) {

            AlertDialog.Builder alertDialog = new AlertDialog.Builder(RecordWhiteBoardActivity.this);
            alertDialog.setTitle("Confirmation");
            alertDialog.setMessage("Do you want to stop and save?");
            alertDialog.setIcon(R.drawable.app_logo_round_new24);
            alertDialog.setPositiveButton("yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    isRecordingRunning = false;
                    if(sessionManager.getSessionAppIsScreenCastActive()){
                        stopScreenCastRecording();
                    }else {
                        destroyMediaProjection();
                    }
                    setResult(REQUEST_CODE);
                    finish();
                    showToast("Video recorded successfully.", Toast.LENGTH_SHORT);
                    recordTime = 0;
                    currentTimeText.setVisibility(View.GONE);
                    startActivity(new Intent(RecordWhiteBoardActivity.this, MainActivity.class));
                }
            });
            alertDialog.setNegativeButton("no", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    return;
                }
            });
            alertDialog.show();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onVideoProcessCompleted() {
        setResult(REQUEST_CODE);
        //startActivity(new Intent(RecordWhiteBoardActivity.this,MainActivity.class));
        finish();
        showToast("Video recorded successfully.", Toast.LENGTH_SHORT);
        startActivity(new Intent(RecordWhiteBoardActivity.this, MainActivity.class));

    }

    @Override
    public void onVideoProcessFailed() {
        setResult(REQUEST_CODE);
        finish();
        showToast("Recording process is failed. Please try again later", Toast.LENGTH_SHORT);
    }

    private void showHideToolbar() {
        if (isToolBarHidden) {
            showHideBtn.setImageDrawable(ContextCompat.getDrawable(RecordWhiteBoardActivity.this, R.drawable.downward_grey));
            upperNavigationLayout.setVisibility(View.GONE);
            toolsLayout.setVisibility(View.VISIBLE);

            mainContainer.removeView(showHideBtn);
            RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(90, 90); // You might want to tweak these to WRAP_CONTENT
            lp.addRule(RelativeLayout.RIGHT_OF, R.id.toolsLayout);
            lp.addRule(RelativeLayout.CENTER_IN_PARENT);
            mainContainer.addView(showHideBtn, lp);

            isToolBarHidden = false;
        } else {
            showHideBtn.setImageDrawable(ContextCompat.getDrawable(RecordWhiteBoardActivity.this, R.drawable.upward_grey));
            upperNavigationLayout.setVisibility(View.VISIBLE);
            toolsLayout.setVisibility(View.GONE);////Change Shashank For Demo
            //toolsLayout.setVisibility(View.VISIBLE);

            mainContainer.removeView(showHideBtn);
            RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(90, 90); // You might want to tweak these to WRAP_CONTENT
            lp.addRule(RelativeLayout.CENTER_IN_PARENT);
            lp.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
            mainContainer.addView(showHideBtn, lp);

            isToolBarHidden = true;
        }
        // showHideBtn.setVisibility(View.VISIBLE);//change shashank for demo
        //showHideBtn.setVisibility(View.GONE);
    }

    private void openPictureGallery() {
        fileUri = getImageUriForImageChooserIntent();
        Intent pickIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        pickIntent.setType("image/*");

        Intent chooserIntent = Intent.createChooser(pickIntent, "Select Image");
        if (getGlobalMethods().isDeviceSupportCamera(RecordWhiteBoardActivity.this)) {
            Intent imageIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            imageIntent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
            chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[]{imageIntent});//003
        }

        try {
            startActivityForResult(chooserIntent, PICK_IMAGE);
            showBusyProgress();
        } catch (ActivityNotFoundException e) {
            Crashlytics.logException(e);
            showToast("No image sources available");
        }
    }

    private void addViewScreenshotInList(int index){
        try {
            WhiteBoardPageFragment temp = (WhiteBoardPageFragment) whiteBoardViewPagerAdapter.getFragmentsList().get(index);

            if (imageScreenshotList.size() > index) {
                imageScreenshotList.remove(index);
            }
            imageScreenshotList.add(index, temp.takeScreenshotView());
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void loadDefaultScreenshots(){
        WhiteBoardPageFragment temp = (WhiteBoardPageFragment) whiteBoardViewPagerAdapter.getFragmentsList().get(0);
                for(int i=0; i<whiteBoardPagesCount;i++){
                    imageScreenshotList.add(i,temp.takeScreenshotView());
                }
    }

    private void setEraser(boolean isActive) {
        for (int i = 0; i < whiteBoardViewPagerAdapter.getFragmentsList().size(); i++) {
            WhiteBoardPageFragment temp = (WhiteBoardPageFragment) whiteBoardViewPagerAdapter.getFragmentsList().get(i);
            temp.setEraserActive(isActive);
        }
    }

    private void setImageOnWhiteBoard(Uri fileUri) {
        trashImagesBtn.setVisibility(View.VISIBLE);
        doneImagesBtn.setVisibility(View.VISIBLE);
        colorPaletLayout.setVisibility(View.GONE);
        exportPDFBtn.setVisibility(View.GONE);
        WhiteBoardPageFragment temp = (WhiteBoardPageFragment) whiteBoardViewPagerAdapter.getFragmentsList().get(whiteBoardViewPager.getCurrentItem());
        hideBusyProgress();
        temp.addImageViewInContainer(fileUri);
    }

    private void setImageMovable(boolean enable) {
        for (int i = 0; i < whiteBoardViewPagerAdapter.getFragmentsList().size(); i++) {
            WhiteBoardPageFragment temp = (WhiteBoardPageFragment) whiteBoardViewPagerAdapter.getFragmentsList().get(i);
            if (temp != null)
                temp.enableImageMoving(enable);
        }
    }

    private void setPenColor(int color) {
        selectedPencilColor=color;
        for (int i = 0; i < whiteBoardViewPagerAdapter.getFragmentsList().size(); i++) {
            WhiteBoardPageFragment temp = (WhiteBoardPageFragment) whiteBoardViewPagerAdapter.getFragmentsList().get(i);
            temp.setPenColorForDrawing(color);
        }
    }

    /*private void disableShapes(boolean isEnable) {
        for (int i = 0; i < whiteBoardViewPagerAdapter.getFragmentsList().size(); i++) {
            WhiteBoardPageFragment temp = (WhiteBoardPageFragment) whiteBoardViewPagerAdapter.getFragmentsList().get(i);
            temp.setActiveShapeForDrawing(0, isEnable);
        }
    }*/

    private void clearDrawingBoard() {//009
        WhiteBoardPageFragment temp = (WhiteBoardPageFragment) whiteBoardViewPagerAdapter.getFragmentsList().get(whiteBoardViewPager.getCurrentItem());
        temp.clearDrawing();
    }

    private void clearGraphicsImages() {
        WhiteBoardPageFragment temp = (WhiteBoardPageFragment) whiteBoardViewPagerAdapter.getFragmentsList().get(whiteBoardViewPager.getCurrentItem());
        temp.clearGraphics();
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        if (position == 0 && whiteBoardPagesCount > 1) {
            homeBtn.setVisibility(View.INVISIBLE);
            prevBtn.setVisibility(View.INVISIBLE);
            nextBtn.setVisibility(View.VISIBLE);
            endBtn.setVisibility(View.VISIBLE);

            prevBtn1.setVisibility(View.INVISIBLE);
            nextBtn1.setVisibility(View.VISIBLE);

        } else if (position == (whiteBoardPagesCount - 1)) {
            homeBtn.setVisibility(View.VISIBLE);
            prevBtn.setVisibility(View.VISIBLE);
            nextBtn.setVisibility(View.INVISIBLE);
            endBtn.setVisibility(View.INVISIBLE);

            prevBtn1.setVisibility(View.VISIBLE);
            nextBtn1.setVisibility(View.INVISIBLE);
        } else {
            homeBtn.setVisibility(View.VISIBLE);
            prevBtn.setVisibility(View.VISIBLE);
            nextBtn.setVisibility(View.VISIBLE);
            endBtn.setVisibility(View.VISIBLE);

            prevBtn1.setVisibility(View.VISIBLE);
            nextBtn1.setVisibility(View.VISIBLE);
        }
        pageIndex.setText("" + (whiteBoardViewPager.getCurrentItem() + 1));
        pageIndex1.setText("" + (whiteBoardViewPager.getCurrentItem() + 1));

    }

    @Override
    public void onPageSelected(int position) {

    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    private void startRecording() {
        if (ContextCompat.checkSelfPermission(RecordWhiteBoardActivity.this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) + ContextCompat
                .checkSelfPermission(RecordWhiteBoardActivity.this,
                        Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale
                    (RecordWhiteBoardActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) ||
                    ActivityCompat.shouldShowRequestPermissionRationale
                            (RecordWhiteBoardActivity.this, Manifest.permission.RECORD_AUDIO)) {
//                mToggleButton.setChecked(false);
                stopBtn.setVisibility(View.GONE);
                resumeBtnOut.setVisibility(View.GONE);
                pauseBtnOut.setVisibility(View.GONE);
                stopBtnOut.setVisibility(View.GONE);
                startBtn.setVisibility(View.VISIBLE);
                Snackbar.make(findViewById(android.R.id.content), R.string.label_permissions,
                        Snackbar.LENGTH_INDEFINITE).setAction("ENABLE",
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                ActivityCompat.requestPermissions(RecordWhiteBoardActivity.this,
                                        new String[]{Manifest.permission
                                                .WRITE_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO},
                                        REQUEST_PERMISSIONS);
                            }
                        }).show();
            } else {
                ActivityCompat.requestPermissions(RecordWhiteBoardActivity.this,
                        new String[]{Manifest.permission
                                .WRITE_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO},
                        REQUEST_PERMISSIONS);
            }
        } else {
            onToggleScreenShare(true);
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {

        switch (requestCode) {
            case REQUEST_CODE:
                if (resultCode != RESULT_OK) {
                    showToast("Screen Cast Permission Denied");
//            mToggleButton.setChecked(false);
                    stopBtn.setVisibility(View.GONE);
                    stopBtnOut.setVisibility(View.GONE);
                    resumeBtnOut.setVisibility(View.GONE);
                    pauseBtnOut.setVisibility(View.GONE);
                    startBtn.setVisibility(View.VISIBLE);//03
                    finish();
                    return;
                }
                try {
                    if (isChromebook()) {
                        pauseBtnOut.setVisibility(View.GONE);
                    } else {
                        pauseBtnOut.setVisibility(View.VISIBLE);
                    }
                    mMediaProjectionCallback = new MediaProjectionCallback();
                    mMediaProjection = mProjectionManager.getMediaProjection(resultCode, intent);
                    mMediaProjection.registerCallback(mMediaProjectionCallback, null);
                    mVirtualDisplay = createVirtualDisplay();
                    mMediaRecorder.start();
                    handler.post(UpdateRecordTime);
                } catch (RuntimeException stopRuntimeException) {
                    Crashlytics.logException(stopRuntimeException);
                    mMediaRecorder.reset();
                    stopScreenSharing();
                    setResult(REQUEST_CODE);
                    showToast("Something went wrong. Please try again later..!");
                    finish();
                }
                break;

            case PICK_IMAGE:
                if (resultCode != RESULT_OK) {
                    hideBusyProgress();
                    showToast("Operation failed. Please try again later..!");
                    return;
                }
                if (intent != null) {
                    if (intent.getData() != null) {
                        setImageOnWhiteBoard(intent.getData());
                    } else {
                        hideBusyProgress();
                        showToast("Something went wrong. Please try again later..!");
                    }
                } else {
                    setImageOnWhiteBoard(fileUri);
                }
                break;
            case PICKIMAGES_RESULT_CODE:
                if (resultCode != RESULT_OK) {
                    hideBusyProgress();
                    showToast("Operation failed. Please try again later..!");
                    return;
                }
                if (intent != null) {
                    if (intent.getData() != null) {
                        setImageOnWhiteBoard(intent.getData());
                    } else {
                        hideBusyProgress();
                        showToast("Something went wrong. Please try again later..!");
                    }
                } else {
                    setImageOnWhiteBoard(fileUri);
                }
                break;
            //case Request Camera:
            case REQUEST_CAMERA:
                if (resultCode != RESULT_OK) {
                    hideBusyProgress();
                    showToast("Operation failed. Please try again later..!");
                    return;
                }
                if (intent != null) {
                    if (intent.getExtras() != null) {
                        //setImageOnWhiteBoard(intent.getData());
                        Bundle extras = intent.getExtras();
                        Bitmap imageBitmap = (Bitmap) extras.get("data");
                        setImageOnWhiteBoard(getImageUri(RecordWhiteBoardActivity.this, imageBitmap));
                    } else {
                        hideBusyProgress();
                        showToast("Something went wrong. Please try again later..!");
                    }
                } else {
                    setImageOnWhiteBoard(fileUri);
                }
                /*if (intent != null) {
                    if (intent.getExtras() != null) {
                        setImageOnWhiteBoard(intent.getData());
                        Bundle extras = intent.getExtras();
                        Bitmap imageBitmap = (Bitmap) extras.get("data");

                        try {
                            fileUri = FileProvider.getUriForFile(RecordWhiteBoardActivity.this, BuildConfig.APPLICATION_ID + ".provider", globalMethods.createImageFile(RecordWhiteBoardActivity.this));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        //setImageOnWhiteBoard(fileUri);
                        setImageOnWhiteBoard(fileUri);
                    } else {
                        hideBusyProgress();
                        showToast("Something went wrong. Please try again later..!");
                    }
                } else {
                    setImageOnWhiteBoard(fileUri);
                }*/
                break;
        }
    }

    @SuppressLint("LongLogTag")
    public void onToggleScreenShare(boolean isShare) {
        isRecordingRunning = isShare;
        try {
            if (isShare) {
                initRecorder();
                shareScreen();
            } else {
                mMediaRecorder.stop();
                mMediaRecorder.reset();
                buildNotification("Video Processing Done...", fileData);
                Log.v(TAG, "Stopping Recording");
                stopScreenSharing();
//                setResult(REQUEST_CODE);
//                finish();
//                showToast("Whiteboard recording is done successfully.", Toast.LENGTH_SHORT);
            }
        } catch (RuntimeException stopRuntimeException) {
            Crashlytics.logException(stopRuntimeException);
            mMediaRecorder.reset();
            stopScreenSharing();
            setResult(REQUEST_CODE);
            finish();
            showToast("Whiteboard recording is not done successfully.", Toast.LENGTH_SHORT);
            buildNotification("Video Processing Failed...", fileData);
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void shareScreen() {
        try {
            if (mMediaProjection == null) {
                startActivityForResult(mProjectionManager.createScreenCaptureIntent(), REQUEST_CODE);
                return;
            }
            mVirtualDisplay = createVirtualDisplay();
            mMediaRecorder.start();
            handler.post(UpdateRecordTime);
        } catch (Exception e) {
            Crashlytics.logException(e);
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private VirtualDisplay createVirtualDisplay() {
        return mMediaProjection.createVirtualDisplay("MainActivity",
                DISPLAY_WIDTH, DISPLAY_HEIGHT, mScreenDensity, DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                mMediaRecorder.getSurface(), null /*Callbacks*/, null
                /*Handler*/);
    }

    private void initRecorder() {
        try {
            File f = new File(Environment.getExternalStorageDirectory() + "/CBtemp");
            if (!f.isDirectory()) {
                f.mkdirs();
            }

            //long mills = System.currentTimeMillis();
            //String outputFileName = Environment.getExternalStorageDirectory() + "/" + getResources().getString(R.string.app_name).toString() + "/VD_" + mills + ".mp4";
            Date date = new Date();

            String outputFileName = Environment.getExternalStorageDirectory() + "/CBtemp/CB_" + date.getTime() + ".mp4";//09
            sourceFilePath.add(outputFileName);

            mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);
            mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            mMediaRecorder.setOutputFile(outputFileName);
//            mMediaRecorder.setVideoSize(DISPLAY_WIDTH, 888);
            mMediaRecorder.setVideoSize(DISPLAY_WIDTH, DISPLAY_HEIGHT);
//            mMediaRecorder.setVideoSize(1280, 720);
            mMediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
            mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            mMediaRecorder.setVideoEncodingBitRate(15000000);
            mMediaRecorder.setVideoFrameRate(85);

            int rotation = getWindowManager().getDefaultDisplay().getRotation();
            int orientation = ORIENTATIONS.get(rotation + 90);
            mMediaRecorder.setOrientationHint(orientation);
            mMediaRecorder.prepare();

            File file = new File(outputFileName);
            fileData = new FileData(file);

        } catch (IOException e) {
            Crashlytics.logException(e);
            e.printStackTrace();
            showToast("Some Devices are busy try again");
            finish();
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private class MediaProjectionCallback extends MediaProjection.Callback {
        @Override
        public void onStop() {
//            if (mToggleButton.isChecked()) {
//                mToggleButton.setChecked(false);
//                mMediaRecorder.stop();
//                mMediaRecorder.reset();
//                Log.v(TAG, "Recording Stopped");
//            }
            mMediaRecorder.stop();
            mMediaRecorder.reset();
            mMediaProjection = null;
            stopScreenSharing();
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void stopScreenSharing() {
        if (mVirtualDisplay == null) {
            return;
        }
        mVirtualDisplay.release();
        //mMediaRecorder.release(); //If used: mMediaRecorder object cannot
        // be reused again
        destroyMediaProjection();
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    private void pauseScreenSharing() {
        mMediaRecorder.stop();
        mMediaRecorder.reset();
        if (mVirtualDisplay == null) {
            return;
        }
        mVirtualDisplay.release();
    }

    private void resumeScreenSharing() {
        isToolBarHidden = false;
        showHideToolbar();
        initRecorder();
        shareScreen();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            destroyMediaProjection();
            getGlobalMethods().clearContentFromDirectory(VolleySingleton.getAppStorageTempDirectory());
        } catch (Exception e) {
            Log.e(RecordWhiteBoardActivity.class.getName(), e.getMessage().toString());
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @SuppressLint("LongLogTag")
    private void destroyMediaProjection() {
        if (mMediaProjection != null) {
            mMediaProjection.unregisterCallback(mMediaProjectionCallback);
            mMediaProjection.stop();
            mMediaProjection = null;
        }

        if (sourceFilePath != null && sourceFilePath.size() == 1) {
            File source = new File(Environment.getExternalStorageDirectory() + "/CBtemp/" + fileData.getFileName());
            File destination = new File(Environment.getExternalStorageDirectory() + "/" + getResources().getString(R.string.app_name) + "/" + fileData.getFileName());

            try {
                copy(source, destination);
                File sourceDir = new File(Environment.getExternalStorageDirectory(), "/CBtemp/");
                if (sourceDir != null) {
                    File[] filenames = sourceDir.listFiles();
                    for (File tmpf : filenames) {
                        tmpf.delete();
                    }
                }
            } catch (IOException e) {
                Log.e(TAG, "" + e.getMessage().toString());
                Crashlytics.logException(e);
                e.printStackTrace();
            }
        }

        if (sourceFilePath != null && sourceFilePath.size() > 1) {
            String sfilePath[] = new String[sourceFilePath.size()];
            for (int i = 0; i < sourceFilePath.size(); i++) {
                sfilePath[i] = sourceFilePath.get(i);
            }
            sourceFilePath.clear();
            new MergeVideosTask().execute(sfilePath);
        }
        Log.v(TAG, "MediaProjection Stopped");
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_PERMISSIONS: {
                if ((grantResults.length > 0) && (grantResults[0] + grantResults[1]) == PackageManager.PERMISSION_GRANTED) {
                    onToggleScreenShare(true);
                } else {
//                    mToggleButton.setChecked(false);
                    Snackbar.make(findViewById(android.R.id.content), R.string.label_permissions,
                            Snackbar.LENGTH_INDEFINITE).setAction("ENABLE",
                            new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Intent intent = new Intent();
                                    intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                    intent.addCategory(Intent.CATEGORY_DEFAULT);
                                    intent.setData(Uri.parse("package:" + getPackageName()));
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                                    startActivity(intent);
                                }
                            }).show();
                }
                return;
            }
            case REQUEST_PERMISSIONS_PDF_EXPORT: {
                if ((grantResults.length > 0) && (grantResults[0]) == PackageManager.PERMISSION_GRANTED) {
                    addViewScreenshotInList(whiteBoardViewPager.getCurrentItem());
                    exportPDFBtn.setEnabled(false);
                    exportAsPdfFile();
                } else {
                    Snackbar.make(findViewById(android.R.id.content), R.string.label_permissions_storage,
                            Snackbar.LENGTH_INDEFINITE).setAction("ENABLE",
                            new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Intent intent = new Intent();
                                    intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                    intent.addCategory(Intent.CATEGORY_DEFAULT);
                                    intent.setData(Uri.parse("package:" + getPackageName()));
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                                    startActivity(intent);
                                }
                            }).show();
                }
                return;
            }
            case GlobalMethods.camera_audio_external_storage_permission_request_code:
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
                        openPictureGallery();
                    }
                } else {
                    String msg = "Sorry now you can not perform this operation by denying permission. Please try again later..!";
                    showToast(msg);
                }
                break;
        }
    }

    public class MyCountDownTimer extends CountDownTimer {

        public MyCountDownTimer(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        @Override
        public void onTick(long millisUntilFinished) {
            int progress = (int) (millisUntilFinished / 1000);
            //countDownTimerText.setText("" + (progress - 1));//eeeeee
            countDownTimerText.setText("" + countDownTime);
            countDownTime--;
        }

        @Override
        public void onFinish() {
            countDownTimerText.setText("0");
            countDownTimerText.setVisibility(View.GONE);
//            drawingView.setEnabled(true);
            stopBtn.setVisibility(View.GONE);//Change
            stopBtnOut.setVisibility(View.VISIBLE);
            ///demo change shashank
            /*if (isChromebook()) {
                pauseBtnOut.setVisibility(View.GONE);
            } else {
                pauseBtnOut.setVisibility(View.VISIBLE);
            }*/
            if(sessionManager.getSessionAppIsScreenCastActive()){
                recordTime = 0;
                startScreenCastRecording();
                return;
            }
            startRecording();
        }

    }

    private List<Fragment> getFragments(int count) {

        List<Fragment> fList = new ArrayList<Fragment>();
        for (int i = 0; i < count; i++) {
            fList.add(WhiteBoardPageFragment.getInstance());
        }
        return fList;
    }

    public Uri getImageUriForImageChooserIntent() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
        String fileName = "IMG_" + sdf.format(new Date()) + ".jpg";
        File file = new File(VolleySingleton.getAppStorageTempDirectory(), fileName);
        return FileProvider.getUriForFile(RecordWhiteBoardActivity.this, BuildConfig.APPLICATION_ID + ".provider", file);
        //return Uri.fromFile(file);
    }

    private void showTextureList() {
        eraseBtn.setBackground(ContextCompat.getDrawable(RecordWhiteBoardActivity.this, R.drawable.icon_eraser_gray));
        setEraser(false);

        TextureListRecyclerViewAdapter textureListRecyclerViewAdapter = new TextureListRecyclerViewAdapter(RecordWhiteBoardActivity.this, new TextureListRecyclerViewAdapter.OnButtonActionListener() {
            @Override
            public void onTextureSelected(int drawableResource) {
                WhiteBoardPageFragment temp = (WhiteBoardPageFragment) whiteBoardViewPagerAdapter.getFragmentsList().get(whiteBoardViewPager.getCurrentItem());
                temp.setBackTextureImgView(getResources().getDrawable(drawableResource));

                rightTextureListDrawer.setVisibility(View.GONE);
                if (isRecordingRunning && isPaused) {
                    resumeBtnOut.callOnClick();
                }
            }
        });
        backTextureListView.setLayoutManager(new LinearLayoutManager(RecordWhiteBoardActivity.this));
        backTextureListView.setAdapter(textureListRecyclerViewAdapter);

        if (isTextureOpen) {
            rightTextureListDrawer.setVisibility(View.GONE);
            isTextureOpen = false;
        } else if (!isTextureOpen) {
            rightTextureListDrawer.setVisibility(View.VISIBLE);
            isTextureOpen = true;
        }


    }

    Runnable UpdateRecordTime = new Runnable() {
        public void run() {
            if (isRecordingRunning) {
                int seconds = 0;
                int minutes = 0;

                if (recordTime >= 60) {
                    seconds = recordTime % 60;
                    minutes = recordTime / 60;
                } else {
                    seconds = recordTime;
                }
                if (minutes > 30) {
//                    stopBtn.callOnClick();
//                    return;
                }

                String str = String.format("%02d:%02d", minutes, seconds);
//                currentTimeText.setText(String.valueOf(recordTime));
                currentTimeText.setVisibility(View.VISIBLE);
                currentTimeText.setText(str);
                recordTime += 1;
                // Delay 1s before next call
                handler.postDelayed(this, 1000);
                /*if (recordTime == slideMaxTimeDuration) {
                    stopBtn.callOnClick();
                } else {
                    recordTime += 1;
                    // Delay 1s before next call
                    handler.postDelayed(this, 1000);
                }*/
            }
        }
    };

    private void getUserProducts(final FileData fData, String userId, String accessCode) {
        final Map<String, Object> requestMap = new HashMap<String, Object>();
        requestMap.put("AccessToken", accessCode);
        requestMap.put("UserId", userId);
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
                                userproductaccessresponse = new Userproductaccessresponse();
                                userproductaccessresponse.setProductAccessList((List<ProductAccessList>) new Gson().fromJson(response.getJSONArray("ProductAccessList").toString(), new TypeToken<List<ProductAccessList>>() {
                                }.getType()));
                                Log.e(RecordPreviewActivity.class.getName(), "" + userproductaccessresponse.getProductAccessList().get(0).getTitle());
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


    //#18DEC19 new code

    public void exportAsPdfFile(){
        try{
            if(imageScreenshotList != null && imageScreenshotList.size()>0){
                convertBitMapToPdfFile();
            }else{
                exportPDFBtn.setEnabled(true);
            }
        }catch (Exception e){
            e.printStackTrace();
            exportPDFBtn.setEnabled(true);
        }

    }

    private void convertBitMapToPdfFile(){
        PdfDocument pdfDocument = new PdfDocument();

        for(int i =0; i<imageScreenshotList.size(); i++){
            Bitmap bitmap = imageScreenshotList.get(i);
            PdfDocument.PageInfo pi = new PdfDocument.PageInfo.Builder(bitmap.getWidth(),bitmap.getHeight(),(i+1)).create();

            PdfDocument.Page page = pdfDocument.startPage(pi);
            Canvas canvas = page.getCanvas();
            Paint paint = new Paint();
            paint.setColor(Color.parseColor("#FFFFFF"));
            canvas.drawPaint(paint);

            bitmap = Bitmap.createScaledBitmap(bitmap, bitmap.getWidth(),bitmap.getHeight(),true);

            paint.setColor(Color.BLUE);

            canvas.drawBitmap(bitmap, 0, 0, null);

            pdfDocument.finishPage(page);


        }

        //Save to sd card

        File root = new File(Environment.getExternalStorageDirectory(), getResources().getString(R.string.app_name)+"/exports");

        if(!root.exists()){
            root.mkdir();
        }

        File pdfFile  = new File(root,"Whiteboard_"+new Date().getTime()+".pdf");
        try{
            FileOutputStream fileOutputStream = new FileOutputStream(pdfFile);
            pdfDocument.writeTo(fileOutputStream);
        }catch (IOException io){
            io.printStackTrace();
        }

        pdfDocument.close();

        showToast("PDF exported successfully. Check in 'My Exports' ");
        exportPDFBtn.setEnabled(true);
    }

    //23 Jan 2020 - rajk

    private int frameCounter;
    private String screenCastRecordOutputFileName = "";

    private void MediaRecorderReadyForAudioRecording(){
        mMediaRecorder=new MediaRecorder();
        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mMediaRecorder.setAudioEncoder(MediaRecorder.OutputFormat.AMR_NB);
        mMediaRecorder.setOutputFile(AudioSavePathInDevice);
    }
    @SuppressLint("LongLogTag")
    private void startScreenCastRecording(){
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        DEFAULT_HEIGHT = displayMetrics.heightPixels;
        DEFAULT_WIDTH = displayMetrics.widthPixels;

        screenCastRecordOutputFileName = "CB_" + new Date().getTime();
        mEncoderConfig = new HevcEncoderConfig(new File(Environment.getExternalStorageDirectory(),getResources().getString(R.string.app_name)+"/"+screenCastRecordOutputFileName+ "_Video.mp4").getAbsolutePath(),DISPLAY_WIDTH, DISPLAY_HEIGHT, DEFAULT_FRAME_RATE_PER_SECOND, DEFAULT_BIT_RATE);
        AudioSavePathInDevice = Environment.getExternalStorageDirectory()+"/"+getResources().getString(R.string.app_name)+ "/" +screenCastRecordOutputFileName+"_Audio.3gp";
        MediaRecorderReadyForAudioRecording();
        frameEncoder = new FrameEncoder(mEncoderConfig);
        isRecordingRunning = true;
        pauseBtnOut.setVisibility(View.VISIBLE);
        try {
            mMediaRecorder.prepare();
            mMediaRecorder.start();
            frameEncoder.start();
            handler.post(UpdateRecordTimeForScreenCastRecording);
        }  catch (IllegalStateException e) {
            // TODO Auto-generated catch block
            isRecordingRunning = false;
            pauseBtnOut.setVisibility(View.GONE);
            e.printStackTrace();
        } catch (IOException e) {
            Log.e(TAG, "Start Encoder Failed", e);
            pauseBtnOut.setVisibility(View.GONE);
            isRecordingRunning = false;
            return;
        }
    }

    private void stopScreenCastRecording() {
        try {
            mMediaRecorder.stop();
            mMediaRecorder.reset();
            handler.removeCallbacks(UpdateRecordTimeForScreenCastRecording);
            VideoSavePathInDevice = mEncoderConfig.getPath();
            frameEncoder.release();
            String outputFileName = doAudioVideoMuxing(VideoSavePathInDevice, AudioSavePathInDevice, true);
            sourceFilePath.add(outputFileName);
            File file = new File(outputFileName);
            fileData = new FileData(file);

            if (!isRecordingRunning) {
                closeScreenCastRecording();
            }
        }catch (Exception e){
            mMediaRecorder.reset();
            e.printStackTrace();
        }
    }

    private void closeScreenCastRecording(){
        //Merge all video
        if (sourceFilePath != null && sourceFilePath.size() == 1) {
            File source = new File(Environment.getExternalStorageDirectory() + "/CBtemp/" + fileData.getFileName());
            File destination = new File(Environment.getExternalStorageDirectory() + "/" + getResources().getString(R.string.app_name) + "/" + fileData.getFileName());

            try {
                copy(source, destination);
                File sourceDir = new File(Environment.getExternalStorageDirectory(), "/CBtemp/");
                if (sourceDir != null) {
                    File[] filenames = sourceDir.listFiles();
                    for (File tmpf : filenames) {
                        tmpf.delete();
                    }
                }
            } catch (IOException e) {
                Log.e(TAG, "" + e.getMessage().toString());
                Crashlytics.logException(e);
                e.printStackTrace();
            }
        }

        if (sourceFilePath != null && sourceFilePath.size() > 1) {
            String sfilePath[] = new String[sourceFilePath.size()];
            for (int i = 0; i < sourceFilePath.size(); i++) {
                sfilePath[i] = sourceFilePath.get(i);
            }
            sourceFilePath.clear();
            new MergeVideosTask().execute(sfilePath);
        }
    }

    Runnable UpdateRecordTimeForScreenCastRecording = new Runnable() {
        public void run() {
            if (isRecordingRunning) {
                int seconds = 0;
                int minutes = 0;

                if (recordTime >= 60) {
                    seconds = recordTime % 60;
                    minutes = recordTime / 60;
                } else {
                    seconds = recordTime;
                }

                if (minutes > 30) {
                    stopBtn.callOnClick();
                    return;
                }

                String str = String.format("%02d:%02d", minutes, seconds);
                currentTimeText.setVisibility(View.GONE);

                frameEncoder.createFrame(Screenshots.takeScreenshotOfRootView(whiteBoardViewPager));

//                WhiteBoardPageFragment temp = (WhiteBoardPageFragment) whiteBoardViewPagerAdapter.getFragmentsList().get(whiteBoardViewPager.getCurrentItem());
//                frameEncoder.createFrame(temp.takeScreenshotOfRootView());
                currentTimeText.setVisibility(View.VISIBLE);
                currentTimeText.setText(str);
                frameCounter +=1;
                recordTime = (frameCounter / (int)DEFAULT_FRAME_RATE_PER_SECOND);
                // Delay 1s before next call
                handler.postDelayed(UpdateRecordTimeForScreenCastRecording, 667);
            }
        }
    };
}