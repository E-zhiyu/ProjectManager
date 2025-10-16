package com.project.manager.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class FlowDatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "flow.db";
    private static final int DATABASE_VERSION = 1;
    public static final String TABLE_BASIC = "basic_data";
    public static final String TABLE_TRANSFER = "transfer_data";
    public static final String COLUMN_FNO = "Fno";
    public static final String COLUMN_AMOUNT = "Amount";
    public static final String COLUMN_TYPE = "Type";
    public static final String COLUMN_REMARK = "Remark";
    public static final String COLUMN_DATETIME = "DateTime";
    public static final String COLUMN_EXPORT = "Export";
    public static final String COLUMN_IMPORT = "Import";

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
        //创建流水基本数据表
        String createBasic = "CREATE TABLE IF NOT EXISTS " + TABLE_BASIC + "(" +
                COLUMN_FNO + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL," +
                COLUMN_AMOUNT + " DECIMAL(20,2) NOT NULL," +
                COLUMN_TYPE + " VARCHAR(15) NOT NULL," +
                COLUMN_REMARK + " VARCHAR(20)," +
                COLUMN_DATETIME + " DATETIME NOT NULL" +
                ")";
        db.execSQL(createBasic);

        //创建转账独占数据表
        String createTransfer = "CREATE TABLE IF NOT EXISTS " + TABLE_TRANSFER + "(" +
                COLUMN_FNO + " INT PRIMARY KEY NOT NULL," +
                COLUMN_EXPORT + " VARCHAR(20) NOT NULL," +
                COLUMN_IMPORT + " VARCHAR(20) NOT NULL" +
                ")";
        db.execSQL(createTransfer);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
