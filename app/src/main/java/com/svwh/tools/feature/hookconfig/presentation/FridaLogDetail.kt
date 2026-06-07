package com.svwh.tools.feature.hookconfig.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.svwh.tools.core.common.AppResult
import com.svwh.tools.feature.hookconfig.domain.model.HookLogRecord
import com.svwh.tools.feature.hookconfig.domain.repository.FridaLogRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

private val FridaDetailTextPrimary = Color(0xFF2F3747)
private val FridaDetailTextSecondary = Color(0xFF6C768A)

internal data class FridaLogDetailUiState(
    val log: HookLogRecord? = null,
    val loading: Boolean = false,
    val errorMessage: String? = null,
)

@HiltViewModel
internal class FridaLogDetailViewModel @Inject constructor(
    private val repository: FridaLogRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(FridaLogDetailUiState())
    val uiState: StateFlow<FridaLogDetailUiState> = _uiState.asStateFlow()

    fun load(packageName: String, logId: Long) {
        if (packageName.isBlank() || logId <= 0L) {
            _uiState.value = FridaLogDetailUiState(errorMessage = "日志参数无效")
            return
        }
        _uiState.value = FridaLogDetailUiState(loading = true)
        viewModelScope.launch {
            when (val result = repository.queryLogDetail(packageName, logId)) {
                is AppResult.Success -> {
                    _uiState.value = FridaLogDetailUiState(
                        log = result.data,
                        errorMessage = if (result.data == null) "日志不存在" else null,
                    )
                }
                is AppResult.Failure -> {
                    _uiState.value = FridaLogDetailUiState(errorMessage = result.error.toString())
                }
            }
        }
    }
}

@Composable
internal fun FridaLogDetailRoute(
    packageName: String,
    logId: Long,
    onBackClick: () -> Unit,
    viewModel: FridaLogDetailViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(packageName, logId) {
        viewModel.load(packageName, logId)
    }

    val log = uiState.log
    if (log != null) {
        FridaLogDetail(log = log, onBackClick = onBackClick)
    } else {
        FridaLogDetailPlaceholder(
            loading = uiState.loading,
            message = uiState.errorMessage ?: "正在加载日志详情",
            onBackClick = onBackClick,
        )
    }
}

@Composable
internal fun FridaLogDetail(
    log: HookLogRecord,
    onBackClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .hookConfigGradientBackground(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 8.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.Outlined.ArrowBack,
                    contentDescription = "返回",
                    tint = FridaDetailTextPrimary,
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Frida日志详情",
                    style = MaterialTheme.typography.titleMedium,
                    color = FridaDetailTextPrimary,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = log.title,
                    style = MaterialTheme.typography.bodySmall,
                    color = FridaDetailTextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            SelectionContainer {
                FridaLogDetailSection("基础信息") {
                    FridaLogDetailField("脚本", log.title)
                    FridaLogDetailField("级别", log.typeLabel)
                    FridaLogDetailField("时间", log.time)
                    FridaLogDetailField("包名", log.packageName)
                }
            }
            FridaLogDetailTextSection(
                title = "完整内容",
                text = log.content.ifBlank { "暂无内容" },
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun FridaLogDetailPlaceholder(
    loading: Boolean,
    message: String,
    onBackClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .hookConfigGradientBackground(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 8.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.Outlined.ArrowBack,
                    contentDescription = "返回",
                    tint = FridaDetailTextPrimary,
                )
            }
            Text(
                text = "Frida日志详情",
                style = MaterialTheme.typography.titleMedium,
                color = FridaDetailTextPrimary,
                fontWeight = FontWeight.SemiBold,
            )
        }
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                if (loading) {
                    CircularProgressIndicator(color = HookConfigPrimaryBlue)
                }
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = FridaDetailTextSecondary,
                )
            }
        }
    }
}

@Composable
private fun FridaLogDetailSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White.copy(alpha = 0.74f), RoundedCornerShape(14.dp))
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = FridaDetailTextPrimary,
            fontWeight = FontWeight.SemiBold,
        )
        content()
    }
}

@Composable
private fun FridaLogDetailField(
    label: String,
    value: String,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = label,
            modifier = Modifier.width(72.dp),
            style = MaterialTheme.typography.bodySmall,
            color = FridaDetailTextSecondary,
        )
        Text(
            text = value.ifBlank { "-" },
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodySmall,
            color = FridaDetailTextPrimary,
        )
    }
}

@Composable
private fun FridaLogDetailTextSection(
    title: String,
    text: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.White.copy(alpha = 0.74f), RoundedCornerShape(14.dp))
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = FridaDetailTextPrimary,
            fontWeight = FontWeight.SemiBold,
        )
        SelectionContainer(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
            ) {
                Text(
                    text = text,
                    style = MaterialTheme.typography.bodySmall,
                    color = FridaDetailTextPrimary,
                )
            }
        }
    }
}
