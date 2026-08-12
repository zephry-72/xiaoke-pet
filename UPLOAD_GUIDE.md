# 小克AI桌宠 - GitHub上传指南

## 📦 项目文件已准备好

源码压缩包位置：`/sdcard/Download/xiaoke-pet-source.tar.gz`

## 🚀 上传到GitHub的步骤

### 方法一：网页上传（最简单）

1. **创建新仓库**
   - 访问 https://github.com/new
   - Repository name: `xiaoke-pet`
   - Description: `小克AI桌宠 - 让AI从对话框溢出到屏幕上`
   - 选择 Public
   - ❌ 不要勾选 "Add a README file"、".gitignore" 或 "license"
   - 点击 "Create repository"

2. **上传代码**
   - 解压 `/sdcard/Download/xiaoke-pet-source.tar.gz`
   - 在新仓库页面，点击 "uploading an existing file"
   - 把解压后的所有文件拖进去（注意：要包含 `.github` 文件夹！）
   - Commit message 填: `初始提交`
   - 点击 "Commit changes"

3. **触发自动编译**
   - 上传完成后，GitHub会自动开始编译
   - 点击仓库上方的 "Actions" 标签
   - 等待绿色✅出现（大约5-8分钟）
   - 点击workflow → "Artifacts" → 下载 `xiaoke-pet-release`

### 方法二：命令行上传（如果你有git客户端）

```bash
# 解压项目
cd ~/Downloads
tar -xzf xiaoke-pet-source.tar.gz
cd ai-pet

# 上传到GitHub（替换成你的用户名）
git remote add origin https://github.com/你的用户名/xiaoke-pet.git
git branch -M main
git push -u origin main
```

## ⚡ 关键点

1. **`.github/workflows/build.yml` 必须上传**  
   这是自动编译的配置文件，没有它就无法自动编译

2. **首次push后自动开始编译**  
   不需要任何额外配置，GitHub会识别到workflow文件

3. **编译产物在Artifacts里**  
   点击绿色✅的workflow运行记录 → 页面底部 "Artifacts" 区域

4. **如果需要手动触发编译**  
   Actions → "Build APK" → "Run workflow" → 选择分支 → "Run"

## 📱 下载APK后

1. 解压 artifact 压缩包，得到 `app-release.apk`
2. 传到手机安装
3. 按照应用提示授予三个权限
4. 小克就会出现在屏幕上啦~ 🦀