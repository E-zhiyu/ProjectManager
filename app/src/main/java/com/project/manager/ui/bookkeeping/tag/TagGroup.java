package com.project.manager.ui.bookkeeping.tag;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;

import com.project.manager.database.RunningAccountColumns;
import com.project.manager.database.RunningAccountDatabaseHelper;
import com.project.manager.database.RunningAccountTables;

import java.util.ArrayList;
import java.util.List;

public class TagGroup {
    private final List<Tag> tags;   //该分组下的标签字符串
    private final String group_name;      //标签组名称
    private final long group_no;    //标签组编号

    public String getGroupName() {
        return group_name;
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
     * @return 对应的标签编号（未找到则返回0）
     */
    public static long nameTransToGno(String group_name, Context context) throws SQLiteException {
        RunningAccountDatabaseHelper db_helper = new RunningAccountDatabaseHelper(context);
        SQLiteDatabase db = db_helper.openReadLink();

        String[] columns = {RunningAccountColumns.GROUP_NO.toString()};
        String selection = RunningAccountColumns.GROUP_NAME + "=?";
        String[] selectionArgs = {group_name};
        Cursor cursor = db.query(
                RunningAccountTables.TAG_GROUP.toString(),
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
            group_no = cursor.getLong(cursor.getColumnIndexOrThrow(RunningAccountColumns.GROUP_NO.toString()));
        } else {
            group_no = 0;
        }

        cursor.close();
        db.close();
        return group_no;
    }

    /**
     * 将标签组编号转换为标签组名称
     *
     * @param group_no 标签组编号
     * @param context  用于打开数据库的上下文
     * @return 对应的标签名称
     */
    public static String groupNoTransToName(long group_no, Context context) throws SQLiteException {
        String group_name;
        RunningAccountDatabaseHelper db_helper = new RunningAccountDatabaseHelper(context);
        SQLiteDatabase db = db_helper.openReadLink();

        String[] columns = {RunningAccountColumns.GROUP_NAME.toString()};
        String selection = RunningAccountColumns.GROUP_NO + "=?";
        String[] selectionArgs = {String.valueOf(group_no)};
        Cursor cursor = db.query(
                RunningAccountTables.TAG_GROUP.toString(),
                columns,
                selection,
                selectionArgs,
                null,
                null,
                null,
                "1"
        );

        if (cursor.moveToNext()) {
            group_name = cursor.getString(cursor.getColumnIndexOrThrow(RunningAccountColumns.GROUP_NAME.toString()));
        } else {
            group_name = "";
        }

        cursor.close();
        db.close();
        return group_name;
    }

    /**
     * 向数据库中保存新的标签分组
     *
     * @param group_name 新分组的名称
     * @param context    打开数据库的上下文
     * @return 新分组对应的编号
     */
    public static long saveNewGroup(String group_name, Context context) throws SQLiteException {
        RunningAccountDatabaseHelper db_helper = new RunningAccountDatabaseHelper(context);
        SQLiteDatabase db = db_helper.openWriteLink();

        ContentValues group_values = new ContentValues();
        group_values.put(RunningAccountColumns.GROUP_NAME.toString(), group_name);
        long group_no = db.insert(RunningAccountTables.TAG_GROUP.toString(), null, group_values);

        db.close();
        return group_no;
    }

    /**
     * 加载标签数据
     *
     * @param context 用于打开数据库的上下文
     * @return 标签分组列表
     */
    public static List<TagGroup> loadTagGroups(Context context) throws SQLiteException {
        RunningAccountDatabaseHelper db_helper = new RunningAccountDatabaseHelper(context);
        SQLiteDatabase db = db_helper.openReadLink();

        //查询标签并分组
        Cursor tag_cursor = db.query(
                RunningAccountTables.TAG + " NATURAL JOIN " + RunningAccountTables.TAG_GROUP,
                null,
                null,
                null,
                null,
                null,
                null
        );

        List<TagGroup> tagGroupList = new ArrayList<>();    //标签组实例列表
        while (tag_cursor.moveToNext()) {
            String tag_name = tag_cursor.getString(tag_cursor.getColumnIndexOrThrow(RunningAccountColumns.TAG_NAME.toString()));      //标签名称
            long tag_no = tag_cursor.getLong(tag_cursor.getColumnIndexOrThrow(RunningAccountColumns.TAG_NO.toString()));               //标签编号
            long group_no = tag_cursor.getLong(tag_cursor.getColumnIndexOrThrow(RunningAccountColumns.GROUP_NO.toString()));           //分组编号
            String group_name = tag_cursor.getString(tag_cursor.getColumnIndexOrThrow(RunningAccountColumns.GROUP_NAME.toString()));  //分组名称                                //分组名称

            boolean isGroupFound = false;   //判断是否找到同号分组
            for (TagGroup group : tagGroupList) {
                if (group.group_no == group_no) {
                    group.addTag(new Tag(tag_name, tag_no));    //找到同号分组：将标签添加至该分组
                    isGroupFound = true;
                    break;
                }
            }
            if (!isGroupFound) {
                TagGroup newGroup = new TagGroup(group_name, group_no);
                newGroup.addTag(new Tag(tag_name, tag_no));     //找不到同号分组：新建分组并添加标签至该分组
                tagGroupList.add(newGroup);
            }
        }

        tag_cursor.close();
        db.close();
        return tagGroupList;
    }
}
