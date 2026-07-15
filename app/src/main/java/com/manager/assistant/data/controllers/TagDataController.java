package com.manager.assistant.data.controllers;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.manager.assistant.data.classes.Tag;
import com.manager.assistant.data.save.database.BookkeepingDbHelper;
import com.manager.assistant.data.save.database.Columns;
import com.manager.assistant.data.save.database.Tables;
import com.manager.assistant.auxiliary.enums.AccountType;

import org.jetbrains.annotations.Contract;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class TagDataController {

    /**
     * 获取标签
     *
     * @param db            数据库实例
     * @param targetGroupNo 标签分组编号（传递-1则不限制标签分组）
     * @param excludedTagNo 需要排除的标签编号（传递0则不排除）
     * @param scopeType     标签作用域（传递null则不限制作用域）
     * @return 该分组下所有标签组成的列表
     * @throws SQLiteException 数据读取失败引发的异常
     */
    @NonNull
    public static List<Tag> getTags(
            SQLiteDatabase db,
            long targetGroupNo,
            long excludedTagNo,
            @Nullable AccountType scopeType
    ) throws SQLiteException {
        StringBuilder selectionBuilder = new StringBuilder("1=1");
        List<String> selectionArgs = new ArrayList<>();

        //生成标签分组选择条件
        if (targetGroupNo != -1) {
            selectionBuilder.append(" AND ");
            selectionBuilder.append(Columns.GROUP_NO);
            selectionBuilder.append("=?");
            selectionArgs.add(String.valueOf(targetGroupNo));
        }

        //生成排除的标签编号选择条件
        if (excludedTagNo != 0) {
            selectionBuilder.append(" AND ");
            selectionBuilder.append(Columns.TAG_NO);
            selectionBuilder.append("!=?");
            selectionArgs.add(String.valueOf(excludedTagNo));
        }

        //生成作用域选择条件
        if (scopeType != null) {
            int binary = (int) Math.pow(2, scopeType.ordinal());
            selectionBuilder.append(" AND ");
            selectionBuilder.append(String.format(
                    Locale.getDefault(),
                    "%s&%d=0",      //某一位为0表示这个标签对于该位数对应的序列数的种类可见
                    Columns.TAG_SCOPE,
                    binary
            ));
        }

        //生成查询游标
        Cursor tagCursor = db.query(
                Tables.TAG.toString(),
                null,
                selectionBuilder.toString(),
                selectionArgs.toArray(new String[0]),
                null,
                null,
                Columns.TAG_NO.toString()   //标签编号升序排序
        );

        //开始查询
        List<Tag> tagList = new ArrayList<>();
        while (tagCursor.moveToNext()) {
            String tagName = tagCursor.getString(tagCursor.getColumnIndexOrThrow(Columns.TAG_NAME.toString()));     //标签名称
            long tagNo = tagCursor.getLong(tagCursor.getColumnIndexOrThrow(Columns.TAG_NO.toString()));             //标签编号
            int tagScope = tagCursor.getInt(tagCursor.getColumnIndexOrThrow(Columns.TAG_SCOPE.toString()));

            Tag oneTag = new Tag(tagName, tagNo, tagScope);
            tagList.add(oneTag);
        }

        tagCursor.close();
        return tagList;
    }

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
     * 将一系列标签编号转换为标签实例
     *
     * @param tagNoList 标签编号列表
     * @param context   上下文
     * @return 标签名称列表
     * @throws SQLiteException 读取失败引发的数据库异常
     */
    @NonNull
    public static List<Tag> getTagByTagNo(@NonNull List<Long> tagNoList, Context context) throws SQLiteException {
        if (tagNoList.isEmpty()) {
            return new ArrayList<>();
        }

        BookkeepingDbHelper dbHelper = new BookkeepingDbHelper(context);
        SQLiteDatabase db = dbHelper.openReadLink();

        //生成选择条件
        StringBuilder selection;
        selection = new StringBuilder(Columns.TAG_NO + " IN (");
        selection.append(TextUtils.join(",", Collections.nCopies(tagNoList.size(), "?")));
        selection.append(")");

        String[] columns = {
                Columns.TAG_NAME.toString(),
                Columns.TAG_NO.toString(),
                Columns.TAG_SCOPE.toString()
        };
        Cursor cursor = db.query(
                Tables.TAG.toString(),
                columns,
                selection.toString(),
                tagNoList.stream().map(String::valueOf).toArray(String[]::new),
                null,
                null,
                null
        );

        List<Tag> tagList = new ArrayList<>();
        while (cursor.moveToNext()) {
            String tagName = cursor.getString(cursor.getColumnIndexOrThrow(Columns.TAG_NAME.toString()));
            long tagNo = cursor.getLong(cursor.getColumnIndexOrThrow(Columns.TAG_NO.toString()));
            int scope = cursor.getInt(cursor.getColumnIndexOrThrow(Columns.TAG_SCOPE.toString()));
            tagList.add(new Tag(tagName, tagNo, scope));
        }

        cursor.close();
        db.close();
        return tagList;
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
