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

        //cursor for the visitors
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

        }

        //cursor for the visitors
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
        }

        //close the cursor
        curSeason.close();
        //close the database
        myDB.close();

        // populate textviews
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
