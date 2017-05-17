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

import org.w3c.dom.Text;

public class exhibition_select_pitcher extends Activity implements View.OnClickListener {
    int[] llRow = {R.id.llRow1, R.id.llRow2, R.id.llRow3, R.id.llRow4, R.id.llRow5};
    int[] llThrows = {R.id.tvThrows1, R.id.tvThrows2, R.id.tvThrows3, R.id.tvThrows4, R.id.tvThrows5};
    int[] llName = {R.id.tvName1, R.id.tvName2, R.id.tvName3, R.id.tvName4, R.id.tvName5};
    int[] llVsLeft = {R.id.tvVsLeft1, R.id.tvVsLeft2, R.id.tvVsLeft3, R.id.tvVsLeft4, R.id.tvVsLeft5};
    int[] llVsRight = {R.id.tvVsRight1, R.id.tvVsRight2, R.id.tvVsRight3, R.id.tvVsRight4, R.id.tvVsRight5};
    LinearLayout[] llStarter = new LinearLayout[5];
    TextView[] tvThrows = new TextView[5];
    TextView[] tvName = new TextView[5];
    TextView[] tvVsLeft = new TextView[5];
    TextView[] tvVsRight = new TextView[5];

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
    int[] playerVsLeft = new int[5];
    int[] playerVsRight = new int[5];
    int selectedStarter;

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
            tvVsLeft[i] = (TextView) findViewById(llVsLeft[i]);
            tvVsRight[i] = (TextView) findViewById(llVsRight[i]);
        }

        // get the season file name
        getSeasonFileName();
        // get rotation
        getRotation();
        showRotation();

        // highlight starter selected
        highlightStarter();

    }

    @Override
    public void onClick(View v) {
        resetColors();

        switch(v.getId()) {
            case R.id.llRow1:
                llStarter[0].setBackgroundColor(Color.RED);
                selectedStarter = 0;
                break;
            case R.id.llRow2:
                llStarter[1].setBackgroundColor(Color.RED);
                selectedStarter = 1;
                break;
            case R.id.llRow3:
                llStarter[2].setBackgroundColor(Color.RED);
                selectedStarter = 2;
                break;
            case R.id.llRow4:
                llStarter[3].setBackgroundColor(Color.RED);
                selectedStarter = 3;
                break;
            case R.id.llRow5:
                llStarter[4].setBackgroundColor(Color.RED);
                selectedStarter = 4;
                break;
            default:
                break;
        }

        SharedPreferences myPrefs = getSharedPreferences("prefsFile", 0);
        SharedPreferences.Editor editor = myPrefs.edit();
        editor.putInt("exhibition_selected_starter", selectedStarter);
        editor.apply();
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

                col = cPlayer.getColumnIndex("throws");
                playerThrows[i] = cPlayer.getString(col).toUpperCase();

                col = cPlayer.getColumnIndex("vsl_rating");
                playerVsLeft[i] = cPlayer.getInt(col);

                col = cPlayer.getColumnIndex("vsr_rating");
                playerVsRight[i] = cPlayer.getInt(col);
            }
        }

        myDB.close();
    }

    private void showRotation() {
        for(int i=0; i<5; i++) {
            tvThrows[i].setText(playerThrows[i]);
            tvName[i].setText(playerName[i]);
            tvVsLeft[i].setText("" + playerVsLeft[i]);
            tvVsRight[i].setText("" + playerVsRight[i]);
        }
    }

    private void highlightStarter() {
        // get season ID
        SharedPreferences myPrefs = getSharedPreferences("prefsFile", 0);
        selectedStarter = myPrefs.getInt("exhibition_selected_starter", 0);

        resetColors();

        llStarter[selectedStarter].setBackgroundColor(Color.RED);
    }
}