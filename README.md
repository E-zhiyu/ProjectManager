# Sly's Coffer

> 这是一款极简的个人记账工具，界面采用Material You主题，功能干净纯粹，为用户提供清爽的记账体验。

## ✨功能概述

<table>
<tr>
<td width="50%" style="vertical-align:top">

### 记录流水

记录您交易的流水信息，每条记录都能添加若干图片。

</td>
<td width="50%" style="vertical-align:top">

### 自动记账

通过提取第三方应用发送的通知中的金额信息实现自动记账，用户可以自定义通知解析的规则。

</td>
</tr>
<tr>
<td width="50%" style="vertical-align:top">

### 预算管理

用户可以设置若干预算，预算余额能够随流水记录自动更新，余额不足时发送通知提醒用户。

</td>
<td width="50%" style="vertical-align:top">

### 流水标签

每条流水记录可以用若干标签标记，标记后更容易查找，且能够更好地支持各种自动化功能。

</td>
</tr>
<tr>
<td width="50%" style="vertical-align:top">

### 报表统计

支持查看某段时间的消费和收入情况，并按照流水标签分类展示收支信息。

</td>
<td width="50%" style="vertical-align:top">

### 数据管理与隐私保护

所有应用数据均保存至本地，用户可以通过内置的备份功能导入或导出应用数据。内置身份验证功能，开启后进入应用时需要进行身份验证。

</td>
</tr>
</table>

## 🛡️需要用到的权限

| 权限名称            | 说明                            |
|-----------------|-------------------------------|
| **摄像头权限**       | 添加流水记录图片时允许在APP内拍照            |
| **应用列表权限**      | 读取已安装的应用列表，便于快速选择应用           |
| **通知权限**        | 运行自动化服务时发送反馈通知                |
| **通知监听权限**      | 监听其他应用发送的通知内容实现自动记账           |
| **自启动权限**       | 魔改安卓特有的权限，确保APP能在后台运行自动化服务    |
| **“无限制”电池优化策略** | 降低APP被杀后台的几率，确保APP能在后台运行自动化服务 |
| **精确闹钟权限**      | 用于每天0点检查并重置需要重置的预算条目          |
| **系统启动监听权限**    | 用于在系统启动后自动安排预算重置检查任务          |

## 🎗️使用建议

- APP支持Android 9及以上的设备，推荐使用不低于Android 12的设备以获得最佳的使用体验。
- 部分国产系统即便授予通知权限也无法弹出横幅通知，需要手动进入通知设置允许发送横幅通知。
- 若使用的是定制安卓系统（如HyperOS、ColorOS等），请授予自启动权限以确保软件能够自启动，否则部分自动化功能会失效。

## 🔗软件下载

本软件的官方发布渠道如下：

- [Gitee仓库](https://gitee.com/e-zhiyu/sly-coffer/releases)
- [GitHub仓库](https://github.com/E-zhiyu/SlyCoffer/releases)

若您在其他地方看到有人分享本软件，请尝试联系作者，与他人分享该软件时也请引用上述发布渠道，谢谢您的配合。

## 🔖引用与致谢

开发本项目时用到了以下开源项目，在此感谢这些项目及其开源贡献者：

- [Material Components](https://github.com/material-components/material-components-android)
- [RxJava](https://github.com/ReactiveX/RxJava)
- [Glide](https://github.com/bumptech/glide)
- [PhotoView](https://github.com/Baseflow/PhotoView)
- [DeviceCompat](https://github.com/getActivity/DeviceCompat)

## 📢免责声明

本软件产生的所有数据均保存在用户的设备上，软件不会上传任何数据。

## 📞联系作者

以下是作者常活跃的社交平台：

- [酷安@E_zhiyu](https://www.coolapk.com/u/36112159)
- [B站@Lazyfishboned](https://space.bilibili.com/3493268083968463)