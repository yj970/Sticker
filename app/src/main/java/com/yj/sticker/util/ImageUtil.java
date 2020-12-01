package com.yj.sticker.util;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.net.Uri;
import android.view.View;

import com.yj.sticker.APP;
import com.yj.sticker.R;
import com.yj.sticker.event.SaveBitmapEvent;

import org.greenrobot.eventbus.EventBus;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.UUID;

import dev.utils.LogPrintUtils;
import dev.utils.app.AppUtils;
import dev.utils.app.DeviceUtils;
import dev.utils.app.PathUtils;
import dev.utils.app.logger.DevLogger;
import dev.utils.app.permission.PermissionUtils;
import dev.utils.app.toast.ToastUtils;

import static dev.utils.app.AppUtils.sendBroadcast;

public class ImageUtil {


    private static final String TAG = "ImageUtil";

    private static boolean isEmptyBitmap(final Bitmap src) {
        return src == null || src.getWidth() == 0 || src.getHeight() == 0;
    }

    private static String save(final Bitmap src,
                               final Bitmap.CompressFormat format,
                               final boolean recycle, String picFilePath) {
        if (isEmptyBitmap(src)) return null;
        File filePic = new File(picFilePath);
        OutputStream os = null;
        boolean ret = false;
        try {
            os = new BufferedOutputStream(new FileOutputStream(filePic));
            ret = src.compress(format, 100, os);
            if (recycle && !src.isRecycled()) src.recycle();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (os != null) {
                    os.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return ret ? filePic.getAbsolutePath() : null;
    }

    public static void save2Data(Activity activity, Bitmap bitmap) {
        String picFilePath = PathUtils.getAppExternal().getAppDataPath() + "/" + UUID.randomUUID().toString() + ".png";
        save(activity, bitmap, picFilePath);
    }

    public static void save2Picture(Activity activity, Bitmap bitmap) {
        String picFilePath = PathUtils.getSDCard().getPicturesDir() + "/" + UUID.randomUUID().toString() + ".png";
        save(activity, bitmap, picFilePath);
    }

    // 保存图片
    private static void save(Activity activity, Bitmap bitmap, String picFilePath) {
        // todo rxjava切换线程
        PermissionUtils.permission(Manifest.permission.WRITE_EXTERNAL_STORAGE).callBack(new PermissionUtils.PermissionCallBack() {
            @Override
            public void onGranted() {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        String filePath = save(bitmap, Bitmap.CompressFormat.JPEG, false, picFilePath);
                        if (filePath != null) {
                            DevLogger.dTag(TAG, "保存成功:"+filePath);
                            ImageUtil.notifySystemUpdatePicture(filePath);
                            EventBus.getDefault().post(new SaveBitmapEvent(filePath));
                        } else {
                            ToastUtils.showShort("请打开手机存储权限！");
                        }
                    }
                }).start();
            }

            @Override
            public void onDenied(List<String> grantedList, List<String> deniedList, List<String> notFoundList) {
                ToastUtils.showShort("保存失败！");
            }
        }).request(activity);
    }

    // 通知系统更新图库
    public static void notifySystemUpdatePicture(String picturePath) {
        Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File file = new File(picturePath);
        Uri uri = Uri.fromFile(file);
        intent.setData(uri);
        sendBroadcast(intent);
    }

    // 创造view的bitmap
    public static Bitmap createViewBitmap(View view) {
        Bitmap b = Bitmap.createBitmap(view.getMeasuredWidth(), view.getMeasuredHeight(), Bitmap.Config.RGB_565);
        Canvas c = new Canvas(b);
        c.drawColor(Color.WHITE);
        view.draw(c);
        view.setDrawingCacheEnabled(false);
        view.destroyDrawingCache();
        return b;
    }

}
