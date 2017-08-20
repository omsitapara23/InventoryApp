package com.android.example.inventoryapp;

import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.android.example.inventoryapp.data.InventoryContract;
import com.android.example.inventoryapp.data.InventoryDbHelper;

public class CatalogActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {
    private ListView listView;
    private static final int INVENTORY_LOADER = 0;
    private InventoryCursorAdapter inventoryCursorAdapter;
    private InventoryDbHelper inventoryDbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_catalog);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.addItem);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CatalogActivity.this, EditorActivity.class);
                startActivity(intent);
            }
        });

        listView = (ListView) findViewById(R.id.inventory_list);
        inventoryCursorAdapter = new InventoryCursorAdapter(this, null);
        listView.setAdapter(inventoryCursorAdapter);
        inventoryDbHelper = new InventoryDbHelper(this);
        getLoaderManager().initLoader(INVENTORY_LOADER, null, this);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(CatalogActivity.this, EditorActivity.class);
                Uri currentItem = ContentUris.withAppendedId(InventoryContract.InventoryEntry.CONTENT_URI, id);
                intent.setData(currentItem);
                startActivity(intent);
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_catalog, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_delete:
                deleteAllItems();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void deleteAllItems() {
        int rowsDeleted = getContentResolver().delete(InventoryContract.InventoryEntry.CONTENT_URI, null, null);
        Log.v("CatalogActivity", rowsDeleted + " rows deleted from pet database");
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] projection = {
                InventoryContract.InventoryEntry._ID,
                InventoryContract.InventoryEntry.NAME_COLUMN,
                InventoryContract.InventoryEntry.TYPE_COLUMN,
                InventoryContract.InventoryEntry.PRICE_COLUMN,
                InventoryContract.InventoryEntry.STOCK_COLUMN
        };
        return new CursorLoader(this,
                InventoryContract.InventoryEntry.CONTENT_URI,
                projection,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        inventoryCursorAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        inventoryCursorAdapter.swapCursor(null);
    }
}
