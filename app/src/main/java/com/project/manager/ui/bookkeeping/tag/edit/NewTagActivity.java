package com.project.manager.ui.bookkeeping.tag.edit;

import android.content.Intent;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.project.manager.R;
import com.project.manager.ResultCode;
import com.project.manager.ui.bookkeeping.KeyValueStrings;
import com.project.manager.ui.bookkeeping.tag.Tag;

import java.util.ArrayList;

public class NewTagActivity extends AppCompatActivity implements View.OnFocusChangeListener, View.OnClickListener {
    TextInputLayout tag_name_layout, tag_group_layout;
    TextInputEditText tag_name_input, tag_group_input;
    int selected_index = -1; //选择的分组的索引

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_tag);

        tag_name_input = findViewById(R.id.tag_name_input);
        tag_group_input = findViewById(R.id.group_name_input);
        tag_name_layout = findViewById(R.id.tag_name_layout);
        tag_group_layout = findViewById(R.id.tag_group_layout);

        initViews();
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        if (!hasFocus) {
            if (v.getId() == R.id.tag_name_input) {
                String tag_name = String.valueOf(tag_name_input.getText());

                if (tag_name.isEmpty()) {
                    tag_name_layout.setErrorEnabled(true);
                    tag_name_layout.setError("标签名不能为空");
                } else {
                    tag_name_layout.setError(null);
                    tag_name_layout.setErrorEnabled(false);
                }
            } else if (v.getId() == R.id.group_name_input) {
                String tag_group_name = String.valueOf(tag_group_input.getText());

                if (tag_group_name.isEmpty()) {
                    tag_group_layout.setErrorEnabled(true);
                    tag_group_layout.setError("分组名不能为空");
                } else {
                    tag_group_layout.setError(null);
                    tag_group_layout.setErrorEnabled(false);
                }
            }
        } else {
            if (v.getId() == R.id.group_name_input) {
                tag_group_layout.setError(null);
                tag_group_layout.setErrorEnabled(false);
            } else if (v.getId() == R.id.tag_name_input) {
                tag_name_layout.setError(null);
                tag_name_layout.setErrorEnabled(false);
            }
        }
    }

    @Override
    public void onClick(@NonNull View v) {
        Intent result2TagEdit = new Intent();
        Bundle dataBundle = new Bundle();

        if (v.getId() == R.id.finish_btn) {
            String error = inputInfoVerify();

            //判断校验后是否有错误
            if (error != null) {
                Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
            } else {
                String tag_name = String.valueOf(tag_name_input.getText());
                dataBundle.putString(KeyValueStrings.TAG_NAME.getValue(), tag_name);         //标签名
                String group_name = String.valueOf(tag_group_input.getText());
                dataBundle.putString(KeyValueStrings.TAG_GROUP_NAME.getValue(), group_name); //分组名称

                result2TagEdit.putExtras(dataBundle);
                setResult(ResultCode.RESULT_OK.ordinal(), result2TagEdit);
                finish();
            }
        } else if (v.getId() == R.id.cancel_btn) {
            finish();
        }
    }

    //初始化视图
    private void initViews() {
        //设置标题栏的图标点击监听器
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        tag_group_layout.setEndIconOnClickListener((v -> onGroupLayoutEndIconClicked()));

        tag_name_input.setOnFocusChangeListener(this);
        tag_group_input.setOnFocusChangeListener(this);

        findViewById(R.id.finish_btn).setOnClickListener(this);
        findViewById(R.id.cancel_btn).setOnClickListener(this);
    }

    //输入内容合法性校验
    private String inputInfoVerify() {
        String tag_name = String.valueOf(tag_name_input.getText());

        String error = null;
        if (tag_name.isEmpty()) {
            error = "标签名不能为空";
        } else if (Tag.nameTransToTno(tag_name, this) != 0) {
            error = "已存在同名标签";
        }

        if (error != null) {
            tag_name_layout.setErrorEnabled(true);
            tag_name_layout.setError(error);
        }

        return error;
    }

    //标签分组右侧按钮点击回调
    private void onGroupLayoutEndIconClicked() {
        ArrayList<String> tagGroupArrayList = getIntent().getStringArrayListExtra(KeyValueStrings.TAG_GROUP_NAME_LIST.getValue());
        if (tagGroupArrayList != null && !tagGroupArrayList.isEmpty()) {
            String[] group_names = tagGroupArrayList.toArray(new String[0]);

            new MaterialAlertDialogBuilder(this)
                    .setTitle("选择标签分组")
                    .setSingleChoiceItems(group_names, selected_index, (dialog, witch) -> {
                        String group_name = group_names[witch];
                        tag_group_input.setText(group_name);
                        selected_index = witch;
                        dialog.dismiss();
                    })
                    .setNegativeButton("关闭", (dialog, id) -> dialog.dismiss())
                    .show();
        } else {
            new MaterialAlertDialogBuilder(this)
                    .setTitle("提示")
                    .setMessage("您还未创建任何标签分组")
                    .setPositiveButton("确认", ((dialog, id) -> dialog.dismiss()))
                    .show();
        }
    }
}