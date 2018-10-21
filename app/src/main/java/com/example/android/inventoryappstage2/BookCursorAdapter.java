package com.example.android.inventoryappstage2;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;
import android.widget.Button;
import android.net.Uri;
import android.content.ContentUris;
import android.content.ContentValues;
import android.widget.Toast;

import com.example.android.inventoryappstage2.data.BookContract.BookEntry;

public class BookCursorAdapter extends CursorAdapter {

    public BookCursorAdapter(Context context, Cursor c) {
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
        return LayoutInflater.from(context).inflate(R.layout.book_list, parent, false);
    }

    /**
     * This method binds the book data (in the current row pointed to by cursor) to the given
     * list item layout. For example, the name for the current book can be set on the name TextView
     * in the list item layout.
     *
     * @param view    Existing view, returned earlier by newView() method
     * @param context app context
     * @param cursor  The cursor from which to get the data. The cursor is already moved to the
     *                correct row.
     */
    @Override
    public void bindView(View view, final Context context, Cursor cursor) {
        // Find individual views that we want to modify in the list item layout
        TextView bookNameTextView = (TextView) view.findViewById(R.id.book_name);
        TextView bookPriceTextView = (TextView) view.findViewById(R.id.book_price);
        TextView bookQuantityTextView = (TextView) view.findViewById(R.id.book_quantity);


        // Find the columns of book attributes that we're interested in
        int bookNameColumnIndex = cursor.getColumnIndex(BookEntry.COLUMN_PRODUCT_NAME);
        int bookPriceColumnIndex = cursor.getColumnIndex(BookEntry.COLUMN_PRICE);
        int bookQuantityColumnIndex = cursor.getColumnIndex(BookEntry.COLUMN_QUANTITY);

        // Read the book attributes from the Cursor for the current book
        String bookName = cursor.getString(bookNameColumnIndex);
        String bookPrice = cursor.getString(bookPriceColumnIndex);
        String bookQuantity = cursor.getString(bookQuantityColumnIndex);

        // Update the TextViews with the attributes for the current book
        bookNameTextView.setText(bookName);
        bookPriceTextView.setText(bookPrice);
        bookQuantityTextView.setText(bookQuantity);

        // column number of "_ID"
        int bookIdColumnIndex = cursor.getColumnIndex(BookEntry._ID);

        // Read the book attributes from the Cursor for the current book for "Sale" button
        final long bookIdValue = Integer.parseInt(cursor.getString(bookIdColumnIndex));
        final int currentBookCount = cursor.getInt(bookQuantityColumnIndex);

        /*
         * Each list view item will have a "Sale" button
         * This "Sale" button has OnClickListener which will decrease the product quantity by one at a time.
         * Update is only carried out if quantity is greater than 0(i.e MIMINUM quantity is 0).
         */
        Button saleButton = view.findViewById(R.id.sale_button);
        saleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Uri currentUri = ContentUris.withAppendedId(BookEntry.CONTENT_URI,
                        bookIdValue);

                String updatedQuantity = String.valueOf(currentBookCount - 1);

                if (Integer.parseInt(updatedQuantity) >= 0) {
                    ContentValues values = new ContentValues();
                    values.put(BookEntry.COLUMN_QUANTITY, updatedQuantity);
                    context.getContentResolver().update(currentUri, values, null,
                            null);
                } else {
                    Toast.makeText(context, R.string.quantity_no_inventory_message,
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
