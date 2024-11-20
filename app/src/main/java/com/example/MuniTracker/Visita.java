package com.example.MuniTracker;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "visites")
public class Visita {
    @PrimaryKey(autoGenerate = true)
    public int visitaId;

    @NonNull
    @ColumnInfo(name = "municipiId")
    public String municipiId; // ID del municipi
    public long dataVisita;
    public String notes;

    public Visita(@NonNull String municipiId, long dataVisita, String notes) {
        this.municipiId = municipiId;
        this.dataVisita = dataVisita;
        this.notes = notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
    public void setDataVisita(long dataVisita) {
        this.dataVisita = dataVisita;
    }
}
