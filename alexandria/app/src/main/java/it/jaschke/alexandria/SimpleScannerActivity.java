package it.jaschke.alexandria;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.google.zxing.Result;

import me.dm7.barcodescanner.zxing.ZXingScannerView;

/**
 * Created by Antonio on 15-10-13.
 * Read a bar code using the camera and parse its content.
 * Used to read the isbn codes.
 */
public class SimpleScannerActivity extends Activity implements ZXingScannerView.ResultHandler{


    private static final String LOG_TAG =  SimpleScannerActivity.class.getSimpleName();
    public static final String SCAN_RESULT = "SCAN_RESULT";
    public static final int READ_BAR_CODE = 1;

    private ZXingScannerView mScannerView;

    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);
        mScannerView = new ZXingScannerView(this);   // Programmatically initialize the scanner view
        setContentView(mScannerView);                // Set the scanner view as the content view
    }


    @Override
    public void onResume() {
        super.onResume();
        mScannerView.setResultHandler(this); // Register ourselves as a handler for scan results.
        mScannerView.startCamera();          // Start camera on resume
    }

    @Override
    public void onPause() {
        super.onPause();
        mScannerView.stopCamera();           // Stop camera on pause
    }


    /**
     *
     * @param rawResult
     */
    @Override
    public void handleResult(Result rawResult) {
        Log.v(LOG_TAG, rawResult.getText()); // Prints scan results
        Log.v(LOG_TAG, rawResult.getBarcodeFormat().toString()); // Prints the scan format (qrcode, pdf417 etc.)
        Intent intent = new Intent();
        intent.putExtra(SCAN_RESULT, rawResult.getText() );
        setResult(RESULT_OK, intent);
        finish();

    }

}
