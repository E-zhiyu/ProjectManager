package com.project.manager.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.project.manager.exception.ExceptionHelper;

public class RunningAccountDatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "running_account.db";   //数据库名称
    private static final int DATABASE_VERSION = 3;                      //数据库版本
    private final Context context;                                      //上下文
    public static final String defaultGroupName = "默认分组";           //默认分组名称

    public RunningAccountDatabaseHelper(@Nullable Context context) {
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
        String create;

        try {
            //创建标签分组表
            create = "CREATE TABLE IF NOT EXISTS " + RunningAccountTables.TAG_GROUP + "(" +
                    RunningAccountColumns.GROUP_NO + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL," +
                    RunningAccountColumns.GROUP_NAME + " VARCHAR(20) NOT NULL UNIQUE" +
                    ")";
            db.execSQL(create);

            ContentValues default_group_values = new ContentValues();
            default_group_values.put(RunningAccountColumns.GROUP_NO.toString(), 0);
            default_group_values.put(RunningAccountColumns.GROUP_NAME.toString(), defaultGroupName);
            db.insert(RunningAccountTables.TAG_GROUP.toString(), null, default_group_values);
        } catch (SQLException e) {
            ExceptionHelper.showExceptionDialog(context, e);
            Toast.makeText(context, "标签分组数据库异常", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            //创建标签表
            create = "CREATE TABLE IF NOT EXISTS " + RunningAccountTables.TAG + "(" +
                    RunningAccountColumns.TAG_NO + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL," +
                    RunningAccountColumns.TAG_NAME + " VARCHAR(20) NOT NULL UNIQUE," +
                    RunningAccountColumns.GROUP_NO + " INTEGER NOT NULL," +

                    //分组编号外键约束
                    "CONSTRAINT " + RunningAccountConstraints.FK_GROUP_NO +
                    " FOREIGN KEY (" + RunningAccountColumns.GROUP_NO + ")" +
                    " REFERENCES " + RunningAccountTables.TAG_GROUP + "(" + RunningAccountColumns.GROUP_NO + ")" +
                    " ON DELETE CASCADE" +
                    ")";
            db.execSQL(create);
        } catch (SQLException e) {
            ExceptionHelper.showExceptionDialog(context, e);
            Toast.makeText(context, "标签数据库创建异常", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            //创建流水基本数据表
            create = "CREATE TABLE IF NOT EXISTS " + RunningAccountTables.BASIC + "(" +
                    RunningAccountColumns.RNO + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL," +
                    RunningAccountColumns.AMOUNT + " DECIMAL(20,2) NOT NULL," +
                    RunningAccountColumns.TYPE + " VARCHAR(15) NOT NULL," +
                    RunningAccountColumns.REMARK + " VARCHAR(20)," +
                    RunningAccountColumns.DATETIME + " DATETIME NOT NULL," +
                    RunningAccountColumns.TAG_NO + " INTEGER DEFAULT 0," +

                    //标签编号外键约束
                    "CONSTRAINT " + RunningAccountConstraints.FK_TAG_NO +
                    " FOREIGN KEY (" + RunningAccountColumns.TAG_NO + ")" +
                    " REFERENCES " + RunningAccountTables.TAG + "(" + RunningAccountColumns.TAG_NO + ")" +
                    " ON DELETE SET DEFAULT" +
                    ")";
            db.execSQL(create);
        } catch (SQLException e) {
            ExceptionHelper.showExceptionDialog(context, e);
            Toast.makeText(context, "流水账基本数据库创建异常", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            //创建转账独占数据表
            create = "CREATE TABLE IF NOT EXISTS " + RunningAccountTables.TRANSFER + "(" +
                    RunningAccountColumns.RNO + " INTEGER PRIMARY KEY NOT NULL," +
                    RunningAccountColumns.EXPORT + " VARCHAR(20) NOT NULL," +
                    RunningAccountColumns.IMPORT + " VARCHAR(20) NOT NULL," +

                    //流水编号外键约束
                    "CONSTRAINT " + RunningAccountConstraints.FK_RNO +
                    " FOREIGN KEY (" + RunningAccountColumns.RNO + ")" +
                    " REFERENCES " + RunningAccountTables.BASIC + "(" + RunningAccountColumns.RNO + ")" +
                    " ON DELETE CASCADE" +
                    ")";
            db.execSQL(create);
        } catch (SQLException e) {
            ExceptionHelper.showExceptionDialog(context, e);
            Toast.makeText(context, "转账流水账数据库创建异常", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        while (oldVersion < newVersion) {
            if (oldVersion == 1) up1To2(db);
            else if (oldVersion == 2) up2To3(db);

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
}
