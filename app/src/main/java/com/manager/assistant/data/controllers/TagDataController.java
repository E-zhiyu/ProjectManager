package com.manager.assistant.data.controllers;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;

import com.manager.assistant.data.save.database.BookkeepingDbHelper;
import com.manager.assistant.data.save.database.Columns;
import com.manager.assistant.data.save.database.Tables;

public class TagDataController {

    /**
     * 将标签编号转换为标签名称
     *
     * @param tagNo   标签编号
     * @param context 用于打开数据库的上下文
     * @return 对应的标签名称（未找到时为空）
     * @throws SQLiteException 无法修改数据库时引发的异常
     */
    public static String tagNoTransToName(long tagNo, Context context) throws SQLiteException {
        BookkeepingDbHelper dbHelper = new BookkeepingDbHelper(context);
        SQLiteDatabase db = dbHelper.openReadLink();

        String[] columns = {Columns.TAG_NAME.toString()};
        String selection = Columns.TAG_NO + "=?";
        String[] selectionArgs = {String.valueOf(tagNo)};
        Cursor cursor = db.query(
                Tables.TAG.toString(),
                columns,
                selection,
                selectionArgs,
                null,
                null,
                null,
                "1"
        );

        String tagName;
        if (cursor.moveToNext()) {
            tagName = cursor.getString(cursor.getColumnIndexOrThrow(Columns.TAG_NAME.toString()));
        } else {
            tagName = "";
        }

        cursor.close();
        db.close();
        return tagName;
    }

}
