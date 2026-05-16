package com.svwh.tools.feature.home.presentation

import androidx.lifecycle.ViewModel
import com.svwh.tools.feature.home.domain.model.ToolShortcut
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class HomeUiState(
    val tools: List<ToolShortcut> = emptyList(),
)

@HiltViewModel
class HomeViewModel @Inject constructor() : ViewModel() {
    private val _uiState = MutableStateFlow(
        HomeUiState(
            tools = listOf(
                ToolShortcut(
                    id = "permissions",
                    title = "Permission Center",
                    description = "A safe entry point for runtime permissions.",
                ),
                ToolShortcut(
                    id = "network",
                    title = "Network Toolkit",
                    description = "Retrofit, OkHttp, errors, and connectivity are ready.",
                ),
                ToolShortcut(
                    id = "theme",
                    title = "Theme Lab",
                    description = "Light, dark, and dynamic color support.",
                ),
            ),
        ),
    )

    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()
}
