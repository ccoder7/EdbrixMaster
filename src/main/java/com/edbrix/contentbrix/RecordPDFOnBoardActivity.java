package com.edbrix.contentbrix;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.graphics.pdf.PdfRenderer;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.MediaRecorder;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.Handler;
import android.os.ParcelFileDescriptor;
import android.provider.Settings;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import com.edbrix.contentbrix.adapters.PageTileListAdapter;
import com.edbrix.contentbrix.encoder.EncoderConfig;
import com.edbrix.contentbrix.encoder.FrameEncoder;
import com.edbrix.contentbrix.encoder.HevcEncoderConfig;
import com.edbrix.contentbrix.utils.Screenshots;
import com.google.android.material.snackbar.Snackbar;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.SparseIntArray;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.edbrix.contentbrix.adapters.PDFViewPagerAdapter;
import com.edbrix.contentbrix.baseclasses.BaseActivity;
import com.edbrix.contentbrix.commons.GlobalMethods;
import com.edbrix.contentbrix.customview.CustomViewPager;
import com.edbrix.contentbrix.customview.DrawingView;
import com.edbrix.contentbrix.customview.InformatoryDialog;
import com.edbrix.contentbrix.data.FileData;
import com.edbrix.contentbrix.fragments.PDFPageFragment;
import com.edbrix.contentbrix.utils.SessionManager;
import com.edbrix.contentbrix.volley.VolleySingleton;
import com.vlk.multimager.utils.Constants;
import com.vlk.multimager.utils.Image;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

import io.github.douglasjunior.androidSimpleTooltip.SimpleTooltip;

import static com.edbrix.contentbrix.SlidePDFReOrderActivity.pdfFileKey;

public class RecordPDFOnBoardActivity extends BaseActivity implements ViewPager.OnPageChangeListener, BaseActivity.OnVideoProcessingListener {
    private static final int PICKFILE_RESULT_CODE = 1;
    private static final int REQUEST_CODE = 1000;
    public static final int RESULT_CODE = 1234;
    public final static int IMAGE_REORDER_RESULT = 1021;
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
    private PDFViewPagerAdapter whiteBoardViewPagerAdapter;
    private int countDownTime;
    private int countDownInterval;
    //    private DrawingView drawingView;
    private DrawingView drawingViewObj;

    private ImageView stopBtn;
    private ImageView startBtn;

    private Button colorsPaletBtn;
    private Button pencilStrokesBtn;
    //009
    private ImageView prevBtn;
    private ImageView nextBtn;

    private ImageView prevBtn1;
    private ImageView nextBtn1;

    private ImageView resumeBtnOut;
    private ImageView pauseBtnOut;
    private ImageView stopBtnOut;

    private Button eraseBtn;
    private Button pinchBtn;
    private ImageView imgScaleBtn;
    private ImageView tileBtn;
    private Button removeBtn;
    private Button exportPDFBtn;
    private ImageView homeBtn;
    private ImageView endBtn;
    private Button undoBtn;
    private Button redoBtn;
    private Button fullscreenBtn;
    private ImageView showHideBtn;
    private LinearLayout colorPaletLayout;
    private LinearLayout upperNavigationLayout;
    private RelativeLayout toolsLayout;
    private RelativeLayout mainContainer;
    private RelativeLayout tilesLayout;
    private RecyclerView pageTileListRecyclerView;

    private boolean isFullScreen = false;


    private TextView pageIndex;

    private TextView pageIndex1;

    private boolean isToolBarHidden = false;

    private boolean isTileViewVisible = false;

    private boolean isRecordingRunning = false;

    private boolean isResumed = false;

    private boolean isPaused = false;

    private int whiteBoardPagesCount = 0;

    private ArrayList<String> sourceFilePath;

    private File pdfFileItem;

    private SessionManager sessionManager;

    private GlobalMethods globalMethods;

    private FileData fileData;

    private int pagerIndex;

    private int counterInd;

    //23Jan2020 - Raj K


    private EncoderConfig mEncoderConfig;
    private FrameEncoder frameEncoder;
    private  int DEFAULT_WIDTH = 320;
    private  int DEFAULT_HEIGHT = 240;
    private static final float DEFAULT_FRAME_RATE_PER_SECOND = 3;
    private static final int DEFAULT_BIT_RATE = 1000000;

    private String AudioSavePathInDevice = null;
    private String VideoSavePathInDevice = null;

    static {
        ORIENTATIONS.append(Surface.ROTATION_0, 90);
        ORIENTATIONS.append(Surface.ROTATION_90, 0);
        ORIENTATIONS.append(Surface.ROTATION_180, 270);
        ORIENTATIONS.append(Surface.ROTATION_270, 180);
    }

    private int recordTime;
    private int tempRecordTime;
    private Handler handler;
    private TextView currentTimeText;
    private ArrayList<Image> reOrderImageList;
    //private List<Image> reOrderImageList;
    private PdfRenderer pdfRenderer;
    private ArrayList<Image> imageAdList;
    private ArrayList<Bitmap> imageScreenshotList;
    public String TAG = RecordPDFOnBoardActivity.class.getName();
    public Random generator;

    private boolean isPinchBtnClicked = false;

    @SuppressLint("WrongConstant")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_pdf_on_board);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);


        counterInd = 0;
        /*View bView = getWindow().getDecorView();
        bView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);//Hiding hardware button of Android Devices*/

//        drawingView = (DrawingView) findViewById(R.id.drawing);
        sessionManager = new SessionManager(RecordPDFOnBoardActivity.this);
        globalMethods = new GlobalMethods();
        sourceFilePath = new ArrayList<>();
        // screen recording
        handler = new Handler();

