package com.example.antyprokrastynoinator_2000.ui.tasks

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.antyprokrastynoinator_2000.model.Task
import com.example.antyprokrastynoinator_2000.ui.theme.ErrorRed
import com.example.antyprokrastynoinator_2000.ui.theme.IceBlue
import com.example.antyprokrastynoinator_2000.ui.theme.IceBlueLight
import com.example.antyprokrastynoinator_2000.ui.theme.NavyPrimary
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
            .background(IceBlue)
            .padding(20.dp)
    ) {
        Text(
            text = task.taskName,
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = NavyPrimary
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text("Opis:", fontWeight = FontWeight.Bold, color = NavyPrimary)
        Text(
            text = task.description.ifBlank { "Brak opisu" },
            fontSize = 16.sp,
            color = NavyPrimary
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text("Termin:", fontWeight = FontWeight.Bold, color = NavyPrimary)
        Text("Od: ${task.start.formatForTask()}", color = NavyPrimary)
        Text("Do: ${task.end.formatForTask()}", color = NavyPrimary)

        Spacer(modifier = Modifier.height(16.dp))

        Text("Priorytet:", fontWeight = FontWeight.Bold, color = NavyPrimary)
        Text(task.priority.name, color = NavyPrimary)

        Spacer(modifier = Modifier.height(16.dp))

        Text("Podzadania:", fontWeight = FontWeight.Bold, color = NavyPrimary)

        if (task.miniTasks.isEmpty()) {
            Text("Brak podzadań", color = NavyPrimary)
        } else {
            task.miniTasks.forEach { miniTask ->
                Text(
                    text = "${if (miniTask.done) "✓" else "○"} ${miniTask.name}",
                    color = NavyPrimary
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            OutlinedButton(
                onClick = onBackClick,
                border = BorderStroke(1.5.dp, NavyPrimary),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = IceBlueLight,
                    contentColor = NavyPrimary
                )
            ) {
                Text("Wróć", fontWeight = FontWeight.Bold)
            }

            Button(
                onClick = onEditClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = NavyPrimary,
                    contentColor = IceBlue
                )
            ) {
                Text("Edytuj", fontWeight = FontWeight.Bold)
            }

            Button(
                onClick = { showDeleteConfirmation = true },
                colors = ButtonDefaults.buttonColors(
                    containerColor = ErrorRed,
                    contentColor = Color.White
                )
            ) {
                Text("Usuń", fontWeight = FontWeight.Bold)
            }
        }
    }

    if (showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            containerColor = IceBlueLight,
            title = {
                Text("Usunąć zadanie?", color = NavyPrimary, fontWeight = FontWeight.Bold)
            },
            text = {
                Text("Czy na pewno chcesz usunąć zadanie \"${task.taskName}\"?", color = NavyPrimary)
            },
            confirmButton = {
                TextButton(onClick = onDeleteClick) {
                    Text("Usuń", color = ErrorRed, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmation = false }) {
                    Text("Anuluj", color = NavyPrimary)
                }
            }
        )
    }
}