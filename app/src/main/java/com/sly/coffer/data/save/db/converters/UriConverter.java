package com.sly.coffer.data.save.db.converters;

import android.net.Uri;

import androidx.room.TypeConverter;

public class UriConverter {
    /**
     * 将{@link Uri}转换为字符串
     *
     * @param uri 需要转换为字符串的Uri对象
     * @return 由{@link Uri}对象转换得到的字符串
     */
    @TypeConverter
    public static String fromUri(Uri uri) {
        return uri == null ? null : uri.toString();
    }

    /**
     * 将字符串转换为{@link Uri}对象
     *
     * @param uriStr 需要转换为{@link Uri}的字符串
     * @return 转换得到的{@link Uri}对象
     */
    @TypeConverter
    public static Uri toUri(String uriStr) {
        return uriStr == null ? null : Uri.parse(uriStr);
    }
}