//        pdfFileItem = new File(FileUtils.getPath(this, getIntent().getData()));

        pdfFileItem = (File) getIntent().getSerializableExtra("pdf");
        initializePDFRenderer(pdfFileItem);

        prevBtn = (ImageView) findViewById(R.id.prevBtn);
        nextBtn = (ImageView) findViewById(R.id.nextBtn);

        prevBtn1 = (ImageView) findViewById(R.id.prevBtn1);
        nextBtn1 = (ImageView) findViewById(R.id.nextBtn1);

        fullscreenBtn = (Button) findViewById(R.id.fullscreenBtn);


        eraseBtn = (Button) findViewById(R.id.eraserBtn);
        pinchBtn = (Button) findViewById(R.id.pinchBtn);
        imgScaleBtn = (ImageView) findViewById(R.id.imgScaleBtn);
        tileBtn = (ImageView) findViewById(R.id.tileBtn);
        removeBtn = (Button) findViewById(R.id.removeBtn);
        homeBtn = (ImageView) findViewById(R.id.homeBtn);
        endBtn = (ImageView) findViewById(R.id.endBtn);
        undoBtn = (Button) findViewById(R.id.undoBtn);
        redoBtn = (Button) findViewById(R.id.redoBtn);
        exportPDFBtn = (Button) findViewById(R.id.exportPDFBtn);
        showHideBtn = (ImageView) findViewById(R.id.showHideBtn);
        pageIndex = (TextView) findViewById(R.id.pageIndex);
        pageIndex1 = (TextView) findViewById(R.id.pageIndex1);

        toolsLayout = (RelativeLayout) findViewById(R.id.toolsLayout);
        tilesLayout = (RelativeLayout) findViewById(R.id.tilesLayout);
        tilesLayout.setVisibility(View.GONE);
        mainContainer = (RelativeLayout) findViewById(R.id.mainContainer1);

        upperNavigationLayout = (LinearLayout) findViewById(R.id.upperNavigationLayout);
        colorPaletLayout = (LinearLayout) findViewById(R.id.colorPaletLayout);
        currentTimeText = (TextView) findViewById(R.id.currentTimeText);

        colorPaletLayout.setVisibility(View.VISIBLE);
        undoBtn.setVisibility(View.VISIBLE);
        redoBtn.setVisibility(View.VISIBLE);
        upperNavigationLayout.setVisibility(View.GONE);

        countDownTimerText = (TextView) findViewById(R.id.countDownTimerText);
        whiteBoardViewPager = (CustomViewPager) findViewById(R.id.whiteBoardViewPager);
        pageTileListRecyclerView = (RecyclerView) findViewById(R.id.pageTileListRecyclerView);


        imageAdList = new ArrayList<Image>();
        imageScreenshotList = new ArrayList<Bitmap>();
        ArrayList<Image> tmpimgAdList = new ArrayList<Image>();
        tmpimgAdList = (ArrayList<Image>) imageAdList.clone();

        generator = new Random();
        new PdfPagesSufflingOperation().execute("");

        whiteBoardViewPager.setPagingEnabled(false);
        whiteBoardViewPagerAdapter = new PDFViewPagerAdapter(getSupportFragmentManager(), pdfFileItem, RecordPDFOnBoardActivity.this, tmpimgAdList);
        whiteBoardViewPager.setAdapter(whiteBoardViewPagerAdapter);

        // new change
//        if(whiteBoardPagesCount>10){
//            whiteBoardViewPager.setOffscreenPageLimit(whiteBoardPagesCount+20);
//        }else {
//            whiteBoardViewPager.setOffscreenPageLimit(20+10);
//        }
//        showToast("You can adjust and rescale images before start recording.");

        if (!sessionManager.getInfoDialogSeenStatus()) {
            InformatoryDialog informatoryDialog = new InformatoryDialog(RecordPDFOnBoardActivity.this, R.layout.image_scale_info_dialog, null, null);
//            informatoryDialog.showMe(); commented for demo
            sessionManager.updateInfoDialogSeenStatus(true);
        }
        colorsPaletBtn = (Button) findViewById(R.id.colorsPaletBtn);
        pencilStrokesBtn = (Button) findViewById(R.id.pencilStrokesBtn);

        startBtn = (ImageView) findViewById(R.id.startBtn);
        stopBtn = (ImageView) findViewById(R.id.stopBtn);

        stopBtnOut = (ImageView) findViewById(R.id.stopBtnOut);
        resumeBtnOut = (ImageView) findViewById(R.id.resumeBtnOut);
        pauseBtnOut = (ImageView) findViewById(R.id.pauseBtnOut);

        startBtn.setVisibility(View.VISIBLE);

        pauseBtnOut.setVisibility(View.GONE);
        resumeBtnOut.setVisibility(View.GONE);
        stopBtnOut.setVisibility(View.GONE);
        stopBtn.setVisibility(View.GONE);

//        drawingView.setEnabled(false);

        // count down time
        countDownTime = 5; // in sec
        countDownInterval = 1000; // in sec

        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);

        mScreenDensity = metrics.densityDpi;

//        DISPLAY_HEIGHT = metrics.heightPixels / mScreenDensity;
//        DISPLAY_WIDTH = metrics.widthPixels / mScreenDensity;

        mMediaRecorder = new MediaRecorder();

        mProjectionManager = (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);

//        countDownTimerText.setVisibility(View.VISIBLE);
        countDownTimerText.setText("" + countDownTime);
        myCountDownTimer = new MyCountDownTimer(countDownTime * 1000, countDownInterval);
//        myCountDownTimer.start();


        showHideBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showHideToolBar();
            }
        });


        pinchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setEraser(false);
                eraseBtn.setBackground(ContextCompat.getDrawable(RecordPDFOnBoardActivity.this, R.drawable.icon_eraser_gray));

                if (!isPinchBtnClicked) {

                    setImageMovable(false);
                    isPinchBtnClicked = true;
                    pinchBtn.setBackgroundResource(R.drawable.pinch);
                } else {
                    mergeZoomImage();
                    setImageMovable(true);
                    isPinchBtnClicked = false;
                    pinchBtn.setBackgroundResource(R.drawable.pinchactive);
                }

            }
        });

        startBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                TypedValue outValue = new TypedValue();
                RecordPDFOnBoardActivity.this.getTheme().resolveAttribute(android.R.attr.selectableItemBackground, outValue, true);
                startBtn.setBackgroundResource(outValue.resourceId);

                view.setVisibility(View.GONE);

                showHideBtn.setVisibility(View.VISIBLE);//change shashank for demo
                //showHideBtn.setVisibility(View.GONE);
             /*   showHideBtn.setImageDrawable(ContextCompat.getDrawable(RecordPDFOnBoardActivity.this, R.drawable.upward_grey));
                upperNavigationLayout.setVisibility(View.GONE); //View.VISIBLE
                toolsLayout.setVisibility(View.GONE);
                isToolBarHidden = true;*/

                colorPaletLayout.setVisibility(View.VISIBLE);
//                undoBtn.setVisibility(View.VISIBLE);
//                redoBtn.setVisibility(View.VISIBLE);
                //blackBtn.callOnClick();
                //setPenColor(Color.BLACK);
                showHideToolBar();

                setImageMovable(false);
                isPinchBtnClicked = true;
                pinchBtn.setBackgroundResource(R.drawable.pinch);
                whiteBoardViewPager.setCurrentItem(0);
                countDownTimerText.setVisibility(View.VISIBLE);
                myCountDownTimer.start();
                getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide navigation bar
                        | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
                if (sessionManager.getSessionVibratePhoneState()) {
                    globalMethods.vibratePhone(RecordPDFOnBoardActivity.this, 500);
                }


            }
        });
        stopBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (isResumed) {
                    onToggleScreenShare(false);
                } else if (isPaused) {
                    isRecordingRunning = false;
                    destroyMediaProjection();
                    setResult(REQUEST_CODE);
                    finish();
                    showToast("PDF recording is done successfully.", Toast.LENGTH_SHORT);
                    recordTime = 0;
                    currentTimeText.setVisibility(View.GONE);
                } else {
                    onToggleScreenShare(false);
                    setResult(REQUEST_CODE);
                    finish();
                    showToast("PDF recording is done successfully.", Toast.LENGTH_SHORT);
                    recordTime = 0;
                    currentTimeText.setVisibility(View.GONE);
                }
            }
        });

        stopBtnOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TypedValue outValue = new TypedValue();
                RecordPDFOnBoardActivity.this.getTheme().resolveAttribute(android.R.attr.selectableItemBackground, outValue, true);
                stopBtnOut.setBackgroundResource(outValue.resourceId);
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
                    startActivity(new Intent(RecordPDFOnBoardActivity.this, MainActivity.class));
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
                    startActivity(new Intent(RecordPDFOnBoardActivity.this, MainActivity.class));
                }
            }
        });

        resumeBtnOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                /*TypedValue outValue = new TypedValue();
                RecordPDFOnBoardActivity.this.getTheme().resolveAttribute(android.R.attr.selectableItemBackground, outValue, true);
                resumeBtnOut.setBackgroundResource(outValue.resourceId);*/

                ///demo change shashank
                if (isChromebook()) {
                    pauseBtnOut.setVisibility(View.GONE);
                } else {
                    pauseBtnOut.setVisibility(View.VISIBLE);
                }
                resumeBtnOut.setVisibility(View.GONE);
                isPaused = false;
                isResumed = true;
