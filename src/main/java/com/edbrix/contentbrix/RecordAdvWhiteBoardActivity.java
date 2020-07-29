package com.edbrix.contentbrix;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.MediaRecorder;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.net.Uri;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;

import androidx.annotation.RequiresApi;

import com.edbrix.contentbrix.adapters.ArrayAdapterWithIcon;
import com.edbrix.contentbrix.customview.CustomDialogSlideFromBottomAdvWhiteboard;
import com.edbrix.contentbrix.encoder.EncoderConfig;
import com.edbrix.contentbrix.encoder.FrameEncoder;
import com.edbrix.contentbrix.encoder.HevcEncoderConfig;
import com.edbrix.contentbrix.utils.Screenshots;
import com.google.android.material.snackbar.Snackbar;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.SparseIntArray;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.edbrix.contentbrix.baseclasses.BaseActivity;
import com.edbrix.contentbrix.commons.GlobalMethods;
import com.edbrix.contentbrix.data.FileData;
import com.edbrix.contentbrix.utils.SessionManager;
import com.edbrix.contentbrix.volley.VolleySingleton;
import com.myscript.iink.Configuration;
import com.myscript.iink.ContentBlock;
import com.myscript.iink.ContentPart;
import com.myscript.iink.ConversionState;
import com.myscript.iink.Editor;
import com.myscript.iink.Engine;
import com.myscript.iink.IEditorListener;
import com.myscript.iink.MimeType;
import com.myscript.iink.graphics.Point;
import com.myscript.iink.graphics.Transform;
import com.myscript.iink.uireferenceimplementation.EditorView;
import com.myscript.iink.uireferenceimplementation.IInputControllerListener;
import com.myscript.iink.uireferenceimplementation.ImageLoader;
import com.myscript.iink.uireferenceimplementation.InputController;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

import io.github.douglasjunior.androidSimpleTooltip.SimpleTooltip;
import petrov.kristiyan.colorpicker.ColorPicker;

import static android.os.Environment.getExternalStorageDirectory;

public class RecordAdvWhiteBoardActivity extends BaseActivity implements View.OnClickListener, BaseActivity.OnVideoProcessingListener {

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
    private TextView countDownTimerText;
    private MyCountDownTimer myCountDownTimer;
    private int countDownTime;
    private int countDownInterval;
    private ArrayList<String> sourceFilePath;
    private boolean isRecordingRunning = false;
    private boolean isResumed = false;
    private boolean isPaused = false;
    private boolean isToolBarHidden = false;
    private int recordTime;
    private int tempRecordTime;
    private Handler handler;
    private TextView currentTimeText;
    private FileData fileData;

    private TextView pageIndex;
    private ImageView prevBtn;
    private ImageView nextBtn;
    private ImageView resumeBtnOut;
    private ImageView pauseBtnOut;
    private ImageView stopBtnOut;
    private ImageView startBtn;
    private ImageView showHideBtn;
    private Button fullscreenBtn;
    private Button colorsPalletBtn;
    private Button pencilStrokesBtn;

    private LinearLayout leftButtonLayout;
    private RelativeLayout toolsLayout;
    private ImageView stopBtn;
    private boolean isPrevBtnClicked;
    private boolean isNextBtnClicked;
    private boolean isNewPartBtnClicked;


    /***********************************************************************************************************************************************************/
    private static final String TAG = "RecordAdvWhiteBoardActivity";
    private static final String INPUT_MODE_KEY = "inputMode";
    protected Engine engine;
    protected EditorView editorView;
    protected DocumentController documentController;
    private String penStrokeColor = "#000000";
    private double penStrokeSize = 0.5;
    public float xVal = 0;
    public float yVal = 0;

    private SessionManager sessionManager;
    private GlobalMethods globalMethods;

    private boolean isFullScreen = false;

    //23Jan2020 - Raj K


    private EncoderConfig mEncoderConfig;
    private FrameEncoder frameEncoder;

    private int DEFAULT_WIDTH = 320;
    private int DEFAULT_HEIGHT = 240;

    private static final float DEFAULT_FRAME_RATE_PER_SECOND = 5;
    private static final int DEFAULT_BIT_RATE = 1000000;

    private String AudioSavePathInDevice = null;
    private String VideoSavePathInDevice = null;
    private int pagePartIndex = 0;
    private int pagePartLength = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        if (isTablet()) {
//            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
//        } else {
//            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
//        }
        engine = ContentBrixApp.getEngine();
        // configure recognition
        Configuration conf = engine.getConfiguration();
        String confDir = "zip://" + getPackageCodePath() + "!/assets/conf";
        conf.setStringArray("configuration-manager.search-path", new String[]{confDir});
        String tempDir = getFilesDir().getPath() + File.separator + "tmp";
        conf.setString("content-package.temp-folder", tempDir);

        setContentView(R.layout.activity_record_adv_white_board);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);


        setTitle(getResources().getString(R.string.adv_record_whiteboard));
        startBtn = (ImageView) findViewById(R.id.startBtn);
        showHideBtn = (ImageView) findViewById(R.id.showHideBtn);
        stopBtn = (ImageView) findViewById(R.id.stopBtn);

