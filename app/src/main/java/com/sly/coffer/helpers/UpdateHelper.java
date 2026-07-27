package com.sly.coffer.helpers;

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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.sly.coffer.R;
import com.sly.coffer.automation.workers.BackupWorker;
import com.sly.coffer.data.save.preference.VersionPreference;
import com.sly.coffer.automation.workers.WorkerScheduler;
import com.sly.coffer.ui.others.dialogs.MarkdownDialogBuilder;

import org.jetbrains.annotations.Unmodifiable;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ConnectException;
import java.net.ProtocolException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Locale;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.HttpsURLConnection;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class UpdateHelper {
    private static final String VERSION_INFO_URL = "https://gitee.com/e-zhiyu/manager-assistant-web/raw/main/version_info.json";
    private static final String UPDATE_LOG_INFO_URL = "https://gitee.com/e-zhiyu/manager-assistant-web/raw/main/CHANGELOG.md";

    static class UpdateInfo {
        String versionInfo = "";    //版本信息文件内容
        String updateLogInfo = "";  //更新日志文件内容
    }

    @JsonIgnoreProperties(ignoreUnknown = true) // 忽略JSON中多余字段
    static class VersionInfo {
        private long versionCode;     //版本代码
        private String versionName;   //版本名称
        private String updateLog;     //更新日志内容
        private boolean isMandatory;  //是否强制更新

        public VersionInfo() {
        }

        public long getVersionCode() {
            return versionCode;
        }

        public void setVersionCode(long versionCode) {
            this.versionCode = versionCode;
        }

        public String getVersionName() {
            return versionName;
        }

        public void setVersionName(String versionName) {
            this.versionName = versionName;
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

    /**
     * 检查更新
     *
     * @param context           上下文
     * @param disposables       多线程任务订阅列表
     * @param isHaveToast       是否需要弹出提示
     * @param ignoreSkipVersion 是否忽视跳过的软件版本
     */
    public static void checkUpdate(
            Context context,
            @NonNull CompositeDisposable disposables,
            boolean isHaveToast,
            boolean ignoreSkipVersion
    ) {
        disposables.add(Observable.fromCallable(UpdateHelper::getVersionInfo)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(updateInfo -> analyseVersionInfo(context, updateInfo, ignoreSkipVersion),
                        e -> {
                            if (e instanceof ProtocolException) {
                                if (isHaveToast) {
                                    Toast.makeText(context, "无法从远程服务器获取版本信息", Toast.LENGTH_SHORT).show();
                                }
                            } else if (e instanceof SocketTimeoutException) {
                                if (isHaveToast) {
                                    Toast.makeText(context, "连接超时，无法获取最新版本", Toast.LENGTH_SHORT).show();
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
                        }
                )
        );
    }

    /**
     * 获取版本信息
     *
     * @return 包含版本信息的字JSON符串
     * @throws ConnectException       无法创建连接时抛出的异常
     * @throws UnknownHostException   无法解析主机名时抛出的异常（例如没有网络时）
     * @throws SocketTimeoutException 连接超时异常
     */
    @NonNull
    private static @Unmodifiable UpdateInfo getVersionInfo() throws IOException {
        //获取版本数据
        URL versionInfoUrl = new URL(VERSION_INFO_URL);
        String versionInfo = readFromUrl(versionInfoUrl);

        //获取更新日志数据
        URL updateLogInfoUrl = new URL(UPDATE_LOG_INFO_URL);
        String updateLogInfo = readFromUrl(updateLogInfoUrl);

        //将两个数据打包到一起
        UpdateInfo updateInfo = new UpdateInfo();
        updateInfo.versionInfo = versionInfo;
        updateInfo.updateLogInfo = updateLogInfo;
        return updateInfo;
    }

    /**
     * 从URL中读取文本
     *
     * @param url 需要读取的URL链接
     * @return 读取到的字符串
     * @throws ConnectException       无法创建连接时抛出的异常
     * @throws UnknownHostException   无法解析主机名时抛出的异常（例如没有网络时）
     * @throws SocketTimeoutException 连接超时异常
     */
    @NonNull
    private static String readFromUrl(@NonNull URL url) throws IOException {
        //获取连接
        HttpsURLConnection versionConnection = (HttpsURLConnection) url.openConnection();
        versionConnection.setRequestMethod("GET");
        versionConnection.setConnectTimeout(5_000);    //设置连接超时
        versionConnection.setReadTimeout(5_000);       //设置读取超时

        //生成字符串
        BufferedReader reader = new BufferedReader(new InputStreamReader(versionConnection.getInputStream()));
        StringBuilder content = new StringBuilder();
        String versionLine;
        while ((versionLine = reader.readLine()) != null) {
            content.append(versionLine);
            content.append("\n");
        }
        reader.close();

        return content.toString();
    }

    /**
     * 解析版本信息文本
     *
     * @param context           上下文
     * @param updateInfo        从服务端获取的版本更新信息
     * @param ignoreSkipVersion 是否跳过忽视的软件版本
     * @throws PackageManager.NameNotFoundException 无法获取版本代码时引发的异常
     */
    private static void analyseVersionInfo(
            Context context,
            @NonNull UpdateInfo updateInfo,
            boolean ignoreSkipVersion
    ) throws JsonProcessingException, PackageManager.NameNotFoundException {
        ObjectMapper mapper = new ObjectMapper();
        VersionInfo versionInfo = mapper.readValue(updateInfo.versionInfo, VersionInfo.class);

        //获取版本信息
        long latestVersionCode = versionInfo.getVersionCode();                  //新版本的版本代码
        long currentVersionCode = AboutHelper.getVersionCode(context);          //当前版本代码
        long skipVersionCode = VersionPreference.getSkipVersionCode(context);   //跳过的版本代码
        boolean isMandatory = versionInfo.isMandatory();                        //是否强制更新
        String versionName = versionInfo.getVersionName();                      //版本名称

        //判断是否需要更新
        if (latestVersionCode > currentVersionCode && (ignoreSkipVersion || latestVersionCode > skipVersionCode)) {
            //根据版本名称生成下载链接
            String filled = versionName.startsWith("v") ? versionName : "v" + versionName;
            String downloadUrl = String.format(
                    Locale.getDefault(),
                    "https://gitee.com/e-zhiyu/manager-assistant-web/releases/download/%s/ManagerAssistant_%s.apk",
                    filled, filled
            );

            //处理更新日志文件的内容
            String cutUpdateLog = getUpdateContentByVersion(updateInfo.updateLogInfo, versionName);
            String updateLog = cutUpdateLog.isEmpty() ? versionInfo.getUpdateLog() : cutUpdateLog;

            //保存强制更新数据
            VersionPreference.setFindMandatoryUpdate(context, isMandatory);
            if (isMandatory) {
                VersionPreference.setMandatoryVersionName(context, versionName);
                VersionPreference.setMandatoryDownloadUrl(context, downloadUrl);
                VersionPreference.setMandatoryUpdateLog(context, updateLog);
            }

            //显示版本更新对话框
            showUpdateDialog(context, downloadUrl, updateLog, versionName, latestVersionCode);
        } else {
            throw new RuntimeException("当前已是最新版本"); //抛出异常是为了在subscribe语句中处理Toast提示
        }
    }

    /**
     * 对获取的更新日志内容进行切片
     *
     * @param fullLog 从网络上下载的完整更新日志
     * @param version 需要获取更新日志的目标版本名称
     * @return 目标版本名称对应的更新日志（包含版本名称）
     */
    @NonNull
    private static String getUpdateContentByVersion(String fullLog, String version) {
        // 正则解释：
        // ^#\s+VERSION : 匹配以 "# " 开头后接目标版本的行
        // ([\s\S]*?)    : 非贪婪匹配后续所有内容（包括换行符）
        // (?=^#\s+\d|$) : 环视(Lookahead)，匹配直到遇到下一个 "# " 开头的版本行 或者 字符串末尾
        String regex = "(?m)^#\\s+" + Pattern.quote(version) + "\\s*\\n([\\s\\S]*?)(?=(^#\\s+\\d)|$)";

        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(fullLog);

        if (matcher.find()) {
            return Objects.requireNonNullElse(matcher.group(1), "");
        }

        return ""; // 未找到对应版本
    }

    /**
     * 显示更新对话框
     *
     * @param context           上下文
     * @param downloadUrl       新版安装包下载链接
     * @param updateLog         更新日志
     * @param versionName       版本名称
     * @param latestVersionCode 更新版本的版本代码
     */
    private static void showUpdateDialog(
            Context context,
            String downloadUrl,
            String updateLog,
            String versionName,
            long latestVersionCode
    ) {
        //显示发现新版本对话框
        MarkdownDialogBuilder dialogBuilder = new MarkdownDialogBuilder(context, "发现新版本", updateLog);
        dialogBuilder.setPositiveButton("更新", (dialog, which) -> {
            try {
                downloadLatestFile(context, downloadUrl, versionName);
            } catch (IllegalArgumentException e) {
                Toast.makeText(context, "下载链接失效，无法下载安装包", Toast.LENGTH_SHORT).show();
            }
        });
        dialogBuilder.setNegativeButton("跳过此版本", (dialog, which) -> skipNextVersion(context, latestVersionCode));

        dialogBuilder.show();
    }

    /**
     * 下载新版安装包
     *
     * @param context     上下文
     * @param downloadUrl 下载链接
     * @param versionName 版本名称
     * @throws IllegalArgumentException 更新链接无效时引发的异常
     */
    private static void downloadLatestFile(
            @NonNull Context context,
            String downloadUrl,
            String versionName
    ) throws IllegalArgumentException {
        Toast.makeText(context, "正在下载安装包，请勿关闭本APP", Toast.LENGTH_SHORT).show();

        //下载安装包时就自动备份一次，防止数据丢失(备份文件存放至ExternalCache中)
        WorkerScheduler.executeWorkOnceNow(context, BackupWorker.class);

        //生成文件名
        String fileName = String.format("ManagerAssistant_v%s.apk", versionName);

        //请求下载
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(downloadUrl));
        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE);
        request.setTitle(context.getString(R.string.app_name));
        request.setDescription("正在下载安装包……");
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE);  //设置通知永远可见

        //设置下载路径
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName);

        //获取下载服务
        DownloadManager downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        long downloadId = downloadManager.enqueue(request);

        //实例化下载完成的广播接收器
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
                                throw new RuntimeException("无法获取安装包状态，请手动安装安装包");
                            int status = cursor.getInt(columnIndex);

                            if (status == DownloadManager.STATUS_SUCCESSFUL) {
                                //获取文件URI
                                columnIndex = cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI);
                                if (columnIndex == -1)
                                    throw new RuntimeException("无法获取安装包URI");
                                String fileUriStr = cursor.getString(columnIndex);
                                VersionPreference.setApkUri(context, fileUriStr);

                                //显示弹窗提醒用户安装
                                new MaterialAlertDialogBuilder(context)
                                        .setTitle("安装更新")
                                        .setMessage("安装包已下载完毕，是否立刻更新？")
                                        .setPositiveButton("立刻更新", (dialog, which) -> installLatestApk(context, fileUriStr))
                                        .setNegativeButton("取消", null)
                                        .show();
                            } else {
                                throw new IllegalArgumentException("安装包下载失败");
                            }
                        } catch (Exception e) {
                            Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                    cursor.close();
                }

                //注销自身
                context.unregisterReceiver(this);
            }
        };

        //注册下载完毕监听器
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
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
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
    private static void skipNextVersion(Context context, long versionCode) {
        VersionPreference.setSkipVersionCode(context, versionCode);
    }
}