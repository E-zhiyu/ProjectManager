package com.manager.assistant.data.data_class;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;

import androidx.annotation.NonNull;

import com.manager.assistant.data.data_save.database.BookKeepingColumns;
import com.manager.assistant.data.data_save.database.BookKeepingDbHelper;
import com.manager.assistant.data.data_save.database.BookKeepingTables;
import com.manager.assistant.ui.pages.bookkeeping.running_account.fragments.RunningAccountType;
import com.manager.assistant.ui.pages.setting.data_io.pojo.PojoTagGroup;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class TagGroup {
    private final List<Tag> tags;   //该分组下的标签字符串
    private String group_name;      //标签组名称
    private final long group_no;    //标签组编号

    public String getGroup_name() {
        return group_name;
    }

    public void setGroup_name(String group_name) {
        this.group_name = group_name;
    }

    public List<Tag> getTags() {
        return tags;
    }

    public long getGroup_no() {
        return group_no;
    }

    /**
     * 未指定标签的构造方法
     *
     * @param group_name 标签分组名
     * @param group_no   标签分组编号
     */
    public TagGroup(String group_name, long group_no) {
        this.group_name = group_name;
        this.group_no = group_no;
        this.tags = new ArrayList<>();
    }

    /**
     * 添加标签到该分组
     *
     * @param tag 被添加的标签名
     */
    public void addTag(Tag tag) {
        this.tags.add(tag);
    }

    /**
     * 删除标签
     *
     * @param index 待删除标签的下标
     */
    public void removeTag(int index) {
        this.tags.remove(index);
    }

    /**
     * 将标签组名称转换为编号
     *
     * @param group_name 标签组名称
     * @param context    用于打开数据库的上下文
     * @return 对应的标签编号（未找到则返回-1）
     * @throws SQLiteException 读取失败产生的数据库异常
     */
    public static long nameTransToGno(String group_name, Context context) throws SQLiteException {
        BookKeepingDbHelper db_helper = new BookKeepingDbHelper(context);
        SQLiteDatabase db = db_helper.openReadLink();

        String[] columns = {BookKeepingColumns.GROUP_NO.toString()};
        String selection = BookKeepingColumns.GROUP_NAME + "=?";
        String[] selectionArgs = {group_name};
        Cursor cursor = db.query(
                BookKeepingTables.TAG_GROUP.toString(),
                columns,
                selection,
                selectionArgs,
                null,
                null,
                null,
                "1"
        );

        long group_no;
        if (cursor.moveToNext()) {
            group_no = cursor.getLong(cursor.getColumnIndexOrThrow(BookKeepingColumns.GROUP_NO.toString()));
        } else {
            group_no = -1;
        }

        cursor.close();
        db.close();
        return group_no;
    }

    /**
     * 向数据库中保存新的标签分组
     *
     * @param group_name 新分组的名称
     * @param context    打开数据库的上下文
     * @return 新分组对应的编号
     * @throws SQLiteException 写入失败产生的数据库异常
     */
    public static long saveNewGroup(String group_name, Context context) throws SQLiteException {
        BookKeepingDbHelper db_helper = new BookKeepingDbHelper(context);
        SQLiteDatabase db = db_helper.openWriteLink();

        ContentValues group_values = new ContentValues();
        group_values.put(BookKeepingColumns.GROUP_NAME.toString(), group_name);
        long group_no = db.insert(BookKeepingTables.TAG_GROUP.toString(), null, group_values);

        db.close();
        return group_no;
    }

    /**
     * 加载标签数据但是不排除标签
     *
     * @param context 用于打开数据库的上下文
     * @return 标签分组列表
     * @throws SQLiteException 读取失败产生的数据库异常
     */
    @NonNull
    public static List<TagGroup> loadTagGroups(Context context) throws SQLiteException {
        return loadTagGroups(context, 0, null);
    }

    /**
     * 加载标签数据并排除某个标签
     *
     * @param context      用于打开数据库的上下文
     * @param excepted_tno 被排除的标签编号
     * @param scopeType    标签作用域
     * @return 标签分组列表
     * @throws SQLiteException 读取失败产生的数据库异常
     */
    @NonNull
    public static List<TagGroup> loadTagGroups(Context context, long excepted_tno, RunningAccountType scopeType) throws SQLiteException {
        BookKeepingDbHelper db_helper = new BookKeepingDbHelper(context);
        SQLiteDatabase db = db_helper.openReadLink();
        List<TagGroup> tagGroupList = new ArrayList<>();    //标签组实例列表

        //先查询分组表
        Cursor group_cursor = db.query(
                BookKeepingTables.TAG_GROUP.toString(),
                null,
                null,
                null,
                null,
                null,
                BookKeepingColumns.GROUP_NO.toString()
        );
        while (group_cursor.moveToNext()) {
            String group_name = group_cursor.getString(group_cursor.getColumnIndexOrThrow(BookKeepingColumns.GROUP_NAME.toString()));
            long group_no = group_cursor.getLong(group_cursor.getColumnIndexOrThrow(BookKeepingColumns.GROUP_NO.toString()));
            tagGroupList.add(new TagGroup(group_name, group_no));
        }

        //生成标签查询条件
        String selection = null;
        if (scopeType != null) {
            int ordinal = scopeType.ordinal();
            selection = String.format(
                    Locale.getDefault(),
                    "%s&%d==0",     //某一位为0表示这个标签对于该位数对应的序列数的种类可见
                    BookKeepingColumns.TAG_SCOPE,
                    ordinal
            );
        }

        //再查询标签表
        Cursor tagCursor = db.query(
                BookKeepingTables.TAG.toString(),
                null,
                selection,
                null,
                null,
                null,
                BookKeepingColumns.TAG_NO.toString()
        );
        while (tagCursor.moveToNext()) {
            String tag_name = tagCursor.getString(tagCursor.getColumnIndexOrThrow(BookKeepingColumns.TAG_NAME.toString()));    //标签名称
            long tag_no = tagCursor.getLong(tagCursor.getColumnIndexOrThrow(BookKeepingColumns.TAG_NO.toString()));            //标签编号
            long group_no = tagCursor.getLong(tagCursor.getColumnIndexOrThrow(BookKeepingColumns.GROUP_NO.toString()));        //分组编号
            int tag_scope = tagCursor.getInt(tagCursor.getColumnIndexOrThrow(BookKeepingColumns.TAG_SCOPE.toString()));
            Tag oneTag = new Tag(tag_name, tag_no, tag_scope);

            if (tag_no == excepted_tno) continue;   //不添加被排除的标签

            for (TagGroup group : tagGroupList) {
                if (group.getGroup_no() == group_no) {
                    group.addTag(oneTag);
                    break;
                }
            }
        }

        tagCursor.close();
        group_cursor.close();
        db.close();
        return tagGroupList;
    }

    /**
     * 获取所有标签分组（POJO类）
     *
     * @param context 上下文
     * @return 由分组POJO类组成的列表
     * @throws SQLiteException 读取失败产生的数据库异常
     */
    @NonNull
    public static List<PojoTagGroup> loadPojoTagGroups(Context context) throws SQLiteException {
        BookKeepingDbHelper db_helper = new BookKeepingDbHelper(context);
        SQLiteDatabase db = db_helper.openReadLink();
        List<PojoTagGroup> tagGroupList = new ArrayList<>();

        String[] columns = {BookKeepingColumns.GROUP_NO.toString(), BookKeepingColumns.GROUP_NAME.toString()};
        Cursor group_cursor = db.query(
                BookKeepingTables.TAG_GROUP.toString(),
                columns,
                null,
                null,
                null,
                null,
                null
        );

        while (group_cursor.moveToNext()) {
            String group_name = group_cursor.getString(group_cursor.getColumnIndexOrThrow(BookKeepingColumns.GROUP_NAME.toString()));
            long group_no = group_cursor.getLong(group_cursor.getColumnIndexOrThrow(BookKeepingColumns.GROUP_NO.toString()));

            PojoTagGroup oneGroup = new PojoTagGroup(group_name, group_no);
            tagGroupList.add(oneGroup);
        }

        group_cursor.close();
        db.close();
        return tagGroupList;
    }

    /**
     * 修改分组名称
     *
     * @param group_no   待修改的标签分组编号
     * @param group_name 新分组名称
     * @param context    上下文
     * @throws SQLiteException 数据库修改失败引发的异常
     */
    public static void modifyGroupName(long group_no, String group_name, Context context) throws SQLiteException {
        BookKeepingDbHelper db_helper = new BookKeepingDbHelper(context);
        SQLiteDatabase db = db_helper.openWriteLink();

        //修改分组名称
        String where = BookKeepingColumns.GROUP_NO + "=?";
        String[] whereArgs = {String.valueOf(group_no)};
        ContentValues group_values = new ContentValues();
        group_values.put(BookKeepingColumns.GROUP_NAME.toString(), group_name);
        db.update(BookKeepingTables.TAG_GROUP.toString(), group_values, where, whereArgs);

        db.close();
    }

    /**
     * 删除标签分组
     *
     * @param group_no 标签分组编号
     * @param context  上下文
     * @throws SQLiteException 数据库修改失败引发的异常
     */
    public static void deleteGroup(long group_no, Context context) throws SQLiteException {
        BookKeepingDbHelper db_helper = new BookKeepingDbHelper(context);
        SQLiteDatabase db = db_helper.openWriteLink();

        Tag.deleteTag(group_no, db);    //删除分组内的标签

        String where = BookKeepingColumns.GROUP_NO + "=?";
        String[] whereArgs = {String.valueOf(group_no)};
        db.delete(BookKeepingTables.TAG_GROUP.toString(), where, whereArgs);

        db.close();
    }

    /**
     * 合并标签分组
     *
     * @param merged_group_no 旧分组编号
     * @param merge_target_no 目标分组编号
     * @param context         上下文
     * @throws SQLiteException 写入数据可能引发的数据库异常
     */
    public static void mergeGroup(long merged_group_no, long merge_target_no, Context context) throws SQLiteException {
        BookKeepingDbHelper db_helper = new BookKeepingDbHelper(context);
        SQLiteDatabase db = db_helper.openWriteLink();

        //更改对应标签的分组编号
        String where = BookKeepingColumns.GROUP_NO + "=?";
        String[] whereArgs = {String.valueOf(merged_group_no)};
        ContentValues new_group_no_values = new ContentValues();
        new_group_no_values.put(BookKeepingColumns.GROUP_NO.toString(), merge_target_no);
        db.update(BookKeepingTables.TAG.toString(), new_group_no_values, where, whereArgs);

        //删除被合并的分组
        db.delete(BookKeepingTables.TAG_GROUP.toString(), where, whereArgs);

        db.close();
    }
}
