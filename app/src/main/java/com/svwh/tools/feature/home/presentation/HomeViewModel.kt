package com.svwh.tools.feature.home.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.svwh.tools.core.common.AppResult
import com.svwh.tools.feature.hookconfig.domain.model.FridaScriptItem
import com.svwh.tools.feature.hookconfig.domain.model.GlobalFridaScriptScope
import com.svwh.tools.feature.hookconfig.domain.repository.FridaScriptRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class HomeUiState(
    val globalFridaScripts: List<FridaScriptItem> = emptyList(),
    val isLoadingFridaScripts: Boolean = false,
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val fridaScriptRepository: FridaScriptRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadGlobalFridaScripts()
    }

    fun loadGlobalFridaScripts() {
        _uiState.value = _uiState.value.copy(isLoadingFridaScripts = true)
        viewModelScope.launch {
            when (
                val result = fridaScriptRepository.getScripts(
                    envType = GlobalFridaScriptScope.ENV_TYPE,
                    packageName = GlobalFridaScriptScope.PACKAGE_NAME,
                )
            ) {
                is AppResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        globalFridaScripts = result.data,
                        isLoadingFridaScripts = false,
                    )
                }
                is AppResult.Failure -> {
                    _uiState.value = _uiState.value.copy(isLoadingFridaScripts = false)
                }
            }
        }
    }
}
