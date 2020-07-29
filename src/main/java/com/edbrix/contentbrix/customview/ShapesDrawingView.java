package com.edbrix.contentbrix.customview;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseArray;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

import java.util.HashSet;
import java.util.Random;

public class ShapesDrawingView extends View {

    private static final String TAG = "CirclesDrawingView";

    private Rect mMeasuredRect;

    private float mScaleFactor = 1f;

    private ScaleGestureDetector detector;

    private boolean isScaleBegin = false;

    private int SHAPE_ID = 0;

    /** Stores data about single shape */
    private static class ShapeArea {
        int radius;
        int centerX;
        int centerY;
        int shapeId = 0;

        ShapeArea(int centerX, int centerY, int radius, int shapeId) {
            this.radius = radius;
            this.centerX = centerX;
            this.centerY = centerY;
            this.shapeId = shapeId;
        }

        @Override
        public String toString() {
            return "Shape[" + centerX + ", " + centerY + ", " + radius + "]";
        }
    }

    /** Paint to draw circles */
    private Paint mShapePaint;

    private final Random mRadiusGenerator = new Random();
    // Radius limit in pixels
    private final static int RADIUS_LIMIT = 200;

    private static final int CIRCLES_LIMIT = 4;

    /** All available circles */
    private HashSet<ShapeArea> mShapes = new HashSet<ShapeArea>(CIRCLES_LIMIT);
    private SparseArray<ShapeArea> mShapePointer = new SparseArray<ShapeArea>(CIRCLES_LIMIT);

    /**
     * Default constructor
     *
     * @param ct {@link android.content.Context}
     */
    public ShapesDrawingView(final Context ct) {
        super(ct);

        init(ct);
    }

    public ShapesDrawingView(final Context ct, final AttributeSet attrs) {
        super(ct, attrs);

        init(ct);
    }

    public ShapesDrawingView(final Context ct, final AttributeSet attrs, final int defStyle) {
        super(ct, attrs, defStyle);

        init(ct);
    }

    private void init(final Context ct) {

        mShapePaint = new Paint();

        mShapePaint.setColor(Color.BLACK);
        mShapePaint.setStrokeWidth(10);
        mShapePaint.setStyle(Paint.Style.STROKE);

        detector = new ScaleGestureDetector(ct,new ScaleListener());
    }

    public void setSHAPE_ID(int shapeId){
        this.SHAPE_ID = shapeId;
    }

    @Override
    public void onDraw(final Canvas canv) {


        for (ShapeArea shape : mShapes) {
            if(shape.shapeId == 0) {
                canv.drawCircle(shape.centerX, shape.centerY, shape.radius, mShapePaint);
            }else if(shape.shapeId == 1) {
                Rect rectangle = new Rect((int) (shape.centerX - ((0.8) * shape.radius)), (int) (shape.centerY - ((0.6) * shape.radius)), (int) (shape.centerX + ((0.8) * shape.radius)), (int) (shape.centerY + ((0.6 * shape.radius))));
                canv.drawRect(rectangle, mShapePaint);
            }else if(shape.shapeId == 2) {
                int halfWidth = (RADIUS_LIMIT*2) / 2;

                Path path = new Path();
                path.moveTo(shape.centerX, shape.centerY - halfWidth); // Top
                path.lineTo(shape.centerX - halfWidth, shape.centerY + halfWidth); // Bottom left
                path.lineTo(shape.centerX + halfWidth, shape.centerY + halfWidth); // Bottom right
                path.lineTo(shape.centerX, shape.centerY - halfWidth); // Back to Top
                path.close();
                canv.drawPath(path, mShapePaint);
            }else if(shape.shapeId ==3){
                double squareSideHalf = 1 / Math.sqrt(2);
                Rect square = new Rect((int) (shape.centerX - (squareSideHalf * shape.radius)), (int) (shape.centerY - (squareSideHalf * shape.radius)), (int) (shape.centerX + (squareSideHalf * shape.radius)), (int) (shape.centerY + ((squareSideHalf * shape.radius))));
                canv.drawRect(square, mShapePaint);
            }
        }
        canv.scale(mScaleFactor,mScaleFactor,getScaleX(),getScaleY());

    }