//                resumeScreenSharing();
                recordTime = tempRecordTime;

                if(sessionManager.getSessionAppIsScreenCastActive()){
                    startScreenCastRecording();
                }else {
                    resumeScreenSharing();
                }
            }
        });

        pauseBtnOut.setOnClickListener(new View.OnClickListener() {
            @TargetApi(Build.VERSION_CODES.KITKAT)
            @Override
            public void onClick(View view) {
               /* TypedValue outValue = new TypedValue();
                RecordPDFOnBoardActivity.this.getTheme().resolveAttribute(android.R.attr.selectableItemBackground, outValue, true);
                pauseBtnOut.setBackgroundResource(outValue.resourceId);*/

                pauseBtnOut.setVisibility(View.GONE);
                //resumeBtnOut.setVisibility(View.VISIBLE);
                if (isChromebook()) {
                    resumeBtnOut.setVisibility(View.GONE);
                } else {
                    resumeBtnOut.setVisibility(View.VISIBLE);
                }
                isPaused = true;
                isResumed = false;
//                pauseScreenSharing();

                tempRecordTime = recordTime;
//                handler.removeCallbacks(UpdateRecordTime);

                if(sessionManager.getSessionAppIsScreenCastActive()){
                    stopScreenCastRecording();
                }else {
                    pauseScreenSharing();
                    handler.removeCallbacks(UpdateRecordTime);
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
                if (isRecordingRunning == true && isPinchBtnClicked == true) {
                    setImageMovable(false);
                }

                if (isRecordingRunning == false && isPinchBtnClicked == true) {
                    setImageMovable(false);
                }

            }
        });

        nextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //next page

                addViewScreenshotInList(whiteBoardViewPager.getCurrentItem());

                if (counterInd == whiteBoardViewPager.getCurrentItem()) {
                    counterInd++;
                    if (whiteBoardViewPager.getCurrentItem() < whiteBoardViewPager.getAdapter().getCount() - 1) {
                        int cItm = whiteBoardViewPager.getCurrentItem() + 1;
                        int cLimit = whiteBoardViewPager.getOffscreenPageLimit();
                        whiteBoardViewPager.setCurrentItem(cItm);
                        int rItm = whiteBoardViewPager.getCurrentItem();
                        whiteBoardViewPager.setOffscreenPageLimit(rItm + 1);
                        if (isRecordingRunning == true && isPinchBtnClicked == true) {
                            setImageMovable(false);
                        }
                    /*if (rItm > cLimit) {
                        whiteBoardViewPager.setOffscreenPageLimit(rItm + 1);
                        if (isRecordingRunning) {
                            setImageMovable(false);
                        }
                    }*/
                    }
                } else {

                    if (whiteBoardViewPager.getCurrentItem() < whiteBoardViewPager.getAdapter().getCount() - 1) {
                        whiteBoardViewPager.setCurrentItem(whiteBoardViewPager.getCurrentItem() + 1);
                    }
                    if (isRecordingRunning == true && isPinchBtnClicked == true) {
                        setImageMovable(false);
                    }
                }
                if (isRecordingRunning == false && isPinchBtnClicked == true) {
                    setImageMovable(false);
                }
            }
        });

        prevBtn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //previous page
                if (whiteBoardViewPager.getCurrentItem() > 0) {
                    whiteBoardViewPager.setCurrentItem(whiteBoardViewPager.getCurrentItem() - 1);
                }
                if (isRecordingRunning) {
                    setImageMovable(false);
                }
                // setImageMovable(false);
            }
        });

        nextBtn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (counterInd == whiteBoardViewPager.getCurrentItem()) {
                    counterInd++;
                    //next page
                    if (whiteBoardViewPager.getCurrentItem() < whiteBoardViewPager.getAdapter().getCount() - 1) {
                        int cItm = whiteBoardViewPager.getCurrentItem() + 1;
                        int cLimit = whiteBoardViewPager.getOffscreenPageLimit();
                        whiteBoardViewPager.setCurrentItem(cItm);
                        int rItm = whiteBoardViewPager.getCurrentItem();
                        whiteBoardViewPager.setOffscreenPageLimit(rItm + 1);
                        if (isRecordingRunning) {
                            setImageMovable(false);
                        }
                    /*if (rItm > cLimit) {
                        whiteBoardViewPager.setOffscreenPageLimit(rItm + 1);
                        if (isRecordingRunning) {
                            setImageMovable(false);
                        }
                    }*/
                    }
                } else {
                    if (whiteBoardViewPager.getCurrentItem() < whiteBoardViewPager.getAdapter().getCount() - 1) {
                        whiteBoardViewPager.setCurrentItem(whiteBoardViewPager.getCurrentItem() + 1);
                    }
                    if (isRecordingRunning) {
                        setImageMovable(false);
                    }
                }
                // setImageMovable(false);
            }
        });

        pencilStrokesBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final SimpleTooltip tooltip = new SimpleTooltip.Builder(RecordPDFOnBoardActivity.this)
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
                smallPencilSizeBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                /*TypedValue outValue = new TypedValue();
                RecordWhiteBoardActivity.this.getTheme().resolveAttribute(android.R.attr.selectableItemBackground, outValue, true);
                smallPencilSizeBtn.setBackgroundResource(outValue.resourceId);*/
                        setEraser(false);
                        eraseBtn.setBackground(ContextCompat.getDrawable(RecordPDFOnBoardActivity.this, R.drawable.icon_eraser_gray));
                        pencilStrokesBtn.setBackground(ContextCompat.getDrawable(RecordPDFOnBoardActivity.this, R.drawable.pencil_small));
                        mediumPencilSizeBtn.setBackground(ContextCompat.getDrawable(RecordPDFOnBoardActivity.this, R.drawable.pencil_medium));
                        largePencilSizeBtn.setBackground(ContextCompat.getDrawable(RecordPDFOnBoardActivity.this, R.drawable.pencil_large));

                        for (int i = 0; i < whiteBoardViewPagerAdapter.getFragmentsList().size(); i++) {
                            PDFPageFragment temp = (PDFPageFragment) whiteBoardViewPagerAdapter.getFragmentsList().get(i);
                            temp.setPenStrokeForDrawing(2);
                        }
                        showHideToolBar();
                        tooltip.dismiss();
                    }
                });
                mediumPencilSizeBtn.setBackground(ContextCompat.getDrawable(RecordPDFOnBoardActivity.this, R.drawable.pencil_medium));
                mediumPencilSizeBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                /*TypedValue outValue = new TypedValue();
                RecordWhiteBoardActivity.this.getTheme().resolveAttribute(android.R.attr.selectableItemBackground, outValue, true);
                mediumPencilSizeBtn.setBackgroundResource(outValue.resourceId);*/
                        setEraser(false);
                        eraseBtn.setBackground(ContextCompat.getDrawable(RecordPDFOnBoardActivity.this, R.drawable.icon_eraser_gray));
                        smallPencilSizeBtn.setBackground(ContextCompat.getDrawable(RecordPDFOnBoardActivity.this, R.drawable.pencil_small));
                        pencilStrokesBtn.setBackground(ContextCompat.getDrawable(RecordPDFOnBoardActivity.this, R.drawable.pencil_medium));
                        largePencilSizeBtn.setBackground(ContextCompat.getDrawable(RecordPDFOnBoardActivity.this, R.drawable.pencil_large));
                        for (int i = 0; i < whiteBoardViewPagerAdapter.getFragmentsList().size(); i++) {
                            PDFPageFragment temp = (PDFPageFragment) whiteBoardViewPagerAdapter.getFragmentsList().get(i);
                            temp.setPenStrokeForDrawing(6);
                        }
                        showHideToolBar();
                        tooltip.dismiss();
                    }
                });
                largePencilSizeBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                /*TypedValue outValue = new TypedValue();
                RecordWhiteBoardActivity.this.getTheme().resolveAttribute(android.R.attr.selectableItemBackground, outValue, true);
                largePencilSizeBtn.setBackgroundResource(outValue.resourceId);*/
                        setEraser(false);
                        eraseBtn.setBackground(ContextCompat.getDrawable(RecordPDFOnBoardActivity.this, R.drawable.icon_eraser_gray));
                        smallPencilSizeBtn.setBackground(ContextCompat.getDrawable(RecordPDFOnBoardActivity.this, R.drawable.pencil_small));
                        mediumPencilSizeBtn.setBackground(ContextCompat.getDrawable(RecordPDFOnBoardActivity.this, R.drawable.pencil_medium));
                        pencilStrokesBtn.setBackground(ContextCompat.getDrawable(RecordPDFOnBoardActivity.this, R.drawable.pencil_large));
                        for (int i = 0; i < whiteBoardViewPagerAdapter.getFragmentsList().size(); i++) {
                            PDFPageFragment temp = (PDFPageFragment) whiteBoardViewPagerAdapter.getFragmentsList().get(i);
                            temp.setPenStrokeForDrawing(10);
                        }
                        showHideToolBar();
                        tooltip.dismiss();
                    }
                });
                tooltip.show();
            }
        });

        colorsPaletBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final SimpleTooltip tooltip = new SimpleTooltip.Builder(RecordPDFOnBoardActivity.this)
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

                blackBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
