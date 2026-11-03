package com.example.antyprokrastynoinator_2000.ui.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters

private val WeekSelectorBackground = Color(0xFF008575)
private val DayNames = listOf("Pn", "Wt", "Śr", "Cz", "Pt", "So", "Nd")
private val ShortWeekFormat = DateTimeFormatter.ofPattern("dd.MM")
private val FullWeekFormat = DateTimeFormatter.ofPattern("dd.MM.yyyy")

/**
 * Panel wyboru tygodnia: strzałki do przełączania tygodni, nagłówek otwierający
 * dialog z kalendarzem oraz pasek dni Pn–Nd.
 *
 * Komponent jest bezstanowy względem daty: wybraną datę trzyma wywołujący.
 */
@Composable
fun WeekSelector(
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    modifier: Modifier = Modifier
) {
    var showWeekPicker by remember { mutableStateOf(false) }

    val weekStart = selectedDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
    val weekEnd = weekStart.plusDays(6)

    if (showWeekPicker) {
        WeekPickerDialog(
            initialDate = selectedDate,
            onConfirm = { date ->
                onDateSelected(date)
                showWeekPicker = false
            },
            onDismiss = { showWeekPicker = false }
        )
    }

    Column(modifier = modifier.fillMaxWidth().background(WeekSelectorBackground)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { onDateSelected(selectedDate.minusWeeks(1)) }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    tint = Color.White,
                    contentDescription = "Poprzedni tydzień"
                )
            }

            Text(
                text = "Tydzień: ${weekStart.format(ShortWeekFormat)} – ${weekEnd.format(FullWeekFormat)}",
                color = Color.White,
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { showWeekPicker = true }
                    .padding(vertical = 8.dp),
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold
            )

            IconButton(onClick = { onDateSelected(selectedDate.plusWeeks(1)) }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    tint = Color.White,
                    contentDescription = "Następny tydzień"
                )
            }
        }

        WeekDaysRow(
            weekStart = weekStart,
            selectedDate = selectedDate,
            onDayClick = onDateSelected
        )
    }
}

@Composable
private fun WeekDaysRow(
    weekStart: LocalDate,
    selectedDate: LocalDate,
    onDayClick: (LocalDate) -> Unit
) {
    val today = LocalDate.now()
    val dayShape = RoundedCornerShape(8.dp)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 4.dp, end = 4.dp, bottom = 8.dp)
    ) {
        for (i in 0..6) {
            val day = weekStart.plusDays(i.toLong())
            val isSelected = day == selectedDate
            val textColor = if (isSelected) WeekSelectorBackground else Color.White

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(2.dp)
                    .clip(dayShape)
                    .background(if (isSelected) Color.White else Color.Transparent)
                    .then(
                        if (day == today && !isSelected) Modifier.border(1.dp, Color.White, dayShape)
                        else Modifier
                    )
                    .clickable { onDayClick(day) }
                    .padding(vertical = 6.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = DayNames[i], color = textColor, fontSize = 12.sp)
                Text(
                    text = day.dayOfMonth.toString(),
                    color = textColor,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WeekPickerDialog(
    initialDate: LocalDate,
    onConfirm: (LocalDate) -> Unit,
    onDismiss: () -> Unit
) {
    // DatePicker operuje na milisekundach UTC, więc konwersja musi iść przez UTC
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = initialDate
            .atStartOfDay(ZoneOffset.UTC)
            .toInstant()
            .toEpochMilli()
    )

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                val millis = datePickerState.selectedDateMillis
                if (millis != null) {
                    onConfirm(Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate())
                } else {
                    onDismiss()
                }
            }) {
                Text("Wybierz")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Anuluj")
            }
        }
    ) {
        DatePicker(
            state = datePickerState,
            title = {
                Text(
                    text = "Wybierz dzień z tygodnia",
                    modifier = Modifier.padding(start = 24.dp, end = 12.dp, top = 16.dp)
                )
            }
        )
    }
}
