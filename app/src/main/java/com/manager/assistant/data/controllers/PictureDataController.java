package com.manager.assistant.data.controllers;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;

import com.manager.assistant.data.save.database.Columns;
import com.manager.assistant.data.save.database.Tables;
import com.manager.assistant.generic_enums.LogTags;

import java.io.File;
import java.util.Locale;
import java.util.Objects;

public class PictureDataController {

    /**
     * 根据流水编号删除图片
     *
     * @param rno 流水编号
     * @param db  需要修改的数据库实例
     * @throws SQLiteException 删除失败引发的异常
     */
    public static void deletePicture(long rno, @NonNull SQLiteDatabase db) throws SQLiteException {
        //查询图片Uri
        String[] columns = {Columns.PICTURE_URI.toString()};
        String selection = Columns.RNO + "=?";
        String[] selectionArgs = {String.valueOf(rno)};
        Cursor pictureCursor = db.query(
                Tables.PICTURE.toString(),
                columns,
                selection,
                selectionArgs,
                null,
                null,
                null
        );

        //通过Uri删除图片文件
        while (pictureCursor.moveToNext()) {
            String uriStr = pictureCursor.getString(pictureCursor.getColumnIndexOrThrow(Columns.PICTURE_URI.toString()));
            File pictureFile = new File(Objects.requireNonNull(Uri.parse(uriStr).getPath()));

            if (!pictureFile.exists() || !pictureFile.delete()) {
                Log.w(LogTags.DB.n(), String.format(Locale.getDefault(), "“%s”删除失败", pictureFile.getName()));
            }
        }

        //删除数据库中的条目
        db.delete(Tables.PICTURE.toString(), selection, selectionArgs);

        pictureCursor.close();
    }
}
