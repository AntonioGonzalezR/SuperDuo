package barqsoft.footballscores.widget;

import android.app.IntentService;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.database.Cursor;
import android.util.Log;
import android.widget.RemoteViews;

import java.text.SimpleDateFormat;
import java.util.Date;

import barqsoft.footballscores.DatabaseContract;
import barqsoft.footballscores.MainActivity;
import barqsoft.footballscores.R;
import barqsoft.footballscores.ScoresAdapter;
import barqsoft.footballscores.Utilities;

/**
 * Created by Antonio on 15-10-04.
 * Handle request of information made by ScoreWidgetProvider
 */
public class ScoreWidgetIntentService extends IntentService {



    private static final String LOG_TAG = ScoreWidgetIntentService.class.getSimpleName();

    public ScoreWidgetIntentService() {
        super(LOG_TAG);
    }

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public ScoreWidgetIntentService(String name) {
        super(name);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        onUpdate();
    }

    private void onUpdate( ){
        Log.d(LOG_TAG, "OnUpdate....");
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(this,
                ScoreWidgetProvider.class));


        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");


        Date date = new Date(Utilities.pagerDate(Utilities.CURRENT_DAY));

        String[] selectionArgs = {format.format(date)};

        Log.d(LOG_TAG, "Updating Widget..."  + appWidgetIds.length + "" );
        // Perform this loop procedure for each Today widget
        String homeName;
        String awayName;
        for (int appWidgetId : appWidgetIds) {


            Cursor cursor = getContentResolver().query(DatabaseContract.scores_table.buildScoreWithDate(),
                    null,
                    null,
                    selectionArgs,
                    null);

            if( null == cursor ){
                return;
            }

            if (!cursor.moveToFirst()) {
                cursor.close();
                return;
            }

            double detail_match_id = cursor.getDouble(ScoresAdapter.COL_ID);
            homeName = cursor.getString(ScoresAdapter.COL_HOME);
            awayName = cursor.getString(ScoresAdapter.COL_AWAY);



            int layoutId = R.layout.widget_score;
            RemoteViews views = new RemoteViews(this.getPackageName(), layoutId);

            // Add the data to the RemoteViews
            views.setImageViewResource(R.id.home_crest,  Utilities.getTeamCrestByTeamName(homeName));
            views.setImageViewResource(R.id.away_crest,  Utilities.getTeamCrestByTeamName(awayName));

            views.setTextViewText(R.id.home_name, homeName);
            views.setTextViewText(R.id.away_name, awayName);

            views.setTextViewText(R.id.score_textview, Utilities.getScores(cursor.getInt(ScoresAdapter.COL_HOME_GOALS),
                    cursor.getInt(ScoresAdapter.COL_AWAY_GOALS)));
            views.setTextViewText(R.id.data_textview, cursor.getString(ScoresAdapter.COL_MATCHTIME));

            // Create an Intent to launch MainActivity
            Intent launchIntent = new Intent(this, MainActivity.class);
            launchIntent.putExtra(MainActivity.SELECTED, detail_match_id);
            launchIntent.putExtra(MainActivity.POSITION, 0);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, launchIntent, 0);
            views.setOnClickPendingIntent(R.id.widget, pendingIntent);

            // Tell the AppWidgetManager to perform an update on the current app widget
            appWidgetManager.updateAppWidget(appWidgetId, views);
        }
    }

}
