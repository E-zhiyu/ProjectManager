package com.manager.assistant.ui.pages.main.setting.sub;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.manager.assistant.databinding.ActivityAboutBinding;
import com.manager.assistant.helpers.about.AboutHelper;
import com.manager.assistant.helpers.appearence.AppearanceHelper;

public class AboutActivity extends AppCompatActivity {
    private ActivityAboutBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAboutBinding.inflate(getLayoutInflater());

        EdgeToEdge.enable(this);
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0);
            binding.scrollView.setPadding(0, 0, 0, systemBars.bottom);
            return insets;
        });

        initViews();
    }

    private void initViews() {
        //工具栏
        binding.toolbar.setNavigationOnClickListener(view -> finish());

        //版本名称文本
        try {
            String versionName = "v" + AboutHelper.getVersionName(this);
            binding.versionNameText.setText(versionName);
        } catch (PackageManager.NameNotFoundException e) {
            binding.versionNameText.setVisibility(View.INVISIBLE);
        }

        //作者卡片
        binding.authorCard.setOnClickListener(view -> {
            Uri uri = Uri.parse("https://github.com/E-zhiyu");
            Intent skip2GitHub = new Intent(Intent.ACTION_VIEW, uri);
            startActivity(skip2GitHub);
        });
        AppearanceHelper.attachMorphAnimation(binding.authorCard);

        //项目地址卡片
        binding.projectAddressCard.setOnClickListener(view -> {
            Uri uri = Uri.parse("https://gitee.com/e-zhiyu/manager-assistant-web");
            Intent skip2Project = new Intent(Intent.ACTION_VIEW, uri);
            startActivity(skip2Project);
        });
        AppearanceHelper.attachMorphAnimation(binding.projectAddressCard);
    }
}