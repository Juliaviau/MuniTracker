package com.example.MuniTracker;
import android.app.Application;
import android.os.AsyncTask;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import kotlinx.coroutines.Dispatchers;

public class MunicipiRepository implements Observer<Integer> {
    private final MunicipiDao municipiDao;
    private final VisitaDao visitaDao;

    public MunicipiRepository(Application application) {
        AppDatabase db = AppDatabase.getInstance(application);
        municipiDao = db.municipiDao();
        visitaDao = db.visitaDao();
    }

    public void eliminarTotMunicipiVisites() {
        Executors.newSingleThreadExecutor().execute(() -> {
            visitaDao.eliminarTotesVisites();
            municipiDao.eliminarTotsMunicipis();
        });
    }


    public LiveData<List<Municipi>> getMunicipisVisitats() {
        return  municipiDao.getMunicipisVisitats(); // Verifica que tu DAO también devuelva LiveData o Flow
    }

    public LiveData<List<Visita>> getVisitasByMunicipiId(String municipiId) {
        return visitaDao.getVisitesByMunicipi(municipiId);
    }

    public LiveData<List<Visita>> getAllVisitsOrderByData() {
        return visitaDao.getAllVisitsOrderByData();
    }


    public void afegirMunicipi(Municipi municipi) {
        Executors.newSingleThreadExecutor().execute(() -> {
            municipiDao.insert(municipi);
        });
    }

    public void afegirVisita(Visita visita) {
        Executors.newSingleThreadExecutor().execute(() -> {
            visitaDao.insert(visita);
        });
    }

    @Override
    public void onChanged(Integer integer) {

    }

    private static class UpdateMunicipiAsyncTask extends AsyncTask<Municipi, Void, Void> {
        private final MunicipiDao municipiDao;

        private UpdateMunicipiAsyncTask(MunicipiDao municipiDao) {
            this.municipiDao = municipiDao;
        }

        @Override
        protected Void doInBackground(Municipi... municipis) {
            municipiDao.updateVisitStatus(municipis[0].id, true);
            return null;
        }
    }

    private static class InsertVisitaAsyncTask extends AsyncTask<Visita, Void, Void> {
        private final VisitaDao visitaDao;

        private InsertVisitaAsyncTask(VisitaDao visitaDao) {
            this.visitaDao = visitaDao;
        }

        @Override
        protected Void doInBackground(Visita... visitas) {
            visitaDao.insert(visitas[0]);
            return null;
        }
    }


    public int getPorcentajeVisitadosComarca(String comarcaId) {
        return municipiDao.getVisitadosMunicipisInComarca(comarcaId);
    }

    public int getPorcentajeVisitadosProvincia(String provinciaId) {
        return municipiDao.getVisitadosMunicipisInProvincia(provinciaId);
    }

    public int getPorcentajeVisitadosVegueria(String vegueriaId) {
        return municipiDao.getVisitadosMunicipisInVegueria(vegueriaId);
    }

    public int nombreMunicipisVisitats() {
        return municipiDao.nombreMunicipisVisitats();
    }


    public void updateMunicipi(Municipi municipi) {
        new UpdateMunicipiAsyncTask(municipiDao).execute(municipi);
    }

    public void insertVisita(Visita visita) {
        new InsertVisitaAsyncTask(visitaDao).execute(visita);
    }

    public LiveData<Integer> getNumeroVisitesByMunicipi(String municipiId) {
        return visitaDao.getNumeroVisitesByMunicipi(municipiId);
    }

    private final MutableLiveData<Boolean> visitaEliminada = new MutableLiveData<>(false);

    public LiveData<Boolean> getVisitaEliminada() {
        return visitaEliminada;
    }

    public void setVisitaEliminada(Boolean value) {
        visitaEliminada.setValue(value);
    }

    public void deleteVisita(Visita visita) {
        new Thread(() -> {
            //visitaDao.delete(visita);

            // Comprobar si quedan visitas para el municipio
            int numeroVisites = visitaDao.getNumeroVisitesByMunicipiSync(visita.municipiId);
            Log.i("qqqqqq", String.valueOf(numeroVisites));
            // Si no quedan visitas, marcar el municipio como no visitado
            if (numeroVisites == 1) {
                municipiDao.eliminarMunicipi(visita.municipiId);
                //municipiDao.updateVisitStatus(visita.municipiId,false);
                visitaDao.delete(visita);
                visitaEliminada.postValue(true);

                Log.i("qqqqqq visites 1, mun no visitat", String.valueOf(numeroVisites));
            } else {
                visitaDao.delete(visita);
                visitaEliminada.postValue(true);

                Log.i("qqqqqq municipi encara visitat", String.valueOf(numeroVisites));
            }
            visitaEliminada.postValue(true);
        }).start();
    }

    public void eliminarMunicipi(String municipi) {
        new Thread(() -> {
            municipiDao.eliminarMunicipi(municipi);
        });
    }



    public LiveData<List<MunicipiVisitCount>> getTop10MostVisitedMunicipalities() {
        return visitaDao.getTop10MostVisitedMunicipalities();
    }
    public LiveData<List<ComarcaVisitCount>> getTop5MostVisitedComarques() {
        return visitaDao.getTop5MostVisitedComarques();
    }
    public LiveData<List<ComarcaVisitCount>> getTop3MostVisitedVegueries() {
        return visitaDao.getTop3MostVisitedVegueries();
    }
    public LiveData<List<ComarcaVisitCount>> getTop3MostVisitedProvincies() {
        return visitaDao.getTop3MostVisitedProvincies();
    }




}
