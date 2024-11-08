package com.example.MuniTracker;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;



@Dao
public interface VisitaDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Visita visita);

    @Query("SELECT * FROM visites WHERE municipiId = :municipiId ORDER BY dataVisita DESC")
    LiveData<List<Visita>> getVisitesByMunicipi(String municipiId);



    @Query("SELECT COUNT(*) FROM visites WHERE municipiId = :municipiId")
    LiveData<Integer> getNumeroVisitesByMunicipi(String municipiId);


    @Query("SELECT COUNT(*) FROM visites")
    LiveData<Integer> getNumeroTotalVisites();

    @Delete
    void delete(Visita visita);

    @Update
    void update(Visita visita);

    @Query("SELECT municipiId, COUNT(*) AS visitCount " +
            "FROM visites " + // Make sure this matches the tableName in Visita entity
            "GROUP BY municipiId " +
            "ORDER BY visitCount DESC " +
            "LIMIT 10")
    LiveData<List<MunicipiVisitCount>> getTop10MostVisitedMunicipalities();


}