        pageIndex = (TextView) findViewById(R.id.pageIndex);
        prevBtn = (ImageView) findViewById(R.id.prevBtn);
        nextBtn = (ImageView) findViewById(R.id.nextBtn);
        resumeBtnOut = (ImageView) findViewById(R.id.resumeBtnOut);
        pauseBtnOut = (ImageView) findViewById(R.id.pauseBtnOut);
        stopBtnOut = (ImageView) findViewById(R.id.stopBtnOut);
        leftButtonLayout = (LinearLayout) findViewById(R.id.leftButtonLayout);
        toolsLayout = (RelativeLayout) findViewById(R.id.toolsLayout);
        colorsPalletBtn = (Button) findViewById(R.id.colorsPalletBtn);
        pencilStrokesBtn = (Button) findViewById(R.id.pencilStrokesBtn);
        fullscreenBtn = (Button) findViewById(R.id.fullscreenBtn);
        currentTimeText = (TextView) findViewById(R.id.currentTimeText);
        countDownTimerText = (TextView) findViewById(R.id.countDownTimerText);
        editorView = findViewById(R.id.editor_view);
        editorView.setEngine(engine);

        sessionManager = new SessionManager(RecordAdvWhiteBoardActivity.this);
        globalMethods = new GlobalMethods();

        sourceFilePath = new ArrayList<>();
        setListners();
        handler = new Handler();

        // count down time
        countDownTime = 5; // in sec
        countDownInterval = 1000; // in sec

        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        mScreenDensity = metrics.densityDpi;
        mMediaRecorder = new MediaRecorder();
        mProjectionManager = (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);
        countDownTimerText.setText("" + countDownTime);
        myCountDownTimer = new MyCountDownTimer(countDownTime * 1000, countDownInterval);
        setThemeForEditor();
        final Editor editor = editorView.getEditor();
//        editor.setTheme("stroke { color: #000000; -myscript-pen-width: 1.0 }");
        editor.addListener(new IEditorListener() {
            @Override
            public void partChanging(Editor editor, ContentPart oldPart, ContentPart newPart) {
                // no-op
            }

            @Override
            public void partChanged(Editor editor) {
                invalidateOptionsMenu();
                invalidateIconButtons();
                if (isPrevBtnClicked) {
                    if (pagePartIndex > 0)
                        pagePartIndex--;

                } else if (isNextBtnClicked) {
                    pagePartIndex++;

                } else if (isNewPartBtnClicked) {
                    pagePartLength++;
                    pagePartIndex = pagePartLength;
                } else {
                    pagePartIndex++;
                }
                pageIndex.setText("" + pagePartIndex);
            }

            @Override
            public void contentChanged(Editor editor, String[] blockIds) {
                invalidateOptionsMenu();
                invalidateIconButtons();
            }

            @SuppressLint("LongLogTag")
            @Override
            public void onError(Editor editor, String blockId, String message) {
                Log.e(TAG, "Failed to edit block \"" + blockId + "\"" + message);
            }
        });

        editorView.setImageLoader(new ImageLoader(editor, this.getCacheDir()));
        editorView.setInputControllerListener(new IInputControllerListener() {
            @Override
            public void onDisplayContextMenu(final float x, final float y, final ContentBlock contentBlock) {
                displayContextMenu(x, y, contentBlock);
            }
        });

        int inputMode = InputController.INPUT_MODE_FORCE_PEN; // If using an active pen, put INPUT_MODE_AUTO here
        if (savedInstanceState != null)
            inputMode = savedInstanceState.getInt(INPUT_MODE_KEY, inputMode);
        setInputMode(inputMode);

        documentController = new DocumentController(this, editorView);
        final String fileName = documentController.getSavedFileName();
        final int partIndex = documentController.getSavedPartIndex();
        pageIndex.setText("" + (partIndex + 1));
        Log.e("Eww", fileName + " : " + partIndex);

        // wait for view size initialization before setting part
        editorView.post(new Runnable() {
            @Override
            public void run() {
                if (fileName != null) {
                    documentController.openPart(fileName, partIndex);
                } else {
                    isNewPartBtnClicked = true;
                    documentController.newPart();
                }
            }
        });

        findViewById(R.id.button_input_mode_forcePen).setOnClickListener(this);
        findViewById(R.id.button_input_mode_forceTouch).setOnClickListener(this);
        findViewById(R.id.button_input_mode_auto).setOnClickListener(this);
        findViewById(R.id.button_input_mode_erase).setOnClickListener(this);

        findViewById(R.id.addImageBtn).setOnClickListener(this);
        findViewById(R.id.button_undo).setOnClickListener(this);
        findViewById(R.id.button_redo).setOnClickListener(this);
        findViewById(R.id.button_clear).setOnClickListener(this);
        findViewById(R.id.button_convert).setOnClickListener(this);
        findViewById(R.id.btnHome).setOnClickListener(this);
        prevBtn.setOnClickListener(this);
        nextBtn.setOnClickListener(this);
        colorsPalletBtn.setOnClickListener(this);
        pencilStrokesBtn.setOnClickListener(this);

        invalidateIconButtons();

