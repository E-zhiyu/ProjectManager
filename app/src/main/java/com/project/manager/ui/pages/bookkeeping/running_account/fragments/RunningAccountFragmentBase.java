package com.project.manager.ui.pages.bookkeeping.running_account.fragments;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.database.sqlite.SQLiteException;
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
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.DateValidatorPointBackward;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;
import com.project.manager.enums.DirectoryPaths;
import com.project.manager.enums.LogTags;
import com.project.manager.R;
import com.project.manager.data.data_class.Picture;
import com.project.manager.helpers.ExceptionHelper;
import com.project.manager.ui.others.dialogs.ProgressDialog;
import com.project.manager.ui.others.bottom_sheets.picture.AddPictureOptionBottomSheet;
import com.project.manager.enums.KeyValueStrings;
import com.project.manager.enums.TagString;
import com.project.manager.ui.data_communication.tag_modify.TagUpdateReason;
import com.project.manager.ui.data_communication.tag_modify.TagRepository;
import com.project.manager.data.data_class.Tag;
import com.project.manager.ui.others.bottom_sheets.tag.GridSpacingItemDecoration;
import com.project.manager.ui.others.bottom_sheets.tag.TagSelectBottomSheet;
import com.project.manager.ui.pages.picture.PictureAdapter;

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

public abstract class RunningAccountFragmentBase extends Fragment implements View.OnFocusChangeListener {
    protected Bundle initData = null;                       //初始化控件内容的数据（用于编辑流水记录时）
    protected View contentView;                             //绑定的XML界面
    protected String defaultRemark;                         //默认备注
    protected RunningAccountType type;                      //流水类型
    protected TextInputLayout amount_layout, tag_layout;    //金额和标签文本框布局管理器
    protected TextInputEditText amount_input, tag_input;    //金额和标签文本输入框
    private long tag_no = 0;                                //用户选择的标签编号（默认无标签则为0）
    private long rno = 0;                                   //流水编号
    private TagSelectBottomSheet tag_sheet;                 //底部弹出窗口
    private ActivityResultLauncher<Intent> cameraLauncher;  //拍照Activity启动器
    private ActivityResultLauncher<String> albumLauncher;   //相册图片选择启动器
    private PictureAdapter pictureAdapter;                  //图片RecyclerView的适配器
    private MaterialButton pictureDeleteBtn;                //删除选中的图片的按钮
    private final CompositeDisposable disposables = new CompositeDisposable();  //多线程任务列表

    public RunningAccountFragmentBase() {
        setDefaultRemark();
    }

    /**
     * 获取图片适配器
     *
     * @return 图片适配器
     */
    public PictureAdapter getPictureAdapter() {
        return pictureAdapter;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        contentView = inflater.inflate(getLayoutResId(), container, false);
        initViews();
        initLaunchers();

        //判断是否传递了外部数据，如果传递了则将数据填入对应控件
        if (initData != null) {
            initViewsWhenModifying(initData);
        }

        //传递完初始化数据后设置RecyclerVIew的适配器
        int spanCount = 3;
        RecyclerView pictureRecycler = contentView.findViewById(R.id.picture_recycler);
        pictureRecycler.setLayoutManager(new GridLayoutManager(requireContext(), spanCount));
        int spacing = 16; // 单位：像素
        pictureRecycler.addItemDecoration(new GridSpacingItemDecoration(spanCount, spacing, true));
        setupRecyclerAdapter(pictureRecycler);

        startObserveTag();

        return contentView;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        disposables.dispose();

        //删除临时图片目录文件
        File tempPictureDir = DirectoryPaths.PICTURE_TEMP.getDir(requireContext());
        if (tempPictureDir != null) {
            File[] files = tempPictureDir.listFiles();
            if (files != null) {
                boolean isAllTempFileDeleted = true;
                for (File tempPicture : files) {
                    if (!tempPicture.delete()) {
                        isAllTempFileDeleted = false;
                    }
                }

                if (!isAllTempFileDeleted) {
                    Log.w(LogTags.ACCOUNT_FRAGMENT.getV(), "临时图片删除失败");
                }
            }
        }
    }


    //修改流水时接收初始化数据
    public void receiveInitData(Bundle initData) {
        this.initData = initData;
    }

    /**
     * 获取碎片名称供TabLayout使用
     *
     * @return 碎片名称
     */
    public String getName() {
        return type.getTitle();
    }

    /**
     * 子类设置默认备注的方法
     */
    protected abstract void setDefaultRemark();

    protected abstract int getLayoutResId();

    public long getTag_no() {
        return tag_no;
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        if (!hasFocus) {
            String edittext_str, error;         //文本框内容和错误提示
            TextInputLayout text_edit_layout;   //被验证的文本框对应的布局管理器
            edittext_str = String.valueOf(((TextInputEditText) v).getText());   //获取待验证组件的文本内容
            if (v.getId() == R.id.amount_input) {
                text_edit_layout = amount_layout;
            } else {
                return;
            }

            if (edittext_str.isEmpty()) {
                error = "金额不能为空";
                text_edit_layout.setErrorEnabled(true);
                text_edit_layout.setError(error);
            } else {
                text_edit_layout.setError(null);    //消除错误提示
                text_edit_layout.setErrorEnabled(false);
            }
        } else {
            if (v.getId() == R.id.amount_input) {
                amount_layout.setError(null);
                amount_layout.setErrorEnabled(false);
            }
        }
    }

