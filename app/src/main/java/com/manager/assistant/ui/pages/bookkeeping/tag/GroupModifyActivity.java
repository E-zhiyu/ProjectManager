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
import com.manager.assistant.data.classes.Tag;
import com.manager.assistant.data.classes.TagGroup;
import com.manager.assistant.data.controllers.TagDataController;
import com.manager.assistant.data.controllers.TagGroupDataController;
import com.manager.assistant.databinding.ActivityGroupModifyBinding;
import com.manager.assistant.generic_enums.RequestResultCode;
import com.manager.assistant.helpers.appearence.AppearanceAnimationHelper;
import com.manager.assistant.helpers.ExceptionHelper;
import com.manager.assistant.generic_enums.KeyValueStrings;
import com.manager.assistant.ui.sync.tag.TagRepository;
import com.manager.assistant.ui.sync.tag.TagUpdateReason;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class GroupModifyActivity extends AppCompatActivity {
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

        //删除按钮
        binding.deleteBtn.setOnClickListener(v -> new MaterialAlertDialogBuilder(this)
                .setTitle("删除分组")
                .setMessage("此操作将清空分组内的所有标签并清除对应流水记录的标签，确认继续吗？")
                .setPositiveButton("确定", ((dialog, which) -> {
                    try {
                        //获取需要被删除的标签
                        List<Tag> tagsToBeDeleted = TagDataController.getTags(this, groupNo);

                        //删除分组数据
                        TagGroupDataController.deleteGroup(groupNo, this);    //删除分组
                        Toast.makeText(this, "标签分组已删除", Toast.LENGTH_SHORT).show();

                        //通知带有标签的输入界面更新UI
                        TagRepository repository = TagRepository.getInstance();
                        repository.updateTag(tagsToBeDeleted, TagUpdateReason.DELETE);
                    } catch (SQLiteException e) {
                        ExceptionHelper.showExceptionDialog(this, e);
                        return;
                    }

                    Intent result2EditActivity = new Intent();
                    Bundle dataBundle = getInputData();
                    result2EditActivity.putExtras(dataBundle);
                    setResult(RequestResultCode.RESULT_DELETE.ordinal(), result2EditActivity);
                    finish();
                }))
                .setNegativeButton("取消", (dialog, which) -> dialog.dismiss())
                .show());

        //合并按钮
        binding.mergeBtn.setOnClickListener(v -> new MaterialAlertDialogBuilder(this)
                .setTitle("合并分组")
                .setMessage("执行此操作会将本分组的标签全部移动至目标分组并删除本分组，确认继续吗？")
                .setNegativeButton("取消", (dialog, which) -> dialog.dismiss())
                .setPositiveButton("确认", (dialog, which) -> {
                    //获取所有分组
                    List<TagGroup> groupList;
                    try {
                        groupList = TagGroupDataController.getTagGroup(this, -1);
                    } catch (SQLiteException e) {
                        ExceptionHelper.showExceptionDialog(this, e);
                        Toast.makeText(this, "无法加载分组列表", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    //转换为分组名称数组
                    String[] groupNames = groupList.stream()
                            .filter(tagGroup -> tagGroup.getGroupNo() != groupNo)
                            .map(TagGroup::getGroupName)
                            .toArray(String[]::new);

                    //显示单选对话框
                    AtomicInteger selectedIndex = new AtomicInteger(-1);
                    new MaterialAlertDialogBuilder(this)
                            .setTitle("合并到哪个分组")
                            .setSingleChoiceItems(groupNames, -1, (d, w) -> selectedIndex.set(w))
                            .setNegativeButton("取消", null)
                            .setPositiveButton("确定", (dialog1, which1) -> {
                                //检测是否选择
                                if (selectedIndex.get() == -1) {
                                    Toast.makeText(this, "请选择需要合并到的分组", Toast.LENGTH_SHORT).show();
                                    return;
                                }

                                //返回数据
                                Bundle dataBundle = getInputData();
                                Intent result2EditActivity = new Intent();
                                long targetGroupNo = groupList.get(selectedIndex.get()).getGroupNo(); //获取合并到的分组的编号
                                dataBundle.putLong(KeyValueStrings.MERGE_TARGET_NO.getValue(), targetGroupNo);
                                result2EditActivity.putExtras(dataBundle);
                                setResult(RequestResultCode.RESULT_MERGE.ordinal(), result2EditActivity);
                                finish();
                            })
                            .show();
                })
                .show());

        //完成按钮
        binding.finishBtn.setOnClickListener(v -> {
            String err = inputInfoVerify();
            if (err != null) {
                Toast.makeText(this, err, Toast.LENGTH_SHORT).show();
            } else {
                Intent result2EditActivity = new Intent();
                Bundle dataBundle = getInputData();

                try {
                    String newGroupName = String.valueOf(binding.tagGroupInput.getText());
                    TagGroupDataController.modifyGroupName(groupNo, newGroupName, this);
                    Toast.makeText(this, "标签分组修改成功", Toast.LENGTH_SHORT).show();
                } catch (SQLiteException e) {
                    ExceptionHelper.showExceptionDialog(this, e);
                    return;
                }

                result2EditActivity.putExtras(dataBundle);
                setResult(RequestResultCode.RESULT_OK.ordinal(), result2EditActivity);
                finish();
            }
        });

        //取消按钮
        binding.cancelBtn.setOnClickListener(v -> finish());

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

    /**
     * 获取输入的数据
     *
     * @return 包含标签分组数据的数据包
     */
    @NonNull
    private Bundle getInputData() {
        Bundle dataBundle = new Bundle();

        String newGroupName = String.valueOf(binding.tagGroupInput.getText());
        dataBundle.putString(KeyValueStrings.TAG_GROUP_NAME.getValue(), newGroupName);  //分组名称
        dataBundle.putLong(KeyValueStrings.TAG_GROUP_NO.getValue(), groupNo);           //分组编号

        return dataBundle;
    }
}