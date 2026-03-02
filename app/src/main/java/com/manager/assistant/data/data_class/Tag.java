package com.manager.assistant.data.data_class;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.manager.assistant.data.data_class.running_account.RunningAccountBase;
import com.manager.assistant.data.data_save.database.Columns;
import com.manager.assistant.data.data_save.database.BookkeepingDbHelper;
import com.manager.assistant.data.data_save.database.Tables;

import org.jetbrains.annotations.Contract;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Tag {
    private String name;    //名称
    private final long tno; //编号
    private int scope;      //作用域

    public Tag(String name, long tno, int scope) {
        this.name = name;
        this.tno = tno;
        this.scope = scope;
    }

    public Tag(String name, long tno) {
        this.name = name;
        this.tno = tno;
        this.scope = 0;
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

    public int getScope() {
        return scope;
    }

    public void setScope(int scope) {
        this.scope = scope;
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

        int tag_no;
        if (cursor.moveToNext()) {
            tag_no = cursor.getInt(cursor.getColumnIndexOrThrow(Columns.TAG_NO.toString()));
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
        BookkeepingDbHelper db_helper = new BookkeepingDbHelper(context);
        SQLiteDatabase db = db_helper.openReadLink();

        String[] columns = {Columns.TAG_NAME.toString()};
        String selection = Columns.TAG_NO + "=?";
        String[] selectionArgs = {String.valueOf(tag_no)};
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

        String tag_name;
        if (cursor.moveToNext()) {
            tag_name = cursor.getString(cursor.getColumnIndexOrThrow(Columns.TAG_NAME.toString()));
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
            long tag_no = cursor.getLong(cursor.getColumnIndexOrThrow(Columns.TAG_NO.toString()));
            int scope = cursor.getInt(cursor.getColumnIndexOrThrow(Columns.TAG_SCOPE.toString()));
            tagList.add(new Tag(tagName, tag_no, scope));
        }

        cursor.close();
        db.close();
        return tagList;
    }

    /**
     * 保存新的标签到数据库
     *
     * @param tagName   标签名称
     * @param tag_scope 标签作用域
     * @param group_no  该标签对应的分组编号
     * @param context   用于打开数据库的上下文
     * @return 对应的标签编号
     * @throws SQLiteException 无法修改数据库时引发的异常
     */
    public static long saveNewTag(String tagName, int tag_scope, long group_no, Context context) throws SQLiteException {
        long tag_no;    //标签编号

        BookkeepingDbHelper db_helper = new BookkeepingDbHelper(context);
        SQLiteDatabase db = db_helper.openWriteLink();

        ContentValues tagValues = new ContentValues();
        tagValues.put(Columns.TAG_NAME.toString(), tagName);
        tagValues.put(Columns.TAG_SCOPE.toString(), tag_scope);
        tagValues.put(Columns.GROUP_NO.toString(), group_no);
        tag_no = db.insert(Tables.TAG.toString(), null, tagValues);

        db.close();
        return tag_no;
    }

    /**
     * 修改标签（不修改所属分组）
     *
     * @param new_name  新标签名称
     * @param tag_no    标签编号
     * @param tag_scope 标签作用域
     * @param context   打开数据库所需的上下文
     * @throws SQLiteException 无法修改数据库时引发的异常
     */
    public static void modifyTag(String new_name, long tag_no, int tag_scope, Context context) throws SQLiteException {
        ContentValues tagValues = new ContentValues();
        tagValues.put(Columns.TAG_NAME.toString(), new_name);
        tagValues.put(Columns.TAG_SCOPE.toString(), tag_scope);
        String whereStr = Columns.TAG_NO + "=?";
        String[] whereStrArgs = {String.valueOf(tag_no)};

        BookkeepingDbHelper db_helper = new BookkeepingDbHelper(context);
        SQLiteDatabase db = db_helper.openWriteLink();

        db.update(
                Tables.TAG.toString(),
                tagValues,
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
     * @param tag_scope    标签作用域
     * @param new_group_no 新分组编号
     * @param context      打开数据库所需的上下文
     * @throws SQLiteException 无法修改数据库时引发的异常
     */
    public static void modifyTag(String new_tag_name, long tag_no, int tag_scope, long new_group_no, Context context) throws SQLiteException {
        ContentValues tagValues = new ContentValues();
        tagValues.put(Columns.TAG_NAME.toString(), new_tag_name);
        tagValues.put(Columns.TAG_SCOPE.toString(), tag_scope);
        tagValues.put(Columns.GROUP_NO.toString(), new_group_no);
        String whereStr = Columns.TAG_NO + "=?";
        String[] whereStrArgs = {String.valueOf(tag_no)};

        BookkeepingDbHelper db_helper = new BookkeepingDbHelper(context);
        SQLiteDatabase db = db_helper.openWriteLink();

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

        RunningAccountBase.onTagDeleted(tagNo, db); //清除流水记录里面的标签编号
        AnalysisRule.onTagDeleted(tagNo, db);       //清除通知解析规则中的标签编号
        Budget.onTagDeleted(tagNo, db);             //删除预算中的标签编号数据

        //再删除对应标签
        db.delete(
                Tables.TAG.toString(),
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
        String[] columns = {Columns.TAG_NO.toString()};
        String selection = Columns.GROUP_NO + "=?";
        String[] selectionArgs = {String.valueOf(group_no)};
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
            long tag_no = tagCursor.getLong(tagCursor.getColumnIndexOrThrow(Columns.TAG_NO.toString()));
            RunningAccountBase.onTagDeleted(tag_no, db); //清除流水记录里面的标签编号
            AnalysisRule.onTagDeleted(tag_no, db);       //清除通知解析规则中的标签编号
        }
        tagCursor.close();

        String where = Columns.GROUP_NO + "=?";
        String[] whereArgs = {String.valueOf(group_no)};
        db.delete(Tables.TAG.toString(), where, whereArgs);
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
        BookkeepingDbHelper dbHelper = new BookkeepingDbHelper(context);
        SQLiteDatabase db = dbHelper.openWriteLink();

        //更改对应流水记录的标签
        String where = Columns.TAG_NO + "=?";
        String[] whereArgs = {String.valueOf(merged_tag_no)};
        ContentValues targetTagNoValues = new ContentValues();
        targetTagNoValues.put(Columns.TAG_NO.toString(), merge_target_tag_no);
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
        long tag_no = 0;
        if (basicCursor.moveToNext()) {
            tag_no = basicCursor.getLong(basicCursor.getColumnIndexOrThrow(Columns.TAG_NO.toString()));
            if (tag_no == 0)
                return new Tag("", 0, 0);
        }

        //查询标签名称
        String[] tagColumns = {
                Columns.TAG_NAME.toString(),
                Columns.TAG_SCOPE.toString()
        };
        String tagSelection = Columns.TAG_NO + "=?";
        String[] tagSelectionArgs = {String.valueOf(tag_no)};
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
        return new Tag(tagName, tag_no, scope);
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
    public static Tag getTagByRuleNo(long rule_no, Context context) throws SQLiteException {
        BookkeepingDbHelper dbHelper = new BookkeepingDbHelper(context);
        SQLiteDatabase db = dbHelper.openWriteLink();

        //查询标签编号
        String[] columns = {Columns.TAG_NO.toString()};
        String selection = Columns.RULE_NO + "=?";
        String[] selectionArgs = {String.valueOf(rule_no)};
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

        long tag_no = 0;
        if (ruleCursor.moveToNext()) {
            tag_no = ruleCursor.getLong(ruleCursor.getColumnIndexOrThrow(Columns.TAG_NO.toString()));
            if (tag_no == 0)
                return new Tag("", 0, 0);
        }

        //查询标签名称
        String[] tagColumns = {
                Columns.TAG_NAME.toString(),
                Columns.TAG_SCOPE.toString()
        };
        String tagSelection = Columns.TAG_NO + "=?";
        String[] tagSelectionArgs = {String.valueOf(tag_no)};
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
        return new Tag(tagName, tag_no, scope);
    }

    /**
     * 获取标签数量
     *
     * @param context 上下文
     * @return 标签数量
     * @throws SQLiteException 读取失败引发的异常
     */
    public static int getTagCount(Context context) throws SQLiteException {
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
