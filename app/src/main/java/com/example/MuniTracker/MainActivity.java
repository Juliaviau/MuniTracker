package com.example.MuniTracker;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.MuniTracker.Fragments.FragmentConfiguracio;
import com.example.MuniTracker.Fragments.FragmentEstadistiques;
import com.example.MuniTracker.Fragments.FragmentLogin;
import com.example.MuniTracker.Fragments.FragmentMapes;
import com.example.MuniTracker.Fragments.FragmentPerfil;
import com.example.MuniTracker.databinding.ActivityMainBinding;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.splashscreen.SplashScreen; // Recuerda añadir la dependencia si no la tienes
import android.os.Bundle;
import android.view.MenuItem;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.preference.PreferenceManager;


public class MainActivity extends AppCompatActivity {


    // 1. Variable para controlar el estado de la carga de datos
    private boolean isKeepOnScreen = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SplashScreen splashScreen = SplashScreen.installSplashScreen(this);


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 3. Condición para mantener el Splash visible mientras cargan los datos
        splashScreen.setKeepOnScreenCondition(() -> isKeepOnScreen);

        // 4. Lanzamos tu método de carga de datos (API, mapas, base de datos, etc.)
        cargarDatosDeLaApp();

        // 5. Cargamos directamente el fragmento inicial del mapa
        loadFragment(new FragmentMapes(), true);

        // Configuración del BottomNavigationView
        BottomNavigationView bottomNav = findViewById(R.id.bottomNav);
        bottomNav.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                Fragment fragment = null;
                int itemId = menuItem.getItemId();
                if (itemId == R.id.mapaId) {
                    fragment = new FragmentMapes();
                } else if (itemId == R.id.altreId) {
                    fragment = new FragmentPerfil();
                } else if (itemId == R.id.estadistiquesId) {
                    fragment = new FragmentEstadistiques();
                } else if (itemId == R.id.configuracioId) {
                    fragment = new FragmentConfiguracio();
                }

                if (fragment != null) {
                    loadFragment(fragment, true);
                }
                return true;
            }
        });
    }
    // 6. Tu función asíncrona para cargar los datos necesarios antes de mostrar la App
    private void cargarDatosDeLaApp() {
        // Este Handler simula una carga de 2.5 segundos (reemplázalo por tu lógica real)
        new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                // Cuando tus datos estén listos, cambiamos a false y el Splash desaparecerá solo
                isKeepOnScreen = false;
            }
        }, 2500);
    }

    private void loadFragment(Fragment fragment, boolean reloadMap) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frameLayout, fragment);

        if (fragment instanceof FragmentMapes && reloadMap) {
            fragmentTransaction.addToBackStack(null);
        }
        fragmentTransaction.commit();
    }
}













/*
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Cargar el fragmento inicial
        //loadFragment(new FragmentMapes());

        BottomNavigationView bottomNav = findViewById(R.id.bottomNav);

        loadFragment(new FragmentMapes(),false);

        bottomNav.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                int itemId = menuItem.getItemId();
                if (itemId == R.id.mapaId) {

                    loadFragment(new FragmentMapes(),true);

                } else if (itemId == R.id.altreId) {
                    loadFragment(new FragmentAfegir(),true);
                } else if (itemId == R.id.estadistiquesId) {
                    loadFragment(new FragmentEstadistiques(),true);
                }


                return false;
            }
        });

        /*bottomNav.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Fragment fragment = null;
                switch (item.getItemId()) {
                    case R.id.mapaId:
                        fragment = new FragmentMapes();
                        break;
                    case R.id.altreId:
                        fragment = new FragmentAfegir();
                        break;
                    case R.id.estadistiquesId:
                        fragment = new FragmentEstadistiques();
                        break;
                    case R.id.configuracioId:
                        fragment = new FragmentConfiguracio();
                        break;
                }
                return loadFragment(fragment);
            }
        });
    }


    private void loadFragment (Fragment fragment, boolean appIsInicialized) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        if (appIsInicialized) {
            fragmentTransaction.add(R.id.frameLayout, fragment);
        } else {
            fragmentTransaction.replace(R.id.frameLayout, fragment);
        }

        fragmentTransaction.commit();
    }

    /*private boolean loadFragment(Fragment fragment) {
        // Cargar fragmento
        if (fragment != null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.frameLayout, fragment)
                    .commit();
            return true;
        }
        return false;
    }
}
*/

