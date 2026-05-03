package com.example.purplediary.model;

public class Note {
    private int id;
    private String title;
    private String content;
    private String category; // 👈 Ini dia tambahan kantong buat nyimpen kategori

    // Constructors
    public Note() {
    }

    public Note(String title, String content, String category) {
        this.title = title;
        this.content = content;
        this.category = category;
    }

    public Note(int id, String title, String content, String category) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.category = category;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }
}