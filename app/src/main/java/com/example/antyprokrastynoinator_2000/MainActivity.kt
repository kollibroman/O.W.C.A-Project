package com.example.antyprokrastynoinator_2000

import android.Manifest
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import com.example.antyprokrastynoinator_2000.data.TaskRepository
import com.example.antyprokrastynoinator_2000.monitor.AppMonitorService
import com.example.antyprokrastynoinator_2000.monitor.MonitorPermissions
import com.example.antyprokrastynoinator_2000.ui.monitor.MonitoringPermissionsCard
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.antyprokrastynoinator_2000.ui.calendar.MainScreenCalendar
import com.example.antyprokrastynoinator_2000.ui.tasks.AddTaskScreen
import com.example.antyprokrastynoinator_2000.ui.theme.Antyprokrastynoinator_2000Theme
import com.example.antyprokrastynoinator_2000.viewmodel.CalendarViewModel
import com.example.antyprokrastynoinator_2000.model.Task
import com.example.antyprokrastynoinator_2000.ui.tasks.TaskDetailsScreen

class CalendarViewModelFactory(private val db: Database) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CalendarViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CalendarViewModel(db) as T
        }
        throw IllegalArgumentException("Nieznana klasa ViewModelu")
    }
}

class MainActivity : ComponentActivity() {
    private var permissionRefresh by mutableIntStateOf(0)

    override fun onResume() {
        super.onResume()
        permissionRefresh++
    }

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            Antyprokrastynoinator_2000Theme {
                val context = LocalContext.current
                val database = remember { Database(context) }

                val calendarViewModel: CalendarViewModel = viewModel(
                    factory = CalendarViewModelFactory(database)
                )

                var showAddSheet by remember { mutableStateOf(false) }
                var selectedTask by remember { mutableStateOf<Task?>(null) }
                var editingTask by remember { mutableStateOf<Task?>(null) }
                val taskList by calendarViewModel.taskList.collectAsState()
                val priorityList by calendarViewModel.priorityList.collectAsState()

                Surface(
                    modifier = Modifier.fillMaxSize().systemBarsPadding(),
                    color = Color.DarkGray
                ) {
                    val hasUsageAccess = remember(permissionRefresh) {
                        MonitorPermissions.hasUsageAccess(context)
                    }
                    val canDrawOverlays = remember(permissionRefresh) {
                        MonitorPermissions.canDrawOverlays(context)
                    }
                    val monitoringReady = hasUsageAccess && canDrawOverlays

                    val notificationPermissionLauncher = rememberLauncherForActivityResult(
                        ActivityResultContracts.RequestPermission()
                    ) { }
                    LaunchedEffect(Unit) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                            !MonitorPermissions.hasNotificationPermission(context)
                        ) {
                            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }
                    }

                    LaunchedEffect(taskList, monitoringReady) {
                        if (monitoringReady && TaskRepository.hasUpcomingOrActiveTask()) {
                            AppMonitorService.start(context)
                        }
                    }

                    Column(modifier = Modifier.fillMaxSize()) {
                        if (!monitoringReady) {
                            MonitoringPermissionsCard(
                                hasUsageAccess = hasUsageAccess,
                                canDrawOverlays = canDrawOverlays,
                                onGrantUsageAccess = {
                                    context.startActivity(MonitorPermissions.usageAccessSettingsIntent())
                                },
                                onGrantOverlay = {
                                    context.startActivity(MonitorPermissions.overlaySettingsIntent(context))
                                }
                            )
                        }
                        MainScreenCalendar(
                            taskList = taskList,
                            onAddClick = {
                                editingTask = null
                                showAddSheet = true
                            },
                            onTaskClick = { task -> selectedTask = task }
                        )
                    }

                    // BOTTOM SHEET: DODAWANIE / EDYCJA ZADANIA
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
                                priorities = priorityList,
                                taskToEdit = editingTask,
                                onDismissRequest = {
                                    showAddSheet = false
                                    editingTask = null
                                },
                                onSaveTask = { newTask ->
                                    // Sprawdzamy czy to edycja czy nowe zadanie
                                    val success = if (editingTask != null) {
                                        calendarViewModel.updateTask(newTask)
                                    } else {
                                        calendarViewModel.addTask(newTask)
                                    }

                                    if (success) {
                                        showAddSheet = false
                                        editingTask = null
                                    } else {
                                        // Wyświetlenie błędu o kolizji
                                        Toast.makeText(context, "Czas zadania pokrywa się z innym!", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            )
                        }
                    }

                    // BOTTOM SHEET: SZCZEGÓŁY ZADANIA
                    if (selectedTask != null) {
                        val task = selectedTask!!
                        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

                        ModalBottomSheet(
                            onDismissRequest = { selectedTask = null },
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
                                onBackClick = { selectedTask = null },
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