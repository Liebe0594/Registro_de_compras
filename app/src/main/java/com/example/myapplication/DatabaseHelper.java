package com.example.myapplication;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.myapplication.model.Compra;
import com.example.myapplication.model.DetalleCompra;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "registro_compras.db";
    // ¡MUY IMPORTANTE! Incrementar la versión para que onUpgrade se ejecute.
    private static final int DATABASE_VERSION = 2;

    // --- Tabla y Columnas para Compras ---
    private static final String TABLE_COMPRAS = "compras";
    private static final String COL_ID = "id";
    private static final String COL_FECHA = "fecha";
    private static final String COL_TIENDA = "tienda";
    private static final String COL_DIRECCION = "direccion";
    private static final String COL_TOTAL = "total";
    private static final String COL_LAT = "latitud";
    private static final String COL_LON = "longitud";

    // --- Tabla y Columnas para Detalles de Compra ---
    private static final String TABLE_DETALLES = "detalles";
    private static final String COL_DETALLE_ID = "detalle_id"; // ID propio de la tabla detalle
    private static final String COL_COMPRA_ID = "compra_id"; // FK a la tabla compras
    private static final String COL_PRODUCTO = "producto";
    private static final String COL_CANTIDAD = "cantidad";
    private static final String COL_PRECIO = "precio";
    private static final String COL_DESCUENTO = "descuento";
    private static final String COL_TOTAL_PRODUCTO = "total_producto";

    // --- Tabla y Columnas para Usuarios ---
    private static final String TABLE_USUARIOS = "usuarios";
    private static final String COL_USER_ID = "id";
    private static final String COL_USERNAME = "username";
    private static final String COL_PASSWORD = "password";


    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Creación tabla compras
        String createCompras = "CREATE TABLE " + TABLE_COMPRAS + "("
                + COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COL_FECHA + " INTEGER,"
                + COL_TIENDA + " TEXT,"
                + COL_DIRECCION + " TEXT,"
                + COL_TOTAL + " REAL,"
                + COL_LAT + " REAL,"
                + COL_LON + " REAL)";
        db.execSQL(createCompras);

        // Creación tabla detalles
        String createDetalles = "CREATE TABLE " + TABLE_DETALLES + "("
                + COL_DETALLE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COL_COMPRA_ID + " INTEGER,"
                + COL_PRODUCTO + " TEXT,"
                + COL_CANTIDAD + " INTEGER,"
                + COL_PRECIO + " REAL,"
                + COL_DESCUENTO + " REAL,"
                + COL_TOTAL_PRODUCTO + " REAL,"
                + "FOREIGN KEY(" + COL_COMPRA_ID + ") REFERENCES " + TABLE_COMPRAS + "(" + COL_ID + "))";
        db.execSQL(createDetalles);

        // Creación NUEVA tabla usuarios
        String createUsuarios = "CREATE TABLE " + TABLE_USUARIOS + "("
                + COL_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COL_USERNAME + " TEXT UNIQUE,"
                + COL_PASSWORD + " TEXT)";
        db.execSQL(createUsuarios);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Esto borrará todos los datos existentes. Para una app real, se usaría una migración más segura.
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_DETALLES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_COMPRAS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USUARIOS); // Añadir esta línea
        onCreate(db);
    }

    // --- Métodos para Usuarios ---

    /**
     * Agrega un nuevo usuario a la base de datos.
     * @return true si el usuario se agregó, false si ya existía.
     */
    public boolean agregarUsuario(String username, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_USERNAME, username);
        // NOTA: En una app real, la contraseña debería estar encriptada (hashed).
        cv.put(COL_PASSWORD, password);

        long result = db.insert(TABLE_USUARIOS, null, cv);
        db.close();
        return result != -1;
    }

    /**
     * Verifica si un usuario y contraseña coinciden en la base de datos.
     * @return true si las credenciales son válidas, false en caso contrario.
     */
    public boolean verificarUsuario(String username, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = db.query(TABLE_USUARIOS,
                    new String[]{COL_USER_ID}, // Solo necesitamos saber si existe
                    COL_USERNAME + " = ? AND " + COL_PASSWORD + " = ?",
                    new String[]{username, password},
                    null, null, null);

            int count = cursor.getCount();
            return count > 0;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            db.close();
        }
    }

    // --- Métodos para Compras ---

    /**
     * Inserta una compra completa (cabecera y detalles) en la base de datos.
     * @param compra El objeto Compra a insertar.
     */
    public void insertarCompra(Compra compra) {
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            db.beginTransaction(); // Inicia una transacción para asegurar la integridad de los datos

            ContentValues cvCompra = new ContentValues();
            cvCompra.put(COL_FECHA, compra.getFecha().getTime());
            cvCompra.put(COL_TIENDA, compra.getNombreTienda());
            cvCompra.put(COL_DIRECCION, compra.getDireccionTienda());
            cvCompra.put(COL_TOTAL, compra.getTotal());
            cvCompra.put(COL_LAT, compra.getLatitud());
            cvCompra.put(COL_LON, compra.getLongitud());

            long compraId = db.insert(TABLE_COMPRAS, null, cvCompra);

            if (compraId != -1 && compra.getDetalles() != null) {
                for (DetalleCompra detalle : compra.getDetalles()) {
                    ContentValues cvDetalle = new ContentValues();
                    cvDetalle.put(COL_COMPRA_ID, compraId);
                    cvDetalle.put(COL_PRODUCTO, detalle.getNombre());
                    cvDetalle.put(COL_CANTIDAD, detalle.getCantidad());
                    cvDetalle.put(COL_PRECIO, detalle.getPrecio());
                    cvDetalle.put(COL_DESCUENTO, detalle.getDescuento());
                    cvDetalle.put(COL_TOTAL_PRODUCTO, detalle.getTotal());
                    db.insert(TABLE_DETALLES, null, cvDetalle);
                }
            }
            db.setTransactionSuccessful(); // Marca la transacción como exitosa
        } finally {
            db.endTransaction(); // Finaliza la transacción
            db.close();
        }
    }

    /**
     * Obtiene todas las compras de la base de datos, incluyendo sus detalles.
     * @return Una lista de objetos Compra.
     */
    public List<Compra> obtenerTodasLasCompras() {
        List<Compra> lista = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursorCompras = null;

        try {
            cursorCompras = db.query(TABLE_COMPRAS, null, null, null, null, null, COL_FECHA + " DESC");

            if (cursorCompras.moveToFirst()) {
                do {
                    long id = cursorCompras.getLong(cursorCompras.getColumnIndexOrThrow(COL_ID));
                    long fechaMillis = cursorCompras.getLong(cursorCompras.getColumnIndexOrThrow(COL_FECHA));
                    String tienda = cursorCompras.getString(cursorCompras.getColumnIndexOrThrow(COL_TIENDA));
                    String direccion = cursorCompras.getString(cursorCompras.getColumnIndexOrThrow(COL_DIRECCION));
                    double total = cursorCompras.getDouble(cursorCompras.getColumnIndexOrThrow(COL_TOTAL));
                    double lat = cursorCompras.getDouble(cursorCompras.getColumnIndexOrThrow(COL_LAT));
                    double lon = cursorCompras.getDouble(cursorCompras.getColumnIndexOrThrow(COL_LON));

                    List<DetalleCompra> detalles = obtenerDetallesParaCompra(db, id);

                    lista.add(new Compra(new Date(fechaMillis), tienda, direccion, total, detalles, lat, lon));
                } while (cursorCompras.moveToNext());
            }
        } finally {
            if (cursorCompras != null) {
                cursorCompras.close();
            }
            db.close();
        }
        return lista;
    }

    /**
     * Método auxiliar para obtener los detalles de una compra específica.
     * @param db La instancia de la base de datos (para no abrir y cerrar múltiples veces).
     * @param compraId El ID de la compra cuyos detalles se buscan.
     * @return Una lista de objetos DetalleCompra.
     */
    private List<DetalleCompra> obtenerDetallesParaCompra(SQLiteDatabase db, long compraId) {
        List<DetalleCompra> detalles = new ArrayList<>();
        Cursor cursorDetalles = null;
        try {
            cursorDetalles = db.query(TABLE_DETALLES, null, COL_COMPRA_ID + " = ?",
                    new String[]{String.valueOf(compraId)}, null, null, null);

            if (cursorDetalles.moveToFirst()) {
                do {
                    String nombre = cursorDetalles.getString(cursorDetalles.getColumnIndexOrThrow(COL_PRODUCTO));
                    int cantidad = cursorDetalles.getInt(cursorDetalles.getColumnIndexOrThrow(COL_CANTIDAD));
                    double precio = cursorDetalles.getDouble(cursorDetalles.getColumnIndexOrThrow(COL_PRECIO));
                    double descuento = cursorDetalles.getDouble(cursorDetalles.getColumnIndexOrThrow(COL_DESCUENTO));
                    double totalProducto = cursorDetalles.getDouble(cursorDetalles.getColumnIndexOrThrow(COL_TOTAL_PRODUCTO));
                    detalles.add(new DetalleCompra(nombre, cantidad, precio, descuento, totalProducto));
                } while (cursorDetalles.moveToNext());
            }
        } finally {
            if (cursorDetalles != null) {
                cursorDetalles.close();
            }
            // No cerramos la BD aquí porque es manejada por el método que llama a este.
        }
        return detalles;
    }
}


