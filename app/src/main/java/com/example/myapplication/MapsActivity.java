package com.example.myapplication;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.example.myapplication.model.Compra;
import com.example.myapplication.model.DetalleCompra;

import org.osmdroid.config.Configuration;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

import java.util.ArrayList;

public class MapsActivity extends AppCompatActivity {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 100;

    private MapView map;
    private ArrayList<Compra> historialDeCompras;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Configuration.getInstance().load(getApplicationContext(), getSharedPreferences("prefs", MODE_PRIVATE));
        setContentView(R.layout.activity_maps);

        map = findViewById(R.id.map);
        map.setBuiltInZoomControls(true);
        map.setMultiTouchControls(true);
        map.getController().setZoom(12.0);

        // Pedir permisos de ubicación
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        }

        // Obtener historial de compras desde MainActivity
        historialDeCompras = (ArrayList<Compra>) getIntent().getSerializableExtra("HISTORIAL_COMPRAS");

        // Centrar mapa: última compra o Santiago
        centrarMapa();
        mostrarMarcadoresCompras();
    }

    private void centrarMapa() {
        GeoPoint centro;
        if (historialDeCompras != null && !historialDeCompras.isEmpty()) {
            Compra ultimaCompra = historialDeCompras.get(historialDeCompras.size() - 1);
            double lat = ultimaCompra.getLatitud();
            double lon = ultimaCompra.getLongitud();
            if (lat != 0 && lon != 0) {
                centro = new GeoPoint(lat, lon);
            } else {
                centro = new GeoPoint(-33.4489, -70.6693); // Santiago
            }
        } else {
            centro = new GeoPoint(-33.4489, -70.6693); // Santiago
        }
        map.getController().setCenter(centro);
    }

    private void mostrarMarcadoresCompras() {
        if (historialDeCompras == null) return;

        for (Compra compra : historialDeCompras) {
            double lat = compra.getLatitud();
            double lon = compra.getLongitud();

            if (lat != 0 && lon != 0) {
                GeoPoint punto = new GeoPoint(lat, lon);
                Marker marker = new Marker(map);
                marker.setPosition(punto);
                marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);

                // Iconos según gasto
                double total = compra.getTotal();
                if (total < 10000) {
                    marker.setIcon(getResources().getDrawable(R.drawable.marker_verde));
                } else if (total < 50000) {
                    marker.setIcon(getResources().getDrawable(R.drawable.marker_amarillo));
                } else {
                    marker.setIcon(getResources().getDrawable(R.drawable.marker_rojo));
                }

                marker.setTitle(compra.getNombreTienda());

                // Mostrar detalles al hacer click
                marker.setOnMarkerClickListener((m, mapView) -> {
                    StringBuilder productos = new StringBuilder();
                    for (DetalleCompra det : compra.getDetalles()) {
                        productos.append(det.getCantidad())
                                .append("x ")
                                .append(det.getNombre())
                                .append("\n");
                    }
                    Toast.makeText(this,
                            "Tienda: " + compra.getNombreTienda() +
                                    "\nDirección: " + compra.getDireccionTienda() +
                                    "\nTotal: $" + String.format("%.2f", compra.getTotal()) +
                                    "\nProductos:\n" + productos.toString(),
                            Toast.LENGTH_LONG).show();
                    return true;
                });

                map.getOverlays().add(marker);
            }
        }
    }

    // Manejo de permisos
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permiso de ubicación concedido", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Permiso de ubicación denegado. No se podrá centrar en tu ubicación real.", Toast.LENGTH_LONG).show();
            }
        }
    }
}
