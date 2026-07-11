package com.manager.assistant.ui.pages.tag;

import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.manager.assistant.R;
import com.manager.assistant.data.save.db.BookkeepingDb;
import com.manager.assistant.data.save.db.entities.TagEntity;
import com.manager.assistant.data.save.db.entities.TagGroupEntity;
import com.manager.assistant.data.save.db.entities.composite.ui.TagListUiModel;
import com.manager.assistant.data.save.db.services.TagService;
import com.manager.assistant.databinding.ActivityTagListBinding;
import com.manager.assistant.generic_enums.KeyStrings;
import com.manager.assistant.helpers.ExceptionHelper;
import com.manager.assistant.helpers.appearence.AppearanceHelper;

import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class TagListActivity extends AppCompatActivity {
    private final CompositeDisposable disposables = new CompositeDisposable();  //订阅列表
    private ActivityTagListBinding binding;                                     //绑定的XML视图的引用

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);    //启用边到边以适配沉浸式小白条

        binding = ActivityTagListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //设置界面边距以防内容被小白条遮挡
        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, 0, systemBars.right, 0);
            binding.recycler.setPadding(systemBars.left, 0, systemBars.right, systemBars.bottom);
            return insets;
        });

        initViews();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;

        // 防止内存泄漏
        disposables.dispose();
    }

    /**
     * 初始化视图
     */
    private void initViews() {
        //工具栏
        binding.toolbar.setNavigationOnClickListener(view -> finish());

        //添加按钮
        binding.addFab.setOnClickListener(view -> {
            Intent skip2Input = new Intent(this, TagInputActivity.class);
            startActivity(skip2Input);
        });
        AppearanceHelper.attachMorphAnimation(binding.addFab);
        AppearanceHelper.setMarginToNavigation(binding.addFab, this);

        //标签列表
        TagListAdapter adapter = new TagListAdapter(
                (entity, anchor) -> {
                    Intent skip2Input = new Intent(this, TagInputActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putLong(KeyStrings.TAG_ID.v(), entity.getTagId());
                    skip2Input.putExtras(bundle);
                    startActivity(skip2Input);
                },
                (entity, anchor) -> {
                    PopupMenu popupMenu = new PopupMenu(this, anchor, Gravity.END);
                    popupMenu.getMenuInflater().inflate(R.menu.menu_tag_list_edit, popupMenu.getMenu());

                    popupMenu.setOnMenuItemClickListener(item -> {
                        int id = item.getItemId();
                        if (id == R.id.action_delete_tag) {
                            deleteTag(entity);
                            return true;
                        } else if (id == R.id.action_merge_tag) {
                            mergeTag(entity);
                            return true;
                        }

                        return false;
                    });

                    popupMenu.show();
                },
                (entity, anchor) -> {
                    PopupMenu popupMenu = new PopupMenu(this, anchor, Gravity.END);
                    popupMenu.getMenuInflater().inflate(R.menu.menu_tag_list_group_edit, popupMenu.getMenu());

                    popupMenu.setOnMenuItemClickListener(item -> {
                        int id = item.getItemId();
                        if (id == R.id.action_delete_group) {
                            deleteGroup(entity);
                            return true;
                        } else if (id == R.id.action_merge_group) {
                            mergeGroup(entity);
                            return true;
                        }

                        return false;
                    });

                    popupMenu.show();
                }
        );
        binding.recycler.setAdapter(adapter);
        BookkeepingDb db = BookkeepingDb.getInstance(this);
        disposables.add(TagService.getTagListFlowable(db)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(
                        modelList -> {
                            if (modelList.isEmpty()) {
                                binding.emptyText.setVisibility(View.VISIBLE);
                            } else {
                                binding.emptyText.setVisibility(View.GONE);
                            }

                            adapter.submitList(modelList);
                        }
                )
        );
    }

    /**
     * 删除标签
     *
     * @param tag 需要删除的标签
     */
    private void deleteTag(@NonNull TagEntity tag) {
        String message = String.format(
                Locale.getDefault(),
                "确认删除“%s”吗？",
                tag.getName()
        );
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.delete_tag)
                .setMessage(message)
                .setPositiveButton("确定", (dialogInterface, i) -> {
                    BookkeepingDb db = BookkeepingDb.getInstance(this);
                    disposables.add(db.tagDao().deleteTag(tag)
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribeOn(Schedulers.io())
                            .subscribe(
                                    () -> Toast.makeText(this, "标签已删除", Toast.LENGTH_SHORT).show(),
                                    e -> ExceptionHelper.showExceptionDialog(this, e)
                            )
                    );
                })
                .setNegativeButton("取消", null)
                .show();
    }

    /**
     * 合并标签
     *
     * @param mergedTag 被合并的标签
     */
    private void mergeTag(TagEntity mergedTag) {
        //TODO:合并标签
    }

    /**
     * 删除分组
     *
     * @param group 需要删除的分组实例
     */
    private void deleteGroup(@NonNull TagGroupEntity group) {
        if (group.getGroupId() != -1) {
            //普通分组删除逻辑
            String message = String.format(
                    Locale.getDefault(),
                    "确认删除“%s”吗？其包含的标签将一并删除。",
                    group.getName()
            );
            new MaterialAlertDialogBuilder(this)
                    .setTitle(R.string.delete_group)
                    .setMessage(message)
                    .setPositiveButton("确定", (dialogInterface, i) -> {
                        BookkeepingDb db = BookkeepingDb.getInstance(this);
                        disposables.add(db.tagDao().deleteGroup(group)
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribeOn(Schedulers.io())
                                .subscribe(
                                        () -> Toast.makeText(this, "标签分组已删除", Toast.LENGTH_SHORT).show(),
                                        e -> ExceptionHelper.showExceptionDialog(this, e)
                                )
                        );
                    })
                    .setNegativeButton("取消", null)
                    .show();
        } else {
            //默认分组删除逻辑
            new MaterialAlertDialogBuilder(this)
                    .setTitle(R.string.delete_group)
                    .setMessage("此操作不会删除默认分组，但是会清空默认分组中的标签，确认继续吗？")
                    .setPositiveButton("确定", (dialogInterface, i) -> {
                        BookkeepingDb db = BookkeepingDb.getInstance(this);
                        disposables.add(db.tagDao().deleteTagInDefaultGroup()
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribeOn(Schedulers.io())
                                .subscribe(
                                        () -> Toast.makeText(this, "默认分组已清空", Toast.LENGTH_SHORT).show(),
                                        e -> ExceptionHelper.showExceptionDialog(this, e)
                                )
                        );
                    })
                    .setNegativeButton("取消", null)
                    .show();
        }
    }

    /**
     * 合并分组
     *
     * @param mergedGroup 被合并的分组
     */
    private void mergeGroup(TagGroupEntity mergedGroup) {
        if (!(binding.recycler.getAdapter() instanceof TagListAdapter)) return;

        //获取排除了被合并的分组以外的分组列表
        List<TagGroupEntity> currentGroupList = ((TagListAdapter) binding.recycler.getAdapter()).getCurrentList().stream()
                .filter(model -> model instanceof TagListUiModel.Group)
                .map(model -> ((TagListUiModel.Group) model).group)
                .filter(group -> group.getGroupId() != mergedGroup.getGroupId())
                .collect(Collectors.toList());

        //显示对话框
        String[] groupNames = currentGroupList.stream()
                .map(TagGroupEntity::getName)
                .toArray(String[]::new);
        AtomicInteger checkedIndex = new AtomicInteger(-1);
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.choose_target_group)
                .setSingleChoiceItems(groupNames, -1, (dialogInterface, i) ->
                        checkedIndex.set(i)
                )
                .setPositiveButton("确认", (dialogInterface, i) -> {
                    int index = checkedIndex.get();
                    if (index == -1) {
                        Toast.makeText(this, "请选择一个分组进行合并", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    TagGroupEntity targetGroup = currentGroupList.get(index);
                    String message;
                    if (mergedGroup.getGroupId() != -1) {
                        message = String.format(
                                Locale.getDefault(),
                                "确认将“%s”中的标签合并至“%s”并删除“%s”吗？",
                                mergedGroup.getName(),
                                targetGroup.getName(),
                                mergedGroup.getName()
                        );
                    } else {
                        message = String.format(
                                Locale.getDefault(),
                                "确认将“%s”中的标签合并至“%s”吗？（默认分组不会消失）",
                                mergedGroup.getName(),
                                targetGroup.getName()
                        );
                    }
                    new MaterialAlertDialogBuilder(this)
                            .setTitle(R.string.merge_group)
                            .setMessage(message)
                            .setPositiveButton("确认", (dialogInterface1, i1) -> {
                                BookkeepingDb db = BookkeepingDb.getInstance(this);
                                disposables.add(TagService.mergeTagGroup(mergedGroup, targetGroup, db)
                                        .observeOn(AndroidSchedulers.mainThread())
                                        .subscribeOn(Schedulers.io())
                                        .subscribe(
                                                () -> Toast.makeText(this, "标签分组合并完成", Toast.LENGTH_SHORT).show(),
                                                e -> ExceptionHelper.showExceptionDialog(this, e)
                                        )
                                );
                            })
                            .setNegativeButton("取消", null)
                            .show();
                })
                .setNegativeButton("取消", null)
                .show();
    }
}