# STool NoEnv Hook Runtime

这个模块用于维护无环境重打包场景中需要注入目标 App 的 Hook runtime。

## 设计目标

- Runtime 源码跟随 STool 主工程演进，但不耦合 `app` 模块的构建产物。
- 不暴露启动 Activity，不携带 UI 壳和无关资源，只保留注入目标 App 后需要执行的 Java Hook 逻辑。
- 通过 Gradle 任务产出 dex，后续由外部 dex 合并/注入程序消费该产物。

## 构建产物

- Debug: `noenv-hook-runtime/build/outputs/noenv-hook/stool-noenv-hook-debug.dex`
- Release: `noenv-hook-runtime/build/outputs/noenv-hook/stool-noenv-hook-release.dex`

对应任务：

```bash
./gradlew :noenv-hook-runtime:packageNoEnvHookDexDebug
./gradlew :noenv-hook-runtime:packageNoEnvHookDexRelease
```

## 接入方式

本模块不自动把 dex 放入 `app` 的 assets，也不参与当前 App 的重打包 workflow。

后续重打包链路应由外部程序完成：

- 构建本模块产出 runtime dex。
- 将 runtime dex 与其他需要注入的 dex 合并为单个 dex。
- 在无环境重打包流程中注入合并后的 dex。

## 后续重构方向

- 将 `com.svwh.noenvhook.app.KillerBaseApplication` 收敛为更明确的 runtime entrypoint。
- 将配置读取统一到 `Android/media/<package>/stool/no_env_enable.s` 与后续配置文件模型。
- 将日志写入协议整理成 STool 主 App 可分页读取的稳定 schema。
