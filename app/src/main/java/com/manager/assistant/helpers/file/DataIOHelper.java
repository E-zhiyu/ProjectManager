package com.manager.assistant.helpers.file;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.os.ParcelFileDescriptor;
import android.util.Log;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.documentfile.provider.DocumentFile;

import com.manager.assistant.RecentTaskManager;
import com.manager.assistant.helpers.ExceptionHelper;
import com.manager.assistant.helpers.about.AboutHelper;
import com.manager.assistant.generic_enums.DirectoryPaths;
import com.manager.assistant.generic_enums.LogTags;
import com.manager.assistant.data.save.preference.AutoBackupPreference;
import com.manager.assistant.ui.pages.main.setting.sub.DataManageActivity;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * 通过SAF处理文件写入和读取的帮助器
 */
public class DataIOHelper {
    private final Context context;                                  //上下文
    private ImportCallback importCallback;                          //文件读取回调
    private final List<File> tempJsonFileList = new ArrayList<>();  //临时JSON文件列表
    private File tempPictureZip;                                    //临时图片压缩包
    private final File dataTempDir;                                 //临时文件目录
    private File tempZipFile;                                       //临时zip压缩文件
    private boolean isPictureNeed;                                  //是否需要打包图片文件（用于SAF回调使用）
    private Uri importZipUri;                                       //用户通过SAF导入数据的zip文件的Uri

    /**
     * 文件读取回调接口
     */
    public interface ImportCallback {
        /**
         * 从用户选择的zip文件中读取文件名称
         *
         * @param entryNameList 通过SAF选择的zip文件的内容名称列表
         */
        void onZipScanned(List<String> entryNameList);

        /**
         * 用户选择单个JSON文件并成功的回调
         *
         * @param jsonFile 单个JSON文件对象
         */
        void onOneJsonFileRead(File jsonFile);

        void onError(String errMessage);
    }

    /**
     * 文件数据IO帮助器构造方法
     *
     * @param context 上下文
     */
    public DataIOHelper(@NonNull Context context) {
        this.context = context;
        dataTempDir = DirectoryPaths.DATA_TEMP.getDir(context);
        if (dataTempDir == null) {
            Log.e(LogTags.DATA_IO_HELPER.getV(), "无法创建临时文件目录");
        }
    }

    /**
     * 将APP数据打包至zip文件中(手动方法)
     *
     * @param launcher        启动SAF的意图启动器
     * @param fileNameList    文件名列表
     * @param fileContentList 与文件名列表对应的文件内容列表
     * @param isPictureNeed   是否需要将图片文件打包
     */
    public void packDataInZip(
            ActivityResultLauncher<Intent> launcher,
            @NonNull List<String> fileNameList,
            List<String> fileContentList,
            boolean isPictureNeed) {
        this.isPictureNeed = isPictureNeed;

        //创建列表中的临时文件
        for (int index = 0; index < fileNameList.size(); index++) {
            String file_name = fileNameList.get(index);
            String file_content = fileContentList.get(index);

            createTempJsonFile(file_name, file_content);
        }

        //启动SAF提示用户选择保存位置
        saveFileUsingSAF(launcher); //手动模式需要选择文件存放位置
    }

    /**
     * 将APP数据打包至zip文件中(自动方法)
     *
     * @param fileNameList    文件名列表
     * @param fileContentList 文件内容列表
     */
    public void packDataInZip(@NonNull List<String> fileNameList, List<String> fileContentList) {
        //创建列表中的临时JSON文件
        for (int index = 0; index < fileNameList.size(); index++) {
            String file_name = fileNameList.get(index);
            String file_content = fileContentList.get(index);

            createTempJsonFile(file_name, file_content);
        }

        String autoBackupDirUriStr = AutoBackupPreference.getBackupDirectoryUri(context);
        if (autoBackupDirUriStr != null) {
            Uri backupDirUri = Uri.parse(autoBackupDirUriStr);
            createZipFile(backupDirUri);
        } else {
            String versionName;
            try {
                versionName = AboutHelper.getVersionName(context);
            } catch (PackageManager.NameNotFoundException e) {
                Log.w(LogTags.DATA_IO_HELPER.getV(), "无法获取版本名称");
                versionName = "UnknowVersion";
            }
            String backupFileName = String.format(Locale.getDefault(), "%s_backup.zip", versionName);
            createZipFile(dataTempDir, true); //没有设置备份目录就备份，则存放在ExternalCache目录中
            moveTempZipToCache(backupFileName);
        }
        clearTempFile();
    }

