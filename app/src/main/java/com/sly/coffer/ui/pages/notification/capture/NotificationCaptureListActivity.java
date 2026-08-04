package com.sly.coffer.ui.pages.notification.capture;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.sly.coffer.databinding.ActivityNotificationCaptureListBinding;

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

        //TODO:Recycler
    }
}