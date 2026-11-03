package com.example.antyprokrastynoinator_2000.ui.tasks

import android.R
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.view.Gravity
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.antyprokrastynoinator_2000.model.Priority
import com.example.antyprokrastynoinator_2000.model.Task
import com.example.antyprokrastynoinator_2000.utils.formatForTask
import java.time.LocalDateTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTaskScreen(
    initialTask: Task? = null,
    onTaskSaved: (Task) -> Unit,
    onBackClick: () -> Unit
) {
    var name by remember(initialTask) {
        mutableStateOf(initialTask?.taskName ?: "")
    }

    var description by remember(initialTask) {
        mutableStateOf(initialTask?.description ?: "")
    }

    var startDate by remember(initialTask) {
        mutableStateOf(initialTask?.start)
    }

    var endDate by remember(initialTask) {
        mutableStateOf(initialTask?.end)
    }

    val priorityOptions = listOf(
        Priority(id = 1, name = "bardzo ważne", color = Color(0xFF1B4D3E)),
        Priority(id = 2, name = "ważne", color = Color(0xFF008575)),
        Priority(id = 3, name = "mało ważne", color = Color(0xFF26A69A))
    )

    var isDropdownExpanded by remember { mutableStateOf(false) }

    var selectedPriority by remember(initialTask) {
        mutableStateOf(
            priorityOptions.find { it.id == initialTask?.priority?.id }
                ?: priorityOptions[1]
        )
    }

    val context = LocalContext.current
    val fieldColor = Color(0xFF008575)

    BackHandler {
        onBackClick()
    }

    fun selectDateTime(
        initial: LocalDateTime?,
        onSelected: (LocalDateTime) -> Unit
    ) {
        val base = initial ?: LocalDateTime.now()

        val dateDialog = DatePickerDialog(
            context,
            R.style.Theme_Material_Light_Dialog_Alert,
            { _, year, month, day ->
                val timeDialog = TimePickerDialog(
                    context,
                    R.style.Theme_Material_Light_Dialog_Alert,
                    { _, hour, minute ->
                        onSelected(
                            LocalDateTime.of(year, month + 1, day, hour, minute)
                        )
                    },
                    base.hour,
                    base.minute,
                    true
                )

                timeDialog.window?.setGravity(Gravity.CENTER)
                timeDialog.show()
            },
            base.year,
            base.monthValue - 1,
            base.dayOfMonth
        )

        dateDialog.window?.setGravity(Gravity.CENTER)
        dateDialog.show()
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, bottom = 16.dp, top = 0.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 15.dp, top = 15.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (initialTask == null) "Dodaj zadanie" else "Edytuj zadanie",
                fontSize = 28.sp,
                color = Color(0xFF008575)
            )

            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Zamknij",
                    tint = Color.Black
                )
            }
        }

        TextField(
            value = name,
            onValueChange = { name = it },
            placeholder = { Text("Nazwa") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            colors = TextFieldDefaults.colors(
                unfocusedContainerColor = fieldColor,
                focusedContainerColor = fieldColor
            )
        )

        TextField(
            value = description,
            onValueChange = { description = it },
            placeholder = { Text("Opis") },
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .padding(bottom = 16.dp),
            colors = TextFieldDefaults.colors(
                unfocusedContainerColor = fieldColor,
                focusedContainerColor = fieldColor
            )
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Button(
                onClick = {
                    selectDateTime(startDate) { newStart ->
                        startDate = newStart

                        val currentEnd = endDate
                        if (currentEnd == null || !currentEnd.isAfter(newStart)) {
                            endDate = newStart.plusHours(1)
                        }
                    }
                },
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = fieldColor),
                shape = MaterialTheme.shapes.extraSmall
            ) {
                Text(
                    text = startDate?.formatForTask() ?: "Wybierz datę rozpoczęcia",
                    color = Color.White,
                    fontSize = 12.sp
                )
            }

            Button(
                onClick = {
                    selectDateTime(endDate ?: startDate) {
                        endDate = it
                    }
                },
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = fieldColor),
                shape = MaterialTheme.shapes.extraSmall
            ) {
                Text(
                    text = endDate?.formatForTask() ?: "Wybierz datę zakończenia",
                    color = Color.White,
                    fontSize = 12.sp
                )
            }
        }

        ExposedDropdownMenuBox(
            expanded = isDropdownExpanded,
            onExpandedChange = {
                isDropdownExpanded = !isDropdownExpanded
            },
            modifier = Modifier.padding(bottom = 32.dp)
        ) {
            TextField(
                value = selectedPriority.name,
                onValueChange = {},
                readOnly = true,
                label = { Text("Priorytet") },
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(
                        expanded = isDropdownExpanded
                    )
                },
                colors = ExposedDropdownMenuDefaults.textFieldColors(
                    unfocusedContainerColor = fieldColor,
                    focusedContainerColor = fieldColor
                ),
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth(0.6f)
            )

            ExposedDropdownMenu(
                expanded = isDropdownExpanded,
                onDismissRequest = {
                    isDropdownExpanded = false
                }
            ) {
                priorityOptions.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option.name) },
                        onClick = {
                            selectedPriority = option
                            isDropdownExpanded = false
                        }
                    )
                }
            }
        }

        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.CenterEnd
        ) {
            Button(
                onClick = {
                    val start = startDate
                    val end = endDate

                    when {
                        name.isBlank() -> {
                            Toast.makeText(
                                context,
                                "Uzupełnij nazwę zadania!",
                                Toast.LENGTH_SHORT
                            ).show()
                        }

                        start == null || end == null -> {
                            Toast.makeText(
                                context,
                                "Wybierz przedział czasowy!",
                                Toast.LENGTH_SHORT
                            ).show()
                        }

                        !end.isAfter(start) -> {
                            Toast.makeText(
                                context,
                                "Data zakończenia musi być późniejsza niż data rozpoczęcia!",
                                Toast.LENGTH_SHORT
                            ).show()
                        }

                        else -> {
                            val savedTask = Task(
                                id = initialTask?.id
                                    ?: System.currentTimeMillis().toInt(),
                                taskName = name,
                                description = description,
                                miniTasks = initialTask?.miniTasks ?: emptyList(),
                                start = start,
                                end = end,
                                priority = selectedPriority
                            )

                            onTaskSaved(savedTask)
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = fieldColor),
                modifier = Modifier.width(120.dp)
            ) {
                Text(
                    text = if (initialTask == null) "Dodaj" else "Zapisz",
                    color = Color.White
                )
            }
        }
    }
}