package com.example.purplediary;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class SuccessActivity extends AppCompatActivity {

    // TODO: MASUKKAN API KEY GROQ KAMU DI SINI
    private static final String GROQ_API_KEY = "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";

    private ProgressBar progressBar;
    private TextView tvLoadingAi, tvEmojiMood, tvAiQuote;
    private LinearLayout layoutHasilAi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_success);

        progressBar = findViewById(R.id.progressBar);
        tvLoadingAi = findViewById(R.id.tvLoadingAi);
        layoutHasilAi = findViewById(R.id.layoutHasilAi);
        tvEmojiMood = findViewById(R.id.tvEmojiMood);
        tvAiQuote = findViewById(R.id.tvAiQuote);
        TextView btnOk = findViewById(R.id.btnOK); // Pastikan K-nya sesuai dengan di XML (btnOk atau btnOK)

        btnOk.setOnClickListener(v -> {
            Intent intent = new Intent(SuccessActivity.this, HomeActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });

        String noteContent = getIntent().getStringExtra("NOTE_CONTENT");

        if (noteContent != null && !noteContent.isEmpty()) {
            analyzeMoodWithAI(noteContent);
        } else {
            showFallbackUI();
        }
    }

    private void analyzeMoodWithAI(String content) {
        OkHttpClient client = new OkHttpClient();
        MediaType JSON = MediaType.get("application/json; charset=utf-8");

        String promptMsg = "Baca catatan ini: '" + content + "'. Deteksi perasaannya. Berikan 1 emoji utama (misal: 😭, 😻, 👩‍🍳), lalu berikan 1 kalimat kutipan penyemangat yang sangat gemoy dan lucu. Format respon HANYA: EMOJI|KUTIPAN. Jangan ada teks tambahan.";

        JSONObject jsonBody = new JSONObject();
        try {
            // MENGGUNAKAN MODEL TERBARU AGAR TIDAK ERROR 400
            jsonBody.put("model", "llama-3.1-8b-instant");
            JSONArray messages = new JSONArray();
            JSONObject message = new JSONObject();
            message.put("role", "user");
            message.put("content", promptMsg);
            messages.put(message);
            jsonBody.put("messages", messages);
        } catch (Exception e) {
            e.printStackTrace();
        }

        RequestBody body = RequestBody.create(jsonBody.toString(), JSON);
        Request request = new Request.Builder()
                .url("https://api.groq.com/openai/v1/chat/completions")
                .addHeader("Authorization", "Bearer " + GROQ_API_KEY)
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
                    Toast.makeText(SuccessActivity.this, "Gagal konek internet untuk AI Mood", Toast.LENGTH_SHORT).show();
                    showFallbackUI();
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        String responseData = response.body().string();
                        JSONObject jsonObject = new JSONObject(responseData);
                        String aiReply = jsonObject.getJSONArray("choices")
                                .getJSONObject(0)
                                .getJSONObject("message")
                                .getString("content").trim();

                        String[] parts = aiReply.split("\\|");
                        String emoji = "✨";
                        String quote = aiReply;

                        if (parts.length >= 2) {
                            emoji = parts[0].trim();
                            quote = parts[1].trim();
                        }

                        String finalEmoji = emoji;
                        String finalQuote = quote;

                        runOnUiThread(() -> updateUI(finalEmoji, finalQuote));
                    } catch (Exception e) {
                        runOnUiThread(() -> {
                            Toast.makeText(SuccessActivity.this, "AI bingung balasannya: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            showFallbackUI();
                        });
                    }
                } else {
                    // MEMUNCULKAN PESAN ERROR ASLI DARI GROQ
                    String errorMsg = "Error: " + response.code();
                    if (response.body() != null) {
                        errorMsg = response.body().string();
                    }
                    String finalErrorMsg = errorMsg;
                    runOnUiThread(() -> {
                        Toast.makeText(SuccessActivity.this, "Groq Error: " + finalErrorMsg, Toast.LENGTH_LONG).show();
                        showFallbackUI();
                    });
                }
            }
        });
    }

    private void updateUI(String emoji, String quote) {
        progressBar.setVisibility(View.GONE);
        tvLoadingAi.setVisibility(View.GONE);
        layoutHasilAi.setVisibility(View.VISIBLE);

        tvEmojiMood.setText(emoji);
        tvAiQuote.setText(quote);
    }

    private void showFallbackUI() {
        updateUI("🐱", "Catatan berhasil disimpan! Semangat ya untuk hari ini!");
    }
}