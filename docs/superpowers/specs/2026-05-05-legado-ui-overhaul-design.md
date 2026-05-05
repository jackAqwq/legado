# Legado UI Overhaul Design (One-shot Replacement)

Date: 2026-05-05
Project: `D:\legado`
Scope mode: C (大幅重塑)
Release mode: A (一次性替换)

## 1. Goal

在一次发布中完成 legado 的整体 UI 重塑，目标包括：

- 用全新设计系统统一视觉（深色沉浸 C 风格）
- 重构主壳与交互架构（导航与关键流程）
- 统一图标与字体体系（含阅读页字号层级）
- 覆盖主页面、阅读页和弹窗体系

本次设计优先级：

1. 新设计系统一致性
2. 一次性替换上线
3. 旧配置兼容“尽量迁移”而非强保留

## 2. Constraints

- 用户已确认：
  - 不使用可视化伴随工具
  - 兼容策略选择 C：新设计系统优先，旧配置尽量迁移
  - 主视觉选择 C：深色沉浸（深底高对比+冷蓝强调）
  - 首轮范围选择 C：A+B+阅读页与弹窗体系一并重做
  - 图标/字体选择 C：图标与字体都统一重做
  - 交互结构选择 C：可重构交互信息架构
  - 发布方式选择 A：一次性替换

- 仍需满足工程门禁：
  - 本地：`:app:testAppDebugUnitTest` + `:app:assembleAppRelease`
  - CI：GitHub Actions `Test Build` 成功

## 3. Architecture

采用“新 UI 壳整体替换”架构，并分 4 层实现一次替换可控落地：

### 3.1 Design System Layer

提供语义化设计令牌（Design Tokens）：

- Color tokens: `surface`, `surfaceVariant`, `onSurface`, `primary`, `secondary`, `outline`, `error`, `success`, `warning`
- Typography tokens: title/body/caption + reader-specific scale
- Shape tokens: radius scale (`6/10/14/20`)
- Spacing tokens: (`4/8/12/16/24`)
- Elevation tokens: (`0/1/2/4`)
- Motion tokens: 统一时长与缓动

### 3.2 UI Shell Layer

替换主壳组织，形成统一容器：

- Top bar container
- Content host container
- Bottom navigation container
- Overlay host container (dialogs/sheets)

并统一系统栏策略：status/navigation bar 对比度与沉浸规则。

### 3.3 Feature Surface Layer

将业务页面接入统一组件语法：

- 主入口：书架 / 发现 / RSS / 我的
- 阅读域：阅读主界面、菜单、配置弹层
- 弹窗域：通用文本/列表/变量/web/dialog 系列

### 3.4 Legacy Bridge Layer

提供旧逻辑桥接：

- 旧主题读取映射到新 token
- 旧页面容器包裹新壳样式
- 旧弹窗调用路径接入新 overlay 语法

## 4. Components

### 4.1 Global Components

- `UiThemeSnapshot`（只读主题快照）
- `AppSurface` / `AppCard` / `AppListItem`
- `AppTopBar` / `AppBottomNav`
- `AppButton`（primary/secondary/tonal）
- `AppDialogShell`

### 4.2 Main Navigation Components

- 新主导航容器（独立回栈）
- 统一底栏交互：选中态、徽章、按压反馈
- 统一顶栏交互：标题、操作区、滚动态

### 4.3 Reading Domain Components

- 阅读页层次（标题/正文/辅助信息）重定义
- 阅读菜单与配置弹层样式统一
- 文本动作菜单统一按钮与状态风格

### 4.4 Dialog System Components

统一关键弹窗外观与行为：

- `TextDialog`
- `TextListDialog`
- `VariableDialog`
- `UrlOptionDialog`
- `BottomWebViewDialog`
- `WaitDialog`
- 其余高频业务弹窗

## 5. Data Flow

统一数据流：

1. **Input**
   - 用户偏好、系统亮暗、阅读配置、eInk 模式
2. **Theme Engine**
   - 输入解析为 `UiThemeSnapshot`
3. **Shell Distribution**
   - 壳层注入 snapshot 到 top/nav/content/overlay
4. **Feature Consumption**
   - 页面只消费 token，不直接拼硬编码色值
5. **Write-back**
   - 配置变更触发新 snapshot，增量刷新

## 6. Interaction Model

在 C 档要求下允许重构信息架构：

- 主入口导航与返回链按新壳规范重构
- 关键操作按钮位置按新交互语义重排
- 保证“操作可达性优先”，减少多层跳转

## 7. Error Handling

为一次替换准备可降级策略：

1. Theme 解析失败 -> 回退默认深色基线 token
2. 页面未完全接入 -> 走 legacy bridge 保底渲染
3. Insets/系统栏异常 -> 回退稳定策略
4. 图标/字体缺失 -> 回退系统资源
5. 壳层开关保底 -> 紧急情况下可启用旧导航流程

## 8. Testing Strategy

### 8.1 Build Gates

- `:app:testAppDebugUnitTest`
- `:app:assembleAppRelease`
- GitHub Actions `Test Build` success

### 8.2 Manual Regression Matrix

- 主入口四页：书架/发现/RSS/我的
- 阅读链路：打开、翻页、菜单、配置、退出恢复
- 弹窗链路：关键 8+ 弹窗
- 主题切换：深色基线 + 变体切换一致性
- eInk 模式：可读性与反馈

### 8.3 Visual Consistency Checks

- 组件基准截图（明/暗）
- 页面基准截图（主入口+阅读+弹窗）
- 检查项：间距节奏、圆角体系、层级阴影、按压反馈

## 9. Rollout Decision

用户要求一次性替换，因此发布策略为：

- 单次主线合并并上线
- 不做分阶段发布
- 通过严格门禁与降级策略控制风险

## 10. Risks and Mitigation

### Risk 1: 回归面过大
- Mitigation: token 引擎 + 壳层统一 + 回归矩阵

### Risk 2: 旧配置迁移不完整
- Mitigation: legacy bridge + 默认值回退

### Risk 3: 阅读域样式重做影响体验
- Mitigation: 阅读链路专项回归（翻页、菜单、配置）

### Risk 4: 一次上线缺乏缓冲
- Mitigation: 壳层降级开关与资源回退策略

## 11. Acceptance Criteria

满足以下全部条件才算完成：

1. 全局 UI 呈现新深色沉浸体系
2. 主入口、阅读域、弹窗域全部切换到新语法
3. 图标与字体体系完成统一
4. 本地 unit + release 构建通过
5. CI `Test Build` 成功
6. 手工回归清单全部通过

## 12. Out of Scope

- 本次不追求保留旧主题配置的完全行为一致
- 不以“最小改动”作为目标
- 不分阶段灰度发布
