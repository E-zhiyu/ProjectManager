package com.manager.assistant.ui.pages.bookkeeping.running_account.fragments;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewbinding.ViewBinding;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.DateValidatorPointBackward;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.loadingindicator.LoadingIndicator;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;
import com.manager.assistant.data.data_class.Picture;
import com.manager.assistant.data.data_class.Tag;
import com.manager.assistant.enums.DirectoryPaths;
import com.manager.assistant.enums.KeyValueStrings;
import com.manager.assistant.enums.LogTags;
import com.manager.assistant.enums.TagString;
import com.manager.assistant.helpers.AnimationHelper;
import com.manager.assistant.helpers.ExceptionHelper;
import com.manager.assistant.ui.data_communication.account_picture.AccountPictureViewModel;
import com.manager.assistant.ui.data_communication.tag_modify.TagRepository;
import com.manager.assistant.ui.data_communication.tag_modify.TagUpdateReason;
import com.manager.assistant.ui.others.bottom_sheets.picture.AddPictureOptionBottomSheet;
import com.manager.assistant.ui.others.bottom_sheets.tag.GridSpacingItemDecoration;
import com.manager.assistant.ui.others.bottom_sheets.tag.TagSelectBottomSheet;
import com.manager.assistant.ui.others.dialogs.ProgressDialog;
import com.manager.assistant.ui.pages.picture.PictureAdapter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

/**
 * 流水记录输入界面基类
 *
 * @param <B> 流水记录输入界面的ViewBinding类型
 */
public abstract class RunningAccountFragmentBase<B extends ViewBinding> extends Fragment {
    protected B binding;                                        //绑定的XML视图
    protected String defaultRemark;                             //默认备注
    protected RunningAccountType type;                          //流水类型
    protected TextInputLayout tagLayout;                        //标签文本框布局管理器
    protected MaterialAutoCompleteTextView tagInput;            //标签输入框
    protected MaterialAutoCompleteTextView datetimeInput;       //日期和时间输入
    protected LoadingIndicator loadingIndicator;                //图片加载组件
    protected RecyclerView pictureRecycler;                     //图片列表视图
    protected MaterialButton pictureDeleteBtn;                  //图片删除按钮
    protected long tno = 0;                                     //用户选择的标签编号（默认无标签则为0）
    protected long rno = 0;                                     //流水编号
    protected TagSelectBottomSheet tagSheet;                    //标签选择弹窗
    protected ActivityResultLauncher<Intent> cameraLauncher;    //拍照Activity启动器
    protected ActivityResultLauncher<String> albumLauncher;     //相册图片选择启动器
    protected PictureAdapter pictureAdapter;                    //图片RecyclerView的适配器
    protected final CompositeDisposable disposables = new CompositeDisposable();    //多线程任务列表
    protected boolean viewModelRefreshPictureEnabled = true;    //是否能够通过ViewModel刷新图片视图
    protected boolean viewModelUpdateAdapterStatEnabled = true; //是否允许ViewModel更新图片适配器删除模式状态

    /**
     * 流水记录输入界面基类构造方法
     *
     * @param type          流水种类
     * @param defaultRemark 默认备注
     */
    public RunningAccountFragmentBase(RunningAccountType type, String defaultRemark) {
        this.type = type;
        this.defaultRemark = defaultRemark;
    }

    public String getName() {
        return type.getTitle();
    }

    public long getTno() {
        return tno;
    }

    public PictureAdapter getPictureAdapter() {
        return pictureAdapter;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = getViewBinding(inflater, container);

        initViews();
        AnimationHelper.setupAllChildMorphAnimation((ViewGroup) binding.getRoot());
        initLaunchers();

        //传递完初始化数据后设置RecyclerVIew的适配器
        setupPictureRecyclerAdapter();

        startObserveTag();
        startObservePicture();

        return binding.getRoot();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        disposables.dispose();
    }

    /**
     * 获取ViewBinding
     *
     * @param inflater  布局填充器
     * @param container 父容器
     * @return ViewBinding实例
     */
    protected abstract B getViewBinding(@NonNull LayoutInflater inflater, ViewGroup container);

    /**
     * 初始化视图
     */
    abstract protected void initViews();

    /**
     * 标签选中弹窗的标签按钮点击回调
     *
     * @param tag_no   点击的标签编号
     * @param tag_name 点击的标签名称
     */
    public void onTagBtnClicked(long tag_no, String tag_name) {
        this.tno = tag_no;   //更新全局变量中的标签编号
        tagInput.setText(tag_name);
        tagLayout.setError(null);
        tagSheet.dismiss();
    }

