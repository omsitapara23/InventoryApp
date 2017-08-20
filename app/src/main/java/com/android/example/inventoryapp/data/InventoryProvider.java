package com.android.example.inventoryapp.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by root on 19/8/17.
 */

public class InventoryProvider extends ContentProvider {
    public static final String LOG_TAG = InventoryProvider.class.getSimpleName();

    private static final int INVENTORY = 100;
    private static final int INVENTORY_ITEM = 101;
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {

        sUriMatcher.addURI(InventoryContract.CONTENT_AUTHORITY, InventoryContract.PATH_INVENTORY, INVENTORY);
        sUriMatcher.addURI(InventoryContract.CONTENT_AUTHORITY, InventoryContract.PATH_INVENTORY + "/#", INVENTORY_ITEM);
    }


    private InventoryDbHelper mDbHelper;

    @Override
    public boolean onCreate() {
        mDbHelper = new InventoryDbHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {

        SQLiteDatabase database = mDbHelper.getReadableDatabase();

        // This cursor will hold the result of the query
        Cursor cursor;

        // Figure out if the URI matcher can match the URI to a specific code
        int match = sUriMatcher.match(uri);
        switch (match) {
            case INVENTORY:

                cursor = database.query(InventoryContract.InventoryEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            case INVENTORY_ITEM:

                selection = InventoryContract.InventoryEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};

                cursor = database.query(InventoryContract.InventoryEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Cannot query unknown URI " + uri);
        }

        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case INVENTORY:
                return insertItem(uri, contentValues);
            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }
    }

    private Uri insertItem(Uri uri, ContentValues values) {
        long id = -1;
        String name = values.getAsString(InventoryContract.InventoryEntry.NAME_COLUMN);
        if (name.length() == 0) {
            Toast.makeText(getContext(), "Name field can't be empty", Toast.LENGTH_SHORT).show();
            return null;
        }
        Integer price = values.getAsInteger(InventoryContract.InventoryEntry.PRICE_COLUMN);
        if (price != null && price <= 0) {
            Toast.makeText(getContext(), "Price invalid", Toast.LENGTH_SHORT).show();
            return null;
        }

        SQLiteDatabase database = mDbHelper.getWritableDatabase();
        id = database.insert(InventoryContract.InventoryEntry.TABLE_NAME, null, values);
        if (id == -1) {
            Log.e(LOG_TAG, "Failed to insert row for " + uri);
            return null;
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return ContentUris.withAppendedId(uri, id);
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // Get writeable database
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // Track the number of rows that were deleted
        int rowsDeleted;

        final int match = sUriMatcher.match(uri);
        switch (match) {
            case INVENTORY:
                // Delete all rows that match the selection and selection args
                rowsDeleted = database.delete(InventoryContract.InventoryEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case INVENTORY_ITEM:
                // Delete a single row given by the ID in the URI
                selection = InventoryContract.InventoryEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                rowsDeleted = database.delete(InventoryContract.InventoryEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Deletion is not supported for " + uri);
        }

        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        // If 1 or more rows were deleted, then notify all listeners that the data at the
        // given URI has changed

        // Return the number of rows deleted
        return rowsDeleted;
    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String selection,
                      String[] selectionArgs) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case INVENTORY:
                return updateItem(uri, contentValues, selection, selectionArgs);
            case INVENTORY_ITEM:

                selection = InventoryContract.InventoryEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                return updateItem(uri, contentValues, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);
        }
    }

    private int updateItem(Uri uri, ContentValues values, String selection, String[] selectionArgs) {

        if (values.containsKey(InventoryContract.InventoryEntry.NAME_COLUMN)) {
            String name = values.getAsString(InventoryContract.InventoryEntry.NAME_COLUMN);
            if (name.length() == 0) {
                Toast.makeText(getContext(), "Name field can't be empty", Toast.LENGTH_SHORT).show();
                return 0;
            }
        }

        if (values.containsKey(InventoryContract.InventoryEntry.PRICE_COLUMN)) {
            Integer price = values.getAsInteger(InventoryContract.InventoryEntry.PRICE_COLUMN);
            if (price != null && price <= 0) {
                Toast.makeText(getContext(), "Price invalid", Toast.LENGTH_SHORT).show();
                return 0;
            }
        }

        if (values.containsKey(InventoryContract.InventoryEntry.STOCK_COLUMN)) {
            Integer stock = values.getAsInteger(InventoryContract.InventoryEntry.STOCK_COLUMN);
            if (stock != null && stock < 0) {
                Toast.makeText(getContext(), "No more item left", Toast.LENGTH_SHORT).show();
                return 0;
            }
        }

        if (values.size() == 0) {
            return 0;
        }

        SQLiteDatabase database = mDbHelper.getWritableDatabase();
        int rowsUpdated = database.update(InventoryContract.InventoryEntry.TABLE_NAME, values, selection, selectionArgs);

        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsUpdated;
    }

}
