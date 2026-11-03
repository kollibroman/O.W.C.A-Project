package com.example.antyprokrastynoinator_2000.ui.tasks

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.antyprokrastynoinator_2000.model.Task
import com.example.antyprokrastynoinator_2000.utils.formatForTask

@Composable
fun TaskDetailsScreen(
    task: Task,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onBackClick: () -> Unit
) {
    var showDeleteConfirmation by remember {
        mutableStateOf(false)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp)
    ) {
        Text(
            text = task.taskName,
            fontSize = 26.sp,
            color = Color(0xFF008575)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text("Opis:")
        Text(
            text = task.description.ifBlank { "Brak opisu" },
            fontSize = 16.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text("Termin:")
        Text("Od: ${task.start.formatForTask()}")
        Text("Do: ${task.end.formatForTask()}")

        Spacer(modifier = Modifier.height(16.dp))

        Text("Priorytet:")
        Text(task.priority.name)

        Spacer(modifier = Modifier.height(16.dp))

        Text("Podzadania:")

        if (task.miniTasks.isEmpty()) {
            Text("Brak podzadań")
        } else {
            task.miniTasks.forEach { miniTask ->
                Text(
                    text = "${if (miniTask.done) "✓" else "○"} ${miniTask.name}"
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            OutlinedButton(onClick = onBackClick) {
                Text("Wróć")
            }

            Button(onClick = onEditClick) {
                Text("Edytuj")
            }

            Button(
                onClick = { showDeleteConfirmation = true },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFB3261E)
                )
            ) {
                Text("Usuń")
            }
        }
    }

    if (showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = {
                showDeleteConfirmation = false
            },
            title = {
                Text("Usunąć zadanie?")
            },
            text = {
                Text("Czy na pewno chcesz usunąć zadanie \"${task.taskName}\"?")
            },
            confirmButton = {
                TextButton(onClick = onDeleteClick) {
                    Text("Usuń")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirmation = false
                    }
                ) {
                    Text("Anuluj")
                }
            }
        )
    }
}