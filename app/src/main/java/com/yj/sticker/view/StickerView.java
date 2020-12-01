package com.yj.sticker.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;

import com.yj.sticker.R;

import dev.utils.app.ScreenUtils;

public class StickerView extends View {
    private static final String TAG = "Sticker";
    private int mDownX;
    private int mDownY;
    private int mMoveX;
    private int mMoveY;
    private float mContentBitmapX;// 内容图片左上角x坐标
    private float mContentBitmapY;// 内容图片左上角y坐标
    private float mRotateBitmapX;// 旋转图片左上角x坐标
    private float mRotateBitmapY;// 旋转图片左上角y坐标
    private Matrix matrix = new Matrix();

    private float oldDegrees;
    private float oldDistance;

    private Bitmap mContentBitmap;
    private float[] mContentBitmapMidPoint = new float[2];// 图片中心坐标
    private RectF mContentRectF = new RectF();
    private RectF mFrameRectF = new RectF();
    private RectF mRotateRectF = new RectF();
    private final int mFramePadding = 30;// 边框与图片的间距
    private Paint mFramePaint;
    private boolean show;

    private enum TouchType { // 触碰类型
        NORMAL,// 正常
        CONTENT_BITMAP,// 内容图片
        ROTATE_BITMAP;// 旋转图片
    }


    private TouchType touchType;
    private Bitmap mRotateBitmap;

    public StickerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public void initSticker() {
        initSticker(R.mipmap.ic_green_hat);
    }

    private void initSticker(int resId) {
        matrix = new Matrix();
        oldDegrees = 0;
        oldDistance = 0;

        mContentBitmap = BitmapFactory.decodeResource(getResources(), resId);

        mContentBitmapX = ScreenUtils.getScreenWidth() / 2 - mContentBitmap.getWidth() / 2;
        mContentBitmapY = ScreenUtils.getScreenHeight() / 2 - mContentBitmap.getHeight() / 2;

        mContentBitmapMidPoint[0] = mContentBitmapX + mContentBitmap.getWidth() / 2;
        mContentBitmapMidPoint[1] = mContentBitmapY + mContentBitmap.getHeight() / 2;

        mContentRectF.set(mContentBitmapX, mContentBitmapY, mContentBitmapX + mContentBitmap.getWidth(), mContentBitmapY + mContentBitmap.getHeight());

        mFrameRectF.set(mContentBitmapX - mFramePadding, mContentBitmapY - mFramePadding, mContentBitmapX + mContentBitmap.getWidth() + mFramePadding, mContentBitmapY + mContentBitmap.getHeight() + mFramePadding);

        mRotateBitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_rotate);
        mRotateBitmapX = mFrameRectF.right - mRotateBitmap.getWidth() / 2;
        mRotateBitmapY = mFrameRectF.bottom - mRotateBitmap.getHeight() / 2;
        mRotateRectF.set(mRotateBitmapX, mRotateBitmapY, mRotateBitmapX + mRotateBitmap.getWidth(), mRotateBitmapY + mRotateBitmap.getHeight());


        mFramePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mFramePaint.setColor(Color.GRAY);
        mFramePaint.setStrokeWidth(7);
        mFramePaint.setStyle(Paint.Style.STROKE);
        mFramePaint.setPathEffect(new DashPathEffect(new float[]{4, 4}, 0));// 设置虚线
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (!show) {
            return;
        }
        canvas.concat(matrix);
        // 画图片
        canvas.drawBitmap(mContentBitmap, mContentBitmapX, mContentBitmapY, null);
        if (touchType != TouchType.NORMAL) {
            // 画虚线
            canvas.drawRect(mFrameRectF, mFramePaint);
            // 画旋转按钮
            canvas.drawBitmap(mRotateBitmap, mRotateBitmapX, mRotateBitmapY, null);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mMoveX = 0;
                mDownY = 0;
                mDownX = (int) event.getX();
                mDownY = (int) event.getY();
                if (isTouchContentBitmap(mDownX, mDownY)) {
                    touchType = TouchType.CONTENT_BITMAP;
                } else if (isTouchRotateBitmap(mDownX, mDownY)) {
                    touchType = TouchType.ROTATE_BITMAP;
                } else {
                    touchType = TouchType.NORMAL;
                    postInvalidate();
                }
                return true;
            case MotionEvent.ACTION_MOVE:
                int newMoveX = (int) event.getX();
                int newMoveY = (int) event.getY();
                switch (touchType) {
                    case CONTENT_BITMAP:
                        if (mMoveX != 0 && mMoveY != 0) {
                            int offSetX = newMoveX - mMoveX;
                            int offSetY = newMoveY - mMoveY;
                            mContentBitmapMidPoint[0] += offSetX;
                            mContentBitmapMidPoint[1] += offSetY;
                            postTranslate(offSetX, offSetY);
                            postInvalidate();
                        }
                        break;
                    case ROTATE_BITMAP:
                        float degrees = calculateRotation(newMoveX, newMoveY, mContentBitmapMidPoint[0], mContentBitmapMidPoint[1]);
                        float distance = calculateDistance(newMoveX, newMoveY, mContentBitmapMidPoint[0], mContentBitmapMidPoint[1]);
                        if (mMoveX != 0 && mMoveY != 0) {
                            postRotate(degrees - oldDegrees);
                            postScale(distance / oldDistance);
                            postInvalidate();
                        }
                        oldDegrees = degrees;
                        oldDistance = distance;
                        break;
                }
                mMoveX = newMoveX;
                mMoveY = newMoveY;
                break;
        }
        return super.onTouchEvent(event);
    }


    protected float calculateRotation(@Nullable MotionEvent event) {
        if (event == null || event.getPointerCount() < 2) {
            return 0f;
        }
        return calculateRotation(event.getX(0), event.getY(0), event.getX(1), event.getY(1));
    }

    protected float calculateRotation(float x1, float y1, float x2, float y2) {
        double x = x1 - x2;
        double y = y1 - y2;
        double radians = Math.atan2(y, x);
        return (float) Math.toDegrees(radians);
    }

    protected float calculateDistance(float x1, float y1, float x2, float y2) {
        double x = x1 - x2;
        double y = y1 - y2;
        return (float) Math.sqrt(x * x + y * y);
    }

    // 是否点击了内容图片
    private boolean isTouchContentBitmap(int downX, int downY) {
        RectF rect = new RectF();
        matrix.mapRect(rect, mContentRectF);
        return rect.contains(downX, downY);
    }

    // 是否点击了旋转图片
    private boolean isTouchRotateBitmap(int downX, int downY) {
        RectF rect = new RectF();
        matrix.mapRect(rect, mRotateRectF);
        return rect.contains(downX, downY);
    }

    private void postTranslate(int x, int y) {
        matrix.postTranslate(x, y);
    }

    private void postRotate(float degrees) {
        matrix.postRotate(degrees, mContentBitmapMidPoint[0], mContentBitmapMidPoint[1]);
    }

    private void postScale(float scale) {
        matrix.postScale(scale, scale, mContentBitmapMidPoint[0], mContentBitmapMidPoint[1]);
    }

    private float[] getMatrixValue() {
        float[] values = new float[9];
        matrix.getValues(values);
        return values;
    }


    public void show() {
        show = true;
        postInvalidate();
    }

    public void hide() {
        show = false;
        postInvalidate();
    }

    public boolean isShow() {
        return show;
    }

    public void hideEditView() {
        touchType = TouchType.NORMAL;
        postInvalidate();
    }


}
