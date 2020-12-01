package com.yj.sticker.view.crop;

import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.view.MotionEvent;
import android.view.View;

import com.yj.sticker.R;

import dev.utils.app.ScreenUtils;
import dev.utils.app.logger.DevLogger;

public class RectFCrop extends CropChild {
    private static final String TAG = "RectFCrop";
    private Matrix mRectFMatrix = new Matrix();
    private float mOldMoveX;
    private float mOldMoveY;
    private RectF mCropRectF = new RectF();// 裁剪区
    private Paint mCropRectFPaint;
    private final int mCornerHalfWidth = 50;

    private boolean isTouchLeftTopCorner;
    private boolean isTouchRightTopCorner;
    private boolean isTouchLeftBottomCorner;
    private boolean isTouchRightBottomCorner;


    private RectF mLeftTopRectF = new RectF();
    private RectF mRightTopRectF = new RectF();
    private RectF mLeftBottomRectF = new RectF();
    private RectF mRightBottomRectF = new RectF();

    private float mDownX;
    private float mDownY;

    private float mThreshold = 2;// 阙值
    private boolean mShowCornerRect = false;// 显示4个边角
    private boolean mShowFrame = true;// 显示边框
    private final int mFrameWidth = 6;
    private Paint mFramePaint;
    private RectF mFrameRectF = new RectF();// 边框
    private float DEFAULT_CROP_RECT_WIDTH = 500;
    private float DEFAULT_CROP_RECT_HEIGHT = 300;


    public RectFCrop(View parent) {
        super(parent);

        float width = parent.getMeasuredWidth();
        float height = parent.getMeasuredHeight();
        mCropRectF.set(width / 2 - DEFAULT_CROP_RECT_WIDTH / 2, height / 2 - DEFAULT_CROP_RECT_HEIGHT / 2, width / 2 + DEFAULT_CROP_RECT_WIDTH / 2, height / 2 + DEFAULT_CROP_RECT_HEIGHT / 2);
        initRectF();

        mCropRectFPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mCropRectFPaint.setStyle(Paint.Style.FILL_AND_STROKE);


        mFramePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mFramePaint.setColor(parent.getContext().getResources().getColor(R.color.primary_light));
        mFramePaint.setStrokeWidth(mFrameWidth);
        mFramePaint.setStyle(Paint.Style.STROKE);
    }

    @Override
    public void touchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mOldMoveX = 0;
                mOldMoveY = 0;
                mDownX = event.getX();
                mDownY = event.getY();

