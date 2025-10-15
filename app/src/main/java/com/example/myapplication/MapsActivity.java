package com.example.myapplication;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import org.osmdroid.config.Configuration;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

public class MapsActivity extends AppCompatActivity {

    private MapView map;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Configuración de osmdroid
        Configuration.getInstance().load(getApplicationContext(), getSharedPreferences("prefs", MODE_PRIVATE));
        setContentView(R.layout.activity_maps);

        map = findViewById(R.id.map);
        map.setBuiltInZoomControls(true);
        map.setMultiTouchControls(true);
        map.getController().setZoom(14.0);

        // Centrar el mapa en un punto inicial
        GeoPoint puntoCentral = new GeoPoint(-33.4489, -70.6693); // Santiago
        map.getController().setCenter(puntoCentral);

        // Agregar 5 marcadores de ejemplo
        agregarMarcador(-33.4489, -70.6693, "Tienda A");
        agregarMarcador(-33.4500, -70.6700, "Tienda B");
        agregarMarcador(-33.4490, -70.6670, "Tienda C");
        agregarMarcador(-33.4470, -70.6680, "Tienda D");
        agregarMarcador(-33.4510, -70.6710, "Tienda E");
    }

    private void agregarMarcador(double lat, double lon, String titulo) {
        GeoPoint punto = new GeoPoint(lat, lon);
        Marker marker = new Marker(map);
        marker.setPosition(punto);
        marker.setTitle(titulo);
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        map.getOverlays().add(marker);
    }
}
