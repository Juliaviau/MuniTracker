package com.example.MuniTracker;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
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
    int countMunicipisVisitats();
}
