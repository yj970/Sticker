package com.yj.sticker.view.crop;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class CropView extends View {
    private static final String TAG = "CropView";
    private Bitmap mSourceBitmap;
    private String mPicturePath;
    private RectFCrop mRectFCrop;
    private boolean isTouchRectF;

    public CropView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mRectFCrop = new RectFCrop(this);
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        mRectFCrop.draw(canvas);
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.save();
        if (mSourceBitmap != null) {
            canvas.drawBitmap(mSourceBitmap, 0, 0, null);
        }
        canvas.restore();
    }

    public void setPicturePath(@NonNull String picturePath) {
        mPicturePath = picturePath;
        mSourceBitmap = BitmapFactory.decodeFile(picturePath);
        postInvalidate();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                isTouchRectF = false;
                isTouchRectF = isTouchRectF(event.getX(), event.getY());
                break;
        }
        if (isTouchRectF) {
            mRectFCrop.touchEvent(event);
        }
        return true;
    }

    // 是否触碰到rectF
    private boolean isTouchRectF(float x, float y) {
        return mRectFCrop.isTouch(x, y);
    }

    // 裁剪
    public Bitmap crop() {
        RectF matrixCropRectF = mRectFCrop.getMatrixCropRectF();
        Bitmap bitmap = Bitmap.createBitmap(mSourceBitmap, (int)matrixCropRectF.left, (int)matrixCropRectF.top, (int)matrixCropRectF.width(),(int)matrixCropRectF.height());
        return bitmap;
    }
}