    /**
     * 将图片目录下的所有图片打包为zip文件
     */
    private void packPicturesInZip() {
        Log.d(LogTags.DATA_IO_HELPER.getV(), "正在打包图片文件");

        //如果图片目录不存在则不打包
        File pictureDir = DirectoryPaths.PICTURE.getDir(context);
        if (pictureDir == null) {
            Log.w(LogTags.DATA_IO_HELPER.getV(), "图片文件夹不存在");
            return;
        }

        //将图片文件写入压缩包
        File[] pictures = pictureDir.listFiles();
        if (pictures == null || pictures.length == 0) {
            Log.w(LogTags.DATA_IO_HELPER.getV(), "图片目录中没有图片");
            return;
        }
        File pictureZip = new File(dataTempDir, "pictures.zip"); //创建图片压缩包文件
        try (FileOutputStream fos = new FileOutputStream(pictureZip);
             ZipOutputStream zos = new ZipOutputStream(fos)) {
            for (File pictureFile : pictures) {
                try (FileInputStream fis = new FileInputStream(pictureFile)) {
                    ZipEntry zipEntry = new ZipEntry(pictureFile.getName());
                    zos.putNextEntry(zipEntry);

                    byte[] buffer = new byte[2048];
                    int length;
                    while ((length = fis.read(buffer)) > 0) {
                        zos.write(buffer, 0, length);
                    }
                    zos.closeEntry();
                }
            }

            //保存图片压缩包的引用
            tempPictureZip = pictureZip;
        } catch (IOException e) {
            Log.e(LogTags.DATA_IO_HELPER.getV(), "无法打包图片文件");
            throw new RuntimeException("无法打包图片文件");
        }
    }

    /**
     * 创建zip压缩包并将临时数据文件放入压缩包内
     *
     * @param parentDir     存放zip文件的目录
     * @param isPictureNeed 是否需要打包图片文件
     */
    private void createZipFile(File parentDir, boolean isPictureNeed) {
        Log.d(LogTags.DATA_IO_HELPER.getV(), "正在创建zip文件……");

        //判断是否需要打包图片
        if (isPictureNeed) {
            packPicturesInZip();
        }

        //创建并写入zip文件
        tempZipFile = new File(parentDir, "temp_backup_file");
        try (FileOutputStream fos = new FileOutputStream(tempZipFile)) {
            ZipOutputStream zos = new ZipOutputStream(fos);

            for (File tempFile : tempJsonFileList) {
                try (FileInputStream fis = new FileInputStream(tempFile)) {
                    ZipEntry zipEntry = new ZipEntry(tempFile.getName());
                    zos.putNextEntry(zipEntry);

                    byte[] buffer = new byte[1024];
                    int length;
                    while ((length = fis.read(buffer)) > 0) {
                        zos.write(buffer, 0, length);
                    }
                    zos.closeEntry();
                }
            }

            //将图片压缩包也写入文件
            if (tempPictureZip != null && tempPictureZip.exists()) {
                try (FileInputStream fis = new FileInputStream(tempPictureZip)) {
                    ZipEntry zipEntry = new ZipEntry(tempPictureZip.getName());
                    zos.putNextEntry(zipEntry);

                    byte[] buffer = new byte[8192];
                    int length;
                    while ((length = fis.read(buffer)) > 0) {
                        zos.write(buffer, 0, length);
                    }
                    zos.closeEntry();
                }
            }

            Log.d(LogTags.DATA_IO_HELPER.getV(), "zip文件创建成功");
        } catch (IOException e) {
            Log.e(LogTags.DATA_IO_HELPER.getV(), "无法创建zip文件");
            throw new RuntimeException("无法创建zip文件");
        }
    }

