package com.example.antyprokrastynoinator_2000

fun extractHour(dataTekst: String): Int {
    return try {
        val time = dataTekst.split(" ").getOrNull(1) ?: return 0
        time.split(":")[0].toInt()
    } catch (e: Exception) { 0 }
}

fun extractMinute(dataTekst: String): Int {
    return try {
        val time = dataTekst.split(" ").getOrNull(1) ?: return 0
        time.split(":")[1].toInt()
    } catch (e: Exception) { 0 }
}