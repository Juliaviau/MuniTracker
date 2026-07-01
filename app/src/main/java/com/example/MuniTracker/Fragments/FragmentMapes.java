package com.example.MuniTracker.Fragments;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.icu.text.DecimalFormat;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.animation.DecelerateInterpolator;
import android.webkit.JavascriptInterface;
import android.webkit.ValueCallback;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.SearchView;
import androidx.core.content.ContextCompat;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.MuniTracker.MapesHelper;
import com.example.MuniTracker.Entity.Municipi;
import com.example.MuniTracker.MunicipiViewModel;
import com.example.MuniTracker.R;
import com.example.MuniTracker.SearchAdapter;
import com.example.MuniTracker.Entity.Visita;
import com.example.MuniTracker.VisitaDialogFragment;
import com.example.MuniTracker.databinding.FragmentMapesBinding;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.progressindicator.LinearProgressIndicator;

public class FragmentMapes extends Fragment {

    private WebView webView;
    private Context context;
    FragmentMapesBinding binding;
    private String ultimID;
    private String originalViewBox;
    MapesHelper mapesHelper;
    String tipusMapa = "m";
    BottomSheetDialog bottomSheetDialog;
    //String colorVisitat = Color.parseColor(paletaActual[4]);

    private static final String TERRITORY_TYPE_MUNICIPALITY = "m";
    private static final String TERRITORY_TYPE_COMARCA = "c";
    private static final String TERRITORY_TYPE_VEGUERIA = "v";
    private static final String TERRITORY_TYPE_PROVINCIA = "p";

    private String territoryType = TERRITORY_TYPE_MUNICIPALITY;
    private SearchAdapter adaptadorActual;
    private RecyclerView llistaResultatsBuscador;
    private LinearLayout layoutLlegenda;
    private MunicipiViewModel viewModel;

    private Map<String, String> cachedSVGs = new HashMap<>();

    // Copia la mateixa estructura de colors que hem definit al FragmentConfiguracio
    private final String[][] PALETES = {
            {"#fff7b2", "#ffd966", "#ffa631", "#d98a2d", "#a65e2e"}, // 0. Groc/Taronja (Original)
            {"#E0F3F8", "#ABD9E9", "#74ADD1", "#4575B4", "#313695"}, // 1. Oceà Blau
            {"#E8F5E9", "#A5D6A7", "#66BB6A", "#388E3C", "#1B5E20"}, // 2. Bosc Verd
            {"#FCE4EC", "#F48FB1", "#F06292", "#E91E63", "#880E4F"}  // 3. Magenta Elèctric
    };

    // Variable per saber quins 5 colors s'estan utilitzant actualment
    private String[] paletaActual;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        //SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        android.content.SharedPreferences prefs = context.getSharedPreferences("ConfigApp", android.content.Context.MODE_PRIVATE);
        int indexPaleta = prefs.getInt("paleta_seleccionada", 0);


        switch (indexPaleta) {
            case 0: context.setTheme(R.style.Tema_Paleta_Original); break;
            case 1: context.setTheme(R.style.Tema_Paleta_Ocea); break;
            case 2: context.setTheme(R.style.Tema_Paleta_Bosc); break;
            case 3: context.setTheme(R.style.Tema_Paleta_Magenta); break;
        }

