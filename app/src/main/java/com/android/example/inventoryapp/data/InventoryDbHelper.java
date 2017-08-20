package com.android.example.inventoryapp.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by root on 18/8/17.
 */

public class InventoryDbHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "inventory.db";
    private static final int DATABASE_VERSION = 1;

    public InventoryDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        String CREATE_SQL_TABLE = " CREATE TABLE " + InventoryContract.InventoryEntry.TABLE_NAME + " ( "
                + InventoryContract.InventoryEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + InventoryContract.InventoryEntry.NAME_COLUMN + " TEXT NOT NULL, "
                + InventoryContract.InventoryEntry.TYPE_COLUMN + " TEXT, "
                + InventoryContract.InventoryEntry.PRICE_COLUMN + " INTEGER NOT NULL, "
                + InventoryContract.InventoryEntry.STOCK_COLUMN + " INTEGER" + " );";

        db.execSQL(CREATE_SQL_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
