package com.example.test240402.presentation.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.forEachGesture
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Gamepad
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.changedToDown
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*
import kotlin.math.*

enum class PickerStage { SELECT_AM_PM, SELECT_TIME }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArcheryTimePicker(
    initialHour: Int? = null,
    initialMinute: Int? = null,
    initialIsAm: Boolean? = null,
    onTimeSelected: (Int, Int, Boolean) -> Unit
) {
    val calendar = remember { Calendar.getInstance() }
    var hour by remember { mutableIntStateOf(initialHour ?: calendar.get(Calendar.HOUR).let { if (it == 0) 12 else it }) }
    var minute by remember { mutableIntStateOf(initialMinute ?: calendar.get(Calendar.MINUTE)) }
    var isAm by remember { mutableStateOf(initialIsAm ?: (calendar.get(Calendar.AM_PM) == Calendar.AM)) }
    
    var stage by remember { mutableStateOf(PickerStage.SELECT_AM_PM) }
    var isDropdownMode by remember { mutableStateOf(false) } 

    val targetScale = remember { Animatable(1f) }
    val nextScale = remember { Animatable(1f) }
    
    var bowPosition by remember { mutableStateOf(Offset.Zero) }
    var dragOffset by remember { mutableStateOf(Offset.Zero) }
    var isDragging by remember { mutableStateOf(false) }
    
    var arrowPos by remember { mutableStateOf(Offset.Zero) }
    var isArrowFlying by remember { mutableStateOf(false) }
    var arrowVelocity by remember { mutableStateOf(Offset.Zero) }
    
    val stuckArrowsRelative = remember { mutableStateListOf<Offset>() }
    
    val scope = rememberCoroutineScope()
    val textMeasurer = rememberTextMeasurer()
    val textStyle = MaterialTheme.typography.bodyMedium.copy(color = Color.White)
    val numberStyle = MaterialTheme.typography.headlineSmall.copy(color = Color.White)

    Box(modifier = Modifier.fillMaxSize().background(if (isAm) Color(0xFF87CEEB) else Color(0xFF191970))) {
        
        if (stage == PickerStage.SELECT_AM_PM || (stage == PickerStage.SELECT_TIME && !isDropdownMode)) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(stage, isAm, isDropdownMode) {
                        forEachGesture {
                            awaitPointerEventScope {
                                val down = awaitFirstDown()
                                // 시스템 버튼 간섭을 피하기 위해 지면 높이를 더 올림
                                bowPosition = Offset(down.position.x, size.height - 500f)
                                isDragging = true
                                dragOffset = Offset.Zero
                                
                                val canvasSize = size
                                var pointerId = down.id
                                while (true) {
                                    val event = awaitPointerEvent()
                                    val anyChange = event.changes.find { it.id == pointerId }
                                    
                                    if (anyChange == null || anyChange.pressed.not()) {
                                        isDragging = false
                                        if (dragOffset.getDistance() > 50f) {
                                            isArrowFlying = true
                                            val finalOffset = dragOffset
                                            scope.launch {
                                                launchArrowWithPhysics(
                                                    startPos = bowPosition,
                                                    velocity = finalOffset * -0.38f,
                                                    onTick = { pos, vel -> 
                                                        if (!isArrowFlying) return@launchArrowWithPhysics
                                                        arrowPos = pos
                                                        arrowVelocity = vel
                                                        
                                                        checkCollisionAll(
                                                            pos, canvasSize.width.toFloat(), canvasSize.height.toFloat(),
                                                            stage, hour,
                                                            onAmPmHit = { targetCenter ->
                                                                isArrowFlying = false
                                                                isAm = !isAm
                                                                stuckArrowsRelative.add(pos - targetCenter)
                                                                scope.launch {
                                                                    targetScale.snapTo(1.6f)
                                                                    targetScale.animateTo(1f, spring(dampingRatio = Spring.DampingRatioHighBouncy))
                                                                }
                                                            },
                                                            onNextHit = {
                                                                isArrowFlying = false
                                                                scope.launch {
                                                                    nextScale.snapTo(1.5f)
                                                                    nextScale.animateTo(1f)
                                                                    delay(200)
                                                                    stage = PickerStage.SELECT_TIME
                                                                    isDropdownMode = true // 시간 설정은 드랍다운을 기본으로 하여 편리함 도모
                                                                    stuckArrowsRelative.clear()
                                                                }
                                                            },
                                                            onTimeHit = { h ->
                                                                if (h != null) {
                                                                    isArrowFlying = false
                                                                    hour = h
                                                                    scope.launch {
                                                                        targetScale.snapTo(1.3f)
                                                                        targetScale.animateTo(1f)
                                                                    }
                                                                }
                                                            }
                                                        )
                                                    },
                                                    onHit = { isArrowFlying = false }
                                                )
                                            }
                                        }
                                        dragOffset = Offset.Zero
                                        break
                                    } else {
                                        dragOffset = anyChange.position - bowPosition
                                        anyChange.consume()
                                    }
                                }
                            }
                        }
                    }
            ) {
                val centerX = size.width / 2
                val centerY = size.height / 2

                if (stage == PickerStage.SELECT_AM_PM) {
                    val targetPos = Offset(centerX - 150f, 800f)
                    withTransform({ scale(targetScale.value, targetScale.value, targetPos) }) {
                        drawCircle(color = if (isAm) Color.Yellow else Color.White, radius = 90f, center = targetPos)
                        stuckArrowsRelative.forEach { relPos ->
                            val absPos = targetPos + relPos
                            val angle = atan2(relPos.y, relPos.x)
                            drawLine(Color.Red, absPos, absPos + Offset(cos(angle)*60f, sin(angle)*60f), strokeWidth = 5f)
                        }
                    }
                    val nextPos = Offset(centerX + 150f, 800f)
                    withTransform({ scale(nextScale.value, nextScale.value, nextPos) }) {
                        drawCircle(Color.Green.copy(alpha = 0.8f), radius = 80f, center = nextPos)
                        drawText(textMeasurer, "NEXT", nextPos + Offset(-40f, -15f), style = textStyle)
                    }
                } else if (!isDropdownMode) {
                    withTransform({ scale(targetScale.value, targetScale.value, Offset(centerX, centerY)) }) {
                        drawCircle(Color.White.copy(alpha = 0.2f), radius = 320f, center = Offset(centerX, centerY), style = Stroke(width = 4f))
                        for (i in 1..12) {
                            val angle = (i * 30 - 90) * (PI / 180)
                            val x = centerX + 260f * cos(angle).toFloat()
                            val y = centerY + 260f * sin(angle).toFloat()
                            drawText(textMeasurer, i.toString(), Offset(x - 15f, y - 25f), style = numberStyle)
                        }
                    }
                }

                if (isDragging) {
                    val limitedOffset = dragOffset.coerceMaxLength(160f)
                    val stringPull = bowPosition + limitedOffset
                    val rotation = atan2(-limitedOffset.y, -limitedOffset.x)
                    withTransform({ rotate(degrees = (rotation * 180 / PI).toFloat() + 90f, pivot = bowPosition) }) {
                        drawArc(color = Color(0xFF8B4513), startAngle = 0f, sweepAngle = 180f, useCenter = false,
                            topLeft = Offset(bowPosition.x - 100f, bowPosition.y - 100f), size = Size(200f, 200f), style = Stroke(width = 12f))
                    }
                    val lEnd = Offset(bowPosition.x + 100f * cos(rotation - PI/2).toFloat(), bowPosition.y + 100f * sin(rotation - PI/2).toFloat())
                    val rEnd = Offset(bowPosition.x + 100f * cos(rotation + PI/2).toFloat(), bowPosition.y + 100f * sin(rotation + PI/2).toFloat())
                    drawLine(Color.White, lEnd, stringPull, strokeWidth = 3f); drawLine(Color.White, rEnd, stringPull, strokeWidth = 3f)
                    val tip = bowPosition + (limitedOffset * -1f).coerceMaxLength(130f)
                    drawLine(Color.Red, stringPull, tip, strokeWidth = 6f); drawCircle(Color.Red, radius = 6f, center = tip)
                }
                if (isArrowFlying) {
                    val flightAngle = atan2(arrowVelocity.y, arrowVelocity.x)
                    drawLine(Color.Red, arrowPos, arrowPos - Offset(cos(flightAngle)*60f, sin(flightAngle)*60f), strokeWidth = 6f); drawCircle(Color.Red, radius = 7f, center = arrowPos)
                }
            }
        }

        if (stage == PickerStage.SELECT_TIME && isDropdownMode) {
            Column(
                modifier = Modifier.fillMaxSize().padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text("시간과 분을 선택하세요", color = Color.White, style = MaterialTheme.typography.headlineMedium)
                Spacer(modifier = Modifier.height(32.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    TimeDropdown("시", hour, 1..12) { hour = it }
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(":", color = Color.White, fontSize = 32.sp)
                    Spacer(modifier = Modifier.width(16.dp))
                    TimeDropdown("분", minute, 0..59) { minute = it }
                }
            }
        }

        Column(modifier = Modifier.align(Alignment.TopCenter).padding(top = 60.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = String.format("%02d:%02d %s", hour, minute, if (isAm) "AM" else "PM"),
                fontSize = 54.sp, color = Color.White, style = MaterialTheme.typography.displayMedium)
            val hint = when {
                stage == PickerStage.SELECT_AM_PM -> "해/달을 맞춰 낮밤을 정하고, 'NEXT'를 쏘세요!"
                !isDropdownMode -> "시계 숫자를 맞춰 시간을 정하세요!"
                else -> "드랍다운으로 시간을 설정하세요."
            }
            Text(text = hint, color = Color.White.copy(alpha = 0.8f))
        }

        // 버튼 위치를 위로 올림 (bottom = 80.dp)
        Box(modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 80.dp).fillMaxWidth()) {
            if (stage == PickerStage.SELECT_TIME) {
                Row(modifier = Modifier.align(Alignment.Center), verticalAlignment = Alignment.CenterVertically) {
                    Button(
                        onClick = { isDropdownMode = !isDropdownMode },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.2f))
                    ) {
                        Icon(if(isDropdownMode) Icons.Default.Gamepad else Icons.Default.Edit, null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if(isDropdownMode) "활쏘기로 변경" else "드랍다운으로 변경")
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    FloatingActionButton(
                        onClick = { onTimeSelected(hour, minute, isAm) },
                        containerColor = Color(0xFF4CAF50)
                    ) { Icon(Icons.Default.Check, null, tint = Color.White) }
                }
            }
        }
    }
}