//                eraseBtn.setBackgroundTintList(ContextCompat.getColorStateList(RecordPDFOnBoardActivity.this,R.color.ColorsWhite));
                        eraseBtn.setBackground(ContextCompat.getDrawable(RecordPDFOnBoardActivity.this, R.drawable.icon_eraser_gray));
                        colorsPaletBtn.setBackground(ContextCompat.getDrawable(RecordPDFOnBoardActivity.this, R.drawable.round_image_black_whiteborder));

                        redBtn.setBackground(ContextCompat.getDrawable(RecordPDFOnBoardActivity.this, R.drawable.round_image_red_whiteborder));
                        greenBtn.setBackground(ContextCompat.getDrawable(RecordPDFOnBoardActivity.this, R.drawable.round_image_green_whiteborder));
                        blueBtn.setBackground(ContextCompat.getDrawable(RecordPDFOnBoardActivity.this, R.drawable.round_image_blue_whiteborder));

//                redBtn.setBackgroundColor(ContextCompat.getColor(RecordPDFOnBoardActivity.this, R.color.ColorRed));
//                blueBtn.setBackgroundColor(ContextCompat.getColor(RecordPDFOnBoardActivity.this, R.color.ColorsBlue));
//                greenBtn.setBackgroundColor(ContextCompat.getColor(RecordPDFOnBoardActivity.this, R.color.ColorsGreen));
                        setPenColor(Color.BLACK);
                        showHideToolBar();
                        tooltip.dismiss();
                    }
                });

                redBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
//                eraseBtn.setBackgroundTintList(ContextCompat.getColorStateList(RecordPDFOnBoardActivity.this,R.color.ColorsWhite));
                        eraseBtn.setBackground(ContextCompat.getDrawable(RecordPDFOnBoardActivity.this, R.drawable.icon_eraser_gray));
                        colorsPaletBtn.setBackground(ContextCompat.getDrawable(RecordPDFOnBoardActivity.this, R.drawable.round_image_red_whiteborder));

                        blackBtn.setBackground(ContextCompat.getDrawable(RecordPDFOnBoardActivity.this, R.drawable.round_image_black_whiteborder));
                        greenBtn.setBackground(ContextCompat.getDrawable(RecordPDFOnBoardActivity.this, R.drawable.round_image_green_whiteborder));
                        blueBtn.setBackground(ContextCompat.getDrawable(RecordPDFOnBoardActivity.this, R.drawable.round_image_blue_whiteborder));

//                blackBtn.setBackgroundColor(ContextCompat.getColor(RecordPDFOnBoardActivity.this, R.color.ColorsBlack));
//                blueBtn.setBackgroundColor(ContextCompat.getColor(RecordPDFOnBoardActivity.this, R.color.ColorsBlue));
//                greenBtn.setBackgroundColor(ContextCompat.getColor(RecordPDFOnBoardActivity.this, R.color.ColorsGreen));
                        setPenColor(Color.RED);
                        showHideToolBar();
                        tooltip.dismiss();
                    }
                });

                greenBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
//                eraseBtn.setBackgroundTintList(ContextCompat.getColorStateList(RecordPDFOnBoardActivity.this,R.color.ColorsWhite));
                        eraseBtn.setBackground(ContextCompat.getDrawable(RecordPDFOnBoardActivity.this, R.drawable.icon_eraser_gray));
                        colorsPaletBtn.setBackground(ContextCompat.getDrawable(RecordPDFOnBoardActivity.this, R.drawable.round_image_green_whiteborder));

                        blackBtn.setBackground(ContextCompat.getDrawable(RecordPDFOnBoardActivity.this, R.drawable.round_image_black_whiteborder));
                        redBtn.setBackground(ContextCompat.getDrawable(RecordPDFOnBoardActivity.this, R.drawable.round_image_red_whiteborder));
                        blueBtn.setBackground(ContextCompat.getDrawable(RecordPDFOnBoardActivity.this, R.drawable.round_image_blue_whiteborder));

