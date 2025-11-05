package com.project.manager.ui.setting.flow_data;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.widget.Toast;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.manager.database.FlowColumns;
import com.project.manager.database.FlowDatabaseHelper;
import com.project.manager.database.FlowTables;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FlowDataHelper {
    Context context;                //用于打开数据库的上下文
    FlowDatabaseHelper db_helper;   //流水数据库帮助器

    public FlowDataHelper(Context context) {
        this.context = context;
        this.db_helper = new FlowDatabaseHelper(context);
    }

    private List<FlowDataBase> getAllFlowData() {
        List<FlowDataBase> flowDataBaseList = new ArrayList<>();
        SQLiteDatabase db = db_helper.openReadLink();

        Cursor basic_cursor = db.query(
                FlowTables.BASIC.toString(),
                null,
                null,           //无WHERE子句
                null,
                null,
                null,
                FlowColumns.DATETIME + " DESC," + FlowColumns.FNO + " DESC"
        );

        //查询数据
        while (basic_cursor.moveToNext()) {
            //流水编号
            long fno = basic_cursor.getLong(basic_cursor.getColumnIndexOrThrow(FlowColumns.FNO.toString()));
            //金额
            double amount = basic_cursor.getDouble(basic_cursor.getColumnIndexOrThrow(FlowColumns.AMOUNT.toString()));
            //种类
            String type = basic_cursor.getString(basic_cursor.getColumnIndexOrThrow(FlowColumns.TYPE.toString()));
            //备注
            String remark = basic_cursor.getString(basic_cursor.getColumnIndexOrThrow(FlowColumns.REMARK.toString()));
            //日期和时间
            String datetime = basic_cursor.getString(basic_cursor.getColumnIndexOrThrow(FlowColumns.DATETIME.toString()));
            //标签编号
            long tag_no = basic_cursor.getLong(basic_cursor.getColumnIndexOrThrow(FlowColumns.TAG_NO.toString()));

            FlowDataBase flowData = null;
            switch (type) {
                case "EXPENSE":
                    flowData = new ExpenseFlowData(type, remark, datetime, tag_no, amount, fno);
                    break;
                case "INCOME":
                    flowData = new IncomeFlowData(type, remark, datetime, tag_no, amount, fno);
                    break;
                case "TRANSFER":
                    String[] columns = {FlowColumns.EXPORT.toString(), FlowColumns.IMPORT.toString()};
                    String selection = FlowColumns.FNO + "=?";
                    String[] selectionArgs = {String.valueOf(fno)};

                    Cursor transfer_cursor = db.query(
                            FlowTables.TRANSFER.toString(),
                            columns,
                            selection,
                            selectionArgs,
                            null,
                            null,
                            null
                    );

                    while (transfer_cursor.moveToNext()) {
                        String exportAccount = transfer_cursor.getString(transfer_cursor.getColumnIndexOrThrow(FlowColumns.EXPORT.toString()));
                        String importAccount = transfer_cursor.getString(transfer_cursor.getColumnIndexOrThrow(FlowColumns.IMPORT.toString()));
                        transfer_cursor.close();
                        flowData = new TransferFlowData(type, remark, datetime, tag_no, amount, fno, exportAccount, importAccount);
                    }

                    break;
                default:
                    throw new RuntimeException("无法获取正确的流水视图类型");
            }

            flowDataBaseList.add(flowData);
        }

        basic_cursor.close();
        db.close();
        return flowDataBaseList;
    }

    private List<TagData> getAllTagData() {
        List<TagData> tagDataList = new ArrayList<>();
        SQLiteDatabase db = db_helper.openReadLink();

        Cursor tag_cursor = db.query(
                FlowTables.TAG.toString(),
                null,
                null,
                null,
                null,
                null,
                null
        );

        while (tag_cursor.moveToNext()) {
            long tag_no = tag_cursor.getLong(tag_cursor.getColumnIndexOrThrow(FlowColumns.TAG_NO.toString()));
            long group_no = tag_cursor.getLong(tag_cursor.getColumnIndexOrThrow(FlowColumns.GROUP_NO.toString()));
            String tag_name = tag_cursor.getString(tag_cursor.getColumnIndexOrThrow(FlowColumns.TAG_NAME.toString()));

            TagData tagData = new TagData(tag_name, tag_no, group_no);
            tagDataList.add(tagData);
        }

        tag_cursor.close();
        db.close();
        return tagDataList;
    }

    private List<TagGroupData> getAllTagGroupData() {
        List<TagGroupData> tagGroupDataList = new ArrayList<>();
        SQLiteDatabase db = db_helper.openReadLink();

        Cursor tag_group_cursor = db.query(
                FlowTables.TAG_GROUP.toString(),
                null,
                null,
                null,
                null,
                null,
                null
        );

        while (tag_group_cursor.moveToNext()) {
            String group_name = tag_group_cursor.getString(tag_group_cursor.getColumnIndexOrThrow(FlowColumns.GROUP_NAME.toString()));
            long group_no = tag_group_cursor.getLong(tag_group_cursor.getColumnIndexOrThrow(FlowColumns.GROUP_NO.toString()));

            TagGroupData tagGroupData = new TagGroupData(group_name, group_no);
            tagGroupDataList.add(tagGroupData);
        }

        tag_group_cursor.close();
        db.close();
        return tagGroupDataList;
    }

    /**
     * 获取流水账数据库的所有数据
     *
     * @return 包含所有数据的Map字典，键值：对应表名
     */
    public Map<String, Object> getAllDataInMap() {
        //读取所有数据
        List<FlowDataBase> flowDataList = getAllFlowData();
        List<TagData> tagDataList = getAllTagData();
        List<TagGroupData> tagGroupDataList = getAllTagGroupData();

        //将所有数据合并至一个字典
        Map<String, Object> mergedMap = new HashMap<>();
        mergedMap.put(FlowTables.BASIC.toString(), flowDataList);
        mergedMap.put(FlowTables.TAG.toString(), tagDataList);
        mergedMap.put(FlowTables.TAG_GROUP.toString(), tagGroupDataList);

        return mergedMap;
    }

    /**
     * 将JSON字符串写入文件
     *
     * @param uri        待写入文件的uri
     * @param jsonString 需要写入的JSON字符串
     * @param context    活动上下文
     */
    public static void writeJsonToFile(Uri uri, String jsonString, Context context) {
        try (OutputStream outputStream = context.getContentResolver().openOutputStream(uri)) {
            if (outputStream != null) {
                outputStream.write(jsonString.getBytes());
                outputStream.flush();
                Toast.makeText(context, "文件保存成功", Toast.LENGTH_SHORT).show();
            }
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(context, "保存失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}
