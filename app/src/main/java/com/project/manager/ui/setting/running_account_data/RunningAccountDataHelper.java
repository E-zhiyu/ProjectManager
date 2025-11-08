package com.project.manager.ui.setting.running_account_data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabaseLockedException;
import android.net.Uri;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.manager.database.RunningAccountColumns;
import com.project.manager.database.RunningAccountDatabaseHelper;
import com.project.manager.database.RunningAccountTables;
import com.project.manager.ui.setting.running_account_data.pojo.PojoBasicRunningAccount;
import com.project.manager.ui.setting.running_account_data.pojo.PojoTag;
import com.project.manager.ui.setting.running_account_data.pojo.PojoTagGroup;
import com.project.manager.ui.setting.running_account_data.pojo.TotalDataMap;
import com.project.manager.ui.setting.running_account_data.pojo.PojoTransferRunningAccount;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class RunningAccountDataHelper {
    Context context;                //用于打开数据库的上下文
    RunningAccountDatabaseHelper db_helper;   //流水数据库帮助器

    public RunningAccountDataHelper(Context context) {
        this.context = context;
        this.db_helper = new RunningAccountDatabaseHelper(context);
    }

    //获取所有流水账基本数据（对应基本流水记录表）
    @NonNull
    private List<PojoBasicRunningAccount> getBasicData() {
        List<PojoBasicRunningAccount> pojoBasicRunningAccountList = new ArrayList<>();
        SQLiteDatabase db = db_helper.openReadLink();

        Cursor basic_cursor = db.query(
                RunningAccountTables.BASIC.toString(),
                null,
                null,           //无WHERE子句
                null,
                null,
                null,
                null
        );

        //查询数据
        while (basic_cursor.moveToNext()) {
            //流水编号
            long rno = basic_cursor.getLong(basic_cursor.getColumnIndexOrThrow(RunningAccountColumns.RNO.toString()));
            //金额
            double amount = basic_cursor.getDouble(basic_cursor.getColumnIndexOrThrow(RunningAccountColumns.AMOUNT.toString()));
            //种类
            String type = basic_cursor.getString(basic_cursor.getColumnIndexOrThrow(RunningAccountColumns.TYPE.toString()));
            //备注
            String remark = basic_cursor.getString(basic_cursor.getColumnIndexOrThrow(RunningAccountColumns.REMARK.toString()));
            //日期和时间
            String datetime = basic_cursor.getString(basic_cursor.getColumnIndexOrThrow(RunningAccountColumns.DATETIME.toString()));
            //标签编号
            long tag_no = basic_cursor.getLong(basic_cursor.getColumnIndexOrThrow(RunningAccountColumns.TAG_NO.toString()));

            PojoBasicRunningAccount pojoBasicRunningAccount = new PojoBasicRunningAccount(type, remark, datetime, tag_no, amount, rno);
            pojoBasicRunningAccountList.add(pojoBasicRunningAccount);
        }

        basic_cursor.close();
        db.close();
        return pojoBasicRunningAccountList;
    }

    //将流水账基本数据写入数据库
    private void setBasicData(@NonNull List<PojoBasicRunningAccount> runningAccountDataList) {
        SQLiteDatabase db = db_helper.openWriteLink();

        //删除之前表的内容
        db.delete(RunningAccountTables.BASIC.toString(), null, null);

        for (PojoBasicRunningAccount basic_data : runningAccountDataList) {
            String type = basic_data.getType();              //种类
            String remark = basic_data.getRemark();          //备注
            String date_time = basic_data.getDate_time();    //日期和时间
            long tag_no = basic_data.getTag_no();            //标签编号
            double amount = basic_data.getAmount();          //金额
            long rno = basic_data.getRno();                  //流水编号

            //写入基本数据
            ContentValues basic_values = new ContentValues();
            basic_values.put(RunningAccountColumns.TYPE.toString(), type);
            basic_values.put(RunningAccountColumns.REMARK.toString(), remark);
            basic_values.put(RunningAccountColumns.DATETIME.toString(), date_time);
            basic_values.put(RunningAccountColumns.TAG_NO.toString(), tag_no);
            basic_values.put(RunningAccountColumns.AMOUNT.toString(), amount);
            basic_values.put(RunningAccountColumns.RNO.toString(), rno);
            db.insert(RunningAccountTables.BASIC.toString(), null, basic_values);
        }

        db.close();
    }

    //获取转账流水账记录（对应转账流水表）
    @NonNull
    private List<PojoTransferRunningAccount> getTransferData() {
        List<PojoTransferRunningAccount> pojoTransferRunningAccountList = new ArrayList<>();
        SQLiteDatabase db = db_helper.openReadLink();

        Cursor transfer_cursor = db.query(
                RunningAccountTables.TRANSFER.toString(),
                null,
                null,
                null,
                null,
                null,
                null
        );

        while (transfer_cursor.moveToNext()) {
            String export_account = transfer_cursor.getString(transfer_cursor.getColumnIndexOrThrow(RunningAccountColumns.EXPORT.toString()));
            String import_account = transfer_cursor.getString(transfer_cursor.getColumnIndexOrThrow(RunningAccountColumns.IMPORT.toString()));
            long rno = transfer_cursor.getLong(transfer_cursor.getColumnIndexOrThrow(RunningAccountColumns.RNO.toString()));

            PojoTransferRunningAccount pojoTransferRunningAccount = new PojoTransferRunningAccount(rno, export_account, import_account);
            pojoTransferRunningAccountList.add(pojoTransferRunningAccount);
        }

        transfer_cursor.close();
        db.close();
        return pojoTransferRunningAccountList;
    }

    private void setTransferData(@NonNull List<PojoTransferRunningAccount> pojoTransferRunningAccountList) {
        SQLiteDatabase db = db_helper.openWriteLink();

        //删除之前表的内容
        db.delete(RunningAccountTables.TRANSFER.toString(), null, null);

        for (PojoTransferRunningAccount transfer_data : pojoTransferRunningAccountList) {
            String export_account = transfer_data.getExport_account();
            String import_account = transfer_data.getImport_account();
            long rno = transfer_data.getRno();

            //将数据写入数据库
            ContentValues transfer_values = new ContentValues();
            transfer_values.put(RunningAccountColumns.EXPORT.toString(), export_account);
            transfer_values.put(RunningAccountColumns.IMPORT.toString(), import_account);
            transfer_values.put(RunningAccountColumns.RNO.toString(), rno);
            db.insert(RunningAccountTables.TRANSFER.toString(), null, transfer_values);
        }

        db.close();
    }

    //获取所有标签数据（对应标签表）
    @NonNull
    private List<PojoTag> getTagData() {
        List<PojoTag> pojoTagList = new ArrayList<>();
        SQLiteDatabase db = db_helper.openReadLink();

        Cursor tag_cursor = db.query(
                RunningAccountTables.TAG.toString(),
                null,
                null,
                null,
                null,
                null,
                null
        );

        while (tag_cursor.moveToNext()) {
            long tag_no = tag_cursor.getLong(tag_cursor.getColumnIndexOrThrow(RunningAccountColumns.TAG_NO.toString()));
            long group_no = tag_cursor.getLong(tag_cursor.getColumnIndexOrThrow(RunningAccountColumns.GROUP_NO.toString()));
            String tag_name = tag_cursor.getString(tag_cursor.getColumnIndexOrThrow(RunningAccountColumns.TAG_NAME.toString()));

            PojoTag pojoTag = new PojoTag(tag_name, tag_no, group_no);
            pojoTagList.add(pojoTag);
        }

        tag_cursor.close();
        db.close();
        return pojoTagList;
    }

    //将标签数据写入数据库
    private void setTagData(@NonNull List<PojoTag> pojoTagList) {
        SQLiteDatabase db = db_helper.openWriteLink();

        //删除之前表的内容
        db.delete(RunningAccountTables.TAG.toString(), null, null);

        for (PojoTag tag_data : pojoTagList) {
            String tag_name = tag_data.getName();
            long tag_no = tag_data.getTno();
            long group_no = tag_data.getGroup_no();

            //将数据写入数据库
            ContentValues tag_values = new ContentValues();
            tag_values.put(RunningAccountColumns.TAG_NAME.toString(), tag_name);
            tag_values.put(RunningAccountColumns.TAG_NO.toString(), tag_no);
            tag_values.put(RunningAccountColumns.GROUP_NO.toString(), group_no);
            db.insert(RunningAccountTables.TAG.toString(), null, tag_values);
        }

        db.close();
    }

    //获取所有标签分组数据（对应标签分组表）
    @NonNull
    private List<PojoTagGroup> getTagGroupData() {
        List<PojoTagGroup> pojoTagGroupList = new ArrayList<>();
        SQLiteDatabase db = db_helper.openReadLink();

        Cursor tag_group_cursor = db.query(
                RunningAccountTables.TAG_GROUP.toString(),
                null,
                null,
                null,
                null,
                null,
                null
        );

        while (tag_group_cursor.moveToNext()) {
            String group_name = tag_group_cursor.getString(tag_group_cursor.getColumnIndexOrThrow(RunningAccountColumns.GROUP_NAME.toString()));
            long group_no = tag_group_cursor.getLong(tag_group_cursor.getColumnIndexOrThrow(RunningAccountColumns.GROUP_NO.toString()));

            PojoTagGroup pojoTagGroup = new PojoTagGroup(group_name, group_no);
            pojoTagGroupList.add(pojoTagGroup);
        }

        tag_group_cursor.close();
        db.close();
        return pojoTagGroupList;
    }

    //将标签分组数据写入数据库
    private void setTagGroupData(@NonNull List<PojoTagGroup> pojoTagGroupList) {
        SQLiteDatabase db = db_helper.openWriteLink();

        //删除之前表的内容
        db.delete(RunningAccountTables.TAG_GROUP.toString(), null, null);

        for (PojoTagGroup tag_group_data : pojoTagGroupList) {
            String group_name = tag_group_data.getGroup_name();
            long group_no = tag_group_data.getGroup_no();

            //将数据写入数据库
            ContentValues group_values = new ContentValues();
            group_values.put(RunningAccountColumns.GROUP_NAME.toString(), group_name);
            group_values.put(RunningAccountColumns.GROUP_NO.toString(), group_no);
            db.insert(RunningAccountTables.TAG_GROUP.toString(), null, group_values);
        }

        db.close();
    }

    /**
     * 获取流水账数据库的所有数据
     *
     * @return 包含所有数据的Map字典，键值：对应表名
     */
    public TotalDataMap getAllDataInMap() {
        //读取所有数据
        List<PojoBasicRunningAccount> pojoBasicRunningAccountList = getBasicData();
        List<PojoTransferRunningAccount> pojoTransferRunningAccountList = getTransferData();
        List<PojoTag> pojoTagList = getTagData();
        List<PojoTagGroup> pojoTagGroupList = getTagGroupData();

        //将所有数据合并至一个字典
        TotalDataMap totalDataMap = new TotalDataMap();
        totalDataMap.setBasic_data(pojoBasicRunningAccountList);
        totalDataMap.setTransfer_data(pojoTransferRunningAccountList);
        totalDataMap.setTag_data(pojoTagList);
        totalDataMap.setTag_group_data(pojoTagGroupList);

        return totalDataMap;
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

    /**
     * 读取文件中的JSON字符串并保存至数据库
     *
     * @param uri     待读取文件的uri
     * @param context 活动上下文
     */
    public static void readFileAndSave(Uri uri, Context context) {
        String tip_str = "数据导入失败";
        try (InputStream inputStream = context.getContentResolver().openInputStream(uri)) {
            if (inputStream != null) {
                // 将 InputStream 转换为字符串
                String jsonString = convertStreamToString(inputStream);

                ObjectMapper mapper = new ObjectMapper();
                try {
                    TotalDataMap dataMap = mapper.readValue(jsonString, TotalDataMap.class);
                    List<PojoTagGroup> pojoTagGroupList = dataMap.getTag_group_data();
                    List<PojoTag> pojoTagList = dataMap.getTag_data();
                    List<PojoBasicRunningAccount> pojoBasicRunningAccountList = dataMap.getBasic_data();
                    List<PojoTransferRunningAccount> pojoTransferRunningAccountList = dataMap.getTransfer_data();

                    //将对应的数据写入数据库
                    RunningAccountDataHelper helper = new RunningAccountDataHelper(context);
                    helper.setTagGroupData(pojoTagGroupList);
                    helper.setTagData(pojoTagList);
                    helper.setBasicData(pojoBasicRunningAccountList);
                    helper.setTransferData(pojoTransferRunningAccountList);

                    tip_str = "数据已成功导入";
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                    tip_str = "无法解析备份文件的内容";
                }
            } else {
                tip_str = "无法读取备份文件";
            }
        } catch (IOException e) {
            tip_str = "无法打开备份文件";
            e.printStackTrace();
        } finally {
            Toast.makeText(context, tip_str, Toast.LENGTH_SHORT).show();
        }
    }

    //将 InputStream 转为 String
    @NonNull
    private static String convertStreamToString(InputStream is) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line).append("\n");
        }
        return sb.toString();
    }

    /**
     * 删除所有流水账数据
     *
     * @param context 用于打开数据库的上下文
     */
    public static void deleteAllData(Context context) {
        String tip_str = "数据清除失败，原因未知";
        try (RunningAccountDatabaseHelper db_helper = new RunningAccountDatabaseHelper(context)) {
            SQLiteDatabase db = db_helper.openWriteLink();

            db.delete(RunningAccountTables.TRANSFER.toString(), null, null);
            db.delete(RunningAccountTables.BASIC.toString(), null, null);
            db.delete(RunningAccountTables.TAG.toString(), null, null);
            db.delete(RunningAccountTables.TAG_GROUP.toString(), null, null);

            db.close();
            tip_str = "数据清除成功";
        } catch (SQLiteDatabaseLockedException e) {
            tip_str = "数据清除失败，无法打开数据库";
        } finally {
            Toast.makeText(context, tip_str, Toast.LENGTH_SHORT).show();
        }
    }
}