//                blackBtn.setBackgroundColor(ContextCompat.getColor(RecordPDFOnBoardActivity.this, R.color.ColorsBlack));
//                blueBtn.setBackgroundColor(ContextCompat.getColor(RecordPDFOnBoardActivity.this, R.color.ColorsBlue));
//                redBtn.setBackgroundColor(ContextCompat.getColor(RecordPDFOnBoardActivity.this, R.color.ColorRed));

                        setPenColor(Color.GREEN);
                        showHideToolBar();
                        tooltip.dismiss();
                    }
                });

                blueBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
//                eraseBtn.setBackgroundTintList(ContextCompat.getColorStateList(RecordPDFOnBoardActivity.this,R.color.ColorsWhite));
                        eraseBtn.setBackground(ContextCompat.getDrawable(RecordPDFOnBoardActivity.this, R.drawable.icon_eraser_gray));
                        colorsPaletBtn.setBackground(ContextCompat.getDrawable(RecordPDFOnBoardActivity.this, R.drawable.round_image_blue_whiteborder));

                        redBtn.setBackground(ContextCompat.getDrawable(RecordPDFOnBoardActivity.this, R.drawable.round_image_red_whiteborder));
                        greenBtn.setBackground(ContextCompat.getDrawable(RecordPDFOnBoardActivity.this, R.drawable.round_image_green_whiteborder));
                        blackBtn.setBackground(ContextCompat.getDrawable(RecordPDFOnBoardActivity.this, R.drawable.round_image_black_whiteborder));

//                blackBtn.setBackgroundColor(ContextCompat.getColor(RecordPDFOnBoardActivity.this, R.color.ColorsBlack));
//                greenBtn.setBackgroundColor(ContextCompat.getColor(RecordPDFOnBoardActivity.this, R.color.ColorsGreen));
//                redBtn.setBackgroundColor(ContextCompat.getColor(RecordPDFOnBoardActivity.this, R.color.ColorRed));
                        setPenColor(Color.BLUE);
                        showHideToolBar();
                        tooltip.dismiss();
                    }
                });
                tooltip.show();


            }
        });

        eraseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                eraseBtn.setBackgroundTintList(ContextCompat.getColorStateList(RecordPDFOnBoardActivity.this,R.color.SubTextColor));
//                blackBtn.setBackgroundColor(ContextCompat.getColor(RecordPDFOnBoardActivity.this, R.color.ColorsBlack));
//                redBtn.setBackgroundColor(ContextCompat.getColor(RecordPDFOnBoardActivity.this, R.color.ColorRed));
//                blueBtn.setBackgroundColor(ContextCompat.getColor(RecordPDFOnBoardActivity.this, R.color.ColorsBlue));
//                greenBtn.setBackgroundColor(ContextCompat.getColor(RecordPDFOnBoardActivity.this, R.color.ColorsGreen));

               /* blackBtn.setBackground(ContextCompat.getDrawable(RecordPDFOnBoardActivity.this, R.drawable.round_image_black_whiteborder));
                redBtn.setBackground(ContextCompat.getDrawable(RecordPDFOnBoardActivity.this, R.drawable.round_image_red_whiteborder));
                greenBtn.setBackground(ContextCompat.getDrawable(RecordPDFOnBoardActivity.this, R.drawable.round_image_green_whiteborder));
                blueBtn.setBackground(ContextCompat.getDrawable(RecordPDFOnBoardActivity.this, R.drawable.round_image_blue_whiteborder));*/

                eraseBtn.setBackground(ContextCompat.getDrawable(RecordPDFOnBoardActivity.this, R.drawable.icon_eraser_blue));
                setEraser(true);
                showHideToolBar();
            }
        });

        removeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setEraser(false);
                eraseBtn.setBackground(ContextCompat.getDrawable(RecordPDFOnBoardActivity.this, R.drawable.icon_eraser_gray));
//                eraseBtn.setBackgroundTintList(ContextCompat.getColorStateList(RecordPDFOnBoardActivity.this,R.color.ColorsWhite));
//                eraseBtn.setBackground(ContextCompat.getDrawable(RecordPDFOnBoardActivity.this, R.drawable.icon_eraser_gray));
                clearDrawingBoard();
                showHideBtn.setVisibility(View.VISIBLE);//change shashank for demo
                //showHideBtn.setVisibility(View.GONE);
                //blackBtn.callOnClick();
                //setPenColor(Color.BLACK);
                showHideToolBar();
