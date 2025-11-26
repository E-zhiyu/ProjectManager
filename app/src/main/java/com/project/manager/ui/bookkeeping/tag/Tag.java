package com.project.manager.ui.bookkeeping.tag;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteStatement;

import androidx.annotation.NonNull;

import com.project.manager.database.BookKeepingColumns;
import com.project.manager.database.BookKeepingDatabaseHelper;
import com.project.manager.database.BookKeepingTables;

import org.jetbrains.annotations.Contract;

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
        BookKeepingDatabaseHelper db_helper = new BookKeepingDatabaseHelper(context);
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
     * @return 对应的标签名称
     * @throws SQLiteException 无法修改数据库时引发的异常
     */
    public static String tagNoTransToName(long tag_no, Context context) throws SQLiteException {
        BookKeepingDatabaseHelper db_helper = new BookKeepingDatabaseHelper(context);
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

        BookKeepingDatabaseHelper db_helper = new BookKeepingDatabaseHelper(context);
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

        BookKeepingDatabaseHelper db_helper = new BookKeepingDatabaseHelper(context);
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

        BookKeepingDatabaseHelper db_helper = new BookKeepingDatabaseHelper(context);
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
        BookKeepingDatabaseHelper db_helper = new BookKeepingDatabaseHelper(context);
        SQLiteDatabase db = db_helper.openWriteLink();

        ContentValues basic_values = new ContentValues();
        basic_values.put(BookKeepingColumns.TAG_NO.toString(), 0);
        String whereStr = BookKeepingColumns.TAG_NO + "=?";
        String[] whereStrArgs = {String.valueOf(tag_no)};

        //先将流水基本数据表的标签清除（修改为0）
        db.update(
                BookKeepingTables.BASIC.toString(),
                basic_values,
                whereStr,
                whereStrArgs
        );

        //再删除对应标签
        db.delete(
                BookKeepingTables.TAG.toString(),
                whereStr,
                whereStrArgs
        );

        db.close();
    }

    /**
     * 批量删除标签
     *
     * @param tagList 待删除的标签列表
     * @param context 上下文
     * @throws SQLiteException 无法修改数据库时引发的异常
     */
    public static void deleteTag(@NonNull List<Tag> tagList, Context context) throws SQLiteException {
        BookKeepingDatabaseHelper db_helper = new BookKeepingDatabaseHelper(context);
        SQLiteDatabase db = db_helper.openWriteLink();

        long[] tag_no_list = new long[tagList.size()];
        int index = 0;
        for (Tag tag : tagList) {
            tag_no_list[index] = tag.getTno();
            index++;
        }

        StringBuilder placeholders = new StringBuilder();
        for (int i = 0; i < tagList.size(); i++) {
            placeholders.append(i == 0 ? "?" : ",?");
        }

        //清除流水表的标签
        String sql;
        SQLiteStatement stmt;
        sql = "UPDATE " + BookKeepingTables.BASIC +
                " SET " + BookKeepingColumns.TAG_NO + " =0" +
                " WHERE " + BookKeepingColumns.TAG_NO + " IN (" + placeholders + ")";
        stmt = db.compileStatement(sql);
        index = 1;
        for (long tagNo : tag_no_list) {
            stmt.bindLong(index++, tagNo);
        }
        stmt.execute();

        //删除标签表的记录
        sql = "DELETE FROM " + BookKeepingTables.TAG +
                " WHERE " + BookKeepingColumns.TAG_NO + " IN (" + placeholders + ")";
        stmt = db.compileStatement(sql);
        index = 1;
        for (long tagNo : tag_no_list) {
            stmt.bindLong(index++, tagNo);
        }
        stmt.execute();

        db.close();
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
        BookKeepingDatabaseHelper db_helper = new BookKeepingDatabaseHelper(context);
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
    public static Tag getTagByRno(long rno, Context context) throws SQLiteException {
        BookKeepingDatabaseHelper db_helper = new BookKeepingDatabaseHelper(context);
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
}
