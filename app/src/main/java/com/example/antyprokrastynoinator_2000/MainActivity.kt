package com.example.antyprokrastynoinator_2000

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.antyprokrastynoinator_2000.data.TaskRepository
import com.example.antyprokrastynoinator_2000.model.Task
import com.example.antyprokrastynoinator_2000.monitor.AppMonitorService
import com.example.antyprokrastynoinator_2000.monitor.MonitorPermissions
import com.example.antyprokrastynoinator_2000.ui.calendar.MainScreenCalendar
import com.example.antyprokrastynoinator_2000.ui.monitor.MonitoringPermissionsCard
import com.example.antyprokrastynoinator_2000.ui.tasks.TaskDetailsScreen
import com.example.antyprokrastynoinator_2000.ui.theme.Antyprokrastynoinator_2000Theme
import com.example.antyprokrastynoinator_2000.ui.theme.IceBlue
import com.example.antyprokrastynoinator_2000.viewmodel.CalendarViewModel

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

                var selectedTask by remember { mutableStateOf<Task?>(null) }
                val taskList by calendarViewModel.taskList.collectAsState()

                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .systemBarsPadding(),
                    color = IceBlue
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
                                context.startActivity(TaskFormActivity.createIntent(context))
                            },
                            onTaskClick = { task -> selectedTask = task }
                        )
                    }

                    if (selectedTask != null) {
                        val task = selectedTask!!
                        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

                        ModalBottomSheet(
                            onDismissRequest = { selectedTask = null },
                            sheetState = sheetState,
                            containerColor = IceBlue,
                            dragHandle = null
                        ) {
                            TaskDetailsScreen(
                                task = task,
                                onEditClick = {
                                    selectedTask = null
                                    context.startActivity(TaskFormActivity.createIntent(context, task.id))
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