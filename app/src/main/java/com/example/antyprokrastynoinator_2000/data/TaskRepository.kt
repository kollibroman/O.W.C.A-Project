package com.example.antyprokrastynoinator_2000.data

import com.example.antyprokrastynoinator_2000.model.Task
import kotlinx.coroutines.flow.MutableStateFlow
import java.time.LocalDateTime

object TaskRepository {
    val tasks = MutableStateFlow<List<Task>>(emptyList())

    fun activeTask(now: LocalDateTime = LocalDateTime.now()): Task? =
        tasks.value.firstOrNull { !now.isBefore(it.start) && now.isBefore(it.end) }

    fun hasUpcomingOrActiveTask(now: LocalDateTime = LocalDateTime.now()): Boolean =
        tasks.value.any { it.end.isAfter(now) }
}
