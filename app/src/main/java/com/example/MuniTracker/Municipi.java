package com.example.MuniTracker;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "municipis")
public class Municipi {
    @PrimaryKey
    @NonNull
    public String id; // Identificador únic del municipi

    public String nom;
    public boolean visitat;

    // Constructor
    public Municipi(@NonNull String id, String nom, boolean visitat) {
        this.id = id;
        this.nom = nom;
        this.visitat = visitat;
    }
}
