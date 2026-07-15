package com.manager.assistant.data.controllers;

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
import com.manager.assistant.auxiliary.enums.AccountType;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class TagGroupDataController {

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
            @Nullable AccountType scopeType
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

}
