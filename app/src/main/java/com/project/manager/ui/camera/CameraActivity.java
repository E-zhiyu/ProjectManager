package com.project.manager.ui.camera;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.common.util.concurrent.ListenableFuture;
import com.project.manager.LogTags;
import com.project.manager.R;
import com.project.manager.databinding.ActivityCameraBinding;
import com.project.manager.helpers.ExceptionHelper;
import com.project.manager.helpers.IconHelper;
import com.project.manager.helpers.PermissionHelper;
import com.project.manager.ui.pages.bookkeeping.KeyValueStrings;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CameraActivity extends AppCompatActivity {
    private ActivityCameraBinding binding;  //绑定的XML视图
    private ExecutorService cameraExecutor; //相机执行器
    private CameraSelector cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA;    //相机选择器（默认主摄）
    private ImageCapture imageCapture;      //图像捕捉器
    private final String[] permissions = {  //权限
            Manifest.permission.CAMERA
    };
    private final ActivityResultLauncher<String[]> requestPermissionLauncher =  //权限申请启动器
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(),
                    this::onPermissionResult);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        binding = ActivityCameraBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(binding.rootLayout, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());

            //设置提示文本框的间距
            ViewGroup.MarginLayoutParams tipParams = (ViewGroup.MarginLayoutParams) binding.tipText.getLayoutParams();
            tipParams.setMargins(0, systemBars.top, 0, 0);

            //设置拍照按钮的间距
            ViewGroup.MarginLayoutParams btnParams = (ViewGroup.MarginLayoutParams) binding.captureBtn.getLayoutParams();
            btnParams.setMargins(
                    IconHelper.dpToPx(this, 15),
                    0,
                    IconHelper.dpToPx(this, 15),
                    IconHelper.dpToPx(this, 10) + systemBars.bottom
            );
            return insets;
        });

        //将底部导航栏设置为黑色
        getWindow().setNavigationBarColor(ContextCompat.getColor(this, R.color.black_alpha));

        initViews();

        if (isAllPermissionGranted()) {
            startCamera();
        } else {
            requestPermissionLauncher.launch(permissions);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cameraExecutor.shutdown();  //界面销毁后关闭相机
    }

    /**
     * 初始化视图
     */
    private void initViews() {
        //设置透明状态栏
        Window window = getWindow();
        window.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS,
                WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

        cameraExecutor = Executors.newSingleThreadExecutor();

        //拍照点击监听
        binding.captureBtn.setOnClickListener(v -> takePhoto());
    }

    /**
     * 判断所有权限是否都已授予
     *
     * @return 权限是否授予
     */
    private boolean isAllPermissionGranted() {
        for (String onePermission : permissions) {
            if (!PermissionHelper.isPermissionsGranted(this, onePermission)) {
                return false;
            }
        }
        return true;
    }

    /**
     * 启动相机
     */
    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture =
                ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                bindCameraUseCases(cameraProvider);
            } catch (ExecutionException | InterruptedException e) {
                ExceptionHelper.showExceptionDialog(this, e);
            }
        }, ContextCompat.getMainExecutor(this));
    }

    /**
     * 处理权限授予情况的方法
     *
     * @param permissions K:权限名称,V:权限是否被授予
     */
    private void onPermissionResult(@NonNull Map<String, Boolean> permissions) {
        boolean allGranted = true;
        for (Map.Entry<String, Boolean> entry : permissions.entrySet()) {
            if (!entry.getValue()) {
                allGranted = false;
                break;
            }
        }

        if (allGranted) {
            startCamera();
        } else {
            Toast.makeText(this, "需要相机权限才能使用此功能", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void bindCameraUseCases(ProcessCameraProvider cameraProvider) {
        // 预览用例
        Preview preview = new Preview.Builder().build();
        preview.setSurfaceProvider(binding.cameraPreview.getSurfaceProvider());

        // 图像捕获用例
        imageCapture = new ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                .build();

        // 选择相机
        cameraSelector = cameraSelector == CameraSelector.DEFAULT_BACK_CAMERA
                ? CameraSelector.DEFAULT_FRONT_CAMERA
                : CameraSelector.DEFAULT_BACK_CAMERA;

        try {
            // 解绑所有用例
            cameraProvider.unbindAll();

            // 绑定用例到生命周期
            cameraProvider.bindToLifecycle(
                    this,
                    cameraSelector,
                    preview,
                    imageCapture
            );
        } catch (Exception e) {
            ExceptionHelper.showExceptionDialog(this, e);
        }
    }

    /**
     * 拍照方法
     */
    private void takePhoto() {
        if (imageCapture == null) {
            Log.e(LogTags.CAMERA_ACTIVITY.getV(), "imageCapture未初始化");
            return;
        }

        //创建照片文件
        File tempDir = getOutputDirectory();
        if (tempDir == null) {
            Log.e(LogTags.CAMERA_ACTIVITY.getV(), "无法获取临时照片保存目录");
            Toast.makeText(this, "拍照失败：无法获取照片保存目录", Toast.LENGTH_SHORT).show();
            return;
        }
        File photoFile = new File(
                tempDir,
                new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS", Locale.getDefault())
                        .format(System.currentTimeMillis()) + ".jpg"
        );

        //创建输出选项
        ImageCapture.OutputFileOptions outputOptions =
                new ImageCapture.OutputFileOptions.Builder(photoFile).build();

        //拍照
        imageCapture.takePicture(
                outputOptions,
                ContextCompat.getMainExecutor(this),
                new ImageCapture.OnImageSavedCallback() {
                    @Override
                    public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                        Uri savedUri = Uri.fromFile(photoFile);

                        //将照片Uri返回至父界面
                        Intent result2AccountFragment = new Intent();
                        result2AccountFragment.putExtra(KeyValueStrings.FILE_URI.getValue(), savedUri.toString());
                        setResult(Activity.RESULT_OK, result2AccountFragment);
                        finish();
                    }

                    @Override
                    public void onError(@NonNull ImageCaptureException exception) {
                        ExceptionHelper.showExceptionDialog(CameraActivity.this, exception);
                    }
                }
        );
    }

    /**
     * 获取照片输出目录
     *
     * @return 临时照片保存目录
     */
    @Nullable
    private File getOutputDirectory() {
        File dir = new File(getExternalFilesDir(null), "picture_temp");
        if (!dir.exists()) {
            if (dir.mkdirs()) {
                return dir;
            }
        } else {
            return dir;
        }

        return null;
    }
}