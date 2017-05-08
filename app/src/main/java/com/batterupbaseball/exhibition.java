package com.batterupbaseball;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

public class exhibition extends Activity {
    SQLiteDatabase myDB;
    String visitorSeasonID, homeSeasonID;
    String visitorYear, homeYear;
    String visitorTeamName, homeTeamName;
    String visitorPrimaryColor, homePrimaryColor;
    String visitorSecondaryColor, homeSecondaryColor;
    int visitorBattingRating, homeBattingRating;
    int visitorPitchingRating, homePitchingRating;
    int visitorFieldingRating, homeFieldingRating;

    private static String TAG = "com.batterupbaseball";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.exhibition);

        // Hide the status bar.
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);

        // get the current season
        getSeasonID();

        // get team names
        getVisitorTeamInfo();
        getHomeTeamInfo();

        TextView tvVisitorTeamName = (TextView) findViewById(R.id.tvVisitorTeamName);
        tvVisitorTeamName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // load Exhibition Team Select
                Intent intent = new Intent(exhibition.this, exhibition_team_select.class);
                startActivity(intent);
            }
        });

        TextView tvHomeTeamName = (TextView) findViewById(R.id.tvHomeTeamName);
        tvHomeTeamName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // load Exhibition Team Select
                Intent intent = new Intent(exhibition.this, exhibition_team_select.class);
                startActivity(intent);
            }
        });
    }

    private void getHomeTeamInfo() {
        myDB = openOrCreateDatabase(homeSeasonID, MODE_PRIVATE, null);

        //cursor for the home team
        Cursor curSeason = myDB.query("teams", null, "_id=8", null, null, null, null);

        if(curSeason.moveToFirst()) {
            int curTeamNameID = curSeason.getColumnIndex("team_name");
            homeTeamName = curSeason.getString(curTeamNameID);

            // primary color
            int cPrimaryColor = curSeason.getColumnIndex("primary_color");
            homePrimaryColor = curSeason.getString(cPrimaryColor);

            // secondary color
            int cSecondaryColor = curSeason.getColumnIndex("secondary_color");
            homeSecondaryColor = curSeason.getString(cSecondaryColor);

            // batting rating
            int cBatting = curSeason.getColumnIndex("batting_rating");
            homeBattingRating = curSeason.getInt(cBatting);

            // pitching rating
            int cPitching = curSeason.getColumnIndex("pitching_rating");
            homePitchingRating = curSeason.getInt(cPitching);

            // fielding rating
            int cFielding = curSeason.getColumnIndex("fielding_rating");
            homeFieldingRating = curSeason.getInt(cFielding);
        }

        //close the cursor
        curSeason.close();
        //close the database
        myDB.close();

        // populate name
        TextView tvHomeTeamName = (TextView) findViewById(R.id.tvHomeTeamName);
        tvHomeTeamName.setText(homeTeamName);

        // apply primary and secondary colors
        int primaryColor = Color.parseColor(homePrimaryColor);
        tvHomeTeamName.setBackgroundColor(primaryColor);

        int secondaryColor = Color.parseColor(homeSecondaryColor);
        tvHomeTeamName.setTextColor(secondaryColor);

        // show ratings
        TextView tvHomeBattingRating = (TextView) findViewById(R.id.tvHomeBattingRating);
        TextView tvHomePitchingRating = (TextView) findViewById(R.id.tvHomePitchingRating);
        TextView tvHomeFieldingRating = (TextView) findViewById(R.id.tvHomeFieldingRating);

        tvHomeBattingRating.setText("" + homeBattingRating);
        tvHomePitchingRating.setText("" + homePitchingRating);
        tvHomeFieldingRating.setText("" + homeFieldingRating);
    }

    private void getVisitorTeamInfo() {
        myDB = openOrCreateDatabase(visitorSeasonID, MODE_PRIVATE, null);

        //cursor for the visiting team
        Cursor curSeason = myDB.query("teams", null, "_id=14", null, null, null, null);

        if(curSeason.moveToFirst()) {
            // team name
            int curTeamNameID = curSeason.getColumnIndex("team_name");
            visitorTeamName = curSeason.getString(curTeamNameID);

            // primary color
            int cPrimaryColor = curSeason.getColumnIndex("primary_color");
            visitorPrimaryColor = curSeason.getString(cPrimaryColor);

            // secondary color
            int cSecondaryColor = curSeason.getColumnIndex("secondary_color");
            visitorSecondaryColor = curSeason.getString(cSecondaryColor);

            // batting rating
            int cBatting = curSeason.getColumnIndex("batting_rating");
            visitorBattingRating = curSeason.getInt(cBatting);
            
            // pitching rating
            int cPitching = curSeason.getColumnIndex("pitching_rating");
            visitorPitchingRating = curSeason.getInt(cPitching);

            // fielding rating
            int cFielding = curSeason.getColumnIndex("fielding_rating");
            visitorFieldingRating = curSeason.getInt(cFielding);

        }

        //close the cursor
        curSeason.close();
        //close the database
        myDB.close();

        // populate name
        TextView tvVisitorTeamName = (TextView) findViewById(R.id.tvVisitorTeamName);
        tvVisitorTeamName.setText(visitorTeamName);

        // apply primary and secondary colors
        int primaryColor = Color.parseColor(visitorPrimaryColor);
        tvVisitorTeamName.setBackgroundColor(primaryColor);

        int secondaryColor = Color.parseColor(visitorSecondaryColor);
        tvVisitorTeamName.setTextColor(secondaryColor);

        // show ratings
        TextView tvVisitorBattingRating = (TextView) findViewById(R.id.tvVisitorBattingRating);
        TextView tvVisitorPitchingRating = (TextView) findViewById(R.id.tvVisitorPitchingRating);
        TextView tvVisitorFieldingRating = (TextView) findViewById(R.id.tvVisitorFieldingRating);

        tvVisitorBattingRating.setText("" + visitorBattingRating);
        tvVisitorPitchingRating.setText("" + visitorPitchingRating);
        tvVisitorFieldingRating.setText("" + visitorFieldingRating);
    }

    private void getSeasonID() {
        // open the season_owned database
        myDB = openOrCreateDatabase("seasons_owned.db", MODE_PRIVATE, null);

        Cursor seasonOwned = myDB.query("seasons", null, "_id=1", null, null, null, null);

        if(seasonOwned.moveToFirst()) {
            int colSeasonID = seasonOwned.getColumnIndex("seasonID");
            visitorSeasonID = seasonOwned.getString(colSeasonID);
            homeSeasonID = visitorSeasonID; // both teams are from the 2016 set onCreate.  Modify when user changes to another season.
        }

        // close the cursor
        seasonOwned.close();
        // close the database
        myDB.close();
    }

}
