package com.batterupbaseball;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
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
    String visitorSeasonFileName, homeSeasonFileName;
    int visitorSeasonID, homeSeasonID; // _id of season in seasons_owned
    int visitorTeamID, homeTeamID;
    String teamName;
    String primaryColor;
    String secondaryColor;
    int battingRating;
    int pitchingRating;
    int fieldingRating;
    int vYear, hYear;
    String TAG = "com.batterupbaseball";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.exhibition);

        // Hide the status bar.
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);

        // get season ID
        SharedPreferences myPrefs = getSharedPreferences("prefsFile", 0);
        visitorTeamID = myPrefs.getInt("vTeamID", 1);
        homeTeamID = myPrefs.getInt("hTeamID", 2);

        // get the current season
        getVisitorSeasonFileName();
        getHomeSeasonFileName();

        // get team names
        getTeamInfo(visitorSeasonFileName, visitorTeamID);
        showVisitorViews();
        getTeamInfo(homeSeasonFileName, homeTeamID);
        showHomeViews();

        TextView tvVisitorTeamName = (TextView) findViewById(R.id.tvVisitorTeamName);
        tvVisitorTeamName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // load Exhibition Team Select
                Intent intent = new Intent(exhibition.this, exhibition_team_select.class);
                String strName = "V";
                intent.putExtra("TEAM_SELECTED", strName);
                startActivityForResult(intent, 1);
            }
        });

        TextView tvHomeTeamName = (TextView) findViewById(R.id.tvHomeTeamName);
        tvHomeTeamName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // load Exhibition Team Select
                Intent intent = new Intent(exhibition.this, exhibition_team_select.class);
                String strName = "H";
                intent.putExtra("TEAM_SELECTED", strName);
                startActivityForResult(intent, 1);
            }
        });

        TextView tvSelectVisitorTeam = (TextView) findViewById(R.id.tvSelectVisitorTeam);
        tvSelectVisitorTeam.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(exhibition.this, exhibition_select_pitcher.class);
                intent.putExtra("USER_TEAM", "V");
                startActivity(intent);
            }
        });

        TextView tvSelectHomeTeam = (TextView) findViewById(R.id.tvSelectHomeTeam);
        tvSelectHomeTeam.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(exhibition.this, exhibition_select_pitcher.class);
                intent.putExtra("USER_TEAM", "H");
                startActivity(intent);
            }
        });
    }

    private void getTeamInfo(String thisSeasonFileName, int teamID) {
        myDB = openOrCreateDatabase(thisSeasonFileName, MODE_PRIVATE, null);

        Cursor curSeason = myDB.query("teams", null, "_id='" + teamID + "'", null, null, null, null);

        if(curSeason.moveToFirst()) {
            int curTeamNameID = curSeason.getColumnIndex("team_name");
            teamName = curSeason.getString(curTeamNameID);

            // primary color
            int cPrimaryColor = curSeason.getColumnIndex("primary_color");
            primaryColor = curSeason.getString(cPrimaryColor);

            // secondary color
            int cSecondaryColor = curSeason.getColumnIndex("secondary_color");
            secondaryColor = curSeason.getString(cSecondaryColor);

            // batting rating
            int cBatting = curSeason.getColumnIndex("batting_rating");
            battingRating = curSeason.getInt(cBatting);

            // pitching rating
            int cPitching = curSeason.getColumnIndex("pitching_rating");
            pitchingRating = curSeason.getInt(cPitching);

            // fielding rating
            int cFielding = curSeason.getColumnIndex("fielding_rating");
            fieldingRating = curSeason.getInt(cFielding);
        }

        //close the cursor
        curSeason.close();
        //close the database
        myDB.close();
    }

    private void showVisitorViews() {
        // populate name
        TextView tvVisitorTeamName = (TextView) findViewById(R.id.tvVisitorTeamName);
        tvVisitorTeamName.setText(vYear + " " + teamName);

        // apply primary and secondary colors
        int primaryColorID = Color.parseColor(primaryColor);
        tvVisitorTeamName.setBackgroundColor(primaryColorID);

        int secondaryColorID = Color.parseColor(secondaryColor);
        tvVisitorTeamName.setTextColor(secondaryColorID);

        // show ratings
        TextView tvVisitorBattingRating = (TextView) findViewById(R.id.tvVisitorBattingRating);
        TextView tvVisitorPitchingRating = (TextView) findViewById(R.id.tvVisitorPitchingRating);
        TextView tvVisitorFieldingRating = (TextView) findViewById(R.id.tvVisitorFieldingRating);

        tvVisitorBattingRating.setText("" + battingRating);
        tvVisitorPitchingRating.setText("" + pitchingRating);
        tvVisitorFieldingRating.setText("" + fieldingRating);
    }

    private void showHomeViews() {
        // populate name
        TextView tvHomeTeamName = (TextView) findViewById(R.id.tvHomeTeamName);
        tvHomeTeamName.setText(hYear + " " + teamName);

        // apply primary and secondary colors
        int primaryColorID = Color.parseColor(primaryColor);
        tvHomeTeamName.setBackgroundColor(primaryColorID);

        int secondaryColorID = Color.parseColor(secondaryColor);
        tvHomeTeamName.setTextColor(secondaryColorID);

        // show ratings
        TextView tvHomeBattingRating = (TextView) findViewById(R.id.tvHomeBattingRating);
        TextView tvHomePitchingRating = (TextView) findViewById(R.id.tvHomePitchingRating);
        TextView tvHomeFieldingRating = (TextView) findViewById(R.id.tvHomeFieldingRating);

        tvHomeBattingRating.setText("" + battingRating);
        tvHomePitchingRating.setText("" + pitchingRating);
        tvHomeFieldingRating.setText("" + fieldingRating);
    }

    private void getVisitorSeasonFileName() {
        SharedPreferences myPrefs = getSharedPreferences("prefsFile", 0);
        visitorSeasonID = myPrefs.getInt("vSeasonID", 1);

        // open the season_owned database
        myDB = openOrCreateDatabase("seasons_owned.db", MODE_PRIVATE, null);

        Cursor seasonOwned = myDB.query("seasons", null, "_id='" + visitorSeasonID + "'", null, null, null, null);

        if(seasonOwned.moveToFirst()) {
            // database file name
            int colSeasonID = seasonOwned.getColumnIndex("seasonID");
            visitorSeasonFileName = seasonOwned.getString(colSeasonID);

            // season year
            int colYear = seasonOwned.getColumnIndex("year");
            vYear = seasonOwned.getInt(colYear);
        }

        // close the cursor
        seasonOwned.close();
        // close the database
        myDB.close();
    }

    private void getHomeSeasonFileName() {
        SharedPreferences myPrefs = getSharedPreferences("prefsFile", 0);
        homeSeasonID = myPrefs.getInt("hSeasonID", 1);

        // open the season_owned database
        myDB = openOrCreateDatabase("seasons_owned.db", MODE_PRIVATE, null);

        Cursor seasonOwned = myDB.query("seasons", null, "_id='" + homeSeasonID + "'", null, null, null, null);

        if(seasonOwned.moveToFirst()) {
            int colSeasonID = seasonOwned.getColumnIndex("seasonID");
            homeSeasonFileName = seasonOwned.getString(colSeasonID);

            int colYear = seasonOwned.getColumnIndex("year");
            hYear = seasonOwned.getInt(colYear);
        }

        // close the cursor
        seasonOwned.close();
        // close the database
        myDB.close();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to
            if (resultCode == RESULT_OK) {
                // The user picked a contact.
                // The Intent's data Uri identifies which contact was selected.
                // update the correct team with the new info
                SharedPreferences myPrefs = getSharedPreferences("prefsFile", 0);
                visitorTeamID = myPrefs.getInt("vTeamID", 1);
                homeTeamID = myPrefs.getInt("hTeamID", 2);

                getVisitorSeasonFileName();
                getHomeSeasonFileName();

                // get team names
                getTeamInfo(visitorSeasonFileName, visitorTeamID);
                showVisitorViews();
                getTeamInfo(homeSeasonFileName, homeTeamID);
                showHomeViews();
            }
    }


}
