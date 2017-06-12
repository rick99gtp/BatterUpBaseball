package com.batterupbaseball;

import android.app.Activity;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;

import static android.content.ContentValues.TAG;

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
        String current_year = getResources().getString(R.string.default_db_name);

        SharedPreferences prefs = getSharedPreferences("prefsFile", 0);
        int vTeamID = prefs.getInt("vTeamID", 1);
        int hTeamID = prefs.getInt("hTeamID", 2);
        teamSelected = prefs.getString("USER_TEAM", "V");

        if(teamSelected.equals("V")) {
            vSeasonName = prefs.getString("userSeasonFileName", current_year);
            hSeasonName = prefs.getString("oppSeasonFileName", current_year);
        }
        else {
            vSeasonName = prefs.getString("oppSeasonFileName", current_year);
            hSeasonName = prefs.getString("userSeasonFileName", current_year);
        }

        buildVisTeam(vSeasonName, vTeamID);
        buildHomeTeam(hSeasonName, hTeamID);

        listVisTeam();
        listHomeTeam();
    }

    private void listVisTeam() {
        for(int i = 0; i < 9; i++) {
            Log.d(TAG, vTeam.lineup.get(i).getName());
        }

        for(int i = 0; i < vTeam.bench.size(); i++) {
            Log.d(TAG, "BENCH: " + vTeam.bench.get(i).getName());
        }

        for(int i = 0; i < vTeam.bullpen.size(); i++) {
            Log.d(TAG, "BULLPEN: " + vTeam.bullpen.get(i).getName());
        }
    }

    private void listHomeTeam() {
        for(int i = 0; i < 9; i++) {
            Log.d(TAG, hTeam.lineup.get(i).getName());
        }

        for(int i = 0; i < hTeam.bench.size(); i++) {
            Log.d(TAG, "BENCH: " + hTeam.bench.get(i).getName());
        }

        for(int i = 0; i < hTeam.bullpen.size(); i++) {
            Log.d(TAG, "BULLPEN: " + hTeam.bullpen.get(i).getName());
        }
    }

    private void buildVisTeam(String seasonName, int teamID) {
        vTeam = new Team();
        String[] sVsl = {"vsl_1b_game", "vsl_2b_game", "vsl_3b_game", "vsl_hr_game", "vsl_bb_game", "vsl_so_game", "vsl_hbp_game"};
        String[] sVsr = {"vsr_1b_game", "vsr_2b_game", "vsr_3b_game", "vsr_hr_game", "vsr_bb_game", "vsr_so_game", "vsr_hbp_game"};
        String[] sprayChart = {"pull", "center", "oppo"};
        String[] ballSpeed = {"soft", "med", "hard"};
        String[] running = {"baserunning", "stealing"};
        String[] defense = {"arm_rating", "defense_rating", "fld_range", "fld_error"};

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

                col1 = cPlayer.getColumnIndex("pos");
                thisPlayer.pos = cPlayer.getString(col1);

                col1 = cPlayer.getColumnIndex("bats");
                thisPlayer.pBats = cPlayer.getString(col1);

                col1 = cPlayer.getColumnIndex("throws");
                thisPlayer.pThrows = cPlayer.getString(col1);

                // vsl ratings
                for(int j = 0; j < 7; j++) {
                    col1 = cPlayer.getColumnIndex(sVsl[j]);
                    thisPlayer.pVsl[j] = cPlayer.getInt(col1);
                }

                // vsr ratings
                for(int j = 0; j < 7; j++) {
                    col1 = cPlayer.getColumnIndex(sVsr[j]);
                    thisPlayer.pVsr[j] = cPlayer.getInt(col1);
                }

                // sac bunt
                col1 = cPlayer.getColumnIndex("sac_bunt");
                thisPlayer.sac_bunt = cPlayer.getInt(col1);

                // spray chart
                for(int j = 0; j < 3; j++) {
                    col1 = cPlayer.getColumnIndex(sprayChart[j]);
                    thisPlayer.sprayChart[j] = cPlayer.getInt(col1);
                }

                // ball speed
                for(int j = 0; j < 3; j++) {
                    col1 = cPlayer.getColumnIndex(ballSpeed[j]);
                    thisPlayer.ballSpeed[j] = cPlayer.getInt(col1);
                }

                // running
                for(int j = 0; j < 2; j++) {
                    col1 = cPlayer.getColumnIndex(running[j]);
                    thisPlayer.running[j] = cPlayer.getInt(col1);
                }

                // avoid dp
                col1 = cPlayer.getColumnIndex("avoid_dp");
                thisPlayer.avoid_dp = cPlayer.getInt(col1);

                col1 = cPlayer.getColumnIndex("hold_rating");
                thisPlayer.hold_rating = cPlayer.getInt(col1);

                col1 = cPlayer.getColumnIndex("rsb");
                thisPlayer.rsb = cPlayer.getInt(col1);

                col1 = cPlayer.getColumnIndex("hold_rating");
                thisPlayer.hold_rating = cPlayer.getInt(col1);

                // arm_rating, defense_rating, fld_range, fld_error
                for(int j = 0; j < 4; j++) {
                    col1 = cPlayer.getColumnIndex(defense[j]);
                    thisPlayer.defense[j] = cPlayer.getInt(col1);
                }

                // vsl_rating, vsr_rating
                col1 = cPlayer.getColumnIndex("vsl_rating");
                thisPlayer.vsl_rating = cPlayer.getInt(col1);

                col1 = cPlayer.getColumnIndex("vsr_rating");
                thisPlayer.vsr_rating = cPlayer.getInt(col1);

                vTeam.addPlayerToLineup(thisPlayer);
            }
        }

        cPlayer.close();

        // *******************
        // BENCH
        // *******************
        getVisBench();

        for(int i = 0; i < vTeam.benchID.length; i++) {
            if(vTeam.benchID[i] != 0) {
                cPlayer = myDB.query("players", null, "_id='" + vTeam.benchID[i] + "'", null, null, null, null);

                if (cPlayer.moveToFirst()) {
                    Player thisPlayer = new Player();

                    int col1 = cPlayer.getColumnIndex("first_name");
                    int col2 = cPlayer.getColumnIndex("last_name");

                    thisPlayer.name = cPlayer.getString(col1) + " " + cPlayer.getString(col2);

                    col1 = cPlayer.getColumnIndex("pos");
                    thisPlayer.pos = cPlayer.getString(col1);

                    col1 = cPlayer.getColumnIndex("bats");
                    thisPlayer.pBats = cPlayer.getString(col1);

                    col1 = cPlayer.getColumnIndex("throws");
                    thisPlayer.pThrows = cPlayer.getString(col1);

                    // vsl ratings
                    for (int j = 0; j < 7; j++) {
                        col1 = cPlayer.getColumnIndex(sVsl[j]);
                        thisPlayer.pVsl[j] = cPlayer.getInt(col1);
                    }

                    // vsr ratings
                    for (int j = 0; j < 7; j++) {
                        col1 = cPlayer.getColumnIndex(sVsr[j]);
                        thisPlayer.pVsr[j] = cPlayer.getInt(col1);
                    }

                    // sac bunt
                    col1 = cPlayer.getColumnIndex("sac_bunt");
                    thisPlayer.sac_bunt = cPlayer.getInt(col1);

                    // spray chart
                    for (int j = 0; j < 3; j++) {
                        col1 = cPlayer.getColumnIndex(sprayChart[j]);
                        thisPlayer.sprayChart[j] = cPlayer.getInt(col1);
                    }

                    // ball speed
                    for (int j = 0; j < 3; j++) {
                        col1 = cPlayer.getColumnIndex(ballSpeed[j]);
                        thisPlayer.ballSpeed[j] = cPlayer.getInt(col1);
                    }

                    // running
                    for (int j = 0; j < 2; j++) {
                        col1 = cPlayer.getColumnIndex(running[j]);
                        thisPlayer.running[j] = cPlayer.getInt(col1);
                    }

                    // avoid dp
                    col1 = cPlayer.getColumnIndex("avoid_dp");
                    thisPlayer.avoid_dp = cPlayer.getInt(col1);

                    col1 = cPlayer.getColumnIndex("hold_rating");
                    thisPlayer.hold_rating = cPlayer.getInt(col1);

                    col1 = cPlayer.getColumnIndex("rsb");
                    thisPlayer.rsb = cPlayer.getInt(col1);

                    col1 = cPlayer.getColumnIndex("hold_rating");
                    thisPlayer.hold_rating = cPlayer.getInt(col1);

                    // arm_rating, defense_rating, fld_range, fld_error
                    for (int j = 0; j < 4; j++) {
                        col1 = cPlayer.getColumnIndex(defense[j]);
                        thisPlayer.defense[j] = cPlayer.getInt(col1);
                    }

                    // vsl_rating, vsr_rating
                    col1 = cPlayer.getColumnIndex("vsl_rating");
                    thisPlayer.vsl_rating = cPlayer.getInt(col1);

                    col1 = cPlayer.getColumnIndex("vsr_rating");
                    thisPlayer.vsr_rating = cPlayer.getInt(col1);

                    vTeam.addPlayerToBench(thisPlayer);
                }
            }
        }

        cPlayer.close();

        // *******************
        // BUPPLEN
        // *******************
        getVisBullpen();

        for(int i = 0; i < 8; i++) {
            cPlayer = myDB.query("players", null, "_id='" + vTeam.bullpenID[i] + "'", null, null, null, null);

            if(cPlayer.moveToFirst()) {
                Player thisPlayer = new Player();

                int col1 = cPlayer.getColumnIndex("first_name");
                int col2 = cPlayer.getColumnIndex("last_name");

                thisPlayer.name = cPlayer.getString(col1) + " " + cPlayer.getString(col2);

                col1 = cPlayer.getColumnIndex("pos");
                thisPlayer.pos = cPlayer.getString(col1);

                col1 = cPlayer.getColumnIndex("bats");
                thisPlayer.pBats = cPlayer.getString(col1);

                col1 = cPlayer.getColumnIndex("throws");
                thisPlayer.pThrows = cPlayer.getString(col1);

                // vsl ratings
                for(int j = 0; j < 7; j++) {
                    col1 = cPlayer.getColumnIndex(sVsl[j]);
                    thisPlayer.pVsl[j] = cPlayer.getInt(col1);
                }

                // vsr ratings
                for(int j = 0; j < 7; j++) {
                    col1 = cPlayer.getColumnIndex(sVsr[j]);
                    thisPlayer.pVsr[j] = cPlayer.getInt(col1);
                }

                // sac bunt
                col1 = cPlayer.getColumnIndex("sac_bunt");
                thisPlayer.sac_bunt = cPlayer.getInt(col1);

                // spray chart
                for(int j = 0; j < 3; j++) {
                    col1 = cPlayer.getColumnIndex(sprayChart[j]);
                    thisPlayer.sprayChart[j] = cPlayer.getInt(col1);
                }

                // ball speed
                for(int j = 0; j < 3; j++) {
                    col1 = cPlayer.getColumnIndex(ballSpeed[j]);
                    thisPlayer.ballSpeed[j] = cPlayer.getInt(col1);
                }

                // running
                for(int j = 0; j < 2; j++) {
                    col1 = cPlayer.getColumnIndex(running[j]);
                    thisPlayer.running[j] = cPlayer.getInt(col1);
                }

                // avoid dp
                col1 = cPlayer.getColumnIndex("avoid_dp");
                thisPlayer.avoid_dp = cPlayer.getInt(col1);

                col1 = cPlayer.getColumnIndex("hold_rating");
                thisPlayer.hold_rating = cPlayer.getInt(col1);

                col1 = cPlayer.getColumnIndex("rsb");
                thisPlayer.rsb = cPlayer.getInt(col1);

                col1 = cPlayer.getColumnIndex("hold_rating");
                thisPlayer.hold_rating = cPlayer.getInt(col1);

                // arm_rating, defense_rating, fld_range, fld_error
                for(int j = 0; j < 4; j++) {
                    col1 = cPlayer.getColumnIndex(defense[j]);
                    thisPlayer.defense[j] = cPlayer.getInt(col1);
                }

                // vsl_rating, vsr_rating
                col1 = cPlayer.getColumnIndex("vsl_rating");
                thisPlayer.vsl_rating = cPlayer.getInt(col1);

                col1 = cPlayer.getColumnIndex("vsr_rating");
                thisPlayer.vsr_rating = cPlayer.getInt(col1);

                vTeam.addPlayerToBullpen(thisPlayer);
            }
        }

        cPlayer.close();

        // *******************
        // STARTERS
        // *******************
        getVisStarters();
