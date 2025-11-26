package com.project.manager.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.project.manager.exception.ExceptionHelper;

public class BookKeepingDatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "running_account.db";   //数据库名称
    private static final int DATABASE_VERSION = 4;                      //数据库版本
    private final Context context;                                      //上下文
    public static final String defaultGroupName = "默认分组";           //默认分组名称

    public BookKeepingDatabaseHelper(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    /**
     * 获取读连接
     *
     * @return 只读数据库实例
     */
    public SQLiteDatabase openReadLink() {
        return this.getReadableDatabase();
    }

    /**
     * 获取写连接
     *
     * @return 写入数据库实例
     */
    public SQLiteDatabase openWriteLink() {
        return this.getWritableDatabase();
    }

    @Override
    public void onCreate(@NonNull SQLiteDatabase db) {
        String create, err = "";

        try {
            //创建标签分组表
            err = "标签分组数据库异常";
            create = "CREATE TABLE IF NOT EXISTS " + BookKeepingTables.TAG_GROUP + "(" +
                    BookKeepingColumns.GROUP_NO + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL," +
                    BookKeepingColumns.GROUP_NAME + " VARCHAR(20) NOT NULL UNIQUE" +
                    ")";
            db.execSQL(create);

            ContentValues default_group_values = new ContentValues();
            default_group_values.put(BookKeepingColumns.GROUP_NO.toString(), 0);
            default_group_values.put(BookKeepingColumns.GROUP_NAME.toString(), defaultGroupName);
            db.insert(BookKeepingTables.TAG_GROUP.toString(), null, default_group_values);

            //创建标签表
            err = "标签数据库创建异常";
            create = "CREATE TABLE IF NOT EXISTS " + BookKeepingTables.TAG + "(" +
                    BookKeepingColumns.TAG_NO + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL," +
                    BookKeepingColumns.TAG_NAME + " VARCHAR(20) NOT NULL UNIQUE," +
                    BookKeepingColumns.GROUP_NO + " INTEGER NOT NULL," +

                    //分组编号外键约束
                    "CONSTRAINT " + BookKeepingConstraints.FK_GROUP_NO +
                    " FOREIGN KEY (" + BookKeepingColumns.GROUP_NO + ")" +
                    " REFERENCES " + BookKeepingTables.TAG_GROUP + "(" + BookKeepingColumns.GROUP_NO + ")" +
                    " ON DELETE CASCADE" +
                    ")";
            db.execSQL(create);

            //创建流水基本数据表
            err = "流水账基本数据库创建异常";
            create = "CREATE TABLE IF NOT EXISTS " + BookKeepingTables.BASIC + "(" +
                    BookKeepingColumns.RNO + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL," +
                    BookKeepingColumns.AMOUNT + " DECIMAL(20,2) NOT NULL," +
                    BookKeepingColumns.TYPE + " VARCHAR(15) NOT NULL," +
                    BookKeepingColumns.REMARK + " VARCHAR(20)," +
                    BookKeepingColumns.DATETIME + " DATETIME NOT NULL," +
                    BookKeepingColumns.TAG_NO + " INTEGER DEFAULT 0," +

                    //标签编号外键约束
                    "CONSTRAINT " + BookKeepingConstraints.FK_TAG_NO +
                    " FOREIGN KEY (" + BookKeepingColumns.TAG_NO + ")" +
                    " REFERENCES " + BookKeepingTables.TAG + "(" + BookKeepingColumns.TAG_NO + ")" +
                    " ON DELETE SET DEFAULT" +
                    ")";
            db.execSQL(create);

            //创建转账独占数据表
            err = "转账流水账数据库创建异常";
            create = "CREATE TABLE IF NOT EXISTS " + BookKeepingTables.TRANSFER + "(" +
                    BookKeepingColumns.RNO + " INTEGER PRIMARY KEY NOT NULL," +
                    BookKeepingColumns.EXPORT + " VARCHAR(20) NOT NULL," +
                    BookKeepingColumns.IMPORT + " VARCHAR(20) NOT NULL," +

                    //流水编号外键约束
                    "CONSTRAINT " + BookKeepingConstraints.FK_RNO +
                    " FOREIGN KEY (" + BookKeepingColumns.RNO + ")" +
                    " REFERENCES " + BookKeepingTables.BASIC + "(" + BookKeepingColumns.RNO + ")" +
                    " ON DELETE CASCADE" +
                    ")";
            db.execSQL(create);

            //创建通知解析规则表
            err = "通知解析规则表创建错误";
            create = "CREATE TABLE IF NOT EXISTS " + BookKeepingTables.ANALYSIS_RULE + "(" +
                    BookKeepingColumns.RULE_NAME + " VARCHAR(20) NOT NULL," +
                    BookKeepingColumns.RULE_NO + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL," +
                    BookKeepingColumns.TYPE + " VARCHAR(20) NOT NULL," +
                    BookKeepingColumns.TAG_NO + " INTEGER DEFAULT 0," +
                    BookKeepingColumns.PACKAGE_NAME + " VARCHAR(50) NOT NULL," +
                    BookKeepingColumns.NOTIFICATION_TITLE + " VARCHAR(20) NOT NULL," +
                    BookKeepingColumns.NOTIFICATION_CONTENT + " VARCHAR(50) NOT NULL," +

                    "CONSTRAINT " + BookKeepingConstraints.FK_TAG_NO +
                    " FOREIGN KEY (" + BookKeepingColumns.TAG_NO + ")" +
                    " REFERENCES " + BookKeepingTables.TAG + "(" + BookKeepingColumns.TAG_NO + ")" +
                    " ON DELETE SET DEFAULT" +
                    ")";
            db.execSQL(create);
        } catch (SQLiteException e) {
            ExceptionHelper.showExceptionDialog(context, e);
            Toast.makeText(context, err, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        while (oldVersion < newVersion) {
            if (oldVersion == 1) up1To2(db);
            else if (oldVersion == 2) up2To3(db);
            else if (oldVersion == 3) up3To4(db);

            oldVersion++;
        }
    }

    //数据库版本升级(1->2)，变化如下：
    //basic表Tag_no列添加默认值0
    //basic表Tag_no外键约束删除动作改为SET DEFAULT
    private void up1To2(@NonNull SQLiteDatabase db) {
        //创建新的临时表
        String sql = "CREATE TABLE IF NOT EXISTS new_basic_data(" +
                "Rno INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL," +
                "Amount DECIMAL(20,2) NOT NULL," +
                "Type VARCHAR(15) NOT NULL," +
                "Remark VARCHAR(20)," +
                "DateTime DATETIME NOT NULL," +
                "TagNo INTEGER DEFAULT 0," +

                "CONSTRAINT fk_tag_no " +
                "FOREIGN KEY (TagNo) " +
                "REFERENCES tag_data(TagNo) " +
                "ON DELETE SET DEFAULT" +
                ")";
        db.execSQL(sql);

        //迁移旧表数据到新表
        sql = "INSERT INTO new_basic_data SELECT * FROM basic_data";
        db.execSQL(sql);

        //删除旧表
        sql = "DROP TABLE basic_data";
        db.execSQL(sql);

        //新表重命名为旧表名称
        sql = "ALTER TABLE new_basic_data RENAME TO basic_data";
        db.execSQL(sql);
    }

    //数据库版本：2->3
    //tag_group表添加一条group_no=0,group_name=defaultGroupName的记录
    private void up2To3(@NonNull SQLiteDatabase db) {
        ContentValues default_group_values = new ContentValues();
        default_group_values.put("GroupNO", 0);
        default_group_values.put("GroupName", defaultGroupName);
        db.insert("tag_group_data", null, default_group_values);
    }

    //数据库版本：3->4
    //添加通知解析规则表
    private void up3To4(@NonNull SQLiteDatabase db) {
        String err = "通知解析规则表创建错误";
        try {
            String create = "CREATE TABLE IF NOT EXISTS analysis_rule_data(" +
                    "Rule_name VARCHAR(20) NOT NULL," +
                    "Rule_no INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL," +
                    "Type VARCHAR(20) NOT NULL," +
                    "TagNo INTEGER DEFAULT 0," +
                    "Package_name VARCHAR(50) NOT NULL," +
                    "Notification_title VARCHAR(20) NOT NULL," +
                    "Notification_content VARCHAR(50) NOT NULL," +

                    "CONSTRAINT fk_tag_no" +
                    " FOREIGN KEY (TagNo)" +
                    " REFERENCES tag_data(TagNo)" +
                    " ON DELETE SET DEFAULT" +
                    ")";
            db.execSQL(create);
        } catch (SQLException e) {
            ExceptionHelper.showExceptionDialog(context, e);
            Toast.makeText(context, err, Toast.LENGTH_SHORT).show();
        }
    }
}
