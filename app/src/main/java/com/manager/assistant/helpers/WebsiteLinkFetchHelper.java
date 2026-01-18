package com.manager.assistant.helpers;

import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;

import com.manager.assistant.data.data_class.WebLink;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.http.GET;
import retrofit2.http.Url;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public class WebsiteLinkFetchHelper {

    private final WebsiteService websiteService;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    public interface WebsiteService {
        @GET
        Call<String> fetchWebsiteContent(@Url String url);
    }

    public interface ScrapeCallback {
        void onSuccess(List<WebLink> links);

        void onFailure(String errorMessage);
    }

    /**
     * 网页链接爬取工具构造器
     *
     * @param webLink 待爬取的网页链接
     */
    public WebsiteLinkFetchHelper(String webLink) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(webLink)
                .addConverterFactory(ScalarsConverterFactory.create())
                .build();

        websiteService = retrofit.create(WebsiteService.class);
    }

    /**
     * 爬取指定URL的链接
     *
     * @param url      目标网站URL
     * @param callback 回调接口
     */
    public void scrapeLinks(String url, ScrapeCallback callback) {
        websiteService.fetchWebsiteContent(url).enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                if (response.isSuccessful() && response.body() != null) {
                    parseHtmlInBackground(response.body(), callback);
                } else {
                    postError("网络请求失败", callback);
                }
            }

            @Override
            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                postError("请求异常，请检查网络连接", callback);
            }
        });
    }

    /**
     * 在后台线程解析HTML
     */
    private void parseHtmlInBackground(String html, ScrapeCallback callback) {
        new Thread(() -> {
            try {
                Document doc = Jsoup.parse(html);
                //通过ul的id定位目标板块
                Element ulElement = doc.getElementById("electronicStoreShow");
                if (ulElement == null) {
                    postError("未找到目标 ul 列表", callback);
                    return;
                }

                //提取所有li中的链接
                List<WebLink> linkList = new ArrayList<>();
                Elements liElements = ulElement.select("li");   //TODO:动态加载无法直接读取
                for (Element li : liElements) {
                    Element linkElement = li.selectFirst("a");

                    if (linkElement != null) {
                        String text = linkElement.text().trim();
                        String href = linkElement.attr("abs:href"); // 获取绝对URL

                        //过滤无效链接
                        if (text.isEmpty()) {
                            continue;   //过滤空标题链接
                        }
                        if (!href.startsWith("http://") && !href.startsWith("https://")) {
                            continue;   //跳过非HTTP/HTTPS链接
                        }

                        linkList.add(new WebLink(text, href));
                    }
                }

                postSuccess(linkList, callback);
            } catch (Exception e) {
                postError("解析失败: " + e.getMessage(), callback);
            }
        }).start();
    }

    /**
     * 切换到主线程返回成功结果
     */
    private void postSuccess(List<WebLink> links, ScrapeCallback callback) {
        mainHandler.post(() -> callback.onSuccess(links));
    }

    /**
     * 切换到主线程返回错误
     */
    private void postError(String error, ScrapeCallback callback) {
        mainHandler.post(() -> callback.onFailure(error));
    }
}
