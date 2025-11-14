package com.project.manager;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.project.manager.databinding.ActivityMainBinding;
import com.project.manager.ui.setting.theme_mode.ThemeModeHelper;
import com.project.manager.ui.setting.theme_mode.ThemePreference;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initThemeMode();

        ActivityMainBinding binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //替换自带工具栏
        Toolbar tl_head = findViewById(R.id.toolbar);
        setSupportActionBar(tl_head);

        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_bookkeeping, R.id.navigation_mine)
                .build();

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);  //设置标题随导航栏变化
        NavigationUI.setupWithNavController(binding.bottomNavi, navController);  //设置导航切换控制器
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        Fragment bookkeepingFragment = getSupportFragmentManager().findFragmentById(R.id.bookkeeping_fragment);
        if (bookkeepingFragment != null) {
            bookkeepingFragment.onActivityResult(requestCode, resultCode, intent);
        }
    }

    //初始化主题
    private void initThemeMode() {
        int theme_mode = ThemePreference.getThemeMode(this);
        ThemeModeHelper.applyTheme(theme_mode);
    }
}