    @Override
    public boolean onTouchEvent(final MotionEvent event) {
        boolean handled = false;

        ShapeArea touchedShape;
        int xTouch;
        int yTouch;
        int pointerId;
        int actionIndex = event.getActionIndex();

        detector.onTouchEvent(event);

        // get touch event coordinates and make transparent shape from it
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                // it's the first pointer, so clear all existing pointers data
                clearShapePointer();

                xTouch = (int) event.getX(0);
                yTouch = (int) event.getY(0);

                // check if we've touched inside some shape
                touchedShape = obtainTouchedShape(xTouch, yTouch,SHAPE_ID);
                if(touchedShape!=null) {
                    touchedShape.centerX = xTouch;
                    touchedShape.centerY = yTouch;
                    mShapePointer.put(event.getPointerId(0), touchedShape);

                    invalidate();
                    handled = true;
                }
                break;

            case MotionEvent.ACTION_POINTER_DOWN:
                Log.w(TAG, "Pointer down");
                // It secondary pointers, so obtain their ids and check shapes
//                pointerId = event.getPointerId(actionIndex);
//
//                xTouch = (int) event.getX(actionIndex);
//                yTouch = (int) event.getY(actionIndex);
//
//                // check if we've touched inside some shape
//                touchedShape = obtainTouchedShape(xTouch, yTouch);
//
//                mShapePointer.put(pointerId, touchedShape);
//                touchedShape.centerX = xTouch;
//                touchedShape.centerY = yTouch;
//                invalidate();
//                handled = true;
                break;

            case MotionEvent.ACTION_MOVE:
                final int pointerCount = event.getPointerCount();

                Log.w(TAG, "Move");


                for (actionIndex = 0; actionIndex < pointerCount; actionIndex++) {
                    // Some pointer has moved, search it by pointer id
                    pointerId = event.getPointerId(actionIndex);

                    xTouch = (int) event.getX(actionIndex);
                    yTouch = (int) event.getY(actionIndex);

                    touchedShape = mShapePointer.get(pointerId);

                    if (null != touchedShape) {
                        touchedShape.centerX = xTouch;
                        touchedShape.centerY = yTouch;
                    }
                }
                invalidate();
                handled = true;

                break;

            case MotionEvent.ACTION_UP:
                clearShapePointer();
                invalidate();
                handled = true;
                break;

            case MotionEvent.ACTION_POINTER_UP:
                // not general pointer was up
                pointerId = event.getPointerId(actionIndex);

                mShapePointer.remove(pointerId);
                invalidate();
                handled = true;
                break;

            case MotionEvent.ACTION_CANCEL:
                handled = true;
                break;

            default:
                // do nothing
                break;
        }

        return super.onTouchEvent(event) || handled;
    }

    /**
     * Clears all ShapeArea - pointer id relations
     */
    private void clearShapePointer() {
        Log.w(TAG, "clearShapePointer");

        mShapePointer.clear();
    }

    public void reset() {

        clearShapePointer();
        mShapes.clear();
        invalidate();
    }

    /**
     * Search and creates new (if needed) shape based on touch area
     *
     * @param xTouch int x of touch
     * @param yTouch int y of touch
     *
     * @return obtained {@link ShapeArea}
     */
    private ShapeArea obtainTouchedShape(final int xTouch, final int yTouch, final int shapeId) {
        ShapeArea touchedShape = getTouchedShape(xTouch, yTouch);

        if (null == touchedShape) {
            touchedShape = new ShapeArea(xTouch, yTouch, mRadiusGenerator.nextInt(RADIUS_LIMIT) + RADIUS_LIMIT, shapeId);

           /* if (mShapes.size() == CIRCLES_LIMIT) {
                Log.w(TAG, "Clear all circles, size is " + mShapes.size());
                // remove first circle
//                mShapes.clear();
//                SHAPE_ID = 0;
//                return null;
            }*/
            Log.w(TAG, "Added circle " + touchedShape);
            mShapes.add(touchedShape);

        }

        return touchedShape;
    }

    /**
     * Determines touched shape
     *
     * @param xTouch int x touch coordinate
     * @param yTouch int y touch coordinate
     *
     * @return {@link ShapeArea} touched shape or null if no shape has been touched
     */
    private ShapeArea getTouchedShape(final int xTouch, final int yTouch) {
        ShapeArea touched = null;

        for (ShapeArea shape : mShapes) {
            if ((shape.centerX - xTouch) * (shape.centerX - xTouch) + (shape.centerY - yTouch) * (shape.centerY - yTouch) <= shape.radius * shape.radius) {
                touched = shape;
                break;
            }
        }

        return touched;
    }

    @Override
    protected void onMeasure(final int widthMeasureSpec, final int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        mMeasuredRect = new Rect(0, 0, getMeasuredWidth(), getMeasuredHeight());
    }

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {


        float onScaleBegin = 0;
        float onScaleEnd = 0;

        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            mScaleFactor *= detector.getScaleFactor();

            // Don't let the object get too small or too large.
            mScaleFactor = Math.max(0.1f, Math.min(mScaleFactor, 5.0f));

            invalidate();
            return true;
        }

        @Override
        public boolean onScaleBegin(ScaleGestureDetector detector) {

            Log.w(TAG,"Scale Begin" );
            onScaleBegin = mScaleFactor;
            isScaleBegin = true;

            return true;
        }

        @Override
        public void onScaleEnd(ScaleGestureDetector detector) {

            Log.w(TAG,"Scale Ended");
            onScaleEnd = mScaleFactor;
            isScaleBegin =false;

            if (onScaleEnd > onScaleBegin){
                Log.w(TAG,"Scaled Up by a factor of  " + String.valueOf( onScaleEnd / onScaleBegin ));
            }

            if (onScaleEnd < onScaleBegin){
                Log.w(TAG,"Scaled Down by a factor of  " + String.valueOf( onScaleBegin / onScaleEnd ));
            }

            super.onScaleEnd(detector);
        }
    }
}