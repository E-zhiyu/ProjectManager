package com.sly.coffer.ui.pages.budget;

public enum ResetFrequency {
    EVERY_DAY("每天", "EVERY_DAY"),
    EVERY_WEEK("每星期", "EVERY_WEEK"),
    EVERY_MONTH("每个月", "EVERY_MONTH"),
    FOREVER("永不", "FOREVER");
    private final String title;     //显示名称
    private final String oldValue;  //旧版的枚举名称

    ResetFrequency(String title, String oldValue) {
        this.title = title;
        this.oldValue = oldValue;
    }

    public String getTitle() {
        return title;
    }

    /**
     * 从旧枚举名称获取实例
     *
     * @param value 旧枚举名称
     * @return 重置频率实例
     */
    public static ResetFrequency fromOldValue(String value) {
        for (ResetFrequency frequency : values()) {
            if (frequency.oldValue.equals(value)) {
                return frequency;
            }
        }

        return FOREVER;
    }
}
