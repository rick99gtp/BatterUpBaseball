package com.batterupbaseball;

import android.app.Activity;
import android.app.ListActivity;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;

public class exhibition_team_select extends Activity {
    ListView lvSeasons;
    ListView lvTeams;
    ArrayList<String> sSeason = new ArrayList<String>();
    public String TAG = "com.batterupbaseball";
    String SeasonSelected = "players_2016.db";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.exhibition_team_select);

        getSeasons();

        lvSeasons = (ListView) findViewById(R.id.lvSeasons);

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, android.R.id.text1, sSeason);

        lvSeasons.setAdapter(adapter);

        // 1. pass context and data to the custom adapter
        Exhibition_Array_Adapter teamAdapter = new Exhibition_Array_Adapter(this, generateData());

        //2. setListAdapter
        ListView lvTeams = (ListView) findViewById(R.id.lvTeams);
        lvTeams.setAdapter(teamAdapter);
    }

    private ArrayList<teams> generateData(){
        ArrayList<teams> teamsArray = new ArrayList<teams>();
        int teamCount = 0;
        String teamname = "";
        int battingrating = 0;
        int pitchingrating = 0;
        int fieldingrating = 0;
        int teamColor1 = 0;
        int teamColor2 = 0;

        SQLiteDatabase myDB = openOrCreateDatabase(SeasonSelected, MODE_PRIVATE, null);

        Cursor cTeams = myDB.query("teams", null, null, null, null, null, null);

        while (cTeams.moveToNext()) {
            int col = cTeams.getColumnIndex("team_name");
            teamname = cTeams.getString(col);
            col = cTeams.getColumnIndex("batting_rating");
            battingrating = cTeams.getInt(col);
            col = cTeams.getColumnIndex("pitching_rating");
            pitchingrating = cTeams.getInt(col);
            col = cTeams.getColumnIndex("fielding_rating");
            fieldingrating = cTeams.getInt(col);
            col = cTeams.getColumnIndex("primary_color");
            teamColor1 = Color.parseColor(cTeams.getString(col));
            col = cTeams.getColumnIndex("secondary_color");
            teamColor2 = Color.parseColor(cTeams.getString(col));

            teamsArray.add(new teams(teamname, battingrating, pitchingrating, fieldingrating, teamColor1, teamColor2));
            // increment counter
            teamCount++;
        }

        cTeams.close();

        myDB.close();
        //teams.add(new teams("Item 1","First Item on the list"));
        //teams.add(new teams("Item 2","Second Item on the list"));
        //teams.add(new teams("Item 3","Third Item on the list"));

        return teamsArray;
    }

    private void getSeasons() {
        boolean done = false;
        int seasonCount = 0;

        SQLiteDatabase myDB = openOrCreateDatabase("seasons_owned.db", MODE_PRIVATE, null);

        Cursor cSeasons = myDB.query("seasons", null, null, null, null, null, null);

        while (cSeasons.moveToNext()) {
            int colTeamName = cSeasons.getColumnIndex("season_name");
            String thisTeamName = cSeasons.getString(colTeamName);
            sSeason.add(seasonCount, thisTeamName);
            // increment counter
            cSeasons.moveToNext();
        }

        cSeasons.close();

        myDB.close();
    }
}