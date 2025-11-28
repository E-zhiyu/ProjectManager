package com.project.manager.ui.bookkeeping.auto_bookkeeping.notification_analysis.package_name_select;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.TypedValue;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.search.SearchView;
import com.project.manager.R;
import com.project.manager.helpers.ExceptionHelper;
import com.project.manager.ui.bookkeeping.KeyValueStrings;

import java.util.ArrayList;
import java.util.List;

public class PackageNameSelectActivity extends AppCompatActivity {
    private List<AppInfo> fullAppInfoList;                  //完整的应用列表
    private boolean isSysAppIncluded = false;               //应用列表是否包含系统应用
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
    @NonNull
    private List<AppInfo> getInstalledApps() {
        List<AppInfo> appInfoList = new ArrayList<>();
        PackageManager pm = getPackageManager();
        List<ApplicationInfo> apps = pm.getInstalledApplications(PackageManager.GET_META_DATA);

        for (ApplicationInfo app : apps) {
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

            // 统一缩放为固定尺寸（例如 48x48dp）
            int targetSize = (int) TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP,
                    48,
                    getResources().getDisplayMetrics()
            );

            Drawable scaledIcon = null;
            if (originIcon != null) {
                Bitmap bitmap = drawableToBitmap(originIcon, targetSize, targetSize);
                Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, targetSize, targetSize, true);
                scaledIcon = new BitmapDrawable(getResources(), scaledBitmap);
            }

            AppInfo appInfo = new AppInfo(appName, packageName, scaledIcon);
            appInfoList.add(appInfo);
        }

        return appInfoList;
    }

    /**
     * 将Drawable转换为Bitmap
     *
     * @param drawable     原Drawable图标
     * @param targetWidth  目标宽度
     * @param targetHeight 目标高度
     * @return 转换后的图标
     */
    private Bitmap drawableToBitmap(Drawable drawable, int targetWidth, int targetHeight) {
        if (drawable instanceof BitmapDrawable) {
            //如果是 BitmapDrawable，直接获取 Bitmap 并缩放
            Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
            return Bitmap.createScaledBitmap(bitmap, targetWidth, targetHeight, true);
        }

        //其他类型（VectorDrawable、AdaptiveIconDrawable 等）需要绘制到 Bitmap
        Bitmap bitmap = Bitmap.createBitmap(
                targetWidth,
                targetHeight,
                Bitmap.Config.ARGB_8888
        );
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }

    //处理应用选择的方法
    private void onAppClicked(String package_name) {
        Intent result2RuleAddActivity = new Intent();
        result2RuleAddActivity.putExtra(KeyValueStrings.PACKAGE_NAME.getValue(), package_name);
        setResult(Activity.RESULT_OK, result2RuleAddActivity);
        finish();
    }
}