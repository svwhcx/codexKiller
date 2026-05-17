package com.svwh.tools.core.designsystem.component

import androidx.compose.animation.core.animate
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

enum class LoadMoreUiState {
    Idle,
    Loading,
    NoMore,
}

private val RefreshTriggerDistance = 84.dp
private val RefreshIndicatorHeight = 76.dp
private val LoadMoreTriggerDistance = 82.dp
private val LoadMoreFooterHeight = 76.dp

@Composable
fun RefreshLoadMoreLazyColumn(
    modifier: Modifier = Modifier,
    listState: LazyListState = rememberLazyListState(),
    contentPadding: PaddingValues = PaddingValues(),
    isRefreshing: Boolean,
    loadMoreState: LoadMoreUiState,
    onRefresh: () -> Unit,
    onLoadMore: () -> Unit,
    content: LazyListScope.() -> Unit,
) {
    val density = LocalDensity.current
    val scope = rememberCoroutineScope()
    val refreshTriggerPx = with(density) { RefreshTriggerDistance.toPx() }
    val refreshIndicatorPx = with(density) { RefreshIndicatorHeight.toPx() }
    val loadMoreTriggerPx = with(density) { LoadMoreTriggerDistance.toPx() }
    val loadMoreFooterPx = with(density) { LoadMoreFooterHeight.toPx() }
    val maxPullPx = with(density) { 148.dp.toPx() }
    var refreshPullPx by remember { mutableFloatStateOf(0f) }
    var loadMorePullPx by remember { mutableFloatStateOf(0f) }

    fun animateRefreshPull(target: Float) {
        scope.launch {
            animate(refreshPullPx, target) { value, _ ->
                refreshPullPx = value
            }
        }
    }

    fun animateLoadMorePull(target: Float) {
        scope.launch {
            animate(loadMorePullPx, target) { value, _ ->
                loadMorePullPx = value
            }
        }
    }

    fun finishPull() {
        if (refreshPullPx >= refreshTriggerPx && !isRefreshing) {
            refreshPullPx = refreshIndicatorPx
            animateRefreshPull(refreshIndicatorPx)
            onRefresh()
        } else {
            animateRefreshPull(0f)
        }

        if (loadMorePullPx >= loadMoreTriggerPx && loadMoreState != LoadMoreUiState.Loading) {
            loadMorePullPx = loadMoreFooterPx
            animateLoadMorePull(loadMoreFooterPx)
            onLoadMore()
        } else if (loadMoreState != LoadMoreUiState.Loading) {
            animateLoadMorePull(0f)
        }
    }

    val nestedScrollConnection = remember(
        listState,
        isRefreshing,
        loadMoreState,
        refreshPullPx,
        loadMorePullPx,
    ) {
        object : NestedScrollConnection {
            override fun onPostScroll(
                consumed: Offset,
                available: Offset,
                source: NestedScrollSource,
            ): Offset {
                if (source != NestedScrollSource.UserInput) return Offset.Zero

                val deltaY = available.y
                if (deltaY > 0f && !listState.canScrollBackward && !isRefreshing) {
                    refreshPullPx = resistedPull(
                        current = refreshPullPx,
                        delta = deltaY,
                        max = maxPullPx,
                    )
                    return Offset(x = 0f, y = deltaY)
                }

                if (deltaY < 0f && !listState.canScrollForward && loadMoreState != LoadMoreUiState.Loading) {
                    loadMorePullPx = resistedPull(
                        current = loadMorePullPx,
                        delta = -deltaY,
                        max = maxPullPx,
                    )
                    return Offset(x = 0f, y = deltaY)
                }

                return Offset.Zero
            }

            override suspend fun onPreFling(available: Velocity): Velocity {
                finishPull()
                return Velocity.Zero
            }

            override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
                finishPull()
                return Velocity.Zero
            }
        }
    }

    LaunchedEffect(isRefreshing) {
        val target = if (isRefreshing) refreshIndicatorPx else 0f
        animate(refreshPullPx, target) { value, _ ->
            refreshPullPx = value
        }
    }

    LaunchedEffect(loadMoreState) {
        val target = when (loadMoreState) {
            LoadMoreUiState.Loading,
            LoadMoreUiState.NoMore -> loadMoreFooterPx
            LoadMoreUiState.Idle -> 0f
        }
        animate(loadMorePullPx, target) { value, _ ->
            loadMorePullPx = value
        }
    }

    LaunchedEffect(listState, loadMoreState) {
        snapshotFlow { listState.isScrollInProgress to listState.isAtListBottom() }
            .distinctUntilChanged()
            .collect { (scrolling, atBottom) ->
                if (scrolling && atBottom && loadMoreState == LoadMoreUiState.Idle) {
                    loadMorePullPx = loadMoreFooterPx
                    onLoadMore()
                }
            }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .nestedScroll(nestedScrollConnection),
    ) {
        RefreshIndicator(
            heightPx = refreshPullPx,
            triggerPx = refreshTriggerPx,
            isRefreshing = isRefreshing,
        )
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            state = listState,
            contentPadding = contentPadding,
        ) {
            content()
        }
        LoadMoreFooter(
            heightPx = loadMorePullPx,
            triggerPx = loadMoreTriggerPx,
            state = loadMoreState,
        )
    }
}

