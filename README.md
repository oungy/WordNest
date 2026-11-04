# 拾词 / Words for Android

本地优先的 Android 英语词汇学习应用，暂用名“拾词”。目前处于产品和交互设计阶段，尚无 Android 工程、APK 或真实 DeepSeek 调用。

确定的技术路线：Kotlin、Jetpack Compose、Room、DeepSeek 普通聊天 API。

## 设计资料

- [产品与界面规范](docs/product-design.md)
- [统一视觉与交互规范](docs/design-system.md)
- [架构、接口验收与版本发布约定](docs/architecture-and-releases.md)
- [词书来源核查](docs/content-sources.md)
- [变更记录](CHANGELOG.md)
- [可点击设计预览源码](design/words-preview.html)（设计模拟，非 Android 实现）

## 当前状态

已完成可点击界面设计：背词、阅读、自定义词本、主题和语言设置、DeepSeek 配置入口、词书分类。

尚待实现：原生界面、本地持久化与复习逻辑、真实 LLM、可分发的完整词书、音频、备份、版本检查与安装升级。

本项目尚未公开发布。开源许可证待确定；不会把第三方词书或字典内容自动归入将来的代码许可证。API Key、个人学习数据和签名密钥均不属于仓库内容。
