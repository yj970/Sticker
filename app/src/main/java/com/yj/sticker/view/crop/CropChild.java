package com.yj.sticker.view.crop;

import android.graphics.Canvas;
import android.view.MotionEvent;
import android.view.View;

public abstract class CropChild {
    protected View parent;

    public CropChild(View parent) {
        this.parent = parent;
    }

    public abstract void touchEvent(MotionEvent event);
    public abstract void draw(Canvas canvas);

    public void postInvalidate() {
        parent.postInvalidate();
    }

}