    /**
     * 创建zip压缩包并将临时数据文件放入压缩包内(DocumentFile API方法)(自动备份专用)
     *
     * @param parentDirUri 存放zip文件的目录Uri
     */
    private void createZipFile(Uri parentDirUri) {
        Log.d(LogTags.DATA_IO_HELPER.getV(), "正在创建zip文件……");

        //打包图片文件
        packPicturesInZip();

        //获取当前日期和时间并生成默认文件名
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd(HHmmss)");
        String now_date = formatter.format(now);
        String backUpFileName = String.format(Locale.getDefault(), "ManagerAssistantData_%s.zip", now_date);

        DocumentFile parentDir = DocumentFile.fromTreeUri(context, parentDirUri);
        DocumentFile backupFile;
        if (parentDir != null) {
            backupFile = parentDir.createFile("application/zip", backUpFileName);

            if (backupFile != null) {
                try (OutputStream fos = context.getContentResolver().openOutputStream(backupFile.getUri())) {
                    ZipOutputStream zos = new ZipOutputStream(fos);

                    for (File tempFile : tempJsonFileList) {
                        try (FileInputStream fis = new FileInputStream(tempFile)) {
                            ZipEntry zipEntry = new ZipEntry(tempFile.getName());
                            zos.putNextEntry(zipEntry);

                            byte[] buffer = new byte[1024];
                            int length;
                            while ((length = fis.read(buffer)) > 0) {
                                zos.write(buffer, 0, length);
                            }
                            zos.closeEntry();
                        }
                    }

                    //将图片压缩包也写入文件
                    if (tempPictureZip != null && tempPictureZip.exists()) {
                        try (FileInputStream fis = new FileInputStream(tempPictureZip)) {
                            ZipEntry zipEntry = new ZipEntry(tempPictureZip.getName());
                            zos.putNextEntry(zipEntry);

                            byte[] buffer = new byte[8192];
                            int length;
                            while ((length = fis.read(buffer)) > 0) {
                                zos.write(buffer, 0, length);
                            }
                            zos.closeEntry();
                        }
                    }

                    Log.d(LogTags.DATA_IO_HELPER.getV(), "自动备份文件创建成功");
                } catch (IOException e) {
                    ExceptionHelper.showExceptionDialog(context, e);
                    Log.e(LogTags.DATA_IO_HELPER.getV(), "无法创建自动备份文件");
                }
            } else {
                Log.e(LogTags.DATA_IO_HELPER.getV(), "无法创建自动备份文件");
            }
        } else {
            Log.e(LogTags.DATA_IO_HELPER.getV(), "无法获取自动备份目录");
        }
    }

    /**
     * 将临时zip文件移动至外部缓存(用于更新软件前保存备份数据)
     *
     * @param targetFileName 移动后的文件名(带后缀)
     */
    private void moveTempZipToCache(String targetFileName) {
        File cacheDir = DirectoryPaths.UPDATE_BACKUP.getDir(context);
        if (cacheDir == null) {
            Log.e(LogTags.DATA_IO_HELPER.getV(), "无法创建缓存文件夹");
            return;
        }

        File targetFile = new File(cacheDir, targetFileName);
        if (tempZipFile != null && tempZipFile.exists()) {
            if (!tempZipFile.renameTo(targetFile)) {
                Log.e(LogTags.DATA_IO_HELPER.getV(), "无法将临时zip文件移动至缓存");
            }
        } else {
            Log.e(LogTags.DATA_IO_HELPER.getV(), "临时zip文件不存在");
        }
    }

    /**
     * 使用SAF保存zip文件
     *
     * @param launcher 用于启动SAF的意图启动器
     */
    private void saveFileUsingSAF(@NonNull ActivityResultLauncher<Intent> launcher) {
        Log.d(LogTags.DATA_IO_HELPER.getV(), "正在通过SAF指定zip备份文件存放位置……");

        //获取当前日期和时间并生成默认文件名
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd(HHmmss)");
        String nowDate = formatter.format(now);
        String targetFileName = String.format(Locale.getDefault(), "ManagerAssistantData_%s.zip", nowDate);

        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/zip");
        intent.putExtra(Intent.EXTRA_TITLE, targetFileName);
        RecentTaskManager.startExternalActivity(launcher, intent);

        Log.d(LogTags.DATA_IO_HELPER.getV(), "SAF启动成功");
    }

