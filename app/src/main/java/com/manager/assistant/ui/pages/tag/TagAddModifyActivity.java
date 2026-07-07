package com.manager.assistant.ui.pages.tag;

import android.content.Intent;

import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.chip.Chip;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.manager.assistant.R;
import com.manager.assistant.data.classes.TagGroup;
import com.manager.assistant.data.controllers.TagDataController;
import com.manager.assistant.data.controllers.TagGroupDataController;
import com.manager.assistant.databinding.ActivityTagAddModifyBinding;
import com.manager.assistant.generic_enums.RequestResultCode;
import com.manager.assistant.generic_enums.KeyStrings;
import com.manager.assistant.generic_enums.TagString;
import com.manager.assistant.helpers.ExceptionHelper;
import com.manager.assistant.ui.others.adapters.NoFilteringArrayAdapter;
import com.manager.assistant.ui.others.bottom_sheets.tag.TagSelectBottomSheet;
import com.manager.assistant.ui.sync.tag.TagUpdateReason;
import com.manager.assistant.ui.sync.tag.TagRepository;
import com.manager.assistant.auxiliary.enums.AccountType;

import java.util.List;
import java.util.stream.Collectors;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class TagAddModifyActivity extends AppCompatActivity {
    private boolean isModifyMode = false;                       //是否为标签编辑模式
    private long tagNo = 0, groupNo = 0;                        //标签和标签分组编号
    private TagSelectBottomSheet tagSheet;                      //标签选择底部弹窗
    private ActivityTagAddModifyBinding binding;                //绑定的XML视图的引用
    private int scope = 0;                                      //标签作用域范围
    private final CompositeDisposable disposables = new CompositeDisposable();                      //订阅列表（便于取消订阅）

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        binding = ActivityTagAddModifyBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            Insets systemBars = insets.getInsets(
                    WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.ime()
            );
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        receiveInitData();
        initViews();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        disposables.dispose();
        binding = null;
    }

    /**
     * 初始化视图
     */
    private void initViews() {
        //设置标题栏的图标点击监听器
        binding.toolbar.setNavigationOnClickListener(v -> finish());

        //标签分组输入
        initTagGroupInput();

        //标签名称输入框
        binding.tagNameInput.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                String tagName = String.valueOf(binding.tagNameInput.getText());
                long tno = TagDataController.nameTransToTno(tagName, this);

                if (tagName.isEmpty()) {
                    binding.tagNameLayout.setErrorEnabled(true);
                    binding.tagNameLayout.setError("标签名不能为空");
                } else if (tno != 0 && tno != tagNo) {              //查询到数据库中存在同名编号并且编号不为自身时
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

        //删除按钮
        binding.deleteBtn.setOnClickListener(v -> new MaterialAlertDialogBuilder(this)
                .setTitle("删除标签")
                .setMessage("此操作将清空所有相应流水记录和通知解析规则的标签数据，确认继续吗？")
                .setPositiveButton("确定", ((dialog, which) -> {
                    //将数据保存至数据库
                    try {
                        TagDataController.deleteTag(tagNo, this);
                        Toast.makeText(this, "标签删除成功", Toast.LENGTH_SHORT).show();
                    } catch (SQLiteException e) {
                        ExceptionHelper.showExceptionDialog(this, e);
                        return;
                    }

                    TagRepository repository = TagRepository.getInstance();
                    repository.updateTag("", tagNo, TagUpdateReason.DELETE);    //更新ViewModel中的标签数据

                    Intent result2TagManage = new Intent();
                    Bundle dataBundle = getInputData();
                    result2TagManage.putExtras(dataBundle);
                    setResult(RequestResultCode.RESULT_DELETE.ordinal(), result2TagManage);
                    finish();
                }))
                .setNegativeButton("取消", (dialog, which) -> dialog.dismiss())
                .show());

        //完成按钮
        binding.finishBtn.setOnClickListener(v -> {
            Intent result2TagManage = new Intent();
            Bundle dataBundle = getInputData();
            String error = inputInfoVerify();

            //判断校验后是否有错误
            if (error != null) {
                Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
                return;
            } else if (isModifyMode) {
                onTagModified(dataBundle);
            } else {
                onTagAdded(dataBundle);
            }

            //通过Intent返回数据
            result2TagManage.putExtras(dataBundle);
            setResult(RequestResultCode.RESULT_OK.ordinal(), result2TagManage);
            finish();
        });

        //取消按钮
        binding.cancelBtn.setOnClickListener(v -> finish());

        //合并按钮
        binding.mergeBtn.setOnClickListener(v -> new MaterialAlertDialogBuilder(this)
                .setTitle("合并标签")
                .setMessage("此操作会将本标签与其他标签合并，使用本标签标记的流水记录将自动替换为用合并后的标签标记，并且本标签将被永久删除，确认继续吗？")
                .setPositiveButton("确认", (dialog, which) -> {
                    tagSheet = new TagSelectBottomSheet(this::onTagMergeConfirmed, tagNo);
                    tagSheet.show(getSupportFragmentManager(), TagString.TAG_MERGE_SHEET.getTag());
                })
                .setNegativeButton("取消", (dialog, which) -> dialog.dismiss())
                .show()
        );

        //标签作用域选择
        for (AccountType type : AccountType.values()) {
            Chip scopeChip = new Chip(this);
            scopeChip.setCheckable(true);
            scopeChip.setCheckedIconVisible(true);
            scopeChip.setCheckedIcon(ContextCompat.getDrawable(this, R.drawable.outline_check_24));
            scopeChip.setText(type.getTitle());

            //设置初始选择状态
            int iBinary = (int) Math.pow(2, type.ordinal());
            scopeChip.setChecked((scope & iBinary) == 0);

            //设置切换监听器
            scopeChip.setOnCheckedChangeListener((buttonView, isChecked) -> {
                int binary = (int) Math.pow(2, type.ordinal());
                if (!isChecked) {
                    scope |= binary;    //未被选择则将对应二进制位置为1，标记为对那种类型不可见
                } else {
                    scope &= ~binary;
                }
            });

            //添加视图到Chip组中
            binding.scopeSelectChipGroup.addView(scopeChip);
        }
    }

    /**
     * 初始化标签分组输入框
     */
    private void initTagGroupInput() {
        disposables.add(
                Observable.fromCallable(() -> TagGroupDataController.getTagGroup(this))
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(tagGroupList -> {
                            //多线程获取所有分组
                            List<String> groupNameList = tagGroupList.stream()
                                    .map(TagGroup::getGroupName)
                                    .collect(Collectors.toList());

                            //设置适配器
                            NoFilteringArrayAdapter<String> adapter = new NoFilteringArrayAdapter<>(
                                    this,
                                    groupNameList
                            );
                            binding.tagGroupInput.setAdapter(adapter);
                        }, e -> Toast.makeText(this, "分组加载失败，分组名自动填充失效", Toast.LENGTH_SHORT).show())
        );
    }

    /**
     * 编辑模式下接收初始化数据
     */
    private void receiveInitData() {
        //加载传入的数据
        Bundle tagData = getIntent().getExtras();
        isModifyMode = getIntent().getBooleanExtra(KeyStrings.IS_MODIFY_MODE.v(), false);
        if (tagData != null && isModifyMode) {
            MaterialToolbar toolbar = binding.toolbar;
            toolbar.setTitle(R.string.title_modify_tag);

            //显示隐藏的组件
            binding.deleteBtn.setVisibility(View.VISIBLE);
            binding.mergeBtn.setVisibility(View.VISIBLE);

            tagNo = tagData.getLong(KeyStrings.TAG_NO.v());                         //该标签编号
            groupNo = tagData.getLong(KeyStrings.TAG_GROUP_NO.v());                 //所属分组编号
            scope = tagData.getInt(KeyStrings.TAG_SCOPE.v());                       //标签作用域
            String tagName = tagData.getString(KeyStrings.TAG_NAME.v());            //该标签名称
            String groupName = tagData.getString(KeyStrings.TAG_GROUP_NAME.v());    //所属分组名称

            binding.tagNameInput.setText(tagName);
            binding.tagGroupInput.setText(groupName);
        }
    }

    /**
     * 合并标签时标签按钮点击回调
     *
     * @param tagNo   目标标签编号
     * @param tagName 目标标签名称
     */
    private void onTagMergeConfirmed(long tagNo, String tagName) {
        //通知流水输入界面更新名称
        TagRepository repository = TagRepository.getInstance();
        repository.updateTag(tagName, tagNo, TagUpdateReason.MERGE);    //传递合并到的标签的名称和原来标签的编号

        //隐藏对话框
        tagSheet.dismiss();

        //修改数据库中的数据
        try {
            TagDataController.mergeTag(this.tagNo, tagNo, this);
            Toast.makeText(this, "标签合并成功", Toast.LENGTH_SHORT).show();
        } catch (SQLiteException e) {
            ExceptionHelper.showExceptionDialog(this, e);
            return;
        }

        //将数据传递给父界面
        Intent result2TagEdit = new Intent();
        Bundle dataBundle = getInputData(); //获取输入内容，包括了被合并的标签编号和分组编号
        dataBundle.putLong(KeyStrings.MERGE_TARGET_NO.v(), tagNo);
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
        String tagName = String.valueOf(binding.tagNameInput.getText());
        long dbTno = TagDataController.nameTransToTno(tagName, this);

        String error = null;
        if (tagName.isEmpty()) {
            error = "标签名不能为空";
        } else if (dbTno != 0 && dbTno != tagNo) {  //查询到数据库中存在同名编号并且编号不为自身时
            error = "已存在同名标签";
        }

        //判断是否需要显示错误提示
        if (error != null) {
            binding.tagNameLayout.setErrorEnabled(true);
            binding.tagNameLayout.setError(error);
        }

        return error;
    }

    /**
     * 获取输入的标签数据
     *
     * @return 需要传递到父界面的数据包
     */
    @NonNull
    private Bundle getInputData() {
        Bundle dataBundle = new Bundle();

        //放入数据
        String tagName = String.valueOf(binding.tagNameInput.getText());
        dataBundle.putString(KeyStrings.TAG_NAME.v(), tagName);         //标签名
        String groupName = String.valueOf(binding.tagGroupInput.getText());
        dataBundle.putString(KeyStrings.TAG_GROUP_NAME.v(), groupName); //分组名称
        dataBundle.putInt(KeyStrings.TAG_SCOPE.v(), scope);             //标签作用域
        dataBundle.putLong(KeyStrings.TAG_GROUP_NO.v(), groupNo);       //分组编号
        dataBundle.putLong(KeyStrings.TAG_NO.v(), tagNo);               //标签编号

        return dataBundle;
    }

    /**
     * 添加模式下完成按钮的点击回调
     *
     * @param dataBundle 包含标签数据的数据包
     */
    private void onTagAdded(Bundle dataBundle) {
        //处理分组
        try {
            //尝试获取分组编号
            String groupName = String.valueOf(binding.tagGroupInput.getText());
            long groupNo = this.groupNo;
            if (!groupName.isEmpty()) {
                groupNo = TagGroupDataController.nameTransToGno(groupName, this);
            }

            //如果是新的分组，则创建新分组
            if (groupNo == -1L) {
                groupNo = TagGroupDataController.saveNewGroup(groupName, this);
            }

            //保存至数据包中
            dataBundle.putLong(KeyStrings.TAG_GROUP_NO.v(), groupNo);
        } catch (SQLiteException e) {
            ExceptionHelper.showExceptionDialog(this, e);
            dataBundle.putLong(KeyStrings.TAG_GROUP_NO.v(), 0L);
            Toast.makeText(this, "分组出错，保存至默认分组", Toast.LENGTH_SHORT).show();
            return;
        }

        //保存数据
        try {
            long tagNo = TagDataController.saveNewTag(dataBundle, this);
            dataBundle.putLong(KeyStrings.TAG_NO.v(), tagNo);
            Toast.makeText(this, "标签添加成功", Toast.LENGTH_SHORT).show();
        } catch (SQLiteException e) {
            ExceptionHelper.showExceptionDialog(this, e);
            Toast.makeText(this, "标签添加失败", Toast.LENGTH_SHORT).show();
            return;
        }

        //触发LiveData以刷新其他界面
        TagRepository repository = TagRepository.getInstance();
        String tagName = String.valueOf(binding.tagNameInput.getText());
        repository.updateTag(tagName, tagNo, TagUpdateReason.ADD);
    }

    /**
     * 编辑模式下完成按钮的点击回调
     *
     * @param dataBundle 包含标签数据的数据包
     */
    private void onTagModified(Bundle dataBundle) {
        //处理分组
        try {
            //尝试获取分组编号
            String groupName = String.valueOf(binding.tagGroupInput.getText());
            long groupNo = this.groupNo;
            if (!groupName.isEmpty()) {
                groupNo = TagGroupDataController.nameTransToGno(groupName, this);
            }

            //如果是新的分组，则创建新分组
            if (groupNo == -1L) {
                groupNo = TagGroupDataController.saveNewGroup(groupName, this);
            }

            //保存至数据包中
            dataBundle.putLong(KeyStrings.TAG_GROUP_NO_NEW.v(), groupNo);
        } catch (SQLiteException e) {
            ExceptionHelper.showExceptionDialog(this, e);
            dataBundle.putLong(KeyStrings.TAG_GROUP_NO_NEW.v(), 0L);
            Toast.makeText(this, "分组出错，保存至默认分组", Toast.LENGTH_SHORT).show();
            return;
        }

        //保存数据
        try {
            TagDataController.modifyTag(dataBundle, this);
            Toast.makeText(this, "标签修改成功", Toast.LENGTH_SHORT).show();
        } catch (SQLiteException e) {
            ExceptionHelper.showExceptionDialog(this, e);
            Toast.makeText(this, "标签修改失败", Toast.LENGTH_SHORT).show();
            return;
        }

        //触发LiveData以刷新其他界面
        TagRepository repository = TagRepository.getInstance();
        String tagName = String.valueOf(binding.tagNameInput.getText());
        repository.updateTag(tagName, tagNo, TagUpdateReason.RENAME);
    }
}