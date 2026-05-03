# 心光

心光是一个面向大学生心理健康支持场景的应用原型，包含 Android 客户端、FastAPI 后端和 Web 管理端。项目提供情绪记录、AI 陪伴对话、心理知识库检索增强、风险监测、反馈和管理员处理等功能。

> 本项目不是医疗诊断工具，不能替代专业心理咨询、精神科诊疗或紧急救援服务。

## 项目结构

```text
.
├── app/          # Android 客户端，Jetpack Compose + Room + Hilt + Retrofit
├── backend/      # FastAPI 后端，SQLite + JWT + RAG 相关接口
├── web-admin/    # 管理端网页，React + Vite + TypeScript
├── knowledge/    # 心理知识库数据
├── gradle/       # Gradle wrapper 依赖
└── *.gradle.kts  # Android/Gradle 根配置
```

## 隐私与安全

公开版本已移除本地数据库、真实 `.env`、部署脚本、临时文档、构建产物、内部规格文档和原型文件。仓库中只保留 `.env.example` 模板。

后端接口已做基础鉴权收紧：缺失或无效 token 不再默认映射到用户 1，管理员接口要求 `role=admin`，聊天会话和消息接口会校验用户归属。Android 客户端不再保存原始密码，并排除了 `auth_prefs` 云备份。

发布前请自行创建真实环境变量文件，不要提交：

```bash
backend/.env
web-admin/.env
```

## 后端运行

```bash
python -m venv .venv
.\.venv\Scripts\activate
pip install -r backend/requirements.txt
copy backend\.env.example backend\.env
uvicorn backend.main:app --host 0.0.0.0 --port 8000 --reload
```

需要在 `backend/.env` 中配置：

```text
DEEPSEEK_API_KEY=your_deepseek_api_key_here
JWT_SECRET_KEY=replace_with_a_random_secret
```

知识库 JSON 由 Android 本地种子数据生成，后端默认读取 `knowledge/knowledge_base.json`：

```bash
python -m backend.scripts.build_knowledge_base
python -m backend.vectorization.embed_knowledge
```

## 管理端运行

```bash
cd web-admin
npm install
copy .env.example .env
npm run dev
```

构建：

```bash
npm run build
```

## Android 构建

使用 Android Studio 打开项目根目录，等待 Gradle 同步后运行 `app`。

命令行构建需要本机有 Android SDK，并设置 `ANDROID_HOME` 或生成本机 `local.properties`：

```powershell
$env:ANDROID_HOME="C:\Users\<you>\AppData\Local\Android\Sdk"
.\gradlew.bat :app:compileDebugKotlin
```

`local.properties` 是本机路径文件，已加入 `.gitignore`，不要提交。

Android 默认后端地址是模拟器可访问的 `http://10.0.2.2:8000/`。真机或局域网调试时可临时覆盖：

```powershell
.\gradlew.bat :app:compileDebugKotlin -PmindfulBaseUrl="http://你的后端IP:8000/"
```

## 当前保留功能

- 用户登录、改密和学校账号体系
- 情绪日记与 AI 分析
- AI 对话与聊天会话
- 心理知识库检索增强
- 能量、呼吸、白噪音和心理资源页面
- 管理员风险名单、报告、反馈和批量建号
- Web 管理端基础页面

已删除社区分享、收藏等废弃模块，以及旧文档生成脚本和内部原型文件。

## 验证

本次清理后已通过：

```bash
python -m compileall -q backend
python -m backend.vectorization.embed_knowledge
npm run build
.\gradlew.bat :app:compileDebugKotlin
```
