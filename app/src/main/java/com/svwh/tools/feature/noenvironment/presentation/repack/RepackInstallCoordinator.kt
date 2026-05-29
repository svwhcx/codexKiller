package com.svwh.tools.feature.noenvironment.presentation.repack

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.InstallMobile
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.FileProvider
import com.svwh.tools.core.permission.PermissionRequestDialog
import com.svwh.tools.feature.noenvironment.domain.model.RepackProgressState
import java.io.File

@Composable
fun rememberRepackInstallCoordinator(
    onInstallSucceeded: (String) -> Unit = {},
): RepackInstallCoordinator {
    val context = LocalContext.current
    var pendingState by remember { mutableStateOf<RepackProgressState?>(null) }
    var showInstallPermissionDialog by remember { mutableStateOf(false) }
    var signatureMismatchState by remember { mutableStateOf<RepackProgressState?>(null) }
    var installStartedAtMillis by remember { mutableStateOf(0L) }

    val installLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
    ) { result ->
        val state = pendingState
        val installedAfterLaunch = state != null &&
            context.isPackageInstalled(state.packageName) &&
            context.getPackageLastUpdateTime(state.packageName) >= installStartedAtMillis
        val installerReportedSuccess = result.resultCode == Activity.RESULT_OK
        if (state != null && context.isPackageInstalled(state.packageName) && (installerReportedSuccess || installedAfterLaunch)) {
            onInstallSucceeded(state.packageName)
            pendingState = null
            Toast.makeText(context, "安装成功", Toast.LENGTH_SHORT).show()
        } else if (state != null && result.resultCode == Activity.RESULT_CANCELED) {
            Toast.makeText(context, "安装未完成", Toast.LENGTH_SHORT).show()
        }
    }

    val installPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
    ) {
        val state = pendingState
        if (state != null && context.canInstallPackages()) {
            if (context.hasDifferentInstalledSignature(state)) {
                signatureMismatchState = state
            } else {
                installStartedAtMillis = System.currentTimeMillis()
                context.launchInstallFlow(state, installLauncher::launch)
            }
        } else if (state != null) {
            Toast.makeText(context, "未授予安装未知应用权限", Toast.LENGTH_SHORT).show()
        }
    }

    val uninstallLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
    ) {
        val state = pendingState
        if (state != null) {
            if (context.isPackageInstalled(state.packageName)) {
                Toast.makeText(context, "原应用仍未卸载，无法覆盖安装", Toast.LENGTH_SHORT).show()
            } else {
                installStartedAtMillis = System.currentTimeMillis()
                context.launchInstallFlow(
                    state = state,
                    installLauncher = installLauncher::launch,
                    skipSignatureCheck = true,
                )
            }
        }
    }

    if (showInstallPermissionDialog) {
        PermissionRequestDialog(
            title = "需要安装权限",
            message = "需要拉起系统安装器，请允许本应用安装未知应用。",
            icon = Icons.Outlined.InstallMobile,
            confirmText = "去授权",
            dismissText = "取消",
            onConfirm = {
                showInstallPermissionDialog = false
                installPermissionLauncher.launch(context.createInstallPermissionIntent())
            },
            onDismiss = {
                showInstallPermissionDialog = false
                pendingState = null
            },
        )
    }

    signatureMismatchState?.let { state ->
        SignatureMismatchDialog(
            onConfirm = {
                signatureMismatchState = null
                pendingState = state
                context.launchUninstallFlow(
                    state = state,
                    uninstallLauncher = uninstallLauncher::launch,
                    onFailed = {
                        pendingState = null
                    },
                )
            },
            onDismiss = {
                signatureMismatchState = null
                pendingState = null
            },
        )
    }

    return remember(context) {
        RepackInstallCoordinator(
            install = { state ->
                pendingState = state
                when {
                    state.outputApkPath.isBlank() -> {
                        pendingState = null
                        Toast.makeText(context, "未找到打包后的 APK", Toast.LENGTH_SHORT).show()
                    }
                    !File(state.outputApkPath).exists() -> {
                        pendingState = null
                        Toast.makeText(context, "APK 文件不存在", Toast.LENGTH_SHORT).show()
                    }
                    !context.canInstallPackages() -> {
                        showInstallPermissionDialog = true
                    }
                    context.hasDifferentInstalledSignature(state) -> {
                        signatureMismatchState = state
                    }
                    else -> {
                        installStartedAtMillis = System.currentTimeMillis()
                        context.launchInstallFlow(state, installLauncher::launch)
                    }
                }
            },
        )
    }
}

class RepackInstallCoordinator(
    private val install: (RepackProgressState) -> Unit,
) {
    fun install(state: RepackProgressState) {
        install.invoke(state)
    }
}

private fun Context.canInstallPackages(): Boolean {
    return Build.VERSION.SDK_INT < Build.VERSION_CODES.O ||
        packageManager.canRequestPackageInstalls()
}

private fun Context.createInstallPermissionIntent(): Intent {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES).apply {
            data = Uri.parse("package:$packageName")
        }
    } else {
        Intent(Settings.ACTION_SECURITY_SETTINGS)
    }
}

