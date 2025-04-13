package com.example.quizapp;

import java.util.Arrays;
import java.util.List;

public class Prompt {
    String model;
    List<Message> messages;
    boolean stream;

    public Prompt(String model, List<Message> messages, boolean stream) {
        this.model = model;
        this.messages = messages;
        this.stream = stream;
    }

    public static class Message {
        String role;
        String content;

        public Message(String role, String content) {
            this.role = role;
            this.content = content;
        }
    }
}