package com.manager.assistant.data.save.db;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.manager.assistant.data.save.db.converters.DateTimeConverter;
import com.manager.assistant.data.save.db.converters.UriConverter;
import com.manager.assistant.data.save.db.daos.AccountDao;
import com.manager.assistant.data.save.db.daos.TagDao;
import com.manager.assistant.data.save.db.entities.AccountTagRefEntity;
import com.manager.assistant.data.save.db.entities.BudgetEntity;
import com.manager.assistant.data.save.db.entities.BudgetTagRefEntity;
import com.manager.assistant.data.save.db.entities.MediaEntity;
import com.manager.assistant.data.save.db.entities.NotificationRuleEntity;
import com.manager.assistant.data.save.db.entities.AccountEntity;
import com.manager.assistant.data.save.db.entities.TagEntity;
import com.manager.assistant.data.save.db.entities.TagGroupEntity;
import com.manager.assistant.data.save.db.entities.AccountTransferEntity;
import com.manager.assistant.data.save.db.entities.RuleTransferAccountEntity;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.schedulers.Schedulers;

@Database(
        entities = {
                BudgetEntity.class,
                BudgetTagRefEntity.class,
                MediaEntity.class,
                NotificationRuleEntity.class,
                AccountEntity.class,
                AccountTagRefEntity.class,
                TagEntity.class,
                TagGroupEntity.class,
                AccountTransferEntity.class,
                RuleTransferAccountEntity.class
        },
        version = 1
)
@TypeConverters({
        DateTimeConverter.class,
        UriConverter.class
})
public abstract class BookkeepingDb extends RoomDatabase {
    private static volatile BookkeepingDb INSTANCE; //单例实例

    /**
     * 获取数据库实例
     *
     * @param context 上下文
     * @return 数据库实例
     */
    public static BookkeepingDb getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (BookkeepingDb.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    BookkeepingDb.class,
                                    "bookkeeping_database"
                            )
                            .addCallback(new Callback() {
                                @Override
                                public void onCreate(@NonNull SupportSQLiteDatabase db) {
                                    super.onCreate(db);

                                    //初始化默认分组逻辑
                                    getInstance(context).initDefaultTagGroup()
                                            .observeOn(AndroidSchedulers.mainThread())
                                            .subscribeOn(Schedulers.io())
                                            .subscribe();
                                }
                            })
                            .addMigrations()
                            .build();
                }
            }
        }

        return INSTANCE;
    }

    public abstract AccountDao accountDao();

    public abstract TagDao tagDao();

    /**
     * 初始化标签默认分组
     *
     * @return 是否完成
     */
    private Completable initDefaultTagGroup() {
        return Completable.defer(() -> {
            TagGroupEntity defaultGroup = new TagGroupEntity("默认分组");
            defaultGroup.setGroupId(-1);
            tagDao().insertTagGroup(defaultGroup);
            return Completable.complete();
        });
    }
}
