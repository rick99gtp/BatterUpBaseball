package com.batterupbaseball;

import android.app.Activity;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import org.w3c.dom.Text;

public class exhibition extends Activity {
    SQLiteDatabase myDB;
    String seasonID;
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
        getTeamInfo();

        ImageView ivBack = (ImageView) findViewById(R.id.ivBack);
        ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void getTeamInfo() {
        myDB = openOrCreateDatabase(seasonID, MODE_PRIVATE, null);

        //cursor for the visiting team
        Cursor curSeason = myDB.query("teams", null, "_id=1", null, null, null, null);

        if(curSeason.moveToFirst()) {
            // team name
            int curTeamNameID = curSeason.getColumnIndex("team_name");
            visitorTeamName = curSeason.getString(curTeamNameID);

            // primary color
            int cPrimaryColor = curSeason.getColumnIndex("primary_color");
            visitorPrimaryColor = curSeason.getString(cPrimaryColor);

            // secondary color
            int cSecondaryColor = curSeason.getColumnIndex("secondary_color");
            visitorSecondaryColor = curSeason.getString(cPrimaryColor);

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

        //cursor for the home team
        curSeason = myDB.query("teams", null, "_id=2", null, null, null, null);

        if(curSeason.moveToFirst()) {
            int curTeamNameID = curSeason.getColumnIndex("team_name");
            homeTeamName = curSeason.getString(curTeamNameID);

            // primary color
            int cPrimaryColor = curSeason.getColumnIndex("primary_color");
            homePrimaryColor = curSeason.getString(cPrimaryColor);

            // secondary color
            int cSecondaryColor = curSeason.getColumnIndex("secondary_color");
            homeSecondaryColor = curSeason.getString(cPrimaryColor);

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
        TextView tvVisitorTeamName = (TextView) findViewById(R.id.tvVisitorTeamName);
        TextView tvHomeTeamName = (TextView) findViewById(R.id.tvHomeTeamName);
        tvVisitorTeamName.setText(visitorTeamName);
        tvHomeTeamName.setText(homeTeamName);

        // apply primary and secondary colors
        int primaryColor = Color.parseColor(visitorPrimaryColor);
        tvVisitorTeamName.setBackgroundColor(primaryColor);

        primaryColor = Color.parseColor(homePrimaryColor);
        tvHomeTeamName.setBackgroundColor(primaryColor);

        int secondaryColor = Color.parseColor(visitorSecondaryColor);
        tvVisitorTeamName.setBackgroundColor(secondaryColor);

        secondaryColor = Color.parseColor(homeSecondaryColor);
        tvHomeTeamName.setBackgroundColor(secondaryColor);

        // show ratings
        TextView tvVisitorBattingRating = (TextView) findViewById(R.id.tvVisitorBattingRating);
        TextView tvVisitorPitchingRating = (TextView) findViewById(R.id.tvVisitorPitchingRating);
        TextView tvVisitorFieldingRating = (TextView) findViewById(R.id.tvVisitorFieldingRating);

        TextView tvHomeBattingRating = (TextView) findViewById(R.id.tvHomeBattingRating);
        TextView tvHomePitchingRating = (TextView) findViewById(R.id.tvHomePitchingRating);
        TextView tvHomeFieldingRating = (TextView) findViewById(R.id.tvHomeFieldingRating);
        
        tvVisitorBattingRating.setText("" + visitorBattingRating);
        tvVisitorPitchingRating.setText("" + visitorPitchingRating);
        tvVisitorFieldingRating.setText("" + visitorFieldingRating);
        tvHomeBattingRating.setText("" + homeBattingRating);
        tvHomePitchingRating.setText("" + homePitchingRating);
        tvHomeFieldingRating.setText("" + homeFieldingRating);

    }

    private void getSeasonID() {
        // open the season_owned database
        myDB = openOrCreateDatabase("seasons_owned.db", MODE_PRIVATE, null);

        Cursor seasonOwned = myDB.query("seasons", null, "_id=1", null, null, null, null);

        if(seasonOwned.moveToFirst()) {
            int colSeasonID = seasonOwned.getColumnIndex("seasonID");
            seasonID = seasonOwned.getString(colSeasonID);
        }

        // close the cursor
        seasonOwned.close();
        // close the database
        myDB.close();
    }

}
