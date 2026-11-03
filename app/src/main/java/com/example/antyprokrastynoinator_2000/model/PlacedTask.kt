package com.example.antyprokrastynoinator_2000.model

data class PlacedTask(
    val task: Task,
    val startMin: Int,
    val durationMin: Int,
    var column: Int = 0,
    var totalColumns: Int = 1
)
