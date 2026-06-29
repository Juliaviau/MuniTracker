package com.example.MuniTracker.Fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.MuniTracker.MunicipiViewModel;
import com.example.MuniTracker.R;
import com.example.MuniTracker.databinding.FragmentConfiguracioBinding;

import java.util.concurrent.atomic.AtomicReference;

public class FragmentConfiguracio extends Fragment {
    private Context context;

    // Defineix 3 o 4 opcions de combinacions de 5 colors
    private final String[][] PALETES = {
            {"#fff7b2", "#ffd966", "#ffa631", "#d98a2d", "#a65e2e"}, // 0. Groc/Taronja (El teu original)
            {"#E0F3F8", "#ABD9E9", "#74ADD1", "#4575B4", "#313695"}, // 1. Escala de Blaus (Fred)
            {"#E8F5E9", "#A5D6A7", "#66BB6A", "#388E3C", "#1B5E20"}, // 2. Escala de Verds (Ecològic)
            {"#FCE4EC", "#F48FB1", "#F06292", "#E91E63", "#880E4F"}  // 3. Escala de Rosa/Magentes
    };

    private final String[] NOMS_PALETES = {
            "Tons Tardor (Original)",
            "Oceà Blau",
            "Bosc Verd",
            "Magenca Elèctric"
    };

    private String[] paletaActual;

    @NonNull
    FragmentConfiguracioBinding binding;
    // Canviar mode clar fosc app
    // Veure anunci
    //boolean nightmode;
    //SharedPreferences sharedPreferences;
    //SharedPreferences.Editor editor;

