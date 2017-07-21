package com.example.android.inventory.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.android.inventory.data.InventoryContract.InventoryEntry;

/**
 * Created by jesus on 9/06/17.
 */

public class InventoryDbHelper extends SQLiteOpenHelper{

    public static final String LOG_TAG = InventoryDbHelper.class.getSimpleName();


    /**
     * Name of the database file
     */
    public static final String DATABASE_NAME = "inventory.db";

    /**
     * Database version. If you change the database schema, you must increment the database version.
     */
    private static final int DATABASE_VERSION = 1;

    /**
     * Constructs a new instance of {@link InventoryDbHelper}.
     *
     * @param context of the app
     */
    public InventoryDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**
     * +     * This is called when the database is created for the first time.
     * +
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create a String that contains the SQL statement to create the inventory tracker table
        String SQL_CREATE_INVENTORY_TABLE = "CREATE TABLE " + InventoryEntry.TABLE_NAME + " ("+
                InventoryEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                InventoryEntry.PRODUCT_NAME + " TEXT NOT NULL," +
                InventoryEntry.COLUMN_PRICE_PER_UNIT + " DOUBLE DEFAULT 0, " +
                InventoryEntry.COLUMN_QUANTITY + " INTEGER DEFAULT 0, " +
                InventoryEntry.COLUMN_FEATURES + " TEXT);";

        Log.v(LOG_TAG, SQL_CREATE_INVENTORY_TABLE);

        // Execute the SQL statement
        db.execSQL(SQL_CREATE_INVENTORY_TABLE);
    }

    /**
     * This is called when the database needs to be upgraded.
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // The database is still at version 1, so there's nothing to do be done here.
    }

}