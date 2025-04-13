package com.example.quizapp;

public class Question {
    String question;
    String[] options;
    int correctIndex;

    public Question(String question, String[] options, int correctIndex) {
        this.question = question;
        this.options = options;
        this.correctIndex = correctIndex;
    }
}
