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

    @Query("SELECT * FROM visites ORDER BY dataVisita DESC")
    LiveData<List<Visita>> getAllVisitsOrderByData();


    @Query("SELECT COUNT(*) FROM visites WHERE municipiId = :municipiId")
    LiveData<Integer> getNumeroVisitesByMunicipi(String municipiId);

    @Query("SELECT COUNT(*) FROM visites WHERE municipiId = :municipiId")
    int getNumeroVisitesByMunicipiSync(String municipiId);



    @Query("SELECT COUNT(*) FROM visites")
    LiveData<Integer> getNumeroTotalVisites();

    @Query("DELETE FROM visites")
    void eliminarTotesVisites();

    @Delete
    void delete(Visita visita);

    @Update
    void update(Visita visita);

    @Query("SELECT municipiId, COUNT(*) AS visitCount " +
            "FROM visites " +
            "GROUP BY municipiId " +
            "ORDER BY visitCount DESC " +
            "LIMIT 10")
    LiveData<List<MunicipiVisitCount>> getTop10MostVisitedMunicipalities();

    @Query("SELECT m.comarca_id AS comarcaID, COUNT(*) AS visitCount " +
            "FROM visites v " +
            "INNER JOIN municipis m ON v.municipiId = m.id " +
            "GROUP BY m.comarca_id " +
            "ORDER BY visitCount DESC " +
            "LIMIT 5")
    LiveData<List<ComarcaVisitCount>> getTop5MostVisitedComarques();

    @Query("SELECT m.vegueria_id AS comarcaID, COUNT(*) AS visitCount " +
            "FROM visites v " +
            "INNER JOIN municipis m ON v.municipiId = m.id " +
            "GROUP BY m.vegueria_id " +
            "ORDER BY visitCount DESC " +
            "LIMIT 3")
    LiveData<List<ComarcaVisitCount>> getTop3MostVisitedVegueries();

    @Query("SELECT m.provincia_id AS comarcaID, COUNT(*) AS visitCount " +
            "FROM visites v " +
            "INNER JOIN municipis m ON v.municipiId = m.id " +
            "GROUP BY m.provincia_id " +
            "ORDER BY visitCount DESC " +
            "LIMIT 3")
    LiveData<List<ComarcaVisitCount>> getTop3MostVisitedProvincies();


}
