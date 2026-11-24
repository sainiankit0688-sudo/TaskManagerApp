
package com.example.taskmanagerapp1

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.taskmanagerapp1.adapter.TaskAdapter
import com.example.taskmanagerapp1.data.Task
import com.example.taskmanagerapp1.databinding.ActivityMainBinding
import com.example.taskmanagerapp1.viewmodel.TaskViewModel

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var taskViewModel: TaskViewModel
    private lateinit var taskAdapter: TaskAdapter

    private var currentTasks = listOf<Task>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        taskViewModel =
            ViewModelProvider(this)[TaskViewModel::class.java]

        setupRecyclerView()
        setupFilters()
        setupSearch()
        observeTasks()

        binding.btnAddTask.setOnClickListener {
            startActivity(
                Intent(this, AddEditTaskActivity::class.java)
            )
        }
    }

    private fun setupRecyclerView() {

        taskAdapter = TaskAdapter(
            onTaskClick = { task ->
                val intent = Intent(
                    this,
                    AddEditTaskActivity::class.java
                )

                intent.putExtra("TASK_ID", task.id)
                startActivity(intent)
            },

            onTaskLongClick = { task ->
                taskViewModel.delete(task)
            }
        )

        binding.recyclerViewTasks.apply {
            layoutManager =
                LinearLayoutManager(this@MainActivity)

            adapter = taskAdapter
        }
    }

    private fun setupFilters() {

        val statusOptions = listOf(
            "All Tasks",
            "Pending",
            "In Progress",
            "Completed"
        )

        val sortOptions = listOf(
            "Due Date",
            "Priority"
        )

        binding.spinnerFilterStatus.adapter =
            ArrayAdapter(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                statusOptions
            )

        binding.spinnerSortTasks.adapter =
            ArrayAdapter(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                sortOptions
            )

        val listener = object : AdapterView.OnItemSelectedListener {

            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                applyFilters()
            }

            override fun onNothingSelected(
                parent: AdapterView<*>?
            ) {}
        }

        binding.spinnerFilterStatus.onItemSelectedListener =
            listener

        binding.spinnerSortTasks.onItemSelectedListener =
            listener
    }

    private fun setupSearch() {

        binding.etSearchTask.addTextChangedListener(
            object : android.text.TextWatcher {

                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {}

                override fun onTextChanged(
                    s: CharSequence?,
                    start: Int,
                    before: Int,
                    count: Int
                ) {
                    applyFilters()
                }

                override fun afterTextChanged(
                    s: android.text.Editable?
                ) {}
            }
        )
    }

    private fun observeTasks() {

        taskViewModel.allTasks.observe(this) { tasks ->

            currentTasks = tasks

            binding.tvTotalCount.text =
                tasks.size.toString()

            binding.tvPendingCount.text =
                tasks.count {
                    it.status == "Pending"
                }.toString()

            binding.tvCompletedCount.text =
                tasks.count {
                    it.status == "Completed"
                }.toString()

            binding.tvTaskCount.text =
                "My Dashboard • ${tasks.size} Tasks"

            applyFilters()
        }
    }

    private fun applyFilters() {

        if (!::taskAdapter.isInitialized) return

        val searchText =
            binding.etSearchTask.text.toString().trim()

        val selectedStatus =
            binding.spinnerFilterStatus.selectedItem?.toString()
                ?: "All Tasks"

        val selectedSort =
            binding.spinnerSortTasks.selectedItem?.toString()
                ?: "Due Date"

        var filteredTasks = currentTasks.filter { task ->

            val matchesSearch =
                task.title.contains(
                    searchText,
                    ignoreCase = true
                )

            val matchesStatus =
                selectedStatus == "All Tasks" ||
                        task.status == selectedStatus

            matchesSearch && matchesStatus
        }

        filteredTasks = when (selectedSort) {

            "Priority" -> {
                val priorityOrder = mapOf(
                    "High" to 0,
                    "Medium" to 1,
                    "Low" to 2
                )

                filteredTasks.sortedWith(
                    compareBy<Task> {
                        priorityOrder[it.priority] ?: 3
                    }.thenBy {
                        it.dueDate
                    }
                )
            }

            else -> {
                filteredTasks.sortedBy {
                    it.dueDate
                }
            }
        }

        taskAdapter.submitList(filteredTasks)
    }
}
