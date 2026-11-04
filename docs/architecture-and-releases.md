# 可维护架构与发布约定

日期：2026-11-04。这是实施约定；当前项目只有设计资料，暂无可构建的 Android 工程。

## 结构

先采用单个 Android app module 内的清晰分包，不为尚不存在的规模引入大量 Gradle module。

- ui：Compose 主题、导航和按 learning / library / reading / chat / settings 划分的页面。
- domain：词本增删规则、复习规则、阅读目标词选择与覆盖检查；不依赖网络和 UI。
- data/local：Room 数据、事务、查询和迁移；一般偏好可使用 DataStore。
- data/remote：DeepSeek 客户端、词书清单与更新下载，均通过可替换的数据接口供业务使用。
- data/security：Key 加密存储；签名密钥属于发布基础设施，不属于应用数据。

## 数据边界

- Word / WordSense：词条与词义；使用稳定 ID。拼写相同但语言、词性或含义不同的项不可盲目合并。
- LearningEvent：每次学习和复习结果的事件；更改词本不会删除事件。
- ReviewState：当前复习状态，规则版本与下次复习时间。
- SavedWord：个人生词收藏。
- Notebook + NotebookWord：用户集合，多对多关系；组合键防止重复词；删除 Notebook 仅删除关联。
- WordPack + WordPackEntry：学习词书及条目关系；清单版本、内容版本、词条身份分开。
- ChatSession + ChatMessage：本地对话，携带上下文快照；Key 不进入消息表。
- GeneratedReading：文章、目标词 ID、目标词义／词形、覆盖结果、模型及提示模板版本。
- UserNote：用户注释与来源内容分离，词书升级不覆盖笔记。

“删除词本后学习记录还在”“同一词重复导入不重置进度”“内容包更新不覆盖笔记”均属于必须验证的规则。

## 词书包格式建议

清单至少包括 packId、displayName、category、sourceUrl、sourceEdition、publisherOrCompiler、licenseIdOrTermsUrl、redistributionStatus、contentVersion、formatVersion、minSupportedAppVersion、entryCount、sha256。

词表、释义、例句、音标和音频的来源与许可分别记录，不能用词表来源替代其他内容的授权。编译时只打包已确认可分发的内容；出处待核实的材料只能留在研究记录中。

内容包更新：获取清单 → 验证下载来源与格式兼容 → 校验哈希及条目数 → 暂存解析 → 事务更新 → 保留个人数据。哈希只能证明内容与可信清单一致，不能单独证明发布者身份。

## DeepSeek 接口约定

默认 HTTPS 服务地址为 https://api.deepseek.com，使用官方模型列表与 /chat/completions 接口。模型列表和 chat 的具体字段以实际实施时官方文档为准。

API 接入包含：

1. Key 加密保存、修改和删除；无 Key 时显示明确配置入口。
2. 读取可用模型；用户主动发起简短聊天测试，展示实际结果而非模拟成功。
3. 流式聊天解析；发送期间允许取消；中止响应标记不完整，不能当成完整文章保存。
4. 有界上下文构建，超过预算裁剪较早内容并告知；不会给每次聊天附加整个词库。
5. 对鉴权、余额／配额、限流、超时和断网分别提示；限制自动重试，生成类请求不无条件重放。
6. UI 与服务层分离；本地测试使用可明确识别的测试替身，生产路径使用真实客户端。

原生接入验收：在设备配置用户 Key，完成跨两轮的真实问答、取消输出、无效 Key 错误、断网背词、基于选定词的阅读生成与本地保存。当前设计预览不满足该验收。

## 四种版本分别维护

| 类型 | 规则 | 举例（规划） |
| --- | --- | --- |
| 应用对外版本 | 明确标识预发布；按兼容性、功能、修复递增 | 0.1.0-alpha.1 → 0.1.0 |
| Android versionCode | 每次发布严格递增整数，不从日期随意拼接 | 1 → 2 → 3 |
| Room schema version | 结构变化递增并提供迁移路径 | 1 → 2 |
| 词书内容／格式版本 | 内容修订与文件格式变更独立 | content 2026.1 / format 1 |

设计稿 Revision 2 不是已发布的 App 0.2.0。

## 应用升级

- applicationId 与发布签名身份保持连续；正式签名密钥安全备份，不提交 Git。
- 数据库 schema 导出并纳入版本控制；每次变化编写迁移并验证，覆盖跨版本升级。
- 生产数据禁止依赖 destructive migration；升级不能为了成功启动就清空学习记录。
- 备份有格式版本和恢复检查；损坏、版本过新或来源不明的备份不能覆盖当前数据。
- 应用检查更新可由 GitHub Releases 提供版本清单，展示版本与变更说明，再由用户选择下载安装。
- 更新安装遵循系统安装界面，不静默安装；检查签名兼容与版本，不能只依据文件名。
- 降级不假定兼容；首先保护备份，明确旧版可读范围。

## 开源与 GitHub

当前先使用本地 Git 保存项目资料，尚不创建远程仓库、不推送、不发布安装包。用户表达的未来可能开源不是本次公开发布指令。

公开前准备 README、构建说明、明确开源许可证、CHANGELOG、贡献指南、问题模板、依赖和内容来源清单。代码许可和词书／音频许可分别管理；暂不替用户选定不可逆的公开许可。

未来 CI 验证构建、静态检查、核心规则、数据库迁移和导入兼容性；Release 发布带版本标签的安装包、校验值和变更说明。签名凭据放在私密发布环境，普通 PR 不接触发布密钥。

## 依据

- [Android 应用版本](https://developer.android.com/studio/publish/versioning)
- [Android 应用签名](https://developer.android.com/studio/publish/app-signing)
- [Room 数据库迁移](https://developer.android.com/training/data-storage/room/migrating-db-versions)
- [DeepSeek 聊天接口](https://api-docs.deepseek.com/api/create-chat-completion/)
- [GitHub 仓库许可证说明](https://docs.github.com/en/repositories/managing-your-repositorys-settings-and-features/customizing-your-repository/licensing-a-repository)
