package com.sly.coffer.ui.pages.notification.capture;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.sly.coffer.data.save.preference.AutoBookKeepingPreference;
import com.sly.coffer.databinding.ActivityNotificationCaptureListBinding;
import com.sly.coffer.helpers.PermissionHelper;
import com.sly.coffer.helpers.appearence.AppearanceHelper;
import com.sly.coffer.helpers.appearence.VisibilityHelper;

import io.reactivex.rxjava3.disposables.CompositeDisposable;

public class NotificationCaptureListActivity extends AppCompatActivity {
    private ActivityNotificationCaptureListBinding binding;
    private final CompositeDisposable disposable = new CompositeDisposable();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityNotificationCaptureListBinding.inflate(getLayoutInflater());

        EdgeToEdge.enable(this);
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, 0, systemBars.right, 0);

            //RecyclerView
            binding.recycler.setPadding(systemBars.left, 0, systemBars.right, systemBars.bottom);

            return insets;
        });

        initViews();
    }

    @Override
    protected void onStart() {
        super.onStart();

        //功能未启用的提示文本
        VisibilityHelper.toggleVisibilityWithFade(
                binding.notEnabledTipCard,
                !AutoBookKeepingPreference.getNotificationCapture(this)
        );
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
        //TODO:搜索框

        //捕获功能未启用提示卡片
        AppearanceHelper.setMarginToNavigation(binding.notEnabledTipCard, this);
        binding.notEnabledTipCard.setOnClickListener(view -> {
            if (PermissionHelper.SpecialPermissionType.NOTIFICATION_LISTENER.isGranted(this)) {
                AutoBookKeepingPreference.setNotificationCapture(this, true);
                Toast.makeText(this, "5分钟后自动关闭通知捕获以节省性能", Toast.LENGTH_SHORT).show();
                VisibilityHelper.toggleVisibilityWithFade(binding.notEnabledTipCard, false);
            } else {
                String message = "通知捕获依赖通知监听服务，请授权后再启用通知捕获功能。";
                new MaterialAlertDialogBuilder(this)
                        .setTitle("权限申请说明")
                        .setMessage(message)
                        .setPositiveButton("去授权",(dialogInterface, i) -> {
                            Intent intent = PermissionHelper.SpecialPermissionType.NOTIFICATION_LISTENER.getIntent(this);
                            startActivity(intent);
                        })
                        .setNegativeButton("取消",null)
                        .show();
            }
        });

        //TODO:Recycler
    }
}