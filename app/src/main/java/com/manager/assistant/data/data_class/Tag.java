package com.manager.assistant.data.data_class;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.manager.assistant.data.data_class.running_account.RunningAccountBase;
import com.manager.assistant.data.data_save.database.BookKeepingColumns;
import com.manager.assistant.data.data_save.database.BookKeepingDbHelper;
import com.manager.assistant.data.data_save.database.BookKeepingTables;

import org.jetbrains.annotations.Contract;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Tag {
    private String name;    //名称
    private final long tno; //编号

    public Tag(String name, long tno) {
        this.name = name;
        this.tno = tno;
    }

    public String getName() {
        return name;
    }

    public void setName(String new_name) {
        this.name = new_name;
    }

    public long getTno() {
        return tno;
    }

    /**
     * 将名称转换为编号
     *
     * @param name    标签名称
     * @param context 用于打开数据库的上下文
     * @return 对应的标签编号
     * @throws SQLiteException 数据库读取失败产生的异常
     */
    public static int nameTransToTno(String name, Context context) throws SQLiteException {
        BookKeepingDbHelper db_helper = new BookKeepingDbHelper(context);
        SQLiteDatabase db = db_helper.openReadLink();

        String[] columns = {BookKeepingColumns.TAG_NO.toString()};
        String selection = BookKeepingColumns.TAG_NAME + "=?";
        String[] selectionArgs = {name};
        Cursor cursor = db.query(
                BookKeepingTables.TAG.toString(),
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
            tag_no = cursor.getInt(cursor.getColumnIndexOrThrow(BookKeepingColumns.TAG_NO.toString()));
        } else {
            tag_no = 0;
        }

        cursor.close();
        db.close();
        return tag_no;
    }

    /**
     * 将标签编号转换为标签名称
     *
     * @param tag_no  标签编号
     * @param context 用于打开数据库的上下文
     * @return 对应的标签名称（未找到时为空）
     * @throws SQLiteException 无法修改数据库时引发的异常
     */
    public static String tagNoTransToName(long tag_no, Context context) throws SQLiteException {
        BookKeepingDbHelper db_helper = new BookKeepingDbHelper(context);
        SQLiteDatabase db = db_helper.openReadLink();

        String[] columns = {BookKeepingColumns.TAG_NAME.toString()};
        String selection = BookKeepingColumns.TAG_NO + "=?";
        String[] selectionArgs = {String.valueOf(tag_no)};
        Cursor cursor = db.query(
                BookKeepingTables.TAG.toString(),
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
            tag_name = cursor.getString(cursor.getColumnIndexOrThrow(BookKeepingColumns.TAG_NAME.toString()));
        } else {
            tag_name = "";
        }

        cursor.close();
        db.close();
        return tag_name;
    }

    /**
     * 将一系列标签编号转换为标签名称
     *
     * @param tagNoList 标签编号列表
     * @param context   上下文
     * @return 标签名称列表
     * @throws SQLiteException 读取失败引发的数据库异常
     */
    @NonNull
    @Contract("_, _ -> new")
    public static List<Tag> getTagByTagNo(@NonNull List<Long> tagNoList, Context context) throws SQLiteException {
        if (tagNoList.isEmpty()) {
            return new ArrayList<>();
        }

        BookKeepingDbHelper db_helper = new BookKeepingDbHelper(context);
        SQLiteDatabase db = db_helper.openReadLink();

        //生成选择条件
        StringBuilder selection;
        selection = new StringBuilder(BookKeepingColumns.TAG_NO + " IN (");
        selection.append(TextUtils.join(",", Collections.nCopies(tagNoList.size(), "?")));
        selection.append(")");

        String[] columns = {
                BookKeepingColumns.TAG_NAME.toString(),
                BookKeepingColumns.TAG_NO.toString()
        };
        Cursor cursor = db.query(
                BookKeepingTables.TAG.toString(),
                columns,
                selection.toString(),
                tagNoList.stream().map(String::valueOf).toArray(String[]::new),
                null,
                null,
                null
        );

        List<Tag> tagList = new ArrayList<>();
        while (cursor.moveToNext()) {
            String tagName = cursor.getString(cursor.getColumnIndexOrThrow(BookKeepingColumns.TAG_NAME.toString()));
            long tag_no = cursor.getLong(cursor.getColumnIndexOrThrow(BookKeepingColumns.TAG_NO.toString()));
            tagList.add(new Tag(tagName, tag_no));
        }

        cursor.close();
        db.close();
        return tagList;
    }

    /**
     * 保存新的标签到数据库
     *
     * @param tag_name 标签名称
     * @param group_no 该标签对应的分组编号
     * @param context  用于打开数据库的上下文
     * @return 对应的标签编号
     * @throws SQLiteException 无法修改数据库时引发的异常
     */
    public static long saveNewTag(String tag_name, long group_no, Context context) throws SQLiteException {
        long tag_no;    //标签编号

        BookKeepingDbHelper db_helper = new BookKeepingDbHelper(context);
        SQLiteDatabase db = db_helper.openWriteLink();

        ContentValues tag_values = new ContentValues();
        tag_values.put(BookKeepingColumns.TAG_NAME.toString(), tag_name);
        tag_values.put(BookKeepingColumns.GROUP_NO.toString(), group_no);
        tag_no = db.insert(BookKeepingTables.TAG.toString(), null, tag_values);

        db.close();
        return tag_no;
    }

    /**
     * 修改标签（不修改所属分组）
     *
     * @param new_name 新标签名称
     * @param tag_no   标签编号
     * @param context  打开数据库所需的上下文
     * @throws SQLiteException 无法修改数据库时引发的异常
     */
    public static void modifyTag(String new_name, long tag_no, Context context) throws SQLiteException {
        ContentValues tag_values = new ContentValues();
        tag_values.put(BookKeepingColumns.TAG_NAME.toString(), new_name);
        String whereStr = BookKeepingColumns.TAG_NO + "=?";
        String[] whereStrArgs = {String.valueOf(tag_no)};

        BookKeepingDbHelper db_helper = new BookKeepingDbHelper(context);
        SQLiteDatabase db = db_helper.openWriteLink();

        db.update(
                BookKeepingTables.TAG.toString(),
                tag_values,
                whereStr,
                whereStrArgs
        );

        db.close();
    }

    /**
     * 修改标签（修改所属分组）
     *
     * @param new_tag_name 新标签名称
     * @param tag_no       待修改的标签编号
     * @param new_group_no 新分组编号
     * @param context      打开数据库所需的上下文
     * @throws SQLiteException 无法修改数据库时引发的异常
     */
    public static void modifyTag(String new_tag_name, long tag_no, long new_group_no, Context context) throws SQLiteException {
        ContentValues tag_values = new ContentValues();
        tag_values.put(BookKeepingColumns.TAG_NAME.toString(), new_tag_name);
        tag_values.put(BookKeepingColumns.GROUP_NO.toString(), new_group_no);
        String whereStr = BookKeepingColumns.TAG_NO + "=?";
        String[] whereStrArgs = {String.valueOf(tag_no)};

        BookKeepingDbHelper db_helper = new BookKeepingDbHelper(context);
        SQLiteDatabase db = db_helper.openWriteLink();

        db.update(
                BookKeepingTables.TAG.toString(),
                tag_values,
                whereStr,
                whereStrArgs
        );

        db.close();
    }

    /**
     * 删除标签
     *
     * @param tag_no  待删除标签的编号
     * @param context 打开数据库所需的上下文
     * @throws SQLiteException 无法修改数据库时引发的异常
     */
    public static void deleteTag(long tag_no, Context context) throws SQLiteException {
        BookKeepingDbHelper db_helper = new BookKeepingDbHelper(context);
        SQLiteDatabase db = db_helper.openWriteLink();

        ContentValues non_tag_values = new ContentValues();
        non_tag_values.put(BookKeepingColumns.TAG_NO.toString(), 0);
        String whereStr = BookKeepingColumns.TAG_NO + "=?";
        String[] whereStrArgs = {String.valueOf(tag_no)};

        RunningAccountBase.setDefaultTagNo(tag_no, db); //清除流水记录里面的标签编号
        AnalysisRule.setDefaultTagNo(tag_no, db);       //清除通知解析规则中的标签编号

        //再删除对应标签
        db.delete(
                BookKeepingTables.TAG.toString(),
                whereStr,
                whereStrArgs
        );

        db.close();
    }

    /**
     * 通过分组编号删除标签
     *
     * @param group_no 标签对应的分组编号
     * @param db       需要修改的数据库
     * @throws SQLiteException 无法修改数据库时引发的异常
     */
    public static void deleteTag(long group_no, @NonNull SQLiteDatabase db) throws SQLiteException {
        //查询标签编号
        String[] columns = {BookKeepingColumns.TAG_NO.toString()};
        String selection = BookKeepingColumns.GROUP_NO + "=?";
        String[] selectionArgs = {String.valueOf(group_no)};
        Cursor tagCursor = db.query(
                BookKeepingTables.TAG.toString(),
                columns,
                selection,
                selectionArgs,
                null,
                null,
                null
        );

        //清空引用了标签编号的数据
        while (tagCursor.moveToNext()) {
            long tag_no = tagCursor.getLong(tagCursor.getColumnIndexOrThrow(BookKeepingColumns.TAG_NO.toString()));
            RunningAccountBase.setDefaultTagNo(tag_no, db); //清除流水记录里面的标签编号
            AnalysisRule.setDefaultTagNo(tag_no, db);       //清除通知解析规则中的标签编号
        }
        tagCursor.close();

        String where = BookKeepingColumns.GROUP_NO + "=?";
        String[] whereArgs = {String.valueOf(group_no)};
        db.delete(BookKeepingTables.TAG.toString(), where, whereArgs);
    }

    /**
     * 合并标签
     *
     * @param merged_tag_no       被合并的标签编号
     * @param merge_target_tag_no 合并到的目标标签编号
     * @param context             上下文
     * @throws SQLiteException 写入数据库可能引发的异常
     */
    public static void mergeTag(long merged_tag_no, long merge_target_tag_no, Context context) throws SQLiteException {
        BookKeepingDbHelper db_helper = new BookKeepingDbHelper(context);
        SQLiteDatabase db = db_helper.openWriteLink();

        //更改对应流水记录的标签
        String where = BookKeepingColumns.TAG_NO + "=?";
        String[] whereArgs = {String.valueOf(merged_tag_no)};
        ContentValues target_tag_no_values = new ContentValues();
        target_tag_no_values.put(BookKeepingColumns.TAG_NO.toString(), merge_target_tag_no);
        db.update(BookKeepingTables.BASIC.toString(), target_tag_no_values, where, whereArgs);

        //删除被合并的标签
        db.delete(BookKeepingTables.TAG.toString(), where, whereArgs);

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
    public static Tag getTagByTagNo(long rno, Context context) throws SQLiteException {
        BookKeepingDbHelper db_helper = new BookKeepingDbHelper(context);
        SQLiteDatabase db = db_helper.openWriteLink();

        //查询标签编号
        String[] columns = {BookKeepingColumns.TAG_NO.toString()};
        String selection = BookKeepingColumns.RNO + "=?";
        String[] selectionArgs = {String.valueOf(rno)};
        Cursor basic_cursor = db.query(
                BookKeepingTables.BASIC.toString(),
                columns,
                selection,
                selectionArgs,
                null,
                null,
                null,
                "1"
        );

        long tag_no = 0;
        if (basic_cursor.moveToNext()) {
            tag_no = basic_cursor.getLong(basic_cursor.getColumnIndexOrThrow(BookKeepingColumns.TAG_NO.toString()));
            if (tag_no == 0)
                return new Tag("", 0);
        }

        //查询标签名称
        String[] tag_columns = {BookKeepingColumns.TAG_NAME.toString()};
        String tag_selection = BookKeepingColumns.TAG_NO + "=?";
        String[] tag_selectionArgs = {String.valueOf(tag_no)};
        Cursor tag_cursor = db.query(
                BookKeepingTables.TAG.toString(),
                tag_columns,
                tag_selection, tag_selectionArgs,
                null,
                null,
                null,
                "1"
        );

        String tag_name = "";
        if (tag_cursor.moveToNext()) {
            tag_name = tag_cursor.getString(tag_cursor.getColumnIndexOrThrow(BookKeepingColumns.TAG_NAME.toString()));
        }

        basic_cursor.close();
        tag_cursor.close();
        db.close();
        return new Tag(tag_name, tag_no);
    }

    /**
     * 获取通知解析规则的标签实例
     *
     * @param rule_no 规则编号
     * @param context 上下文
     * @return 获取到的标签实例
     * @throws SQLiteException 读取数据库可能引发的异常
     */
    @NonNull
    @Contract("_, _ -> new")
    public static Tag getTagOfAnalysisRule(long rule_no, Context context) throws SQLiteException {
        BookKeepingDbHelper db_helper = new BookKeepingDbHelper(context);
        SQLiteDatabase db = db_helper.openWriteLink();

        //查询标签编号
        String[] columns = {BookKeepingColumns.TAG_NO.toString()};
        String selection = BookKeepingColumns.RULE_NO + "=?";
        String[] selectionArgs = {String.valueOf(rule_no)};
        Cursor rule_cursor = db.query(
                BookKeepingTables.ANALYSIS_RULE.toString(),
                columns,
                selection,
                selectionArgs,
                null,
                null,
                null,
                "1"
        );

        long tag_no = 0;
        if (rule_cursor.moveToNext()) {
            tag_no = rule_cursor.getLong(rule_cursor.getColumnIndexOrThrow(BookKeepingColumns.TAG_NO.toString()));
            if (tag_no == 0)
                return new Tag("", 0);
        }

        //查询标签名称
        String[] tag_columns = {BookKeepingColumns.TAG_NAME.toString()};
        String tag_selection = BookKeepingColumns.TAG_NO + "=?";
        String[] tag_selectionArgs = {String.valueOf(tag_no)};
        Cursor tag_cursor = db.query(
                BookKeepingTables.TAG.toString(),
                tag_columns,
                tag_selection, tag_selectionArgs,
                null,
                null,
                null,
                "1"
        );

        String tag_name = "";
        if (tag_cursor.moveToNext()) {
            tag_name = tag_cursor.getString(tag_cursor.getColumnIndexOrThrow(BookKeepingColumns.TAG_NAME.toString()));
        }

        rule_cursor.close();
        tag_cursor.close();
        db.close();
        return new Tag(tag_name, tag_no);
    }
}
