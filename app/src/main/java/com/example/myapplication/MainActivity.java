package com.example.myapplication;

import com.example.myapplication.model.Compra;
import com.example.myapplication.model.DetalleCompra;
import com.example.myapplication.model.Transaccion;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private ArrayList<Producto> carritoDeCompras;
    private ArrayAdapter<Producto> adaptador;
    private ListView listViewCompras;

    private TextView textoSubtotal, textoAhorro, textoTotalFinal;
    private Button botonAgregarProducto, botonFinalizarCompra, botonVerHistorial;

    private ArrayList<Compra> historialDeCompras = new ArrayList<>();
    private ArrayList<Transaccion> libroDeTransacciones = new ArrayList<>();

    // Variables para el mapa en el diálogo
    private MapView mapaDelDialogo;
    private Marker marcadorDeTienda;
    private Handler searchHandler = new Handler();
    private Runnable searchRunnable;
    private GeoPoint ultimaUbicacionEncontrada;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Configuración de osmdroid (esencial)
        Context ctx = getApplicationContext();
        Configuration.getInstance().load(ctx, getSharedPreferences("osmdroid", MODE_PRIVATE));

        setContentView(R.layout.activity_main);

        // Inicialización de vistas
        carritoDeCompras = new ArrayList<>();
        listViewCompras = findViewById(R.id.lista_compras);
        textoSubtotal = findViewById(R.id.texto_subtotal);
        textoAhorro = findViewById(R.id.texto_ahorro);
        textoTotalFinal = findViewById(R.id.texto_total_final);
        botonAgregarProducto = findViewById(R.id.boton_agregar_producto);
        botonFinalizarCompra = findViewById(R.id.boton_finalizar_compra);
        botonVerHistorial = findViewById(R.id.boton_ver_historial);

        adaptador = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, carritoDeCompras);
        listViewCompras.setAdapter(adaptador);

        // Configuración de listeners para los botones
        botonAgregarProducto.setOnClickListener(v -> mostrarDialogoAgregarProducto());
        botonFinalizarCompra.setOnClickListener(v -> mostrarDialogoFinalizarCompra());
        botonVerHistorial.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, HistorialActivity.class);
            intent.putExtra("HISTORIAL_COMPRAS", historialDeCompras);
            startActivity(intent);
        });

        actualizarTotales();
    }

    private void mostrarDialogoFinalizarCompra() {
        if (carritoDeCompras.isEmpty()) {
            Toast.makeText(this, "El carrito está vacío. Añade productos primero.", Toast.LENGTH_SHORT).show();
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View viewDialogo = LayoutInflater.from(this).inflate(R.layout.dialogo_finalizar_compra, null);
        builder.setView(viewDialogo);

        final EditText editNombreTienda = viewDialogo.findViewById(R.id.edit_nombre_tienda);
        final EditText editDireccionTienda = viewDialogo.findViewById(R.id.edit_direccion_tienda);

        // Lógica del mapa en el diálogo
        mapaDelDialogo = viewDialogo.findViewById(R.id.mapa_dialogo);
        mapaDelDialogo.setTileSource(TileSourceFactory.MAPNIK);
        mapaDelDialogo.setMultiTouchControls(true);
        mapaDelDialogo.getController().setZoom(12.0);

        GeoPoint startPoint = new GeoPoint(-33.4489, -70.6693); // Santiago
        mapaDelDialogo.getController().setCenter(startPoint);
        ultimaUbicacionEncontrada = null; // Reseteamos la ubicación guardada

        marcadorDeTienda = new Marker(mapaDelDialogo);
        marcadorDeTienda.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);

        // Listeners para los EditText del diálogo
        editDireccionTienda.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                if (searchRunnable != null) searchHandler.removeCallbacks(searchRunnable);
                searchRunnable = () -> {
                    String address = s.toString();
                    if (!address.trim().isEmpty()) {
                        new GeocodeTask().execute(address);
                    } else {
                        mapaDelDialogo.getOverlays().remove(marcadorDeTienda);
                        mapaDelDialogo.invalidate();
                        ultimaUbicacionEncontrada = null;
                    }
                };
                searchHandler.postDelayed(searchRunnable, 1000);
            }
        });

        editNombreTienda.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(Editable s) {
                if (marcadorDeTienda != null) {
                    marcadorDeTienda.setTitle(s.toString());
                }
            }
        });

        builder.setPositiveButton("Guardar Compra", (dialog, which) -> {
            String nombreTienda = editNombreTienda.getText().toString().trim();
            String direccionTienda = editDireccionTienda.getText().toString().trim();

            if (TextUtils.isEmpty(nombreTienda) || TextUtils.isEmpty(direccionTienda)) {
                Toast.makeText(this, "El nombre y la dirección de la tienda son obligatorios", Toast.LENGTH_SHORT).show();
                return;
            }
            procesarYGuardarCompra(nombreTienda, direccionTienda);
        });

        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.cancel());

        AlertDialog dialog = builder.create();
        dialog.setOnShowListener(d -> mapaDelDialogo.onResume());
        dialog.setOnDismissListener(d -> mapaDelDialogo.onPause());
        dialog.show();
    }

    private void procesarYGuardarCompra(String nombreTienda, String direccionTienda) {
        Date fechaActual = new Date();
        double totalCompra = 0;

        try {
            totalCompra = Double.parseDouble(textoTotalFinal.getText().toString().replace("$", ""));
        } catch (NumberFormatException e) {
            Log.e("FinalizarCompra", "Error al parsear el total final");
            Toast.makeText(this, "Error en el cálculo del total.", Toast.LENGTH_SHORT).show();
            return;
        }

        List<DetalleCompra> detallesDeLaCompra = new ArrayList<>();
        for (Producto p : carritoDeCompras) {
            detallesDeLaCompra.add(new DetalleCompra(p.getNombre(), p.getCantidad(), p.getPrecioUnitario(), p.getDescuento(), p.getPrecioTotal()));
        }

        double lat = 0.0;
        double lon = 0.0;
        if (ultimaUbicacionEncontrada != null) {
            lat = ultimaUbicacionEncontrada.getLatitude();
            lon = ultimaUbicacionEncontrada.getLongitude();
        }

        Compra nuevaCompra = new Compra(fechaActual, nombreTienda, direccionTienda, totalCompra, detallesDeLaCompra, lat, lon);
        historialDeCompras.add(nuevaCompra);

        Transaccion nuevaTransaccion = new Transaccion(fechaActual, "Compra en " + nombreTienda, totalCompra, "egreso");
        libroDeTransacciones.add(nuevaTransaccion);

        Toast.makeText(this, "¡Compra en '" + nombreTienda + "' registrada!", Toast.LENGTH_LONG).show();
        carritoDeCompras.clear();
        adaptador.notifyDataSetChanged();
        actualizarTotales();
    }

    private void actualizarTotales() {
        double subtotal = 0.0, totalFinal = 0.0, ahorroTotal = 0.0;
        for (Producto producto : carritoDeCompras) {
            subtotal += producto.getPrecioSubtotal();
            totalFinal += producto.getPrecioTotal();
            ahorroTotal += producto.getAhorro();
        }
        textoSubtotal.setText(String.format(Locale.US, "$%.2f", subtotal));
        textoAhorro.setText(String.format(Locale.US, "-$%.2f", ahorroTotal));
        textoTotalFinal.setText(String.format(Locale.US, "$%.2f", totalFinal));
    }

    private void mostrarDialogoAgregarProducto() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View viewDialogo = LayoutInflater.from(this).inflate(R.layout.dialogo_agregar_producto, null);
        builder.setView(viewDialogo);

        final EditText editNombre = viewDialogo.findViewById(R.id.edit_nombre_producto);
        final EditText editCantidad = viewDialogo.findViewById(R.id.edit_cantidad_producto);
        final EditText editPrecio = viewDialogo.findViewById(R.id.edit_precio_producto);
        final EditText editDescuento = viewDialogo.findViewById(R.id.edit_descuento_producto);

        builder.setPositiveButton("Agregar", (dialog, which) -> {
            String nombre = editNombre.getText().toString().trim();
            String cantidadStr = editCantidad.getText().toString().trim();
            String precioStr = editPrecio.getText().toString().trim();
            String descuentoStr = editDescuento.getText().toString().trim();

            if (TextUtils.isEmpty(nombre) || TextUtils.isEmpty(cantidadStr) || TextUtils.isEmpty(precioStr)) {
                Toast.makeText(MainActivity.this, "Nombre, cantidad y precio son obligatorios", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                int cantidad = Integer.parseInt(cantidadStr);
                double precio = Double.parseDouble(precioStr);
                double descuento = TextUtils.isEmpty(descuentoStr) ? 0.0 : Double.parseDouble(descuentoStr);

                Producto nuevoProducto = new Producto(nombre, cantidad, precio, descuento);
                carritoDeCompras.add(nuevoProducto);

                adaptador.notifyDataSetChanged();
                actualizarTotales();
                Toast.makeText(MainActivity.this, "Producto añadido al carrito", Toast.LENGTH_SHORT).show();

            } catch (NumberFormatException e) {
                Toast.makeText(MainActivity.this, "Por favor, ingresa números válidos", Toast.LENGTH_LONG).show();
            }
        });

        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.cancel());
        builder.create().show();
    }

    // Clase interna para realizar la geocodificación en segundo plano
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
                    return new GeoPoint(result.getDouble("lat"), result.getDouble("lon"));
                }
            } catch (Exception e) {
                Log.e("GeocodeTask", "Error en geocodificación", e);
            }
            return null;
        }

        @Override
        protected void onPostExecute(GeoPoint resultPoint) {
            if (mapaDelDialogo == null || marcadorDeTienda == null) return;

            if (resultPoint != null) {
                ultimaUbicacionEncontrada = resultPoint;
                mapaDelDialogo.getOverlays().remove(marcadorDeTienda);
                marcadorDeTienda.setPosition(resultPoint);
                mapaDelDialogo.getOverlays().add(marcadorDeTienda);
                mapaDelDialogo.getController().animateTo(resultPoint);
                mapaDelDialogo.getController().setZoom(17.0);
                mapaDelDialogo.invalidate();
            } else {
                mapaDelDialogo.getOverlays().remove(marcadorDeTienda);
                mapaDelDialogo.invalidate();
                ultimaUbicacionEncontrada = null;
            }
        }
    }
}




