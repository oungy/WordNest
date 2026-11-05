# WordNest / 拾词

本地优先的 Android 英语词汇学习应用。当前已建立 Kotlin + Jetpack Compose 工程骨架，原生页面仅为欢迎页；完整学习流程仍在开发中。

确定的技术路线：Kotlin、Jetpack Compose、Room、DeepSeek 普通聊天 API。

目标流程：选择一本或多本在学词书 → 每日新词与到期复习 → 语境阅读与 AI 讲解。Room 与真实 DeepSeek 接入尚未实现。

## 开始开发

使用支持 AGP 8.13 的 Android Studio 打开本仓库根目录，等待 Gradle 同步。构建需要 JDK 17 或 21、Android SDK Platform 36 和 Build Tools 35.0.0，最低运行版本为 Android 8.0（API 26）。

Windows 命令行：

```powershell
.\gradlew.bat :app:assembleDebug :app:lintDebug
```

macOS / Linux 使用 `./gradlew :app:assembleDebug :app:lintDebug`。首次执行会下载 Gradle 与依赖，需要访问 Google Maven、Maven Central 和 Gradle 分发服务。

详细的环境配置、目录说明和 Git 操作见 [开发指南](docs/development.md)。GitHub Actions 对 main 推送与 Pull Request 执行构建和 Lint；通过后保存临时调试 APK 构建产物，不自动发布正式版本。

## 设计资料

- [产品与界面规范](docs/product-design.md)
- [统一视觉与交互规范](docs/design-system.md)
- [架构、接口验收与版本发布约定](docs/architecture-and-releases.md)
- [词书来源核查](docs/content-sources.md)
- [变更记录](CHANGELOG.md)
- [可点击设计预览源码](design/words-preview.html)（设计模拟，非 Android 实现）

## 当前状态

已完成可点击界面设计：背词、阅读、自定义词本、主题和语言设置、DeepSeek 配置入口、词书分类。HTML 设计预览与原生 App 是两个独立产物。

已建立：单个 app module、Gradle Wrapper、依赖版本目录、原生欢迎页、浅深主题、中英文欢迎文案和 GitHub 构建流程。

尚待实现：原生界面、本地持久化与复习逻辑、真实 LLM、可分发的完整词书、音频、备份、版本检查与安装升级。

代码仓库：[oungy/WordNest](https://github.com/oungy/WordNest)。尚未发布正式 App；项目代码许可证待确定，公开仓库不代表已授予开源许可。第三方词书、字典和构建工具遵循各自许可，不能自动归入将来的项目许可证。API Key、个人学习数据和签名密钥均不属于仓库内容。
