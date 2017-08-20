package com.android.example.inventoryapp;

import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.example.inventoryapp.data.InventoryContract;
import com.android.example.inventoryapp.data.InventoryDbHelper;

import static com.android.example.inventoryapp.R.id.price;

public class EditorActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private EditText nameEditText;
    private EditText typeEditText;
    private EditText priceEditText;
    private EditText stockEditText;
    private EditText reorderEditText;
    private Button sellButton;
    private Button reorderButton;
    private InventoryDbHelper inventoryDbHelper;
    private static final int EXIXTING_ITEM_LOADER = 0;
    private Uri mCurrentItemUri;
    private boolean mItemHasChanged = false;

    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mItemHasChanged = true;
            return false;
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        sellButton = (Button) findViewById(R.id.sellButton);
        reorderButton = (Button) findViewById(R.id.reorderButton);
        nameEditText = (EditText) findViewById(R.id.name);
        typeEditText = (EditText) findViewById(R.id.type);
        priceEditText = (EditText) findViewById(price);
        stockEditText = (EditText) findViewById(R.id.stockText);
        reorderEditText = (EditText) findViewById(R.id.reorderEditText);
        inventoryDbHelper = new InventoryDbHelper(this);
        nameEditText.setOnTouchListener(mTouchListener);
        typeEditText.setOnTouchListener(mTouchListener);
        priceEditText.setOnTouchListener(mTouchListener);
        stockEditText.setOnTouchListener(mTouchListener);


        Intent intent = getIntent();
        mCurrentItemUri = intent.getData();

        if (mCurrentItemUri == null) {
            setTitle(getString(R.string.add_item));
            reorderButton.setVisibility(View.GONE);
            reorderEditText.setVisibility(View.GONE);
            sellButton.setVisibility(View.GONE);

        } else {
            setTitle(getString(R.string.edit_item));
            getLoaderManager().initLoader(EXIXTING_ITEM_LOADER, null, this);
        }

        sellButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                int stock;
                stock = Integer.parseInt(getText(stockEditText));
                stock = stock - 1;
                if (stock >= 0) {
                    stockEditText.setText(Integer.toString(stock));
                    insertInventoryData();
                } else {
                    Toast.makeText(EditorActivity.this, "No more item left", Toast.LENGTH_SHORT).show();
                }
            }
        });

        reorderButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int reorder;
                int stock;
                if (getText(reorderEditText).length() != 0) {
                    reorder = Integer.parseInt(getText(reorderEditText));
                    stock = Integer.parseInt(getText(stockEditText));
                    if (reorder == 0) {
                        Toast.makeText(EditorActivity.this, "Reorder cannot be 0", Toast.LENGTH_SHORT).show();
                    } else {
                        stock = stock + reorder;
                        stockEditText.setText(Integer.toString(stock));
                        insertInventoryData();
                        String orderSummary = "Want " + getText(nameEditText) + "\n" + "Amount:" + reorder;
                        Intent intent = new Intent(Intent.ACTION_SENDTO);
                        intent.setData(Uri.parse("mailto:retailshopper@gmail.com"));
                        intent.putExtra(Intent.EXTRA_SUBJECT, getText(nameEditText) + " Order from Om retailers");
                        intent.putExtra(Intent.EXTRA_TEXT, orderSummary);
                        if (intent.resolveActivity(getPackageManager()) != null) {
                            startActivity(intent);
                        }
                    }
                } else {
                    Toast.makeText(EditorActivity.this, "Enter valid reorder", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        // If this is a new pet, hide the "Delete" menu item.
        if (mCurrentItemUri == null) {
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            // Respond to a click on the "Save" menu option
            case R.id.action_save:
                insertInventoryData();
                finish();

                return true;
            // Respond to a click on the "Delete" menu option
            case R.id.action_delete:
                showDeleteConfirmationDialog();
                return true;

            case android.R.id.home:
                // If the pet hasn't changed, continue with navigating up to parent activity
                // which is the {@link CatalogActivity}.
                if (!mItemHasChanged) {
                    NavUtils.navigateUpFromSameTask(EditorActivity.this);
                    return true;
                }

                // Otherwise if there are unsaved changes, setup a dialog to warn the user.
                // Create a click listener to handle the user confirming that
                // changes should be discarded.
                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // User clicked "Discard" button, navigate to parent activity.
                                NavUtils.navigateUpFromSameTask(EditorActivity.this);
                            }
                        };

                // Show a dialog that notifies the user they have unsaved changes
                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private static String getText(EditText editText) {
        String text = editText.getText().toString().trim();
        return text;
    }

    private void insertInventoryData() {
        SQLiteDatabase db = inventoryDbHelper.getWritableDatabase();
        String nameText = getText(nameEditText);
        String typeText = getText(typeEditText);
        String priceText = getText(priceEditText);
        String stockText = getText(stockEditText);


        if (mCurrentItemUri == null && TextUtils.isEmpty(nameText) && TextUtils.isEmpty(typeText) && TextUtils.isEmpty(priceText) && TextUtils.isEmpty(stockText)) {
            return;
        }
        int price = 0;
        if (!TextUtils.isEmpty(priceText)) {
            price = Integer.parseInt(priceText);
        }
        int stock = 0;
        if (!TextUtils.isEmpty(stockText)) {
            stock = Integer.parseInt(stockText);
        }

        ContentValues values = new ContentValues();
        values.put(InventoryContract.InventoryEntry.NAME_COLUMN, nameText);
        values.put(InventoryContract.InventoryEntry.TYPE_COLUMN, typeText);
        values.put(InventoryContract.InventoryEntry.PRICE_COLUMN, price);
        values.put(InventoryContract.InventoryEntry.STOCK_COLUMN, stock);

        if (mCurrentItemUri == null) {
            Uri newUri = getContentResolver().insert(InventoryContract.InventoryEntry.CONTENT_URI, values);

            // Show a toast message depending on whether or not the insertion was successful.
            if (newUri == null) {
                // If the new content URI is null, then there was an error with insertion.
                Toast.makeText(this, "Error inserting Item",
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the insertion was successful and we can display a toast.
                Toast.makeText(this, "Item successfully inserted",
                        Toast.LENGTH_SHORT).show();
            }
        } else {
            int rowsAffected = getContentResolver().update(mCurrentItemUri, values, null, null);
            if (rowsAffected == 0) {
                Toast.makeText(this, getString(R.string.error_update), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(R.string.success_update), Toast.LENGTH_SHORT).show();
            }
        }
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
                mCurrentItemUri,
                projection,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {

        if (cursor == null || cursor.getCount() < 1) {
            return;
        }

        if (cursor.moveToFirst()) {
            int nameColumnIndex = cursor.getColumnIndex(InventoryContract.InventoryEntry.NAME_COLUMN);
            int typeColumnIndex = cursor.getColumnIndex(InventoryContract.InventoryEntry.TYPE_COLUMN);
            int priceColumnIndex = cursor.getColumnIndex(InventoryContract.InventoryEntry.PRICE_COLUMN);
            int stockColumnIndex = cursor.getColumnIndex(InventoryContract.InventoryEntry.STOCK_COLUMN);

            String name = cursor.getString(nameColumnIndex);
            String type = cursor.getString(typeColumnIndex);
            int price = cursor.getInt(priceColumnIndex);
            int stock = cursor.getInt(stockColumnIndex);

            nameEditText.setText(name);
            typeEditText.setText(type);
            priceEditText.setText(Integer.toString(price));
            stockEditText.setText(Integer.toString(stock));
        }

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        nameEditText.setText("");
        typeEditText.setText("");
        priceEditText.setText("");
        stockEditText.setText("");

    }

    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    public void onBackPressed() {
        // If the pet hasn't changed, continue with handling back button press
        if (!mItemHasChanged) {
            super.onBackPressed();
            return;
        }

        // Otherwise if there are unsaved changes, setup a dialog to warn the user.
        // Create a click listener to handle the user confirming that changes should be discarded.
        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // User clicked "Discard" button, close the current activity.
                        finish();
                    }
                };

        // Show dialog that there are unsaved changes
        showUnsavedChangesDialog(discardButtonClickListener);
    }

    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the postivie and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                deleteItem();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }


    private void deleteItem() {

        if (mCurrentItemUri != null) {

            int rowsDeleted = getContentResolver().delete(mCurrentItemUri, null, null);

            // Show a toast message depending on whether or not the delete was successful.
            if (rowsDeleted == 0) {
                // If no rows were deleted, then there was an error with the delete.
                Toast.makeText(this, getString(R.string.editor_delete_pet_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the delete was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_delete_pet_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }

        // Close the activity
        finish();
    }
}



