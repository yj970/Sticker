
package com.yj.sticker.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.yj.sticker.Constant;
import com.yj.sticker.databinding.ActivityCropBinding;
import com.yj.sticker.event.SaveBitmapEvent;
import com.yj.sticker.util.ImageUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;

import dev.utils.app.toast.ToastUtils;

public class CropActivity extends AppCompatActivity {
    private ActivityCropBinding mBinding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = ActivityCropBinding.inflate(getLayoutInflater());
        setContentView(mBinding.getRoot());

        String picturePath;
        if ((picturePath = getIntent().getStringExtra(Constant.IntentData.PICTURE_PATH)) != null) {
            mBinding.cropView.setPicturePath(picturePath);
        }

        mBinding.tvCrop.setOnClickListener(v -> {
            Bitmap cropBitmap = mBinding.cropView.crop();
            ImageUtil.save2Picture(this, cropBitmap);
        });

//        // test todo
//        String testPath = "/storage/emulated/0/Pictures/f47c818d-a863-4086-a1eb-65f249b01a82.png";
//        mBinding.cropView.setPicturePath(testPath);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void accept(SaveBitmapEvent event) {
        ToastUtils.showShort("保存成功,请打开相册查看！");
        SharePhoto(event.getFilePath(), this);
    }


    // 分享图片
    public void SharePhoto(String picFilePath, final Activity activity) {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        File file = new File(picFilePath);
        Uri photoUri = FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", file);
        shareIntent.putExtra(Intent.EXTRA_STREAM, photoUri);
        shareIntent.setType("image/jpeg");
        startActivity(Intent.createChooser(shareIntent, activity.getTitle()));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }
}

