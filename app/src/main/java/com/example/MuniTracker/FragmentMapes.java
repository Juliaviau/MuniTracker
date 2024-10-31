package com.example.MuniTracker;


import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.MuniTracker.databinding.FragmentMapesBinding;
import com.google.android.material.bottomsheet.BottomSheetDialog;

public class FragmentMapes extends Fragment {
    private WebView webView;
    private Context context;
    FragmentMapesBinding binding;
    private String lastComarcaId;
    private String originalViewBox;
    MapesHelper territoryHelper;
    // MunicipiViewModel viewModel;
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = FragmentMapesBinding.inflate(getLayoutInflater());
       // setContentView(binding.getRoot());
    }

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_mapes, container, false);
        territoryHelper = new MapesHelper(context);
        webView = view.findViewById(R.id.webView);
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);

        webView.setBackgroundColor(Color.TRANSPARENT);
        webSettings.setBuiltInZoomControls(true);
        webSettings.setDisplayZoomControls(false);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setUseWideViewPort(true);

        loadMap(R.raw.municipis);

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                marcarMunicipisVisitats();  // Marcar municipios visitados una vez que se carga el SVG
            }
        });


        webView.addJavascriptInterface(new WebAppInterface(), "Android");
        webView.setWebViewClient(new WebViewClient());

        Button buttonProvincies = view.findViewById(R.id.btProvincies);
        Button buttonVegueries = view.findViewById(R.id.btVegueries);
        Button buttonComarques = view.findViewById(R.id.btComarques);
        Button buttonMunicipis = view.findViewById(R.id.btMunicipis);

        buttonProvincies.setOnClickListener(v -> {
            loadMap(R.raw.provinscies);
            webView.setWebViewClient(new WebViewClient() {
                @Override
                public void onPageFinished(WebView view, String url) {
                    super.onPageFinished(view, url);
                    pintarProvinciesPerVisites();
                }
            });
        });
        buttonVegueries.setOnClickListener(v -> {
            loadMap(R.raw.vegueries);
            webView.setWebViewClient(new WebViewClient() {
                @Override
                public void onPageFinished(WebView view, String url) {
                    super.onPageFinished(view, url);
                    pintarVegueriesPerVisites();
                }
            });
        });
        buttonComarques.setOnClickListener(v -> {
            loadMap(R.raw.comarquesactu);
            webView.setWebViewClient(new WebViewClient() {
                @Override
                public void onPageFinished(WebView view, String url) {
                    super.onPageFinished(view, url);
                    //if (url.contains("comarques")) { // Verifica que el mapa de comarcas esté completamente cargado
                        colorearComarcasPorVisitas();
                    //}
                }
            });
        });

        buttonMunicipis.setOnClickListener(v -> loadMap(R.raw.municipis));

        return view;
    }

    private void pintarVegueriesPerVisites() {
        MunicipiViewModel viewModel = new ViewModelProvider(this).get(MunicipiViewModel.class);

        List<String> listaVegueries = obtenerListaDeVegueries();

        //Per a cada comarca
        for (String vegueriaId : listaVegueries) {
            Log.d("PROVINCIES", vegueriaId);
            //Retorna quants municipis estan visitats
            viewModel.obtenerPorcentajeVisitadosVegueria(vegueriaId).observe(getViewLifecycleOwner(), porcentaje -> {
                //Log.d("PROVINCIES", vegueriaId + " " + porcentaje);
                //Quantitat municipis per comarca
                int quantitat = territoryHelper.getCantidadMunicipiosPorVegueria(vegueriaId);
                //Calcula el percentatge
                double percentarge = quantitat == 0 ? 0 : (double) (porcentaje * 100) / quantitat;

                Log.d("PROVINCIES", vegueriaId + " " + porcentaje+ " " + quantitat+ " " + percentarge);
                //I assigna el color
                int color = obtenerColorPorPorcentaje(percentarge);
                canviarColorSVG(vegueriaId, String.format("#%06X", (0xFFFFFF & color)));  // Cambia el color de la comarca en el mapa
            });
        }
    }

    private void pintarProvinciesPerVisites() {
        MunicipiViewModel viewModel = new ViewModelProvider(this).get(MunicipiViewModel.class);

        List<String> listaProvincies = obtenerListaDeProvincies();

        //Per a cada comarca
        for (String provinciaId : listaProvincies) {
            //Retorna quants municipis estan visitats
            viewModel.obtenerPorcentajeVisitadosProvincia(provinciaId).observe(getViewLifecycleOwner(), porcentaje -> {
                //Quantitat municipis per comarca
                int quantitat = territoryHelper.getCantidadMunicipiosPorProvincia(provinciaId);
                //Calcula el percentatge
                double percentarge = quantitat == 0 ? 0 : (double) (porcentaje * 100) / quantitat;

                //I assigna el color
                int color = obtenerColorPorPorcentaje(percentarge);
                canviarColorSVG(provinciaId, String.format("#%06X", (0xFFFFFF & color)));  // Cambia el color de la comarca en el mapa
            });
        }
    }

    private void colorearComarcasPorVisitas() {
        MunicipiViewModel viewModel = new ViewModelProvider(this).get(MunicipiViewModel.class);

        List<String> listaComarcas = obtenerListaDeComarcas();

        //Per a cada comarca
        for (String comarcaId : listaComarcas) {
            //Retorna quants municipis estan visitats
            viewModel.obtenerPorcentajeVisitadosComarca(comarcaId).observe(getViewLifecycleOwner(), porcentaje -> {
                //Quantitat municipis per comarca
                int quantitat = territoryHelper.getCantidadMunicipiosPorComarca(comarcaId);
                //Calcula el percentatge
                double percentarge = quantitat == 0 ? 0 : (double) (porcentaje * 100) / quantitat;
                //I assigna el color
                int color = obtenerColorPorPorcentaje(percentarge);
                canviarColorSVG(comarcaId, String.format("#%06X", (0xFFFFFF & color)));  // Cambia el color de la comarca en el mapa
            });
        }
    }

    private List<String> obtenerListaDeComarcas() {
        return Arrays.asList(
                "Alt Empordà","Garrotxa","Pla de l'Estany","Gironès","Baix Empordà", "Ripollès","La Selva","Montsià",
                "Baix Ebre", "Terra Alta","Ribera d'Ebre","Priorat","Baix Camp","Segrià","Garrigues","Tarragonès",
                "Alt Camp", "Conca de Barberà", "Pla d'Urgell","Baix Penedès","Garraf", "Alt Penedès","Urgell",
                "Noguera", "Segarra","Anoia","Pallars Jussà","Alta Ribagorça","Val d'Aran","Pallars Sobirà",
                "Alt Urgell","Baix Llobregat","Barcelonès","Maresme","Solsonès","Cerdanya","Lluçanès", "Berguedà",
                "Osona","Bages","Moianès","Vallès Occidental","Vallès Oriental");
    }

    private List<String> obtenerListaDeVegueries() {
        return Arrays.asList(
                "Alt Pirineu i Aran","Barcelona","Camp de Tarragona","Catalunya Central","Girona",
                "Lleida","Penedès","Terres de l'Ebre");
    }

    private List<String> obtenerListaDeProvincies() {
        return Arrays.asList(
                "Província de Barcelona","Província de Girona","Província de Lleida","Província de Tarragona");
    }


    private void loadMap(int mapResource) {
        String svgContent = loadSvgFromResources(mapResource);
        if (svgContent != null) {
            String svgData = "<html><body style=\"margin: 0; padding: 0;\">" + svgContent + "</body></html>";
            webView.loadDataWithBaseURL(null, svgData, "text/html", "UTF-8", null);

            // Marcar municipios visitados solo si es el mapa de municipios
            if (mapResource == R.raw.municipis) {
                webView.setWebViewClient(new WebViewClient() {
                    @Override
                    public void onPageFinished(WebView view, String url) {
                        super.onPageFinished(view, url);
                        marcarMunicipisVisitats();  // Solo ejecutado si se carga el mapa de municipios
                        inicializeMap();
                    }
                });
            }
        }
    }

    private void inicializeMap() {
        webView.evaluateJavascript(
                "document.getElementsByTagName('svg')[0].getAttribute('viewBox');",
                value -> {
                    originalViewBox = value; // Almacenar el viewBox original
                    Log.d("WebViewLog", "Mapa inicializado con viewBox: " + originalViewBox); // Log de inicialización
                }
        );
    }

    private String loadSvgFromResources(int resourceId) {
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

    public class WebAppInterface {

        @JavascriptInterface
        public void logMessage(final String message) {
            Log.d("WebViewLog", message); // Enviar el log a Android Studio
        }

        @JavascriptInterface
        public void showProvince(final String comarcaId, final String originalColor, final String originalViewBox) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    lastComarcaId = comarcaId;
                    showProvinciaBottomSheet(comarcaId, originalColor, originalViewBox);
                }
            });
        }

        @JavascriptInterface
        public void showComarca(final String comarcaId, final String originalColor, final String originalViewBox) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    lastComarcaId = comarcaId;
                    showComarcaBottomSheet(comarcaId, originalColor, originalViewBox);
                }
            });
        }

        @JavascriptInterface
        public void showVegueria(final String comarcaId, final String originalColor, final String originalViewBox) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    lastComarcaId = comarcaId;
                    showVegueriaBottomSheet(comarcaId, originalColor, originalViewBox);
                }
            });
        }

        @JavascriptInterface
        public void showMunicipi(final String comarcaId, final String originalColor, final String originalViewBox) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    lastComarcaId = comarcaId;
                    showMunicipiBottomSheet(comarcaId, originalColor, originalViewBox);
                }
            });
        }
    }

    private void showComarcaBottomSheet(String comarcaId, String originalColor, String originalViewBox) {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(context);
        View bottomSheetView = getLayoutInflater().inflate(R.layout.bottom_sheet_dialog, null);

        TextView comarcaInfo = bottomSheetView.findViewById(R.id.zonaNom);
        comarcaInfo.setText(comarcaId);

        // Cambiar el color de la comarca pulsada a verde
        canviarColorSVG(comarcaId, "white");

        // Cerrar el BottomSheetDialog con el botón de cerrar
        Button closeButton = bottomSheetView.findViewById(R.id.closeButton);
        closeButton.setOnClickListener(v -> {
            canviarColorSVG(comarcaId, originalColor); // Restaurar el color original
            resetZoom(originalViewBox); // Restaurar el viewBox original
            bottomSheetDialog.dismiss(); // Cierra el BottomSheetDialog
        });

        // Restaurar el color y el viewBox original al cerrar el BottomSheetDialog (de cualquier manera)
        bottomSheetDialog.setOnDismissListener(dialog -> {
            canviarColorSVG(comarcaId, originalColor); // Restaurar el color original
            resetZoom(originalViewBox); // Restaurar el viewBox original
        });

        TextView infoMuni = bottomSheetView.findViewById(R.id.zonaIfnfo);
        NestedScrollView scroll = bottomSheetView.findViewById(R.id.scrollView);
        Button markAsVisitedButton = bottomSheetView.findViewById(R.id.visit);
        View viewBottom = bottomSheetView.findViewById(R.id.viewbottom);

        infoMuni.setText("");
        closeButton.setVisibility(View.GONE);
        scroll.setVisibility(View.GONE);  // Oculta el scroll si no hay visitas
        markAsVisitedButton.setVisibility(View.GONE);
        viewBottom.setVisibility(View.GONE);


        bottomSheetDialog.setContentView(bottomSheetView);
        bottomSheetDialog.show();
    }

    private void showProvinciaBottomSheet(String provinciaId, String originalColor, String originalViewBox) {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(context);
        View bottomSheetView = getLayoutInflater().inflate(R.layout.bottom_sheet_dialog, null);

        TextView provinciaInfo = bottomSheetView.findViewById(R.id.zonaNom);
        provinciaInfo.setText(provinciaId);

        // Cambiar el color de la comarca pulsada a verde
        canviarColorSVG(provinciaId, "white");

        // Cerrar el BottomSheetDialog con el botón de cerrar
        Button closeButton = bottomSheetView.findViewById(R.id.closeButton);
        closeButton.setOnClickListener(v -> {
            canviarColorSVG(provinciaId, originalColor); // Restaurar el color original
            resetZoom(originalViewBox); // Restaurar el viewBox original
            bottomSheetDialog.dismiss(); // Cierra el BottomSheetDialog
        });

        // Restaurar el color y el viewBox original al cerrar el BottomSheetDialog (de cualquier manera)
        bottomSheetDialog.setOnDismissListener(dialog -> {
            canviarColorSVG(provinciaId, originalColor); // Restaurar el color original
            resetZoom(originalViewBox); // Restaurar el viewBox original
        });


        TextView infoMuni = bottomSheetView.findViewById(R.id.zonaIfnfo);
        NestedScrollView scroll = bottomSheetView.findViewById(R.id.scrollView);
        Button markAsVisitedButton = bottomSheetView.findViewById(R.id.visit);
        View viewBottom = bottomSheetView.findViewById(R.id.viewbottom);

        infoMuni.setText("");
        closeButton.setVisibility(View.GONE);
        scroll.setVisibility(View.GONE);  // Oculta el scroll si no hay visitas
        markAsVisitedButton.setVisibility(View.GONE);
        viewBottom.setVisibility(View.GONE);

        bottomSheetDialog.setContentView(bottomSheetView);
        bottomSheetDialog.show();
    }

    private void showVegueriaBottomSheet(String vegueriaId, String originalColor, String originalViewBox) {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(context);
        View bottomSheetView = getLayoutInflater().inflate(R.layout.bottom_sheet_dialog, null);

        TextView vegueriaInfo = bottomSheetView.findViewById(R.id.zonaNom);
        vegueriaInfo.setText(vegueriaId);

        // Cambiar el color de la comarca pulsada a verde
        canviarColorSVG(vegueriaId, "white");

        // Cerrar el BottomSheetDialog con el botón de cerrar
        Button closeButton = bottomSheetView.findViewById(R.id.closeButton);
        closeButton.setOnClickListener(v -> {
            canviarColorSVG(vegueriaId, originalColor); // Restaurar el color original
            resetZoom(originalViewBox); // Restaurar el viewBox original
            bottomSheetDialog.dismiss(); // Cierra el BottomSheetDialog
        });

        // Restaurar el color y el viewBox original al cerrar el BottomSheetDialog (de cualquier manera)
        bottomSheetDialog.setOnDismissListener(dialog -> {
            canviarColorSVG(vegueriaId, originalColor); // Restaurar el color original
            resetZoom(originalViewBox); // Restaurar el viewBox original
        });


        TextView infoMuni = bottomSheetView.findViewById(R.id.zonaIfnfo);
        NestedScrollView scroll = bottomSheetView.findViewById(R.id.scrollView);
        Button markAsVisitedButton = bottomSheetView.findViewById(R.id.visit);
        View viewBottom = bottomSheetView.findViewById(R.id.viewbottom);

        infoMuni.setText("");
        closeButton.setVisibility(View.GONE);
        scroll.setVisibility(View.GONE);  // Oculta el scroll si no hay visitas
        markAsVisitedButton.setVisibility(View.GONE);
        viewBottom.setVisibility(View.GONE);

        bottomSheetDialog.setContentView(bottomSheetView);
        bottomSheetDialog.show();
    }

    private void showMunicipiBottomSheet(String municipiId, String originalColor, String originalViewBox) {


        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(context);
        View bottomSheetView = getLayoutInflater().inflate(R.layout.bottom_sheet_dialog, null);

        TextView municipiInfo = bottomSheetView.findViewById(R.id.zonaNom);
        municipiInfo.setText(municipiId);

        Button markAsVisitedButton = bottomSheetView.findViewById(R.id.visit);
        MunicipiViewModel viewModel= new ViewModelProvider(this).get(MunicipiViewModel.class);
        Button closeButton = bottomSheetView.findViewById(R.id.closeButton);
        View viewBottom = bottomSheetView.findViewById(R.id.viewbottom);

        Log.d("Coloer " , originalColor);
        boolean municipiVisitat = comparaColor(originalColor,"rgb(96, 82, 110)");
        TextView infoMuni = bottomSheetView.findViewById(R.id.zonaIfnfo);
        // Cambia el tipo de ScrollView a NestedScrollView
        NestedScrollView scroll = bottomSheetView.findViewById(R.id.scrollView);

        if (municipiVisitat) {

            closeButton.setVisibility(View.VISIBLE);
            scroll.setVisibility(View.VISIBLE);  // Oculta el scroll si no hay visitas
            markAsVisitedButton.setVisibility(View.VISIBLE);
            viewBottom.setVisibility(View.VISIBLE);


            infoMuni.setText("Visites anteriors");
            markAsVisitedButton.setText("Afegir visita");

            LinearLayout visitasContainer = bottomSheetView.findViewById(R.id.visitasContainer);



            viewModel.getVisitasByMunicipiId(municipiId).observe(getViewLifecycleOwner(), visitas -> {
                // Limpia el contenedor antes de agregar nuevos elementos
                visitasContainer.removeAllViews();

                for (Visita visita : visitas) {
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
                    visitaTextView.setOnClickListener(v -> showNotasDialog(visita));
                    visitasContainer.addView(visitaTextView);
                }
            });

        } else {
            infoMuni.setText("");
            closeButton.setVisibility(View.GONE);
            scroll.setVisibility(View.GONE);  // Oculta el scroll si no hay visitas
            markAsVisitedButton.setText("Marcar com a visitat");
            viewBottom.setVisibility(View.GONE);
        }

        // En tu código para marcar el municipio

        MapesHelper.TerritoryData territoryData = territoryHelper.getTerritoryData(municipiId);


        markAsVisitedButton.setOnClickListener(v -> {
            VisitaDialogFragment bottomSheet = new VisitaDialogFragment((fecha, notas) -> {

                AtomicReference<Boolean> isVisited = new AtomicReference<>(false);
                viewModel.getMunicipisVisitats().observe(getViewLifecycleOwner(), municipisVisitats -> {
                    for (Municipi municipi : municipisVisitats) {
                        if (municipi.id.equals(municipiId)) {
                            isVisited.set(true);
                            break;
                        }
                    }
                    if (!isVisited.get()) {
                        Log.d("CREA MUNI ", municipiId + " " + territoryData.comarcaId+ " " + territoryData.vegueriaId+ " " + territoryData.provinciaId);
                        Municipi municipi = new Municipi(municipiId, municipiId, true, territoryData.comarcaId, territoryData.vegueriaId, territoryData.provinciaId);
                        viewModel.afegirMunicipi(municipi);
                    }
                });
                Visita visita = new Visita(municipiId, fecha, notas);

                viewModel.afegirVisita(visita);

                marcarMunicipisVisitats();
            });

            bottomSheet.show(getParentFragmentManager(), "AgregarVisitaBottomSheet");

            bottomSheetDialog.dismiss();
        });


        closeButton.setOnClickListener(v -> {
            canviarColorSVG(municipiId, originalColor); // Restaurar el color original
            resetZoom(originalViewBox); // Restaurar el viewBox original
            bottomSheetDialog.dismiss();
        });

        bottomSheetDialog.setOnDismissListener(dialog -> {
            canviarColorSVG(municipiId, originalColor); // Restaurar el color original
            resetZoom(originalViewBox); // Restaurar el viewBox original
            bottomSheetDialog.dismiss();
        });

        bottomSheetDialog.setContentView(bottomSheetView);
        bottomSheetDialog.show();
    }

    private boolean comparaColor (String color1, String color2) {
        if (color1.equalsIgnoreCase(color2)) {
            return true;
        } else {
            return false;
        }
    }

    private void showNotasDialog(Visita visita) {
        // Puedes usar un AlertDialog para mostrar las notas
        new AlertDialog.Builder(context)
                .setTitle("Notes de la visita")
                .setMessage(visita.notes)
                .setPositiveButton("OK", null)
                .show();
    }

    private void marcarMunicipisVisitats() {
        MunicipiViewModel viewModel = new ViewModelProvider(FragmentMapes.this).get(MunicipiViewModel.class);
        viewModel.getMunicipisVisitats().observe(getViewLifecycleOwner(), municipisVisitats -> {
            for (Municipi municipi : municipisVisitats) {
                canviarColorSVG(municipi.id, "#60526e"); // Canvia el color de municipis visitats
            }
        });
    }

    private void resetZoom(String originalViewBox) {
        String jsCode = "document.getElementsByTagName('svg')[0].setAttribute('viewBox', '" + originalViewBox + "');";
        webView.evaluateJavascript(jsCode, null);
    }

    public int obtenerColorPorPorcentaje(double porcentaje) {
        if (porcentaje == 0.0) return ContextCompat.getColor(context, R.color.blau_mapa);
        else if (porcentaje < 25.0) return ContextCompat.getColor(context, R.color.lleg25);
        else if (porcentaje < 50.0) return ContextCompat.getColor(context, R.color.lleg50);
        else if (porcentaje < 75.0) return ContextCompat.getColor(context, R.color.lleg75);
        else return ContextCompat.getColor(context, R.color.llegComplet);
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        marcarMunicipisVisitats();
    }

    private void canviarColorSVG(String comarcaId, String color) {
        String escapedComarcaId = comarcaId.replace("'", "\\'");
        String jsCode = "document.getElementById('" + escapedComarcaId + "').style.fill = '" + color + "';";
        webView.evaluateJavascript(jsCode, null);
    }
}