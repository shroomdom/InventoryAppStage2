package com.example.android.inventoryappstage2;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.example.android.inventoryappstage2.data.BookContract.BookEntry;

/**
     * Allows user to create a new book or edit an existing one.
     */
    public class EditorActivity extends AppCompatActivity implements
            LoaderManager.LoaderCallbacks<Cursor> {

        /** Identifier for the book data loader */
        private static final int EXISTING_BOOK_LOADER = 0;

        /** Content URI for the existing book (null if it's a new book) */
        private Uri mCurrentBookUri;

        /** EditText field to enter the book name */
        private EditText mbookNameEditText;

        /** EditText field to enter the book price */
        private EditText mbookPriceEditText;

        /** EditText field to enter the book quantity */
        private EditText mbookQuantityEditText;

    /** EditText field to enter the book supplier name */
    private EditText mbookSupplierNameEditText;

    /** EditText field to enter the book supplier contact */
    private EditText mbookSupplierContactEditText;
        

        /** Boolean flag that keeps track of whether the book has been edited (true) or not (false) */
        private boolean mBookHasChanged = false;

        /**
         * OnTouchListener that listens for any user touches on a View, implying that they are modifying
         * the view, and we change the mBookHasChanged boolean to true.
         */
        private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                mBookHasChanged = true;
                return false;
            }
        };

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_editor);

            // Examine the intent that was used to launch this activity,
            // in order to figure out if we're creating a new book or editing an existing one.
            Intent intent = getIntent();
            mCurrentBookUri = intent.getData();

            // If the intent DOES NOT contain a book content URI, then we know that we are
            // creating a new book.
            if (mCurrentBookUri == null) {
                // This is a new book, so change the app bar to say "Add a book"
                setTitle(getString(R.string.editor_activity_title_new_book));

                // Invalidate the options menu, so the "Delete" menu option can be hidden.
                // (It doesn't make sense to delete a book that hasn't been created yet.)
                invalidateOptionsMenu();
            } else {
                // Otherwise this is an existing book, so change app bar to say "Edit book"
                setTitle(getString(R.string.editor_activity_title_edit_book));

                // Initialize a loader to read the book data from the database
                // and display the current values in the editor
                getLoaderManager().initLoader(EXISTING_BOOK_LOADER, null, this);
            }

            // Find all relevant views that we will need to read user input from
            mbookNameEditText = (EditText) findViewById(R.id.edit_book_name);
            mbookPriceEditText = (EditText) findViewById(R.id.edit_book_price);
            mbookQuantityEditText = (EditText) findViewById(R.id.edit_book_quantity);
            mbookSupplierNameEditText = (EditText) findViewById(R.id.edit_book_supplier_name);
            mbookSupplierContactEditText = (EditText) findViewById(R.id.edit_book_supplier_contact);

            // Setup OnTouchListeners on all the input fields, so we can determine if the user
            // has touched or modified them. This will let us know if there are unsaved changes
            // or not, if the user tries to leave the editor without saving.
            mbookNameEditText.setOnTouchListener(mTouchListener);
            mbookPriceEditText.setOnTouchListener(mTouchListener);
            mbookQuantityEditText.setOnTouchListener(mTouchListener);
            mbookSupplierNameEditText.setOnTouchListener(mTouchListener);
            mbookSupplierContactEditText.setOnTouchListener(mTouchListener);
        }

        /**
         * Get user input from editor and save book into database.
         */
        private void savebook() {
            // Read from input fields
            // Use trim to eliminate leading or trailing white space
            String bookNameString = mbookNameEditText.getText().toString().trim();
            String bookPriceString = mbookPriceEditText.getText().toString().trim();
            String bookQuantityString = mbookQuantityEditText.getText().toString().trim();
            String bookSupplierNameString = mbookSupplierNameEditText.getText().toString().trim();
            String bookSupplierContactString = mbookSupplierContactEditText.getText().toString().trim();

            boolean hasError = false;

            // Check if this is supposed to be a new book
            // and check if all the fields in the editor are blank
            if (mCurrentBookUri == null &&
                    TextUtils.isEmpty(bookNameString) && TextUtils.isEmpty(bookPriceString) &&
                    TextUtils.isEmpty(bookQuantityString) && TextUtils.isEmpty(bookSupplierNameString)
                    && TextUtils.isEmpty(bookSupplierContactString)) {
                // Since no fields were modified, we can return early without creating a new book.
                // No need to create ContentValues and no need to do any ContentProvider operations.
                return;
            }

            // Create a ContentValues object where column names are the keys,
            // and book attributes from the editor are the values.
            ContentValues values = new ContentValues();
            values.put(BookEntry.COLUMN_PRICE, bookPriceString);
            values.put(BookEntry.COLUMN_QUANTITY, bookQuantityString);

            if (!TextUtils.isEmpty(bookNameString)) {
                values.put(BookEntry.COLUMN_PRODUCT_NAME, bookNameString);
            }else {
                Toast.makeText(this, getString(R.string.editor_insert_name_empty),
                        Toast.LENGTH_SHORT).show();
                return;
            }

            // If the weight is not provided by the user, don't try to parse the string into an
            // integer value. Use 0 by default.
            int bookPrice = 0;
            if (!TextUtils.isEmpty(bookPriceString)) {
                System.out.println("not empty");
                bookPrice = Integer.parseInt(bookPriceString);
                values.put(BookEntry.COLUMN_PRICE, bookPrice);
            }else {
                System.out.println("book price is empty");
                Toast.makeText(this, getString(R.string.editor_insert_price_empty),
                        Toast.LENGTH_SHORT).show();
                return;
            }

            int bookQuantity = 0;
            if (!TextUtils.isEmpty(bookQuantityString)) {
                bookQuantity = Integer.parseInt(bookQuantityString);
                values.put(BookEntry.COLUMN_QUANTITY, bookQuantity);
            }else {
                Toast.makeText(this, getString(R.string.editor_insert_quantity_empty),
                        Toast.LENGTH_SHORT).show();
                return;
            }

            if (!TextUtils.isEmpty(bookSupplierNameString)) {
                values.put(BookEntry.COLUMN_SUPPLIER_NAME, bookSupplierNameString);
            }else {
                Toast.makeText(this, getString(R.string.editor_insert_supplier_name_empty),
                        Toast.LENGTH_SHORT).show();
                return;
            }

            if (!TextUtils.isEmpty(bookSupplierContactString)) {
                values.put(BookEntry.COLUMN_SUPPLIER_PHONE_NUMBER, bookSupplierContactString);
            }else {
                Toast.makeText(this, getString(R.string.editor_insert_supplier_contact_empty),
                        Toast.LENGTH_SHORT).show();
                return;
            }

            // Determine if this is a new or existing book by checking if mCurrentBookUri is null or not
            if (mCurrentBookUri == null) {
                // This is a NEW book, so insert a new book into the provider,
                // returning the content URI for the new book.
                Uri newUri = getContentResolver().insert(BookEntry.CONTENT_URI, values);

                // Show a toast message depending on whether or not the insertion was successful.
                if (newUri == null) {
                    // If the new content URI is null, then there was an error with insertion.
                    Toast.makeText(this, getString(R.string.editor_insert_book_failed),
                            Toast.LENGTH_SHORT).show();
                } else {
                    // Otherwise, the insertion was successful and we can display a toast.
                    Toast.makeText(this, getString(R.string.editor_insert_book_successful),
                            Toast.LENGTH_SHORT).show();
                }
            } else {
                // Otherwise this is an EXISTING book, so update the book with content URI: mCurrentBookUri
                // and pass in the new ContentValues. Pass in null for the selection and selection args
                // because mCurrentBookUri will already identify the correct row in the database that
                // we want to modify.
                int rowsAffected = getContentResolver().update(mCurrentBookUri, values, null, null);

                // Show a toast message depending on whether or not the update was successful.
                if (rowsAffected == 0) {
                    // If no rows were affected, then there was an error with the update.
                    Toast.makeText(this, getString(R.string.editor_update_book_failed),
                            Toast.LENGTH_SHORT).show();
                } else {
                    // Otherwise, the update was successful and we can display a toast.
                    Toast.makeText(this, getString(R.string.editor_update_book_successful),
                            Toast.LENGTH_SHORT).show();
                }
            }
        }

        @Override
        public boolean onCreateOptionsMenu(Menu menu) {
            // Inflate the menu options from the res/menu/menu_editor.xml file.
            // This adds menu items to the app bar.
            getMenuInflater().inflate(R.menu.menu_editor, menu);
            return true;
        }

        /**
         * This method is called after invalidateOptionsMenu(), so that the
         * menu can be updated (some menu items can be hidden or made visible).
         */
        @Override
        public boolean onPrepareOptionsMenu(Menu menu) {
            super.onPrepareOptionsMenu(menu);
            // If this is a new book, hide the "Delete" menu item.
            if (mCurrentBookUri == null) {
                MenuItem menuItem = menu.findItem(R.id.action_delete);
                menuItem.setVisible(false);
            }
            return true;
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            // User clicked on a menu option in the app bar overflow menu
            switch (item.getItemId()) {
                // Respond to a click on the "Save" menu option
                case R.id.action_save:
                    // Save book to database
                    savebook();
                    // Exit activity
                    finish();
                    return true;
                // Respond to a click on the "Delete" menu option
                case R.id.action_delete:
                    // Pop up confirmation dialog for deletion
                    showDeleteConfirmationDialog();
                    return true;
                // Respond to a click on the "Up" arrow button in the app bar
                case android.R.id.home:
                    // If the book hasn't changed, continue with navigating up to parent activity
                    // which is the {@link CatalogActivity}.
                    if (!mBookHasChanged) {
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

        /**
         * This method is called when the back button is pressed.
         */
        @Override
        public void onBackPressed() {
            // If the book hasn't changed, continue with handling back button press
            if (!mBookHasChanged) {
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

        @Override
        public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
            // Since the editor shows all book attributes, define a projection that contains
            // all columns from the book table
            String[] projection = {
                    BookEntry._ID,
                    BookEntry.COLUMN_PRODUCT_NAME,
                    BookEntry.COLUMN_PRICE,
                    BookEntry.COLUMN_QUANTITY,
                    BookEntry.COLUMN_SUPPLIER_NAME,
                    BookEntry.COLUMN_SUPPLIER_PHONE_NUMBER };

            // This loader will execute the ContentProvider's query method on a background thread
            return new CursorLoader(this,   // Parent activity context
                    mCurrentBookUri,         // Query the content URI for the current book
                    projection,             // Columns to include in the resulting Cursor
                    null,                   // No selection clause
                    null,                   // No selection arguments
                    null);                  // Default sort order
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
            // Bail early if the cursor is null or there is less than 1 row in the cursor
            if (cursor == null || cursor.getCount() < 1) {
                return;
            }

            // Proceed with moving to the first row of the cursor and reading data from it
            // (This should be the only row in the cursor)
            if (cursor.moveToFirst()) {

                final int idColumnIndex = cursor.getColumnIndex(BookEntry._ID);

                // Find the columns of book attributes that we're interested in
                int bookNameColumnIndex = cursor.getColumnIndex(BookEntry.COLUMN_PRODUCT_NAME);
                int bookPriceColumnIndex = cursor.getColumnIndex(BookEntry.COLUMN_PRICE);
                int bookQuantityColumnIndex = cursor.getColumnIndex(BookEntry.COLUMN_QUANTITY);
                int bookSupplierNameColumnIndex = cursor.getColumnIndex(BookEntry.COLUMN_SUPPLIER_NAME);
                int bookSupplierContactColumnIndex = cursor.getColumnIndex(BookEntry.COLUMN_SUPPLIER_PHONE_NUMBER);

                // Extract out the value from the Cursor for the given column index
                String bookName = cursor.getString(bookNameColumnIndex);
                int bookPrice = cursor.getInt(bookPriceColumnIndex);
                final int bookQuantity = cursor.getInt(bookQuantityColumnIndex);
                String bookSupplierName = cursor.getString(bookSupplierNameColumnIndex);
                final int bookSupplierContact = cursor.getInt(bookSupplierContactColumnIndex);

                // Update the views on the screen with the values from the database
                mbookNameEditText.setText(bookName);
                mbookPriceEditText.setText(Integer.toString(bookPrice));
                mbookQuantityEditText.setText(Integer.toString(bookQuantity));
                mbookSupplierNameEditText.setText(bookSupplierName);
                mbookSupplierContactEditText.setText(Integer.toString(bookSupplierContact));

                ImageButton mPhone = findViewById(R.id.phoneButton);
                mPhone.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String phone = String.valueOf(bookSupplierContact);
                        Intent intent = new Intent(Intent.ACTION_DIAL,
                                Uri.fromParts("tel", phone, null));
                        startActivity(intent);
                    }
                });

                ImageButton bookQuantityDecreaseButton = findViewById(R.id.edit_quantity_decrease);
                bookQuantityDecreaseButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        decreaseButtonAmount(idColumnIndex, bookQuantity);
                    }
                });

                ImageButton bookQuantityIncreaseButton = findViewById(R.id.edit_quantity_increase);
                bookQuantityIncreaseButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        increaseButtonAmount(idColumnIndex, bookQuantity);
                    }
                });
            }
        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {
            // If the loader is invalidated, clear out all the data from the input fields.
            mbookNameEditText.setText("");
            mbookPriceEditText.setText("");
            mbookQuantityEditText.setText("");
            mbookSupplierNameEditText.setText("");
            mbookSupplierContactEditText.setText("");
        }

    public void decreaseButtonAmount(int bookID, int bookQuantity) {
        bookQuantity -= 1;
        if (bookQuantity >= 0) {
            updateBookAmount(bookQuantity);
            //Toast.makeText(this, getString(R.string.quantity_change_msg), Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, getString(R.string.quantity_no_inventory_message), Toast.LENGTH_SHORT).show();
        }
    }

    public void increaseButtonAmount(int bookID, int bookQuantity) {
        bookQuantity = bookQuantity + 1;
        if (bookQuantity >= 0) {
            updateBookAmount(bookQuantity);
            //Toast.makeText(this, getString(R.string.quantity_change_msg), Toast.LENGTH_SHORT).show();
        }
    }


    private void updateBookAmount(int bookQuantity) {

        if (mCurrentBookUri == null) {
            return;
        }
        ContentValues values = new ContentValues();
        values.put(BookEntry.COLUMN_QUANTITY, bookQuantity);

        /**
        if (mCurrentBookUri == null) {
            Uri newUri = getContentResolver().insert(BookEntry.CONTENT_URI, values);
            if (newUri == null) {
                Toast.makeText(this, getString(R.string.insert_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(R.string.insert_successful),
                        Toast.LENGTH_SHORT).show();
            }
        } else { */
            int rowsAffected = getContentResolver().update(mCurrentBookUri, values, null, null);
            if (rowsAffected == 0) {
                Toast.makeText(this, getString(R.string.update_amount_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(R.string.update_amount_successful),
                        Toast.LENGTH_SHORT).show();
            }
        //}
    }

        /**
         * Show a dialog that warns the user there are unsaved changes that will be lost
         * if they continue leaving the editor.
         *
         * @param discardButtonClickListener is the click listener for what to do when
         *                                   the user confirms they want to discard their changes
         */
        private void showUnsavedChangesDialog(
                DialogInterface.OnClickListener discardButtonClickListener) {
            // Create an AlertDialog.Builder and set the message, and click listeners
            // for the postivie and negative buttons on the dialog.
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(R.string.unsaved_changes_dialog_msg);
            builder.setPositiveButton(R.string.discard, discardButtonClickListener);
            builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    // User clicked the "Keep editing" button, so dismiss the dialog
                    // and continue editing the book.
                    if (dialog != null) {
                        dialog.dismiss();
                    }
                }
            });

            // Create and show the AlertDialog
            AlertDialog alertDialog = builder.create();
            alertDialog.show();
        }

        /**
         * Prompt the user to confirm that they want to delete this book.
         */
        private void showDeleteConfirmationDialog() {
            // Create an AlertDialog.Builder and set the message, and click listeners
            // for the postivie and negative buttons on the dialog.
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(R.string.delete_dialog_msg);
            builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    // User clicked the "Delete" button, so delete the book.
                    deletebook();
                }
            });
            builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    // User clicked the "Cancel" button, so dismiss the dialog
                    // and continue editing the book.
                    if (dialog != null) {
                        dialog.dismiss();
                    }
                }
            });

            // Create and show the AlertDialog
            AlertDialog alertDialog = builder.create();
            alertDialog.show();
        }

        /**
         * Perform the deletion of the book in the database.
         */
        private void deletebook() {
            // Only perform the delete if this is an existing book.
            if (mCurrentBookUri != null) {
                // Call the ContentResolver to delete the book at the given content URI.
                // Pass in null for the selection and selection args because the mCurrentBookUri
                // content URI already identifies the book that we want.
                int rowsDeleted = getContentResolver().delete(mCurrentBookUri, null, null);

                // Show a toast message depending on whether or not the delete was successful.
                if (rowsDeleted == 0) {
                    // If no rows were deleted, then there was an error with the delete.
                    Toast.makeText(this, getString(R.string.editor_delete_book_failed),
                            Toast.LENGTH_SHORT).show();
                } else {
                    // Otherwise, the delete was successful and we can display a toast.
                    Toast.makeText(this, getString(R.string.editor_delete_book_successful),
                            Toast.LENGTH_SHORT).show();
                }
            }

            // Close the activity
            finish();
        }
    }
