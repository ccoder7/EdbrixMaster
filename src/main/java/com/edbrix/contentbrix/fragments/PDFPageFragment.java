package com.edbrix.contentbrix.fragments;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.davemorrissey.labs.subscaleview.ImageSource;
import com.edbrix.contentbrix.R;
import com.edbrix.contentbrix.customview.DrawingOnImageView;
import com.edbrix.contentbrix.utils.Screenshots;
import com.vlk.multimager.utils.Image;

import edbrix.image.gestures.MoveGestureDetector;
import edbrix.image.gestures.RotateGestureDetector;

/**
 * Created by rajk on 31/07/17.
 */

public class PDFPageFragment extends Fragment implements View.OnTouchListener {

    private Image imageResource;
    private ImageView img_scale;
    private String tempFolderPath;
    private int mScreenHeight;
    private int mScreenWidth;
    private Bitmap pdfTmpBitmap;
    private FrameLayout frame_layout;
    public static String TAG = PDFPageFragment.class.getName();
    private DrawingOnImageView drawingView;
    //private Bitmap pdfImageBitmap;
    private float mScaleFactor = 0.5f;
    private float mRotationDegree = 0.f;
    private float mFocusX = 0.f;
    private float mFocusY = 0.f;
    private Matrix matrix = new Matrix();//Các lớp Matrix giữ một ma trận 3x3 để di chuyển tọa độ.
    private int mImageWidth, mImageHeight;
    private ScaleGestureDetector mScaleDetector;
    private RotateGestureDetector mRotateDetector;
    private MoveGestureDetector mMoveDetector;
    private int fragmentId;
    //

    public static PDFPageFragment getInstance() {
        PDFPageFragment pdfPageFragment = new PDFPageFragment();
        Log.e(TAG,"--------------------\ngetInstance");
        return pdfPageFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.e(TAG,"onCreate");
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.e(TAG,"onCreateView");
        return inflater.inflate(R.layout.fragment_pdf_page, container, false);
    }

    /*public void setPdfImageBitmap(Bitmap bitmapImg) {
        pdfImageBitmap = bitmapImg;
    }*/
    public void setImageResource(Image imageResource) {
        this.imageResource = imageResource;
    }

    public Image getImageResource() {
        return imageResource;
    }

    public void setFragmentId(int fragmentId){
        this.fragmentId = fragmentId;
        Log.e(TAG,"setFragmentId "+this.fragmentId);
    }

