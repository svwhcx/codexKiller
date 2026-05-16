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
