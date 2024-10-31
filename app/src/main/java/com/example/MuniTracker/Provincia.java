package com.example.MuniTracker;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "provincias")
public class Provincia {
    @PrimaryKey @NonNull
    public String id;
    public String nombre;
}