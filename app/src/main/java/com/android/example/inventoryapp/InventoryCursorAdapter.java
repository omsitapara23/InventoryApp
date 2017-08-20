package com.android.example.inventoryapp;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.android.example.inventoryapp.data.InventoryContract;

/**
 * Created by root on 19/8/17.
 */

public class InventoryCursorAdapter extends CursorAdapter {
    public InventoryCursorAdapter(Context context, Cursor c) {
        super(context, c, 0);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        TextView nameTextView = (TextView) view.findViewById(R.id.name);
        TextView typeTextView = (TextView) view.findViewById(R.id.type);
        TextView priceTextView = (TextView) view.findViewById(R.id.price);

        int nameColumnIndex = cursor.getColumnIndex(InventoryContract.InventoryEntry.NAME_COLUMN);
        int typeColumnIndex = cursor.getColumnIndex(InventoryContract.InventoryEntry.TYPE_COLUMN);
        int priceColumnIndex = cursor.getColumnIndex(InventoryContract.InventoryEntry.PRICE_COLUMN);

        nameTextView.setText(cursor.getString(nameColumnIndex));
        typeTextView.setText(cursor.getString(typeColumnIndex));
        priceTextView.setText("Rs." + cursor.getString(priceColumnIndex));
    }
}
