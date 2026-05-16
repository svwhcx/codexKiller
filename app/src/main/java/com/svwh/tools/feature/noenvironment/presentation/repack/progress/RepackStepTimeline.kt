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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.svwh.tools.feature.noenvironment.domain.model.RepackStep
import com.svwh.tools.feature.noenvironment.domain.model.RepackStepStatus
import com.svwh.tools.feature.noenvironment.domain.model.RepackTerminalOutcome
import com.svwh.tools.feature.noenvironment.presentation.components.RepackBlue
import com.svwh.tools.feature.noenvironment.presentation.components.RepackFailureRed
import com.svwh.tools.feature.noenvironment.presentation.components.RepackSuccessGreen

private val StepIndicatorSize = 24.dp
private val StepRowHeight = 44.dp
private val VerticalConnectorWidth = 2.dp

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
            .height(if (showConnectorBelow) StepRowHeight + 12.dp else StepRowHeight),
        verticalAlignment = Alignment.Top,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            RepackStepIndicator(step = step)
            if (showConnectorBelow) {
                Box(
                    modifier = Modifier
                        .padding(top = 4.dp)
                        .width(VerticalConnectorWidth)
                        .height(12.dp)
                        .background(connectorColor(step)),
                )
            }
        }

        Text(
            text = step.label,
            style = MaterialTheme.typography.bodyMedium,
            color = stepLabelColor(step),
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .padding(start = 14.dp, top = 2.dp)
                .weight(1f),
        )
    }
}

@Composable
private fun RepackStepIndicator(step: RepackStep) {
    when (step.status) {
        RepackStepStatus.Pending -> {
            Box(
                modifier = Modifier
                    .size(StepIndicatorSize)
                    .border(1.5.dp, Color(0xFFBFC7D1), CircleShape),
            )
        }
        RepackStepStatus.Running -> {
            CircularProgressIndicator(
                modifier = Modifier.size(StepIndicatorSize),
                strokeWidth = 2.5.dp,
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
                    modifier = Modifier.size(14.dp),
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
                    modifier = Modifier.size(14.dp),
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
                RepackSuccessGreen
            }
        }
        RepackStepStatus.Failed -> RepackFailureRed
        RepackStepStatus.Pending -> MaterialTheme.colorScheme.onSurfaceVariant
    }
}

private fun connectorColor(step: RepackStep): Color {
    return when (step.status) {
        RepackStepStatus.Success -> RepackSuccessGreen.copy(alpha = 0.55f)
        RepackStepStatus.Failed -> RepackFailureRed.copy(alpha = 0.55f)
        else -> Color(0xFFD7DCE3)
    }
}
