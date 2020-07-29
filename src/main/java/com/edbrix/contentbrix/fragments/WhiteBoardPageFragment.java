package com.edbrix.contentbrix.fragments;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import androidx.fragment.app.Fragment;
import android.text.Html;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.edbrix.contentbrix.R;
import com.edbrix.contentbrix.customview.DrawingOnImageView;

import com.edbrix.contentbrix.customview.ShapesDrawingView;
import com.edbrix.contentbrix.utils.Screenshots;
import com.rajasharan.layout.RearrangeableLayout;

import java.io.IOException;
import java.io.InputStream;

import edbrix.image.gestures.MoveGestureDetector;
import edbrix.image.gestures.RotateGestureDetector;

/**
 * Created by rajk on 31/07/17.
 */

public class WhiteBoardPageFragment extends Fragment implements View.OnTouchListener {

    private DrawingOnImageView drawingView;
    private ShapesDrawingView shapesDrawingView;

   // private MathWidgetApi mWidget;

    private ImageView img_scale;
    private ImageView backTextureImgView;
    private FrameLayout imageContainer;
    private FrameLayout frame_layout;
    //
    private float mScaleFactor = 0.5f;
    private float mRotationDegree = 0.f;
    private float mFocusX = 0.f;
    private float mFocusY = 0.f;
    private int mScreenHeight;
    private int mScreenWidth;
    private Matrix matrix = new Matrix();//Các lớp Matrix giữ một ma trận 3x3 để di chuyển tọa độ.
    private int mImageWidth, mImageHeight;
    private ScaleGestureDetector mScaleDetector;
    private RotateGestureDetector mRotateDetector;
    private MoveGestureDetector mMoveDetector;
    private Bitmap loadTempImg;
    private SeekBar sizeBar;
    private TextView scannedText;
    private RearrangeableLayout rearrangeableLayout;
    private int childIndex = 0;
    private int fragmentId;
    //

    public static WhiteBoardPageFragment getInstance() {
        WhiteBoardPageFragment whiteBoardPageFragment = new WhiteBoardPageFragment();
//        Bundle args = new Bundle();
//        args.putParcelable("image_source", imageResource);
//        imagePageFragment.setArguments(args);
        return whiteBoardPageFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_white_board_page, container, false);
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        drawingView = (DrawingOnImageView) view.findViewById(R.id.drawingView);
        shapesDrawingView = (ShapesDrawingView) view.findViewById(R.id.shapeDrawView);
        frame_layout = (FrameLayout) view.findViewById(R.id.frame_layout);
        img_scale = (ImageView) view.findViewById(R.id.img_scale);
        backTextureImgView = (ImageView) view.findViewById(R.id.backTextureImgView);
        imageContainer = (FrameLayout) view.findViewById(R.id.imageContainer);

