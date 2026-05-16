# Android 工具类 App 技术设计文档

版本确认日期：2026-05-16

## 1. 目标与边界

本项目采用原生 Android 技术栈，使用 Kotlin、Jetpack Compose、Material 3 与轻量级 Clean Architecture 构建一款工具类 App。当前阶段不包含登录、账号体系、支付、复杂后台同步和多端协同能力，但框架需要提前处理好主题、权限、网络、持久化、错误模型、可测试性和后续模块扩展。

设计原则：

- 优先使用稳定版依赖，避免 alpha/beta/rc 进入主干。
- 使用 Gradle Version Catalog 锁定所有依赖版本。
- UI 层只处理展示状态和用户事件，不直接访问数据库、网络或 Android 系统 API。
- Domain 层保持纯 Kotlin，尽量不依赖 Android Framework。
- Data 层负责网络、缓存、本地存储、DTO/Entity 映射和错误归一化。
- 先采用单工程多 package 的轻量架构，业务变大后再拆 Gradle module。

## 2. 技术选型

| 方向 | 选择 | 说明 |
| --- | --- | --- |
| 语言 | Kotlin 2.3.21 | 当前稳定版，Compose Compiler 由 Kotlin 插件管理 |
| UI | Jetpack Compose + Material 3 | 全 Compose，不使用 XML 页面 |
| 架构 | 轻量 Clean Architecture + MVVM/MVI-ish | ViewModel 暴露单一 UI State，UI 发送 Action/Event |
| DI | Hilt | 编译期依赖注入，适合 Android 原生项目 |
| 异步 | Kotlin Coroutines + Flow | 网络、数据库、设置项统一 suspend/Flow |
| 导航 | Navigation Compose 2.9.8 | 当前先用稳定 Navigation 2，暂不引入 Navigation 3 |
| 网络 | Retrofit 3 + OkHttp 5 + kotlinx.serialization | Retrofit 定义 API，OkHttp 统一拦截器与超时，JSON 使用 Kotlin 原生序列化 |
| 本地设置 | DataStore Preferences | 主题、开关、轻量配置 |
| 本地数据库 | Room 2.8.4 | 需要结构化缓存/历史记录时使用 |
| 后台任务 | WorkManager 2.11.2 | 仅用于可延迟、可约束的后台任务 |
| 图片 | Coil 3 | 如工具类 App 需要加载本地/网络图片再启用 |
| 测试 | JUnit4、MockK、Turbine、Compose UI Test | 覆盖 domain、repository、ViewModel、核心 UI |

## 3. 版本矩阵

### 3.1 构建环境

| 项 | 版本 |
| --- | --- |
| JDK | 17 |
| Gradle | 9.4.1 |
| Android Gradle Plugin | 9.2.0 |
| Kotlin | 2.3.21 |
| Gradle Foojay Toolchain Resolver | 1.0.0 |
| KSP | 2.3.7 |
| compileSdk | 36 |
| targetSdk | 36 |
| minSdk | 23 |
| Java/Kotlin JVM target | 17 |

说明：AGP 9.2.0 官方兼容信息要求 Gradle 9.4.1、JDK 17，并支持最高 API level 36.1。生产项目这里先使用稳定 `compileSdk = 36` 和 `targetSdk = 36`，不追 preview SDK。

### 3.2 Version Catalog 建议

后续工程使用 `gradle/libs.versions.toml` 集中定义：

