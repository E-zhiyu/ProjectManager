package com.manager.assistant.auxiliary.enums;

import android.content.Context;

import androidx.annotation.Nullable;

import com.manager.assistant.data.backup.helpers.BackupHelperBase;
import com.manager.assistant.data.backup.helpers.BudgetBackupHelper;
import com.manager.assistant.data.backup.helpers.NotificationRuleBackupHelper;
import com.manager.assistant.data.backup.helpers.RunningAccountBackupHelper;

import java.util.function.Function;

/**
 * 备份的数据类型
 */
public enum BackupDataType {
    RUNNING_ACCOUNT(
            "流水记录数据",
            "RunningAccount.json",
            RunningAccountBackupHelper::new
    ),
    NOTIFICATION_RULE(
            "通知解析规则数据",
            "NotificationRule.json",
            NotificationRuleBackupHelper::new
    ),
    BUDGET(
            "预算数据",
            "Budget.json",
            BudgetBackupHelper::new
    );
    private final String title;
    private final String fileName;
    private final Function<Context, BackupHelperBase<?, ?>> helperFactory;

    BackupDataType(String title, String fileName, Function<Context, BackupHelperBase<?, ?>> helperFactory) {
        this.title = title;
        this.fileName = fileName;
        this.helperFactory = helperFactory;
    }

    public String getTitle() {
        return title;
    }

    public String getFileName() {
        return fileName;
    }

    /**
     * 创建备份帮助器
     *
     * @param context 上下文
     * @return 备份帮助器实例
     */
    public BackupHelperBase<?, ?> createBackupHelper(Context context) {
        return helperFactory.apply(context);
    }

    /**
     * 根据文件名称判断数据种类
     *
     * @param fileName 文件名称
     * @return 数据类型，若无法匹配类型则返回 null
     */
    @Nullable
    public static BackupDataType fromFileName(String fileName) {
        for (BackupDataType type : values()) {
            if (type.getFileName().equals(fileName)) {
                return type;
            }
        }
        return null; // 或者返回一个表示“未知”的类型
    }
}