                isTouchLeftTopCorner = false;
                isTouchRightTopCorner = false;
                isTouchLeftBottomCorner = false;
                isTouchRightBottomCorner = false;
                isTouchLeftTopCorner = isTouchLeftTopCorner(mDownX, mDownY);
                isTouchRightTopCorner = isTouchRightTopCorner(mDownX, mDownY);
                isTouchLeftBottomCorner = isTouchLeftBottomCorner(mDownX, mDownY);
                isTouchRightBottomCorner = isTouchRightBottomCorner(mDownX, mDownY);
                break;
            case MotionEvent.ACTION_MOVE:
                float moveX = event.getX();
                float moveY = event.getY();
                if (mOldMoveX != 0 && mOldMoveY != 0) {
                    float offSetX = moveX - mOldMoveX;
                    float offsetY = moveY - mOldMoveY;

                    // 超过阙值
                    boolean canDragCorner = Math.abs(offSetX) > mThreshold || Math.abs(offsetY) > mThreshold;

                    if (canDragCorner) {
                        if (isTouchLeftTopCorner) {
                            mCropRectF.left += offSetX;
                            mCropRectF.top += offsetY;
                            initRectF();
                            postInvalidate();
                        } else if (isTouchRightTopCorner) {
                            mCropRectF.right += offSetX;
                            mCropRectF.top += offsetY;
                            initRectF();
                            postInvalidate();
                        } else if (isTouchLeftBottomCorner) {
                            mCropRectF.left += offSetX;
                            mCropRectF.bottom += offsetY;
                            initRectF();
                            postInvalidate();
                        } else if (isTouchRightBottomCorner) {
                            mCropRectF.right += offSetX;
                            mCropRectF.bottom += offsetY;
                            initRectF();
                            postInvalidate();
                        }
                    }

                    if (!isTouchLeftTopCorner && !isTouchRightTopCorner && !isTouchLeftBottomCorner && !isTouchRightBottomCorner) {
                        // 不能越过边界
                        boolean canTranslate = true;
                        RectF matrixFrameRectF = getMatrixFrameRectF();
                        matrixFrameRectF.offset(offSetX, offsetY);
                        if (matrixFrameRectF.left - mFrameWidth / 2 <= 0 || matrixFrameRectF.right + mFrameWidth / 2 >= parent.getMeasuredWidth() || matrixFrameRectF.top - mFrameWidth / 2 <= 0 || matrixFrameRectF.bottom + mFrameWidth / 2 >= parent.getMeasuredHeight()) {
                            canTranslate = false;
                        }
                        if (canTranslate) {
                            postTranslate(offSetX, offsetY);
                            postInvalidate();
                        }
                    }
                }
                mOldMoveX = event.getX();
                mOldMoveY = event.getY();
                break;
        }

    }

    @Override
    public void draw(Canvas canvas) {
        // 将绘制操作保存在新的图层，因为图像合成是很昂贵的操作，将用到硬件加速，这里将图像合成的处理放到离屏缓存中进行
        int saveCount = canvas.saveLayer(0, 0, canvas.getWidth(), canvas.getHeight(), mCropRectFPaint, Canvas.ALL_SAVE_FLAG);
        // 绘制背景图
        canvas.drawColor(0xb2000000);
        // 设置混合模式
        mCropRectFPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        // 使用matrix
        canvas.concat(mRectFMatrix);
        // 绘制裁剪区
        canvas.drawRect(mCropRectF, mCropRectFPaint);
        // 清除混合模式
        mCropRectFPaint.setXfermode(null);
        // 绘制4个边框
        if (mShowFrame) {
//            canvas.drawLine(mRectF.left - mFrameWidth / 2, mRectF.top, mRectF.left - mFrameWidth / 2, mRectF.bottom, mFramePaint);
//            canvas.drawLine(mRectF.left - mFrameWidth, mRectF.top - mFrameWidth / 2, mRectF.right + mFrameWidth, mRectF.top - mFrameWidth / 2, mFramePaint);
//            canvas.drawLine(mRectF.right + mFrameWidth / 2, mRectF.top, mRectF.right + mFrameWidth / 2, mRectF.bottom, mFramePaint);
//            canvas.drawLine(mRectF.right + mFrameWidth, mRectF.bottom + mFrameWidth / 2, mRectF.left - mFrameWidth, mRectF.bottom + mFrameWidth / 2, mFramePaint);
            canvas.drawRect(mFrameRectF, mFramePaint);
        }

        // 绘制4个边角
        if (mShowCornerRect) {
            canvas.drawRect(mLeftTopRectF, mCropRectFPaint);
            canvas.drawRect(mRightTopRectF, mCropRectFPaint);
            canvas.drawRect(mLeftBottomRectF, mCropRectFPaint);
            canvas.drawRect(mRightBottomRectF, mCropRectFPaint);
        }
        // 还原画布
        canvas.restoreToCount(saveCount);
    }

    private void postTranslate(float offSetX, float offsetY) {
        mRectFMatrix.postTranslate(offSetX, offsetY);
    }

    // 是否触碰到rectF
    public boolean isTouch(float x, float y) {
        RectF rectF = new RectF();
        mRectFMatrix.mapRect(rectF, mCropRectF);
        rectF.inset(-mCornerHalfWidth, -mCornerHalfWidth);
        return rectF.contains(x, y);
    }

    private boolean isTouchCorner(float x, float y) {
        return isTouchLeftTopCorner(x, y) || isTouchRightTopCorner(x, y) || isTouchLeftBottomCorner(x, y) || isTouchRightBottomCorner(x, y);
    }


    private boolean isTouchLeftTopCorner(float x, float y) {
        RectF rectF = getMatrixCropRectF();
        RectF leftTopRectF = new RectF();
        leftTopRectF.set(rectF.left - mCornerHalfWidth, rectF.top - mCornerHalfWidth, rectF.left + mCornerHalfWidth, rectF.top + mCornerHalfWidth);
        boolean touch = leftTopRectF.contains(x, y);
//        DevLogger.dTag(TAG, "isTouchLeftTopCorner=" + touch);
        return touch;
    }

    private boolean isTouchRightTopCorner(float x, float y) {
        RectF rectF = getMatrixCropRectF();
        RectF rightTopRectF = new RectF();
        rightTopRectF.set(rectF.right - mCornerHalfWidth, rectF.top - mCornerHalfWidth, rectF.right + mCornerHalfWidth, rectF.top + mCornerHalfWidth);
        boolean touch = rightTopRectF.contains(x, y);
//        DevLogger.dTag(TAG, "isTouchRightTopCorner=" + touch);
        return touch;
    }

    private boolean isTouchLeftBottomCorner(float x, float y) {
        RectF rectF = getMatrixCropRectF();
        RectF leftBottomRectF = new RectF();
        leftBottomRectF.set(rectF.left - mCornerHalfWidth, rectF.bottom - mCornerHalfWidth, rectF.left + mCornerHalfWidth, rectF.bottom + mCornerHalfWidth);
        boolean touch = leftBottomRectF.contains(x, y);
//        DevLogger.dTag(TAG, "isTouchLeftBottomCorner=" + touch);
        return touch;
    }

    private boolean isTouchRightBottomCorner(float x, float y) {
        RectF rectF = getMatrixCropRectF();
        RectF rightBottomRectF = new RectF();
        rightBottomRectF.set(rectF.right - mCornerHalfWidth, rectF.bottom - mCornerHalfWidth, rectF.right + mCornerHalfWidth, rectF.bottom + mCornerHalfWidth);
        boolean touch = rightBottomRectF.contains(x, y);
//        DevLogger.dTag(TAG, "isTouchRightBottomCorner=" + touch);
        return touch;
    }

    private void initRectF() {
        mLeftTopRectF.set(mCropRectF.left - mCornerHalfWidth, mCropRectF.top - mCornerHalfWidth, mCropRectF.left + mCornerHalfWidth, mCropRectF.top + mCornerHalfWidth);
        mRightTopRectF.set(mCropRectF.right - mCornerHalfWidth, mCropRectF.top - mCornerHalfWidth, mCropRectF.right + mCornerHalfWidth, mCropRectF.top + mCornerHalfWidth);
        mLeftBottomRectF.set(mCropRectF.left - mCornerHalfWidth, mCropRectF.bottom - mCornerHalfWidth, mCropRectF.left + mCornerHalfWidth, mCropRectF.bottom + mCornerHalfWidth);
        mRightBottomRectF.set(mCropRectF.right - mCornerHalfWidth, mCropRectF.bottom - mCornerHalfWidth, mCropRectF.right + mCornerHalfWidth, mCropRectF.bottom + mCornerHalfWidth);
        mFrameRectF.set(mCropRectF.left - mFrameWidth / 2, mCropRectF.top - mFrameWidth / 2, mCropRectF.right + mFrameWidth / 2, mCropRectF.bottom + mFrameWidth / 2);
    }

    public RectF getMatrixCropRectF() {
        RectF rectF = new RectF();
        mRectFMatrix.mapRect(rectF, mCropRectF);
        return rectF;
    }

    private RectF getMatrixFrameRectF() {
        RectF rectF = new RectF();
        mRectFMatrix.mapRect(rectF, mFrameRectF);
        return rectF;
    }

    public void setShowCornerRect(boolean mShowCornerRect) {
        this.mShowCornerRect = mShowCornerRect;
    }
}
