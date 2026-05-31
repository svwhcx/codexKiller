package com.svwh.tools.feature.hookconfig.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.svwh.tools.core.common.AppError
import com.svwh.tools.core.common.AppResult
import com.svwh.tools.feature.hookconfig.domain.model.FridaScriptDraft
import com.svwh.tools.feature.hookconfig.domain.model.FridaScriptItem
import com.svwh.tools.feature.hookconfig.domain.model.GlobalFridaScriptScope
import com.svwh.tools.feature.hookconfig.domain.repository.FridaScriptRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

private const val ERROR_SCRIPT_NAME_REQUIRED = "脚本名称不能为空"
private const val ERROR_SCRIPT_NAME_DUPLICATE = "脚本名称已存在"
private const val ERROR_UPDATE_SCRIPT_SWITCH = "更新脚本开关失败"
private const val ERROR_DELETE_SCRIPT = "删除脚本失败"

internal data class FridaScriptUiState(
    val envType: String = "",
    val packageName: String = "",
    val items: List<FridaScriptItem> = emptyList(),
    val globalItems: List<FridaScriptItem> = emptyList(),
    val editingDraft: FridaScriptDraft? = null,
    val selectedIds: Set<Long> = emptySet(),
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val isSelectionMode: Boolean = false,
    val saveSuccessToken: Long = 0L,
    val showImportDialog: Boolean = false,
    val errorMessage: String? = null,
)

