package com.example.MuniTracker;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface VisitaDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Visita visita);

    @Query("SELECT * FROM visites WHERE municipiId = :municipiId ORDER BY dataVisita DESC")
    LiveData<List<Visita>> getVisitesByMunicipi(String municipiId);
}
