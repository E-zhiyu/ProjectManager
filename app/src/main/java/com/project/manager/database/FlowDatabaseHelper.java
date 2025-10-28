package com.project.manager.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class FlowDatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "flow.db";          //数据库名称
    private static final int DATABASE_VERSION = 1;                  //数据库版本

    public FlowDatabaseHelper(@Nullable Context context) {
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
        create = "CREATE TABLE IF NOT EXISTS " + FlowTables.TAG_GROUP + "(" +
                FlowColumns.GROUP_NO + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL," +
                FlowColumns.GROUP_NAME + " VARCHAR(20) NOT NULL UNIQUE" +
                ")";
        db.execSQL(create);

        //创建标签表
        create = "CREATE TABLE IF NOT EXISTS " + FlowTables.TAG + "(" +
                FlowColumns.TAG_NO + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL," +
                FlowColumns.TAG_NAME + " VARCHAR(20) NOT NULL UNIQUE," +
                FlowColumns.GROUP_NO + " VARCHAR(20) NOT NULL," +

                "FOREIGN KEY (" + FlowColumns.GROUP_NO + ") REFERENCES " + FlowTables.TAG_GROUP + "(" + FlowColumns.GROUP_NO + ")" +
                ")";
        db.execSQL(create);

        //创建流水基本数据表
        create = "CREATE TABLE IF NOT EXISTS " + FlowTables.BASIC + "(" +
                FlowColumns.FNO + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL," +
                FlowColumns.AMOUNT + " DECIMAL(20,2) NOT NULL," +
                FlowColumns.TYPE + " VARCHAR(15) NOT NULL," +
                FlowColumns.REMARK + " VARCHAR(20)," +
                FlowColumns.DATETIME + " DATETIME NOT NULL," +
                FlowColumns.TAG_NO + " INTEGER," +

                "FOREIGN KEY (" + FlowColumns.TAG_NO + ") REFERENCES " + FlowTables.TAG + "(" + FlowColumns.TAG_NO + ")" +
                ")";
        db.execSQL(create);

        //创建转账独占数据表
        create = "CREATE TABLE IF NOT EXISTS " + FlowTables.TRANSFER + "(" +
                FlowColumns.FNO + " INT PRIMARY KEY NOT NULL," +
                FlowColumns.EXPORT + " VARCHAR(20) NOT NULL," +
                FlowColumns.IMPORT + " VARCHAR(20) NOT NULL," +

                "FOREIGN KEY (" + FlowColumns.FNO + ") REFERENCES " + FlowTables.BASIC + "(" + FlowColumns.FNO + ")" +
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
