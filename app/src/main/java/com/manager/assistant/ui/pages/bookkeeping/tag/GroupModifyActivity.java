package com.manager.assistant.ui.pages.bookkeeping.tag;

import android.content.Intent;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.manager.assistant.R;
import com.manager.assistant.data.controllers.TagGroupDataController;
import com.manager.assistant.databinding.ActivityGroupModifyBinding;
import com.manager.assistant.generic_enums.RequestResultCode;
import com.manager.assistant.helpers.appearence.AppearanceAnimationHelper;
import com.manager.assistant.helpers.ExceptionHelper;
import com.manager.assistant.generic_enums.KeyValueStrings;
import com.manager.assistant.data.io.pojos.PojoTagGroup;

import java.util.List;

public class GroupModifyActivity extends AppCompatActivity implements View.OnClickListener {
    private long groupNo;                       //分组编号
    private ActivityGroupModifyBinding binding; //绑定的XML视图引用

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        binding = ActivityGroupModifyBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            Insets systemBars = insets.getInsets(
                    WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.ime()
            );
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initViews();
        AppearanceAnimationHelper.setupAllChildMorphAnimation(binding.getRoot());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }

    @Override
    public void onClick(@NonNull View v) {
        Intent result2EditActivity = new Intent();
        Bundle dataBundle = new Bundle();
        dataBundle.putLong(KeyValueStrings.TAG_GROUP_NO.getValue(), groupNo);  //先放入分组编号

        if (v.getId() == R.id.finish_btn) {
            String err = inputInfoVerify();

            if (err != null) {
                Toast.makeText(this, err, Toast.LENGTH_SHORT).show();
            } else {
                String new_group_name = String.valueOf(binding.tagGroupInput.getText());
                dataBundle.putString(KeyValueStrings.TAG_GROUP_NAME.getValue(), new_group_name);

                result2EditActivity.putExtras(dataBundle);
                setResult(RequestResultCode.RESULT_OK.ordinal(), result2EditActivity);
                finish();
            }
        } else if (v.getId() == R.id.cancel_btn) {
            setResult(RequestResultCode.RESULT_CANCEL.ordinal(), result2EditActivity);
            finish();
        } else if (v.getId() == R.id.delete_btn) {
            new MaterialAlertDialogBuilder(this)
                    .setTitle("删除分组")
                    .setMessage("此操作将清空分组内的所有标签并清除对应流水记录的标签，确认继续吗？")
                    .setPositiveButton("确定", ((dialog, which) -> {
                        result2EditActivity.putExtras(dataBundle);
                        setResult(RequestResultCode.RESULT_DELETE.ordinal(), result2EditActivity);
                        finish();
                    }))
                    .setNegativeButton("取消", (dialog, which) -> dialog.dismiss())
                    .show();
        } else if (v.getId() == R.id.merge_btn) {
            new MaterialAlertDialogBuilder(this)
                    .setTitle("合并分组")
                    .setMessage("执行此操作会将本分组的标签全部移动至目标分组并删除本分组，确认继续吗？")
                    .setNegativeButton("取消", (dialog, which) -> dialog.dismiss())
                    .setPositiveButton("确认", (dialog, which) -> {
                        dialog.dismiss();

                        List<PojoTagGroup> groupList;
                        try {
                            groupList = TagGroupDataController.loadPojoTagGroups(this);
                        } catch (SQLiteException e) {
                            ExceptionHelper.showExceptionDialog(this, e);
                            Toast.makeText(this, "无法加载分组列表", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        String[] group_names = new String[groupList.size() - 1];

                        int index = 0, same_group_index = -1;
                        for (PojoTagGroup group : groupList) {
                            if (group.getGroup_no() == groupNo) {
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
                                    setResult(RequestResultCode.RESULT_MERGE.ordinal(), result2EditActivity);
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

    /**
     * 初始化视图
     */
    private void initViews() {
        //设置标题栏的图标点击监听器
        binding.toolbar.setNavigationOnClickListener(v -> finish());

        binding.tagGroupInput.setOnFocusChangeListener(((v, hasFocus) -> {
            if (!hasFocus) {
                String groupName = String.valueOf(binding.tagGroupInput.getText());
                long groupNo = TagGroupDataController.nameTransToGno(groupName, this);

                if (groupName.isEmpty()) {
                    binding.groupNameLayout.setErrorEnabled(true);
                    binding.groupNameLayout.setError("分组名称不能为空");
                } else if (groupNo != -1 && groupNo != this.groupNo) {
                    binding.groupNameLayout.setErrorEnabled(true);
                    binding.groupNameLayout.setError("已存在同名分组");
                }
            } else {
                binding.groupNameLayout.setError(null);
            }
        }));
        binding.tagGroupInput.setOnClickListener(v -> binding.groupNameLayout.setError(null));

        //设置按钮点击监听器
        binding.deleteBtn.setOnClickListener(this);
        binding.mergeBtn.setOnClickListener(this);
        binding.finishBtn.setOnClickListener(this);
        binding.cancelBtn.setOnClickListener(this);

        //加载传入的数据
        Bundle dataBundle = getIntent().getExtras();
        if (dataBundle != null) {
            groupNo = dataBundle.getLong(KeyValueStrings.TAG_GROUP_NO.getValue());
            String group_name = dataBundle.getString(KeyValueStrings.TAG_GROUP_NAME.getValue());
            binding.tagGroupInput.setText(group_name);
        } else {
            Toast.makeText(this, "无法初始化分组信息", Toast.LENGTH_SHORT).show();
        }

        //当修改默认分组时隐藏部分组件
        if (groupNo == 0) {
            binding.deleteBtn.setVisibility(View.GONE);
            binding.mergeBtn.setVisibility(View.GONE);
        }
    }

    /**
     * 校验输入内容的合法性
     *
     * @return 错误提示（没有错误则为null）
     */
    private String inputInfoVerify() {
        String groupName = String.valueOf(binding.tagGroupInput.getText());

        String error = null;
        if (groupName.isEmpty()) {
            error = "分组名不能为空";
            binding.groupNameLayout.setErrorEnabled(true);
            binding.groupNameLayout.setError(error);
        } else if (TagGroupDataController.nameTransToGno(groupName, this) != -1) {
            error = "已存在同名分组";
            binding.groupNameLayout.setErrorEnabled(true);
            binding.groupNameLayout.setError(error);
        }

        return error;
    }
}