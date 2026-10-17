package com.example.easycom.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.random.Random

@Composable
fun BarcodeVisual(
    eanCode: String,
    modifier: Modifier = Modifier,
    height: Int = 54,
    showText: Boolean = true
) {
    // Generate deterministic bar widths based on EAN digits
    val pattern = remember(eanCode) {
        val seed = eanCode.hashCode().toLong()
        val rng = Random(seed)
        val list = mutableListOf<Float>()
        // Guard pattern
        list.add(1.5f); list.add(1.0f); list.add(1.5f)
        for (ch in eanCode) {
            val d = ch.digitToIntOrNull() ?: 3
            val b1 = (d % 3 + 1).toFloat()
            val s1 = (rng.nextInt(2) + 1).toFloat()
            val b2 = ((d / 2) % 3 + 1).toFloat()
            val s2 = (rng.nextInt(2) + 1).toFloat()
            list.add(b1); list.add(s1); list.add(b2); list.add(s2)
        }
        list.add(1.5f); list.add(1.0f); list.add(1.5f)
        list
    }

    Column(
        modifier = modifier
            .background(Color.White, RoundedCornerShape(6.dp))
            .border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 6.dp)
            .testTag("barcode_visual_$eanCode"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(height.dp)
        ) {
            val totalUnits = pattern.sum()
            val unitWidth = size.width / totalUnits
            var currentX = 0f

            for (i in pattern.indices) {
                val barW = pattern[i] * unitWidth
                if (i % 2 == 0) { // Black bar
                    drawRect(
                        color = Color.Black,
                        topLeft = Offset(currentX, 0f),
                        size = Size(barW, size.height)
                    )
                }
                currentX += barW
            }
        }

        if (showText) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = formatEanDisplay(eanCode),
                fontFamily = FontFamily.Monospace,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                letterSpacing = 1.5.sp
            )
        }
    }
}

private fun formatEanDisplay(code: String): String {
    return if (code.length == 13) {
        "${code.substring(0, 1)} ${code.substring(1, 7)} ${code.substring(7, 13)}"
    } else {
        code
    }
}
