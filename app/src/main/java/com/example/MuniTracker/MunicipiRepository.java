package com.example.MuniTracker;
import android.app.Application;
import android.os.AsyncTask;

import androidx.lifecycle.LiveData;

import java.util.List;
import java.util.concurrent.Executors;

public class MunicipiRepository {
    private final MunicipiDao municipiDao;
    private final VisitaDao visitaDao;

    public MunicipiRepository(Application application) {
        AppDatabase db = AppDatabase.getInstance(application);
        municipiDao = db.municipiDao();
        visitaDao = db.visitaDao();
    }

    /*public List<Municipi> getMunicipisVisitats() {

        return municipiDao.getMunicipisVisitats();
    }*/
    public LiveData<List<Municipi>> getMunicipisVisitats() {
        return  municipiDao.getMunicipisVisitats(); // Verifica que tu DAO también devuelva LiveData o Flow
    }

    public LiveData<List<Visita>> getVisitasByMunicipiId(String municipiId) {
        return visitaDao.getVisitesByMunicipi(municipiId);
    }
    /*public void marcarMunicipiComVisitant(Municipi municipi) {
        //new UpdateMunicipiAsyncTask(municipiDao).execute(municipi);
        municipiDao.insert(municipi);
    }*/
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

    /*public List<Visita> getVisitesPerMunicipi(String municipiId) {
        return visitaDao.getVisitesByMunicipi(municipiId);
    }*/



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
}
