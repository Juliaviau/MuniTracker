package com.example.MuniTracker;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class SearchAdapter extends RecyclerView.Adapter<SearchAdapter.ViewHolder> {
    private List<String> municipis;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(String municipi);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    // Constructor per inicialitzar la llista
    public SearchAdapter(List<String> municipis) {
        this.municipis = municipis;
    }

    // Mètode per actualitzar la llista
    public void updateList(List<String> newMunicipis) {
        //this.municipis = newMunicipis;
        this.municipis =  (newMunicipis.size() > 5) ? newMunicipis.subList(0, 5): newMunicipis;

        notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(android.R.layout.simple_list_item_1, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        String municipi = municipis.get(position);
        holder.textView.setText(municipi);
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(municipi);
            }
        });
    }

    @Override
    public int getItemCount() {
        return municipis.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView textView;

        public ViewHolder(View itemView) {
            super(itemView);
            textView = itemView.findViewById(android.R.id.text1);
        }
    }
}
