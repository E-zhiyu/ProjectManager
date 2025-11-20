package com.project.manager.ui.bookkeeping.tag.edit;

import android.content.Intent;
import android.database.sqlite.SQLiteException;
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
import com.project.manager.exception.ExceptionHelper;
import com.project.manager.ui.bookkeeping.KeyValueStrings;
import com.project.manager.ui.bookkeeping.tag.TagGroup;
import com.project.manager.ui.setting.running_account_data.pojo.PojoTagGroup;

import java.util.List;

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
            setResult(ResultCode.RESULT_CANCEL.ordinal(), result2EditActivity);
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
        } else if (v.getId() == R.id.group_merge_btn) {
            new MaterialAlertDialogBuilder(this)
                    .setTitle("合并分组")
                    .setMessage("执行此操作会将本分组的标签全部移动至目标分组并删除本分组，确认继续吗？")
                    .setNegativeButton("取消", (dialog, which) -> dialog.dismiss())
                    .setPositiveButton("确认", (dialog, which) -> {
                        dialog.dismiss();

                        List<PojoTagGroup> groupList;
                        try {
                            groupList = TagGroup.loadPojoTagGroups(this);
                        } catch (SQLiteException e) {
                            ExceptionHelper.showExceptionDialog(this, e);
                            Toast.makeText(this, "无法加载分组列表", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        String[] group_names = new String[groupList.size() - 1];

                        int index = 0, same_group_index = -1;
                        for (PojoTagGroup group : groupList) {
                            if (group.getGroup_no() == group_no) {
                                same_group_index = index;
                            } else {
                                group_names[index] = group.getGroup_name();
                                index++;
                            }
                        }
                        if (same_group_index != -1) {
                            groupList.remove(same_group_index); //去除同号分组
                        }

                        new MaterialAlertDialogBuilder(this)
                                .setTitle("合并到哪个分组")
                                .setSingleChoiceItems(group_names, -1, (select_dialog, select_which) -> {
                                    long merged_no = groupList.get(select_which).getGroup_no(); //获取合并到的分组的编号
                                    dataBundle.putLong(KeyValueStrings.MERGE_TARGET_NO.getValue(), merged_no);
                                    result2EditActivity.putExtras(dataBundle);
                                    setResult(ResultCode.RESULT_MERGE.ordinal(), result2EditActivity);
                                    finish();

                                    select_dialog.dismiss();
                                })
                                .show();
                    })
                    .show();
        } else {
            NullPointerException e = new NullPointerException("无法获取正确的视图ID");
            ExceptionHelper.showExceptionDialog(this, e);
        }
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        if (!hasFocus) {
            if (v.getId() == R.id.group_name_input) {
                String group_name = String.valueOf(group_input.getText());

                if (group_name.isEmpty()) {
                    group_layout.setErrorEnabled(true);
                    group_layout.setError("分组名称不能为空");
                } else {
                    group_layout.setError(null);
                    group_layout.setErrorEnabled(false);
                }
            }
        } else {
            if (v.getId() == R.id.group_name_input) {
                group_layout.setError(null);
                group_layout.setErrorEnabled(false);
            }
        }
    }

    private void initViews() {
        //设置标题栏的图标点击监听器
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        group_input = findViewById(R.id.group_name_input);
        group_layout = findViewById(R.id.group_name_layout);

        group_input.setOnFocusChangeListener(this);

        //设置按钮点击监听器
        findViewById(R.id.finish_btn).setOnClickListener(this);
        findViewById(R.id.cancel_btn).setOnClickListener(this);
        findViewById(R.id.delete_btn).setOnClickListener(this);
        findViewById(R.id.group_merge_btn).setOnClickListener(this);

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
        } else if (TagGroup.nameTransToGno(group_name, this) != 0) {
            error = "已存在同名分组";
        }

        if (error != null) {
            group_layout.setErrorEnabled(true);
            group_layout.setError(error);
        }

        return error;
    }
}