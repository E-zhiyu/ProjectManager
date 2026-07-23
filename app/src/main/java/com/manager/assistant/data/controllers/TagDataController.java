package com.manager.assistant.data.controllers;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;

import androidx.annotation.NonNull;

import com.manager.assistant.data.classes.Tag;
import com.manager.assistant.data.save.database.BookkeepingDbHelper;
import com.manager.assistant.data.save.database.Columns;
import com.manager.assistant.data.save.database.Tables;

import org.jetbrains.annotations.Contract;

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

    /**
     * 获取通知解析规则的标签实例
     *
     * @param ruleNo  规则编号
     * @param context 上下文
     * @return 获取到的标签实例
     * @throws SQLiteException 读取数据库可能引发的异常
     */
    @NonNull
    @Contract("_, _ -> new")
    public static Tag getTagByRuleNo(long ruleNo, Context context) throws SQLiteException {
        BookkeepingDbHelper dbHelper = new BookkeepingDbHelper(context);
        SQLiteDatabase db = dbHelper.openWriteLink();

        //查询标签编号
        String[] columns = {Columns.TAG_NO.toString()};
        String selection = Columns.RULE_NO + "=?";
        String[] selectionArgs = {String.valueOf(ruleNo)};
        Cursor ruleCursor = db.query(
                Tables.ANALYSIS_RULE.toString(),
                columns,
                selection,
                selectionArgs,
                null,
                null,
                null,
                "1"
        );

        long tagNo = 0;
        if (ruleCursor.moveToNext()) {
            tagNo = ruleCursor.getLong(ruleCursor.getColumnIndexOrThrow(Columns.TAG_NO.toString()));
            if (tagNo == 0)
                return new Tag("", 0, 0);
        }

        //查询标签名称
        String[] tagColumns = {
                Columns.TAG_NAME.toString(),
                Columns.TAG_SCOPE.toString()
        };
        String tagSelection = Columns.TAG_NO + "=?";
        String[] tagSelectionArgs = {String.valueOf(tagNo)};
        Cursor tagCursor = db.query(
                Tables.TAG.toString(),
                tagColumns,
                tagSelection, tagSelectionArgs,
                null,
                null,
                null,
                "1"
        );

        String tagName = "";
        int scope = 0;
        if (tagCursor.moveToNext()) {
            tagName = tagCursor.getString(tagCursor.getColumnIndexOrThrow(Columns.TAG_NAME.toString()));
            scope = tagCursor.getInt(tagCursor.getColumnIndexOrThrow(Columns.TAG_SCOPE.toString()));
        }

        ruleCursor.close();
        tagCursor.close();
        db.close();
        return new Tag(tagName, tagNo, scope);
    }

}
