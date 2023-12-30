package com.bezkoder.spring.login.service;

import org.springframework.boot.configurationprocessor.json.JSONObject;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class FileProcessor {
    private static final String API_KEY = "AIzaSyDd-cRJlXiX7bmvWA2FAKMLARpJbZ52amw";

    public String generateContent(String inputText) throws Exception {

        String pre ="In accordance with";
        String prompt  = "Classify the file name content and group similar topics together. The topic is Chinese and generates ONLY json";
        String format= " {File name theme (excluding file extension)}:{[File List]}" +
                "                { File name theme (excluding file extension) : [fileA,fileB]," +
                "                  File name theme 2 (excluding file extension)}: [fileC]}";


        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI("https://generativelanguage.googleapis.com/v1beta/models/gemini-pro:generateContent?key=" + API_KEY))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString("{\"contents\": [{\"parts\":[{\"text\": \""
                        +pre+ inputText + prompt+"\"}]}]}"))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());


        // Parse the response body
        JSONObject jsonObject = new JSONObject(response.body());

        // Get the "text" content
        String textContent = jsonObject.getJSONArray("candidates")
                .getJSONObject(0)
                .getJSONObject("content")
                .getJSONArray("parts")
                .getJSONObject(0)
                .getString("text");

        System.out.println(textContent);

        return textContent;

    }
}