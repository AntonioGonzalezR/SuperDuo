package barqsoft.footballscores.sync;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * Created by Antonio on 15-10-04.
 * Allow the sync adapter framework to run the code in the sync adapter class.
 */
public class FootballScoresSyncService extends Service {

    private static final Object sSyncAdapterLock = new Object();
    private static FootballScoresSyncAdapter sFootballScoresSyncAdapter = null;

    @Override
    public void onCreate() {
        synchronized (sSyncAdapterLock) {
            if (sFootballScoresSyncAdapter == null) {
                sFootballScoresSyncAdapter = new FootballScoresSyncAdapter(getApplicationContext(), true);
            }
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return sFootballScoresSyncAdapter.getSyncAdapterBinder();
    }
}
