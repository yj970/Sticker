package com.yj.sticker.activity;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;

import com.yj.sticker.Constant;
import com.yj.sticker.databinding.ActivityMainBinding;
import com.yj.sticker.event.SaveBitmapEvent;
import com.yj.sticker.util.ImageLoad;
import com.yj.sticker.util.ImagePickerUtil;
import com.yj.sticker.util.ImageUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import dev.utils.app.toast.ToastUtils;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding mBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mBinding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(mBinding.getRoot());


        mBinding.tvAddPic.setOnClickListener(v -> {
            ImagePickerUtil.pickImage(MainActivity.this);
        });

        mBinding.tvAddGreenHat.setOnClickListener(v -> {
            if (mBinding.ivPic.getDrawable() == null) {
                ToastUtils.showShort("请先添加幸运儿");
                return;
            }
            if (mBinding.sticker.isShow()) {
                ToastUtils.showShort("一顶绿帽已经够了！");
                return;
            }
            mBinding.sticker.initSticker();
            mBinding.sticker.show();
        });

        mBinding.tvOk.setOnClickListener(v -> {
            if (mBinding.ivPic.getDrawable() == null) {
                ToastUtils.showShort("请先添加幸运儿");
                return;
            }
            if (!mBinding.sticker.isShow()) {
                ToastUtils.showShort("一定要有绿帽！");
                return;
            }
            mBinding.sticker.hideEditView();
            // 生成bitmap
            Bitmap b = ImageUtil.createViewBitmap(mBinding.fl);
//            // 保存到本地
            ImageUtil.save2Data(this, b);

        });
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

    private void jump2Crop(String picturePath) {
        Intent intent = new Intent(this, CropActivity.class);
        intent.putExtra(Constant.IntentData.PICTURE_PATH, picturePath);
        startActivity(intent);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void accept(SaveBitmapEvent event) {
        jump2Crop(event.getFilePath());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        String picturePath = ImagePickerUtil.handleResult(this, requestCode, resultCode, data);
        if (picturePath != null) {
            setImage(picturePath);
        }
    }

    // 设置图片
    private void setImage(String picturePath) {
        mBinding.sticker.hide();
        ImageLoad.load(picturePath, mBinding.ivPic);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}