package com.svwh.tools.feature.noenvironment.presentation.repack.progress

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.svwh.tools.feature.noenvironment.domain.model.RepackProgressState
import com.svwh.tools.feature.noenvironment.domain.model.RepackStepStatus
import com.svwh.tools.feature.noenvironment.domain.model.RepackTerminalOutcome
import com.svwh.tools.feature.noenvironment.presentation.components.RepackBlue
import com.svwh.tools.feature.noenvironment.presentation.components.RepackFailureRed

private val PanelMinHeight = 400.dp
private val PanelHeightFraction = 0.58f
private val PanelTopCornerRadius = 20.dp
private val ActionTextHorizontalPadding = 8.dp
private val ActionTextVerticalPadding = 4.dp
private val ActionRippleCornerRadius = 4.dp

/**
 * 固定在页面底部的进度面板：仅在 [RepackProgressState.visible] 首次为 true 时做进入动画，
 * 后续步骤更新只刷新内容，不会重复弹窗。
 */
@Composable
fun RepackProgressSheetHost(
    state: RepackProgressState,
    onDismiss: () -> Unit,
    onStop: () -> Unit,
    onInstall: () -> Unit,
    onDetails: () -> Unit,
    modifier: Modifier = Modifier,
) {
    BackHandler(enabled = state.visible && state.sessionFinished) {
        onDismiss()
    }

    Box(modifier = modifier.fillMaxSize()) {
        AnimatedVisibility(
            visible = state.visible,
            enter = fadeIn(),
            exit = fadeOut(),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.35f)),
            )
        }

        AnimatedVisibility(
            visible = state.visible,
            modifier = Modifier.align(Alignment.BottomCenter),
            enter = slideInVertically(initialOffsetY = { fullHeight -> fullHeight }),
            exit = slideOutVertically(targetOffsetY = { fullHeight -> fullHeight }),
        ) {
            RepackProgressPanel(
                state = state,
                onDismiss = onDismiss,
                onStop = onStop,
                onInstall = onInstall,
                onDetails = onDetails,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun RepackProgressPanel(
    state: RepackProgressState,
    onDismiss: () -> Unit,
    onStop: () -> Unit,
    onInstall: () -> Unit,
    onDetails: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val configuration = LocalConfiguration.current
    val panelHeight = heightInMin(
        min = PanelMinHeight,
        fractionOfScreen = PanelHeightFraction,
        screenHeightDp = configuration.screenHeightDp,
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(panelHeight)
            .navigationBarsPadding(),
        shape = RoundedCornerShape(
            topStart = PanelTopCornerRadius,
            topEnd = PanelTopCornerRadius,
        ),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 12.dp,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
                .padding(top = 12.dp, bottom = 20.dp),
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .width(40.dp)
                    .height(4.dp)
                    .background(
                        color = Color(0xFFD7DCE3),
                        shape = RoundedCornerShape(2.dp),
                    ),
            )

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = state.appName,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )

            Spacer(modifier = Modifier.height(10.dp))

            RepackCurrentStatusRow(
                state = state,
                onDismiss = onDismiss,
                onStop = onStop,
                onInstall = onInstall,
                onDetails = onDetails,
            )

            Spacer(modifier = Modifier.height(16.dp))

            HorizontalDivider(color = Color(0xFFE8ECF1))

            Spacer(modifier = Modifier.height(14.dp))

            if (state.steps.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "正在准备打包流程…",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            } else {
                RepackStepTimeline(
                    steps = state.steps,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .fillMaxHeight(),
                )
            }
        }
    }
}

@Composable
private fun RepackCurrentStatusRow(
    state: RepackProgressState,
    onDismiss: () -> Unit,
    onStop: () -> Unit,
    onInstall: () -> Unit,
    onDetails: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "当前状态：",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = currentStatusText(state),
            style = MaterialTheme.typography.bodyMedium,
            color = RepackBlue,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )

        RepackStatusActions(
            state = state,
            onDismiss = onDismiss,
            onStop = onStop,
            onInstall = onInstall,
            onDetails = onDetails,
        )
    }
}

@Composable
private fun RepackStatusActions(
    state: RepackProgressState,
    onDismiss: () -> Unit,
    onStop: () -> Unit,
    onInstall: () -> Unit,
    onDetails: () -> Unit,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (state.sessionFinished) {
            if (isRepackSuccess(state)) {
                RepackTextAction(
                    text = "安装",
                    color = RepackBlue,
                    onClick = onInstall,
                )
            } else {
                RepackTextAction(
                    text = "详情",
                    color = RepackBlue,
                    onClick = onDetails,
                )
            }
            RepackTextAction(
                text = "关闭",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                onClick = onDismiss,
            )
        } else {
            RepackTextAction(
                text = "停止",
                color = RepackFailureRed,
                onClick = onStop,
            )
        }
    }
}

@Composable
private fun RepackTextAction(
    text: String,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val rippleShape = RoundedCornerShape(ActionRippleCornerRadius)

    Box(
        modifier = modifier
            .clip(rippleShape)
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(
                    bounded = true,
                    color = color.copy(alpha = 0.18f),
                ),
                onClick = onClick,
            )
            .padding(
                horizontal = ActionTextHorizontalPadding,
                vertical = ActionTextVerticalPadding,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = color,
        )
    }
}

private fun isRepackSuccess(state: RepackProgressState): Boolean {
    if (!state.sessionFinished) return false
    val terminal = state.steps.lastOrNull { it.isTerminal }
    return terminal?.terminalOutcome == RepackTerminalOutcome.Success &&
        terminal.status == RepackStepStatus.Success
}

private fun currentStatusText(state: RepackProgressState): String {
    if (state.sessionFinished) {
        if (isRepackSuccess(state)) {
            return "打包完成"
        }
        val terminal = state.steps.lastOrNull { it.isTerminal }
        return when {
            terminal?.terminalOutcome == RepackTerminalOutcome.Failure ||
                terminal?.status == RepackStepStatus.Failed -> "打包失败"
            state.steps.any { it.status == RepackStepStatus.Failed } -> "已停止"
            else -> "打包失败"
        }
    }

    val running = state.steps.lastOrNull { it.status == RepackStepStatus.Running }
    return when {
        running != null -> "正在执行「${running.label}」"
        state.steps.isEmpty() -> "准备开始打包"
        else -> "等待下一步"
    }
}

private fun heightInMin(
    min: Dp,
    fractionOfScreen: Float,
    screenHeightDp: Int,
): Dp {
    val fractionHeight = (screenHeightDp * fractionOfScreen).dp
    return maxOf(min, fractionHeight)
}
