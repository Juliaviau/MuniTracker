package com.example.MuniTracker;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.SearchView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.MuniTracker.databinding.FragmentMapesBinding;
import com.example.MuniTracker.databinding.FragmentPerfilBinding;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;


public class FragmentPerfil extends Fragment {

    // Veure totes les visites ordenades segons data
    // Eliminar totes les dades
    // ??

    FragmentPerfilBinding binding;
    private Context context;

    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
    }

    public FragmentPerfil() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = FragmentPerfilBinding.inflate(getLayoutInflater());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Activity activity = getActivity();
            if (activity != null) {
                Window window = activity.getWindow();
                window.setStatusBarColor(ContextCompat.getColor(activity, R.color.fons));
                View decorView = window.getDecorView();
                decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_perfil, container, false);

        MunicipiViewModel viewModel = new ViewModelProvider(this).get(MunicipiViewModel.class);
        LinearLayout visitesContainer = view.findViewById(R.id.visitasContainer);
        SearchView searchView = view.findViewById(R.id.searchViewperfil);

        viewModel.getAllVisitsOrderByData().observe(getViewLifecycleOwner(), visites -> {
            List<Visita> filteredList = new ArrayList<>(visites);

            // Funció per actualitzar la vista
            Runnable updateVisites = () -> {
                visitesContainer.removeAllViews();

                if (filteredList.isEmpty()) {
                    TextView visitaTextView = new TextView(context);
                    visitaTextView.setText("No hi ha cap visita per mostrar");
                    visitaTextView.setPadding(16, 16, 16, 16);
                    visitaTextView.setTextSize(16);
                    visitaTextView.setGravity(Gravity.CENTER);
                    visitaTextView.setTextColor(ContextCompat.getColor(context, R.color.black));
                    visitaTextView.setTypeface(null, Typeface.ITALIC);

                    LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                    );
                    layoutParams.gravity = Gravity.CENTER;
                    layoutParams.setMargins(8, 8, 8, 8);
                    visitaTextView.setLayoutParams(layoutParams);

                    visitesContainer.addView(visitaTextView);
                } else {
                    for (Visita visita : filteredList) {
                        View visitaView = LayoutInflater.from(context).inflate(R.layout.item_visita_perfil, visitesContainer, false);

                        TextView dataTextView = visitaView.findViewById(R.id.datavis);
                        TextView municipiTextView = visitaView.findViewById(R.id.municipivis);
                        ImageView contenotes = visitaView.findViewById(R.id.contenotes);

                        if (visita.notes.equals("")) {
                            contenotes.setVisibility(View.GONE);
                        } else {
                            contenotes.setVisibility(View.VISIBLE);
                        }

                        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                        String formattedDate = sdf.format(new Date(visita.dataVisita));

                        dataTextView.setText(formattedDate);
                        municipiTextView.setText(visita.municipiId);

                        visitaView.setOnClickListener(v -> {
                            showNotasDialog(visita);
                        });
                        visitesContainer.addView(visitaView);
                    }
                }
            };

            // Filtrar visites amb el SearchView
            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {
                    return false;
                }

                @Override
                public boolean onQueryTextChange(String newText) {
                    filteredList.clear();
                    for (Visita visita : visites) {
                        if (visita.municipiId.toLowerCase().contains(newText.toLowerCase())) {
                            filteredList.add(visita);
                        }
                    }
                    updateVisites.run();
                    return true;
                }
            });

            // Inicialitzar la vista
            updateVisites.run();
        });

        Button eliminarTot = view.findViewById(R.id.eliminarTot);
        eliminarTot.setOnClickListener(v -> {
            new AlertDialog.Builder(context)
                    .setTitle("Confirmació per eliminar totes les dades")
                    .setMessage("Estàs segur que vols eliminar totes les dades? Aquesta acció no es pot desfer i les dades no es podran recuperar.")
                    .setPositiveButton("Eliminar", (dialog, which) -> {
                        viewModel.eliminarTotMunicipiVisites();
                        Toast.makeText(context, "Dades eliminades correctament", Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton("Cancel·lar", (dialog, which) -> {
                        dialog.dismiss();
                    })
                    .show();
        });

        return view;
    }

    private void showNotasDialog(Visita visita) {

        View view = getLayoutInflater().inflate(R.layout.dialog_visita, null);

        TextView titolNotaTextView = view.findViewById(R.id.titolnota);
        titolNotaTextView.setText("Visita a " + visita.municipiId);

        TextView dataVisitaTextView = view.findViewById(R.id.datavisita);

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        String formattedDate = sdf.format(new Date(visita.dataVisita));
        dataVisitaTextView.setText(formattedDate);

        TextView notesTextView = view.findViewById(R.id.succesdesc);
        notesTextView.setText(visita.notes);

        ScrollView scrollView = view.findViewById(R.id.scrollView);

        final int midaMaxima = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 150, getResources().getDisplayMetrics());

        notesTextView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                notesTextView.getViewTreeObserver().removeOnGlobalLayoutListener(this);

                if (notesTextView.getHeight() > midaMaxima) {
                    scrollView.getLayoutParams().height = midaMaxima;
                } else if (visita.notes.equals("")) {
                    notesTextView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                    scrollView.getLayoutParams().height = notesTextView.getHeight() + 100;
                    notesTextView.setText("No hi ha notes guardades");
                } else {
                    scrollView.getLayoutParams().height = notesTextView.getHeight();
                }
                scrollView.requestLayout();
            }
        });

        ImageButton elimboto = view.findViewById(R.id.btnEliminar);
        ImageButton editboto = view.findViewById(R.id.btnModificar);
        MunicipiViewModel viewModel = new ViewModelProvider(this).get(MunicipiViewModel.class);

        AppCompatImageButton tancarButton = view.findViewById(R.id.btntancar);

        //On es mostra?
        ProgressDialog progressDialog = new ProgressDialog(context);
        progressDialog.setMessage("Eliminant...");
        progressDialog.setCancelable(false);

        AlertDialog dialog = new AlertDialog.Builder(context)
                .setView(view)
                .create();

        editboto.setOnClickListener(v-> {

        });

        elimboto.setOnClickListener(v -> {
            progressDialog.show();
            viewModel.deleteVisita(visita);
            viewModel.getVisitaEliminada().observe(getViewLifecycleOwner(), eliminada -> {

                if (eliminada) {
                    progressDialog.dismiss();
                    Toast.makeText(context, "Visita eliminada correctament", Toast.LENGTH_SHORT).show();
                    viewModel.setVisitaEliminada(false);
                    dialog.dismiss();

                } else {
                    Toast.makeText(context, "No s'ha pogut eliminar la visita", Toast.LENGTH_SHORT).show();
                }
            });
            // bottomSheetDialog.dismiss();
        });
        tancarButton.setOnClickListener(v -> dialog.dismiss());

        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.show();
    }
}