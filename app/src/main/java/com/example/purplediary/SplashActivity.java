package com.example.purplediary;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {

    private static final int SPLASH_DELAY = 2000; // Jeda 2 detik

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Pastikan Anda punya layout bernama 'activity_splash.xml'
        // Jika tidak punya, buat file XML kosong saja untuk menghindari error.
        setContentView(R.layout.activity_splash);

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            // Pindah dari SplashActivity ke LanguageActivity
            Intent intent = new Intent(SplashActivity.this, LanguageActivity.class);
            startActivity(intent);
            finish(); // Tutup SplashActivity agar tidak bisa kembali
        }, SPLASH_DELAY);
    }
}