@Composable
private fun RefreshIndicator(
    heightPx: Float,
    triggerPx: Float,
    isRefreshing: Boolean,
) {
    val density = LocalDensity.current
    val height = with(density) { heightPx.toDp() }
    val text = when {
        isRefreshing -> "正在刷新"
        heightPx >= triggerPx -> "松手刷新"
        heightPx > 0f -> "下拉刷新"
        else -> ""
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(height),
        contentAlignment = Alignment.Center,
    ) {
        if (text.isNotEmpty()) {
            RefreshLoadMoreStatus(text = text, showProgress = isRefreshing)
        }
    }
}

@Composable
private fun LoadMoreFooter(
    heightPx: Float,
    triggerPx: Float,
    state: LoadMoreUiState,
) {
    val density = LocalDensity.current
    val height = with(density) { heightPx.toDp() }
    val text = when (state) {
        LoadMoreUiState.Loading -> "正在加载"
        LoadMoreUiState.NoMore -> "暂无更多"
        LoadMoreUiState.Idle -> {
            if (heightPx >= triggerPx) {
                "松手加载更多"
            } else if (heightPx > 0f) {
                "上拉加载更多"
            } else {
                ""
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(height),
        contentAlignment = Alignment.Center,
    ) {
        if (text.isNotEmpty()) {
            RefreshLoadMoreStatus(
                text = text,
                showProgress = state == LoadMoreUiState.Loading,
            )
        }
    }
}

@Composable
private fun RefreshLoadMoreStatus(
    text: String,
    showProgress: Boolean,
) {
    Row(
        modifier = Modifier
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.42f),
                shape = RoundedCornerShape(999.dp),
            )
            .padding(horizontal = 12.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        if (showProgress) {
            CircularProgressIndicator(
                modifier = Modifier.size(18.dp),
                strokeWidth = 2.dp,
            )
        }
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

private fun resistedPull(
    current: Float,
    delta: Float,
    max: Float,
): Float {
    val progress = (current / max).coerceIn(0f, 0.92f)
    val resistance = (1f - progress) * (1f - progress)
    return (current + delta * resistance).coerceIn(0f, max)
}

private fun LazyListState.isAtListBottom(): Boolean {
    val lastVisibleItem = layoutInfo.visibleItemsInfo.lastOrNull() ?: return false
    if (layoutInfo.totalItemsCount == 0) return false
    return lastVisibleItem.index >= layoutInfo.totalItemsCount - 2 &&
        lastVisibleItem.offset + lastVisibleItem.size <= layoutInfo.viewportEndOffset + 2
}
