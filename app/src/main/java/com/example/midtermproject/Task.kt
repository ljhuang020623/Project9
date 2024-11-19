package com.example.midtermproject

/**
 * Data class representing a task with a name.
 */
data class Task(
    var id: String? = null,
    val name: String = "",
    val description: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

