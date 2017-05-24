package com.batterupbaseball;

import android.app.Activity;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import java.util.Random;

public class exhibition_select_lineup extends Activity {

    int oppSeasonID;
    int oppTeamID;
    int userSeasonID;
    int userTeamID;
    SQLiteDatabase myDB;
    String userSeasonFileName;
    String oppSeasonFileName;
    int[] starterID = new int[5];
    String oppPitcherName;
    String oppPitcherThrows;
    int oppPitcherValueVsLeft;
    int oppPitcherValueVsRight;
    int oppPitcherDefense;
    int oppPitcherContactVsLeft;
    int oppPitcherContactVsRight;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.exhibition_select_lineup);

        getMatchupIDs();
        getUserSeasonFileName();
        getOppSeasonFileName();
        getOpponentPitcher();
        getUserLineup();

    }

    private void getMatchupIDs() {
        SharedPreferences myPrefs = getSharedPreferences("prefsFile", 0);

        userSeasonID = myPrefs.getInt("userSeasonID", 1);
        oppSeasonID = myPrefs.getInt("oppSeasonID", 1);

        userTeamID = myPrefs.getInt("userTeamID", 1);
        oppTeamID = myPrefs.getInt("oppTeamID", 1);
    }

    private void getUserSeasonFileName() {

        // open the season_owned database
        myDB = openOrCreateDatabase("seasons_owned.db", MODE_PRIVATE, null);

        Cursor seasonOwned = myDB.query("seasons", null, "_id='" + userSeasonID + "'", null, null, null, null);

        if(seasonOwned.moveToFirst()) {
            // database file name
            int colSeasonID = seasonOwned.getColumnIndex("seasonID");
            userSeasonFileName = seasonOwned.getString(colSeasonID);
        }

        // close the cursor
        seasonOwned.close();
        // close the database
        myDB.close();
    }

    private void getOppSeasonFileName() {

        // open the season_owned database
        myDB = openOrCreateDatabase("seasons_owned.db", MODE_PRIVATE, null);

        Cursor seasonOwned = myDB.query("seasons", null, "_id='" + oppSeasonID + "'", null, null, null, null);

        if(seasonOwned.moveToFirst()) {
            // database file name
            int colSeasonID = seasonOwned.getColumnIndex("seasonID");
            oppSeasonFileName = seasonOwned.getString(colSeasonID);
        }

        // close the cursor
        seasonOwned.close();
        // close the database
        myDB.close();
    }

    private void getOpponentPitcher() {
        int numStarters = 0;

        String lastName, firstName;
        myDB = openOrCreateDatabase(oppSeasonFileName, MODE_PRIVATE, null);

        // get player IDs
        Cursor cTeam = myDB.query("rotation", null, "team_id='" + oppTeamID + "'", null, null, null, null);

        if(cTeam.moveToFirst()) {
            for (int i = 0; i < 5; i++) {
                int col = cTeam.getColumnIndex("sp" + (i + 1));
                starterID[i] = cTeam.getInt(col);
                if(starterID[i] > 0) {
                    numStarters++; // used to count number of starters in rotation.  Not always 5.  Used for choosing opponent pitcher using random number
                }
            }
        }

        cTeam.close();

        // get random number between 1 and numStarters
        Random r = new Random();
        int iStarterID = r.nextInt(numStarters) + 1;

        // get player names
        Cursor cPlayer = null;

        cPlayer = myDB.query("players", null, "_id='" + starterID[iStarterID-1] + "'", null, null, null, null);

        if(cPlayer.moveToFirst()) {
            int col = cPlayer.getColumnIndex("first_name");
            firstName = cPlayer.getString(col);

            col = cPlayer.getColumnIndex("last_name");
            lastName = cPlayer.getString(col);

            oppPitcherName = firstName + " " + lastName;

            col = cPlayer.getColumnIndex("throws");
            oppPitcherThrows = cPlayer.getString(col).toUpperCase();

            col = cPlayer.getColumnIndex("vsl_rating");
            oppPitcherValueVsLeft = cPlayer.getInt(col);

            col = cPlayer.getColumnIndex("vsr_rating");
            oppPitcherValueVsRight = cPlayer.getInt(col);

            col = cPlayer.getColumnIndex("defense_rating");
            oppPitcherDefense = cPlayer.getInt(col);

            col = cPlayer.getColumnIndex("vsl_rating");
            oppPitcherContactVsLeft = cPlayer.getInt(col);

            col = cPlayer.getColumnIndex("vsr_rating");
            oppPitcherContactVsRight = cPlayer.getInt(col);
        }

        cPlayer.close();

        myDB.close();

        // put in textview
        TextView tvOppPitcher = (TextView) findViewById(R.id.tvOppPitcherName);
        tvOppPitcher.setText("Opponent Pitcher: \n" + oppPitcherThrows + " | " + oppPitcherName);
    }

    private void getUserLineup() {

    }
}
