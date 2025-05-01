package com.example.todolist

import android.graphics.Paint.STRIKE_THRU_TEXT_FLAG
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.CompoundButton
import androidx.recyclerview.widget.RecyclerView
import com.example.todolist.databinding.ItemTodoBinding

class TodoAdapter(private val todos: MutableList<Todo>) :
    RecyclerView.Adapter<TodoAdapter.TodoViewHolder>() {

    inner class TodoViewHolder(val binding: ItemTodoBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TodoViewHolder {
        val binding = ItemTodoBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TodoViewHolder(binding)
    }

    private fun toggleStrikeThrough(binding: ItemTodoBinding, isChecked: Boolean) {
        if (isChecked) {
            binding.tvTodoTitle.paintFlags = binding.tvTodoTitle.paintFlags or STRIKE_THRU_TEXT_FLAG
        } else {
            binding.tvTodoTitle.paintFlags = binding.tvTodoTitle.paintFlags and STRIKE_THRU_TEXT_FLAG.inv()
        }
    }

    override fun onBindViewHolder(holder: TodoViewHolder, position: Int) {
        val curTodo = todos[position]
        val binding = holder.binding

        binding.tvTodoTitle.text = curTodo.title
        binding.cbDone.isChecked = curTodo.isChecked
        toggleStrikeThrough(binding, curTodo.isChecked)

        binding.cbDone.setOnCheckedChangeListener(null) // prevent unwanted triggers
        binding.cbDone.setOnCheckedChangeListener { _, isChecked ->
            toggleStrikeThrough(binding, isChecked)
            curTodo.isChecked = isChecked
        }
    }

    override fun getItemCount(): Int = todos.size

    fun addTodo(todo: Todo) {
        todos.add(todo)
        notifyItemInserted(todos.size - 1)
    }

    fun deleteDoneTodos() {
        todos.removeAll { it.isChecked }
        notifyDataSetChanged()
    }
    }