    /**
     * 将临时zip文件写入到uri指定的位置
     *
     * @param uri 用户通过SAF生成的uri
     */
    private void copyTempZipToUri(Uri uri) {
        Log.d(LogTags.DATA_IO_HELPER.getV(), "开始复制临时zip文件到指定位置");

        ContentResolver resolver = context.getContentResolver();
        ParcelFileDescriptor pfd = null;
        FileInputStream fis = null;
        FileOutputStream finalFos = null;

        //将临时文件写入SAF URI
        try {
            pfd = resolver.openFileDescriptor(uri, "w");
            if (pfd != null) {
                finalFos = new FileOutputStream(pfd.getFileDescriptor());
                fis = new FileInputStream(tempZipFile);

                byte[] buffer = new byte[1024];
                int length;
                while ((length = fis.read(buffer)) > 0) {
                    finalFos.write(buffer, 0, length);
                }
            }
        } catch (IOException e) {
            Log.e(LogTags.DATA_IO_HELPER.getV(), "复制临时zip文件失败");
            throw new RuntimeException("复制临时zip文件失败");
        } finally {
            //关闭所有流并删除临时文件
            try {
                if (fis != null) fis.close();
                if (finalFos != null) finalFos.close();
                if (pfd != null) pfd.close();
            } catch (IOException e) {
                Log.w(LogTags.DATA_IO_HELPER.getV(), "无法正确关闭流");
            }
        }
    }

    /**
     * 处理数据导出结果
     *
     * @param uri 通过SAF返回的目标Uri
     */
    public void handleExportResult(@NonNull Uri uri) {
        createZipFile(dataTempDir, isPictureNeed);
        copyTempZipToUri(uri);
        Log.i(LogTags.DATA_IO_HELPER.getV(), "用户确认选择并进行下一步");

        clearTempFile();
    }

    /**
     * 处理导入数据结果
     *
     * @param uri 通过SAF返回的Uri
     */
    public void handleImportResul(@NonNull Uri uri) {
        String type = getMimeType(uri);
        if (type != null) {
            importZipUri = uri; //保存压缩文件Uri

            //判断文件类型
            if (type.equals("application/zip")) {
                Log.i(LogTags.DATA_IO_HELPER.getV(), "用户选择zip备份文件");
                scanZipFile(uri);
            } else if (type.equals("application/json")) {
                File oneJsonFile = getFileFromDocumentUri(uri);
                if (importCallback != null) {
                    importCallback.onOneJsonFileRead(oneJsonFile);
                }
                Log.i(LogTags.DATA_IO_HELPER.getV(), "用户选择JSON文件");
            } else {
                Log.e(LogTags.DATA_IO_HELPER.getV(), "用户选择了未知种类的文件");
                if (importCallback != null) {
                    importCallback.onError("请选择zip或json文件");
                }
            }
        } else {
            Log.e(LogTags.DATA_IO_HELPER.getV(), "无法获取文件信息");
        }
    }

    /**
     * 通过SAF打开备份文件
     *
     * @param callback 处理打开文件的回调
     * @param launcher SAF启动器
     */
    public void openFileViaSAF(ImportCallback callback,
                               ActivityResultLauncher<Intent> launcher) {
        Log.d(LogTags.DATA_IO_HELPER.getV(), "正在使用SAF选择zip备份文件……");
        this.importCallback = callback;

        try {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("*/*");
            String[] fileTypes = {"application/json", "application/zip"};
            intent.putExtra(Intent.EXTRA_MIME_TYPES, fileTypes);    //限制只能打开JSON和zip文件
            RecentTaskManager.startExternalActivity(launcher, intent);
            Log.d(LogTags.DATA_IO_HELPER.getV(), "SAF启动成功");
        } catch (Exception e) {
            ExceptionHelper.showExceptionDialog(context, e);
            if (importCallback != null) {
                importCallback.onError("SAF出错");
            }
            Log.e(LogTags.DATA_IO_HELPER.getV(), "SAF出错");
        }
    }

