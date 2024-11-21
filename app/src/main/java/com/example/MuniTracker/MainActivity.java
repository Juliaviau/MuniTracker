package com.example.MuniTracker;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        startActivity(new Intent(MainActivity.this, FragmentLogin.class));

        // Cargar el fragmento inicial
        loadFragment(new FragmentMapes(), true);  // Indicamos que es el fragmento inicial

        BottomNavigationView bottomNav = findViewById(R.id.bottomNav);

        bottomNav.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                Fragment fragment = null;
                int itemId = menuItem.getItemId();
                if (itemId == R.id.mapaId) {
                    fragment = new FragmentMapes();  // Regresar al mapa
                } else if (itemId == R.id.altreId) {
                    fragment = new FragmentPerfil();
                } else if (itemId == R.id.estadistiquesId) {
                    fragment = new FragmentEstadistiques();
                } else if (itemId == R.id.configuracioId) {
                    fragment = new FragmentConfiguracio();
                }

                if (fragment != null) {
                    loadFragment(fragment, true);  // Recargar fragmento seleccionado
                }
                return true;  // Indica que el item fue seleccionado
            }
        });
    }

    private void loadFragment(Fragment fragment, boolean reloadMap) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frameLayout, fragment);  // Siempre usa replace

        // Si el fragmento es el de mapa, asegura que se recargue
        if (fragment instanceof FragmentMapes && reloadMap) {
            fragmentTransaction.addToBackStack(null);  // Asegura que el estado anterior sea restaurado
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

