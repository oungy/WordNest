# WordNest 开发指南

## 本地与 GitHub

远程仓库为 https://github.com/oungy/WordNest.git，默认分支为 main。保留已有设计提交，首次同步包含设计文档和 Android 工程骨架。当前本地文件夹沿用 `Words_for Android`，Gradle 工程名和应用名为 WordNest。

```powershell
git status
git pull --ff-only
git switch -c codex/your-feature
# 修改并检查代码后，明确列出本次文件：
git add path/to/file
git commit -m "feat: describe the change"
git push -u origin codex/your-feature
```

以上为后续开发示例；分支名和文件路径需替换。API Key、签名文件、设备路径、构建缓存及个人数据都不提交。

## 构建环境

| 配置 | 当前锁定值 |
| --- | --- |
| Android Gradle Plugin | 8.13.2 |
| Gradle Wrapper | 8.13，校验官方 SHA-256 |
| Kotlin / Compose 编译插件 | 2.2.21 |
| Compose BOM | 2025.10.01 |
| DataStore Preferences | 1.1.7 |
| 构建 JDK | 17 或 21，CI 使用 21 |
| Java / Kotlin 字节码 | 17 |
| compileSdk / targetSdk | 36 |
| minSdk | 26（Android 8.0） |
| applicationId | io.github.oungy.wordnest |

这是项目初始锁定组合，不代表各依赖的最新版本。兼容性依据：[AGP 8.13 文档](https://developer.android.com/build/releases/agp-8-13-0-release-notes)、[Compose BOM](https://developer.android.com/develop/ui/compose/bom)。

Android Studio 中将 Gradle JDK 设为 17 或 21，用 SDK Manager 安装 Platform 36 和 Build Tools 35.0.0。SDK 路径由 Android Studio 写入本机 `local.properties`，该文件已忽略。

命令行也可在当前 PowerShell 会话设置环境变量，不需要修改系统全局 Java：

```powershell
$env:JAVA_HOME = 'C:\path\to\jdk-21'
$env:ANDROID_HOME = 'C:\path\to\Android\Sdk'
.\gradlew.bat :app:assembleDebug :app:lintDebug
```

路径为占位示例，必须替换为本机实际位置。当前 Gradle 8.13 不应使用系统 Java 25 启动。调试 APK 输出到 `app/build/outputs/apk/debug/app-debug.apk`。连接设备后可运行 `:app:installDebug`；调试签名不作为未来正式发布身份。

## 目录

```text
app/
  build.gradle.kts
  src/main/
    AndroidManifest.xml
    java/io/github/oungy/wordnest/
      MainActivity.kt
      ui/                 # 首页、共用控件和各功能页面
        theme/
        navigation/      # 首页、外观设置和选书面板的显示状态
        components/      # 共用选择行和模态选书面板
        learning/ library/ reading/ chat/ settings/
      domain/             # 预留：每日计划、词本和复习规则
      data/
        local/settings/   # 用 DataStore 保存外观设置
        remote/           # 预留：DeepSeek 与内容包
        security/         # 预留：Key 加密存储
    res/                  # 文案、主题、临时图标
gradle/                   # Wrapper 与依赖版本目录
.github/workflows/        # 构建与 Lint
design/                   # HTML 交互设计预览
docs/                     # 产品、设计、架构与开发说明
```

空目录通过 `.gitkeep` 跟踪。阶段 0 提供用于界面验收的单选示例词书。当前选择保存在界面状态，主题和减少透明效果设置写入设备。后续会用 Room 保存每日计划和学习记录。

## 阶段 0 验收与代码跟读

1. 安装调试 APK。打开「选择词书」，确认未选中时「完成」禁用，且「阅读词汇」不可选。选中一项后完成，首页应显示选中的词书。再选另一项后取消，首页应保留原选择。
2. 在面板内搜索「日常」，再搜索不存在的内容，检查匹配结果和空态。展开软键盘后按系统返回，第一次隐藏键盘，第二次关闭面板。接入实体键盘后可用方向键或 Home/End 移动选项焦点，用 Enter 或 Space 试选，用 Escape 关闭并检查焦点回到入口。
3. 在「外观」里切换浅色、深色和跟随系统。开启「减少透明效果」后再次打开面板并检查实色背景。强制停止应用并重开，确认外观选项仍生效。在窄屏和大字号下重复一次选书，检查长说明可换行、底部操作可滚动到达。

从 `MainActivity.kt` 跟到 `ui/navigation/WordNestNavHost.kt` 可以看到选书入口和选择状态。面板及搜索在 `ui/components/SelectionSheet.kt`，选书和主题共用的选择行在 `ui/components/ChoiceRow.kt`。主题设置由 `ui/settings/AppearanceScreen.kt` 发出更新，再由 `data/local/settings/PreferencesStore.kt` 写入设备。

下一个开发目标：选中一本或多本测试词书，生成并保存当日计划，完成学习后重启仍能恢复进度。