    /**
     * 扫描zip文件并获取其中所有文件的文件名
     *
     * @param uri 通过SAF选中的zip文件的Uri
     */
    private void scanZipFile(Uri uri) {
        Log.d(LogTags.DATA_IO_HELPER.getV(), "正在扫描zip文件");
        ContentResolver resolver = context.getContentResolver();

        //生成合法文件名列表
        List<String> legalEntryNameList = Arrays.stream(DataManageActivity.IODataType.values())
                .map(DataManageActivity.IODataType::getDefaultFileName)
                .collect(Collectors.toList());
        String pictureZipName = String.format(Locale.getDefault(), "%s.zip", DirectoryPaths.PICTURE.getChildDirName());
        legalEntryNameList.add(pictureZipName);

        try (InputStream is = resolver.openInputStream(uri)) {
            if (is == null) throw new IOException("无法打开输入流");

            try (ZipInputStream zis = new ZipInputStream(is)) {
                ZipEntry entry;
                List<String> entryNameList = new ArrayList<>();
                while ((entry = zis.getNextEntry()) != null) {
                    String entryName = entry.getName();

                    //如果检测到不合法的文件名，则直接退出循环并通过回调返回空列表
                    if (!legalEntryNameList.contains(entryName)) {
                        entryNameList.clear();
                        break;
                    }

                    entryNameList.add(entryName);
                    zis.closeEntry();
                }

                //通过回调传送给设置界面处理逻辑
                if (importCallback != null) {
                    //在主线程更新UI
                    new Handler(Looper.getMainLooper()).post(() -> importCallback.onZipScanned(entryNameList));
                } else {
                    Log.e(LogTags.DATA_IO_HELPER.getV(), "数据导入回调为空");
                }
            }
        } catch (IOException e) {
            Log.e(LogTags.DATA_IO_HELPER.getV(), "zip文件扫描失败");
            importCallback.onError("zip文件扫描失败");
            ExceptionHelper.showExceptionDialog(context, e);
        }
    }

    /**
     * 将来自SAF的uri转换为File对象
     *
     * @param uri 来自SAF的uri
     * @return 复制到临时目录下的JSON文件对象
     */
    @Nullable
    private File getFileFromDocumentUri(Uri uri) {
        try {
            //获取DocumentFile对象
            DocumentFile documentFile = DocumentFile.fromSingleUri(context, uri);
            if (documentFile.canRead()) {
                //复制文件到Android/data下的临时目录
                File tempFile = new File(dataTempDir, "temp_json");
                try (InputStream in = context.getContentResolver().openInputStream(uri);
                     OutputStream out = new FileOutputStream(tempFile)) {
                    byte[] buffer = new byte[1024];
                    int length;
                    if (in != null) {
                        while ((length = in.read(buffer)) > 0) {
                            out.write(buffer, 0, length);
                        }

                        tempJsonFileList.add(tempFile); //加入临时文件列表
                        return tempFile;                //返回临时文件
                    } else {
                        throw new IOException("无法打开输入流");
                    }
                }
            } else {
                throw new IOException("文件无法读取");
            }
        } catch (IOException e) {
            ExceptionHelper.showExceptionDialog(context, e);
            if (importCallback != null) {
                importCallback.onError("JSON文件读取失败");
            }
            Log.e(LogTags.DATA_IO_HELPER.getV(), "JSON文件读取失败");
            return null;
        }
    }

