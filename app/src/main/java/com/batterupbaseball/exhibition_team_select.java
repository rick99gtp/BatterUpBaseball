package com.batterupbaseball;

import android.app.Activity;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;

public class exhibition_team_select extends Activity {
    ListView lvSeasons;
    ListView lvTeams;
    ArrayList<String> sSeason = new ArrayList<String>();
    ArrayList<String> seasonFileName = new ArrayList<String>();
    public String TAG = "com.batterupbaseball";
    String seasonSelected;
    int selectedListItem;
    String teamSelected;
    Exhibition_Array_Adapter teamAdapter = null;
    Exhibition_Seasons_Array_Adapter seasonAdapter = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.exhibition_team_select);

        // Did the user click on the Visitor or Home Team?
        Bundle bundle=getIntent().getExtras();
        teamSelected = bundle.getString("TEAM_SELECTED");

        getSeasons();

        lvSeasons = (ListView) findViewById(R.id.lvSeasons);
        lvSeasons.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectedListItem = position;
                SharedPreferences myPrefs = getSharedPreferences("prefsFile", 0);
                SharedPreferences.Editor Editor = myPrefs.edit();
                Editor.putInt("seasonSelectedID", selectedListItem);
                Editor.commit();

                seasonAdapter.setSelectedIndex(position);

                // get seasonSelected from sSeason using selectedListItem
                seasonSelected = seasonFileName.get(selectedListItem);

                // TO DO **********************************************************************
                // reload team listview with teams from season selected if not already selected
                // TO DO **********************************************************************
                teamAdapter.clear();
                getTeams();

            }
        });

        SharedPreferences myPrefs = getSharedPreferences("prefsFile", 0);
        seasonSelected = myPrefs.getString("seasonSelected", getString(R.string.default_db_name));

        getTeams();

    }

    private void getTeams() {
        teamAdapter = new Exhibition_Array_Adapter(exhibition_team_select.this, generateData());
        ListView lvTeams = (ListView) findViewById(R.id.lvTeams);
        lvTeams.setAdapter(teamAdapter);

        lvTeams.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                teamAdapter.setSelectedIndex(position);
            }
        });
    }

    private ArrayList<season_class> generateSeasonData(){
        ArrayList<season_class> seasonsArray = new ArrayList<season_class>();
        String seasonName = "";
        int seasonCount = 0;

        SQLiteDatabase myDB = openOrCreateDatabase("seasons_owned.db", MODE_PRIVATE, null);

        Cursor cSeasons = myDB.query("seasons", null, null, null, null, null, null);

        while (cSeasons.moveToNext()) {
            // store the season name in sSeason ArrayList
            int colSeasonName = cSeasons.getColumnIndex("season_name");
            seasonName = cSeasons.getString(colSeasonName);

            int colSeasonFileName = cSeasons.getColumnIndex("seasonID");
            String seasonID = cSeasons.getString(colSeasonFileName);

            seasonsArray.add(new season_class(seasonName));
            seasonFileName.add(seasonCount, seasonID);
            seasonCount++;
        }

        cSeasons.close();

        myDB.close();

        return seasonsArray;
    }

    private ArrayList<teams> generateData(){
        ArrayList<teams> teamsArray = new ArrayList<teams>();
        String teamname = "";
        int battingrating = 0;
        int pitchingrating = 0;
        int fieldingrating = 0;
        int teamColor1 = 0;
        int teamColor2 = 0;

        SQLiteDatabase myDB = openOrCreateDatabase(seasonSelected, MODE_PRIVATE, null);

        Log.d("com.batterupbaseball", seasonSelected);
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
        }

        cTeams.close();

        myDB.close();

        return teamsArray;
    }

    private void getSeasons() {
        seasonAdapter = new Exhibition_Seasons_Array_Adapter(exhibition_team_select.this, generateSeasonData());
        ListView lvSeasons = (ListView) findViewById(R.id.lvSeasons);
        lvSeasons.setAdapter(seasonAdapter);
    }
}