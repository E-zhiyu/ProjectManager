package com.manager.assistant.ui.pages.bookkeeping.running_account;

import android.content.Intent;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.manager.assistant.data.controllers.AccountDataController;
import com.manager.assistant.data.controllers.PictureDataController;
import com.manager.assistant.ui.sync.picture.AccountPictureViewModel;
import com.manager.assistant.ui.others.adapters.FragmentPagerAdapter;
import com.manager.assistant.databinding.ActivityRunningAccountAddBinding;
import com.manager.assistant.generic_enums.DirectoryPaths;
import com.manager.assistant.helpers.appearence.AppearanceAnimationHelper;
import com.manager.assistant.helpers.ExceptionHelper;
import com.manager.assistant.generic_enums.KeyValueStrings;
import com.manager.assistant.ui.pages.bookkeeping.running_account.fragments.ExpenseFragment;
import com.manager.assistant.ui.pages.bookkeeping.running_account.fragments.IncomeFragment;
import com.manager.assistant.ui.pages.bookkeeping.running_account.fragments.RunningAccountFragmentBase;
import com.manager.assistant.ui.pages.bookkeeping.running_account.fragments.TransferFragment;
import com.manager.assistant.generic_enums.RequestResultCode;
import com.manager.assistant.ui.pages.picture.PictureAdapter;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class RunningAccountAddActivity extends AppCompatActivity {
    private ActivityRunningAccountAddBinding binding;   //绑定的XML视图
    private int currentIndex;                           //当前Fragment的下标

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        binding = ActivityRunningAccountAddBinding.inflate(getLayoutInflater());
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

        //设置返回监听器
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                try {
                    RunningAccountFragmentBase<?> fragment = getCurrentFragment();
                    if (fragment == null) {
                        setEnabled(false);
                        finish();
                        return;
                    }
                    PictureAdapter pictureAdapter = fragment.getPictureAdapter();
                    if (pictureAdapter.isDeleteMode()) {
                        //使用ViewModel通知所有适配器更新状态
                        AccountPictureViewModel viewModel = new ViewModelProvider(RunningAccountAddActivity.this).get(AccountPictureViewModel.class);
                        viewModel.updateAdapterStat(false);
                    } else {
                        setEnabled(false);
                        finish();
                    }
                } catch (NumberFormatException e) {
                    setEnabled(false);
                    finish();
                }
            }
        });
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
        MaterialToolbar toolbar = binding.toolbar;
        toolbar.setNavigationOnClickListener(v -> finish());

        //为完成按钮绑定单击监听器
        binding.finishBtn.setOnClickListener(v -> {
            RunningAccountFragmentBase<?> currentFragment = getCurrentFragment();
            if (currentFragment == null) return;
            String error = currentFragment.verifyInputData();

            //判断是否获取到警告消息（null:无警告，验证通过）
            if (error != null) {
                Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
            } else {
                onFinishBtnClicked();
            }
        });
        binding.cancelBtn.setOnClickListener(v -> finish());

        //创建碎片列表
        List<Fragment> fragmentList = new ArrayList<>();
        fragmentList.add(new ExpenseFragment());
        fragmentList.add(new IncomeFragment());
        fragmentList.add(new TransferFragment());

        //初始化ViewPager并设置ViewPager适配器
        ViewPager2 viewPager2 = binding.viewPager2;  //翻页视图
        FragmentPagerAdapter viewPagerAdapter = new FragmentPagerAdapter(this, fragmentList);
        viewPager2.setAdapter(viewPagerAdapter);

        //绑定ViewPager和TabLayout
        TabLayout tabLayout = binding.runningAccountAddTabLayout;
        new TabLayoutMediator(
                tabLayout,
                viewPager2,
                (tab, position) -> tab.setText(((RunningAccountFragmentBase<?>) fragmentList.get(position)).getName())
        ).attach();

        //定义翻页回调以刷新活动的fragment实例
        viewPager2.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                currentIndex = position;
            }
        });
        viewPager2.setOffscreenPageLimit(2);    //设置保留邻近Fragment数量
    }

    /**
     * 完成流水新建
     */
    private void onFinishBtnClicked() {
        Intent result2BookKeeping = new Intent();
        RunningAccountFragmentBase<?> currentFragment = getCurrentFragment();
        if (currentFragment == null) return;
        Bundle dataBundle = currentFragment.getInputData();    //获取输入的信息并打包

        //将流水保存至数据库
        long rno;
        try {
            rno = AccountDataController.saveNewAccount(dataBundle, this);
            dataBundle.putLong(KeyValueStrings.RNO.getValue(), rno);
            moveTempPictures(rno);
        } catch (SQLiteException e) {
            ExceptionHelper.showExceptionDialog(this, e);
            Toast.makeText(this, "添加流水记录时出错", Toast.LENGTH_SHORT).show();
        }

        result2BookKeeping.putExtras(dataBundle);
        setResult(RequestResultCode.RESULT_OK.ordinal(), result2BookKeeping);
        finish();
    }

    /**
     * 将临时图片移动至永久目录
     *
     * @param rno 图片对应的流水编号
     */
    private void moveTempPictures(long rno) {
        //获取文件目录
        File tempPictureDir = DirectoryPaths.PICTURE_TEMP.getDir(this);
        File permanentPictureDir = DirectoryPaths.PICTURE.getDir(this);

        //移动文件
        List<File> filesOnMovedList = new ArrayList<>();    //成功移动的文件列表
        if (tempPictureDir != null && permanentPictureDir != null) {
            File[] files = tempPictureDir.listFiles();
            if (files != null) {
                boolean isAllFileMoved = true;
                for (File pictureFile : files) {
                    File permanentPicture = new File(permanentPictureDir, pictureFile.getName());
                    if (!pictureFile.renameTo(permanentPicture)) {
                        isAllFileMoved = false;
                    } else {
                        filesOnMovedList.add(permanentPicture);
                    }
                }

                if (!isAllFileMoved) {
                    Toast.makeText(this, "临时图片移动失败", Toast.LENGTH_SHORT).show();
                }
            }
        }

        //将移动后的文件路径保存至数据库
        try {
            PictureDataController.addPicture(this, filesOnMovedList, rno);
        } catch (SQLiteException e) {
            Toast.makeText(this, "将图片保存至数据库失败", Toast.LENGTH_SHORT).show();
            ExceptionHelper.showExceptionDialog(this, e);
        }
    }

    @Nullable
    private RunningAccountFragmentBase<?> getCurrentFragment() {
        String tag = "f" + currentIndex;
        return (RunningAccountFragmentBase<?>) getSupportFragmentManager().findFragmentByTag(tag);
    }
}