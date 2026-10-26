package com.example.antyprokrastynoinator_2000

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTaskScreen(onTaskAdded: (Task) -> Unit, onBackClick: () -> Unit) {
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var startDate by remember { mutableStateOf("Wybierz datę rozpoczęcia") }
    var endDate by remember { mutableStateOf("Wybierz datę zakończenia") }

    val priorityOptions = listOf(
        Priority(id = 1, name = "bardzo ważne", color = Color(0xFF1B4D3E)),
        Priority(id = 2, name = "ważne", color = Color(0xFF008575)),
        Priority(id = 3, name = "mało ważne", color = Color(0xFF26A69A))
    )
    var isDropdownExpanded by remember { mutableStateOf(false) }
    var selectedPriority by remember { mutableStateOf(priorityOptions[1]) }

    val context = LocalContext.current
    val calendar = Calendar.getInstance()
    val fieldColor = Color(0xFF008575)

    BackHandler {
        onBackClick()
    }

    fun selectDateTime(onSelected: (String) -> Unit) {
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val dateDialog = DatePickerDialog(context, android.R.style.Theme_Material_Light_Dialog_Alert, { _, y, m, d ->
            val hour = calendar.get(Calendar.HOUR_OF_DAY)
            val minute = calendar.get(Calendar.MINUTE)

            val timeDialog = TimePickerDialog(context, android.R.style.Theme_Material_Light_Dialog_Alert, { _, h, min ->
                val result = String.format("%02d.%02d.%d %02d:%02d", d, m + 1, y, h, min)
                onSelected(result)
            }, hour, minute, true)

            timeDialog.window?.setGravity(Gravity.CENTER)
            timeDialog.show()

        }, year, month, day)

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
                text = "Dodaj zadanie",
                fontSize = 28.sp,
                fontWeight = FontWeight.Normal,
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
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            colors = TextFieldDefaults.colors(
                unfocusedContainerColor = fieldColor,
                focusedContainerColor = fieldColor
            )
        )

        TextField(
            value = description,
            onValueChange = { description = it },
            placeholder = { Text("Opis") },
            modifier = Modifier.fillMaxWidth().height(180.dp).padding(bottom = 16.dp),
            colors = TextFieldDefaults.colors(
                unfocusedContainerColor = fieldColor,
                focusedContainerColor = fieldColor
            )
        )

        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Button(
                onClick = { selectDateTime { startDate = it } },
                modifier = Modifier.weight(1f).height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = fieldColor),
                shape = MaterialTheme.shapes.extraSmall
            ) {
                Text(text = startDate, color = Color.White, fontSize = 12.sp)
            }

            Button(
                onClick = { selectDateTime { endDate = it } },
                modifier = Modifier.weight(1f).height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = fieldColor),
                shape = MaterialTheme.shapes.extraSmall
            ) {
                Text(text = endDate, color = Color.White, fontSize = 12.sp)
            }
        }

        ExposedDropdownMenuBox(
            expanded = isDropdownExpanded,
            onExpandedChange = { isDropdownExpanded = !isDropdownExpanded },
            modifier = Modifier.padding(bottom = 32.dp)
        ) {
            TextField(
                value = selectedPriority.name,
                onValueChange = {},
                readOnly = true,
                label = { Text("Priorytet") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isDropdownExpanded) },
                colors = ExposedDropdownMenuDefaults.textFieldColors(
                    unfocusedContainerColor = fieldColor,
                    focusedContainerColor = fieldColor
                ),
                modifier = Modifier.menuAnchor().fillMaxWidth(0.6f)
            )

            ExposedDropdownMenu(
                expanded = isDropdownExpanded,
                onDismissRequest = { isDropdownExpanded = false }
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
                    when {
                        name.isBlank() -> {
                            Toast.makeText(context, "Uzupełnij nazwę zadania!", Toast.LENGTH_SHORT).show()
                        }
                        startDate == "Wybierz datę rozpoczęcia" || endDate == "Wybierz datę zakończenia" -> {
                            Toast.makeText(context, "Wybierz przedział czasowy!", Toast.LENGTH_SHORT).show()
                        }
                        else -> {
                            val newTask = Task(
                                id = System.currentTimeMillis().toInt(),
                                taskName = name,
                                description = description,
                                miniTasks = emptyList(),
                                start = startDate,
                                end = endDate,
                                priority = selectedPriority,
                            )
                            onTaskAdded(newTask)
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = fieldColor),
                modifier = Modifier.width(120.dp)
            ) {
                Text("Dodaj", color = Color.White)
            }
        }
    }
}