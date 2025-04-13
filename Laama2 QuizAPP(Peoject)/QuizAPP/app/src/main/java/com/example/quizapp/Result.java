package com.example.quizapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;
import android.widget.TextView;
public class Result extends AppCompatActivity {
    TextView welcomeText, scoreText;
    Button newQuizBtn, finishBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        welcomeText = findViewById(R.id.welcomeText);
        scoreText = findViewById(R.id.scoreText);
        newQuizBtn = findViewById(R.id.newQuizBtn);
        finishBtn = findViewById(R.id.finishBtn);

        SharedPreferences prefs = getSharedPreferences("QuizPrefs", MODE_PRIVATE);
        String name = prefs.getString("username", "User");
        int score = getIntent().getIntExtra("score", 0);
        int total = getIntent().getIntExtra("total", 5);

        welcomeText.setText("Congratulations " + name + "!");
        scoreText.setText("YOUR SCORE:\n" + score + "/" + total);

        newQuizBtn.setOnClickListener(v -> {
            startActivity(new Intent(this, Quiz.class));
            finish();
        });

        finishBtn.setOnClickListener(v -> finishAffinity());
    }
}
