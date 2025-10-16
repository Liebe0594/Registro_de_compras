package com.example.myapplication;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout; // Importar LinearLayout
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.model.Compra; // Importar tu modelo Compra

import org.json.JSONArray;
import org.json.JSONObject;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class MapsActivity extends AppCompatActivity {

    private MapView map;
    private EditText editTextStoreName;
    private EditText editTextStoreAddress;
    private Marker storeMarker;

    private Handler searchHandler = new Handler();
    private Runnable searchRunnable;

    // --- INICIO DE NUEVOS CAMBIOS ---
    private LinearLayout formContainer; // Referencia al contenedor del formulario
    private Compra compraParaMostrar; // Variable para guardar la compra recibida
    // --- FIN DE NUEVOS CAMBIOS ---

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Context ctx = getApplicationContext();
        Configuration.getInstance().load(ctx, getSharedPreferences("osmdroid", MODE_PRIVATE));

        setContentView(R.layout.activity_maps);

        // --- INICIO DE NUEVOS CAMBIOS ---
        // Obtenemos la compra enviada desde HistorialActivity, si existe.
        if (getIntent().hasExtra("COMPRA_SELECCIONADA")) {
            compraParaMostrar = (Compra) getIntent().getSerializableExtra("COMPRA_SELECCIONADA");
        }
        // --- FIN DE NUEVOS CAMBIOS ---

        // Inicialización de vistas
        map = findViewById(R.id.map);
        editTextStoreName = findViewById(R.id.edit_text_store_name);
        editTextStoreAddress = findViewById(R.id.edit_text_store_address);
        formContainer = findViewById(R.id.form_container); // Obtenemos el contenedor

        // Configuración inicial del mapa
        map.setTileSource(TileSourceFactory.MAPNIK);
        map.setMultiTouchControls(true);
        map.getController().setZoom(12.0);

        // Inicializar el marcador
        storeMarker = new Marker(map);
        storeMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);

        // --- INICIO DE LÓGICA DE MODO DUAL ---
        if (compraParaMostrar != null) {
            // MODO VISUALIZACIÓN: Recibimos una compra, mostramos su ubicación.
            setupVisualizationMode();
        } else {
            // MODO INTERACTIVO: No recibimos nada, activamos la búsqueda en tiempo real.
            setupInteractiveMode();
        }
        // --- FIN DE LÓGICA DE MODO DUAL ---
    }

    private void setupInteractiveMode() {
        formContainer.setVisibility(View.VISIBLE); // Mostramos el formulario

        // Centrar en una ubicación inicial para la búsqueda
        GeoPoint startPoint = new GeoPoint(-33.4489, -70.6693); // Santiago
        map.getController().setCenter(startPoint);

        // Activamos los listeners para la búsqueda en tiempo real
        setupInputListeners();
    }

    private void setupVisualizationMode() {
        formContainer.setVisibility(View.GONE); // Ocultamos el formulario de búsqueda

        // Verificamos si la compra tiene una ubicación válida
        if (compraParaMostrar.getLatitud() != 0.0 || compraParaMostrar.getLongitud() != 0.0) {
            GeoPoint storeLocation = new GeoPoint(compraParaMostrar.getLatitud(), compraParaMostrar.getLongitud());

            // Colocamos el marcador en el mapa
            storeMarker.setPosition(storeLocation);
            storeMarker.setTitle(compraParaMostrar.getNombreTienda());

            map.getOverlays().add(storeMarker);

            // Centramos el mapa en la ubicación y hacemos zoom
            map.getController().setCenter(storeLocation);
            map.getController().setZoom(17.0);

            // Abrimos la ventana de información del marcador automáticamente
            storeMarker.showInfoWindow();

        } else {
            // Si la compra no tiene coordenadas, mostramos un mensaje
            Toast.makeText(this, "Esta compra no tiene una ubicación guardada.", Toast.LENGTH_LONG).show();
            // Centramos el mapa en la ubicación por defecto
            GeoPoint startPoint = new GeoPoint(-33.4489, -70.6693);
            map.getController().setCenter(startPoint);
        }
    }

    private void setupInputListeners() {
        // ... (Este método es el mismo que antes, no necesita cambios)
        editTextStoreAddress.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (searchRunnable != null) {
                    searchHandler.removeCallbacks(searchRunnable);
                }
                searchRunnable = () -> {
                    String addressString = s.toString();
                    if (!addressString.trim().isEmpty()) {
                        new GeocodeTask().execute(addressString);
                    } else {
                        map.getOverlays().remove(storeMarker);
                        map.invalidate();
                    }
                };
                searchHandler.postDelayed(searchRunnable, 1000);
            }
        });

        editTextStoreName.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(Editable s) {
                if (storeMarker != null) {
                    storeMarker.setTitle(s.toString());
                }
            }
        });
    }

    // --- GeocodeTask y updateMarkerPosition son los mismos que antes ---
    // (Incluidos aquí para que el archivo esté completo)

    private class GeocodeTask extends AsyncTask<String, Void, GeoPoint> {
        @Override
        protected GeoPoint doInBackground(String... params) {
            String addressQuery = params[0];
            String urlString = "https://nominatim.openstreetmap.org/search?q=" +
                    URLEncoder.encode(addressQuery) + "&format=json&addressdetails=1&limit=1";
            try {
                URL url = new URL(urlString);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("User-Agent", getPackageName());

                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder response = new StringBuilder();
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

                JSONArray jsonArray = new JSONArray(response.toString());
                if (jsonArray.length() > 0) {
                    JSONObject result = jsonArray.getJSONObject(0);
                    double lat = result.getDouble("lat");
                    double lon = result.getDouble("lon");
                    return new GeoPoint(lat, lon);
                }
            } catch (Exception e) {
                Log.e("GeocodeTask", "Error en la geocodificación", e);
            }
            return null;
        }

        @Override
        protected void onPostExecute(GeoPoint resultPoint) {
            if (resultPoint != null) {
                updateMarkerPosition(resultPoint);
            } else {
                map.getOverlays().remove(storeMarker);
                map.invalidate();
            }
        }
    }

    private void updateMarkerPosition(GeoPoint point) {
        map.getOverlays().remove(storeMarker);
        storeMarker.setPosition(point);
        storeMarker.setTitle(editTextStoreName.getText().toString());
        map.getOverlays().add(storeMarker);
        map.getController().animateTo(point);
        map.invalidate();
    }

    @Override
    public void onResume() {
        super.onResume();
        map.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        map.onPause();
    }
}
