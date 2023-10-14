package com.example.digitalnic;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private SQLiteDatabase database;

    private EditText etProducto, etCantidad, etPrecio;
    private Button btnAgregar, btnEliminar;
    private ListView lbProducto;

    private ArrayList<String> productNames = new ArrayList<>();
    private ArrayAdapter<String> adapter;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dbHelper = new DatabaseHelper(this);
        database = dbHelper.getWritableDatabase();

        etProducto = findViewById(R.id.etProducto);
        etCantidad = findViewById(R.id.etCantidad);
        etPrecio = findViewById(R.id.etPrecio);
        btnAgregar = findViewById(R.id.btnAgregar);
        btnEliminar = findViewById(R.id.btnEliminar);
        lbProducto = findViewById(R.id.lbProducto);

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_single_choice, productNames);
        lbProducto.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        lbProducto.setAdapter(adapter);

        loadProducts();

        btnAgregar.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                addProduct();
            }
        });

        btnEliminar.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                deleteProduct();
            }
        });
    }

    private void loadProducts() {
        productNames.clear();
        double total = 0.0;

        Cursor cursor = database.query(DatabaseHelper.TABLE_PRODUCTS, null, null, null, null, null, null);
        while (cursor.moveToNext()) {
            String name = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_PRODUCT_NAME));
            int quantity = cursor.getInt(cursor.getColumnIndex(DatabaseHelper.COLUMN_PRODUCT_QUANTITY));
            double price = cursor.getDouble(cursor.getColumnIndex(DatabaseHelper.COLUMN_PRODUCT_PRICE));

            total += price * quantity;

            productNames.add(name + " - " + quantity + " Precio C$" + price);
        }
        cursor.close();

        productNames.add("Total Inventario: C$" + total);

        adapter.notifyDataSetChanged();
    }

    private void addProduct() {
        String name = etProducto.getText().toString();
        String quantityStr = etCantidad.getText().toString();
        String priceStr = etPrecio.getText().toString();

        if (name.isEmpty() || quantityStr.isEmpty() || priceStr.isEmpty()) {
            Toast.makeText(this, "Por favor, introduce todos los datos del producto", Toast.LENGTH_SHORT).show();
            return;
        }

        int quantity = Integer.parseInt(quantityStr);
        double price = Double.parseDouble(priceStr);
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_PRODUCT_NAME, name);
        values.put(DatabaseHelper.COLUMN_PRODUCT_QUANTITY, quantity);
        values.put(DatabaseHelper.COLUMN_PRODUCT_PRICE, price);

        long result = database.insert(DatabaseHelper.TABLE_PRODUCTS, null, values);
        if (result != -1) {
            Toast.makeText(this, "Producto añadido", Toast.LENGTH_SHORT).show();
            etProducto.setText("");
            etCantidad.setText("");
            etPrecio.setText("");
            loadProducts();
        } else {
            Toast.makeText(this, "Error al añadir producto", Toast.LENGTH_SHORT).show();
        }
    }

    private void deleteProduct() {
        int position = lbProducto.getCheckedItemPosition();
        if (position != ListView.INVALID_POSITION && position != productNames.size() - 1) {
            String item = adapter.getItem(position);
            String[] parts = item.split(" - ");
            String name = parts[0];
            database.delete(DatabaseHelper.TABLE_PRODUCTS, DatabaseHelper.COLUMN_PRODUCT_NAME + "=?", new String[]{name});
            loadProducts();
            Toast.makeText(this, "Producto eliminado", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Selecciona un producto para eliminar", Toast.LENGTH_SHORT).show();
        }
    }

    protected void onDestroy() {
        database.close();
        dbHelper.close();
        super.onDestroy();
    }
}
