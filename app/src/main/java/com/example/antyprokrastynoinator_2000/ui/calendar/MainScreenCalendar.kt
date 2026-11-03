package com.example.antyprokrastynoinator_2000.ui.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.antyprokrastynoinator_2000.model.PlacedTask
import com.example.antyprokrastynoinator_2000.model.Task
import com.example.antyprokrastynoinator_2000.utils.endMinuteOn
import com.example.antyprokrastynoinator_2000.utils.occursOn
import com.example.antyprokrastynoinator_2000.utils.startMinuteOn
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun MainScreenCalendar(taskList: List<Task>, onAddClick: () -> Unit, onTaskClick: (Task) -> Unit) {
    val startHour = 0
    val hourHeight = 60.dp

    var currentDate by remember { mutableStateOf(LocalDate.now()) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Box(
            modifier = Modifier.fillMaxWidth().background(Color(0xFF008575)).padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Prokrastynoinator2000",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.height(16.dp))

        fun updateCurrentDate(inPlus: Boolean)
        {
            currentDate = if(inPlus) {
                currentDate.plusDays(1);
            } else{
                currentDate.minusDays(1);
            }
        }

        WeekSelector(
            selectedDate = currentDate,
            onDateSelected = { currentDate = it }
        )

        Row(modifier = Modifier.fillMaxWidth().background(Color(0xFF008575))) {
            IconButton(onClick = { updateCurrentDate(false) }){
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    tint = Color.White,
                    contentDescription = "Poprzedni dzień"
                )
            }

            Text(
                text = "Godziny",
                color = Color.White,
                modifier = Modifier.weight(1f).padding(5.dp),
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold,
                fontSize = TextUnit.Unspecified
            )

            val dateFormat = DateTimeFormatter.ofPattern("dd.MM.yyyy")

            Text(
                text = currentDate.format(dateFormat),
                color = Color.White,
                modifier = Modifier.weight(3f).padding(8.dp),
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold
            )

            IconButton(onClick = { updateCurrentDate(true) }){
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    tint = Color.White,
                    contentDescription = "Następny dzień"
                )
            }
        }

        val sortedTasks = taskList.filter { it.occursOn(currentDate) }.map { task ->
            val startMin = task.startMinuteOn(currentDate) - startHour * 60
            val duration = task.endMinuteOn(currentDate) - task.startMinuteOn(currentDate)
            PlacedTask(task, startMin, duration)
        }.sortedWith(compareBy({ it.startMin }, { -(it.startMin + it.durationMin) }))

        val clusters = mutableListOf<List<PlacedTask>>()
        var currentCluster = mutableListOf<PlacedTask>()
        var clusterEnd = -1

        sortedTasks.forEach { pt ->
            if (currentCluster.isEmpty()) {
                currentCluster.add(pt)
                clusterEnd = pt.startMin + pt.durationMin
            } else {
                if (pt.startMin < clusterEnd) {
                    currentCluster.add(pt)
                    clusterEnd = maxOf(clusterEnd, pt.startMin + pt.durationMin)
                } else {
                    clusters.add(currentCluster)
                    currentCluster = mutableListOf(pt)
                    clusterEnd = pt.startMin + pt.durationMin
                }
            }
        }
        if (currentCluster.isNotEmpty()) clusters.add(currentCluster)

        clusters.forEach { cluster ->
            val columns = mutableListOf<MutableList<PlacedTask>>()
            cluster.forEach { pt ->
                var placed = false
                for (i in columns.indices) {
                    if (columns[i].last().startMin + columns[i].last().durationMin <= pt.startMin) {
                        columns[i].add(pt)
                        pt.column = i
                        placed = true
                        break
                    }
                }
                if (!placed) {
                    columns.add(mutableListOf(pt))
                    pt.column = columns.size - 1
                }
            }
            val columnCount = columns.size
            cluster.forEach { it.totalColumns = columnCount }
        }

        LazyColumn(modifier = Modifier.weight(1f)) {
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
                                        .border(0.5.dp, Color.White)
                                        .padding(5.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = String.format("%02d:00", hour),
                                        fontSize = 14.sp,
                                        color = Color.White
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .weight(3f)
                                        .fillMaxHeight()
                                        .border(0.5.dp, Color.White)
                                )
                            }
                        }
                    }

                    Row(modifier = Modifier.fillMaxWidth()) {
                        Spacer(modifier = Modifier.weight(1f))

                        BoxWithConstraints(modifier = Modifier.weight(3f)) {
                            sortedTasks.forEach { pt ->
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
                                                    color = Color.LightGray,
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
                }
            }
        }

        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
            Button(
                onClick = onAddClick,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF008575)),
                modifier = Modifier.padding(top = 8.dp)
            ) {
                Text("Dodaj", color = Color.White)
            }
        }
    }
}