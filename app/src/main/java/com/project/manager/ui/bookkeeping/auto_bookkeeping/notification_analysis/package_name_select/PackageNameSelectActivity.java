package com.project.manager.ui.bookkeeping.auto_bookkeeping.notification_analysis.package_name_select;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.search.SearchView;
import com.project.manager.R;
import com.project.manager.ui.bookkeeping.KeyValueStrings;

import java.util.ArrayList;
import java.util.List;

public class PackageNameSelectActivity extends AppCompatActivity {
    private List<AppInfo> fullAppInfoList;                  //完整的应用列表
    private boolean isSysAppIncluded;                       //应用列表是否包含系统应用
    private SearchView searchView;                          //搜索视图
    private AppListAdapter fullAppAdapter, searchAdapter;   //完整的应用列表适配器和搜索结果适配器

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_package_name_select);

        initViews();
    }

    //初始化视图
    private void initViews() {
        searchView = findViewById(R.id.search_view);

        //TODO: 将下面的代码放置于另一个线程中执行
        fullAppInfoList = getInstalledApps();
        RecyclerView full_app_list_recycler = findViewById(R.id.app_list_recycler);         //打开页面时显示的完整应用列表视图
        fullAppAdapter = new AppListAdapter(fullAppInfoList, this::onAppClicked, this);
        full_app_list_recycler.setAdapter(fullAppAdapter);
        RecyclerView search_result_recycler = findViewById(R.id.search_result_recycler);    //搜索结果列表视图
        searchAdapter = new AppListAdapter(new ArrayList<>(), this::onAppClicked, this);
        search_result_recycler.setAdapter(searchAdapter);
    }

    /**
     * 加载应用列表
     *
     * @return 包含应用信息的列表
     */
    private List<AppInfo> getInstalledApps() {
        List<AppInfo> appInfoList = new ArrayList<>();

        return appInfoList;
    }

    //处理应用选择的方法
    private void onAppClicked(String package_name) {
        Intent result2RuleAddActivity = new Intent();
        result2RuleAddActivity.putExtra(KeyValueStrings.PACKAGE_NAME.getValue(), package_name);
        setResult(Activity.RESULT_OK, result2RuleAddActivity);
        finish();
    }
}