package barqsoft.footballscores.widget;

import android.annotation.TargetApi;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.RemoteViews;

import barqsoft.footballscores.R;
import barqsoft.footballscores.sync.FootballScoresSyncAdapter;

/**
 * Created by Antonio on 15-09-30.
 * Receives the event broadcast relevant for the widget and acts according to them
 */
public class ScoreWidgetProvider extends AppWidgetProvider {
    private static final String LOG_TAG = ScoreWidgetProvider.class.getSimpleName();

    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        Intent service_start = new Intent(context, ScoreWidgetIntentService.class);
        context.startService(service_start);
    }

    @Override
    public void onReceive(@NonNull Context context, @NonNull Intent intent) {
        super.onReceive(context, intent);
        Log.d(LOG_TAG, "Receiving broadcast: " + intent.getAction());
        if (FootballScoresSyncAdapter.ACTION_DATA_UPDATED.equals(intent.getAction())) {
            Log.d(LOG_TAG, "Starting service: " + intent.getAction());
            context.startService(new Intent(context, ScoreWidgetIntentService.class));
        }
    }

    @Override
    public void onAppWidgetOptionsChanged(Context context, AppWidgetManager appWidgetManager,
                                          int appWidgetId, Bundle newOptions) {
        context.startService(new Intent(context, ScoreWidgetIntentService.class));
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1)
    private void setRemoteContentDescription(RemoteViews views, String description) {
        views.setContentDescription(R.id.widget, description);
    }
}
