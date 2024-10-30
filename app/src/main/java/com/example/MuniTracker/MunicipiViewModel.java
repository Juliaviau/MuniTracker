package com.example.MuniTracker;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.List;

public class MunicipiViewModel extends AndroidViewModel {
    private final MunicipiRepository municipiRepository;

    public MunicipiViewModel(@NonNull Application application) {
        super(application);
        municipiRepository = new MunicipiRepository(application);
    }

    public LiveData<List<Municipi>> getMunicipisVisitats() {
        return municipiRepository.getMunicipisVisitats();
    }


    /*public void marcarMunicipiComVisitant(Municipi municipi) {
        municipiRepository.marcarMunicipiComVisitant(municipi);
    }*/
    public void afegirMunicipi(Municipi municipi) {
        municipiRepository.afegirMunicipi(municipi);
    }

    public void afegirVisita(Visita visita) {
        municipiRepository.afegirVisita(visita);
    }

    /*public List<Visita> getVisitesPerMunicipi(String municipiId) {
        return municipiRepository.getVisitesPerMunicipi(municipiId);
    }*/

    public LiveData<List<Visita>> getVisitasByMunicipiId(String municipiId) {
        return municipiRepository.getVisitasByMunicipiId(municipiId); // Asegúrate de tener este método en tu DAO
    }


}
