# ManagerAssistant

这是一款极简记账工具，秉持“简洁无广、隐私保护”的核心理念，为用户提供清爽、安全的记账体验

## 特色功能

### 流水标签

- 每条流水记录均可使用最多一个标签标记，便于快速查询流水记录信息
- 设置标签分组便于用户将各个标签分门别类
- 在通知解析规则中设置标签能够在通知内容解析成功后自动添加带有标签的流水记录

### 基于应用通知解析的自动记账

- 支持添加多条通知解析规则，以生成不同的流水记录
- 通过读取其他应用发送的通知并根据用户设置的规则解析其内容，动态生成流水记录以实现自动记账
- 通知内容解析完全本地运行，用户无需担心隐私泄露问题

### 报表统计

- 支持选择统计的日期和日期范围
- 根据流水记录的标签动态生成收支来源，并计算它们的比例

### 其他

- 使用Material You主题，为用户提供简洁流畅的UI界面
- 支持一键式导出和导入应用数据，避免数据丢失
- 所有功能均在本地实现，运行高效的同时也避免了隐私泄露问题

## 效果展示

<div style="display: flex; justify-content: center; gap: 10px;">
  <img src="https://github.com/user-attachments/assets/407b9b59-efed-4c9f-a564-d9a83a02ed9e" width="19%" alt="记账"/>
  <img src="https://github.com/user-attachments/assets/0f4b5bd0-900c-472d-8820-0505600a36fb" width="19%" alt="主页"/>
  <img src="https://github.com/user-attachments/assets/ee3f7b8a-8f01-45af-8b06-637bc00a4aa2" width="19%" alt="报表"/>
  <img src="https://github.com/user-attachments/assets/5225cad4-56b7-4fd3-941d-b228b1ea6f8b" width="19%" alt="标签管理"/>
  <img src="https://github.com/user-attachments/assets/292d4ca2-5b2f-453d-b094-9b18dfb86367" width="19%" alt="通知解析规则管理"/>
</div>

## 权限说明

***本应用不含任何网络服务，因此不会也无法获取用户隐私或推送广告，请您放心使用***

- 应用列表权限：用于扫描应用列表，供用户快捷输入包名
- 自启动权限：用于在启动时自动运行通知监听服务（国产系统授予此权限才能监听通知）

## 软件下载

- Github仓库:https://github.com/E-zhiyu/ManagerAssitant/releases
- 123云盘:https://www.123865.com/s/C5xcVv-kRYT3