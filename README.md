# ManagerAssistant

> 这是一款极简的个人记账工具，界面采用Material You风格，无多余功能，为用户提供清爽的记账体验。

## ✨功能概述

<table>
<tr>
<td width="50%" style="vertical-align:top">

### 记录流水

记录金额、备注、日期、收支种类等流水数据，并且支持为每条流水记录添加若干图片。

</td>
<td width="50%" style="vertical-align:top">

### 自动记账

使用安卓的通知监听服务解析通知内容实现自动记账，只要交易后产生通知就能自动记账。触发自动记账时会发送通知反馈记账结果，支持用户自定义记账规则。

</td>
</tr>
<tr>
<td width="50%" style="vertical-align:top">

### 预算管理

用户可以设置若干预算条目，产生新流水记录或修改现有流水记录时更新预算余额，余额不足时发送通知提醒用户，预算余额支持手动重置与自动重置两种方式。

</td>
<td width="50%" style="vertical-align:top">

### 流水标签

每条流水记录都可以用一个标签进行标记，允许用户通过流水标签筛选流水记录，操作流水记录时会根据其标签自动更新对应预算的余额。

</td>
</tr>
<tr>
<td width="50%" style="vertical-align:top">

### 报表统计

在应用主页会生成简易的每日报表，用户点击该报表视图可以跳转到详细报表界面，支持查看某一时间段的收支情况以及每月流水总结。

</td>
<td width="50%" style="vertical-align:top">

### 数据管理与隐私保护

用户产生的所有数据均保存在本地，用户可以随时利用APP内置的数据导出功能备份流水数据。同时本APP还内置身份验证功能，当启用该功能时每次进入APP都将要求用户进行身份验证，若验证不通过将无法使用本APP，尽可能地降低了数据被他人窃取的风险。

</td>
</tr>
<tr>
<td width="50%" style="vertical-align:top">

### 界面显示

本APP使用Google设计的Material You主题，为用户提供简洁明了的UI界面，同时各组件交互的反馈效果明显，尽可能让用户能够快速上手。

</td>
<td style="vertical-align:top;width:50%">

### 性能开销与资源占用

本APP极其轻量化，内存和存储占用极低，使用一段时间后占用20MB左右存储以及340MB内存。（~~不像某超级APP占用几GB的内存和存储空间~~）

</td>
</tr>
</table>

## 🎞️效果展示

<div style="display: flex; justify-content: center; gap: 10px;">
  <img src="https://github.com/user-attachments/assets/6fc0e0bb-3f68-49e8-a49a-02103183db15" width="19%" alt="记账"/>
  <img src="https://github.com/user-attachments/assets/7dbc4e12-3a2e-4c0c-915d-97be9071f32c" width="19%" alt="主页"/>
  <img src="https://github.com/user-attachments/assets/54acae40-c0e3-4001-89ee-84da27e06b91" width="19%" alt="报表"/>
  <img src="https://github.com/user-attachments/assets/b72f4eb5-832b-4f02-a88d-abce51c23dd2" width="19%" alt="标签管理"/>
  <img src="https://github.com/user-attachments/assets/7e56588f-cfa6-46d1-9409-9d8794a7a0fb" width="19%" alt="解析规则管理"/>
</div>

## 🛡️需要用到的权限

| 权限名称          | 说明                            |
|---------------|-------------------------------|
| **摄像头权限**     | 添加流水记录图片时允许在APP内拍照            |
| **应用列表权限**    | 读取已安装的应用列表，便于快速选择应用           |
| **通知权限**      | 运行自动化服务时发送反馈通知                |
| **通知监听权限**    | 监听其他应用发送的通知内容实现自动记账           |
| **自启动权限**     | 魔改安卓特有的权限，确保APP能在后台运行自动化服务    |
| **“无限制”电池优化** | 降低APP被杀后台的几率，确保APP能在后台运行自动化服务 |
| **精确闹钟权限**    | 用于每天0点检查并重置需要重置的预算条目          |
| **系统启动监听权限**  | 用于在系统启动后自动安排第二天0点的预算重置任务      |

## 📱支持的设备

软件的targetAPI等级为36，minApi等级为28，理论支持Android 9及以上的设备，推荐使用不低于Android 12的设备以获得最佳的使用体验。

## 🔗软件下载

本软件的官方发布渠道如下：

- [Gitee仓库](https://gitee.com/e-zhiyu/manager-assistant-web/releases)
- [123云盘](https://www.123865.com/s/C5xcVv-kRYT3)
- [小飞机网盘](https://share.feijipan.com/s/kTVc2PiI)

如果您在其他地方看到有人分享本软件，请私信我的社交账号，在跟别人分享该软件时也请您引用本仓库界面，谢谢您的配合。

## 🔖引用与致谢

开发ManagerAssistant时用到了以下开源项目，在此感谢这些项目及其开源贡献者：

- [Material Components](https://github.com/material-components/material-components-android)
- [RxJava](https://github.com/ReactiveX/RxJava)
- [Glide](https://github.com/bumptech/glide)
- [PhotoView](https://github.com/Baseflow/PhotoView)

## 📢免责声明

本软件不会收集任何有关于您的数据，除了软件更新以外没有需要联网使用的功能。  
本软件不会申请无关权限，且不会将已申请的权限应用于不正当的行为上，请您放心使用。

## 📡作者的话

作者只是一名学艺不精的在校大学生，受老师的启发创建了本项目，并且有相当一部分代码借助了AI进行实现，可能有部分功能实现得并不好，如果您发现软件有什么BUG，欢迎来到我的仓库提出Issue，或者通过其他方式与我联系，反馈问题时请描述清楚您使用的安卓版本、问题描述以及复现方式。作者活跃的平台如下：

- [酷安@E_zhiyu](https://www.coolapk.com/u/36112159)
- [B站@Lazyfishbones](https://space.bilibili.com/3493268083968463)

由于作者不会Kotlin，因此本软件完全是用Java开发的，写这个项目还锻炼了我的Java水平，也是成功让我爱上Java了。~~Python：终究是错付了~~