//                setImageMovable(isImageScale);
            }
        });


        exportPDFBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ContextCompat.checkSelfPermission(RecordPDFOnBoardActivity.this,Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(RecordPDFOnBoardActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

                        Snackbar.make(findViewById(android.R.id.content), R.string.label_permissions_storage,
                                Snackbar.LENGTH_INDEFINITE).setAction("ENABLE",
                                new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        ActivityCompat.requestPermissions(RecordPDFOnBoardActivity.this,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},REQUEST_PERMISSIONS_PDF_EXPORT);
                                    }
                                }).show();
                    } else {
                        ActivityCompat.requestPermissions(RecordPDFOnBoardActivity.this,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},REQUEST_PERMISSIONS_PDF_EXPORT);
                    }
                }else {
                    addViewScreenshotInList(whiteBoardViewPager.getCurrentItem());
                    exportPDFBtn.setEnabled(false);
                    exportAsPdfFile();
                }
            }
        });

        homeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addViewScreenshotInList(whiteBoardViewPager.getCurrentItem());
                whiteBoardViewPager.setCurrentItem(0);
                if (isRecordingRunning == true && isPinchBtnClicked == true) {
                    setImageMovable(false);
                }else{
                    setImageMovable(true);
                }
                if (isRecordingRunning == false && isPinchBtnClicked == true) {
                    setImageMovable(false);
                }else{
                    setImageMovable(true);
                }
            }
        });

        endBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addViewScreenshotInList(whiteBoardViewPager.getCurrentItem());
                int index = whiteBoardViewPager.getAdapter().getCount();
                whiteBoardViewPager.setCurrentItem(index);
                if (isRecordingRunning == true && isPinchBtnClicked == true) {
                    setImageMovable(false);
                }

                if (isRecordingRunning == false && isPinchBtnClicked == true) {
                    setImageMovable(false);
                }
                // setImageMovable(false);
            }
        });

        imgScaleBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setImageMovable(true);
            }
        });

        undoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setEraser(false);
                eraseBtn.setBackground(ContextCompat.getDrawable(RecordPDFOnBoardActivity.this, R.drawable.icon_eraser_gray));
                PDFPageFragment temp = (PDFPageFragment) whiteBoardViewPagerAdapter.getFragmentsList().get(whiteBoardViewPager.getCurrentItem());
                temp.undoDrawing();
            }
        });

        redoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setEraser(false);
                eraseBtn.setBackground(ContextCompat.getDrawable(RecordPDFOnBoardActivity.this, R.drawable.icon_eraser_gray));
                PDFPageFragment temp = (PDFPageFragment) whiteBoardViewPagerAdapter.getFragmentsList().get(whiteBoardViewPager.getCurrentItem());
                temp.redoDrawing();
            }
        });

        whiteBoardViewPager.addOnPageChangeListener(this);

        setOnVideoProcessingListener(this); //set listener for video processing

        fullscreenBtn.setOnClickListener(new View.OnClickListener() {
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

//        blackBtn.callOnClick();

        tileBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tilesLayout.setVisibility(View.GONE);
                if(isTileViewVisible){
                    tilesLayout.setVisibility(View.VISIBLE);
                }
                isTileViewVisible =!isTileViewVisible;
            }
        });

    }

    @Override
    public void onBackPressed() {//Ewww
        if (isRecordingRunning) {
            if(!isPaused)
            {
                pauseBtnOut.callOnClick();
            }

            final boolean[] noBtnClicked = {false};
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(RecordPDFOnBoardActivity.this);
            alertDialog.setTitle("Confirmation");
            alertDialog.setMessage("Do you want to stop and save?");
            alertDialog.setIcon(R.drawable.app_logo_round_new24);
            alertDialog.setPositiveButton("yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    try {
                        deleteGeneratedPDFBitmaps();
                        isRecordingRunning = false;
                        destroyMediaProjection();
                        setResult(REQUEST_CODE);
                        finish();
                        showToast("Video recorded successfully.", Toast.LENGTH_SHORT);
                        recordTime = 0;
                        currentTimeText.setVisibility(View.GONE);
                        startActivity(new Intent(RecordPDFOnBoardActivity.this, MainActivity.class));
                    } catch (Exception e) {
                        Log.e(TAG, "onBackPressed Exception " + e.getMessage().toString());
                    }
                    stopBtn.callOnClick();

                }
            });
            alertDialog.setNegativeButton("no", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    noBtnClicked[0] =true;
                    return;
                }
            });
            alertDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    if(noBtnClicked[0])
                    {
                        resumeBtnOut.callOnClick();
                    }
                }
            });
            alertDialog.show();

        } else if (isPaused) {

            AlertDialog.Builder alertDialog = new AlertDialog.Builder(RecordPDFOnBoardActivity.this);
            alertDialog.setTitle("Confirmation");
            alertDialog.setMessage("Do you want to stop and save?");
            alertDialog.setIcon(R.drawable.app_logo_round_new24);
            alertDialog.setPositiveButton("yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                    isRecordingRunning = false;
                    destroyMediaProjection();
                    setResult(REQUEST_CODE);
                    try {
                        deleteGeneratedPDFBitmaps();
                    } catch (Exception e) {
                        Log.e(TAG, "onBackPressed Exception " + e.getMessage().toString());
                    }
                    finish();
                    showToast("Video recorded successfully.", Toast.LENGTH_SHORT);
                    recordTime = 0;
                    currentTimeText.setVisibility(View.GONE);
                    startActivity(new Intent(RecordPDFOnBoardActivity.this, MainActivity.class));
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
            try {
                deleteGeneratedPDFBitmaps();
            } catch (Exception e) {
                Log.e(TAG, "onBackPressed Exception " + e.getMessage().toString());
            }
            finish();
            super.onBackPressed();
        }
        /*AlertDialog.Builder alertDialog = new AlertDialog.Builder(RecordPDFOnBoardActivity.this);
        alertDialog.setTitle("Confirmation");
        alertDialog.setMessage("Do you want to stop and save?");
        alertDialog.setIcon(R.drawable.app_logo_round_new24);
        alertDialog.setPositiveButton("yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
               // if (isRecordingRunning)
               // {    onToggleScreenShare(false);   }
                if (isResumed) {
                    onToggleScreenShare(false);
                } else if (isPaused) {
                    isRecordingRunning = false;
                    destroyMediaProjection();
                    setResult(REQUEST_CODE);
                    finish();
                    showToast("Video recorded successfully.", Toast.LENGTH_SHORT);
                    recordTime = 0;
                    currentTimeText.setVisibility(View.GONE);
                } else {
                    onToggleScreenShare(false);
                    setResult(REQUEST_CODE);
                    finish();
                    showToast("Video recorded successfully.", Toast.LENGTH_SHORT);
                    recordTime = 0;
                    currentTimeText.setVisibility(View.GONE);
                }

                try {
                    deleteGeneratedPDFBitmaps();
                }catch (Exception e){Log.e(TAG,"onBackPressed Exception "+e.getMessage().toString());}
                finish();
            }
        });
        alertDialog.setNegativeButton("no", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                return;
            }
        });
        alertDialog.show();*/
        //super.onBackPressed();


    }

    @Override
    public void onVideoProcessCompleted() {
        setResult(REQUEST_CODE);
        finish();
        showToast("Video recorded successfully.", Toast.LENGTH_SHORT);
        startActivity(new Intent(RecordPDFOnBoardActivity.this, MainActivity.class));
    }

    @Override
    public void onVideoProcessFailed() {
        setResult(REQUEST_CODE);
        finish();
        showToast("Recording process is failed. Please try again later", Toast.LENGTH_SHORT);
    }

    private void showHideToolBar() {
        if (isToolBarHidden) {
            showHideBtn.setImageDrawable(ContextCompat.getDrawable(RecordPDFOnBoardActivity.this, R.drawable.downward_grey));
            upperNavigationLayout.setVisibility(View.GONE);
            toolsLayout.setVisibility(View.VISIBLE);

            mainContainer.removeView(showHideBtn);
            RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(90, 90); // You might want to tweak these to WRAP_CONTENT
            lp.addRule(RelativeLayout.RIGHT_OF, R.id.toolsLayout);
            lp.addRule(RelativeLayout.CENTER_IN_PARENT);
            mainContainer.addView(showHideBtn, lp);

            isToolBarHidden = false;
        } else {
            showHideBtn.setImageDrawable(ContextCompat.getDrawable(RecordPDFOnBoardActivity.this, R.drawable.upward_grey));
            upperNavigationLayout.setVisibility(View.GONE);//View.VISIBLE
            toolsLayout.setVisibility(View.GONE);////Change Shashank For Demo
            //toolsLayout.setVisibility(View.VISIBLE);

            mainContainer.removeView(showHideBtn);
            RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(90, 90); // You might want to tweak these to WRAP_CONTENT
            lp.addRule(RelativeLayout.CENTER_IN_PARENT);
            lp.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
            mainContainer.addView(showHideBtn, lp);

            isToolBarHidden = true;
        }
    }

    public void initializePDFRenderer(File pdfFile) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                pdfRenderer = new PdfRenderer(ParcelFileDescriptor.open(pdfFileItem, ParcelFileDescriptor.MODE_READ_ONLY));
                ///this is last seen from here check tommorow
                this.whiteBoardPagesCount = new PdfRenderer(ParcelFileDescriptor.open(pdfFile, ParcelFileDescriptor.MODE_READ_ONLY)).getPageCount();//+20;
               // showToast("whiteBoardPagesCount in PDFRenderer : " + whiteBoardPagesCount);
                Log.e("111",""+this.whiteBoardPagesCount);
                /** if(this.whiteBoardPagesCount > 50){
                 showToast("PDF Pages count must be less then 50 pages");
                 finish();
                 return;
                 }*/
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void mergeZoomImage(){
        PDFPageFragment temp = (PDFPageFragment) whiteBoardViewPagerAdapter.getFragmentsList().get(whiteBoardViewPager.getCurrentItem());
        temp.mergeBitmap();
    }

    private void addViewScreenshotInList(int index){
        try {
            PDFPageFragment temp = (PDFPageFragment) whiteBoardViewPagerAdapter.getFragmentsList().get(index);

            if (imageScreenshotList.size() > index) {
                imageScreenshotList.remove(index);
            }
            imageScreenshotList.add(index, temp.takeScreenshotView());
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void setEraser(boolean isActive) {
        for (int i = 0; i < whiteBoardViewPagerAdapter.getFragmentsList().size(); i++) {
            PDFPageFragment temp = (PDFPageFragment) whiteBoardViewPagerAdapter.getFragmentsList().get(i);
            if (temp != null)
                temp.setEraserActive(isActive);
        }

    }

    private void setImageMovable(boolean enable) {
        for (int i = 0; i < whiteBoardViewPagerAdapter.getFragmentsList().size(); i++) {
            PDFPageFragment temp = (PDFPageFragment) whiteBoardViewPagerAdapter.getFragmentsList().get(i);
            if (temp != null)
                temp.enableImageMoving(enable);
        }
    }

    private void setPenColor(int color) {
        for (int i = 0; i < whiteBoardViewPagerAdapter.getFragmentsList().size(); i++) {
            PDFPageFragment temp = (PDFPageFragment) whiteBoardViewPagerAdapter.getFragmentsList().get(i);
            if (temp != null)
                temp.setPenColorForDrawing(color);
        }

    }

    private void clearDrawingBoard() {
        for (int i = 0; i < whiteBoardViewPagerAdapter.getFragmentsList().size(); i++) {
            PDFPageFragment temp = (PDFPageFragment) whiteBoardViewPagerAdapter.getFragmentsList().get(i);
            if (temp != null)
                if (temp.getFragmentId() == whiteBoardViewPager.getCurrentItem()) {
                    temp.clearDrawing();
                    break;
                }
        }
    }

    @SuppressLint("LongLogTag")
    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

        pagerIndex = whiteBoardViewPager.getCurrentItem();
        Log.e("111","onPageScrolled"+whiteBoardPagesCount);

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
        //Log.e(TAG,"INDEX "+pagerIndex);
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
        if (ContextCompat.checkSelfPermission(RecordPDFOnBoardActivity.this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) + ContextCompat
                .checkSelfPermission(RecordPDFOnBoardActivity.this,
                        Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale
                    (RecordPDFOnBoardActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) ||
                    ActivityCompat.shouldShowRequestPermissionRationale
                            (RecordPDFOnBoardActivity.this, Manifest.permission.RECORD_AUDIO)) {
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
                                ActivityCompat.requestPermissions(RecordPDFOnBoardActivity.this,
                                        new String[]{Manifest.permission
                                                .WRITE_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO},
                                        REQUEST_PERMISSIONS);
                            }
                        }).show();
            } else {
                ActivityCompat.requestPermissions(RecordPDFOnBoardActivity.this,
                        new String[]{Manifest.permission
                                .WRITE_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO},
                        REQUEST_PERMISSIONS);
            }
        } else {
            onToggleScreenShare(true);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {

        switch (requestCode) {
            case IMAGE_REORDER_RESULT:
                if (whiteBoardViewPagerAdapter != null) {

                    ArrayList<Image> imageReOrderedList = intent.getParcelableArrayListExtra(Constants.KEY_BUNDLE_LIST);
                    imageAdList.clear(); //#18DEC19
                    imageAdList = imageReOrderedList;//#18DEC19
                    this.whiteBoardPagesCount = imageReOrderedList.size();
                    //whiteBoardViewPagerAdapter.reOrderList(imageReOrderedList);
                    //reOrderImageList= whiteBoardViewPagerAdapter.getPDFImageList();
                    //System.out.print("" + imageReOrderedList);
                    Log.e("1 imageReOrderedList ", ""+ imageReOrderedList.size());

                    whiteBoardViewPagerAdapter.reOrderList(imageReOrderedList);
                    setPDFTileList(imageReOrderedList);
                    /*showToast("" + imageReOrderedList.size());

                    Log.e("111",""+this.whiteBoardPagesCount);
                    showToast("whiteBoardPagesCount : " + this.whiteBoardPagesCount+" ~~ "+imageReOrderedList.size());*/

                } else {
//                    imageRecordList = intent.getParcelableArrayListExtra(Constants.KEY_BUNDLE_LIST);
                }
                //setImageMovable(false);
                break;

            case REQUEST_CODE:
                if (resultCode != RESULT_OK) {
                    showToast("Screen Cast Permission Denied");
//            mToggleButton.setChecked(false);
                    stopBtn.setVisibility(View.GONE);
                    resumeBtnOut.setVisibility(View.GONE);
                    pauseBtnOut.setVisibility(View.GONE);
                    stopBtnOut.setVisibility(View.GONE);
                    startBtn.setVisibility(View.VISIBLE);
                    finish();
                    return;
                }
                try {
                    mMediaProjectionCallback = new MediaProjectionCallback();
                    mMediaProjection = mProjectionManager.getMediaProjection(resultCode, intent);
                    mMediaProjection.registerCallback(mMediaProjectionCallback, null);
                    mVirtualDisplay = createVirtualDisplay();
                    mMediaRecorder.start();
                    handler.post(UpdateRecordTime);
                } catch (RuntimeException stopRuntimeException) {
                    mMediaRecorder.reset();
                    stopScreenSharing();
                    setResult(REQUEST_CODE);
                    showToast("Something went wrong. Please try again later..!");
                    finish();
                }

                break;

            case PICKFILE_RESULT_CODE:

                if (resultCode != RESULT_OK) {
                    showToast("Operation failed. Please try again later..!");
                    return;
                }
                if (intent.getData() != null) {
                    openSlidePDFRecordingScreen(intent.getData());
                } else {
                    showToast("Something went wrong. Please try again later..!");
                }
                break;
        }
    }

    private void setPDFTileList(ArrayList<Image> imageTileList){
        PageTileListAdapter pageTileListAdapter = new PageTileListAdapter(RecordPDFOnBoardActivity.this, imageTileList, new PageTileListAdapter.PageTileListActionListener() {
            @Override
            public void onPageItemSelected(int index, Image image) {
                tilesLayout.setVisibility(View.GONE);
                addViewScreenshotInList(whiteBoardViewPager.getCurrentItem());
                whiteBoardViewPager.setCurrentItem(index);
            }
        });

        pageTileListRecyclerView.setLayoutManager(new LinearLayoutManager(RecordPDFOnBoardActivity.this));
        pageTileListRecyclerView.setAdapter(pageTileListAdapter);

    }

    private void openSlidePDFRecordingScreen(Uri pdfUriData) {

    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
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
//                showToast("PDF recording is done successfully.", Toast.LENGTH_SHORT);
            }
        } catch (RuntimeException stopRuntimeException) {
            mMediaRecorder.reset();
            stopScreenSharing();
            setResult(REQUEST_CODE);
            buildNotification("Video Processing Failed...", fileData);
            finish();
            showToast("PDF recording is not done successfully.", Toast.LENGTH_SHORT);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void shareScreen() {
        if (mMediaProjection == null) {
            startActivityForResult(mProjectionManager.createScreenCaptureIntent(), REQUEST_CODE);
            return;
        }
        mVirtualDisplay = createVirtualDisplay();
        mMediaRecorder.start();
        handler.post(UpdateRecordTime);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private VirtualDisplay createVirtualDisplay() {
        return mMediaProjection.createVirtualDisplay("MainActivity",
                DISPLAY_WIDTH, DISPLAY_HEIGHT, mScreenDensity,
                DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
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
            mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            mMediaRecorder.setOutputFile(outputFileName);
//            mMediaRecorder.setVideoSize(DISPLAY_WIDTH, 888);
            mMediaRecorder.setVideoSize(DISPLAY_WIDTH, DISPLAY_HEIGHT);
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
            e.printStackTrace();
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
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

    @TargetApi(Build.VERSION_CODES.KITKAT)
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void pauseScreenSharing() {
        mMediaRecorder.stop();
        mMediaRecorder.reset();
        if (mVirtualDisplay == null) {
            return;
        }
        mVirtualDisplay.release();

    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void resumeScreenSharing() {
        initRecorder();
        shareScreen();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void stopScreenSharing() {
        if (mVirtualDisplay == null) {
            return;
        }
        mVirtualDisplay.release();
        //mMediaRecorder.release(); //If used: mMediaRecorder object cannot
        // be reused again
        destroyMediaProjection();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (isRecordingRunning)
            onToggleScreenShare(false);

//        destroyMediaProjection();

        getGlobalMethods().clearContentFromDirectory(VolleySingleton.getAppStorageTempDirectory());
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
        Log.v(TAG, "MediaProjection Stopped");
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

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
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
            ///demo change shashank
            if (isChromebook()) {
                pauseBtnOut.setVisibility(View.GONE);
            } else {
                pauseBtnOut.setVisibility(View.VISIBLE);
            }
            stopBtnOut.setVisibility(View.VISIBLE);

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
            fList.add(PDFPageFragment.getInstance());
        }

        return fList;
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
                    stopBtn.callOnClick();
                    return;
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

    private class PdfPagesSufflingOperation extends AsyncTask<String, Void, Boolean> {

        @Override
        protected Boolean doInBackground(String... params) {
            try {
                for (int i = 0; i < pdfRenderer.getPageCount(); i++) {
                    int autoNo = generator.nextInt(10000);//009
                    Bitmap tmpBitmap = getBitmapFromPDFByIndex(i);
                    if (tmpBitmap != null) {
                        File tmpfile = saveImageFromBitmap(tmpBitmap, "" + autoNo);
                        imageAdList.add(i, new Image(autoNo, Uri.fromFile(tmpfile), tmpfile.getPath(), false));
                        Log.e(TAG, "imageAdList Size" + imageAdList.size());
                    }
                }
                Log.e(TAG, "PDF Page Count :" + pdfRenderer.getPageCount());
                return true;
            } catch (Exception e) {
                Log.e(TAG, "Exception :" + e.getMessage().toString());
            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (result) {
                hideBusyProgress();
                Intent imageReOrderIntent = new Intent(RecordPDFOnBoardActivity.this, SlidePDFReOrderActivity.class);
                imageReOrderIntent.putExtra(pdfFileKey, pdfFileItem);
                imageReOrderIntent.putParcelableArrayListExtra(Constants.KEY_BUNDLE_LIST, imageAdList);// reOrderImageList);
                startActivityForResult(imageReOrderIntent, IMAGE_REORDER_RESULT);
            } else {
                showToast("System Fail To Create Bitmap's");
            }
        }

        @Override
        protected void onPreExecute() {
            showBusyProgress();
            try {
                deleteGeneratedPDFBitmaps();
            } catch (Exception e) {
                Log.e(TAG, "onBackPressed Exception " + e.getMessage().toString());
            }
        }

       /* @Override
        protected void onProgressUpdate(Void... values) {}*/
    }

    private Bitmap getBitmapFromPDFByIndex(int index) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            try {
                Bitmap bitmap;
                Bitmap newBitmap = null;
                PdfRenderer.Page page = pdfRenderer.openPage(index);

                int width = 1280;
                int height = 720;

//                int width = mContext.getResources().getDisplayMetrics().densityDpi / 72 * page.getWidth();
//                int height = mContext.getResources().getDisplayMetrics().densityDpi / 72 * page.getHeight();

                if (page.getHeight() > page.getWidth()) {
                    height = 1056;
                    width = 816;
                }
                bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
                newBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
                page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);

//                    bitmaps.add(bitmap);
                Canvas canvas = new Canvas(newBitmap);
                canvas.drawColor(Color.WHITE);
                canvas.drawBitmap(bitmap, 0, 0, null);
                bitmap.recycle();
                // close the page
                page.close();
                // close the renderer
//                pdfRenderer.close();
                return newBitmap;
            } catch (Exception e) {
                Crashlytics.logException(e);
                e.printStackTrace();
                return null;
            } catch (OutOfMemoryError memoryError) {
                Crashlytics.logException(memoryError);
                memoryError.printStackTrace();
                return null;
            }
        } else {
            return null;
        }
    }

    public File saveImageFromBitmap(Bitmap finalBitmap, String fileId) {
        File myDir = new File(VolleySingleton.getAppStorageDirectory() + "/pdf");
        if (!myDir.isDirectory())
            myDir.mkdirs();

        String fname = "img_" + fileId + ".jpg";
        File file = new File(myDir, fname);
        if (file.exists()) file.delete();
        try {
            FileOutputStream out = new FileOutputStream(file);
            finalBitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
            out.flush();
            out.close();
            return file;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public void deleteGeneratedPDFBitmaps() {
        File dir = new File(VolleySingleton.getAppStorageDirectory() + "/pdf");
        if (dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                new File(dir, children[i]).delete();
            }
        }
    }

    //#18DEC19 new code

    public void exportAsPdfFile(){
        if(imageScreenshotList != null && imageAdList.size()>0){
            if(imageScreenshotList.size()>0 && imageScreenshotList.size() == imageAdList.size()){
                //go for convert bitmap to pdf
                convertBitMapToPdfFile();
            }else{
                int removeFromIndex = imageScreenshotList.size();
                for(int i = removeFromIndex; i<imageAdList.size(); i++){
                    imageScreenshotList.add(BitmapFactory.decodeFile(imageAdList.get(i).imagePath));
                    imageAdList.remove(i);
                }
                if(imageScreenshotList.size()>0){
                    //go for convert bitmap to pdf
                    convertBitMapToPdfFile();
                }
            }
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

        File pdfFile  = new File(root,"Presentation_"+new Date().getTime()+".pdf");
        try{
            FileOutputStream fileOutputStream = new FileOutputStream(pdfFile);
            pdfDocument.writeTo(fileOutputStream);
        }catch (IOException io){
            io.printStackTrace();
            exportPDFBtn.setEnabled(true);
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
