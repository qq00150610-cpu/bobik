# Bobik — 自包含 Android AI Agent

[![Build & Release APK](https://github.com/qq00150610-cpu/bobik/actions/workflows/build.yml/badge.svg)](https://github.com/qq00150610-cpu/bobik/actions/workflows/build.yml)

**Bobik** 是 [Bailongma（白龙马）](https://github.com/qq00150610-cpu/bailongma) 数字意识框架与 [Bobi](https://github.com/qq00150610-cpu/bobi) Android 客户端的融合体——一个完全自包含的 Android AI Agent 应用。

> ⚡ **无需外部服务器**：Bobik 内置完整的 AI 引擎，所有数据存储在本地 SQLite 数据库。仅 LLM 调用需要网络。

## 🧠 核心特性

| 模块 | 来源 | 说明 |
|------|------|------|
| **AI 引擎** | Bailongma | TICK 循环、双层思考（L1/L2）、持久化记忆、多 LLM 支持 |
| **对话界面** | Bobi | 实时聊天、消息气泡、流式 LLM 回复 |
| **脑图** | Bobi | 意识事件流实时监控（思考/记忆/行动/错误） |
| **记忆库** | Bobi | 语义搜索、分类筛选、置信度显示 |
| **系统状态** | Bobi | TICK 计数、配额使用、引擎启停 |
| **LLM 配置** | 新增 | DeepSeek / OpenAI / MiniMax 自由切换 |

## 🏗 技术架构

```
┌─────────────────────────────────────────┐
│                Bobik APK                 │
│  ┌──────────────┐  ┌──────────────────┐ │
│  │  Compose UI   │  │   Agent Engine   │ │
│  │  (5 个页面)    │◄─┤   (Kotlin)       │ │
│  │  Chat/Brain/  │  │  TICK 循环       │ │
│  │  Memories/    │  │  LLM 客户端      │ │
│  │  Status/      │  │  Prompt 构建     │ │
│  │  Settings     │  │  配额追踪        │ │
│  └──────────────┘  │  事件总线        │ │
│                     └──────┬───────────┘ │
│                            │             │
│                     ┌──────▼───────────┐ │
│                     │  Room (SQLite)    │ │
│                     │  Memories/Conv/   │ │
│                     │  Config           │ │
│                     └──────────────────┘ │
└─────────────────────────────────────────┘
          │                    │
          ▼                    ▼
   DeepSeek / OpenAI    用户设备本地
   / MiniMax API        SQLite 数据库
```

## ✨ 与 Bobi 的区别

| 对比项 | Bobi | Bobik |
|--------|------|-------|
| 运行方式 | 客户端→连接 Bailongma 服务器 | **自包含**，无需外部服务器 |
| AI 引擎 | 依赖远程服务器 | **内置** TICK 循环引擎 |
| 数据存储 | 服务器端 | **本地** SQLite (Room) |
| LLM 配置 | 在服务器 Web 面板 | **App 内设置页**直接配置 |
| 离线能力 | 完全依赖网络 | 仅 LLM 调用需网络 |

## 📦 安装

从 [Releases](https://github.com/qq00150610-cpu/bobik/releases) 下载最新 APK：

- `app-debug.apk` — Debug 版本
- `app-release-unsigned.apk` — Release 版本

> Android 8.0+ (API 26+)

## 🚀 快速开始

1. 安装 Bobik APK
2. 进入「设置」→ 配置 LLM（选择 Provider + 填写 API Key）
3. 进入「状态」→ 点击「启动引擎」
4. 切换到「对话」→ 开始聊天

## 📁 项目结构

```
bobik/
├── app/src/main/java/com/example/bobik/
│   ├── BobikApp.kt                     # Application（初始化引擎+数据库）
│   ├── MainActivity.kt                 # Compose 入口
│   ├── engine/                         # 🤖 AI 引擎层 (from bailongma)
│   │   ├── AgentEngine.kt              # 主控制器
│   │   ├── TickDriver.kt               # TICK 循环驱动
│   │   ├── LLMClient.kt                # 多 Provider LLM 客户端
│   │   ├── PromptBuilder.kt            # System Prompt 构建
│   │   ├── QuotaTracker.kt             # Token 配额追踪
│   │   └── EventBus.kt                 # 内部事件总线
│   ├── data/
│   │   ├── db/AppDatabase.kt           # Room 数据库 (SQLite)
│   │   └── models/Models.kt            # UI 层数据模型
│   ├── navigation/AppNavigation.kt     # 底部导航 (5 Tab)
│   ├── ui/
│   │   ├── screens/                    # 5 个页面
│   │   │   ├── ChatScreen.kt
│   │   │   ├── BrainScreen.kt
│   │   │   ├── MemoriesScreen.kt
│   │   │   ├── StatusScreen.kt
│   │   │   └── SettingsScreen.kt
│   │   └── theme/Theme.kt
│   └── viewmodel/MainViewModel.kt      # MVVM 状态管理
└── .github/workflows/build.yml         # CI 自动构建
```

## 🔧 构建

```bash
git clone https://github.com/qq00150610-cpu/bobik.git
# 在 Android Studio 中打开，Sync Gradle 后运行
```

> 构建环境: Gradle 8.5 + AGP 8.2.2 + Kotlin 1.9.22 + Room (KSP)

## 📄 许可

MIT License

## 🔗 相关项目

- [Bailongma](https://github.com/qq00150610-cpu/bailongma) — Electron 版数字意识框架
- [Bobi](https://github.com/qq00150610-cpu/bobi) — Bailongma 的轻量 Android 客户端
- **Bobik** — 两者的融合，自包含的 Android AI Agent
