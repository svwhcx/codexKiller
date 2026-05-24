package com.svwh.tools.feature.hookconfig.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.svwh.tools.core.common.AppError
import com.svwh.tools.core.common.AppResult
import com.svwh.tools.feature.hookconfig.domain.model.UserHookConfigDraft
import com.svwh.tools.feature.hookconfig.domain.model.UserHookConfigItem
import com.svwh.tools.feature.hookconfig.domain.model.UserHookConfigRule
import com.svwh.tools.feature.hookconfig.domain.repository.UserHookConfigRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

private const val ERROR_UPDATE_SWITCH = "\u66f4\u65b0\u5f00\u5173\u5931\u8d25"
private const val ERROR_DELETE_CONFIG = "\u5220\u9664\u914d\u7f6e\u5931\u8d25"
private const val ERROR_CONFIG_NAME_REQUIRED = "\u914d\u7f6e\u540d\u79f0\u4e0d\u80fd\u4e3a\u7a7a"
private const val ERROR_CLASS_NAME_REQUIRED = "\u7c7b\u540d\u4e0d\u80fd\u4e3a\u7a7a"
private const val ERROR_METHOD_NAME_REQUIRED = "\u65b9\u6cd5\u540d\u4e0d\u80fd\u4e3a\u7a7a"
private const val ERROR_CONFIG_NAME_DUPLICATE = "\u914d\u7f6e\u540d\u79f0\u5df2\u5b58\u5728"
private const val ERROR_METHOD_SIGNATURE_DUPLICATE = "\u76f8\u540c\u7684 Hook \u65b9\u6cd5\u7b7e\u540d\u5df2\u5b58\u5728"

internal data class UserHookConfigUiState(
    val envType: String = "",
    val packageName: String = "",
    val items: List<UserHookConfigItem> = emptyList(),
    val editingDraft: UserHookConfigDraft? = null,
    val selectedIds: Set<Long> = emptySet(),
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val isSelectionMode: Boolean = false,
    val saveSuccessToken: Long = 0L,
    val errorMessage: String? = null,
)

@HiltViewModel
internal class UserHookConfigViewModel @Inject constructor(
    private val repository: UserHookConfigRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(UserHookConfigUiState())
    internal val uiState: StateFlow<UserHookConfigUiState> = _uiState.asStateFlow()

    fun initialize(envType: String, packageName: String) {
        val current = _uiState.value
        if (current.envType == envType && current.packageName == packageName) {
            return
        }
        _uiState.value = UserHookConfigUiState(
            envType = envType,
            packageName = packageName,
            isLoading = true,
        )
        loadConfigs()
    }

    fun loadConfigs() {
        val state = _uiState.value
        if (state.envType.isBlank() || state.packageName.isBlank()) return
        _uiState.value = state.copy(isLoading = true, errorMessage = null)
        viewModelScope.launch {
            when (
                val result = repository.getConfigs(
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
                        errorMessage = result.error.toString(),
                    )
                }
            }
        }
    }

    fun startCreate() {
        val state = _uiState.value
        _uiState.value = state.copy(
            editingDraft = UserHookConfigDraft(
                packageName = state.packageName,
                envType = state.envType,
            ),
            errorMessage = null,
        )
    }

    fun startEdit(id: Long) {
        viewModelScope.launch {
            when (val result = repository.getConfigById(id)) {
                is AppResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        editingDraft = result.data,
                        errorMessage = null,
                    )
                }
                is AppResult.Failure -> {
                    _uiState.value = _uiState.value.copy(errorMessage = result.error.toString())
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

    fun updateDraft(transform: (UserHookConfigDraft) -> UserHookConfigDraft) {
        val draft = _uiState.value.editingDraft ?: return
        _uiState.value = _uiState.value.copy(
            editingDraft = transform(draft),
            errorMessage = null,
        )
    }

    fun addRule(rule: UserHookConfigRule) {
        updateDraft { draft -> draft.copy(rules = draft.rules + rule) }
    }

    fun removeRule(ruleIndex: Int) {
        updateDraft { draft ->
            draft.copy(rules = draft.rules.filterIndexed { index, _ -> index != ruleIndex })
        }
    }

    fun updateRule(ruleIndex: Int, transform: (UserHookConfigRule) -> UserHookConfigRule) {
        updateDraft { draft ->
            draft.copy(
                rules = draft.rules.mapIndexed { index, rule ->
                    if (index == ruleIndex) transform(rule) else rule
                },
            )
        }
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
            when (val result = repository.saveConfig(draft)) {
                is AppResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isSaving = false,
                        errorMessage = null,
                        saveSuccessToken = System.currentTimeMillis(),
                    )
                    loadConfigs()
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

    fun toggleEnabled(item: UserHookConfigItem) {
        viewModelScope.launch {
            when (repository.updateEnabled(item.id, !item.enabled)) {
                is AppResult.Success -> loadConfigs()
                is AppResult.Failure -> {
                    _uiState.value = _uiState.value.copy(errorMessage = ERROR_UPDATE_SWITCH)
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
            when (repository.deleteConfigs(ids)) {
                is AppResult.Success -> loadConfigs()
                is AppResult.Failure -> {
                    _uiState.value = _uiState.value.copy(errorMessage = ERROR_DELETE_CONFIG)
                }
            }
        }
    }

    private fun validateDraftForSave(
        draft: UserHookConfigDraft,
        existingItems: List<UserHookConfigItem>,
    ): String? {
        val configName = draft.configName.trim()
        val className = draft.className.trim()
        val methodName = draft.methodName.trim()
        val params = draft.params.trim()

        if (configName.isBlank()) return ERROR_CONFIG_NAME_REQUIRED
        if (className.isBlank()) return ERROR_CLASS_NAME_REQUIRED
        if (methodName.isBlank()) return ERROR_METHOD_NAME_REQUIRED

        val hasDuplicateName = existingItems.any { item ->
            item.id != draft.id && item.configName.trim() == configName
        }
        if (hasDuplicateName) return ERROR_CONFIG_NAME_DUPLICATE

        val hasDuplicateSignature = existingItems.any { item ->
            item.id != draft.id &&
                item.className.trim() == className &&
                item.methodName.trim() == methodName &&
                item.params.trim() == params
        }
        if (hasDuplicateSignature) return ERROR_METHOD_SIGNATURE_DUPLICATE

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
