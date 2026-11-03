package com.example.antyprokrastynoinator_2000.model

import java.time.LocalDateTime

data class Task(
    val id: Int,
    val taskName: String,
    val description: String,
    val miniTasks: List<MiniTask>,
    val start: LocalDateTime,
    val end: LocalDateTime,
    val priority: Priority
)