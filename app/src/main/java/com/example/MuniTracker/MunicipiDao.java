package com.example.MuniTracker;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface MunicipiDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Municipi municipi);

    @Query("SELECT * FROM municipis WHERE visitat = 1")
    LiveData<List<Municipi>> getMunicipisVisitats();

    @Query("UPDATE municipis SET visitat = :visitat WHERE id = :id")
    void updateVisitStatus(String id, boolean visitat);

    @Query("SELECT COUNT(*) FROM municipis WHERE visitat = 1")
    int nombreMunicipisVisitats();

    @Query("SELECT COUNT(*) FROM municipis WHERE comarca_id = :comarcaId")
    int getTotalMunicipisInComarca(String comarcaId);

    @Query("SELECT COUNT(*) FROM municipis WHERE comarca_id = :comarcaId AND visitat = 1")
    int getVisitadosMunicipisInComarca(String comarcaId);

    @Query("SELECT COUNT(*) FROM municipis WHERE provincia_id = :provinciaId AND visitat = 1")
    int getVisitadosMunicipisInProvincia(String provinciaId);

    @Query("SELECT COUNT(*) FROM municipis WHERE vegueria_id = :vegueriaId AND visitat = 1")
    int getVisitadosMunicipisInVegueria(String vegueriaId);

    @Query("DELETE FROM municipis WHERE id = :municipi")
    void eliminarMunicipi(String municipi);

    /*default int getPorcentajeVisitadosInComarca(String comarcaId) {

        int total = getTotalMunicipisInComarca(comarcaId);
        int visitados = getVisitadosMunicipisInComarca(comarcaId);
        //Log.d("MUNUCUOU", "total " + total + " visi " + visitados + " com " + comarcaId);
        return visitados;
        //return total == 0 ? 0 : (visitados * 100) / total;
    }*/



}