@Composable
fun TimeDropdown(label: String, currentValue: Int, range: IntRange, onValueChange: (Int) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        Surface(
            modifier = Modifier.width(100.dp).clickable { expanded = true },
            shape = MaterialTheme.shapes.medium, color = Color.White.copy(alpha = 0.2f)
        ) {
            Row(modifier = Modifier.padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(text = String.format("%02d", currentValue), color = Color.White, fontSize = 24.sp)
                Icon(Icons.Default.ArrowDropDown, null, tint = Color.White)
            }
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            range.forEach { value ->
                DropdownMenuItem(text = { Text(String.format("%02d %s", value, label)) }, onClick = { onValueChange(value); expanded = false })
            }
        }
    }
}

fun checkCollisionAll(pos: Offset, width: Float, height: Float, stage: PickerStage, currentHour: Int, onAmPmHit: (Offset) -> Unit, onNextHit: () -> Unit, onTimeHit: (Int?) -> Unit) {
    val centerX = width / 2
    if (stage == PickerStage.SELECT_AM_PM) {
        val targetPos = Offset(centerX - 150f, 800f)
        if ((pos - targetPos).getDistance() < 110f) { onAmPmHit(targetPos); return }
        val nextPos = Offset(centerX + 150f, 800f)
        if ((pos - nextPos).getDistance() < 100f) { onNextHit() }
    } else {
        val dx = pos.x - centerX
        val dy = pos.y - (height / 2)
        val dist = sqrt(dx*dx + dy*dy)
        if (dist in 180f..450f) {
            var angle = atan2(dy, dx) * (180 / PI) + 90
            if (angle < 0) angle += 360
            val h = ((angle / 30).roundToInt() % 12).let { if (it == 0) 12 else it }
            onTimeHit(h)
        }
    }
}

suspend fun launchArrowWithPhysics(startPos: Offset, velocity: Offset, onTick: (Offset, Offset) -> Unit, onHit: (Offset) -> Unit) {
    var currPos = startPos
    var currVel = velocity
    val grav = Offset(0f, 0.4f)
    for (t in 0..200) {
        currPos += currVel; currVel += grav; onTick(currPos, currVel)
        if (currPos.y < -1000 || currPos.y > 5000 || currPos.x < -1000 || currPos.x > 4000) break
        delay(16)
    }
    onHit(currPos)
}

suspend fun androidx.compose.ui.input.pointer.AwaitPointerEventScope.awaitFirstDown(): PointerInputChange {
    var event: androidx.compose.ui.input.pointer.PointerEvent
    do { event = awaitPointerEvent() } while (!event.changes.all { it.changedToDown() })
    return event.changes[0]
}

fun Offset.coerceMaxLength(max: Float): Offset {
    val l = getDistance(); return if (l > max) this * (max / l) else this
}
