package com.example.purplediary.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.purplediary.R;
import com.example.purplediary.model.Note;

import java.util.List;

public class NoteAdapter extends RecyclerView.Adapter<NoteAdapter.NoteViewHolder> {

    private List<Note> noteList;
    private OnNoteClickListener listener;

    public interface OnNoteClickListener {
        void onNoteClick(Note note);
        void onNoteLongClick(Note note);
    }

    public NoteAdapter(List<Note> noteList, OnNoteClickListener listener) {
        this.noteList = noteList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_note, parent, false);
        return new NoteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NoteViewHolder holder, int position) {
        Note currentNote = noteList.get(position);

        holder.tvTitle.setText(currentNote.getTitle());
        holder.tvContent.setText(currentNote.getContent());

        // Menampilkan Kategori dari AI (Kalau kosong, set default)
        String category = currentNote.getCategory();
        if (category == null || category.isEmpty()) {
            category = "LAINNYA";
        }
        holder.tvCategory.setText(category);

        holder.itemView.setOnClickListener(v -> listener.onNoteClick(currentNote));
        holder.itemView.setOnLongClickListener(v -> {
            listener.onNoteLongClick(currentNote);
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return noteList.size();
    }

    public void updateData(List<Note> newNoteList) {
        this.noteList = newNoteList;
        notifyDataSetChanged();
    }

    public static class NoteViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvContent, tvCategory; // Tambah tvCategory di sini

        public NoteViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvItemTitle);
            tvContent = itemView.findViewById(R.id.tvItemContent);
            tvCategory = itemView.findViewById(R.id.tvItemCategory); // Hubungkan ke XML
        }
    }
}