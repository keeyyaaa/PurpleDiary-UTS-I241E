package com.example.purplediary;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.purplediary.db.DatabaseHelper;
import com.example.purplediary.model.Note;

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

public class AddNoteActivity extends AppCompatActivity {

    // API KEY GROQ KAMU
    private static final String GROQ_API_KEY = "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";

    private EditText etTitle, etContent;
    private DatabaseHelper dbHelper;
    private boolean isEditMode = false;
    private int editNoteId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_note);

        dbHelper = new DatabaseHelper(this);
        etTitle = findViewById(R.id.etTitle);
        etContent = findViewById(R.id.etContent);
        ImageView btnBack = findViewById(R.id.btnBack);
        TextView btnSave = findViewById(R.id.btnSave);

        isEditMode = getIntent().getBooleanExtra("IS_EDIT", false);
        if (isEditMode) {
            editNoteId = getIntent().getIntExtra("NOTE_ID", -1);
            etTitle.setText(getIntent().getStringExtra("NOTE_TITLE"));
            etContent.setText(getIntent().getStringExtra("NOTE_CONTENT"));
        }

        btnBack.setOnClickListener(v -> finish());
        btnSave.setOnClickListener(v -> processNote());
    }

    private void processNote() {
        String title = etTitle.getText().toString().trim();
        String content = etContent.getText().toString().trim();

        if (title.isEmpty() && content.isEmpty()) {
            Toast.makeText(this, "Isi catatan dulu ya!", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "AI sedang membaca dan mengelompokkan...", Toast.LENGTH_LONG).show();
            analyzeWithGroq(title, content);
        }
    }

    private void analyzeWithGroq(String userTitle, String noteContent) {
        OkHttpClient client = new OkHttpClient();
        MediaType JSON = MediaType.get("application/json; charset=utf-8");

        boolean isTitleEmpty = userTitle.isEmpty();
        String promptMsg;

        if (isTitleEmpty) {
            promptMsg = "Buat 1 judul (maks 4 kata) dan tentukan kategorinya (Pilih 1: RESEP, CURHAT, PENGINGAT, atau LAINNYA). Format WAJIB: JUDUL|KATEGORI. Isi: " + noteContent;
        } else {
            promptMsg = "Tentukan kategorinya (Pilih 1: RESEP, CURHAT, PENGINGAT, atau LAINNYA). Format WAJIB HANYA 1 KATA. Isi: " + noteContent;
        }

        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("model", "llama-3.1-8b-instant");
            JSONArray messages = new JSONArray();

            // ==========================================
            // INSTRUKSI BOS (SYSTEM PROMPT) ANTI-BAWEL
            // ==========================================
            JSONObject systemMessage = new JSONObject();
            systemMessage.put("role", "system");
            systemMessage.put("content", "Kamu adalah AI pengelompok catatan. Jawab LANGSUNG dengan format yang diminta. DILARANG KERAS memakai kalimat pengantar seperti 'Berikut adalah', basa-basi, atau tanda kutip.");
            messages.put(systemMessage);

            // PERMINTAAN KITA (USER)
            JSONObject userMessage = new JSONObject();
            userMessage.put("role", "user");
            userMessage.put("content", promptMsg);
            messages.put(userMessage);

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
                runOnUiThread(() -> saveToDatabase(userTitle.isEmpty() ? "Catatan Baru" : userTitle, noteContent, "LAINNYA"));
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        String responseData = response.body().string();
                        JSONObject jsonObject = new JSONObject(responseData);
                        String aiReply = jsonObject.getJSONArray("choices").getJSONObject(0).getJSONObject("message").getString("content").trim();

                        String finalTitle = userTitle;
                        String rawCategory = "LAINNYA";

                        if (isTitleEmpty) {
                            String[] parts = aiReply.split("\\|");
                            if (parts.length >= 2) {
                                finalTitle = parts[0].replace("\"", "").trim();
                                rawCategory = parts[1].replace("\"", "").trim().toUpperCase();
                            } else {
                                finalTitle = aiReply.replace("\"", "").trim();
                            }
                        } else {
                            rawCategory = aiReply.replace("\"", "").trim().toUpperCase();
                        }

                        // Pastikan AI mematuhi aturan saringan
                        String cleanCategory;
                        if (rawCategory.contains("RESEP")) cleanCategory = "RESEP";
                        else if (rawCategory.contains("PENGINGAT")) cleanCategory = "PENGINGAT"; // Pengingat dicek lebih dulu biar aman
                        else if (rawCategory.contains("CURHAT")) cleanCategory = "CURHAT";
                        else cleanCategory = "LAINNYA";

                        String finalSavedTitle = finalTitle;
                        runOnUiThread(() -> saveToDatabase(finalSavedTitle, noteContent, cleanCategory));

                    } catch (Exception e) {
                        runOnUiThread(() -> saveToDatabase(userTitle.isEmpty() ? "Catatan Baru" : userTitle, noteContent, "LAINNYA"));
                    }
                } else {
                    runOnUiThread(() -> saveToDatabase(userTitle.isEmpty() ? "Catatan Baru" : userTitle, noteContent, "LAINNYA"));
                }
            }
        });
    }

    private void saveToDatabase(String title, String content, String category) {
        Note newNote = new Note(title, content, category);
        if (isEditMode) {
            newNote.setId(editNoteId);
            dbHelper.updateNote(newNote);
        } else {
            dbHelper.addNote(newNote);
        }

        Intent intent = new Intent(AddNoteActivity.this, SuccessActivity.class);
        intent.putExtra("NOTE_CONTENT", content);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}