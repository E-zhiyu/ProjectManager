package com.project.manager.ui.pages.bookkeeping.tag;

import android.app.Activity;
import android.content.Intent;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.project.manager.databinding.ActivityTagManageBinding;
import com.project.manager.helpers.ColorHelper;
import com.project.manager.enums.RequestResultCode;
import com.project.manager.helpers.ExceptionHelper;
import com.project.manager.enums.KeyValueStrings;
import com.project.manager.data.data_class.Tag;
import com.project.manager.data.data_class.TagGroup;

import java.util.ArrayList;
import java.util.List;

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

        initActivityLauncher();
        initViews();

        List<TagGroup> tagGroupList;    //获取标签分组数据
        try {
            tagGroupList = TagGroup.loadTagGroups(this);
        } catch (SQLiteException e) {
            ExceptionHelper.showExceptionDialog(this, e);
            Toast.makeText(this, "标签数据读取失败", Toast.LENGTH_SHORT).show();
            tagGroupList = new ArrayList<>();
        }

        RecyclerView tagGroupRecycler = binding.tagGroupRecycler;
        adapter = new TagManageRecyclerAdapter(tagGroupList, this, this);
        tagGroupRecycler.setAdapter(adapter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;

        // 防止内存泄漏
        disposables.dispose();
    }

    @Override
    public void onTagTextViewClicked(long tag_no, String tag_name, long group_no, String group_name) {
        Intent skip2ModifyTag = new Intent(this, TagAddModifyActivity.class);
        Bundle clickedTagData = new Bundle();

        clickedTagData.putString(KeyValueStrings.TAG_NAME.getValue(), tag_name);
        clickedTagData.putString(KeyValueStrings.TAG_GROUP_NAME.getValue(), group_name);
        clickedTagData.putLong(KeyValueStrings.TAG_NO.getValue(), tag_no);
        clickedTagData.putLong(KeyValueStrings.TAG_GROUP_NO.getValue(), group_no);

        //获取已保存的标签分组信息
        List<TagGroup> tagGroupList = adapter.getTagGroupList();
        ArrayList<String> groupNameList = new ArrayList<>();
        for (TagGroup group : tagGroupList) {
            groupNameList.add(group.getGroup_name());
        }
        clickedTagData.putStringArrayList(KeyValueStrings.TAG_GROUP_NAME_LIST.getValue(), groupNameList);

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
        MaterialToolbar toolbar = binding.toolbar;
        toolbar.setNavigationOnClickListener(v -> finish());

        //设置按钮点击监听
        MaterialButton tag_add_btn = binding.tagAddBtn;
        tag_add_btn.setOnClickListener(v -> {
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

        //设置刷新布局的刷新动作监听
        SwipeRefreshLayout refreshLayout = binding.refreshLayout;

        //获取颜色资源并设置下拉刷新布局的颜色
        int colorPrimary = ColorHelper.getPrimaryColor(this);
        int colorSecondary = ColorHelper.getSecondaryPrimaryColor(this);
        refreshLayout.setColorSchemeColors(colorPrimary, colorSecondary);
        int colorBackground = ColorHelper.getBackgroundColor(this);
        refreshLayout.setProgressBackgroundColorSchemeColor(colorBackground);

        refreshLayout.setOnRefreshListener(() -> disposables.add(
                Observable.fromCallable(() -> TagGroup.loadTagGroups(this))
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(tagGroupList -> adapter.refreshUI(tagGroupList),
                                e -> {
                                    ExceptionHelper.showExceptionDialog(this, e);
                                    Toast.makeText(this, "刷新失败", Toast.LENGTH_SHORT).show();
                                },
                                () -> refreshLayout.setRefreshing(false)
                        )
        ));
    }

    //初始化活动启动器
    private void initActivityLauncher() {
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
            String tag_name = null;         //标签名称
            String group_name = null;       //分组名称
            if (dataBundle != null) {
                tag_name = dataBundle.getString(KeyValueStrings.TAG_NAME.getValue());
                group_name = dataBundle.getString(KeyValueStrings.TAG_GROUP_NAME.getValue());
            }

            //判断是否需要新的分组
            long group_no = 0;   //分组编号
            List<TagGroup> tagGroupList = adapter.getTagGroupList();
            boolean needNewGroup = true;
            if (group_name != null && !group_name.isEmpty()) {
                for (TagGroup oneGroup : tagGroupList) {
                    if (oneGroup.getGroup_name().equals(group_name)) {
                        needNewGroup = false;
                        try {
                            group_no = TagGroup.nameTransToGno(group_name, this);
                            Toast.makeText(this, "标签已成功添加", Toast.LENGTH_SHORT).show();
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
                    group_no = TagGroup.saveNewGroup(group_name, this);
                } catch (SQLiteException e) {
                    ExceptionHelper.showExceptionDialog(this, e);
                    return;
                }
            }

            //将新标签的数据写入数据库
            long tag_no = 0;
            try {
                tag_no = Tag.saveNewTag(tag_name, group_no, this);
            } catch (SQLiteException e) {
                ExceptionHelper.showExceptionDialog(this, e);
            }
            if (tag_no != 0) {
                //将变化保存至列表中并传递给适配器
                Tag new_tag = new Tag(tag_name, tag_no);
                if (needNewGroup) {
                    TagGroup new_group = new TagGroup(group_name, group_no);
                    adapter.addNewTag(new_tag, new_group);
                } else {
                    adapter.addNewTag(new_tag, group_no);
                }
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
            String group_name = dataBundle.getString(KeyValueStrings.TAG_GROUP_NAME.getValue());

            //获取修改后的分组编号
            long group_no_after_modifying;
            if (group_name != null && !group_name.isEmpty()) {  //判断用户是否输入了分组名称
                try {
                    group_no_after_modifying = TagGroup.nameTransToGno(group_name, this);
                } catch (SQLiteException e) {
                    ExceptionHelper.showExceptionDialog(this, e);
                    Toast.makeText(this, "标签修改失败", Toast.LENGTH_SHORT).show();
                    return;
                }
            } else {
                group_no_after_modifying = 0;
            }

            if (origin_group_no == group_no_after_modifying) {
                adapter.modifyTag(tag_name, tag_no, origin_group_no);
            } else {
                adapter.modifyTag(tag_name, tag_no, group_name, origin_group_no, group_no_after_modifying);
            }
        } else if (resultCode == RequestResultCode.RESULT_DELETE.ordinal()) {
            adapter.deleteTag(tag_no, origin_group_no);
        } else if (resultCode == RequestResultCode.RESULT_MERGE.ordinal()) {
            long merge_target_tag_no = dataBundle.getLong(KeyValueStrings.MERGE_TARGET_NO.getValue());  //获取合并到的目标标签编号
            adapter.mergeTag(tag_no, merge_target_tag_no, origin_group_no);
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

        long group_no = dataBundle.getLong(KeyValueStrings.TAG_GROUP_NO.getValue());
        if (resultCode == RequestResultCode.RESULT_OK.ordinal()) {
            String new_group_name = dataBundle.getString(KeyValueStrings.TAG_GROUP_NAME.getValue());

            //修改视图中的分组并保存
            adapter.modifyGroup(group_no, new_group_name);
        } else if (resultCode == RequestResultCode.RESULT_DELETE.ordinal()) {
            adapter.deleteGroup(group_no);
        } else if (resultCode == RequestResultCode.RESULT_MERGE.ordinal()) {
            long merge_target_no = dataBundle.getLong(KeyValueStrings.MERGE_TARGET_NO.getValue());
            adapter.mergeGroup(group_no, merge_target_no);
        }
    }
}