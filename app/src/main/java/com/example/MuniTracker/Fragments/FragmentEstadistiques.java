package com.example.MuniTracker.Fragments;

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

import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.DecelerateInterpolator;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.MuniTracker.ComarcaVisitCount;
import com.example.MuniTracker.MapesHelper;
import com.example.MuniTracker.MunicipiViewModel;
import com.example.MuniTracker.MunicipiVisitCount;
import com.example.MuniTracker.R;
import com.example.MuniTracker.databinding.FragmentEstadistiquesBinding;
import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.HorizontalBarChart;
import com.github.mikephil.charting.charts.PieChart;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class FragmentEstadistiques extends Fragment {

    private FragmentEstadistiquesBinding binding;
    private Context context;
    private MapesHelper mapesHelper;

    private final AtomicInteger visitatsProvLleida = new AtomicInteger();
    private final AtomicInteger visitatsProvBarcelona = new AtomicInteger();
    private final AtomicInteger visitatsProvTarragona = new AtomicInteger();
    private final AtomicInteger visitatsProvGirona = new AtomicInteger();

    private final Map<String, AtomicInteger> visitatsPerVegueria = new HashMap<>();
    private final List<String> vegueries = Arrays.asList("Alt Pirineu i Aran", "Barcelona", "Camp de Tarragona", "Catalunya Central", "Girona", "Lleida", "Penedès", "Terres de l'Ebre");
    private final Map<String, AtomicInteger> visitatsPerComarcaL = new HashMap<>();
    private final List<String> comarques = Arrays.asList(
            "Alt Penedès", "Anoia", "Bages", "Baix Llobregat", "Barcelonès", "Garraf", "Maresme", "Osona", "Vallès Occidental", "Vallès Oriental",
            "Alt Camp", "Baix Camp", "Baix Ebre", "Baix Penedès", "Conca de Barberà", "Montsià", "Priorat", "Ribera d'Ebre", "Tarragonès", "Terra Alta",
            "Alt Empordà", "Baix Empordà", "Cerdanya", "Garrotxa", "Gironès", "Pla de l'Estany", "La Selva", "Ripollès",
            "Alta Ribagorça", "Alt Urgell", "Cerdanya", "Garrigues", "Noguera", "Pallars Jussà", "Pallars Sobirà", "Pla d'Urgell", "Segarra", "Segrià", "Solsonès", "Urgell", "Val d'Aran");

    private static final int TOTAL_MUNICIPIS = 949;

    // Paleta de colors plana i estilitzada per als gràfics
    int[] colorsGrafiques = {
            Color.parseColor("#4E79A7"), Color.parseColor("#F28E2B"),
            Color.parseColor("#E15759"), Color.parseColor("#76B7B2"),
            Color.parseColor("#59A14F"), Color.parseColor("#EDC948"),
            Color.parseColor("#AF7AA1"), Color.parseColor("#FF9DA7")
    };

    public FragmentEstadistiques() {}

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentEstadistiquesBinding.inflate(inflater, container, false);
        mapesHelper = new MapesHelper(context);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && getActivity() != null) {
            Window window = getActivity().getWindow();
            window.setStatusBarColor(ContextCompat.getColor(getActivity(), R.color.fons));
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }

        MunicipiViewModel viewModel = new ViewModelProvider(this).get(MunicipiViewModel.class);

        // Progress bar superior
        viewModel.nombreMunicipisVisitats().observe(getViewLifecycleOwner(), nombreMunicipisVisitats -> {
            double percentatge = (double) (nombreMunicipisVisitats * 100) / TOTAL_MUNICIPIS;
            DecimalFormat df = new DecimalFormat("#.##");
            binding.progressBar.setMax(TOTAL_MUNICIPIS);
            binding.indicadorpercentatge.setText(df.format(percentatge) + " %");

            ObjectAnimator animator = ObjectAnimator.ofInt(binding.progressBar, "progress", nombreMunicipisVisitats);
            animator.setDuration(900);
            animator.setInterpolator(new DecelerateInterpolator());
            animator.start();

            binding.munvisi.setText("S'han visitat " + nombreMunicipisVisitats + " municipis d'un total de " + TOTAL_MUNICIPIS + ".");
        });

        // Observadors de Províncies
        viewModel.obtenirQuantitatMunicipisVisitatsProvincia("Província de Lleida").observe(getViewLifecycleOwner(), n -> { visitatsProvLleida.set(n); actualitzarPieChartGeneral(binding.pieChart, obtenirEntradasProvincies()); });
        viewModel.obtenirQuantitatMunicipisVisitatsProvincia("Província de Girona").observe(getViewLifecycleOwner(), n -> { visitatsProvGirona.set(n); actualitzarPieChartGeneral(binding.pieChart, obtenirEntradasProvincies()); });
        viewModel.obtenirQuantitatMunicipisVisitatsProvincia("Província de Barcelona").observe(getViewLifecycleOwner(), n -> { visitatsProvBarcelona.set(n); actualitzarPieChartGeneral(binding.pieChart, obtenirEntradasProvincies()); });
        viewModel.obtenirQuantitatMunicipisVisitatsProvincia("Província de Tarragona").observe(getViewLifecycleOwner(), n -> { visitatsProvTarragona.set(n); actualitzarPieChartGeneral(binding.pieChart, obtenirEntradasProvincies()); });

        // Inicialització i observadors de Vegueries
        for (String vegueria : vegueries) {
            visitatsPerVegueria.put(vegueria, new AtomicInteger());
            viewModel.obtenirQuantitatMunicipisVisitatsVegueria(vegueria).observe(getViewLifecycleOwner(), n -> {
                visitatsPerVegueria.get(vegueria).set(n);
                actualitzarPieChartGeneral(binding.pieChartVegueria, obtenirEntradesVegueries());
            });
        }

        // Percentatges per comarques de la llista lineal
        List<Pair<String, Integer>> comarcaPercentatges = new ArrayList<>();
        AtomicInteger remainingComarques = new AtomicInteger(comarques.size());
        for (String comarca : comarques) {
            visitatsPerComarcaL.put(comarca, new AtomicInteger());
            viewModel.obtenirQuantitatMunicipisVisitatsComarca(comarca).observe(getViewLifecycleOwner(), n -> {
                int totalComarca = mapesHelper.obtenirQuantitatMunicipisPerComarca(comarca);
                int pct = totalComarca > 0 ? (int) ((n / (float) totalComarca) * 100) : 0;
                comarcaPercentatges.add(new Pair<>(comarca, pct));

                if (remainingComarques.decrementAndGet() == 0) {
                    Collections.sort(comarcaPercentatges, (o1, o2) -> Integer.compare(o2.second, o1.second));
                    updateUI(comarcaPercentatges);
                }
            });
        }

        // Tops convertits a Rànquings Horitzontals (Molt més fàcil de llegir!)
        viewModel.getTop10MostVisitedMunicipalities().observe(getViewLifecycleOwner(), llista -> {
            ArrayList<BarEntry> entries = new ArrayList<>();
            ArrayList<String> labels = new ArrayList<>();
            // Invertim l'ordre perquè el TOP 1 quedi a dalt de tot del gràfic horitzontal
            for (int i = 0; i < llista.size(); i++) {
                MunicipiVisitCount item = llista.get(llista.size() - 1 - i);
                entries.add(new BarEntry(i, item.visitCount));
                labels.add(item.municipiId);
            }
            configurarGraficHoritzontal(binding.barChartGa, entries, labels);
        });

        viewModel.getTop5MostVisitedComarques().observe(getViewLifecycleOwner(), llista -> {
            ArrayList<BarEntry> entries = new ArrayList<>();
            ArrayList<String> labels = new ArrayList<>();
            for (int i = 0; i < llista.size(); i++) {
                ComarcaVisitCount item = llista.get(llista.size() - 1 - i);
                entries.add(new BarEntry(i, item.visitCount));
                labels.add(item.comarcaID);
            }
            configurarGraficHoritzontal(binding.barChartCovi, entries, labels);
        });

        viewModel.getTop3MostVisitedVegueries().observe(getViewLifecycleOwner(), llista -> {
            ArrayList<BarEntry> entries = new ArrayList<>();
            ArrayList<String> labels = new ArrayList<>();
            for (int i = 0; i < llista.size(); i++) {
                ComarcaVisitCount item = llista.get(llista.size() - 1 - i);
                entries.add(new BarEntry(i, item.visitCount));
                labels.add(item.comarcaID);
            }
            configurarGraficHoritzontal(binding.barChartVevi, entries, labels);
        });

        viewModel.getTop3MostVisitedProvincies().observe(getViewLifecycleOwner(), llista -> {
            ArrayList<BarEntry> entries = new ArrayList<>();
            ArrayList<String> labels = new ArrayList<>();
            for (int i = 0; i < llista.size(); i++) {
                ComarcaVisitCount item = llista.get(llista.size() - 1 - i);
                entries.add(new BarEntry(i, item.visitCount));
                labels.add(item.comarcaID);
            }
            configurarGraficHoritzontal(binding.barChartPrvi, entries, labels);
        });
    }

    // ==========================================
    // NOU MÈTODE PER A GRÀFICS HORITZONTALS NETS
    // ==========================================
    private void configurarGraficHoritzontal(HorizontalBarChart chart, ArrayList<BarEntry> entries, ArrayList<String> labels) {
        if (entries.isEmpty()) return;

        BarDataSet dataSet = new BarDataSet(entries, "");
        dataSet.setColors(colorsGrafiques);
        dataSet.setValueTextSize(12f);
        dataSet.setValueTextColor(Color.parseColor("#333333"));

        BarData barData = new BarData(dataSet);
        barData.setBarWidth(0.6f); // Control d'amplada de les barres perquè respirin

        // Eix X (A l'esquerra ara: els noms es llegeixen perfectament de costat)
        XAxis xAxis = chart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setTextSize(13);
        xAxis.setTextColor(Color.parseColor("#222222"));
        xAxis.setDrawGridLines(false); // Sense ratlles lletges de fons
        xAxis.setDrawAxisLine(true);
        xAxis.setGranularity(1f);
        xAxis.setLabelCount(labels.size());

        // Eix Y (A dalt/A baix: valors numèrics de visites)
        YAxis yAxisLeft = chart.getAxisLeft();
        yAxisLeft.setDrawGridLines(false);
        yAxisLeft.setGranularity(1f);
        yAxisLeft.setTextColor(Color.parseColor("#777777"));

        chart.getAxisRight().setEnabled(false); // Fora l'eix secundari duplicat

        // Ajustaments generals del gràfic
        chart.setData(barData);
        chart.setFitBars(true);
        chart.setDrawValueAboveBar(true);
        chart.getLegend().setEnabled(false); // No cal llegenda, el nom és al costat de cada barra
        chart.getDescription().setEnabled(false);

        chart.animateY(800, Easing.EaseOutCubic);
        chart.invalidate();
    }

    // ==========================================
    // NOU MÈTODE PER A PIECHART SÒLID (SENSE DONUT)
    // ==========================================
    private void actualitzarPieChartGeneral(PieChart pieChart, ArrayList<PieEntry> entries) {
        if (entries.isEmpty()) return;

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setSliceSpace(2f); // Espai suau de separació lineal blanca
        dataSet.setSelectionShift(8f);
        dataSet.setColors(colorsGrafiques);

        PieData data = new PieData(dataSet);
        data.setValueTextSize(13f);
        data.setValueTextColor(Color.WHITE); // Percentatge visible en blanc dins del color
        data.setValueFormatter(new PercentFormatter(pieChart));

        pieChart.setData(data);
        pieChart.setUsePercentValues(true);

        // ELIMINACIÓ DEL FORAT DEL DONUT
        pieChart.setDrawHoleEnabled(false);
        pieChart.setDrawEntryLabels(true); // Escriu el nom (Lleida, Girona...) directament dins del format format format de format format format format format sector
        pieChart.setEntryLabelColor(Color.WHITE);
        pieChart.setEntryLabelTextSize(12f);

        pieChart.getDescription().setEnabled(false);
        pieChart.getLegend().setEnabled(false); // Eliminem la llegenda inferior que ocupa espai inexistent

        pieChart.animateY(900, Easing.EaseInOutQuad);
        pieChart.invalidate();
    }

    private ArrayList<PieEntry> obtenirEntradasProvincies() {
        ArrayList<PieEntry> entries = new ArrayList<>();
        if (visitatsProvLleida.get() > 0) entries.add(new PieEntry(visitatsProvLleida.get(), "Lleida"));
        if (visitatsProvGirona.get() > 0) entries.add(new PieEntry(visitatsProvGirona.get(), "Girona"));
        if (visitatsProvBarcelona.get() > 0) entries.add(new PieEntry(visitatsProvBarcelona.get(), "Barcelona"));
        if (visitatsProvTarragona.get() > 0) entries.add(new PieEntry(visitatsProvTarragona.get(), "Tarragona"));
        return entries;
    }

    private ArrayList<PieEntry> obtenirEntradesVegueries() {
        ArrayList<PieEntry> entries = new ArrayList<>();
        for (Map.Entry<String, AtomicInteger> entry : visitatsPerVegueria.entrySet()) {
            int valor = entry.getValue().get();
            if (valor > 0) {
                entries.add(new PieEntry(valor, entry.getKey()));
            }
        }
        return entries;
    }

    private void updateUI(List<Pair<String, Integer>> comarcaPorcentajes) {
        LinearLayout progressContainer = binding.progressContainer;
        progressContainer.removeAllViews();

        for (Pair<String, Integer> item : comarcaPorcentajes) {
            View progressItem = LayoutInflater.from(getContext()).inflate(R.layout.progress_item, progressContainer, false);

            TextView comarcaName = progressItem.findViewById(R.id.comarca_name);
            ProgressBar progressBar = progressItem.findViewById(R.id.progress_bar);
            TextView percentageText = progressItem.findViewById(R.id.percentage_text);

            comarcaName.setText(item.first);
            progressBar.setMax(100);
            progressBar.setProgress(item.second);
            percentageText.setText(item.second + "%");

            progressContainer.addView(progressItem);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}