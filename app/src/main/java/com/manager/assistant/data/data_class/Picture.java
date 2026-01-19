package com.manager.assistant.data.data_class;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;

import com.manager.assistant.enums.LogTags;
import com.manager.assistant.data.data_save.database.BookKeepingColumns;
import com.manager.assistant.data.data_save.database.BookKeepingDbHelper;
import com.manager.assistant.data.data_save.database.BookKeepingTables;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * 流水记录相关图片
 */
public class Picture {
    private final Uri pictureUri;   //图片文件Uri
    private final long rno;         //所属标签编号
    private long pno = 0;           //图片编号（默认为0，只有当确认保存时才写入数据库并分配编号）

    public Picture(Uri pictureUri, long rno) {
        this.pictureUri = pictureUri;
        this.rno = rno;
    }

    public long getPno() {
        return pno;
    }

    public void setPno(long pno) {
        this.pno = pno;
    }

    public long getRno() {
        return rno;
    }

    public Uri getPictureUri() {
        return pictureUri;
    }

    /**
     * 加载与流水编号相符的图片资源
     *
     * @param context    上下文
     * @param target_rno 流水编号
     * @return 该流水记录包含的所有图片的列表
     * @throws SQLiteException 数据库读取异常时引发的异常
     */
    @NonNull
    public static List<Picture> loadPicturesByRno(Context context, long target_rno) throws SQLiteException {
        BookKeepingDbHelper db_helper = new BookKeepingDbHelper(context);
        SQLiteDatabase db = db_helper.openReadLink();
        List<Picture> pictureList = new ArrayList<>();

        String selection = BookKeepingColumns.RNO + "=?";
        String[] selectionArgs = {String.valueOf(target_rno)};
        Cursor picture_cursor = db.query(
                BookKeepingTables.PICTURE.toString(),
                null,
                selection,
                selectionArgs,
                null,
                null,
                BookKeepingColumns.PNO.toString()
        );

        while (picture_cursor.moveToNext()) {
            String pictureUri = picture_cursor.getString(picture_cursor.getColumnIndexOrThrow(BookKeepingColumns.PICTURE_URI.toString()));
            long rno = picture_cursor.getLong(picture_cursor.getColumnIndexOrThrow(BookKeepingColumns.RNO.toString()));
            long pno = picture_cursor.getLong(picture_cursor.getColumnIndexOrThrow(BookKeepingColumns.PNO.toString()));

            Picture newPicture = new Picture(Uri.parse(pictureUri), rno);
            newPicture.setPno(pno);
            pictureList.add(newPicture);
        }

        picture_cursor.close();
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
        BookKeepingDbHelper dbHelper = new BookKeepingDbHelper(context);
        SQLiteDatabase db = dbHelper.openWriteLink();

        for (File picture : pictureFileList) {
            String uriStr = picture.toURI().toString();

            ContentValues pictureValues = new ContentValues();
            pictureValues.put(BookKeepingColumns.PICTURE_URI.toString(), uriStr);
            pictureValues.put(BookKeepingColumns.RNO.toString(), rno);
            db.insert(BookKeepingTables.PICTURE.toString(), null, pictureValues);
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
        BookKeepingDbHelper dbHelper = new BookKeepingDbHelper(context);
        SQLiteDatabase db = dbHelper.openWriteLink();

        String where = BookKeepingColumns.PNO + "=?";
        String[] whereArgs = {String.valueOf(pno)};
        db.delete(BookKeepingTables.PICTURE.toString(), where, whereArgs);

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
        String[] columns = {BookKeepingColumns.PICTURE_URI.toString()};
        String selection = BookKeepingColumns.RNO + "=?";
        String[] selectionArgs = {String.valueOf(rno)};
        Cursor pictureCursor = db.query(
                BookKeepingTables.PICTURE.toString(),
                columns,
                selection,
                selectionArgs,
                null,
                null,
                null
        );

        //通过Uri删除图片文件
        while (pictureCursor.moveToNext()) {
            String uriStr = pictureCursor.getString(pictureCursor.getColumnIndexOrThrow(BookKeepingColumns.PICTURE_URI.toString()));
            File pictureFile = new File(Objects.requireNonNull(Uri.parse(uriStr).getPath()));

            if (!pictureFile.exists() || !pictureFile.delete()) {
                Log.w(LogTags.DB.getV(), String.format(Locale.getDefault(), "“%s”删除失败", pictureFile.getName()));
            }
        }

        //删除数据库中的条目
        db.delete(BookKeepingTables.PICTURE.toString(), selection,selectionArgs);

        pictureCursor.close();
    }
}
