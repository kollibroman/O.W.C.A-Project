package com.example.antyprokrastynoinator_2000.utils

import com.example.antyprokrastynoinator_2000.model.Task
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

val TaskDateTimeFormat: DateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")

fun LocalDateTime.formatForTask(): String = format(TaskDateTimeFormat)

private const val MINUTES_IN_DAY = 24 * 60

fun Task.startMinuteOn(day: LocalDate): Int =
    if (start.toLocalDate().isBefore(day)) 0
    else start.hour * 60 + start.minute

fun Task.endMinuteOn(day: LocalDate): Int =
    if (end.toLocalDate().isAfter(day)) MINUTES_IN_DAY
    else end.hour * 60 + end.minute

fun Task.occursOn(day: LocalDate): Boolean {
    val dayStart = day.atStartOfDay()
    val dayEnd = day.plusDays(1).atStartOfDay()
    return start.isBefore(dayEnd) && end.isAfter(dayStart)
}
