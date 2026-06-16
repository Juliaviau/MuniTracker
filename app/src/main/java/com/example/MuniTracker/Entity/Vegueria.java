package com.example.MuniTracker.Entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "vegueries")
public class Vegueria {
    @PrimaryKey @NonNull
    public String id;
    public String nombre;
}