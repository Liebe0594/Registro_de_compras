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
    private static final int DATABASE_VERSION = 1;

    private static final String TABLE_COMPRAS = "compras";
    private static final String COL_ID = "id";
    private static final String COL_FECHA = "fecha";
    private static final String COL_TIENDA = "tienda";
    private static final String COL_DIRECCION = "direccion";
    private static final String COL_TOTAL = "total";
    private static final String COL_LAT = "latitud";
    private static final String COL_LON = "longitud";

    private static final String TABLE_DETALLES = "detalles";
    private static final String COL_COMPRA_ID = "compra_id";
    private static final String COL_PRODUCTO = "producto";
    private static final String COL_CANTIDAD = "cantidad";
    private static final String COL_PRECIO = "precio";
    private static final String COL_DESCUENTO = "descuento";
    private static final String COL_TOTAL_PRODUCTO = "total_producto";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createCompras = "CREATE TABLE " + TABLE_COMPRAS + "("
                + COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COL_FECHA + " INTEGER,"
                + COL_TIENDA + " TEXT,"
                + COL_DIRECCION + " TEXT,"
                + COL_TOTAL + " REAL,"
                + COL_LAT + " REAL,"
                + COL_LON + " REAL)";
        db.execSQL(createCompras);

        String createDetalles = "CREATE TABLE " + TABLE_DETALLES + "("
                + COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COL_COMPRA_ID + " INTEGER,"
                + COL_PRODUCTO + " TEXT,"
                + COL_CANTIDAD + " INTEGER,"
                + COL_PRECIO + " REAL,"
                + COL_DESCUENTO + " REAL,"
                + COL_TOTAL_PRODUCTO + " REAL,"
                + "FOREIGN KEY(" + COL_COMPRA_ID + ") REFERENCES " + TABLE_COMPRAS + "(" + COL_ID + "))";
        db.execSQL(createDetalles);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_DETALLES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_COMPRAS);
        onCreate(db);
    }

    public void insertarCompra(Compra compra) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_FECHA, compra.getFecha().getTime());
        cv.put(COL_TIENDA, compra.getNombreTienda());
        cv.put(COL_DIRECCION, compra.getDireccionTienda());
        cv.put(COL_TOTAL, compra.getTotal());
        cv.put(COL_LAT, compra.getLatitud());
        cv.put(COL_LON, compra.getLongitud());

        long compraId = db.insert(TABLE_COMPRAS, null, cv);

        for (DetalleCompra d : compra.getDetalles()) {
            ContentValues cvDet = new ContentValues();
            cvDet.put(COL_COMPRA_ID, compraId);
            cvDet.put(COL_PRODUCTO, d.getNombre());
            cvDet.put(COL_CANTIDAD, d.getCantidad());
            cvDet.put(COL_PRECIO, d.getPrecio());
            cvDet.put(COL_DESCUENTO, d.getDescuento());
            cvDet.put(COL_TOTAL_PRODUCTO, d.getPrecio());
            db.insert(TABLE_DETALLES, null, cvDet);
        }
        db.close();
    }

    public List<Compra> obtenerTodasLasCompras() {
        List<Compra> lista = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursorCompras = db.query(TABLE_COMPRAS, null, null, null, null, null, COL_FECHA + " DESC");

        if (cursorCompras.moveToFirst()) {
            do {
                long id = cursorCompras.getLong(cursorCompras.getColumnIndexOrThrow(COL_ID));
                Date fecha = new Date(cursorCompras.getLong(cursorCompras.getColumnIndexOrThrow(COL_FECHA)));
                String tienda = cursorCompras.getString(cursorCompras.getColumnIndexOrThrow(COL_TIENDA));
                String direccion = cursorCompras.getString(cursorCompras.getColumnIndexOrThrow(COL_DIRECCION));
                double total = cursorCompras.getDouble(cursorCompras.getColumnIndexOrThrow(COL_TOTAL));
                double lat = cursorCompras.getDouble(cursorCompras.getColumnIndexOrThrow(COL_LAT));
                double lon = cursorCompras.getDouble(cursorCompras.getColumnIndexOrThrow(COL_LON));

                // Obtener detalles
                List<DetalleCompra> detalles = new ArrayList<>();
                Cursor cursorDetalles = db.query(TABLE_DETALLES, null, COL_COMPRA_ID + "=?",
                        new String[]{String.valueOf(id)}, null, null, null);
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
                cursorDetalles.close();

                lista.add(new Compra(fecha, tienda, direccion, total, detalles, lat, lon));
            } while (cursorCompras.moveToNext());
        }
        cursorCompras.close();
        db.close();
        return lista;
    }
}
