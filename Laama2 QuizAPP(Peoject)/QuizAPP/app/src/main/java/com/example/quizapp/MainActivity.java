package com.example.quizapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;
import android.widget.EditText;

public class MainActivity extends AppCompatActivity {
    EditText nameInput;
    Button startQuizBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        nameInput = findViewById(R.id.nameInput);
        startQuizBtn = findViewById(R.id.startQuizBtn);

        SharedPreferences prefs = getSharedPreferences("QuizPrefs", MODE_PRIVATE);
        String savedName = prefs.getString("username", "");
        nameInput.setText(savedName);

        startQuizBtn.setOnClickListener(v -> {
            String name = nameInput.getText().toString().trim();
            if (!name.isEmpty()) {
                prefs.edit().putString("username", name).apply();
                startActivity(new Intent(this, Quiz.class));
            } else {
                nameInput.setError("Please enter your name");
            }
        });
    }
}
