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
import com.github.mikephil.charting.charts.BarChart;
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
import com.github.mikephil.charting.formatter.ValueFormatter;
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

    int[] colorsGrafiques = {
            Color.parseColor("#4E79A7"), // Azul oscuro
            Color.parseColor("#F28E2B"), // Naranja cálido
            Color.parseColor("#E15759"), // Rojo suave
            Color.parseColor("#76B7B2"), // Verde-azulado
            Color.parseColor("#59A14F"), // Verde oliva
            Color.parseColor("#EDC948"), // Amarillo dorado
            Color.parseColor("#AF7AA1"), // Lila pastel
            Color.parseColor("#FF9DA7"), // Rosa opaco
            Color.parseColor("#9C755F"), // Marrón
            Color.parseColor("#BAB0AC"), // Gris claro
    };


    private static final int TOTAL_MUNICIPIS = 949;

    private Context context;
    MapesHelper mapesHelper;

    private Map<String, AtomicInteger> visitatsPerVegueria = new HashMap<>();
    private List<String> vegueries = Arrays.asList("Alt Pirineu i Aran", "Barcelona", "Camp de Tarragona", "Catalunya Central", "Girona", "Lleida", "Penedès", "Terres de l'Ebre");

    private Map<String, AtomicInteger> visitatsPerComarcaL = new HashMap<>();

    private List<String> comarques = Arrays.asList(
            "Alt Penedès", "Anoia", "Bages", "Baix Llobregat", "Barcelonès", "Garraf", "Maresme", "Osona", "Vallès Occidental", "Vallès Oriental",
            "Alt Camp", "Baix Camp", "Baix Ebre", "Baix Penedès", "Conca de Barberà", "Montsià", "Priorat", "Ribera d'Ebre", "Tarragonès", "Terra Alta",
            "Alt Empordà", "Baix Empordà", "Cerdanya", "Garrotxa", "Gironès", "Pla de l'Estany", "La Selva", "Ripollès",
            "Alta Ribagorça", "Alt Urgell", "Cerdanya", "Garrigues", "Noguera", "Pallars Jussà", "Pallars Sobirà", "Pla d'Urgell", "Segarra", "Segrià", "Solsonès", "Urgell", "Val d'Aran");//13

    public FragmentEstadistiques() {}

    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
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

            double percentatge = (double) (nombreMunicipisVisitats * 100) / TOTAL_MUNICIPIS;
            DecimalFormat df = new DecimalFormat("#.##");
            binding.progressBar.setMax(TOTAL_MUNICIPIS);
            binding.indicadorpercentatge.setText(df.format(percentatge) + " %");

            ObjectAnimator animator = ObjectAnimator.ofInt(binding.progressBar, "progress", nombreMunicipisVisitats);
            animator.setDuration(900);
            animator.setInterpolator(new DecelerateInterpolator());
            animator.start();

            binding.munvisi.setText("S'han visitat " + nombreMunicipisVisitats + " municipis d'un total de 949.");
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

        List<Pair<String, Integer>> comarcaPercentatges = new ArrayList<>();

        AtomicInteger remainingComarques = new AtomicInteger(comarques.size());

        for (String comarca : comarques) {
            visitatsPerComarcaL.put(comarca, new AtomicInteger());

            viewModel.obtenirQuantitatMunicipisVisitatsComarca(comarca).observe(getViewLifecycleOwner(), nombreMunicipisVisitats -> {
                int quantitatMunicipisComarca = mapesHelper.obtenirQuantitatMunicipisPerComarca(comarca);
                int porcentajeVisitado = (int) ((nombreMunicipisVisitats / (float) quantitatMunicipisComarca) * 100);

                comarcaPercentatges.add(new Pair<>(comarca, porcentajeVisitado));

                if (remainingComarques.decrementAndGet() == 0) {
                    Collections.sort(comarcaPercentatges, (o1, o2) -> Integer.compare(o2.second, o1.second));

                    updateUI(comarcaPercentatges);
                }
            });
        }

        //************************************************
        //Top 10 municipis visitats mes vegades grafica horitzontal
        viewModel.getTop10MostVisitedMunicipalities().observe(getViewLifecycleOwner(), topmunicipisvisitats -> {
            //obte Municipivisitcount que te municipiid i visitcount
            actualizarGrafico(topmunicipisvisitats);
        });

        viewModel.getTop5MostVisitedComarques().observe(getViewLifecycleOwner(), topmunicipisvisitats -> {
            //obte Municipivisitcount que te municipiid i visitcount
            actualizarGraficoComa(topmunicipisvisitats,getView().findViewById(R.id.barChartCovi));
        });

        viewModel.getTop3MostVisitedVegueries().observe(getViewLifecycleOwner(), topmunicipisvisitats -> {
            //obte Municipivisitcount que te municipiid i visitcount
            actualizarGraficoComa(topmunicipisvisitats,getView().findViewById(R.id.barChartVevi));
        });

        viewModel.getTop3MostVisitedProvincies().observe(getViewLifecycleOwner(), topmunicipisvisitats -> {
            //obte Municipivisitcount que te municipiid i visitcount
            actualizarGraficoComa(topmunicipisvisitats,getView().findViewById(R.id.barChartPrvi));
        });
    }

    private void actualizarGraficoComa(List<ComarcaVisitCount> topMunicipisVisitats, BarChart chart) {
        ArrayList<BarEntry> entries = new ArrayList<>();
        ArrayList<String> labels = new ArrayList<>();

        for (int i = 0; i < topMunicipisVisitats.size(); i++) {
            ComarcaVisitCount municipi = topMunicipisVisitats.get(i);
            entries.add(new BarEntry(i, municipi.visitCount));
            labels.add(municipi.comarcaID);
        }

        BarDataSet dataSet = new BarDataSet(entries, "");
        dataSet.setColors(colorsGrafiques);
        dataSet.setValueTextSize(12f);

        BarData barData = new BarData(dataSet);
        barData.setBarWidth(0.8f);

        YAxis yAxis = chart.getAxisLeft();
        yAxis.setTextSize(14f);
        yAxis.setDrawGridLines(false);
        yAxis.setGranularity(1f);
        yAxis.setGranularityEnabled(true);

        chart.getAxisRight().setEnabled(false);

        XAxis xAxis = chart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));

        xAxis.setLabelCount(labels.size(), true);

        chart.getXAxis().setSpaceMin(0.5f);
        chart.getXAxis().setSpaceMax(0.5f);

        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setTextSize(14f);
        xAxis.setLabelRotationAngle(270f);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f);
        xAxis.setGranularityEnabled(true);
        xAxis.setLabelCount(labels.size());

        chart.setData(barData);
        chart.setFitBars(true);
        chart.setDrawValueAboveBar(true);

        chart.getLegend().setEnabled(false);

        Description description = new Description();
        description.setText("");
        description.setTextSize(12f);
        chart.setDescription(description);

        chart.invalidate();
    }

    private void updateUI(List<Pair<String, Integer>> comarcaPorcentajes) {
        LinearLayout progressContainer = getView().findViewById(R.id.progress_container);

        progressContainer.removeAllViews();

        for (Pair<String, Integer> item : comarcaPorcentajes) {
            String comarcaNombre = item.first;
            int porcentaje = item.second;

            View progressItem = LayoutInflater.from(getContext()).inflate(R.layout.progress_item, progressContainer, false);

            TextView comarcaName = progressItem.findViewById(R.id.comarca_name);
            ProgressBar progressBar = progressItem.findViewById(R.id.progress_bar);
            TextView percentageText = progressItem.findViewById(R.id.percentage_text);

            comarcaName.setText(comarcaNombre);
            progressBar.setMax(100);
            progressBar.setProgress(porcentaje);
            percentageText.setText(porcentaje + "%");

            progressContainer.addView(progressItem);
        }
    }

    private void actualizarGrafico(List<MunicipiVisitCount> topMunicipisVisitats) {
        ArrayList<BarEntry> entries = new ArrayList<>();
        ArrayList<String> labels = new ArrayList<>();

        for (int i = 0; i < topMunicipisVisitats.size(); i++) {
            MunicipiVisitCount municipi = topMunicipisVisitats.get(i);
            entries.add(new BarEntry(i, municipi.visitCount));
            labels.add(municipi.municipiId);
        }

        BarChart chart = getView().findViewById(R.id.barChartGa);

        BarDataSet dataSet = new BarDataSet(entries, "");
        dataSet.setColors(colorsGrafiques);
        dataSet.setValueTextSize(12f);

        BarData barData = new BarData(dataSet);
        barData.setBarWidth(0.8f);

        YAxis yAxis = chart.getAxisLeft();
        yAxis.setTextSize(10f);
        yAxis.setDrawGridLines(false);
        yAxis.setGranularity(1f);
        yAxis.setGranularityEnabled(true);

        chart.getAxisRight().setEnabled(false);

        XAxis xAxis = chart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));

        xAxis.setLabelCount(labels.size(), true);

        chart.getXAxis().setSpaceMin(0.5f);
        chart.getXAxis().setSpaceMax(0.5f);

        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setTextSize(10f);
        xAxis.setLabelRotationAngle(270f);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f);
        xAxis.setGranularityEnabled(true);
        xAxis.setLabelCount(labels.size());

        chart.setData(barData);
        chart.setFitBars(true);
        chart.setDrawValueAboveBar(true);

        chart.getLegend().setEnabled(false);

        Description description = new Description();
        description.setText("");
        description.setTextSize(12f);
        chart.setDescription(description);

        chart.invalidate();
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

        PieDataSet dataSet = new PieDataSet(yValues, "");
        dataSet.setSliceSpace(3f);
        dataSet.setSelectionShift(10f);

        ArrayList<Integer> colors = new ArrayList<>();
        for (int i = 0; i < yValues.size(); i++) {
            colors.add(colorsGrafiques[i % colorsGrafiques.length]);
        }
        dataSet.setColors(colors);

        PieData data = new PieData(dataSet);
        data.setValueTextSize(12f);
        data.setValueTextColor(Color.WHITE);
        data.setValueFormatter(new PercentFormatter(binding.pieChartVegueria));

        binding.pieChartVegueria.setData(data);
        binding.pieChartVegueria.setUsePercentValues(true);
        binding.pieChartVegueria.setDrawHoleEnabled(true);
        binding.pieChartVegueria.setHoleColor(Color.TRANSPARENT);
        binding.pieChartVegueria.setHoleRadius(40f);
        binding.pieChartVegueria.setTransparentCircleRadius(45f);

        binding.pieChartVegueria.setCenterText("Municipis visitats per Vegueries");
        binding.pieChartVegueria.setCenterTextSize(14f);
        binding.pieChartVegueria.setCenterTextColor(Color.DKGRAY);

        binding.pieChartVegueria.getDescription().setEnabled(false);

        Legend legend = binding.pieChartVegueria.getLegend();
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
        legend.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        legend.setDrawInside(false);
        legend.setTextSize(12f);
        legend.setTextColor(Color.DKGRAY);

        legend.setWordWrapEnabled(true);

        legend.setFormToTextSpace(6f);
        legend.setXEntrySpace(12f);
        legend.setYEntrySpace(6f);

        binding.pieChartVegueria.setDrawEntryLabels(false);

        binding.pieChartVegueria.animateY(1000, Easing.EaseInOutQuad);
        binding.pieChartVegueria.invalidate();
    }

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
            dataSet.setColors(colorsGrafiques);

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