package com.svwh.tools.feature.hookconfig.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.svwh.tools.core.common.AppError
import com.svwh.tools.core.common.AppResult
import com.svwh.tools.feature.hookconfig.domain.model.FridaScriptDraft
import com.svwh.tools.feature.hookconfig.domain.model.FridaScriptItem
import com.svwh.tools.feature.hookconfig.domain.repository.FridaScriptRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

private const val ERROR_SCRIPT_NAME_REQUIRED = "\u811a\u672c\u540d\u79f0\u4e0d\u80fd\u4e3a\u7a7a"
private const val ERROR_SCRIPT_NAME_DUPLICATE = "\u811a\u672c\u540d\u79f0\u5df2\u5b58\u5728"
private const val ERROR_UPDATE_SCRIPT_SWITCH = "\u66f4\u65b0\u811a\u672c\u5f00\u5173\u5931\u8d25"
private const val ERROR_DELETE_SCRIPT = "\u5220\u9664\u811a\u672c\u5931\u8d25"

internal data class FridaScriptUiState(
    val envType: String = "",
    val packageName: String = "",
    val items: List<FridaScriptItem> = emptyList(),
    val editingDraft: FridaScriptDraft? = null,
    val selectedIds: Set<Long> = emptySet(),
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val isSelectionMode: Boolean = false,
    val saveSuccessToken: Long = 0L,
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
        viewModelScope.launch {
            when (val result = repository.getScriptById(id)) {
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
            when (repository.updateEnabled(item.id, !item.enabled)) {
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
            isSelectionMode = next.isNotEmpty(),
        )
    }

    fun deleteSelected() {
        val ids = _uiState.value.selectedIds.toList()
        if (ids.isEmpty()) return
        viewModelScope.launch {
            when (repository.deleteScripts(ids)) {
                is AppResult.Success -> loadScripts()
                is AppResult.Failure -> {
                    _uiState.value = _uiState.value.copy(errorMessage = ERROR_DELETE_SCRIPT)
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

    private fun AppError.toUserMessage(): String {
        return when (this) {
            is AppError.Unknown -> throwable.message ?: throwable.localizedMessage ?: throwable.toString()
            is AppError.Http -> message ?: "HTTP $code"
            AppError.NetworkUnavailable -> "\u7f51\u7edc\u4e0d\u53ef\u7528"
            AppError.Timeout -> "\u8bf7\u6c42\u8d85\u65f6"
            is AppError.Serialization -> message ?: "\u6570\u636e\u89e3\u6790\u5931\u8d25"
        }
    }
}
