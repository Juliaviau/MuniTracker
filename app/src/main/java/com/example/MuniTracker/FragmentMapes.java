package com.example.MuniTracker;


import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.webkit.ValueCallback;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
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
import java.util.concurrent.CountDownLatch;
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

        LinearLayout layout = view.findViewById(R.id.legendLayout);

        loadMap(R.raw.municipis);

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                marcarMunicipisVisitats();  // Marcar municipios visitados una vez que se carga el SVG
            }
        });
        layout.setVisibility(View.GONE);

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
                    layout.setVisibility(View.VISIBLE);
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
                    layout.setVisibility(View.VISIBLE);
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
                    layout.setVisibility(View.VISIBLE);
                    colorearComarcasPorVisitas();
                }
            });
        });
        buttonMunicipis.setOnClickListener(v -> { layout.setVisibility(View.GONE);  loadMap(R.raw.municipis);});





        SearchView searchView = view.findViewById(R.id.searchView);
        RecyclerView resultsRecyclerView = view.findViewById(R.id.resultsRecyclerView);

        // Inicialitzem la llista de municipis i l'adapter
        List<String> municipis = obtenerListaDeMunicipis(); // Llista dels noms dels municipis
        SearchAdapter adapter = new SearchAdapter(municipis);
        resultsRecyclerView.setAdapter(adapter);
        resultsRecyclerView.setLayoutManager(new LinearLayoutManager(context));

        // Configurar l'adapter amb l'element seleccionat
        adapter.setOnItemClickListener(municipi -> {
            //canviarColorSVG(municipi,"white");  // Funció que pintarà el municipi seleccionat al mapa SVG
            searchView.setQuery(municipi, true); // Esborra el text del SearchView
            searchView.clearFocus(); // Amaga el teclat
            resultsRecyclerView.setVisibility(View.GONE); // Oculta la llista després de seleccionar un municipi
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (municipis.contains(query)) {
                    //canviarColorSVG(query,"white");  // Pintem el municipi seleccionat


                    /*String a;
                    webView.evaluateJavascript(
                            "document.getElementsByTagName('svg')[0].getAttribute('viewBox');",
                            value -> {
                                originalViewBox = value; // Almacenar el viewBox original
                                Log.d("WebViewLog", "Mapa inicializado con viewBox: " + originalViewBox); // Log de inicialización
                            }
                    );*/
                    String color = obtenirColorSVG(query) ;

                    Log.i("color municipi ", query + " " + color);

                    showMunicipiBottomSheet(query, color, "0 0 425 400");//colors per ocmparar


                    searchView.setQuery("", true);
                }
                return false; // No cal fer res especial quan es prem "Enter"
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                // Filtrar els municipis que contenen el text introduït
                String query = newText.toLowerCase();
                List<String> filteredMunicipis = new ArrayList<>();
                for (String municipi : municipis) {
                    if (municipi.toLowerCase().contains(query)) {
                        filteredMunicipis.add(municipi);
                    }
                }
                adapter.updateList(filteredMunicipis);

                // Mostrar o ocultar la RecyclerView segons hi hagi resultats
                if (filteredMunicipis.isEmpty()) {
                    resultsRecyclerView.setVisibility(View.GONE);
                } else {
                    resultsRecyclerView.setVisibility(View.VISIBLE);
                }
                return true;
            }
        });

        return view;
    }


    private void pintarVegueriesPerVisites() {
        MunicipiViewModel viewModel = new ViewModelProvider(this).get(MunicipiViewModel.class);

        List<String> listaVegueries = obtenerListaDeVegueries();

        //Per a cada comarca
        for (String vegueriaId : listaVegueries) {
            //Retorna quants municipis estan visitats
            viewModel.obtenerPorcentajeVisitadosVegueria(vegueriaId).observe(getViewLifecycleOwner(), porcentaje -> {
                //Log.d("PROVINCIES", vegueriaId + " " + porcentaje);
                //Quantitat municipis per comarca
                int quantitat = territoryHelper.getCantidadMunicipiosPorVegueria(vegueriaId);
                //Calcula el percentatge
                double percentarge = quantitat == 0 ? 0 : (double) (porcentaje * 100) / quantitat;

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
                canviarColorSVG(provinciaId, String.format("#%06X", (0xFFFFFF & color)));
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


    @NonNull
    private List<String> obtenerListaDeMunicipis() {


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
                "El Pinell de Brai","El Prat del Comte",

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
                "La Nou de Gaià","La Pobla de Montornès","La Secuita","Queralbs","Quart", "Querol",

                "Mediona","Montmaneu","Masquefa","Manresa","Mura","Marganell","Monistrol de Montserrat","Martorell","Mataró","Montgat","Malgrat de Mar","Muntanyola","Montesquiu", "Malla","Manlleu","Montcada i Reixac","Matadepera",
                "Martorelles","Mollet del Vallès", "Montmeló","Montornès del Vallès","Montseny","Maçanet de Cabrenys","Meranges","Mont-ras","Maià de Montcal","Mieres","Montagut i Oix","Molló",
                "Massanes","Maçanet de la Selva","Madremanya","Mont-ral","Montferri", "Maspujols", "Mont-roig del Camp", "Masllorenç", "Montblanc", "Mas de Barberans", "Masdenverge", "Margalef",
                "Marçà","Móra d'Ebre","Móra la Nova","Miravet", "Montbrió del Camp",

                "Navarcles","Navàs","Nulles",

                "Olesa de Bonesvalls", "Olèrdola","Orpí", "Òdena","Olesa de Montserrat","Olivella","Òrrius","Orís","Ordis","Olot","Ogassa","Osor",

                "Puigdàlber","Pacs del Penedès",  "Pontons","Pujalt","Piera","Premià de Dalt","Premià de Mar", "Palafolls","Pineda de Mar","Palau-solità i Plegamans", "Polinyà","Parets del Vallès",
                "Pau","Peralada","Palafrugell","Palamós","Pont de Molins","Parlavà",  "Portbou","Palol de Revardit", "Porqueres", "Pardines","Planoles","Puigpelat","Prades","Pratdip","Paüls","Pontils",
                "Passanant i Belltall","Pira","Poboleda","Pradell de la Teixeta","Porrera","Perafort","Palau-saverdera","Pals","Prats i Sansor","Puigcerdà",

                "Rubió","Rupit i Pruit","Roda de Ter","Rellinars","Rubí","Rajadell","Ripollet","Regencós","Roses","Riudaura","Ripoll",  "Ribes de Freser","Riudarenes","Rodonyà","Riudecols",
                "Riudecanyes","Riudoms","Reus","Roquetes","Rocafort de Queralt","Riba-roja d'Ebre","Rasquera","Roda de Berà","Renau",

                "Subirats", "Santa Margarida i els Monjos", "Sant Llorenç d'Hortons", "Sant Martí Sarroca", "Santa Fe del Penedès", "Sant Cugat Sesgarrigues", "Sant Quintí de Mediona",
                "Sant Pere de Riudebitlles","Sant Sadurní d'Anoia", "Sant Martí Sesgueioles","Sant Martí de Tous", "Santa Margarida de Montbui", "Santa Maria de Miralles",
                "St. Pere Sallavinera","Sant Feliu Sasserra","Sallent","Sant Mateu de Bages","Suria","Santpedor","Sant Fruitós de Bages", "Sant Joan de Vilatorrada",
                "Sant Salvador de Guardiola","Sant Vicenç de Castellet","Sant Esteve Sesrovires","Sant Andreu de la Barca","Sant Feliu de LLobregat","Sant Vicenç dels Horts",
                "Sant Just Desvern","Santa Coloma de Cervelló","Sant Joan Despí","Sant Climent de Llobregat","Sant Boi de Llobregat","Sant Climent de Llobregat","Santa Coloma de Gramenet",
                "Sant Adrià del Besòs","Sitges","Sant Pere de Ribes","Santa Susanna","Sant Iscle de Vilalta","Sant Cebrià de Vallalta", "Sant Vicenç de Montalt","Sant Andreu de Llavaneres",
                "Sant Pol de Mar","Seva","Sant Martí de Centelles","Santa Eulàlia de Riuprimer","Santa Maria de Besora", "Sora","Sant Quirze de Besora","Sant Pere de Torelló",
                "Sant Agustí de LLuçanès","Sant Vicenç de Torelló","Sant Boi de Lluçanès","Sant Hipòlit de Voltregà","Santa Cecilia de Voltregà","Sant Bartolomeu del Grau","Sant Julià de Vilatorta",
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
    private List<String> obtenerListaDeComarcas() {
        return Arrays.asList(
                "Alt Empordà","Garrotxa","Pla de l'Estany","Gironès","Baix Empordà", "Ripollès","La Selva","Montsià",
                "Baix Ebre", "Terra Alta","Ribera d'Ebre","Priorat","Baix Camp","Segrià","Garrigues","Tarragonès",
                "Alt Camp", "Conca de Barberà", "Pla d'Urgell","Baix Penedès","Garraf", "Alt Penedès","Urgell",
                "Noguera", "Segarra","Anoia","Pallars Jussà","Alta Ribagorça","Val d'Aran","Pallars Sobirà",
                "Alt Urgell","Baix Llobregat","Barcelonès","Maresme","Solsonès","Cerdanya","Lluçanès", "Berguedà",
                "Osona","Bages","Moianès","Vallès Occidental","Vallès Oriental");
    }

    @NonNull
    private List<String> obtenerListaDeVegueries() {
        return Arrays.asList(
                "Alt Pirineu i Aran","Barcelona","Camp de Tarragona","Catalunya Central","Girona",
                "Lleida","Penedès","Terres de l'Ebre");
    }

    @NonNull
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
        scroll.setVisibility(View.GONE);
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
        boolean municipiVisitat = comparaColor(originalColor,"rgb(27, 58, 95)");
        TextView infoMuni = bottomSheetView.findViewById(R.id.zonaIfnfo);
        // Cambia el tipo de ScrollView a NestedScrollView
        NestedScrollView scroll = bottomSheetView.findViewById(R.id.scrollView);

        closeButton.setVisibility(View.VISIBLE);
        scroll.setVisibility(View.VISIBLE);
        markAsVisitedButton.setVisibility(View.VISIBLE);
        viewBottom.setVisibility(View.VISIBLE);

        if (municipiVisitat) {

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
                canviarColorSVG(municipi.id,  String.format("#%06X", (0xFFFFFF & ContextCompat.getColor(context, R.color.llegComplet)))); // Canvia el color de municipis visitats
            }
        });
    }

    private void resetZoom(String originalViewBox) {

        Log.d("NouZOOM", "Mapa inicializado con viewBox: " + originalViewBox);

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

    public interface ColorCallback {
        void onColorReceived(String color);
    }


    private String obtenirColorSVG(String id) {
        final String[] colorResult = {null};
        final CountDownLatch latch = new CountDownLatch(1);

        String jsCode = "document.getElementById('" + id + "').getAttribute('style');";

        webView.evaluateJavascript(jsCode, new ValueCallback<String>() {
            @Override
            public void onReceiveValue(String value) {
                if (value != null && !value.equals("null")) {
                    value = value.replace("\"", ""); // Limpiar comillas

                    // Buscar el valor de 'fill' dentro del estilo
                    String color = null;
                    String[] styles = value.split(";");
                    for (String style : styles) {
                        if (style.trim().startsWith("fill:")) {
                            color = style.split(":")[1].trim();
                            break;
                        }
                    }
                    colorResult[0] = (color != null) ? color : ""; // Asignar color encontrado
                } else {
                    colorResult[0] = ""; // Si no se encontró el color
                }
                latch.countDown(); // Liberar el latch una vez recibido el color
            }
        });

        try {
            latch.await(); // Esperar hasta que onReceiveValue llame a countDown()
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return colorResult[0];
    }



    private void canviarColorSVG(String comarcaId, String color) {
        //Toast.makeText(context,comarcaId,Toast.LENGTH_SHORT).show();
        String escapedComarcaId = comarcaId.replace("'", "\\'");
        String jsCode = "document.getElementById('" + escapedComarcaId + "').style.fill = '" + color + "';";
        webView.evaluateJavascript(jsCode, null);
    }
}