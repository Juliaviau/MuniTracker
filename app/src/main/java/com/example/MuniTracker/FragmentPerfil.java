package com.example.MuniTracker;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.SearchView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.os.Looper;
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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;

public class FragmentPerfil extends Fragment {

    private FragmentPerfilBinding binding;
    private static Context context;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
    }

    public FragmentPerfil() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = FragmentPerfilBinding.inflate(getLayoutInflater());
        configurarStatusBar();
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
        View view = inflater.inflate(R.layout.fragment_perfil, container, false);
        MunicipiViewModel viewModel = new ViewModelProvider(this).get(MunicipiViewModel.class);

        //LinearLayout visitesListContainer = view.findViewById(R.id.visitasContainer);
        SearchView searchView = view.findViewById(R.id.searchViewperfil);
        Button eliminarTot = view.findViewById(R.id.eliminarTot);

       // En el FragmentPerfil
       RecyclerView recyclerView = view.findViewById(R.id.visitasContainer); // Reemplazar visitasContainer con un RecyclerView
       recyclerView.setLayoutManager(new LinearLayoutManager(context));
       VisitaAdapter adapter = new VisitaAdapter();
       recyclerView.setAdapter(adapter);

       /*viewModel.getAllVisitsOrderByData().observe(getViewLifecycleOwner(), visitas -> {
           adapter.submitList(visitas); // Actualizar la lista del adaptador
       });*/



       viewModel.getAllVisitsPaged().observe(viewLifecycleOwner) { pagingData ->
               lifecycleScope.launch {
                    adapter.submitData(pagingData)
                }
       }
        //observarVisites(viewModel, visitesListContainer, searchView);
        configurarBotonEliminarTot(eliminarTot, viewModel);

        return view;
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
            filteredList.forEach(visita -> visitesListContainer.addView(crearVistaVisita(visita)));
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

    private View crearVistaVisita(Visita visita) {
        View visitaView = LayoutInflater.from(context).inflate(R.layout.item_visita_perfil, null, false);

        TextView dataTextView = visitaView.findViewById(R.id.datavis);
        TextView municipiTextView = visitaView.findViewById(R.id.municipivis);
        ImageView notesIcona = visitaView.findViewById(R.id.contenotes);

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

        return visitaView;
    }


    private void configurarBotonEliminarTot(Button eliminarTot, MunicipiViewModel viewModel) {
        eliminarTot.setOnClickListener(v -> {
            new AlertDialog.Builder(context)
                    .setTitle(R.string.eliminar_datos_titulo)
                    .setMessage(R.string.eliminar_datos_mensaje)
                    .setPositiveButton(R.string.eliminar, (dialog, which) -> {
                        viewModel.eliminarTotMunicipiVisites();
                        Toast.makeText(context, R.string.datos_eliminados, Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton(R.string.cancelar, (dialog, which) -> dialog.dismiss())
                    .show();
        });
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
