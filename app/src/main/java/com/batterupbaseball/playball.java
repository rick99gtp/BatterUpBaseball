package com.batterupbaseball;

import android.app.Activity;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;

public class playball extends Activity {
    Game game;
    Bundle bundle;
    Team vTeam, hTeam;
    SQLiteDatabase myDB;
    String teamSelected;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.playball);

        bundle = new Bundle();

        initGame();
    }

    private void initGame() {
        String vSeasonName, hSeasonName;

        SharedPreferences prefs = getSharedPreferences("prefsFile", 0);
        int vTeamID = prefs.getInt("vTeamID", 1);
        int hTeamID = prefs.getInt("hTeamID", 2);
        teamSelected = prefs.getString("USER_TEAM", "V");

        if(teamSelected.equals("V")) {
            vSeasonName = prefs.getString("userSeasonFileName", "players_2016.db");
            hSeasonName = prefs.getString("oppSeasonFileName", "players_2016.db");
        }
        else {
            vSeasonName = prefs.getString("oppSeasonFileName", "players_2016.db");
            hSeasonName = prefs.getString("userSeasonFileName", "players_2016.db");
        }

        buildVisTeam(vSeasonName, vTeamID);
        buildHomeTeam(hSeasonName, hTeamID);

        getVisLineups();
        getHomeLineups();
    }

    private void buildVisTeam(String seasonName, int teamID) {
        vTeam = new Team();

        myDB = openOrCreateDatabase(seasonName, MODE_PRIVATE, null);

        // *********
        // TEAMNAME
        // *********
        Cursor cTeam = myDB.query("teams", null, "_id='" + teamID + "'", null, null, null, null);

        if(cTeam.moveToFirst()) {
            int col = cTeam.getColumnIndex("team_name");
            vTeam.name = cTeam.getString(col);
        }

        cTeam.close();

        // *******
        // LINEUP
        // *******
        getVisLineups();

        Cursor cPlayer = null;

        for(int i = 0; i < 9; i++) {
             cPlayer = myDB.query("players", null, "_id='" + vTeam.lineupID[i] + "'", null, null, null, null);

            if(cPlayer.moveToFirst()) {
                Player thisPlayer = new Player();

                int col1 = cPlayer.getColumnIndex("first_name");
                int col2 = cPlayer.getColumnIndex("last_name");

                thisPlayer.name = cPlayer.getString(col1) + " " + cPlayer.getString(col2);

                vTeam.addPlayerToLineup(thisPlayer);
            }
        }

        cPlayer.close();

        myDB.close();
    }

    private void buildHomeTeam(String seasonName, int teamID) {

    }

    private void getVisLineups() {
        vTeam.lineupID = getIntent().getIntArrayExtra("visLineup");
    }

    private void getHomeLineups() {
        //game.homePlayerID = getIntent().getIntArrayExtra("homeLineup");
    }
}