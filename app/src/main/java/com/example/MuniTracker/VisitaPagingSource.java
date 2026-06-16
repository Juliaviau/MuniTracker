package com.example.MuniTracker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.paging.PagingSource;
import androidx.paging.PagingState;

import com.example.MuniTracker.Entity.Visita;

import kotlin.coroutines.Continuation;

public class VisitaPagingSource extends PagingSource<Integer, Visita> {

    private VisitaDao visitaDao;

    public VisitaPagingSource(VisitaDao visitaDao) {
        this.visitaDao = visitaDao;
    }


   /* public LoadResult<Integer, Visita> load(@NonNull LoadParams<Integer> params) throws IOException {
        try {
            // Obtener la clave de la página actual
            Integer currentKey = params.getKey();
            if (currentKey == null) {
                currentKey = 0; // Si es la primera página, la clave es 0
            }

            // Obtener las visitas de la base de datos
           // List<Visita> visitas = visitaDao.getVisitasPaged(currentKey, params.getLoadSize());

            // Obtener las claves de la página anterior y la siguiente
            Integer prevKey = (currentKey == 0) ? null : currentKey - params.getLoadSize();
            Integer nextKey = (visitas.isEmpty()) ? null : currentKey + params.getLoadSize();

            // Devolver los datos y las claves de las páginas
            return new LoadResult.Page<>(visitas, prevKey, nextKey);
        } catch (Exception e) {
            // Manejar errores
            return new LoadResult.Error<>(e);
        }
    }*/

    @Nullable
    @Override
    public Integer getRefreshKey(@NonNull PagingState<Integer, Visita> state) {
        // Obtener la clave de la página actual para la actualización
        Integer anchorPosition = state.getAnchorPosition();
        if (anchorPosition == null) {
            return null;
        }

        LoadResult.Page<Integer, Visita> anchorPage = state.closestPageToPosition(anchorPosition);
        if (anchorPage == null) {
            return null;
        }

        return anchorPage.getPrevKey() != null ? anchorPage.getPrevKey() : anchorPage.getNextKey();
    }

    @Nullable
    @Override
    public Object load(@NonNull LoadParams<Integer> loadParams, @NonNull Continuation<? super LoadResult<Integer, Visita>> continuation) {
        return null;
    }
}