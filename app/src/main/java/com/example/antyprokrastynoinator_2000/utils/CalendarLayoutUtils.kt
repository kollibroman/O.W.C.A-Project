package com.example.antyprokrastynoinator_2000.utils

import com.example.antyprokrastynoinator_2000.model.PlacedTask

fun assignTaskColumns(tasks: List<PlacedTask>): List<PlacedTask> {
    val result = mutableListOf<PlacedTask>()

    var clusterStart = 0

    while (clusterStart < tasks.size) {
        val cluster = mutableListOf(tasks[clusterStart])
        var clusterEnd = tasks[clusterStart].startMin +
                tasks[clusterStart].durationMin

        var nextIndex = clusterStart + 1

        while (nextIndex < tasks.size &&
            tasks[nextIndex].startMin < clusterEnd
        ) {
            val task = tasks[nextIndex]
            cluster.add(task)

            clusterEnd = maxOf(
                clusterEnd,
                task.startMin + task.durationMin
            )

            nextIndex++
        }

        val columns = mutableListOf<MutableList<PlacedTask>>()
        val placements = mutableListOf<Pair<PlacedTask, Int>>()

        cluster.forEach { task ->
            val availableColumn = columns.indexOfFirst { column ->
                val lastTask = column.last()

                lastTask.startMin + lastTask.durationMin <= task.startMin
            }

            val columnIndex = if (availableColumn == -1) {
                columns.add(mutableListOf(task))
                columns.lastIndex
            } else {
                columns[availableColumn].add(task)
                availableColumn
            }

            placements.add(task to columnIndex)
        }

        val totalColumns = columns.size

        placements.forEach { (task, column) ->
            result.add(
                task.copy(
                    column = column,
                    totalColumns = totalColumns
                )
            )
        }

        clusterStart = nextIndex
    }

    return result
}