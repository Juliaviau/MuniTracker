package com.example.MuniTracker;

import android.content.Context;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONArray;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class MapesHelper {

    private JSONObject territorios;

    public MapesHelper(Context context) {
        // Cargar y parsear el archivo JSON
        try {
            InputStream is = context.getResources().openRawResource(R.raw.contenen);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            String json = new String(buffer, "UTF-8");
            territorios = new JSONObject(json);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public int obtenirQuantitatMunicipisPerVegueria(String vegueriaId) {
        int cantidadMunicipios = 0;
        try {
            // Obtiene el objeto JSON de comarques
            JSONObject vegueries = territorios.getJSONObject("vegueriesMUN");
            // Verifica si la comarca existe y cuenta sus municipios
            if (vegueries.has(vegueriaId)) {
                JSONArray municipis = vegueries.getJSONArray(vegueriaId);
                cantidadMunicipios = municipis.length();
            } else {
                Log.i("MapesHelper","Comarca con ID " + vegueriaId + " no encontrada.");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return cantidadMunicipios;
    }

    public int obtenirQuantitatMunicipisPerProvincia(String provinciaId) {
        int cantidadMunicipios = 0;
        try {
            // Obtiene el objeto JSON de comarques
            JSONObject provincies = territorios.getJSONObject("provinciesMUN");
            // Verifica si la comarca existe y cuenta sus municipios
            if (provincies.has(provinciaId)) {
                JSONArray municipis = provincies.getJSONArray(provinciaId);
                cantidadMunicipios = municipis.length();
            } else {
                Log.i("MapesHelper","Comarca con ID " + provinciaId + " no encontrada.");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return cantidadMunicipios;
    }

    public int obtenirQuantitatMunicipisPerComarca(String comarcaId) {
        int cantidadMunicipios = 0;
        try {
            // Obtiene el objeto JSON de comarques
            JSONObject comarques = territorios.getJSONObject("comarques");
            // Verifica si la comarca existe y cuenta sus municipios
            if (comarques.has(comarcaId)) {
                JSONArray municipis = comarques.getJSONArray(comarcaId);
                cantidadMunicipios = municipis.length();
            } else {
                Log.i("MapesHelper","Comarca con ID " + comarcaId + " no encontrada.");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return cantidadMunicipios;
    }


    public List<String> obtenirNomsMunicipisTotesComarques() {
        List<String> nomsMunicipis = new ArrayList<>();
        try {
            JSONObject comarques = territorios.getJSONObject("comarques");
            Iterator<String> comarcaIds = comarques.keys(); // Get all comarca IDs

            while (comarcaIds.hasNext()) {
                String comarcaId = comarcaIds.next();
                JSONArray municipis = comarques.getJSONArray(comarcaId);
                for (int i = 0; i < municipis.length(); i++) {
                    String nomMunicipi = municipis.getString(i);
                    nomsMunicipis.add(nomMunicipi);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return nomsMunicipis;
    }

    public List<String> obtenirNomsComarques() {
        List<String> nomsComarques = new ArrayList<>();
        try {
            JSONObject comarques = territorios.getJSONObject("comarques");
            Iterator<String> comarcaIds = comarques.keys(); // Get all comarca IDs

            while (comarcaIds.hasNext()) {
                String comarcaId = comarcaIds.next();
                nomsComarques.add(comarcaId); // Add comarca ID to the list
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return nomsComarques;
    }


    // Método para obtener la comarca, veguería y provincia
    public TerritoryData getTerritoryData(String municipiId) {
        String comarcaId = null;
        String vegueriaId = null;
        String provinciaId = null;

        try {
            // Paso 2: Encontrar la comarca
            JSONObject comarques = territorios.getJSONObject("comarques");
            for (Iterator<String> it = comarques.keys(); it.hasNext(); ) {
                String comarca = it.next();
                JSONArray municipis = comarques.getJSONArray(comarca);
                for (int i = 0; i < municipis.length(); i++) {
                    if (municipis.getString(i).equals(municipiId)) {
                        comarcaId = comarca;
                        break;
                    }
                }
                if (comarcaId != null) break;
            }

            // Paso 3: Encontrar la veguería de la comarca
            if (comarcaId != null) {
                JSONObject vegueries = territorios.getJSONObject("vegueries");
                for (Iterator<String> it = vegueries.keys(); it.hasNext(); ) {
                    String vegueria = it.next();
                    JSONArray comarquesInVegueria = vegueries.getJSONArray(vegueria);
                    for (int i = 0; i < comarquesInVegueria.length(); i++) {
                        if (comarquesInVegueria.getString(i).equals(comarcaId)) {
                            vegueriaId = vegueria;
                            break;
                        }
                    }
                    if (vegueriaId != null) break;
                }
            }

            // Paso 4: Encontrar la provincia de la veguería
            if (comarcaId != null) {
                JSONObject provincies = territorios.getJSONObject("provincies");
                for (Iterator<String> it = provincies.keys(); it.hasNext(); ) {
                    String provincia = it.next();
                    JSONArray vegueriesInProvincia = provincies.getJSONArray(provincia);
                    for (int i = 0; i < vegueriesInProvincia.length(); i++) {
                        if (vegueriesInProvincia.getString(i).equals(comarcaId)) {
                            provinciaId = provincia;
                            break;
                        }
                    }
                    if (provinciaId != null) break;
                }
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return new TerritoryData(comarcaId, vegueriaId, provinciaId);
    }

    public static class TerritoryData {
        public final String comarcaId;
        public final String vegueriaId;
        public final String provinciaId;

        public TerritoryData(String comarcaId, String vegueriaId, String provinciaId) {
            this.comarcaId = comarcaId;
            this.vegueriaId = vegueriaId;
            this.provinciaId = provinciaId;
        }
    }
}
