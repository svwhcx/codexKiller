package com.svwh.tools.feature.home.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val HomeBackground = Color(0xFFF4F6F9)
private val HomePrimary = Color(0xFF1677FF)
private val HomeTitleColor = Color(0xFF1F2330)
private val HomeSubtitleColor = Color(0xFF8A93A4)
private val HomeArrowColor = Color(0xFFB8C0CC)

@Composable
fun HomeRoute(
    onOpenFridaScripts: () -> Unit,
) {
    HomeScreen(onOpenFridaScripts = onOpenFridaScripts)
}

@Composable
private fun HomeScreen(onOpenFridaScripts: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(HomeBackground)
            .statusBarsPadding()
            .padding(horizontal = 16.dp),
    ) {
        HomeTitle()
        Spacer(modifier = Modifier.height(16.dp))
        FridaScriptManagerCard(onClick = onOpenFridaScripts)
    }
}

@Composable
private fun HomeTitle() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 18.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .height(20.dp)
                .width(4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(HomePrimary),
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "首页",
            color = HomeTitleColor,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
private fun FridaScriptManagerCard(onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            DecorativeCircle(
                color = Color(0xFFE6F0FF),
                size = 110.dp,
                offsetX = 200.dp,
                offsetY = (-40).dp,
            )
            DecorativeCircle(
                color = Color(0xFFEEF4FF),
                size = 80.dp,
                offsetX = 250.dp,
                offsetY = 30.dp,
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 18.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Surface(
                    modifier = Modifier.size(44.dp),
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFFF1F5FB),
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Code,
                        contentDescription = null,
                        modifier = Modifier.padding(10.dp),
                        tint = HomePrimary,
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Frida 脚本管理",
                        color = HomeTitleColor,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "管理、配置和复用 Frida 脚本",
                        color = HomeSubtitleColor,
                        fontSize = 12.sp,
                    )
                }
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowRight,
                    contentDescription = null,
                    tint = HomeArrowColor,
                    modifier = Modifier.size(20.dp),
                )
            }
        }
    }
}

@Composable
private fun DecorativeCircle(
    color: Color,
    size: androidx.compose.ui.unit.Dp,
    offsetX: androidx.compose.ui.unit.Dp,
    offsetY: androidx.compose.ui.unit.Dp,
) {
    Box(
        modifier = Modifier
            .offset(x = offsetX, y = offsetY)
            .size(size)
            .clip(CircleShape)
            .background(
                Brush.radialGradient(
                    colors = listOf(color, Color.Transparent),
                ),
            ),
    )
}
