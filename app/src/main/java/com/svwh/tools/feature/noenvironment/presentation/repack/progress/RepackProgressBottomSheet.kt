package com.svwh.tools.feature.noenvironment.presentation.repack.progress

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import com.svwh.tools.feature.noenvironment.domain.model.RepackProgressState
import com.svwh.tools.feature.noenvironment.domain.model.RepackStepStatus
import com.svwh.tools.feature.noenvironment.domain.model.RepackTerminalOutcome
import com.svwh.tools.feature.noenvironment.presentation.components.RepackBlue
import com.svwh.tools.feature.noenvironment.presentation.components.RepackFailureRed

private val PanelMinHeight = 400.dp
private val PanelHeightFraction = 0.58f
private val PanelTopCornerRadius = 18.dp
private val ActionTextHorizontalPadding = 8.dp
private val ActionTextVerticalPadding = 4.dp
private val ActionRippleCornerRadius = 4.dp
private val PanelBackgroundTop = Color(0xFFF8FBFF)
private val PanelBackgroundMiddle = Color(0xFFF1F7FF)
private val PanelBackgroundBottom = Color(0xFFFFFFFF)
private val HeaderDivider = Color(0xFFE9EEF7)
private val AppIconSurface = Color(0xFFFFFFFF)
private val AppIconFallbackStart = Color(0xFFFF8A00)
private val AppIconFallbackEnd = Color(0xFF1677FF)
private val StatusTextMuted = Color(0xFF7A8598)
private val TitleText = Color(0xFF14213A)

@Composable
fun RepackProgressSheetHost(
    state: RepackProgressState,
    onDismiss: () -> Unit,
    onStop: () -> Unit,
    onInstall: () -> Unit,
    onDetails: () -> Unit,
    modifier: Modifier = Modifier,
) {
    BackHandler(enabled = state.visible) {
        if (state.sessionFinished) {
            onDismiss()
        }
    }
    val scrimInteractionSource = remember { MutableInteractionSource() }

    Box(modifier = modifier.fillMaxSize()) {
        AnimatedVisibility(
            visible = state.visible,
            enter = fadeIn(),
            exit = fadeOut(),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.28f))
                    .clickable(
                        interactionSource = scrimInteractionSource,
                        indication = null,
                        onClick = {},
                    ),
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
        color = Color.Transparent,
        shadowElevation = 12.dp,
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colorStops = arrayOf(
                            0.00f to PanelBackgroundTop,
                            0.40f to PanelBackgroundMiddle,
                            1.00f to PanelBackgroundBottom,
                        ),
                    ),
                ),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
                    .padding(top = 18.dp, bottom = 18.dp),
            ) {
                RepackProgressHeader(
                    state = state,
                    onDismiss = onDismiss,
                    onStop = onStop,
                    onInstall = onInstall,
                    onDetails = onDetails,
                )

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = HeaderDivider, thickness = 0.7.dp)
                Spacer(modifier = Modifier.height(18.dp))

                if (state.steps.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.TopStart,
                    ) {
                        Text(
                            text = "正在准备打包流程...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = StatusTextMuted,
                        )
                    }
                } else {
                    RepackStepTimeline(
                        steps = state.steps,
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                    )
                }
            }
        }
    }
}

@Composable
private fun RepackProgressHeader(
    state: RepackProgressState,
    onDismiss: () -> Unit,
    onStop: () -> Unit,
    onInstall: () -> Unit,
    onDetails: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top,
    ) {
        RepackAppMark(state = state)
        Spacer(modifier = Modifier.width(10.dp))
        Column(
            modifier = Modifier.weight(1f),
        ) {
            Text(
                text = state.appName.ifBlank { state.packageName },
                style = MaterialTheme.typography.titleSmall,
                color = TitleText,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "当前状态：",
                    style = MaterialTheme.typography.bodySmall,
                    color = StatusTextMuted,
                )
                Text(
                    text = currentStatusText(state),
                    style = MaterialTheme.typography.bodySmall,
                    color = currentStatusColor(state),
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }

        RepackStatusActions(
            state = state,
            onDismiss = onDismiss,
            onStop = onStop,
            onInstall = onInstall,
            onDetails = onDetails,
            modifier = Modifier.padding(top = 8.dp),
        )
    }
}

@Composable
private fun RepackAppMark(state: RepackProgressState) {
    val icon = remember(state.packageName, state.appIcon) {
        state.appIcon?.toBitmap(width = 72, height = 72)?.asImageBitmap()
    }
    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(AppIconSurface),
        contentAlignment = Alignment.Center,
    ) {
        if (icon != null) {
            Image(
                bitmap = icon,
                contentDescription = state.appName,
                modifier = Modifier
                    .size(30.dp)
                    .clip(RoundedCornerShape(7.dp)),
            )
        } else {
            Box(
                modifier = Modifier
                    .size(22.dp)
                    .clip(RoundedCornerShape(5.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(AppIconFallbackStart, AppIconFallbackEnd),
                        ),
                    ),
            )
        }
    }
}

@Composable
private fun RepackStatusActions(
    state: RepackProgressState,
    onDismiss: () -> Unit,
    onStop: () -> Unit,
    onInstall: () -> Unit,
    onDetails: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
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
                terminal?.status == RepackStepStatus.Failed -> "失败"
            state.steps.any { it.status == RepackStepStatus.Failed } -> "已停止"
            else -> "失败"
        }
    }

    val running = state.steps.lastOrNull { it.status == RepackStepStatus.Running }
    return when {
        running != null -> "执行中"
        state.steps.isEmpty() -> "准备开始"
        else -> "执行中"
    }
}

private fun currentStatusColor(state: RepackProgressState): Color {
    return when {
        state.steps.any { it.status == RepackStepStatus.Failed } -> RepackFailureRed
        state.sessionFinished && isRepackSuccess(state) -> RepackBlue
        else -> RepackBlue
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
