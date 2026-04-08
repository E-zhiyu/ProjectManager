package com.manager.assistant.ui.pages.bookkeeping.tag;

import android.app.Activity;
import android.content.Intent;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.manager.assistant.R;
import com.manager.assistant.data.controllers.TagGroupDataController;
import com.manager.assistant.databinding.ActivityTagManageBinding;
import com.manager.assistant.databinding.ViewRailHeaderBinding;
import com.manager.assistant.helpers.appearence.AppearanceAnimationHelper;
import com.manager.assistant.helpers.appearence.ColorHelper;
import com.manager.assistant.generic_enums.RequestResultCode;
import com.manager.assistant.helpers.ExceptionHelper;
import com.manager.assistant.generic_enums.KeyValueStrings;
import com.manager.assistant.data.classes.Tag;
import com.manager.assistant.data.classes.TagGroup;
import com.manager.assistant.helpers.appearence.ViewEdgeHelper;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class TagManageActivity extends AppCompatActivity implements TagManageRecyclerAdapter.OnTextViewClickedListener {
    private TagManageRecyclerAdapter adapter;
    private final CompositeDisposable disposables = new CompositeDisposable();                      //订阅列表（便于取消订阅）
    private ActivityResultLauncher<Intent> tagAddLauncher, tagModifyLauncher, modifyGroupLauncher;  //活动启动器
    private ActivityTagManageBinding binding;   //绑定的XML视图的引用
    private final Map<Integer, Long> itemIdAndGroupNoMap = new HashMap<>();                         //保存左侧导航栏Item的ID与标签分组编号映射的Map
    private Long currentGroupNo = -1L;                                                              //当前显示的标签分组编号

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
            binding.tagGroupRecycler.setPadding(0, 0, 0, systemBars.bottom);
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
    public void onTagTextViewClicked(long tagNo, String tagName, int tagScope, long groupNo, String groupName) {
        Intent skip2ModifyTag = new Intent(this, TagAddModifyActivity.class);
        Bundle clickedTagData = new Bundle();

        clickedTagData.putString(KeyValueStrings.TAG_NAME.getValue(), tagName);
        clickedTagData.putString(KeyValueStrings.TAG_GROUP_NAME.getValue(), groupName);
        clickedTagData.putLong(KeyValueStrings.TAG_NO.getValue(), tagNo);
        clickedTagData.putLong(KeyValueStrings.TAG_GROUP_NO.getValue(), groupNo);
        clickedTagData.putInt(KeyValueStrings.TAG_SCOPE.getValue(), tagScope);

        skip2ModifyTag.putExtras(clickedTagData);
        skip2ModifyTag.putExtra(KeyValueStrings.IS_MODIFY_MODE.getValue(), true);
        tagModifyLauncher.launch(skip2ModifyTag);
    }

    @Override
    public void onGroupTextViewClicked(long groupNo, String groupName) {
        Intent skip2GroupModify = new Intent(this, GroupModifyActivity.class);
        Bundle clickedGroupData = new Bundle();

        clickedGroupData.putString(KeyValueStrings.TAG_GROUP_NAME.getValue(), groupName);
        clickedGroupData.putLong(KeyValueStrings.TAG_GROUP_NO.getValue(), groupNo);

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
        binding.refreshLayout.setOnRefreshListener(() -> refreshUI(false));

        //获取标签分组数据
        Map<TagGroup, List<Tag>> tagGroupMap;
        try {
            tagGroupMap = TagGroupDataController.loadTagGroup(this);
        } catch (SQLiteException e) {
            ExceptionHelper.showExceptionDialog(this, e);
            Toast.makeText(this, "标签数据读取失败", Toast.LENGTH_SHORT).show();
            tagGroupMap = new LinkedHashMap<>();
        }

        //左侧标签分组抽屉
        ViewRailHeaderBinding headerBinding = ViewRailHeaderBinding.inflate(getLayoutInflater());
        headerBinding.headerBtn.setOnClickListener(v -> {   //设置头部视图点击监听
            if (binding.tagGroupNaviRail.isExpanded()) {
                binding.tagGroupNaviRail.collapse();
            } else {
                binding.tagGroupNaviRail.expand();
            }
        });
        binding.tagGroupNaviRail.addHeaderView(headerBinding.getRoot());
        int index = 1;
        Menu groupMenu = binding.tagGroupNaviRail.getMenu();
        MenuItem menuItem = groupMenu.add(Menu.NONE, 0, Menu.NONE, "显示所有");
        menuItem.setIcon(R.drawable.outline_select_all_24);
        itemIdAndGroupNoMap.put(0, -1L);
        for (TagGroup group : tagGroupMap.keySet()) {
            menuItem = groupMenu.add(Menu.NONE, index, Menu.NONE, group.getGroupName());
            menuItem.setIcon(R.drawable.outline_tab_group_24);

            //将index与groupNo的映射保存到Map中
            itemIdAndGroupNoMap.put(index, group.getGroupNo());
            index++;
        }
        binding.tagGroupNaviRail.setOnItemSelectedListener(item -> {
            Long targetGroupNo = itemIdAndGroupNoMap.get(item.getItemId());
            if (!Objects.equals(currentGroupNo, targetGroupNo) && targetGroupNo != null) {
                //刷新标签列表
                currentGroupNo = targetGroupNo;
                refreshUI(false);
                return true;
            } else
                return targetGroupNo == null;    //如果targetGroupNo为null，说明是手动刷新RailView时触发的监听器，需要返回true
        });

        //初始化RecyclerView
        adapter = new TagManageRecyclerAdapter(tagGroupMap, this);
        binding.tagGroupRecycler.setAdapter(adapter);

        //添加RecyclerView滚动监听器，用以控制添加按钮的显示与否
        AppearanceAnimationHelper.setupFloatingBtnBehaviour(binding.tagGroupRecycler, binding.addFloatingBtn);
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

        //只有当标签的分组与当前显示的分组一致时，或者当前显示所有分组时才更新RecyclerView
        if (groupNo == currentGroupNo || currentGroupNo == -1) {
            //通过适配器刷新UI
            Tag newTag = new Tag(tagName, tagNo, tagScope);
            TagGroup group = new TagGroup(groupName, groupNo);
            adapter.addNewTag(newTag, group);
        }

        //刷新RailView
        refreshRailView();
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
            refreshRailView();
            //TODO:修复选择单个分组时移动分组，会在界面中添加新分组的BUG
            adapter.modifyTag(tagName, tagNo, tagScope, groupName, oldGroupNo, newGroupNo);
        } else if (resultCode == RequestResultCode.RESULT_DELETE.ordinal()) {
            adapter.deleteTag(tagNo, oldGroupNo);
        } else if (resultCode == RequestResultCode.RESULT_MERGE.ordinal()) {
            adapter.mergeTag(tagNo, oldGroupNo);
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
            adapter.modifyGroup(groupNo, newGroupName);

            //更新RailView中的选项Title
            for (Map.Entry<Integer, Long> entry : itemIdAndGroupNoMap.entrySet()) {
                int itemIndex = entry.getKey();     //MenuItem的Id就是下标
                long mapGroupNo = entry.getValue();
                if (mapGroupNo == groupNo) {
                    Menu railMenu = binding.tagGroupNaviRail.getMenu();
                    railMenu.getItem(itemIndex).setTitle(newGroupName);
                    break;
                }
            }
        } else if (resultCode == RequestResultCode.RESULT_DELETE.ordinal()) {
            adapter.deleteGroup(groupNo);
            if (currentGroupNo != -1L) {
                refreshUI(true);    //显示单个分组时直接刷新整个界面
            } else {
                refreshRailView();                  //显示所有分组时只需要刷新RailView以保证有动画
            }
        } else if (resultCode == RequestResultCode.RESULT_MERGE.ordinal()) {
            long mergeTargetNo = dataBundle.getLong(KeyValueStrings.MERGE_TARGET_NO.getValue());
            adapter.mergeGroup(groupNo, mergeTargetNo);
            refreshUI(true);
        }
    }

    /**
     * 视图刷新回调
     *
     * @param refreshRailView 是否需要刷新左侧导航栏
     */
    private void refreshUI(boolean refreshRailView) {
        if (refreshRailView) {
            currentGroupNo = -1L;
        }
        disposables.add(
                Observable.fromCallable(() -> TagGroupDataController.loadTagGroup(
                                this,
                                0,
                                currentGroupNo,
                                null
                        ))
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(tagGroupListMap -> {
                                    adapter.refreshUI(tagGroupListMap);

                                    if (!refreshRailView) {
                                        return;
                                    }

                                    //清空
                                    Menu groupMenu = binding.tagGroupNaviRail.getMenu();
                                    groupMenu.clear();
                                    itemIdAndGroupNoMap.clear();

                                    //添加显示所有的选项
                                    MenuItem menuItem = groupMenu.add(Menu.NONE, 0, Menu.NONE, "显示所有");
                                    menuItem.setIcon(R.drawable.outline_select_all_24);
                                    itemIdAndGroupNoMap.put(0, -1L);

                                    //遍历添加分组选项
                                    int index = 1;
                                    for (TagGroup group : tagGroupListMap.keySet()) {
                                        menuItem = groupMenu.add(Menu.NONE, index, Menu.NONE, group.getGroupName());
                                        menuItem.setIcon(R.drawable.outline_tab_group_24);

                                        //将index与groupNo的映射保存到Map中
                                        itemIdAndGroupNoMap.put(index, group.getGroupNo());
                                        index++;
                                    }
                                },
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

    /**
     * 只刷新RailView
     */
    private void refreshRailView() {
        disposables.add(
                Observable.fromCallable(() -> TagGroupDataController.getTagGroup(this, -1))
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeOn(Schedulers.io())
                        .subscribe(tagGroupList -> {
                            //先清空
                            Menu groupMenu = binding.tagGroupNaviRail.getMenu();
                            groupMenu.clear();
                            itemIdAndGroupNoMap.clear();

                            //添加“显示所有”
                            MenuItem menuItem = groupMenu.add(Menu.NONE, 0, Menu.NONE, "显示所有");
                            menuItem.setIcon(R.drawable.outline_select_all_24);
                            itemIdAndGroupNoMap.put(0, -1L);

                            //遍历添加选项
                            int index = 1;
                            for (TagGroup group : tagGroupList) {
                                menuItem = groupMenu.add(Menu.NONE, index, Menu.NONE, group.getGroupName());
                                menuItem.setIcon(R.drawable.outline_tab_group_24);

                                //重新选中当前分组（放在这里是为了让监听器中获得到的groupNo为null，避免重复刷新RecyclerView）
                                if (currentGroupNo == group.getGroupNo()) {
                                    binding.tagGroupNaviRail.setSelectedItemId(index);
                                }

                                //将index与groupNo的映射保存到Map中
                                itemIdAndGroupNoMap.put(index, group.getGroupNo());
                                index++;
                            }
                        })
        );
    }
}