```toml
[versions]
agp = "9.2.0"
kotlin = "2.3.21"
ksp = "2.3.7"
composeBom = "2026.05.00"
activity = "1.13.0"
core = "1.18.0"
lifecycle = "2.10.0"
navigation = "2.9.8"
hiltAndroidx = "1.3.0"
hilt = "2.59.2"
datastore = "1.2.1"
room = "2.8.4"
work = "2.11.2"
retrofit = "3.0.0"
okhttp = "5.3.2"
serialization = "1.11.0"
coroutines = "1.10.2"
coil = "3.4.0"
junit = "4.13.2"
androidxTestExt = "1.3.0"
espresso = "3.7.0"
mockk = "1.14.9"
turbine = "1.2.1"

[plugins]
android-application = { id = "com.android.application", version.ref = "agp" }
kotlin-compose = { id = "org.jetbrains.kotlin.plugin.compose", version.ref = "kotlin" }
kotlin-serialization = { id = "org.jetbrains.kotlin.plugin.serialization", version.ref = "kotlin" }
ksp = { id = "com.google.devtools.ksp", version.ref = "ksp" }
hilt = { id = "com.google.dagger.hilt.android", version.ref = "hilt" }

[libraries]
androidx-core-ktx = { module = "androidx.core:core-ktx", version.ref = "core" }
androidx-activity-compose = { module = "androidx.activity:activity-compose", version.ref = "activity" }
androidx-lifecycle-runtime-compose = { module = "androidx.lifecycle:lifecycle-runtime-compose", version.ref = "lifecycle" }
androidx-lifecycle-viewmodel-compose = { module = "androidx.lifecycle:lifecycle-viewmodel-compose", version.ref = "lifecycle" }
androidx-navigation-compose = { module = "androidx.navigation:navigation-compose", version.ref = "navigation" }

androidx-compose-bom = { module = "androidx.compose:compose-bom", version.ref = "composeBom" }
androidx-compose-ui = { module = "androidx.compose.ui:ui" }
androidx-compose-ui-tooling = { module = "androidx.compose.ui:ui-tooling" }
androidx-compose-ui-tooling-preview = { module = "androidx.compose.ui:ui-tooling-preview" }
androidx-compose-ui-test-junit4 = { module = "androidx.compose.ui:ui-test-junit4" }
androidx-compose-ui-test-manifest = { module = "androidx.compose.ui:ui-test-manifest" }
androidx-compose-foundation = { module = "androidx.compose.foundation:foundation" }
androidx-compose-material3 = { module = "androidx.compose.material3:material3" }
androidx-compose-material-icons-extended = { module = "androidx.compose.material:material-icons-extended" }

hilt-android = { module = "com.google.dagger:hilt-android", version.ref = "hilt" }
hilt-compiler = { module = "com.google.dagger:hilt-compiler", version.ref = "hilt" }
androidx-hilt-navigation-compose = { module = "androidx.hilt:hilt-navigation-compose", version.ref = "hiltAndroidx" }
androidx-hilt-work = { module = "androidx.hilt:hilt-work", version.ref = "hiltAndroidx" }
androidx-hilt-compiler = { module = "androidx.hilt:hilt-compiler", version.ref = "hiltAndroidx" }

androidx-datastore-preferences = { module = "androidx.datastore:datastore-preferences", version.ref = "datastore" }
androidx-room-runtime = { module = "androidx.room:room-runtime", version.ref = "room" }
androidx-room-ktx = { module = "androidx.room:room-ktx", version.ref = "room" }
androidx-room-compiler = { module = "androidx.room:room-compiler", version.ref = "room" }
androidx-work-runtime-ktx = { module = "androidx.work:work-runtime-ktx", version.ref = "work" }

retrofit = { module = "com.squareup.retrofit2:retrofit", version.ref = "retrofit" }
retrofit-converter-kotlinx-serialization = { module = "com.squareup.retrofit2:converter-kotlinx-serialization", version.ref = "retrofit" }
okhttp-bom = { module = "com.squareup.okhttp3:okhttp-bom", version.ref = "okhttp" }
okhttp = { module = "com.squareup.okhttp3:okhttp" }
okhttp-logging = { module = "com.squareup.okhttp3:logging-interceptor" }
kotlinx-serialization-json = { module = "org.jetbrains.kotlinx:kotlinx-serialization-json", version.ref = "serialization" }
kotlinx-coroutines-android = { module = "org.jetbrains.kotlinx:kotlinx-coroutines-android", version.ref = "coroutines" }
coil-compose = { module = "io.coil-kt.coil3:coil-compose", version.ref = "coil" }
coil-network-okhttp = { module = "io.coil-kt.coil3:coil-network-okhttp", version.ref = "coil" }

junit = { module = "junit:junit", version.ref = "junit" }
androidx-test-ext-junit = { module = "androidx.test.ext:junit", version.ref = "androidxTestExt" }
androidx-espresso-core = { module = "androidx.test.espresso:espresso-core", version.ref = "espresso" }
mockk = { module = "io.mockk:mockk", version.ref = "mockk" }
turbine = { module = "app.cash.turbine:turbine", version.ref = "turbine" }
```

