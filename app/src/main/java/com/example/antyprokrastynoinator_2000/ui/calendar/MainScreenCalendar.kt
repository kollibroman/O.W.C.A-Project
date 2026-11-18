package com.example.antyprokrastynoinator_2000.ui.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.example.antyprokrastynoinator_2000.model.PlacedTask
import com.example.antyprokrastynoinator_2000.model.Task
import com.example.antyprokrastynoinator_2000.ui.theme.ErrorRed
import com.example.antyprokrastynoinator_2000.ui.theme.IceBlue
import com.example.antyprokrastynoinator_2000.ui.theme.IceBlueLight
import com.example.antyprokrastynoinator_2000.ui.theme.NavyPrimary
import com.example.antyprokrastynoinator_2000.utils.assignTaskColumns
import com.example.antyprokrastynoinator_2000.utils.endMinuteOn
import com.example.antyprokrastynoinator_2000.utils.occursOn
import com.example.antyprokrastynoinator_2000.utils.startMinuteOn
import kotlinx.coroutines.delay
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun MainScreenCalendar(taskList: List<Task>, onAddClick: () -> Unit, onTaskClick: (Task) -> Unit) {
    val startHour = 0
    val hourHeight = 60.dp

    var currentDate by remember { mutableStateOf(LocalDate.now()) }
    var currentTime by remember { mutableStateOf(LocalTime.now()) }

    LaunchedEffect(Unit) {
        while (true) {
            currentTime = LocalTime.now()
            delay(60_000.milliseconds)
        }
    }
    val listState = rememberLazyListState()
    val density = LocalDensity.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(IceBlue)
            .padding(16.dp)
    ) {
        // NAGŁÓWEK APLIKACJI
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(NavyPrimary)
                .padding(14.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Prokrastynoinator2000",
                color = IceBlue,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        fun updateCurrentDate(inPlus: Boolean) {
            currentDate = if (inPlus) {
                currentDate.plusDays(1)
            } else {
                currentDate.minusDays(1)
            }
        }

        WeekSelector(
            selectedDate = currentDate,
            onDateSelected = { currentDate = it }
        )

        HorizontalDivider(thickness = 1.dp, color = IceBlue.copy(alpha = 0.25f))

        // PASEK WYBRANEGO DNIA
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(NavyPrimary),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { updateCurrentDate(false) }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    tint = IceBlue,
                    contentDescription = "Poprzedni dzień"
                )
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .padding(6.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Godziny",
                    color = IceBlue,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    maxLines = 1
                )
            }

            val dateFormat = DateTimeFormatter.ofPattern("dd.MM.yyyy")

            Text(
                text = currentDate.format(dateFormat),
                color = IceBlue,
                modifier = Modifier
                    .weight(3f)
                    .padding(8.dp),
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )

            IconButton(onClick = { updateCurrentDate(true) }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    tint = IceBlue,
                    contentDescription = "Następny dzień"
                )
            }
        }

        val sortedTasks = taskList.filter { it.occursOn(currentDate) }.map { task ->
            val startMin = task.startMinuteOn(currentDate) - startHour
            val duration = task.endMinuteOn(currentDate) - task.startMinuteOn(currentDate)
            PlacedTask(task, startMin, duration)
        }.sortedWith(compareBy({ it.startMin }, { -(it.startMin + it.durationMin) }))

        LaunchedEffect(currentDate, sortedTasks) {
            if (sortedTasks.isNotEmpty()) {
                val currentMinute = if (currentDate == LocalDate.now()) {
                    LocalTime.now().hour * 60 + LocalTime.now().minute
                } else {
                    0
                }

                val nextTask = sortedTasks.firstOrNull {
                    it.startMin + it.durationMin > currentMinute
                }

                val targetMinute = nextTask?.startMin ?: 0

                val scrollOffset = with(density) {
                    targetMinute.coerceAtLeast(0).dp.roundToPx()
                }

                listState.animateScrollToItem(
                    index = 0,
                    scrollOffset = scrollOffset
                )
            }
        }

        val tasksWithColumns = assignTaskColumns(sortedTasks)
        val gridLineColor = NavyPrimary.copy(alpha = 0.22f)

        // SIATKA KALENDARZA
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(bottomStart = 12.dp, bottomEnd = 12.dp))
                .background(IceBlueLight)
                .border(
                    width = 1.dp,
                    color = NavyPrimary.copy(alpha = 0.35f),
                    shape = RoundedCornerShape(bottomStart = 12.dp, bottomEnd = 12.dp)
                ),
            state = listState
        ) {
            item {
                Box(modifier = Modifier.fillMaxWidth()) {
                    Column {
                        for (hour in startHour..23) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(hourHeight)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxHeight()
                                        .border(0.5.dp, gridLineColor)
                                        .padding(5.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = String.format("%02d:00", hour),
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = NavyPrimary
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .weight(3f)
                                        .fillMaxHeight()
                                        .border(0.5.dp, gridLineColor)
                                )
                            }
                        }
                    }

                    Row(modifier = Modifier.fillMaxWidth()) {
                        Spacer(modifier = Modifier.weight(1f))

                        BoxWithConstraints(modifier = Modifier.weight(3f)) {
                            tasksWithColumns.forEach { pt ->
                                if (pt.startMin >= 0 && pt.durationMin > 0) {
                                    val blockWidth = maxWidth / pt.totalColumns
                                    val offsetX = blockWidth * pt.column

                                    Card(
                                        modifier = Modifier
                                            .width(blockWidth)
                                            .offset(x = offsetX, y = pt.startMin.dp)
                                            .height(pt.durationMin.dp)
                                            .padding(end = 4.dp, start = 2.dp)
                                            .clickable { onTaskClick(pt.task) },
                                        colors = CardDefaults.cardColors(containerColor = pt.task.priority.color)
                                    ) {
                                        Column(modifier = Modifier.padding(6.dp)) {
                                            Text(
                                                text = pt.task.taskName,
                                                color = Color.White,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            if (pt.task.description.isNotEmpty()) {
                                                Text(
                                                    text = pt.task.description,
                                                    color = IceBlueLight,
                                                    fontSize = 10.sp,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    if (currentDate == LocalDate.now()) {
                        val currentMinute = currentTime.hour * 60 + currentTime.minute

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .offset(y = (currentMinute.dp - 4.dp))
                                .zIndex(1f),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Spacer(modifier = Modifier.weight(1f))
                            Row(
                                modifier = Modifier.weight(3f),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(ErrorRed)
                                )
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(2.dp)
                                        .background(ErrorRed)
                                )
                            }
                        }
                    }
                }
            }
        }

        // PRZYCISK DODAJ
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp),
            contentAlignment = Alignment.CenterEnd
        ) {
            Button(
                onClick = onAddClick,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = NavyPrimary,
                    contentColor = IceBlue
                ),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Dodaj",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}