package com.project.manager.ui.bookkeeping.auto_bookkeeping.notification_analysis.package_name_select;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.search.SearchView;
import com.project.manager.R;
import com.project.manager.helpers.ExceptionHelper;
import com.project.manager.helpers.ImageHelper;
import com.project.manager.ui.bookkeeping.KeyValueStrings;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class PackageNameSelectActivity extends AppCompatActivity {
    private List<AppInfo> fullAppInfoList;                  //完整的应用列表
    private CompositeDisposable disposables = new CompositeDisposable();    //订阅列表
    private boolean isSysAppIncluded = false;               //应用列表是否包含系统应用
    private SearchView searchView;                          //搜索视图
    private AppListAdapter fullAppAdapter, searchAdapter;   //完整的应用列表适配器和搜索结果适配器

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_package_name_select);

        initViews();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 防止内存泄漏
        disposables.dispose();
    }

    //初始化视图
    private void initViews() {
        searchView = findViewById(R.id.search_view);

        RecyclerView full_app_list_recycler = findViewById(R.id.app_list_recycler);         //打开页面时显示的完整应用列表视图
        RecyclerView search_result_recycler = findViewById(R.id.search_result_recycler);    //搜索结果列表视图

        //使用多线程实现应用列表加载
        disposables.add(
                Observable.fromCallable(() ->
                        fullAppInfoList = getInstalledApps()
                )
                .subscribeOn(Schedulers.io()) //在 IO 线程执行查询
                .observeOn(AndroidSchedulers.mainThread()) //切换到主线程更新 UI
                .subscribe(
                        apps -> {
                            fullAppAdapter = new AppListAdapter(fullAppInfoList, this::onAppClicked, this);
                            full_app_list_recycler.setAdapter(fullAppAdapter);
                        },  //成功回调
                        e -> {
                            fullAppAdapter = new AppListAdapter(new ArrayList<>(), this::onAppClicked, this);
                            full_app_list_recycler.setAdapter(fullAppAdapter);
                            ExceptionHelper.showExceptionDialog(this, e);
                        }   //错误处理
                ));

        full_app_list_recycler.setAdapter(fullAppAdapter);
        searchAdapter = new AppListAdapter(new ArrayList<>(), this::onAppClicked, this);
        search_result_recycler.setAdapter(searchAdapter);
    }

    /**
     * 加载应用列表
     *
     * @return 包含应用信息的列表
     */
    @NonNull
    private List<AppInfo> getInstalledApps() {
        List<AppInfo> appInfoList = new ArrayList<>();
        PackageManager pm = getPackageManager();
        List<ApplicationInfo> apps = pm.getInstalledApplications(PackageManager.GET_META_DATA);

        for (ApplicationInfo app : apps) {
            if (!isSysAppIncluded && ((app.flags & ApplicationInfo.FLAG_SYSTEM) != 0))
                continue;   //动态排除系统应用

            String appName = pm.getApplicationLabel(app).toString();    //获取应用名称
            String packageName = app.packageName;                       //获取包名
            Drawable originIcon;                                        //获取应用图标（Drawable）
            try {
                originIcon = pm.getApplicationIcon(packageName);
            } catch (PackageManager.NameNotFoundException e) {
                ExceptionHelper.showExceptionDialog(this, e);
                Toast.makeText(this, "获取应用图标时出错", Toast.LENGTH_SHORT).show();
                originIcon = AppCompatResources.getDrawable(this, R.mipmap.unknown_app_ic_channel);
            }

            Drawable scaledIcon = ImageHelper.resizeIcon(originIcon, 48, this);
            AppInfo appInfo = new AppInfo(appName, packageName, scaledIcon);
            appInfoList.add(appInfo);
        }

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