package com.project.manager.ui.bookkeeping.tag;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabaseLockedException;

import com.project.manager.database.FlowColumns;
import com.project.manager.database.FlowDatabaseHelper;
import com.project.manager.database.FlowTables;

public class Tag {
    String name;    //名称
    int tno;        //编号

    /**
     * 将名称转换为编号
     *
     * @param name 标签名称
     * @param context 用于打开数据库的上下文
     * @return 对应的标签编号
     */
    public static int nameTransToTno(String name,Context context) {
        try (FlowDatabaseHelper db_helper = new FlowDatabaseHelper(context)) {
            SQLiteDatabase db = db_helper.openReadLink();

            String[] columns = {FlowColumns.TAG_NO.toString()};
            String selection = FlowColumns.TAG_NAME + "=?";
            String[] selectionArgs = {name};
            Cursor cursor = db.query(
                    FlowTables.TAG.toString(),
                    columns,
                    selection,
                    selectionArgs,
                    null,
                    null,
                    null,
                    "1"
            );

            int tag_no;
            if (cursor.moveToNext()) {
                tag_no = cursor.getInt(cursor.getColumnIndexOrThrow(FlowColumns.TAG_NO.toString()));
            } else {
                tag_no = 0;
            }

            cursor.close();
            db.close();
            return tag_no;
        } catch (SQLiteDatabaseLockedException e) {
            throw new RuntimeException("无法打开数据库：数据库被其他进程占用");
        }
    }

    /**
     * 将标签编号转换为标签名称
     *
     * @param tag_no 标签编号
     * @param context 用于打开数据库的上下文
     * @return 对应的标签名称
     */
    public static String tagNoTransToName(int tag_no, Context context) {
        try (FlowDatabaseHelper db_helper = new FlowDatabaseHelper(context)) {
            SQLiteDatabase db = db_helper.openReadLink();

            String[] columns = {FlowColumns.TAG_NAME.toString()};
            String selection = FlowColumns.TAG_NO + "=?";
            String[] selectionArgs = {String.valueOf(tag_no)};
            Cursor cursor = db.query(
                    FlowTables.TAG.toString(),
                    columns,
                    selection,
                    selectionArgs,
                    null,
                    null,
                    null,
                    "1"
            );

            String tag_name;
            if (cursor.moveToNext()) {
                tag_name = cursor.getString(cursor.getColumnIndexOrThrow(FlowColumns.TAG_NAME.toString()));
            } else {
                tag_name = "";
            }

            cursor.close();
            db.close();
            return tag_name;
        } catch (SQLiteDatabaseLockedException e) {
            throw new RuntimeException("无法打开数据库：数据库被其他进程占用");
        }
    }
}