    /**
     * 将用户选择的目标zip复制到临时目录并解压
     *
     * @return 解压得到的JSON文件
     */
    @Nullable
    public List<File> copyZipToTempAndUnpack() {
        Log.d(LogTags.DATA_IO_HELPER.getV(), "开始复制目标zip备份文件到临时目录……");

        //生成临时zip文件对象
        tempZipFile = new File(dataTempDir, "backup_temp.zip");
        if (!dataTempDir.exists() && !dataTempDir.mkdirs()) {
            RuntimeException e = new RuntimeException("临时文件目录创建失败");
            ExceptionHelper.showExceptionDialog(context, e);
            if (importCallback != null) {
                importCallback.onError("临时文件目录创建失败");
            }
            return null;
        }

        ParcelFileDescriptor pfd = null;
        InputStream inputStream = null;
        OutputStream outputStream = null;
        try {
            pfd = context.getContentResolver()
                    .openFileDescriptor(importZipUri, "r");
            if (pfd == null) throw new RuntimeException("无法正确打开流");

            inputStream = new FileInputStream(pfd.getFileDescriptor());
            outputStream = new FileOutputStream(tempZipFile);

            //使用缓冲流提高复制效率
            byte[] buffer = new byte[8192];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }

            //复制完成后解包并返回解压后的文件列表
            return unpackZipFile();
        } catch (Exception e) {
            ExceptionHelper.showExceptionDialog(context, e);
            if (importCallback != null) {
                importCallback.onError("复制zip文件时出错");
            }
            Log.e(LogTags.DATA_IO_HELPER.getV(), "zip文件复制失败");

            //复制失败时清理可能残留的临时文件
            clearTempFile();
        } finally {
            try {
                if (pfd != null) pfd.close();
                if (inputStream != null) inputStream.close();
                if (outputStream != null) outputStream.close();
            } catch (IOException e) {
                ExceptionHelper.showExceptionDialog(context, e);
                if (importCallback != null) {
                    importCallback.onError("无法正确关闭流");
                }
                Log.e(LogTags.DATA_IO_HELPER.getV(), "无法正确关闭流");
            }
        }