/*
        for(int i = 0; i < 5; i++) {
            cPlayer = myDB.query("players", null, "_id='" + vTeam.startersID[i] + "'", null, null, null, null);

            if(cPlayer.moveToFirst()) {
                Player thisPlayer = new Player();

                int col1 = cPlayer.getColumnIndex("first_name");
                int col2 = cPlayer.getColumnIndex("last_name");

                thisPlayer.name = cPlayer.getString(col1) + " " + cPlayer.getString(col2);

                col1 = cPlayer.getColumnIndex("pos");
                thisPlayer.pos = cPlayer.getString(col1);

                col1 = cPlayer.getColumnIndex("bats");
                thisPlayer.pBats = cPlayer.getString(col1);

                col1 = cPlayer.getColumnIndex("throws");
                thisPlayer.pThrows = cPlayer.getString(col1);

                // vsl ratings
                for(int j = 0; j < 7; j++) {
                    col1 = cPlayer.getColumnIndex(sVsl[j]);
                    thisPlayer.pVsl[j] = cPlayer.getInt(col1);
                }

                // vsr ratings
                for(int j = 0; j < 7; j++) {
                    col1 = cPlayer.getColumnIndex(sVsr[j]);
                    thisPlayer.pVsr[j] = cPlayer.getInt(col1);
                }

                // sac bunt
                col1 = cPlayer.getColumnIndex("sac_bunt");
                thisPlayer.sac_bunt = cPlayer.getInt(col1);

                // spray chart
                for(int j = 0; j < 3; j++) {
                    col1 = cPlayer.getColumnIndex(sprayChart[j]);
                    thisPlayer.sprayChart[j] = cPlayer.getInt(col1);
                }

                // ball speed
                for(int j = 0; j < 3; j++) {
                    col1 = cPlayer.getColumnIndex(ballSpeed[j]);
                    thisPlayer.ballSpeed[j] = cPlayer.getInt(col1);
                }

                // running
                for(int j = 0; j < 2; j++) {
                    col1 = cPlayer.getColumnIndex(running[j]);
                    thisPlayer.running[j] = cPlayer.getInt(col1);
                }

                // avoid dp
                col1 = cPlayer.getColumnIndex("avoid_dp");
                thisPlayer.avoid_dp = cPlayer.getInt(col1);

                col1 = cPlayer.getColumnIndex("hold_rating");
                thisPlayer.hold_rating = cPlayer.getInt(col1);

                col1 = cPlayer.getColumnIndex("rsb");
                thisPlayer.rsb = cPlayer.getInt(col1);

                col1 = cPlayer.getColumnIndex("hold_rating");
                thisPlayer.hold_rating = cPlayer.getInt(col1);

                // arm_rating, defense_rating, fld_range, fld_error
                for(int j = 0; j < 4; j++) {
                    col1 = cPlayer.getColumnIndex(defense[j]);
                    thisPlayer.defense[j] = cPlayer.getInt(col1);
                }

                // vsl_rating, vsr_rating
                col1 = cPlayer.getColumnIndex("vsl_rating");
                thisPlayer.vsl_rating = cPlayer.getInt(col1);

                col1 = cPlayer.getColumnIndex("vsr_rating");
                thisPlayer.vsr_rating = cPlayer.getInt(col1);

                vTeam.addPlayerToStarters(thisPlayer);
            }
        }
        */
        myDB.close();
    }

    private void buildHomeTeam(String seasonName, int teamID) {
        hTeam = new Team();
        String[] sVsl = {"vsl_1b_game", "vsl_2b_game", "vsl_3b_game", "vsl_hr_game", "vsl_bb_game", "vsl_so_game", "vsl_hbp_game"};
        String[] sVsr = {"vsr_1b_game", "vsr_2b_game", "vsr_3b_game", "vsr_hr_game", "vsr_bb_game", "vsr_so_game", "vsr_hbp_game"};
        String[] sprayChart = {"pull", "center", "oppo"};
        String[] ballSpeed = {"soft", "med", "hard"};
        String[] running = {"baserunning", "stealing"};
        String[] defense = {"arm_rating", "defense_rating", "fld_range", "fld_error"};

        myDB = openOrCreateDatabase(seasonName, MODE_PRIVATE, null);

        // *********
        // TEAMNAME
        // *********
        Cursor cTeam = myDB.query("teams", null, "_id='" + teamID + "'", null, null, null, null);

        if(cTeam.moveToFirst()) {
            int col = cTeam.getColumnIndex("team_name");
            hTeam.name = cTeam.getString(col);
        }

        cTeam.close();

        // *******
        // LINEUP
        // *******
        getHomeLineups();

        Cursor cPlayer = null;

        for(int i = 0; i < 9; i++) {
            cPlayer = myDB.query("players", null, "_id='" + hTeam.lineupID[i] + "'", null, null, null, null);

            if(cPlayer.moveToFirst()) {
                Player thisPlayer = new Player();

                int col1 = cPlayer.getColumnIndex("first_name");
                int col2 = cPlayer.getColumnIndex("last_name");

                thisPlayer.name = cPlayer.getString(col1) + " " + cPlayer.getString(col2);

                col1 = cPlayer.getColumnIndex("pos");
                thisPlayer.pos = cPlayer.getString(col1);

                col1 = cPlayer.getColumnIndex("bats");
                thisPlayer.pBats = cPlayer.getString(col1);

                col1 = cPlayer.getColumnIndex("throws");
                thisPlayer.pThrows = cPlayer.getString(col1);

                // vsl ratings
                for(int j = 0; j < 7; j++) {
                    col1 = cPlayer.getColumnIndex(sVsl[j]);
                    thisPlayer.pVsl[j] = cPlayer.getInt(col1);
                }

                // vsr ratings
                for(int j = 0; j < 7; j++) {
                    col1 = cPlayer.getColumnIndex(sVsr[j]);
                    thisPlayer.pVsr[j] = cPlayer.getInt(col1);
                }

                // sac bunt
                col1 = cPlayer.getColumnIndex("sac_bunt");
                thisPlayer.sac_bunt = cPlayer.getInt(col1);

                // spray chart
                for(int j = 0; j < 3; j++) {
                    col1 = cPlayer.getColumnIndex(sprayChart[j]);
                    thisPlayer.sprayChart[j] = cPlayer.getInt(col1);
                }

                // ball speed
                for(int j = 0; j < 3; j++) {
                    col1 = cPlayer.getColumnIndex(ballSpeed[j]);
                    thisPlayer.ballSpeed[j] = cPlayer.getInt(col1);
                }

                // running
                for(int j = 0; j < 2; j++) {
                    col1 = cPlayer.getColumnIndex(running[j]);
                    thisPlayer.running[j] = cPlayer.getInt(col1);
                }

                // avoid dp
                col1 = cPlayer.getColumnIndex("avoid_dp");
                thisPlayer.avoid_dp = cPlayer.getInt(col1);

                col1 = cPlayer.getColumnIndex("hold_rating");
                thisPlayer.hold_rating = cPlayer.getInt(col1);

                col1 = cPlayer.getColumnIndex("rsb");
                thisPlayer.rsb = cPlayer.getInt(col1);

                col1 = cPlayer.getColumnIndex("hold_rating");
                thisPlayer.hold_rating = cPlayer.getInt(col1);

                // arm_rating, defense_rating, fld_range, fld_error
                for(int j = 0; j < 4; j++) {
                    col1 = cPlayer.getColumnIndex(defense[j]);
                    thisPlayer.defense[j] = cPlayer.getInt(col1);
                }

                // vsl_rating, vsr_rating
                col1 = cPlayer.getColumnIndex("vsl_rating");
                thisPlayer.vsl_rating = cPlayer.getInt(col1);

                col1 = cPlayer.getColumnIndex("vsr_rating");
                thisPlayer.vsr_rating = cPlayer.getInt(col1);

                hTeam.addPlayerToLineup(thisPlayer);
            }
        }

        cPlayer.close();

        // *******************
        // BENCH
        // *******************
        getHomeBench();

        for(int i = 0; i < 6; i++) {
            if(hTeam.benchID[i] != 0) {
                cPlayer = myDB.query("players", null, "_id='" + hTeam.benchID[i] + "'", null, null, null, null);

                if (cPlayer.moveToFirst()) {
                    Player thisPlayer = new Player();

                    int col1 = cPlayer.getColumnIndex("first_name");
                    int col2 = cPlayer.getColumnIndex("last_name");

                    thisPlayer.name = cPlayer.getString(col1) + " " + cPlayer.getString(col2);

                    col1 = cPlayer.getColumnIndex("pos");
                    thisPlayer.pos = cPlayer.getString(col1);

                    col1 = cPlayer.getColumnIndex("bats");
                    thisPlayer.pBats = cPlayer.getString(col1);

                    col1 = cPlayer.getColumnIndex("throws");
                    thisPlayer.pThrows = cPlayer.getString(col1);

                    // vsl ratings
                    for (int j = 0; j < 7; j++) {
                        col1 = cPlayer.getColumnIndex(sVsl[j]);
                        thisPlayer.pVsl[j] = cPlayer.getInt(col1);
                    }

                    // vsr ratings
                    for (int j = 0; j < 7; j++) {
                        col1 = cPlayer.getColumnIndex(sVsr[j]);
                        thisPlayer.pVsr[j] = cPlayer.getInt(col1);
                    }

                    // sac bunt
                    col1 = cPlayer.getColumnIndex("sac_bunt");
                    thisPlayer.sac_bunt = cPlayer.getInt(col1);

                    // spray chart
                    for (int j = 0; j < 3; j++) {
                        col1 = cPlayer.getColumnIndex(sprayChart[j]);
                        thisPlayer.sprayChart[j] = cPlayer.getInt(col1);
                    }

                    // ball speed
                    for (int j = 0; j < 3; j++) {
                        col1 = cPlayer.getColumnIndex(ballSpeed[j]);
                        thisPlayer.ballSpeed[j] = cPlayer.getInt(col1);
                    }

                    // running
                    for (int j = 0; j < 2; j++) {
                        col1 = cPlayer.getColumnIndex(running[j]);
                        thisPlayer.running[j] = cPlayer.getInt(col1);
                    }

                    // avoid dp
                    col1 = cPlayer.getColumnIndex("avoid_dp");
                    thisPlayer.avoid_dp = cPlayer.getInt(col1);

                    col1 = cPlayer.getColumnIndex("hold_rating");
                    thisPlayer.hold_rating = cPlayer.getInt(col1);

                    col1 = cPlayer.getColumnIndex("rsb");
                    thisPlayer.rsb = cPlayer.getInt(col1);

                    col1 = cPlayer.getColumnIndex("hold_rating");
                    thisPlayer.hold_rating = cPlayer.getInt(col1);

                    // arm_rating, defense_rating, fld_range, fld_error
                    for (int j = 0; j < 4; j++) {
                        col1 = cPlayer.getColumnIndex(defense[j]);
                        thisPlayer.defense[j] = cPlayer.getInt(col1);
                    }

                    // vsl_rating, vsr_rating
                    col1 = cPlayer.getColumnIndex("vsl_rating");
                    thisPlayer.vsl_rating = cPlayer.getInt(col1);

                    col1 = cPlayer.getColumnIndex("vsr_rating");
                    thisPlayer.vsr_rating = cPlayer.getInt(col1);

                    hTeam.addPlayerToBench(thisPlayer);
                }
            }
        }

        cPlayer.close();

        // *******************
        // BUPPLEN
        // *******************
        getHomeBullpen();

        for(int i = 0; i < 8; i++) {
            cPlayer = myDB.query("players", null, "_id='" + hTeam.bullpenID[i] + "'", null, null, null, null);

            if(cPlayer.moveToFirst()) {
                Player thisPlayer = new Player();

                int col1 = cPlayer.getColumnIndex("first_name");
                int col2 = cPlayer.getColumnIndex("last_name");

                thisPlayer.name = cPlayer.getString(col1) + " " + cPlayer.getString(col2);

                col1 = cPlayer.getColumnIndex("pos");
                thisPlayer.pos = cPlayer.getString(col1);

                col1 = cPlayer.getColumnIndex("bats");
                thisPlayer.pBats = cPlayer.getString(col1);

                col1 = cPlayer.getColumnIndex("throws");
                thisPlayer.pThrows = cPlayer.getString(col1);

                // vsl ratings
                for(int j = 0; j < 7; j++) {
                    col1 = cPlayer.getColumnIndex(sVsl[j]);
                    thisPlayer.pVsl[j] = cPlayer.getInt(col1);
                }

                // vsr ratings
                for(int j = 0; j < 7; j++) {
                    col1 = cPlayer.getColumnIndex(sVsr[j]);
                    thisPlayer.pVsr[j] = cPlayer.getInt(col1);
                }

                // sac bunt
                col1 = cPlayer.getColumnIndex("sac_bunt");
                thisPlayer.sac_bunt = cPlayer.getInt(col1);

                // spray chart
                for(int j = 0; j < 3; j++) {
                    col1 = cPlayer.getColumnIndex(sprayChart[j]);
                    thisPlayer.sprayChart[j] = cPlayer.getInt(col1);
                }

                // ball speed
                for(int j = 0; j < 3; j++) {
                    col1 = cPlayer.getColumnIndex(ballSpeed[j]);
                    thisPlayer.ballSpeed[j] = cPlayer.getInt(col1);
                }

                // running
                for(int j = 0; j < 2; j++) {
                    col1 = cPlayer.getColumnIndex(running[j]);
                    thisPlayer.running[j] = cPlayer.getInt(col1);
                }

                // avoid dp
                col1 = cPlayer.getColumnIndex("avoid_dp");
                thisPlayer.avoid_dp = cPlayer.getInt(col1);

                col1 = cPlayer.getColumnIndex("hold_rating");
                thisPlayer.hold_rating = cPlayer.getInt(col1);

                col1 = cPlayer.getColumnIndex("rsb");
                thisPlayer.rsb = cPlayer.getInt(col1);

                col1 = cPlayer.getColumnIndex("hold_rating");
                thisPlayer.hold_rating = cPlayer.getInt(col1);

                // arm_rating, defense_rating, fld_range, fld_error
                for(int j = 0; j < 4; j++) {
                    col1 = cPlayer.getColumnIndex(defense[j]);
                    thisPlayer.defense[j] = cPlayer.getInt(col1);
                }

                // vsl_rating, vsr_rating
                col1 = cPlayer.getColumnIndex("vsl_rating");
                thisPlayer.vsl_rating = cPlayer.getInt(col1);

                col1 = cPlayer.getColumnIndex("vsr_rating");
                thisPlayer.vsr_rating = cPlayer.getInt(col1);

                hTeam.addPlayerToBullpen(thisPlayer);
            }
        }

        cPlayer.close();

        // *******************
        // STARTERS
        // *******************
        getHomeStarters();
/*
        for(int i = 0; i < 5; i++) {
            cPlayer = myDB.query("players", null, "_id='" + hTeam.startersID[i] + "'", null, null, null, null);

            if(cPlayer.moveToFirst()) {
                Player thisPlayer = new Player();

                int col1 = cPlayer.getColumnIndex("first_name");
                int col2 = cPlayer.getColumnIndex("last_name");

                thisPlayer.name = cPlayer.getString(col1) + " " + cPlayer.getString(col2);

                col1 = cPlayer.getColumnIndex("pos");
                thisPlayer.pos = cPlayer.getString(col1);

                col1 = cPlayer.getColumnIndex("bats");
                thisPlayer.pBats = cPlayer.getString(col1);

                col1 = cPlayer.getColumnIndex("throws");
                thisPlayer.pThrows = cPlayer.getString(col1);

                // vsl ratings
                for(int j = 0; j < 7; j++) {
                    col1 = cPlayer.getColumnIndex(sVsl[j]);
                    thisPlayer.pVsl[j] = cPlayer.getInt(col1);
                }

                // vsr ratings
                for(int j = 0; j < 7; j++) {
                    col1 = cPlayer.getColumnIndex(sVsr[j]);
                    thisPlayer.pVsr[j] = cPlayer.getInt(col1);
                }

                // sac bunt
                col1 = cPlayer.getColumnIndex("sac_bunt");
                thisPlayer.sac_bunt = cPlayer.getInt(col1);

                // spray chart
                for(int j = 0; j < 3; j++) {
                    col1 = cPlayer.getColumnIndex(sprayChart[j]);
                    thisPlayer.sprayChart[j] = cPlayer.getInt(col1);
                }

                // ball speed
                for(int j = 0; j < 3; j++) {
                    col1 = cPlayer.getColumnIndex(ballSpeed[j]);
                    thisPlayer.ballSpeed[j] = cPlayer.getInt(col1);
                }

                // running
                for(int j = 0; j < 2; j++) {
                    col1 = cPlayer.getColumnIndex(running[j]);
                    thisPlayer.running[j] = cPlayer.getInt(col1);
                }

                // avoid dp
                col1 = cPlayer.getColumnIndex("avoid_dp");
                thisPlayer.avoid_dp = cPlayer.getInt(col1);

                col1 = cPlayer.getColumnIndex("hold_rating");
                thisPlayer.hold_rating = cPlayer.getInt(col1);

                col1 = cPlayer.getColumnIndex("rsb");
                thisPlayer.rsb = cPlayer.getInt(col1);

                col1 = cPlayer.getColumnIndex("hold_rating");
                thisPlayer.hold_rating = cPlayer.getInt(col1);

                // arm_rating, defense_rating, fld_range, fld_error
                for(int j = 0; j < 4; j++) {
                    col1 = cPlayer.getColumnIndex(defense[j]);
                    thisPlayer.defense[j] = cPlayer.getInt(col1);
                }

                // vsl_rating, vsr_rating
                col1 = cPlayer.getColumnIndex("vsl_rating");
                thisPlayer.vsl_rating = cPlayer.getInt(col1);

                col1 = cPlayer.getColumnIndex("vsr_rating");
                thisPlayer.vsr_rating = cPlayer.getInt(col1);

                hTeam.addPlayerToStarters(thisPlayer);
            }
        }

        */
        myDB.close();
    }

    private void getVisLineups() {
        vTeam.lineupID = getIntent().getIntArrayExtra("visLineup");
    }

    private void getHomeLineups() {
        hTeam.lineupID = getIntent().getIntArrayExtra("homeLineup");
    }

    private void getVisBench() {
        vTeam.benchID = getIntent().getIntArrayExtra("visBench");
    }

    private void getHomeBench() {
        hTeam.benchID = getIntent().getIntArrayExtra("homeBench");
    }

    private void getVisBullpen() {
        vTeam.bullpenID = getIntent().getIntArrayExtra("visBullpen");
    }

    private void getHomeBullpen() {
        hTeam.bullpenID = getIntent().getIntArrayExtra("homeBullpen");
    }

    private void getVisStarters() {

    }

    private void getHomeStarters() {

    }

}
