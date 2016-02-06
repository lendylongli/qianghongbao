package com.codeboy.qianghongbao.util;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

/**
 * <p>Created 16/2/7 上午1:37.</p>
 * <p><a href="mailto:730395591@qq.com">Email:730395591@qq.com</a></p>
 * <p><a href="http://www.happycodeboy.com">LeonLee Blog</a></p>
 *
 * @author LeonLee
 */
public final class BitmapUtils {

    private BitmapUtils() {}

    public static boolean saveBitmap(Context context, File output, Bitmap bitmap) {
        if(output.exists()) {
            return false;
        }
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(output);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            insertMedia(context, output, "image/jpeg");
            return true;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return false;
        } finally {
            if(fos != null) {
                try {
                    fos.close();
                } catch (Exception e) {}

            }
        }


    }

    /** 加入到系统的图库中*/
    private static void insertMedia(Context context, File output, String mime) {
        try {
            ContentValues values = new ContentValues();
            values.put(MediaStore.Video.Media.DATA, output.getAbsolutePath());
            values.put(MediaStore.Video.Media.MIME_TYPE, mime);
            //记录到系统媒体数据库，通过系统的gallery可以即时查看
            context.getContentResolver().insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values);
            //通知系统去扫描
            context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(output)));
        } catch (Exception e){}
    }
}
