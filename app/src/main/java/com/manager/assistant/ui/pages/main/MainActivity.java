package com.manager.assistant.ui.pages.main;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.search.SearchView;
import com.manager.assistant.R;
import com.manager.assistant.data.save.preference.VersionPreference;
import com.manager.assistant.databinding.ActivityMainBinding;
import com.manager.assistant.data.save.preference.AppSettingsPreference;
import com.manager.assistant.helpers.UpdateHelper;
import com.manager.assistant.ui.others.adapters.FragmentPagerAdapter;
import com.manager.assistant.ui.pages.main.bookkeeping.BookKeepingFragment;
import com.manager.assistant.ui.pages.main.home.HomeFragment;
import com.manager.assistant.ui.pages.main.settings.SettingsFragment;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.rxjava3.disposables.CompositeDisposable;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;                                            //绑定的 XML 视图
    private final CompositeDisposable disposable = new CompositeDisposable();      //多线程任务列表

    public SearchView getSearchView() {
        return binding.searchView;
    }

    public RecyclerView getSearchHistoryView() {
        return binding.searchHistoryRecycler;
    }

    public MaterialButton getClearHistoryBtn() {
        return binding.clearHistoryBtn;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, 0, systemBars.right, 0);
            return insets;
        });

        initViews();

        //启动时更新检测
        int startVersionCheckNum = VersionPreference.getStartVersionCheckNum(this);
        final int MODULAR = 2;
        boolean isMandatoryUpdateFound = VersionPreference.getFindMandatoryUpdate(this);    //是否获取到强制更新
        if (!isMandatoryUpdateFound) {
            if (System.currentTimeMillis() % MODULAR == 0) {
                UpdateHelper.checkUpdate(this, disposable, false, false);
            }
            VersionPreference.setStartVersionCheckNum(this, (startVersionCheckNum + 1) % MODULAR);
        } else {
            UpdateHelper.showMandatoryUpdateDialog(this);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        disposable.dispose();
        binding = null;
    }

    /**
     * 初始化视图
     */
    private void initViews() {
        //翻页视图
        List<Fragment> fragmentList = new ArrayList<>();
        fragmentList.add(new BookKeepingFragment());
        fragmentList.add(new HomeFragment());
        fragmentList.add(new SettingsFragment());
        ViewPager2 viewPager2 = getViewPager2(fragmentList, binding.bottomNavi);

        //设置APP启动第一屏
        int firstScreenCode = AppSettingsPreference.getFirstScreen(this);
        viewPager2.setCurrentItem(firstScreenCode, false);

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

    /**
     * 获取翻页视图
     *
     * @param fragmentList   页面Fragment列表
     * @param navigationView 底部导航栏视图
     * @return 带有列表中所有页面且与底部导航栏绑定的翻页视图
     */
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
}