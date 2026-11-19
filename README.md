# WordNest / 拾词

本地优先的 Android 英语词汇学习应用。阶段 2 调试版支持离线背词、到期复习和个人词本。可以在「词本」里查看学习记录、收藏生词，把同一个词加入多个自定义词本。

确定的技术路线：Kotlin、Jetpack Compose、Room、DeepSeek 普通聊天 API。

目标流程为多词书选学、每日新词与到期复习、语境阅读与 AI 讲解。

## 开始开发

使用支持 AGP 8.13 的 Android Studio 打开本仓库根目录，等待 Gradle 同步。构建需要 JDK 17 或 21、Android SDK Platform 36 和 Build Tools 35.0.0，最低运行版本为 Android 8.0（API 26）。

Windows 命令行：

```powershell
.\gradlew.bat :app:assembleDebug :app:testDebugUnitTest :app:lintDebug
```

macOS / Linux 使用 `./gradlew :app:assembleDebug :app:testDebugUnitTest :app:lintDebug`。首次构建需要访问 Google Maven、Maven Central、Gradle Plugin Portal 和 Gradle 分发服务。

构建环境和各阶段验收步骤见 [开发指南](docs/development.md)。GitHub Actions 对 main 推送与 Pull Request 执行构建、单元测试和 Lint，通过后保存临时调试 APK。

## 设计资料

- [产品与界面规范](docs/product-design.md)
- [统一视觉与交互规范](docs/design-system.md)
- [架构、接口验收与版本发布约定](docs/architecture-and-releases.md)
- [词书来源核查](docs/content-sources.md)
- [变更记录](CHANGELOG.md)
- [可点击设计预览源码](design/words-preview.html)（设计模拟，非 Android 实现）

## 当前状态

已完成可点击界面设计：背词、阅读、自定义词本、主题和语言设置、DeepSeek 配置入口、词书分类。HTML 设计预览与原生 App 是两个独立产物。

原生版内置两本测试词书，共 16 个不同词条。主题、减少透明效果、在学词书、每日目标和作答进度均跨启动保存。阶段 1 覆盖升级到阶段 2 时，Room 自动迁移到 schema 2。完整词书、阅读、DeepSeek、音频及备份仍在规划中。

代码仓库：[oungy/WordNest](https://github.com/oungy/WordNest)。尚未发布正式 App；项目代码许可证待确定，公开仓库不代表已授予开源许可。第三方词书、字典和构建工具遵循各自许可，不能自动归入将来的项目许可证。API Key、个人学习数据和签名密钥均不属于仓库内容。