        setOnVideoProcessingListener(this); // set Listener for video processing
    }


    @Override
    protected void onDestroy() {
        editorView.setOnTouchListener(null);
        editorView.close();

        documentController.close();
        // IInkApplication has the ownership, do not close here
        engine = null;
        super.onDestroy();
        try {
            destroyMediaProjection();
            getGlobalMethods().clearContentFromDirectory(VolleySingleton.getAppStorageTempDirectory());
        } catch (Exception e) {
            Log.e(RecordWhiteBoardActivity.class.getName(), e.getMessage().toString());
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        documentController.saveToTemp();
        outState.putInt(INPUT_MODE_KEY, editorView.getInputMode());
        super.onSaveInstanceState(outState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.adv_whiteboard_menu, menu);

        Editor editor = editorView.getEditor();
        boolean hasEditor = editor != null && !editor.isClosed();
        boolean hasPart = documentController.hasPart();
        int partIndex = documentController.getPartIndex();
        int partCount = documentController.getPartCount();

        MenuItem previousPartMenuItem = menu.findItem(R.id.menu_previousPart);
        previousPartMenuItem.setVisible(false);
        previousPartMenuItem.setEnabled(hasPart && partIndex - 1 >= 0);

        prevBtn.setVisibility(View.INVISIBLE);
        if (hasPart && partIndex - 1 >= 0)
            prevBtn.setVisibility(View.VISIBLE);

        MenuItem nextPartMenuItem = menu.findItem(R.id.menu_nextPart);
        nextPartMenuItem.setVisible(false);
        nextPartMenuItem.setEnabled(hasPart && partIndex + 1 < partCount);

        nextBtn.setVisibility(View.INVISIBLE);
        if (hasPart && partIndex + 1 < partCount)
            nextBtn.setVisibility(View.VISIBLE);

        MenuItem zoomInMenuItem = menu.findItem(R.id.menu_zoomIn);
        zoomInMenuItem.setEnabled(hasEditor && hasPart);
        MenuItem zoomOutMenuItem = menu.findItem(R.id.menu_zoomOut);
        zoomOutMenuItem.setEnabled(hasEditor && hasPart);
        MenuItem resetViewMenuItem = menu.findItem(R.id.menu_resetView);
        resetViewMenuItem.setEnabled(hasEditor && hasPart);
        MenuItem convertMenuItem = menu.findItem(R.id.menu_convert);
        convertMenuItem.setEnabled(hasPart);
        MenuItem exportMenuItem = menu.findItem(R.id.menu_export);
        exportMenuItem.setEnabled(hasEditor && hasPart);

        MenuItem saveMenuItem = menu.findItem(R.id.menu_savePackage);
        saveMenuItem.setVisible(false);
        MenuItem openMenuItem = menu.findItem(R.id.menu_openPackage);
        openMenuItem.setVisible(false);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Editor editor = editorView.getEditor();
        switch (item.getItemId()) {
            case R.id.menu_newPart:
                isNewPartBtnClicked = true;
                isPrevBtnClicked = false;
                isNextBtnClicked = false;
                return documentController.newPart();
            case R.id.menu_previousPart:
                isNewPartBtnClicked = false;
                isNextBtnClicked = false;
                isPrevBtnClicked = true;
                return documentController.previousPart();
            case R.id.menu_nextPart:
                isNextBtnClicked = true;
                isNewPartBtnClicked = false;
                isPrevBtnClicked = false;
                return documentController.nextPart();
            case R.id.menu_zoomIn:
                return documentController.zoomIn();
            case R.id.menu_zoomOut:
                return documentController.zoomOut();
            case R.id.menu_resetView:
                return documentController.resetView();
            case R.id.menu_convert:
                return documentController.convert(null);
            case R.id.menu_export:
                return documentController.export(null);
            case R.id.menu_newPackage:
                return documentController.newPackage();
            case R.id.menu_openPackage:
                return documentController.openPackage();
            case R.id.menu_savePackage:
                return documentController.savePackage();
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @SuppressLint("LongLogTag")
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_input_mode_forcePen:
                setInputMode(InputController.INPUT_MODE_FORCE_PEN);
                break;
            case R.id.button_input_mode_forceTouch:
                setInputMode(InputController.INPUT_MODE_FORCE_TOUCH);
                break;
            case R.id.button_input_mode_auto:
                setInputMode(InputController.INPUT_MODE_AUTO);
                break;
            case R.id.button_input_mode_erase:
                setInputMode(InputController.INPUT_MODE_ERASER);
                break;
            case R.id.button_undo:
                editorView.getEditor().undo();
                break;
            case R.id.button_redo:
                editorView.getEditor().redo();
                break;
            case R.id.button_clear:
                editorView.getEditor().clear();
                break;
            case R.id.button_convert:
                documentController.convert(null);
                break;
            case R.id.btnHome:
                isPrevBtnClicked = false;
                documentController.newPackage();
                break;
            case R.id.addImageBtn:
                xVal = 50;
                getTransformedXY();
                showAddContainerDialog();
                break;
            case R.id.prevBtn:
                isNewPartBtnClicked = false;
                isNextBtnClicked = false;
                isPrevBtnClicked = true;
                documentController.previousPart();
                break;
            case R.id.nextBtn:
                isNextBtnClicked = true;
                isNewPartBtnClicked = false;
                isPrevBtnClicked = false;
                documentController.nextPart();
                break;
            case R.id.colorsPalletBtn:
                showColorPicker();
                break;
            case R.id.pencilStrokesBtn:
                showPencilStrokePopup(v);
                break;
            default:
                Log.e(TAG, "Failed to handle click event");
                break;
        }
    }

    private void showPencilStrokePopup(View v){
                final SimpleTooltip tooltip = new SimpleTooltip.Builder(RecordAdvWhiteBoardActivity.this)
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
                        penStrokeSize = 0.5;
                        setThemeForEditor();
                        tooltip.dismiss();

                    }
                });
                mediumPencilSizeBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        penStrokeSize = 0.75;
                        setThemeForEditor();
                        tooltip.dismiss();

                    }
                });
                largePencilSizeBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        penStrokeSize = 1.0;
                        setThemeForEditor();
                        tooltip.dismiss();

                    }
                });
                tooltip.show();
    }
    private void showColorPicker() {
        ColorPicker colorPicker = new ColorPicker(RecordAdvWhiteBoardActivity.this);
        colorPicker.setOnChooseColorListener(new ColorPicker.OnChooseColorListener() {
            @Override
            public void onChooseColor(int position, int color) {
                // put code
                penStrokeColor = String.format("#%06X", (0xFFFFFF & color));
                setThemeForEditor();
            }

            @Override
            public void onCancel() {
                // put code
            }
        }).show();
    }

    private void setThemeForEditor() {
        editorView.getEditor().setPenStyle("greenThickPen");
        editorView.getEditor().setTheme("stroke { color: " + penStrokeColor + ";-myscript-pen-width: "+penStrokeSize+"}" +
                "glyph.math { color: " + penStrokeColor + "; }" +
                "glyph.math-solved {" +
                "  color: #1E9035FF;" +
                "  font-style: italic;" +
                "}");
    }

    @SuppressLint("LongLogTag")
    private boolean addImage(final float x, final float y) {
        final File[] files = getExternalStorageDirectory().listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return (name.endsWith(".gif") || name.endsWith(".png")
                        || name.endsWith(".svg") || name.endsWith(".jpg")
                        || name.endsWith(".jpeg") || name.endsWith(".jpe"));
            }
        });

        if (files.length == 0) {
            Log.e(TAG, "Failed to add image, image list is empty");
            return false;
        }

        String[] fileNames = new String[files.length];
        for (int i = 0; i < files.length; ++i)
            fileNames[i] = files[i].getName();

        final int[] selected = new int[]{0};
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        dialogBuilder.setTitle(R.string.addImage_title);

        dialogBuilder.setSingleChoiceItems(fileNames, selected[0], new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                selected[0] = which;
            }
        });

        dialogBuilder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                try {
                    File file = files[selected[0]];

                    if (file != null) {
                        MimeType mimeType = null;

                        for (MimeType mimeType_ : MimeType.values()) {
                            if (!mimeType_.isImage())
                                continue;

                            String fileExtensions = mimeType_.getFileExtensions();

                            if (fileExtensions == null)
                                continue;

                            String[] extensions = fileExtensions.split(" *, *");

                            for (int i = 0; i < extensions.length; ++i) {
                                if (file.getName().endsWith(extensions[i])) {
                                    mimeType = mimeType_;
                                    break;
                                }
                            }
                        }

                        if (mimeType != null) {
                            editorView.getEditor().addImage(x, y, file, mimeType);
                        }
                    }
                } catch (Exception e) {
                    Toast.makeText(RecordAdvWhiteBoardActivity.this, "Failed to add image\n" + e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });

        dialogBuilder.setNegativeButton(R.string.cancel, null);

        AlertDialog dialog = dialogBuilder.create();
        dialog.show();

        return true;
    }

    private final boolean addBlock(final float x, final float y, final String blockType) {
        final Editor editor = editorView.getEditor();
        final MimeType[] mimeTypes = editor.getSupportedAddBlockDataMimeTypes(blockType);

        if (mimeTypes.length == 0) {
            editor.addBlock(x, y, blockType);
            return true;
        }

        final ArrayList<String> typeDescriptions = new ArrayList<String>();

        for (MimeType mimeType : mimeTypes)
            typeDescriptions.add(mimeType.getTypeName());

        if (typeDescriptions.isEmpty())
            return false;

        final int[] selected = new int[]{0};
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);

        dialogBuilder.setTitle(R.string.addType_title);
        dialogBuilder.setSingleChoiceItems(typeDescriptions.toArray(new String[0]), selected[0], new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                selected[0] = which;
            }
        });
        dialogBuilder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                addBlock_(x, y, blockType, mimeTypes[selected[0]]);
            }
        });
        dialogBuilder.setNegativeButton(R.string.cancel, null);

        AlertDialog dialog = dialogBuilder.create();
        dialog.show();

        return true;
    }

    private final boolean addBlock_(final float x, final float y, final String blockType, final MimeType mimeType) {
        final EditText input = new EditText(this);

        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        input.setLayoutParams(params);
        input.setMinLines(5);
        input.setMaxLines(5);
        input.setGravity(Gravity.TOP | Gravity.LEFT);

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        dialogBuilder.setTitle(R.string.addData_title);
        dialogBuilder.setView(input);
        dialogBuilder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                try {
                    String data = input.getText().toString();
                    if (!data.isEmpty())
                        editorView.getEditor().addBlock(x, y, blockType, mimeType, data);
                } catch (Exception e) {
                    Toast.makeText(RecordAdvWhiteBoardActivity.this, "Failed to add block", Toast.LENGTH_LONG).show();
                }
            }
        });

        dialogBuilder.setNegativeButton(R.string.cancel, null);

        AlertDialog dialog = dialogBuilder.create();
        dialog.show();

        return true;
    }

    private void displayContextMenu(final float x, final float y, ContentBlock contentBlock_) {
        final Editor editor = editorView.getEditor();

        final ContentPart part = editor.getPart();
        if (part == null)
            return;

        final ContentBlock rootBlock = editor.getRootBlock();
        final ContentBlock contentBlock = (contentBlock_ != null) && !contentBlock_.getType().equals("Container") ? contentBlock_ : rootBlock;

        final boolean isRoot = contentBlock.getId().equals(rootBlock.getId());

        final boolean onRawContent = part.getType().equals("Raw Content");
        final boolean onTextDocument = part.getType().equals("Text Document");

        final boolean isEmpty = editor.isEmpty(contentBlock);

        final String[] supportedTypes = editor.getSupportedAddBlockTypes();
        final ConversionState[] supportedStates = editor.getSupportedTargetConversionStates(contentBlock);

        final boolean hasTypes = supportedTypes.length > 0;
        //final boolean hasExports = supportedExports.length > 0;
        //final boolean hasImports = supportedImports.length > 0;
        final boolean hasStates = supportedStates.length > 0;

        final boolean displayConvert = hasStates && !isEmpty;
        final boolean displayAddBlock = hasTypes && isRoot;
        final boolean displayAddImage = hasTypes && isRoot;//false; // hasTypes && isRoot;
        final boolean displayRemove = !isRoot;
        final boolean displayCopy = (onTextDocument ? !isRoot : !onRawContent);
        final boolean displayPaste = hasTypes && isRoot;
        final boolean displayImport = false; // hasImports;
        final boolean displayExport = false; // hasExports;

        final ArrayList<String> items = new ArrayList<>();

        if (displayAddBlock) {
            for (String blockType : supportedTypes)
                items.add("Add " + blockType);
        }


        if (displayAddImage)
            items.add("Add Image");

        if (displayRemove)
            items.add("Remove");

        if (displayConvert)
            items.add("Convert");

        if (displayCopy)
            items.add("Copy");

        if (displayPaste)
            items.add("Paste");

        if (displayImport)
            items.add("Import");

        if (displayExport)
            items.add("Export");

        if (items.isEmpty())
            return;

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        dialogBuilder.setTitle(contentBlock.getType() + " (id: " + contentBlock.getId() + ")");
        dialogBuilder.setItems(items.toArray(new String[items.size()]), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                try {
                    String item = items.get(which);

                    if (item.equals("Convert")) {
                        editor.convert(contentBlock, supportedStates[0]);
                    } else if (item.equals("Remove")) {
                        editor.removeBlock(contentBlock);
                    } else if (item.equals("Copy")) {
                        editor.copy(contentBlock);
                    } else if (item.equals("Paste")) {
                        editor.paste(x, y);
                    } else if (item.equals("Import")) {
                        // TODO
                    } else if (item.equals("Export")) {
                        // TODO
                    } else if (item.equals("Add Image")) {
                        //addImage(x, y);
                        if(isRecordingRunning && !isPaused){
                            pauseBtnOut.callOnClick();
                        }
                        xVal = x;
                        yVal = y;
                        Intent i = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                        startActivityForResult(i, 99);
                    } else if (item.startsWith("Add ")) {
                        String blockType = item.substring(4);
                        addBlock(x, y, blockType);
                    }
                } catch (Exception e) {
                    Toast.makeText(RecordAdvWhiteBoardActivity.this, "Operation failed : " + e.getMessage(), Toast.LENGTH_LONG).show();
                }
                dialog.dismiss();
            }
        });
        dialogBuilder.show();
    }

    private void getTransformedXY() {
        final Editor editor = editorView.getEditor();

        final ContentPart part = editor.getPart();
        if (part == null)
            return;

        final ContentBlock rootBlock = editor.getRootBlock();

        float subblocksYMax = 0;

        float height = 0;

        for (int i = 0; i < rootBlock.getChildren().length; i++) {
            float blockY = rootBlock.getChildren()[i].getBox().y;

            if (subblocksYMax < blockY) {
                subblocksYMax = blockY;
                height = rootBlock.getChildren()[i].getBox().height;
            }
        }

        float ymax = subblocksYMax + height;

        Transform transform = editor.getRenderer().getViewTransform();
        Point point = transform.apply(xVal, ymax + 5);
        xVal = point.x;
        yVal = point.y;
    }

    private void showAddContainerDialog() {
        final String[] items = new String[]{"Image", "Drawing", "Diagram", "Math", "Text"};
        final Integer[] icons = new Integer[]{R.drawable.add_image_gray_86, R.drawable.draw_86, R.drawable.diagram_86, R.drawable.math_86, R.drawable.text_86};
        ListAdapter adapter = new ArrayAdapterWithIcon(RecordAdvWhiteBoardActivity.this, items, icons);

        new AlertDialog.Builder(RecordAdvWhiteBoardActivity.this).setTitle("Add Content")
                .setAdapter(adapter, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int item) {
//                        Toast.makeText(RecordAdvWhiteBoardActivity.this, "Item Selected: " + item, Toast.LENGTH_SHORT).show();
                        switch (items[item]) {
                            case "Image":
                                if(isRecordingRunning && !isPaused){
                                    pauseBtnOut.callOnClick();
                                }
                                Intent i = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                                startActivityForResult(i, 99);
                                break;
                            default:
                                addBlock(xVal, yVal, items[item]);
                        }
                    }
                }).show();
    }

    private void setInputMode(int inputMode) {
        editorView.setInputMode(inputMode);
        findViewById(R.id.button_input_mode_forcePen).setEnabled(inputMode != InputController.INPUT_MODE_FORCE_PEN);
        findViewById(R.id.button_input_mode_forceTouch).setEnabled(inputMode != InputController.INPUT_MODE_FORCE_TOUCH);
        findViewById(R.id.button_input_mode_auto).setEnabled(inputMode != InputController.INPUT_MODE_AUTO);
        findViewById(R.id.button_input_mode_erase).setEnabled(inputMode != InputController.INPUT_MODE_ERASER);
        invalidateIconButtons();
    }

    private void invalidateIconButtons() {
        final Editor editor = editorView.getEditor();
        final boolean canUndo = editor.canUndo();
        final boolean canRedo = editor.canRedo();
        final int editorInputMode = editorView.getInputMode();
        final ContentPart part = editor.getPart();

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Button imageButtonUndo = (Button) findViewById(R.id.button_undo);
                imageButtonUndo.setEnabled(canUndo);
                Button imageButtonRedo = (Button) findViewById(R.id.button_redo);
                imageButtonRedo.setEnabled(canRedo);
                Button imageButtonClear = (Button) findViewById(R.id.button_clear);
                imageButtonClear.setEnabled(documentController != null && documentController.hasPart());
                Button addImageBtn = (Button) findViewById(R.id.addImageBtn);
                addImageBtn.setVisibility(View.GONE);

                if (part != null && part.getType().equals("Text Document")) {
                    addImageBtn.setVisibility(View.VISIBLE);
                }

            }
        });
    }

    /*************************************************************************************************************************************************************/

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
            if (sessionManager.getSessionAppIsScreenCastActive()) {
                recordTime = 0;
                startScreenCastRecording();
                return;
            }
            startRecording();
        }
    }

    private void startRecording() {
        if (ContextCompat.checkSelfPermission(RecordAdvWhiteBoardActivity.this,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE) + ContextCompat
                .checkSelfPermission(RecordAdvWhiteBoardActivity.this,
                        android.Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale
                    (RecordAdvWhiteBoardActivity.this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) ||
                    ActivityCompat.shouldShowRequestPermissionRationale
                            (RecordAdvWhiteBoardActivity.this, android.Manifest.permission.RECORD_AUDIO)) {
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
                                ActivityCompat.requestPermissions(RecordAdvWhiteBoardActivity.this,
                                        new String[]{android.Manifest.permission
                                                .WRITE_EXTERNAL_STORAGE, android.Manifest.permission.RECORD_AUDIO},
                                        REQUEST_PERMISSIONS);
                            }
                        }).show();
            } else {
                ActivityCompat.requestPermissions(RecordAdvWhiteBoardActivity.this,
                        new String[]{android.Manifest.permission
                                .WRITE_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO},
                        REQUEST_PERMISSIONS);
            }
        } else {
            onToggleScreenShare(true);
        }
    }

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
                //buildNotification("Video Processing Done...", fileData);
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
            Toast.makeText(RecordAdvWhiteBoardActivity.this, "Whiteboard recording is not done successfully.", Toast.LENGTH_SHORT);
            //buildNotification("Video Processing Failed...", fileData);
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

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
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
        return mMediaProjection.createVirtualDisplay("RecordAdvWhiteBoardActivity",
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
            mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
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
            Crashlytics.logException(e);
            e.printStackTrace();
            Log.e(RecordAdvWhiteBoardActivity.class.getName(), e.getMessage().toString());
            showToast("Some Devices are busy try again");
            finish();
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

    private void setListners() {
        stopBtn.setVisibility(View.GONE);
        resumeBtnOut.setVisibility(View.GONE);
        pauseBtnOut.setVisibility(View.GONE);
        stopBtnOut.setVisibility(View.GONE);

        startBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                TypedValue outValue = new TypedValue();
                RecordAdvWhiteBoardActivity.this.getTheme().resolveAttribute(android.R.attr.selectableItemBackground, outValue, true);
                startBtn.setBackgroundResource(outValue.resourceId);
                view.setVisibility(View.GONE);
                countDownTimerText.setVisibility(View.VISIBLE);
                showHideBtn.callOnClick();
                myCountDownTimer.start();
                getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide navigation bar
                        | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);

                if (sessionManager.getSessionVibratePhoneState()) {
                    globalMethods.vibratePhone(RecordAdvWhiteBoardActivity.this, 500);
                }

            }
        });

        resumeBtnOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {



                /*if (isChromebook()) {
                    pauseBtnOut.setVisibility(View.GONE);
                } else {
                    pauseBtnOut.setVisibility(View.VISIBLE);
                }*/
                // resumeBtnOut.setVisibility(View.GONE);
                if (isChromebook()) {
                    pauseBtnOut.setVisibility(View.GONE);
                } else {
                    pauseBtnOut.setVisibility(View.VISIBLE);
                }
                resumeBtnOut.setVisibility(View.GONE);

                isResumed = true;
                isPaused = false;
//                resumeScreenSharing();

                recordTime = tempRecordTime;
                //handler.post(UpdateRecordTime);
                if (sessionManager.getSessionAppIsScreenCastActive()) {
                    startScreenCastRecording();
                } else {
                    resumeScreenSharing();
                }

            }
        });

        pauseBtnOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                pauseBtnOut.setVisibility(View.GONE);
                resumeBtnOut.setVisibility(View.VISIBLE);
               /* if (isChromebook()) {
                    resumeBtnOut.setVisibility(View.GONE);
                } else {
                    resumeBtnOut.setVisibility(View.VISIBLE);
                }*/
                isResumed = false;
                isPaused = true;
                isToolBarHidden = true;
                showHideToolbar();
                tempRecordTime = recordTime;

                if (sessionManager.getSessionAppIsScreenCastActive()) {
                    stopScreenCastRecording();
                } else {
                    pauseScreenSharing();
                    handler.removeCallbacks(UpdateRecordTime);
                }
            }
        });

        stopBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isRecordingRunning = false;
                if (isResumed) {
                    if (sessionManager.getSessionAppIsScreenCastActive()) {
                        stopScreenCastRecording();
                    } else {
                        onToggleScreenShare(false);
                    }
                } else if (isPaused) {
                    if (sessionManager.getSessionAppIsScreenCastActive()) {
                        stopScreenCastRecording();
                    } else {
                        destroyMediaProjection();
                    }
                    setResult(REQUEST_CODE);
                    finish();
                    showToast("Video recorded successfully.", Toast.LENGTH_SHORT);
                    recordTime = 0;
                    currentTimeText.setVisibility(View.GONE);
                    startActivity(new Intent(RecordAdvWhiteBoardActivity.this, MainActivity.class));
                } else {
                    if (sessionManager.getSessionAppIsScreenCastActive()) {
                        stopScreenCastRecording();
                    } else {
                        onToggleScreenShare(false);
                    }
                    setResult(REQUEST_CODE);
                    finish();
                    showToast("Video recorded successfully.", Toast.LENGTH_SHORT);
                    recordTime = 0;
                    currentTimeText.setVisibility(View.GONE);
                    startActivity(new Intent(RecordAdvWhiteBoardActivity.this, MainActivity.class));
                }
            }
        });

        stopBtnOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                TypedValue outValue = new TypedValue();
                RecordAdvWhiteBoardActivity.this.getTheme().resolveAttribute(android.R.attr.selectableItemBackground, outValue, true);
                stopBtnOut.setBackgroundResource(outValue.resourceId);
                isRecordingRunning = false;
                if (isResumed) {
                    if (sessionManager.getSessionAppIsScreenCastActive()) {
                        stopScreenCastRecording();
                    } else {
                        onToggleScreenShare(false);
                    }
                } else if (isPaused) {
                    if (sessionManager.getSessionAppIsScreenCastActive()) {
                        stopScreenCastRecording();
                    } else {
                        destroyMediaProjection();
                    }
                    setResult(REQUEST_CODE);
                    finish();
                    showToast("Video recorded successfully.", Toast.LENGTH_SHORT);
                    recordTime = 0;
                    currentTimeText.setVisibility(View.GONE);
                    startActivity(new Intent(RecordAdvWhiteBoardActivity.this, MainActivity.class));
                } else {//ok
                    if (sessionManager.getSessionAppIsScreenCastActive()) {
                        stopScreenCastRecording();
                    } else {
                        onToggleScreenShare(false);
                    }
                    setResult(REQUEST_CODE);
                    finish();
                    showToast("Video recorded successfully.", Toast.LENGTH_SHORT);
                    recordTime = 0;
                    currentTimeText.setVisibility(View.GONE);
                    startActivity(new Intent(RecordAdvWhiteBoardActivity.this, MainActivity.class));
                }
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
                    decorView.setSystemUiVisibility(0);
                    isFullScreen = false;
                    fullscreenBtn.setBackgroundResource(R.drawable.fullscreen);
                }

            }
        });

        showHideBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showHideToolbar();
            }
        });
    }

    private void showHideToolbar() {
        if (isToolBarHidden) {
            showHideBtn.setImageDrawable(ContextCompat.getDrawable(RecordAdvWhiteBoardActivity.this, R.drawable.downward_grey));
            leftButtonLayout.setVisibility(View.VISIBLE);
             isToolBarHidden = false;
        } else {
            showHideBtn.setImageDrawable(ContextCompat.getDrawable(RecordAdvWhiteBoardActivity.this, R.drawable.upward_grey));
            leftButtonLayout.setVisibility(View.GONE);
            isToolBarHidden = true;
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
            case 99:
                if (resultCode == RESULT_OK && null != intent) {
                    Uri selectedImage = intent.getData();
                    String[] filePathColumn = {MediaStore.Images.Media.DATA};
                    Cursor cursor = getContentResolver().query(selectedImage, filePathColumn, null, null, null);
                    cursor.moveToFirst();
                    int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                    String picturePath = cursor.getString(columnIndex);
                    //showToast(picturePath);

                    try {
                        //File file = files[selected[0]];
                        File file = new File(picturePath);
                        if (file != null) {
                            MimeType mimeType = null;

                            for (MimeType mimeType_ : MimeType.values()) {
                                if (!mimeType_.isImage())
                                    continue;

                                String fileExtensions = mimeType_.getFileExtensions();

                                if (fileExtensions == null)
                                    continue;

                                String[] extensions = fileExtensions.split(" *, *");

                                for (int i = 0; i < extensions.length; ++i) {
                                    if (file.getName().endsWith(extensions[i])) {
                                        mimeType = mimeType_;
                                        break;
                                    }
                                }
                            }

                            if (mimeType != null) {
                                editorView.getEditor().addImage(xVal, yVal, file, mimeType);
                            }
                        }
                    } catch (Exception e) {
                        Toast.makeText(RecordAdvWhiteBoardActivity.this, "Failed to add image\n" + e.getMessage(), Toast.LENGTH_LONG).show();
                    }finally {
                        if(isRecordingRunning && isPaused){
                            resumeBtnOut.callOnClick();
                        }
                    }
                }
                break;

        }
    }

    @Override
    public void onBackPressed() {//Ewww
        if (isRecordingRunning) {
            if (!isPaused) {
                pauseBtnOut.callOnClick();
            }
            final boolean[] noBtnClicked = {false};
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(RecordAdvWhiteBoardActivity.this);
            alertDialog.setTitle("Confirmation");
            alertDialog.setMessage("Do you want to stop and save?");
            alertDialog.setIcon(R.drawable.app_logo_round_new24);
            alertDialog.setPositiveButton("yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    isRecordingRunning = false;
                    if (sessionManager.getSessionAppIsScreenCastActive()) {
                        stopScreenCastRecording();
                    } else {
                        destroyMediaProjection();
                    }
//                    destroyMediaProjection();

                    setResult(REQUEST_CODE);
                    finish();
                    showToast("Video recorded successfully.", Toast.LENGTH_SHORT);
                    recordTime = 0;
                    currentTimeText.setVisibility(View.GONE);
                    startActivity(new Intent(RecordAdvWhiteBoardActivity.this, MainActivity.class));
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

            AlertDialog.Builder alertDialog = new AlertDialog.Builder(RecordAdvWhiteBoardActivity.this);
            alertDialog.setTitle("Confirmation");
            alertDialog.setMessage("Do you want to stop and save?");
            alertDialog.setIcon(R.drawable.app_logo_round_new24);
            alertDialog.setPositiveButton("yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    isRecordingRunning = false;
                    if (sessionManager.getSessionAppIsScreenCastActive()) {
                        stopScreenCastRecording();
                    } else {
                        destroyMediaProjection();
                    }
//                    destroyMediaProjection();
                    setResult(REQUEST_CODE);
                    finish();
                    showToast("Video recorded successfully.", Toast.LENGTH_SHORT);
                    recordTime = 0;
                    currentTimeText.setVisibility(View.GONE);
                    startActivity(new Intent(RecordAdvWhiteBoardActivity.this, MainActivity.class));
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

        /*AlertDialog.Builder alertDialog = new AlertDialog.Builder(RecordAdvWhiteBoardActivity.this);
        alertDialog.setTitle("Confirmation");
        alertDialog.setMessage("Do you want to stop and save?");
        alertDialog.setIcon(R.drawable.app_logo_round_new24);
        alertDialog.setPositiveButton("yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //if (isRecordingRunning)
                //{    onToggleScreenShare(false);    }
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
                } else {//ok
                    onToggleScreenShare(false);
                    setResult(REQUEST_CODE);
                    finish();
                    showToast("Video recorded successfully.", Toast.LENGTH_SHORT);
                    recordTime = 0;
                    currentTimeText.setVisibility(View.GONE);
                }
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
        startActivity(new Intent(RecordAdvWhiteBoardActivity.this, MainActivity.class));
    }

    @Override
    public void onVideoProcessFailed() {
        setResult(REQUEST_CODE);
        finish();
        showToast("Recording process is failed. Please try again later", Toast.LENGTH_SHORT);
    }

    //20 Feb 2020 - rajk

    private int frameCounter;
    private String screenCastRecordOutputFileName = "";

    private void MediaRecorderReadyForAudioRecording() {
        mMediaRecorder = new MediaRecorder();
        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mMediaRecorder.setAudioEncoder(MediaRecorder.OutputFormat.AMR_NB);
        mMediaRecorder.setOutputFile(AudioSavePathInDevice);
    }

    @SuppressLint("LongLogTag")
    private void startScreenCastRecording() {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        DEFAULT_HEIGHT = displayMetrics.heightPixels;
        DEFAULT_WIDTH = displayMetrics.widthPixels;

        screenCastRecordOutputFileName = "CB_" + new Date().getTime();
        mEncoderConfig = new HevcEncoderConfig(new File(Environment.getExternalStorageDirectory(), getResources().getString(R.string.app_name) + "/" + screenCastRecordOutputFileName + "_Video.mp4").getAbsolutePath(), DISPLAY_WIDTH, DISPLAY_HEIGHT, DEFAULT_FRAME_RATE_PER_SECOND, DEFAULT_BIT_RATE);
        AudioSavePathInDevice = Environment.getExternalStorageDirectory() + "/" + getResources().getString(R.string.app_name) + "/" + screenCastRecordOutputFileName + "_Audio.3gp";
        MediaRecorderReadyForAudioRecording();
        frameEncoder = new FrameEncoder(mEncoderConfig);
        isRecordingRunning = true;
        pauseBtnOut.setVisibility(View.VISIBLE);
        try {
            mMediaRecorder.prepare();
            mMediaRecorder.start();
            frameEncoder.start();
            handler.post(UpdateRecordTimeForScreenCastRecording);
        } catch (IllegalStateException e) {
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
        } catch (Exception e) {
            mMediaRecorder.reset();
            e.printStackTrace();
        }
    }

    private void closeScreenCastRecording() {
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
//                Log.e(TAG, "" + e.getMessage().toString());
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

                frameEncoder.createFrame(Screenshots.takeScreenshotOfRootView(editorView));

//                WhiteBoardPageFragment temp = (WhiteBoardPageFragment) whiteBoardViewPagerAdapter.getFragmentsList().get(whiteBoardViewPager.getCurrentItem());
//                frameEncoder.createFrame(temp.takeScreenshotOfRootView());
                currentTimeText.setVisibility(View.VISIBLE);
                currentTimeText.setText(str);
                frameCounter += 1;
                recordTime = (frameCounter / (int) DEFAULT_FRAME_RATE_PER_SECOND);
                // Delay 1s before next call
                handler.postDelayed(UpdateRecordTimeForScreenCastRecording, 200);
            }
        }
    };

}
