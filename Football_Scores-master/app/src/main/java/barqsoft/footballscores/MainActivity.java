package barqsoft.footballscores;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import barqsoft.footballscores.sync.FootballScoresSyncAdapter;


/**
 * App's main activity
 */
public class MainActivity extends AppCompatActivity
{
    public static double selected_match_id;
    public static int position;
    public static int current_fragment = 2;
    public static String LOG_TAG = "MainActivity";
    private final String save_tag = "Save Test";
    private PagerFragment my_main;
    public static String SELECTED = "SELECTED";
    public static String POSITION = "POSITION";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d(LOG_TAG, "Reached MainActivity onCreate" +  (savedInstanceState != null ? "savedInstanceState" : "null" ));

        if ( null == savedInstanceState ) {

            my_main = new PagerFragment();

            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, my_main)
                    .commit();

            if( getIntent() != null ){
                selected_match_id = getIntent().getDoubleExtra(MainActivity.SELECTED, 0);
                position = getIntent().getIntExtra(MainActivity.POSITION, 0);
                MainActivity.current_fragment = 2;
            }
        }
        FootballScoresSyncAdapter.initializeSyncAdapter(this);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_about)
        {
            Intent start_about = new Intent(this,AboutActivity.class);
            startActivity(start_about);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState)
    {
        Log.v(save_tag, "will save");
        Log.v(save_tag, "selected id: " + selected_match_id);
        outState.putDouble("Selected_match", selected_match_id);
        outState.putInt("position", position);
        super.onSaveInstanceState(outState);
    }



    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState){
        Log.v(save_tag,"will retrive");
        Log.v(save_tag,"selected id: "+savedInstanceState.getDouble("Selected_match"));
        selected_match_id = savedInstanceState.getDouble("Selected_match");
        position = savedInstanceState.getInt("position");
        super.onRestoreInstanceState(savedInstanceState);
    }
}
