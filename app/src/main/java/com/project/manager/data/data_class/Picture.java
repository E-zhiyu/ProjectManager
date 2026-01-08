package com.project.manager.data.data_class;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.net.Uri;

import androidx.annotation.NonNull;

import com.project.manager.data.data_save.database.BookKeepingColumns;
import com.project.manager.data.data_save.database.BookKeepingDbHelper;
import com.project.manager.data.data_save.database.BookKeepingTables;

import java.util.ArrayList;
import java.util.List;

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
     * @param context    上下文
     * @param newPicture 新图片数据类
     * @return 分配到的图片编号
     * @throws SQLiteException 数据库写入失败引发的异常
     */
    public static long addPicture(Context context, @NonNull Picture newPicture) throws SQLiteException {
        BookKeepingDbHelper dbHelper = new BookKeepingDbHelper(context);
        SQLiteDatabase db = dbHelper.openWriteLink();

        String uriStr = newPicture.getPictureUri().toString();
        long rno = newPicture.getRno();

        ContentValues pictureValues = new ContentValues();
        pictureValues.put(BookKeepingColumns.PICTURE_URI.toString(), uriStr);
        pictureValues.put(BookKeepingColumns.RNO.toString(), rno);
        long pno = db.insert(BookKeepingTables.PICTURE.toString(), null, pictureValues);

        db.close();
        return pno;
    }
}
