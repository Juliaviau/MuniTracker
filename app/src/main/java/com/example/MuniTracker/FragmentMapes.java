package com.example.MuniTracker;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
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
import com.example.MuniTracker.databinding.FragmentMapesBinding;
import com.google.android.material.bottomsheet.BottomSheetDialog;

public class FragmentMapes extends Fragment {

    private WebView webView;
    private Context context;
    FragmentMapesBinding binding;
    private String ultimID;
    private String originalViewBox;
    MapesHelper mapesHelper;
    String tipusMapa = "m";
    BottomSheetDialog bottomSheetDialog;
    String colorVisitat = "rgb(166, 94, 46)";

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

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
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

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentMapesBinding.inflate(inflater, container, false);
        View view = binding.getRoot();
        context = requireContext();
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

        layoutLlegenda = view.findViewById(R.id.legendLayout);
        layoutLlegenda.setVisibility(View.GONE);

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

    private void setupTerritoryButton(Button button, String territoryType, SearchAdapter adapter) {
        button.setOnClickListener(v -> {
            tipusMapa = territoryType;
            carregarMapa(territoryType); // Usar mapas precargados
            adaptadorActual = adapter;
            llistaResultatsBuscador.setAdapter(adaptadorActual);

            webView.setWebViewClient(new WebViewClient() {
                @Override
                public void onPageFinished(WebView view, String url) {
                    super.onPageFinished(view, url);
                    layoutLlegenda.setVisibility(View.VISIBLE);
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
                    mostrarZona(query, color, "0 0 425 400", TipoZona.MUNICIPI, new ViewModelProvider.AndroidViewModelFactory(getActivity().getApplication()));
                    break;
                case TERRITORY_TYPE_COMARCA:
                    mostrarZona(query, color, "0 0 425 400", TipoZona.COMARCA, new ViewModelProvider.AndroidViewModelFactory(getActivity().getApplication()));
                    break;
                case TERRITORY_TYPE_VEGUERIA:
                    //mostrarVegueria(query, color, "0 0 425 400");
                    mostrarZona(query, color, "0 0 425 400", TipoZona.VEGUERIA, new ViewModelProvider.AndroidViewModelFactory(getActivity().getApplication()));
                    break;
                case TERRITORY_TYPE_PROVINCIA:
                    mostrarZona(query, color, "0 0 425 400", TipoZona.PROVINCIA, new ViewModelProvider.AndroidViewModelFactory(getActivity().getApplication()));
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

        ProgressDialog progressDialog = new ProgressDialog(context);
        progressDialog.setMessage("Eliminant...");
        progressDialog.setCancelable(false);

        AlertDialog dialog = new AlertDialog.Builder(context)
                .setView(view)
                .create();

        editboto.setOnClickListener(v -> {
            //dialog.dismiss();
            // Pass the original visita and data to the dialog fragment
            VisitaDialogFragment bottomSheet = new VisitaDialogFragment(visita.notes, visita.dataVisita, (dataModificada, notesModificades) -> {
                // Update the visita object with the modified values
                visita.setDataVisita(dataModificada);
                visita.setNotes(notesModificades);
                viewModel.updateVisita(visita); // Assuming afegirVisita handles updates as well
                dialog.dismiss();
            });

            bottomSheet.show(getParentFragmentManager(), "AgregarVisitaBottomSheet");
        });

        elimboto.setOnClickListener(v -> {
            progressDialog.show();
            viewModel.deleteVisita(visita);
            viewModel.getVisitaEliminada().observe(getViewLifecycleOwner(), eliminada -> {

                if (eliminada) {

                    progressDialog.dismiss();
                    Toast.makeText(context, "Visita eliminada correctament", Toast.LENGTH_SHORT).show();

                    pintarMunicipisVisitats();
                    // actualizarBottomSheet();

                    canviarColorSVG(visita.municipiId, colorVisitat);
                    carregarMapa(TERRITORY_TYPE_MUNICIPALITY);

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
        ProgressBar progressBar = bottomSheetView.findViewById(R.id.progressBar);

        MunicipiViewModel viewModel = new ViewModelProvider(this, viewModelFactory).get(MunicipiViewModel.class);
        LiveData<Integer> quantitatVisitada;
        int totalMunicipis;

        //ScrollView scrollView = bottomSheetView.findViewById(R.id.scrollViewS);
        LinearLayout visitasContainer = bottomSheetView.findViewById(R.id.visitasContainerS);

        switch (tipoZona) {
            case COMARCA:
                quantitatVisitada = viewModel.obtenirQuantitatMunicipisVisitatsComarca(zonaId);
                totalMunicipis = mapesHelper.obtenirQuantitatMunicipisPerComarca(zonaId);

                viewModel.obtenirNomsMunicipisvisitatspercomarca(zonaId).observe(getViewLifecycleOwner(), municipis -> {
                    visitasContainer.removeAllViews();
                    for (Municipi municipi : municipis) {
                        TextView visitaTextView = new TextView(context);
                        visitaTextView.setText(municipi.id + " " + municipi.comarcaId );
                        visitaTextView.setPadding(16, 16, 16, 16);
                        visitaTextView.setBackgroundResource(R.drawable.rounded_card);
                        visitaTextView.setTextSize(16);
                        visitaTextView.setTextColor(ContextCompat.getColor(context, R.color.blau_mapa_fosc));
                        visitaTextView.setTypeface(null, Typeface.BOLD);
                        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                        );
                        params.setMargins(8, 8, 8, 8);
                        visitaTextView.setLayoutParams(params);
                        /*visitaTextView.setOnClickListener(v ->  {
                            showNotasDialog(municipi,bottomSheetDialog);
                            //mostrarMunicipi(municipiId, originalColor, originalViewBox);

                        });*/


                        visitasContainer.addView(visitaTextView);
                    }
                });
                break;
            case PROVINCIA:
                quantitatVisitada = viewModel.obtenirQuantitatMunicipisVisitatsProvincia(zonaId);
                totalMunicipis = mapesHelper.obtenirQuantitatMunicipisPerProvincia(zonaId);
                viewModel.obtenirNomsMunicipisvisitatsPerProvincia(zonaId).observe(getViewLifecycleOwner(), municipis -> {
                    visitasContainer.removeAllViews();
                    for (Municipi municipi : municipis) {
                        TextView visitaTextView = new TextView(context);
                        visitaTextView.setText(municipi.id + " " + municipi.provinciaId);
                        visitaTextView.setPadding(16, 16, 16, 16);
                        visitaTextView.setBackgroundResource(R.drawable.rounded_card);
                        visitaTextView.setTextSize(16);
                        visitaTextView.setTextColor(ContextCompat.getColor(context, R.color.blau_mapa_fosc));
                        visitaTextView.setTypeface(null, Typeface.BOLD);
                        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                        );
                        params.setMargins(8, 8, 8, 8);
                        visitaTextView.setLayoutParams(params);
                        /*visitaTextView.setOnClickListener(v ->  {
                            showNotasDialog(municipi,bottomSheetDialog);
                            //mostrarMunicipi(municipiId, originalColor, originalViewBox);

                        });*/


                        visitasContainer.addView(visitaTextView);
                    }
                });
                break;
            case VEGUERIA:
            default:
                quantitatVisitada = viewModel.obtenirQuantitatMunicipisVisitatsVegueria(zonaId);
                totalMunicipis = mapesHelper.obtenirQuantitatMunicipisPerVegueria(zonaId);
                viewModel.obtenirNomsMunicipisvisitatsPerVegueria(zonaId).observe(getViewLifecycleOwner(), municipis -> {
                    visitasContainer.removeAllViews();
                    for (Municipi municipi : municipis) {
                        TextView visitaTextView = new TextView(context);
                        visitaTextView.setText(municipi.id + " " + municipi.vegueriaId);
                        visitaTextView.setPadding(16, 16, 16, 16);
                        visitaTextView.setBackgroundResource(R.drawable.rounded_card);
                        visitaTextView.setTextSize(16);
                        visitaTextView.setTextColor(ContextCompat.getColor(context, R.color.blau_mapa_fosc));
                        visitaTextView.setTypeface(null, Typeface.BOLD);
                        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                        );
                        params.setMargins(8, 8, 8, 8);
                        visitaTextView.setLayoutParams(params);
                        /*visitaTextView.setOnClickListener(v ->  {
                            showNotasDialog(municipi,bottomSheetDialog);
                            //mostrarMunicipi(municipiId, originalColor, originalViewBox);

                        });*/


                        visitasContainer.addView(visitaTextView);
                    }
                });
                break;
        }

        progressBar.setMax(totalMunicipis);
        quantitatVisitada.observe(getViewLifecycleOwner(), visitats -> {
            double percentatge = totalMunicipis == 0 ? 0 : (double) (visitats * 100) / totalMunicipis;
            DecimalFormat df = new DecimalFormat("#.##");
            indicadorPercentatge.setText(df.format(percentatge) + " %");
            ObjectAnimator animator = ObjectAnimator.ofInt(progressBar, "progress", visitats);
            animator.setDuration(400);
            animator.setInterpolator(new DecelerateInterpolator());
            animator.start();
            infoMuni.setText("S'han visitat " + visitats + " de " + totalMunicipis + " municipis");
        });
    }

    private void manejarMunicipi(String municipiId, View bottomSheetView, ViewModelProvider.Factory viewModelFactory,
                                 BottomSheetDialog bottomSheetDialog, String originalColor, String originalViewBox) {
        TextView infoMuni = bottomSheetView.findViewById(R.id.zonaIfnfo);
        Button markAsVisitedButton = bottomSheetView.findViewById(R.id.visit);
        NestedScrollView scroll = bottomSheetView.findViewById(R.id.scrollView);
        View viewBottom = bottomSheetView.findViewById(R.id.viewbottom);

        boolean municipiVisitat = comparaColor(originalColor, colorVisitat);//rgb(27, 58, 95) es el blau fosc
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
        visitaTextView.setText(sdf.format(new Date(visita.dataVisita)) + "        " + visita.notes);
        visitaTextView.setPadding(16, 16, 16, 16);
        visitaTextView.setBackgroundResource(R.drawable.rounded_card);
        visitaTextView.setTextSize(16);
        visitaTextView.setTextColor(ContextCompat.getColor(context, R.color.blau_mapa_fosc));
        visitaTextView.setTypeface(null, Typeface.BOLD);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(8, 8, 8, 8);
        visitaTextView.setLayoutParams(params);
        visitaTextView.setOnClickListener(v ->  {
            showNotasDialog(visita,bottomSheetDialog);
            //mostrarMunicipi(municipiId, originalColor, originalViewBox);

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
                    canviarColorSVG(municipi.id, String.format("#%06X", (0xFFFFFF & ContextCompat.getColor(context, R.color.llegComplet))));
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
    public int obtenirColorPerPercentatge(double percentatge) {
        if (percentatge == 0.0) return ContextCompat.getColor(context, R.color.blau_mapa);
        else if (percentatge <= 25.0) return ContextCompat.getColor(context, R.color.lleg25);
        else if (percentatge <= 50.0) return ContextCompat.getColor(context, R.color.lleg50);
        else if (percentatge <= 75.0) return ContextCompat.getColor(context, R.color.lleg75);
        else if (percentatge < 100.0) return ContextCompat.getColor(context, R.color.lleg99);
        else return ContextCompat.getColor(context, R.color.llegComplet);
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