package com.example.MuniTracker;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "municipis")
public class Municipi {
    @PrimaryKey
    @NonNull
    public String id; // Identificador únic del municipi

    public String nom;
    public boolean visitat;

    @ColumnInfo(name = "comarca_id")
    public String comarcaId;

    @ColumnInfo(name = "provincia_id")
    public String provinciaId;

    @ColumnInfo(name = "vegueria_id")
    public String vegueriaId;

    // Constructor
    public Municipi(@NonNull String id, String nom, boolean visitat, String comarcaId, String vegueriaId, String provinciaId) {
        this.id = id;
        this.nom = nom;
        this.visitat = visitat;
        this.comarcaId = comarcaId;
        this.vegueriaId = vegueriaId;
        this.provinciaId = provinciaId;
    }
}
