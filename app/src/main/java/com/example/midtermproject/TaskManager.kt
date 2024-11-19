package com.example.midtermproject

import android.content.Context
import com.google.firebase.crashlytics.buildtools.reloc.com.google.common.reflect.TypeToken
import com.google.gson.Gson

object TaskManager {
    /**
     * Loads a list of tasks from SharedPreferences.
     * @param context The context from which to access SharedPreferences.
     * @return A mutable list of tasks retrieved from SharedPreferences. If no tasks are found, an empty mutable list is returned.
     */
    fun loadTasksFromSharedPreferences(context: Context): MutableList<Task> {
        val sharedPreferences = context.getSharedPreferences("ToDoApp", Context.MODE_PRIVATE)
        val tasksJson = sharedPreferences.getString("tasks", null)
        return if (!tasksJson.isNullOrEmpty()) {
            Gson().fromJson(tasksJson, object : TypeToken<MutableList<Task>>() {}.type)
        } else {
            mutableListOf()
        }
    }
    /**
     * Saves a list of tasks to SharedPreferences.
     * @param context The context from which to access SharedPreferences.
     * @param tasks A mutable list of tasks to be saved.
     */
    fun saveTasksToSharedPreferences(context: Context, tasks: MutableList<Task>) {
        val sharedPreferences = context.getSharedPreferences("ToDoApp", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        val tasksJson = Gson().toJson(tasks)
        editor.putString("tasks", tasksJson)
        editor.apply()
    }
}