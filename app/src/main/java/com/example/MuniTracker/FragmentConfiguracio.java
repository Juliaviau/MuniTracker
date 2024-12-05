package com.example.MuniTracker;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;

import com.example.MuniTracker.databinding.FragmentConfiguracioBinding;
import com.example.MuniTracker.databinding.FragmentEstadistiquesBinding;

public class FragmentConfiguracio extends Fragment {
    private Context context;
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

        binding.buttoninici.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    finalize();
                } catch (Throwable e) {
                    throw new RuntimeException(e);
                }
                startActivity(new Intent(getActivity(), FragmentLogin.class));
            }
        });

        return view;
    }
}