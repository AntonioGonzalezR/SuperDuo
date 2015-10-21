package barqsoft.footballscores;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.text.SimpleDateFormat;
import java.util.Date;

import barqsoft.footballscores.sync.FootballScoresSyncAdapter;

/**
 * Created by yehya khaled on 2/27/2015.
 * Fragment that paginates the fragments of different days
 */
public class PagerFragment extends Fragment {

    public static final String LOG_TAG = PagerFragment.class.getSimpleName();
    public static final int NUM_PAGES = 5;

    public ViewPager mPagerHandler;
    private PageAdapter mPagerAdapter;

    private MainScreenFragment[] viewFragments = new MainScreenFragment[5];

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState){

        Log.d(LOG_TAG, "onCreateView" );

        View rootView = inflater.inflate(R.layout.pager_fragment, container, false);
        mPagerHandler = (ViewPager) rootView.findViewById(R.id.pager);
        mPagerAdapter = new PageAdapter( getChildFragmentManager() );


        long dateMilis;
        Date fragmentDate;
        //Taking creation of variable format out of the loop and renaming it, avoiding memory leaks
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        for (int i = 0;i < NUM_PAGES;i++){
            dateMilis = Utilities.pagerDate( i );
            fragmentDate = new Date(dateMilis);
            viewFragments[i] = new MainScreenFragment();
            viewFragments[i].setFragmentDate(format.format(fragmentDate));
            viewFragments[i].setDayName( Utilities.getDayName(getActivity(), dateMilis) );
        }

        mPagerHandler.setAdapter(mPagerAdapter);
        if(savedInstanceState != null) {
            MainActivity.current_fragment = savedInstanceState.getInt("Pager_Current");
        }
        mPagerHandler.setCurrentItem(MainActivity.current_fragment);
        FootballScoresSyncAdapter.syncImmediately(getActivity(), false);
        return rootView;
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        Log.d(LOG_TAG, "Saving instance....");
        outState.putInt("Pager_Current", mPagerHandler.getCurrentItem() );
        super.onSaveInstanceState(outState);
    }

    /**
     *
     */
    private class PageAdapter extends FragmentStatePagerAdapter {
        public PageAdapter(FragmentManager fm)
        {
            super(fm);
        }

        @Override
        public Fragment getItem(int i){
            return viewFragments[i];
        }

        @Override
        public int getCount(){
            return NUM_PAGES;
        }

        // Returns the page title for the top indicator
        @Override
        public CharSequence getPageTitle(int position) {
            return Utilities.getDayName(getActivity(), Utilities.pagerDate( position ));
        }
    }
}
