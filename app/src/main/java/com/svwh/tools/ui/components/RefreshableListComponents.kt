package com.svwh.tools.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.MutatePriority
import androidx.compose.foundation.gestures.ScrollableState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.pow

enum class ListOrientation {
    Vertical, Horizontal
}

enum class LoadMoreStatus {
    Idle, Pulling, CanRelease, Loading, NoMoreData
}

enum class RefreshStatus {
    Idle, Pulling, CanRelease, Refreshing
}

/**
 * 核心逻辑包装器 - 像 Vue 插槽一样包装任何可滚动组件
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RefreshableWrapper(
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    isLoadingMore: Boolean,
    onLoadMore: () -> Unit,
    hasMoreData: Boolean = true,
    scrollableState: ScrollableState? = null,
    orientation: ListOrientation = ListOrientation.Vertical,
    modifier: Modifier = Modifier,
    pullThreshold: Dp = 80.dp,
    maxPullDistance: Dp = 160.dp,
    noMoreDataDuration: Long = 2000L,
    loadMoreContent: @Composable (status: LoadMoreStatus, progress: Float) -> Unit = { status, progress ->
        DefaultLoadMoreContent(status, progress, orientation)
    },
    noMoreDataContent: @Composable () -> Unit = { DefaultNoMoreDataContent(orientation) },
    refreshContent: @Composable (status: RefreshStatus, progress: Float) -> Unit = { status, progress ->
        DefaultRefreshContent(status, progress, orientation)
    },
    content: @Composable () -> Unit
) {
    val scope = rememberCoroutineScope()
    val density = LocalDensity.current
    val thresholdPx = with(density) { pullThreshold.toPx() }
    val maxPullPx = with(density) { maxPullDistance.toPx() }

    // Load More States
    var pullLoadMoreOffsetPx by remember { mutableFloatStateOf(0f) }
    val animatableLoadMoreOffset = remember { Animatable(0f) }
    var isLoadMoreAnimating by remember { mutableStateOf(false) }
    var currentLoadMoreStatus by remember { mutableStateOf(LoadMoreStatus.Idle) }
    var showNoMoreData by remember { mutableStateOf(false) }

    // Pull Refresh States
    var pullRefreshOffsetPx by remember { mutableFloatStateOf(0f) }
    val animatableRefreshOffset = remember { Animatable(0f) }
    var isRefreshAnimating by remember { mutableStateOf(false) }
    var currentRefreshStatus by remember { mutableStateOf(RefreshStatus.Idle) }

    val displayOffset = if (isLoadMoreAnimating) animatableLoadMoreOffset.value else pullLoadMoreOffsetPx +
            if (isRefreshAnimating) animatableRefreshOffset.value else pullRefreshOffsetPx
    val progress = (abs(displayOffset) / thresholdPx).coerceIn(0f, 1f)

    fun triggerLoadMoreRelease(targetOffset: Float = 0f) {
        if (isLoadMoreAnimating) return
        isLoadMoreAnimating = true
        scope.launch {
            animatableLoadMoreOffset.snapTo(pullLoadMoreOffsetPx)
            if (currentLoadMoreStatus == LoadMoreStatus.CanRelease) {
                currentLoadMoreStatus = LoadMoreStatus.Loading
                animatableLoadMoreOffset.animateTo(-thresholdPx)
                onLoadMore()
            } else {
                animatableLoadMoreOffset.animateTo(targetOffset)
                currentLoadMoreStatus = if (targetOffset == 0f) LoadMoreStatus.Idle else currentLoadMoreStatus
            }
            pullLoadMoreOffsetPx = animatableLoadMoreOffset.value
            isLoadMoreAnimating = false
        }
    }

    fun triggerRefreshRelease(targetOffset: Float = 0f) {
        if (isRefreshAnimating) return
        isRefreshAnimating = true
        scope.launch {
            animatableRefreshOffset.snapTo(pullRefreshOffsetPx)
            if (currentRefreshStatus == RefreshStatus.CanRelease) {
                currentRefreshStatus = RefreshStatus.Refreshing
                animatableRefreshOffset.animateTo(thresholdPx)
                onRefresh()
            } else {
                animatableRefreshOffset.animateTo(targetOffset)
                currentRefreshStatus = if (targetOffset == 0f) RefreshStatus.Idle else currentRefreshStatus
            }
            pullRefreshOffsetPx = animatableRefreshOffset.value
            isRefreshAnimating = false
        }
    }

    LaunchedEffect(isLoadingMore) {
        if (isLoadingMore) {
            currentLoadMoreStatus = LoadMoreStatus.Loading
            isLoadMoreAnimating = true
            animatableLoadMoreOffset.snapTo(pullLoadMoreOffsetPx)
            animatableLoadMoreOffset.animateTo(-thresholdPx)
        } else if (currentLoadMoreStatus == LoadMoreStatus.Loading) {
            if (!hasMoreData) {
                currentLoadMoreStatus = LoadMoreStatus.NoMoreData
                showNoMoreData = true
                delay(noMoreDataDuration)
                showNoMoreData = false
            }
            animatableLoadMoreOffset.animateTo(0f)
            pullLoadMoreOffsetPx = 0f
            isLoadMoreAnimating = false
            currentLoadMoreStatus = LoadMoreStatus.Idle
        }
    }

    LaunchedEffect(isRefreshing) {
        if (isRefreshing) {
            currentRefreshStatus = RefreshStatus.Refreshing
            isRefreshAnimating = true
            animatableRefreshOffset.snapTo(pullRefreshOffsetPx)
            animatableRefreshOffset.animateTo(thresholdPx)
        } else if (currentRefreshStatus == RefreshStatus.Refreshing) {
            animatableRefreshOffset.animateTo(0f)
            pullRefreshOffsetPx = 0f
            isRefreshAnimating = false
            currentRefreshStatus = RefreshStatus.Idle
        }
    }

    val nestedScrollConnection = remember(orientation, thresholdPx, maxPullPx, hasMoreData, isLoadingMore, isRefreshing) {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                // Handle pull-up (load more) release when scrolling down
                if (pullLoadMoreOffsetPx < 0) {
                    val delta = if (orientation == ListOrientation.Vertical) available.y else available.x
                    if (delta > 0) { // Scrolling down
                        val newOffset = (pullLoadMoreOffsetPx + delta).coerceAtMost(0f)
                        val consumed = pullLoadMoreOffsetPx - newOffset
                        pullLoadMoreOffsetPx = newOffset
                        currentLoadMoreStatus = updateLoadMoreStatus(newOffset, thresholdPx)
                        return if (orientation == ListOrientation.Vertical) Offset(0f, consumed) else Offset(consumed, 0f)
                    }
                }
                // Handle pull-down (refresh) release when scrolling up
                if (pullRefreshOffsetPx > 0) {
                    val delta = if (orientation == ListOrientation.Vertical) available.y else available.x
                    if (delta < 0) { // Scrolling up
                        val newOffset = (pullRefreshOffsetPx + delta).coerceAtLeast(0f)
                        val consumed = pullRefreshOffsetPx - newOffset
                        pullRefreshOffsetPx = newOffset
                        currentRefreshStatus = updateRefreshStatus(newOffset, thresholdPx)
                        return if (orientation == ListOrientation.Vertical) Offset(0f, consumed) else Offset(consumed, 0f)
                    }
                }
                return Offset.Zero
            }

            override fun onPostScroll(consumed: Offset, available: Offset, source: NestedScrollSource): Offset {
                val delta = if (orientation == ListOrientation.Vertical) available.y else available.x
                // Handle pull-up (load more)
                if (delta < 0 && !isLoadMoreAnimating && !isRefreshing && !showNoMoreData) { // Scrolling up
                    val resistance = (abs(pullLoadMoreOffsetPx) / maxPullPx).coerceIn(0f, 1f).pow(2)
                    val newOffset = (pullLoadMoreOffsetPx + delta * (1f - resistance)).coerceAtLeast(-maxPullPx)
                    pullLoadMoreOffsetPx = newOffset
                    currentLoadMoreStatus = updateLoadMoreStatus(newOffset, thresholdPx)

                    if (source == NestedScrollSource.SideEffect && abs(newOffset) >= thresholdPx) {
                        scrollableState?.let { state ->
                            scope.launch { state.scroll(MutatePriority.UserInput) { } }
                        }
                        triggerLoadMoreRelease(-thresholdPx)
                    }
                    return if (orientation == ListOrientation.Vertical) Offset(0f, delta) else Offset(delta, 0f)
                }

                // Handle pull-down (refresh)
                if (delta > 0 && !isRefreshAnimating && !isLoadingMore) { // Scrolling down
                    val lazyListState = scrollableState as? LazyListState
                    val isAtTop = lazyListState?.run { firstVisibleItemIndex == 0 && firstVisibleItemScrollOffset == 0 } ?: true

                    if (isAtTop) {
                        val resistance = (pullRefreshOffsetPx / maxPullPx).coerceIn(0f, 1f).pow(2)
                        val newOffset = (pullRefreshOffsetPx + delta * (1f - resistance)).coerceAtMost(maxPullPx)
                        pullRefreshOffsetPx = newOffset
                        currentRefreshStatus = updateRefreshStatus(newOffset, thresholdPx)

                        if (source == NestedScrollSource.SideEffect && newOffset >= thresholdPx) {
                            scrollableState?.let { state ->
                                scope.launch { state.scroll(MutatePriority.UserInput) { } }
                            }
                            triggerRefreshRelease(thresholdPx)
                        }
                        return if (orientation == ListOrientation.Vertical) Offset(0f, delta) else Offset(delta, 0f)
                    }
                }

                return Offset.Zero
            }

            override suspend fun onPreFling(available: Velocity): Velocity {
                val anyPullActive = (abs(pullLoadMoreOffsetPx) > 0 && !isLoadMoreAnimating) ||
                        (abs(pullRefreshOffsetPx) > 0 && !isRefreshAnimating)

                if (anyPullActive) {
                    if (abs(pullLoadMoreOffsetPx) > 0 && !isLoadMoreAnimating) {
                        triggerLoadMoreRelease()
                    }
                    if (abs(pullRefreshOffsetPx) > 0 && !isRefreshAnimating) {
                        triggerRefreshRelease()
                    }
                    return Velocity.Zero // Consume the entire fling if any pull was active
                }
                return Velocity.Zero // No active pull, let the child (LazyColumn/Row) handle the fling
            }
        }
    }

    val itemOffsetModifier = if (orientation == ListOrientation.Vertical) {
        Modifier.offset(y = with(density) { displayOffset.toDp() })
    } else {
        Modifier.offset(x = with(density) { displayOffset.toDp() })
    }

    Box(modifier = modifier.nestedScroll(nestedScrollConnection).clipToBounds()) {
        Box(modifier = Modifier.fillMaxSize().then(itemOffsetModifier)) {

            // Custom Header for Pull-to-Refresh
            Box(
                modifier = if (orientation == ListOrientation.Vertical)
                    Modifier.align(Alignment.TopCenter).fillMaxWidth().height(pullThreshold).offset(y = -pullThreshold)
                else
                    Modifier.align(Alignment.CenterStart).fillMaxHeight().width(pullThreshold).offset(x = -pullThreshold),
                contentAlignment = Alignment.Center
            ) {
                if (currentRefreshStatus != RefreshStatus.Idle) refreshContent(currentRefreshStatus, progress)
            }

            content()

            // 外挂 Footer for Load More
            Box(
                modifier = if (orientation == ListOrientation.Vertical)
                    Modifier.align(Alignment.BottomCenter).fillMaxWidth().height(pullThreshold).offset(y = pullThreshold)
                else
                    Modifier.align(Alignment.CenterEnd).fillMaxHeight().width(pullThreshold).offset(x = pullThreshold),
                contentAlignment = Alignment.Center
            ) {
                if (showNoMoreData) noMoreDataContent()
                else if (currentLoadMoreStatus != LoadMoreStatus.Idle) loadMoreContent(currentLoadMoreStatus, progress)
            }
        }
    }
}

private fun updateLoadMoreStatus(offset: Float, threshold: Float): LoadMoreStatus {
    return when {
        offset == 0f -> LoadMoreStatus.Idle
        abs(offset) >= threshold -> LoadMoreStatus.CanRelease
        else -> LoadMoreStatus.Pulling
    }
}

private fun updateRefreshStatus(offset: Float, threshold: Float): RefreshStatus {
    return when {
        offset == 0f -> RefreshStatus.Idle
        offset >= threshold -> RefreshStatus.CanRelease
        else -> RefreshStatus.Pulling
    }
}

/**
 * 开箱即用的垂直列表实现
 */
