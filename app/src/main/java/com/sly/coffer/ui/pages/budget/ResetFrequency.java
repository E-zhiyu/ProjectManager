package com.sly.coffer.ui.pages.budget;

public enum ResetFrequency {
    EVERY_DAY("每天"),
    EVERY_WEEK("每星期"),
    EVERY_MONTH("每个月"),
    FOREVER("永不");
    private final String title;     //显示名称

    ResetFrequency(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }
}
