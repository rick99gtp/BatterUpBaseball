package com.batterupbaseball;

import android.app.Activity;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

public class exhibition_select_pitcher extends Activity implements View.OnClickListener {
    int[] llRow = {R.id.llRow1, R.id.llRow2, R.id.llRow3, R.id.llRow4, R.id.llRow5};
    int[] llThrows = {R.id.tvThrows1, R.id.tvThrows2, R.id.tvThrows3, R.id.tvThrows4, R.id.tvThrows5};
    int[] llName = {R.id.tvName1, R.id.tvName2, R.id.tvName3, R.id.tvName4, R.id.tvName5};
    LinearLayout[] llStarter = new LinearLayout[5];
    TextView[] tvThrows = new TextView[5];
    TextView[] tvName = new TextView[5];

    int seasonID;
    String seasonFileName;
    String teamSelected; // V=Visitor, H=Home
    int teamID;
    SQLiteDatabase myDB = null;
    int[] starterID = new int[5];
    String[] playerName = new String[5];
    String[] playerThrows = new String[5];
    //int[] playerWins = new int[5];
    //int[] playerLosses = new int[5];
    //int[] playerPct = new int[5];
    //int[] playerVsLeft = new int[5];
    //int[] playerVsRight = new int[5];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.exhibition_select_pitcher);

        // Did the user click on the Visitor or Home Team?
        Bundle bundle=getIntent().getExtras();
        teamSelected = bundle.getString("USER_TEAM");

        // prepare the Layouts
        for(int i=0; i<5; i++) {
            llStarter[i] = (LinearLayout) findViewById(llRow[i]);
            llStarter[i].setOnClickListener(this);
            tvThrows[i] = (TextView) findViewById(llThrows[i]);
            tvName[i] = (TextView) findViewById(llName[i]);
        }

        // get the season file name
        getSeasonFileName();
        // get rotation
        getRotation();
        showRotation();

    }

    @Override
    public void onClick(View v) {
        resetColors();

        switch(v.getId()) {
            case R.id.llRow1:
                llStarter[0].setBackgroundColor(Color.RED);
                break;
            case R.id.llRow2:
                llStarter[1].setBackgroundColor(Color.RED);
                break;
            case R.id.llRow3:
                llStarter[2].setBackgroundColor(Color.RED);
                break;
            case R.id.llRow4:
                llStarter[3].setBackgroundColor(Color.RED);
                break;
            case R.id.llRow5:
                llStarter[4].setBackgroundColor(Color.RED);
                break;
            default:
                break;
        }
    }

    private void resetColors() {
        for(int i=0; i<5; i++) {
            llStarter[i].setBackgroundColor(Color.TRANSPARENT);
        }
    }

    private void getSeasonFileName() {
        SharedPreferences myPrefs = getSharedPreferences("prefsFile", 0);
        if(teamSelected.equals("V")) {
            seasonID = myPrefs.getInt("vSeasonID", 1);
            teamID = myPrefs.getInt("vTeamID", 1);
        }
        else {
            seasonID = myPrefs.getInt("hSeasonID", 1);
            teamID = myPrefs.getInt("hTeamID", 1);
        }

        // open the season_owned database
        myDB = openOrCreateDatabase("seasons_owned.db", MODE_PRIVATE, null);

        Cursor seasonOwned = myDB.query("seasons", null, "_id='" + seasonID + "'", null, null, null, null);

        if(seasonOwned.moveToFirst()) {
            // database file name
            int colSeasonID = seasonOwned.getColumnIndex("seasonID");
            seasonFileName = seasonOwned.getString(colSeasonID);
        }

        // close the cursor
        seasonOwned.close();
        // close the database
        myDB.close();
    }

    private void getRotation() {
        String lastName, firstName;
        myDB = openOrCreateDatabase(seasonFileName, MODE_PRIVATE, null);

        // get player IDs
        Cursor cTeam = myDB.query("rotation", null, "team_id='" + teamID + "'", null, null, null, null);

        if(cTeam.moveToFirst()) {
            for (int i = 0; i < 5; i++) {
                int col = cTeam.getColumnIndex("sp" + (i + 1));
                starterID[i] = cTeam.getInt(col);
                Log.d("com.batterupbaseball", "" + starterID[i]);
            }
        }

        cTeam.close();

        // get player names
        Cursor cPlayer = null;

        for(int i=0; i<5; i++) {
            cPlayer = myDB.query("players", null, "_id='" + starterID[i] + "'", null, null, null, null);

            if(cPlayer.moveToFirst()) {
                int col = cPlayer.getColumnIndex("first_name");
                firstName = cPlayer.getString(col);

                col = cPlayer.getColumnIndex("last_name");
                lastName = cPlayer.getString(col);

                playerName[i] = firstName + " " + lastName;

                Log.d("com.batterupbaseball", playerName[i]);

                col = cPlayer.getColumnIndex("throws");
                playerThrows[i] = cPlayer.getString(col).toUpperCase();
            }
        }

        myDB.close();
    }

    private void showRotation() {
        for(int i=0; i<5; i++) {
            tvThrows[i].setText(playerThrows[i]);
            tvName[i].setText(playerName[i]);
        }
    }
}
