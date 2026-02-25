package com.manager.assistant.data.io.data_helpers;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabaseLockedException;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.manager.assistant.isolated_enums.DirectoryPaths;
import com.manager.assistant.isolated_enums.LogTags;
import com.manager.assistant.data.data_save.database.BookkeepingColumns;
import com.manager.assistant.data.data_save.database.BookkeepingDbHelper;
import com.manager.assistant.data.data_save.database.BookkeepingTables;
import com.manager.assistant.data.data_save.preference.BookKeepingStartDatePreference;
import com.manager.assistant.data.io.pojo.PojoBasicRunningAccount;
import com.manager.assistant.data.io.pojo.PojoPicture;
import com.manager.assistant.data.io.pojo.PojoTag;
import com.manager.assistant.data.io.pojo.PojoTagGroup;
import com.manager.assistant.data.io.maps.TotalAccountDataMap;
import com.manager.assistant.data.io.pojo.PojoTransferRunningAccount;
import com.manager.assistant.ui.data_sync.tag_modify.TagRepository;
import com.manager.assistant.ui.data_sync.tag_modify.TagUpdateReason;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class RunningAccountDataHelper extends DataHelperBase<BookkeepingDbHelper, TotalAccountDataMap> {
    public RunningAccountDataHelper(Context context) {
        super(context);
    }

    @Override
    protected BookkeepingDbHelper createHelper() {
        return new BookkeepingDbHelper(context);
    }

    @Override
    protected Class<TotalAccountDataMap> getMapClass() {
        return TotalAccountDataMap.class;
    }

    @Override
    protected void saveDataInMapToDb(@NonNull TotalAccountDataMap map) {
        List<PojoTagGroup> pojoTagGroupList = map.getTag_group_data();
        List<PojoTag> pojoTagList = map.getTag_data();
        List<PojoBasicRunningAccount> pojoBasicRunningAccountList = map.getBasic_data();
        List<PojoTransferRunningAccount> pojoTransferRunningAccountList = map.getTransfer_data();
        List<PojoPicture> pojoPictureList = map.getPicture_data();

        //将对应的数据写入数据库
        setTagGroupData(pojoTagGroupList);
        setTagData(pojoTagList);
        setBasicData(pojoBasicRunningAccountList);
        setTransferData(pojoTransferRunningAccountList);
        setPictureData(pojoPictureList == null ? new ArrayList<>() : pojoPictureList);

        BookKeepingStartDatePreference.saveStartDate("", context);  //清空已保存的开始记账的日期
    }

    @Override
    public TotalAccountDataMap getAllDataInMap() {
        //读取所有数据
        List<PojoBasicRunningAccount> pojoBasicRunningAccountList = getBasicData();
        List<PojoTransferRunningAccount> pojoTransferRunningAccountList = getTransferData();
        List<PojoTag> pojoTagList = getTagData();
        List<PojoTagGroup> pojoTagGroupList = getTagGroupData();
        List<PojoPicture> pojoPictureList = getPictureData();

        //将所有数据合并至一个字典
        TotalAccountDataMap totalAccountDataMap = new TotalAccountDataMap();
        totalAccountDataMap.setBasic_data(pojoBasicRunningAccountList);
        totalAccountDataMap.setTransfer_data(pojoTransferRunningAccountList);
        totalAccountDataMap.setTag_data(pojoTagList);
        totalAccountDataMap.setTag_group_data(pojoTagGroupList);
        totalAccountDataMap.setPicture_data(pojoPictureList);

        return totalAccountDataMap;
    }

    //获取所有流水账基本数据（对应基本流水记录表）
    @NonNull
    private List<PojoBasicRunningAccount> getBasicData() {
        List<PojoBasicRunningAccount> pojoBasicRunningAccountList = new ArrayList<>();
        SQLiteDatabase db = dbHelper.openReadLink();

        Cursor basicCursor = db.query(
                BookkeepingTables.BASIC.toString(),
                null,
                null,           //无WHERE子句
                null,
                null,
                null,
                null
        );

        //查询数据
        while (basicCursor.moveToNext()) {
            //流水编号
            long rno = basicCursor.getLong(basicCursor.getColumnIndexOrThrow(BookkeepingColumns.RNO.toString()));
            //金额
            double amount = basicCursor.getDouble(basicCursor.getColumnIndexOrThrow(BookkeepingColumns.AMOUNT.toString()));
            //种类
            String type = basicCursor.getString(basicCursor.getColumnIndexOrThrow(BookkeepingColumns.TYPE.toString()));
            //备注
            String remark = basicCursor.getString(basicCursor.getColumnIndexOrThrow(BookkeepingColumns.REMARK.toString()));
            //日期和时间
            String datetime = basicCursor.getString(basicCursor.getColumnIndexOrThrow(BookkeepingColumns.DATETIME.toString()));
            //标签编号
            long tag_no = basicCursor.getLong(basicCursor.getColumnIndexOrThrow(BookkeepingColumns.TAG_NO.toString()));

            PojoBasicRunningAccount pojoBasicRunningAccount = new PojoBasicRunningAccount(type, remark, datetime, tag_no, amount, rno);
            pojoBasicRunningAccountList.add(pojoBasicRunningAccount);
        }

        basicCursor.close();
        db.close();
        return pojoBasicRunningAccountList;
    }

    //将流水账基本数据写入数据库
    private void setBasicData(List<PojoBasicRunningAccount> runningAccountDataList) {
        SQLiteDatabase db = dbHelper.openWriteLink();

        //删除之前表的内容
        db.delete(BookkeepingTables.BASIC.toString(), null, null);

        if (runningAccountDataList == null) {
            return;
        }

        for (PojoBasicRunningAccount basic_data : runningAccountDataList) {
            String type = basic_data.getType();              //种类
            String remark = basic_data.getRemark();          //备注
            String datetime = basic_data.getDate_time();     //日期和时间
            long tag_no = basic_data.getTag_no();            //标签编号
            double amount = basic_data.getAmount();          //金额
            long rno = basic_data.getRno();                  //流水编号

            //写入基本数据
            ContentValues basicValues = new ContentValues();
            basicValues.put(BookkeepingColumns.TYPE.toString(), type);
            basicValues.put(BookkeepingColumns.REMARK.toString(), remark);
            basicValues.put(BookkeepingColumns.DATETIME.toString(), datetime);
            basicValues.put(BookkeepingColumns.TAG_NO.toString(), tag_no);
            basicValues.put(BookkeepingColumns.AMOUNT.toString(), amount);
            basicValues.put(BookkeepingColumns.RNO.toString(), rno);
            db.insert(BookkeepingTables.BASIC.toString(), null, basicValues);
        }

        db.close();
    }

    //获取转账流水账记录（对应转账流水表）
    @NonNull
    private List<PojoTransferRunningAccount> getTransferData() {
        List<PojoTransferRunningAccount> pojoTransferRunningAccountList = new ArrayList<>();
        SQLiteDatabase db = dbHelper.openReadLink();

        Cursor transferCursor = db.query(
                BookkeepingTables.TRANSFER.toString(),
                null,
                null,
                null,
                null,
                null,
                null
        );

        while (transferCursor.moveToNext()) {
            String exportAccount = transferCursor.getString(transferCursor.getColumnIndexOrThrow(BookkeepingColumns.EXPORT.toString()));
            String importAccount = transferCursor.getString(transferCursor.getColumnIndexOrThrow(BookkeepingColumns.IMPORT.toString()));
            long rno = transferCursor.getLong(transferCursor.getColumnIndexOrThrow(BookkeepingColumns.RNO.toString()));

            PojoTransferRunningAccount pojoTransferRunningAccount = new PojoTransferRunningAccount(rno, exportAccount, importAccount);
            pojoTransferRunningAccountList.add(pojoTransferRunningAccount);
        }

        transferCursor.close();
        db.close();
        return pojoTransferRunningAccountList;
    }

    private void setTransferData(List<PojoTransferRunningAccount> pojoTransferRunningAccountList) {
        SQLiteDatabase db = dbHelper.openWriteLink();

        //删除之前表的内容
        db.delete(BookkeepingTables.TRANSFER.toString(), null, null);

        if (pojoTransferRunningAccountList == null) {
            return;
        }

        for (PojoTransferRunningAccount transfer_data : pojoTransferRunningAccountList) {
            String exportAccount, importAccount;
            exportAccount = transfer_data.getExport_account();
            importAccount = transfer_data.getImport_account();
            long rno = transfer_data.getRno();

            //将数据写入数据库
            ContentValues transferValues = new ContentValues();
            transferValues.put(BookkeepingColumns.EXPORT.toString(), exportAccount);
            transferValues.put(BookkeepingColumns.IMPORT.toString(), importAccount);
            transferValues.put(BookkeepingColumns.RNO.toString(), rno);
            db.insert(BookkeepingTables.TRANSFER.toString(), null, transferValues);
        }

        db.close();
    }

    //获取所有标签数据（对应标签表）
    @NonNull
    private List<PojoTag> getTagData() {
        List<PojoTag> pojoTagList = new ArrayList<>();
        SQLiteDatabase db = dbHelper.openReadLink();

        Cursor tagCursor = db.query(
                BookkeepingTables.TAG.toString(),
                null,
                null,
                null,
                null,
                null,
                null
        );

        while (tagCursor.moveToNext()) {
            long tag_no = tagCursor.getLong(tagCursor.getColumnIndexOrThrow(BookkeepingColumns.TAG_NO.toString()));
            long group_no = tagCursor.getLong(tagCursor.getColumnIndexOrThrow(BookkeepingColumns.GROUP_NO.toString()));
            String tag_name = tagCursor.getString(tagCursor.getColumnIndexOrThrow(BookkeepingColumns.TAG_NAME.toString()));
            int scope = tagCursor.getInt(tagCursor.getColumnIndexOrThrow(BookkeepingColumns.TAG_SCOPE.toString()));

            PojoTag pojoTag = new PojoTag(tag_name, tag_no, group_no, scope);
            pojoTagList.add(pojoTag);
        }

        tagCursor.close();
        db.close();
        return pojoTagList;
    }

    //将标签数据写入数据库
    private void setTagData(List<PojoTag> pojoTagList) {
        SQLiteDatabase db = dbHelper.openWriteLink();

        //删除之前表的内容
        db.delete(BookkeepingTables.TAG.toString(), null, null);

        if (pojoTagList == null) {
            return;
        }

        for (PojoTag tagData : pojoTagList) {
            String tag_name = tagData.getName();
            long tag_no = tagData.getTno();
            long group_no = tagData.getGroup_no();
            int scope = tagData.getScope();

            //将数据写入数据库
            ContentValues tagValues = new ContentValues();
            tagValues.put(BookkeepingColumns.TAG_SCOPE.toString(), scope);
            tagValues.put(BookkeepingColumns.TAG_NAME.toString(), tag_name);
            tagValues.put(BookkeepingColumns.TAG_NO.toString(), tag_no);
            tagValues.put(BookkeepingColumns.GROUP_NO.toString(), group_no);
            db.insert(BookkeepingTables.TAG.toString(), null, tagValues);
        }

        db.close();
    }

    //获取所有标签分组数据（对应标签分组表）
    @NonNull
    private List<PojoTagGroup> getTagGroupData() {
        List<PojoTagGroup> pojoTagGroupList = new ArrayList<>();
        SQLiteDatabase db = dbHelper.openReadLink();

        Cursor tag_group_cursor = db.query(
                BookkeepingTables.TAG_GROUP.toString(),
                null,
                null,
                null,
                null,
                null,
                null
        );

        while (tag_group_cursor.moveToNext()) {
            String group_name = tag_group_cursor.getString(tag_group_cursor.getColumnIndexOrThrow(BookkeepingColumns.GROUP_NAME.toString()));
            long group_no = tag_group_cursor.getLong(tag_group_cursor.getColumnIndexOrThrow(BookkeepingColumns.GROUP_NO.toString()));

            PojoTagGroup pojoTagGroup = new PojoTagGroup(group_name, group_no);
            pojoTagGroupList.add(pojoTagGroup);
        }

        tag_group_cursor.close();
        db.close();
        return pojoTagGroupList;
    }

    //将标签分组数据写入数据库
    private void setTagGroupData(List<PojoTagGroup> pojoTagGroupList) {
        SQLiteDatabase db = dbHelper.openWriteLink();

        //删除之前表的内容
        db.delete(BookkeepingTables.TAG_GROUP.toString(), null, null);

        if (pojoTagGroupList == null) {
            return;
        }

        boolean isDefaultGroupInImportedData = false;
        for (PojoTagGroup tagGroupData : pojoTagGroupList) {
            String groupName = tagGroupData.getGroup_name();
            long group_no = tagGroupData.getGroup_no();

            if (group_no == 0) isDefaultGroupInImportedData = true; //判断导入的数据是否含有编号为0的默认分组

            //将数据写入数据库
            ContentValues groupValues = new ContentValues();
            groupValues.put(BookkeepingColumns.GROUP_NAME.toString(), groupName);
            groupValues.put(BookkeepingColumns.GROUP_NO.toString(), group_no);
            db.insert(BookkeepingTables.TAG_GROUP.toString(), null, groupValues);
        }

        //如果没有默认分组，则添加一个默认分组记录
        if (!isDefaultGroupInImportedData) {
            ContentValues defaultGroupValues = new ContentValues();
            defaultGroupValues.put(BookkeepingColumns.GROUP_NAME.toString(), BookkeepingDbHelper.defaultGroupName);
            defaultGroupValues.put(BookkeepingColumns.GROUP_NO.toString(), 0);
            db.insert(BookkeepingTables.TAG_GROUP.toString(), null, defaultGroupValues);
        }

        db.close();
    }

    /**
     * 从数据库读取图片数据
     *
     * @return 包含图片Pojo数据的列表
     */
    @NonNull
    private List<PojoPicture> getPictureData() {
        SQLiteDatabase db = dbHelper.openReadLink();
        List<PojoPicture> pictureList = new ArrayList<>();

        Cursor pictureCursor = db.query(
                BookkeepingTables.PICTURE.toString(),
                null,
                null,
                null,
                null,
                null,
                null
        );

        while (pictureCursor.moveToNext()) {
            long pno = pictureCursor.getLong(pictureCursor.getColumnIndexOrThrow(BookkeepingColumns.PNO.toString()));
            long rno = pictureCursor.getLong(pictureCursor.getColumnIndexOrThrow(BookkeepingColumns.RNO.toString()));
            String uri = pictureCursor.getString(pictureCursor.getColumnIndexOrThrow(BookkeepingColumns.PICTURE_URI.toString()));

            PojoPicture picture = new PojoPicture(pno, rno, uri);
            pictureList.add(picture);
        }

        pictureCursor.close();
        db.close();
        return pictureList;
    }

    /**
     * 将图片数据写入数据库
     *
     * @param pictureList 图片数据列表
     */
    private void setPictureData(List<PojoPicture> pictureList) {
        SQLiteDatabase db = dbHelper.openWriteLink();

        db.delete(BookkeepingTables.PICTURE.toString(), null, null);

        if (pictureList == null) {
            return;
        }

        for (PojoPicture picture : pictureList) {
            long pno = picture.getPno();
            long rno = picture.getRno();
            String uri = picture.getUri();

            ContentValues pictureValues = new ContentValues();
            pictureValues.put(BookkeepingColumns.PNO.toString(), pno);
            pictureValues.put(BookkeepingColumns.RNO.toString(), rno);
            pictureValues.put(BookkeepingColumns.PICTURE_URI.toString(), uri);
            db.insert(BookkeepingTables.PICTURE.toString(), null, pictureValues);
        }

        db.close();
    }

    /**
     * 删除所有流水账数据
     *
     * @param context 用于打开数据库的上下文
     */
    public static void deleteAllData(@NonNull Context context) {
        String tipStr = "数据清除失败，原因未知";
        try (BookkeepingDbHelper db_helper = new BookkeepingDbHelper(context)) {
            SQLiteDatabase db = db_helper.openWriteLink();

            db.delete(BookkeepingTables.TRANSFER.toString(), null, null);
            db.delete(BookkeepingTables.BASIC.toString(), null, null);
            db.delete(BookkeepingTables.TAG.toString(), null, null);
            db.delete(BookkeepingTables.TAG_GROUP.toString(), BookkeepingColumns.GROUP_NO + "!=0", null);
            db.delete(BookkeepingTables.PICTURE.toString(), null, null);

            //删除旧图片
            File pictureDir = DirectoryPaths.PICTURE.getDir(context);
            if (pictureDir != null) {
                File[] oldPictureFiles = pictureDir.listFiles();
                if (oldPictureFiles != null) {
                    for (File oldPicture : oldPictureFiles) {
                        if (!oldPicture.delete()) {
                            Log.w(LogTags.ACCOUNT_DATA_HELPER.getV(), String.format(Locale.getDefault(), "“%s”删除失败", oldPicture.getName()));
                        }
                    }
                }
            }

            //删除通知解析规则的标签数据
            ContentValues rule_tag_value = new ContentValues();
            rule_tag_value.put(BookkeepingColumns.TAG_NO.toString(), 0);
            db.update(BookkeepingTables.ANALYSIS_RULE.toString(), rule_tag_value, null, null);

            tipStr = "数据清除成功";
        } catch (SQLiteDatabaseLockedException e) {
            tipStr = "数据清除失败：数据库异常";
        } finally {
            Toast.makeText(context, tipStr, Toast.LENGTH_SHORT).show();

            //更新主页标签数量
            TagRepository tagRepository = TagRepository.getInstance();
            tagRepository.updateTag(TagUpdateReason.CLEAR);
        }
    }
}
