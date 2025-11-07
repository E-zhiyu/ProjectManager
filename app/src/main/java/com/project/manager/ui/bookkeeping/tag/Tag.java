package com.project.manager.ui.bookkeeping.tag;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabaseLockedException;
import android.widget.Toast;

import com.project.manager.database.FlowColumns;
import com.project.manager.database.FlowDatabaseHelper;
import com.project.manager.database.FlowTables;

import java.util.ArrayList;
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
     */
    public static long nameTransToTno(String name, Context context) {
        try (FlowDatabaseHelper db_helper = new FlowDatabaseHelper(context)) {
            SQLiteDatabase db = db_helper.openReadLink();

            String[] columns = {FlowColumns.TAG_NO.toString()};
            String selection = FlowColumns.TAG_NAME + "=?";
            String[] selectionArgs = {name};
            Cursor cursor = db.query(
                    FlowTables.TAG.toString(),
                    columns,
                    selection,
                    selectionArgs,
                    null,
                    null,
                    null,
                    "1"
            );

            long tag_no;
            if (cursor.moveToNext()) {
                tag_no = cursor.getLong(cursor.getColumnIndexOrThrow(FlowColumns.TAG_NO.toString()));
            } else {
                tag_no = 0;
            }

            cursor.close();
            db.close();
            return tag_no;
        } catch (SQLiteDatabaseLockedException e) {
            throw new RuntimeException("无法打开数据库：数据库被其他进程占用");
        }
    }

    /**
     * 将标签编号转换为标签名称
     *
     * @param tag_no  标签编号
     * @param context 用于打开数据库的上下文
     * @return 对应的标签名称
     */
    public static String tagNoTransToName(long tag_no, Context context) {
        try (FlowDatabaseHelper db_helper = new FlowDatabaseHelper(context)) {
            SQLiteDatabase db = db_helper.openReadLink();

            String[] columns = {FlowColumns.TAG_NAME.toString()};
            String selection = FlowColumns.TAG_NO + "=?";
            String[] selectionArgs = {String.valueOf(tag_no)};
            Cursor cursor = db.query(
                    FlowTables.TAG.toString(),
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
                tag_name = cursor.getString(cursor.getColumnIndexOrThrow(FlowColumns.TAG_NAME.toString()));
            } else {
                tag_name = "";
            }

            cursor.close();
            db.close();
            return tag_name;
        } catch (SQLiteDatabaseLockedException e) {
            throw new RuntimeException("无法打开数据库：数据库被其他进程占用");
        }
    }

    /**
     * 获取所有标签实例
     *
     * @param context 打开数据库所需的上下文
     * @return 标签实例列表
     */
    public static List<Tag> getAllTags(Context context) {
        List<Tag> allTagList = new ArrayList<>();

        try (FlowDatabaseHelper db_helper = new FlowDatabaseHelper(context)) {
            SQLiteDatabase db = db_helper.openWriteLink();

            String[] columns = {FlowColumns.TAG_NO.toString(), FlowColumns.TAG_NAME.toString()};
            String orderBy = FlowColumns.TAG_NO + " DESC";
            Cursor tag_cursor = db.query(
                    FlowTables.TAG.toString(),
                    columns,
                    null,
                    null,
                    null,
                    null,
                    orderBy
            );

            while (tag_cursor.moveToNext()) {
                long tag_no = tag_cursor.getLong(tag_cursor.getColumnIndexOrThrow(FlowColumns.TAG_NO.toString()));
                String tag_name = tag_cursor.getString(tag_cursor.getColumnIndexOrThrow(FlowColumns.TAG_NAME.toString()));
                Tag oneTag = new Tag(tag_name, tag_no);
                allTagList.add(oneTag);
            }

            tag_cursor.close();
            db.close();
        } catch (SQLiteDatabaseLockedException e) {
            throw new RuntimeException("无法打开数据库：数据库被其他进程占用");
        }

        return allTagList;
    }

    /**
     * 保存新的标签到数据库
     *
     * @param tag_name 标签名称
     * @param group_no 该标签对应的分组编号
     * @param context  用于打开数据库的上下文
     * @return 对应的标签编号
     */
    public static long saveNewTag(String tag_name, long group_no, Context context) {
        String tip_str = "标签保存失败";
        long tag_no = 0;    //标签编号
        try (FlowDatabaseHelper db_helper = new FlowDatabaseHelper(context)) {
            SQLiteDatabase db = db_helper.openWriteLink();

            ContentValues tag_values = new ContentValues();
            tag_values.put(FlowColumns.TAG_NAME.toString(), tag_name);
            tag_values.put(FlowColumns.GROUP_NO.toString(), group_no);
            tag_no = db.insert(FlowTables.TAG.toString(), null, tag_values);

            db.close();
        } catch (SQLiteDatabaseLockedException e) {
            tip_str = "标签保存失败：数据库被其他进程占用";
            e.printStackTrace();
        } finally {
            Toast.makeText(context, tip_str, Toast.LENGTH_SHORT).show();
        }

        return tag_no;
    }

    /**
     * 修改标签（不修改所属分组）
     *
     * @param new_name 新标签名称
     * @param tag_no   标签编号
     * @param context  打开数据库所需的上下文
     */
    public static void modifyTag(String new_name, long tag_no, Context context) {
        ContentValues tag_values = new ContentValues();
        tag_values.put(FlowColumns.TAG_NAME.toString(), new_name);
        String whereStr = FlowColumns.TAG_NO + "=?";
        String[] whereStrArgs = {String.valueOf(tag_no)};

        String tip_str = "标签修改失败";
        try (FlowDatabaseHelper db_helper = new FlowDatabaseHelper(context)) {
            SQLiteDatabase db = db_helper.openWriteLink();

            db.update(
                    FlowTables.TAG.toString(),
                    tag_values,
                    whereStr,
                    whereStrArgs
            );

            db.close();
            tip_str = "标签修改成功";
        } catch (SQLiteDatabaseLockedException e) {
            tip_str = "标签修改失败：数据库被其他进程占用";
            e.printStackTrace();
        } finally {
            Toast.makeText(context, tip_str, Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 修改标签（修改所属分组）
     *
     * @param new_tag_name 新标签名称
     * @param tag_no       待修改的标签编号
     * @param new_group_no 新分组编号
     * @param context      打开数据库所需的上下文
     */
    public static void modifyTag(String new_tag_name, long tag_no, long new_group_no, Context context) {
        ContentValues tag_values = new ContentValues();
        tag_values.put(FlowColumns.TAG_NAME.toString(), new_tag_name);
        tag_values.put(FlowColumns.GROUP_NO.toString(), new_group_no);
        String whereStr = FlowColumns.TAG_NO + "=?";
        String[] whereStrArgs = {String.valueOf(tag_no)};

        String tip_str = "标签修改失败";
        try (FlowDatabaseHelper db_helper = new FlowDatabaseHelper(context)) {
            SQLiteDatabase db = db_helper.openWriteLink();

            db.update(
                    FlowTables.TAG.toString(),
                    tag_values,
                    whereStr,
                    whereStrArgs
            );

            db.close();
            tip_str = "标签修改成功";
        } catch (SQLiteDatabaseLockedException e) {
            tip_str = "标签修改失败：数据库被其他进程占用";
            e.printStackTrace();
        } finally {
            Toast.makeText(context, tip_str, Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 删除标签
     *
     * @param tag_no  待删除标签的编号
     * @param context 打开数据库所需的上下文
     */
    public static void deleteTag(long tag_no, Context context) {
        ContentValues basic_values = new ContentValues();
        basic_values.put(FlowColumns.TAG_NO.toString(), 0);
        String whereStr = FlowColumns.TAG_NO + "=?";
        String[] whereStrArgs = {String.valueOf(tag_no)};

        String tip_str = "标签删除失败";
        try (FlowDatabaseHelper db_helper = new FlowDatabaseHelper(context)) {
            SQLiteDatabase db = db_helper.openWriteLink();

            //先将流水基本数据表的标签清除
            db.update(
                    FlowTables.BASIC.toString(),
                    basic_values,
                    whereStr,
                    whereStrArgs
            );

            //再删除对应标签
            db.delete(
                    FlowTables.TAG.toString(),
                    whereStr,
                    whereStrArgs
            );

            db.close();
            tip_str = "标签已成功删除";
        } catch (SQLiteDatabaseLockedException e) {
            tip_str = "标签删除失败：数据库被其他进程占用";
            e.printStackTrace();
        } finally {
            Toast.makeText(context, tip_str, Toast.LENGTH_SHORT).show();
        }
    }
}
