package com.sly.coffer.helpers.file;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.webkit.MimeTypeMap;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;

public class MediaHelper {
    /**
     * 将媒体文件保存至相册
     *
     * @param context  上下文
     * @param mediaUri 媒体文件的 Uri
     * @return 保存后的媒体文件的 file 类型 Uri
     */
    public static Observable<Uri> saveMediaToGalleryObservable(
            Context context,
            Uri mediaUri
    ) {
        return Observable.defer(() -> {
            if (mediaUri == null || mediaUri.getPath() == null) {
                return Observable.error(new RuntimeException("无法获取媒体文件路径"));
            }

            File sourceFile = new File(mediaUri.getPath());
            if (!sourceFile.exists()) {
                return Observable.error(new RuntimeException("媒体文件不存在"));
            }

            String fileName = sourceFile.getName();
            // 根据文件后缀获取 MimeType (例如 image/jpeg, video/mp4)
            String extension = MimeTypeMap.getFileExtensionFromUrl(mediaUri.toString());
            String mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension.toLowerCase());
            boolean isVideo = mimeType != null && mimeType.startsWith("video");

            ContentResolver resolver = context.getContentResolver();
            ContentValues values = new ContentValues();

            // 设置文件的显示名称和类型
            values.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName);
            values.put(MediaStore.MediaColumns.MIME_TYPE, mimeType);

            Uri collectionUri;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // Android 10+ 使用分区存储
                if (isVideo) {
                    collectionUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                    values.put(
                            MediaStore.Video.Media.RELATIVE_PATH,
                            Environment.DIRECTORY_MOVIES + "/" + context.getPackageName()
                    );
                } else {
                    collectionUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                    values.put(
                            MediaStore.Images.Media.RELATIVE_PATH,
                            Environment.DIRECTORY_PICTURES + "/" + context.getPackageName()
                    );
                }
                // IS_PENDING = 1 表示文件正在写入，此时其他APP（如系统相册）不可见，防止文件损坏
                values.put(MediaStore.MediaColumns.IS_PENDING, 1);
            } else {
                // Android 9 及以下老版本处理
                File targetDir = isVideo ?
                        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES) :
                        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
                File appDir = new File(targetDir, context.getPackageName());
                if (!appDir.exists() && !appDir.mkdirs()) {
                    return Observable.error(new RuntimeException("无法创建媒体文件保存目录"));
                }
                File targetFile = new File(appDir, fileName);
                values.put(MediaStore.MediaColumns.DATA, targetFile.getAbsolutePath());

                if (isVideo) {
                    collectionUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else {
                    collectionUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                }
            }

            // 在 MediaStore 中插入一条空记录，获取公共区域的 Uri
            Uri itemUri = resolver.insert(collectionUri, values);
            if (itemUri == null) {
                return Observable.error(new RuntimeException("无法获取保存后的媒体文件路径"));
            }

            // 开始从私有目录拷贝数据到公共区域
            try (InputStream is = new FileInputStream(sourceFile);
                 OutputStream os = resolver.openOutputStream(itemUri)) {

                if (os == null) return null;

                byte[] buffer = new byte[4096];
                int byteCount;
                while ((byteCount = is.read(buffer)) != -1) {
                    os.write(buffer, 0, byteCount);
                }
                os.flush();

                // Android 10+ 写入完成后，释放 IS_PENDING 状态，让相册可见
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    values.clear();
                    values.put(MediaStore.MediaColumns.IS_PENDING, 0);
                    resolver.update(itemUri, values, null, null);
                }

                return Observable.just(itemUri);
            } catch (IOException e) {
                resolver.delete(itemUri, null, null);
                return Observable.error(e);
            }
        });
    }

    /**
     * 根据图片的 Uri 判断其是否为 HDR 图片
     *
     * @param context  上下文
     * @param imageUri 图片的 Uri (例如 content://... 或 file://...)
     * @return true 表示为 HDR 图片（包含 GainMap），false 表示否或系统不支持
     */
    public static Single<Boolean> isHdrImage(Context context, Uri imageUri) {
        return Single.fromCallable(() -> {
            // GainMap API 是在 Android 14 (API 34) 引入的，低于此版本的系统无法原生解析 Ultra HDR 的增益图
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                return false;
            }

            if (imageUri == null) {
                return false;
            }

            InputStream inputStream = null;
            Bitmap bitmap = null;
            try {
                // 打开 Uri 输入流
                inputStream = context.getContentResolver().openInputStream(imageUri);
                if (inputStream == null) {
                    return false;
                }

                // 配置解码选项：通过降采样（inSampleSize）减少内存占用
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inSampleSize = 4; // 缩小为原图的 1/4 解码，足够读取 HDR 元数据/GainMap

                // 解码得到 Bitmap
                bitmap = BitmapFactory.decodeStream(inputStream, null, options);

                // 判断 Bitmap 是否包含 Ultra HDR 的增益图 (Gainmap)
                if (bitmap != null && bitmap.hasGainmap()) {
                    return true;
                }
            } finally {
                // 及时释放资源
                if (bitmap != null) {
                    bitmap.recycle();
                }
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (Exception ignored) {
                    }
                }
            }

            return false;
        });
    }
}