### 3.3 依赖使用方式

`app/build.gradle.kts` 核心依赖形态：

```kotlin
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
}

android {
    namespace = "com.svwh.tools"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.svwh.tools"
        minSdk = 23
        targetSdk = 36
        versionCode = 1
        versionName = "0.1.0"
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(platform(libs.androidx.compose.bom))
    implementation(platform(libs.okhttp.bom))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    implementation(libs.hilt.android)
    implementation(libs.androidx.hilt.navigation.compose)
    ksp(libs.hilt.compiler)

    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.kotlinx.serialization)
    implementation(libs.okhttp)
    debugImplementation(libs.okhttp.logging)

    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    testImplementation(libs.junit)
    testImplementation(libs.mockk)
    testImplementation(libs.turbine)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
}
```

## 4. 推荐目录结构

先采用单 `app` module，目录按架构切分：

```text
app/src/main/java/com/svwh/tools/
  SToolApp.kt
  MainActivity.kt

  core/
    common/
      AppDispatchers.kt
      Result.kt
      UiText.kt
    designsystem/
      theme/
      component/
      icon/
    navigation/
      AppNavHost.kt
      AppRoute.kt
    permission/
      PermissionController.kt
      PermissionState.kt
    network/
      NetworkModule.kt
      NetworkResult.kt
      ErrorMapper.kt
    database/
      AppDatabase.kt
    datastore/
      SettingsDataStore.kt

  feature/
    home/
      presentation/
      domain/
      data/
    settings/
      presentation/
      domain/
      data/
    tool_x/
      presentation/
      domain/
      data/
```

后续业务变复杂后再演进为：

```text
:app
:core:common
:core:designsystem
:core:network
:core:database
:core:datastore
:core:permission
:feature:home
:feature:settings
:feature:tool-x
```

## 5. 架构分层

### 5.1 Presentation 层

职责：

- Compose 页面、组件和导航入口。
- ViewModel 管理 `StateFlow<UiState>`。
- 用户操作通过 `onAction(action)` 进入 ViewModel。
- 一次性事件使用 `Channel` 或 `SharedFlow`，例如 Snackbar、跳转、权限弹窗。

示例模型：

```kotlin
data class HomeUiState(
    val isLoading: Boolean = false,
    val tools: List<ToolItemUi> = emptyList(),
    val error: UiText? = null
)

sealed interface HomeAction {
    data object Refresh : HomeAction
    data class OpenTool(val id: String) : HomeAction
}
```

### 5.2 Domain 层

职责：

- 定义业务模型、Repository interface、UseCase。
- 不依赖 Android、Retrofit、Room、DataStore。
- UseCase 保持小而清晰，复杂流程通过多个 UseCase 编排。

示例：

```kotlin
interface ToolRepository {
    fun observeTools(): Flow<List<Tool>>
    suspend fun refreshTools(): AppResult<Unit>
}

class RefreshToolsUseCase(
    private val repository: ToolRepository
) {
    suspend operator fun invoke(): AppResult<Unit> = repository.refreshTools()
}
```

### 5.3 Data 层

职责：

