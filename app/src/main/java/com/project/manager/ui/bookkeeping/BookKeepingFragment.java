package com.project.manager.ui.bookkeeping;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.project.manager.R;
import com.project.manager.databinding.FragmentBookkeepingBinding;
import com.project.manager.pages.newflow.NewFlowActivity;

public class BookKeepingFragment extends Fragment implements View.OnClickListener {

    private FragmentBookkeepingBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        HomeViewModel homeViewModel =
                new ViewModelProvider(this).get(HomeViewModel.class);

        binding = FragmentBookkeepingBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        //绑定单击按钮监听器
        root.findViewById(R.id.flow_btn).setOnClickListener(this);
        root.findViewById(R.id.report_btn).setOnClickListener(this);

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.flow_btn) {  //新建流水按钮
            startActivity(new Intent(binding.getRoot().getContext(), NewFlowActivity.class));
        } else if (v.getId() == R.id.report_btn) {  //查看报表按钮

        }
    }
}