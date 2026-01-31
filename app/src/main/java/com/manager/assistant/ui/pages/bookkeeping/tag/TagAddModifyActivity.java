package com.manager.assistant.ui.pages.bookkeeping.tag;

import android.content.Intent;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.chip.Chip;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.manager.assistant.R;
import com.manager.assistant.databinding.ActivityTagAddModifyBinding;
import com.manager.assistant.enums.RequestResultCode;
import com.manager.assistant.enums.KeyValueStrings;
import com.manager.assistant.enums.TagString;
import com.manager.assistant.data.data_class.Tag;
import com.manager.assistant.helpers.AnimationHelper;
import com.manager.assistant.ui.others.adapters.NoFilteringArrayAdapter;
import com.manager.assistant.ui.others.bottom_sheets.tag.TagSelectBottomSheet;
import com.manager.assistant.ui.data_communication.tag_modify.TagUpdateReason;
import com.manager.assistant.ui.data_communication.tag_modify.TagRepository;
import com.manager.assistant.ui.pages.bookkeeping.running_account.fragments.RunningAccountType;

import java.util.ArrayList;

public class TagAddModifyActivity extends AppCompatActivity implements View.OnClickListener {
    private boolean isModifyMode = false;                       //是否为标签编辑模式
    private long tag_no = 0, group_no = 0;                      //标签和标签分组编号
    private TagSelectBottomSheet tagSheet;                      //标签选择底部弹窗
    private ActivityTagAddModifyBinding binding;                //绑定的XML视图的引用
    private int scope = 0;                                      //标签作用域范围

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityTagAddModifyBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        receiveInitData();
        initViews();
        AnimationHelper.setupAllChildMorphAnimation(binding.getRoot());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }

    @Override
    public void onClick(@NonNull View v) {
        Intent result2TagManage = new Intent();
        Bundle dataBundle = new Bundle();
        dataBundle.putLong(KeyValueStrings.TAG_GROUP_NO.getValue(), group_no);      //分组编号
        dataBundle.putLong(KeyValueStrings.TAG_NO.getValue(), tag_no);              //标签编号

        if (v.getId() == R.id.finish_btn) {
            String error = inputInfoVerify();

            //判断校验后是否有错误
            if (error != null) {
                Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
            } else {
                String tag_name = String.valueOf(binding.tagNameInput.getText());
                dataBundle.putString(KeyValueStrings.TAG_NAME.getValue(), tag_name);            //标签名
                String group_name = String.valueOf(binding.tagGroupInput.getText());
                dataBundle.putString(KeyValueStrings.TAG_GROUP_NAME.getValue(), group_name);    //分组名称
                dataBundle.putInt(KeyValueStrings.TAG_SCOPE.getValue(), scope);                 //标签作用域

                if (isModifyMode) {
                    TagRepository repository = TagRepository.getInstance();
                    repository.updateTag(tag_name, tag_no, TagUpdateReason.RENAME);    //更新ViewModel中的标签数据
                }

                result2TagManage.putExtras(dataBundle);
                setResult(RequestResultCode.RESULT_OK.ordinal(), result2TagManage);
                finish();
            }
        } else if (v.getId() == R.id.cancel_btn) {
            finish();
        } else if (v.getId() == R.id.delete_btn) {
            new MaterialAlertDialogBuilder(this)
                    .setTitle("删除标签")
                    .setMessage("此操作将清空所有相应流水记录和通知解析规则的标签数据，确认继续吗？")
                    .setPositiveButton("确定", ((dialog, which) -> {
                        TagRepository repository = TagRepository.getInstance();
                        repository.updateTag("", tag_no, TagUpdateReason.DELETE);    //更新ViewModel中的标签数据

                        result2TagManage.putExtras(dataBundle);
                        setResult(RequestResultCode.RESULT_DELETE.ordinal(), result2TagManage);
                        finish();
                    }))
                    .setNegativeButton("取消", (dialog, which) -> dialog.dismiss())
                    .show();
        }
    }

    /**
     * 初始化视图
     */
    private void initViews() {
        //设置标题栏的图标点击监听器
        binding.toolbar.setNavigationOnClickListener(v -> finish());

        ArrayList<String> groupNameList = getIntent().getStringArrayListExtra(KeyValueStrings.TAG_GROUP_NAME_LIST.getValue());
        if (groupNameList != null) {
            NoFilteringArrayAdapter<String> adapter = new NoFilteringArrayAdapter<>(
                    this,
                    groupNameList
            );
            binding.tagGroupInput.setAdapter(adapter);

            binding.tagGroupInput.setOnItemClickListener((parent, view, position, id) -> {
                String groupName = groupNameList.get(position);
                binding.tagGroupInput.setText(groupName);
            });
        }

        binding.tagNameInput.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                String tagName = String.valueOf(binding.tagNameInput.getText());
                long tno = Tag.nameTransToTno(tagName, this);

                if (tagName.isEmpty()) {
                    binding.tagNameLayout.setErrorEnabled(true);
                    binding.tagNameLayout.setError("标签名不能为空");
                } else if (tno != 0 && tno != tag_no) {              //查询到数据库中存在同名编号并且编号不为自身时
                    binding.tagNameLayout.setErrorEnabled(true);
                    binding.tagNameLayout.setError("已存在同名标签");
                } else {
                    binding.tagNameLayout.setError(null);
                }
            } else {
                binding.tagNameLayout.setError(null);
            }
        });
        binding.tagNameInput.setOnClickListener(v -> binding.tagNameLayout.setError(null));

        binding.deleteBtn.setOnClickListener(this);
        binding.finishBtn.setOnClickListener(this);
        binding.cancelBtn.setOnClickListener(this);
        binding.mergeBtn.setOnClickListener(this);
        binding.mergeBtn.setOnClickListener(v -> new MaterialAlertDialogBuilder(this)
                .setTitle("合并标签")
                .setMessage("此操作会将本标签与其他标签合并，使用本标签标记的流水记录将自动替换为用合并后的标签标记，并且本标签将被永久删除，确认继续吗？")
                .setPositiveButton("确认", (dialog, which) -> {
                    tagSheet = new TagSelectBottomSheet(this::onTagBtnClicked, tag_no);
                    tagSheet.show(getSupportFragmentManager(), TagString.TAG_MERGE_SHEET.getValue());
                })
                .setNegativeButton("取消", (dialog, which) -> dialog.dismiss())
                .show()
        );

        //标签作用域选择
        for (RunningAccountType type : RunningAccountType.values()) {
            Chip scopeChip = new Chip(this);
            scopeChip.setCheckable(true);
            scopeChip.setCheckedIconVisible(true);
            scopeChip.setCheckedIcon(ContextCompat.getDrawable(this, R.drawable.baseline_check_24));
            scopeChip.setText(type.getTitle());

            //设置初始选择状态
            int iBinary = (int) Math.pow(2, type.ordinal());
            scopeChip.setChecked((scope & iBinary) == 0);

            scopeChip.setOnCheckedChangeListener((buttonView, isChecked) -> {
                int binary = (int) Math.pow(2, type.ordinal());
                if (!isChecked) {
                    scope |= binary;    //未被选择则将对应二进制位置为1，标记为对那种类型不可见
                } else {
                    scope &= ~binary;
                }
            });

            binding.scopeSelectChipGroup.addView(scopeChip);
        }
    }

    /**
     * 编辑模式下接收初始化数据
     */
    private void receiveInitData() {
        //加载传入的数据
        Bundle tagData = getIntent().getExtras();
        isModifyMode = getIntent().getBooleanExtra(KeyValueStrings.IS_MODIFY_MODE.getValue(), false);
        if (tagData != null && isModifyMode) {
            MaterialToolbar toolbar = binding.toolbar;
            toolbar.setTitle(R.string.title_modify_tag);

            //显示隐藏的组件
            binding.deleteBtn.setVisibility(View.VISIBLE);
            binding.mergeBtn.setVisibility(View.VISIBLE);

            tag_no = tagData.getLong(KeyValueStrings.TAG_NO.getValue());                        //该标签编号
            group_no = tagData.getLong(KeyValueStrings.TAG_GROUP_NO.getValue());                //所属分组编号
            scope = tagData.getInt(KeyValueStrings.TAG_SCOPE.getValue());                       //标签作用域
            String tag_name = tagData.getString(KeyValueStrings.TAG_NAME.getValue());           //该标签名称
            String group_name = tagData.getString(KeyValueStrings.TAG_GROUP_NAME.getValue());   //所属分组名称

            binding.tagNameInput.setText(tag_name);
            binding.tagGroupInput.setText(group_name);
        }
    }

    /**
     * 合并标签时标签按钮点击回调
     *
     * @param tag_no   目标标签编号
     * @param tag_name 目标标签名称
     */
    private void onTagBtnClicked(long tag_no, String tag_name) {
        //通知流水输入界面更新名称
        TagRepository repository = TagRepository.getInstance();
        repository.updateTag(tag_name, tag_no, TagUpdateReason.MERGE);    //传递合并到的标签的名称和原来标签的编号

        tagSheet.dismiss();

        //将数据传递给父界面
        Intent result2TagEdit = new Intent();
        Bundle dataBundle = new Bundle();
        dataBundle.putLong(KeyValueStrings.TAG_GROUP_NO.getValue(), this.group_no);     //被合并标签的分组编号
        dataBundle.putLong(KeyValueStrings.TAG_NO.getValue(), this.tag_no);             //被合并标签的编号
        dataBundle.putLong(KeyValueStrings.MERGE_TARGET_NO.getValue(), tag_no);
        result2TagEdit.putExtras(dataBundle);
        setResult(RequestResultCode.RESULT_MERGE.ordinal(), result2TagEdit);
        finish();
    }

    /**
     * 校验输入内容的合法性
     *
     * @return 错误提示（没有错误则为null）
     */
    private String inputInfoVerify() {
        String tag_name = String.valueOf(binding.tagNameInput.getText());
        long db_tno = Tag.nameTransToTno(tag_name, this);

        String error = null;
        if (tag_name.isEmpty()) {
            error = "标签名不能为空";
        } else if (db_tno != 0 && db_tno != tag_no) {  //查询到数据库中存在同名编号并且编号不为自身时
            error = "已存在同名标签";
        }

        //判断是否需要显示错误提示
        if (error != null) {
            binding.tagNameLayout.setErrorEnabled(true);
            binding.tagNameLayout.setError(error);
        }

        return error;
    }
}