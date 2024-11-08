package com.example.MuniTracker;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.icu.text.DecimalFormat;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.DecelerateInterpolator;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.MuniTracker.databinding.FragmentEstadistiquesBinding;
import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.HorizontalBarChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class FragmentEstadistiques extends Fragment {

 private FragmentEstadistiquesBinding binding;

    private AtomicInteger visitatsProvLleida = new AtomicInteger();
    private AtomicInteger visitatsProvBarcelona = new AtomicInteger();
    private AtomicInteger visitatsProvTarragona = new AtomicInteger();
    private AtomicInteger visitatsProvGirona = new AtomicInteger();

    private Context context;
    MapesHelper mapesHelper;

    private Map<String, AtomicInteger> visitatsPerVegueria = new HashMap<>();
    private List<String> vegueries = Arrays.asList("Alt Pirineu i Aran","Barcelona","Camp de Tarragona","Catalunya Central","Girona","Lleida","Penedès","Terres de l'Ebre");

    private Map<String, AtomicInteger> visitatsPerComarcaL = new HashMap<>();
    private Map<String, AtomicInteger> visitatsPerComarcaG = new HashMap<>();
    private Map<String, AtomicInteger> visitatsPerComarcaT = new HashMap<>();
    private Map<String, AtomicInteger> visitatsPerComarcaB = new HashMap<>();
    private Map<String, AtomicInteger> visitatsPerComarca = new HashMap<>();

    private List<String> comarquesB = Arrays.asList(
            "Alt Penedès", "Anoia", "Bages", "Baix Llobregat", "Barcelonès", "Garraf", "Maresme", "Osona", "Vallès Occidental", "Vallès Oriental");//10
    private List<String> comarquesT = Arrays.asList(
            "Alt Camp", "Baix Camp", "Baix Ebre", "Baix Penedès", "Conca de Barberà", "Montsià", "Priorat", "Ribera d'Ebre", "Tarragonès", "Terra Alta");//10
    private List<String> comarquesG = Arrays.asList(
            "Alt Empordà", "Baix Empordà", "Cerdanya", "Garrotxa", "Gironès", "Pla de l'Estany", "Selva","Ripollès");//8
    private List<String> comarquesL = Arrays.asList(
            "Alta Ribagorça", "Alt Urgell", "Cerdanya", "Garrigues", "Noguera", "Pallars Jussà", "Pallars Sobirà", "Pla d'Urgell", "Segarra", "Segrià", "Solsonès", "Urgell", "Val d'Aran");//13



    private List<String> comarques = Arrays.asList(
            "Alt Penedès", "Anoia", "Bages", "Baix Llobregat", "Barcelonès", "Garraf", "Maresme", "Osona", "Vallès Occidental", "Vallès Oriental",
            "Alt Camp", "Baix Camp", "Baix Ebre", "Baix Penedès", "Conca de Barberà", "Montsià", "Priorat", "Ribera d'Ebre", "Tarragonès", "Terra Alta",
            "Alt Empordà", "Baix Empordà", "Cerdanya", "Garrotxa", "Gironès", "Pla de l'Estany", "Selva","Ripollès",
            "Alta Ribagorça", "Alt Urgell", "Cerdanya", "Garrigues", "Noguera", "Pallars Jussà", "Pallars Sobirà", "Pla d'Urgell", "Segarra", "Segrià", "Solsonès", "Urgell", "Val d'Aran");//13


    public FragmentEstadistiques() {
        // Required empty public constructor
    }

    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView (LayoutInflater inflater,ViewGroup container,Bundle savedInstanceState) {
        binding = FragmentEstadistiquesBinding.inflate(inflater, container, false);
        View view = binding.getRoot();
        mapesHelper = new MapesHelper(context);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Activity activity = getActivity();
            if (activity != null) {
                Window window = activity.getWindow();
                window.setStatusBarColor(ContextCompat.getColor(activity, R.color.fons));
                View decorView = window.getDecorView();
                decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            }
        }


        MunicipiViewModel viewModel = new ViewModelProvider(this).get(MunicipiViewModel.class);

        viewModel.nombreMunicipisVisitats().observe(getViewLifecycleOwner(), nombreMunicipisVisitats -> {

            double percentatge = (double) (nombreMunicipisVisitats * 100) / 804;
            DecimalFormat df = new DecimalFormat("#.##");
            binding.progressBar.setMax(804);
            binding.indicadorpercentatge.setText(df.format(percentatge) + " %");

            ObjectAnimator animator = ObjectAnimator.ofInt(binding.progressBar, "progress", nombreMunicipisVisitats);
            animator.setDuration(900);
            animator.setInterpolator(new DecelerateInterpolator());
            animator.start();

            binding.munvisi.setText("S'han visitat " + nombreMunicipisVisitats + " municipis d'un total de 804.");
        });

        //****************************************************

        // Observa els valors per a cada província
        viewModel.obtenirQuantitatMunicipisVisitatsProvincia("Província de Lleida").observe(getViewLifecycleOwner(), nombreMunicipisVisitats -> {
            visitatsProvLleida.set(nombreMunicipisVisitats);
            actualitzarGraficProvincies();
        });

        viewModel.obtenirQuantitatMunicipisVisitatsProvincia("Província de Girona").observe(getViewLifecycleOwner(), nombreMunicipisVisitats -> {
            visitatsProvGirona.set(nombreMunicipisVisitats);
            actualitzarGraficProvincies();
        });

        viewModel.obtenirQuantitatMunicipisVisitatsProvincia("Província de Barcelona").observe(getViewLifecycleOwner(), nombreMunicipisVisitats -> {
            visitatsProvBarcelona.set(nombreMunicipisVisitats);
            actualitzarGraficProvincies();
        });

        viewModel.obtenirQuantitatMunicipisVisitatsProvincia("Província de Tarragona").observe(getViewLifecycleOwner(), nombreMunicipisVisitats -> {
            visitatsProvTarragona.set(nombreMunicipisVisitats);
            actualitzarGraficProvincies();
        });

        //****************************************************

        // Inicialitza el Map amb totes les vegueries
        for (String vegueria : vegueries) {
            visitatsPerVegueria.put(vegueria, new AtomicInteger());
        }

        // Observa les dades de cada vegueria i actualitza el Map
        for (String vegueria : vegueries) {
            viewModel.obtenirQuantitatMunicipisVisitatsVegueria(vegueria).observe(getViewLifecycleOwner(), nombreMunicipisVisitats -> {
                visitatsPerVegueria.get(vegueria).set(nombreMunicipisVisitats);
                actualitzarGraficVegueria();
            });
        }

        //*******************************************************

        /*LinearLayout progressContainer = getView().findViewById(R.id.progress_container);

        for (String comarca : comarques) {
            visitatsPerComarcaL.put(comarca, new AtomicInteger());
        }

        for (String comarca : comarques) {
            viewModel.obtenirQuantitatMunicipisVisitatsComarca(comarca).observe(getViewLifecycleOwner(), nombreMunicipisVisitats -> {
                int quantitatMunicipisComarca = mapesHelper.obtenirQuantitatMunicipisPerComarca(comarca);
                int porcentajeVisitado = (int) ((nombreMunicipisVisitats / (float) quantitatMunicipisComarca) * 100);

                // Agregar un nuevo ProgressBar para la comarca
                View progressItem = LayoutInflater.from(getContext()).inflate(R.layout.progress_item, progressContainer, false);

                // Configura cada elemento del ProgressBar
                TextView comarcaName = progressItem.findViewById(R.id.comarca_name);
                ProgressBar progressBar = progressItem.findViewById(R.id.progress_bar);
                TextView percentageText = progressItem.findViewById(R.id.percentage_text);

                comarcaName.setText(comarca);
                progressBar.setMax(100); // Máximo del ProgressBar en 100%
                progressBar.setProgress(porcentajeVisitado); // Porcentaje de municipios visitados
                percentageText.setText(porcentajeVisitado + "%"); // Texto del porcentaje

                // Agrega el elemento a la vista contenedora
                progressContainer.addView(progressItem);


            });
        }*/


        // Lista para almacenar las comarcas con su porcentaje
        List<Pair<String, Integer>> comarcaPorcentajes = new ArrayList<>();

        AtomicInteger remainingComarques = new AtomicInteger(comarques.size()); // Para saber cuántas comarcas aún nos quedan por procesar

// Cargar los datos de una vez y actualizar la UI después de recibir todos los resultados
        for (String comarca : comarques) {
            // Inicializar los valores en el mapa (si lo necesitas)
            visitatsPerComarcaL.put(comarca, new AtomicInteger());

            // Obtenemos el dato de la cantidad de municipios visitados de forma asíncrona
            viewModel.obtenirQuantitatMunicipisVisitatsComarca(comarca).observe(getViewLifecycleOwner(), nombreMunicipisVisitats -> {
                // Obtener la cantidad total de municipios para la comarca
                int quantitatMunicipisComarca = mapesHelper.obtenirQuantitatMunicipisPerComarca(comarca);
                int porcentajeVisitado = (int) ((nombreMunicipisVisitats / (float) quantitatMunicipisComarca) * 100);

                // Guardar el porcentaje junto con el nombre de la comarca
                comarcaPorcentajes.add(new Pair<>(comarca, porcentajeVisitado));

                // Reducir el contador de comarcas restantes
                if (remainingComarques.decrementAndGet() == 0) {
                    // Una vez que hemos recibido todos los datos, ordenamos la lista
                    Collections.sort(comarcaPorcentajes, (o1, o2) -> Integer.compare(o2.second, o1.second));

                    // Actualizar la UI de forma eficiente
                    updateUI(comarcaPorcentajes);
                }
            });
        }

        //************************************************
        //Top 10 municipis visitats mes vegades grafica horitzontal

    }
    // Método para actualizar la interfaz de usuario con la lista de comarcas ordenadas
    private void updateUI(List<Pair<String, Integer>> comarcaPorcentajes) {
        LinearLayout progressContainer = getView().findViewById(R.id.progress_container);


        // Limpiar el contenedor antes de actualizarlo
        progressContainer.removeAllViews();

        // Inflar los ProgressBar ordenados
        for (Pair<String, Integer> item : comarcaPorcentajes) {
            String comarcaNombre = item.first;
            int porcentaje = item.second;

            // Inflar el ProgressBar
            View progressItem = LayoutInflater.from(getContext()).inflate(R.layout.progress_item, progressContainer, false);

            // Configurar el ProgressBar
            TextView comarcaName = progressItem.findViewById(R.id.comarca_name);
            ProgressBar progressBar = progressItem.findViewById(R.id.progress_bar);
            TextView percentageText = progressItem.findViewById(R.id.percentage_text);

            comarcaName.setText(comarcaNombre);
            progressBar.setMax(100); // Máximo del ProgressBar
            progressBar.setProgress(porcentaje); // Establecer el progreso
            percentageText.setText(porcentaje + "%"); // Establecer el porcentaje en el texto

            // Agregar el ProgressBar al contenedor
            progressContainer.addView(progressItem);
        }
    }


