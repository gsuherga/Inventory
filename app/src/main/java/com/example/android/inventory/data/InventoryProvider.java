package com.example.android.inventory.data;

/**
 * Created by jesus on 14/06/17.
 */

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

/**
 * {@link ContentProvider} for INVENTORY app.
 */
public class InventoryProvider extends ContentProvider {

    private InventoryDbHelper mDInventoryDbHelper;

    /**
     * Tag for the log messages
     */
    public static final String LOG_TAG = InventoryProvider.class.getSimpleName();

    /**
     * URI matcher code for the content URI for the inventory table
     */
    private static final int INVENTORY = 100;

    /**
     * URI matcher code for the content URI for a single item in the inventory table
     */
    private static final int INVENTORY_ID = 101;

    /**
     * UriMatcher object to match a content URI to a corresponding code.
     * The input passed into the constructor represents the code to return for the root URI.
     * It's common to use NO_MATCH as the input for this case.
     */
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    // Static initializer. This is run the first time anything is called from this class.
    static {
        // The calls to addURI() go here, for all of the content URI patterns that the provider
        // should recognize. All paths added to the UriMatcher have a corresponding code to return
        // when a match is found.

        // TODO: Add 2 content URIs to URI matcher.Done with:

        sUriMatcher.addURI(InventoryContract.CONTENT_AUTHORITY, InventoryContract.PATH_INVENTORY, INVENTORY);
        sUriMatcher.addURI(InventoryContract.CONTENT_AUTHORITY, InventoryContract.PATH_INVENTORY + "/#", INVENTORY_ID);
    }

    /**
     * Initialize the provider and the database helper object.
     */
    @Override
    public boolean onCreate() {
        // TODO: Create and initialize a InventoryDbHelper object to gain access to the inventory database.
        // Make sure the variable is a global variable, so it can be referenced from other
        // ContentProvider methods.
        //Done with the following line:

        mDInventoryDbHelper = new InventoryDbHelper(getContext());

        return true;
    }

