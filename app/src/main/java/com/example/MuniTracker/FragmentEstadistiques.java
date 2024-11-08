package com.example.MuniTracker;

import android.animation.ObjectAnimator;
import android.graphics.Color;
import android.icu.text.DecimalFormat;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;

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


    public FragmentEstadistiques() {
        // Required empty public constructor
    }



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView (LayoutInflater inflater,ViewGroup container,Bundle savedInstanceState) {
        binding = FragmentEstadistiquesBinding.inflate(inflater, container, false);
        View view = binding.getRoot();
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


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

        //barChart.getXAxis().setAxisMinimum(0);
        //barChart.getXAxis().setAxisMaximum(4);

        //************************************************

        for (String comarca : comarquesB) {
            visitatsPerComarcaB.put(comarca, new AtomicInteger());
        }

        // Observa les dades de cada vegueria i actualitza el Map
        for (String comarca : comarquesB) {
            viewModel.obtenirQuantitatMunicipisVisitatsComarca(comarca).observe(getViewLifecycleOwner(), nombreMunicipisVisitats -> {
                visitatsPerComarcaB.get(comarca).set(nombreMunicipisVisitats);
                grafichorizB();
            });
        }

        for (String comarca : comarquesG) {
            visitatsPerComarcaG.put(comarca, new AtomicInteger());
        }

        // Observa les dades de cada vegueria i actualitza el Map
        for (String comarca : comarquesG) {
            viewModel.obtenirQuantitatMunicipisVisitatsComarca(comarca).observe(getViewLifecycleOwner(), nombreMunicipisVisitats -> {
                visitatsPerComarcaG.get(comarca).set(nombreMunicipisVisitats);
                grafichorizG();
            });
        }

        for (String comarca : comarquesT) {
            visitatsPerComarcaT.put(comarca, new AtomicInteger());
        }

        // Observa les dades de cada vegueria i actualitza el Map
        for (String comarca : comarquesT) {
            viewModel.obtenirQuantitatMunicipisVisitatsComarca(comarca).observe(getViewLifecycleOwner(), nombreMunicipisVisitats -> {
                visitatsPerComarcaT.get(comarca).set(nombreMunicipisVisitats);
                grafichorizT();
            });
        }

        for (String comarca : comarquesL) {
            visitatsPerComarcaL.put(comarca, new AtomicInteger());
        }

        // Observa les dades de cada vegueria i actualitza el Map
        for (String comarca : comarquesL) {
            viewModel.obtenirQuantitatMunicipisVisitatsComarca(comarca).observe(getViewLifecycleOwner(), nombreMunicipisVisitats -> {
                visitatsPerComarcaL.get(comarca).set(nombreMunicipisVisitats);
                grafichorizL();
            });
        }




    }

    public void grafichorizB() {

        ArrayList<BarEntry> barEntries = new ArrayList<>();
        ArrayList<String> labels = new ArrayList<>();
        int i = 0;

        // Afegeix les dades al BarChart
        for (Map.Entry<String, AtomicInteger> entry : visitatsPerComarcaB.entrySet()) {
            barEntries.add(new BarEntry(i, entry.getValue().get()));
            labels.add(entry.getKey());
            i++;
        }

        HorizontalBarChart barChart = getView().findViewById(R.id.barChartB);

        BarDataSet barSet = new BarDataSet(barEntries, "Barcelona");
        barSet.setColors(ColorTemplate.LIBERTY_COLORS);

        BarData barData = new BarData(barSet);
        barData.setBarWidth(0.9f);  // Ajuste de ancho de barra

        barChart.setData(barData);
        barChart.setFitBars(true);
        barChart.animateY(2000);

        Description description = new Description();
        description.setText("Barcelona");
        barChart.setDescription(description);

// Configura el eje X para que muestre todas las etiquetas de manera escalonada
        XAxis xAxis = barChart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setLabelCount(10); // Muestra todas las etiquetas del eje X
        xAxis.setGranularity(1f); // Espaciado consistente
        xAxis.setTextSize(16f); // Reduce el tamaño de fuente para mayor claridad
        xAxis.setDrawGridLines(false);

// Configura el eje Y para que muestre el rango completo de valores sin optimización
        YAxis leftAxis = barChart.getAxisLeft();
        leftAxis.setGranularity(1f);
        leftAxis.setDrawLabels(true);
        leftAxis.setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART);
        barChart.getAxisRight().setEnabled(false); // Desactiva el eje derecho

