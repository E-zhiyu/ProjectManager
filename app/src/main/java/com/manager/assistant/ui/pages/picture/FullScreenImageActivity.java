package com.manager.assistant.ui.pages.picture;

import android.net.Uri;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.manager.assistant.databinding.ActivityFullScreenImageBinding;
import com.manager.assistant.helpers.file.PictureFileHelper;
import com.manager.assistant.generic_enums.KeyStrings;

import java.io.File;
import java.util.Locale;
import java.util.Objects;

public class FullScreenImageActivity extends AppCompatActivity {
    private ActivityFullScreenImageBinding binding;             //绑定的XML视图
    private String[] pictureUriStrings;                 //图片Uri字符串数组

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //设置底部导航栏和状态栏的颜色并设置全屏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().setStatusBarColor(ContextCompat.getColor(this, android.R.color.black));
        getWindow().setNavigationBarColor(ContextCompat.getColor(this, android.R.color.black));

        binding = ActivityFullScreenImageBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //获取传递的图片URI
        pictureUriStrings = getIntent().getStringArrayExtra(KeyStrings.FILE_URI.v());

        initViews();
    }

    private void initViews() {
        FullScreenPictureAdapter adapter = new FullScreenPictureAdapter(pictureUriStrings);
        binding.viewPager2.setAdapter(adapter);

        int start_index = getIntent().getIntExtra(KeyStrings.VIEW_HOLDER_POSITION.v(), 0);
        binding.viewPager2.setCurrentItem(start_index, false);

        binding.savePictureBtn.setOnClickListener(v -> savePicture());
        binding.sharePictureBtn.setOnClickListener(v -> sharePicture());
    }

    /**
     * 保存图片到系统相册
     */
    private void savePicture() {
        int current_picture_index = binding.viewPager2.getCurrentItem();
        Uri currentUri = Uri.parse(pictureUriStrings[current_picture_index]);
        PictureFileHelper.saveToGallery(
                this,
                currentUri,
                new PictureFileHelper.OnSaveListener() {
                    @Override
                    public void onSaveSuccess(Uri savedUri, String fileName) {
                        Toast.makeText(FullScreenImageActivity.this, "图片已保存至系统相册", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onSaveFailed(String error) {
                        String info = String.format(Locale.getDefault(), "图片保存失败：%s", error);
                        Toast.makeText(FullScreenImageActivity.this, info, Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    /**
     * 分享图片
     */
    private void sharePicture() {
        int current_picture_index = binding.viewPager2.getCurrentItem();
        Uri currentUri = Uri.parse(pictureUriStrings[current_picture_index]);
        File pictureFile = new File(Objects.requireNonNull(currentUri.getPath()));
        PictureFileHelper.shareImage(this, pictureFile, new PictureFileHelper.OnShareListener() {
            @Override
            public void onShareSuccess() {
                Toast.makeText(FullScreenImageActivity.this, "正在分享图片……", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onShareFailed(String err) {
                String info = String.format(Locale.getDefault(), "分享失败：%s", err);
                Toast.makeText(FullScreenImageActivity.this, info, Toast.LENGTH_SHORT).show();
            }
        });
    }
}