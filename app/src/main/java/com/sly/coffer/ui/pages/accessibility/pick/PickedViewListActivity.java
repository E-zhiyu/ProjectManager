package com.sly.coffer.ui.pages.accessibility.pick;

import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.sly.coffer.R;
import com.sly.coffer.data.save.db.BookkeepingDb;
import com.sly.coffer.data.save.db.entities.PickedViewEntity;
import com.sly.coffer.data.save.preference.SearchHistoryPreference;
import com.sly.coffer.databinding.ActivityPickedViewListBinding;
import com.sly.coffer.helpers.BackPressedCallbackHelper;
import com.sly.coffer.helpers.ExceptionHelper;
import com.sly.coffer.helpers.PermissionHelper;
import com.sly.coffer.helpers.SearchHelper;
import com.sly.coffer.helpers.appearence.AppearanceHelper;
import com.sly.coffer.helpers.appearence.VisibilityHelper;
import com.sly.coffer.ui.others.dialogs.MarkdownDialogBuilder;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class PickedViewListActivity extends AppCompatActivity {
    private final CompositeDisposable disposable = new CompositeDisposable();
    private ActivityPickedViewListBinding binding;  //绑定的 XML 布局
    private BackPressedCallbackHelper backHelper;   //返回手势拦截器
    private BackPressedCallbackHelper.BackHandler searchBackHandler;    //搜索返回处理器

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPickedViewListBinding.inflate(getLayoutInflater());

        EdgeToEdge.enable(this);
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, 0, systemBars.right, 0);

            //RecyclerView
            binding.recycler.setPadding(systemBars.left, 0, systemBars.right, systemBars.bottom);

            return insets;
        });

        initViews();
        addPermissionRequests();
        observeLiveData();
        initBackHandlers();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        disposable.dispose();
        binding = null;
    }

    /**
     * 初始化视图
     */
    private void initViews() {
        //搜索框
        SearchHelper.initSearchComponents(
                binding.searchBar,
                binding.searchView,
                binding.searchHistoryRecycler,
                binding.clearHistoryBtn,
                SearchHistoryPreference.KEY_PICKED_VIEW,
                keyword -> {
                    PickedViewViewModel viewModel = new ViewModelProvider(this).get(PickedViewViewModel.class);
                    viewModel.executeSearch(keyword.trim());
                },
                item -> {
                    int id = item.getItemId();
                    if (id == R.id.action_help) {
                        final String EXPLANATION = "### 1. 工作原理\n" +
                                "处于视图拾取模式时，本APP会：\n" +
                                "1. 识别点击的屏幕位置；\n" +
                                "2. 获取点击位置对应的视图的信息，并保存至本地数据库。\n" +
                                "\n" +
                                "### 2. 与无障碍规则的关系\n" +
                                "输入无障碍规则时的目标视图时，可以从已保存的视图信息中选择。\n";
                        new MarkdownDialogBuilder(this, "功能说明", EXPLANATION)
                                .setNegativeButton("关闭", null)
                                .show();
                        return true;
                    }

                    return false;
                }
        );

        //Recycler 列表
        PickedViewListAdapter adapter = new PickedViewListAdapter(
                (entity, anchor) -> {
                    //TODO:点击监听
//                    Bundle bundle = new Bundle();
//                    bundle.putLong(KeyStrings.PICKED_VIEW_ID.v(), entity.getId());
//
//                    Intent intent = new Intent(this, null);
//                    intent.putExtras(bundle);
//                    startActivity(intent);
                },
                this::showPopupMenu
        );
        binding.recycler.setAdapter(adapter);
        PickedViewViewModel viewModel = new ViewModelProvider(this).get(PickedViewViewModel.class);
        BookkeepingDb db = BookkeepingDb.getInstance(this);
        disposable.add(viewModel.getPickedViewFlowable(db)
                .subscribe(
                        roleList -> {
                            VisibilityHelper.toggleVisibilityWithFade(binding.emptyText, roleList.isEmpty());

                            adapter.submitList(roleList);
                        },
                        e -> ExceptionHelper.showExceptionDialog(this, e)
                )
        );

        //添加按钮
        binding.addFab.setOnClickListener(view -> {
            if (!PermissionHelper.SpecialPermissionType.ACCESSIBILITY_PICK.isGranted(this)) {
                Toast.makeText(this, "请开启无障碍中的“自动记账-视图拾取”服务", Toast.LENGTH_SHORT).show();
                return;
            }
            //TODO:添加按钮
        });
        AppearanceHelper.attachMorphAnimation(binding.addFab);
        AppearanceHelper.setMarginToNavigation(binding.addFab, this);
    }

    /**
     * 添加权限请求
     */
    private void addPermissionRequests() {
        PermissionHelper helper = new PermissionHelper(this);
        helper.addPermission(
                PermissionHelper.SpecialPermissionType.ACCESSIBILITY_PICK,
                "视图拾取服务",
                "请开启无障碍中的“自动记账-视图拾取”服务，以允许APP获取点击屏幕的位置并保存点击的视图的信息。"
        );
    }

    /**
     * 观察 ViewModel 的 LiveData
     */
    private void observeLiveData() {
        PickedViewViewModel roleListViewModel = new ViewModelProvider(PickedViewListActivity.this).get(PickedViewViewModel.class);
        roleListViewModel.getFilterUpdatedLiveData().observe(this, v ->
                setSearchMode(!roleListViewModel.isNoFilter())
        );
    }

    /**
     * 初始化返回手势拦截器
     */
    private void initBackHandlers() {
        OnBackPressedCallback backPressedCallback = new OnBackPressedCallback(false) {
            @Override
            public void handleOnBackPressed() {
                backHelper.dispatchBackPressed();
            }
        };
        getOnBackPressedDispatcher().addCallback(backPressedCallback);
        backHelper = new BackPressedCallbackHelper(backPressedCallback);

        //搜索
        searchBackHandler = new BackPressedCallbackHelper.BackHandler() {
            @Override
            public boolean handleBack() {
                PickedViewViewModel viewModel = new ViewModelProvider(PickedViewListActivity.this).get(PickedViewViewModel.class);
                viewModel.clearFilter();
                return true;
            }

            @Override
            public int getPriority() {
                return 1;
            }
        };
    }

    /**
     * 显示角色长按菜单
     *
     * @param view   角色实体
     * @param anchor 锚点视图
     */
    private void showPopupMenu(PickedViewEntity view, View anchor) {
        PopupMenu popupMenu = new PopupMenu(this, anchor, Gravity.END);
        popupMenu.getMenuInflater().inflate(R.menu.menu_captured_notification_edit, popupMenu.getMenu());

        //设置监听
        popupMenu.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_delete) {
                new MaterialAlertDialogBuilder(this)
                        .setTitle(R.string.delete_picked_view)
                        .setMessage("即将删除该拾取记录，确认继续吗？")
                        .setPositiveButton("确定", (dialogInterface, i) -> {
                            BookkeepingDb db = BookkeepingDb.getInstance(this);
                            disposable.add(db.accessibilityRuleDao().deletePickedViewCompletable(view)
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribeOn(Schedulers.io())
                                    .subscribe(
                                            () -> Toast.makeText(this, "拾取记录删除成功", Toast.LENGTH_SHORT).show(),
                                            e -> ExceptionHelper.showExceptionDialog(this, e)
                                    )
                            );
                        })
                        .setNegativeButton("取消", null)
                        .show();

                return true;
            }
            return false;
        });

        popupMenu.show();
    }

    /**
     * 设置搜索模式
     *
     * @param isSearchMode 是否为搜索模式
     */
    private void setSearchMode(boolean isSearchMode) {
        if (!isSearchMode) {
            binding.searchBar.setText("");
            backHelper.unregisterHandler(searchBackHandler);
        } else {
            backHelper.registerHandler(searchBackHandler);
        }
    }
}