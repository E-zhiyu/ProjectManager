package com.project.manager.ui.pages.bookkeeping.tag.edit;

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
import com.project.manager.databinding.ActivityTagAddModifyBinding;
import com.project.manager.ui.RequestResultCode;
import com.project.manager.ui.pages.bookkeeping.KeyValueStrings;
import com.project.manager.ui.pages.bookkeeping.TagString;
import com.project.manager.data.data_class.Tag;
import com.project.manager.ui.pages.bookkeeping.tag.select_sheet.TagSelectBottomSheet;
import com.project.manager.ui.data_communication.tag_modify.TagUpdateReason;
import com.project.manager.ui.data_communication.tag_modify.TagRepository;

import java.util.ArrayList;

public class TagAddModifyActivity extends AppCompatActivity implements View.OnFocusChangeListener, View.OnClickListener {
    private TextInputLayout tag_name_layout, tag_group_layout;  //标签名称和标签分组输入框布局管理器
    private TextInputEditText tag_name_input, tag_group_input;  //标签名称和标签分组输入框
    private boolean isModifyMode = false;                       //是否为标签编辑模式
    private int selected_group_index = -1;                      //选择的分组的索引
    private long tag_no = 0, group_no = 0;                      //标签和标签分组编号
    private TagSelectBottomSheet tag_sheet;                     //标签选择底部弹窗
    private ActivityTagAddModifyBinding binding;                //绑定的XML视图的引用

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityTagAddModifyBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        tag_name_input = binding.tagNameInput;
        tag_group_input = binding.tagGroupInput;
        tag_name_layout = binding.tagNameLayout;
        tag_group_layout = binding.tagGroupLayout;

        initViews();
        receiveInitData();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
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
            }
        } else {
            if (v.getId() == R.id.tag_group_input) {
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
                String tag_name = String.valueOf(tag_name_input.getText());
                dataBundle.putString(KeyValueStrings.TAG_NAME.getValue(), tag_name);         //标签名
                String group_name = String.valueOf(tag_group_input.getText());
                dataBundle.putString(KeyValueStrings.TAG_GROUP_NAME.getValue(), group_name); //分组名称

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

    //初始化视图
    private void initViews() {
        //设置标题栏的图标点击监听器
        MaterialToolbar toolbar = binding.toolbar;
        toolbar.setNavigationOnClickListener(v -> finish());

        tag_group_layout.setEndIconOnClickListener((v -> onGroupLayoutEndIconClicked()));

        tag_name_input.setOnFocusChangeListener(this);

        binding.deleteBtn.setOnClickListener(this);
        binding.finishBtn.setOnClickListener(this);
        binding.cancelBtn.setOnClickListener(this);
        binding.mergeBtn.setOnClickListener(this);
        binding.mergeBtn.setOnClickListener(v -> new MaterialAlertDialogBuilder(this)
                .setTitle("合并标签")
                .setMessage("此操作会将本标签与其他标签合并，使用本标签标记的流水记录将自动替换为用合并后的标签标记，并且本标签将被永久删除，确认继续吗？")
                .setPositiveButton("确认", (dialog, which) -> {
                    tag_sheet = new TagSelectBottomSheet(this::onTagBtnClicked, tag_no);
                    tag_sheet.show(getSupportFragmentManager(), TagString.TAG_MERGE_SHEET.getValue());
                })
                .setNegativeButton("取消", (dialog, which) -> dialog.dismiss())
                .show()
        );
    }

    //接收初始化数据
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
                String[] group_names;   //标签分组名称数组
                if (tagGroupArrayList != null) {
                    group_names = tagGroupArrayList.toArray(new String[0]);

                    for (selected_group_index = 0; selected_group_index < group_names.length; selected_group_index++) {
                        if (group_name.equals(group_names[selected_group_index]))
                            break;
                    }
                }
            }
        }
    }

    //标签按钮点击处理方法
    private void onTagBtnClicked(long tag_no, String tag_name) {
        //通知流水输入界面更新名称
        TagRepository repository = TagRepository.getInstance();
        repository.updateTag(tag_name, this.tag_no, TagUpdateReason.MERGE);    //传递合并到的标签的名称和原来标签的编号

        tag_sheet.dismiss();

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

    //输入内容合法性校验
    private String inputInfoVerify() {
        String tag_name = String.valueOf(tag_name_input.getText());

        String error = null;
        if (tag_name.isEmpty()) {
            error = "标签名不能为空";
        } else if (Tag.nameTransToTno(tag_name, this) != 0 && !isModifyMode) {  //仅在添加模式检测同名
            error = "已存在同名标签";
        }

        //判断是否需要显示错误提示
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
                    .setSingleChoiceItems(group_names, selected_group_index, (dialog, witch) -> {
                        String group_name = group_names[witch];
                        tag_group_input.setText(group_name);
                        selected_group_index = witch;
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