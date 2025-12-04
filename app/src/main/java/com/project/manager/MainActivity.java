package com.project.manager;

import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.appbar.MaterialToolbar;
import com.project.manager.data_save.preference.KeepAlivePreference;
import com.project.manager.databinding.ActivityMainBinding;
import com.project.manager.helpers.ThemeModeHelper;
import com.project.manager.data_save.preference.ThemeModePreference;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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
    }

    private void initViews() {
        ActivityMainBinding binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //替换自带工具栏
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_bookkeeping, R.id.navigation_mine)
                .build();

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);  //设置标题随导航栏变化
        NavigationUI.setupWithNavController(binding.bottomNavi, navController);  //设置导航切换控制器
    }

    //初始化主题模式
    private void initThemeMode() {
        int theme_mode = ThemeModePreference.getThemeMode(this);
        ThemeModeHelper.applyTheme(theme_mode);
    }
}