        rearrangeableLayout = (RearrangeableLayout) view.findViewById(R.id.rearrangeableLayout);
        sizeBar = (SeekBar) view.findViewById(R.id.seekBarSize);
        scannedText = (TextView) view.findViewById(R.id.scannedText);
        sizeBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int size, boolean b) {
                scannedText.setTextSize(size);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        childPositionListener();
        preDrawListener();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (loadTempImg != null) {
            loadTempImg.recycle();
            loadTempImg = null;
        }

    }

    public int getFragmentId(){
        Log.e(WhiteBoardPageFragment.class.getName(),"getFragmentId "+this.fragmentId);
        return this.fragmentId;
    }

    public void setFragmentId(int fragmentId){
        this.fragmentId = fragmentId;
        Log.e(WhiteBoardPageFragment.class.getName(),"setFragmentId "+this.fragmentId);
    }

    public void setImageForScale(Uri imageUri) {
        try {
            //
            img_scale.setOnTouchListener(this);
            //// Get the screen size in pixels.
            DisplayMetrics displayMetrics = new DisplayMetrics();
            getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
            mScreenHeight = displayMetrics.heightPixels;
            mScreenWidth = displayMetrics.widthPixels;

            //load anh mau
//        Bitmap loadTempImg = BitmapFactory.decodeResource(getResources(), R.mipmap.app_logo_round);
            loadTempImg = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), imageUri);
            mImageHeight = loadTempImg.getHeight();
            mImageWidth = loadTempImg.getWidth();
            img_scale.setImageBitmap(loadTempImg);
            //view anh thu nho lai boi ma tran so voi anh goc //He looks like he's scared of being compared to him
            matrix.postScale(mScaleFactor, mScaleFactor);
            img_scale.setImageMatrix(matrix);

            // Set Detectors Gesture
            mScaleDetector = new ScaleGestureDetector(getContext(), new ScaleListener());
            mRotateDetector = new RotateGestureDetector(getContext(), new RotateListener());
            mMoveDetector = new MoveGestureDetector(getContext(), new MoveListener());

            enableImageMoving(true);
        } catch (IOException e) {
            Crashlytics.logException(e);
            Toast.makeText(getContext(), "IOException error :" + e.getMessage(), Toast.LENGTH_SHORT).show();
        } catch (OutOfMemoryError e) {
            Crashlytics.logException(e);
            Toast.makeText(getContext(), "OutOfMemoryError error :" + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    public Bitmap getResizedBitmap(Bitmap image, int maxSize) {
        int width = image.getWidth();
        int height = image.getHeight();

        float bitmapRatio = (float)width / (float) height;
        if (bitmapRatio > 1) {
            width = maxSize;
            height = (int) (width / bitmapRatio);
        } else {
            height = maxSize;
            width = (int) (height * bitmapRatio);
        }
        return Bitmap.createScaledBitmap(image, width, height, true);
    }

    public void addImageViewInContainer(Uri imageUri)
    {
        try {
            Bitmap bitmapImg;
            ImageView imageView = new ImageView(getContext());
            imageView.setLayoutParams(
                    new ViewGroup.LayoutParams(
                            // or ViewGroup.LayoutParams.WRAP_CONTENT
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            // or ViewGroup.LayoutParams.WRAP_CONTENT,
                            ViewGroup.LayoutParams.MATCH_PARENT));
            imageView.setScaleType(ImageView.ScaleType.MATRIX);

            imageContainer.addView(imageView);

            imageView.setOnTouchListener(this);

            //// Get the screen size in pixels.
            DisplayMetrics displayMetrics = new DisplayMetrics();
            getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
            mScreenHeight = displayMetrics.heightPixels;
            mScreenWidth = displayMetrics.widthPixels;

            //bitmapImg = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), imageUri);

            InputStream imageStream = getActivity().getContentResolver().openInputStream(imageUri);
            Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
            if(selectedImage !=null) {
                selectedImage = getResizedBitmap(selectedImage, 800);


            /*mImageHeight = bitmapImg.getHeight();
            mImageWidth = bitmapImg.getWidth();
            imageView.setImageBitmap(bitmapImg);*/

                mImageHeight = selectedImage.getHeight();
                mImageWidth = selectedImage.getWidth();
                imageView.setImageBitmap(selectedImage);

                //view anh thu nho lai boi ma tran so voi anh goc //He looks like he's scared of being compared to him
                matrix.postScale(mScaleFactor, mScaleFactor);
                imageView.setImageMatrix(matrix);

                // Set Detectors Gesture
                mScaleDetector = new ScaleGestureDetector(getContext(), new ScaleListener());
                mRotateDetector = new RotateGestureDetector(getContext(), new RotateListener());
                mMoveDetector = new MoveGestureDetector(getContext(), new MoveListener());

                enableImageMoving(true);
            }
        } catch (IOException e) {
            Crashlytics.logException(e);
            Toast.makeText(getContext(), "IOException error :" + e.getMessage(), Toast.LENGTH_SHORT).show();
        } catch (OutOfMemoryError e) {
            Crashlytics.logException(e);
            Toast.makeText(getContext(), "OutOfMemoryError error :" + e.getMessage(), Toast.LENGTH_SHORT).show();
        }catch (Exception e){
            Crashlytics.logException(e);
            e.printStackTrace();
        }
    }

    public void addScannedText(String textString) {
        drawingView.setVisibility(View.GONE);
        sizeBar.setVisibility(View.VISIBLE);
        scannedText.setVisibility(View.VISIBLE);
        scannedText.setText(textString);
    }

    public void addDraggableText(String textString, int textStyle, int textSize, boolean isUnderlined) {
        drawingView.setVisibility(View.GONE);
        shapesDrawingView.setVisibility(View.GONE);
        rearrangeableLayout.setVisibility(View.VISIBLE);

        if (childIndex < rearrangeableLayout.getChildCount()) {
            TextView draggableTextView = (TextView) rearrangeableLayout.getChildAt(childIndex);

            draggableTextView.setTypeface(null, textStyle);
            draggableTextView.setTextSize(textSize);
            draggableTextView.setText(textString);
            if (isUnderlined) {
                draggableTextView.setText(Html.fromHtml("<u>" + textString + "</u>"));
            }
            rearrangeableLayout.removeViewAt(childIndex);
            rearrangeableLayout.addView(draggableTextView, childIndex);
            childIndex++;
        }
    }

    public void setEraserActive(boolean isActive) {
        if (isActive) {
            drawingView.activateEraser();
        } else {
            drawingView.deactivateEraser();
        }
    }

    public void enableImageMoving(boolean enable) {
        if (enable) {
            drawingView.setVisibility(View.INVISIBLE);
            shapesDrawingView.setVisibility(View.INVISIBLE);
        } else {
            drawingView.setVisibility(View.VISIBLE);
            shapesDrawingView.setVisibility(View.VISIBLE);
        }
    }

    public void enableShapeDrawingView(boolean enable){
        if(enable){
            drawingView.setVisibility(View.GONE);
            shapesDrawingView.setVisibility(View.VISIBLE);
        }else{
            drawingView.setVisibility(View.VISIBLE);
        }
    }

    public void setShapeDrawingId(int shapeDrawingId){
        shapesDrawingView.setSHAPE_ID(shapeDrawingId);
    }

    public void setBackTextureImgView(Drawable imageDrawable) {
        backTextureImgView.setVisibility(View.GONE);
        backTextureImgView.setImageDrawable(imageDrawable);

        if (imageDrawable != null)
            backTextureImgView.setVisibility(View.VISIBLE);
    }

    public void setPenColorForDrawing(int color) {
        drawingView.deactivateEraser();
        drawingView.setPenColor(color);
    }

    public void setPenStrokeForDrawing(int size) {
        drawingView.deactivateEraser();
        drawingView.setPenStroke(size);
        Log.e("123","Stroke Size"+size);
    }

    public void clearDrawing() {//009
        if (drawingView != null) {
            drawingView.deactivateEraser();
            drawingView.reset();
            shapesDrawingView.reset();
            drawingView.setVisibility(View.VISIBLE);
            img_scale.setImageBitmap(null);
        }  if(imageContainer != null){
            clearGraphics();
        }
    }

    public void clearGraphics() {
        imageContainer.removeAllViews();
    }

    public void undoDrawing() {
        try {
            if (drawingView != null) {
                drawingView.onClickUndo();
            }
        } catch (Exception e) {
            Crashlytics.logException(e);
            e.printStackTrace();
        }
    }

    public void redoDrawing() {
        try {
            if (drawingView != null){
                drawingView.onClickRedo();
            }
        } catch (Exception e) {
            Crashlytics.logException(e);

        }
    }

    public Bitmap takeScreenshotView(){
        try {
            return Screenshots.takeScreenshot(frame_layout);
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }

    }

    public Bitmap takeScreenshotOfRootView(){
        try {
            return Screenshots.takeScreenshotOfRootView(frame_layout);
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }

    }
    /*public void setShapeRecognationViewVisibility(boolean isActive) {
        if (isActive) {
            drawingView.setVisibility(View.GONE);
            DiagramWidgetRelativeLayout.setVisibility(View.VISIBLE);
            MathWidgetRelativeLayout.setVisibility(View.GONE);
            Log.e("123", "setShapeRecognationViewForDrawing : " + isActive +" "+DiagramWidgetRelativeLayout.getVisibility()+""+drawingView.getVisibility());
        }else {
            drawingView.setVisibility(View.VISIBLE);
            DiagramWidgetRelativeLayout.setVisibility(View.GONE);
            MathWidgetRelativeLayout.setVisibility(View.GONE);
            Log.e("123", "setShapeRecognationViewForDrawing : " + isActive +" "+DiagramWidgetRelativeLayout.getVisibility()+""+drawingView.getVisibility());
        }
    }

    public void setMathRecognationViewVisibility(boolean isActive) {
        if (isActive) {
            drawingView.setVisibility(View.GONE);
            DiagramWidgetRelativeLayout.setVisibility(View.GONE);
            MathWidgetRelativeLayout.setVisibility(View.VISIBLE);
            Log.e("123", "setMathRecognationViewVisibility : " + isActive +" "+DiagramWidgetRelativeLayout.getVisibility()+""+drawingView.getVisibility());
        }else {
            drawingView.setVisibility(View.VISIBLE);
            DiagramWidgetRelativeLayout.setVisibility(View.GONE);
            MathWidgetRelativeLayout.setVisibility(View.GONE);
            Log.e("123", "setMathRecognationViewVisibility : " + isActive +" "+DiagramWidgetRelativeLayout.getVisibility()+""+drawingView.getVisibility());
        }
    }*/

    //
    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            mScaleFactor *= detector.getScaleFactor();
            mScaleFactor = Math.max(0.1f, Math.min(mScaleFactor, 4.0f));
            return true;
        }
    }

    private class RotateListener extends RotateGestureDetector.SimpleOnRotateGestureListener {
        @Override
        public boolean onRotate(RotateGestureDetector detector) {
            mRotationDegree -= detector.getRotationDegreesDelta();
            return true;
        }
    }

    private class MoveListener extends MoveGestureDetector.SimpleOnMoveGestureListener {
        @Override
        public boolean onMove(MoveGestureDetector detector) {
            PointF d = detector.getFocusDelta();
            mFocusX += d.x;
            mFocusY += d.y;

            return true;
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        mScaleDetector.onTouchEvent(event);
        mRotateDetector.onTouchEvent(event);
        mMoveDetector.onTouchEvent(event);
        float scaleImageCenterX = (mImageWidth * mScaleFactor) / 2;
        float scaleImageCenterY = (mImageHeight * mScaleFactor) / 2;

        matrix.reset();//Set up the matrix to calculate
        matrix.postScale(mScaleFactor, mScaleFactor);//The post concatens the matrix with the specified scale.
        matrix.postRotate(mRotationDegree, scaleImageCenterX, scaleImageCenterY);//Postconcats matrix with specified rotation.
        matrix.postTranslate(mFocusX - scaleImageCenterX, mFocusY - scaleImageCenterY);//Postconcats matrix with translation provisions.

        ImageView view = (ImageView) v;
        view.setImageMatrix(matrix);
        return true;
    }
    public void childPositionListener() {

        rearrangeableLayout.setChildPositionListener(new RearrangeableLayout.ChildPositionListener() {

            @Override
            public void onChildMoved(View childView, Rect oldPosition, Rect newPosition) {

                Log.e("WhiteBoardFrg", childView.toString());
                Log.e("WhiteBoardFrg", oldPosition.toString() + "-" + newPosition.toString());
            }
        });
    }

    /*
      Added a PreviewListener to the root layout to receive update during
      child view is dragging*/

    public void preDrawListener() {

        rearrangeableLayout.getViewTreeObserver()
                .addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {

                    @Override
                    public boolean onPreDraw() {

                        Log.e("WhiteBoardFrg", "onPrepreview&quot");
                        Log.e("WhiteBoardFrg", rearrangeableLayout.toString());
                        return true;
                    }
                });
    }
}