        return null;
    }

    /**
     * 解包临时zip文件
     */
    @Nullable
    private List<File> unpackZipFile() {
        try (FileInputStream fis = new FileInputStream(tempZipFile);
             ZipInputStream zis = new ZipInputStream(fis)) {
            Log.d(LogTags.DATA_IO_HELPER.getV(), "开始解压zip备份文件……");

            ZipEntry entry;
            byte[] buffer = new byte[8192]; // 8KB缓冲区

            while ((entry = zis.getNextEntry()) != null) {
                File entryFile = new File(dataTempDir, entry.getName());

                if (entryFile.isDirectory()) continue;  //不处理目录类

                if (entry.getName().endsWith("json")) {
                    tempJsonFileList.add(entryFile);
                } else if (entry.getName().equals("pictures.zip")) {
                    tempPictureZip = entryFile;
                }

                //写入文件内容
                try (FileOutputStream fos = new FileOutputStream(entryFile)) {
                    int length;
                    while ((length = zis.read(buffer)) > 0) {
                        fos.write(buffer, 0, length);
                    }
                }

                zis.closeEntry(); //关闭当前条目
            }

            //读取解压得到的文件内容
            if (!tempJsonFileList.isEmpty()) {
                return tempJsonFileList;
            } else {
                if (importCallback != null) {
                    importCallback.onError("zip文件为空");
                }
                Log.w(LogTags.DATA_IO_HELPER.getV(), "zip备份文件为空");
            }
        } catch (IOException e) {
            ExceptionHelper.showExceptionDialog(context, e);
            if (importCallback != null) {
                importCallback.onError("zip文件解压失败");
            }
            Log.e(LogTags.DATA_IO_HELPER.getV(), "zip备份文件解压失败");
            clearTempFile();
        }

        return null;
    }

    /**
     * 将图片压缩包解压至图片目录
     */
    public void unpackPictureZip() {
        Log.d(LogTags.DATA_IO_HELPER.getV(), "正在解压图片文件");

        File pictureDir = DirectoryPaths.PICTURE.getDir(context);
        if (pictureDir == null) {
            Log.e(LogTags.DATA_IO_HELPER.getV(), "无法创建永久图片目录");
            return;
        }

        //遍历删除旧图片
        File[] oldPictureFiles = pictureDir.listFiles();
        if (oldPictureFiles != null) {
            for (File oldPicture : oldPictureFiles) {
                if (!oldPicture.delete()) {
                    Log.w(LogTags.DATA_IO_HELPER.getV(), String.format(
                            Locale.getDefault(),
                            "旧图片“%s”删除失败",
                            oldPicture.getName()
                    ));
                }
            }
        }

        if (tempPictureZip == null || !tempPictureZip.exists()) {
            Log.w(LogTags.DATA_IO_HELPER.getV(), "图片压缩包不存在");
            return;
        }

        try (FileInputStream fis = new FileInputStream(tempPictureZip);
             ZipInputStream zis = new ZipInputStream(fis)) {
            Log.d(LogTags.DATA_IO_HELPER.getV(), "开始解压图片压缩包");

            ZipEntry entry;
            byte[] buffer = new byte[8192]; //创建8K的缓冲区

            while ((entry = zis.getNextEntry()) != null) {
                File entryFile = new File(pictureDir, entry.getName());

                //写入文件内容
                try (FileOutputStream fos = new FileOutputStream(entryFile)) {
                    int length;
                    while ((length = zis.read(buffer)) > 0) {
                        fos.write(buffer, 0, length);
                    }
                }

                zis.closeEntry(); //关闭当前条目
            }
        } catch (IOException e) {
            Log.e(LogTags.DATA_IO_HELPER.getV(), "图片压缩包解压失败");
            ExceptionHelper.showExceptionDialog(context, e);
        }
    }

    /**
     * 创建空白临时JSON文件
     *
     * @param file_name 文件名称
     * @param content   文件内容
     */
    private void createTempJsonFile(String file_name, String content) {
        //创建临时目录
        if (!dataTempDir.exists() && !dataTempDir.mkdirs()) {
            RuntimeException e = new RuntimeException("无法创建临时文件目录");
            ExceptionHelper.showExceptionDialog(context, e);
            clearTempFile();
            return;
        }

        File tempJsonFile = new File(dataTempDir, file_name);
        tempJsonFileList.add(tempJsonFile);
        writeContentToTempFile(tempJsonFile, content);
    }

    /**
     * 将字符串内容写入临时文件
     *
     * @param targetFile 待写入的目标文件
     * @param content    字符串内容
     */
    private void writeContentToTempFile(@NonNull File targetFile, String content) {
        Log.d(
                LogTags.DATA_IO_HELPER.getV(),
                String.format(Locale.getDefault(), "正在将内容写入文件%s……", targetFile.getName())
        );
        try (FileOutputStream fos = new FileOutputStream(targetFile)) {
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(fos));

            writer.write(content);
            writer.flush();
            Log.d(LogTags.DATA_IO_HELPER.getV(), "文件内容写入完毕");
        } catch (IOException e) {
            ExceptionHelper.showExceptionDialog(context, e);
            Log.e(LogTags.DATA_IO_HELPER.getV(), "临时文件写入失败");
        }
    }

    /**
     * 清除临时文件
     */
    public void clearTempFile() {
        Log.d(LogTags.DATA_IO_HELPER.getV(), "开始清除临时文件……");
        boolean isFileDeleteFailed = false;

        for (File tempFile : tempJsonFileList) {
            if (tempFile.exists() && !tempFile.delete()) {
                isFileDeleteFailed = true;
            }
        }
        tempJsonFileList.clear();

        if (tempZipFile != null && !tempZipFile.delete()) {
            isFileDeleteFailed = true;
        }
        tempZipFile = null;

        if (tempPictureZip != null && !tempPictureZip.delete()) {
            isFileDeleteFailed = true;
        }
        tempPictureZip = null;

        if (isFileDeleteFailed) {
            Log.w(LogTags.DATA_IO_HELPER.getV(), "临时文件清除失败");
        } else {
            Log.d(LogTags.DATA_IO_HELPER.getV(), "临时文件清除完毕");
        }
    }

    /**
     * 通过uri获取文件种类
     *
     * @param uri 目标文件的uri
     * @return 文件种类
     */
    private String getMimeType(Uri uri) {
        ContentResolver contentResolver = context.getContentResolver();
        return contentResolver.getType(uri); //返回MIME类型，如"image/jpeg"
    }
}