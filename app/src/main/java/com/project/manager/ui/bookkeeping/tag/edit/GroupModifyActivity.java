package com.project.manager.ui.bookkeeping.tag.edit;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.project.manager.R;
import com.project.manager.ResultCode;
import com.project.manager.ui.bookkeeping.KeyValueStrings;

public class GroupModifyActivity extends AppCompatActivity implements View.OnFocusChangeListener, View.OnClickListener {
    long group_no;                  //分组编号
    String group_name;              //分组名称
    TextInputLayout group_layout;   //分组名称文本框Layout
    TextInputEditText group_input;  //分组名称输入文本框

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_modify);

        initViews();
    }

    @Override
    public void onClick(@NonNull View v) {
        Intent result2EditActivity = new Intent();
        Bundle dataBundle = new Bundle();
        dataBundle.putLong(KeyValueStrings.TAG_GROUP_NO.getValue(), group_no);  //先放入分组编号

        if (v.getId() == R.id.finish_btn) {
            String err = inputInfoVerify();

            if (err != null) {
                Toast.makeText(this, err, Toast.LENGTH_SHORT).show();
            } else {
                String new_group_name = String.valueOf(group_input.getText());
                dataBundle.putString(KeyValueStrings.TAG_GROUP_NAME.getValue(), new_group_name);

                result2EditActivity.putExtras(dataBundle);
                setResult(ResultCode.RESULT_OK.ordinal(), result2EditActivity);
                finish();
            }
        } else if (v.getId() == R.id.cancel_btn) {
            setResult(ResultCode.RESULT_REJECT.ordinal(), result2EditActivity);
            finish();
        } else if (v.getId() == R.id.delete_btn) {
            new MaterialAlertDialogBuilder(this)
                    .setTitle("删除分组")
                    .setMessage("此操作将清空分组内的所有标签并清除对应流水记录的标签，确认继续吗？")
                    .setPositiveButton("确定", ((dialog, which) -> {
                        result2EditActivity.putExtras(dataBundle);
                        setResult(ResultCode.RESULT_DELETE.ordinal(), result2EditActivity);
                        finish();
                    }))
                    .setNegativeButton("取消", (dialog, which) -> dialog.dismiss())
                    .show();
        }
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        if (!hasFocus) {
            if (v.getId() == R.id.tag_group_input) {
                String group_name = String.valueOf(group_input.getText());

                if (group_name.isEmpty()) {
                    group_layout.setErrorEnabled(true);
                    group_layout.setError("分组名称不能为空");
                } else {
                    group_layout.setError(null);
                    group_layout.setErrorEnabled(false);
                }
            }
        }
    }

    private void initViews() {
        group_input = findViewById(R.id.group_name_input);
        group_layout = findViewById(R.id.group_name_layout);

        group_input.setOnFocusChangeListener(this);

        //设置按钮点击监听器
        findViewById(R.id.finish_btn).setOnClickListener(this);
        findViewById(R.id.cancel_btn).setOnClickListener(this);
        findViewById(R.id.delete_btn).setOnClickListener(this);

        //加载传入的数据
        Bundle dataBundle = getIntent().getExtras();
        if (dataBundle != null) {
            group_no = dataBundle.getLong(KeyValueStrings.TAG_GROUP_NO.getValue());
            group_name = dataBundle.getString(KeyValueStrings.TAG_GROUP_NAME.getValue());
            group_input.setText(group_name);
        } else {
            Toast.makeText(this, "无法初始化分组信息", Toast.LENGTH_SHORT).show();
        }
    }

    //输入内容合法性校验
    private String inputInfoVerify() {
        String group_name = String.valueOf(group_input.getText());

        String error = null;
        if (group_name.isEmpty()) {
            error = "分组名不能为空";
            group_layout.setErrorEnabled(true);
            group_layout.setError(error);
        }

        return error;
    }
}