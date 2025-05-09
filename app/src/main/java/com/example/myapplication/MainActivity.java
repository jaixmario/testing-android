package com.example.myapplication;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.content.ClipboardManager;
import android.content.ClipData;
import android.content.Context;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    TextView keyTextView;
    Button fetchButton, copyButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        keyTextView = findViewById(R.id.keyTextView);
        fetchButton = findViewById(R.id.fetchButton);
        copyButton = findViewById(R.id.copyButton);

        fetchButton.setOnClickListener(v -> fetchKey());

        copyButton.setOnClickListener(v -> {
            String textToCopy = keyTextView.getText().toString();
            if (!textToCopy.isEmpty()) {
                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("key", textToCopy);
                clipboard.setPrimaryClip(clip);
                Toast.makeText(this, "Copied!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchKey() {
        new Thread(() -> {
            try {
                URL url = new URL("https://severv1.onrender.com/mypassword/jai/free/");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");

                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();

                JSONObject json = new JSONObject(response.toString());
                String key = json.getString("key");

                runOnUiThread(() -> keyTextView.setText(key));

            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(this, "Error fetching key", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }
}
