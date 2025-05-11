package com.example.myapplication;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.EditText;
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

    TextView keyTextView, statusTextView, ipTextView;
    Button fetchButton, copyButton, checkButton;
    EditText keyInput;
    String userIP = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        keyTextView = findViewById(R.id.keyTextView);
        fetchButton = findViewById(R.id.fetchButton);
        copyButton = findViewById(R.id.copyButton);
        checkButton = findViewById(R.id.checkButton);
        keyInput = findViewById(R.id.keyInput);
        statusTextView = findViewById(R.id.statusTextView);
        ipTextView = findViewById(R.id.ipTextView);

        fetchPublicIP();

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

        checkButton.setOnClickListener(v -> checkKey());
    }

    private void fetchPublicIP() {
        new Thread(() -> {
            try {
                URL url = new URL("https://api.ipify.org?format=json");
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
                userIP = json.getString("ip");

                runOnUiThread(() -> ipTextView.setText("Your IP: " + userIP));
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> ipTextView.setText("Failed to get IP"));
            }
        }).start();
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

    private void checkKey() {
        String key = keyInput.getText().toString().trim();

        if (key.isEmpty() || userIP.isEmpty()) {
            Toast.makeText(this, "Enter Key or wait for IP", Toast.LENGTH_SHORT).show();
            return;
        }

        new Thread(() -> {
            try {
                URL url = new URL("https://severv1.onrender.com/mypassword/jai/check/" + key + "/" + userIP);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");

                int responseCode = conn.getResponseCode();
                BufferedReader reader;

                if (responseCode == 200) {
                    reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                } else {
                    reader = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
                }

                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();

                JSONObject json = new JSONObject(response.toString());
                String message = json.optString("message", "Unknown error");
                String resultText;

                if (responseCode == 200 && message.equals("ACCESS GRANTED!") && json.has("remaining_time")) {
                    resultText = message + "\nTime Left: " + json.getString("remaining_time");
                } else {
                    resultText = message;
                }

                runOnUiThread(() -> statusTextView.setText(resultText));

            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> statusTextView.setText("Exception occurred!"));
            }
        }).start();
    }
}
