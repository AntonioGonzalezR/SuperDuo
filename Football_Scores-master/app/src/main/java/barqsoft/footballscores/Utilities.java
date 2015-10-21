package barqsoft.footballscores;

import android.content.Context;
import android.text.format.Time;

import java.text.SimpleDateFormat;

import barqsoft.footballscores.service.DataFetcher;

/**
 * Created by yehya khaled on 3/3/2015.
 * App's helper class
 */
public class Utilities {

    public static final int CURRENT_DAY = 2;

    /**
     * Get the correct name of a league, based on its number
     *
     * @param ctx
     * @param league_num
     * @return
     */
    public static String getLeague(Context ctx, int league_num){
        //Fixed problem with the league number
        switch (String.valueOf(league_num)){
            case DataFetcher.SERIE_A : return ctx.getString( R.string.seriaa );
            case DataFetcher.PREMIER_LEAGUE : return  ctx.getString( R.string.premierleague );
            case DataFetcher.CHAMPIONS_LEAGUE : return ctx.getString(R.string.champions_league);
            case DataFetcher.PRIMERA_DIVISION : return ctx.getString(R.string.primeradivison);
            case DataFetcher.BUNDESLIGA1 : return ctx.getString(R.string.bundesliga);
            case DataFetcher.BUNDESLIGA2 : return ctx.getString(R.string.bundesliga2);
            case DataFetcher.LIGUE1 : return ctx.getString(R.string.ligue1);
            case DataFetcher.SEGUNDA_DIVISION : return ctx.getString(R.string.segundadivison);

            default: return "Not known League Please report";
        }
    }

    /**
     * Show information about the match day
     * Fix problem with the league number
     * @param match_day
     * @param league_num
     * @return
     */
    public static String getMatchDay(Context ctx, int match_day,int league_num){
        if(String.valueOf(league_num).equals( DataFetcher.CHAMPIONS_LEAGUE ) ){
            if (match_day <= 6){
                return ctx.getString(R.string.group_stage_text) + " , " + ctx.getString(R.string.group_stage_text) + " : " + match_day ;
            }
            else if(match_day == 7 || match_day == 8) {
                return ctx.getString(R.string.first_knockout_round);
            }
            else if(match_day == 9 || match_day == 10) {
                return ctx.getString(R.string.quarter_final);
            }
            else if(match_day == 11 || match_day == 12) {
                return ctx.getString(R.string.semi_final);
            }
            else {
                return ctx.getString(R.string.final_text);
            }
        }else{
            return ctx.getString(R.string.matchday_text) + ": " + match_day;
        }
    }


    /**
     *
     * @param context
     * @param dateInMillis
     * @return
     */
    public static String getDayName(Context context, long dateInMillis) {
        // If the date is today, return the localized version of "Today" instead of the actual
        // day name.

        Time t = new Time();
        t.setToNow();
        int julianDay = Time.getJulianDay(dateInMillis, t.gmtoff);
        int currentJulianDay = Time.getJulianDay(System.currentTimeMillis(), t.gmtoff);
        if (julianDay == currentJulianDay) {
            return context.getString(R.string.today);
        } else if ( julianDay == currentJulianDay +1 ) {
            return context.getString(R.string.tomorrow);
        }else if ( julianDay == currentJulianDay -1)
        {
            return context.getString(R.string.yesterday);
        }
        else{
            Time time = new Time();
            time.setToNow();
            // Otherwise, the format is just the day of the week (e.g "Wednesday".
            SimpleDateFormat dayFormat = new SimpleDateFormat("EEEE");
            return dayFormat.format(dateInMillis);
        }
    }

    public static String getScores(int home_goals,int awaygoals) {
        if(home_goals < 0 || awaygoals < 0){
            return " - ";
        }
        else{
            return String.valueOf(home_goals) + " - " + String.valueOf(awaygoals);
        }
    }

    public static int getTeamCrestByTeamName (String teamname)
    {
        if (teamname==null){return R.drawable.no_icon;}
        switch (teamname){ //This is the set of icons that are currently in the app. Feel free to find and add more
            //as you go.
            case "Arsenal FC" : return R.drawable.arsenal;
            case "Manchester United FC" : return R.drawable.manchester_united;
            case "Swansea City FC" : return R.drawable.swansea_city_afc;
            case "Leicester City" : return R.drawable.leicester_city_fc_hd_logo;
            case "Everton FC" : return R.drawable.everton_fc_logo1;
            case "West Ham United FC" : return R.drawable.west_ham;
            case "Tottenham Hotspur FC" : return R.drawable.tottenham_hotspur;
            case "West Bromwich Albion" : return R.drawable.west_bromwich_albion_hd_logo;
            case "Sunderland AFC" : return R.drawable.sunderland;
            case "Stoke City FC" : return R.drawable.stoke_city;
            case "Livierpool FC" : return R.drawable.liverpool;
            default: return R.drawable.no_icon;
        }
    }

    public static long pagerDate( int pagerPos ){
        return System.currentTimeMillis()+((pagerPos-2)*86400000);
    }
}
