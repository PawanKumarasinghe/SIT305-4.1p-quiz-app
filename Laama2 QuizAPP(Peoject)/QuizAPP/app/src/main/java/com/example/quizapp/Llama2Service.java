package com.example.quizapp;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface Llama2Service {

    @Headers({
            "Authorization: Bearer 1ea775a7-4e7d-47bf-b646-f18e28075d58",
            "Content-Type: application/json"
    })
    @POST("chat/completions")
    Call<LlamaResponse> generateQuestion(@Body Prompt prompt);
}
