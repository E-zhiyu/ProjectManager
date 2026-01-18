package com.manager.assistant.data.data_class;

/**
 * 网站超链接数据类
 */
public class WebLink {
    private final String title;   //链接标题
    private final String url;     //链接Url

    public WebLink(String title, String url) {
        this.title = title;
        this.url = url;
    }

    public String getTitle() {
        return title;
    }

    public String getUrl() {
        return url;
    }
}