private fun Context.launchUninstallFlow(
    state: RepackProgressState,
    uninstallLauncher: (Intent) -> Unit,
    onFailed: () -> Unit,
) {
    if (state.packageName.isBlank()) {
        Toast.makeText(this, "无法识别原应用包名", Toast.LENGTH_SHORT).show()
        onFailed()
        return
    }
    if (!isPackageInstalled(state.packageName)) {
        Toast.makeText(this, "原应用已卸载，请重新点击安装", Toast.LENGTH_SHORT).show()
        onFailed()
        return
    }

    val uninstallIntent = Intent(
        Intent.ACTION_UNINSTALL_PACKAGE,
        Uri.parse("package:${state.packageName}"),
    ).apply {
        putExtra(Intent.EXTRA_RETURN_RESULT, true)
    }
    val fallbackIntent = Intent(
        Intent.ACTION_DELETE,
        Uri.parse("package:${state.packageName}"),
    ).apply {
        putExtra(Intent.EXTRA_RETURN_RESULT, true)
    }

    runCatching {
        uninstallLauncher(uninstallIntent)
    }.recoverCatching {
        uninstallLauncher(fallbackIntent)
    }.onFailure {
        Toast.makeText(this, "无法打开系统卸载页面", Toast.LENGTH_SHORT).show()
        onFailed()
    }
}

private fun Context.launchInstallFlow(
    state: RepackProgressState,
    installLauncher: (Intent) -> Unit,
    skipSignatureCheck: Boolean = false,
) {
    val apkFile = File(state.outputApkPath)
    if (!apkFile.exists()) {
        Toast.makeText(this, "APK 文件不存在", Toast.LENGTH_SHORT).show()
        return
    }
    if (!skipSignatureCheck && hasDifferentInstalledSignature(state)) {
        Toast.makeText(this, "签名不同，请先卸载原应用", Toast.LENGTH_SHORT).show()
        return
    }

    val apkUri = FileProvider.getUriForFile(
        this,
        "$packageName.fileprovider",
        apkFile,
    )
    val intent = Intent(Intent.ACTION_VIEW).apply {
        setDataAndType(apkUri, "application/vnd.android.package-archive")
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        putExtra(Intent.EXTRA_NOT_UNKNOWN_SOURCE, true)
    }
    runCatching { installLauncher(intent) }
        .onFailure {
            Toast.makeText(this, "无法打开系统安装器", Toast.LENGTH_SHORT).show()
        }
}

private fun Context.hasDifferentInstalledSignature(state: RepackProgressState): Boolean {
    if (state.packageName.isBlank() || state.outputApkPath.isBlank()) return false
    val archiveInfo = packageManager.getArchivePackageInfo(state.outputApkPath) ?: return false
    if (archiveInfo.packageName != state.packageName) return false
    val installedInfo = packageManager.getInstalledPackageInfo(state.packageName) ?: return false
    val archiveSignatures = archiveInfo.signatureBytes()
    val installedSignatures = installedInfo.signatureBytes()
    if (archiveSignatures.isEmpty() || installedSignatures.isEmpty()) return false
    return archiveSignatures.toSet() != installedSignatures.toSet()
}

private fun PackageManager.getArchivePackageInfo(apkPath: String): PackageInfo? {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        getPackageArchiveInfo(apkPath, PackageManager.GET_SIGNING_CERTIFICATES)
    } else {
        @Suppress("DEPRECATION")
        getPackageArchiveInfo(apkPath, PackageManager.GET_SIGNATURES)
    }
}

private fun PackageManager.getInstalledPackageInfo(packageName: String): PackageInfo? {
    return runCatching {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            getPackageInfo(packageName, PackageManager.GET_SIGNING_CERTIFICATES)
        } else {
            @Suppress("DEPRECATION")
            getPackageInfo(packageName, PackageManager.GET_SIGNATURES)
        }
    }.getOrNull()
}

private fun Context.isPackageInstalled(packageName: String): Boolean {
    if (packageName.isBlank()) return false
    return packageManager.getInstalledPackageInfo(packageName) != null
}

private fun Context.getPackageLastUpdateTime(packageName: String): Long {
    if (packageName.isBlank()) return 0L
    return packageManager.getInstalledPackageInfo(packageName)?.lastUpdateTime ?: 0L
}

private fun PackageInfo.signatureBytes(): List<ByteArraySignature> {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        signingInfo?.apkContentsSigners
            ?.map { ByteArraySignature(it.toByteArray()) }
            .orEmpty()
    } else {
        @Suppress("DEPRECATION")
        signatures?.map { ByteArraySignature(it.toByteArray()) }.orEmpty()
    }
}

private data class ByteArraySignature(
    val bytes: ByteArray,
) {
    override fun equals(other: Any?): Boolean {
        return other is ByteArraySignature && bytes.contentEquals(other.bytes)
    }

    override fun hashCode(): Int {
        return bytes.contentHashCode()
    }
}

@Composable
private fun SignatureMismatchDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.78f)
                .widthIn(max = 320.dp),
            shape = RoundedCornerShape(20.dp),
            color = Color.White,
            tonalElevation = 0.dp,
            shadowElevation = 12.dp,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 22.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .background(color = Color(0xFFEDF1F7), shape = CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Shield,
                        contentDescription = null,
                        tint = Color(0xFF2D6CCB),
                        modifier = Modifier.size(26.dp),
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "签名不一致",
                    color = Color(0xFF1F2330),
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "应用签名与已安装版本不同，无法直接安装。",
                    color = Color(0xFF8A93A4),
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center,
                )

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    OutlinedButton(
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp),
                        onClick = onDismiss,
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, Color(0xFFE0E5EE)),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = Color.White,
                            contentColor = Color(0xFF1F2330),
                        ),
                    ) {
                        Text(text = "取消", fontSize = 14.sp)
                    }
                    Button(
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp),
                        onClick = onConfirm,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF2D6CCB),
                            contentColor = Color.White,
                        ),
                    ) {
                        Text(text = "卸载旧版", fontSize = 14.sp)
                    }
                }
            }
        }
    }
}
