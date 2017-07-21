package com.batterupbaseball;

import android.app.Activity;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

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

        game = new Game();

        initGame();

        getResultAverages();

        updateScreen();
    }

    public void roll_dice(View view) {
        int[] dieImageRed = {R.drawable.red_die_0, R.drawable.red_die_1, R.drawable.red_die_2, R.drawable.red_die_3, R.drawable.red_die_4, R.drawable.red_die_5, R.drawable.red_die_6, R.drawable.red_die_7, R.drawable.red_die_8, R.drawable.red_die_9};
        int[] dieImageWhite = {R.drawable.white_die_0, R.drawable.white_die_1, R.drawable.white_die_2, R.drawable.white_die_3, R.drawable.white_die_4, R.drawable.white_die_5, R.drawable.white_die_6, R.drawable.white_die_7, R.drawable.white_die_8, R.drawable.white_die_9};
        int[] dieImageBlue = {R.drawable.blue_die_0, R.drawable.blue_die_1, R.drawable.blue_die_2, R.drawable.blue_die_3, R.drawable.blue_die_4, R.drawable.blue_die_5, R.drawable.blue_die_6, R.drawable.blue_die_7, R.drawable.blue_die_8, R.drawable.blue_die_9};

        int dieRedResult = game.rollDie();
        int dieWhiteResult = game.rollDie();
        int dieBlueResult = game.rollDie();

        ImageView ivRedDie = (ImageView) findViewById(R.id.ivDie_1);
        ivRedDie.setImageResource(dieImageRed[dieRedResult]);

        ImageView ivWhiteDie = (ImageView) findViewById(R.id.ivDie_2);
        ivWhiteDie.setImageResource(dieImageWhite[dieWhiteResult]);

        ImageView ivBlueDie = (ImageView) findViewById(R.id.ivDie_3);
        ivBlueDie.setImageResource(dieImageBlue[dieBlueResult]);

        game.dieResult = (dieRedResult*100) + (dieWhiteResult*10) + dieBlueResult;
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

        game.vSeasonName = vSeasonName;
        game.hSeasonName = hSeasonName;

        buildVisTeam(vSeasonName, vTeamID);
        buildHomeTeam(hSeasonName, hTeamID);

        // show team names on scoreboard
        TextView tvVTeamName = (TextView) findViewById(R.id.tvVisitorTeamName);
        TextView tvHTeamName = (TextView) findViewById(R.id.tvHomeTeamName);

        tvVTeamName.setText(vTeam.name);
        tvHTeamName.setText(hTeam.name);

        listVisTeam();
        listHomeTeam();

        game.vPitcher = vTeam.starter.get(0);
        game.hPitcher = hTeam.starter.get(0);
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

        Log.d(TAG, "STARTER: " + vTeam.starter.get(0).getName());
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

        Log.d(TAG, "STARTER: " + hTeam.starter.get(0).getName());
    }

    private void buildVisTeam(String seasonName, int teamID) {
        vTeam = new Team();
        String[] sVsl = {"vsl_1b_game", "vsl_2b_game", "vsl_3b_game", "vsl_hr_game", "vsl_bb_game", "vsl_so_game", "vsl_hbp_game"};
        String[] sVsr = {"vsr_1b_game", "vsr_2b_game", "vsr_3b_game", "vsr_hr_game", "vsr_bb_game", "vsr_so_game", "vsr_hbp_game"};
        String[] sprayChart = {"pull", "center", "oppo"};
        String[] ballSpeed = {"soft", "med", "hard"};
        String[] running = {"baserunning", "stealing"};
        String[] defense = {"arm_rating", "defense_rating", "fld_range", "fld_error"};
        int stamina = 0;

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

        cPlayer = myDB.query("players", null, "_id='" + vTeam.starterID + "'", null, null, null, null);

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

        cPlayer = myDB.query("players", null, "_id='" + hTeam.starterID + "'", null, null, null, null);

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
        vTeam.starterID = getIntent().getIntExtra("visStarter", 0);
        Log.d(TAG, "VIS STARTER" + vTeam.starterID);
    }

    private void getHomeStarters() {
        hTeam.starterID = getIntent().getIntExtra("homeStarter",0);
    }

    private void updateScreen() {
        updateInning();
        highlightInning();
        updateRunsHitsErrors();
        updateOuts();
        updateRunsByInning();
        updateBatterCard();
        updateBaseRunners();
        updatePitcherCard();
        updateOnDeck();
        updateInTheHole();
        updateResult();
        updateStamina();
        updatePossibleOutcomes();
    }

    private void updateInning() {
        String sInning = "";
        String pInning = "";

        TextView tvInning = (TextView) findViewById(R.id.tvInning);

        switch(game.inning) {
            case 1:
            case 21:
            case 31:
                sInning = "st";
                break;
            case 2:
            case 22:
            case 32:
                sInning = "nd";
                break;
            case 3:
            case 23:
            case 33:
                sInning="rd";
                break;
            default:
                sInning = "th";
        }

        if(game.teamAtBat==0)
            pInning = "Top of the ";
        else
            pInning = "Bottom of the ";

        tvInning.setText(pInning + game.inning + sInning);
    }

    private void highlightInning() {
        int[] vInning = {R.id.tvVScore1, R.id.tvVScore2, R.id.tvVScore3, R.id.tvVScore4, R.id.tvVScore5, R.id.tvVScore6, R.id.tvVScore7, R.id.tvVScore8, R.id.tvVScore9, R.id.tvVScoreX};
        int[] hInning = {R.id.tvHScore1, R.id.tvHScore2, R.id.tvHScore3, R.id.tvHScore4, R.id.tvHScore5, R.id.tvHScore6, R.id.tvHScore7, R.id.tvHScore8, R.id.tvHScore9, R.id.tvHScoreX};

        // clear all innings
        for(int i=0; i < 10; i++) {
            TextView tvInning1 = (TextView) findViewById(vInning[i]);
            tvInning1.setBackgroundColor(Color.GRAY);

            TextView tvInning2 = (TextView) findViewById(hInning[i]);
            tvInning2.setBackgroundColor(Color.GRAY);
        }

        TextView tvInning;

        // set inning to highlight
        if(game.teamAtBat==0) {
            if(game.inning > 9) {
                // extra innings
                tvInning = (TextView) findViewById(R.id.tvVScoreX);
            }
            else {
                // during the first 9 innings
                tvInning = (TextView) findViewById(vInning[game.inning-1]);
            }
        }
        else {
            if(game.inning > 9) {
                // extra innings
                tvInning = (TextView) findViewById(R.id.tvHScoreX);
            }
            else {
                // during the first 9 innings
                tvInning = (TextView) findViewById(hInning[game.inning-1]);
            }
        }

        tvInning.setBackgroundColor(Color.WHITE);
    }

    private void updateRunsHitsErrors() {
        TextView tvVRuns = (TextView) findViewById(R.id.tvVRuns);
        TextView tvHRuns = (TextView) findViewById(R.id.tvHRuns);

        TextView tvVHits = (TextView) findViewById(R.id.tvVHits);
        TextView tvHHits = (TextView) findViewById(R.id.tvHHits);

        TextView tvVErrors = (TextView) findViewById(R.id.tvVErrors);
        TextView tvHErrors = (TextView) findViewById(R.id.tvHErrors);

        tvVRuns.setText("" + game.vRuns);
        tvHRuns.setText("" + game.hRuns);

        tvVHits.setText("" + game.vHits);
        tvHHits.setText("" + game.hHits);

        tvVErrors.setText("" + game.vErrors);
        tvHErrors.setText("" + game.hErrors);
    }

    private void updateOuts() {
        TextView tvOuts = (TextView) findViewById(R.id.tvOuts);
        tvOuts.setText("" + game.outs);
    }

    private void updateRunsByInning() {
        int[] vInning = {R.id.tvVScore1, R.id.tvVScore2, R.id.tvVScore3, R.id.tvVScore4, R.id.tvVScore5, R.id.tvVScore6, R.id.tvVScore7, R.id.tvVScore8, R.id.tvVScore9, R.id.tvVScoreX};
        int[] hInning = {R.id.tvHScore1, R.id.tvHScore2, R.id.tvHScore3, R.id.tvHScore4, R.id.tvHScore5, R.id.tvHScore6, R.id.tvHScore7, R.id.tvHScore8, R.id.tvHScore9, R.id.tvHScoreX};

        TextView tvInning;

        for(int i=0; i < game.inning; i++) {
            tvInning = (TextView) findViewById(vInning[i]);
            tvInning.setText("" + game.vScoreByInning[i]);

            if(game.teamAtBat==1) {
                tvInning = (TextView) findViewById(hInning[i]);
                tvInning.setText("" + game.hScoreByInning[i]);
            }
        }
    }

    private void updateBatterCard() {
        int[] cardValue = {R.drawable.value_1, R.drawable.value_2, R.drawable.value_3, R.drawable.value_4, R.drawable.value_5, R.drawable.value_6, R.drawable.value_7, R.drawable.value_8, R.drawable.value_9, R.drawable.value_10};
        int[] cardDefense = {R.drawable.defense_1, R.drawable.defense_2, R.drawable.defense_3, R.drawable.defense_4, R.drawable.defense_5, R.drawable.defense_6, R.drawable.defense_7, R.drawable.defense_8, R.drawable.defense_9, R.drawable.defense_10};
        int[] cardContact = {R.drawable.contact_1, R.drawable.contact_2, R.drawable.contact_3, R.drawable.contact_4, R.drawable.contact_5, R.drawable.contact_6, R.drawable.contact_7, R.drawable.contact_8, R.drawable.contact_9, R.drawable.contact_10};
        int[] cardPos = {R.drawable.pos_sp, R.drawable.pos_rp, R.drawable.pos_c, R.drawable.pos_1b, R.drawable.pos_2b, R.drawable.pos_3b, R.drawable.pos_ss, R.drawable.pos_lf, R.drawable.pos_cf, R.drawable.pos_rf};

        ImageView ivCard = (ImageView) findViewById(R.id.ivBatterCard);

        Drawable[] layers = new Drawable[5];

        // batter bats right or left? get correct bg image
        if(game.teamAtBat==0) {
            if(vTeam.lineup.get(game.vBatter).getBats().equals("r")) {
                if(game.hPitcher.getThrows().equals("r")) {
                    // base
                    layers[0] = ContextCompat.getDrawable(this, R.drawable.card_vsr_as_right);

                    // contact
                    Log.d(TAG, "VSR RATING: " + vTeam.lineup.get(game.vBatter).getRatings(0));
                    layers[2] = ContextCompat.getDrawable(this, cardContact[vTeam.lineup.get(game.vBatter).getRatings(0)-1]);
                }
                else {

                    // contact
                    Log.d(TAG, "VSL RATING: " + vTeam.lineup.get(game.vBatter).getRatings(1));
                    layers[2] = ContextCompat.getDrawable(this, cardContact[vTeam.lineup.get(game.vBatter).getRatings(1)-1]);

                    layers[0] = ContextCompat.getDrawable(this, R.drawable.card_vsl_as_right);
                }
            }
            else if(vTeam.lineup.get(game.vBatter).getBats().equals("s")) {
                if(game.hPitcher.getThrows().equals("r")) {
                    // base
                    layers[0] = ContextCompat.getDrawable(this, R.drawable.card_vsr_as_left);
                }
                else {
                    layers[0] = ContextCompat.getDrawable(this, R.drawable.card_vsl_as_right);
                }

                // contact
                layers[2] = ContextCompat.getDrawable(this, cardContact[vTeam.lineup.get(game.vBatter).getRatings(0)]);

                int i = vTeam.lineup.get(game.vBatter).getRatings(0);
            }
            else {
                if(game.hPitcher.getThrows().equals("r")) {
                    // base
                    layers[0] = ContextCompat.getDrawable(this, R.drawable.card_vsr_as_left);
                }
                else {
                    layers[0] = ContextCompat.getDrawable(this, R.drawable.card_vsl_as_left);
                }

                // contact
                layers[2] = ContextCompat.getDrawable(this, cardContact[vTeam.lineup.get(game.vBatter).getRatings(1)]);
            }

            // value
            layers[1] = ContextCompat.getDrawable(this, cardValue[vTeam.lineup.get(game.vBatter).getValue()]);

            // defense
            int[] def = vTeam.lineup.get(game.vBatter).getDefense();
            layers[3] = ContextCompat.getDrawable(this, cardDefense[def[1]-1]);

            // position
            int posNum = game.convertPos(vTeam.lineup.get(game.vBatter).getPos());
            layers[4] = ContextCompat.getDrawable(this, cardPos[posNum]);
        }
        else {
            if(hTeam.lineup.get(game.hBatter).getBats().equals("r")) {
                if(game.vPitcher.getThrows().equals("r")) {
                    // base
                    layers[0] = ContextCompat.getDrawable(this, R.drawable.card_vsr_as_right);
                }
                else {
                    layers[0] = ContextCompat.getDrawable(this, R.drawable.card_vsl_as_right);
                }

                // contact
                layers[2] = ContextCompat.getDrawable(this, cardContact[hTeam.lineup.get(game.hBatter).getRatings(0)]);
            }
            else if(hTeam.lineup.get(game.hBatter).getBats().equals("s")) {
                if(game.vPitcher.getThrows().equals("r")) {
                    // base
                    layers[0] = ContextCompat.getDrawable(this, R.drawable.card_vsr_as_left);
                }
                else {
                    layers[0] = ContextCompat.getDrawable(this, R.drawable.card_vsl_as_right);
                }

                // contact
                layers[2] = ContextCompat.getDrawable(this, cardContact[vTeam.lineup.get(game.vBatter).getRatings(0)]);
            }
            else {
                if(game.vPitcher.getThrows().equals("r")) {
                    // base
                    layers[0] = ContextCompat.getDrawable(this, R.drawable.card_vsr_as_left);
                }
                else {
                    layers[0] = ContextCompat.getDrawable(this, R.drawable.card_vsl_as_left);
                }

                // contact
                layers[2] = ContextCompat.getDrawable(this, cardContact[hTeam.lineup.get(game.hBatter).getRatings(1)]);
            }
            // value
            layers[1] = ContextCompat.getDrawable(this, cardValue[hTeam.lineup.get(game.hBatter).getValue()]);

            // defense
            int[] def = hTeam.lineup.get(game.hBatter).getDefense();
            layers[3] = ContextCompat.getDrawable(this, cardDefense[def[1]]);

            // position
            int posNum = game.convertPos(hTeam.lineup.get(game.hBatter).getPos());
            layers[4] = ContextCompat.getDrawable(this, cardPos[posNum]);
        }

        LayerDrawable layerDrawable = new LayerDrawable(layers);
        ivCard.setImageDrawable(layerDrawable);

    }

    private void updateBaseRunners() {

    }

    private void updatePitcherCard() {
        int[] cardValue = {R.drawable.value_1, R.drawable.value_2, R.drawable.value_3, R.drawable.value_4, R.drawable.value_5, R.drawable.value_6, R.drawable.value_7, R.drawable.value_8, R.drawable.value_9, R.drawable.value_10};
        int[] cardDefense = {R.drawable.defense_1, R.drawable.defense_2, R.drawable.defense_3, R.drawable.defense_4, R.drawable.defense_5, R.drawable.defense_6, R.drawable.defense_7, R.drawable.defense_8, R.drawable.defense_9, R.drawable.defense_10};
        int[] cardContact = {R.drawable.contact_1, R.drawable.contact_2, R.drawable.contact_3, R.drawable.contact_4, R.drawable.contact_5, R.drawable.contact_6, R.drawable.contact_7, R.drawable.contact_8, R.drawable.contact_9, R.drawable.contact_10};
        int[] cardPos = {R.drawable.pos_sp, R.drawable.pos_rp, R.drawable.pos_c, R.drawable.pos_1b, R.drawable.pos_2b, R.drawable.pos_3b, R.drawable.pos_ss, R.drawable.pos_lf, R.drawable.pos_cf, R.drawable.pos_rf};

        ImageView ivCard = (ImageView) findViewById(R.id.ivPitcherCard);

        Drawable[] layers = new Drawable[5];

        // batter bats right or left? get correct bg image
        if(game.teamAtBat==0) {
            if(game.hPitcher.getThrows().equals("r")) {
                if(vTeam.lineup.get(game.vBatter).getBats().equals("r")) {
                    // base
                    layers[0] = ContextCompat.getDrawable(this, R.drawable.pitcher_card_vsr_as_right);
                }
                else {
                    layers[0] = ContextCompat.getDrawable(this, R.drawable.pitcher_card_vsl_as_right);
                }

                // contact
                layers[2] = ContextCompat.getDrawable(this, cardContact[game.hPitcher.getRatings(0)-1]);
            }
            else {
                if(vTeam.lineup.get(game.vBatter).getBats().equals("r")) {
                    // base
                    layers[0] = ContextCompat.getDrawable(this, R.drawable.pitcher_card_vsr_as_left);
                }
                else if(vTeam.lineup.get(game.vBatter).getBats().equals("s")) {
                    // base
                    layers[0] = ContextCompat.getDrawable(this, R.drawable.pitcher_card_vsr_as_left);
                }
                else {
                    layers[0] = ContextCompat.getDrawable(this, R.drawable.pitcher_card_vsl_as_left);
                }

                // contact
                layers[2] = ContextCompat.getDrawable(this, cardContact[game.hPitcher.getRatings(1)-1]);
            }

            // value
            layers[1] = ContextCompat.getDrawable(this, cardValue[game.hPitcher.getValue()]);

            // defense
            int[] def = game.hPitcher.getDefense();
            layers[3] = ContextCompat.getDrawable(this, cardDefense[def[1]-1]);

            // position
            int posNum = game.convertPos(game.hPitcher.getPos());
            layers[4] = ContextCompat.getDrawable(this, cardPos[posNum]);
        }
        else {
            if(game.vPitcher.getThrows().equals("r")) {
                if(hTeam.lineup.get(game.hBatter).getBats().equals("r")) {
                    // base
                    layers[0] = ContextCompat.getDrawable(this, R.drawable.pitcher_card_vsr_as_right);
                }
                else {
                    layers[0] = ContextCompat.getDrawable(this, R.drawable.pitcher_card_vsl_as_right);
                }

                // contact
                layers[2] = ContextCompat.getDrawable(this, cardContact[game.vPitcher.getRatings(0)-1]);
            }
            else {
                if(hTeam.lineup.get(game.hBatter).getBats().equals("r")) {
                    // base
                    layers[0] = ContextCompat.getDrawable(this, R.drawable.pitcher_card_vsr_as_left);
                }
                else if(vTeam.lineup.get(game.vBatter).getBats().equals("s")) {
                    // base
                    layers[0] = ContextCompat.getDrawable(this, R.drawable.pitcher_card_vsr_as_left);
                }
                else {
                    layers[0] = ContextCompat.getDrawable(this, R.drawable.pitcher_card_vsl_as_left);
                }

                // contact
                layers[2] = ContextCompat.getDrawable(this, cardContact[game.vPitcher.getRatings(1)-1]);
            }

            // value
            layers[1] = ContextCompat.getDrawable(this, cardValue[hTeam.lineup.get(game.hBatter).getValue()]);

            // defense
            int[] def = game.vPitcher.getDefense();
            layers[3] = ContextCompat.getDrawable(this, cardDefense[def[1]-1]);

            // position
            int posNum = game.convertPos(game.vPitcher.getPos());
            layers[4] = ContextCompat.getDrawable(this, cardPos[posNum]);
        }

        LayerDrawable layerDrawable = new LayerDrawable(layers);
        ivCard.setImageDrawable(layerDrawable);

    }

    private void updateOnDeck() {
        TextView tvOnDeckBats = (TextView) findViewById(R.id.tvOnDeckBats);
        TextView tvOnDeckName = (TextView) findViewById(R.id.tvOnDeckPlayerName);

        String sBats = "";
        String sName = "";

        if(game.teamAtBat==0) {
            if(game.vBatter == 9) {
                sBats = vTeam.lineup.get(0).getBats();
                sName = vTeam.lineup.get(0).getName();
            }
            else {
                sBats = vTeam.lineup.get(game.vBatter + 1).getBats();
                sName = vTeam.lineup.get(game.vBatter + 1).getName();
            }
        }
        else {
            if(game.hBatter == 9) {
                sBats = hTeam.lineup.get(0).getBats();
                sName = hTeam.lineup.get(0).getName();
            }
            else {
                sBats = hTeam.lineup.get(game.hBatter + 1).getBats();
                sName = hTeam.lineup.get(game.hBatter + 1).getName();
            }
        }

        tvOnDeckBats.setText(sBats);
        tvOnDeckName.setText(sName);
    }

    private void updateInTheHole() {
        TextView tvInTheHoleBats = (TextView) findViewById(R.id.tvInTheHoleBats);
        TextView tvInTheHoleName = (TextView) findViewById(R.id.tvInTheHolePlayerName);

        String sBats = "";
        String sName = "";

        if(game.teamAtBat==0) {
            if(game.vBatter == 8) {
                sBats = vTeam.lineup.get(0).getBats();
                sName = vTeam.lineup.get(0).getName();
            }
            else if(game.vBatter == 9) {
                sBats = vTeam.lineup.get(1).getBats();
                sName = vTeam.lineup.get(1).getName();
            }
            else {
                sBats = vTeam.lineup.get(game.vBatter + 2).getBats();
                sName = vTeam.lineup.get(game.vBatter + 2).getName();
            }
        }
        else {
            if(game.hBatter == 8) {
                sBats = hTeam.lineup.get(0).getBats();
                sName = hTeam.lineup.get(0).getName();
            }
            else if(game.hBatter == 9) {
                sBats = vTeam.lineup.get(1).getBats();
                sName = vTeam.lineup.get(1).getName();
            }
            else {
                sBats = hTeam.lineup.get(game.hBatter + 2).getBats();
                sName = hTeam.lineup.get(game.hBatter + 2).getName();
            }
        }

        tvInTheHoleBats.setText(sBats);
        tvInTheHoleName.setText(sName);
    }

    private void updateResult() {

    }

    private void updateStamina() {
        ProgressBar pb = (ProgressBar) findViewById(R.id.pbStamina);

        int iStamina = 0; // progress
        int pStamina = 0; // pitcher
        int gStamina = 0; // game

        if(game.teamAtBat==0) {
            pStamina = game.hPitcher.getStamina();
            gStamina = game.hStamina;
        }
        else {
            pStamina = game.vPitcher.getStamina();
            gStamina = game.vStamina;
        }

        if(pStamina==0) {
            pStamina = 1;
        }
        iStamina = ((gStamina / pStamina) * 100);

        pb.setProgress(iStamina);
    }

    private void updatePossibleOutcomes(){
        joinRanges();

        game.minOutcome[0] = 1;
        game.maxOutcome[0] = game.resultRange[0];

        int[] tvPossibleOutcomes = {R.id.tvResult_1b, R.id.tvResult_2b, R.id.tvResult_3b, R.id.tvResult_hr, R.id.tvResult_bb, R.id.tvResult_k, R.id.tvResult_hbp, R.id.tvResult_glove, R.id.tvResult_out};

        for(int i=0; i < 7; i++) {
            TextView tvOutcome = (TextView) findViewById(tvPossibleOutcomes[i]);

            if(game.minOutcome[i] == game.maxOutcome[i]) {
                tvOutcome.setText("" + game.minOutcome[i]);
            }
            else if(game.minOutcome[i] > game.maxOutcome[i]) {
                tvOutcome.setText("");
            }
            else {
                tvOutcome.setText("" + game.minOutcome[i] + "-" + game.maxOutcome[i]);
            }
            
            if((i+1)<7) {
                game.minOutcome[i+1] = game.maxOutcome[i] + 1;
                game.maxOutcome[i+1] = game.resultRange[i+1] + game.maxOutcome[i];
            }
        }

        // glove
        game.minOutcome[7] = game.maxOutcome[6] + 1;
        game.maxOutcome[7] = game.maxOutcome[6] + 140;

        TextView tvGlove = (TextView) findViewById(tvPossibleOutcomes[7]);
        tvGlove.setText("" + game.minOutcome[7] + "-" + game.maxOutcome[7]);

        // outs
        game.minOutcome[8] = game.maxOutcome[7] + 1;
        game.maxOutcome[8] = 1000;

        TextView tvOuts = (TextView) findViewById(tvPossibleOutcomes[8]);
        tvOuts.setText("" + game.minOutcome[8] + "-" + game.maxOutcome[8]);

    }

    private void joinRanges() {
        game.minOutcome[0] = 1;

        for(int i=0; i < 7; i++) {
            if(game.teamAtBat==0) {
                if (game.hPitcher.getThrows().equals("r")) {
                    if(vTeam.lineup.get(game.vBatter).getBats().equals("r")) {
                        game.resultRange[i] = calculateNewRange(vTeam.lineup.get(game.vBatter).pVsr[i], game.hPitcher.pVsr[i], i);
                        Log.d(TAG, "resultRange[" + i + "]: " + game.resultRange[i]);
                    }
                    else {
                        game.resultRange[i] = calculateNewRange(vTeam.lineup.get(game.vBatter).pVsr[i], game.hPitcher.pVsl[i], i);
                        Log.d(TAG, "resultRange[" + i + "]: " + game.resultRange[i]);
                    }
                }
                else {
                    if(vTeam.lineup.get(game.vBatter).getBats().equals("l")) {
                        game.resultRange[i] = calculateNewRange(vTeam.lineup.get(game.vBatter).pVsl[i], game.hPitcher.pVsl[i], i);
                        Log.d(TAG, "resultRange[" + i + "]: " + game.resultRange[i]);
                    }
                    else {
                        game.resultRange[i] = calculateNewRange(vTeam.lineup.get(game.vBatter).pVsl[i], game.hPitcher.pVsr[i], i);
                        Log.d(TAG, "resultRange[" + i + "]: " + game.resultRange[i]);
                    }
                }
            }
            else {
                if (game.vPitcher.getThrows().equals("r")) {
                    if(hTeam.lineup.get(game.hBatter).getBats().equals("r")) {
                        game.resultRange[i] = calculateNewRange(hTeam.lineup.get(game.hBatter).pVsr[i], game.vPitcher.pVsr[i], i);
                        Log.d(TAG, "resultRange[" + i + "]: " + game.resultRange[i]);
                    }
                    else {
                        game.resultRange[i] = calculateNewRange(hTeam.lineup.get(game.hBatter).pVsr[i], game.vPitcher.pVsl[i], i);
                        Log.d(TAG, "resultRange[" + i + "]: " + game.resultRange[i]);
                    }
                }
                else {
                    if(hTeam.lineup.get(game.hBatter).getBats().equals("l")) {
                        game.resultRange[i] = calculateNewRange(hTeam.lineup.get(game.hBatter).pVsl[i], game.vPitcher.pVsl[i], i);
                        Log.d(TAG, "resultRange[" + i + "]: " + game.resultRange[i]);
                    }
                    else {
                        game.resultRange[i] = calculateNewRange(hTeam.lineup.get(game.hBatter).pVsl[i], game.vPitcher.pVsr[i], i);
                        Log.d(TAG, "resultRange[" + i + "]: " + game.resultRange[i]);
                    }
                }
            }
        }
    }

    private int calculateNewRange(double b, double p, int avg) {
        double newValue = 0;

        newValue = Math.ceil((p*b)/game.resultAverages[avg])/(((p*b)/game.resultAverages[avg])+(((1-p)*(1-b))/(1-game.resultAverages[avg])));

        int newRange = (int) newValue;

        return newRange;
    }

    private void getResultAverages() {
        myDB = openOrCreateDatabase(game.hSeasonName, MODE_PRIVATE, null);

        Cursor cTeam = myDB.query("league_average", null, null, null, null, null, null);
        int cCur = 0;

        if (cTeam.moveToFirst()) {
            cCur = cTeam.getColumnIndex("singles");
            game.resultAverages[0] = cTeam.getInt(cCur);

            cCur = cTeam.getColumnIndex("doubles");
            game.resultAverages[1] = cTeam.getInt(cCur);

            cCur = cTeam.getColumnIndex("triples");
            game.resultAverages[2] = cTeam.getInt(cCur);

            cCur = cTeam.getColumnIndex("homeruns");
            game.resultAverages[3] = cTeam.getInt(cCur);

            cCur = cTeam.getColumnIndex("walks");
            game.resultAverages[4] = cTeam.getInt(cCur);

            cCur = cTeam.getColumnIndex("strikeouts");
            game.resultAverages[5] = cTeam.getInt(cCur);

            cCur = cTeam.getColumnIndex("hbp");
            game.resultAverages[6] = cTeam.getInt(cCur);
        }

        cTeam.close();

        myDB.close();
    }
}
