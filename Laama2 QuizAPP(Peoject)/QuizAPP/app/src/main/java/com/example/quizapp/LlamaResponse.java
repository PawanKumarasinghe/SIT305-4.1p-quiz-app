package com.example.quizapp;

import java.util.List;

public class LlamaResponse {
    public List<Choice> choices;

    public String getResponse() {
        return choices != null && !choices.isEmpty()
                ? choices.get(0).message.content
                : "No response";
    }

    public static class Choice {
        public Message message;
    }

    public static class Message {
        public String role;
        public String content;
    }
}
