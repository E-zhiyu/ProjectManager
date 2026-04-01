package com.manager.assistant;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.manager.assistant.data.save.preference.VersionPreference;
import com.manager.assistant.databinding.ActivityMainBinding;
import com.manager.assistant.helpers.appearence.ThemeModeHelper;
import com.manager.assistant.data.save.preference.AppSettingsPreference;
import com.manager.assistant.helpers.UpdateHelper;
import com.manager.assistant.ui.others.adapters.FragmentPagerAdapter;
import com.manager.assistant.ui.pages.bookkeeping.BookKeepingFragment;
import com.manager.assistant.ui.pages.home.HomeFragment;
import com.manager.assistant.ui.pages.setting.SettingFragment;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.rxjava3.disposables.CompositeDisposable;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;                                            //绑定的 XML 视图
    private final CompositeDisposable disposables = new CompositeDisposable();      //多线程任务列表

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0);
            return insets;
        });

        initThemeMode();
        initViews();

        //启动时更新检测
        int startVersionCheckNum = VersionPreference.getStartVersionCheckNum(this);
        int recycleNum = VersionPreference.VERSION_CHECK_RECYCLE_NUM;
        boolean isMandatoryUpdateFound = VersionPreference.getFindMandatoryUpdate(this);    //是否获取到强制更新
        if (!isMandatoryUpdateFound) {
            if (startVersionCheckNum % recycleNum == 0) {
                UpdateHelper.checkUpdate(this, disposables, false, false);
            }
            VersionPreference.setStartVersionCheckNum(this, (startVersionCheckNum + 1) % recycleNum);
        } else {
            UpdateHelper.showMandatoryUpdateDialog(this);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        disposables.dispose();
        binding = null;
    }

    private void initViews() {
        //替换自带工具栏
        setSupportActionBar(binding.toolbar);

        List<Fragment> fragmentList = new ArrayList<>();
        fragmentList.add(new BookKeepingFragment());
        fragmentList.add(new HomeFragment());
        fragmentList.add(new SettingFragment());
        ViewPager2 viewPager2 = getViewPager2(fragmentList, binding.bottomNavi);    //设置翻页器

        //设置APP启动第一屏
        int first_screen_code = AppSettingsPreference.getFirstScreen(this);
        viewPager2.setCurrentItem(first_screen_code, false);
        int[] titleIds = {
                R.string.title_bookkeeping,
                R.string.title_home
        };
        binding.toolbar.setTitle(titleIds[first_screen_code]);

        //设置底部导航栏点击监听
        binding.bottomNavi.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.navigation_bookkeeping) {
                viewPager2.setCurrentItem(0, true);
                binding.toolbar.setTitle(R.string.title_bookkeeping);
                return true;
            } else if (id == R.id.navigation_home) {
                viewPager2.setCurrentItem(1, true);
                binding.toolbar.setTitle(R.string.title_home);
                return true;
            } else if (id == R.id.navigation_settings) {
                viewPager2.setCurrentItem(2, true);
                binding.toolbar.setTitle(R.string.title_setting);
                return true;
            }
            return false;
        });

        //设置滚动监听器
        viewPager2.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                switch (position) {
                    case 0:
                        binding.toolbar.setTitle(R.string.title_bookkeeping);
                        break;
                    case 1:
                        binding.toolbar.setTitle(R.string.title_home);
                        break;
                    case 2:
                        binding.toolbar.setTitle(R.string.title_setting);
                        break;
                }
            }
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
        viewPager2.setOffscreenPageLimit(2);    //设置保留邻近Fragment

        return viewPager2;
    }

    //初始化主题模式
    private void initThemeMode() {
        int theme_mode = AppSettingsPreference.getThemeMode(this);
        ThemeModeHelper.applyTheme(theme_mode);
    }
}