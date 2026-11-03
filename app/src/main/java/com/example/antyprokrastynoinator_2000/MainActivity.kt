package com.example.antyprokrastynoinator_2000

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.example.antyprokrastynoinator_2000.ui.calendar.MainScreenCalendar
import com.example.antyprokrastynoinator_2000.ui.tasks.AddTaskScreen
import com.example.antyprokrastynoinator_2000.ui.theme.Antyprokrastynoinator_2000Theme
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.antyprokrastynoinator_2000.viewmodel.CalendarViewModel
import com.example.antyprokrastynoinator_2000.model.Task
import com.example.antyprokrastynoinator_2000.ui.tasks.TaskDetailsScreen
class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            Antyprokrastynoinator_2000Theme {
                var showAddSheet by remember { mutableStateOf(false) }
                var selectedTask by remember { mutableStateOf<Task?>(null) }
                var editingTask by remember { mutableStateOf<Task?>(null) }
                val calendarViewModel: CalendarViewModel = viewModel()
                val taskList by calendarViewModel.taskList.collectAsState()

                val context = LocalContext.current


                Surface(
                    modifier = Modifier.fillMaxSize().systemBarsPadding(),
                    color = Color.DarkGray
                ) {
                    MainScreenCalendar(
                        taskList = taskList,
                        onAddClick = {
                            editingTask = null
                            showAddSheet = true
                        },
                        onTaskClick = { task -> selectedTask = task }
                    )

                    if (showAddSheet) {
                        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

                        ModalBottomSheet(
                            onDismissRequest = {
                                showAddSheet = false
                                editingTask = null
                            },
                            sheetState = sheetState,
                            containerColor = Color.White,
                            dragHandle = null
                        ) {
                            AddTaskScreen(
                                initialTask = editingTask,

                                onTaskSaved = { savedTask ->
                                    val success = if (editingTask == null) {
                                        calendarViewModel.addTask(savedTask)
                                    } else {
                                        calendarViewModel.updateTask(savedTask)
                                    }

                                    if (success) {
                                        showAddSheet = false
                                        editingTask = null
                                    } else {
                                        Toast.makeText(
                                            context,
                                            "Zadania kolidują ze sobą w tym czasie",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                },

                                onBackClick = {
                                    showAddSheet = false
                                    editingTask = null
                                }
                            )
                        }
                    }
                    if (selectedTask != null) {
                        val task = selectedTask!!

                        val sheetState = rememberModalBottomSheetState(
                            skipPartiallyExpanded = true
                        )

                        ModalBottomSheet(
                            onDismissRequest = {
                                selectedTask = null
                            },
                            sheetState = sheetState,
                            containerColor = Color.White,
                            dragHandle = null
                        ) {
                            TaskDetailsScreen(
                                task = task,
                                onEditClick = {
                                    editingTask = task
                                    selectedTask = null
                                    showAddSheet = true
                                },
                                onBackClick = {
                                    selectedTask = null
                                },
                                onDeleteClick = {
                                    calendarViewModel.deleteTask(task.id)
                                    selectedTask = null
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}