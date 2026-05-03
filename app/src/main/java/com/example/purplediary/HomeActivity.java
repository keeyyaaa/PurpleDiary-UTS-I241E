package com.example.purplediary;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.purplediary.adapter.NoteAdapter;
import com.example.purplediary.db.DatabaseHelper;
import com.example.purplediary.model.Note;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class HomeActivity extends AppCompatActivity {

    private RecyclerView rvNotes;
    private NoteAdapter noteAdapter;
    private DatabaseHelper dbHelper;
    private TextView tvNoteCount;

    // 1. TAMBAHAN: Variabel untuk Dropdown dan List Penyimpan Semua Catatan
    private Spinner spinnerFilter;
    private List<Note> allNotesMasterList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        dbHelper = new DatabaseHelper(this);
        allNotesMasterList = new ArrayList<>(); // Inisialisasi list master

        tvNoteCount = findViewById(R.id.tvNoteCount);
        rvNotes = findViewById(R.id.rvNotes);
        FloatingActionButton fabAddNote = findViewById(R.id.fabAddNote);

        // 2. TAMBAHAN: Hubungkan Spinner dari XML ke Java
        spinnerFilter = findViewById(R.id.spinnerFilter);

        noteAdapter = new NoteAdapter(new ArrayList<>(), new NoteAdapter.OnNoteClickListener() {
            @Override
            public void onNoteClick(Note note) {
                showOptionsDialog(note);
            }

            @Override
            public void onNoteLongClick(Note note) {
                showOptionsDialog(note);
            }
        });

        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(this, 2);
        rvNotes.setLayoutManager(layoutManager);
        rvNotes.setAdapter(noteAdapter);

        // 3. TAMBAHAN: Panggil fungsi untuk menyiapkan isi Dropdown
        setupDropdown();

        fabAddNote.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, AddNoteActivity.class);
            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadNotesFromDatabase();
    }

    // =================================================================
    // FUNGSI BARU: Menyiapkan Pilihan Kategori di Dropdown
    // =================================================================
    private void setupDropdown() {
        String[] categories = {"Semua Kategori", "RESEP", "CURHAT", "PENGINGAT", "LAINNYA"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, categories);
        spinnerFilter.setAdapter(adapter);

        // Aksi ketika salah satu kategori dipilih
        spinnerFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                filterNotes(categories[position]);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    // =================================================================
    // FUNGSI BARU: Menyaring Catatan Berdasarkan Kategori
    // =================================================================
    private void filterNotes(String selectedCategory) {
        if (selectedCategory.equals("Semua Kategori")) {
            // Tampilkan semua
            noteAdapter.updateData(allNotesMasterList);
            tvNoteCount.setText(String.format(Locale.getDefault(), "%d catatan", allNotesMasterList.size()));
        } else {
            // Buat list baru khusus untuk kategori yang dipilih
            List<Note> filteredList = new ArrayList<>();
            for (Note n : allNotesMasterList) {
                if (n.getCategory() != null && n.getCategory().equalsIgnoreCase(selectedCategory)) {
                    filteredList.add(n);
                }
            }
            // Update layar dengan list yang sudah disaring
            noteAdapter.updateData(filteredList);
            tvNoteCount.setText(String.format(Locale.getDefault(), "%d catatan (%s)", filteredList.size(), selectedCategory));
        }
    }

    private void loadNotesFromDatabase() {
        // Simpan semua data dari database ke Master List
        allNotesMasterList = dbHelper.getAllNotes();

        // Cek kategori apa yang sedang dipilih di dropdown saat ini
        int currentSelection = spinnerFilter.getSelectedItemPosition();
        if (currentSelection == AdapterView.INVALID_POSITION) {
            currentSelection = 0;
        }
        String currentCat = spinnerFilter.getItemAtPosition(currentSelection).toString();

        // Saring datanya sesuai pilihan terakhir
        filterNotes(currentCat);
    }

    private void showOptionsDialog(Note note) {
        String[] options = {"✏️ Edit Catatan", "🗑️ Hapus Catatan"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Mau diapain nih?");
        builder.setItems(options, (dialog, which) -> {
            if (which == 0) {
                goToEditPage(note);
            } else if (which == 1) {
                confirmDelete(note);
            }
        });
        builder.show();
    }

    private void goToEditPage(Note note) {
        Intent intent = new Intent(HomeActivity.this, AddNoteActivity.class);
        intent.putExtra("IS_EDIT", true);
        intent.putExtra("NOTE_ID", note.getId());
        intent.putExtra("NOTE_TITLE", note.getTitle());
        intent.putExtra("NOTE_CONTENT", note.getContent());
        startActivity(intent);
    }

    private void confirmDelete(Note note) {
        new AlertDialog.Builder(this)
                .setTitle("Hapus Catatan?")
                .setMessage("Yakin mau menghapus catatan yang ini?")
                .setPositiveButton("Hapus", (dialog, which) -> {
                    dbHelper.deleteNote(note.getId());
                    Toast.makeText(this, "Catatan berhasil dihapus! ✨", Toast.LENGTH_SHORT).show();
                    loadNotesFromDatabase();
                })
                .setNegativeButton("Batal", null)
                .show();
    }
}