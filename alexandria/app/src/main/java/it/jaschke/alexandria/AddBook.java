package it.jaschke.alexandria;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import it.jaschke.alexandria.data.AlexandriaContract;
import it.jaschke.alexandria.services.BookService;

/**
 * Add a book to the database. Books can be added by scanning a bar code or introducing the ISBN code into the text view
 */
public class AddBook extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    private EditText ean;
    private final int LOADER_ID = 1;
    private View rootView;
    private final String EAN_CONTENT="eanContent";


    public AddBook(){
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if(ean!=null) {
            outState.putString(EAN_CONTENT, ean.getText().toString());
        }
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        String savedEan;
        rootView = inflater.inflate(R.layout.fragment_add_book, container, false);
        ean = (EditText) rootView.findViewById(R.id.ean);

        ean.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                //no need
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //no need
            }

            @Override
            public void afterTextChanged(Editable s) {
                String ean =s.toString();
                //catch isbn10 numbers
                if(ean.length()==10 && !ean.startsWith("978")){
                    ean = generateISBN13(ean);
                }
                if(ean.length()<13){
                    clearFields();
                    Toast toast = Toast.makeText(getActivity(), getActivity().getString(R.string.bar_code_no_valid), Toast.LENGTH_SHORT);
                    toast.show();
                    return;
                }

                if( isNetworkAvailable( getActivity() )  ){
                    //Once we have an ISBN, start a book intent
                    Intent bookIntent = new Intent(getActivity(), BookService.class);
                    bookIntent.putExtra(BookService.EAN, ean);
                    bookIntent.setAction(BookService.FETCH_BOOK);
                    getActivity().startService(bookIntent);
                }else {
                    Toast toast = Toast.makeText(getActivity(), getActivity().getString(R.string.no_network_available), Toast.LENGTH_SHORT);
                    toast.show();
                }

                AddBook.this.restartLoader();
            }
        });

        rootView.findViewById(R.id.scan_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // This is the callback method that the system will invoke when your button is
                // clicked. You might do this by launching another app or by including the
                //functionality directly in this app.
                // Hint: Use a Try/Catch block to handle the Intent dispatch gracefully, if you
                // are using an external app.
                //when you're done, remove the toast below.

                Context context = getActivity();


                Intent intent = new Intent(context,SimpleScannerActivity.class);
                startActivityForResult(intent, SimpleScannerActivity.READ_BAR_CODE);
            }
        });

        rootView.findViewById(R.id.save_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ean.setText("");
            }
        });

        rootView.findViewById(R.id.delete_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent bookIntent = new Intent(getActivity(), BookService.class);
                bookIntent.putExtra(BookService.EAN, ean.getText().toString());
                bookIntent.setAction(BookService.DELETE_BOOK);
                getActivity().startService(bookIntent);
                ean.setText("");
            }
        });

        //Fixing error when screen rotates, textview loosing hint
        if(savedInstanceState!=null){
            savedEan = savedInstanceState.getString(EAN_CONTENT);
            if( null != savedEan && !savedEan.equals("") ) {
                ean.setText(savedEan);
                ean.setHint("");
            }
        }

        return rootView;
    }

    private void restartLoader(){
        getLoaderManager().restartLoader(LOADER_ID, null, this);
    }

    @Override
    public android.support.v4.content.Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if(ean.getText().length()==0){
            return null;
        }
        String eanStr= ean.getText().toString();
        if(eanStr.length()==10 && !eanStr.startsWith("978")){
            eanStr=generateISBN13( eanStr );
        }
        return new CursorLoader(
                getActivity(),
                AlexandriaContract.BookEntry.buildFullBookUri(Long.parseLong(eanStr)),
                null,
                null,
                null,
                null
        );
    }

    @Override
    public void onLoadFinished(android.support.v4.content.Loader<Cursor> loader, Cursor data) {
        if (!data.moveToFirst()) {
            return;
        }

        String bookTitle = data.getString(data.getColumnIndex(AlexandriaContract.BookEntry.TITLE));
        ((TextView) rootView.findViewById(R.id.bookTitle)).setText(bookTitle);

        String bookSubTitle = data.getString(data.getColumnIndex(AlexandriaContract.BookEntry.SUBTITLE));
        ((TextView) rootView.findViewById(R.id.bookSubTitle)).setText(bookSubTitle);

        String authors = data.getString(data.getColumnIndex(AlexandriaContract.AuthorEntry.AUTHOR));
        //Fixing null pointer exception error
        if(null == authors){
            authors = "";
        }
        String[] authorsArr = authors!= null ? authors.split(",") : (new String[0]);
        ((TextView) rootView.findViewById(R.id.authors)).setLines(authorsArr.length);
        ((TextView) rootView.findViewById(R.id.authors)).setText(authors.replace(",","\n"));
        String imgUrl = data.getString(data.getColumnIndex(AlexandriaContract.BookEntry.IMAGE_URL));
        if(Patterns.WEB_URL.matcher(imgUrl).matches()){
            Picasso.with(getActivity())
                    .load(imgUrl)
                    .placeholder(R.drawable.ic_launcher)
                    .tag(getActivity())
                    .into((ImageView) rootView.findViewById(R.id.bookCover));

            rootView.findViewById(R.id.bookCover).setVisibility(View.VISIBLE);
        }

        String categories = data.getString(data.getColumnIndex(AlexandriaContract.CategoryEntry.CATEGORY));
        ((TextView) rootView.findViewById(R.id.categories)).setText(categories);

        rootView.findViewById(R.id.save_button).setVisibility(View.VISIBLE);
        rootView.findViewById(R.id.delete_button).setVisibility(View.VISIBLE);
    }

    @Override
    public void onLoaderReset(android.support.v4.content.Loader<Cursor> loader) {

    }

    /**
     * Clear fragment text views
     */
    private void clearFields(){
        ((TextView) rootView.findViewById(R.id.bookTitle)).setText("");
        ((TextView) rootView.findViewById(R.id.bookSubTitle)).setText("");
        ((TextView) rootView.findViewById(R.id.authors)).setText("");
        ((TextView) rootView.findViewById(R.id.categories)).setText("");
        rootView.findViewById(R.id.bookCover).setVisibility(View.INVISIBLE);
        rootView.findViewById(R.id.save_button).setVisibility(View.INVISIBLE);
        rootView.findViewById(R.id.delete_button).setVisibility(View.INVISIBLE);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        activity.setTitle(R.string.scan);
    }

    // Call Back method  to get the Message form other Activity
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        // check if the request code is same as what is passed  here it is 2
        if( requestCode == SimpleScannerActivity.READ_BAR_CODE  ){
            if( resultCode == getActivity().RESULT_OK ) {
                String message = data.getStringExtra(SimpleScannerActivity.SCAN_RESULT);

                if( isNumeric(message) ){
                    ean = (EditText) rootView.findViewById(R.id.ean);
                    ean.setText( message );
                }else{
                    Toast toast = Toast.makeText(getActivity(),getActivity().getString(R.string.bar_code_no_valid), Toast.LENGTH_LONG );
                    toast.show();
                }
            }
        }
    }

    /**
     * Validate if the isbn code is numeric
     * @param str isbn code to validate
     * @return
     */
    private static boolean isNumeric( String str ){
        return str.matches("-?\\d+(\\.\\d+)?");  //match a number with optional '-' and decimal.
    }


    /**
     * Generate a isbn 13 after a isbn 10 code
     * @param isbn10 isbn code to convert
     * @return
     */
    private static String generateISBN13( final String isbn10 ){
        int sum=0;
        int number;
        int checkDigit;
        String isbn13 = isbn10.substring(0, isbn10.length() - 1 );
        isbn13 = "978" + isbn13;
        for( int i = 0; i < isbn13.length(); i++ ){
            number = Character.digit( isbn13.charAt(i) ,10 );
            if( (i + 1) % 2 == 0 ){
                number *=3;
            }
            sum += number;
        }
        checkDigit = (10 - (sum % 10) ) % 10;
        return isbn13 + checkDigit;
    }

    /**
     * Check if the network is available
     * @param c - context in which the method was invoked
     * @return true if network is available, false otherwise.
     */
    public static boolean isNetworkAvailable(Context c){
        ConnectivityManager cm =
                (ConnectivityManager)c.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();
    }
}
