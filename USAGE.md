# Bobik 使用指南

## 🚀 首次使用

### 1. 安装
从 GitHub Releases 下载 Bobik APK 并安装到 Android 设备（Android 8.0+）。

### 2. 配置 LLM
打开 Bobik，进入「**设置**」页面：

| 配置项 | 说明 | 示例 |
|--------|------|------|
| **Provider** | LLM 提供商 | DeepSeek（推荐，便宜）/ OpenAI / MiniMax |
| **Model** | 模型名称 | `deepseek-chat` / `gpt-4o` / `abab6.5s-chat` |
| **API Key** | API 密钥 | 从对应平台获取 |

点击「保存配置」，引擎将自动应用新设置。

### 3. 启动引擎
进入「**状态**」页面 → 点击「**启动引擎**」。

引擎启动后将：
- 开始 TICK 循环（默认每 15 秒一个周期，随配额使用量自适应）
- 事件开始出现在「脑图」页面
- 可以切换到「对话」页面开始交流

---

## 📱 页面使用

### 💬 对话（Chat）
与 Bobik AI 对话的主界面。

- 输入消息后点击发送
- AI 回复通过**流式 SSE**实时显示（逐字输出）
- 对话历史自动保存到本地 SQLite
- 未启动引擎时也可对话（手动触发 LLM）

### 🧠 脑图（Brain）
实时监控 Bobik「大脑」的内部活动。

| 事件类型 | 颜色 | 含义 |
|----------|------|------|
| `thought` | 青色 | 思考过程（含流式 LLM 输出） |
| `memory` | 紫色 | 记忆操作 |
| `action` | 橙色 | 执行的动作 |
| `response` | 绿色 | 完整 AI 回复 |
| `error` | 红色 | 错误信息 |
| `system` | 灰色 | 系统事件 |
| `tick` | 蓝色 | TICK 循环计数 |

### 📚 记忆库（Memories）
浏览 Bobik 的持久化记忆。

- 顶部搜索框可关键词搜索
- 记忆按时间倒序排列
- 每条记忆显示：分类标签、标题、内容预览、置信度
- 引擎会自动在水位线触发时进行记忆检索和注入

### 📊 系统状态（Status）
查看引擎运行情况。

- 引擎启停控制
- TICK 计数的实时更新
- LLM 配额使用情况（已用/上限/剩余）
- 数据统计（记忆数、对话记录数）

### ⚙️ 设置（Settings）
- **LLM 配置**：Provider / Model / API Key
- **用户标识**：Bobik 用此 ID 标记消息来源
- **关于**：版本信息和功能简介

---

## 🔧 常见问题

### Q: 如何获取 API Key？
- **DeepSeek**: https://platform.deepseek.com/api_keys
- **OpenAI**: https://platform.openai.com/api-keys
- **MiniMax**: https://platform.minimax.chat

### Q: 引擎启动后没有反应？
- 检查 LLM 配置是否正确（API Key 有效）
- 查看「脑图」是否有 error 事件
- 尝试在「对话」页发送一条消息触发 LLM

### Q: 配额用完了怎么办？
- 每日配额在本地追踪，次日自动重置
- 可在设置页修改 `max_tokens` 或调整 Provider

### Q: 对话数据存在哪里？
- 全部数据存储在设备本地 SQLite 数据库
- 卸载应用会删除所有数据

### Q: 和 Bobi 有什么区别？
- **Bobi** 需要连接电脑上运行的 Bailongma 服务器
- **Bobik** 内置完整 AI 引擎，无需外部服务器
- Bobik = Bailongma 引擎 + Bobi 界面 + 本地数据库

---

## 📊 TICK 循环说明

Bobik 引擎在后台周期性运行 TICK 循环：

```
每个 TICK：
  1. 检查是否有待处理的用户消息
  2. 有消息 → 调用 LLM 生成回复（流式）
  3. 无消息 → 每 5 个 TICK 触发一次自主反思
  4. 更新配额计数
  5. 发布事件到脑图

间隔自适应：
  - 配额充足 (>70%): 15 秒
  - 配额紧张 (50-70%): 30 秒  
  - 配额告急 (<50%): 60 秒+
```

---

## 🔗 相关链接

- [Bobik GitHub](https://github.com/qq00150610-cpu/bobik)
- [Bailongma 项目](https://github.com/qq00150610-cpu/bailongma)
- [Bobi 项目](https://github.com/qq00150610-cpu/bobi)
