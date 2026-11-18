package com.example.antyprokrastynoinator_2000

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.antyprokrastynoinator_2000.ui.tasks.AddTaskScreen
import com.example.antyprokrastynoinator_2000.ui.theme.Antyprokrastynoinator_2000Theme
import com.example.antyprokrastynoinator_2000.ui.theme.IceBlue
import com.example.antyprokrastynoinator_2000.ui.theme.NavyPrimary
import com.example.antyprokrastynoinator_2000.viewmodel.CalendarViewModel

class TaskFormActivity : ComponentActivity() {

    companion object {
        const val EXTRA_TASK_ID = "extra_task_id"

        fun createIntent(context: Context, taskId: Int? = null): Intent {
            return Intent(context, TaskFormActivity::class.java).apply {
                if (taskId != null) {
                    putExtra(EXTRA_TASK_ID, taskId)
                }
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val editingTaskId = intent.getIntExtra(EXTRA_TASK_ID, -1)

        setContent {
            Antyprokrastynoinator_2000Theme {
                val context = LocalContext.current
                val database = remember { Database(context) }
                val calendarViewModel: CalendarViewModel = viewModel(
                    factory = CalendarViewModelFactory(database)
                )

                val taskList by calendarViewModel.taskList.collectAsState()
                val priorityList by calendarViewModel.priorityList.collectAsState()
                val taskToEdit = remember(taskList, editingTaskId) {
                    if (editingTaskId != -1) taskList.find { it.id == editingTaskId } else null
                }

                var errorMessage by remember { mutableStateOf<String?>(null) }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = IceBlue,
                    topBar = {
                        TopAppBar(
                            title = {
                                Text(
                                    text = if (editingTaskId == -1) "Nowe zadanie" else "Edytuj zadanie",
                                    fontWeight = FontWeight.Bold
                                )
                            },
                            navigationIcon = {
                                IconButton(onClick = { finish() }) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = "Wróć"
                                    )
                                }
                            },
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = NavyPrimary,
                                titleContentColor = IceBlue,
                                navigationIconContentColor = IceBlue
                            )
                        )
                    }
                ) { innerPadding ->
                    AddTaskScreen(
                        modifier = Modifier.padding(innerPadding),
                        priorities = priorityList,
                        taskToEdit = taskToEdit,
                        externalError = errorMessage,
                        onSaveTask = { savedTask ->
                            val success = if (taskToEdit != null) {
                                calendarViewModel.updateTask(savedTask)
                            } else {
                                calendarViewModel.addTask(savedTask)
                            }

                            if (success) {
                                setResult(RESULT_OK)
                                finish()
                            } else {
                                errorMessage = "Czas zadania pokrywa się z innym zadaniem o wyższym lub równym priorytecie!"
                            }
                        }
                    )
                }
            }
        }
    }
}