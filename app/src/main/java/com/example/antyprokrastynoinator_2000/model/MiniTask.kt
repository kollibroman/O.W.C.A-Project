package com.example.antyprokrastynoinator_2000.model

data class MiniTask(
    val id: Int,
    val name: String,
    val done: Boolean = false
)