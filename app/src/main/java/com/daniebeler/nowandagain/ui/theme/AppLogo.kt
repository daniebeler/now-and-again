package com.daniebeler.nowandagain.ui.theme

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

private const val BLOB =
    "M 340 70 C 410 62, 470 110, 478 185 C 486 262, 430 330, 345 334 " +
            "C 262 338, 200 290, 198 210 C 196 135, 262 78, 340 70 Z"
private const val BODY =
    "M 378 248 L 318 176 C 298 152 308 124 338 124 C 362 124 372 150 354 172 " +
            "C 334 196 286 222 290 246 C 294 272 336 276 366 238"
private const val TAIL = "M 378 248 C 398 268 436 256 432 216 C 430 200 424 190 410 180"
private const val HEAD = "M 419.9 166.2 L 400.1 193.8 L 394 168.4 Z"

@Composable
fun AppLogo(modifier: Modifier = Modifier, size: Dp = 36.dp) {
    val blobColor = MaterialTheme.colorScheme.onBackground
    val markColor = MaterialTheme.colorScheme.background

    val blob = remember { PathParser().parsePathString(BLOB).toPath() }
    val body = remember { PathParser().parsePathString(BODY).toPath() }
    val tail = remember { PathParser().parsePathString(TAIL).toPath() }
    val head = remember { PathParser().parsePathString(HEAD).toPath() }

    val line = Stroke(width = 22f, cap = StrokeCap.Round, join = StrokeJoin.Round)
    val headLine = Stroke(width = 8f, cap = StrokeCap.Round, join = StrokeJoin.Round)

    Canvas(modifier.size(size)) {
        // The blob spans roughly x 198..478 and y 62..338 in the original drawing.
        val scale = this.size.minDimension / 290f
        withTransform({
            scale(scale, scale, pivot = Offset.Zero)
            translate(left = -193f, top = -55f)
        }) {
            drawPath(blob, blobColor, style = Fill)
            withTransform({
                rotate(-6f, pivot = Offset(340f, 200f))
                translate(left = -20f, top = 0f)
            }) {
                drawPath(body, markColor, style = line)
                drawPath(tail, markColor, style = line)
                drawPath(head, markColor, style = Fill)
                drawPath(head, markColor, style = headLine)
            }
        }
    }
}