    public int getFragmentId(){
       Log.e(TAG,"getFragmentId "+this.fragmentId);
        return this.fragmentId;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.e(TAG,"onViewCreated ");
        frame_layout = (FrameLayout) view.findViewById(R.id.frame_layout);
        drawingView = (DrawingOnImageView) view.findViewById(R.id.drawingViewPDF);

        img_scale = (ImageView) view.findViewById(R.id.img_scale);

        try {
            //// Get the screen size in pixels.
            DisplayMetrics displayMetrics = new DisplayMetrics();
            getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
            mScreenHeight = displayMetrics.heightPixels;
            mScreenWidth = displayMetrics.widthPixels;
          /*  if (imageResource != null) {
                pdfTmpBitmap = BitmapFactory.decodeFile(imageResource.imagePath);
//                ImageSource imageSource = ImageSource.uri(imageResource.uri);
//                drawingView.setImage(ImageSource.bitmap(pdfTmpBitmap));
                drawingView.loadImageOnCanvas(pdfTmpBitmap);
                drawingView.setCanvasMovable(true);
            }*/
            //load anh mau
//        Bitmap loadTempImg = BitmapFactory.decodeResource(getResources(), R.mipmap.app_logo_round);
            if (imageResource != null) {
                pdfTmpBitmap = BitmapFactory.decodeFile(imageResource.imagePath);

                img_scale.setOnTouchListener(this);
                mImageHeight = pdfTmpBitmap.getHeight();
                mImageWidth = pdfTmpBitmap.getWidth();
                //img_scale.setImageBitmap(imageResource);


                img_scale.setImageBitmap(pdfTmpBitmap);
//                BitmapDrawable bitmapDrawable = new BitmapDrawable(getResources(), pdfTmpBitmap);//#19Nov+
//                img_scale.setBackground(bitmapDrawable);//#19Nov+

                //view anh thu nho lai boi ma tran so voi anh goc //He looks like he's scared of being compared to him
                matrix.postScale(mScaleFactor, mScaleFactor);
                //img_scale.setImageMatrix(matrix);

                // Set Detectors Gesture
                mScaleDetector = new ScaleGestureDetector(getContext(), new ScaleListener());
                mRotateDetector = new RotateGestureDetector(getContext(), new RotateListener());
                mMoveDetector = new MoveGestureDetector(getContext(), new MoveListener());
            }
        } catch (Exception e) {
            Crashlytics.logException(e);
            Toast.makeText(getContext(), "Exception error :" + e.getMessage(), Toast.LENGTH_SHORT).show();
        } catch (OutOfMemoryError e) {
            Crashlytics.logException(e);
            Toast.makeText(getContext(), "OutOfMemoryError error :" + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.e(TAG,"onDestroy "+" FID "+this.fragmentId);
        if (pdfTmpBitmap != null)
        {
            pdfTmpBitmap.recycle();
            pdfTmpBitmap = null;
        }
    }

    public void undoDrawing() {
        try {
            if (drawingView != null)
                drawingView.onClickUndo();
        } catch (Exception e) {
            Crashlytics.logException(e);
            e.printStackTrace();
        }
    }

    public void redoDrawing() {
        try {
            if (drawingView != null)
                drawingView.onClickRedo();
        } catch (Exception e) {
            Crashlytics.logException(e);

        }
    }

    public void setEraserActive(boolean isActive) {
        if (isActive) {
            drawingView.activateEraser();
        } else {
            drawingView.deactivateEraser();
        }

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

    public void clearDrawing() {
        drawingView.deactivateEraser();
        drawingView.reset();
//        drawingView.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.ColorsWhite));
//        img_scale.setImageBitmap(null);
    }

    public void enableImageMoving(boolean enable) {
        if (enable) {
            drawingView.setVisibility(View.INVISIBLE);
        } else {
            drawingView.setVisibility(View.VISIBLE);
        }
    }

    public void mergeBitmap(){
//        if(pdfTmpBitmap !=null && drawingView.getCanvasBitmap() !=null){
////            img_scale.setImageBitmap(overlay(pdfTmpBitmap, drawingView.getCanvasBitmap()));
//            pdfTmpBitmap = overlay(pdfTmpBitmap, drawingView.getCanvasBitmap());
//            img_scale.setImageBitmap(pdfTmpBitmap);
//            drawingView.reset();
//        }
        Bitmap bitMapScreenshot = takeScreenshotView();
        if(bitMapScreenshot!= null){
            img_scale.setImageBitmap(bitMapScreenshot);
            drawingView.reset();
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

    public  Bitmap overlay(Bitmap bmp1, Bitmap bmp2) {
        Bitmap bmOverlay = Bitmap.createBitmap(bmp2.getWidth(), bmp2.getHeight(), bmp2.getConfig());
        Canvas canvas = new Canvas(bmOverlay);
        canvas.drawBitmap(bmp1, matrix, null);
        canvas.drawBitmap(bmp2, 0,0, null);
        return bmOverlay;
    }


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

    @Override
    public void onSaveInstanceState(Bundle outState) {
        Log.e(TAG,"onSaveInstanceState()"+" FID "+this.fragmentId);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        Log.e(TAG,"onViewStateRestored()"+" FID "+this.fragmentId+"\n--------------------");
        super.onViewStateRestored(savedInstanceState);
    }
}