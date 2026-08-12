# 小克AI桌宠 🦀

让AI从对话框"溢出"到屏幕上的Android悬浮窗应用

## 功能特点

- 🎨 12种像素风表情动画（idle/happy/shy/work/study等）
- 👆 手势交互（双击/长按/拖拽）
- 📊 App使用情况监控
- ☁️ Supabase云端同步
- 🤖 AI实时反馈

## 技术架构

- **语言**: Kotlin
- **最低版本**: Android 8.0 (API 26)
- **核心技术**:
  - 悬浮窗服务 (SYSTEM_ALERT_WINDOW)
  - WebView + SVG动画
  - Supabase Realtime
  - 使用情况统计 (UsageStatsManager)

## 快速开始

### 编译APK

项目使用GitHub Actions自动编译，每次push到main/master分支都会自动构建：

1. 进入 [Actions](../../actions) 页面
2. 找到最新的workflow运行记录
3. 下载 `xiaoke-pet-release` artifact

或本地编译：
```bash
./gradlew assembleRelease
```

### 安装使用

1. 安装APK后打开应用
2. 依次授予权限：
   - 悬浮窗权限
   - 使用情况访问权限
   - 通知权限
3. 启动服务，小克就会出现在屏幕上~

### 交互方式

- **双击**: 打招呼
- **长按**: 查看详细状态
- **拖拽**: 移动位置

## Supabase配置

应用需要连接Supabase后端，数据库包含三张表：

1. `gesture_log` - 手势日志
2. `app_usage` - App使用记录
3. `pet_state` - 宠物状态（AI反馈）

## 许可证

MIT License
