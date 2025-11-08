package com.project.manager.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class RunningAccountDatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "running_account.db";   //数据库名称
    private static final int DATABASE_VERSION = 1;                      //数据库版本

    public RunningAccountDatabaseHelper(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
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
    public void onCreate(SQLiteDatabase db) {
        String create;

        //创建标签分组表
        create = "CREATE TABLE IF NOT EXISTS " + RunningAccountTables.TAG_GROUP + "(" +
                RunningAccountColumns.GROUP_NO + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL," +
                RunningAccountColumns.GROUP_NAME + " VARCHAR(20) NOT NULL UNIQUE" +
                ")";
        db.execSQL(create);

        //创建标签表
        create = "CREATE TABLE IF NOT EXISTS " + RunningAccountTables.TAG + "(" +
                RunningAccountColumns.TAG_NO + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL," +
                RunningAccountColumns.TAG_NAME + " VARCHAR(20) NOT NULL UNIQUE," +
                RunningAccountColumns.GROUP_NO + " INTEGER NOT NULL," +

                "FOREIGN KEY (" + RunningAccountColumns.GROUP_NO + ") REFERENCES " + RunningAccountTables.TAG_GROUP + "(" + RunningAccountColumns.GROUP_NO + ")" +
                ")";
        db.execSQL(create);

        //创建流水基本数据表
        create = "CREATE TABLE IF NOT EXISTS " + RunningAccountTables.BASIC + "(" +
                RunningAccountColumns.RNO + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL," +
                RunningAccountColumns.AMOUNT + " DECIMAL(20,2) NOT NULL," +
                RunningAccountColumns.TYPE + " VARCHAR(15) NOT NULL," +
                RunningAccountColumns.REMARK + " VARCHAR(20)," +
                RunningAccountColumns.DATETIME + " DATETIME NOT NULL," +
                RunningAccountColumns.TAG_NO + " INTEGER," +

                "FOREIGN KEY (" + RunningAccountColumns.TAG_NO + ") REFERENCES " + RunningAccountTables.TAG + "(" + RunningAccountColumns.TAG_NO + ")" +
                ")";
        db.execSQL(create);

        //创建转账独占数据表
        create = "CREATE TABLE IF NOT EXISTS " + RunningAccountTables.TRANSFER + "(" +
                RunningAccountColumns.RNO + " INTEGER PRIMARY KEY NOT NULL," +
                RunningAccountColumns.EXPORT + " VARCHAR(20) NOT NULL," +
                RunningAccountColumns.IMPORT + " VARCHAR(20) NOT NULL," +

                "FOREIGN KEY (" + RunningAccountColumns.RNO + ") REFERENCES " + RunningAccountTables.BASIC + "(" + RunningAccountColumns.RNO + ")" +
                ")";
        db.execSQL(create);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        while (oldVersion < newVersion) {
            oldVersion++;
        }
    }
}
