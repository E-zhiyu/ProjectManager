package com.manager.assistant.data.save.db;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.manager.assistant.data.save.db.converters.DateTimeConverter;
import com.manager.assistant.data.save.db.converters.UriConverter;
import com.manager.assistant.data.save.db.daos.AccountDao;
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
                            .addMigrations()
                            .build();
                }
            }
        }

        return INSTANCE;
    }

    public abstract AccountDao accountDao();
}
