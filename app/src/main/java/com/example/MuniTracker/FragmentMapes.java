package com.example.MuniTracker;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.icu.text.DecimalFormat;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
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
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

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
    String colorVisitat = "rgb(27, 58, 95)";

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = FragmentMapesBinding.inflate(getLayoutInflater());
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        pintarMunicipisVisitats();
    }

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_mapes, container, false);

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

        LinearLayout layoutLlegenda = view.findViewById(R.id.legendLayout);
        layoutLlegenda.setVisibility(View.GONE);

        carregarMapa(R.raw.municipis);

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                pintarMunicipisVisitats();
            }
        });

        webView.addJavascriptInterface(new WebAppInterface(), "Android");
        webView.setWebViewClient(new WebViewClient());

        SearchView buscador = view.findViewById(R.id.searchView);
        RecyclerView llistaResultatsBuscador = view.findViewById(R.id.resultsRecyclerView);
        llistaResultatsBuscador.setLayoutManager(new LinearLayoutManager(context));

        List<String> llistaMunicipis = obtenirLlistaDeMunicipis();
        List<String> llistaComarques = obtenirLlistaDeComarques();
        List<String> llistaVegueries = obtenirLlistaDeVegueries();
        List<String> llistaProvincies = obtenirLlistaDeProvincies();

        SearchAdapter adapterMunicipis = new SearchAdapter(llistaMunicipis);
        SearchAdapter adapterComarques = new SearchAdapter(llistaComarques);
        SearchAdapter adapterVegueries = new SearchAdapter(llistaVegueries);
        SearchAdapter adapterProvincies = new SearchAdapter(llistaProvincies);

        llistaResultatsBuscador.setAdapter(adapterMunicipis);
        final SearchAdapter[] adaptadorActual = {adapterMunicipis};

        Button botoProvincies = view.findViewById(R.id.btProvincies);
        Button botoVegueries = view.findViewById(R.id.btVegueries);
        Button botoComarques = view.findViewById(R.id.btComarques);
        Button botoMunicipis = view.findViewById(R.id.btMunicipis);

        botoProvincies.setOnClickListener(v -> {
            carregarMapa(R.raw.provincies);
            webView.setWebViewClient(new WebViewClient() {
                @Override
                public void onPageFinished(WebView view, String url) {
                    super.onPageFinished(view, url);
                    layoutLlegenda.setVisibility(View.VISIBLE);
                    adaptadorActual[0] = adapterProvincies;
                    llistaResultatsBuscador.setAdapter(adaptadorActual[0]);
                    pintarProvinciesPerVisites();
                    tipusMapa = "p";
                }
            });
        });
        botoVegueries.setOnClickListener(v -> {
            carregarMapa(R.raw.vegueries);
            webView.setWebViewClient(new WebViewClient() {
                @Override
                public void onPageFinished(WebView view, String url) {
                    super.onPageFinished(view, url);
                    layoutLlegenda.setVisibility(View.VISIBLE);
                    adaptadorActual[0] = adapterVegueries;
                    llistaResultatsBuscador.setAdapter(adaptadorActual[0]);
                    pintarVegueriesPerVisites();
                    tipusMapa = "v";
                }
            });
        });
        botoComarques.setOnClickListener(v -> {
            carregarMapa(R.raw.comarquesactu);
            webView.setWebViewClient(new WebViewClient() {
                @Override
                public void onPageFinished(WebView view, String url) {
                    super.onPageFinished(view, url);
                    layoutLlegenda.setVisibility(View.VISIBLE);
                    adaptadorActual[0] = adapterComarques;
                    llistaResultatsBuscador.setAdapter(adaptadorActual[0]);
                    pintarComarquesPerVisites();
                    tipusMapa = "c";
                }
            });
        });
        botoMunicipis.setOnClickListener(v -> {
            layoutLlegenda.setVisibility(View.GONE);
           // adapter[0] = new SearchAdapter(llistaMunicipis);
            adaptadorActual[0] = adapterMunicipis;
            llistaResultatsBuscador.setAdapter(adaptadorActual[0]);
            carregarMapa(R.raw.municipis);
        });

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
                adaptadorActual[0].updateList(filteredMunicipis);
                llistaResultatsBuscador.setVisibility(filteredMunicipis.isEmpty() ? View.GONE : View.VISIBLE);
                return true;
            }
        });

        setAdapterListeners(adapterMunicipis,buscador,llistaResultatsBuscador);
        setAdapterListeners(adapterComarques, buscador, llistaResultatsBuscador);
        setAdapterListeners(adapterVegueries, buscador, llistaResultatsBuscador);
        setAdapterListeners(adapterProvincies, buscador, llistaResultatsBuscador);

        return view;
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
                case "m":
                    mostrarMunicipi(query, color, "0 0 425 400");
                    break;
                case "c":
                    mostrarComarca(query, color, "0 0 425 400");
                    break;
                case "v":
                    mostrarVegueria(query, color, "0 0 425 400");
                    break;
                case "p":
                    mostrarProvincia(query, color, "0 0 425 400");
                    break;
            }
        });
    }

    private void carregarMapa(int mapaSVG) {
        String contingutSVG = obtenirSVG(mapaSVG);
        if (contingutSVG != null) {
            String dadesSVG = "<html><body style=\"margin: 0; padding: 0;\">" + contingutSVG + "</body></html>";
            webView.loadDataWithBaseURL(null, dadesSVG, "text/html", "UTF-8", null);
            if (mapaSVG == R.raw.municipis) {
                tipusMapa = "m";
                webView.setWebViewClient(new WebViewClient() {
                    @Override
                    public void onPageFinished(WebView view, String url) {
                        super.onPageFinished(view, url);
                        pintarMunicipisVisitats();
                        inicialitzarMapa();
                    }
                });
            }
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
        Log.d("NouZOOM", "Mapa inicializado con viewBox: " + originalViewBox);
        String jsCode = "document.getElementsByTagName('svg')[0].setAttribute('viewBox', '" + originalViewBox + "');";
        webView.evaluateJavascript(jsCode, null);
    }

    public class WebAppInterface {

        @JavascriptInterface
        public void logMessage(final String message) {
            Log.d("WebViewLog", message);
        }

        @JavascriptInterface
        public void showProvince(final String comarcaId, final String originalColor, final String originalViewBox) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ultimID = comarcaId;
                    mostrarProvincia(comarcaId, originalColor, originalViewBox);
                }
            });
        }

        @JavascriptInterface
        public void showComarca(final String comarcaId, final String originalColor, final String originalViewBox) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ultimID = comarcaId;
                    mostrarComarca(comarcaId, originalColor, originalViewBox);
                }
            });
        }

        @JavascriptInterface
        public void showVegueria(final String comarcaId, final String originalColor, final String originalViewBox) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ultimID = comarcaId;
                    mostrarVegueria(comarcaId, originalColor, originalViewBox);
                }
            });
        }

        @JavascriptInterface
        public void showMunicipi(final String comarcaId, final String originalColor, final String originalViewBox) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ultimID = comarcaId;
                    mostrarMunicipi(comarcaId, originalColor, originalViewBox);
                }
            });
        }
    }

    /*private void showNotasDialog(Visita visita, BottomSheetDialog bottomSheetDialog) {

        View view = getLayoutInflater().inflate(R.layout.dialog_visita, null);

        TextView titolNotaTextView = view.findViewById(R.id.titolnota);
        titolNotaTextView.setText("Visita a " + visita.municipiId);

        TextView dataVisitaTextView = view.findViewById(R.id.datavisita);
        dataVisitaTextView.setText(visita.dataVisita);

        TextView notesTextView = view.findViewById(R.id.succesdesc);
        notesTextView.setText(visita.notes);

        ScrollView scrollView = view.findViewById(R.id.scrollView);

        // Ajuste de altura máxima en píxeles, por ejemplo 400dp convertido a px
        final int maxHeight = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 150, getResources().getDisplayMetrics());

        // Listener para ajustar la altura del ScrollView en función del contenido
        notesTextView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                notesTextView.getViewTreeObserver().removeOnGlobalLayoutListener(this);

                // Si la altura del texto es mayor que el máximo permitido
                if (notesTextView.getHeight() > maxHeight) {
                    scrollView.getLayoutParams().height = maxHeight;
                } else if (visita.notes.equals("")) {
                    notesTextView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                    scrollView.getLayoutParams().height = notesTextView.getHeight()+100;
                    notesTextView.setText("No hi ha notes guardades");
                } else {
                    // Si el texto es corto, ajusta la altura del ScrollView al tamaño del texto
                    scrollView.getLayoutParams().height = notesTextView.getHeight();
                }
                // Actualiza el layout
                scrollView.requestLayout();
            }
        });

        ImageButton elimboto = view.findViewById(R.id.btnEliminar);
        MunicipiViewModel viewModel= new ViewModelProvider(this).get(MunicipiViewModel.class);



        AppCompatImageButton tancarButton = view.findViewById(R.id.btntancar);

        AlertDialog dialog = new AlertDialog.Builder(context)
                .setView(view)
                .create();

        elimboto.setOnClickListener(v -> {

            viewModel.deleteVisita(visita);

            MunicipiRepository municipiRepository = new MunicipiRepository(getActivity().getApplication());
            municipiRepository.getVisitaEliminada().observe(getViewLifecycleOwner(), eliminada -> {

                Toast.makeText(context, "Visita d " +eliminada, Toast.LENGTH_SHORT).show();
                if (eliminada) {
                    // Si la visita ha sido eliminada, actualizar el mapa y el BottomSheet
                   // pintarMunicipisVisitats();  // Actualizar el mapa
                    //actualizarBottomSheet();    // Actualizar el BottomSheet con los datos más recientes
                    Toast.makeText(context, "Visita eliminadasssssss", Toast.LENGTH_SHORT).show();

                    // Resetear el valor de la visita eliminada
                    municipiRepository.setVisitaEliminada(false);  // Es importante resetearlo
                    canviarColorSVG(visita.municipiId, colorVisitat);
                    pintarMunicipisVisitats();

                    dialog.dismiss();
                }
            });



            //bottomSheetDialog.dismiss();

            Toast.makeText(context, "Visita eliminada correctament", Toast.LENGTH_SHORT).show();

            //carregarMapa(R.raw.municipis);
           // mostrarMunicipi(visita.municipiId, originalColor, originalViewBox);
        });

        tancarButton.setOnClickListener(v -> dialog.dismiss());

        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.show();
    }*/

    private void showNotasDialog(Visita visita, BottomSheetDialog bottomSheetDialog) {

        View view = getLayoutInflater().inflate(R.layout.dialog_visita, null);

        TextView titolNotaTextView = view.findViewById(R.id.titolnota);
        titolNotaTextView.setText("Visita a " + visita.municipiId);

        TextView dataVisitaTextView = view.findViewById(R.id.datavisita);
        dataVisitaTextView.setText(visita.dataVisita);

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
        MunicipiViewModel viewModel = new ViewModelProvider(this).get(MunicipiViewModel.class);

        AppCompatImageButton tancarButton = view.findViewById(R.id.btntancar);

        // Crear el ProgressDialog
        ProgressDialog progressDialog = new ProgressDialog(context);
        progressDialog.setMessage("Eliminando...");
        progressDialog.setCancelable(false); // Evitar que el usuario lo cierre manualmente

        AlertDialog dialog = new AlertDialog.Builder(context)
                .setView(view)
                .create();

        elimboto.setOnClickListener(v -> {
            progressDialog.show();
            viewModel.deleteVisita(visita);
            viewModel.getVisitaEliminada().observe(getViewLifecycleOwner(), eliminada -> {

                if (eliminada) {

                    progressDialog.dismiss();
                    Toast.makeText(context, "Visita eliminada correctament", Toast.LENGTH_SHORT).show();

                    pintarMunicipisVisitats();  // Actualizar el mapa
                    // actualizarBottomSheet();    // Actualizar el BottomSheet

                    canviarColorSVG(visita.municipiId, colorVisitat);
                    carregarMapa(R.raw.municipis);

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


    private void mostrarComarca(String comarcaId, String originalColor, String originalViewBox) {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(context);
        View bottomSheetView = getLayoutInflater().inflate(R.layout.bottom_sheet_dialog, null);

        TextView comarcaInfo = bottomSheetView.findViewById(R.id.zonacNom);
        comarcaInfo.setText(comarcaId);

        canviarColorSVG(comarcaId, "white");

        Button closeButton = bottomSheetView.findViewById(R.id.closeButton);
        closeButton.setOnClickListener(v -> {
            canviarColorSVG(comarcaId, originalColor);
            resetZoom(originalViewBox);
            bottomSheetDialog.dismiss();
        });

        bottomSheetDialog.setOnDismissListener(dialog -> {
            canviarColorSVG(comarcaId, originalColor);
            resetZoom(originalViewBox);
        });

        TextView infoMuni = bottomSheetView.findViewById(R.id.zonaIfnfo);
        TextView indicadorpercentatge = bottomSheetView.findViewById(R.id.indicadorpercentatge);

        ProgressBar progressBar = bottomSheetView.findViewById(R.id.progressBar);
        progressBar.setProgress(0);

        MunicipiViewModel viewModel = new ViewModelProvider(this).get(MunicipiViewModel.class);

        viewModel.obtenirQuantitatMunicipisVisitatsComarca(comarcaId).observe(getViewLifecycleOwner(), nombreMunicipisVisitats -> {
            int quantitatMunicipisComarca = mapesHelper.obtenirQuantitatMunicipisPerComarca(comarcaId);
            double percentatge = quantitatMunicipisComarca == 0 ? 0 : (double) (nombreMunicipisVisitats * 100) / quantitatMunicipisComarca;
            DecimalFormat df = new DecimalFormat("#.##");
            progressBar.setMax(quantitatMunicipisComarca);
            indicadorpercentatge.setText(df.format(percentatge) + " %");
            ObjectAnimator animator = ObjectAnimator.ofInt(progressBar, "progress", nombreMunicipisVisitats);
            animator.setDuration(400); // Duración de la animación en milisegundos
            animator.setInterpolator(new DecelerateInterpolator()); // Suaviza la animación
            animator.start();
            infoMuni.setText("S'han visitat " + nombreMunicipisVisitats + " de " + quantitatMunicipisComarca + " municipis");

        });

        bottomSheetDialog.setContentView(bottomSheetView);
        bottomSheetDialog.show();
    }
    private void mostrarProvincia(String provinciaId, String originalColor, String originalViewBox) {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(context);
        View bottomSheetView = getLayoutInflater().inflate(R.layout.bottom_sheet_dialog, null);

        TextView provinciaInfo = bottomSheetView.findViewById(R.id.zonacNom);
        provinciaInfo.setText(provinciaId);

        canviarColorSVG(provinciaId, "white");

        Button closeButton = bottomSheetView.findViewById(R.id.closeButton);
        closeButton.setOnClickListener(v -> {
            canviarColorSVG(provinciaId, originalColor);
            resetZoom(originalViewBox);
            bottomSheetDialog.dismiss();
        });

        bottomSheetDialog.setOnDismissListener(dialog -> {
            canviarColorSVG(provinciaId, originalColor);
            resetZoom(originalViewBox);
        });


        TextView infoMuni = bottomSheetView.findViewById(R.id.zonaIfnfo);
        TextView indicadorpercentatge = bottomSheetView.findViewById(R.id.indicadorpercentatge);

        ProgressBar progressBar = bottomSheetView.findViewById(R.id.progressBar);
        progressBar.setProgress(0);

        MunicipiViewModel viewModel = new ViewModelProvider(this).get(MunicipiViewModel.class);

        viewModel.obtenirQuantitatMunicipisVisitatsProvincia(provinciaId).observe(getViewLifecycleOwner(), nombreMunicipisVisitats -> {
            int quantitatMunicipisComarca = mapesHelper.obtenirQuantitatMunicipisPerProvincia(provinciaId);
            double percentatge = quantitatMunicipisComarca == 0 ? 0 : (double) (nombreMunicipisVisitats * 100) / quantitatMunicipisComarca;
            DecimalFormat df = new DecimalFormat("#.##");
            progressBar.setMax(quantitatMunicipisComarca);
            indicadorpercentatge.setText(df.format(percentatge) + " %");
            ObjectAnimator animator = ObjectAnimator.ofInt(progressBar, "progress", nombreMunicipisVisitats);
            animator.setDuration(400); // Duración de la animación en milisegundos
            animator.setInterpolator(new DecelerateInterpolator()); // Suaviza la animación
            animator.start();
            infoMuni.setText("S'han visitat " + nombreMunicipisVisitats + " de " + quantitatMunicipisComarca + " municipis");

        });

        bottomSheetDialog.setContentView(bottomSheetView);
        bottomSheetDialog.show();
    }
    private void mostrarVegueria(String vegueriaId, String originalColor, String originalViewBox) {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(context);
        View bottomSheetView = getLayoutInflater().inflate(R.layout.bottom_sheet_dialog, null);

        TextView vegueriaInfo = bottomSheetView.findViewById(R.id.zonacNom);
        vegueriaInfo.setText(vegueriaId);

        canviarColorSVG(vegueriaId, "white");

        Button closeButton = bottomSheetView.findViewById(R.id.closeButton);
        closeButton.setOnClickListener(v -> {
            canviarColorSVG(vegueriaId, originalColor);
            resetZoom(originalViewBox);
            bottomSheetDialog.dismiss();
        });

        bottomSheetDialog.setOnDismissListener(dialog -> {
            canviarColorSVG(vegueriaId, originalColor);
            resetZoom(originalViewBox);
        });


        TextView infoMuni = bottomSheetView.findViewById(R.id.zonaIfnfo);
        TextView indicadorpercentatge = bottomSheetView.findViewById(R.id.indicadorpercentatge);

        ProgressBar progressBar = bottomSheetView.findViewById(R.id.progressBar);
        progressBar.setProgress(0);

        MunicipiViewModel viewModel = new ViewModelProvider(this).get(MunicipiViewModel.class);

        viewModel.obtenirQuantitatMunicipisVisitatsVegueria(vegueriaId).observe(getViewLifecycleOwner(), nombreMunicipisVisitats -> {
            int quantitatMunicipisComarca = mapesHelper.obtenirQuantitatMunicipisPerVegueria(vegueriaId);
            double percentatge = quantitatMunicipisComarca == 0 ? 0 : (double) (nombreMunicipisVisitats * 100) / quantitatMunicipisComarca;
            DecimalFormat df = new DecimalFormat("#.##");
            progressBar.setMax(quantitatMunicipisComarca);
            indicadorpercentatge.setText(df.format(percentatge) + " %");
            ObjectAnimator animator = ObjectAnimator.ofInt(progressBar, "progress", nombreMunicipisVisitats);
            animator.setDuration(400); // Duración de la animación en milisegundos
            animator.setInterpolator(new DecelerateInterpolator()); // Suaviza la animación
            animator.start();
            infoMuni.setText("S'han visitat " + nombreMunicipisVisitats + " de " + quantitatMunicipisComarca + " municipis");

        });

        bottomSheetDialog.setContentView(bottomSheetView);
        bottomSheetDialog.show();
    }
    private void mostrarMunicipi(String municipiId, String originalColor, String originalViewBox) {

        Log.i("mostrarMunicipiIIIIIIIIIIIII", municipiId + " " + originalColor + " " + originalViewBox);

        bottomSheetDialog = new BottomSheetDialog(context);
        View bottomSheetView = getLayoutInflater().inflate(R.layout.bottom_sheet_dialog_municipis, null);
        TextView municipiInfo = bottomSheetView.findViewById(R.id.zonaNom);
        municipiInfo.setText(municipiId);

        Button markAsVisitedButton = bottomSheetView.findViewById(R.id.visit);
        MunicipiViewModel viewModel= new ViewModelProvider(this).get(MunicipiViewModel.class);
        Button closeButton = bottomSheetView.findViewById(R.id.closeButton);
        View viewBottom = bottomSheetView.findViewById(R.id.viewbottom);

        Log.d("Coloer " , originalColor);
        boolean municipiVisitat = comparaColor(originalColor,"rgb(27, 58, 95)");
        TextView infoMuni = bottomSheetView.findViewById(R.id.zonaIfnfo);

        NestedScrollView scroll = bottomSheetView.findViewById(R.id.scrollView);

        closeButton.setVisibility(View.VISIBLE);
        scroll.setVisibility(View.VISIBLE);
        markAsVisitedButton.setVisibility(View.VISIBLE);
        viewBottom.setVisibility(View.VISIBLE);

        if (municipiVisitat) {

            infoMuni.setText("Visites anteriors");
            markAsVisitedButton.setText("Afegir visita");

            LinearLayout visitasContainer = bottomSheetView.findViewById(R.id.visitasContainer);

            viewModel.getVisitasByMunicipiId(municipiId).observe(getViewLifecycleOwner(), visites -> {

                visitasContainer.removeAllViews();

                for (Visita visita : visites) {
                    TextView visitaTextView = new TextView(context);
                    visitaTextView.setText(visita.dataVisita + "        " + visita.notes);
                    visitaTextView.setPadding(16, 16, 16, 16);
                    visitaTextView.setBackgroundResource(R.drawable.rounded_card);
                    visitaTextView.setTextSize(16);
                    visitaTextView.setTextColor(ContextCompat.getColor(context, R.color.blau_mapa_fosc));
                    visitaTextView.setTypeface(null, Typeface.BOLD);
                    visitaTextView.setLayoutParams(new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                    ));
                    ((LinearLayout.LayoutParams) visitaTextView.getLayoutParams()).setMargins(8, 8, 8, 8);
                    visitaTextView.setOnClickListener(v ->  {
                        showNotasDialog(visita,bottomSheetDialog);
                        //mostrarMunicipi(municipiId, originalColor, originalViewBox);

                    });
                    visitasContainer.addView(visitaTextView);
                }
            });

        } else {
            infoMuni.setText("");
            closeButton.setVisibility(View.GONE);
            scroll.setVisibility(View.GONE);
            markAsVisitedButton.setText("Marcar com a visitat");
            viewBottom.setVisibility(View.GONE);
        }

        MapesHelper.TerritoryData territoryData = mapesHelper.getTerritoryData(municipiId);

        markAsVisitedButton.setOnClickListener(v -> {
            VisitaDialogFragment bottomSheet = new VisitaDialogFragment((data, notes) -> {

                AtomicReference<Boolean> estaVisitat = new AtomicReference<>(false);
                viewModel.obtenirMunicipisVisitats().observe(getViewLifecycleOwner(), municipisVisitats -> {
                    for (Municipi municipi : municipisVisitats) {
                        if (municipi.id.equals(municipiId)) {
                            estaVisitat.set(true);
                            break;
                        }
                    }
                    if (!estaVisitat.get()) {
                        Log.d("CREA MUNI ", municipiId + " " + territoryData.comarcaId+ " " + territoryData.vegueriaId+ " " + territoryData.provinciaId);
                        Municipi municipi = new Municipi(municipiId, municipiId, true, territoryData.comarcaId, territoryData.vegueriaId, territoryData.provinciaId);
                        viewModel.afegirMunicipi(municipi);
                    }
                });
                Visita visita = new Visita(municipiId, data, notes);

                viewModel.afegirVisita(visita);

                pintarMunicipisVisitats();
            });

            bottomSheet.show(getParentFragmentManager(), "AgregarVisitaBottomSheet");

            bottomSheetDialog.dismiss();
        });


        closeButton.setOnClickListener(v -> {
            canviarColorSVG(municipiId, originalColor);
            resetZoom(originalViewBox);
            bottomSheetDialog.dismiss();
        });

        bottomSheetDialog.setOnDismissListener(dialog -> {
            canviarColorSVG(municipiId, originalColor);
            resetZoom(originalViewBox);
            bottomSheetDialog.dismiss();
        });

        bottomSheetDialog.setContentView(bottomSheetView);
        bottomSheetDialog.show();
    }


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
        List<String> llistaComarques = obtenirLlistaDeComarques();
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
        MunicipiViewModel viewModel = new ViewModelProvider(FragmentMapes.this).get(MunicipiViewModel.class);
        viewModel.obtenirMunicipisVisitats().observe(getViewLifecycleOwner(), municipisVisitats -> {
            Log.d("pintarMunicipisVisitats", "Updating visited municipalities");

            for (Municipi municipi : municipisVisitats) {
                canviarColorSVG(municipi.id,  String.format("#%06X", (0xFFFFFF & ContextCompat.getColor(context, R.color.llegComplet))));
            }
        });
    }


    private boolean comparaColor (String color1, String color2) {
        return color1.equalsIgnoreCase(color2);
    }
    public int obtenirColorPerPercentatge(double percentatge) {
        if (percentatge == 0.0) return ContextCompat.getColor(context, R.color.blau_mapa);
        else if (percentatge < 25.0) return ContextCompat.getColor(context, R.color.lleg25);
        else if (percentatge < 50.0) return ContextCompat.getColor(context, R.color.lleg50);
        else if (percentatge < 75.0) return ContextCompat.getColor(context, R.color.lleg75);
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
        //Log.i("color canviat", color);
        String jsCode = "document.getElementById('" + escapedComarcaId + "').style.fill = '" + color + "';";
        webView.evaluateJavascript(jsCode, null);
    }


    private List<String> obtenirLlistaSegonsTipus() {
        switch (tipusMapa) {
            case "c":
                return obtenirLlistaDeComarques();
            case "v":
                return obtenirLlistaDeVegueries();
            case "p":
                return obtenirLlistaDeProvincies();
            default:
                return obtenirLlistaDeMunicipis();
        }
    }
    @NonNull
    private List<String> obtenirLlistaDeMunicipis() {


        return Arrays.asList(
                "Avinyonet del Penedès","Argençola","Avinyó","Artés","Aguilar de Segarra","Abrera", "Alella","Arenys de Munt","Argentona","Arenys de Mar","Aiguafreda",
                "Agullana","Albanyà","Alp","Avinyonet de Puigventós","Argelaguer","Aiguaviva","Anglès", "Amer", "Arbúcies","Aiguamúrcia","Alió","Alcover","Arbolí","Almoster","Alforja",
                "Albinyana","Alcanar","Amposta","Alfara de Carles","Aldover","Ascó","Altafulla", "Arnes",

                "Bellprat","Balsareny","Begues","Barcelona","Badalona","Balenyà","Badia del Vallès","Barberà del Vallès","Bigues i Riells","Blanes",
                "Begur","Bellcaire d'Empordà","Biure","Bolvir","Borrassà","Besalú","Beuda","Bordils","Banyoles","Blanes","Breda", "Brunyola i Sant Martí Sapresa","Bràfim","Botarell",
                "Benifallet","Banyeres del Penedès","Bellvei","Bonastre","Blancafort","Barberà de la Conca","Batea","Benissanet","Bot","Bellmunt del Proiorat",

                "Castellet i la Gornal","Castellví de la Marca","Cabrera d'Anoia","Castellfollit de Riubregós","Calonge de Segarra", "Calaf", "Copons","Castellolí","Carme",
                "Capellades", "Castellbell i el Vilar","Cardona", "Castellnou de Bages","Callús","Castellfollit del Boix", "Castellgalí","Corbera de Llobregat",
                "Castellví de Rosanes","Colbató","Cervelló", "Cornellà de Llobregat","Castelldefels","Canyelles","Cubelles","Caldes d'Estrac","Cabrils","Calella","Canet de Mar",
                "Centelles","Calldetenes", "Castellbisbal","Cerdanyola del Vallès","Castellar del Vallès","Caldes de Montbui","Campins","Canovelles","Cardedeu","Cànoves i Samalús",
                "Calonge i Sant Antoni","Cadaqués","Capmany","Castell-Platja d'Aro","Castelló d'Empúries","Cistella","Colera","Corçà","Cruïlles, Monells i Sant Sadurní de l'Heura",
                "Castellfollit de la Roca","Cassà de la Selva","Celrà", "Campllong","Cornellà del Terri","Crespià","Camós","Campdevànol","Camprodon","Caldes de Malavella",
                "Cabra del Camp","Capafonts","Castellvell del Camp","Colldejou","Cambrils","Camarles","Calafell","Cunit","Conesa","Cornudella de Montsant","Caseres","Cabaçés","Capçanes","Constantí","Creixell","Corbera d'Ebre",

                "Dosrius","Darnius","Das","Duesaigües","Deltebre",

                "El Pla del Penedès","El Bruc","Els Hostalets de Pierola","Els Prats de Rei","El Pont de Vilomara i Rocafort","El Papiol","El Prat de Llobregat", "Esparreguera",
                "Esplugues de Llobregat", "El Masnou", "El Brull","Espinelves","Empuriabrava","Espolla", "Esponellà","El Pla de Santa Maria","El Pont d'Armentera","El Rourell",
                "Els Garidells","El Milà","El Perelló","El Vendrell","El Montmell","Els Guiamets","El Molar","El Lloar","El Masroig","Els Pallaresos","El Catllar","El Morell",
                "El Pinell de Brai","El Prat del Comte","Molins de Rei","Pallejà","La Palma de Cervelló",

                "Font-rubí","Fonollosa","Folgueroles","Figaró-Montmany","Fogars de Montclús","Finca de les Fonts","Fontanals de Cerdanya","Fontanilles","Fornells de la Selva",
                "Fontcoberta","Figueres","Forallac","Figuerola del Camp","Forès","Freginals","Falset","Flix",

                "Gelida","Gaià","Gavà","Gurb","Galifa","Gualba","Granollers","Gualta","Guils de Cerdanya","Garriguella","Girona", "Gombrèn","Godall","Gratallops","Garcia","Ginestar","Gandesa",

                "Hospitalet de Llobregat","Horta de Sant Joan",

                "Igualada","Isòvol",

                "Jorba","Jafre","Juià",

                "La Granada","Les Cabanyes","La Pobla de Claramunt","La Torre de Claramunt","La Llacuna","L'Esquirol","Les Masies de Voltregà","Les Masies de Roda","Llinars del Vallès",
                "La Garriga", "La Llagosta","La Roca del Vallès","L'Ametlla del Vallès","Lliçà d'Amunt", "Lliçà de Vall","Les Franqueses del Vallès",
                "La Bisbal d'Empordà","La Jonquera","La Pera","L'Escala","Llançà","Llers","Llívia","Les Planes d'Hostoles", "Les Preses", "Llagostera","Llambilles","Llanars","Lloret de Mar","La Riba",
                "L'Arboç","La Masó","La Febró","L'Albiol","Les Borjes del Camp","L'Aleixar","La Selva del Camp","L'Argentera","L'Aldea","L'Ametlla de Mar","L'Ampolla","Llorenç del Penedès",
                "La Bisbal del Penedès","Les Piles","Llorac", "L'Espluga de Francolí", "La Galera","La Sènia","La Pobla de Massaluca","La Fatarella","La Morera de Montsant","La Morera de Montsant",
                "La Bisbal de Falset","La Torre de Fontaubella","La Palma d'Ebre","La Vilella Alta","La Vilella Baixa","La Figuera","La Torre de l'Espanyol","La Pobla de Mafumet","La Riera de Gaià",
                "La Nou de Gaià","La Pobla de Montornès","La Secuita","Queralbs","Quart", "Querol","La Pobla de Lillet",

                "Mediona","Montmaneu","Masquefa","Manresa","Mura","Marganell","Monistrol de Montserrat","Martorell","Mataró","Montgat","Malgrat de Mar","Muntanyola","Montesquiu", "Malla","Manlleu","Montcada i Reixac","Matadepera",
                "Martorelles","Mollet del Vallès", "Montmeló","Montornès del Vallès","Montseny","Maçanet de Cabrenys","Meranges","Mont-ras","Maià de Montcal","Mieres","Montagut i Oix","Molló",
                "Massanes","Maçanet de la Selva","Madremanya","Mont-ral","Montferri", "Maspujols", "Mont-roig del Camp", "Masllorenç", "Montblanc", "Mas de Barberans", "Masdenverge", "Margalef",
                "Marçà","Móra d'Ebre","Móra la Nova","Miravet", "Montbrió del Camp",

                "Navarcles","Navàs","Nulles",

                "Olesa de Bonesvalls", "Olèrdola","Orpí", "Òdena","Olesa de Montserrat","Olivella","Òrrius","Orís","Ordis","Olot","Ogassa","Osor",

                "Puigdàlber","Pacs del Penedès",  "Pontons","Pujalt","Piera","Premià de Dalt","Premià de Mar", "Palafolls","Pineda de Mar","Palau-solità i Plegamans", "Polinyà","Parets del Vallès",
                "Pau","Peralada","Palafrugell","Palamós","Pont de Molins","Parlavà",  "Portbou","Palol de Revardit", "Porqueres", "Pardines","Planoles","Puigpelat","Prades","Pratdip","Paüls","Pontils",
                "Passanant i Belltall","Pira","Poboleda","Pradell de la Teixeta","Porrera","Perafort","Palau-saverdera","Pals","Prats i Sansor","Puigcerdà","Prats de Lluçanès",

                "Rubió","Rupit i Pruit","Roda de Ter","Rellinars","Rubí","Rajadell","Ripollet","Regencós","Roses","Riudaura","Ripoll",  "Ribes de Freser","Riudarenes","Rodonyà","Riudecols",
                "Riudecanyes","Riudoms","Reus","Roquetes","Rocafort de Queralt","Riba-roja d'Ebre","Rasquera","Roda de Berà","Renau",

                "Subirats", "Santa Margarida i els Monjos", "Sant Llorenç d'Hortons", "Sant Martí Sarroca", "Santa Fe del Penedès", "Sant Cugat Sesgarrigues", "Sant Quintí de Mediona",
                "Sant Pere de Riudebitlles","Sant Sadurní d'Anoia", "Sant Martí Sesgueioles","Sant Martí de Tous", "Santa Margarida de Montbui", "Santa Maria de Miralles",
                "St. Pere Sallavinera","Sant Feliu Sasserra","Sallent","Sant Mateu de Bages","Súria","Santpedor","Sant Fruitós de Bages", "Sant Joan de Vilatorrada",
                "Sant Salvador de Guardiola","Sant Vicenç de Castellet","Sant Esteve Sesrovires","Sant Andreu de la Barca","Sant Feliu de LLobregat","Sant Vicenç dels Horts",
                "Sant Just Desvern","Santa Coloma de Cervelló","Sant Joan Despí","Sant Climent de Llobregat","Sant Boi de Llobregat","Sant Climent de Llobregat","Santa Coloma de Gramenet",
                "Sant Adrià del Besòs","Sitges","Sant Pere de Ribes","Santa Susanna","Sant Iscle de Vilalta","Sant Cebrià de Vallalta", "Sant Vicenç de Montalt","Sant Andreu de Llavaneres",
                "Sant Pol de Mar","Seva","Sant Martí de Centelles","Santa Eulàlia de Riuprimer","Santa Maria de Besora", "Sora","Sant Quirze de Besora","Sant Pere de Torelló",
                "Sant Agustí de Lluçanès","Sant Vicenç de Torelló","Sant Boi de Lluçanès","Sant Hipòlit de Voltregà","Santa Cecilia de Voltregà","Sant Bartolomeu del Grau","Sant Julià de Vilatorta",
                "Sant Sadurní d'Osmort","Santa Eugènia de Berga","Sant Cugat del Vallès","Sant Llorenç Savall","Santmenat","Sabadell","Santa Perpètua de Mogoda", "Sant Quirze del Vallès",
                "Sant Antoni de Vilamajor","Sant Celoni","Sant Esteve de Palautordera","Sant Feliu de Codines","Sant Fost de Campsentelles","Sant Pere de Vilamajor","Santa Eulàlia de Ronçana",
                "Santa Maria de Martorelles","Santa Maria de Palautordera","Sant Climent Sescebes","Sant Feliu de Guíxols","Sant Miquel de Fluvià","Sant Pere Pescador","Siurana","Sales de Llierca",
                "Sant Aniol de Finestres","Sant Feliu de Pallerols","Sant Jaume de Llierca", "Santa Pau","Salt","Sant Gregori", "Sant Julià de Ramis","Sarrià de Ter","Sant Martí Vell",
                "Sant Andreu Salou","Sant Miquel de Campmajor","Serinyà", "Sant Joan de les Abadesses", "Sant Pau de Segúries", "Setcases","Sant Hilari Sacalm", "Sils", "Santa Coloma de Farners",
                "Sant Julià del Llor i Bonmatí","Susqueda", "Sant Jaume dels Domenys","Santa Oliva","Sarral","Solivella","Senan","Santa Coloma de Queralt","Savallà del Comtat","Sant Jaume d'Enveja",
                "Santa Bàrbara","Sant Carles de la Ràpita","Salomó","Salou",

                "Torrelavit", "Torrelles de Foix","Talamanca","Tordera","Teià","Tiana","Tona","Taradell","Travertet","Torelló","Tavèrnoles","Terrassa","Tagamanent",
                "Torrent","Tossa de Mar","Torroella de Fluvià","Torroella de Montgrí","Tivenys","Tortosa","Torroja del Priorat","Tivissa", "Tarragona","Torredembarra",

                "Ullastrell","Ullà","Ullastret","Urús","Vilabertran","Vilafant","Vilajuïga","Vilamacolum","Vila-sacra","Vilanova de la Muga","Verges", "Ulldecona","Ulldemolins",

                "Vilafranca del Penedès", "Vilobí del Penedès", "Veciana","Vilanova del Camí",  "Vallbona d'Anoia","Vallirana","Viladecans","Vilanova i la Geltrú","Vilassar de Dalt",
                "Vilassar de Mar","Vidrà", "Viladrau","Vilanova de Sau","Vic", "Vacarisses","Viladecavalls","Vallgorguina","Vallromanes","Vilanova del Vallès","Vilalba Sasserra",
                "Vall d'en Bas",  "Vall de Bianya","Vilablareix","Vilademuls","Vallfogona de Ripollès","Vilallonga de Ter","Vidreres", "Vilobí d'Onyar","Valls","Vila-rodona","Vallmoll",
                "Vilabella","Vinyols i els Arcs","Vilaplana","Vilanova d'Escornalbou","Vandellòs i l'Hospitalet de l'Infant","Vallfogona de Riucorb","Vimbodí i Poblet","Vallclara",
                "Vilanova de Prades","Vilaverd","Vinebre","Vila-seca","Vespella de Gaià","Vilallonga del Camp","Vilalba dels Arcs",

                "Xerta"
        );

    }
    @NonNull
    private List<String> obtenirLlistaDeComarques() {
        return Arrays.asList(
                "Alt Empordà","Garrotxa","Pla de l'Estany","Gironès","Baix Empordà", "Ripollès","La Selva","Montsià",
                "Baix Ebre", "Terra Alta","Ribera d'Ebre","Priorat","Baix Camp","Segrià","Garrigues","Tarragonès",
                "Alt Camp", "Conca de Barberà", "Pla d'Urgell","Baix Penedès","Garraf", "Alt Penedès","Urgell",
                "Noguera", "Segarra","Anoia","Pallars Jussà","Alta Ribagorça","Val d'Aran","Pallars Sobirà",
                "Alt Urgell","Baix Llobregat","Barcelonès","Maresme","Solsonès","Cerdanya","Lluçanès", "Berguedà",
                "Osona","Bages","Moianès","Vallès Occidental","Vallès Oriental");
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