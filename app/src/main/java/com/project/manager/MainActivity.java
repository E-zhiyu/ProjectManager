package com.project.manager;

import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.project.manager.data.data_save.preference.KeepAlivePreference;
import com.project.manager.data.data_save.preference.VersionPreference;
import com.project.manager.databinding.ActivityMainBinding;
import com.project.manager.helpers.ThemeModeHelper;
import com.project.manager.data.data_save.preference.AppSettingsPreference;
import com.project.manager.helpers.UpdateHelper;
import com.project.manager.ui.bookkeeping.BookKeepingFragment;
import com.project.manager.ui.home.HomeFragment;
import com.project.manager.ui.setting.SettingFragment;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initThemeMode();
        initViews();

        //拦截返回行为
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (KeepAlivePreference.getHideRecents(getBaseContext())) {
                    finishAndRemoveTask();
                } else {
                    finish();
                }
            }
        });

        //启动时更新检测
        int start_version_check_num = VersionPreference.getStartVersionCheckNum(this);
        int recycle_num = VersionPreference.VERSION_CHECK_RECYCLE_NUM;
        boolean isMandatoryUpdateFound = VersionPreference.getFindMandatoryUpdate(this);    //是否获取到强制更新
        if (!isMandatoryUpdateFound) {
            if (start_version_check_num % recycle_num == 0) {
                UpdateHelper.checkUpdate(this, false);
            }
            VersionPreference.setStartVersionCheckNum(this, (start_version_check_num + 1) % recycle_num);
        } else {
            UpdateHelper.showMandatoryUpdateDialog(this);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }

    private void initViews() {
        //替换自带工具栏
        MaterialToolbar toolbar = binding.toolbar;
        setSupportActionBar(toolbar);

        List<Fragment> fragmentList = new ArrayList<>();
        fragmentList.add(new BookKeepingFragment());
        fragmentList.add(new HomeFragment());
        fragmentList.add(new SettingFragment());
        ViewPager2 viewPager2 = getViewPager2(fragmentList, binding.bottomNavi);    //设置翻页器

        //设置APP启动第一屏
        int first_screen_code = AppSettingsPreference.getFirstScreen(this);
        viewPager2.setCurrentItem(first_screen_code, false);

        //设置底部导航栏点击监听
        binding.bottomNavi.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.navigation_bookkeeping) {
                viewPager2.setCurrentItem(0, true);
                return true;
            } else if (id == R.id.navigation_home) {
                viewPager2.setCurrentItem(1, true);
                return true;
            } else if (id == R.id.navigation_settings) {
                viewPager2.setCurrentItem(2, true);
                return true;
            }
            return false;
        });
    }

    @NonNull
    private ViewPager2 getViewPager2(List<Fragment> fragmentList, BottomNavigationView navigationView) {
        ViewPager2 viewPager2 = binding.viewPager2;
        FragmentPagerAdapter viewPagerAdapter = new FragmentPagerAdapter(this, fragmentList);
        viewPager2.setAdapter(viewPagerAdapter);

        //ViewPager 页面切换监听
        viewPager2.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                // 更新底部导航栏选中状态
                navigationView.getMenu().getItem(position).setChecked(true);
            }
        });
        viewPager2.setOffscreenPageLimit(1);    //设置保留邻近Fragment

        return viewPager2;
    }

    //初始化主题模式
    private void initThemeMode() {
        int theme_mode = AppSettingsPreference.getThemeMode(this);
        ThemeModeHelper.applyTheme(theme_mode);
    }
}