// Limita el rango visible para que el usuario pueda hacer scroll si no caben todas las barras
        barChart.setVisibleXRangeMaximum(10); // Muestra 6 barras a la vez, el usuario puede desplazarse

// Aumenta el espaciado inferior para que las etiquetas se muestren sin recortes
        barChart.setExtraBottomOffset(30f);

// Ajuste de leyenda
        Legend legend = barChart.getLegend();
        legend.setEnabled(true);
        legend.setTextSize(12f);

        barChart.invalidate(); // Redibuja el gráfico con la nueva configuración
    }
    public void grafichorizG() {

        ArrayList<BarEntry> barEntries = new ArrayList<>();
        ArrayList<String> labels = new ArrayList<>();
        int i = 0;

        // Afegeix les dades al BarChart
        for (Map.Entry<String, AtomicInteger> entry : visitatsPerComarcaG.entrySet()) {
            barEntries.add(new BarEntry(i, entry.getValue().get()));
            labels.add(entry.getKey());
            i++;
        }

        HorizontalBarChart barChart = getView().findViewById(R.id.barChartG);

        BarDataSet barSet = new BarDataSet(barEntries, "Girona");
        barSet.setColors(ColorTemplate.JOYFUL_COLORS);

        BarData barData = new BarData(barSet);
        barData.setBarWidth(0.9f);  // Ajuste de ancho de barra

        barChart.setData(barData);
        barChart.setFitBars(true);
        barChart.animateY(2000);

        Description description = new Description();
        description.setText("Girona");
        barChart.setDescription(description);

// Configura el eje X para que muestre todas las etiquetas de manera escalonada
        XAxis xAxis = barChart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setLabelCount(8); // Muestra todas las etiquetas del eje X
        xAxis.setGranularity(1f); // Espaciado consistente
        xAxis.setTextSize(14f); // Reduce el tamaño de fuente para mayor claridad
        xAxis.setDrawGridLines(false);

// Configura el eje Y para que muestre el rango completo de valores sin optimización
        YAxis leftAxis = barChart.getAxisLeft();
        leftAxis.setGranularity(1f);
        leftAxis.setDrawLabels(true);
        leftAxis.setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART);
        barChart.getAxisRight().setEnabled(false); // Desactiva el eje derecho

// Limita el rango visible para que el usuario pueda hacer scroll si no caben todas las barras
        barChart.setVisibleXRangeMaximum(8); // Muestra 6 barras a la vez, el usuario puede desplazarse

// Aumenta el espaciado inferior para que las etiquetas se muestren sin recortes
        barChart.setExtraBottomOffset(30f);

