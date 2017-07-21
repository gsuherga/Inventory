package com.example.android.inventory;

/**
 * Created by jesus on 17/06/17.
 */

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.example.android.inventory.data.InventoryContract;

import java.util.Locale;

import static android.R.attr.id;

/**
 * {@link InventoryCursorAdapter} is an adapter for a list or grid view
 * that uses a {@link Cursor} of item data as its data source. This adapter knows
 * how to create list items for each row of item data in the {@link Cursor}.
 */
public class InventoryCursorAdapter extends CursorAdapter {

    EditorActivity currentItemUri;

    EditorActivity currentItem;

    View mView;

   /*
   Units of a item to sell
    */

    Integer quantityToSell = 0;

    /**
     * Constructs a new {@link InventoryCursorAdapter}.
     *
     * @param context The context
     * @param c       The cursor from which to get the data.
     */
    public InventoryCursorAdapter(Context context, Cursor c) {
        super(context, c, 0 /* flags */);
    }

    /**
     * Makes a new blank list item view. No data is set (or bound) to the views yet.
     *
     * @param context app context
     * @param cursor  The cursor from which to get the data. The cursor is already
     *                moved to the correct position.
     * @param parent  The parent to which the new view is attached to
     * @return the newly created list item view.
     */
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        // Inflate a list item view using the layout specified in list_item.xml
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }


    /**
     * This method binds the item data (in the current row pointed to by cursor) to the given
     * list item layout. For example, the name for the current item can be set on the name TextView
     * in the list item layout.
     *
     * @param view    Existing view, returned earlier by newView() method
     * @param context app context
     * @param cursor  The cursor from which to get the data. The cursor is already moved to the
     *                correct row.
     */
    @Override
    public void bindView(View view, Context context, final Cursor cursor) {

        mView = view;
        // Find individual views that we want to modify in the list item layout
        TextView nameTextView = (TextView) view.findViewById(R.id.user_product_name_to_show);
        TextView pricePerUnitTextView = (TextView) view.findViewById(R.id.price_per_unit_to_show);
        final TextView quantityTextView = (TextView) view.findViewById(R.id.quantity_to_show);
        TextView featureTextView = (TextView) view.findViewById(R.id.features_to_show);

        // Find the columns of item attributes that we're interested in
        final int IndexColum = cursor.getColumnIndex(InventoryContract.InventoryEntry._ID);
        int nameColumnIndex = cursor.getColumnIndex(InventoryContract.InventoryEntry.PRODUCT_NAME);
        int pricePerUnitColumnIndex = cursor.getColumnIndex(InventoryContract.InventoryEntry.COLUMN_PRICE_PER_UNIT);
        final int quantityColumnIndex = cursor.getColumnIndex(InventoryContract.InventoryEntry.COLUMN_QUANTITY);
        int featuresColumnIndex = cursor.getColumnIndex(InventoryContract.InventoryEntry.COLUMN_FEATURES);


        // Read the item attributes from the Cursor for the current item
        final String productsName = cursor.getString(nameColumnIndex);
        final Double price = cursor.getDouble(pricePerUnitColumnIndex);
        String features = cursor.getString(featuresColumnIndex);
        final int quantity = cursor.getInt(quantityColumnIndex);
        final String unit = view.getContext().getString(R.string.unit_quantity);

        // Update the TextViews with the attributes for the current item
        nameTextView.setText(productsName);
        pricePerUnitTextView.setText(String.format(Locale.ENGLISH, " " + "%.2f", price) + " " + " €/U");
        quantityTextView.setText((Integer.toString(quantity)) + " " + unit);
        featureTextView.setText(features);

        //If we are selling some items, say how many, the price and update the quantity

        Button decreaseQuantity = (Button) view.findViewById(R.id.one_less_to_sell);
        Button increaseQuantity = (Button) view.findViewById(R.id.one_more_to_sell);
        final TextView totalPriceTextView = (TextView) view.findViewById(R.id.total_price);
        final TextView quantityToSellTextView = (TextView) view.findViewById(R.id.quantity_to_sell);

        quantityToSellTextView.setText(Integer.toString(quantityToSell));
        totalPriceTextView.setText(Double.toString(quantityToSell * price) + " €");

        decreaseQuantity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (quantityToSell > 0) {
                    quantityToSell = quantityToSell - 1;
                    quantityToSellTextView.setText(Integer.toString(quantityToSell));
                    Double totalprice = quantityToSell * price;

                    totalPriceTextView.setText(String.format(Locale.ENGLISH, " " + "%.2f", totalprice) + " " + " €");

                    ContentValues values = new ContentValues();

                    values.put(InventoryContract.InventoryEntry.COLUMN_QUANTITY, Integer.toString(quantity + 1));

                    Uri currentItemInventory = ContentUris.withAppendedId(InventoryContract.InventoryEntry.CONTENT_URI, id);

                    mView.getContext().getContentResolver().update(currentItemInventory,values, null, null);

                    quantityTextView.setText((Integer.toString(cursor.getInt(quantityColumnIndex))) + " " + unit);
                }

            }
        });

        increaseQuantity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (quantityToSell < quantity) {
                    quantityToSell = quantityToSell + 1;
                    quantityToSellTextView.setText(Integer.toString(quantityToSell));
                    Double totalprice = quantityToSell * price;
                    totalPriceTextView.setText(String.format(Locale.ENGLISH, " " + "%.2f", totalprice) + " " + " €");

                    ContentValues values = new ContentValues();

                    cursor.moveToFirst();

                    values.put(InventoryContract.InventoryEntry.COLUMN_QUANTITY, Integer.toString(quantity - quantityToSell));

                    Uri currentItemInventory = ContentUris.withAppendedId(InventoryContract.InventoryEntry.CONTENT_URI, id);

                    mView.getContext().getContentResolver().update(currentItemInventory,values, null, null);

                    quantityTextView.setText((Integer.toString(cursor.getInt(quantityColumnIndex))) + " " + unit);

                }
            }
        });

        Button sendOrderButton = (Button) view.findViewById(R.id.sell);
        sendOrderButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (quantityToSell > 0) {

                    //Send the information about the order by email
                    Intent sendOrder = new Intent(Intent.ACTION_SENDTO);
                    sendOrder.setData(Uri.parse("mailto:")); // only email apps should handle this
                    sendOrder.putExtra(Intent.EXTRA_TEXT, quantityToSell + " " + productsName + " " + " = " +
                            totalPriceTextView.getText());
                    sendOrder.putExtra(Intent.EXTRA_SUBJECT, v.getContext().getString(R.string.your_order));

                    if (sendOrder.resolveActivity(v.getContext().getPackageManager()) != null) {
                        v.getContext().startActivity(sendOrder);
                    }
                }
            }
        });

    }

}