@HiltViewModel
internal class FridaScriptViewModel @Inject constructor(
    private val repository: FridaScriptRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(FridaScriptUiState())
    internal val uiState: StateFlow<FridaScriptUiState> = _uiState.asStateFlow()

    fun initialize(envType: String, packageName: String) {
        val current = _uiState.value
        if (current.envType == envType && current.packageName == packageName) {
            return
        }
        _uiState.value = FridaScriptUiState(
            envType = envType,
            packageName = packageName,
            isLoading = true,
        )
        loadScripts()
    }

    fun loadScripts() {
        val state = _uiState.value
        if (state.envType.isBlank() || state.packageName.isBlank()) return
        _uiState.value = state.copy(isLoading = true, errorMessage = null)
        viewModelScope.launch {
            when (
                val result = repository.getScripts(
                    envType = state.envType,
                    packageName = state.packageName,
                )
            ) {
                is AppResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        items = result.data,
                        isLoading = false,
                        errorMessage = null,
                        selectedIds = emptySet(),
                        isSelectionMode = false,
                    )
                }
                is AppResult.Failure -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = result.error.toUserMessage(),
                    )
                }
            }
        }
    }

    fun loadGlobalScripts() {
        viewModelScope.launch {
            when (
                val result = repository.getScripts(
                    envType = GlobalFridaScriptScope.ENV_TYPE,
                    packageName = GlobalFridaScriptScope.PACKAGE_NAME,
                )
            ) {
                is AppResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        globalItems = result.data,
                        errorMessage = null,
                    )
                }
                is AppResult.Failure -> {
                    _uiState.value = _uiState.value.copy(errorMessage = result.error.toUserMessage())
                }
            }
        }
    }

    fun startCreate() {
        val state = _uiState.value
        _uiState.value = state.copy(
            editingDraft = FridaScriptDraft(
                packageName = state.packageName,
                envType = state.envType,
            ),
            errorMessage = null,
        )
    }

    fun startEdit(id: Long) {
        val state = _uiState.value
        viewModelScope.launch {
            when (
                val result = repository.getScriptById(
                    envType = state.envType,
                    packageName = state.packageName,
                    id = id,
                )
            ) {
                is AppResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        editingDraft = result.data,
                        errorMessage = null,
                    )
                }
                is AppResult.Failure -> {
                    _uiState.value = _uiState.value.copy(errorMessage = result.error.toUserMessage())
                }
            }
        }
    }

    fun closeEditor() {
        _uiState.value = _uiState.value.copy(editingDraft = null)
    }

    fun consumeSaveSuccess() {
        _uiState.value = _uiState.value.copy(
            editingDraft = null,
            saveSuccessToken = 0L,
        )
    }

    fun updateDraft(transform: (FridaScriptDraft) -> FridaScriptDraft) {
        val draft = _uiState.value.editingDraft ?: return
        _uiState.value = _uiState.value.copy(
            editingDraft = transform(draft),
            errorMessage = null,
        )
    }

    fun saveDraft() {
        val draft = _uiState.value.editingDraft ?: return
        val validationError = validateDraftForSave(draft, _uiState.value.items)
        if (validationError != null) {
            _uiState.value = _uiState.value.copy(
                isSaving = false,
                errorMessage = validationError,
            )
            return
        }
        _uiState.value = _uiState.value.copy(isSaving = true, errorMessage = null)
        viewModelScope.launch {
            when (val result = repository.saveScript(draft)) {
                is AppResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isSaving = false,
                        errorMessage = null,
                        saveSuccessToken = System.currentTimeMillis(),
                    )
                    loadScripts()
                }
                is AppResult.Failure -> {
                    _uiState.value = _uiState.value.copy(
                        isSaving = false,
                        errorMessage = result.error.toUserMessage(),
                    )
                }
            }
        }
    }

    fun toggleEnabled(item: FridaScriptItem) {
        viewModelScope.launch {
            when (
                repository.updateEnabled(
                    envType = item.envType,
                    packageName = item.packageName,
                    id = item.id,
                    enabled = !item.enabled,
                )
            ) {
                is AppResult.Success -> loadScripts()
                is AppResult.Failure -> {
                    _uiState.value = _uiState.value.copy(errorMessage = ERROR_UPDATE_SCRIPT_SWITCH)
                }
            }
        }
    }

    fun enterSelectionMode(initialId: Long) {
        _uiState.value = _uiState.value.copy(
            isSelectionMode = true,
            selectedIds = setOf(initialId),
        )
    }

    fun toggleSelection(id: Long) {
        val selected = _uiState.value.selectedIds
        val next = if (id in selected) selected - id else selected + id
        _uiState.value = _uiState.value.copy(
            selectedIds = next,
            isSelectionMode = next.isNotEmpty(),
        )
    }

    fun cancelSelection() {
        _uiState.value = _uiState.value.copy(
            selectedIds = emptySet(),
            isSelectionMode = false,
        )
    }

    fun selectAll() {
        _uiState.value = _uiState.value.copy(
            selectedIds = _uiState.value.items.map { it.id }.toSet(),
            isSelectionMode = _uiState.value.items.isNotEmpty(),
        )
    }

    fun reverseSelection() {
        val current = _uiState.value.selectedIds
        val next = _uiState.value.items.map { it.id }.filterNot { it in current }.toSet()
        _uiState.value = _uiState.value.copy(
            selectedIds = next,
            isSelectionMode = _uiState.value.items.isNotEmpty(),
        )
    }

    fun deleteSelected() {
        val ids = _uiState.value.selectedIds.toList()
        if (ids.isEmpty()) return
        val state = _uiState.value
        viewModelScope.launch {
            when (
                repository.deleteScripts(
                    envType = state.envType,
                    packageName = state.packageName,
                    ids = ids,
                )
            ) {
                is AppResult.Success -> loadScripts()
                is AppResult.Failure -> {
                    _uiState.value = _uiState.value.copy(errorMessage = ERROR_DELETE_SCRIPT)
                }
            }
        }
    }

    fun openImportDialog() {
        _uiState.value = _uiState.value.copy(showImportDialog = true, errorMessage = null)
        loadGlobalScripts()
    }

    fun closeImportDialog() {
        _uiState.value = _uiState.value.copy(showImportDialog = false)
    }

    fun importGlobalScript(item: FridaScriptItem) {
        val state = _uiState.value
        if (state.packageName.isBlank() || state.envType.isBlank()) return
        val importedName = uniqueImportedName(item.name, state.items)
        viewModelScope.launch {
            when (
                val result = repository.saveScript(
                    FridaScriptDraft(
                        packageName = state.packageName,
                        envType = state.envType,
                        name = importedName,
                        scriptContent = item.scriptContent,
                        enabled = item.enabled,
                    ),
                )
            ) {
                is AppResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        showImportDialog = false,
                        errorMessage = null,
                    )
                    loadScripts()
                }
                is AppResult.Failure -> {
                    _uiState.value = _uiState.value.copy(errorMessage = result.error.toUserMessage())
                }
            }
        }
    }

    private fun validateDraftForSave(
        draft: FridaScriptDraft,
        existingItems: List<FridaScriptItem>,
    ): String? {
        val name = draft.name.trim()
        if (name.isBlank()) return ERROR_SCRIPT_NAME_REQUIRED
        val hasDuplicateName = existingItems.any { item ->
            item.id != draft.id && item.name.trim() == name
        }
        if (hasDuplicateName) return ERROR_SCRIPT_NAME_DUPLICATE
        return null
    }

    private fun uniqueImportedName(
        baseName: String,
        existingItems: List<FridaScriptItem>,
    ): String {
        val names = existingItems.map { it.name.trim() }.toSet()
        val trimmed = baseName.trim().ifBlank { "Imported Script" }
        if (trimmed !in names) return trimmed
        var index = 2
        while (true) {
            val candidate = "$trimmed ($index)"
            if (candidate !in names) return candidate
            index++
        }
    }

    private fun AppError.toUserMessage(): String {
        return when (this) {
            is AppError.Unknown -> throwable.message ?: throwable.localizedMessage ?: throwable.toString()
            is AppError.Http -> message ?: "HTTP $code"
            AppError.NetworkUnavailable -> "网络不可用"
            AppError.Timeout -> "请求超时"
            is AppError.Serialization -> message ?: "数据解析失败"
        }
    }
}
