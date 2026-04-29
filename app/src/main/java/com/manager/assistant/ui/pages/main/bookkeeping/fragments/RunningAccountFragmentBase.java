package com.manager.assistant.ui.pages.main.bookkeeping.fragments;

import android.Manifest;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewbinding.ViewBinding;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.loadingindicator.LoadingIndicator;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputLayout;
import com.manager.assistant.data.classes.Picture;
import com.manager.assistant.data.classes.Tag;
import com.manager.assistant.data.controllers.PictureDataController;
import com.manager.assistant.data.controllers.TagDataController;
import com.manager.assistant.generic_enums.DirectoryPaths;
import com.manager.assistant.generic_enums.LogTags;
import com.manager.assistant.generic_enums.TagString;
import com.manager.assistant.helpers.DateTimePickerHelper;
import com.manager.assistant.helpers.PermissionHelper;
import com.manager.assistant.helpers.appearence.AppearanceAnimationHelper;
import com.manager.assistant.helpers.ExceptionHelper;
import com.manager.assistant.ui.sync.picture.AccountPictureViewModel;
import com.manager.assistant.ui.sync.tag.TagRepository;
import com.manager.assistant.ui.sync.tag.TagUpdateReason;
import com.manager.assistant.ui.others.bottom_sheets.picture.AddPictureOptionBottomSheet;
import com.manager.assistant.ui.others.bottom_sheets.tag.GridSpacingItemDecoration;
import com.manager.assistant.ui.others.bottom_sheets.tag.TagSelectBottomSheet;
import com.manager.assistant.ui.others.dialogs.ProgressDialog;
import com.manager.assistant.ui.pages.picture.PictureAdapter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
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
    protected PictureAdapter pictureAdapter;                    //图片RecyclerView的适配器
    protected final CompositeDisposable disposables = new CompositeDisposable();    //多线程任务列表
    protected boolean viewModelRefreshPictureEnabled = true;    //是否能够通过ViewModel刷新图片视图
    private ActivityResultLauncher<PickVisualMediaRequest> albumLauncher;   //相册图片选择启动器
    private ActivityResultLauncher<Uri> takePictureLauncher;    //调用系统相机的启动器
    protected Uri tempPictureUri;                               //临时图片文件的Uri
    private ActivityResultLauncher<String> permissionLauncher;  //权限申请启动器

    /**
     * 图片复制结果
     */
    static class PictureCopyResult {
        Uri copiedFileUri;      //复制的文件的Uri
        String tipStr;          //复制成功的文件名
        boolean isSuccessful;   //是否复制成功
        int currentCount;       //当前复制的图片在第几个
        int totalCount;         //一共有几个
        Throwable exception;    //复制失败产生的异常

        /**
         * 复制成功构造方法
         *
         * @param copiedFileUri 复制成功的文件的Uri
         * @param currentCount  当前复制的文件是第几个
         * @param totalCount    一共有多少文件需要复制
         */
        public PictureCopyResult(Uri copiedFileUri, String tipStr, int currentCount, int totalCount) {
            this.copiedFileUri = copiedFileUri;
            this.tipStr = tipStr;
            this.currentCount = currentCount;
            this.totalCount = totalCount;
            this.isSuccessful = true;
            this.exception = null;
        }

        public PictureCopyResult(String errTip, int currentCount, int totalCount, Throwable exception) {
            this.tipStr = errTip;
            this.copiedFileUri = null;
            this.currentCount = currentCount;
            this.totalCount = totalCount;
            this.isSuccessful = false;
            this.exception = exception;
        }
    }

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
        AppearanceAnimationHelper.setupAllChildMorphAnimation((ViewGroup) binding.getRoot());
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

        binding = null;
        disposables.dispose();
    }

    /**
     * 设置初始焦点
     */
    public abstract void setInitFocus();

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
     * @param tagNo   点击的标签编号
     * @param tagName 点击的标签名称
     */
    public void onTagBtnClicked(long tagNo, String tagName) {
        this.tno = tagNo;   //更新全局变量中的标签编号
        tagInput.setText(tagName);
        tagLayout.setError(null);
        tagSheet.dismiss();
    }

    /**
     * 初始化启动器
     */
    private void initLaunchers() {
        //从相册选择图片启动器
        albumLauncher = registerForActivityResult(
                new ActivityResultContracts.PickMultipleVisualMedia(),
                this::onAlbumPictureUrisReceived
        );

        //系统相机拍照启动器
        takePictureLauncher = registerForActivityResult(
                new ActivityResultContracts.TakePicture(),
                result -> {
                    if (result) {
                        onCameraPictureUriReceived(tempPictureUri);
                    } else {
                        Toast.makeText(requireContext(), "拍照已取消", Toast.LENGTH_SHORT).show();
                    }
                }
        );

        //权限申请启动器
        permissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                result -> {
                    if (result) {
                        launchSystemCamera();
                    } else if (ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(), Manifest.permission.CAMERA)) {
                        //弹出解释对话框
                        new MaterialAlertDialogBuilder(requireContext())
                                .setTitle("申请权限")
                                .setMessage("使用相机拍照需要先授予摄像头权限")
                                .setNegativeButton("取消", null)
                                .setPositiveButton(
                                        "确定",
                                        (dialogInterface, i) -> requestCameraPermission()
                                )
                                .show();
                    } else {
                        Toast.makeText(requireContext(), "请授予相机权限后再拍照", Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    /**
     * 观察标签数据的变化
     */
    private void startObserveTag() {
        TagRepository repository = TagRepository.getInstance();
        repository.getChangedTagList().observe(
                getViewLifecycleOwner(),
                tagList -> {
                    if (tagList == null) {
                        return;
                    }

                    TagUpdateReason updateReason = repository.getUpdateReason();
                    for (Tag tag : tagList) {
                        String tagName = tag.getName();
                        long tagNo = tag.getTno();

                        if (tagNo == this.tno) {    //只有找到匹配的标签编号才修改
                            switch (updateReason) {
                                case RENAME:
                                    tagInput.setText(tagName);
                                    break;
                                case DELETE:
                                    this.tno = 0;
                                    tagInput.setText("");
                                    break;
                                case MERGE:
                                    this.tno = TagDataController.nameTransToTno(tagName, requireContext());
                                    tagInput.setText(tagName);
                                    break;
                                default:
                                    break;
                            }
                        }
                    }
                }
        );
    }

    /**
     * 观察来自其他Fragment的图片变化
     */
    private void startObservePicture() {
        AccountPictureViewModel viewModel = new ViewModelProvider(requireActivity()).get(AccountPictureViewModel.class);

        //观察适配器删除模式状态
        viewModel.getAdapterStatData().observe(
                getViewLifecycleOwner(),
                isDeleteMode -> {
                    if (isDeleteMode != null) {
                        pictureAdapter.switchDeleteMode(isDeleteMode);
                    }
                }
        );

        //观察新增图片
        viewModel.getNewPictureData().observe(
                getViewLifecycleOwner(), pictureList -> {
                    if (pictureList != null) {
                        pictureAdapter.addPicture(pictureList);
                    }
                }
        );

        //观察图片删除
        viewModel.getPictureSelectData().observe(
                getViewLifecycleOwner(), pictureSelectList -> {
                    if (pictureSelectList != null) {
                        pictureAdapter.deleteSelectedPicture(pictureSelectList, requireContext());
                    }
                }
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
    protected void showMaterialDatePicker() {
        //解析已输入的时间
        String datetimeStr = String.valueOf(datetimeInput.getText());
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        LocalDateTime inputDatetime = LocalDateTime.parse(datetimeStr, formatter);
        LocalDate date = inputDatetime.toLocalDate();

        //显示日期选择对话框
        DateTimePickerHelper.selectDate(
                date, getParentFragmentManager(),
                selection -> {
                    //时间戳转换为LocalDateTime
                    LocalDateTime selectedDatetime = DateTimePickerHelper.getLocalDateTimeFromTimeMilli(selection);

                    //选择日期后，再弹出时间选择器
                    showMaterialTimePicker(selectedDatetime, inputDatetime);
                }
        );
    }

    /**
     * 显示时间选择对话框
     *
     * @param selectedDatetime 日期选择对话框选择的日期
     * @param initialDatetime  初始化日期
     */
    protected void showMaterialTimePicker(@NonNull LocalDateTime selectedDatetime, @NonNull LocalDateTime initialDatetime) {
        DateTimePickerHelper.selectDateTime(
                initialDatetime,
                getParentFragmentManager(),
                timePicker -> {
                    //组合日期和时间
                    int hour = timePicker.getHour();
                    int minute = timePicker.getMinute();
                    LocalDateTime finalDatetime = selectedDatetime.withHour(hour).withMinute(minute);

                    //修改文本框的日期和时间
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
                    datetimeInput.setText(formatter.format(finalDatetime));
                }
        );
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
        AccountPictureViewModel viewModel = new ViewModelProvider(requireActivity()).get(AccountPictureViewModel.class);
        pictureAdapter = new PictureAdapter(
                requireActivity(),
                viewModel,
                isDeleteMode -> {
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
                    Observable.fromCallable(() -> PictureDataController.loadPicturesByRno(requireContext(), rno))
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
                () -> {
                    if (PermissionHelper.isRuntimePermissionGranted(
                            Manifest.permission.CAMERA,
                            requireContext()
                    )) {
                        launchSystemCamera();
                    } else {
                        requestCameraPermission();
                    }
                },
                () -> albumLauncher.launch(new PickVisualMediaRequest.Builder()
                        .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                        .build()
                )
        );
        sheet.show(getParentFragmentManager(), TagString.PICTURE_ADD_SHEET.getValue());
    }

    /**
     * 申请相机权限
     */
    private void requestCameraPermission() {
        permissionLauncher.launch(Manifest.permission.CAMERA);
    }

    /**
     * 启动系统相机拍照
     */
    private void launchSystemCamera() {
        try {
            //在缓存目录下创建一个临时文件
            File photoFile = File.createTempFile(
                    "IMG_",
                    ".jpg",
                    DirectoryPaths.PICTURE_TEMP.getDir(requireContext())
            );

            //通过 FileProvider 获取 Content URI
            tempPictureUri = FileProvider.getUriForFile(
                    requireContext(),
                    requireContext().getPackageName() + ".fileprovider",
                    photoFile
            );

            //启动相机
            takePictureLauncher.launch(tempPictureUri);
        } catch (IOException e) {
            Toast.makeText(requireContext(), "无法创建相片文件", Toast.LENGTH_SHORT).show();
        } catch (SecurityException e) {
            Toast.makeText(requireContext(), "请授予相机权限后再拍照", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 处理拍照后
     *
     * @param pictureUri 拍照完成后照片的Uri
     */
    private void onCameraPictureUriReceived(@NonNull Uri pictureUri) {
        Toast.makeText(requireContext(), "拍照成功", Toast.LENGTH_SHORT).show();

        Picture newPicture = new Picture(pictureUri, rno);

        //通过ViewModel更新视图
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
        AtomicInteger errFileCount = new AtomicInteger(0);
        List<Uri> successfulUriList = new ArrayList<>();
        disposables.add(
                Observable.range(0, uriList.size())
                        .flatMap(index -> {
                            Uri uri = uriList.get(index);
                            return Observable.fromCallable(() -> copySinglePicture(uri, tempPictureDir))
                                    .map(fileName -> new PictureCopyResult(uri, fileName, index + 1, uriList.size()))
                                    .onErrorReturn(throwable -> new PictureCopyResult("文件复制出错！", index + 1, uriList.size(), throwable))
                                    .subscribeOn(Schedulers.io());
                        })
                        .observeOn(AndroidSchedulers.mainThread())
                        .doOnComplete(() -> {
                            //全部完成后弹出Toast
                            String tipStr;
                            if (errFileCount.get() == 0) {
                                tipStr = String.format(
                                        Locale.getDefault(),
                                        "%d张图片全部复制成功",
                                        successfulUriList.size()
                                );
                            } else {
                                tipStr = String.format(
                                        Locale.getDefault(),
                                        "图片复制完毕，成功%d个，失败%d个",
                                        successfulUriList.size(),
                                        errFileCount.get()
                                );
                            }
                            Toast.makeText(requireContext(), tipStr, Toast.LENGTH_SHORT).show();

                            //让进度对话框消失
                            processDialog.dismiss();

                            //将图片显示在UI中
                            List<Picture> successfulPictureList = successfulUriList.stream()
                                    .map(uri -> new Picture(uri, rno))
                                    .collect(Collectors.toList());
                            AccountPictureViewModel viewModel = new ViewModelProvider(requireActivity()).get(AccountPictureViewModel.class);
                            viewModel.addPicture(successfulPictureList);
                        })
                        .subscribe(result -> {
                            //将进度条对话框切换为确定模式
                            processDialog.setIndeterminate(false);

                            //更新进度条
                            processDialog.updateProgress(result.currentCount, result.totalCount, result.tipStr);

                            //统计成功与失败的文件数量
                            if (result.isSuccessful && result.copiedFileUri != null) {
                                successfulUriList.add(result.copiedFileUri);
                            } else {
                                errFileCount.set(errFileCount.get() + 1);
                            }
                        })
        );
    }

    /**
     * 复制单个图片
     *
     * @param imageUri  图片Uri
     * @param targetDir 目标目录
     * @return 复制成功的文件名
     * @throws IOException 文件复制出错引发的异常
     */
    private String copySinglePicture(Uri imageUri, File targetDir) throws IOException {
        //使用 UUID 确保唯一性
        String fileName = String.format(Locale.getDefault(), "album_%s.jpg",
                UUID.randomUUID().toString().substring(0, 8));
        File destinationFile = new File(targetDir, fileName);

        try (InputStream inputStream = requireContext().getContentResolver().openInputStream(imageUri);
             OutputStream outputStream = new FileOutputStream(destinationFile)
        ) {
            if (inputStream == null) {
                throw new IOException("无法正确打开文件输入流");
            }

            //使用 Android 内置的工具类更简洁，性能也更好
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }

            return fileName;
        } catch (IOException e) {
            Log.e(LogTags.ACCOUNT_FRAGMENT.getV(), "图片文件复制出错", e);
            throw e;
        }
    }
}

