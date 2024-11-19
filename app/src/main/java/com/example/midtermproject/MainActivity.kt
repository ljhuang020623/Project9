package com.example.midtermproject



import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.crashlytics.buildtools.reloc.com.google.common.reflect.TypeToken
import com.google.gson.Gson
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

/**
 * MainActivity is the home screen of the application, allowing users to manage their tasks
 */
class MainActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var addButton: Button
    private lateinit var tooMuchWorkText: TextView
    private lateinit var aboutButton: Button
    private val tasks = mutableListOf<Task>()
    private var showTooMuchWork = true
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var logoutButton: Button

    /**
     * Initializes the activity, sets up the UI components, and restores or loads tasks.
     * @param savedInstanceState contains the activity's previously saved state
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser
        if (currentUser == null) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }
        db = FirebaseFirestore.getInstance()




        recyclerView = findViewById(R.id.recyclerViewTasks)
        addButton = findViewById(R.id.addButton)
        tooMuchWorkText = findViewById(R.id.tooMuchWorkText)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = TaskAdapter(tasks) { task, position ->
            openTaskDetail(task, position)
        }

        if (savedInstanceState != null) {
            val tasksJson = savedInstanceState.getString("tasksJson")
            showTooMuchWork = savedInstanceState.getBoolean("showTooMuchWork")
            if (!tasksJson.isNullOrEmpty()) {
                val loadedTasks: List<Task> = Gson().fromJson(tasksJson, object : TypeToken<List<Task>>() {}.type)
                tasks.clear()
                tasks.addAll(loadedTasks)
            }
        } else {
            loadTasks()
        }

        updateUI()

        addButton.setOnClickListener {
            if (tasks.size < 20) {
                startActivityForResult(Intent(this, AddTaskActivity::class.java), 1)
            }
        }
        tooMuchWorkText.setOnClickListener {
            showTooMuchWork = false;
            updateUI()
        }

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = TaskAdapter(tasks) { task, position ->
            openTaskDetail(task, position)
        }
        logoutButton = findViewById(R.id.logoutButton)
        logoutButton.setOnClickListener {
            auth.signOut()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }


    /**
     * Opens the details of a specific task
     * @param task The task to view
     * @param position The position of task in list
     */
    private fun openTaskDetail(task: Task, position: Int) {
        val intent = Intent(this, TaskDetailActivity::class.java)
        intent.putExtra("task_name", task.name)
        intent.putExtra("task_position", position)
        startActivityForResult(intent, 2)
    }

    /**
     * Updates the UI components based on the current state of tasks
     * Hides or shows buttons and updates the visibility of the RecyclerView
     */
    fun updateUI() {
        recyclerView.adapter?.notifyDataSetChanged()

        if (tasks.size >= 20 && showTooMuchWork) {
            addButton.visibility = View.GONE
            tooMuchWorkText.visibility = View.VISIBLE
        } else {
            addButton.visibility = View.VISIBLE
            tooMuchWorkText.visibility = View.GONE
        }
        recyclerView.visibility = View.VISIBLE
    }

    /**
     * Loads tasks from SharedPreferences and updates the tasks list
     */
    private fun loadTasks() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            db.collection("users")
                .document(currentUser.uid)
                .collection("tasks")
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .get()
                .addOnSuccessListener { documents ->
                    tasks.clear()
                    for (document in documents) {
                        val taskTitle = document.getString("title") ?: ""
                        val taskDescription = document.getString("description") ?: ""
                        val taskId = document.id
                        val task = Task(
                            id = taskId,
                            name = taskTitle,
                            description = taskDescription
                        )
                        tasks.add(task)
                    }
                    updateUI()
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(this, "Error loading tasks: ${exception.message}", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(this, "User not authenticated.", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Saves the current list of tasks to SharedPreferences when paused
     */
    override fun onPause() {
        super.onPause()
    }

    /**
     * Handles the results from started activities
     * @param requestCode The request code that identifies the activity result.
     * @param resultCode The result code returned by the child activity.
     * @param data Intent containing additional data returned from the child activity.
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1 && resultCode == RESULT_OK) {
            val taskName = data?.getStringExtra("task_name")
            val taskDescription = data?.getStringExtra("task_description")
            val taskId = data?.getStringExtra("task_id") // If you included the task ID

            if (taskName != null) {
                val newTask = Task(
                    name = taskName,
                    description = taskDescription ?: "",
                    id = taskId // Ensure your Task class has an 'id' field if you use this
                )
                tasks.add(newTask)
                recyclerView.adapter?.notifyDataSetChanged()
                updateUI()
            }
        } else if (requestCode == 2 && resultCode == RESULT_OK) {
            val position = data?.getIntExtra("task_position", -1)
            if (position != -1) {
                tasks.removeAt(position!!)
                recyclerView.adapter?.notifyDataSetChanged()
                updateUI()
            }
        }
    }


    /**
     * Saves the instance state when the activity is recreated.
     * @param outState stores the instance state.
     */
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString("tasksJson", Gson().toJson(tasks))
        outState.putBoolean("showTooMuchWork", showTooMuchWork)
    }

    /**
     * Restores the instance state after the activity is created
     * @param savedInstanceState contains previously saved state.
     */
    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        val tasksJson = savedInstanceState.getString("tasksJson")
        showTooMuchWork = savedInstanceState.getBoolean("showTooMuchWork")
        if (!tasksJson.isNullOrEmpty()) {
            val loadedTasks: List<Task> = Gson().fromJson(tasksJson, object : TypeToken<List<Task>>() {}.type)
            tasks.clear()
            tasks.addAll(loadedTasks)
        }
        updateUI()
    }
    override fun onDestroy() {
        super.onDestroy()
        // Sign out the user when the activity is destroyed
        auth.signOut()
    }

}