    /**
     * 初始化视图
     */
    protected void initViews() {
        TextInputEditText dt_input = contentView.findViewById(R.id.datetime_input);
        amount_layout = contentView.findViewById(R.id.amount_layout);
        amount_input = contentView.findViewById(R.id.amount_input);
        tag_layout = contentView.findViewById(R.id.running_account_tag_layout);
        tag_input = contentView.findViewById(R.id.running_account_tag_input);

        amount_input.setOnFocusChangeListener(this);
        dt_input.setOnClickListener(v -> showMaterialDateTimePicker());
        tag_input.setOnClickListener(v -> showTagSelectSheet());

        //初始化日期内容
        Calendar calendar = Calendar.getInstance();
        @SuppressLint("DefaultLocale") String dt_string = String.format("%04d-%02d-%02d %02d:%02d",
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH) + 1,
                calendar.get(Calendar.DAY_OF_MONTH),
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE));
        dt_input.setText(dt_string);

        //添加图片的按钮
        MaterialButton pictureAddBtn = contentView.findViewById(R.id.picture_add);
        pictureAddBtn.setOnClickListener(v -> showAddPictureBottomSheet());

        //删除图片按钮
        pictureDeleteBtn = contentView.findViewById(R.id.picture_delete_btn);
        pictureDeleteBtn.setOnClickListener(v -> new MaterialAlertDialogBuilder(requireContext())
                .setTitle("删除图片")
                .setMessage("是否删除选中的图片？此操作会立刻执行并且无法撤回！")
                .setPositiveButton(
                        "确定",
                        (dialog, which) -> pictureAdapter.deleteSelectedPicture()
                )
                .setNegativeButton("取消", null)
                .show());
    }

    public void onTagBtnClicked(long tag_no, String tag_name) {
        this.tag_no = tag_no;   //更新全局变量中的标签编号

        tag_input.setText(tag_name);
        tag_layout.setErrorEnabled(false);  //去除错误提示
        tag_layout.setError(null);
        tag_sheet.dismiss();
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

    //观察标签数据变化
    private void startObserveTag() {
        TagRepository repository = TagRepository.getInstance();
        repository.getChangedTagList().observe(getViewLifecycleOwner(), tagList -> {
            if (tagList != null) {
                TagUpdateReason updateReason = repository.getUpdateReason();
                for (Tag tag : tagList) {
                    String tag_name = tag.getName();
                    long tag_no = tag.getTno();

                    if (tag_no == this.tag_no) {    //只有找到匹配的标签编号才修改
                        switch (updateReason) {
                            case RENAME:
                                tag_input.setText(tag_name);
                                break;
                            case DELETE:
                                this.tag_no = 0;
                                tag_input.setText("");
                                break;
                            case MERGE:
                                this.tag_no = Tag.nameTransToTno(tag_name, requireContext());
                                tag_input.setText(tag_name);
                                break;
                        }
                    }
                }
            }
        });
    }

    /**
     * 验证输入内容
     *
     * @return 错误提示(无错误为null)
     */
    public String verifyInputData() {
        String error = null;

        //判断是否输入金额
        String amountStr = String.valueOf(amount_input.getText());
        if (amountStr.isEmpty()) {
            error = "金额不能为空";
            amount_layout.setErrorEnabled(true);
            amount_layout.setError(error);
        } else if (Double.parseDouble(amountStr) == 0) {
            error = "金额不能为0";
            amount_layout.setErrorEnabled(true);
            amount_layout.setError(error);
        }

        return error;
    }

    /**
     * 编辑流水时初始化控件内容的方法
     *
     * @param dataBundle 包含初始信息的包裹
     */
    public void initViewsWhenModifying(@NonNull Bundle dataBundle) {
        double amount = dataBundle.getDouble(KeyValueStrings.ACCOUNT_AMOUNT.getValue(), -1);
        String remark = dataBundle.getString(KeyValueStrings.ACCOUNT_REMARK.getValue());
        boolean isDefaultRemark = dataBundle.getBoolean(KeyValueStrings.ACCOUNT_IS_DEFAULT_REMARK.getValue());
        String date_time = dataBundle.getString(KeyValueStrings.ACCOUNT_DATETIME.getValue());
        rno = dataBundle.getLong(KeyValueStrings.ACCOUNT_NO.getValue());

        String tag_name = "";
        try {
            Tag tag = Tag.getTagOfRunningAccount(rno, requireContext());
            tag_no = tag.getTno();
            tag_name = tag.getName();
        } catch (SQLiteException e) {
            ExceptionHelper.showExceptionDialog(requireContext(), e);
            Toast.makeText(requireContext(), "无法加载该流水记录的标签信息", Toast.LENGTH_SHORT).show();
        }

        amount_input.setText(String.valueOf(amount));                                   //金额
        TextInputEditText remark_input = contentView.findViewById(R.id.remark_input);   //备注
        remark_input.setText(isDefaultRemark ? "" : remark);
        TextInputEditText date_input = contentView.findViewById(R.id.datetime_input);   //日期
        date_input.setText(date_time);
        tag_input.setText(tag_name);                                                    //标签名称
    }

    /**
     * 获取输入的数据
     *
     * @return 包含输入数据的Bundle
     */
    public Bundle getInputData() {
        Bundle dataBundle = new Bundle();

        dataBundle.putString(KeyValueStrings.ACCOUNT_TYPE.getValue(), type.toString());     //种类
        TextInputEditText dateTimeTextView = contentView.findViewById(R.id.datetime_input); //日期和时间
        String date_time = String.valueOf(dateTimeTextView.getText());
        dataBundle.putString(KeyValueStrings.ACCOUNT_DATETIME.getValue(), date_time);
        TextInputEditText remarkEditText = contentView.findViewById(R.id.remark_input);     //备注
        String remark = String.valueOf(remarkEditText.getText());
        boolean isDefaultRemark;                                                            //是否使用默认备注
        if (remark.isEmpty()) {
            isDefaultRemark = true;
            remark = defaultRemark;
        } else {
            isDefaultRemark = false;
        }
        dataBundle.putBoolean(KeyValueStrings.ACCOUNT_IS_DEFAULT_REMARK.getValue(), isDefaultRemark);
        dataBundle.putString(KeyValueStrings.ACCOUNT_REMARK.getValue(), remark);
        double amount = Double.parseDouble(String.valueOf(amount_input.getText()));         //金额
        dataBundle.putDouble(KeyValueStrings.ACCOUNT_AMOUNT.getValue(), amount);
        dataBundle.putLong(KeyValueStrings.TAG_NO.getValue(), tag_no);                      //标签编号

        return dataBundle;
    }

    /**
     * 弹出日期和时间选择框
     */
    protected void showMaterialDateTimePicker() {
        //创建日期选择器
        MaterialDatePicker.Builder<Long> dateBuilder = MaterialDatePicker.Builder.datePicker();
        dateBuilder.setTitleText("选择日期");

        //初始化日期格式化器
        TextInputEditText dateTimeInput = contentView.findViewById(R.id.datetime_input);
        String input_datetime = String.valueOf(dateTimeInput.getText());
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
    private void showMaterialTimePicker(@NonNull Calendar selectionCalendar, @NonNull Calendar initialCalendar) {
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
            TextInputEditText datetime_input = contentView.findViewById(R.id.datetime_input);
            @SuppressLint("DefaultLocale") String datetime_str = String.format("%04d-%02d-%02d %02d:%02d",
                    selectionCalendar.get(Calendar.YEAR),
                    selectionCalendar.get(Calendar.MONTH) + 1,
                    selectionCalendar.get(Calendar.DAY_OF_MONTH),
                    selectionCalendar.get(Calendar.HOUR_OF_DAY),
                    selectionCalendar.get(Calendar.MINUTE));
            datetime_input.setText(datetime_str);
        });
    }

    /**
     * 标签文本框点击回调
     */
    private void showTagSelectSheet() {
        tag_sheet = new TagSelectBottomSheet(this::onTagBtnClicked);
        tag_sheet.show(getParentFragmentManager(), TagString.TAG_SELECT_SHEET.getValue());
    }

    /**
     * 设置RecyclerView的适配器
     *
     * @param recyclerView 需要设置适配器的RecyclerView
     */
    private void setupRecyclerAdapter(RecyclerView recyclerView) {
        //加载图片资源
        List<Picture> pictureList;
        if (rno == 0) {
            pictureList = new ArrayList<>();
        } else {
            try {
                pictureList = Picture.loadPicturesByRno(requireContext(), rno);
            } catch (SQLiteException e) {
                Toast.makeText(requireContext(), "无法加载图片资源", Toast.LENGTH_SHORT).show();
                ExceptionHelper.showExceptionDialog(requireContext(), e);
                pictureList = new ArrayList<>();
            }
        }

        pictureAdapter = new PictureAdapter(requireContext(), pictureList, isDeleteMode -> {
            if (isDeleteMode) {
                pictureDeleteBtn.setVisibility(View.VISIBLE);
            } else {
                pictureDeleteBtn.setVisibility(View.GONE);
            }
        });
        recyclerView.setAdapter(pictureAdapter);
    }

    /**
     * 添加图片按钮点击回调
     */
    private void showAddPictureBottomSheet() {
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
        pictureAdapter.addPicture(newPicture);
    }

    /**
     * 处理相册选择图片的Uri
     *
     * @param uriList 包含选择图片的Uri的列表
     */
    private void onAlbumPictureUrisReceived(@NonNull List<Uri> uriList) {
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
                                    pictureAdapter.addPicture(pictureList);
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

