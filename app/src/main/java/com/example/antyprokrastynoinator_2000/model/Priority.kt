package com.example.antyprokrastynoinator_2000.model

import androidx.compose.ui.graphics.Color

data class Priority(
    val id: Int,
    val name: String,
    val color: Color,
    val alarmOn: Boolean = true
)
