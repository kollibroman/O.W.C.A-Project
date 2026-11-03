package com.example.antyprokrastynoinator_2000.model

data class PlacedTask(
    val task: Task,
    val startMin: Int,
    val durationMin: Int,
    val column: Int = 0,
    val totalColumns: Int = 1
)
