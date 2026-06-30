package com.example.MuniTracker.Fragments;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.SearchView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.util.Log;
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
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.MuniTracker.MunicipiViewModel;
import com.example.MuniTracker.R;
import com.example.MuniTracker.Entity.Visita;
import com.example.MuniTracker.VisitaDialogFragment;
import com.example.MuniTracker.databinding.FragmentPerfilBinding;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class FragmentPerfil extends Fragment {

    private FragmentPerfilBinding binding;
    private static Context context;
    private MunicipiViewModel viewModel;
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
    }

    public FragmentPerfil() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        android.content.SharedPreferences prefs = context.getSharedPreferences("ConfigApp", android.content.Context.MODE_PRIVATE);
        int indexPaleta = prefs.getInt("paleta_seleccionada", 0);


        switch (indexPaleta) {
            case 0: context.setTheme(R.style.Tema_Paleta_Original); break;
            case 1: context.setTheme(R.style.Tema_Paleta_Ocea); break;
            case 2: context.setTheme(R.style.Tema_Paleta_Bosc); break;
            case 3: context.setTheme(R.style.Tema_Paleta_Magenta); break;
        }

        super.onCreate(savedInstanceState);
       // binding = FragmentPerfilBinding.inflate(getLayoutInflater());
        configurarStatusBar();
        viewModel = new ViewModelProvider(this).get(MunicipiViewModel.class);
    }


    private void configurarStatusBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Activity activity = getActivity();
            if (activity != null) {
                Window window = activity.getWindow();
                window.setStatusBarColor(ContextCompat.getColor(activity, R.color.fons));
                window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            }
        }
    }

    //*********************
    //fer servir paging 3
    //*********************
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentPerfilBinding.inflate(inflater, container, false);
        //View view = inflater.inflate(R.layout.fragment_perfil, container, false);
        View view = binding.getRoot();

        LinearLayout visitesListContainer = view.findViewById(R.id.visitasContainer);
        SearchView searchView = view.findViewById(R.id.searchViewperfil);
       // Button eliminarTot = view.findViewById(R.id.eliminarTot);

        dadescurioses(viewModel);


        observarVisites(viewModel, visitesListContainer, searchView);
       // configurarBotonEliminarTot(eliminarTot, viewModel);
        return view;
    }

    private void dadescurioses(MunicipiViewModel perfilViewModel) {
        // Dins de la zona on reps la llista de visites a la teva View
        // Enllacem els elements de la targeta superior

// AL TEU FRAGMENT (Dins d'on demanes les fites)

        perfilViewModel.getMunicipiTalisma().observe(getActivity(), municipi -> {
            Log.d("DEBUG_UI", "Pintant talismà: " + municipi);
            binding.textMunicipiTalisma.setText(municipi);
        });

        perfilViewModel.getUltimaConquesta().observe(getActivity(), municipi -> {
            Log.d("DEBUG_UI", "Pintant última conquesta: " + municipi);
            binding.textUltimaConquesta.setText(municipi);
        });

        perfilViewModel.getComptadorMunicipisUnics().observe(getActivity(), total -> {
            Log.d("DEBUG_UI", "Pintant total municipis: " + total);
            final int TOTAL_MUNICIPIS_CAT = 947;

            // Apaguem totes les medalles per defecte
            binding.medallaRookie.setTextColor(android.graphics.Color.parseColor("#A5A5A5"));
            binding.medallaNomada.setTextColor(android.graphics.Color.parseColor("#A5A5A5"));
            binding.medallaExplorador.setTextColor(android.graphics.Color.parseColor("#A5A5A5"));
            binding.medallaMestre.setTextColor(android.graphics.Color.parseColor("#A5A5A5"));

            // Calculem el percentatge real del país completat
            double percentatgeTotalPaisd = (total * 100.0) / TOTAL_MUNICIPIS_CAT;
            String percentatgeTotalPais = String.format(Locale.getDefault(), "%.1f", percentatgeTotalPaisd);

            Log.d("DEBUG_UI", "Pintant percentatge: " + percentatgeTotalPais + "%");

            binding.textProgresSeguentRang.setText(percentatgeTotalPais+"% de Catalunya completat");
            if (total == null || total == 0) {
                binding.textRangViatger.setText("Començant");
                binding.textProgresSeguentRang.setText("0% de Catalunya completat. Visita un municipi!");
                binding.progressRang.setProgress(0);

            } else if (total < 10) {
                binding.textRangViatger.setText("Rookie Urbà");
                int restants = 10 - total;
                binding.textProgresSeguentRang.setText(percentatgeTotalPais + "% del mapa. Falten " + restants + " per a Local");
                binding.progressRang.setProgress((total * 100) / 10);

            } else if (total < 50) {
                binding.textRangViatger.setText("Viatger Local");
                binding.medallaRookie.setTextColor(android.graphics.Color.parseColor("#CD7F32")); // 🥉 Bronze

                int restants = 50 - total;
                binding.textProgresSeguentRang.setText(percentatgeTotalPais + "% del mapa. Falten " + restants + " per a Nòmada");
                // Progrés relatiu entre 10 i 50 municipis
                binding.progressRang.setProgress(((total - 10) * 100) / 40);

            } else if (total < 200) {
                binding.textRangViatger.setText("Nòmada Comarcal");
                binding.medallaRookie.setTextColor(android.graphics.Color.parseColor("#CD7F32")); // 🥉
                binding.medallaNomada.setTextColor(android.graphics.Color.parseColor("#9E9E9E"));  // 🥈 Plata

                int restants = 200 - total;
                binding.textProgresSeguentRang.setText(percentatgeTotalPais + "% del mapa. Falten " + restants + " per a Explorador");
                binding.progressRang.setProgress(((total - 50) * 100) / 150);

            } else if (total < 500) {
                binding.textRangViatger.setText("Explorador Expert");
                binding.medallaRookie.setTextColor(android.graphics.Color.parseColor("#CD7F32")); // 🥉
                binding.medallaNomada.setTextColor(android.graphics.Color.parseColor("#9E9E9E"));  // 🥈
                binding.medallaExplorador.setTextColor(android.graphics.Color.parseColor("#FFD700")); // 🥇 Or

                int restants = 500 - total;
                binding.textProgresSeguentRang.setText(percentatgeTotalPais + "% del mapa. Falten " + restants + " per a Mestre");
                binding.progressRang.setProgress(((total - 200) * 100) / 300);

            } else {
                binding.textRangViatger.setText("Mestre de la Terra");
                binding.medallaRookie.setTextColor(android.graphics.Color.parseColor("#CD7F32")); // 🥉
                binding.medallaNomada.setTextColor(android.graphics.Color.parseColor("#9E9E9E"));  // 🥈
                binding.medallaExplorador.setTextColor(android.graphics.Color.parseColor("#FFD700")); // 🥇
                binding.medallaMestre.setTextColor(android.graphics.Color.parseColor("#E91E63"));     // 👑 Rosa imperial / Corona

                binding.textProgresSeguentRang.setText("Ets una llegenda! " + percentatgeTotalPais + "% de Catalunya conquerit!");
                binding.progressRang.setProgress(100);
            }
        });







        /*visitesListContainer.getAllVisitsOrderByData().observe(getViewLifecycleOwner(), visites -> {
            List<Visita> filteredList = new ArrayList<>(visites);

            if (!filteredList.isEmpty()) {

                if (filteredList.size() < 5 ) {
                    binding.textRangViatger.setText("Rookie");
                } else if (filteredList.size() < 15) {
                    binding.textRangViatger.setText("Nòmada");
                } else {
                    binding.textRangViatger.setText("Explorador");
                }
                // 2. Trobar el municipi més repetit (Talismà)
                java.util.HashMap<String, Integer> comptadorMunicipis = new java.util.HashMap<>();
                String municipiMesVisitat = filteredList.get(0).municipiId;
                int maxVisites = 0;

                for (Visita v : filteredList) {
                    int count = comptadorMunicipis.getOrDefault(v.municipiId, 0) + 1;
                    comptadorMunicipis.put(v.municipiId, count);
                    if (count > maxVisites) {
                        maxVisites = count;
                        municipiMesVisitat = v.municipiId;
                    }
                }
                binding.textMunicipiTalisma.setText(municipiMesVisitat);
            } else {
                binding.textRangViatger.setText("Principiant");
                binding.textMunicipiTalisma.setText("Cap");
            }

        });*/
    }

    private void observarVisites(MunicipiViewModel viewModel, LinearLayout visitesListContainer, SearchView searchView) {
        viewModel.getAllVisitsOrderByData().observe(getViewLifecycleOwner(), visites -> {
            List<Visita> filteredList = new ArrayList<>(visites);

            Runnable updateVisites = () -> actualizarVistaVisites(filteredList, visitesListContainer);
            configurarSearchView(searchView, visites, filteredList, updateVisites);
            updateVisites.run();
        });
    }

    private void configurarSearchView(SearchView searchView, List<Visita> visites, List<Visita> filteredList, Runnable updateVisites) {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filteredList.clear();
                filteredList.addAll(visites.stream()
                        .filter(visita -> visita.municipiId.toLowerCase().contains(newText.toLowerCase()))
                        .toList());
                updateVisites.run();
                return true;
            }
        });
    }

    private void actualizarVistaVisites(List<Visita> filteredList, LinearLayout visitesListContainer) {
        visitesListContainer.removeAllViews();
        if (filteredList.isEmpty()) {
            mostrarMensajeSinVisitas(visitesListContainer);
        } else {
            filteredList.forEach(visita -> visitesListContainer.addView(crearVistaVisita(visita, viewModel)));
        }
    }

    //todo: posar recyclerview i no linear
    private void mostrarMensajeSinVisitas(LinearLayout container) {
        TextView visitaTextView = new TextView(context);
        visitaTextView.setText(R.string.no_visitas_disponibles);
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

        container.addView(visitaTextView);
    }

    private View crearVistaVisita(Visita visita, MunicipiViewModel viewmodel) {
        View visitaView = LayoutInflater.from(context).inflate(R.layout.item_visita_perfil, null, false);

        TextView dataTextView = visitaView.findViewById(R.id.datavis);
        TextView municipiTextView = visitaView.findViewById(R.id.municipivis);
        TextView comarcaTextView = visitaView.findViewById(R.id.comarcavis);

        LinearLayout layoutPreviewNotes = visitaView.findViewById(R.id.layoutPreviewNotes);
        TextView textPreviewNotes = visitaView.findViewById(R.id.textPreviewNotes);

        //ImageView notesIcona = visitaView.findViewById(R.id.contenotes);
/*
        notesIcona.setVisibility(visita.notes.isEmpty() ? View.GONE : View.VISIBLE);

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        dataTextView.setText(sdf.format(new Date(visita.dataVisita)));
        municipiTextView.setText(visita.municipiId);

        visitaView.setOnClickListener(v -> showNotasDialog(visita));

        // Añadir margen a la vista inflada
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );

        // Establece márgenes (por ejemplo, 8 píxeles en todos los lados)
        layoutParams.setMargins(0, 8, 0, 8); // Ajusta los valores según lo que necesites

        visitaView.setLayoutParams(layoutParams);

        return visitaView;*/
        ImageView iconaNotaPetita = visitaView.findViewById(R.id.iconaNotaPetita); // 👈 Nova línia vinculada
        // Gestió intel·ligent de les notes del teu diàleg
        if (visita.notes != null && !visita.notes.trim().isEmpty()) {
            layoutPreviewNotes.setVisibility(View.VISIBLE);
            iconaNotaPetita.setVisibility(View.VISIBLE); // Mostrem l'indicador visual
            textPreviewNotes.setText(visita.notes); // Mostra el fragment inicial del text
        } else {
            layoutPreviewNotes.setVisibility(View.GONE);
        }

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        dataTextView.setText(sdf.format(new Date(visita.dataVisita)));
        municipiTextView.setText(visita.municipiId);

        // Si tens guardada la comarca al model Visita la pots afegir aquí:

        viewModel.obtenirComarcaPerMunicipiId(visita.municipiId).observe(getViewLifecycleOwner(), comarca -> {
            comarcaTextView.setText(comarca);
        });



        visitaView.setOnClickListener(v -> showNotasDialog(visita));

        // Ajust de marges dels elements del container amb scroll intern
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        layoutParams.setMargins(0, 4, 0, 4);
        visitaView.setLayoutParams(layoutParams);

        return visitaView;
    }



    private void showNotasDialog(Visita visita) {
        View view = getLayoutInflater().inflate(R.layout.dialog_visita, null);

        TextView titolNotaTextView = view.findViewById(R.id.titolnota);
        titolNotaTextView.setText(getString(R.string.visita_titulo, visita.municipiId));

        TextView dataVisitaTextView = view.findViewById(R.id.datavisita);
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        dataVisitaTextView.setText(sdf.format(new Date(visita.dataVisita)));

        configurarVistaNotas(view, visita);

        AlertDialog dialog = new AlertDialog.Builder(context)
                .setView(view)
                .create();

        configurarBotonesDialog(view, dialog, visita);

        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.show();
    }

    private void configurarVistaNotas(View view, Visita visita) {
        TextView notesTextView = view.findViewById(R.id.succesdesc);
        ScrollView scrollView = view.findViewById(R.id.scrollView);

        notesTextView.setText(visita.notes.isEmpty() ? getString(R.string.no_notas_disponibles) : visita.notes);

        notesTextView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                notesTextView.getViewTreeObserver().removeOnGlobalLayoutListener(this);

                int maxScrollHeight = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 150, getResources().getDisplayMetrics());
                scrollView.getLayoutParams().height = Math.min(notesTextView.getHeight(), maxScrollHeight);
                scrollView.requestLayout();
            }
        });
    }

    private void configurarBotonesDialog(View view, AlertDialog dialog, Visita visita) {
        ImageButton editboto = view.findViewById(R.id.btnModificar);
        ImageButton elimboto = view.findViewById(R.id.btnEliminar);
        AppCompatImageButton tancarButton = view.findViewById(R.id.btntancar);
        MunicipiViewModel viewModel = new ViewModelProvider(this).get(MunicipiViewModel.class);

        editboto.setOnClickListener(v -> editarVisita(visita, dialog));
        elimboto.setOnClickListener(v -> eliminarVisita(visita, dialog, viewModel));
        tancarButton.setOnClickListener(v -> dialog.dismiss());
    }

    private void editarVisita(Visita visita, AlertDialog dialog) {
        VisitaDialogFragment bottomSheet = new VisitaDialogFragment(visita.notes, visita.dataVisita, (dataModificada, notesModificades) -> {
            visita.setDataVisita(dataModificada);
            visita.setNotes(notesModificades);
            new ViewModelProvider(this).get(MunicipiViewModel.class).updateVisita(visita);
            dialog.dismiss();
        });

        bottomSheet.show(getParentFragmentManager(), "AgregarVisitaBottomSheet");
    }

    private void eliminarVisita(Visita visita, AlertDialog dialog, MunicipiViewModel viewModel) {
        ProgressDialog progressDialog = new ProgressDialog(context);
        progressDialog.setMessage(getString(R.string.eliminando));
        progressDialog.setCancelable(false);
        progressDialog.show();

        viewModel.deleteVisita(visita);
        viewModel.getVisitaEliminada().observe(getViewLifecycleOwner(), eliminada -> {
            if (eliminada) {
                progressDialog.dismiss();
                viewModel.setVisitaEliminada(false);
                dialog.dismiss();
            }
        });
    }
}