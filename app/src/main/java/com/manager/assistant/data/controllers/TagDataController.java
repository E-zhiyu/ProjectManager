package com.manager.assistant.data.controllers;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.manager.assistant.data.classes.Tag;
import com.manager.assistant.data.save.database.BookkeepingDbHelper;
import com.manager.assistant.data.save.database.Columns;
import com.manager.assistant.data.save.database.Tables;
import com.manager.assistant.generic_enums.KeyStrings;
import com.manager.assistant.auxiliary.enums.AccountType;

import org.jetbrains.annotations.Contract;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class TagDataController {
    /**
     * 获取某个分组内的标签
     *
     * @param context 上下文
     * @param groupNo 待查询的分组编号
     * @return 该分组下的所有标签组成的列表
     */
    @NonNull
    public static List<Tag> getTags(Context context, long groupNo) {
        BookkeepingDbHelper dbHelper = new BookkeepingDbHelper(context);
        SQLiteDatabase db = dbHelper.openReadLink();

        List<Tag> tagList = getTags(db, groupNo, 0, null);

        db.close();
        return tagList;
    }

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
     * 将名称转换为编号
     *
     * @param name    标签名称
     * @param context 用于打开数据库的上下文
     * @return 对应的标签编号（查询不到则返回0）
     * @throws SQLiteException 数据库读取失败产生的异常
     */
    public static int nameTransToTno(String name, Context context) throws SQLiteException {
        BookkeepingDbHelper db_helper = new BookkeepingDbHelper(context);
        SQLiteDatabase db = db_helper.openReadLink();

        String[] columns = {Columns.TAG_NO.toString()};
        String selection = Columns.TAG_NAME + "=?";
        String[] selectionArgs = {name};
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

        int tagNo;
        if (cursor.moveToNext()) {
            tagNo = cursor.getInt(cursor.getColumnIndexOrThrow(Columns.TAG_NO.toString()));
        } else {
            tagNo = 0;
        }

        cursor.close();
        db.close();
        return tagNo;
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
     * 保存新的标签到数据库
     *
     * @param dataBundle 包含标签数据的数据包
     * @param context    用于打开数据库的上下文
     * @return 对应的标签编号
     * @throws SQLiteException 无法修改数据库时引发的异常
     */
    public static long saveNewTag(@NonNull Bundle dataBundle, Context context) throws SQLiteException {
        BookkeepingDbHelper dbHelper = new BookkeepingDbHelper(context);
        SQLiteDatabase db = dbHelper.openWriteLink();

        String tagName = dataBundle.getString(KeyStrings.TAG_NAME.v());
        int tagScope = dataBundle.getInt(KeyStrings.TAG_SCOPE.v());
        long groupNo = dataBundle.getLong(KeyStrings.TAG_GROUP_NO.v());

        ContentValues tagValues = new ContentValues();
        tagValues.put(Columns.TAG_NAME.toString(), tagName);
        tagValues.put(Columns.TAG_SCOPE.toString(), tagScope);
        tagValues.put(Columns.GROUP_NO.toString(), groupNo);
        long tagNo = db.insert(Tables.TAG.toString(), null, tagValues);

        db.close();
        return tagNo;
    }

    /**
     * 修改标签
     *
     * @param dataBundle 包含修改后的标签的数据包
     * @param context    打开数据库所需的上下文
     * @throws SQLiteException 无法修改数据库时引发的异常
     */
    public static void modifyTag(@NonNull Bundle dataBundle, Context context) throws SQLiteException {
        //解析数据包
        String tagName = dataBundle.getString(KeyStrings.TAG_NAME.v());
        int tagScope = dataBundle.getInt(KeyStrings.TAG_SCOPE.v());
        long newGroupNo = dataBundle.getLong(KeyStrings.TAG_GROUP_NO_NEW.v());
        long tagNo = dataBundle.getLong(KeyStrings.TAG_NO.v());

        ContentValues tagValues = new ContentValues();
        tagValues.put(Columns.TAG_NAME.toString(), tagName);
        tagValues.put(Columns.TAG_SCOPE.toString(), tagScope);
        tagValues.put(Columns.GROUP_NO.toString(), newGroupNo);
        String whereStr = Columns.TAG_NO + "=?";
        String[] whereStrArgs = {String.valueOf(tagNo)};

        BookkeepingDbHelper dbHelper = new BookkeepingDbHelper(context);
        SQLiteDatabase db = dbHelper.openWriteLink();

        db.update(
                Tables.TAG.toString(),
                tagValues,
                whereStr,
                whereStrArgs
        );

        db.close();
    }

    /**
     * 删除标签
     *
     * @param tagNo   待删除标签的编号
     * @param context 打开数据库所需的上下文
     * @throws SQLiteException 无法修改数据库时引发的异常
     */
    public static void deleteTag(long tagNo, Context context) throws SQLiteException {
        BookkeepingDbHelper dbHelper = new BookkeepingDbHelper(context);
        SQLiteDatabase db = dbHelper.openWriteLink();

        ContentValues nonTagValues = new ContentValues();
        nonTagValues.put(Columns.TAG_NO.toString(), 0);
        String whereStr = Columns.TAG_NO + "=?";
        String[] whereStrArgs = {String.valueOf(tagNo)};

        AccountDataController.onTagDeleted(tagNo, db); //清除流水记录里面的标签编号
        RuleDataController.onTagDeleted(tagNo, db);       //清除通知解析规则中的标签编号
        BudgetDataController.onTagDeleted(tagNo, db);             //删除预算中的标签编号数据

        //再删除对应标签
        db.delete(
                Tables.TAG.toString(),
                whereStr,
                whereStrArgs
        );

        db.close();
    }

    /**
     * 删除某个分组内的所有标签
     *
     * @param groupNo 标签对应的分组编号
     * @param db      需要修改的数据库
     * @throws SQLiteException 无法修改数据库时引发的异常
     */
    public static void deleteTag(long groupNo, @NonNull SQLiteDatabase db) throws SQLiteException {
        //查询标签编号
        String[] columns = {Columns.TAG_NO.toString()};
        String selection = Columns.GROUP_NO + "=?";
        String[] selectionArgs = {String.valueOf(groupNo)};
        Cursor tagCursor = db.query(
                Tables.TAG.toString(),
                columns,
                selection,
                selectionArgs,
                null,
                null,
                null
        );

        //清空引用了标签编号的数据
        while (tagCursor.moveToNext()) {
            long tagNo = tagCursor.getLong(tagCursor.getColumnIndexOrThrow(Columns.TAG_NO.toString()));
            AccountDataController.onTagDeleted(tagNo, db);  //清除流水记录里面的标签编号
            RuleDataController.onTagDeleted(tagNo, db);     //清除通知解析规则中的标签编号
        }
        tagCursor.close();

        //清空引用后再删除标签表中的数据
        String where = Columns.GROUP_NO + "=?";
        String[] whereArgs = {String.valueOf(groupNo)};
        db.delete(Tables.TAG.toString(), where, whereArgs);
    }

    /**
     * 合并标签
     *
     * @param mergedTagNo      被合并的标签编号
     * @param mergeTargetTagNo 合并到的目标标签编号
     * @param context          上下文
     * @throws SQLiteException 写入数据库可能引发的异常
     */
    public static void mergeTag(long mergedTagNo, long mergeTargetTagNo, Context context) throws SQLiteException {
        BookkeepingDbHelper dbHelper = new BookkeepingDbHelper(context);
        SQLiteDatabase db = dbHelper.openWriteLink();

        //更改对应流水记录的标签
        String where = Columns.TAG_NO + "=?";
        String[] whereArgs = {String.valueOf(mergedTagNo)};
        ContentValues targetTagNoValues = new ContentValues();
        targetTagNoValues.put(Columns.TAG_NO.toString(), mergeTargetTagNo);
        db.update(Tables.BASIC.toString(), targetTagNoValues, where, whereArgs);

        //删除被合并的标签
        db.delete(Tables.TAG.toString(), where, whereArgs);

        db.close();
    }

    /**
     * 通过流水编号获取标签实例
     *
     * @param rno     流水编号
     * @param context 上下文
     * @return 该流水对应的标签实例
     * @throws SQLiteException 读取数据库可能引发的数据库异常
     */
    @NonNull
    @Contract("_, _ -> new")
    public static Tag getTagByRno(long rno, Context context) throws SQLiteException {
        BookkeepingDbHelper dbHelper = new BookkeepingDbHelper(context);
        SQLiteDatabase db = dbHelper.openWriteLink();

        //查询标签编号
        String[] columns = {Columns.TAG_NO.toString()};
        String selection = Columns.RNO + "=?";
        String[] selectionArgs = {String.valueOf(rno)};
        Cursor basicCursor = db.query(
                Tables.BASIC.toString(),
                columns,
                selection,
                selectionArgs,
                null,
                null,
                null,
                "1"
        );

        //判断该流水记录是否有标签，如果没有则返回空标签对象
        long tagNo = 0;
        if (basicCursor.moveToNext()) {
            tagNo = basicCursor.getLong(basicCursor.getColumnIndexOrThrow(Columns.TAG_NO.toString()));
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

        basicCursor.close();
        tagCursor.close();
        db.close();
        return new Tag(tagName, tagNo, scope);
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

    /**
     * 获取标签数量
     *
     * @param context 上下文
     * @return 标签数量
     * @throws SQLiteException 读取失败引发的异常
     */
    public static int getDbCount(Context context) throws SQLiteException {
        BookkeepingDbHelper dbHelper = new BookkeepingDbHelper(context);
        SQLiteDatabase db = dbHelper.openReadLink();

        String sql = "SELECT COUNT(*) FROM " + Tables.TAG;
        int rowCount = 0;
        Cursor cursor = db.rawQuery(sql, null);
        if (cursor.moveToFirst()) {
            rowCount = cursor.getInt(0);
        }

        cursor.close();
        db.close();
        return rowCount;
    }
}
