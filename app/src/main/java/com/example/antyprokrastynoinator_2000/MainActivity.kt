package com.example.antyprokrastynoinator_2000

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.example.antyprokrastynoinator_2000.ui.theme.Antyprokrastynoinator_2000Theme

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Antyprokrastynoinator_2000Theme {
                var showAddSheet by remember { mutableStateOf(false) }
                val taskList = remember { mutableStateListOf<Task>() }

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
                                    taskList.add(newTask)
                                    showAddSheet = false
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