# STool 功能文档

## 主框架导航

当前 App 使用底部四 Tab 作为一级导航：

- 首页
- 无环境
- 有环境
- 设置

实现约定：

- 顶层入口位于 `core/navigation/AppNavHost.kt`。
- Tab 定义集中在 `core/navigation/AppRoute.kt`。
- 内容区域使用 Compose `HorizontalPager`，支持左右滑动切换页面。
- 底部栏使用 Material 3 `NavigationBar`，点击 Tab 时调用 `scrollToPage` 直接切换，避免跨页闪动。
- 当前页面内容先使用文字占位，后续功能接入时按 Tab 拆分到对应 feature。

后续扩展建议：

- 首页承载工具入口和最近使用。
- 无环境承载无需运行环境或无需外部依赖的工具。
- 有环境承载需要网络、权限、本地服务或外部运行环境的工具。
- 设置承载主题、权限说明、缓存、关于等配置项。

## 设置：UI 设置

设置页新增 `UI 设置` 分组，当前包含：

- `显示无环境`：控制底部栏 `无环境` Tab 及页面是否显示。
- `显示有环境`：控制底部栏 `有环境` Tab 及页面是否显示。

实现约定：

- 两个开关使用 DataStore 持久化。
- 点击设置项整行或右侧 Switch 都可以切换。
- 设置保存后不立即改变当前底部栏，重启 App 后才按最新配置生成 Tab。
- 首页和设置 Tab 始终显示，避免用户隐藏全部入口。

## 有环境：应用 Hook 列表

`有环境` 页面用于面向逆向分析场景管理目标应用：

- 进入页面后加载系统已安装应用，排除 STool 当前包名。
- 默认隐藏系统应用，可通过搜索框右侧 `筛选` 菜单重新加载并包含系统应用。
- 列表按 `Hook 已开启优先`、`安装时间倒序` 排序。
- 顶部显示 LSPosed 状态：启用时使用成功色，未启用时使用红色警告色。
- 搜索框支持按应用名称或包名过滤。
- LSPosed 未启用提示使用浅红渐变警告条，左侧展示警告图标，文案直接提示 Hook 开关仅保存配置。
- 应用列表区域使用白色 card 容器，搜索框右侧为轻量 `筛选` 按钮。
- 应用项展示图标、应用名称、包名和自定义 Hook 开关；开启后开关轨道为绿色，应用名称绿色高亮。
- Hook 开关使用 DataStore 持久化；开启后应用名称使用绿色高亮。
- 加载应用列表时在列表 card 内展示行内 loading，不使用 dialog 弹窗；即使读取很快也保持短暂 loading 反馈。

实现约定：

- 页面入口位于 `feature/environment/presentation/EnvironmentScreen.kt`。
- 应用读取和基础排序位于 `feature/environment/data/InstalledAppRepositoryImpl`，ViewModel 只编排 UI 状态。
- Hook 状态按包名保存到 DataStore 的 `hooked_packages`。
- Hook 排序只在加载应用列表时应用；加载完成后切换开关只更新当前项状态，不动态改变列表位置。
- LSPosed 激活状态由 `core/environment/LsposedStatus` 提供，后续模块可在运行时更新该状态。
- 读取已安装应用列表没有 Android 运行时权限弹窗；Android 11+ 依赖 `QUERY_ALL_PACKAGES` 和 `<queries>` 包可见性声明。
- `QUERY_ALL_PACKAGES` 已声明用于完整读取应用列表；如面向应用商店分发，需要单独评估平台政策。
- 应用列表页面不展示权限说明卡片；真正需要运行时授权的后续功能统一接入 `core/permission`。

## 无环境：重打包应用列表

`无环境` 页面用于后续无 LSPosed/无运行环境场景：

- 顶部使用绿色说明条，固定展示 `无环境可重打包 App 后执行有环境相似功能。`
- 页面 UI 与 `有环境` 保持一致：搜索栏、白色列表 card、应用图标、名称、包名、Hook 开关和行内 loading。
- 当前先加载用户已安装应用，不包含系统应用。
- 搜索框支持按应用名称或包名过滤。
- 搜索框右侧为 `添加` 按钮；点击后预留跳转到“选择已安装用户应用”页面，目标页面后续实现。
- 后续添加功能需要读取已安装应用 Manifest meta-data，根据配置过滤可添加应用。

实现约定：

- 页面入口位于 `feature/noenvironment/presentation/NoEnvironmentScreen.kt`。
- 当前复用 `InstalledAppRepository` 加载用户应用。
- 通用应用列表 UI 复用 `feature/environment/presentation` 中的搜索框、应用项、Hook 开关和 loading 组件。