// Ajuste de leyenda
        Legend legend = barChart.getLegend();
        legend.setEnabled(true);
        legend.setTextSize(12f);

        barChart.invalidate(); // Redibuja el gráfico con la nueva configuración
    }
    public void grafichorizT() {

        ArrayList<BarEntry> barEntries = new ArrayList<>();
        ArrayList<String> labels = new ArrayList<>();
        int i = 0;

        // Afegeix les dades al BarChart
        for (Map.Entry<String, AtomicInteger> entry : visitatsPerComarcaT.entrySet()) {
            barEntries.add(new BarEntry(i, entry.getValue().get()));
            labels.add(entry.getKey());
            i++;
        }

        HorizontalBarChart barChart = getView().findViewById(R.id.barChartT);

        BarDataSet barSet = new BarDataSet(barEntries, "Tarragona");
        barSet.setColors(ColorTemplate.VORDIPLOM_COLORS);

        BarData barData = new BarData(barSet);
        barData.setBarWidth(0.9f);  // Ajuste de ancho de barra

        barChart.setData(barData);
        barChart.setFitBars(true);
        barChart.animateY(2000);

        Description description = new Description();
        description.setText("Tarragona");
        barChart.setDescription(description);

// Configura el eje X para que muestre todas las etiquetas de manera escalonada
        XAxis xAxis = barChart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setLabelCount(10); // Muestra todas las etiquetas del eje X
        xAxis.setGranularity(1f); // Espaciado consistente
        xAxis.setTextSize(14f); // Reduce el tamaño de fuente para mayor claridad
        xAxis.setDrawGridLines(false);

// Configura el eje Y para que muestre el rango completo de valores sin optimización
        YAxis leftAxis = barChart.getAxisLeft();
        leftAxis.setGranularity(1f);
        leftAxis.setDrawLabels(true);
        leftAxis.setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART);
        barChart.getAxisRight().setEnabled(false); // Desactiva el eje derecho

// Limita el rango visible para que el usuario pueda hacer scroll si no caben todas las barras
        barChart.setVisibleXRangeMaximum(10); // Muestra 6 barras a la vez, el usuario puede desplazarse

// Aumenta el espaciado inferior para que las etiquetas se muestren sin recortes
        barChart.setExtraBottomOffset(30f);

// Ajuste de leyenda
        Legend legend = barChart.getLegend();
        legend.setEnabled(true);
        legend.setTextSize(12f);

        barChart.invalidate(); // Redibuja el gráfico con la nueva configuración
    }
    public void grafichorizL() {

        ArrayList<BarEntry> barEntries = new ArrayList<>();
        ArrayList<String> labels = new ArrayList<>();
        int i = 0;

        // Afegeix les dades al BarChart
        for (Map.Entry<String, AtomicInteger> entry : visitatsPerComarcaL.entrySet()) {
            barEntries.add(new BarEntry(i, entry.getValue().get()));
            labels.add(entry.getKey());
            i++;
        }

        HorizontalBarChart barChart = getView().findViewById(R.id.barChartL);

        BarDataSet barSet = new BarDataSet(barEntries, "Lleida");
        barSet.setColors(ColorTemplate.PASTEL_COLORS);

        BarData barData = new BarData(barSet);
        barData.setBarWidth(0.9f);  // Ajuste de ancho de barra

        barChart.setData(barData);
        barChart.setFitBars(true);
        barChart.animateY(2000);

        Description description = new Description();
        description.setText("Lleida");
        barChart.setDescription(description);

// Configura el eje X para que muestre todas las etiquetas de manera escalonada
        XAxis xAxis = barChart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setLabelCount(13); // Muestra todas las etiquetas del eje X
        xAxis.setGranularity(1f); // Espaciado consistente
        xAxis.setTextSize(14f); // Reduce el tamaño de fuente para mayor claridad
        xAxis.setDrawGridLines(false);

// Configura el eje Y para que muestre el rango completo de valores sin optimización
        YAxis leftAxis = barChart.getAxisLeft();
        leftAxis.setGranularity(1f);
        leftAxis.setDrawLabels(true);
        leftAxis.setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART);
        barChart.getAxisRight().setEnabled(false); // Desactiva el eje derecho

// Limita el rango visible para que el usuario pueda hacer scroll si no caben todas las barras
        barChart.setVisibleXRangeMaximum(13); // Muestra 6 barras a la vez, el usuario puede desplazarse

// Aumenta el espaciado inferior para que las etiquetas se muestren sin recortes
        barChart.setExtraBottomOffset(30f);

