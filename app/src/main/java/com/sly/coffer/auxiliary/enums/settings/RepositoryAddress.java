package com.sly.coffer.auxiliary.enums.settings;

public enum RepositoryAddress {
    GITEE("前往Gitee", "https://gitee.com/e-zhiyu/sly-coffer"),
    GITHUB("前往GitHub", "https://github.com/E-zhiyu/SlyCoffer");
    private final String title;
    private final String address;

    RepositoryAddress(String title, String address) {
        this.title = title;
        this.address = address;
    }

    public String getTitle() {
        return title;
    }

    public String getAddress() {
        return address;
    }
}
