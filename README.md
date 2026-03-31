# ManagerAssistant

这是一款极简记账工具，秉持“简洁无广、隐私保护”的核心理念，为用户提供清爽、安全的记账体验

## 功能概述

### 流水记录

- 流水记录可以记录每笔交易的金额、种类、日期、备注等信息
- 记账界面中会根据流水记录的日期进行分类，用户可以查看和编辑流水记录
- 记账界面包含流水过滤功能，用户可以快速筛选目标流水记录
- 每条流水记录都可以被一个标签标记，便于日后查询，也可配合其他自动化功能使用

### 自动记账

- 基于安卓的NotificationListenerService，通过解析通知内容实现自动记账
- 触发自动记账后发送横幅通知提示用户是否保留刚生成的记录
- 支持自定义通知解析规则，以适配不同用户的实际需求

### 预算管理

- 预算项包含起算日期、金额、重置频率等属性，且支持同时存在多个预算项
- 当流水记录更新时，软件会自动计算对应标签的预算余额
- 当预算余额低于一定范围时，会发送通知提醒用户
- 用户可以重置预算，将余额重置为初始值并将起算日期更新为当前日期
- 预算重置包含手动和自动两种方式，软件会在每天0点自动重置需要重置的预算

### 流水图片

- 可以将多个图片添加到某条流水记录中，可以用于保存凭据并便于日后快速了解交易内容
- 可以通过内置拍照功能和相册导入两种方式添加图片
- 在流水输入界面点击图片可以以全屏模式查看大图，支持保存到系统相册或者分享到其他应用
- 在流水输入界面长按图片进入图片编辑模式，此时可以选择需要删除的图片并点击“删除图片”按钮确认删除
- 在图片编辑模式时返回即可回到查看模式，再次返回将关闭流水输入界面

### 流水标签

- 每条流水记录均可使用标签标记
- 每个标签都有对应的分组，便于用户分类
- 软件的各种自动化功能大多需要流水标签的参与
- 每个标签都有其作用域，在流水输入界面添加标签时只能看到对应作用域的标签

### 报表统计

- 在主页会自动生成今日简易报表，点击简易报表视图后将跳转报表详情界面
- 软件会自动统计日期范围内的流水记录，并根据标签显示各收支来源的金额占比

### 数据管理

- 所有应用数据完全保存在本地，不会上传至云端，保障用户隐私安全
- 支持一键式导入和导出数据，防止出现意外情况导致数据丢失
- 支持自动备份，在指定备份目录后会以一定时间间隔自动生成备份文件，用户可以定期将备份文件保存至其他地方

### 界面显示

- 使用Material You主题，为用户提供简洁直观的UI界面
- 软件不包含任何广告，告别国产软件各种辣眼广告
- 专注于记账功能，不包含其他杂乱的功能，告别国产超级APP某个功能找半天的情况

## 效果展示

<div style="display: flex; justify-content: center; gap: 10px;">
  <img src="https://github.com/user-attachments/assets/6fc0e0bb-3f68-49e8-a49a-02103183db15" width="19%" alt="记账"/>
  <img src="https://github.com/user-attachments/assets/7dbc4e12-3a2e-4c0c-915d-97be9071f32c" width="19%" alt="主页"/>
  <img src="https://github.com/user-attachments/assets/54acae40-c0e3-4001-89ee-84da27e06b91" width="19%" alt="报表"/>
  <img src="https://github.com/user-attachments/assets/b72f4eb5-832b-4f02-a88d-abce51c23dd2" width="19%" alt="标签管理"/>
  <img src="https://github.com/user-attachments/assets/7e56588f-cfa6-46d1-9409-9d8794a7a0fb" width="19%" alt="解析规则管理"/>
</div>

## 权限使用说明

#### 摄像头权限

- 在应用内拍照，为流水记录添加图片

#### 应用列表权限

- 在自定义通知解析规则时快速输入包名

#### 通知权限

- 预算余额低时发送通知提醒用户
- 触发自动记账后发送通知，让用户决定是否保留刚产生的记录

#### 通知监听权限

- 解析其他软件发送的通知，以实现自动记账

#### 自启动权限

- 确保通知监听服务能够在后台正常运行，缺少该权限可能导致无法使用自动记账功能

#### “无限制”电池优化策略

- 确保应用能在后台发送通知，严格的电池优化策略将导致触发自动记账后通知发送不及时
- 自动授予精确闹钟权限

#### 精确闹钟权限

- 用于在每天0点自动检查并重置预算项

#### 系统启动监听权限

- 用于在开机后自动安排第二天0点的预算重置闹钟

## 软件下载

- [Github仓库](https://github.com/E-zhiyu/ManagerAssitant/releases)
- [Gitee仓库](https://gitee.com/e-zhiyu/manager-assistant-web/releases)
- [123云盘](https://www.123865.com/s/C5xcVv-kRYT3)
- [小飞机网盘](https://share.feijipan.com/s/kTVc2PiI)

## 使用到的第三方库

- [Material Components](https://github.com/material-components/material-components-android)
- [Groupie](https://github.com/lisawray/groupie)
- [RxJava](https://github.com/ReactiveX/RxJava)
- [Glide](https://github.com/bumptech/glide)
- [PhotoView](https://github.com/Baseflow/PhotoView)