package com.manager.assistant.data.save.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.manager.assistant.helpers.ExceptionHelper;
import com.manager.assistant.ui.pages.bookkeeping.running_account.fragments.RunningAccountType;

public class BookkeepingDbHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "running_account.db";   //数据库名称
    private static final int DATABASE_VERSION = 8;                      //数据库版本
    private final Context context;                                      //上下文
    public static final String defaultGroupName = "默认分组";           //默认分组名称

    public String getDatabaseName() {
        return DATABASE_NAME;
    }

    public BookkeepingDbHelper(@Nullable Context context) {
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
            err = "标签分组表失败";
            create = "CREATE TABLE IF NOT EXISTS " + Tables.TAG_GROUP + "(" +
                    Columns.GROUP_NO + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    Columns.GROUP_NAME + " VARCHAR(20) NOT NULL UNIQUE" +
                    ")";
            db.execSQL(create);

            ContentValues default_group_values = new ContentValues();
            default_group_values.put(Columns.GROUP_NO.toString(), 0);
            default_group_values.put(Columns.GROUP_NAME.toString(), defaultGroupName);
            db.insert(Tables.TAG_GROUP.toString(), null, default_group_values);

            //创建标签表
            err = "标签表创建失败";
            create = "CREATE TABLE IF NOT EXISTS " + Tables.TAG + "(" +
                    Columns.TAG_NO + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    Columns.TAG_NAME + " VARCHAR(20) NOT NULL UNIQUE," +
                    Columns.GROUP_NO + " INTEGER NOT NULL," +
                    Columns.TAG_SCOPE + " INTEGER DEFAULT 0," +  //默认为0，为了让旧版升级的标签默认对所有种类可见，每位中0表示可见，1则表示不可见

                    //分组编号外键约束
                    "CONSTRAINT " + Constraints.FK_GROUP_NO +
                    " FOREIGN KEY (" + Columns.GROUP_NO + ")" +
                    " REFERENCES " + Tables.TAG_GROUP + "(" + Columns.GROUP_NO + ")" +
                    " ON DELETE CASCADE" +
                    ")";
            db.execSQL(create);

            //创建流水基本数据表
            err = "流水基本数据表创建失败";
            create = "CREATE TABLE IF NOT EXISTS " + Tables.BASIC + "(" +
                    Columns.RNO + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    Columns.AMOUNT + " DECIMAL(20,2) NOT NULL," +
                    Columns.TYPE + " VARCHAR(15) NOT NULL," +
                    Columns.REMARK + " VARCHAR(20)," +
                    Columns.DATETIME + " DATETIME NOT NULL," +
                    Columns.TAG_NO + " INTEGER DEFAULT 0," +

                    //标签编号外键约束
                    "CONSTRAINT " + Constraints.FK_TAG_NO +
                    " FOREIGN KEY (" + Columns.TAG_NO + ")" +
                    " REFERENCES " + Tables.TAG + "(" + Columns.TAG_NO + ")" +
                    " ON DELETE SET DEFAULT" +
                    ")";
            db.execSQL(create);

            //创建转账独占数据表
            err = "转账流水表创建失败";
            create = "CREATE TABLE IF NOT EXISTS " + Tables.TRANSFER + "(" +
                    Columns.RNO + " INTEGER PRIMARY KEY," +
                    Columns.EXPORT + " VARCHAR(20) NOT NULL," +
                    Columns.IMPORT + " VARCHAR(20) NOT NULL," +

                    //流水编号外键约束
                    "CONSTRAINT " + Constraints.FK_RNO +
                    " FOREIGN KEY (" + Columns.RNO + ")" +
                    " REFERENCES " + Tables.BASIC + "(" + Columns.RNO + ")" +
                    " ON DELETE CASCADE" +
                    ")";
            db.execSQL(create);

            //创建通知解析规则表
            err = "通知解析规则表创建失败";
            create = "CREATE TABLE IF NOT EXISTS " + Tables.ANALYSIS_RULE + "(" +
                    Columns.RULE_NAME + " VARCHAR(20) NOT NULL," +
                    Columns.RULE_NO + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    Columns.TYPE + " VARCHAR(20) NOT NULL," +
                    Columns.TAG_NO + " INTEGER DEFAULT 0," +
                    Columns.PACKAGE_NAME + " VARCHAR(50) NOT NULL," +
                    Columns.NOTIFICATION_TITLE + " VARCHAR(20) NOT NULL," +
                    Columns.NOTIFICATION_CONTENT + " VARCHAR(50) NOT NULL," +

                    "CONSTRAINT " + Constraints.FK_TAG_NO +
                    " FOREIGN KEY (" + Columns.TAG_NO + ")" +
                    " REFERENCES " + Tables.TAG + "(" + Columns.TAG_NO + ")" +
                    " ON DELETE SET DEFAULT" +
                    ")";
            db.execSQL(create);

            addDefaultRule(db); //添加默认规则

            //创建转账类型规则的账户基本表
            err = "通知解析规则的转账账户表创建失败";
            create = "CREATE TABLE IF NOT EXISTS " + Tables.RULE_ACCOUNT + "(" +
                    Columns.RULE_NO + " INTEGER PRIMARY KEY," +
                    Columns.EXPORT + " VARCHAR(20) NOT NULL," +
                    Columns.IMPORT + " VARCHAR(20) NOT NULL," +

                    "CONSTRAINT " + Constraints.FK_RULE_NO +
                    " FOREIGN KEY (" + Columns.RULE_NO + ")" +
                    " REFERENCES " + Tables.ANALYSIS_RULE + "(" + Columns.RULE_NO + ")" +
                    " ON DELETE CASCADE" +
                    ")";
            db.execSQL(create);

            //创建流水图片基本表
            err = "流水图片表创建失败";
            create = "CREATE TABLE IF NOT EXISTS " + Tables.PICTURE + "(" +
                    Columns.RNO + " INTEGER NOT NULL," +
                    Columns.PNO + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    Columns.PICTURE_URI + " VARCHAR(100) NOT NULL," +

                    "CONSTRAINT " + Constraints.FK_RNO +
                    " FOREIGN KEY (" + Columns.RNO + ")" +
                    " REFERENCES " + Tables.BASIC + "(" + Columns.RNO + ")" +
                    " ON DELETE CASCADE" +
                    ")";
            db.execSQL(create);

            //创建预算基本表
            err = "预算表创建失败";
            create = "CREATE TABLE IF NOT EXISTS " + Tables.BUDGET + "(" +
                    Columns.BNO + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    Columns.BUDGET_NAME + " VARCHAR(20) NOT NULL," +
                    Columns.INIT_AMOUNT + " DECIMAL(20,2) NOT NULL," +
                    Columns.LEFT_AMOUNT + " DECIMAL(20,2) NOT NULL," +
                    Columns.START_DATE + " DATE NOT NULL," +
                    Columns.RESET_FREQUENCY + " VARCHAR(20) NOT NULL" +
                    ")";
            db.execSQL(create);

            //创建预算标签表
            err = "预算标签表创建失败";
            create = "CREATE TABLE IF NOT EXISTS " + Tables.BUDGET_TAG + "(" +
                    Columns.BNO + " INTEGER NOT NULL," +
                    Columns.TAG_NO + " INTEGER NOT NULL," +

                    "CONSTRAINT " + Constraints.FK_BUDGET_NO +
                    " FOREIGN KEY (" + Columns.BNO + ")" +
                    " REFERENCES " + Tables.BUDGET + "(" + Columns.BNO + ")" +
                    " ON DELETE CASCADE," +
                    "CONSTRAINT " + Constraints.FK_TAG_NO +
                    " FOREIGN KEY (" + Columns.TAG_NO + ")" +
                    " REFERENCES " + Tables.TAG + "(" + Columns.TAG_NO + ")" +
                    " ON DELETE CASCADE" +
                    ")";
            db.execSQL(create);
        } catch (SQLiteException e) {
            ExceptionHelper.showExceptionDialog(context, e);
            Toast.makeText(context, err, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        switch (oldVersion) {
            case 1:
                up1To2(db);
            case 2:
                up2To3(db);
            case 3:
                up3To4(db);
            case 4:
                up4To5(db);
            case 5:
                up5To6(db);
            case 6:
                up6To7(db);
            case 7:
                up7To8(db);
        }
    }

    //basic表Tag_no列添加默认值0
    //basic表Tag_no外键约束删除动作改为SET DEFAULT
    private void up1To2(@NonNull SQLiteDatabase db) {
        //创建新的临时表
        String sql = "CREATE TABLE IF NOT EXISTS new_basic_data(" +
                "Rno INTEGER PRIMARY KEY AUTOINCREMENT," +
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

    //tag_group表添加一条group_no=0,group_name=defaultGroupName的记录
    private void up2To3(@NonNull SQLiteDatabase db) {
        ContentValues default_group_values = new ContentValues();
        default_group_values.put("GroupNO", 0);
        default_group_values.put("GroupName", defaultGroupName);
        db.insert("tag_group_data", null, default_group_values);
    }

    //添加通知解析规则表以及默认规则
    private void up3To4(@NonNull SQLiteDatabase db) {
        String err = "通知解析规则表创建错误";
        try {
            String create = "CREATE TABLE IF NOT EXISTS analysis_rule_data(" +
                    "Rule_name VARCHAR(20) NOT NULL," +
                    "Rule_no INTEGER PRIMARY KEY AUTOINCREMENT," +
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

            addDefaultRule(db);
        } catch (SQLException e) {
            ExceptionHelper.showExceptionDialog(context, e);
            Toast.makeText(context, err, Toast.LENGTH_SHORT).show();
        }
    }

    //添加图片表
    private void up4To5(@NonNull SQLiteDatabase db) {
        String err = "流水图片表创建失败";
        try {
            String create = "CREATE TABLE IF NOT EXISTS " + Tables.PICTURE + "(" +
                    Columns.RNO + " INTEGER NOT NULL," +
                    Columns.PNO + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    Columns.PICTURE_URI + " VARCHAR(100) NOT NULL," +

                    "CONSTRAINT " + Constraints.FK_RNO +
                    " FOREIGN KEY (" + Columns.RNO + ")" +
                    " REFERENCES " + Tables.BASIC + "(" + Columns.RNO + ")" +
                    " ON DELETE CASCADE" +
                    ")";
            db.execSQL(create);
        } catch (SQLException e) {
            ExceptionHelper.showExceptionDialog(context, e);
            Toast.makeText(context, err, Toast.LENGTH_SHORT).show();
        }
    }

    //添加标签作用域字段
    private void up5To6(@NonNull SQLiteDatabase db) {
        String err = "标签表升级失败";
        try {
            String sql = "ALTER TABLE tag_data ADD COLUMN TagScope INTEGER DEFAULT 0";
            db.execSQL(sql);
        } catch (SQLException e) {
            ExceptionHelper.showExceptionDialog(context, e);
            Toast.makeText(context, err, Toast.LENGTH_SHORT).show();
        }
    }

    //添加通知解析规则的转入转出账户表
    private void up6To7(@NonNull SQLiteDatabase db) {
        String err = "规则账户表创建失败";
        try {
            String create = "CREATE TABLE IF NOT EXISTS " + Tables.RULE_ACCOUNT + "(" +
                    Columns.RULE_NO + " INTEGER PRIMARY KEY," +
                    Columns.EXPORT + " VARCHAR(20) NOT NULL," +
                    Columns.IMPORT + " VARCHAR(20) NOT NULL," +

                    "CONSTRAINT " + Constraints.FK_RULE_NO +
                    " FOREIGN KEY (" + Columns.RULE_NO + ")" +
                    " REFERENCES " + Tables.ANALYSIS_RULE + "(" + Columns.RULE_NO + ")" +
                    " ON DELETE CASCADE" +
                    ")";
            db.execSQL(create);
        } catch (SQLiteException e) {
            ExceptionHelper.showExceptionDialog(context, e);
            Toast.makeText(context, err, Toast.LENGTH_SHORT).show();
        }
    }

    //添加预算相关的表
    private void up7To8(@NonNull SQLiteDatabase db) {
        String err = "";
        try {
            err = "预算表创建失败";
            String create = "CREATE TABLE IF NOT EXISTS " + Tables.BUDGET + "(" +
                    Columns.BNO + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    Columns.BUDGET_NAME + " VARCHAR(20) NOT NULL," +
                    Columns.INIT_AMOUNT + " DECIMAL(20,2) NOT NULL," +
                    Columns.LEFT_AMOUNT + " DECIMAL(20,2) NOT NULL," +
                    Columns.START_DATE + " DATE NOT NULL," +
                    Columns.RESET_FREQUENCY + " VARCHAR(20) NOT NULL" +
                    ")";
            db.execSQL(create);

            err = "预算标签表创建失败";
            create = "CREATE TABLE IF NOT EXISTS " + Tables.BUDGET_TAG + "(" +
                    Columns.BNO + " INTEGER NOT NULL," +
                    Columns.TAG_NO + " INTEGER NOT NULL," +

                    "CONSTRAINT " + Constraints.FK_BUDGET_NO +
                    " FOREIGN KEY (" + Columns.BNO + ")" +
                    " REFERENCES " + Tables.BUDGET + "(" + Columns.BNO + ")" +
                    " ON DELETE CASCADE," +
                    "CONSTRAINT " + Constraints.FK_TAG_NO +
                    " FOREIGN KEY (" + Columns.TAG_NO + ")" +
                    " REFERENCES " + Tables.TAG + "(" + Columns.TAG_NO + ")" +
                    " ON DELETE CASCADE" +
                    ")";
            db.execSQL(create);
        } catch (SQLException e) {
            ExceptionHelper.showExceptionDialog(context, e);
            Toast.makeText(context, err, Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 添加默认规则
     *
     * @param db 通知解析规则表所在的数据库
     */
    public static void addDefaultRule(@NonNull SQLiteDatabase db) {
        //微信支付
        ContentValues rule_values = new ContentValues();
        rule_values.put(Columns.RULE_NAME.toString(), "微信支付");                       //名称
        rule_values.put(Columns.TYPE.toString(), RunningAccountType.EXPENSE.toString());//流水种类
        rule_values.put(Columns.PACKAGE_NAME.toString(), "com.tencent.mm");              //包名
        rule_values.put(Columns.NOTIFICATION_TITLE.toString(), "微信支付");              //通知标题
        rule_values.put(Columns.NOTIFICATION_CONTENT.toString(), "已支付.(\\d+\\.?\\d{0,2})"); //匹配通知内容
        db.insert(Tables.ANALYSIS_RULE.toString(), null, rule_values);

        //支付宝支付
        rule_values.clear();
        rule_values.put(Columns.RULE_NAME.toString(), "支付宝支付");                       //名称
        rule_values.put(Columns.TYPE.toString(), RunningAccountType.EXPENSE.toString());//流水种类
        rule_values.put(Columns.PACKAGE_NAME.toString(), "com.eg.android.AlipayGphone");//包名
        rule_values.put(Columns.NOTIFICATION_TITLE.toString(), "交易提醒");              //通知标题
        rule_values.put(Columns.NOTIFICATION_CONTENT.toString(), "有一笔(\\d+\\.?\\d{0,2})元的支出"); //匹配通知内容
        db.insert(Tables.ANALYSIS_RULE.toString(), null, rule_values);
    }
}
