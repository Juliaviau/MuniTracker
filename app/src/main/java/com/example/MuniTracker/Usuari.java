package com.example.MuniTracker;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "usuari")
public class Usuari {
    @PrimaryKey
    @NonNull
    public String id;
    public String nom;
}