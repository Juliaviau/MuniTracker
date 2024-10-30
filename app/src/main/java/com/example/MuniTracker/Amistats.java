package com.example.MuniTracker;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "amic")
public class Amistats {
    @PrimaryKey(autoGenerate = true)
    public int id;
    public String usuariId;
    public String amicId;
}