@Composable
fun RefreshableList(
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    isLoadingMore: Boolean,
    onLoadMore: () -> Unit,
    hasMoreData: Boolean = true,
    modifier: Modifier = Modifier,
    content: LazyListScope.() -> Unit
) {
    val listState = rememberLazyListState()
    RefreshableWrapper(
        isRefreshing = isRefreshing,
        onRefresh = onRefresh,
        isLoadingMore = isLoadingMore,
        onLoadMore = onLoadMore,
        hasMoreData = hasMoreData,
        scrollableState = listState,
        modifier = modifier,
        orientation = ListOrientation.Vertical
    ) {
        LazyColumn(state = listState, modifier = Modifier.fillMaxSize()) {
            content()
        }
    }
}

@Composable
fun DefaultLoadMoreContent(status: LoadMoreStatus, progress: Float, orientation: ListOrientation) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        if (orientation == ListOrientation.Vertical) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (status == LoadMoreStatus.Loading) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    Spacer(Modifier.width(8.dp))
                    Text("正在加载...", style = MaterialTheme.typography.bodySmall)
                } else {
                    Text(
                        text = if (status == LoadMoreStatus.CanRelease) "松手即刻加载" else "继续上拉加载",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (status == LoadMoreStatus.CanRelease) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                    )
                }
            }
        } else {
            if (status == LoadMoreStatus.Loading) CircularProgressIndicator(modifier = Modifier.size(20.dp))
            else Text(if (status == LoadMoreStatus.CanRelease) "松手" else "拉", style = MaterialTheme.typography.labelSmall)
        }
    }
}

@Composable
fun DefaultNoMoreDataContent(orientation: ListOrientation) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("—— 到底啦 ——", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
    }
}

@Composable
fun DefaultRefreshContent(status: RefreshStatus, progress: Float, orientation: ListOrientation) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        if (orientation == ListOrientation.Vertical) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (status == RefreshStatus.Refreshing) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    Spacer(Modifier.width(8.dp))
                    Text("正在刷新...", style = MaterialTheme.typography.bodySmall)
                } else {
                    Text(
                        text = if (status == RefreshStatus.CanRelease) "松手即刻刷新" else "下拉刷新",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (status == RefreshStatus.CanRelease) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                    )
                }
            }
        } else {
            if (status == RefreshStatus.Refreshing) CircularProgressIndicator(modifier = Modifier.size(20.dp))
            else Text(if (status == RefreshStatus.CanRelease) "松手" else "拉", style = MaterialTheme.typography.labelSmall)
        }
    }
}