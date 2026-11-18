package com.example.antyprokrastynoinator_2000.ui.tasks

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.antyprokrastynoinator_2000.model.MiniTask
import com.example.antyprokrastynoinator_2000.model.Priority
import com.example.antyprokrastynoinator_2000.model.Task
import com.example.antyprokrastynoinator_2000.ui.theme.ErrorRed
import com.example.antyprokrastynoinator_2000.ui.theme.IceBlue
import com.example.antyprokrastynoinator_2000.ui.theme.IceBlueLight
import com.example.antyprokrastynoinator_2000.ui.theme.NavyPrimary
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

private val DateOnlyFormat = DateTimeFormatter.ofPattern("dd.MM.yyyy")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTaskScreen(
    priorities: List<Priority>,
    taskToEdit: Task? = null,
    externalError: String? = null,
    onSaveTask: (Task) -> Unit,
    modifier: Modifier = Modifier
) {
    val defaultStart = remember {
        LocalDateTime.now().plusHours(1).withMinute(0).withSecond(0).withNano(0)
    }

    var name by remember(taskToEdit) { mutableStateOf(taskToEdit?.taskName ?: "") }
    var description by remember(taskToEdit) { mutableStateOf(taskToEdit?.description ?: "") }

    var selectedDate by remember(taskToEdit) {
        mutableStateOf(taskToEdit?.start?.toLocalDate() ?: defaultStart.toLocalDate())
    }
    var startHour by remember(taskToEdit) {
        mutableIntStateOf(taskToEdit?.start?.hour ?: defaultStart.hour)
    }
    var startMinute by remember(taskToEdit) {
        mutableIntStateOf(taskToEdit?.start?.minute ?: 0)
    }
    var endHour by remember(taskToEdit) {
        mutableIntStateOf(taskToEdit?.end?.hour ?: defaultStart.plusHours(1).hour)
    }
    var endMinute by remember(taskToEdit) {
        mutableIntStateOf(taskToEdit?.end?.minute ?: 0)
    }

    var showDatePicker by remember { mutableStateOf(false) }

    var newMiniTaskText by remember { mutableStateOf("") }
    val miniTasks = remember(taskToEdit) {
        mutableStateListOf<MiniTask>().apply {
            addAll(taskToEdit?.miniTasks ?: emptyList())
        }
    }

    var isDropdownExpanded by remember { mutableStateOf(false) }
    var selectedPriority by remember(taskToEdit, priorities) {
        mutableStateOf(taskToEdit?.priority ?: priorities.firstOrNull())
    }

    var validationError by remember(externalError) { mutableStateOf(externalError) }

    val inputColors = OutlinedTextFieldDefaults.colors(
        focusedContainerColor = IceBlueLight,
        unfocusedContainerColor = IceBlueLight,
        focusedTextColor = NavyPrimary,
        unfocusedTextColor = NavyPrimary,
        focusedBorderColor = NavyPrimary,
        unfocusedBorderColor = NavyPrimary.copy(alpha = 0.6f),
        focusedLabelColor = NavyPrimary,
        unfocusedLabelColor = NavyPrimary
    )

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = selectedDate
                .atStartOfDay(ZoneOffset.UTC)
                .toInstant()
                .toEpochMilli()
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        selectedDate = Instant.ofEpochMilli(millis)
                            .atZone(ZoneOffset.UTC)
                            .toLocalDate()
                    }
                    showDatePicker = false
                }) {
                    Text("Wybierz", color = NavyPrimary, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Anuluj", color = NavyPrimary)
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(IceBlue)
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        OutlinedTextField(
            value = name,
            onValueChange = {
                name = it
                validationError = null
            },
            label = { Text("Nazwa zadania") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            colors = inputColors
        )

        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Opis (opcjonalnie)") },
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp),
            colors = inputColors
        )

        // SEKCJA: DATA (bez kaskady okienek)
        Text(
            text = "Data zadania: ${selectedDate.format(DateOnlyFormat)}",
            fontWeight = FontWeight.Bold,
            color = NavyPrimary,
            fontSize = 15.sp
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val today = LocalDate.now()
            val tomorrow = today.plusDays(1)

            DateQuickButton(
                label = "Dziś",
                selected = selectedDate == today,
                onClick = { selectedDate = today },
                modifier = Modifier.weight(1f)
            )
            DateQuickButton(
                label = "Jutro",
                selected = selectedDate == tomorrow,
                onClick = { selectedDate = tomorrow },
                modifier = Modifier.weight(1f)
            )
            OutlinedButton(
                onClick = { showDatePicker = true },
                modifier = Modifier.weight(1.3f),
                border = BorderStroke(1.dp, NavyPrimary),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = if (selectedDate != today && selectedDate != tomorrow) NavyPrimary else IceBlueLight,
                    contentColor = if (selectedDate != today && selectedDate != tomorrow) IceBlue else NavyPrimary
                )
            ) {
                Icon(Icons.Default.DateRange, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text("Kalendarz", fontSize = 12.sp)
            }
        }

        // SEKCJA: GODZINY INLINE (zero TimePickerDialog)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            InlineTimeStepper(
                label = "Początek",
                hour = startHour,
                minute = startMinute,
                onHourChange = { startHour = it; validationError = null },
                onMinuteChange = { startMinute = it; validationError = null },
                modifier = Modifier.weight(1f)
            )
            InlineTimeStepper(
                label = "Koniec",
                hour = endHour,
                minute = endMinute,
                onHourChange = { endHour = it; validationError = null },
                onMinuteChange = { endMinute = it; validationError = null },
                modifier = Modifier.weight(1f)
            )
        }

        // Szybkie ustawienie czasu trwania od godziny startu
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Czas trwania:", color = NavyPrimary, fontSize = 13.sp, fontWeight = FontWeight.Medium)
            listOf(30L to "+30 min", 60L to "+1 godz.", 120L to "+2 godz.").forEach { (mins, text) ->
                OutlinedButton(
                    onClick = {
                        val endTime = LocalTime.of(startHour, startMinute).plusMinutes(mins)
                        endHour = endTime.hour
                        endMinute = endTime.minute
                        validationError = null
                    },
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    border = BorderStroke(1.dp, NavyPrimary),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = IceBlueLight,
                        contentColor = NavyPrimary
                    )
                ) {
                    Text(text, fontSize = 12.sp)
                }
            }
        }

        // SEKCJA: PRIORYTET
        ExposedDropdownMenuBox(
            expanded = isDropdownExpanded,
            onExpandedChange = { isDropdownExpanded = !isDropdownExpanded }
        ) {
            OutlinedTextField(
                value = selectedPriority?.name ?: "Brak priorytetu",
                onValueChange = {},
                readOnly = true,
                label = { Text("Priorytet") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isDropdownExpanded) },
                colors = inputColors,
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth()
            )

            ExposedDropdownMenu(
                expanded = isDropdownExpanded,
                onDismissRequest = { isDropdownExpanded = false },
                containerColor = IceBlueLight
            ) {
                priorities.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option.name, color = NavyPrimary) },
                        onClick = {
                            selectedPriority = option
                            isDropdownExpanded = false
                        }
                    )
                }
            }
        }

        // SEKCJA: PODZADANIA (jedno pole wpisywania + kompaktowa lista)
        Text(
            text = "Podzadania (${miniTasks.size})",
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = NavyPrimary
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = newMiniTaskText,
                onValueChange = { newMiniTaskText = it },
                placeholder = { Text("Wpisz podzadanie...", color = NavyPrimary.copy(alpha = 0.6f)) },
                singleLine = true,
                modifier = Modifier.weight(1f),
                colors = inputColors
            )
            IconButton(
                onClick = {
                    val trimmed = newMiniTaskText.trim()
                    if (trimmed.isNotEmpty()) {
                        val nextId = (miniTasks.maxOfOrNull { it.id } ?: 0) + 1
                        miniTasks.add(MiniTask(id = nextId, name = trimmed, done = false))
                        newMiniTaskText = ""
                    }
                },
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(NavyPrimary)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Dodaj podzadanie", tint = IceBlue)
            }
        }

        if (miniTasks.isNotEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(IceBlueLight)
                    .border(1.dp, NavyPrimary.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            ) {
                miniTasks.forEachIndexed { index, miniTask ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "${index + 1}. ${miniTask.name}",
                            color = NavyPrimary,
                            fontSize = 14.sp,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = { miniTasks.removeAt(index) }) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Usuń podzadanie",
                                tint = ErrorRed,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }

        // BŁĄD INLINE (zamiast wyskakującego Toast)
        if (validationError != null) {
            Text(
                text = validationError!!,
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(ErrorRed)
                    .padding(12.dp)
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Button(
            onClick = {
                val startDateTime = LocalDateTime.of(selectedDate, LocalTime.of(startHour, startMinute))
                val endDateTime = LocalDateTime.of(selectedDate, LocalTime.of(endHour, endMinute))

                when {
                    name.isBlank() -> {
                        validationError = "Uzupełnij nazwę zadania!"
                    }
                    !endDateTime.isAfter(startDateTime) -> {
                        validationError = "Godzina zakończenia musi być późniejsza niż rozpoczęcia!"
                    }
                    selectedPriority == null -> {
                        validationError = "Brak dostępnych priorytetów w bazie!"
                    }
                    else -> {
                        val savedTask = Task(
                            id = taskToEdit?.id ?: System.currentTimeMillis().toInt(),
                            taskName = name.trim(),
                            description = description.trim(),
                            miniTasks = miniTasks.toList(),
                            start = startDateTime,
                            end = endDateTime,
                            priority = selectedPriority!!,
                            isDone = taskToEdit?.isDone ?: false
                        )
                        onSaveTask(savedTask)
                    }
                }
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = NavyPrimary,
                contentColor = IceBlue
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
        ) {
            Text(
                text = if (taskToEdit == null) "Dodaj zadanie" else "Zapisz zmiany",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun DateQuickButton(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier,
        border = BorderStroke(1.dp, NavyPrimary),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = if (selected) NavyPrimary else IceBlueLight,
            contentColor = if (selected) IceBlue else NavyPrimary
        )
    ) {
        Text(label, fontSize = 13.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun InlineTimeStepper(
    label: String,
    hour: Int,
    minute: Int,
    onHourChange: (Int) -> Unit,
    onMinuteChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(IceBlueLight)
            .border(1.dp, NavyPrimary, RoundedCornerShape(8.dp))
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(label, color = NavyPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
        Spacer(Modifier.height(4.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            // Godzina
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                TextButton(
                    onClick = { onHourChange((hour + 1) % 24) },
                    contentPadding = PaddingValues(0.dp),
                    modifier = Modifier.size(32.dp)
                ) { Text("+", color = NavyPrimary, fontWeight = FontWeight.Bold, fontSize = 18.sp) }

                Text(
                    text = String.format("%02d", hour),
                    color = NavyPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    textAlign = TextAlign.Center
                )

                TextButton(
                    onClick = { onHourChange((hour + 23) % 24) },
                    contentPadding = PaddingValues(0.dp),
                    modifier = Modifier.size(32.dp)
                ) { Text("-", color = NavyPrimary, fontWeight = FontWeight.Bold, fontSize = 18.sp) }
            }

            Text(":", color = NavyPrimary, fontWeight = FontWeight.Bold, fontSize = 20.sp, modifier = Modifier.padding(horizontal = 4.dp))

            // Minuty (co 15 min)
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                TextButton(
                    onClick = { onMinuteChange((minute + 15) % 60) },
                    contentPadding = PaddingValues(0.dp),
                    modifier = Modifier.size(32.dp)
                ) { Text("+", color = NavyPrimary, fontWeight = FontWeight.Bold, fontSize = 18.sp) }

                Text(
                    text = String.format("%02d", minute),
                    color = NavyPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    textAlign = TextAlign.Center
                )

                TextButton(
                    onClick = { onMinuteChange((minute + 45) % 60) },
                    contentPadding = PaddingValues(0.dp),
                    modifier = Modifier.size(32.dp)
                ) { Text("-", color = NavyPrimary, fontWeight = FontWeight.Bold, fontSize = 18.sp) }
            }
        }
    }
}