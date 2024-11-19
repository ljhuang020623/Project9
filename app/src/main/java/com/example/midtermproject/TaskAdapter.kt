package com.example.midtermproject

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

/**
 * TaskAdapter is a RecyclerView adapter for displaying a list of tasks.
 * It handles the creation and binding of task items within a RecyclerView.
 * @property tasks A mutable list of tasks to be displayed.
 * @property onTaskClick A lambda function to handle task click events.
 */
class TaskAdapter(
    private val tasks: MutableList<Task>,
    private val onTaskClick: (Task, Int) -> Unit
) : RecyclerView.Adapter<TaskAdapter.TaskViewHolder>() {

    /**
     * Creates a new ViewHolder for the task item view.
     * @param parent The parent ViewGroup into which the new view will be added.
     * @param viewType The view type of the new view.
     * @return A new instance of TaskViewHolder.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_task, parent, false)
        return TaskViewHolder(view)
    }

    /**
     * Binds the data to the ViewHolder for the specified position.
     * @param holder The ViewHolder that will display the data.
     * @param position The position of the item within the adapter's data set.
     */
    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val task = tasks[position]
        holder.bind(task, position)
    }

    /**
     * Returns the total number of items in the data set held by the adapter.
     */
    override fun getItemCount(): Int = tasks.size

    /**
     * ViewHolder class that holds references to the views for a single task item.
     * @property itemView The view representing a task item.
     */
    inner class TaskViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val taskName: TextView = itemView.findViewById(R.id.taskName)
        private val deleteButton: Button = itemView.findViewById(R.id.deleteButton)

        /**
         * Binds the task data to the views and sets up click listeners.
         * @param task The task to be displayed.
         * @param position The position of the task in the list.
         */
        fun bind(task: Task, position: Int) {
            taskName.text = task.name

            itemView.setOnClickListener {
                onTaskClick(task, position)
            }
            deleteButton.setOnClickListener {
                val currentUser = FirebaseAuth.getInstance().currentUser
                if (currentUser != null && task.id != null) {
                    val db = FirebaseFirestore.getInstance()
                    db.collection("users")
                        .document(currentUser.uid)
                        .collection("tasks")
                        .document(task.id!!)
                        .delete()
                        .addOnSuccessListener {
                            tasks.removeAt(position)
                            notifyItemRemoved(position)
                            notifyItemRangeChanged(position, tasks.size)
                            (itemView.context as MainActivity).updateUI()
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(itemView.context, "Error deleting task: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                } else {
                    Toast.makeText(itemView.context, "Error: User not authenticated or task ID is null.", Toast.LENGTH_SHORT).show()
                }
            }


        }
    }
}