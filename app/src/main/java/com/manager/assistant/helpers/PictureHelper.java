package com.manager.assistant.helpers;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.content.FileProvider;

import com.manager.assistant.enums.DirectoryPaths;
import com.manager.assistant.enums.LogTags;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class PictureHelper {
    /**
     * 清空临时图片目录
     *
     * @param context 上下文
     */
    public static void clearTempPictureDir(Context context) {
        Log.d(LogTags.PICTURE_HELPER.getV(), "开始清理临时图片目录");

        //删除临时图片目录文件
        File tempPictureDir = DirectoryPaths.PICTURE_TEMP.getDir(context);
        if (tempPictureDir != null) {
            File[] files = tempPictureDir.listFiles();
            if (files != null) {
                Log.d(LogTags.PICTURE_HELPER.getV(), String.format(Locale.getDefault(), "清理%d个临时图片", files.length));
                boolean isAllTempFileDeleted = true;
                for (File tempPicture : files) {
                    if (!tempPicture.delete()) {
                        isAllTempFileDeleted = false;
                    }
                }

                if (!isAllTempFileDeleted) {
                    Log.w(LogTags.PICTURE_HELPER.getV(), "临时图片未完全删除");
                }
            }
        }
    }

    /**
     * 分享单张图片
     *
     * @param context     上下文
     * @param pictureFile 待分享的图片文件
     */
    public static void shareImage(Context context, File pictureFile, OnShareListener listener) {
        try {
            if (!pictureFile.exists()) {
                listener.onShareFailed("图片文件不存在");
                return;
            }

            //获取可分享的Uri
            Uri pictureUri = getShareableUri(context, pictureFile);

            //创建分享Intent
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("image/*");
            shareIntent.putExtra(Intent.EXTRA_STREAM, pictureUri);
            shareIntent.putExtra(Intent.EXTRA_TEXT, pictureFile.getName());

            //授予临时权限
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            //启动分享
            Intent chooserIntent = Intent.createChooser(shareIntent, pictureFile.getName());
            if (chooserIntent.resolveActivity(context.getPackageManager()) != null) {
                context.startActivity(chooserIntent);
                listener.onShareSuccess();
            } else {
                listener.onShareFailed("未找到可以分享的应用");
            }
        } catch (Exception e) {
            ExceptionHelper.showExceptionDialog(context, e);
            listener.onShareFailed("分享失败");
        }
    }

    /**
     * 获取可分享的Uri（处理Android 7.0+）
     *
     * @param context 上下文
     * @param file    待分享的图片文件
     * @return 通过FileProvider处理的可分享的Uri
     */
    public static Uri getShareableUri(Context context, File file) {
        return FileProvider.getUriForFile(
                context,
                context.getPackageName() + ".fileprovider",
                file
        );
    }

    /**
     * 将图片保存至系统相册
     *
     * @param context  上下文
     * @param imageUri 图片Uri
     * @param listener 保存情况监听器
     */
    public static void saveToGallery(Context context, Uri imageUri, OnSaveListener listener) {
        try {
            // 获取Bitmap（用于获取图片信息）
            Bitmap bitmap = getBitmapFromUri(context, imageUri);
            if (bitmap == null) {
                if (listener != null) listener.onSaveFailed("无法读取图片");
                return;
            }

            // 创建保存信息
            String fileName = generateFileName(context);
            String mimeType = getMimeType(context, imageUri);

            Uri savedUri;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                savedUri = saveUsingMediaStoreAPI(context, imageUri, fileName, mimeType);
            } else {
                savedUri = saveUsingLegacyAPI(context, imageUri, fileName);
            }

            if (savedUri != null) {
                if (listener != null) {
                    listener.onSaveSuccess(savedUri, fileName);
                }
            } else {
                if (listener != null) {
                    listener.onSaveFailed("保存失败");
                }
            }

        } catch (Exception e) {
            ExceptionHelper.showExceptionDialog(context, e);
            if (listener != null) {
                listener.onSaveFailed(e.getMessage());
            }
        }
    }

    /**
     * 生成文件名
     */
    @NonNull
    private static String generateFileName(Context context) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd(HHmmss)");
        LocalDateTime now = LocalDateTime.now();
        return String.format(
                Locale.getDefault(),
                "%s_%s.jpg",
                AboutHelper.getAppName(context),
                formatter.format(now)
        );
    }

    /**
     * 从Uri获取Bitmap
     */
    @Nullable
    private static Bitmap getBitmapFromUri(@NonNull Context context, Uri uri) {
        try {
            ContentResolver resolver = context.getContentResolver();
            InputStream inputStream = resolver.openInputStream(uri);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            if (inputStream != null) inputStream.close();
            return bitmap;
        } catch (IOException e) {
            ExceptionHelper.showExceptionDialog(context, e);
            return null;
        }
    }

    /**
     * 获取MIME类型
     */
    private static String getMimeType(@NonNull Context context, Uri uri) {
        ContentResolver resolver = context.getContentResolver();
        return resolver.getType(uri);
    }

    /**
     * Android 10+ 使用MediaStore API
     */
    @Nullable
    @RequiresApi(api = Build.VERSION_CODES.Q)
    private static Uri saveUsingMediaStoreAPI(@NonNull Context context, Uri sourceUri,
                                              String fileName, String mimeType) {
        try {
            // 创建ContentValues
            ContentValues contentValues = new ContentValues();
            contentValues.put(MediaStore.Images.Media.DISPLAY_NAME, fileName);
            contentValues.put(MediaStore.Images.Media.MIME_TYPE,
                    mimeType != null ? mimeType : "image/jpeg");
            contentValues.put(MediaStore.Images.Media.RELATIVE_PATH,
                    Environment.DIRECTORY_PICTURES + File.separator + AboutHelper.getAppName(context));
            contentValues.put(MediaStore.Images.Media.IS_PENDING, 1);

            // 插入MediaStore
            ContentResolver resolver = context.getContentResolver();
            Uri collection = MediaStore.Images.Media.getContentUri(
                    MediaStore.VOLUME_EXTERNAL_PRIMARY);
            Uri imageUri = resolver.insert(collection, contentValues);

            if (imageUri == null) return null;

            // 写入数据
            try (InputStream inputStream = resolver.openInputStream(sourceUri);
                 OutputStream outputStream = resolver.openOutputStream(imageUri)) {

                if (inputStream != null && outputStream != null) {
                    copyStream(inputStream, outputStream);
                }
            }

            // 标记为完成
            contentValues.clear();
            contentValues.put(MediaStore.Images.Media.IS_PENDING, 0);
            resolver.update(imageUri, contentValues, null, null);

            return imageUri;

        } catch (IOException e) {
            ExceptionHelper.showExceptionDialog(context, e);
            return null;
        }
    }

    /**
     * Android 9及以下使用传统API
     */
    @Nullable
    private static Uri saveUsingLegacyAPI(Context context, Uri sourceUri, String fileName) {
        try {
            // 创建目录
            File picturesDir = new File(
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                    AboutHelper.getAppName(context)
            );

            if (!picturesDir.exists() && !picturesDir.mkdirs()) {
                return null;
            }

            // 创建文件
            File imageFile = new File(picturesDir, fileName);

            // 复制文件
            ContentResolver resolver = context.getContentResolver();
            try (InputStream inputStream = resolver.openInputStream(sourceUri);
                 OutputStream outputStream = new java.io.FileOutputStream(imageFile)) {

                if (inputStream != null) {
                    copyStream(inputStream, outputStream);
                }
            }

            // 通知媒体扫描
            android.content.Intent mediaScanIntent = new android.content.Intent(
                    android.content.Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            mediaScanIntent.setData(Uri.fromFile(imageFile));
            context.sendBroadcast(mediaScanIntent);

            return Uri.fromFile(imageFile);

        } catch (IOException e) {
            ExceptionHelper.showExceptionDialog(context, e);
            return null;
        }
    }

    /**
     * 复制流数据
     */
    private static void copyStream(@NonNull InputStream input, OutputStream output) throws IOException {
        byte[] buffer = new byte[4096];
        int bytesRead;
        while ((bytesRead = input.read(buffer)) != -1) {
            output.write(buffer, 0, bytesRead);
        }
    }

    /**
     * 保存回调接口
     */
    public interface OnSaveListener {
        void onSaveSuccess(Uri savedUri, String fileName);

        void onSaveFailed(String error);
    }

    /**
     * 分享回调接口
     */
    public interface OnShareListener {
        void onShareSuccess();

        void onShareFailed(String err);
    }
}
