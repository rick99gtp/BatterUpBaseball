package com.batterupbaseball;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
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
    int userStarter, oppStarter;
    String oppPitcherName;
    String oppPitcherThrows;
    int[] userBullpen = new int[8];
    int[] oppBullpen = new int[8];
    int[] userLineup = new int[9];
    int[] userDefense = new int[9];
    int[] userBench = new int[6];
    int[] oppLineup = new int[9];
    int[] oppDefense = new int[9];
    int[] oppBench = new int[6];
    String TAG = "com.batterupbaseball";
    boolean useDH = true;
    String userTeam;
    int hTeamID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.exhibition_select_lineup);

        SharedPreferences prefs = getSharedPreferences("prefsFile", 0);
        userStarter = prefs.getInt("exhibition_user_starter", 0);
        useDH = prefs.getBoolean("exhibition_useDH", true);
        hTeamID = prefs.getInt("hTeamID", 0);
        userTeam = prefs.getString("USER_TEAM", "V");

        getMatchupIDs();
        getUserSeasonFileName();
        getOppSeasonFileName();
        getOpponentPitcher();
        getUserLineup();
        getUserBullpen();
        getOppBullpen();

        TextView tvPlayBall = (TextView) findViewById(R.id.tvPlayBall);
        tvPlayBall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getOppLineup();

                Intent intent = new Intent(exhibition_select_lineup.this, playball.class);

                if(userTeam.equals("V")) {
                    intent.putExtra("visLineup", userLineup);
                    intent.putExtra("visBench", userBench);
                    intent.putExtra("visBullpen", userBullpen);
                    intent.putExtra("visStarter", userStarter);
                    Log.d(TAG, "VIS STARTER #1: " + userStarter);
                    intent.putExtra("visDefense", userDefense);

                    intent.putExtra("homeLineup", oppLineup);
                    intent.putExtra("homeBench", oppBench);
                    intent.putExtra("homeBullpen", oppBullpen);
                    intent.putExtra("homeStarter", oppStarter);
                    intent.putExtra("homeDefense", oppDefense);
                }
                else {
                    intent.putExtra("visLineup", oppLineup);
                    intent.putExtra("visBench", oppBench);
                    intent.putExtra("visBullpen", oppBullpen);
                    intent.putExtra("visStarter", oppStarter);
                    intent.putExtra("visDefense", oppDefense);

                    intent.putExtra("homeLineup", userLineup);
                    intent.putExtra("homeBench", userBench);
                    intent.putExtra("homeBullpen", userBullpen);
                    intent.putExtra("homeStarter", userStarter);
                    intent.putExtra("homeDefense", userDefense);
                }

                startActivity(intent);
            }
        });

        final CheckBox chkDH = (CheckBox) findViewById(R.id.chkDH);
        chkDH.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences prefs = getSharedPreferences("prefsFile", 0);
                SharedPreferences.Editor editor = prefs.edit();

                if(chkDH.isChecked()) {
                    useDH = true;
                }
                else {
                    useDH = false;
                }

                editor.putBoolean("exhibition_useDH", useDH);
                editor.apply();
            }
        });
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
            SharedPreferences prefs = getSharedPreferences("prefsFile", 0);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString("USERSEASONFILENAME", userSeasonFileName);
            editor.apply();
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
            SharedPreferences prefs = getSharedPreferences("prefsFile", 0);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString("OPPSEASONFILENAME", oppSeasonFileName);
            editor.apply();
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

        oppStarter = starterID[iStarterID-1];

        cPlayer = myDB.query("players", null, "_id='" + oppStarter + "'", null, null, null, null);

        if(cPlayer.moveToFirst()) {
            int col = cPlayer.getColumnIndex("first_name");
            firstName = cPlayer.getString(col);

            col = cPlayer.getColumnIndex("last_name");
            lastName = cPlayer.getString(col);

            oppPitcherName = firstName + " " + lastName;

            col = cPlayer.getColumnIndex("throws");
            oppPitcherThrows = cPlayer.getString(col).toUpperCase();
        }

        cPlayer.close();

        myDB.close();

        // put in textview
        TextView tvOppPitcher = (TextView) findViewById(R.id.tvOppPitcherName);
        tvOppPitcher.setText("Opponent Pitcher: \n" + oppPitcherThrows + " | " + oppPitcherName);
    }

    private void getUserLineup() {
        int[] tvPlayer = {R.id.tvName1, R.id.tvName2, R.id.tvName3, R.id.tvName4, R.id.tvName5, R.id.tvName6, R.id.tvName7, R.id.tvName8, R.id.tvName9};
        int[] tvPos = {R.id.tvPos1, R.id.tvPos2, R.id.tvPos3, R.id.tvPos4, R.id.tvPos5, R.id.tvPos6, R.id.tvPos7, R.id.tvPos8, R.id.tvPos9};

        Cursor cLineup = null;
        Cursor cBench = null;

        myDB = openOrCreateDatabase(userSeasonFileName, MODE_PRIVATE, null);

        CheckBox chkDH = (CheckBox) findViewById(R.id.chkDH);

        if(chkDH.isChecked()) {
            cLineup = myDB.query("lineups_dh", null, "team_id='" + userTeamID + "'", null, null, null, null);
        }
        else {
            cLineup = myDB.query("lineups_noDH", null, "team_id='" + userTeamID + "'", null, null, null, null);
        }

        if(cLineup.moveToFirst()) {
            for(int i=0; i < 9; i++) {
                int col = cLineup.getColumnIndex("bat_" + (i+1));
                userLineup[i] = cLineup.getInt(col);

                // position
                col = cLineup.getColumnIndex("def_" + (i+1));
                String pos = cLineup.getString(col);

                userDefense[setPos(pos)] = i;

                TextView tPos = (TextView) findViewById(tvPos[i]);
                tPos.setText(pos);

                Cursor cPlayer = myDB.query("players", null, "_id='" + userLineup[i] + "'", null, null, null, null);

                if(cPlayer.moveToFirst()) {
                    // name
                    col = cPlayer.getColumnIndex("first_name");
                    String fName = cPlayer.getString(col);

                    col = cPlayer.getColumnIndex("last_name");
                    String lName = cPlayer.getString(col);

                    String playerName = fName + " " + lName;

                    TextView tPlayer = (TextView) findViewById(tvPlayer[i]);
                    tPlayer.setText(playerName);

                }

                cPlayer.close();
            }
        }

        cLineup.close();

        if(chkDH.isChecked()) {
            cBench = myDB.query("bench_dh", null, "team_id='" + userTeamID + "'", null, null, null, null);
        }
        else {
            cBench = myDB.query("bench_noDH", null, "team_id='" + userTeamID + "'", null, null, null, null);
        }

        if(cBench.moveToFirst()) {
            for (int i = 0; i < 6; i++) {
                // save bench id's, 9999 = no player-skip
                int col = cBench.getColumnIndex("bench_" + (i+1));
                int bPlayer = cBench.getInt(col);

                if(bPlayer !=9999) {
                    userBench[i] = bPlayer;
                }
            }
        }

        cBench.close();

        myDB.close();
    }

    private void getUserBullpen() {
        Cursor cBullpen = null;
        myDB = openOrCreateDatabase(userSeasonFileName, MODE_PRIVATE, null);

        CheckBox chkDH = (CheckBox) findViewById(R.id.chkDH);

        if(chkDH.isChecked()) {
            cBullpen = myDB.query("bullpen_dh", null, "team_id='" + userTeamID + "'", null, null, null, null);
        }
        else {
            cBullpen = myDB.query("bullpen_noDH", null, "team_id='" + userTeamID + "'", null, null, null, null);
        }

        if(cBullpen.moveToFirst()) {
            for (int i = 0; i < 8; i++) {
                int col = cBullpen.getColumnIndex("bullpen_" + (i+1));
                if(cBullpen.getInt(col) != 9999) {
                    userBullpen[i] = cBullpen.getInt(col);
                }
            }
        }

        cBullpen.close();

        myDB.close();
    }

    private void getOppBullpen() {
        Cursor cBullpen = null;
        myDB = openOrCreateDatabase(oppSeasonFileName, MODE_PRIVATE, null);

        CheckBox chkDH = (CheckBox) findViewById(R.id.chkDH);

        if(chkDH.isChecked()) {
            cBullpen = myDB.query("bullpen_dh", null, "team_id='" + oppTeamID + "'", null, null, null, null);
        }
        else {
            cBullpen = myDB.query("bullpen_noDH", null, "team_id='" + oppTeamID + "'", null, null, null, null);
        }

        if(cBullpen.moveToFirst()) {
            for (int i = 0; i < 8; i++) {
                int col = cBullpen.getColumnIndex("bullpen_" + (i+1));
                if(cBullpen.getInt(col) != 9999) {
                    oppBullpen[i] = cBullpen.getInt(col);
                }
            }
        }

        cBullpen.close();

        myDB.close();
    }

    private void getOppLineup() {
        Cursor cLineup = null;
        Cursor cBench = null;

        myDB = openOrCreateDatabase(oppSeasonFileName, MODE_PRIVATE, null);

        CheckBox chkDH = (CheckBox) findViewById(R.id.chkDH);

        if(chkDH.isChecked()) {
            cLineup = myDB.query("lineups_dh", null, "team_id='" + oppTeamID + "'", null, null, null, null);
        }
        else {
            cLineup = myDB.query("lineups_noDH", null, "team_id='" + oppTeamID + "'", null, null, null, null);
        }

        if(cLineup.moveToFirst()) {
            for(int i=0; i < 9; i++) {
                int col = cLineup.getColumnIndex("bat_" + (i+1));
                oppLineup[i] = cLineup.getInt(col);

                // position
                col = cLineup.getColumnIndex("def_" + (i+1));
                String pos = cLineup.getString(col);

                oppDefense[setPos(pos)] = i;
            }
        }

        cLineup.close();

        if(chkDH.isChecked()) {
            cBench = myDB.query("bench_dh", null, "team_id='" + oppTeamID + "'", null, null, null, null);
        }
        else {
            cBench = myDB.query("bench_noDH", null, "team_id='" + oppTeamID + "'", null, null, null, null);
        }

        if(cBench.moveToFirst()) {
            for (int i = 0; i < 6; i++) {
                // save bench id's, 9999 = no player-skip
                int col = cBench.getColumnIndex("bench_" + (i+1));
                int bPlayer = cBench.getInt(col);

                if(bPlayer != 9999) {
                    oppBench[i] = bPlayer;
                }
            }
        }

        cBench.close();

        myDB.close();
    }

    private int setPos(String pos) {
        int posNum = 0;

        switch(pos) {
            case "c":
                posNum = 1;
                break;
            case "1b":
                posNum = 2;
                break;
            case "2b":
                posNum = 3;
                break;
            case "3b":
                posNum = 4;
                break;
            case "ss":
                posNum = 5;
                break;
            case "lf":
                posNum = 6;
                break;
            case "cf":
                posNum = 7;
                break;
            case "rf":
                posNum = 8;
                break;
            default:
                break;
        }
        return posNum;
    }
}
