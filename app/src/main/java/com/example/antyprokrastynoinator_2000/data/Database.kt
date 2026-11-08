package com.example.antyprokrastynoinator_2000

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import androidx.compose.ui.graphics.Color
import com.example.antyprokrastynoinator_2000.model.MiniTask
import com.example.antyprokrastynoinator_2000.model.Priority
import com.example.antyprokrastynoinator_2000.model.Task
import java.time.LocalDateTime
import java.time.ZoneOffset

class Database(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "prokrastynoinator.db"
        private const val DATABASE_VERSION = 1
    }

    override fun onCreate(db: SQLiteDatabase) {
        // Tabela Ustawień widoku
        val tabSettings = """
        CREATE TABLE settings (
            id INTEGER PRIMARY KEY,
            callendarView INTEGER,  
            antyMode BOOLEAN
        )
        """.trimIndent()
        db.execSQL(tabSettings)

        // Tabela priorytetów
        val tabPriority = """
        CREATE TABLE priority (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            priorityName TEXT,
            alarmOn BOOLEAN,
            color TEXT
        )
        """.trimIndent()
        db.execSQL(tabPriority)

        val insertPriorities = """
        INSERT INTO priority (priorityName, color, alarmOn) VALUES 
        ('bardzo ważne', '0xFF1B4D3E', 1),
        ('ważne', '0xFF008575', 1),
        ('mało ważne', '0xFF26A69A', 0);
         """.trimIndent()
        db.execSQL(insertPriorities)

        // Tabela Zadań (Główna tabela), nie da się usunąć priorytetu gdy ma przypisany task
        val tabTask = """
        CREATE TABLE tasks (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            taskName TEXT,
            description TEXT,
            start_date INTEGER,
            end_date INTEGER,
            priority INTEGER,
            is_done BOOLEAN,
            FOREIGN KEY(priority) REFERENCES priority(id) ON DELETE RESTRICT
        )
        """.trimIndent()
        db.execSQL(tabTask)

        // Tabela minitasków
        val tabMiniTask = """
        CREATE TABLE miniTasks (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            taskId INTEGER,
            miniTaskName TEXT,
            is_done BOOLEAN,
            FOREIGN KEY(taskId) REFERENCES tasks(id) ON DELETE CASCADE
        )
        """.trimIndent()
        db.execSQL(tabMiniTask)


    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS settings")
        db.execSQL("DROP TABLE IF EXISTS miniTasks")
        db.execSQL("DROP TABLE IF EXISTS tasks")
        db.execSQL("DROP TABLE IF EXISTS priority")
        onCreate(db)
    }

    override fun onConfigure(db: SQLiteDatabase) {
        super.onConfigure(db)
        db.setForeignKeyConstraintsEnabled(true)
    }

    fun addTask(task: Task): Long {
        val db = this.writableDatabase
        var taskId: Long = -1

        db.beginTransaction()
        try {
            val taskValues = ContentValues().apply {
                put("taskName", task.taskName)
                put("description", task.description)
                put("start_date", task.start.toEpochSecond(java.time.ZoneOffset.UTC))
                put("end_date", task.end.toEpochSecond(java.time.ZoneOffset.UTC))
                put("priority", task.priority.id)
                put("is_done", 0)
            }
            taskId = db.insert("tasks", null, taskValues)

            for (mini in task.miniTasks) {
                val miniTaskValues = ContentValues().apply {
                    put("taskId", taskId)
                    put("miniTaskName", mini.name)
                    put("is_done", if (mini.done) 1 else 0)
                }
                db.insert("miniTasks", null, miniTaskValues)
            }

            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
        return taskId
    }

    fun getTasks(): List<Task> {
        val tasks = mutableListOf<Task>()
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM tasks", null)

        if (cursor.moveToFirst()) {
            do {
                val id = cursor.getInt(cursor.getColumnIndexOrThrow("id"))
                val taskName = cursor.getString(cursor.getColumnIndexOrThrow("taskName"))
                val description = cursor.getString(cursor.getColumnIndexOrThrow("description"))
                val startEpoch = cursor.getLong(cursor.getColumnIndexOrThrow("start_date"))
                val endEpoch = cursor.getLong(cursor.getColumnIndexOrThrow("end_date"))
                val startDate = LocalDateTime.ofEpochSecond(startEpoch, 0, ZoneOffset.UTC)
                val endDate = LocalDateTime.ofEpochSecond(endEpoch, 0, ZoneOffset.UTC)

                val priorityId = cursor.getInt(cursor.getColumnIndexOrThrow("priority"))
                val isDone = cursor.getInt(cursor.getColumnIndexOrThrow("is_done")) == 1

                var priority: Priority? = null
                val priorityCursor = db.rawQuery("SELECT * FROM priority WHERE id = ?", arrayOf(priorityId.toString()))
                if (priorityCursor.moveToFirst()) {
                    val pId = priorityCursor.getInt(priorityCursor.getColumnIndexOrThrow("id"))
                    val pName = priorityCursor.getString(priorityCursor.getColumnIndexOrThrow("priorityName"))
                    val pAlarm = priorityCursor.getInt(priorityCursor.getColumnIndexOrThrow("alarmOn")) == 1
                    val pColorStr = priorityCursor.getString(priorityCursor.getColumnIndexOrThrow("color"))

                    val color = Color(pColorStr.removePrefix("0x").toLong(16))

                    priority = Priority(id = pId, name = pName, color = color, alarmOn = pAlarm)
                }
                priorityCursor.close()

                val miniTasksList = mutableListOf<MiniTask>()
                val miniTasksCursor = db.rawQuery("SELECT * FROM miniTasks WHERE taskId = ?", arrayOf(id.toString()))
                if (miniTasksCursor.moveToFirst()) {
                    do {
                        val mId = miniTasksCursor.getInt(miniTasksCursor.getColumnIndexOrThrow("id"))
                        val mName = miniTasksCursor.getString(miniTasksCursor.getColumnIndexOrThrow("miniTaskName"))
                        val mDone = miniTasksCursor.getInt(miniTasksCursor.getColumnIndexOrThrow("is_done")) == 1

                        miniTasksList.add(MiniTask(id = mId, name = mName, done = mDone))
                    } while (miniTasksCursor.moveToNext())
                }
                miniTasksCursor.close()

                if (priority != null) {
                    tasks.add(
                        Task(
                            id = id,
                            taskName = taskName,
                            description = description,
                            start = startDate,
                            end = endDate,
                            priority = priority,
                            miniTasks = miniTasksList
                        )
                    )
                }

            } while (cursor.moveToNext())
        }
        cursor.close()

        return tasks
    }

    fun updateTask(task: Task) {
        val db = this.writableDatabase
        db.beginTransaction()

        try {
            val taskValues = ContentValues().apply {
                put("taskName", task.taskName)
                put("description", task.description)
                put("start_date", task.start.toEpochSecond(java.time.ZoneOffset.UTC))
                put("end_date", task.end.toEpochSecond(java.time.ZoneOffset.UTC))
                put("priority", task.priority.id)
                put("is_done", if (task.isDone) 1 else 0)
            }
            db.update("tasks", taskValues, "id = ?", arrayOf(task.id.toString()))

            db.delete("miniTasks", "taskId = ?", arrayOf(task.id.toString()))
            for (mini in task.miniTasks) {
                val miniTaskValues = ContentValues().apply {
                    put("taskId", task.id)
                    put("miniTaskName", mini.name)
                    put("is_done", if (mini.done) 1 else 0)
                }
                db.insert("miniTasks", null, miniTaskValues)
            }

            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    fun deleteTask(taskId: Int) {
        val db = this.writableDatabase
        db.delete("tasks", "id = ?", arrayOf(taskId.toString()))
    }

    fun getPriorities(): List<Priority> {
        val priorities = mutableListOf<Priority>()
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM priority", null)

        if (cursor.moveToFirst()) {
            do {
                val id = cursor.getInt(cursor.getColumnIndexOrThrow("id"))
                val name = cursor.getString(cursor.getColumnIndexOrThrow("priorityName"))
                val alarmOn = cursor.getInt(cursor.getColumnIndexOrThrow("alarmOn")) == 1
                val colorStr = cursor.getString(cursor.getColumnIndexOrThrow("color"))

                val color = Color(colorStr.removePrefix("0x").toLong(16))

                priorities.add(Priority(id = id, name = name, color = color, alarmOn = alarmOn))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return priorities
    }
}