    public FragmentConfiguracio() {
        // Required empty public constructor
    }

    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
    }
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Activity activity = getActivity();
            if (activity != null) {
                Window window = activity.getWindow();
                window.setStatusBarColor(ContextCompat.getColor(activity, R.color.fons));
                View decorView = window.getDecorView();
                decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            }
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentConfiguracioBinding.inflate(inflater, container, false);
        View view = binding.getRoot();
        MunicipiViewModel viewModel = new ViewModelProvider(this).get(MunicipiViewModel.class);


        /*sharedPreferences = getActivity().getSharedPreferences("MODE", Context.MODE_PRIVATE);
        nightmode = sharedPreferences.getBoolean("nightmode", false);

        if (nightmode) {
            binding.switchCompat.setChecked(true);
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        }

        binding.switchCompat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (nightmode) {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                    editor = sharedPreferences.edit();
                    editor.putBoolean("nightmode",false);
                } else {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                    editor = sharedPreferences.edit();
                    editor.putBoolean("nightmode",true);
                }
                editor.apply();
            }
        });*/

        /*binding.switchCompat.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                binding.switchCompat.setText("Mode fosc");
            } else {
                binding.switchCompat.setText("Mode clar");
            }

        });*/

        /*binding.buttoninici.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    finalize();
                } catch (Throwable e) {
                    throw new RuntimeException(e);
                }
                startActivity(new Intent(getActivity(), FragmentLogin.class));
            }
        });*/

        binding.cardEliminarDades.setOnClickListener(v -> {
            new AlertDialog.Builder(getContext())
                    .setTitle("Esborrar totes les dades?")
                //    .setTitle(R.string.eliminar_datos_titulo)
                    .setMessage("Aquesta acció eliminarà el teu historial, mapes guardats i preferències. No es pot desfer.")
                    .setPositiveButton("Eliminar", (dialog, which) -> {
                        // Aquí poses el teu codi per netejar SharedPreferences, SQLite, etc.
                        //esborrarDadesDeLApp();
                        viewModel.eliminarTotMunicipiVisites();
                        Toast.makeText(context, R.string.datos_eliminados, Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton("Cancel·lar", null)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
        });

        AtomicReference<SharedPreferences> prefs = new AtomicReference<>(context.getSharedPreferences("ConfigApp", Context.MODE_PRIVATE));
        int indexPaleta = prefs.get().getInt("paleta_seleccionada", 0);
        paletaActual = PALETES[indexPaleta]; // Assignem l'array de 5 colors d'aquella paleta
        binding.txtNomPaleta.setText("Tema actual: " + NOMS_PALETES[indexPaleta]);

        binding.vistaColor25.setBackgroundColor(Color.parseColor(paletaActual[0]));
        binding.vistaColor50.setBackgroundColor(Color.parseColor(paletaActual[1]));
        binding.vistaColor75.setBackgroundColor(Color.parseColor(paletaActual[2]));
        binding.vistaColor99.setBackgroundColor(Color.parseColor(paletaActual[3]));
        binding.vistaColorComplet.setBackgroundColor(Color.parseColor(paletaActual[4]));

        binding.cardPaletaColors.setOnClickListener(v -> {
            new AlertDialog.Builder(getContext())
                    .setTitle("Tria una combinació de colors")
                    .setItems(NOMS_PALETES, (dialog, indexTriat) -> {

                        // 1. Guardar la paleta triada (indexTriat) a SharedPreferences perquè tota l'app ho sàpiga
                        prefs.set(requireActivity().getSharedPreferences("ConfigApp", Context.MODE_PRIVATE));
                        prefs.get().edit().putInt("paleta_seleccionada", indexTriat).apply();

                        // 2. Actualitzar visualment el text i la barreta de degradat de la targeta a l'instant
                        binding.txtNomPaleta.setText("Tema actual: " + NOMS_PALETES[indexTriat]);

                        String[] colorsTriats = PALETES[indexTriat];
                        binding.vistaColor25.setBackgroundColor(Color.parseColor(colorsTriats[0]));
                        binding.vistaColor50.setBackgroundColor(Color.parseColor(colorsTriats[1]));
                        binding.vistaColor75.setBackgroundColor(Color.parseColor(colorsTriats[2]));
                        binding.vistaColor99.setBackgroundColor(Color.parseColor(colorsTriats[3]));
                        binding.vistaColorComplet.setBackgroundColor(Color.parseColor(colorsTriats[4]));

                        // 3. OPCIONAL: Aquí pots cridar un mètode global per repintar el mapa si cal
                    })
                    .show();
        });

        binding.cardPaletaColors.setOnClickListener(v -> {

            // Creem un ArrayAdapter personalitzat per pintar el text i els colors a cada fila
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(), R.layout.fila_paleta_dialog, NOMS_PALETES) {
                @NonNull
                @Override
                public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                    if (convertView == null) {
                        convertView = LayoutInflater.from(getContext()).inflate(R.layout.fila_paleta_dialog, parent, false);
                    }

                    // Busquem els elements de la fila XML
                    TextView txtNom = convertView.findViewById(R.id.txtNomFila);
                    View v1 = convertView.findViewById(R.id.v1);
                    View v2 = convertView.findViewById(R.id.v2);
                    View v3 = convertView.findViewById(R.id.v3);
                    View v4 = convertView.findViewById(R.id.v4);
                    View v5 = convertView.findViewById(R.id.v5);

                    // Assignem el text de la posició actual
                    txtNom.setText(NOMS_PALETES[position]);

                    // Assignem els colors corresponents d'aquesta paleta
                    String[] colorsFila = PALETES[position];
                    v1.setBackgroundColor(Color.parseColor(colorsFila[0]));
                    v2.setBackgroundColor(Color.parseColor(colorsFila[1]));
                    v3.setBackgroundColor(Color.parseColor(colorsFila[2]));
                    v4.setBackgroundColor(Color.parseColor(colorsFila[3]));
                    v5.setBackgroundColor(Color.parseColor(colorsFila[4]));

                    return convertView;
                }
            };

            // Construïm el diàleg utilitzant el nou adaptador
            new AlertDialog.Builder(getContext())
                    .setTitle("Tria una combinació de colors")
                    .setAdapter(adapter, (dialog, indexTriat) -> {

                        // 1. Guardar la paleta triada a SharedPreferences
                        prefs.set(requireActivity().getSharedPreferences("ConfigApp", Context.MODE_PRIVATE));
                        prefs.get().edit().putInt("paleta_seleccionada", indexTriat).apply();

                        // 2. Actualitzar visualment el text i la barra de la targeta principal a l'instant
                        binding.txtNomPaleta.setText("Tema actual: " + NOMS_PALETES[indexTriat]);

                        String[] colorsTriats = PALETES[indexTriat];
                        binding.vistaColor25.setBackgroundColor(Color.parseColor(colorsTriats[0]));
                        binding.vistaColor50.setBackgroundColor(Color.parseColor(colorsTriats[1]));
                        binding.vistaColor75.setBackgroundColor(Color.parseColor(colorsTriats[2]));
                        binding.vistaColor99.setBackgroundColor(Color.parseColor(colorsTriats[3]));
                        binding.vistaColorComplet.setBackgroundColor(Color.parseColor(colorsTriats[4]));
                    })
                    .show();
        });

        return view;
    }
}