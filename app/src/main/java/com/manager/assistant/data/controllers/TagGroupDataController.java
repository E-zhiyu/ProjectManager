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
import com.manager.assistant.data.io.pojos.PojoTagGroup;
import com.manager.assistant.data.save.database.BookkeepingDbHelper;
import com.manager.assistant.data.save.database.Columns;
import com.manager.assistant.data.save.database.Tables;
import com.manager.assistant.ui.pages.bookkeeping.running_account.fragments.RunningAccountType;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

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
     * @param group_name 新分组的名称
     * @param context    打开数据库的上下文
     * @return 新分组对应的编号
     * @throws SQLiteException 写入失败产生的数据库异常
     */
    public static long saveNewGroup(String group_name, Context context) throws SQLiteException {
        BookkeepingDbHelper dbHelper = new BookkeepingDbHelper(context);
        SQLiteDatabase db = dbHelper.openWriteLink();

        ContentValues groupValues = new ContentValues();
        groupValues.put(Columns.GROUP_NAME.toString(), group_name);
        long groupNo = db.insert(Tables.TAG_GROUP.toString(), null, groupValues);

        db.close();
        return groupNo;
    }

    /**
     * 加载单个分组
     *
     * @param context 上下文
     * @param groupNo 需要加载的分组编号
     * @return 标签分组字典（k:标签分组，v:标签列表）
     * @throws SQLiteException 读取失败引发的异常
     */
    @NonNull
    public static Map<TagGroup, List<Tag>> loadSingleTagGroup(Context context, long groupNo) throws SQLiteException {
        return loadTagGroup(context, 0, groupNo, null);
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
        return loadTagGroup(context, 0, -1, null);
    }

    /**
     * 加载所有标签分组并排除某个标签
     *
     * @param context       用于打开数据库的上下文
     * @param targetGroupNo 需要查询单个分组时对应分组的编号（传递-1以忽略该选择条件）
     * @param exceptedTno   被排除的标签编号（传递0以忽略该选择条件）
     * @param scopeType     标签作用域
     * @return 标签分组字典，并按照分组编号升序排序（k:标签分组，v:标签列表）
     * @throws SQLiteException 读取失败产生的数据库异常
     */
    @NonNull
    public static Map<TagGroup, List<Tag>> loadTagGroup(
            Context context,
            long exceptedTno,
            long targetGroupNo,
            @Nullable RunningAccountType scopeType
    ) throws SQLiteException {
        BookkeepingDbHelper dbHelper = new BookkeepingDbHelper(context);
        SQLiteDatabase db = dbHelper.openReadLink();

        //生成标签查询条件
        List<String> selectionArgs = new ArrayList<>();
        StringBuilder selectionBuilder = new StringBuilder("1=1");

        //生成排除某个标签的条件
        if (exceptedTno != 0) {
            selectionBuilder.append(" AND ");
            selectionBuilder.append(Columns.TAG_NO);
            selectionBuilder.append("!=?");
            selectionArgs.add(String.valueOf(exceptedTno));
        }

        //生成过滤标签作用域的条件
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

        //生成过滤标签分组编号的条件
        if (targetGroupNo != -1) {
            selectionBuilder.append(" AND ");
            selectionBuilder.append(Columns.GROUP_NO);
            selectionBuilder.append("=?");
            selectionArgs.add(String.valueOf(targetGroupNo));
        }

        //生成查询游标
        Cursor tagCursor = db.query(
                Tables.TAG.toString(),
                null,
                selectionBuilder.toString(),
                selectionArgs.toArray(new String[0]),
                null,
                null,
                Columns.TAG_NO.toString()
        );

        //开始查询
        Map<TagGroup, List<Tag>> groupMap = new TreeMap<>(Comparator.comparingLong(TagGroup::getGroupNo));
        while (tagCursor.moveToNext()) {
            String tagName = tagCursor.getString(tagCursor.getColumnIndexOrThrow(Columns.TAG_NAME.toString()));     //标签名称
            long tagNo = tagCursor.getLong(tagCursor.getColumnIndexOrThrow(Columns.TAG_NO.toString()));             //标签编号
            long groupNo = tagCursor.getLong(tagCursor.getColumnIndexOrThrow(Columns.GROUP_NO.toString()));         //分组编号
            int tagScope = tagCursor.getInt(tagCursor.getColumnIndexOrThrow(Columns.TAG_SCOPE.toString()));

            Tag oneTag = new Tag(tagName, tagNo, tagScope);
            TagGroup group = new TagGroup(groupNo);
            List<Tag> tagList = groupMap.get(group);
            if (tagList == null) {
                tagList = new ArrayList<>();
                groupMap.put(group, tagList);

                //获取分组的名称
                String groupName = TagGroupDataController.getGroupName(db, groupNo);
                group.setGroupName(groupName);
            }
            tagList.add(oneTag);
        }

        tagCursor.close();
        db.close();
        return groupMap;
    }

    /**
     * 获取分组名称
     *
     * @param db      数据库实例
     * @param groupNo 分组编号
     * @return 分组编号对应的分组名称
     * @throws SQLiteException 数据读取失败引发的异常
     */
    public static String getGroupName(@NonNull SQLiteDatabase db, long groupNo) throws SQLiteException {
        String selection = Columns.GROUP_NO + "=?";
        String[] selectionArgs = {String.valueOf(groupNo)};
        String[] columns = {Columns.GROUP_NAME.toString()};

        Cursor groupCursor = db.query(
                Tables.TAG_GROUP.toString(),
                columns,
                selection,
                selectionArgs,
                null,
                null,
                null
        );

        String groupName = null;
        if (groupCursor.moveToFirst()) {
            groupName = groupCursor.getString(groupCursor.getColumnIndexOrThrow(Columns.GROUP_NAME.toString()));
        }

        groupCursor.close();
        return groupName;
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
        BookkeepingDbHelper dbHelper = new BookkeepingDbHelper(context);
        SQLiteDatabase db = dbHelper.openReadLink();
        List<PojoTagGroup> tagGroupList = new ArrayList<>();

        String[] columns = {Columns.GROUP_NO.toString(), Columns.GROUP_NAME.toString()};
        Cursor groupCursor = db.query(
                Tables.TAG_GROUP.toString(),
                columns,
                null,
                null,
                null,
                null,
                null
        );

        while (groupCursor.moveToNext()) {
            String group_name = groupCursor.getString(groupCursor.getColumnIndexOrThrow(Columns.GROUP_NAME.toString()));
            long group_no = groupCursor.getLong(groupCursor.getColumnIndexOrThrow(Columns.GROUP_NO.toString()));

            PojoTagGroup oneGroup = new PojoTagGroup(group_name, group_no);
            tagGroupList.add(oneGroup);
        }

        groupCursor.close();
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
