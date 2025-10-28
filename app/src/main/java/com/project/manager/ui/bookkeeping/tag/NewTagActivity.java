package com.project.manager.ui.bookkeeping.tag;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.project.manager.R;
import com.project.manager.RequestResultCode;

public class NewTagActivity extends AppCompatActivity implements View.OnFocusChangeListener, View.OnClickListener {
    TextInputLayout tag_name_layout, tag_group_layout;
    TextInputEditText tag_name_input, tag_group_input;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_tag);

        tag_name_input = findViewById(R.id.tag_name_input);
        tag_group_input = findViewById(R.id.tag_group_input);
        tag_name_layout = findViewById(R.id.tag_name_layout);
        tag_group_layout = findViewById(R.id.tag_group_layout);

        initViews();
    }

    //初始化视图
    private void initViews() {
        tag_group_layout.setEndIconOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onGroupLayoutEndIconClicked();
            }
        });

        tag_name_input.setOnFocusChangeListener(this);
        tag_group_input.setOnFocusChangeListener(this);

        findViewById(R.id.finish_btn).setOnClickListener(this);
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        if (v.getId() == R.id.tag_name_input && !hasFocus) {
            String tag_name = String.valueOf(tag_name_input.getText());

            if (tag_name.isEmpty()) {
                tag_name_layout.setErrorEnabled(true);
                tag_name_layout.setError("标签名不能为空");
            } else {
                tag_name_layout.setError(null);
                tag_name_layout.setErrorEnabled(false);
            }
        } else if (v.getId() == R.id.tag_group_input && !hasFocus) {
            String tag_group_name = String.valueOf(tag_group_input.getText());

            if (tag_group_name.isEmpty()) {
                tag_group_layout.setErrorEnabled(true);
                tag_group_layout.setError("分组名不能为空");
            } else {
                tag_group_layout.setError(null);
                tag_group_layout.setErrorEnabled(false);
            }
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.finish_btn) {
            onFinishBtnClicked();
        }
    }

    //输入内容合法性校验
    private String inputInfoVerify() {
        String tag_name = String.valueOf(tag_name_input.getText());
        String tag_group = String.valueOf(tag_group_input.getText());

        String error = null;
        if (tag_name.isEmpty()) {
            error = "标签名不能为空";
            tag_name_layout.setErrorEnabled(true);
            tag_name_layout.setError(error);
        } else if (tag_group.isEmpty()) {
            error = "分组名不能为空";
            tag_group_layout.setErrorEnabled(true);
            tag_group_layout.setError(error);
        }

        return error;
    }

    //标签分组右侧按钮点击回调
    private void onGroupLayoutEndIconClicked() {
        //TODO: 设计标签选择逻辑（用BottomSheet实现）
    }

    //完成按钮点击回调
    private void onFinishBtnClicked() {
        String error = inputInfoVerify();

        //判断校验后是否有错误
        if (error != null) {
            Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
        } else {
            Intent result2TagSelectBottomSheet = new Intent();
            Bundle dataBundle = new Bundle();

            String tag_name = String.valueOf(tag_name_input.getText());
            dataBundle.putString(TagAttributions.NAME.value, tag_name);
            String tag_group = String.valueOf(tag_group_input.getText());
            dataBundle.putString(TagAttributions.GROUP.value, tag_group);

            result2TagSelectBottomSheet.putExtras(dataBundle);
            setResult(RequestResultCode.RESULT_OK.ordinal(), result2TagSelectBottomSheet);
            finish();
        }
    }
}