        super.onCreate(savedInstanceState);
        binding = FragmentMapesBinding.inflate(getLayoutInflater());
        // Precargar mapas en memoria
        cachedSVGs.put(TERRITORY_TYPE_MUNICIPALITY, obtenirSVG(R.raw.municipis));
        cachedSVGs.put(TERRITORY_TYPE_COMARCA, obtenirSVG(R.raw.comarquesactu));
        cachedSVGs.put(TERRITORY_TYPE_VEGUERIA, obtenirSVG(R.raw.vegueries));
        cachedSVGs.put(TERRITORY_TYPE_PROVINCIA, obtenirSVG(R.raw.provincies));
        setRetainInstance(true); // Mantener estado entre configuraciones
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //pintarMunicipisVisitats();
    }




    private int obtenirColorDelTema(int attrId) {
        TypedValue typedValue = new TypedValue();
        getContext().getTheme().resolveAttribute(attrId, typedValue, true);
        return typedValue.data;
    }

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentMapesBinding.inflate(inflater, container, false);
        View view = binding.getRoot();
        context = requireContext();

        android.content.SharedPreferences prefs = context.getSharedPreferences("ConfigApp", android.content.Context.MODE_PRIVATE);
        int indexPaleta = prefs.getInt("paleta_seleccionada", 0);
        paletaActual = PALETES[indexPaleta]; // Assignem l'array de 5 colors d'aquella paleta

        /*binding.llegendaColor25.setBackgroundColor(Color.parseColor(paletaActual[0]));
        binding.llegendaColor50.setBackgroundColor(Color.parseColor(paletaActual[1]));
        binding.llegendaColor75.setBackgroundColor(Color.parseColor(paletaActual[2]));
        binding.llegendaColorComplet.setBackgroundColor(Color.parseColor(paletaActual[4]));*/


        //View view = inflater.inflate(R.layout.fragment_mapes, container, false);
        viewModel = new ViewModelProvider(this).get(MunicipiViewModel.class);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Activity activity = getActivity();
            if (activity != null) {
                Window window = activity.getWindow();
                window.setStatusBarColor(ContextCompat.getColor(activity, R.color.blau_mapa_fosc));
            }
        }

        mapesHelper = new MapesHelper(context);
        webView = view.findViewById(R.id.webView);
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);

        webView.setBackgroundColor(Color.TRANSPARENT);
        webSettings.setBuiltInZoomControls(true);
        webSettings.setDisplayZoomControls(false);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setUseWideViewPort(true);

        //layoutLlegenda = view.findViewById(R.id.legendLayout);
        //layoutLlegenda.setVisibility(View.GONE);

        actualitzarLlegenda(TERRITORY_TYPE_MUNICIPALITY);

        carregarMapa(TERRITORY_TYPE_MUNICIPALITY);

        webView.setWebViewClient(new WebViewClient() {

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                //new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    if (tipusMapa.equals(TERRITORY_TYPE_MUNICIPALITY)) {
                        pintarMunicipisVisitats();
                    }
                //}, 100);
            }
        });

        webView.addJavascriptInterface(new WebAppInterface(), "Android");
        //webView.setWebViewClient(new WebViewClient());

        SearchView buscador = view.findViewById(R.id.searchView);
        llistaResultatsBuscador = view.findViewById(R.id.resultsRecyclerView);
        llistaResultatsBuscador.setLayoutManager(new LinearLayoutManager(context));

        List<String> llistaMunicipis = mapesHelper.obtenirNomsMunicipisTotesComarques();
        List<String> llistaComarques = mapesHelper.obtenirNomsComarques();
        List<String> llistaVegueries = obtenirLlistaDeVegueries();
        List<String> llistaProvincies = obtenirLlistaDeProvincies();

        SearchAdapter adapterMunicipis = new SearchAdapter(llistaMunicipis);
        SearchAdapter adapterComarques = new SearchAdapter(llistaComarques);
        SearchAdapter adapterVegueries = new SearchAdapter(llistaVegueries);
        SearchAdapter adapterProvincies = new SearchAdapter(llistaProvincies);

        llistaResultatsBuscador.setAdapter(adapterMunicipis);
        adaptadorActual = adapterMunicipis;

        setupTerritoryButton(binding.btProvincies, TERRITORY_TYPE_PROVINCIA, adapterProvincies);
        setupTerritoryButton(binding.btVegueries, TERRITORY_TYPE_VEGUERIA, adapterVegueries);
        setupTerritoryButton(binding.btComarques, TERRITORY_TYPE_COMARCA, adapterComarques);
        setupTerritoryButton(binding.btMunicipis, TERRITORY_TYPE_MUNICIPALITY, adapterMunicipis);

        buscador.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (obtenirLlistaSegonsTipus().contains(query)) {
                    processarCerca(query);
                    buscador.setQuery("", true);
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                String query = newText.toLowerCase();
                List<String> filteredMunicipis = new ArrayList<>();
                for (String item : obtenirLlistaSegonsTipus()) {
                    if (item.toLowerCase().contains(query)) {
                        filteredMunicipis.add(item);
                    }
                }
                adaptadorActual.updateList(filteredMunicipis);
                llistaResultatsBuscador.setVisibility(filteredMunicipis.isEmpty() ? View.GONE : View.VISIBLE);
                return true;
            }
        });

        // Esconder la lista al pulsar la cruz del SearchView
        buscador.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                llistaResultatsBuscador.setVisibility(View.GONE); // Oculta la lista
                return false;
            }
        });

        // Esconder la lista cuando se hace clic fuera del SearchView
        //TODO: NO FA RES AQUESTA PART. EN TOCAR LA PANTALLA AMAGR LA LLISTA
        view.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    Rect outRect = new Rect();
                    buscador.getGlobalVisibleRect(outRect);  // Obtén el rectángulo de la vista del SearchView
                    if (!outRect.contains((int) event.getRawX(), (int) event.getRawY())) {
                        llistaResultatsBuscador.setVisibility(View.GONE); // Ocultar la lista si el toque está fuera del SearchView
                    }
                }
                return false;  // No consumimos el toque
            }
        });

        setAdapterListeners(adapterMunicipis, buscador,llistaResultatsBuscador);
        setAdapterListeners(adapterComarques, buscador, llistaResultatsBuscador);
        setAdapterListeners(adapterVegueries, buscador, llistaResultatsBuscador);
        setAdapterListeners(adapterProvincies, buscador, llistaResultatsBuscador);

        return view;
    }

    private void actualitzarLlegenda(String territoryType) {
        LinearLayout container = binding.legendContainer;
        container.removeAllViews(); // Esborrem l'antiga llegenda

        // Títol capçalera de la llegenda
        TextView titol = new TextView(getContext());
        titol.setTextSize(11);
        titol.setTypeface(null, android.graphics.Typeface.BOLD);
        titol.setTextColor(Color.parseColor("#777777"));
        LinearLayout.LayoutParams paramsTitol = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        paramsTitol.setMargins(0, 0, 0, 12);
        titol.setLayoutParams(paramsTitol);

        if (territoryType == TERRITORY_TYPE_MUNICIPALITY) {
            titol.setText("Estat del municipi");
            container.addView(titol);

            // El "No visitat" fa servir el color de base (0%), i el visitat el màxim (100%)
            afegirElementLlegenda(container, obtenirColorDelTema(R.attr.colorLlegenda0), "No visitat");
            afegirElementLlegenda(container, obtenirColorDelTema(R.attr.colorLlegenda100), "Visitat");
        } else {
            titol.setText("Percentatge Conquesta");
            container.addView(titol);

            // Es demana per nom d'etiqueta genèrica de l'attrs.xml!
            afegirElementLlegenda(container, obtenirColorDelTema(R.attr.colorLlegenda0), "Verge (0%)");
            afegirElementLlegenda(container, obtenirColorDelTema(R.attr.colorLlegenda25), "Iniciat (< 25%)");
            afegirElementLlegenda(container, obtenirColorDelTema(R.attr.colorLlegenda50), "A meitat (< 50%)");
            afegirElementLlegenda(container, obtenirColorDelTema(R.attr.colorLlegenda75), "Avançat (< 75%)");
            afegirElementLlegenda(container, obtenirColorDelTema(R.attr.colorLlegenda100), "Conquerit (100%)");
        }
    }

    // Funció auxiliar per muntar les línies horitzontals ràpidament sense duplicar codi
    private void afegirElementLlegenda(LinearLayout container, int colorHex, String text) {
        LinearLayout row = new LinearLayout(getContext());
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(android.view.Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        rowParams.setMargins(0, 0, 0, 8);
        row.setLayoutParams(rowParams);

        // Quadre de color
        View box = new View(getContext());
        LinearLayout.LayoutParams boxParams = new LinearLayout.LayoutParams(14, 14); // Mida de 14dp equivalents
        final float scale = getContext().getResources().getDisplayMetrics().density;
        int pixels = (int) (14 * scale + 0.5f);
        boxParams.width = pixels;
        boxParams.height = pixels;
        box.setLayoutParams(boxParams);
        box.setBackgroundColor(colorHex);

        // Text descriptiu
        TextView tv = new TextView(getContext());
        tv.setText(text);
        tv.setTextSize(12);
        tv.setTextColor(Color.parseColor("#222222"));
        LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        textParams.setMarginStart((int) (8 * scale + 0.5f));
        tv.setLayoutParams(textParams);

        row.addView(box);
        row.addView(tv);
        container.addView(row);
    }

    private void setupTerritoryButton(com.google.android.material.chip.Chip chip , String territoryType, SearchAdapter adapter) {
        chip.setOnClickListener(v -> {

            chip.setChecked(true);
            actualitzarLlegenda(territoryType);

            tipusMapa = territoryType;
            carregarMapa(territoryType); // Usar mapas precargados
            adaptadorActual = adapter;
            llistaResultatsBuscador.setAdapter(adaptadorActual);

            webView.setWebViewClient(new WebViewClient() {
                @Override
                public void onPageFinished(WebView view, String url) {
                    super.onPageFinished(view, url);
                    //layoutLlegenda.setVisibility(View.VISIBLE);
                    switch (territoryType) {
                        case TERRITORY_TYPE_MUNICIPALITY:
                            pintarMunicipisVisitats();
                            break;
                        case TERRITORY_TYPE_COMARCA:
                            pintarComarquesPerVisites();
                            break;
                        case TERRITORY_TYPE_VEGUERIA:
                            pintarVegueriesPerVisites();
                            break;
                        case TERRITORY_TYPE_PROVINCIA:
                            pintarProvinciesPerVisites();
                            break;
                    }
                }
            });
        });
    }

    private void setAdapterListeners(SearchAdapter adapter, SearchView searchView, RecyclerView resultsRecyclerView) {
        adapter.setOnItemClickListener(municipi -> {
            searchView.setQuery(municipi, true);
            searchView.clearFocus();
            resultsRecyclerView.setVisibility(View.GONE);
        });
    }

    private void processarCerca(String query) {
        obtenirColorSVG(query, color -> {
            switch (tipusMapa) {
                case TERRITORY_TYPE_MUNICIPALITY:
                    mostrarZona(query, color, paletaActual[4], TipoZona.MUNICIPI, new ViewModelProvider.AndroidViewModelFactory(getActivity().getApplication()));
                    break;
                case TERRITORY_TYPE_COMARCA:
                    mostrarZona(query, color, paletaActual[4], TipoZona.COMARCA, new ViewModelProvider.AndroidViewModelFactory(getActivity().getApplication()));
                    break;
                case TERRITORY_TYPE_VEGUERIA:
                    //mostrarVegueria(query, color, "0 0 425 400");
                    mostrarZona(query, color, paletaActual[4], TipoZona.VEGUERIA, new ViewModelProvider.AndroidViewModelFactory(getActivity().getApplication()));
                    break;
                case TERRITORY_TYPE_PROVINCIA:
                    mostrarZona(query, color, paletaActual[4], TipoZona.PROVINCIA, new ViewModelProvider.AndroidViewModelFactory(getActivity().getApplication()));
                    break;
            }
        });
    }
    private void carregarMapa(String territoryType) {
        String contingutSVG = cachedSVGs.get(territoryType);
        if (contingutSVG != null) {
            String dadesSVG = "<html><body style=\"margin: 0; padding: 0;\">" + contingutSVG + "</body></html>";
            webView.loadDataWithBaseURL(null, dadesSVG, "text/html", "UTF-8", null);
        }
    }

    private void inicialitzarMapa() {
        webView.evaluateJavascript(
                "document.getElementsByTagName('svg')[0].getAttribute('viewBox');",
                value -> {
                    originalViewBox = value;
                    Log.d("WebViewLog", "Mapa inicializat amb viewBox: " + originalViewBox);
                }
        );
    }

    private String obtenirSVG(int resourceId) {
        InputStream inputStream = getResources().openRawResource(resourceId);
        try {
            byte[] bytes = new byte[inputStream.available()];
            inputStream.read(bytes);
            return new String(bytes, StandardCharsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void resetZoom(String originalViewBox) {
        Log.d("NouZOOM", "Mapa inicialitzat amb viewBox: " + originalViewBox);
        String jsCode = "document.getElementsByTagName('svg')[0].setAttribute('viewBox', '" + originalViewBox + "');";
        webView.evaluateJavascript(jsCode, null);
    }

    public class WebAppInterface {

        @JavascriptInterface
        public void logMessage(final String missatge) {
            Log.d("WebViewLog", missatge);
        }

        @JavascriptInterface
        public void showProvince(final String provinciaId, final String originalColor, final String originalViewBox) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ultimID = provinciaId;
                    mostrarZona(provinciaId, originalColor, originalViewBox, TipoZona.PROVINCIA, new ViewModelProvider.AndroidViewModelFactory(getActivity().getApplication()));
                }
            });
        }

        @JavascriptInterface
        public void showComarca(final String comarcaId, final String originalColor, final String originalViewBox) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ultimID = comarcaId;
                    mostrarZona(comarcaId, originalColor, originalViewBox, TipoZona.COMARCA, new ViewModelProvider.AndroidViewModelFactory(getActivity().getApplication()));
                }
            });
        }

        @JavascriptInterface
        public void showVegueria(final String vegueriaId, final String originalColor, final String originalViewBox) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ultimID = vegueriaId;
                    mostrarZona(vegueriaId, originalColor, originalViewBox, TipoZona.VEGUERIA, new ViewModelProvider.AndroidViewModelFactory(getActivity().getApplication()));
                }
            });
        }

        @JavascriptInterface
        public void showMunicipi(final String municipiId, final String originalColor, final String originalViewBox) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ultimID = municipiId;
                    //mostrarMunicipi(municipiId, originalColor, originalViewBox);
                    mostrarZona(municipiId, originalColor, originalViewBox, TipoZona.MUNICIPI, new ViewModelProvider.AndroidViewModelFactory(getActivity().getApplication()));

                }
            });
        }
    }

    private void showNotasDialog(Visita visita, BottomSheetDialog bottomSheetDialog) {

        View view = getLayoutInflater().inflate(R.layout.dialog_visita, null);

        TextView titolNotaTextView = view.findViewById(R.id.titolnota);
        titolNotaTextView.setText("Visita a " + visita.municipiId);

        TextView dataVisitaTextView = view.findViewById(R.id.datavisita);
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        String formattedDate = sdf.format(new Date(visita.dataVisita));
        dataVisitaTextView.setText(formattedDate);

        TextView notesTextView = view.findViewById(R.id.succesdesc);

        // Si no hi ha notes, posem un missatge elegant d'estat buit
        if (visita.notes == null || visita.notes.trim().isEmpty()) {
            notesTextView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            notesTextView.setText("No hi ha notes guardades");
            notesTextView.setTextColor(Color.parseColor("#94A3B8")); // Gris suau de placeholder
        } else {
            notesTextView.setText(visita.notes);
        }

        // NOTA: Hem eliminat el OnGlobalLayoutListener perquè el ScrollView
        // ara es gestiona de manera intel·ligent i dinàmica directament des de l'XML.

        ImageButton elimboto = view.findViewById(R.id.btnEliminarvisita);
        ImageButton editboto = view.findViewById(R.id.btnModificar);
        ImageButton tancarButton = view.findViewById(R.id.btntancar);

        MunicipiViewModel viewModel = new ViewModelProvider(this).get(MunicipiViewModel.class);

        ProgressDialog progressDialog = new ProgressDialog(context);
        progressDialog.setMessage("Eliminant...");
        progressDialog.setCancelable(false);

        AlertDialog dialog = new AlertDialog.Builder(context)
                .setView(view)
                .create();

        editboto.setOnClickListener(v -> {
            VisitaDialogFragment bottomSheet = new VisitaDialogFragment(visita.notes, visita.dataVisita, (dataModificada, notesModificades) -> {
                visita.setDataVisita(dataModificada);
                visita.setNotes(notesModificades);
                viewModel.updateVisita(visita);
                dialog.dismiss();
            });
            bottomSheet.show(getParentFragmentManager(), "AgregarVisitaBottomSheet");
        });

        // ACCIÓ D'ELIMINAR AMB CONFIRMACIÓ
        elimboto.setOnClickListener(v -> {
            new AlertDialog.Builder(context, R.style.CustomAlertDialog)
                    .setTitle("Eliminar visita")
                    .setMessage("Estàs segur que vols esborrar la visita a " + visita.municipiId + "? Aquesta acció no es pot desfer.")
                    .setPositiveButton("Eliminar", (dialogInterface, i) -> {
                        // Si l'usuari confirma, executem la lògica d'esborrat que ja tenies
                        progressDialog.show();
                        viewModel.deleteVisita(visita);
                        viewModel.getVisitaEliminada().observe(getViewLifecycleOwner(), eliminada -> {
                            if (eliminada) {
                                progressDialog.dismiss();
                                Toast.makeText(context, "Visita eliminada correctament", Toast.LENGTH_SHORT).show();

                                pintarMunicipisVisitats();
                                canviarColorSVG(visita.municipiId, String.valueOf(Color.parseColor(paletaActual[4])));
                                carregarMapa(TERRITORY_TYPE_MUNICIPALITY);

                                viewModel.setVisitaEliminada(false);
                                dialog.dismiss(); // Tanquem el diàleg principal de la visita
                            } else {
                                progressDialog.dismiss();
                                Toast.makeText(context, "No s'ha pogut eliminar la visita", Toast.LENGTH_SHORT).show();
                            }
                        });
                    })
                    .setNegativeButton("Cancel·lar", (dialogInterface, i) -> dialogInterface.dismiss()) // No fa res
                    .show();
        });

        tancarButton.setOnClickListener(v -> dialog.dismiss());

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
        dialog.show();
    }
    //88888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888

    private void mostrarZona(String zonaId, String originalColor, String originalViewBox, TipoZona tipoZona, ViewModelProvider.Factory viewModelFactory) {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(context);

        int layoutId = (tipoZona == TipoZona.MUNICIPI) ? R.layout.bottom_sheet_dialog_municipis : R.layout.bottom_sheet_dialog;

        View bottomSheetView = getLayoutInflater().inflate(layoutId, null);

        TextView zonaInfo = bottomSheetView.findViewById(R.id.zonaNom);
        zonaInfo.setText(zonaId);

        canviarColorSVG(zonaId, "white");

        Button closeButton = bottomSheetView.findViewById(R.id.closeButton);
        closeButton.setOnClickListener(v -> {
            restaurarVistaZona(zonaId, originalColor, originalViewBox, bottomSheetDialog);
        });

        bottomSheetDialog.setOnDismissListener(dialog -> {
            restaurarVistaZona(zonaId, originalColor, originalViewBox, null);
        });

        if (tipoZona == TipoZona.MUNICIPI) {
            manejarMunicipi(zonaId, bottomSheetView, viewModelFactory, bottomSheetDialog, originalColor, originalViewBox);
        } else {
            manejarZonaGeneral(zonaId, tipoZona, bottomSheetView, viewModelFactory);
        }

        bottomSheetDialog.setContentView(bottomSheetView);
        bottomSheetDialog.show();
    }

    private void manejarZonaGeneral(String zonaId, TipoZona tipoZona, View bottomSheetView, ViewModelProvider.Factory viewModelFactory) {
        TextView infoMuni = bottomSheetView.findViewById(R.id.zonaIfnfo);
        TextView indicadorPercentatge = bottomSheetView.findViewById(R.id.indicadorpercentatge);

        // Canviem el ProgressBar al nou component de Material
        //com.google.android.material.progressindicator.LinearProgressIndicator progressBar = bottomSheetView.findViewById(R.id.progressBar);
        LinearLayout visitasContainer = bottomSheetView.findViewById(R.id.visitasContainerS);

        MunicipiViewModel viewModel = new ViewModelProvider(this, viewModelFactory).get(MunicipiViewModel.class);
        LiveData<Integer> quantitatVisitada;
        int totalMunicipis;

        // 1. Assignació de dades segons el tipus de zona (Molt més net)
        switch (tipoZona) {
            case COMARCA:
                quantitatVisitada = viewModel.obtenirQuantitatMunicipisVisitatsComarca(zonaId);
                totalMunicipis = mapesHelper.obtenirQuantitatMunicipisPerComarca(zonaId);
                viewModel.obtenirNomsMunicipisvisitatspercomarca(zonaId).observe(getViewLifecycleOwner(), municipis -> {
                    omplirLlistaMunicipis(visitasContainer, municipis);
                });
                break;

            case PROVINCIA:
                quantitatVisitada = viewModel.obtenirQuantitatMunicipisVisitatsProvincia(zonaId);
                totalMunicipis = mapesHelper.obtenirQuantitatMunicipisPerProvincia(zonaId);
                viewModel.obtenirNomsMunicipisvisitatsPerProvincia(zonaId).observe(getViewLifecycleOwner(), municipis -> {
                    omplirLlistaMunicipis(visitasContainer, municipis);
                });
                break;

            case VEGUERIA:
            default:
                quantitatVisitada = viewModel.obtenirQuantitatMunicipisVisitatsVegueria(zonaId);
                totalMunicipis = mapesHelper.obtenirQuantitatMunicipisPerVegueria(zonaId);
                viewModel.obtenirNomsMunicipisvisitatsPerVegueria(zonaId).observe(getViewLifecycleOwner(), municipis -> {
                    omplirLlistaMunicipis(visitasContainer, municipis);
                });
                break;
        }

        // 2. Control de progressió i percentatges dinàmics
        // Busquem els nous components customitzats
        View progressTrack = bottomSheetView.findViewById(R.id.customProgressTrack);
        View progressIndicator = bottomSheetView.findViewById(R.id.customProgressIndicator);

        quantitatVisitada.observe(getViewLifecycleOwner(), visitats -> {
            // 1. Calculem el percentatge matemàtic (Ex: 0.3333)
            double percentatge = totalMunicipis == 0 ? 0 : (double) visitats / totalMunicipis;

            DecimalFormat df = new DecimalFormat("#.##");
            indicadorPercentatge.setText(df.format(percentatge * 100) + " %");
            infoMuni.setText("S'han visitat " + visitats + " de " + totalMunicipis + " municipis");

            // 2. Esperem que Android sàpiga l'amplada real del fons per escalar l'indicador correctament
            progressTrack.post(() -> {
                int ampladaTotal = progressTrack.getWidth();
                int ampladaFinalIndicador = (int) (ampladaTotal * percentatge);

                // 3. Animem l'amplada de la vista des de 0 fins al seu percentatge de forma suada i contínua
                ValueAnimator anim = ValueAnimator.ofInt(0, ampladaFinalIndicador);
                anim.addUpdateListener(valueAnimator -> {
                    int val = (Integer) valueAnimator.getAnimatedValue();
                    ViewGroup.LayoutParams layoutParams = progressIndicator.getLayoutParams();
                    layoutParams.width = val;
                    progressIndicator.setLayoutParams(layoutParams);
                });
                anim.setDuration(500);
                anim.setInterpolator(new DecelerateInterpolator());
                anim.start();
            });

            ImageView goalIcon = bottomSheetView.findViewById(R.id.progressGoalIcon);

// Si s'ha completat tota la comarca, il·luminem el trofeu!
            if (visitats == totalMunicipis && totalMunicipis > 0) {
                goalIcon.setImageTintList(ColorStateList.valueOf(Color.parseColor("#EAB308"))); // Color or modern
            } else {
                goalIcon.setImageTintList(ColorStateList.valueOf(Color.parseColor("#94A3B8"))); // Gris apagat
            }
        });
    }

    // 🔥 Funció auxiliar unificada: Dibuixa els municipis idèntic al llistat de visites normals
    private void omplirLlistaMunicipis(LinearLayout container, List<Municipi> municipis) {
        container.removeAllViews();
        for (Municipi municipi : municipis) {
            TextView visitaTextView = new TextView(context);

            // Icona i nom amb format elegant
            visitaTextView.setText("📍  " + municipi.id);
            visitaTextView.setPadding(32, 24, 32, 24); // El mateix padding de contrast

            // Reutilitzem el disseny blanc amb ombres tridimensionals
            visitaTextView.setBackgroundResource(R.drawable.rounded_card);
            visitaTextView.setTextSize(14);
            visitaTextView.setTextColor(Color.parseColor("#1E293B")); // Gris fosc de gran contrast
            visitaTextView.setTypeface(null, Typeface.BOLD);
            visitaTextView.setElevation(3f);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(4, 6, 4, 6);
            visitaTextView.setLayoutParams(params);

            container.addView(visitaTextView);
        }
    }

    private int parseRgbString(String rgb) {
        rgb = rgb.replace("rgb(", "").replace(")", "");
        String[] parts = rgb.split(",");

        int r = Integer.parseInt(parts[0].trim());
        int g = Integer.parseInt(parts[1].trim());
        int b = Integer.parseInt(parts[2].trim());

        return Color.rgb(r, g, b);
    }

    private void manejarMunicipi(String municipiId, View bottomSheetView, ViewModelProvider.Factory viewModelFactory,
                                 BottomSheetDialog bottomSheetDialog, String originalColor, String originalViewBox) {
        TextView infoMuni = bottomSheetView.findViewById(R.id.zonaIfnfo);
        Button markAsVisitedButton = bottomSheetView.findViewById(R.id.visit);
        NestedScrollView scroll = bottomSheetView.findViewById(R.id.scrollView);
        View viewBottom = bottomSheetView.findViewById(R.id.viewbottom);


        Log.d("COLOR", paletaActual[4] + " " + originalColor);

        boolean municipiVisitat = originalColor.equals("")?false:comparaColor(String.valueOf(parseRgbString(originalColor)), String.valueOf(Color.parseColor(paletaActual[4])));

        MunicipiViewModel viewModel = new ViewModelProvider(this, viewModelFactory).get(MunicipiViewModel.class);

        if (municipiVisitat) {
            infoMuni.setText("Visites anteriors");
            markAsVisitedButton.setText("Afegir visita");
            mostrarVisitesMunicipi(municipiId, viewModel, bottomSheetView);
        } else {
            infoMuni.setText("");
            scroll.setVisibility(View.GONE);
            markAsVisitedButton.setText("Marcar com a visitat");
            viewBottom.setVisibility(View.GONE);
        }

        MapesHelper.TerritoryData territoryData = mapesHelper.getTerritoryData(municipiId);
        markAsVisitedButton.setOnClickListener(v -> {
            VisitaDialogFragment dialog = new VisitaDialogFragment("", 0, (data, notes) -> {
                if (!municipiVisitat) {
                    Municipi municipi = new Municipi(municipiId, municipiId, true, territoryData.comarcaId, territoryData.vegueriaId, territoryData.provinciaId);
                    viewModel.afegirMunicipi(municipi);
                }
                Visita visita = new Visita(municipiId, data, notes);
                viewModel.afegirVisita(visita);
                pintarMunicipisVisitats();
            });
            dialog.show(getParentFragmentManager(), "AgregarVisitaBottomSheet");
            bottomSheetDialog.dismiss();
        });
    }

    private void mostrarVisitesMunicipi(String municipiId, MunicipiViewModel viewModel, View bottomSheetView) {
        LinearLayout visitasContainer = bottomSheetView.findViewById(R.id.visitasContainer);
        viewModel.getVisitasByMunicipiId(municipiId).observe(getViewLifecycleOwner(), visites -> {
            visitasContainer.removeAllViews();
            for (Visita visita : visites) {
                TextView visitaTextView = crearVisitaTextView(visita);
                visitasContainer.addView(visitaTextView);
            }
        });
    }

    private TextView crearVisitaTextView(Visita visita) {
        TextView visitaTextView = new TextView(context);
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

        // 1. Text amb espaiat i un guió mitjà net si notes està buit
        String textNotes = (visita.notes != null && !visita.notes.trim().isEmpty()) ? visita.notes : "Sense comentaris";
        visitaTextView.setText("📅  " + sdf.format(new Date(visita.dataVisita)) + "   •   " + textNotes);

        // 2. Marges interns més amplis (Paddings) per donar aire a les dades
        visitaTextView.setPadding(32, 24, 32, 24);

        // 3. Assignem el fons blanc que acabem de modificar
        visitaTextView.setBackgroundResource(R.drawable.rounded_card);

        // 4. Mida i color contrastat de la lletra (Gris fosc de debò, gairebé negre)
        visitaTextView.setTextSize(14);
        visitaTextView.setTextColor(Color.parseColor("#1E293B"));
        visitaTextView.setTypeface(null, Typeface.BOLD); // Mantenim negreta per marcar importància

        // 5. El truc mestre: Elevació nativa (Ombra tridimensional)
        visitaTextView.setElevation(3f);

        // 6. Configurar els marges externs perquè no s'enganxin entre elles
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(4, 8, 4, 8); // Deixem espai vertical entre targetes
        visitaTextView.setLayoutParams(params);

        visitaTextView.setOnClickListener(v ->  {
            showNotasDialog(visita, bottomSheetDialog);
        });

        return visitaTextView;
    }

    private void restaurarVistaZona(String zonaId, String originalColor, String originalViewBox, BottomSheetDialog dialog) {
        canviarColorSVG(zonaId, originalColor);
        resetZoom(originalViewBox);
        if (dialog != null) {
            dialog.dismiss();
        }
    }

    enum TipoZona {
        COMARCA, PROVINCIA, VEGUERIA, MUNICIPI
    }

    //88888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888

    private void pintarVegueriesPerVisites() {
        MunicipiViewModel viewModel = new ViewModelProvider(this).get(MunicipiViewModel.class);
        List<String> llistaVegueries = obtenirLlistaDeVegueries();
        for (String vegueriaId : llistaVegueries) {
            viewModel.obtenirQuantitatMunicipisVisitatsVegueria(vegueriaId).observe(getViewLifecycleOwner(), nombreMunicipisVisitats -> {
                int quantitatMunicipisVegueria = mapesHelper.obtenirQuantitatMunicipisPerVegueria(vegueriaId);
                double percentatge = quantitatMunicipisVegueria == 0 ? 0 : (double) (nombreMunicipisVisitats * 100) / quantitatMunicipisVegueria;
                int color = obtenirColorPerPercentatge(percentatge);
                canviarColorSVG(vegueriaId, String.format("#%06X", (0xFFFFFF & color)));
            });
        }
    }
    private void pintarProvinciesPerVisites() {
        MunicipiViewModel viewModel = new ViewModelProvider(this).get(MunicipiViewModel.class);
        List<String> llistaProvincies = obtenirLlistaDeProvincies();
        for (String provinciaId : llistaProvincies) {
            viewModel.obtenirQuantitatMunicipisVisitatsProvincia(provinciaId).observe(getViewLifecycleOwner(), nombreMunicipisVisitats -> {
                int quantitatMunicipisProvincia = mapesHelper.obtenirQuantitatMunicipisPerProvincia(provinciaId);
                double percentatge = quantitatMunicipisProvincia == 0 ? 0 : (double) (nombreMunicipisVisitats * 100) / quantitatMunicipisProvincia;
                int color = obtenirColorPerPercentatge(percentatge);
                canviarColorSVG(provinciaId, String.format("#%06X", (0xFFFFFF & color)));
            });
        }
    }
    private void pintarComarquesPerVisites() {
        MunicipiViewModel viewModel = new ViewModelProvider(this).get(MunicipiViewModel.class);
        List<String> llistaComarques = mapesHelper.obtenirNomsComarques();
        for (String comarcaId : llistaComarques) {
            viewModel.obtenirQuantitatMunicipisVisitatsComarca(comarcaId).observe(getViewLifecycleOwner(), nombreMunicipisVisitats -> {
                int quantitatMunicipisComarca = mapesHelper.obtenirQuantitatMunicipisPerComarca(comarcaId);
                double percentatge = quantitatMunicipisComarca == 0 ? 0 : (double) (nombreMunicipisVisitats * 100) / quantitatMunicipisComarca;
                int color = obtenirColorPerPercentatge(percentatge);
                canviarColorSVG(comarcaId, String.format("#%06X", (0xFFFFFF & color)));
            });
        }
    }
    private void pintarMunicipisVisitats() {
        MunicipiViewModel viewModel = new ViewModelProvider(this).get(MunicipiViewModel.class);
        viewModel.obtenirMunicipisVisitats().observe(getViewLifecycleOwner(), municipisVisitats -> {
            if (municipisVisitats != null) {
                for (Municipi municipi : municipisVisitats) {
                    canviarColorSVG(municipi.id, String.format("#%06X", (0xFFFFFF &  Color.parseColor(paletaActual[4]) )/*(0xFFFFFF & ContextCompat.getColor(context, R.color.llegComplet))*/));
                }
            }
        });
    }




    private boolean comparaColor (String color1, String color2) {
        return color1.equalsIgnoreCase(color2);
    }

    //0
    //0 25
    //25 50
    //50 75
    //75 100
    //100
    /*public int obtenirColorPerPercentatge(double percentatge) {
        if (percentatge == 0.0) return ContextCompat.getColor(context, R.color.blau_mapa);
        else if (percentatge <= 25.0) return ContextCompat.getColor(context, R.color.lleg25);
        else if (percentatge <= 50.0) return ContextCompat.getColor(context, R.color.lleg50);
        else if (percentatge <= 75.0) return ContextCompat.getColor(context, R.color.lleg75);
        else if (percentatge < 100.0) return ContextCompat.getColor(context, R.color.lleg99);
        else return ContextCompat.getColor(context, R.color.llegComplet);
    }*/

    public int obtenirColorPerPercentatge(double percentatge) {
        // Si és 0, es manté el color de fons del mapa que ja tenies
        if (percentatge == 0.0) {
            return ContextCompat.getColor(context, R.color.blau_mapa);
        }

        // Si s'ha de pintar, agafem el color corresponent del nostre array dinàmic
        if (percentatge <= 25.0) {
            return Color.parseColor(paletaActual[0]); // lleg25 dinàmic
        } else if (percentatge <= 50.0) {
            return Color.parseColor(paletaActual[1]); // lleg50 dinàmic
        } else if (percentatge <= 75.0) {
            return Color.parseColor(paletaActual[2]); // lleg75 dinàmic
        } else if (percentatge < 100.0) {
            return Color.parseColor(paletaActual[3]); // lleg99 dinàmic
        } else {
            return Color.parseColor(paletaActual[4]); // llegComplet dinàmic
        }
    }



    public interface ColorCallback {
        void onColorReceived(String color);
    }

    private void obtenirColorSVG(String id, ColorCallback callback) {
        String jsCode = "document.getElementById('" + id + "').getAttribute('style');";
        webView.evaluateJavascript(jsCode, new ValueCallback<String>() {
            @Override
            public void onReceiveValue(String value) {
                String color = "";
                if (value != null && !value.equals("null")) {
                    value = value.replace("\"", "");
                    String[] styles = value.split(";");
                    for (String style : styles) {
                        if (style.trim().startsWith("fill:")) {
                            color = style.split(":")[1].trim();
                            break;
                        }
                    }
                }
                callback.onColorReceived(color);
            }
        });
    }
    private void canviarColorSVG(String comarcaId, String color) {
        String escapedComarcaId = comarcaId.replace("'", "\\'");
        String jsCode = "document.getElementById('" + escapedComarcaId + "').style.fill = '" + color + "';";
        webView.evaluateJavascript(jsCode, null);
    }

    @Override
    public void onResume() {
        super.onResume();

        // Restaurar el estado de los municipios visitados
        restoreVisitedMunicipalities();

        // Recargar el WebView si es necesario
        if (webView != null) {
            webView.reload();
        }
    }
    private void restoreVisitedMunicipalities() {
        // Restaurar la lista de municipios desde SharedPreferences o alguna fuente persistente
        SharedPreferences preferences = getActivity().getSharedPreferences("MunicipalitiesPrefs", Context.MODE_PRIVATE);
        String visitedMunicipalities = preferences.getString("visited_municipalities", "");
        // Aquí puedes usar la lista de municipios para pintarlos nuevamente en el mapa
    }


    private List<String> obtenirLlistaSegonsTipus() {
        switch (tipusMapa) {
            case TERRITORY_TYPE_COMARCA:
                return mapesHelper.obtenirNomsComarques();
            case TERRITORY_TYPE_VEGUERIA:
                return obtenirLlistaDeVegueries();
            case TERRITORY_TYPE_PROVINCIA:
                return obtenirLlistaDeProvincies();
            default:
                return mapesHelper.obtenirNomsMunicipisTotesComarques();
        }
    }

    @NonNull
    private List<String> obtenirLlistaDeVegueries() {
        return Arrays.asList(
                "Alt Pirineu i Aran","Barcelona","Camp de Tarragona","Catalunya Central","Girona",
                "Lleida","Penedès","Terres de l'Ebre");
    }
    @NonNull
    private List<String> obtenirLlistaDeProvincies() {
        return Arrays.asList(
                "Província de Barcelona","Província de Girona","Província de Lleida","Província de Tarragona");
    }


}
//859 728