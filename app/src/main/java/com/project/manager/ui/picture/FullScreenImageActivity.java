package com.project.manager.ui.picture;

import android.net.Uri;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.github.chrisbanes.photoview.PhotoView;
import com.project.manager.R;
import com.project.manager.databinding.ActivityFullScreenImageBinding;
import com.project.manager.ui.pages.bookkeeping.KeyValueStrings;

public class FullScreenImageActivity extends AppCompatActivity {
    private PhotoView photoView;
    private ScaleGestureDetector scaleGestureDetector;
    private GestureDetector gestureDetector;
    private float scaleFactor = 1.0f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //设置全屏和系统底部导航栏颜色
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().setNavigationBarColor(ContextCompat.getColor(this, R.color.black_alpha));

        ActivityFullScreenImageBinding binding = ActivityFullScreenImageBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        photoView = binding.photoView;

        //获取传递的图片URI
        String[] imageUriStrings = getIntent().getStringArrayExtra(KeyValueStrings.FILE_URI.getValue());
        int startPosition = getIntent().getIntExtra(KeyValueStrings.VIEW_HOLDER_POSITION.getValue(), 0);

        //加载图片
        if (imageUriStrings != null) {
            Uri imageUri = Uri.parse(imageUriStrings[startPosition]);
            loadImage(imageUri);
        } else {
            Toast.makeText(this, "图片加载失败", Toast.LENGTH_SHORT).show();
            finish();
        }

        //设置点击关闭
        photoView.setOnClickListener(v -> finish());

        //设置长按保存/分享
        photoView.setOnLongClickListener(v -> {
            showImageOptions();
            return true;
        });

        //初始化手势检测器
        initGestureDetectors();
    }

    /**
     * 加载图片
     *
     * @param imageUri 图片Uri
     */
    private void loadImage(Uri imageUri) {
        Glide.with(this)
                .load(imageUri)
                .placeholder(R.drawable.baseline_photo_24)
                .error(R.drawable.baseline_error_outline_24)
                .into(photoView);
    }

    /**
     * 初始化手势监听器
     */
    private void initGestureDetectors() {
        //缩放手势检测器
        scaleGestureDetector = new ScaleGestureDetector(this, new ScaleGestureDetector.OnScaleGestureListener() {
            @Override
            public boolean onScale(@NonNull ScaleGestureDetector detector) {
                scaleFactor *= detector.getScaleFactor();
                scaleFactor = Math.max(0.1f, Math.min(scaleFactor, 5.0f));
                photoView.setScaleX(scaleFactor);
                photoView.setScaleY(scaleFactor);
                return true;
            }

            @Override
            public boolean onScaleBegin(@NonNull ScaleGestureDetector detector) {
                return true;
            }

            @Override
            public void onScaleEnd(@NonNull ScaleGestureDetector detector) {
            }
        });

        //滑动手势检测器（左右滑动切换图片）
        gestureDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onFling(MotionEvent e1, @NonNull MotionEvent e2, float velocityX, float velocityY) {
                //TODO:左右滑动切换图片的逻辑可以在这里实现
                return super.onFling(e1, e2, velocityX, velocityY);
            }

            @Override
            public boolean onSingleTapConfirmed(@NonNull MotionEvent e) {
                finish();
                return true;
            }
        });
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        scaleGestureDetector.onTouchEvent(event);
        gestureDetector.onTouchEvent(event);
        return super.onTouchEvent(event);
    }

    /**
     * 显示图片操作
     */
    private void showImageOptions() {
        String[] options = {"保存图片", "分享图片", "取消"};

        //TODO:对话框替换为Material You风格
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setTitle("图片操作")
                .setItems(options, (dialog, which) -> {
                    switch (which) {
                        case 0: // 保存
                            saveImage();
                            break;
                        case 1: // 分享
                            shareImage();
                            break;
                    }
                })
                .show();
    }

    /**
     * 保存图片到系统相册
     */
    private void saveImage() {
        //TODO:保存图片到相册的逻辑
        Toast.makeText(this, "保存图片功能", Toast.LENGTH_SHORT).show();
    }

    /**
     * 分享图片
     */
    private void shareImage() {
        //TODO:分享图片的逻辑
        Toast.makeText(this, "分享图片功能", Toast.LENGTH_SHORT).show();
    }
}