    /**
     * 初始化启动器
     */
    private void initLaunchers() {
        cameraLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    Intent data = result.getData();
                    int resultCode = result.getResultCode();

                    if (resultCode == Activity.RESULT_OK && data != null) {
                        onCameraPictureUriReceived(data);
                    }
                }
        );

        albumLauncher = registerForActivityResult(
                new ActivityResultContracts.GetMultipleContents(),
                this::onAlbumPictureUrisReceived
        );
    }

    /**
     * 观察标签数据的变化
     */
    private void startObserveTag() {
        TagRepository repository = TagRepository.getInstance();
        repository.getChangedTagList().observe(getViewLifecycleOwner(), tagList -> {
            if (tagList != null) {
                TagUpdateReason updateReason = repository.getUpdateReason();
                for (Tag tag : tagList) {
                    String tag_name = tag.getName();
                    long tag_no = tag.getTno();

                    if (tag_no == this.tno) {    //只有找到匹配的标签编号才修改
                        switch (updateReason) {
                            case RENAME:
                                tagInput.setText(tag_name);
                                break;
                            case DELETE:
                                this.tno = 0;
                                tagInput.setText("");
                                break;
                            case MERGE:
                                this.tno = Tag.nameTransToTno(tag_name, requireContext());
                                tagInput.setText(tag_name);
                                break;
                        }
                    }
                }
            }
        });
    }

    /**
     * 观察来自其他Fragment的图片变化
     */
    private void startObservePicture() {
        AccountPictureViewModel viewModel = new ViewModelProvider(requireActivity()).get(AccountPictureViewModel.class);

        //观察适配器删除模式状态
        viewModel.getAdapterStatData().observe(
                getViewLifecycleOwner(), isDeleteMode -> pictureAdapter.switchDeleteMode(isDeleteMode)
        );

        //观察新增图片
        viewModel.getNewPictureData().observe(
                getViewLifecycleOwner(), pictureList -> pictureAdapter.addPicture(pictureList)
        );

        //观察图片删除
        viewModel.getPictureSelectData().observe(
                getViewLifecycleOwner(), pictureSelectList -> pictureAdapter.deleteSelectedPicture(pictureSelectList)
        );
    }

    /**
     * 验证输入内容
     *
     * @return 错误提示(无错误为null)
     */
    abstract public String verifyInputData();

    /**
     * 编辑流水时初始化控件内容的方法
     */
    abstract protected void receiveInitData();

    /**
     * 获取输入的数据
     *
     * @return 包含输入数据的Bundle
     */
    abstract public Bundle getInputData();

    /**
     * 弹出日期和时间选择框
     */
    protected void showMaterialDateTimePicker() {
        //创建日期选择器
        MaterialDatePicker.Builder<Long> dateBuilder = MaterialDatePicker.Builder.datePicker();
        dateBuilder.setTitleText("选择日期");

        //初始化日期格式化器
        String input_datetime = String.valueOf(datetimeInput.getText());
        String pattern = "yyyy-MM-dd HH:mm";    //日期字符串格式
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
        LocalDateTime localDateTime = LocalDateTime.parse(input_datetime, formatter);

        // 转换为 java.util.Date
        long epochMillis = localDateTime.atZone(java.time.ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli();
        Date date = new Date(epochMillis);

        Calendar initialCalendar = Calendar.getInstance();
        initialCalendar.setTime(date);

        //显示日期选择器
        MaterialDatePicker<Long> datePicker = dateBuilder
                .setSelection(initialCalendar.getTimeInMillis())    //默认选中输入的日期
                .setCalendarConstraints(
                        new CalendarConstraints.Builder()
                                .setValidator(DateValidatorPointBackward.now()) //限制为过去日期
                                .build()
                )
                .build();
        datePicker.show(getParentFragmentManager(), TagString.DATE_PICKER.getValue());

        datePicker.addOnPositiveButtonClickListener(selection -> {
            Calendar selected_calendar = Calendar.getInstance();
            selected_calendar.setTimeInMillis(selection);

            //选择日期后，再弹出时间选择器
            showMaterialTimePicker(selected_calendar, initialCalendar);
        });
    }

    /**
     * 显示时间选择对话框
     *
     * @param selectionCalendar 包含选择日期信息的日历对象
     * @param initialCalendar   初始化用的日历对象
     */
    protected void showMaterialTimePicker(@NonNull Calendar selectionCalendar, @NonNull Calendar initialCalendar) {
        //创建时间选择器
        MaterialTimePicker.Builder timeBuilder = new MaterialTimePicker.Builder();
        timeBuilder.setTimeFormat(TimeFormat.CLOCK_24H); // 24小时制
        int init_hour = initialCalendar.get(Calendar.HOUR_OF_DAY);
        timeBuilder.setHour(init_hour);
        int init_minute = initialCalendar.get(Calendar.MINUTE);
        timeBuilder.setMinute(init_minute);
        timeBuilder.setInputMode(MaterialTimePicker.INPUT_MODE_CLOCK);  //默认使用时钟输入模式而不是键盘
        timeBuilder.setTitleText("选择时间");

        //显示时间选择器
        MaterialTimePicker timePicker = timeBuilder.build();
        timePicker.show(getParentFragmentManager(), TagString.TIME_PICKER.getValue());

        //监听选择结果
        timePicker.addOnPositiveButtonClickListener(view -> {
            int hour = timePicker.getHour();
            int minute = timePicker.getMinute();

            //组合日期和时间
            selectionCalendar.set(Calendar.HOUR_OF_DAY, hour);
            selectionCalendar.set(Calendar.MINUTE, minute);

            //修改文本框的日期和时间
            String datetime_str = String.format(
                    Locale.getDefault(),
                    "%04d-%02d-%02d %02d:%02d",
                    selectionCalendar.get(Calendar.YEAR),
                    selectionCalendar.get(Calendar.MONTH) + 1,
                    selectionCalendar.get(Calendar.DAY_OF_MONTH),
                    selectionCalendar.get(Calendar.HOUR_OF_DAY),
                    selectionCalendar.get(Calendar.MINUTE));
            datetimeInput.setText(datetime_str);
        });
    }

    /**
     * 标签文本框点击回调
     */
    protected void showTagSelectSheet() {
        tagSheet = new TagSelectBottomSheet(this::onTagBtnClicked, type);
        tagSheet.show(getParentFragmentManager(), TagString.TAG_SELECT_SHEET.getValue());
    }

    /**
     * 设置RecyclerView的适配器
     */
    private void setupPictureRecyclerAdapter() {
        //获取RecyclerView实例
        int spanCount = 3;
        pictureRecycler.setLayoutManager(new GridLayoutManager(requireContext(), spanCount));
        int spacing = 16; // 单位：像素
        pictureRecycler.addItemDecoration(new GridSpacingItemDecoration(spanCount, spacing, true));

        //先设置适配器
        pictureAdapter = new PictureAdapter(requireActivity(), requireActivity(), isDeleteMode -> {
            if (isDeleteMode) {
                pictureDeleteBtn.setVisibility(View.VISIBLE);
            } else {
                pictureDeleteBtn.setVisibility(View.GONE);
            }
        });
        pictureRecycler.setAdapter(pictureAdapter);

        //多线程刷新图片防止阻塞
        if (rno != 0) {
            disposables.add(
                    Observable.fromCallable(() -> Picture.loadPicturesByRno(requireContext(), rno))
                            .subscribeOn(Schedulers.newThread())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(pictureList -> pictureAdapter.refreshPicture(pictureList),
                                    e -> ExceptionHelper.showExceptionDialog(requireContext(), e),
                                    () -> loadingIndicator.setVisibility(View.GONE)
                            )
            );
        } else {
            loadingIndicator.setVisibility(View.GONE);
        }
    }

    /**
     * 添加图片按钮点击回调
     */
    protected void showAddPictureBottomSheet() {
        AddPictureOptionBottomSheet sheet = new AddPictureOptionBottomSheet(
                requireContext(),
                cameraLauncher,
                albumLauncher
        );
        sheet.show(getParentFragmentManager(), TagString.PICTURE_ADD_SHEET.getValue());
    }

    /**
     * 处理拍照后
     *
     * @param intent 包含图片Uri的Intent
     */
    private void onCameraPictureUriReceived(@NonNull Intent intent) {
        String uriStr = intent.getStringExtra(KeyValueStrings.FILE_URI.getValue());
        if (uriStr == null) return;

        Uri pictureUri = Uri.parse(uriStr);
        Picture newPicture = new Picture(pictureUri, rno);

        AccountPictureViewModel viewModel = new ViewModelProvider(requireActivity()).get(AccountPictureViewModel.class);
        viewModel.addPicture(newPicture);
    }

    /**
     * 处理相册选择图片的Uri
     *
     * @param uriList 包含选择图片的Uri的列表
     */
    private void onAlbumPictureUrisReceived(@NonNull List<Uri> uriList) {
        if (uriList.isEmpty()) return;  //若Uri列表为空说明用户取消添加

        //创建临时文件夹
        File tempPictureDir = DirectoryPaths.PICTURE_TEMP.getDir(requireContext());
        if (tempPictureDir == null) {
            Toast.makeText(requireContext(), "图片添加失败：无法创建临时目录", Toast.LENGTH_SHORT).show();
            Log.e(LogTags.ACCOUNT_FRAGMENT.getV(), "图片添加失败：无法创建临时目录");
            return;
        }

        ProgressDialog processDialog = new ProgressDialog(requireContext(), "复制图片", "正在复制图片……");
        processDialog.buildDialog(
                null,
                () -> {
                    //用户点击取消
                    disposables.clear();
                    Toast.makeText(requireContext(), "已取消添加图片", Toast.LENGTH_SHORT).show();
                },
                false);
        processDialog.show();

        //在IO线程完成文件复制并在主线程刷新UI
        disposables.add(
                Observable.fromCallable(() -> {
                            List<File> copiedFIleList = new ArrayList<>();
                            boolean switchedOffIndeterminate = false;   //标记是否切换到确定模式
                            for (int i = 0; i < uriList.size(); i++) {
                                Uri pictureUri = uriList.get(i);

                                //复制单个图片
                                File copiedFile = copySinglePicture(pictureUri, i, tempPictureDir);
                                if (copiedFile != null && copiedFile.exists()) {
                                    copiedFIleList.add(copiedFile);

                                    //如果是第一张图片，切换到确定模式
                                    if (!switchedOffIndeterminate) {
                                        switchedOffIndeterminate = true;
                                        new Handler(Looper.getMainLooper()).post(() ->
                                                processDialog.setIndeterminate(false)
                                        );
                                    }

                                    //更新进度
                                    final int current = i + 1;
                                    final String fileName = copiedFile.getName();
                                    new Handler(Looper.getMainLooper()).post(() ->
                                            processDialog.updateProgress(current, uriList.size(), fileName)
                                    );
                                }
                            }

                            //返回复制成功的图片列表
                            return copiedFIleList.stream()
                                    .map(Uri::fromFile)
                                    .map(uri -> new Picture(uri, rno))
                                    .collect(Collectors.toList());
                        })
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeOn(Schedulers.io())
                        .subscribe(
                                pictureList -> {
                                    if (pictureList.isEmpty()) {
                                        return;
                                    }

                                    Toast.makeText(
                                            requireContext(),
                                            String.format(
                                                    Locale.getDefault(),
                                                    "已添加%d张图片",
                                                    pictureList.size()
                                            ), Toast.LENGTH_SHORT
                                    ).show();

                                    AccountPictureViewModel viewModel = new ViewModelProvider(requireActivity()).get(AccountPictureViewModel.class);
                                    viewModel.addPicture(pictureList);
                                },
                                e -> ExceptionHelper.showExceptionDialog(requireContext(), e),
                                processDialog::dismiss
                        )
        );
    }

    /**
     * 复制单个图片
     *
     * @param imageUri  图片Uri
     * @param index     图片的下标(外部调用的循环中的下标)
     * @param targetDir 目标目录
     * @return 复制完成的文件
     */
    @Nullable
    private File copySinglePicture(Uri imageUri, int index, File targetDir) {
        try (InputStream inputStream = requireContext().getContentResolver().openInputStream(imageUri)) {
            if (inputStream == null) return null;

            // 生成唯一的文件名
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss_SSS", Locale.getDefault())
                    .format(new Date());
            String fileName = String.format(Locale.getDefault(), "album_%s_%d.jpg", timeStamp, index);

            // 创建目标文件
            File destinationFile = new File(targetDir, fileName);

            // 复制文件
            try (FileOutputStream outputStream = new FileOutputStream(destinationFile)) {
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
            }

            return destinationFile;
        } catch (Exception e) {
            ExceptionHelper.showExceptionDialog(requireContext(), e);
            return null;
        }
    }
}

