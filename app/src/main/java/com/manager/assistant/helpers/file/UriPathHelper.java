package com.manager.assistant.helpers.file;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Uri和路径转换的工具类
 */
public class UriPathHelper {
    private static final String TAG = "UriPathHelper";

    /**
     * 获取SAF目录Uri的可显示路径
     *
     * @param context 上下文
     * @param uri     SAF目录的Uri
     * @return 可显示的路径字符串
     */
    public static String getDisplayPathFromSAFUri(Context context, Uri uri) {
        if (context == null || uri == null) {
            return "无效的Uri";
        }

        // 检查是否为Document Uri（SAF返回的Uri）
        if (DocumentsContract.isDocumentUri(context, uri)) {
            return getDocumentPathFromUri(context, uri);
        }

        // 检查是否为Tree Uri（目录选择时返回的Uri）
        if (isTreeUri(uri)) {
            return getTreePathFromUri(context, uri);
        }

        // 其他类型的Uri
        return getFallbackPath(uri);
    }

    /**
     * 检查是否为Tree Uri
     */
    private static boolean isTreeUri(Uri uri) {
        return uri != null && "content".equals(uri.getScheme())
                && uri.toString().contains("/tree/");
    }

    /**
     * 处理Document Uri（单个文件/目录）
     */
    private static String getDocumentPathFromUri(Context context, Uri uri) {
        String documentId = DocumentsContract.getDocumentId(uri);
        String authority = uri.getAuthority();

        if (TextUtils.isEmpty(documentId)) {
            return uri.toString();
        }

        // 根据不同的Provider处理
        if ("com.android.externalstorage.documents".equals(authority)) {
            return handleExternalStorageDocument(documentId);
        } else if ("com.android.providers.downloads.documents".equals(authority)) {
            return handleDownloadsDocument(context, documentId);
        } else if ("com.android.providers.media.documents".equals(authority)) {
            return handleMediaDocument(context, documentId);
        }

        // 其他Document Provider
        return queryDocumentDisplayName(context, uri);
    }

    /**
     * 处理Tree Uri（目录访问权限）
     */
    private static String getTreePathFromUri(Context context, Uri uri) {
        try {
            String documentId = DocumentsContract.getTreeDocumentId(uri);
            String authority = uri.getAuthority();

            if ("com.android.externalstorage.documents".equals(authority)) {
                return handleExternalStorageDocument(documentId);
            }

            // 尝试获取显示名称
            String displayName = queryTreeDisplayName(context, uri);
            if (!TextUtils.isEmpty(displayName)) {
                return displayName;
            }

            return "目录: " + documentId;

        } catch (Exception e) {
            Log.e(TAG, "获取Tree路径失败", e);
            return "目录访问权限";
        }
    }

    /**
     * 处理外部存储路径
     */
    private static String handleExternalStorageDocument(String documentId) {
        try {
            // documentId格式: "primary:Android/data/com.example.app" 或 "5B2A-1A1B:Documents"
            String[] split = documentId.split(":");
            if (split.length >= 2) {
                String type = split[0];
                String path = split[1];

                if ("primary".equalsIgnoreCase(type)) {
                    // 主存储
                    return "主存储:" + path;
                } else {
                    // SD卡或其他存储
                    return "/storage/" + type + "/" + path;
                }
            }
            return "/storage/" + documentId.replace(':', '/');
        } catch (Exception e) {
            Log.e(TAG, "解析外部存储路径失败", e);
            return documentId;
        }
    }

    /**
     * 处理下载目录
     */
    @NonNull
    private static String handleDownloadsDocument(Context context, String documentId) {
        if (TextUtils.isEmpty(documentId)) {
            return "下载目录";
        }

        try {
            // documentId可能直接是数字ID
            if (documentId.matches("\\d+")) {
                long id = Long.parseLong(documentId);
                Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"),
                        id
                );

                String path = getDataColumn(context, contentUri, null, null);
                if (path != null) {
                    return path;
                }
            }

            // 如果解析失败，返回通用名称
            return "下载目录/" + documentId;

        } catch (Exception e) {
            Log.e(TAG, "解析下载目录失败", e);
            return "下载目录";
        }
    }

    /**
     * 处理媒体目录
     */
    @NonNull
    private static String handleMediaDocument(Context context, @NonNull String documentId) {
        String[] split = documentId.split(":");
        if (split.length < 2) {
            return documentId;
        }

        String type = split[0];
        String id = split[1];

        Uri contentUri = null;
        if ("image".equals(type)) {
            contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        } else if ("video".equals(type)) {
            contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
        } else if ("audio".equals(type)) {
            contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        }

        if (contentUri != null) {
            String selection = "_id=?";
            String[] selectionArgs = new String[]{id};

            String path = getDataColumn(context, contentUri, selection, selectionArgs);
            if (path != null) {
                return path;
            }
        }

        return type + "目录/" + id;
    }

    /**
     * 查询Document的显示名称
     */
    private static String queryDocumentDisplayName(Context context, Uri uri) {
        try (Cursor cursor = context.getContentResolver().query(
                uri,
                new String[]{DocumentsContract.Document.COLUMN_DISPLAY_NAME},
                null, null, null
        )) {

            if (cursor != null && cursor.moveToFirst()) {
                int displayNameIndex = cursor.getColumnIndex(
                        DocumentsContract.Document.COLUMN_DISPLAY_NAME
                );
                if (displayNameIndex != -1) {
                    String displayName = cursor.getString(displayNameIndex);
                    if (!TextUtils.isEmpty(displayName)) {
                        return displayName;
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "查询Document显示名称失败", e);
        }
        return uri.getLastPathSegment();
    }

    /**
     * 查询Tree的显示名称
     */
    private static String queryTreeDisplayName(Context context, Uri treeUri) {
        Uri docUri = DocumentsContract.buildDocumentUriUsingTree(
                treeUri,
                DocumentsContract.getTreeDocumentId(treeUri)
        );

        return queryDocumentDisplayName(context, docUri);
    }

    /**
     * 获取_data列的值（文件路径）
     */
    @Nullable
    private static String getDataColumn(Context context, Uri uri,
                                        String selection, String[] selectionArgs) {
        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {column};

        try {
            cursor = context.getContentResolver().query(
                    uri, projection, selection, selectionArgs, null
            );

            if (cursor != null && cursor.moveToFirst()) {
                int columnIndex = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(columnIndex);
            }
        } catch (Exception e) {
            Log.e(TAG, "查询_data列失败", e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return null;
    }

    /**
     * 备用方案：返回可读的Uri信息
     */
    private static String getFallbackPath(@NonNull Uri uri) {
        String path = uri.getPath();
        if (!TextUtils.isEmpty(path)) {
            // 提取最后一部分作为显示
            int lastSlash = path.lastIndexOf('/');
            if (lastSlash != -1 && lastSlash < path.length() - 1) {
                return path.substring(lastSlash + 1);
            }
            return path;
        }
        return uri.toString();
    }
}
