package com.yj.sticker.util;

import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.yj.sticker.APP;

public class ImageLoad {

    public static void load(String path, ImageView imageView) {
        Glide.with(APP.getApp()).load(path)
                .into(imageView);
    }
}
