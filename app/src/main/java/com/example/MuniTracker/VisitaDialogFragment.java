// AgregarVisitaBottomSheet.java
package com.example.MuniTracker;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.Calendar;
public class VisitaDialogFragment extends BottomSheetDialogFragment {
    public interface VisitaCallback {
        void onVisitaAdded(long data, String notes);
    }

    private VisitaCallback callback;

    public VisitaDialogFragment(VisitaCallback callback) {
        this.callback = callback;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_visita_dialog, container, false);

        EditText notasEditText = view.findViewById(R.id.notasEditText);
        DatePicker datePicker = view.findViewById(R.id.fechaTextView);
        Button agregarButton = view.findViewById(R.id.guardarButton);

        // Configurar el botón de agregar visita
        agregarButton.setOnClickListener(v -> {
            String notes = notasEditText.getText().toString();

            // Formato de fecha como "dd/MM/yyyy"
            //String fecha = datePicker.getDayOfMonth() + "/" + (datePicker.getMonth() + 1) + "/" + datePicker.getYear();
            Calendar calendar = Calendar.getInstance();
            calendar.set(datePicker.getYear(), datePicker.getMonth(), datePicker.getDayOfMonth());
            long dataTimestamp = calendar.getTimeInMillis();




            if (callback != null) {
                callback.onVisitaAdded(dataTimestamp, notes);
            }
            dismiss(); // Cerrar el BottomSheetDialogFragment después de agregar
        });

        return view;
    }
}
