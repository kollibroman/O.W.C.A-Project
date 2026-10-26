package com.example.antyprokrastynoinator_2000

import androidx.compose.ui.graphics.Color

data class Task(
    val id: Int,
    val taskName: String,
    val description: String,
    val miniTasks: List<MiniTask>,
    val start: String,
    val end: String,
    val priority: Priority
)

data class Priority(
    val id: Int,
    val name: String,
    val color: Color,
    val alarmOn: Boolean = true
)

data class MiniTask(
    val id: Int,
    val name: String,
    val done: Boolean = false
)

data class PlacedTask(
    val task: Task,
    val startMin: Int,
    val durationMin: Int,
    var column: Int = 0,
    var totalColumns: Int = 1
)