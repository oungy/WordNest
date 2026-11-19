# WordNest / 拾词

拾词是一款本地优先的 Android 英语词汇学习应用，使用 Kotlin、Jetpack Compose 和 Room 开发。当前原生调试版为 **0.1.0-alpha.4（阶段 2）**，背词、复习和个人词本均可离线使用。

## 可以怎样使用

- 在**今日**选择一本或多本词书，设定每日新词总量。首次开始学习时会固定当天的词序，退出后可以接着上次的进度学习。当前内置两本测试词书，共 16 个不同词条。
- 在**到期复习**中回顾之前学过的词，根据回忆情况选择「忘记」「模糊」或「记得」。取消某本词书的在学状态后，其中已学词仍会按期出现。
- 在**词本**中查看生词、错词、已学和已掌握。可以搜索并批量选词，也可以从词条详情把一个词加入多个自定义词本。删除词本后，收藏、其他词本中的归属和学习记录都会保留。

## 安装与升级

可从成功的构建记录中下载 `WordNest-debug`，解压后安装 APK。构建产物保留 14 天。[GitHub Actions 构建记录](https://github.com/oungy/WordNest/actions/workflows/android.yml)

已安装阶段 1（`0.1.0-alpha.3`）时，使用相同签名的阶段 2 APK 直接覆盖安装即可保留数据。已在 Android 16 模拟器验证升级后能从原词序继续学习，并保留到期复习。来自不同构建环境的调试 APK 可能使用不同签名。

阶段 2 已通过 4 项本地单元测试和 8 项模拟器设备测试。设备验收还覆盖了保存时旋转屏幕、浅深色面板，以及 320dp 窄屏配合 200% 字号的操作。[验收步骤与代码跟读](docs/development.md#阶段-2-验收与代码跟读)

## 开始开发

使用支持 AGP 8.13 的 Android Studio 打开本仓库根目录，等待 Gradle 同步。构建需要 JDK 17 或 21、Android SDK Platform 36 和 Build Tools 35.0.0，最低运行版本为 Android 8.0（API 26）。

Windows 命令行：

```powershell
.\gradlew.bat :app:assembleDebug :app:testDebugUnitTest :app:lintDebug
```

macOS / Linux 使用 `./gradlew :app:assembleDebug :app:testDebugUnitTest :app:lintDebug`。首次构建需要访问 Google Maven、Maven Central、Gradle Plugin Portal 和 Gradle 分发服务。

GitHub Actions 在推送到 `main` 或创建 Pull Request 时执行构建、单元测试和 Lint。连接模拟器或 Android 设备后，可另外运行 `:app:connectedDebugAndroidTest` 验证数据库迁移、学习记录和词本操作。完整构建说明见[开发指南](docs/development.md)。

## 设计资料

- [产品与界面规范](docs/product-design.md)
- [统一视觉与交互规范](docs/design-system.md)
- [架构、接口验收与版本发布约定](docs/architecture-and-releases.md)
- [词书来源核查](docs/content-sources.md)
- [变更记录](CHANGELOG.md)
- [可点击设计预览源码](design/words-preview.html)（设计模拟，非 Android 实现）

## 后续计划

下一阶段将接入真实的 DeepSeek 聊天 API。语境阅读、完整词书、音频和数据备份仍在规划中。

代码仓库：[oungy/WordNest](https://github.com/oungy/WordNest)。尚未发布正式 App；项目代码许可证待确定，公开仓库不代表已授予开源许可。第三方词书、字典和构建工具遵循各自许可，不能自动归入将来的项目许可证。API Key、个人学习数据和签名密钥均不属于仓库内容。
