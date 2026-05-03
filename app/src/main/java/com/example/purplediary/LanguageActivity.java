package com.example.purplediary;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class LanguageActivity extends AppCompatActivity {

    private static final int DELAY = 3000; // Jeda 3 detik
    private FusedLocationProviderClient fusedLocationClient;
    private TextView tvGreeting;
    private ImageView ivFlag;

    // Launcher untuk meminta izin lokasi
    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    // Jika izin diberikan, dapatkan lokasi
                    getLocationAndSetGreeting();
                } else {
                    // Jika izin ditolak, gunakan bahasa default HP
                    Toast.makeText(this, "Izin lokasi ditolak, menggunakan bahasa sistem.", Toast.LENGTH_SHORT).show();
                    setGreetingBasedOnSystemLanguage();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_location);

        tvGreeting = findViewById(R.id.tvGreeting);
        ivFlag = findViewById(R.id.ivFlag);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Memulai proses pengecekan lokasi
        checkLocationPermission();
    }

    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            // Jika izin sudah ada, langsung dapatkan lokasi
            getLocationAndSetGreeting();
        } else {
            // Jika belum ada, minta izin ke pengguna
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
        }
    }

    private void getLocationAndSetGreeting() {
        // Cek lagi untuk keamanan (wajib ada)
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
            if (location != null) {
                // Jika lokasi berhasil didapat, ubah jadi nama negara
                Geocoder geocoder = new Geocoder(this, Locale.getDefault());
                try {
                    List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                    if (addresses != null && !addresses.isEmpty()) {
                        String countryCode = addresses.get(0).getCountryCode();
                        setGreetingBasedOnCountry(countryCode);
                    } else {
                        setGreetingBasedOnSystemLanguage(); // Fallback jika negara tidak terdeteksi
                    }
                } catch (IOException e) {
                    setGreetingBasedOnSystemLanguage(); // Fallback jika terjadi error
                }
            } else {
                // Jika lokasi null, pakai bahasa HP
                setGreetingBasedOnSystemLanguage();
            }
        });
    }

    // Fungsi untuk mengatur UI berdasarkan KODE NEGARA (hasil dari GPS)
    private void setGreetingBasedOnCountry(String countryCode) {
        String greeting;
        int flagResource;

        switch (countryCode.toUpperCase()) {
            case "ID": // Indonesia
                greeting = "Halo!";
                flagResource = R.drawable.ic_flag_indonesia;
                break;
            case "JP": // Jepang
                greeting = "Konnichiwa!";
                flagResource = R.drawable.ic_flag_japan;
                break;
            case "US": // Amerika Serikat
            default:   // Negara lain
                greeting = "Hello!";
                flagResource = R.drawable.ic_flag_usa;
                break;
        }
        updateUI(greeting, flagResource);
    }

    // Fungsi LAMA Anda, sekarang jadi cadangan (fallback)
    private void setGreetingBasedOnSystemLanguage() {
        String lang = Locale.getDefault().getLanguage();
        String greeting;
        int flagResource;

        switch (lang) {
            case "in":
                greeting = "Halo!";
                flagResource = R.drawable.ic_flag_indonesia;
                break;
            case "ja":
                greeting = "Konnichiwa!";
                flagResource = R.drawable.ic_flag_japan;
                break;
            default:
                greeting = "Hello!";
                flagResource = R.drawable.ic_flag_usa;
                break;
        }
        updateUI(greeting, flagResource);
    }

    // Fungsi untuk update UI dan pindah activity
    private void updateUI(String greeting, int flagResource){
        tvGreeting.setText(greeting);
        ivFlag.setImageResource(flagResource);

        // Pindah ke HomeActivity setelah jeda
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            Intent intent = new Intent(LanguageActivity.this, HomeActivity.class);
            startActivity(intent);
            finish();
        }, DELAY);
    }
}
