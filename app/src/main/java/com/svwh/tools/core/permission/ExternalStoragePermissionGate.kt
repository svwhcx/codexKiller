package com.svwh.tools.core.permission

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext

@Composable
fun rememberExternalStoragePermissionGate(
    onPermissionDenied: () -> Unit = {},
): ExternalStoragePermissionGate {
    val context = LocalContext.current
    val currentOnPermissionDenied by rememberUpdatedState(onPermissionDenied)
    val permissionController = remember(context) {
        PermissionController(context.applicationContext)
    }
    var pendingAction by remember { mutableStateOf<(() -> Unit)?>(null) }
    var showAllFilesDialog by remember { mutableStateOf(false) }
    var requestInProgress by remember { mutableStateOf(false) }

    fun completeWithPermissionState() {
        requestInProgress = false
        if (permissionController.getExternalStorageAccessStatus() is ExternalStorageAccessStatus.Granted) {
            pendingAction?.invoke()
            pendingAction = null
        } else {
            pendingAction = null
            currentOnPermissionDenied()
        }
    }

    val runtimePermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { granted ->
        if (granted) {
            pendingAction?.invoke()
            pendingAction = null
        } else {
            pendingAction = null
            currentOnPermissionDenied()
        }
        requestInProgress = false
    }

    val allFilesAccessLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
    ) {
        completeWithPermissionState()
    }

    if (showAllFilesDialog) {
        FileAccessPermissionDialog(
            onConfirm = {
                showAllFilesDialog = false
                allFilesAccessLauncher.launch(context.createAllFilesAccessIntent())
            },
            onDismiss = {
                showAllFilesDialog = false
                pendingAction = null
                requestInProgress = false
                currentOnPermissionDenied()
            },
        )
    }

    return remember(permissionController, runtimePermissionLauncher, allFilesAccessLauncher) {
        ExternalStoragePermissionGate(
            request = { action ->
                when (val status = permissionController.getExternalStorageAccessStatus()) {
                    ExternalStorageAccessStatus.Granted -> action()
                    is ExternalStorageAccessStatus.RuntimePermissionRequired -> {
                        pendingAction = action
                        if (!requestInProgress) {
                            requestInProgress = true
                            runtimePermissionLauncher.launch(status.permission)
                        }
                    }
                    ExternalStorageAccessStatus.AllFilesAccessRequired -> {
                        pendingAction = action
                        if (!requestInProgress && !showAllFilesDialog) {
                            requestInProgress = true
                            showAllFilesDialog = true
                        }
                    }
                }
            },
        )
    }
}

class ExternalStoragePermissionGate(
    private val request: (() -> Unit) -> Unit,
) {
    fun runAfterPermission(action: () -> Unit) {
        request(action)
    }
}

private fun android.content.Context.createAllFilesAccessIntent(): Intent {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
        return Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", packageName, null)
        }
    }

    return Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION).apply {
        data = Uri.fromParts("package", packageName, null)
    }
}
