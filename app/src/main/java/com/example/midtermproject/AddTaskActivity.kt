package com.example.midtermproject

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.midtermproject.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import android.content.Intent
import com.example.midtermproject.Task

class AddTaskActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    // UI elements
    private lateinit var titleEditText: EditText
    private lateinit var descriptionEditText: EditText
    private lateinit var saveButton: Button
    private lateinit var cancelButton: Button


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_task)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        titleEditText = findViewById(R.id.titleEditText)
        descriptionEditText = findViewById(R.id.descriptionEditText)
        saveButton = findViewById(R.id.saveButton)
        cancelButton = findViewById(R.id.cancelButton)

        saveButton.setOnClickListener {
            saveTask()
        }
        cancelButton.setOnClickListener {
            finish()
        }
    }

    private fun saveTask() {
        val title = titleEditText.text.toString()
        val description = descriptionEditText.text.toString()

        // Create a new task
        val task = Task(
            name = title,
            description = description
        )

        // Get current user ID
        val userId = auth.currentUser?.uid

        if (userId != null) {
            // Add task to user's tasks collection
            db.collection("users")
                .document(userId)
                .collection("tasks")
                .add(task)
                .addOnSuccessListener { documentReference ->
                    task.id = documentReference.id
                    Toast.makeText(this, "Task added.", Toast.LENGTH_SHORT).show()

                    // Create an Intent to hold the result
                    val resultIntent = Intent()
                    resultIntent.putExtra("task_name", title)
                    resultIntent.putExtra("task_description", description)
                    // Optionally, include the document ID if needed
                    resultIntent.putExtra("task_id", documentReference.id)

                    // Set the result code and attach the Intent
                    setResult(RESULT_OK, resultIntent)

                    // Close the activity
                    finish()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Error adding task: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(this, "User not authenticated.", Toast.LENGTH_SHORT).show()
        }
    }

}
