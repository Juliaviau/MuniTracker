package com.example.MuniTracker;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.List;
import java.util.concurrent.Executors;

public class MunicipiViewModel extends AndroidViewModel {
    private final MunicipiRepository municipiRepository;

    public MunicipiViewModel(@NonNull Application application) {
        super(application);
        municipiRepository = new MunicipiRepository(application);
    }

    public LiveData<List<Municipi>> obtenirMunicipisVisitats() {
        return municipiRepository.getMunicipisVisitats();
    }

    public void afegirMunicipi(Municipi municipi) {
        municipiRepository.afegirMunicipi(municipi);
    }

    public void afegirVisita(Visita visita) {
        municipiRepository.afegirVisita(visita);
    }

    public LiveData<List<Visita>> getVisitasByMunicipiId(String municipiId) {
        return municipiRepository.getVisitasByMunicipiId(municipiId);
    }

    public LiveData<Integer> obtenirQuantitatMunicipisVisitatsComarca(String comarcaId) {
        MutableLiveData<Integer> porcentaje = new MutableLiveData<>();
        Executors.newSingleThreadExecutor().execute(() -> {
            int resultado = municipiRepository.getPorcentajeVisitadosComarca(comarcaId);
            porcentaje.postValue(resultado);
        });
        return porcentaje;
    }

    public LiveData<Integer> obtenirQuantitatMunicipisVisitatsProvincia(String provinciaId) {
        MutableLiveData<Integer> porcentaje = new MutableLiveData<>();
        Executors.newSingleThreadExecutor().execute(() -> {
            int resultado = municipiRepository.getPorcentajeVisitadosProvincia(provinciaId);
            porcentaje.postValue(resultado);
        });
        return porcentaje;
    }

    public LiveData<Integer> obtenirQuantitatMunicipisVisitatsVegueria(String vegueriaId) {
        MutableLiveData<Integer> porcentaje = new MutableLiveData<>();
        Executors.newSingleThreadExecutor().execute(() -> {
            int resultado = municipiRepository.getPorcentajeVisitadosVegueria(vegueriaId);
            porcentaje.postValue(resultado);
        });
        return porcentaje;
    }

    public LiveData<Integer> nombreMunicipisVisitats() {
        MutableLiveData<Integer> porcentaje = new MutableLiveData<>();
        Executors.newSingleThreadExecutor().execute(() -> {
            int resultado = municipiRepository.nombreMunicipisVisitats();
            porcentaje.postValue(resultado);
        });
        return porcentaje;
    }

    public void updateMunicipi(Municipi municipi) {
        municipiRepository.updateMunicipi(municipi);
    }

    public void insertVisita(Visita visita) {
        municipiRepository.insertVisita(visita);
    }

    public LiveData<List<MunicipiVisitCount>> getTop10MostVisitedMunicipalities() {
        return municipiRepository.getTop10MostVisitedMunicipalities();
    }

    public LiveData<List<ComarcaVisitCount>> getTop5MostVisitedComarques() {
        return municipiRepository.getTop5MostVisitedComarques();
    }

    public LiveData<List<ComarcaVisitCount>> getTop3MostVisitedVegueries() {
        return municipiRepository.getTop3MostVisitedVegueries();
    }

    public LiveData<List<ComarcaVisitCount>> getTop3MostVisitedProvincies() {
        return municipiRepository.getTop3MostVisitedProvincies();
    }

}
