package com.example.midtermproject

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

/**
 * MainActivity is the home screen of the application, allowing users to manage their tasks.
 * It handles user authentication, task display, addition, deletion, and search functionality.
 */
class MainActivity : AppCompatActivity() {


    private lateinit var recyclerView: RecyclerView
    private lateinit var addButton: Button
    private lateinit var logoutButton: Button
    private lateinit var searchEditText: EditText
    private lateinit var searchButton: Button


    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore


    private val tasks = mutableListOf<Task>()

    /**
     * Initializes the activity, sets up UI components, and handles user authentication.
     * @param savedInstanceState Contains the activity's previously saved state.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        val currentUser = auth.currentUser
        if (currentUser == null) {

            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }
        recyclerView = findViewById(R.id.recyclerViewTasks)
        addButton = findViewById(R.id.addButton)
        logoutButton = findViewById(R.id.logoutButton)
        searchEditText = findViewById(R.id.searchEditText)
        searchButton = findViewById(R.id.searchButton)

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = TaskAdapter(tasks) { task, position ->
            openTaskDetail(task, position)
        }

        loadTasks()

        addButton.setOnClickListener {
            startActivityForResult(Intent(this, AddTaskActivity::class.java), 1)
        }

        logoutButton.setOnClickListener {
            auth.signOut()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
        searchButton.setOnClickListener {
            val queryText = searchEditText.text.toString()
            if (queryText.isNotEmpty()) {
                searchTasks(queryText)
            } else {
                loadTasks()
            }
        }
    }

    /**
     * Opens the details of a specific task.
     * @param task The task to view.
     * @param position The position of the task in the list.
     */
    private fun openTaskDetail(task: Task, position: Int) {
        val intent = Intent(this, TaskDetailActivity::class.java)
        intent.putExtra("task_name", task.name)
        intent.putExtra("task_description", task.description)
        intent.putExtra("task_id", task.id)
        intent.putExtra("task_position", position)
        startActivityForResult(intent, 2)
    }

    /**
     * Updates the UI components based on the current state of tasks.
     */
    public fun updateUI() {
        recyclerView.adapter?.notifyDataSetChanged()
    }

    /**
     * Loads tasks from Firestore and updates the tasks list.
     */
    private fun loadTasks() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            db.collection("users")
                .document(currentUser.uid)
                .collection("tasks")
                .orderBy("timestamp")
                .get()
                .addOnSuccessListener { documents ->
                    tasks.clear()
                    for (document in documents) {
                        val task = document.toObject(Task::class.java)
                        task.id = document.id
                        tasks.add(task)
                    }
                    updateUI()
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(this, "Error loading tasks: ${exception.message}", Toast.LENGTH_SHORT).show()
                }
        } else {
            // User not authenticated; redirect to LoginActivity
            Toast.makeText(this, "User not authenticated.", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    /**
     * Searches for tasks that exactly match the query text.
     * @param queryText The text to search for in task names.
     */
    private fun searchTasks(queryText: String) {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            db.collection("users")
                .document(currentUser.uid)
                .collection("tasks")
                .whereEqualTo("name", queryText)
                .get()
                .addOnSuccessListener { documents ->
                    tasks.clear()
                    for (document in documents) {
                        val task = document.toObject(Task::class.java)
                        task.id = document.id
                        tasks.add(task)
                    }
                    updateUI()
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(this, "Error searching tasks: ${exception.message}", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(this, "User not authenticated.", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    /**
     * Handles the results from started activities.
     * @param requestCode The request code that identifies the activity result.
     * @param resultCode The result code returned by the child activity.
     * @param data Intent containing additional data returned from the child activity.
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1 && resultCode == RESULT_OK) {
            loadTasks()
        } else if (requestCode == 2 && resultCode == RESULT_OK) {
            loadTasks()
        }
    }

    /**
     * Signs out the user when the app is no longer in the foreground.
     */
    override fun onPause() {
        super.onPause()
        auth.signOut()
    }

    /**
     * Signs out the user when the app is no longer visible.
     */
    override fun onStop() {
        super.onStop()
        auth.signOut()
    }

    /**
     * Handles changes in multi-window mode.
     * @param isInMultiWindowMode Boolean indicating whether the app is in multi-window mode.
     */
    override fun onMultiWindowModeChanged(isInMultiWindowMode: Boolean) {
        super.onMultiWindowModeChanged(isInMultiWindowMode)
        if (!isInMultiWindowMode) {
            auth.signOut()
        }
    }
}