package com.example.MuniTracker;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "visites")
public class Visita {
    @PrimaryKey(autoGenerate = true)
    public int visitaId;

    @NonNull
    public String municipiId; // ID del municipi
    @NonNull
    public String dataVisita;
    public String notes;

    public Visita(@NonNull String municipiId, String dataVisita, String notes) {
        this.municipiId = municipiId;
        this.dataVisita = dataVisita;
        this.notes = notes;
    }
}
