package com.sly.coffer.auxiliary.enums;

public enum TransitionName {
    ACCOUNT_MEDIA("account_media");
    private final String s;

    TransitionName(String s) {
        this.s = s;
    }

    public String getS() {
        return s;
    }
}
