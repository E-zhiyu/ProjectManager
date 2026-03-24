package com.manager.assistant.data.controllers;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;

import com.manager.assistant.data.classes.Picture;
import com.manager.assistant.data.save.database.BookkeepingDbHelper;
import com.manager.assistant.data.save.database.Columns;
import com.manager.assistant.data.save.database.Tables;
import com.manager.assistant.generic_enums.LogTags;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class PictureDataController {
    /**
     * 加载与流水编号相符的图片资源
     *
     * @param context   上下文
     * @param targetRno 流水编号
     * @return 该流水记录包含的所有图片的列表
     * @throws SQLiteException 数据库读取异常时引发的异常
     */
    @NonNull
    public static List<Picture> loadPicturesByRno(Context context, long targetRno) throws SQLiteException {
        BookkeepingDbHelper dbHelper = new BookkeepingDbHelper(context);
        SQLiteDatabase db = dbHelper.openReadLink();
        List<Picture> pictureList = new ArrayList<>();

        String selection = Columns.RNO + "=?";
        String[] selectionArgs = {String.valueOf(targetRno)};
        Cursor pictureCursor = db.query(
                Tables.PICTURE.toString(),
                null,
                selection,
                selectionArgs,
                null,
                null,
                Columns.PNO.toString()
        );

        while (pictureCursor.moveToNext()) {
            String pictureUri = pictureCursor.getString(pictureCursor.getColumnIndexOrThrow(Columns.PICTURE_URI.toString()));
            long rno = pictureCursor.getLong(pictureCursor.getColumnIndexOrThrow(Columns.RNO.toString()));
            long pno = pictureCursor.getLong(pictureCursor.getColumnIndexOrThrow(Columns.PNO.toString()));

            Picture newPicture = new Picture(Uri.parse(pictureUri), rno);
            newPicture.setPno(pno);
            pictureList.add(newPicture);
        }

        pictureCursor.close();
        db.close();
        return pictureList;
    }

    /**
     * 添加一张图片到数据库
     *
     * @param context         上下文
     * @param pictureFileList 新图片文件数组
     * @param rno             图片所属流水记录的编号
     * @throws SQLiteException 数据库写入失败引发的异常
     */
    public static void addPicture(Context context, @NonNull List<File> pictureFileList, long rno) throws SQLiteException {
        BookkeepingDbHelper dbHelper = new BookkeepingDbHelper(context);
        SQLiteDatabase db = dbHelper.openWriteLink();

        for (File picture : pictureFileList) {
            String uriStr = picture.toURI().toString();

            ContentValues pictureValues = new ContentValues();
            pictureValues.put(Columns.PICTURE_URI.toString(), uriStr);
            pictureValues.put(Columns.RNO.toString(), rno);
            db.insert(Tables.PICTURE.toString(), null, pictureValues);
        }

        db.close();
    }

    /**
     * 删除图片
     *
     * @param context 上下文
     * @param pno     待删除图片的编号
     * @throws SQLiteException 删除失败引发的异常
     */
    public static void deletePicture(Context context, long pno) throws SQLiteException {
        BookkeepingDbHelper dbHelper = new BookkeepingDbHelper(context);
        SQLiteDatabase db = dbHelper.openWriteLink();

        String where = Columns.PNO + "=?";
        String[] whereArgs = {String.valueOf(pno)};
        db.delete(Tables.PICTURE.toString(), where, whereArgs);

        db.close();
    }

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
                Log.w(LogTags.DB.getV(), String.format(Locale.getDefault(), "“%s”删除失败", pictureFile.getName()));
            }
        }

        //删除数据库中的条目
        db.delete(Tables.PICTURE.toString(), selection, selectionArgs);

        pictureCursor.close();
    }
}
