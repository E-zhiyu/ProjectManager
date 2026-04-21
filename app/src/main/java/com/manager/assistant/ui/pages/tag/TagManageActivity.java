package com.manager.assistant.ui.pages.tag;

import android.app.Activity;
import android.content.Intent;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.ConcatAdapter;

import com.manager.assistant.data.controllers.TagGroupDataController;
import com.manager.assistant.databinding.ActivityTagManageBinding;
import com.manager.assistant.helpers.appearence.AppearanceAnimationHelper;
import com.manager.assistant.helpers.appearence.ColorHelper;
import com.manager.assistant.generic_enums.RequestResultCode;
import com.manager.assistant.helpers.ExceptionHelper;
import com.manager.assistant.generic_enums.KeyValueStrings;
import com.manager.assistant.data.classes.Tag;
import com.manager.assistant.data.classes.TagGroup;
import com.manager.assistant.helpers.appearence.ViewEdgeHelper;
import com.manager.assistant.ui.pages.tag.adapters.TagAdapter;
import com.manager.assistant.ui.pages.tag.adapters.TagGroupAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class TagManageActivity extends AppCompatActivity implements TagGroupAdapter.OnClickedListener, TagAdapter.OnClickedListener {
    private ConcatAdapter adapter;                                                                  //Recycler适配器
    private final Map<Long, AdapterContainer> adapterContainerMap = new HashMap<>();                //分组与标签适配器对的引用
    private final CompositeDisposable disposables = new CompositeDisposable();                      //订阅列表（便于取消订阅）
    private ActivityResultLauncher<Intent> tagAddLauncher, tagModifyLauncher, modifyGroupLauncher;  //活动启动器
    private ActivityTagManageBinding binding;                                                       //绑定的XML视图的引用

    /**
     * 分组与标签适配器对
     */
    public static class AdapterContainer {
        public TagGroupAdapter groupAdapter;
        public TagAdapter tagAdapter;

        public AdapterContainer(TagGroupAdapter g, TagAdapter t) {
            this.groupAdapter = g;
            this.tagAdapter = t;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);    //启用边到边以适配沉浸式小白条

        binding = ActivityTagManageBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //设置界面边距以防内容被小白条遮挡
        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0);
            binding.tagRecycler.setPadding(0, 0, 0, systemBars.bottom);
            return insets;
        });
        ViewEdgeHelper.setMarginToNavigation(binding.addFloatingBtn, this);

        initLaunchers();
        initViews();
        AppearanceAnimationHelper.setupAllChildMorphAnimation(binding.getRoot());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;

        // 防止内存泄漏
        disposables.dispose();
    }

    @Override
    public void onTagClicked(@NonNull Tag tag, @NonNull TagGroup group) {
        Intent skip2ModifyTag = new Intent(this, TagAddModifyActivity.class);
        Bundle clickedTagData = new Bundle();

        clickedTagData.putString(KeyValueStrings.TAG_NAME.getValue(), tag.getName());
        clickedTagData.putString(KeyValueStrings.TAG_GROUP_NAME.getValue(), group.getGroupName());
        clickedTagData.putLong(KeyValueStrings.TAG_NO.getValue(), tag.getTno());
        clickedTagData.putLong(KeyValueStrings.TAG_GROUP_NO.getValue(), group.getGroupNo());
        clickedTagData.putInt(KeyValueStrings.TAG_SCOPE.getValue(), tag.getScope());

        skip2ModifyTag.putExtras(clickedTagData);
        skip2ModifyTag.putExtra(KeyValueStrings.IS_MODIFY_MODE.getValue(), true);
        tagModifyLauncher.launch(skip2ModifyTag);
    }

    @Override
    public void onGroupClicked(@NonNull TagGroup group) {
        Intent skip2GroupModify = new Intent(this, GroupModifyActivity.class);
        Bundle clickedGroupData = new Bundle();

        clickedGroupData.putString(KeyValueStrings.TAG_GROUP_NAME.getValue(), group.getGroupName());
        clickedGroupData.putLong(KeyValueStrings.TAG_GROUP_NO.getValue(), group.getGroupNo());

        skip2GroupModify.putExtras(clickedGroupData);
        modifyGroupLauncher.launch(skip2GroupModify);
    }

    @Override
    public void onExpandStatueChanged(long groupNo, boolean isExpanded) {
        //获取下标和适配器容器
        AdapterContainer container = adapterContainerMap.get(groupNo);
        if (container == null) {
            return;
        }

        //通知适配器折叠或展开标签
        container.tagAdapter.changeExpandStatue(isExpanded);
    }

    //初始化视图
    private void initViews() {
        //设置标题栏的图标点击监听器
        binding.toolbar.setNavigationOnClickListener(v -> finish());

        //设置按钮点击监听
        binding.addFloatingBtn.setOnClickListener(v -> {
            Intent skip2TagAdd = new Intent(this, TagAddModifyActivity.class);
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
        binding.refreshLayout.setOnRefreshListener(this::refreshUI);

        //获取标签分组数据
        Map<TagGroup, List<Tag>> tagGroupMap;
        try {
            tagGroupMap = TagGroupDataController.loadTagGroup(this);
        } catch (SQLiteException e) {
            ExceptionHelper.showExceptionDialog(this, e);
            Toast.makeText(this, "标签数据读取失败", Toast.LENGTH_SHORT).show();
            tagGroupMap = new LinkedHashMap<>();
        }

        //初始化RecyclerView
        adapter = new ConcatAdapter();
        binding.tagRecycler.setAdapter(adapter);
        refreshRecyclerView(tagGroupMap);

        //添加RecyclerView滚动监听器，用以控制添加按钮的显示与否
        AppearanceAnimationHelper.setupFloatingBtnBehaviour(binding.tagRecycler, binding.addFloatingBtn);
    }

    /**
     * 刷新RecyclerView
     *
     * @param tagGroupMap 数据源字典
     */
    private void refreshRecyclerView(@NonNull Map<TagGroup, List<Tag>> tagGroupMap) {
        //清空引用以及旧的Adapters
        for (Map.Entry<Long, AdapterContainer> entry : adapterContainerMap.entrySet()) {
            AdapterContainer container = entry.getValue();
            adapter.removeAdapter(container.groupAdapter);
            adapter.removeAdapter(container.tagAdapter);
        }
        adapterContainerMap.clear();

        //动态生成适配器
        for (Map.Entry<TagGroup, List<Tag>> entry : tagGroupMap.entrySet()) {
            TagGroup group = entry.getKey();
            List<Tag> tagList = entry.getValue();

            TagGroupAdapter groupAdapter = new TagGroupAdapter(group, this);
            TagAdapter tagAdapter = new TagAdapter(group, tagList, this);

            adapter.addAdapter(groupAdapter);
            adapter.addAdapter(tagAdapter);

            //保存适配器引用
            AdapterContainer container = new AdapterContainer(groupAdapter, tagAdapter);
            adapterContainerMap.put(group.getGroupNo(), container);
        }
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
                        onTagAdded(resultCode, data);
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
                        onTagModified(resultCode, data);
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
                        onGroupModified(resultCode, data);
                    }
                }
        );
    }

    /**
     * 处理新标签添加回调
     *
     * @param resultCode   子界面的返回代码
     * @param resultIntent 返回的Intent
     */
    private void onTagAdded(int resultCode, Intent resultIntent) {
        if (resultCode != RequestResultCode.RESULT_OK.ordinal()) {
            return;
        }

        Bundle dataBundle = resultIntent.getExtras();
        if (dataBundle == null) {
            return;
        }

        String tagName = dataBundle.getString(KeyValueStrings.TAG_NAME.getValue());
        String groupName = dataBundle.getString(KeyValueStrings.TAG_GROUP_NAME.getValue());
        int tagScope = dataBundle.getInt(KeyValueStrings.TAG_SCOPE.getValue());
        long tagNo = dataBundle.getLong(KeyValueStrings.TAG_NO.getValue(), 0L);
        long groupNo = dataBundle.getLong(KeyValueStrings.TAG_GROUP_NO.getValue(), 0L);
        if (tagNo == 0L) {
            return;
        }

        //通过适配器刷新UI
        Tag newTag = new Tag(tagName, tagNo, tagScope);
        AdapterContainer container = adapterContainerMap.get(groupNo);
        if (container != null) {
            container.tagAdapter.addTag(newTag);
        } else {
            //添加新分组
            TagGroup newGroup = new TagGroup(groupName, groupNo);
            TagGroupAdapter groupAdapter = new TagGroupAdapter(newGroup, this);

            //添加新标签Adapter
            List<Tag> tagList = new ArrayList<>();
            tagList.add(new Tag(tagName, tagNo, tagScope));
            TagAdapter tagAdapter = new TagAdapter(newGroup, tagList, this);

            //添加到RecyclerView中
            container = new AdapterContainer(groupAdapter, tagAdapter);
            adapterContainerMap.put(groupNo, container);    //保存新引用
            adapter.addAdapter(groupAdapter);
            adapter.addAdapter(tagAdapter);
        }
    }

    /**
     * 处理标签修改回调
     *
     * @param resultCode   子界面返回的代码
     * @param resultIntent 返回的数据
     */
    private void onTagModified(int resultCode, Intent resultIntent) {
        if (resultCode == Activity.RESULT_CANCELED) {
            return;
        }

        Bundle dataBundle = resultIntent.getExtras();
        if (dataBundle == null) {
            NullPointerException e = new NullPointerException("无法获取修改后的标签信息");
            ExceptionHelper.showExceptionDialog(this, e);
            return;
        }

        //解析数据包
        long tagNo = dataBundle.getLong(KeyValueStrings.TAG_NO.getValue());                     //标签编号
        long oldGroupNo = dataBundle.getLong(KeyValueStrings.TAG_GROUP_NO.getValue());          //原分组编号
        long newGroupNo = dataBundle.getLong(KeyValueStrings.TAG_GROUP_NO_NEW.getValue());      //新分组编号
        String groupName = dataBundle.getString(KeyValueStrings.TAG_GROUP_NAME.getValue());     //分组名称
        String tagName = dataBundle.getString(KeyValueStrings.TAG_NAME.getValue());             //标签名称
        int tagScope = dataBundle.getInt(KeyValueStrings.TAG_SCOPE.getValue());                 //标签作用域

        //执行操作
        if (resultCode == RequestResultCode.RESULT_OK.ordinal()) {
            if (oldGroupNo == newGroupNo) {
                AdapterContainer container = adapterContainerMap.get(oldGroupNo);
                if (container != null) {
                    container.tagAdapter.modifyTag(new Tag(tagName, tagNo, tagScope));
                }
            } else {
                //删除旧分组中的标签
                AdapterContainer oldContainer = adapterContainerMap.get(oldGroupNo);
                if (oldContainer != null) {
                    oldContainer.tagAdapter.deleteTag(tagNo);
                }

                //新分组中添加标签
                AdapterContainer newContainer = adapterContainerMap.get(newGroupNo);
                if (newContainer != null) {
                    newContainer.tagAdapter.addTag(new Tag(tagName, tagNo, tagScope));
                } else {
                    //添加新分组
                    TagGroup newGroup = new TagGroup(groupName, newGroupNo);
                    TagGroupAdapter groupAdapter = new TagGroupAdapter(newGroup, this);

                    //添加新标签Adapter
                    List<Tag> tagList = new ArrayList<>();
                    tagList.add(new Tag(tagName, tagNo, tagScope));
                    TagAdapter tagAdapter = new TagAdapter(newGroup, tagList, this);

                    //添加到RecyclerView中
                    newContainer = new AdapterContainer(groupAdapter, tagAdapter);
                    adapterContainerMap.put(newGroupNo, newContainer);  //保存新引用
                    adapter.addAdapter(groupAdapter);
                    adapter.addAdapter(tagAdapter);
                }
            }
        } else if (resultCode == RequestResultCode.RESULT_DELETE.ordinal()) {
            AdapterContainer container = adapterContainerMap.get(oldGroupNo);
            if (container != null) {
                container.tagAdapter.deleteTag(tagNo);
            }
        } else if (resultCode == RequestResultCode.RESULT_MERGE.ordinal()) {
            AdapterContainer container = adapterContainerMap.get(oldGroupNo);
            if (container != null) {
                container.tagAdapter.deleteTag(tagNo);
            }
        }
    }

    /**
     * 分组修改回调
     *
     * @param resultCode 子界面返回的代码
     * @param data       返回的数据
     */
    private void onGroupModified(int resultCode, Intent data) {
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
            AdapterContainer container = adapterContainerMap.get(groupNo);
            if (container != null) {
                TagGroup newGroup = new TagGroup(newGroupName, groupNo);
                container.groupAdapter.onGroupModified(newGroup);
                container.tagAdapter.onGroupModified(newGroup);
            }
        } else if (resultCode == RequestResultCode.RESULT_DELETE.ordinal()) {
            //删除Recycler中对应的适配器
            AdapterContainer container = adapterContainerMap.get(groupNo);
            if (container != null) {
                adapter.removeAdapter(container.groupAdapter);
                adapter.removeAdapter(container.tagAdapter);
            }
        } else if (resultCode == RequestResultCode.RESULT_MERGE.ordinal()) {
            //删除Recycler中对应的旧适配器
            AdapterContainer oldContainer = adapterContainerMap.get(groupNo);
            if (oldContainer == null) {
                return;
            }

            //删除旧的适配器
            List<Tag> tagList = oldContainer.tagAdapter.getTagList();  //获取被合并分组的标签列表
            adapter.removeAdapter(oldContainer.groupAdapter);
            adapter.removeAdapter(oldContainer.tagAdapter);

            //更新现有的适配器
            long mergeTargetNo = dataBundle.getLong(KeyValueStrings.MERGE_TARGET_NO.getValue());
            AdapterContainer newContainer = adapterContainerMap.get(mergeTargetNo);
            if (newContainer != null) {
                newContainer.tagAdapter.addTag(tagList);
            }
        }
    }

    /**
     * 视图刷新回调
     */
    private void refreshUI() {
        disposables.add(
                Observable.fromCallable(() -> TagGroupDataController.loadTagGroup(
                                this,
                                0,
                                null
                        ))
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(this::refreshRecyclerView,
                                e -> {
                                    ExceptionHelper.showExceptionDialog(this, e);
                                    Toast.makeText(this, "标签列表刷新失败", Toast.LENGTH_SHORT).show();
                                },
                                () -> {
                                    binding.refreshLayout.setRefreshing(false);
                                    binding.addFloatingBtn.show();
                                }
                        )
        );
    }
}