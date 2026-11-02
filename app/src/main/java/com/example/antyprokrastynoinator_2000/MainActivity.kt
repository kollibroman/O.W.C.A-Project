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
import com.example.antyprokrastynoinator_2000.ui.theme.Antyprokrastynoinator_2000Theme

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            Antyprokrastynoinator_2000Theme {
                var showAddSheet by remember { mutableStateOf(false) }
                val taskList = remember { mutableStateListOf<Task>() }

                val context = LocalContext.current

                fun validateAndSaveTask(newTask: Task): Boolean
                {
                    val collisionExists = taskList.any { x ->
                        newTask.start.isBefore(x.end) && x.start.isBefore(newTask.end)
                    }

                    if (collisionExists)
                    {
                        Toast.makeText(context, "Zadania kolidują ze sobą w ten dzień", Toast.LENGTH_SHORT).show()
                        return false
                    }

                    taskList.add(newTask)
                    return true
                }

                Surface(
                    modifier = Modifier.fillMaxSize().systemBarsPadding(),
                    color = Color.DarkGray
                ) {
                    MainScreenCalendar(
                        taskList = taskList,
                        onAddClick = { showAddSheet = true }
                    )

                    if (showAddSheet) {
                        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

                        ModalBottomSheet(
                            onDismissRequest = { showAddSheet = false },
                            sheetState = sheetState,
                            containerColor = Color.White,
                            dragHandle = null
                        ) {
                            AddTaskScreen(
                                onTaskAdded = { newTask ->
                                    if (validateAndSaveTask(newTask)) {
                                        showAddSheet = false
                                    }
                                },
                                onBackClick = { showAddSheet = false }
                            )
                        }
                    }
                }
            }
        }
    }
}