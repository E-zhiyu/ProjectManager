package com.manager.assistant.ui.pages.bookkeeping.budget;

public enum ResetFrequency {
    EVERY_DAY("每天"),
    EVERY_WEEK("每星期"),
    EVERY_MONTH("每个月"),
    MANUAL("手动");
    private final String title;     //显示名称

    ResetFrequency(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }
}