- 实现 Domain 层的 Repository。
- 处理 API、DAO、DataStore、DTO/Entity/Domain 映射。
- 将异常归一化为 `AppError`，禁止原始 `HttpException`、`IOException` 泄漏到 UI。

推荐错误模型：

```kotlin
sealed interface AppError {
    data object NetworkUnavailable : AppError
    data object Timeout : AppError
    data class Http(val code: Int, val message: String?) : AppError
    data class Serialization(val message: String?) : AppError
    data class Unknown(val throwable: Throwable) : AppError
}

sealed interface AppResult<out T> {
    data class Success<T>(val data: T) : AppResult<T>
    data class Failure(val error: AppError) : AppResult<Nothing>
}
```

## 6. 网络设计

### 6.1 基础策略

- Release 环境默认只允许 HTTPS。
- Debug 环境可通过 `network_security_config_debug.xml` 单独放开测试域名明文 HTTP。
- 统一配置连接超时、读取超时、写入超时。
- 统一增加 User-Agent、App-Version、Request-Id 等 header。
- Debug 才启用 `HttpLoggingInterceptor`，Release 禁止打印 body。
- Repository 只返回 `AppResult<T>` 或 `Flow<AppResult<T>>`。

### 6.2 网络模块

```kotlin
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        val json = Json {
            ignoreUnknownKeys = true
            explicitNulls = false
        }

        return Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
    }
}
```

注：`kotlinx-coroutines-android` 这里锁定 `1.10.2`，因为核对时 `1.11.0` 在 Maven Central Android artifact 侧仍出现 RC 信息；工具类 App 初始框架优先保守稳定。若后续确认 `1.11.x` 稳定发布，再统一升级 Version Catalog。

## 7. 权限设计

工具类 App 常见权限可能包括通知、相机、图片选择、文件读写、定位、蓝牙等。当前框架先提供统一权限入口，不提前申请无关权限。

原则：

- Manifest 只声明当前功能真实需要的权限。
- 运行时权限在用户触发相关功能时再请求。
- 请求前说明用途，请求后处理拒绝、永久拒绝、系统设置返回。
- Android 13+ 通知权限 `POST_NOTIFICATIONS` 单独处理。
- 图片选择优先使用系统 Photo Picker，避免申请整库读取权限。
- 文件导入导出优先使用 Storage Access Framework，避免申请外部存储权限。

建议封装：

```text
core/permission/
  PermissionController.kt
  PermissionRequest.kt
  PermissionStatus.kt
  PermissionRationaleDialog.kt
```

Compose 中使用 `rememberLauncherForActivityResult` 和 Activity Result API，不引入第三方权限库，减少版本风险。

## 8. 主题与设计系统

### 8.1 主题能力

- 支持浅色、深色、跟随系统。
- Android 12+ 支持 Dynamic Color，可在设置中关闭。
- 使用 Material 3 `ColorScheme`、`Typography`、`Shapes`。
- 将通用组件沉淀到 `core/designsystem/component`。
- 状态栏、导航栏颜色由主题统一管理。

### 8.2 设置持久化

主题设置保存到 DataStore：

```kotlin
enum class ThemeMode {
    FollowSystem,
    Light,
    Dark
}

data class UserSettings(
    val themeMode: ThemeMode = ThemeMode.FollowSystem,
    val dynamicColor: Boolean = true
)
```

App 根节点订阅设置：

```kotlin
@Composable
fun SToolApp(settings: UserSettings) {
    SToolTheme(
        themeMode = settings.themeMode,
        dynamicColor = settings.dynamicColor
    ) {
        AppNavHost()
    }
}
```

## 9. 数据持久化

### 9.1 DataStore

适合保存：

- 主题模式。
- 动态色开关。
- 首次启动标记。
- 工具功能的轻量配置。

### 9.2 Room

适合保存：

- 工具历史记录。
- 离线缓存。
- 收藏、最近使用、用户自定义模板。

Room 要求：

