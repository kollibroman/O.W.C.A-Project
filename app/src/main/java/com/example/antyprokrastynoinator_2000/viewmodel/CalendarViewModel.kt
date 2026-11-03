package com.example.antyprokrastynoinator_2000.viewmodel

import androidx.lifecycle.ViewModel
import com.example.antyprokrastynoinator_2000.model.Task
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class CalendarViewModel : ViewModel() {

    private val _taskList = MutableStateFlow<List<Task>>(emptyList())
    val taskList: StateFlow<List<Task>> = _taskList.asStateFlow()

    // Sprawdza, czy zadanie koliduje z innymi zadaniami.
    private fun hasCollision(task: Task): Boolean {
        return _taskList.value.any { existingTask ->
            existingTask.id != task.id &&
                    task.start.isBefore(existingTask.end) &&
                    existingTask.start.isBefore(task.end)
        }
    }

    // Dodawanie zadania.
    fun addTask(newTask: Task): Boolean {
        if (hasCollision(newTask)) {
            return false
        }

        _taskList.value = _taskList.value + newTask
        return true
    }

    // Edycja zadania.
    fun updateTask(updatedTask: Task): Boolean {
        val taskExists = _taskList.value.any {
            it.id == updatedTask.id
        }

        if (!taskExists || hasCollision(updatedTask)) {
            return false
        }

        _taskList.value = _taskList.value.map { task ->
            if (task.id == updatedTask.id) updatedTask else task
        }

        return true
    }

    // Usuwanie zadania.
    fun deleteTask(taskId: Int) {
        _taskList.value = _taskList.value.filterNot {
            it.id == taskId
        }
    }
}