/*
    public void grafichorizG() {
        ArrayList<BarEntry> barEntries = new ArrayList<>();
        ArrayList<String> labels = new ArrayList<>();
        int i = 0;

        // Añade los datos al BarChart
        for (Map.Entry<String, AtomicInteger> entry : visitatsPerComarcaG.entrySet()) {
            barEntries.add(new BarEntry(i, entry.getValue().get()));
            labels.add(entry.getKey());
            i++;
        }

        HorizontalBarChart barChart = getView().findViewById(R.id.barChartG);

        BarDataSet barSet = new BarDataSet(barEntries, "");
        barSet.setColors(Color.rgb(135, 206, 250));

        BarData barData = new BarData(barSet);
        barData.setBarWidth(0.8f); // Ajuste uniforme del ancho de las barras

        barChart.setData(barData);
        barChart.setFitBars(true);
        barChart.animateY(2000);

        // Configuración del eje X
        XAxis xAxis = barChart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setLabelCount(10);  // Etiquetas visibles uniformes
        xAxis.setGranularity(1f);
        xAxis.setTextSize(14f);
        xAxis.setDrawGridLines(false);

        // Configuración del eje Y
        YAxis leftAxis = barChart.getAxisLeft();
        leftAxis.setAxisMinimum(0f);  // Elimina el espacio antes del valor 0
        leftAxis.setGranularity(1f);
        leftAxis.setDrawLabels(true);
        leftAxis.setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART);
        leftAxis.setLabelCount(labels.size(), true); // Asegura que todas las etiquetas se muestren

        // Establece un margen izquierdo para que todas las etiquetas empiecen desde el mismo punto
        leftAxis.setXOffset(20f); // Ajuste del margen izquierdo de las etiquetas
        leftAxis.setTextSize(10f); // Ajusta el tamaño de la fuente para evitar recortes
        barChart.getAxisRight().setEnabled(false); // Desactiva el eje derecho

        // Ajusta márgenes izquierdo y derecho para uniformidad en el área de barras
        barChart.setExtraLeftOffset(50f); // Ajuste uniforme del margen izquierdo para todas las gráficas
        barChart.setExtraRightOffset(10f); // Ajuste del margen derecho para uniformidad

        // Elimina la leyenda para quitar el recuadro de color
        Legend legend = barChart.getLegend();
        legend.setEnabled(false);  // Deshabilita la leyenda

        // Ajuste de espaciado inferior
        barChart.setExtraBottomOffset(30f);

        barChart.invalidate(); // Redibuja el gráfico con la nueva configuración
    }
    public void grafichorizL() {
        ArrayList<BarEntry> barEntries = new ArrayList<>();
        ArrayList<String> labels = new ArrayList<>();
        int i = 0;

        // Añade los datos al BarChart
        for (Map.Entry<String, AtomicInteger> entry : visitatsPerComarcaL.entrySet()) {
            barEntries.add(new BarEntry(i, entry.getValue().get()));
            labels.add(entry.getKey());
            i++;
        }

        HorizontalBarChart barChart = getView().findViewById(R.id.barChartL);

        BarDataSet barSet = new BarDataSet(barEntries, "");
        barSet.setColors(Color.rgb(135, 206, 250));

        BarData barData = new BarData(barSet);
        barData.setBarWidth(0.8f); // Ajuste uniforme del ancho de las barras

        barChart.setData(barData);
        barChart.setFitBars(true);
        barChart.animateY(2000);

        // Configuración del eje X
        XAxis xAxis = barChart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setLabelCount(10);  // Etiquetas visibles uniformes
        xAxis.setGranularity(1f);
        xAxis.setTextSize(14f);
        xAxis.setDrawGridLines(false);

        // Configuración del eje Y
        YAxis leftAxis = barChart.getAxisLeft();
        leftAxis.setAxisMinimum(0f);  // Elimina el espacio antes del valor 0
        leftAxis.setGranularity(1f);
        leftAxis.setDrawLabels(true);
        leftAxis.setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART);
        barChart.getAxisRight().setEnabled(false); // Desactiva el eje derecho

        // Ajusta los márgenes a la izquierda y derecha para una alineación uniforme
        barChart.setExtraLeftOffset(10f); // Ajuste del margen izquierdo
        barChart.setExtraRightOffset(10f); // Ajuste del margen derecho

        // Elimina la leyenda para quitar el recuadro de color
        Legend legend = barChart.getLegend();
        legend.setEnabled(false);  // Deshabilita la leyenda

        // Ajuste de espaciado inferior
        barChart.setExtraBottomOffset(30f);

        barChart.invalidate(); // Redibuja el gráfico con la nueva configuración
    }*/


    private void actualitzarGraficVegueria() {
        ArrayList<PieEntry> yValues = new ArrayList<>();

        // Afegeix cada entrada de vegueria al PieChart
        for (Map.Entry<String, AtomicInteger> entry : visitatsPerVegueria.entrySet()) {
            int valor = entry.getValue().get();
            if (valor > 0) {
                yValues.add(new PieEntry(valor, entry.getKey()));
            }
        }

        // Configura el DataSet
        PieDataSet dataSet = new PieDataSet(yValues, "");
        dataSet.setSliceSpace(3f);
        dataSet.setSelectionShift(10f);

        // Genera colores automáticamente para cada entrada
        ArrayList<Integer> colors = new ArrayList<>();
        for (int i = 0; i < yValues.size(); i++) {
            colors.add(ColorTemplate.MATERIAL_COLORS[i % ColorTemplate.MATERIAL_COLORS.length]);
        }
        dataSet.setColors(colors);

        // Configura el texto de los datos en el gráfico
        PieData data = new PieData(dataSet);
        data.setValueTextSize(12f);
        data.setValueTextColor(Color.WHITE);
        data.setValueFormatter(new PercentFormatter(binding.pieChartVegueria)); // Muestra valores como porcentaje

        // Estilos para el PieChart
        binding.pieChartVegueria.setData(data);
        binding.pieChartVegueria.setUsePercentValues(true); // Muestra los valores en porcentajes
        binding.pieChartVegueria.setDrawHoleEnabled(true); // Agujero en el centro
        binding.pieChartVegueria.setHoleColor(Color.TRANSPARENT); // Agujero transparente en el centro
        binding.pieChartVegueria.setHoleRadius(40f); // Radio del agujero
        binding.pieChartVegueria.setTransparentCircleRadius(45f); // Radio del círculo transparente

        // Texto al centro del gráfico
        binding.pieChartVegueria.setCenterText("Municipis visitats per Vegueries");
        binding.pieChartVegueria.setCenterTextSize(14f);
        binding.pieChartVegueria.setCenterTextColor(Color.DKGRAY);

        // Desactiva la descripción por defecto
        binding.pieChartVegueria.getDescription().setEnabled(false);

        // Configura la leyenda
        Legend legend = binding.pieChartVegueria.getLegend();
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
        legend.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        legend.setDrawInside(false);
        legend.setTextSize(12f);
        legend.setTextColor(Color.DKGRAY);

        // Habilita el modo de "word wrap" para leyendas de múltiples filas
        legend.setWordWrapEnabled(true);

        // Ajusta el espacio entre ítems para hacer la leyenda más compacta
        legend.setFormToTextSpace(6f); // Espacio entre el símbolo de la leyenda y el texto
        legend.setXEntrySpace(12f); // Espacio entre los elementos de la leyenda
        legend.setYEntrySpace(6f); // Espacio vertical entre filas

        // Desactiva los textos dentro de las secciones
        binding.pieChartVegueria.setDrawEntryLabels(false);

        // Animación para hacer el gráfico más atractivo
        binding.pieChartVegueria.animateY(1000, Easing.EaseInOutQuad);
        binding.pieChartVegueria.invalidate(); // Refresca el gráfico con los nuevos datos
    }




    // Mètode per actualitzar el gràfic amb estils i informació més atractius
    private void actualitzarGraficProvincies() {
        if (visitatsProvLleida.get() > 0 || visitatsProvBarcelona.get() > 0 ||
                visitatsProvTarragona.get() > 0 || visitatsProvGirona.get() > 0) {

            ArrayList<PieEntry> yValues = new ArrayList<>();
            yValues.add(new PieEntry(visitatsProvLleida.get(), "Lleida"));
            yValues.add(new PieEntry(visitatsProvGirona.get(), "Girona"));
            yValues.add(new PieEntry(visitatsProvBarcelona.get(), "Barcelona"));
            yValues.add(new PieEntry(visitatsProvTarragona.get(), "Tarragona"));

            // Configura el DataSet
            PieDataSet dataSet = new PieDataSet(yValues, "");
            dataSet.setSliceSpace(3f);
            dataSet.setSelectionShift(10f);

            // Colors personalitzats per cada segment
            ArrayList<Integer> colors = new ArrayList<>();
            colors.add(Color.parseColor("#FFA726")); // Taronja per Lleida
            colors.add(Color.parseColor("#66BB6A")); // Verd per Girona
            colors.add(Color.parseColor("#42A5F5")); // Blau per Barcelona
            colors.add(Color.parseColor("#FF7043")); // Vermell per Tarragona
            dataSet.setColors(colors);

            // Configura el text de les dades al gràfic
            PieData data = new PieData(dataSet);
            data.setValueTextSize(12f);
            data.setValueTextColor(Color.WHITE);
            data.setValueFormatter(new PercentFormatter(binding.pieChart)); // Mostra valors com a percentatge

            // Estils per al PieChart
            binding.pieChart.setData(data);
            binding.pieChart.setUsePercentValues(true); // Mostra els valors en percentatges
            binding.pieChart.setDrawHoleEnabled(true); // Forat al centre
            binding.pieChart.setHoleColor(Color.TRANSPARENT); // Forat transparent al centre
            binding.pieChart.setHoleRadius(40f); // Radi del forat
            binding.pieChart.setTransparentCircleRadius(45f); // Radi del cercle transparent

            binding.pieChart.setDrawEntryLabels(false);


            // Text al centre del gràfic
            binding.pieChart.setCenterText("Municipis visitats per províncies");
            binding.pieChart.setCenterTextSize(14f);
            binding.pieChart.setCenterTextColor(Color.DKGRAY);

            // Desactiva la descripció per defecte
            binding.pieChart.getDescription().setEnabled(false);

            // Configura la llegenda
            Legend legend = binding.pieChart.getLegend();
            legend.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
            legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
            legend.setOrientation(Legend.LegendOrientation.HORIZONTAL);
            legend.setDrawInside(false);
            legend.setTextSize(12f);
            legend.setTextColor(Color.DKGRAY);

            binding.pieChart.animateY(1000, Easing.EaseInOutQuad); // Animació per fer el gràfic més atractiu
            binding.pieChart.invalidate(); // Refresca el gràfic amb les noves dades
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}