package com.example.MuniTracker;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.lifecycle.Lifecycle;
import androidx.paging.PagingData;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

// Adaptador para el RecyclerView
public class VisitaAdapter extends ListAdapter<Visita, VisitaAdapter.VisitaViewHolder> {

    public VisitaAdapter() {
        super(new VisitaDiffCallback());
    }

    @NonNull
    @Override
    public VisitaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_visita_perfil, parent, false);
        return new VisitaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VisitaViewHolder holder, int position) {
        Visita visita = getItem(position);
        // Vincular los datos de la visita a la vista
        holder.bind(visita);
    }

    // ViewHolder para el RecyclerView
    static class VisitaViewHolder extends RecyclerView.ViewHolder {
        private final TextView dataTextView;
        private final TextView municipiTextView;
        private final ImageView notesIcona;

        public VisitaViewHolder(@NonNull View itemView) {
            super(itemView);
            dataTextView = itemView.findViewById(R.id.datavis);
            municipiTextView = itemView.findViewById(R.id.municipivis);
            notesIcona = itemView.findViewById(R.id.contenotes);
        }

        public void bind(Visita visita) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            dataTextView.setText(sdf.format(new Date(visita.dataVisita)));
            municipiTextView.setText(visita.municipiId);
            notesIcona.setVisibility(!visita.notes.isEmpty() ? View.VISIBLE : View.GONE);

            itemView.setOnClickListener(v -> {
                // Aquí puedes manejar el clic en la vista de la visita, por ejemplo, mostrar un diálogo con más detalles
                // ...
            });
        }
    }

    // DiffCallback para DiffUtil
    static class VisitaDiffCallback extends DiffUtil.ItemCallback<Visita> {
        @Override
        public boolean areItemsTheSame(@NonNull Visita oldItem, @NonNull Visita newItem) {
            // Dos elementos son el mismo si tienen el mismo ID
            return oldItem.visitaId == newItem.visitaId;
        }

        @Override
        public boolean areContentsTheSame(@NonNull Visita oldItem, @NonNull Visita newItem) {
            // Dos elementos tienen el mismo contenido si todos sus campos son iguales
            return oldItem.municipiId.equals(newItem.municipiId) &&
                    oldItem.dataVisita == newItem.dataVisita &&
                    oldItem.notes.equals(newItem.notes);

            //return oldItem.equals(newItem);
        }
    }
}