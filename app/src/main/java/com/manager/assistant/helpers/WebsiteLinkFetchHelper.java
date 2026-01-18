package com.manager.assistant.helpers;

import androidx.annotation.NonNull;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

public class WebsiteLinkFetchHelper {
    @JsonIgnoreProperties(ignoreUnknown = true) // 忽略JSON中多余字段
    public static class WebLink {
        private String title;   //链接标题
        private String pageurl; //链接Url

        public WebLink(String title, String pageurl) {
            this.title = title;
            this.pageurl = pageurl;
        }

        public WebLink() {
        }

        @JsonSetter("title")
        public void setTitle(String title) {
            this.title = title;
        }

        @JsonSetter("pageurl")
        public void setPageurl(String pageurl) {
            this.pageurl = String.format("https://www.ccgp-shaanxi.gov.cn/%s", pageurl);
        }

        @JsonGetter("title")
        public String getTitle() {
            return title;
        }

        @JsonGetter("pageurl")
        public String getPageurl() {
            return pageurl;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true) // 忽略JSON中多余字段
    public static class LinkDataMap {
        private List<WebLink> data;

        public LinkDataMap() {
        }

        @JsonGetter("data")
        public List<WebLink> getData() {
            return data;
        }

        @JsonSetter("data")
        public void setData(List<WebLink> data) {
            this.data = data;
        }
    }



    @NonNull
    public static List<WebLink> getUrlJson(String sourceUrl) throws IOException {
        URL url = new URL(sourceUrl);
        HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setConnectTimeout(10_000);    //设置连接超时
        connection.setReadTimeout(10_000);       //设置读取超时

        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        StringBuilder urlData = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            urlData.append(line);
            urlData.append("\n");
        }
        reader.close();

        ObjectMapper mapper = new ObjectMapper();
        LinkDataMap data = mapper.readValue(urlData.toString(), LinkDataMap.class);
        return data.getData();
    }
}
