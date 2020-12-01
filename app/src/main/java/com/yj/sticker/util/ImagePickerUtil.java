package com.yj.sticker.util;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.yj.sticker.Constant;

import java.util.List;

import dev.utils.app.permission.PermissionUtils;
import dev.utils.app.toast.ToastUtils;

public class ImagePickerUtil {

    public static void pickImage(AppCompatActivity activity) {
        PermissionUtils.permission(Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE).callBack(new PermissionUtils.PermissionCallBack() {
            @Override
            public void onGranted() {
                Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                activity.startActivityForResult(intent, Constant.RequestCode.IMAGE);
            }

            @Override
            public void onDenied(List<String> grantedList, List<String> deniedList, List<String> notFoundList) {
                ToastUtils.showShort("请打开手机拍摄和存储权限");
            }
        }).request(activity);
    }

    @Nullable
    public static String handleResult(Context context, int requestCode, int resultCode, @Nullable Intent data) {
        String picturePath = null;
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case Constant.RequestCode.IMAGE:
                    Uri selectedImage = data.getData();
                    String[] filePathColumn = {MediaStore.Images.Media.DATA};
                    //查询我们需要的数据
                    Cursor cursor = context.getContentResolver().query(selectedImage,
                            filePathColumn, null, null, null);
                    cursor.moveToFirst();
                    int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                    picturePath = cursor.getString(columnIndex);
                    cursor.close();
                    break;
            }
        }
        return picturePath;
    }


}