// Ajuste de leyenda
        Legend legend = barChart.getLegend();
        legend.setEnabled(true);
        legend.setTextSize(12f);

        barChart.invalidate(); // Redibuja el gráfico con la nueva configuración
    }

    public void barchart() {
        HorizontalBarChart barChart = getView().findViewById(R.id.barChartB);

        ArrayList<BarEntry> barEntries = new ArrayList<>();
        ArrayList<String> labels = new ArrayList<>();
        int i = 0;

        // Afegeix les dades al BarChart
        for (Map.Entry<String, AtomicInteger> entry : visitatsPerComarca.entrySet()) {
            barEntries.add(new BarEntry(i, entry.getValue().get()));
            labels.add(entry.getKey());
            i++;
        }

        barChart.setDrawBarShadow(false);
        barChart.setFitBars(true); // Ajusta las barras al espacio disponible
        barChart.setDrawValueAboveBar(true);
        barChart.setPinchZoom(true); // Permite hacer zoom en el gráfico

        // Configuración de fondo
        barChart.setBackgroundColor(Color.TRANSPARENT);
        barChart.setDrawGridBackground(false); // Desactiva el fondo de la cuadrícula

        // DataSet y barra
        BarDataSet barDataSet = new BarDataSet(barEntries, "Municipios Visitados");
        barDataSet.setColors(ColorTemplate.COLORFUL_COLORS);
        BarData barData = new BarData(barDataSet);
        barData.setBarWidth(0.9f);
        barData.setValueTextSize(10f); // Ajusta el tamaño del texto en las barras

        // Configura el gráfico con los datos
        barChart.setData(barData);

        // Configura el eje Y (izquierda)
        YAxis leftAxis = barChart.getAxisLeft();
        leftAxis.setGranularity(1f);  // Asegura que se muestren todas las etiquetas sin espacios innecesarios
        leftAxis.setDrawLabels(true); // Asegura que las etiquetas del eje Y se dibujen
        leftAxis.setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART); // Las etiquetas estarán fuera del gráfico

        // Desactiva el eje derecho
        barChart.getAxisRight().setEnabled(false);

        // Configura el eje X (abajo)
        XAxis xAxis = barChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM); // Pone las etiquetas del eje X en la parte inferior
        xAxis.setTextSize(12f);  // Ajusta el tamaño de la fuente de las etiquetas del eje X
        xAxis.setGranularity(1f);  // Asegura que las etiquetas del eje X se dibujen correctamente
        xAxis.setDrawGridLines(false); // Desactiva las líneas de la cuadrícula en el eje X
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels)); // Asigna las etiquetas a las barras

        // Ajuste del espaciado entre las etiquetas
        xAxis.setLabelRotationAngle(-45f); // Rota las etiquetas para que se lean mejor si hay muchas barras

        // Muestra la leyenda
        Legend l = barChart.getLegend();
        l.setTextSize(10f);
        l.setFormSize(10f); // Ajusta el tamaño de los iconos de la leyenda

        // Redibuja el gráfico
        barChart.invalidate();
    }

    private void actualitzarGraficCoamrca() {
        ArrayList<BarEntry> barEntries = new ArrayList<>();
        ArrayList<String> labels = new ArrayList<>();
        int i = 0;

        // Afegeix les dades al BarChart
        for (Map.Entry<String, AtomicInteger> entry : visitatsPerComarca.entrySet()) {
            barEntries.add(new BarEntry(i, entry.getValue().get()));
            labels.add(entry.getKey());
            i++;
        }

        // Configura el DataSet
        BarDataSet barDataSet = new BarDataSet(barEntries, "Municipios Visitados por Comarca");
        barDataSet.setColor(Color.parseColor("#66BB6A")); // Color de las barras
        barDataSet.setValueTextColor(Color.BLACK);
        barDataSet.setValueTextSize(10f);

        HorizontalBarChart horizontalBarChart = getView().findViewById(R.id.barChartB);

        // Configura los datos del HorizontalBarChart
        BarData data = new BarData(barDataSet);
        horizontalBarChart.setData(data);

        // Configura el eje Y para mostrar los nombres de las comarcas
        YAxis yAxis = horizontalBarChart.getAxisLeft();
        yAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
        yAxis.setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART);  // Asegura que las etiquetas estén fuera del gráfico
        yAxis.setGranularity(1f); // Asegura que cada etiqueta esté alineada correctamente

        // Configura el eje X para que las barras sean horizontales
        XAxis xAxis = horizontalBarChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM); // Las etiquetas del eje X estarán abajo
        xAxis.setGranularity(1f); // Mínimo de valores que pueden aparecer
        xAxis.setAxisMinimum(0f); // El eje X comienza desde 0
        horizontalBarChart.getAxisRight().setEnabled(false); // Desactiva el eje derecho si no se necesita

        // Evitar que el eje X dibuje líneas de la cuadrícula
        horizontalBarChart.getXAxis().setDrawGridLines(false);
        horizontalBarChart.getAxisLeft().setDrawGridLines(false);

        // Configura el gráfico
        horizontalBarChart.setFitBars(true); // Ajusta las barras dentro del espacio del gráfico
        horizontalBarChart.getDescription().setEnabled(false); // Desactiva la descripción por defecto
        horizontalBarChart.setDrawGridBackground(false);
        horizontalBarChart.animateY(1000); // Animación vertical
        horizontalBarChart.invalidate(); // Refresca el gráfico con los datos
    }




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

        // Genera colors automàticament per a cada entrada
        ArrayList<Integer> colors = new ArrayList<>();
        for (int i = 0; i < yValues.size(); i++) {
            colors.add(ColorTemplate.MATERIAL_COLORS[i % ColorTemplate.MATERIAL_COLORS.length]);
        }
        dataSet.setColors(colors);

        // Configura el text de les dades al gràfic
        PieData data = new PieData(dataSet);
        data.setValueTextSize(12f);
        data.setValueTextColor(Color.WHITE);
        data.setValueFormatter(new PercentFormatter(binding.pieChartVegueria)); // Mostra valors com a percentatge

        // Estils per al PieChart
        binding.pieChartVegueria.setData(data);
        binding.pieChartVegueria.setUsePercentValues(true); // Mostra els valors en percentatges
        binding.pieChartVegueria.setDrawHoleEnabled(true); // Forat al centre
        binding.pieChartVegueria.setHoleColor(Color.TRANSPARENT); // Forat transparent al centre
        binding.pieChartVegueria.setHoleRadius(40f); // Radi del forat
        binding.pieChartVegueria.setTransparentCircleRadius(45f); // Radi del cercle transparent

        // Text al centre del gràfic
        binding.pieChartVegueria.setCenterText("Municipis visitats per Vegueries");
        binding.pieChartVegueria.setCenterTextSize(14f);
        binding.pieChartVegueria.setCenterTextColor(Color.DKGRAY);

        // Desactiva la descripció per defecte
        binding.pieChartVegueria.getDescription().setEnabled(false);

        // Configura la llegenda
        Legend legend = binding.pieChartVegueria.getLegend();
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
        legend.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        legend.setDrawInside(false);
        legend.setTextSize(12f);
        legend.setTextColor(Color.DKGRAY);

        // Habilita el mode de "word wrap" per a llegendes de múltiples files
        legend.setWordWrapEnabled(true);

        // Ajusta l’espai entre ítems per fer la llegenda més compacta
        legend.setFormToTextSpace(6f); // Espai entre el símbol de la llegenda i el text
        legend.setXEntrySpace(12f); // Espai entre els elements de la llegenda
        legend.setYEntrySpace(6f); // Espai vertical entre files

        binding.pieChartVegueria.animateY(1000, Easing.EaseInOutQuad); // Animació per fer el gràfic més atractiu
        binding.pieChartVegueria.invalidate(); // Refresca el gràfic amb les noves dades
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