// VisitaDialogFragment.java
package com.example.MuniTracker;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.datepicker.MaterialDatePicker;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class VisitaDialogFragment extends BottomSheetDialogFragment {

    private final String notesOriginal;
    private final long dataOriginal;
    private static Context context;
    private long dataSeleccionadaTimestamp;

    public interface VisitaCallback {
        void onVisitaAdded(long data, String notes);
    }
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
    }
    private VisitaCallback callback;

    public VisitaDialogFragment(String notesOriginal, long dataOriginal, VisitaCallback callback) {
        this.notesOriginal = notesOriginal;
        this.dataOriginal = dataOriginal;
        this.callback = callback;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        android.content.SharedPreferences prefs = context.getSharedPreferences("ConfigApp", android.content.Context.MODE_PRIVATE);
        int indexPaleta = prefs.getInt("paleta_seleccionada", 0);


        switch (indexPaleta) {
            case 0: context.setTheme(R.style.Tema_Paleta_Original); break;
            case 1: context.setTheme(R.style.Tema_Paleta_Ocea); break;
            case 2: context.setTheme(R.style.Tema_Paleta_Bosc); break;
            case 3: context.setTheme(R.style.Tema_Paleta_Magenta); break;
        }

        View view = inflater.inflate(R.layout.fragment_visita_dialog, container, false);

        EditText notasEditText = view.findViewById(R.id.notasEditText);
        LinearLayout btnSeleccionarFecha = view.findViewById(R.id.btnSeleccionarFecha);
        TextView txtFechaSeleccionada = view.findViewById(R.id.txtFechaSeleccionada);
        Button agregarButton = view.findViewById(R.id.guardarButton);

        if (dataOriginal != 0) {
            dataSeleccionadaTimestamp = dataOriginal;
        } else {
            dataSeleccionadaTimestamp = System.currentTimeMillis();
        }

        SimpleDateFormat sdf = new SimpleDateFormat("dd 'de' MMMM 'de' yyyy", Locale.getDefault());
        txtFechaSeleccionada.setText(sdf.format(new Date(dataSeleccionadaTimestamp)));

        if (notesOriginal != null) {
            notasEditText.setText(notesOriginal);
        }

        btnSeleccionarFecha.setOnClickListener(v -> {
            MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder.datePicker()
                    .setTitleText("Selecciona la data de visita")
                    .setSelection(dataSeleccionadaTimestamp)
                    .setTheme(R.style.CustomDatePickerStyle)
                    .build();

            datePicker.addOnPositiveButtonClickListener(selection -> {
                if (selection != null) {
                    dataSeleccionadaTimestamp = selection;
                    txtFechaSeleccionada.setText(sdf.format(new Date(dataSeleccionadaTimestamp)));
                }
            });

            datePicker.show(getParentFragmentManager(), "DATE_PICKER");
        });

        agregarButton.setOnClickListener(v -> {
            String notes = notasEditText.getText().toString();

            if (callback != null) {
                callback.onVisitaAdded(dataSeleccionadaTimestamp, notes);
            }
            dismiss();
        });

        return view;
    }
}