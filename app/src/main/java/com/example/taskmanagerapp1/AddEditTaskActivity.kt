package com.example.taskmanagerapp1

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.taskmanagerapp1.data.Task
import com.example.taskmanagerapp1.databinding.ActivityAddEditTaskBinding
import com.example.taskmanagerapp1.viewmodel.TaskViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class AddEditTaskActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddEditTaskBinding
    private lateinit var viewModel: TaskViewModel

    private var taskId = 0
    private var selectedDate = Calendar.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityAddEditTaskBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this)[TaskViewModel::class.java]

        taskId = intent.getIntExtra("TASK_ID", 0)

        val priorities = listOf("Low", "Medium", "High")
        val statuses = listOf("Pending", "In Progress", "Completed")

        binding.spinnerPriority.adapter = ArrayAdapter(
            this, android.R.layout.simple_spinner_dropdown_item, priorities
        )

        binding.spinnerStatus.adapter = ArrayAdapter(
            this, android.R.layout.simple_spinner_dropdown_item, statuses
        )

        updateDateText()

        binding.btnSelectDate.setOnClickListener {
            DatePickerDialog(
                this,
                { _, year, month, day ->
                    selectedDate.set(year, month, day)
                    updateDateText()
                },
                selectedDate.get(Calendar.YEAR),
                selectedDate.get(Calendar.MONTH),
                selectedDate.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        if (taskId != 0) {
            binding.tvFormHeading.text = "Edit Task"

            viewModel.getTaskById(taskId) { task ->
                task?.let {
                    binding.etTaskTitle.setText(it.title)
                    binding.etTaskDescription.setText(it.description)
                    selectedDate.timeInMillis = it.dueDate
                    updateDateText()

                    binding.spinnerPriority.setSelection(priorities.indexOf(it.priority))
                    binding.spinnerStatus.setSelection(statuses.indexOf(it.status))
                }
            }
        }

        binding.btnSaveTask.setOnClickListener {
            saveTask()
        }
    }

    private fun updateDateText() {
        val format = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        binding.btnSelectDate.text = format.format(selectedDate.time)
    }

    private fun saveTask() {
        val title = binding.etTaskTitle.text.toString().trim()
        val description = binding.etTaskDescription.text.toString().trim()

        if (title.isEmpty()) {
            binding.etTaskTitle.error = "Enter task title"
            return
        }

        if (description.isEmpty()) {
            binding.etTaskDescription.error = "Enter description"
            return
        }

        val task = Task(
            id = taskId,
            title = title,
            description = description,
            dueDate = selectedDate.timeInMillis,
            priority = binding.spinnerPriority.selectedItem.toString(),
            status = binding.spinnerStatus.selectedItem.toString()
        )

        if (taskId == 0) {
            viewModel.insert(task)
            Toast.makeText(this, "Task Added Successfully", Toast.LENGTH_SHORT).show()
        } else {
            viewModel.update(task)
            Toast.makeText(this, "Task Updated Successfully", Toast.LENGTH_SHORT).show()
        }

        finish()
    }
}