    /**
     * Perform the query for the given URI. Use the given projection, selection, selection arguments, and sort order.
     */
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {

        // Get readable database
        SQLiteDatabase database = mDInventoryDbHelper.getReadableDatabase();

        // This cursor will hold the result of the query
        Cursor cursor;

        // Figure out if the URI matcher can match the URI to a specific code
        int match = sUriMatcher.match(uri);
        switch (match) {
            case INVENTORY:
                // For the INVENTORY code, query the inventory table directly with the given
                // projection, selection, selection arguments, and sort order. The cursor
                // could contain multiple rows of the inventory table.
                // TODO: Perform database query on inventory table. Done!

                cursor = database.query(InventoryContract.InventoryEntry.TABLE_NAME,
                        projection,
                        null,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            case INVENTORY_ID:
                // For the INVENTORY_ID code, extract out the ID from the URI.
                // For an example URI such as "content://com.example.android.inventory/inventory/3",
                // the selection will be "_id=?" and the selection argument will be a
                // String array containing the actual ID of 3 in this case.
                //
                // For every "?" in the selection, we need to have an element in the selection
                // arguments that will fill in the "?". Since we have 1 question mark in the
                // selection, we have 1 String in the selection arguments' String array.

                selection = InventoryContract.InventoryEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};

                // This will perform a query on the inventory table where the _id equals 3 to return a
                // Cursor containing that row of the table.
                cursor = database.query(InventoryContract.InventoryEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Cannot query unknown URI " + uri);
        }
        // To send the notification about changes in the cursor
        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        return cursor;
    }

    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case INVENTORY:
                return insertInventoryData(uri, contentValues);
            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }
    }

    /**
     * Insert a item inventory into the database with the given content values. Return the new content URI
     * for that specific row in the database.
     */
    private Uri insertInventoryData(Uri uri, ContentValues values) {

        // Check that the productName is not null
        String productName = values.getAsString(InventoryContract.InventoryEntry.PRODUCT_NAME);
        if (productName == null) {
            throw new IllegalArgumentException("Requires a name for the product");
        }

        // If the pricePerUnit is provided. It could be zero as it could be something for free as a gift for clients.
        Double pricePerUnit = values.getAsDouble(InventoryContract.InventoryEntry.COLUMN_PRICE_PER_UNIT);

        if (pricePerUnit != null  && pricePerUnit < 0) {
            throw new IllegalArgumentException("Please introduce the price per unit of the product");
        }

        // If the productQuantity is provided, check that it's greater than 0.
        Integer productQuantity = values.getAsInteger(InventoryContract.InventoryEntry.COLUMN_QUANTITY);

        if (productQuantity != null && productQuantity < 0) {
            throw new IllegalArgumentException("Please introduce how much productFeatures you burnt");
        }

        // If the productFeatures is provided, get it as a String
        String productFeatures = values.getAsString(InventoryContract.InventoryEntry.COLUMN_FEATURES);

        // Get writeable database
        SQLiteDatabase database = mDInventoryDbHelper.getWritableDatabase();

        // Insert the new inventory with the given values
        long id = database.insert(InventoryContract.InventoryEntry.TABLE_NAME, null, values);
        // If the ID is -1, then the insertion failed. Log an error and return null.
        if (id == -1) {
            Log.e(LOG_TAG, "Failed to insert row for " + uri);
            return null;
        }

        //To notify that the data has change for the inventory content in the URI
        getContext().getContentResolver().notifyChange(uri, null);

        // Once we know the ID of the new row in the table,
        // return the new URI with the ID appended to the end of it
        return ContentUris.withAppendedId(uri, id);
    }

    /**
     * Updates the data at the given selection and selection arguments, with the new ContentValues.
     */
    @Override
    public int update(Uri uri, ContentValues contentValues, String selection,
                      String[] selectionArgs) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case INVENTORY:
                return updateInventory(uri, contentValues, selection, selectionArgs);
            case INVENTORY_ID:
                // For the INVENTORY_ID code, extract out the ID from the URI,
                // so we know which row to update. Selection will be "_id=?" and selection
                // arguments will be a String array containing the actual ID.
                selection = InventoryContract.InventoryEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                return updateInventory(uri, contentValues, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);
        }
    }

    /**
     * Update inventory table in the database with the given content values. Apply the changes to the rows
     * specified in the selection and selection arguments (which could be 0 or 1 or more item in the inventory).
     * Return the number of rows that were successfully updated.
     */
    private int updateInventory(Uri uri, ContentValues values, String selection, String[] selectionArgs) {

        // If the {@link InventoryEntry#PRODUCT_NAME} key is present,
        // check that the name value is not null.
        if (values.containsKey(InventoryContract.InventoryEntry.PRODUCT_NAME)) {
            String name = values.getAsString(InventoryContract.InventoryEntry.PRODUCT_NAME);
            if (name == null) {
                throw new IllegalArgumentException("Requires a name for the product");
            }
        }

        // If the {@link InventoryEntry#COLUMN_PRICE_PER_UNIT} key is present,
        if (values.containsKey(InventoryContract.InventoryEntry.COLUMN_PRICE_PER_UNIT)) {
            // Check price.
            Integer productPrice = values.getAsInteger(InventoryContract.InventoryEntry.COLUMN_PRICE_PER_UNIT);
            if (productPrice != null && productPrice < 0) {
                throw new IllegalArgumentException("Please introduce how much the product costs");
            }
        }

        // If the {@link InventoryEntry#COLUMN_} key is present,
        if (values.containsKey(InventoryContract.InventoryEntry.COLUMN_QUANTITY)) {
            // Check quantity
            Integer quantity = values.getAsInteger(InventoryContract.InventoryEntry.COLUMN_QUANTITY);
            if (quantity != null && quantity < 0) {
                throw new IllegalArgumentException("Please introduce how many items of the product you have");
            }
        }

        // If the {@link InventoryEntry#COLUMN_FEATURES} key is present,
        // check that the weight value is valid.
        if (values.containsKey(InventoryContract.InventoryEntry.COLUMN_FEATURES)) {
            // Check that features has a valid value
            String productFeatures = values.getAsString(InventoryContract.InventoryEntry.COLUMN_FEATURES);
        }

        // No need to check the breed, any value is valid (including null).

        // If there are no values to update, then don't try to update the database
        if (values.size() == 0) {
            return 0;
        }
        // Otherwise, get writeable database to update the data
        SQLiteDatabase database = mDInventoryDbHelper.getWritableDatabase();

        // Returns the number of database rows affected by the update statement
        //return database.update(InventoryContract.InventoryEntry.TABLE_NAME, values, selection, selectionArgs);

        // Perform the update on the database and get the number of rows affected
        int rowsUpdated = database.update(InventoryContract.InventoryEntry.TABLE_NAME, values, selection, selectionArgs);

        // If 1 or more rows were updated, then notify all listeners that the data at the
        // given URI has changed
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        // Return the number of rows updated
        return rowsUpdated;
    }

    /**
     * Delete the data at the given selection and selection arguments.
     */
    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // Get writeable database
        SQLiteDatabase database = mDInventoryDbHelper.getWritableDatabase();

        // Track the number of rows that were deleted
        int rowsDeleted;

        final int match = sUriMatcher.match(uri);
        switch (match) {
            case INVENTORY:
                // Delete all rows that match the selection and selection args
                //   return database.delete(InventoryContract.InventoryEntry.TABLE_NAME, selection, selectionArgs);
                rowsDeleted = database.delete(InventoryContract.InventoryEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case INVENTORY_ID:
                // Delete a single row given by the ID in the URI
                selection = InventoryContract.InventoryEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                // return database.delete(InventoryContract.InventoryEntry.TABLE_NAME, selection, selectionArgs);

               // return database.delete(InventoryContract.InventoryEntry.TABLE_NAME, selection, selectionArgs);
            rowsDeleted = database.delete(InventoryContract.InventoryEntry.TABLE_NAME, selection, selectionArgs);
            break;
            default:
                throw new IllegalArgumentException("Deletion is not supported for " + uri);
        }

        // If 1 or more rows were deleted, then notify all listeners that the data at the
        // given URI has changed
        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        // Return the number of rows deleted
        return rowsDeleted;
    }

    /**
     * Returns the MIME type of data for the content URI.
     */
    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case INVENTORY:
                return InventoryContract.InventoryEntry.CONTENT_LIST_TYPE;
            case INVENTORY_ID:
                return InventoryContract.InventoryEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalStateException("Unknown URI " + uri + " with match " + match);
        }
    }
}