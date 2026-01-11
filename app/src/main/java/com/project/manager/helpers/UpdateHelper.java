package com.project.manager.helpers;

import android.annotation.SuppressLint;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textview.MaterialTextView;
import com.project.manager.R;
import com.project.manager.data.data_save.preference.VersionPreference;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ConnectException;
import java.net.ProtocolException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Objects;

import javax.net.ssl.HttpsURLConnection;

import io.noties.markwon.Markwon;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class UpdateHelper {
    private static final String versionInfoUrL = "https://gitee.com/e-zhiyu/manager-assistant-web/raw/main/version_info.json";

    /**
     * 检查更新
     *
     * @param context     上下文
     * @param isHaveToast 是否需要弹出提示
     */
    public static void checkUpdate(Context context, boolean isHaveToast) {
        Disposable disposable = Observable.fromCallable(() -> {
                    URL versionInfoUrl = new URL(versionInfoUrL);
                    return getVersionInfo(versionInfoUrl);
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(version_info_json -> analyseVersionInfo(context, version_info_json),
                        e -> {
                            if (e instanceof ProtocolException) {
                                if (isHaveToast) {
                                    Toast.makeText(context, "无法从远程服务器获取版本信息", Toast.LENGTH_SHORT).show();
                                }
                            } else if (e instanceof SocketTimeoutException) {
                                if (isHaveToast) {
                                    Toast.makeText(context, "无法获取最新版本：连接超时", Toast.LENGTH_SHORT).show();
                                }
                            } else if (e instanceof ConnectException || e instanceof UnknownHostException) {
                                if (isHaveToast) {
                                    Toast.makeText(context, "请检查网络连接", Toast.LENGTH_SHORT).show();
                                }
                            } else if (e instanceof RuntimeException) {
                                if (isHaveToast) {
                                    Toast.makeText(context, "当前已是最新版本", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                ExceptionHelper.showExceptionDialog(context, e);
                            }
                        });
    }

    /**
     * 获取版本信息
     *
     * @param versionInfoUrl 版本信息文件的链接
     * @return 包含版本信息的字JSON符串
     * @throws IOException 无法读取文件信息时的异常
     */
    @NonNull
    private static String getVersionInfo(@NonNull URL versionInfoUrl) throws IOException {
        HttpsURLConnection connection = (HttpsURLConnection) versionInfoUrl.openConnection();
        connection.setRequestMethod("GET");
        connection.setConnectTimeout(5_000);    //设置连接超时
        connection.setReadTimeout(5_000);       //设置读取超时

        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        StringBuilder versionInfoContent = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            versionInfoContent.append(line);
            versionInfoContent.append("\n");
        }
        reader.close();

        return versionInfoContent.toString();
    }

    /**
     * 解析版本信息文本
     *
     * @param context           上下文
     * @param version_info_json 从服务端获取的最新版本信息
     * @throws PackageManager.NameNotFoundException 无法获取版本代码时引发的异常
     */
    private static void analyseVersionInfo(Context context, String version_info_json) throws PackageManager.NameNotFoundException, JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        VersionInfo versionInfo = mapper.readValue(version_info_json, VersionInfo.class);

        int latestVersionCode = versionInfo.getVersionCode();                   //新版本的版本代码
        int currentVersionCode = AboutHelper.getVersionCode(context);           //当前版本代码
        int skipVersionCode = VersionPreference.getSkipVersionCode(context);    //跳过的版本代码
        if (latestVersionCode > currentVersionCode && latestVersionCode > skipVersionCode) {
            boolean isMandatory = versionInfo.isMandatory();
            String downloadUrl = versionInfo.getDownloadUrl();
            String updateLog = versionInfo.getUpdateLog();
            String versionName = versionInfo.getVersionName();

            //保存强制更新数据
            VersionPreference.setFindMandatoryUpdate(context, isMandatory);
            if (isMandatory) {
                VersionPreference.setMandatoryVersionName(context, versionName);
                VersionPreference.setMandatoryDownloadUrl(context, downloadUrl);
                VersionPreference.setMandatoryUpdateLog(context, updateLog);
            }

            //显示版本更新对话框
            showUpdateDialog(context, downloadUrl, updateLog, versionName, latestVersionCode, isMandatory);
        } else {
            throw new RuntimeException("当前已是最新版本"); //抛出异常是为了在subscribe语句中处理Toast提示
        }
    }

    /**
     * 显示更新对话框
     *
     * @param context           上下文
     * @param downloadUrl       新版安装包下载链接
     * @param updateLog         更新日志
     * @param versionName       版本名称
     * @param latestVersionCode 更新版本的版本代码
     * @param isMandatory       是否强制更新
     */
    private static void showUpdateDialog(
            Context context,
            String downloadUrl,
            String updateLog,
            String versionName,
            int latestVersionCode,
            boolean isMandatory) {
        View markdownDialog = LayoutInflater.from(context)
                .inflate(R.layout.view_markdown_text, null);
        MaterialTextView textView = markdownDialog.findViewById(R.id.md_textview_in_dialog);

        //使用Markown渲染Markdown文本
        Markwon markwon = Markwon.create(context);
        markwon.setMarkdown(textView, updateLog);

        //显示发现新版本对话框
        MaterialAlertDialogBuilder dialogBuilder = new MaterialAlertDialogBuilder(context)
                .setTitle("发现新版本")
                .setView(markdownDialog)
                .setPositiveButton(
                        "更新",
                        (dialog, which) -> downloadLatestFile(context, downloadUrl, versionName)
                );
        if (!isMandatory) {
            dialogBuilder.setNegativeButton("跳过此版本", (dialog, which) -> skipNextVersion(context, latestVersionCode));
        } else {
            dialogBuilder.setNegativeButton("退出", (dialog, which) -> dialog.cancel());
            dialogBuilder.setCancelable(false);     //强制更新不能取消
            dialogBuilder.setOnCancelListener(dialog -> android.os.Process.killProcess(android.os.Process.myPid()));
        }

        dialogBuilder.show();
    }

    /**
     * 下载新版安装包
     *
     * @param context     上下文
     * @param downloadUrl 下载链接
     * @param versionName 版本名称
     */
    private static void downloadLatestFile(@NonNull Context context, String downloadUrl, String versionName) {
        Toast.makeText(context,"正在下载安装包，请勿关闭本APP",Toast.LENGTH_SHORT).show();

        //生成文件名
        String fileName = String.format("ManagerAssistant_v%s.apk", versionName);

        //请求下载
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(downloadUrl));
        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE);
        request.setTitle("经理助手");
        request.setDescription("正在下载安装包...");
        request.setVisibleInDownloadsUi(true);
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);

        //设置下载路径
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName);

        //获取下载服务
        DownloadManager downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        long downloadId = downloadManager.enqueue(request);

        //注册下载完成的广播接收器
        BroadcastReceiver downloadFinishReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, @NonNull Intent intent) {
                long id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
                if (id == downloadId) {
                    //检测下载完成的状态
                    DownloadManager.Query query = new DownloadManager.Query();
                    query.setFilterById(downloadId);
                    Cursor cursor = downloadManager.query(query);

                    if (cursor.moveToFirst()) {
                        try {
                            //获取下载状态
                            int columnIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS);
                            if (columnIndex == -1)
                                throw new RuntimeException("无法获取安装包下载状态");
                            int status = cursor.getInt(columnIndex);

                            if (status == DownloadManager.STATUS_SUCCESSFUL) {
                                //获取文件URI
                                columnIndex = cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI);
                                if (columnIndex == -1)
                                    throw new RuntimeException("无法获取安装包URI");
                                String fileUri = cursor.getString(columnIndex);

                                //显示弹窗提醒用户安装
                                new MaterialAlertDialogBuilder(context)
                                        .setTitle("安装更新")
                                        .setMessage("安装包已下载完毕，是否立刻更新？")
                                        .setPositiveButton("立刻更新", (dialog, which) -> installLatestApk(context, fileUri))
                                        .setNegativeButton("取消", null)
                                        .show();
                            } else {
                                throw new RuntimeException("安装包下载失败");
                            }
                        } catch (Exception e) {
                            ExceptionHelper.showExceptionDialog(context, e);
                        }
                    }
                    cursor.close();
                }

                //注销自身
                context.unregisterReceiver(this);
            }
        };
        IntentFilter filter = new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.registerReceiver(downloadFinishReceiver, filter, Context.RECEIVER_EXPORTED);
        } else {
            ContextCompat.registerReceiver(context, downloadFinishReceiver, filter, ContextCompat.RECEIVER_NOT_EXPORTED);
        }
    }

    /**
     * 安装下载好的APK安装包
     *
     * @param context 上下文
     * @param fileUri 安装包Uri
     */
    private static void installLatestApk(@NonNull Context context, String fileUri) {
        //将强制更新标识改为false
        VersionPreference.setFindMandatoryUpdate(context, false);

        File apkFile = new File(Objects.requireNonNull(Uri.parse(fileUri).getPath()));

        Uri apkUri = FileProvider.getUriForFile(
                context,
                context.getPackageName() + ".fileprovider",
                apkFile
        );
        @SuppressLint("RequestInstallPackagesPolicy") Intent intent = new Intent(Intent.ACTION_INSTALL_PACKAGE);
        intent.setDataAndType(apkUri, "application/vnd.android.package-archive");
        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        context.startActivity(intent);
    }

    /**
     * 跳过新版本
     *
     * @param context     上下文
     * @param versionCode 被跳过的版本的版本代码
     */
    private static void skipNextVersion(Context context, int versionCode) {
        VersionPreference.setSkipVersionCode(context, versionCode);
    }

    /**
     * 使用本地保存的强制更新数据显示强制更新对话框
     *
     * @param context 上下文
     */
    public static void showMandatoryUpdateDialog(Context context) {
        String MandatoryVersionName = VersionPreference.getMandatoryVersionName(context);
        String MandatoryUpdateLog = VersionPreference.getMandatoryUpdateLog(context);
        String MandatoryDownloadUrl = VersionPreference.getMandatoryDownloadUrl(context);

        View markDownDialog = LayoutInflater.from(context)
                .inflate(R.layout.view_markdown_text, null);
        MaterialTextView textView = markDownDialog.findViewById(R.id.md_textview_in_dialog);

        //使用Markown渲染Markdown文本
        Markwon markwon = Markwon.create(context);
        markwon.setMarkdown(textView, MandatoryUpdateLog);

        //显示发现新版本对话框
        MaterialAlertDialogBuilder dialogBuilder = new MaterialAlertDialogBuilder(context)
                .setTitle("发现新版本")
                .setView(markDownDialog)
                .setCancelable(false)   //强制更新不可取消
                .setPositiveButton(
                        "更新",
                        (dialog, which) -> downloadLatestFile(context, MandatoryDownloadUrl, MandatoryVersionName)
                );
        dialogBuilder.setNegativeButton("退出", (dialog, which) -> dialog.cancel());
        dialogBuilder.setOnCancelListener(dialog -> android.os.Process.killProcess(android.os.Process.myPid()));

        dialogBuilder.show();
    }
}

class VersionInfo {
    private int versionCode;                //版本代码
    private String versionName;             //版本名称
    private String downloadUrl;             //下载链接
    private String updateLog;               //更新日志内容
    private boolean isMandatory;            //是否强制更新

    public VersionInfo() {
    }

    public int getVersionCode() {
        return versionCode;
    }

    public void setVersionCode(int versionCode) {
        this.versionCode = versionCode;
    }

    public String getVersionName() {
        return versionName;
    }

    public void setVersionName(String versionName) {
        this.versionName = versionName;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }

    public String getUpdateLog() {
        return updateLog;
    }

    public void setUpdateLog(String updateLog) {
        this.updateLog = updateLog;
    }

    public boolean isMandatory() {
        return isMandatory;
    }

    public void setMandatory(boolean mandatory) {
        isMandatory = mandatory;
    }
}