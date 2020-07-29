package com.edbrix.contentbrix.customview;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Pair;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

import java.io.IOException;
import java.util.ArrayList;

public class DrawingCanvasView extends View {
    private Bitmap myCanvasBitmap;
    private Canvas m_Canvas;
    private Path m_Path;
    private Paint m_Paint;
    private ArrayList<Pair<Path, Paint>> paths = new ArrayList<Pair<Path, Paint>>();
    private ArrayList<Pair<Path, Paint>> undonePaths = new ArrayList<Pair<Path, Paint>>();
    private float mX, mY;
    private static final float TOUCH_TOLERANCE = 4;
    private boolean isEraserActive = false;
    private int colorCode = Color.BLACK;
    private int penStrokeSize = 6;
    //    private float mScaleFactor = 0.5f;
    private boolean isCircleActive = false;
    private boolean isShapeActive = false;

    private int shapeId;
    public float x, y;
    private ScaleGestureDetector scaleGestureDetector;

    private Bitmap mBitmap;

    private int mImageWidth;
    private int mImageHeight;

    private boolean isMovable = false;

    private float mPositionX;
    private float mPositionY;
    private float mLastTouchX;
    private float mLastTouchY;

    private static final int INVALID_POINTER_ID = -1;
    private int mActivePointerID = INVALID_POINTER_ID;

    private float mScaleFactor = 1.0f;
    private final static float mMinZoom = 1.0f;
    private final static float mMaxZoom = 5.0f;

    public DrawingCanvasView(Context context, AttributeSet attr) {
        super(context, attr);
        setFocusable(true);
        setFocusableInTouchMode(true);
//		setBackgroundColor(Color.WHITE);
//        this.setOnTouchListener(this);
        scaleGestureDetector = new ScaleGestureDetector(context, new ScaleListener());
        //points = new ArrayList<Point>();
        onCanvasInitialization();
    }


    public void onCanvasInitialization() {
        m_Paint = new Paint();
        m_Paint.setAntiAlias(true);
        m_Paint.setDither(true);
        m_Paint.setColor(colorCode);
        m_Paint.setStyle(Paint.Style.STROKE);
        m_Paint.setStrokeWidth(penStrokeSize);
        m_Canvas = new Canvas();
        m_Path = new Path();
        Paint newPaint = new Paint(m_Paint);
        //paths.add(new Pair<Path, Paint>(m_Path, newPaint));
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        try {
            //
            int w = MeasureSpec.getSize(widthMeasureSpec);
            int h = MeasureSpec.getSize(heightMeasureSpec);

            if (w > 0 && h > 0) {
                myCanvasBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
                m_Canvas.setBitmap(myCanvasBitmap);
                //
                myCanvasBitmap.recycle();
            }
            //
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        x = event.getX();
        y = event.getY();

        scaleGestureDetector.onTouchEvent(event);

        final int action = event.getAction();

        int pointerIndex;

        switch (action & MotionEvent.ACTION_MASK) {

            case MotionEvent.ACTION_DOWN:

                //get x and y cords of where we touch the screen
//                final float x = event.getX();
//                final float y = event.getY();

                //remember where touch event started
                mLastTouchX = event.getX();
                mLastTouchY = event.getY();

                //save the ID of this pointer
                mActivePointerID = event.getPointerId(0);

//                if (!isCanvasMovable()) {
                    touch_down(x, y);
                    invalidate();
//                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (isCanvasMovable()) {
                    //find the index of the active pointer and fetch its position
                    pointerIndex = event.findPointerIndex(mActivePointerID);
                    final float x = event.getX(pointerIndex);
                    final float y = event.getY(pointerIndex);

                    if (!scaleGestureDetector.isInProgress()) {

                        //calculate the distance in x and y directions
                        final float distanceX = x - mLastTouchX;
                        final float distanceY = y - mLastTouchY;

                        mPositionX += distanceX;
                        mPositionY += distanceY;

                        //redraw canvas call onDraw method
                        invalidate();

                    }
                    //remember this touch position for next move event
                    mLastTouchX = x;
                    mLastTouchY = y;
                } else {
                    touch_move(x, y);
                    invalidate();
                }
                break;
            case MotionEvent.ACTION_UP:
                mActivePointerID = INVALID_POINTER_ID;
//                if (!isCanvasMovable()) {
                    touch_up();
                    invalidate();
//                }
                break;
            case MotionEvent.ACTION_CANCEL:
                mActivePointerID = INVALID_POINTER_ID;
                break;
            case MotionEvent.ACTION_POINTER_UP:
                //Extract the index of the pointer that left the screen
                pointerIndex = (action & MotionEvent.ACTION_POINTER_INDEX_MASK) >> MotionEvent.ACTION_POINTER_INDEX_SHIFT;
                final int pointerId = event.getPointerId(pointerIndex);
                if (pointerId == mActivePointerID) {
                    //Our active pointer is going up Choose another active pointer and adjust
                    final int newPointerIndex = pointerIndex == 0 ? 1 : 0;
                    mLastTouchX = event.getX(newPointerIndex);
                    mLastTouchY = event.getY(newPointerIndex);
                    mActivePointerID = event.getPointerId(newPointerIndex);
                }
                break;
        }

        return true;
    }
/*
    public boolean onTouch(View arg0, MotionEvent event) {
        x = event.getX();
        y = event.getY();
        scaleGestureDetector.onTouchEvent(event);
        int pointerIndex;
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:// A pressed gesture has started, the motion contains the initial starting location
                //touch_start(x, y);
                touch_down(x, y);
                invalidate();
                break;
            case MotionEvent.ACTION_MOVE:// A change has happened during a press gesture
                touch_move(x, y);
                invalidate();
                break;
            case MotionEvent.ACTION_UP://A pressed gesture has finished, the motion contains the final release location
                // as well as any intermediate points since the last down or move event
                //points.add(new Point(Math.round(x), Math.round(y)));
                touch_up();
                invalidate();
                break;
        }
        return true;
    }*/

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.save();
        canvas.translate(mPositionX, mPositionY);
        canvas.scale(mScaleFactor, mScaleFactor);
        if (mBitmap != null) {
            canvas.drawBitmap(mBitmap, 0, 0, null);

        }

