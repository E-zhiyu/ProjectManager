package com.manager.assistant.ui.others.bottom_sheets.picture;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.manager.assistant.databinding.BottomSheetPictureAddOptionBinding;
import com.manager.assistant.ui.others.bottom_sheets.BaseBottomSheetDialogFragment;

public class AddPictureOptionBottomSheet extends BaseBottomSheetDialogFragment {
    private final TakePictureListener takePictureListener;
    private final OpenAlbumListener openAlbumListener;

    public interface TakePictureListener {
        void takePicture();
    }

    public interface OpenAlbumListener {
        void openAlbum();
    }

    public AddPictureOptionBottomSheet(
            TakePictureListener takePictureListener,
            OpenAlbumListener openAlbumListener
    ) {
        this.takePictureListener = takePictureListener;
        this.openAlbumListener = openAlbumListener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        //绑定的XML视图
        BottomSheetPictureAddOptionBinding binding = BottomSheetPictureAddOptionBinding.inflate(inflater, container, false);

        binding.addViaCamera.setOnClickListener(v -> {
            takePictureListener.takePicture();
            dismiss();
        });

        binding.addViaAlbum.setOnClickListener(v -> {
            openAlbumListener.openAlbum();
            dismiss();
        });

        return binding.getRoot();
    }
}
