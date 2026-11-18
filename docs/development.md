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
| Room / KSP | 2.8.4 / 2.3.6 |
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
.\gradlew.bat :app:assembleDebug :app:testDebugUnitTest :app:lintDebug
```

路径为占位示例，必须替换为本机实际位置。当前 Gradle 8.13 不应使用系统 Java 25 启动。调试 APK 输出到 `app/build/outputs/apk/debug/app-debug.apk`。连接设备后可运行 `:app:installDebug`；调试签名不作为未来正式发布身份。

## 目录

```text
app/
  build.gradle.kts
  schemas/                # Room 导出的数据库结构
  src/test/               # 本地规则测试
  src/androidTest/        # 设备上的 Room 事务与持久化测试
  src/main/
    AndroidManifest.xml
    java/io/github/oungy/wordnest/
      MainActivity.kt
      ui/                 # 首页、共用控件和各功能页面
        theme/
        navigation/      # 今日、练习、外观与面板切换
        components/      # 选择行、操作按钮和模态面板
        learning/ library/ reading/ chat/ settings/
      domain/             # 每日选词和复习间隔
      data/
        local/settings/   # 用 DataStore 保存外观设置
        local/study/      # Room 词书、每日计划、学习记录和复习状态
        remote/           # 预留：DeepSeek 与内容包
        security/         # 预留：Key 加密存储
    res/                  # 文案、主题、临时图标
gradle/                   # Wrapper 与依赖版本目录
.github/workflows/        # 构建与 Lint
design/                   # HTML 交互设计预览
docs/                     # 产品、设计、架构与开发说明
```

## 阶段 1 验收与代码跟读

1. 在「今日」多选两本测试词书，把每日新词目标改为 3，再开始学习。应依次看到 notice、route、steady。释义和例句在点击「查看释义」后出现。
2. 评完第一个词后强制停止并重开。今日应显示已学 1 / 3，继续学习应从 route 开始。完成计划后再重开，首页仍应显示今日计划已完成。
3. 生成计划后修改词书或目标，今日应保留原计划。把所有词书取消在学，次日新词区应提示选书，前一天评为「忘记」的词仍应出现在到期复习。
4. 在独立测试设备上断网重复背词。用浅色、深色和减少透明效果分别打开选书与目标面板，再检查窄屏、大字、禁用项、搜索空态、软键盘返回和实体键盘关闭后焦点归位。

连接测试设备或模拟器后可运行 `:app:connectedDebugAndroidTest`，验证 Room 事务、同日复用、共享词进度、换书后的复习和数据库重开。纯规则测试通过 `:app:testDebugUnitTest` 运行。

从 `MainActivity.kt` 跟到 `ui/learning/StudyViewModel.kt` 查看今日与练习的状态管理。计划生成和作答事务在 `data/local/study/StudyRepository.kt`，分配和间隔规则在 `domain/StudyRules.kt`。

## 阶段 0 调试版验收记录

1. 安装调试 APK。打开「选择词书」，确认未选中时「完成」禁用，且「阅读词汇」不可选。选中一项后完成，首页应显示选中的词书。再选另一项后取消，首页应保留原选择。
2. 在面板内搜索「日常」，再搜索不存在的内容，检查匹配结果和空态。展开软键盘后按系统返回，第一次隐藏键盘，第二次关闭面板。接入实体键盘后可用方向键或 Home/End 移动选项焦点，用 Enter 或 Space 试选，用 Escape 关闭并检查焦点回到入口。
3. 在「外观」里切换浅色、深色和跟随系统。开启「减少透明效果」后再次打开面板并检查实色背景。强制停止应用并重开，确认外观选项仍生效。在窄屏和大字号下重复一次选书，检查长说明可换行、底部操作可滚动到达。
