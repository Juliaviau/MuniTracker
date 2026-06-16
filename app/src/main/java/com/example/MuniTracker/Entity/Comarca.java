package com.example.MuniTracker.Entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "comarcas")
public class Comarca {
    @PrimaryKey
    @NonNull
    public String id;
    public String nombre;
}