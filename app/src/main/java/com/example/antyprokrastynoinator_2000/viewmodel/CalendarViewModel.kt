package com.example.antyprokrastynoinator_2000.viewmodel

import androidx.compose.runtime.simulateHotReload
import androidx.lifecycle.ViewModel
import com.example.antyprokrastynoinator_2000.data.TaskRepository
import com.example.antyprokrastynoinator_2000.model.Task
import com.example.antyprokrastynoinator_2000.Database
import com.example.antyprokrastynoinator_2000.model.Priority
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class CalendarViewModel(private val database: Database) : ViewModel() {

    private val _taskList: MutableStateFlow<List<Task>> = TaskRepository.tasks
    val taskList: StateFlow<List<Task>> = _taskList.asStateFlow()

    private val _priorityList = MutableStateFlow<List<Priority>>(emptyList())
    val priorityList: StateFlow<List<Priority>> = _priorityList.asStateFlow()

    init {
        loadTasks()
        loadPriorities()
    }

    private fun loadTasks() {
        _taskList.value = database.getTasks()
    }

    private fun loadPriorities() {
        _priorityList.value = database.getPriorities()
    }


    // Sprawdza, czy zadanie koliduje z innymi zadaniami.
    private fun hasCollision(task: Task): Boolean {
        return _taskList.value.any { existingTask ->
            val overlaps =
                task.id != existingTask.id &&
                        task.start.isBefore(existingTask.end) &&
                        existingTask.start.isBefore(task.end)

            val existingPriority = existingTask.priority.id
            val newPriority = task.priority.id

            overlaps && newPriority >= existingPriority
        }
    }

    // Dodawanie zadania.
    fun addTask(newTask: Task): Boolean {
        if (hasCollision(newTask)) {
            return false
        }

        database.addTask(newTask)
        loadTasks()
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

        database.updateTask(updatedTask)
        loadTasks()

        return true
    }

    // Usuwanie zadania.
    fun deleteTask(taskId: Int) {
        database.deleteTask(taskId)
        loadTasks()
    }
}