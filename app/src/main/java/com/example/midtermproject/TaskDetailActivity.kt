package com.example.midtermproject

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

import com.google.firebase.crashlytics.buildtools.reloc.com.google.common.reflect.TypeToken
import com.google.gson.Gson

/**
 * TaskDetailActivity allows users to mark it as complete,navigate to previous or next tasks,
 * and cancel the operation. It interacts with SharedPreferences to load and save the task list.
 */
class TaskDetailActivity : AppCompatActivity() {

    private lateinit var taskDetailName: TextView
    private lateinit var markCompleteButton: Button
    private lateinit var cancelButton: Button
    private lateinit var previousTaskButton: Button
    private lateinit var nextTaskButton: Button

    private var taskPosition: Int = -1
    private var tasks: MutableList<Task> = mutableListOf()

    /**
     * Initializes the activity, sets up UI components, and loads task details.
     * @param savedInstanceState contains the activity's previously saved state
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_task_detail)

        taskDetailName = findViewById(R.id.taskDetailName)
        markCompleteButton = findViewById(R.id.markCompleteButton)
        cancelButton = findViewById(R.id.cancelButton)
        previousTaskButton = findViewById(R.id.previousTaskButton)
        nextTaskButton = findViewById(R.id.nextTaskButton)

        taskPosition = intent.getIntExtra("task_position", -1)
        loadTasksFromSharedPreferences()

        if (taskPosition != -1 && taskPosition < tasks.size) {
            taskDetailName.text = tasks[taskPosition].name
        }

        markCompleteButton.setOnClickListener { markTaskAsComplete() }
        cancelButton.setOnClickListener { finish() }
        previousTaskButton.setOnClickListener { navigateToPreviousTask() }
        nextTaskButton.setOnClickListener { navigateToNextTask() }

        updateNavigationButtons()
    }

    /**
     * Marks the task as complete and removes it from task list
     */
    private fun markTaskAsComplete() {
        if (taskPosition != -1 && taskPosition < tasks.size) {
            tasks.removeAt(taskPosition)
            saveTasksToSharedPreferences()
            setResult(RESULT_OK, Intent().putExtra("task_position", taskPosition))
            finish()
        }
    }

    /**
     * Navigates to previous task and updates task name
     */
    private fun navigateToPreviousTask() {
        if (taskPosition > 0) {
            taskPosition--
            taskDetailName.text = tasks[taskPosition].name
        }
        updateNavigationButtons()
    }

    /**
     * Navigates to next task and updates the task name
     */
    private fun navigateToNextTask() {
        if (taskPosition < tasks.size - 1) {
            taskPosition++
            taskDetailName.text = tasks[taskPosition].name
        }
        updateNavigationButtons()
    }

    /**
     * Updates navigation buttons states based on the current task position
     */

    private fun updateNavigationButtons() {
        previousTaskButton.isEnabled = taskPosition > 0
        nextTaskButton.isEnabled = taskPosition < tasks.size - 1
    }

    /**
     * Loads tasks from SharedPreferences and puts them in task list
     */
    private fun loadTasksFromSharedPreferences() {
        val sharedPreferences = getSharedPreferences("ToDoApp", Context.MODE_PRIVATE)
        val tasksJson = sharedPreferences.getString("tasks", null)
        if (!tasksJson.isNullOrEmpty()) {
            val loadedTasks: List<Task> = Gson().fromJson(tasksJson, object : TypeToken<List<Task>>() {}.type)
            tasks.clear()
            tasks.addAll(loadedTasks)
        }
    }

    /**
     * Saves current task list to SharedPreferences
     */
    private fun saveTasksToSharedPreferences() {
        val sharedPreferences = getSharedPreferences("ToDoApp", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        val tasksJson = Gson().toJson(tasks)
        editor.putString("tasks", tasksJson)
        editor.apply()
    }
}