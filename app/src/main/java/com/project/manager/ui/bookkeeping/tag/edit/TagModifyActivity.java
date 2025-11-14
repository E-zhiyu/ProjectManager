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
import com.project.manager.ManagerAssistant;
import com.project.manager.R;
import com.project.manager.ResultCode;
import com.project.manager.ui.bookkeeping.KeyValueStrings;
import com.project.manager.ui.view_model.AccountTagViewModel;

import java.util.ArrayList;

public class TagModifyActivity extends AppCompatActivity implements View.OnFocusChangeListener, View.OnClickListener {
    private TextInputLayout tag_name_layout, tag_group_layout;  //标签名称和分组的文本框布局器
    private TextInputEditText tag_name_input, tag_group_input;  //标签名称和分组的文本输入框
    private AccountTagViewModel tagViewModel;                   //标签数据更新用的ViewModel
    int selected_index = -1;                                    //选择的分组的索引
    long tag_no, group_no;                                      //标签和标签分组编号
    private String[] group_names;                               //标签分组名称数组

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tag_modify);

        tag_name_input = findViewById(R.id.tag_name_input);
        tag_group_input = findViewById(R.id.tag_group_input);
        tag_name_layout = findViewById(R.id.tag_name_layout);
        tag_group_layout = findViewById(R.id.tag_group_layout);

        //获取Application中的ViewModel
        ManagerAssistant app = (ManagerAssistant) getApplication();
        tagViewModel = app.getAccountTagViewModel();

        initViews();
    }

    @Override
    public void onClick(@NonNull View v) {
        Intent result2TagEdit = new Intent();
        Bundle dataBundle = new Bundle();
        dataBundle.putLong(KeyValueStrings.TAG_GROUP_NO.getValue(), group_no);      //分组编号
        dataBundle.putLong(KeyValueStrings.TAG_NO.getValue(), tag_no);              //标签编号

        if (v.getId() == R.id.finish_btn) {
            String error = inputInfoVerify();

            //判断校验后是否有错误
            if (error != null) {
                Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
            } else {
                String tag_name = String.valueOf(tag_name_input.getText());
                dataBundle.putString(KeyValueStrings.TAG_NAME.getValue(), tag_name);            //标签名
                String group_name = String.valueOf(tag_group_input.getText());
                dataBundle.putString(KeyValueStrings.TAG_GROUP_NAME.getValue(), group_name);    //分组名称

                tagViewModel.updateTag(tag_name, tag_no);    //更新ViewModel中的标签数据

                result2TagEdit.putExtras(dataBundle);
                setResult(ResultCode.RESULT_OK.ordinal(), result2TagEdit);
                finish();
            }
        } else if (v.getId() == R.id.delete_btn) {
            new MaterialAlertDialogBuilder(this)
                    .setTitle("删除标签")
                    .setMessage("此操作将清空所有相应流水的标签数据，确认继续吗？")
                    .setPositiveButton("确定", ((dialog, which) -> {
                        tagViewModel.updateTag("", tag_no);    //更新ViewModel中的标签数据

                        result2TagEdit.putExtras(dataBundle);
                        setResult(ResultCode.RESULT_DELETE.ordinal(), result2TagEdit);
                        finish();
                    }))
                    .setNegativeButton("取消", (dialog, which) -> dialog.dismiss())
                    .show();
        } else if (v.getId() == R.id.cancel_btn) {
            setResult(ResultCode.RESULT_REJECT.ordinal(), result2TagEdit);
            finish();
        }
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
            } else if (v.getId() == R.id.tag_group_input) {
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
            if (v.getId() == R.id.tag_group_input) {
                //标签分组输入框获取焦点时清空错误提示以显示右侧按钮
                tag_group_layout.setError(null);
                tag_group_layout.setErrorEnabled(false);
            }
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
        findViewById(R.id.delete_btn).setOnClickListener(this);
        findViewById(R.id.cancel_btn).setOnClickListener(this);

        //加载传入的数据
        Bundle tagData = getIntent().getExtras();
        if (tagData != null) {
            tag_no = tagData.getLong(KeyValueStrings.TAG_NO.getValue());                        //该标签编号
            group_no = tagData.getLong(KeyValueStrings.TAG_GROUP_NO.getValue());                //所属分组编号
            String tag_name = tagData.getString(KeyValueStrings.TAG_NAME.getValue());           //该标签名称
            String group_name = tagData.getString(KeyValueStrings.TAG_GROUP_NAME.getValue());   //所属分组名称

            tag_name_input.setText(tag_name);
            tag_group_input.setText(group_name);

            //判断是否正确获取分组名称
            if (group_name == null) {
                Toast.makeText(this, "无法初始化分组名列表选中的下标：无效的分组名称", Toast.LENGTH_SHORT).show();
            } else {
                //初始化分组名列表选择下标
                ArrayList<String> tagGroupArrayList = getIntent().getStringArrayListExtra(KeyValueStrings.TAG_GROUP_NAME_LIST.getValue());
                if (tagGroupArrayList != null) {
                    group_names = tagGroupArrayList.toArray(new String[0]);

                    for (selected_index = 0; selected_index < group_names.length; selected_index++) {
                        if (group_name.equals(group_names[selected_index]))
                            break;
                    }
                } else {
                    group_names = new String[0];
                }
            }
        } else {
            Toast.makeText(this, "无法初始化标签信息", Toast.LENGTH_SHORT).show();
        }
    }

    //标签分组右侧按钮点击回调
    private void onGroupLayoutEndIconClicked() {
        if (group_names.length != 0) {
            new MaterialAlertDialogBuilder(this)
                    .setTitle("选择标签分组")
                    .setSingleChoiceItems(group_names, selected_index, (dialog, witch) -> {
                        String group_name = group_names[witch];
                        tag_group_input.setText(group_name);
                        selected_index = witch;
                        dialog.dismiss();
                    })
                    .setNegativeButton("关闭", (dialog, id) -> {
                        // 关闭对话框
                        dialog.dismiss();
                    })
                    .show();
        } else {
            new MaterialAlertDialogBuilder(this)
                    .setTitle("提示")
                    .setMessage("您还未创建任何标签分组")
                    .setPositiveButton("确认", ((dialog, id) -> dialog.dismiss()))
                    .show();
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
}