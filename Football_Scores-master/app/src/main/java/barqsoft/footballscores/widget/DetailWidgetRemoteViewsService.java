package barqsoft.footballscores.widget;

import android.annotation.TargetApi;
import android.content.Intent;
import android.database.Cursor;
import android.os.Binder;
import android.os.Build;
import android.widget.AdapterView;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import java.text.SimpleDateFormat;
import java.util.Date;

import barqsoft.footballscores.DatabaseContract;
import barqsoft.footballscores.MainActivity;
import barqsoft.footballscores.R;
import barqsoft.footballscores.ScoresAdapter;
import barqsoft.footballscores.Utilities;

/**
 * Created by Antonio on 15-10-05.
 * Create the views used by the widget
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class DetailWidgetRemoteViewsService extends RemoteViewsService {

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {

        return new RemoteViewsFactory() {
            private Cursor data = null;

            @Override
            public void onCreate() {
                // Nothing to do
            }

            @Override
            public void onDataSetChanged() {

                if (data != null) {
                    data.close();
                }
                // This method is called by the app hosting the widget (e.g., the launcher)
                // However, our ContentProvider is not exported so it doesn't have access to the
                // data. Therefore we need to clear (and finally restore) the calling identity so
                // that calls use our process and permission
                final long identityToken = Binder.clearCallingIdentity();
                SimpleDateFormat mformat = new SimpleDateFormat("yyyy-MM-dd");
                Date date = new Date(Utilities.pagerDate( Utilities.CURRENT_DAY ) );
                String[] selectionArgs = {mformat.format(date)};

                data = getContentResolver().query(DatabaseContract.scores_table.buildScoreWithDate(),
                        null,
                        null,
                        selectionArgs,
                        null);

                Binder.restoreCallingIdentity(identityToken);
            }

            @Override
            public void onDestroy() {
                if (data != null) {
                    data.close();
                    data = null;
                }
            }

            @Override
            public int getCount() {
                return data == null ? 0 : data.getCount();
            }

            @Override
            public RemoteViews getViewAt(int position) {
                if (position == AdapterView.INVALID_POSITION ||
                        data == null || !data.moveToPosition(position)) {
                    return null;
                }
                RemoteViews views = new RemoteViews(getPackageName(),
                        R.layout.widget_detail_list_item);



                double detail_match_id = data.getDouble(ScoresAdapter.COL_ID);
                String homeName = data.getString(ScoresAdapter.COL_HOME);
                String awayName = data.getString(ScoresAdapter.COL_AWAY);


                // Add the data to the RemoteViews
                views.setImageViewResource(R.id.home_crest,  Utilities.getTeamCrestByTeamName(homeName));
                views.setImageViewResource(R.id.away_crest,  Utilities.getTeamCrestByTeamName(awayName));

                // Content Descriptions for RemoteViews were only added in ICS MR1
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
                    setRemoteContentDescription(views, homeName + R.string.vs + awayName );
                }
                views.setTextViewText(R.id.home_name, homeName);
                views.setTextViewText(R.id.away_name, awayName);

                views.setTextViewText(R.id.score_textview, Utilities.getScores(data.getInt(ScoresAdapter.COL_HOME_GOALS),
                        data.getInt(ScoresAdapter.COL_AWAY_GOALS)));
                views.setTextViewText(R.id.data_textview, data.getString(ScoresAdapter.COL_MATCHTIME));

                final Intent fillInIntent = new Intent();

                fillInIntent.putExtra(MainActivity.SELECTED, detail_match_id);
                fillInIntent.putExtra(MainActivity.POSITION, position);
                views.setOnClickFillInIntent(R.id.widget_list_item, fillInIntent);
                return views;
            }

            @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1)
            private void setRemoteContentDescription(RemoteViews views, String description) {
                views.setContentDescription(R.id.icon, description);
            }

            @Override
            public RemoteViews getLoadingView() {
                return new RemoteViews(getPackageName(), R.layout.widget_detail_list_item);
            }

            @Override
            public int getViewTypeCount() {
                return 1;
            }

            @Override
            public long getItemId(int position) {
                if (data.moveToPosition(position))
                    return data.getLong(ScoresAdapter.COL_ID);
                return position;
            }

            @Override
            public boolean hasStableIds() {
                return true;
            }
        };
    }
}
