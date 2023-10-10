package com.example.digitalnic;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import android.content.ContentValues;


import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private SQLiteDatabase database;

    private EditText productNameEditText, productQuantityEditText;
    private Button addButton, deleteButton;
    private ListView productListView;

    private ArrayList<String> productNames = new ArrayList<>();
    private ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dbHelper = new DatabaseHelper(this);
        database = dbHelper.getWritableDatabase();

        productNameEditText = findViewById(R.id.productNameEditText);
        productQuantityEditText = findViewById(R.id.productQuantityEditText);
        addButton = findViewById(R.id.addButton);
        deleteButton = findViewById(R.id.deleteButton);
        productListView = findViewById(R.id.productListView);

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_single_choice, productNames);
        productListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        productListView.setAdapter(adapter);

        loadProducts();

        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addProduct();
            }
        });

        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteProduct();
            }
        });
    }

    private void loadProducts() {
        productNames.clear();
        Cursor cursor = database.query(DatabaseHelper.TABLE_PRODUCTS, null, null, null, null, null, null);

        while (cursor.moveToNext()) {
            String name = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_PRODUCT_NAME));
            int quantity = cursor.getInt(cursor.getColumnIndex(DatabaseHelper.COLUMN_PRODUCT_QUANTITY));
            productNames.add(name + " - " + quantity);
        }
        cursor.close();
        adapter.notifyDataSetChanged();
    }

    private void addProduct() {
        String name = productNameEditText.getText().toString();
        String quantityStr = productQuantityEditText.getText().toString();

        if (name.isEmpty() || quantityStr.isEmpty()) {
            Toast.makeText(this, "Por favor, introduce los datos del producto", Toast.LENGTH_SHORT).show();
            return;
        }

        int quantity = Integer.parseInt(quantityStr);
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_PRODUCT_NAME, name);
        values.put(DatabaseHelper.COLUMN_PRODUCT_QUANTITY, quantity);

        long result = database.insert(DatabaseHelper.TABLE_PRODUCTS, null, values);
        if (result != -1) {
            Toast.makeText(this, "Producto añadido", Toast.LENGTH_SHORT).show();
            productNameEditText.setText("");
            productQuantityEditText.setText("");
            loadProducts();
        } else {
            Toast.makeText(this, "Error al añadir producto", Toast.LENGTH_SHORT).show();
        }
    }

    private void deleteProduct() {
        int position = productListView.getCheckedItemPosition();
        if (position != ListView.INVALID_POSITION) {
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

    @Override
    protected void onDestroy() {
        database.close();
        dbHelper.close();
        super.onDestroy();
    }
}
