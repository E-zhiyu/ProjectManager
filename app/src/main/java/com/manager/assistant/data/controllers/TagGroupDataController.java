package com.manager.assistant.data.controllers;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.manager.assistant.data.classes.Tag;
import com.manager.assistant.data.classes.TagGroup;
import com.manager.assistant.data.save.database.BookkeepingDbHelper;
import com.manager.assistant.data.save.database.Columns;
import com.manager.assistant.data.save.database.Tables;
import com.manager.assistant.ui.pages.bookkeeping.running_account.fragments.RunningAccountType;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class TagGroupDataController {
    /**
     * 将标签组名称转换为编号
     *
     * @param groupName 标签组名称
     * @param context   用于打开数据库的上下文
     * @return 对应的标签编号（未找到则返回-1）
     * @throws SQLiteException 读取失败产生的数据库异常
     */
    public static long nameTransToGno(String groupName, Context context) throws SQLiteException {
        BookkeepingDbHelper dbHelper = new BookkeepingDbHelper(context);
        SQLiteDatabase db = dbHelper.openReadLink();

        String[] columns = {
                Columns.GROUP_NO.toString()
        };
        String selection = Columns.GROUP_NAME + "=?";
        String[] selectionArgs = {groupName};
        Cursor cursor = db.query(
                Tables.TAG_GROUP.toString(),
                columns,
                selection,
                selectionArgs,
                null,
                null,
                null,
                "1"
        );

        long groupNo;
        if (cursor.moveToFirst()) {
            groupNo = cursor.getLong(cursor.getColumnIndexOrThrow(Columns.GROUP_NO.toString()));
        } else {
            groupNo = -1;
        }

        cursor.close();
        db.close();
        return groupNo;
    }

    /**
     * 向数据库中保存新的标签分组
     *
     * @param groupName 新分组的名称
     * @param context   打开数据库的上下文
     * @return 新分组对应的编号
     * @throws SQLiteException 写入失败产生的数据库异常
     */
    public static long saveNewGroup(String groupName, Context context) throws SQLiteException {
        BookkeepingDbHelper dbHelper = new BookkeepingDbHelper(context);
        SQLiteDatabase db = dbHelper.openWriteLink();

        ContentValues groupValues = new ContentValues();
        groupValues.put(Columns.GROUP_NAME.toString(), groupName);
        long groupNo = db.insert(Tables.TAG_GROUP.toString(), null, groupValues);

        db.close();
        return groupNo;
    }

    /**
     * 加载所有标签分组但是不排除标签
     *
     * @param context 用于打开数据库的上下文
     * @return 标签分组字典，并按照分组编号升序排序（k:标签分组，v:标签列表）
     * @throws SQLiteException 读取失败产生的数据库异常
     */
    @NonNull
    public static Map<TagGroup, List<Tag>> loadTagGroup(Context context) throws SQLiteException {
        return loadTagGroup(context, 0, null);
    }

    /**
     * 加载所有标签分组并排除某个标签
     *
     * @param context       用于打开数据库的上下文
     * @param excludedTagNo 被排除的标签编号（传递0以忽略该选择条件）
     * @param scopeType     标签作用域
     * @return 标签分组字典，并按照分组编号升序排序（k:标签分组，v:标签列表）
     * @throws SQLiteException 读取失败产生的数据库异常
     */
    @NonNull
    public static Map<TagGroup, List<Tag>> loadTagGroup(
            Context context,
            long excludedTagNo,
            @Nullable RunningAccountType scopeType
    ) throws SQLiteException {
        BookkeepingDbHelper dbHelper = new BookkeepingDbHelper(context);
        SQLiteDatabase db = dbHelper.openReadLink();

        //查询所有分组
        List<TagGroup> groupList = getTagGroup(db);

        //根据分组编号依次查询组内标签
        Map<TagGroup, List<Tag>> groupMap = new LinkedHashMap<>();
        for (TagGroup group : groupList) {
            List<Tag> tagList = TagDataController.getTags(db, group.getGroupNo(), excludedTagNo, scopeType);

            groupMap.put(group, tagList);
        }

        db.close();
        return groupMap;
    }

    /**
     * 获取指定编号的分组实例
     *
     * @param context 上下文
     * @return 包含所有标签分组的列表
     * @throws SQLiteException 数据读取失败引发的异常
     */
    @NonNull
    public static List<TagGroup> getTagGroup(Context context) throws SQLiteException {
        BookkeepingDbHelper dbHelper = new BookkeepingDbHelper(context);
        SQLiteDatabase db = dbHelper.openReadLink();

        List<TagGroup> groupList = getTagGroup(db);

        db.close();
        return groupList;
    }

    /**
     * 获取指定编号的分组实例
     *
     * @param db 数据库实例
     * @return 包含所有标签分组的列表
     * @throws SQLiteException 数据读取失败引发的异常
     */
    @NonNull
    public static List<TagGroup> getTagGroup(@NonNull SQLiteDatabase db) throws SQLiteException {
        //生成游标
        Cursor groupCursor = db.query(
                Tables.TAG_GROUP.toString(),
                null,
                null,
                null,
                null,
                null,
                Columns.GROUP_NO.toString() //分组编号升序排序
        );

        //开始查询
        List<TagGroup> groupList = new ArrayList<>();
        while (groupCursor.moveToNext()) {
            String groupName = groupCursor.getString(groupCursor.getColumnIndexOrThrow(Columns.GROUP_NAME.toString()));
            long groupNo = groupCursor.getLong(groupCursor.getColumnIndexOrThrow(Columns.GROUP_NO.toString()));
            groupList.add(new TagGroup(groupName, groupNo));
        }

        groupCursor.close();
        return groupList;
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
        BookkeepingDbHelper dbHelper = new BookkeepingDbHelper(context);
        SQLiteDatabase db = dbHelper.openWriteLink();

        //修改分组名称
        String where = Columns.GROUP_NO + "=?";
        String[] whereArgs = {String.valueOf(group_no)};
        ContentValues group_values = new ContentValues();
        group_values.put(Columns.GROUP_NAME.toString(), group_name);
        db.update(Tables.TAG_GROUP.toString(), group_values, where, whereArgs);

        db.close();
    }

    /**
     * 删除标签分组
     *
     * @param groupNo 标签分组编号
     * @param context 上下文
     * @throws SQLiteException 数据库修改失败引发的异常
     */
    public static void deleteGroup(long groupNo, Context context) throws SQLiteException {
        BookkeepingDbHelper dbHelper = new BookkeepingDbHelper(context);
        SQLiteDatabase db = dbHelper.openWriteLink();

        TagDataController.deleteTag(groupNo, db);    //删除分组内的标签

        String where = Columns.GROUP_NO + "=?";
        String[] whereArgs = {String.valueOf(groupNo)};
        db.delete(Tables.TAG_GROUP.toString(), where, whereArgs);

        db.close();
    }

    /**
     * 合并标签分组
     *
     * @param mergedGroupNo 旧分组编号
     * @param mergeTargetNo 目标分组编号
     * @param context       上下文
     * @throws SQLiteException 写入数据可能引发的数据库异常
     */
    public static void mergeGroup(long mergedGroupNo, long mergeTargetNo, Context context) throws SQLiteException {
        BookkeepingDbHelper dbHelper = new BookkeepingDbHelper(context);
        SQLiteDatabase db = dbHelper.openWriteLink();

        //更改对应标签的分组编号
        String where = Columns.GROUP_NO + "=?";
        String[] whereArgs = {String.valueOf(mergedGroupNo)};
        ContentValues newGroupNoValues = new ContentValues();
        newGroupNoValues.put(Columns.GROUP_NO.toString(), mergeTargetNo);
        db.update(Tables.TAG.toString(), newGroupNoValues, where, whereArgs);

        //删除被合并的分组
        db.delete(Tables.TAG_GROUP.toString(), where, whereArgs);

        db.close();
    }
}
