package com.manager.assistant.data.data_class;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;

import com.manager.assistant.isolated_enums.LogTags;
import com.manager.assistant.data.data_save.database.BookkeepingColumns;
import com.manager.assistant.data.data_save.database.BookkeepingDbHelper;
import com.manager.assistant.data.data_save.database.BookkeepingTables;

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
        BookkeepingDbHelper db_helper = new BookkeepingDbHelper(context);
        SQLiteDatabase db = db_helper.openReadLink();
        List<Picture> pictureList = new ArrayList<>();

        String selection = BookkeepingColumns.RNO + "=?";
        String[] selectionArgs = {String.valueOf(target_rno)};
        Cursor picture_cursor = db.query(
                BookkeepingTables.PICTURE.toString(),
                null,
                selection,
                selectionArgs,
                null,
                null,
                BookkeepingColumns.PNO.toString()
        );

        while (picture_cursor.moveToNext()) {
            String pictureUri = picture_cursor.getString(picture_cursor.getColumnIndexOrThrow(BookkeepingColumns.PICTURE_URI.toString()));
            long rno = picture_cursor.getLong(picture_cursor.getColumnIndexOrThrow(BookkeepingColumns.RNO.toString()));
            long pno = picture_cursor.getLong(picture_cursor.getColumnIndexOrThrow(BookkeepingColumns.PNO.toString()));

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
        BookkeepingDbHelper dbHelper = new BookkeepingDbHelper(context);
        SQLiteDatabase db = dbHelper.openWriteLink();

        for (File picture : pictureFileList) {
            String uriStr = picture.toURI().toString();

            ContentValues pictureValues = new ContentValues();
            pictureValues.put(BookkeepingColumns.PICTURE_URI.toString(), uriStr);
            pictureValues.put(BookkeepingColumns.RNO.toString(), rno);
            db.insert(BookkeepingTables.PICTURE.toString(), null, pictureValues);
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

        String where = BookkeepingColumns.PNO + "=?";
        String[] whereArgs = {String.valueOf(pno)};
        db.delete(BookkeepingTables.PICTURE.toString(), where, whereArgs);

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
        String[] columns = {BookkeepingColumns.PICTURE_URI.toString()};
        String selection = BookkeepingColumns.RNO + "=?";
        String[] selectionArgs = {String.valueOf(rno)};
        Cursor pictureCursor = db.query(
                BookkeepingTables.PICTURE.toString(),
                columns,
                selection,
                selectionArgs,
                null,
                null,
                null
        );

        //通过Uri删除图片文件
        while (pictureCursor.moveToNext()) {
            String uriStr = pictureCursor.getString(pictureCursor.getColumnIndexOrThrow(BookkeepingColumns.PICTURE_URI.toString()));
            File pictureFile = new File(Objects.requireNonNull(Uri.parse(uriStr).getPath()));

            if (!pictureFile.exists() || !pictureFile.delete()) {
                Log.w(LogTags.DB.getV(), String.format(Locale.getDefault(), "“%s”删除失败", pictureFile.getName()));
            }
        }

        //删除数据库中的条目
        db.delete(BookkeepingTables.PICTURE.toString(), selection,selectionArgs);

        pictureCursor.close();
    }
}
