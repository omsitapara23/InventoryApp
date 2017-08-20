package com.android.example.inventoryapp.data;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by root on 18/8/17.
 */

public class InventoryContract {
    private InventoryContract() {
    }

    public static final String CONTENT_AUTHORITY = "com.example.android.inventoryapp";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    public static final String PATH_INVENTORY = "inventory";

    public static final class InventoryEntry implements BaseColumns {

        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_INVENTORY);

        public static final String TABLE_NAME = "inventory";
        public static final String _ID = BaseColumns._ID;
        public static final String NAME_COLUMN = "Name";
        public static final String TYPE_COLUMN = "Type";
        public static final String PRICE_COLUMN = "Price";
        public static final String STOCK_COLUMN = "Stock";
    }
}