        for (Pair<Path, Paint> p : paths) {
            canvas.drawPath(p.first, p.second);
        }
        canvas.restore();
    }

    private void touch_down(float x, float y) {

        if (isEraserActive) {
            m_Paint.setColor(Color.WHITE);
            m_Paint.setStrokeWidth(20);
            Paint newPaint = new Paint(m_Paint); // Clones the mPaint object
            paths.add(new Pair<Path, Paint>(m_Path, newPaint));

        } else {
            m_Paint.setColor(colorCode);
            m_Paint.setStrokeWidth(penStrokeSize);
            Paint newPaint = new Paint(m_Paint); // Clones the mPaint object
            paths.add(new Pair<Path, Paint>(m_Path, newPaint));
        }
        m_Path.reset();
        m_Path.moveTo(x, y);
        mX = x;
        mY = y;
    }

    private void touch_move(float x, float y) {
        float dx = Math.abs(x - mX);
        float dy = Math.abs(y - mY);
        if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
            m_Path.quadTo(mX, mY, (x + mX) / 2, (y + mY) / 2);
            mX = x;
            mY = y;
        }
    }

    private void touch_up() {

        if (isShapeActive) {
            /*switch (shapeId) {
                case 0:
                    drawCircle(m_Canvas,x,y,50,m_Paint);
                    break;
                case 1:
                    drawRectangle(m_Canvas,m_Paint);
                    break;
                case 2:
                    drawTriangle(m_Canvas, m_Paint, (int) x,(int)y, 100);
                    break;
                case 3:
                    //drawLine(m_Canvas,m_Paint);
                    break;
            }*/
        } else {
            m_Path.lineTo(mX, mY);
            m_Canvas.drawPath(m_Path, m_Paint); // commit the path to our offscreen
        }

        m_Path = new Path(); // kill this so we don't double draw
        Paint newPaint = new Paint(m_Paint); // Clones the mPaint object
//        paths.add(new Pair<Path, Paint>(m_Path, newPaint));
    }


    public void activateEraser() {
        isEraserActive = true;
    }

    public void deactivateEraser() {
        isEraserActive = false;
    }

    public boolean isEraserActive() {
        return isEraserActive;
    }

    public void reset() {

        paths.clear();
        undonePaths.clear();
        invalidate();
    }

    public void setPenColor(int penColor) {
        colorCode = penColor;
    }

    public void setPenStroke(int size) {
        penStrokeSize = size;
        onCanvasInitialization();
        Log.e("123", "Stroke Sizee" + size);
    }

    public void onClickUndo() {
        if (paths.size() > 0) {
            undonePaths.add(paths.remove(paths.size() - 1));
            invalidate();
           /* int size = paths.size();
            for (int i = 0; i < paths.size(); i++) {

            }*/
        } else {

        }
    }

    public void onClickRedo() {
        if (undonePaths.size() > 0) {
            paths.add(undonePaths.remove(undonePaths.size() - 1));
            invalidate();

            /*int size = undonePaths.size();
            for (int i = 0; i < undonePaths.size(); i++) {
                paths.add(undonePaths.remove(undonePaths.size() - 1));
                invalidate();
            }*/
        } else {
        }
    }

    public void activateShape(int shapeId, boolean isActive) {
        this.isShapeActive = isActive;
        this.shapeId = shapeId;
    }

    public Bitmap getCanvasBitmap() {

        return myCanvasBitmap;

    }

    public void setCanvasMovable(boolean movable) {
        isMovable = movable;
    }

    public boolean isCanvasMovable() {
        return isMovable;
    }

    public void loadImageOnCanvas(Uri selectedImage) {

//        Bitmap bitmap = null;

        try {
//            bitmap = MediaStore.Images.Media.getBitmap(getContext().getContentResolver(), selectedImage);
            loadImageOnCanvas(MediaStore.Images.Media.getBitmap(getContext().getContentResolver(), selectedImage));
        } catch (IOException e) {
            e.printStackTrace();
        }
/*
        float aspectRatio = (float) bitmap.getHeight() / (float) bitmap.getWidth();
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
//        mImageWidth = displayMetrics.widthPixels;
//        mImageHeight = Math.round(mImageWidth * aspectRatio);
//        mBitmap = bitmap.createScaledBitmap(bitmap,  mImageWidth, mImageHeight,false);

        mImageHeight = displayMetrics.heightPixels;
        mImageWidth = Math.round(mImageHeight / aspectRatio);
        mBitmap = bitmap.createScaledBitmap(bitmap, mImageWidth, mImageHeight, false);
        invalidate();
        //requestLayout();
*/
    }

    public void loadImageOnCanvas(Bitmap bitmap) {
        if (bitmap != null) {
            float aspectRatio = (float) bitmap.getHeight() / (float) bitmap.getWidth();
            DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
//        mImageWidth = displayMetrics.widthPixels;
//        mImageHeight = Math.round(mImageWidth * aspectRatio);
//        mBitmap = bitmap.createScaledBitmap(bitmap,  mImageWidth, mImageHeight,false);

            mImageHeight = displayMetrics.heightPixels;
            mImageWidth = Math.round(mImageHeight / aspectRatio);
            mBitmap = bitmap.createScaledBitmap(bitmap, mImageWidth, mImageHeight, false);
            invalidate();
            //requestLayout();
        }
    }

    //4March 2020
    public class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {

        // when a scale gesture is detected, use it to resize the image
        @Override
        public boolean onScale(ScaleGestureDetector scaleGestureDetector) {
                mScaleFactor *= scaleGestureDetector.getScaleFactor();
                mScaleFactor = Math.max(mMinZoom, Math.min(mScaleFactor, mMaxZoom));
            // Don't let the object get too small or too large.
//            mScaleFactor = Math.max(0.1f, Math.min(mScaleFactor, 5.0f));
//                mImageView.setScaleX(mScaleFactor);
//                mImageView.setScaleY(mScaleFactor);
            return true;
        }
    }

    //4March 2020

    /* ************************************************************************************************************************************ */

    /*public void drawLine(Canvas canvas,Paint paint) {
        Path path = new Path();
        boolean first = true;
        for(Point point : points){
            if(first){
                first = false;
                path.moveTo(point.x, point.y);
            }
            else{
                path.lineTo(point.x, point.y);
            }
        }

        canvas.drawPath(path, paint);
        Paint newPaint = new Paint(m_Paint);
        paths.add(new Pair<Path, Paint>(path, newPaint));
    }*/

  /*  public void drawRectangle(Canvas canvas,Paint paint) {
        Path path = new Path();

        *//*float right = mStartX > x ? mStartX : x;
        float left = mStartX > x ? x : mStartX;
        float bottom = mStartY > y ? mStartY : y;
        float top = mStartY > y ? y : mStartY;*//*

        //path.addRect(left,top,right,bottom, Path.Direction.CCW);
        //path.addRect( x-1.25f, y-1.25f, 2.5F, 2.5f, Path.Direction.CCW );
        //path.addRect(left,top,right,bottom, Path.Direction.CW);
        path.addRect(x,y,200,200, Path.Direction.CW);
        path.close();

        canvas.drawPath(path, paint);
        Paint newPaint = new Paint(m_Paint);
        paths.add(new Pair<Path, Paint>(path, newPaint));
    }

    public void drawCircle(Canvas canvas,float x, float y,int radius,Paint paint) {
        Path path = new Path();

        *//*float radiuss = (float)Math.sqrt(Math.pow(mX - x, 2) +Math.pow(mY - y, 2));
        path.addCircle(x,y,radiuss, Path.Direction.CCW);
        path.close();*//*

        path.addCircle(x,y,radius, Path.Direction.CCW);
        path.close();

        canvas.drawPath(path, paint);
        Paint newPaint = new Paint(m_Paint);
        paths.add(new Pair<Path, Paint>(path, newPaint));
    }

    public void drawTriangle(Canvas canvas, Paint paint, int x, int y, int width) {
        int halfWidth = width / 2;
        Path path = new Path();
        path.moveTo(x, y - halfWidth); // Top
        path.lineTo(x - halfWidth, y + halfWidth); // Bottom left
        path.lineTo(x + halfWidth, y + halfWidth); // Bottom right
        path.lineTo(x, y - halfWidth); // Back to Top
        path.close();
        //paths.add(new Pair<Path, Paint>(path, paint));

        *//*Paint newPaint = new Paint(m_Paint);
        paths.add(new Pair<Path, Paint>(path, newPaint));*//*

        canvas.drawPath(path, paint);
        Paint newPaint = new Paint(m_Paint);
        paths.add(new Pair<Path, Paint>(path, newPaint));

    }*/
}