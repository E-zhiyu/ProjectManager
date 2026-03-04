package com.manager.assistant.ui.pages.bookkeeping.running_account;

import android.content.Intent;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.manager.assistant.R;
import com.manager.assistant.data.data_class.Picture;
import com.manager.assistant.data.data_class.running_account.RunningAccountBase;
import com.manager.assistant.databinding.ActivityRunningAccountModifyBinding;
import com.manager.assistant.generic_enums.DirectoryPaths;
import com.manager.assistant.generic_enums.RequestResultCode;
import com.manager.assistant.generic_enums.TagString;
import com.manager.assistant.helpers.appearence.AnimationHelper;
import com.manager.assistant.helpers.ExceptionHelper;
import com.manager.assistant.generic_enums.KeyValueStrings;
import com.manager.assistant.ui.data_sync.account_picture.AccountPictureViewModel;
import com.manager.assistant.ui.pages.bookkeeping.running_account.fragments.ExpenseFragment;
import com.manager.assistant.ui.pages.bookkeeping.running_account.fragments.RunningAccountFragmentBase;
import com.manager.assistant.ui.pages.bookkeeping.running_account.fragments.RunningAccountType;
import com.manager.assistant.ui.pages.bookkeeping.running_account.fragments.IncomeFragment;
import com.manager.assistant.ui.pages.bookkeeping.running_account.fragments.TransferFragment;
import com.manager.assistant.ui.pages.picture.PictureAdapter;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class RunningAccountModifyActivity extends AppCompatActivity {
    private RunningAccountType type = null;                         //流水种类
    private long rno;                                               //流水编号
    private ActivityRunningAccountModifyBinding binding;            //绑定的XML视图引用

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityRunningAccountModifyBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        receiveInitData();
        initViews();
        AnimationHelper.setupAllChildMorphAnimation(binding.getRoot());

        //只在第一次创建界面时创建新Fragment
        if (savedInstanceState == null) {
            //创建流水编辑Fragment实例
            RunningAccountFragmentBase<?> runningAccountFragment;
            if (type == RunningAccountType.EXPENSE) {
                runningAccountFragment = new ExpenseFragment();
            } else if (type == RunningAccountType.INCOME) {
                runningAccountFragment = new IncomeFragment();
            } else if (type == RunningAccountType.TRANSFER) {
                runningAccountFragment = new TransferFragment();
            } else {
                NullPointerException e = new NullPointerException("无法创建有效的流水数据Fragment");
                ExceptionHelper.showExceptionDialog(this, e);
                return;
            }

            //将Fragment添加至布局
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.add(R.id.fragment_container, runningAccountFragment, TagString.ACCOUNT_FRAGMENT.getValue());
            transaction.commit();
        }

        //设置返回监听器
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                try {
                    RunningAccountFragmentBase<?> fragment = getAccountFragment(TagString.ACCOUNT_FRAGMENT.getValue());
                    if (fragment == null) {
                        setEnabled(false);
                        finish();
                        return;
                    }
                    PictureAdapter pictureAdapter = fragment.getPictureAdapter();
                    if (pictureAdapter.isDeleteMode()) {
                        //使用ViewModel通知所有适配器更新状态
                        AccountPictureViewModel viewModel = new ViewModelProvider(RunningAccountModifyActivity.this).get(AccountPictureViewModel.class);
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
     * 接收初始化数据
     */
    private void receiveInitData() {
        Bundle dataBundle = getIntent().getExtras();
        if (dataBundle != null) {
            type = RunningAccountType.valueOf(dataBundle.getString(KeyValueStrings.ACCOUNT_TYPE.getValue()));
            rno = dataBundle.getLong(KeyValueStrings.ACCOUNT_NO.getValue());
        } else {
            NullPointerException e = new NullPointerException("编辑流水时无法读取原有的数据");
            ExceptionHelper.showExceptionDialog(this, e);
        }
    }

    /**
     * 初始化视图
     */
    private void initViews() {
        binding.toolbar.setNavigationOnClickListener(v -> finish());
        binding.toolbar.setTitle(String.format(Locale.getDefault(), "%s编辑", type.getTitle()));

        //为按钮设置单击监听器
        binding.cancelBtn.setOnClickListener(v -> finish());
        binding.finishBtn.setOnClickListener(v -> {
            Intent result2BookKeeping = new Intent();
            String error;
            RunningAccountFragmentBase<?> runningAccountFragment = getAccountFragment(TagString.ACCOUNT_FRAGMENT.getValue());
            if (runningAccountFragment == null) {
                return;
            }
            error = runningAccountFragment.verifyInputData();

            //判断是否获取到报错消息（null:无报错，验证通过）
            if (error != null) {
                Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
            } else {
                Bundle dataBundle = getInputData();
                if (dataBundle == null) return;
                result2BookKeeping.putExtras(dataBundle);

                //将数据保存至数据库
                try {
                    RunningAccountBase.modifyAccount(dataBundle, this);
                } catch (SQLiteException e) {
                    ExceptionHelper.showExceptionDialog(this, e);
                    Toast.makeText(this, "修改流水数据失败", Toast.LENGTH_SHORT).show();
                    return;
                }

                //移动临时图片文件夹中的图片
                moveTempPictures(rno);

                setResult(RequestResultCode.RESULT_OK.ordinal(), result2BookKeeping);
                finish();
            }
        });

        binding.deleteBtn.setOnClickListener(v -> {
            Intent result2BookKeeping = new Intent();
            new MaterialAlertDialogBuilder(this)
                    .setTitle("删除流水记录")
                    .setMessage("此流水记录将会被永久删除，确认继续吗？")
                    .setPositiveButton("确认", (dialog, which) -> {
                        Bundle dataBundle = new Bundle();
                        dataBundle.putLong(KeyValueStrings.RNO.getValue(), rno);
                        result2BookKeeping.putExtras(dataBundle);
                        setResult(RequestResultCode.RESULT_DELETE.ordinal(), result2BookKeeping);
                        finish();
                    })
                    .setNegativeButton("取消", (dialog, which) -> dialog.dismiss())
                    .show();
        });
    }

    /**
     * 获取修改后的数据
     *
     * @return 包含修改后数据的包裹
     */
    @Nullable
    private Bundle getInputData() {
        RunningAccountFragmentBase<?> runningAccountFragment = getAccountFragment(TagString.ACCOUNT_FRAGMENT.getValue());
        if (runningAccountFragment == null) {
            return null;
        }
        Bundle dataBundle = runningAccountFragment.getInputData();

        dataBundle.putString(KeyValueStrings.ACCOUNT_TYPE.getValue(), type.toString());     //流水种类
        dataBundle.putLong(KeyValueStrings.ACCOUNT_NO.getValue(), rno);                     //流水编号

        return dataBundle;
    }

    /**
     * 将临时图片移动至永久目录
     *
     * @param rno 图片对应的流水编号
     */
    private void moveTempPictures(long rno) {
        //获取文件夹路径
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
            Picture.addPicture(this, filesOnMovedList, rno);
        } catch (SQLiteException e) {
            Toast.makeText(this, "将图片保存至数据库失败", Toast.LENGTH_SHORT).show();
            ExceptionHelper.showExceptionDialog(this, e);
        }
    }

    /**
     * 获取流水记录输入Fragment
     *
     * @param tag 目标Fragment的tag
     * @return 用于输入流水记录的Fragment
     */
    @Nullable
    private RunningAccountFragmentBase<?> getAccountFragment(String tag) {
        return (RunningAccountFragmentBase<?>) getSupportFragmentManager().findFragmentByTag(tag);
    }
}