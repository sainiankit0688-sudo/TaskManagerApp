
package com.example.taskmanagerapp1.adapter

import android.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.taskmanagerapp1.R
import com.example.taskmanagerapp1.data.Task

class TaskAdapter(
    private val onTaskClick: (Task) -> Unit,
    private val onTaskLongClick: (Task) -> Unit
) : RecyclerView.Adapter<TaskAdapter.TaskViewHolder>() {

    private var taskList = listOf<Task>()

    class TaskViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.tvTaskTitle)
        val description: TextView = itemView.findViewById(R.id.tvTaskDescription)
        val date: TextView = itemView.findViewById(R.id.tvTaskDate)
        val priority: TextView = itemView.findViewById(R.id.tvTaskPriority)
        val status: TextView = itemView.findViewById(R.id.tvTaskStatus)
        val menu: TextView = itemView.findViewById(R.id.btnTaskMenu)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_task, parent, false)

        return TaskViewHolder(view)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val task = taskList[position]

        holder.title.text = task.title
        holder.description.text = task.description

        holder.date.text = android.text.format.DateFormat.format(
            "dd MMM yyyy", task.dueDate
        )

        holder.priority.text = "Priority: ${task.priority}"
        holder.status.text = task.status

        // Task par click karne se Edit
        holder.itemView.setOnClickListener {
            onTaskClick(task)
        }

        // Three Dots Menu
        holder.menu.setOnClickListener {

            val popupMenu = PopupMenu(holder.menu.context, holder.menu)

            popupMenu.menu.add(0, 1, 0, "Edit")
            popupMenu.menu.add(0, 2, 1, "Delete")

            popupMenu.setOnMenuItemClickListener { item ->

                when (item.itemId) {

                    1 -> {
                        onTaskClick(task)
                        true
                    }

                    2 -> {
                        AlertDialog.Builder(holder.itemView.context)
                            .setTitle("Delete Task")
                            .setMessage("Are you sure you want to delete this task?")
                            .setPositiveButton("Delete") { _, _ ->
                                onTaskLongClick(task)
                            }
                            .setNegativeButton("Cancel", null)
                            .show()

                        true
                    }

                    else -> false
                }
            }

            popupMenu.show()
        }

        // Existing Long Press Delete
        holder.itemView.setOnLongClickListener {
            onTaskLongClick(task)
            true
        }
    }

    override fun getItemCount(): Int = taskList.size

    fun submitList(tasks: List<Task>) {
        taskList = tasks
        notifyDataSetChanged()
    }
}
