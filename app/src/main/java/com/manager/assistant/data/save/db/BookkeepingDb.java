package com.manager.assistant.data.save.db;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.manager.assistant.data.save.db.converters.DateTimeConverter;
import com.manager.assistant.data.save.db.converters.UriConverter;
import com.manager.assistant.data.save.db.entity.BudgetEntity;
import com.manager.assistant.data.save.db.entity.BudgetTagRefEntity;
import com.manager.assistant.data.save.db.entity.MediaEntity;
import com.manager.assistant.data.save.db.entity.NotificationRuleEntity;
import com.manager.assistant.data.save.db.entity.RunningAccountEntity;
import com.manager.assistant.data.save.db.entity.TagEntity;
import com.manager.assistant.data.save.db.entity.TagGroupEntity;
import com.manager.assistant.data.save.db.entity.TransferAccountEntity;
import com.manager.assistant.data.save.db.entity.TransferRuleAccountEntity;

@Database(
        entities = {
                BudgetEntity.class,
                BudgetTagRefEntity.class,
                MediaEntity.class,
                NotificationRuleEntity.class,
                RunningAccountEntity.class,
                TagEntity.class,
                TagGroupEntity.class,
                TransferAccountEntity.class,
                TransferRuleAccountEntity.class
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
//                            .addMigrations()
                            .build();
                }
            }
        }

        return INSTANCE;
    }
}
