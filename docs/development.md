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
      ui/                 # 欢迎页、主题，以及预留的功能目录
        theme/
        learning/ library/ reading/ chat/ settings/
      domain/             # 预留：每日计划、词本和复习规则
      data/
        local/            # 预留：Room 和本地设置
        remote/           # 预留：DeepSeek 与内容包
        security/         # 预留：Key 加密存储
    res/                  # 文案、主题、临时图标
gradle/                   # Wrapper 与依赖版本目录
.github/workflows/        # 构建与 Lint
design/                   # HTML 交互设计预览
docs/                     # 产品、设计、架构与开发说明
```

空目录通过 `.gitkeep` 跟踪，不表示已有功能实现。初始欢迎页没有学习、选书、网络或数据存储行为；当前也没有业务单元测试。此阶段用构建与 Android Lint 验证工程，后续加入业务规则时再添加对应测试。

## 当前检查与后续验收

本地已校验 Gradle Wrapper 来源、SHA-256 并验证启动。完整构建与 Lint 的结果以 GitHub Actions 当前提交对应的运行结果为准。尚未进行真机或模拟器运行验收；Compose 浅深色预览函数不等于实际运行测试。

欢迎页采用实色、系统浅深主题、安全边距和可滚动布局。后续添加交互时，必须验证窄屏、大字号、展开浮层、选中／禁用、返回关闭与焦点恢复，遵循统一设计规范。

下一个开发目标：选中一本或多本测试词书，生成并保存当日计划，完成学习后重启仍能恢复进度。
