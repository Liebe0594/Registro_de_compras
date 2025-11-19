package com.example.myapplication;

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
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.ItemTouchHelper;

import com.example.myapplication.model.Compra;
import com.example.myapplication.model.DetalleCompra;
import com.example.myapplication.model.Producto;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import org.json.JSONArray;
import org.json.JSONObject;
import org.osmdroid.config.Configuration;
import org.osmdroid.events.MapEventsReceiver;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.MapEventsOverlay;
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

    // --- UI Variables ---
    private ArrayList<Producto> carritoDeCompras;
    private RecyclerView recyclerViewCarrito;
    private ProductoAdapter productoAdapter;

    private TextView textoSubtotal, textoAhorro, textoTotalFinal;

    // Botones de acción
    private Button botonAgregarProducto, botonFinalizarCompra, botonVerHistorial, botonLimpiarCarrito;
    private Button botonCerrarSesion; // Botón para salir

    // --- Datos y Base de Datos Local ---
    private DatabaseHelper dbHelper;

    // --- Firebase ---
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    // --- Variables para el Mapa (Osmdroid) ---
    private MapView mapaDelDialogo;
    private Marker marcadorDeTienda;
    private Handler searchHandler = new Handler();
    private Runnable searchRunnable;
    private GeoPoint ultimaUbicacionEncontrada;

    private static final int HISTORIAL_REQUEST_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Configuración de Osmdroid
        Context ctx = getApplicationContext();
        Configuration.getInstance().load(ctx, getSharedPreferences("osmdroid", MODE_PRIVATE));

        setContentView(R.layout.activity_main);

        // Inicializar Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Inicializar DB Local
        dbHelper = new DatabaseHelper(this);

        // Inicializar Vistas
        textoSubtotal = findViewById(R.id.texto_subtotal);
        textoAhorro = findViewById(R.id.texto_ahorro);
        textoTotalFinal = findViewById(R.id.texto_total_final);

        botonAgregarProducto = findViewById(R.id.boton_agregar_producto);
        botonFinalizarCompra = findViewById(R.id.boton_finalizar_compra);
        botonVerHistorial = findViewById(R.id.boton_ver_historial);
        botonLimpiarCarrito = findViewById(R.id.boton_limpiar_carrito);
        botonCerrarSesion = findViewById(R.id.boton_cerrar_sesion); // Vincular botón salir

        // --- CONFIGURACIÓN DEL RECYCLERVIEW (CARRITO) ---
        carritoDeCompras = new ArrayList<>();
        recyclerViewCarrito = findViewById(R.id.recycler_view_carrito);

        // 1. Inicializamos el adaptador pasándole: la lista Y el método para editar
        productoAdapter = new ProductoAdapter(carritoDeCompras, this::mostrarDialogoEditarProducto);

        recyclerViewCarrito.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewCarrito.setAdapter(productoAdapter);

        // 2. CONFIGURAR SWIPE TO DELETE (Deslizar para borrar)
        ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false; // No queremos mover (drag & drop), solo deslizar
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                Producto productoBorrado = carritoDeCompras.get(position);

                // Borrar de la lista y notificar
                carritoDeCompras.remove(position);
                productoAdapter.notifyItemRemoved(position);

                // Actualizar totales
                actualizarTotales();

                Toast.makeText(MainActivity.this, "Borrado: " + productoBorrado.getNombre(), Toast.LENGTH_SHORT).show();
            }
        };

        // Unir el gesto al RecyclerView
        new ItemTouchHelper(simpleCallback).attachToRecyclerView(recyclerViewCarrito);


        // --- LISTENERS (EVENTOS DE LOS BOTONES) ---

        botonAgregarProducto.setOnClickListener(v -> mostrarDialogoAgregarProducto());
        botonFinalizarCompra.setOnClickListener(v -> mostrarDialogoFinalizarCompra());

        botonVerHistorial.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, HistorialActivity.class);
            startActivityForResult(intent, HISTORIAL_REQUEST_CODE);
        });

        // Lógica del botón Limpiar
        botonLimpiarCarrito.setOnClickListener(v -> {
            if (carritoDeCompras.isEmpty()) {
                Toast.makeText(MainActivity.this, "El carrito ya está vacío", Toast.LENGTH_SHORT).show();
                return;
            }

            new AlertDialog.Builder(MainActivity.this)
                    .setTitle("¿Limpiar Carrito?")
                    .setMessage("¿Estás seguro de que deseas eliminar todos los productos?")
                    .setPositiveButton("Sí, Limpiar", (dialog, which) -> limpiarDespuesDeGuardar())
                    .setNegativeButton("Cancelar", null)
                    .show();
        });

        // --- LÓGICA DE CERRAR SESIÓN ---
        botonCerrarSesion.setOnClickListener(v -> {
            new AlertDialog.Builder(MainActivity.this)
                    .setTitle("Cerrar Sesión")
                    .setMessage("¿Deseas salir de la aplicación?")
                    .setPositiveButton("Sí, Salir", (dialog, which) -> {
                        // 1. Cerrar sesión en Firebase
                        FirebaseAuth.getInstance().signOut();

                        // 2. Redirigir al Login (Asegúrate de que tu clase se llame LoginActivity)
                        Intent intent = new Intent(MainActivity.this, LoginActivity.class);

                        // Esto evita que el usuario pueda volver atrás con el botón "Back"
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

                        startActivity(intent);
                        finish();
                    })
                    .setNegativeButton("Cancelar", null)
                    .show();
        });

        actualizarTotales();
    }

    // --- Lógica de Totales ---

    private void actualizarTotales() {
        double subtotal = 0.0, totalFinal = 0.0, ahorroTotal = 0.0;
        for (Producto producto : carritoDeCompras) {
            subtotal += (producto.getCantidad() * producto.getPrecioUnitario());
            totalFinal += producto.getPrecioTotal();
            ahorroTotal += (producto.getPrecioSubtotal() - producto.getPrecioTotal());
        }

        textoSubtotal.setText(String.format(Locale.US, "$%.2f", subtotal));
        if(textoAhorro != null) {
            textoAhorro.setText(String.format(Locale.US, "-$%.2f", ahorroTotal));
        }
        textoTotalFinal.setText(String.format(Locale.US, "$%.2f", totalFinal));
    }

    // --- Diálogos ---

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
            String descuentoStr = editDescuento != null ? editDescuento.getText().toString().trim() : "";

            if (TextUtils.isEmpty(nombre) || TextUtils.isEmpty(cantidadStr) || TextUtils.isEmpty(precioStr)) {
                Toast.makeText(MainActivity.this, "Campos obligatorios vacíos", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                int cantidad = Integer.parseInt(cantidadStr);
                double precio = Double.parseDouble(precioStr);
                double descuento = TextUtils.isEmpty(descuentoStr) ? 0.0 : Double.parseDouble(descuentoStr);

                Producto nuevoProducto = new Producto(nombre, cantidad, precio, descuento);

                carritoDeCompras.add(nuevoProducto);

                if (productoAdapter != null) {
                    productoAdapter.notifyDataSetChanged();
                } else {
                    productoAdapter = new ProductoAdapter(carritoDeCompras, this::mostrarDialogoEditarProducto);
                    recyclerViewCarrito.setAdapter(productoAdapter);
                }

                actualizarTotales();
                Toast.makeText(MainActivity.this, "Producto añadido", Toast.LENGTH_SHORT).show();

            } catch (NumberFormatException e) {
                Toast.makeText(MainActivity.this, "Números inválidos", Toast.LENGTH_LONG).show();
            }
        });

        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.cancel());
        builder.create().show();
    }

    // --- MÉTODO: EDITAR PRODUCTO ---
    private void mostrarDialogoEditarProducto(int position) {
        Producto productoAEditar = carritoDeCompras.get(position);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View viewDialogo = LayoutInflater.from(this).inflate(R.layout.dialogo_agregar_producto, null);
        builder.setView(viewDialogo);
        builder.setTitle("Editar Producto");

        final EditText editNombre = viewDialogo.findViewById(R.id.edit_nombre_producto);
        final EditText editCantidad = viewDialogo.findViewById(R.id.edit_cantidad_producto);
        final EditText editPrecio = viewDialogo.findViewById(R.id.edit_precio_producto);
        final EditText editDescuento = viewDialogo.findViewById(R.id.edit_descuento_producto);

        // Rellenar con datos existentes
        editNombre.setText(productoAEditar.getNombre());
        editCantidad.setText(String.valueOf(productoAEditar.getCantidad()));
        editPrecio.setText(String.valueOf(productoAEditar.getPrecioUnitario()));
        editDescuento.setText(String.valueOf(productoAEditar.getDescuento()));

        builder.setPositiveButton("Guardar Cambios", (dialog, which) -> {
            String nombre = editNombre.getText().toString().trim();
            String cantidadStr = editCantidad.getText().toString().trim();
            String precioStr = editPrecio.getText().toString().trim();
            String descuentoStr = editDescuento != null ? editDescuento.getText().toString().trim() : "";

            if (TextUtils.isEmpty(nombre) || TextUtils.isEmpty(cantidadStr) || TextUtils.isEmpty(precioStr)) {
                Toast.makeText(MainActivity.this, "Error: Campos vacíos", Toast.LENGTH_SHORT).show();
                productoAdapter.notifyItemChanged(position);
                return;
            }

            try {
                int cantidad = Integer.parseInt(cantidadStr);
                double precio = Double.parseDouble(precioStr);
                double descuento = TextUtils.isEmpty(descuentoStr) ? 0.0 : Double.parseDouble(descuentoStr);

                // Actualizar objeto existente
                productoAEditar.setNombre(nombre);
                productoAEditar.setCantidad(cantidad);
                productoAEditar.setPrecioUnitario(precio);
                productoAEditar.setDescuento(descuento);

                productoAdapter.notifyItemChanged(position);
                actualizarTotales();
                Toast.makeText(MainActivity.this, "Producto actualizado", Toast.LENGTH_SHORT).show();

            } catch (NumberFormatException e) {
                Toast.makeText(MainActivity.this, "Números inválidos", Toast.LENGTH_LONG).show();
            }
        });

        builder.setNegativeButton("Cancelar", (dialog, which) -> {
            dialog.cancel();
            productoAdapter.notifyItemChanged(position);
        });
        builder.create().show();
    }

    private void mostrarDialogoFinalizarCompra() {
        if (carritoDeCompras.isEmpty()) {
            Toast.makeText(this, "El carrito está vacío.", Toast.LENGTH_SHORT).show();
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View viewDialogo = LayoutInflater.from(this).inflate(R.layout.dialogo_finalizar_compra, null);
        builder.setView(viewDialogo);

        final EditText editNombreTienda = viewDialogo.findViewById(R.id.edit_nombre_tienda);
        final EditText editDireccionTienda = viewDialogo.findViewById(R.id.edit_direccion_tienda);

        // Configuración del Mapa
        mapaDelDialogo = viewDialogo.findViewById(R.id.mapa_dialogo);
        mapaDelDialogo.setTileSource(TileSourceFactory.MAPNIK);
        mapaDelDialogo.setMultiTouchControls(true);
        mapaDelDialogo.getController().setZoom(15.0);

        GeoPoint startPoint = new GeoPoint(-33.4489, -70.6693); // Santiago por defecto
        mapaDelDialogo.getController().setCenter(startPoint);
        ultimaUbicacionEncontrada = null;

        marcadorDeTienda = new Marker(mapaDelDialogo);
        marcadorDeTienda.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);

        // Eventos del Mapa
        MapEventsReceiver mapEventsReceiver = new MapEventsReceiver() {
            @Override
            public boolean singleTapConfirmedHelper(GeoPoint p) {
                actualizarPosicionMarcadorManualmente(p, editDireccionTienda, editNombreTienda);
                return true;
            }
            @Override public boolean longPressHelper(GeoPoint p) { return false; }
        };
        MapEventsOverlay mapEventsOverlay = new MapEventsOverlay(mapEventsReceiver);
        mapaDelDialogo.getOverlays().add(0, mapEventsOverlay);

        // Geocoding
        editDireccionTienda.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                if (searchRunnable != null) searchHandler.removeCallbacks(searchRunnable);
                searchRunnable = () -> {
                    String address = s.toString();
                    if (!address.trim().isEmpty()) {
                        new GeocodeTask().execute(address);
                    }
                };
                searchHandler.postDelayed(searchRunnable, 1000);
            }
        });

        builder.setPositiveButton("Guardar Compra", (dialog, which) -> {
            String nombreTienda = editNombreTienda.getText().toString().trim();
            String direccionTienda = editDireccionTienda.getText().toString().trim();

            if (TextUtils.isEmpty(nombreTienda)) {
                Toast.makeText(this, "El nombre de la tienda es obligatorio", Toast.LENGTH_SHORT).show();
                return;
            }
            procesarYGuardarCompra(nombreTienda, direccionTienda);
        });

        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.cancel());
        builder.create().show();
    }

    // --- Lógica de Guardado (Local + Firebase) ---

    private void procesarYGuardarCompra(String nombreTienda, String direccionTienda) {
        Date fechaActual = new Date();

        String totalString = textoTotalFinal.getText().toString().replace("$", "").replace(",", ".");
        double totalCompra = 0.0;
        try {
            totalCompra = Double.parseDouble(totalString);
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }

        List<DetalleCompra> detallesDeLaCompra = new ArrayList<>();
        for (Producto p : carritoDeCompras) {
            detallesDeLaCompra.add(new DetalleCompra(
                    p.getNombre(),
                    p.getCantidad(),
                    p.getPrecioUnitario(),
                    p.getDescuento(),
                    p.calcularTotal()
            ));
        }

        double lat = 0.0, lon = 0.0;
        if (ultimaUbicacionEncontrada != null) {
            lat = ultimaUbicacionEncontrada.getLatitude();
            lon = ultimaUbicacionEncontrada.getLongitude();
        }

        Compra nuevaCompra = new Compra(fechaActual, nombreTienda, direccionTienda, totalCompra, detallesDeLaCompra, lat, lon);

        boolean exitoLocal = false;
        try {
            dbHelper.insertarCompra(nuevaCompra);
            exitoLocal = true;
        } catch (Exception e) {
            Log.e("DB_LOCAL", "Error guardando localmente", e);
        }

        FirebaseUser user = mAuth.getCurrentUser();

        if (user != null) {
            String userId = user.getUid();
            db.collection("usuarios").document(userId).collection("compras")
                    .add(nuevaCompra)
                    .addOnSuccessListener(documentReference -> {
                        Toast.makeText(MainActivity.this, "¡Guardado en la Nube y Local!", Toast.LENGTH_LONG).show();
                        limpiarDespuesDeGuardar();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(MainActivity.this, "Guardado local, pero error en Nube: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        limpiarDespuesDeGuardar();
                    });
        } else {
            Toast.makeText(this, "No hay usuario logueado. Se guardó solo localmente.", Toast.LENGTH_LONG).show();
            if(exitoLocal) limpiarDespuesDeGuardar();
        }
    }

    private void limpiarDespuesDeGuardar() {
        runOnUiThread(() -> {
            Log.d("DEBUG_APP", "--- INICIANDO LIMPIEZA TOTAL ---");

            carritoDeCompras = new ArrayList<>();

            // IMPORTANTE: Al recrear el adaptador, volvemos a pasar el listener
            productoAdapter = new ProductoAdapter(carritoDeCompras, this::mostrarDialogoEditarProducto);
            recyclerViewCarrito.setAdapter(productoAdapter);
            recyclerViewCarrito.invalidate();

            textoSubtotal.setText("$0.00");
            if (textoAhorro != null) textoAhorro.setText("-$0.00");
            textoTotalFinal.setText("$0.00");

            ultimaUbicacionEncontrada = null;
            if (mapaDelDialogo != null && marcadorDeTienda != null) {
                mapaDelDialogo.getOverlays().remove(marcadorDeTienda);
            }

            Toast.makeText(MainActivity.this, "Carrito vaciado", Toast.LENGTH_SHORT).show();
        });
    }


    // --- Lógica del Mapa (Helpers) ---

    private void actualizarPosicionMarcadorManualmente(GeoPoint p, EditText editDireccion, EditText editNombre) {
        ultimaUbicacionEncontrada = p;
        mapaDelDialogo.getOverlays().remove(marcadorDeTienda);
        marcadorDeTienda.setPosition(p);
        marcadorDeTienda.setTitle(editNombre.getText().toString());
        mapaDelDialogo.getOverlays().add(marcadorDeTienda);
        mapaDelDialogo.invalidate();
        new ReverseGeocodeTask(editDireccion).execute(p);
    }

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
                while ((inputLine = in.readLine()) != null) response.append(inputLine);
                in.close();
                JSONArray jsonArray = new JSONArray(response.toString());
                if (jsonArray.length() > 0) {
                    JSONObject result = jsonArray.getJSONObject(0);
                    return new GeoPoint(result.getDouble("lat"), result.getDouble("lon"));
                }
            } catch (Exception e) {
                Log.e("GeocodeTask", "Error", e);
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
            }
        }
    }

    private class ReverseGeocodeTask extends AsyncTask<GeoPoint, Void, String> {
        private EditText targetEditText;
        ReverseGeocodeTask(EditText target) { this.targetEditText = target; }

        @Override
        protected String doInBackground(GeoPoint... params) {
            GeoPoint point = params[0];
            String urlString = String.format(Locale.US,
                    "https://nominatim.openstreetmap.org/reverse?format=json&lat=%f&lon=%f&addressdetails=1",
                    point.getLatitude(), point.getLongitude());
            try {
                URL url = new URL(urlString);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("User-Agent", getPackageName());
                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder response = new StringBuilder();
                String inputLine;
                while ((inputLine = in.readLine()) != null) response.append(inputLine);
                in.close();
                JSONObject jsonObject = new JSONObject(response.toString());
                if (jsonObject.has("display_name")) return jsonObject.getString("display_name");
            } catch (Exception e) {
                Log.e("ReverseGeocodeTask", "Error", e);
            }
            return null;
        }

        @Override
        protected void onPostExecute(String resultAddress) {
            if (resultAddress != null && targetEditText != null) {
                targetEditText.setText(resultAddress);
            }
        }
    }
}








//Implementar geocodificación en el diálogo de finalizar compra
//
//Se integra un mapa de osmdroid en `MainActivity` para geolocalizar la tienda al finalizar una compra.
//
//- Se añade un mapa al diálogo de finalización.
//- Una `AsyncTask` convierte la dirección a coordenadas (lat/lon) usando la API de Nominatim.
//- Un `Handler` optimiza las llamadas a la API mientras el usuario escribe.
//- Las coordenadas se guardan en el objeto `Compra`.


