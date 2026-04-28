package com.manager.assistant.ui.pages.main;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.inputmethod.EditorInfo;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.search.SearchView;
import com.manager.assistant.R;
import com.manager.assistant.data.save.preference.SearchHistoryPreference;
import com.manager.assistant.data.save.preference.VersionPreference;
import com.manager.assistant.databinding.ActivityMainBinding;
import com.manager.assistant.generic_enums.LogTags;
import com.manager.assistant.data.save.preference.AppSettingsPreference;
import com.manager.assistant.helpers.UpdateHelper;
import com.manager.assistant.ui.others.adapters.FragmentPagerAdapter;
import com.manager.assistant.ui.others.adapters.SearchHistoryAdapter;
import com.manager.assistant.ui.pages.main.bookkeeping.BookKeepingFragment;
import com.manager.assistant.ui.pages.main.home.HomeFragment;
import com.manager.assistant.ui.pages.main.setting.SettingFragment;
import com.manager.assistant.ui.sync.AccountSearchViewModel;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.rxjava3.disposables.CompositeDisposable;

public class MainActivity extends AppCompatActivity {
    public ActivityMainBinding binding;                                             //绑定的 XML 视图（设为public方便子元素访问）
    private final CompositeDisposable disposables = new CompositeDisposable();      //多线程任务列表

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(binding.viewPager2, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0);
            return insets;
        });

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

    /**
     * 初始化视图
     */
    private void initViews() {
        //翻页视图
        List<Fragment> fragmentList = new ArrayList<>();
        fragmentList.add(new BookKeepingFragment());
        fragmentList.add(new HomeFragment());
        fragmentList.add(new SettingFragment());
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

        //SearchView
        SearchHistoryAdapter searchViewAdapter = new SearchHistoryAdapter(keyWord -> {
            //更新搜索词
            AccountSearchViewModel viewModel = new ViewModelProvider(this).get(AccountSearchViewModel.class);
            viewModel.updateSearchText(keyWord);

            //隐藏SearchView
            binding.searchView.hide();

            //保存搜索关键词
            if (!keyWord.isEmpty()) {
                List<String> searchHistoryList = SearchHistoryPreference.getHistory(
                        SearchHistoryPreference.KEY_ACCOUNT_REMARK,
                        this
                );
                searchHistoryList.remove(keyWord);       //移除已存在的项
                searchHistoryList.add(0, keyWord);   //添加新项
                if (searchHistoryList.size() > 15) {        //限制15条记录
                    searchHistoryList = searchHistoryList.subList(0, 14);
                }
                SearchHistoryPreference.setHistory(
                        SearchHistoryPreference.KEY_ACCOUNT_REMARK,
                        searchHistoryList,
                        this
                );
            }
        });
        binding.searchHistoryRecycler.setAdapter(searchViewAdapter);

        //先刷新一下内容，以应对SearchView展开时的界面重建
        List<String> historyList = SearchHistoryPreference.getHistory(
                SearchHistoryPreference.KEY_ACCOUNT_REMARK,
                this
        );
        searchViewAdapter.refreshSearchHistory(historyList);

        //设置显示监听，用于初始化常用词与清空提示词按钮
        binding.searchView.addTransitionListener((searchView, previousState, newState) -> {
            //显示时执行的动作
            if (newState == SearchView.TransitionState.SHOWING) {
                List<String> searchHistoryList = SearchHistoryPreference.getHistory(
                        SearchHistoryPreference.KEY_ACCOUNT_REMARK,
                        this
                );
                searchViewAdapter.refreshSearchHistory(searchHistoryList);
            }
        });

        //设置清除搜索历史按钮点击监听
        binding.clearHistoryBtn.setOnClickListener(v -> {
            SearchHistoryPreference.setHistory(
                    SearchHistoryPreference.KEY_ACCOUNT_REMARK,
                    new ArrayList<>(),
                    this
            );
            searchViewAdapter.refreshSearchHistory(new ArrayList<>());
        });

        //添加文本变化监听器，用于在清空文本后自动清除搜索并刷新视图
        binding.searchView.getEditText().addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {

            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() == 0) {
                    Log.d(LogTags.MAIN_ACTIVITY.getV(), "搜索结果变为空，请求刷新界面");
                    AccountSearchViewModel viewModel = new ViewModelProvider(MainActivity.this).get(AccountSearchViewModel.class);
                    viewModel.updateSearchText("");
                }
            }
        });

        //设置搜索监听
        binding.searchView.getEditText().setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                //发送搜索请求
                String searchText = binding.searchView.getText().toString();
                AccountSearchViewModel viewModel = new ViewModelProvider(this).get(AccountSearchViewModel.class);
                viewModel.updateSearchText(searchText);

                //保存搜索关键词
                if (!searchText.isEmpty()) {
                    List<String> searchHistoryList = SearchHistoryPreference.getHistory(
                            SearchHistoryPreference.KEY_ACCOUNT_REMARK,
                            this
                    );
                    searchHistoryList.remove(searchText);       //移除已存在的项
                    searchHistoryList.add(0, searchText);   //添加新项
                    if (searchHistoryList.size() > 15) {        //限制15条记录
                        searchHistoryList = searchHistoryList.subList(0, 14);
                    }
                    SearchHistoryPreference.setHistory(
                            SearchHistoryPreference.KEY_ACCOUNT_REMARK,
                            searchHistoryList,
                            this
                    );
                }

                //隐藏SearchView
                binding.searchView.hide();

                return true;
            } else {
                return false;
            }
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