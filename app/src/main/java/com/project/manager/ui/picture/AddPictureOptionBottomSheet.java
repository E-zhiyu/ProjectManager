package com.project.manager.ui.picture;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.project.manager.databinding.BottomSheetPictureAddOptionBinding;

public class AddPictureOptionBottomSheet extends BottomSheetDialogFragment {
    private final Context context;                                //上下文
    private final ActivityResultLauncher<Intent> cameraLauncher;  //启动相机界面的启动器

    public AddPictureOptionBottomSheet(Context context, ActivityResultLauncher<Intent> cameraLauncher) {
        this.context = context;
        this.cameraLauncher = cameraLauncher;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        //绑定的XML视图
        BottomSheetPictureAddOptionBinding binding = BottomSheetPictureAddOptionBinding.inflate(inflater, container, false);

        binding.addViaCamera.setOnClickListener(v -> {
            Intent skip2CameraActivity = new Intent(context, CameraActivity.class);
            cameraLauncher.launch(skip2CameraActivity);
            dismiss();
        });

        binding.addViaAlbum.setOnClickListener(v -> Toast.makeText(context, "此功能正在开发", Toast.LENGTH_SHORT).show());

        return binding.getRoot();
    }
}
