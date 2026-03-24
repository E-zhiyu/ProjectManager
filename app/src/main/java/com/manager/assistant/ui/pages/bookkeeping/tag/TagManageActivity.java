package com.manager.assistant.ui.pages.bookkeeping.tag;

import android.app.Activity;
import android.content.Intent;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.manager.assistant.data.controllers.TagDataController;
import com.manager.assistant.data.controllers.TagGroupDataController;
import com.manager.assistant.databinding.ActivityTagManageBinding;
import com.manager.assistant.helpers.appearence.AnimationHelper;
import com.manager.assistant.helpers.resourse.ColorHelper;
import com.manager.assistant.generic_enums.RequestResultCode;
import com.manager.assistant.helpers.ExceptionHelper;
import com.manager.assistant.generic_enums.KeyValueStrings;
import com.manager.assistant.data.classes.Tag;
import com.manager.assistant.data.classes.TagGroup;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class TagManageActivity extends AppCompatActivity implements TagManageRecyclerAdapter.OnTextViewClickedListener {
    private TagManageRecyclerAdapter adapter;
    private final CompositeDisposable disposables = new CompositeDisposable();                      //订阅列表（便于取消订阅）
    private ActivityResultLauncher<Intent> tagAddLauncher, tagModifyLauncher, modifyGroupLauncher;  //活动启动器
    private ActivityTagManageBinding binding;   //绑定的XML视图的引用

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityTagManageBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initLaunchers();
        initViews();
        AnimationHelper.setupAllChildMorphAnimation(binding.getRoot());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;

        // 防止内存泄漏
        disposables.dispose();
    }

    @Override
    public void onTagTextViewClicked(long tag_no, String tag_name, int tag_scope, long group_no, String group_name) {
        Intent skip2ModifyTag = new Intent(this, TagAddModifyActivity.class);
        Bundle clickedTagData = new Bundle();

        clickedTagData.putString(KeyValueStrings.TAG_NAME.getValue(), tag_name);
        clickedTagData.putString(KeyValueStrings.TAG_GROUP_NAME.getValue(), group_name);
        clickedTagData.putLong(KeyValueStrings.TAG_NO.getValue(), tag_no);
        clickedTagData.putLong(KeyValueStrings.TAG_GROUP_NO.getValue(), group_no);
        clickedTagData.putInt(KeyValueStrings.TAG_SCOPE.getValue(), tag_scope);

        //获取已保存的标签分组信息并传递到子界面
        ArrayList<String> groupNames = adapter.getTagGroupList().stream()
                .map(TagGroup::getGroup_name)
                .collect(Collectors.toCollection(ArrayList::new));
        clickedTagData.putStringArrayList(KeyValueStrings.TAG_GROUP_NAME_LIST.getValue(), groupNames);

        skip2ModifyTag.putExtras(clickedTagData);
        skip2ModifyTag.putExtra(KeyValueStrings.IS_MODIFY_MODE.getValue(), true);
        tagModifyLauncher.launch(skip2ModifyTag);
    }

    @Override
    public void onGroupTextViewClicked(long group_no, String group_name) {
        Intent skip2GroupModify = new Intent(this, GroupModifyActivity.class);
        Bundle clickedGroupData = new Bundle();

        clickedGroupData.putString(KeyValueStrings.TAG_GROUP_NAME.getValue(), group_name);
        clickedGroupData.putLong(KeyValueStrings.TAG_GROUP_NO.getValue(), group_no);

        skip2GroupModify.putExtras(clickedGroupData);
        modifyGroupLauncher.launch(skip2GroupModify);
    }

    //初始化视图
    private void initViews() {
        //设置标题栏的图标点击监听器
        binding.toolbar.setNavigationOnClickListener(v -> finish());

        //设置按钮点击监听
        binding.addFloatingBtn.setOnClickListener(v -> {
            Intent skip2TagAdd = new Intent(this, TagAddModifyActivity.class);

            //获取已保存的标签分组信息
            List<TagGroup> tagGroupList = adapter.getTagGroupList();
            ArrayList<String> groupNameList = new ArrayList<>();
            for (TagGroup group : tagGroupList) {
                groupNameList.add(group.getGroup_name());
            }
            skip2TagAdd.putStringArrayListExtra(KeyValueStrings.TAG_GROUP_NAME_LIST.getValue(), groupNameList);

            skip2TagAdd.putExtra(KeyValueStrings.IS_MODIFY_MODE.getValue(), false);

            tagAddLauncher.launch(skip2TagAdd);
        });

        //获取颜色资源并设置下拉刷新布局的颜色
        int colorPrimary = ColorHelper.getPrimaryColor(this);
        int colorSecondary = ColorHelper.getSecondaryPrimaryColor(this);
        binding.refreshLayout.setColorSchemeColors(colorPrimary, colorSecondary);
        int colorBackground = ColorHelper.getBackgroundColor(this);
        binding.refreshLayout.setProgressBackgroundColorSchemeColor(colorBackground);

        //设置刷新监听器
        binding.refreshLayout.setOnRefreshListener(() -> disposables.add(
                Observable.fromCallable(() -> TagGroupDataController.loadTagGroups(this))
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(tagGroupList -> adapter.refreshUI(tagGroupList),
                                e -> {
                                    ExceptionHelper.showExceptionDialog(this, e);
                                    Toast.makeText(this, "刷新失败", Toast.LENGTH_SHORT).show();
                                },
                                () -> {
                                    binding.refreshLayout.setRefreshing(false);
                                    binding.addFloatingBtn.show();
                                }
                        )
        ));

        //初始化RecyclerView
        List<TagGroup> tagGroupList;    //获取标签分组数据
        try {
            tagGroupList = TagGroupDataController.loadTagGroups(this);
        } catch (SQLiteException e) {
            ExceptionHelper.showExceptionDialog(this, e);
            Toast.makeText(this, "标签数据读取失败", Toast.LENGTH_SHORT).show();
            tagGroupList = new ArrayList<>();
        }
        adapter = new TagManageRecyclerAdapter(tagGroupList, this);
        binding.tagGroupRecycler.setAdapter(adapter);

        //添加RecyclerView滚动监听器，用以控制添加按钮的显示与否
        AnimationHelper.setupFloatingBtnBehaviour(binding.tagGroupRecycler, binding.addFloatingBtn);
    }

    //初始化活动启动器
    private void initLaunchers() {
        tagAddLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    int resultCode = result.getResultCode();
                    Intent data = result.getData();

                    if (data == null && resultCode != Activity.RESULT_CANCELED) {
                        NullPointerException e = new NullPointerException("无法获取新建标签数据");
                        ExceptionHelper.showExceptionDialog(this, e);
                    } else {
                        onNewTagActivityResulted(resultCode, data);
                    }
                }
        );

        tagModifyLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    int resultCode = result.getResultCode();
                    Intent data = result.getData();

                    if (data == null && resultCode != Activity.RESULT_CANCELED) {
                        NullPointerException e = new NullPointerException("无法获取修改后的标签数据");
                        ExceptionHelper.showExceptionDialog(this, e);
                    } else {
                        onTagModifyActivityResulted(resultCode, data);
                    }
                }
        );

        modifyGroupLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    int resultCode = result.getResultCode();
                    Intent data = result.getData();

                    if (data == null && resultCode != Activity.RESULT_CANCELED) {
                        NullPointerException e = new NullPointerException("无法获取修改后的分组数据");
                        ExceptionHelper.showExceptionDialog(this, e);
                    } else {
                        modifyGroup(resultCode, data);
                    }
                }
        );
    }

    private void onNewTagActivityResulted(int resultCode, Intent resultIntent) {
        if (resultCode == RequestResultCode.RESULT_OK.ordinal()) {
            Bundle dataBundle = resultIntent.getExtras();
            if (dataBundle == null) {
                return;
            }

            String tagName = dataBundle.getString(KeyValueStrings.TAG_NAME.getValue());
            String groupName = dataBundle.getString(KeyValueStrings.TAG_GROUP_NAME.getValue());
            int tag_scope = dataBundle.getInt(KeyValueStrings.TAG_SCOPE.getValue());

            //判断是否需要新的分组
            long groupNo = 0;   //分组编号
            List<TagGroup> tagGroupList = adapter.getTagGroupList();
            boolean needNewGroup = true;
            if (groupName != null && !groupName.isEmpty()) {
                for (TagGroup oneGroup : tagGroupList) {
                    if (oneGroup.getGroup_name().equals(groupName)) {
                        needNewGroup = false;
                        try {
                            groupNo = TagGroupDataController.nameTransToGno(groupName, this);
                        } catch (SQLiteException e) {
                            ExceptionHelper.showExceptionDialog(this, e);
                        }
                        break;
                    }
                }
            } else {
                needNewGroup = false;   //如果用户没有输入分组名称，则添加至默认分组（编号为0的分组）
            }

            //如果需要添加新分组，则通过数据库获取为新分组分配的编号
            if (needNewGroup) {
                try {
                    groupNo = TagGroupDataController.saveNewGroup(groupName, this);
                } catch (SQLiteException e) {
                    ExceptionHelper.showExceptionDialog(this, e);
                    return;
                }
            }

            //将新标签的数据写入数据库
            long tagNo = 0;
            try {
                tagNo = TagDataController.saveNewTag(tagName, tag_scope, groupNo, this);
            } catch (SQLiteException e) {
                ExceptionHelper.showExceptionDialog(this, e);
            }
            if (tagNo != 0) {
                //将变化保存至列表中并传递给适配器
                Tag newTag = new Tag(tagName, tagNo, tag_scope);
                if (needNewGroup) {
                    TagGroup new_group = new TagGroup(groupName, groupNo);
                    adapter.addNewTag(newTag, new_group);
                    binding.tagGroupRecycler.scrollToPosition(adapter.getItemCount() - 1);
                } else {
                    adapter.addNewTag(newTag, groupNo);
                }
                Toast.makeText(this, "标签添加成功", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void onTagModifyActivityResulted(int resultCode, Intent resultIntent) {
        if (resultCode == Activity.RESULT_CANCELED) {
            return;
        }

        Bundle dataBundle = resultIntent.getExtras();
        if (dataBundle == null) {
            NullPointerException e = new NullPointerException("无法获取修改后的标签信息");
            ExceptionHelper.showExceptionDialog(this, e);
            return;
        }

        long tag_no = dataBundle.getLong(KeyValueStrings.TAG_NO.getValue());                    //标签编号
        long origin_group_no = dataBundle.getLong(KeyValueStrings.TAG_GROUP_NO.getValue());     //原分组编号
        if (resultCode == RequestResultCode.RESULT_OK.ordinal()) {
            String tag_name = dataBundle.getString(KeyValueStrings.TAG_NAME.getValue());
            String groupName = dataBundle.getString(KeyValueStrings.TAG_GROUP_NAME.getValue());
            int tag_scope = dataBundle.getInt(KeyValueStrings.TAG_SCOPE.getValue());            //标签作用域

            //获取修改后的分组编号
            long group_no_after_modifying;
            if (groupName != null && !groupName.isEmpty()) {  //判断用户是否输入了分组名称
                try {
                    group_no_after_modifying = TagGroupDataController.nameTransToGno(groupName, this);
                } catch (SQLiteException e) {
                    ExceptionHelper.showExceptionDialog(this, e);
                    Toast.makeText(this, "标签修改失败", Toast.LENGTH_SHORT).show();
                    return;
                }
            } else {
                group_no_after_modifying = 0;
            }

            if (origin_group_no == group_no_after_modifying) {
                adapter.modifyTag(tag_name, tag_no, tag_scope, origin_group_no, this);
            } else {
                adapter.modifyTag(
                        tag_name,
                        tag_no,
                        tag_scope,
                        groupName,
                        origin_group_no,
                        group_no_after_modifying,
                        this);
            }
        } else if (resultCode == RequestResultCode.RESULT_DELETE.ordinal()) {
            adapter.deleteTag(tag_no, origin_group_no, this);
        } else if (resultCode == RequestResultCode.RESULT_MERGE.ordinal()) {
            long merge_target_tag_no = dataBundle.getLong(KeyValueStrings.MERGE_TARGET_NO.getValue());  //获取合并到的目标标签编号
            adapter.mergeTag(tag_no, merge_target_tag_no, origin_group_no, this);
        }
    }

    private void modifyGroup(int resultCode, Intent data) {
        if (resultCode == RequestResultCode.RESULT_CANCEL.ordinal()) {
            return;
        }

        Bundle dataBundle = data.getExtras();
        if (dataBundle == null) {
            NullPointerException e = new NullPointerException("无法获取修改后的分组信息");
            ExceptionHelper.showExceptionDialog(this, e);
            return;
        }

        long groupNo = dataBundle.getLong(KeyValueStrings.TAG_GROUP_NO.getValue());
        if (resultCode == RequestResultCode.RESULT_OK.ordinal()) {
            String newGroupName = dataBundle.getString(KeyValueStrings.TAG_GROUP_NAME.getValue());

            //修改视图中的分组并保存
            adapter.modifyGroup(groupNo, newGroupName, this);
        } else if (resultCode == RequestResultCode.RESULT_DELETE.ordinal()) {
            adapter.deleteGroup(groupNo, this);
        } else if (resultCode == RequestResultCode.RESULT_MERGE.ordinal()) {
            long mergeTargetNo = dataBundle.getLong(KeyValueStrings.MERGE_TARGET_NO.getValue());
            adapter.mergeGroup(groupNo, mergeTargetNo, this);
        }
    }
}