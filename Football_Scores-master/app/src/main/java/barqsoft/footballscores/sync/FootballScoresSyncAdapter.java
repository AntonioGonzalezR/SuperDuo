package barqsoft.footballscores.sync;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SyncRequest;
import android.content.SyncResult;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import barqsoft.footballscores.R;
import barqsoft.footballscores.service.DataFetcher;

/**
 * Created by Antonio on 15-10-04.
 * Handle the transfer of data between a server and an
 * app, using the Android sync adapter framework.
 */
public class FootballScoresSyncAdapter extends AbstractThreadedSyncAdapter {

    public static final String ACTION_DATA_UPDATED = "barqsoft.footballscores.ACTION_DATA_UPDATED";
    // Interval at which to sync with the scores, in milliseconds.
    // 60 seconds (1 minute) * 180 = 3 hours
    public static final int SYNC_INTERVAL = 60 * 30;
    public static final int SYNC_FLEXTIME = SYNC_INTERVAL/3;

    public static final String UPDATE_ONLY_CURRENT_DAY = "UPDATE_ONLY_CURRENT_DAY";
    public static final String LOG_TAG = FootballScoresSyncAdapter.class.getSimpleName();

    public FootballScoresSyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {
        Log.d(LOG_TAG, "onPerformSync Called.");
        if( !extras.containsKey(UPDATE_ONLY_CURRENT_DAY) ) {
            DataFetcher.getData(getContext(), "n2");
            DataFetcher.getData(getContext(), "p2");
        }else{
            DataFetcher.getData(getContext(), "n" + extras.getString(UPDATE_ONLY_CURRENT_DAY) );
            DataFetcher.getData(getContext(), "p" + extras.getString(UPDATE_ONLY_CURRENT_DAY) );
        }
        updateWidgets();
    }

    private void updateWidgets(){
        Intent dataUpdatedIntent = new Intent(ACTION_DATA_UPDATED)
                .setPackage(getContext().getPackageName());
        getContext().sendBroadcast(dataUpdatedIntent);
    }

    /**
     * Helper method to have the sync adapter sync immediately
     * @param context The context used to access the account service
     */
    public static void syncImmediately( Context context, boolean isOnlyToday ) {
        Bundle bundle = new Bundle();
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        if( isOnlyToday ) {
            bundle.putString(UPDATE_ONLY_CURRENT_DAY, "0");
        }
        ContentResolver.requestSync(getSyncAccount(context),
                context.getString(R.string.content_authority), bundle);
    }

    /**
     * Helper method to get the fake account to be used with SyncAdapter, or make a new one
     * if the fake account doesn't exist yet.  If we make a new account, we call the
     * onAccountCreated method so we can initialize things.
     *
     * @param context The context used to access the account service
     * @return a fake account.
     */
    public static Account getSyncAccount(Context context) {
        // Get an instance of the Android account manager
        AccountManager accountManager =
                (AccountManager) context.getSystemService(Context.ACCOUNT_SERVICE);

        // Create the account type and default account
        Account newAccount = new Account(
                context.getString(R.string.app_name), context.getString(R.string.sync_account_type));

        // If the password doesn't exist, the account doesn't exist
        if ( null == accountManager.getPassword(newAccount) ) {

        /*
         * Add the account and account type, no password or user data
         * If successful, return the Account object, otherwise report an error.
         */
            if (!accountManager.addAccountExplicitly(newAccount, "", null)) {
                return null;
            }
            /*
             * If you don't set android:syncable="true" in
             * in your <provider> element in the manifest,
             * then call ContentResolver.setIsSyncable(account, AUTHORITY, 1)
             * here.
             */

            onAccountCreated(newAccount, context);
        }
        return newAccount;
    }


    /**
     * Helper method to schedule the sync adapter periodic execution
     */
    public static void configurePeriodicSync(Context context, int syncInterval, int flexTime) {
        Account account = getSyncAccount(context);
        String authority = context.getString(R.string.content_authority);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            // we can enable inexact timers in our periodic sync
            SyncRequest request = new SyncRequest.Builder().
                    syncPeriodic(syncInterval, flexTime).
                    setSyncAdapter(account, authority).
                    setExtras(new Bundle()).build();
            ContentResolver.requestSync(request);
        } else {
            ContentResolver.addPeriodicSync(account,
                    authority, new Bundle(), syncInterval);
        }
    }


    private static void onAccountCreated(Account newAccount, Context context) {
        /*
         * Since we've created an account
         */
        FootballScoresSyncAdapter.configurePeriodicSync(context, SYNC_INTERVAL, SYNC_FLEXTIME);

        /*
         * Without calling setSyncAutomatically, our periodic sync will not be enabled.
         */
        ContentResolver.setSyncAutomatically(newAccount, context.getString(R.string.content_authority), true);

        /*
         * Finally, let's do a sync to get things started
         */
        syncImmediately(context, false);

    }

    public static void initializeSyncAdapter(Context context) {
        getSyncAccount(context);
    }
}
