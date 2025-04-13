// Updated Quiz.java with loading state while waiting for Llama 2 API
package com.example.quizapp;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class Quiz extends AppCompatActivity {
    TextView questionText;
    RadioGroup optionsGroup;
    RadioButton option1, option2, option3, option4;
    Button submitBtn;
    ProgressBar progressBar;

    ArrayList<Question> questions;
    int currentIndex = 0;
    int score = 0;
    boolean answered = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz);

        questionText = findViewById(R.id.questionText);
        optionsGroup = findViewById(R.id.optionsGroup);
        option1 = findViewById(R.id.option1);
        option2 = findViewById(R.id.option2);
        option3 = findViewById(R.id.option3);
        option4 = findViewById(R.id.option4);
        submitBtn = findViewById(R.id.submitBtn);
        progressBar = findViewById(R.id.progressBar);

        questions = new ArrayList<>();

        // Disable UI initially and show loading
        submitBtn.setEnabled(false);
        questionText.setText("Loading question...");
        optionsGroup.setVisibility(View.INVISIBLE);
        progressBar.setVisibility(View.INVISIBLE);

        fetchQuestionFromLlama2(); // Fetch from Llama 2 API

        submitBtn.setOnClickListener(v -> {
            if (questions.isEmpty()) return;

            if (!answered) {
                int selectedId = optionsGroup.getCheckedRadioButtonId();
                if (selectedId == -1) {
                    Toast.makeText(this, "Select an answer", Toast.LENGTH_SHORT).show();
                    return;
                }

                int selectedIndex = optionsGroup.indexOfChild(findViewById(selectedId));
                highlightAnswer(selectedIndex);
                if (selectedIndex == questions.get(currentIndex).correctIndex) score++;

                answered = true;
                submitBtn.setText("Next");
            } else {
                currentIndex++;
                if (currentIndex < questions.size()) {
                    loadQuestion();
                } else {
                    Intent intent = new Intent(this, Result.class);
                    intent.putExtra("score", score);
                    intent.putExtra("total", questions.size());
                    startActivity(intent);
                    finish();
                }
            }
        });
    }

    void loadQuestion() {
        Question q = questions.get(currentIndex);
        questionText.setText((currentIndex + 1) + "/" + questions.size() + "  " + q.question);
        option1.setText(q.options[0]);
        option2.setText(q.options[1]);
        option3.setText(q.options[2]);
        option4.setText(q.options[3]);
        optionsGroup.clearCheck();
        resetColors();
        progressBar.setProgress((currentIndex + 1) * 100 / questions.size());
        submitBtn.setText("Submit");
        answered = false;
    }

    void resetColors() {
        for (int i = 0; i < optionsGroup.getChildCount(); i++) {
            ((RadioButton) optionsGroup.getChildAt(i)).setBackgroundColor(Color.TRANSPARENT);
        }
    }

    void highlightAnswer(int selectedIndex) {
        int correctIndex = questions.get(currentIndex).correctIndex;

        RadioButton selectedBtn = (RadioButton) optionsGroup.getChildAt(selectedIndex);
        RadioButton correctBtn = (RadioButton) optionsGroup.getChildAt(correctIndex);

        if (selectedIndex == correctIndex) {
            selectedBtn.setBackgroundColor(Color.parseColor("#99CC00"));
        } else {
            selectedBtn.setBackgroundColor(Color.parseColor("#FF4444"));
            correctBtn.setBackgroundColor(Color.parseColor("#99CC00"));
        }
    }

    private void fetchQuestionFromLlama2() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://api.llama-api.com/") // Verified endpoint
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        Llama2Service service = retrofit.create(Llama2Service.class);

        Prompt.Message systemMessage = new Prompt.Message("system", "Assistant is a large language model trained by OpenAI.");
        Prompt.Message userMessage = new Prompt.Message("user",
                "Generate 5 multiple-choice questions about Android development. " +
                        "Format each question block as:\n" +
                        "Question: ...\nOptions:\nA) ...\nB) ...\nC) ...\nD) ...\nAnswer: A) ...\n\n" +
                        "Separate each question block with two newlines.");
        Prompt prompt = new Prompt("llama3.1-70b", Arrays.asList(systemMessage, userMessage), false);

        Call<LlamaResponse> call = service.generateQuestion(prompt);
        call.enqueue(new Callback<LlamaResponse>() {
            @Override
            public void onResponse(Call<LlamaResponse> call, Response<LlamaResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    String result = response.body().getResponse();
                    Log.d("LLAMA", "Response:\n" + result);
                    parseLlamaQuestion(result);

                    // Enable UI after receiving question
                    optionsGroup.setVisibility(View.VISIBLE);
                    progressBar.setVisibility(View.VISIBLE);
                    submitBtn.setEnabled(true);
                } else {
                    Log.e("LLAMA", "API Error: " + response.code());
                    questionText.setText("Failed to load question. Try again later.");
                }
            }

            @Override
            public void onFailure(Call<LlamaResponse> call, Throwable t) {
                Log.e("LLAMA", "Failure: ", t);
                questionText.setText("Failed to connect. Check your internet.");
                submitBtn.setEnabled(false);
            }
        });
    }

    private void parseLlamaQuestion(String raw) {
        try {
            String[] questionBlocks = raw.split("\\n\\n");
            for (String block : questionBlocks) {
                String[] lines = block.split("\\n");

                String question = "";
                List<String> optionList = new ArrayList<>();
                String answerLine = "";
                String answerText = "";

                for (String line : lines) {
                    if (line.startsWith("Question:")) {
                        question = line.replace("Question:", "").trim();
                    } else if (line.startsWith("Options:")) {
                        continue;
                    } else if (line.startsWith("Answer:")) {
                        answerLine = line.replace("Answer:", "").trim();
                    } else if (line.trim().matches("^[A-D]\\)\\s?.+")) {
                        optionList.add(line.trim());
                    }
                }

                // Extract answer text like from "A) Java"
                String[] answerParts = answerLine.split("\\)", 2);
                if (answerParts.length == 2) {
                    answerText = answerParts[1].trim();
                }

                String[] options = new String[optionList.size()];
                int correctIndex = -1;

                for (int i = 0; i < optionList.size(); i++) {
                    String[] parts = optionList.get(i).split("\\)", 2);
                    String optText = parts.length == 2 ? parts[1].trim() : optionList.get(i);
                    options[i] = optText;

                    if (optText.equalsIgnoreCase(answerText)) {
                        correctIndex = i;
                    }
                }

                if (!question.isEmpty() && correctIndex >= 0) {
                    questions.add(new Question(question, options, correctIndex));
                }
            }

            if (!questions.isEmpty()) {
                loadQuestion();
                optionsGroup.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.VISIBLE);
                submitBtn.setEnabled(true);
            } else {
                questionText.setText("No valid questions parsed.");
            }
        } catch (Exception e) {
            Log.e("ParseError", "Failed to parse multiple Llama2 questions", e);
            questionText.setText("Failed to parse response.");
        }
    }


}
