package barqsoft.footballscores;

import android.annotation.TargetApi;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import java.io.Serializable;

import barqsoft.footballscores.sync.FootballScoresSyncAdapter;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainScreenFragment extends Fragment implements Serializable, LoaderManager.LoaderCallbacks<Cursor> {


    private static final String LOG_TAG = MainScreenFragment.class.getSimpleName();
    public static final int SCORES_LOADER = 0;

    public ScoresAdapter mAdapter;
    private String[] fragmentdate = new String[1];

    private ListView score_list = null;
    private int scroll_position;
    public String mDayName;

    public MainScreenFragment() { }

    private void update_scores(){
        Log.d( LOG_TAG,"Fragment position: " + mDayName );
        FootballScoresSyncAdapter.syncImmediately(getActivity(), true);

    }
    public void setFragmentDate(String date){
        fragmentdate[0] = date;
    }
    public void setDayName(String dayName){
        mDayName = dayName;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             final Bundle savedInstanceState) {


        if(  savedInstanceState != null ){
            setDayName( savedInstanceState.getString("mDayName") );
            setFragmentDate( savedInstanceState.getString("fragmentdate"));
        }
        update_scores();

        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        score_list = (ListView) rootView.findViewById(R.id.scores_list);

        mAdapter = new ScoresAdapter(getActivity(),null,0);
        score_list.setAdapter(mAdapter);

        mAdapter.detail_match_id = MainActivity.selected_match_id;

        if( MainActivity.position > 0){
            scroll_position = MainActivity.position;
        }

        score_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ViewHolder selected = (ViewHolder) view.getTag();
                mAdapter.detail_match_id = selected.match_id;
                MainActivity.selected_match_id = (int) selected.match_id;
                mAdapter.notifyDataSetChanged();
            }
        });
        return rootView;
    }

    @Override
    public void onResume(){
        Log.d(LOG_TAG, "Selected Item cursor " + MainActivity.selected_match_id);
        mAdapter.detail_match_id = MainActivity.selected_match_id;
        getLoaderManager().initLoader(SCORES_LOADER,null,this);
        super.onResume();
   }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        Log.d(LOG_TAG, "Saving instance MainScreenFragment....");
        outState.putString("mDayName", mDayName);
        outState.putString("fragmentdate", fragmentdate[0] );
        super.onSaveInstanceState(outState);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        CursorLoader loader = null;
        if(null != fragmentdate && null!= fragmentdate[0] ) {
            Log.d(LOG_TAG, fragmentdate[0] );
            loader = new CursorLoader(getActivity(), DatabaseContract.scores_table.buildScoreWithDate(),
                    null, null, fragmentdate, null);
        }
        return loader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor){
        mAdapter.swapCursor(cursor);
        if( null != score_list && MainActivity.position > 0 ) {
            // Set up the collection
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                scrollToPosition();
            } else {
                scrollToPositionV10( );
            }
            MainActivity.position = 0;
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader){
        mAdapter.swapCursor(null);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void scrollToPosition(){
        score_list.smoothScrollToPositionFromTop(MainActivity.position, 0);
    }

    public void scrollToPositionV10(){
        score_list.smoothScrollToPosition(MainActivity.position, 0);
    }


}