- Room 2.8+ 不允许空实体数据库；如果启用 Room KSP，`@Database` 必须至少声明一个 `@Entity`。
- Entity 不直接暴露给 Domain/UI。
- DAO 返回 `Flow<List<Entity>>`。
- 数据迁移必须显式写 Migration，禁止生产环境使用 destructive migration。

## 10. 后台任务

WorkManager 仅用于：

- 周期性清理缓存。
- 延迟导出/同步。
- 网络可用时重试任务。

暂不用于：

- 实时任务。
- 秒级定时器。
- 长时间前台服务。

如果工具功能后续需要长时间运行，应单独设计 Foreground Service，并处理 Android 14+ 前台服务类型限制。

## 11. 构建类型与配置

建议 build types：

```kotlin
buildTypes {
    debug {
        applicationIdSuffix = ".debug"
        versionNameSuffix = "-debug"
        buildConfigField("String", "BASE_URL", "\"https://api-dev.example.com/\"")
    }
    release {
        isMinifyEnabled = true
        isShrinkResources = true
        buildConfigField("String", "BASE_URL", "\"https://api.example.com/\"")
        proguardFiles(
            getDefaultProguardFile("proguard-android-optimize.txt"),
            "proguard-rules.pro"
        )
    }
}
```

安全要求：

- API key 不硬编码进仓库。
- Release 不允许 cleartext。
- Release 不打印敏感日志。
- ProGuard/R8 规则随引入库同步维护。

## 12. 测试策略

| 层级 | 测试内容 | 工具 |
| --- | --- | --- |
| Domain | UseCase、纯业务规则 | JUnit4 |
| Data | Repository、Mapper、错误转换 | JUnit4、MockK |
| Flow | 状态流、异步事件 | Turbine |
| ViewModel | Action 到 UiState 的转换 | JUnit4、MockK、Turbine |
| UI | 关键页面状态、空态、错误态 | Compose UI Test |
| Instrumentation | Room、DataStore、导航关键路径 | AndroidX Test |

最低要求：

- 每个新增 UseCase 至少有单元测试。
- 每个 Repository 至少覆盖成功、网络失败、解析失败。
- 每个核心页面至少覆盖 loading、content、error、empty 四种状态。

## 13. 后续工程落地顺序

1. 创建 Gradle Android 项目，加入 Version Catalog。
2. 配置 AGP、Kotlin、Compose、Hilt、KSP。
3. 建立 `core/common`、`core/designsystem`、`core/navigation`。
4. 实现主题、DataStore 设置、根导航。
5. 建立网络层、错误模型和 Repository 模板。
6. 建立权限封装。
7. 创建 `home` 和 `settings` 两个基础 feature。
8. 补充单元测试和 Compose UI Test 基础样例。

## 14. 版本与资料来源

- Android Gradle Plugin 9.2.0 兼容信息：https://developer.android.com/build/releases/gradle-plugin
- AndroidX 当前版本表：https://developer.android.com/jetpack/androidx/versions
- Compose BOM 说明：https://developer.android.com/develop/ui/compose/bom
- Compose 当前版本表：https://developer.android.com/jetpack/androidx/releases/compose
- Kotlin 当前版本与发布信息：https://kotlinlang.org/docs/faq.html
- Kotlin release process：https://kotlinlang.org/docs/releases.html
- Android App Architecture：https://developer.android.com/topic/architecture
- Android 权限最佳实践：https://developer.android.com/training/permissions/usage-notes
- Android Network Security / Cleartext：https://developer.android.com/privacy-and-security/risks/cleartext-communications
- Dagger/Hilt 当前版本：https://dagger.dev/
- KSP releases：https://github.com/google/ksp/releases
- Retrofit：https://github.com/square/retrofit
- OkHttp：https://square.github.io/okhttp/
- kotlinx.serialization：https://github.com/Kotlin/kotlinx.serialization
- kotlinx.coroutines：https://kotlinlang.org/api/kotlinx.coroutines/
