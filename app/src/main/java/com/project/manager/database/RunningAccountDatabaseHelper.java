package com.project.manager.database;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.project.manager.exception.ExceptionHelper;

public class RunningAccountDatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "running_account.db";   //数据库名称
    private static final int DATABASE_VERSION = 1;                      //数据库版本
    private final Context context;                                      //上下文

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
    public void onCreate(SQLiteDatabase db) {
        String create;

        try {
            //创建标签分组表
            create = "CREATE TABLE IF NOT EXISTS " + RunningAccountTables.TAG_GROUP + "(" +
                    RunningAccountColumns.GROUP_NO + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL," +
                    RunningAccountColumns.GROUP_NAME + " VARCHAR(20) NOT NULL UNIQUE" +
                    ")";
            db.execSQL(create);
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
                    RunningAccountColumns.TAG_NO + " INTEGER," +

                    //标签编号外键约束
                    "CONSTRAINT " + RunningAccountConstraints.FK_TAG_NO +
                    " FOREIGN KEY (" + RunningAccountColumns.TAG_NO + ")" +
                    " REFERENCES " + RunningAccountTables.TAG + "(" + RunningAccountColumns.TAG_NO + ")" +
                    " ON DELETE CASCADE" +
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

                    //分组编号外键约束
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
            oldVersion++;
        }
    }
}
