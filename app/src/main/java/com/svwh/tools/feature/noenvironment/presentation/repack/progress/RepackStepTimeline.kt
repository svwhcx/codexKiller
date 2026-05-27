package com.svwh.tools.feature.noenvironment.presentation.repack.progress

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.svwh.tools.feature.noenvironment.domain.model.RepackStep
import com.svwh.tools.feature.noenvironment.domain.model.RepackStepStatus
import com.svwh.tools.feature.noenvironment.domain.model.RepackTerminalOutcome
import com.svwh.tools.feature.noenvironment.presentation.components.RepackBlue
import com.svwh.tools.feature.noenvironment.presentation.components.RepackFailureRed
import com.svwh.tools.feature.noenvironment.presentation.components.RepackSuccessGreen
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val StepIndicatorSize = 16.dp
private val StepIconSize = 10.dp
private val StepRowHeight = 56.dp
private val VerticalConnectorWidth = 1.dp
private val TimelineText = Color(0xFF2F3A4A)
private val TimelineTime = Color(0xFF8A96A8)
private val TimelineConnector = Color(0xFFDCE4EF)

@Composable
fun RepackStepTimeline(
    steps: List<RepackStep>,
    modifier: Modifier = Modifier,
) {
    val listState = rememberLazyListState()

    LaunchedEffect(steps.size) {
        if (steps.isNotEmpty()) {
            listState.animateScrollToItem(steps.lastIndex)
        }
    }

    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        state = listState,
        verticalArrangement = Arrangement.spacedBy(0.dp),
    ) {
        itemsIndexed(
            items = steps,
            key = { _, step -> step.id },
        ) { index, step ->
            RepackStepTimelineRow(
                step = step,
                showConnectorBelow = index < steps.lastIndex,
            )
        }
    }
}

@Composable
private fun RepackStepTimelineRow(
    step: RepackStep,
    showConnectorBelow: Boolean,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(StepRowHeight),
        verticalAlignment = Alignment.Top,
    ) {
        Column(
            modifier = Modifier.width(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            RepackStepIndicator(step = step)
            if (showConnectorBelow) {
                Box(
                    modifier = Modifier
                        .padding(top = 4.dp)
                        .width(VerticalConnectorWidth)
                        .height(32.dp)
                        .background(connectorColor(step)),
                )
            }
        }

        Column(
            modifier = Modifier
                .padding(start = 10.dp)
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = step.label,
                style = MaterialTheme.typography.bodyMedium,
                color = stepLabelColor(step),
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = formatStepTime(step.timestampMillis),
                style = MaterialTheme.typography.bodySmall,
                color = TimelineTime,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun RepackStepIndicator(step: RepackStep) {
    when (step.status) {
        RepackStepStatus.Pending -> {
            Box(
                modifier = Modifier
                    .size(StepIndicatorSize)
                    .border(1.dp, TimelineConnector, CircleShape),
            )
        }
        RepackStepStatus.Running -> {
            CircularProgressIndicator(
                modifier = Modifier.size(StepIndicatorSize),
                strokeWidth = 2.dp,
                color = RepackBlue,
            )
        }
        RepackStepStatus.Success -> {
            Box(
                modifier = Modifier
                    .size(StepIndicatorSize)
                    .clip(CircleShape)
                    .background(RepackSuccessGreen),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = null,
                    modifier = Modifier.size(StepIconSize),
                    tint = Color.White,
                )
            }
        }
        RepackStepStatus.Failed -> {
            Box(
                modifier = Modifier
                    .size(StepIndicatorSize)
                    .clip(CircleShape)
                    .background(RepackFailureRed),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = null,
                    modifier = Modifier.size(StepIconSize),
                    tint = Color.White,
                )
            }
        }
    }
}

@Composable
private fun stepLabelColor(step: RepackStep): Color {
    return when (step.status) {
        RepackStepStatus.Running -> RepackBlue
        RepackStepStatus.Success -> {
            if (step.terminalOutcome == RepackTerminalOutcome.Failure) {
                RepackFailureRed
            } else {
                TimelineText
            }
        }
        RepackStepStatus.Failed -> RepackFailureRed
        RepackStepStatus.Pending -> MaterialTheme.colorScheme.onSurfaceVariant
    }
}

private fun connectorColor(step: RepackStep): Color {
    return when (step.status) {
        RepackStepStatus.Success -> RepackSuccessGreen.copy(alpha = 0.45f)
        RepackStepStatus.Failed -> RepackFailureRed.copy(alpha = 0.45f)
        else -> TimelineConnector
    }
}

private fun formatStepTime(timestampMillis: Long): String {
    return SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(timestampMillis))
}
