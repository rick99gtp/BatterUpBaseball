package com.batterupbaseball;

import android.app.Activity;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.drawable.ClipDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import static android.content.ContentValues.TAG;

public class playball extends Activity {
    Game game;
    Bundle bundle;
    Team vTeam, hTeam;
    SQLiteDatabase myDB;
    String teamSelected;
    int[] vLineup;
    int[] hLineup;
    int[] vDefense = new int[9];
    int[] hDefense = new int[9];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.playball);

        bundle = new Bundle();
        bundle = getIntent().getExtras();

        game = new Game();

        initGame();

        getResultAverages();

        updateScreen();

    }

    public void getBatterResult(View view) {
        int balkRating, passedBallRating, wildPitchRating;

        if(game.basesOccupied()) {
            if(game.teamAtBat == 0) {
                balkRating = game.hPitcher.balkRating;
                wildPitchRating = game.hPitcher.wildPitchRating;
                passedBallRating = hTeam.roster.get(hDefense[1]).passedBallRating;
            }
            else {
                balkRating = game.vPitcher.balkRating;
                wildPitchRating = game.vPitcher.wildPitchRating;
                passedBallRating = vTeam.roster.get(vDefense[1]).passedBallRating;
            }

            // baserunners, check for wild pitch, passed ball, and balk
            roll_dice();

            if (game.dieResult >= 1 && game.dieResult <= wildPitchRating) {
                // wild pitch
            }
            else if (game.dieResult > wildPitchRating && game.dieResult <= (wildPitchRating + balkRating)) {
                // balk
            }
            else if(game.dieResult > (wildPitchRating + balkRating) && game.dieResult <= (wildPitchRating + balkRating + passedBallRating)) {
                // passed ball
            }
            else {
                roll_dice();

                highlightOutcome();
                showResultText();
                updateScreen();
            }
        }
        else {
            roll_dice();

            highlightOutcome();
            showResultText();
            updateScreen();
        }
    }

    private void roll_dice() {
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

    private void checkEndOfInning() {
        if(game.outs < 3) {
           nextBatter();
        }
        else {
            // check for end of game
            if(checkEndOfGame()) {
                TextView tvResultText = (TextView) findViewById(R.id.tvResult);
                tvResultText.setText("Game Over");
            }
            else {
                game.outs=0;
                game.clearTheBases();
                game.nextHalfInning();
                nextBatter();
            }
        }
    }

    private boolean checkEndOfGame() {

        if(game.inning >= 9) {
            if(game.teamAtBat==0) {
                if(game.hRuns > game.vRuns)
                    // game over
                    return true;
            }
            else {
                if(game.vRuns > game.hRuns)
                    // game over
                    return true;
            }
        }

        return false;
    }

    private void nextBatter() {
        if(game.teamAtBat==0) {
            game.vLineupBatter++;

            if(game.vLineupBatter > 8) {
                game.vLineupBatter = 0;
            }

            game.vBatter = vTeam.roster.get(game.vLineupBatter);
        }
        else {
            game.hLineupBatter++;

            if(game.hLineupBatter > 8) {
                game.hLineupBatter = 0;
            }

            game.hBatter = hTeam.roster.get(game.hLineupBatter);
        }
    }

    private void updateResult(Player player) {
        switch(game.resultID) {
            case 1:
                // singles
                game.pitcher.gameP_H++;
                game.pitcher.gameP_1B++;
                game.pitcher.gameP_PA++;

                if(game.manOnThird()) {
                    moveBaseRunnerFrom(3,1);
                    player.gameRBI++;
                    game.pitcher.gameP_R++;
                }
                if(game.manOnSecond()) {
                    moveBaseRunnerFrom(2,1);
                }
                if(game.manOnFirst()) {
                    moveBaseRunnerFrom(1,1);
                }

                moveBatter(1,player);

                player.gameH++;
                player.game1B++;
                player.gamePA++;
                player.gameAB++;
                break;
            case 2:
                // doubles
                game.pitcher.gameP_H++;
                game.pitcher.gameP_2B++;
                game.pitcher.gameP_PA++;

                if(game.manOnThird()) {
                    moveBaseRunnerFrom(3,1);
                    player.gameRBI++;
                }
                if(game.manOnSecond()) {
                    moveBaseRunnerFrom(2,2);
                    player.gameRBI++;
                    game.pitcher.gameP_R++;
                }
                if(game.manOnFirst()) {
                    moveBaseRunnerFrom(1,2);
                }
                moveBatter(2,player);
                player.gameH++;
                player.game2B++;
                player.gamePA++;
                player.gameAB++;
                break;
            case 3:
                // triples
                game.pitcher.gameP_H++;
                game.pitcher.gameP_3B++;
                game.pitcher.gameP_PA++;

                if(game.manOnThird()) {
                    moveBaseRunnerFrom(3,1);
                    player.gameRBI++;
                    game.pitcher.gameP_R++;
                }
                if(game.manOnSecond()) {
                    moveBaseRunnerFrom(2,2);
                    game.pitcher.gameP_R++;
                }
                if(game.manOnFirst()) {
                    moveBaseRunnerFrom(1,3);
                    game.pitcher.gameP_R++;
                }
                moveBatter(3,player);
                player.gameH++;
                player.game3B++;
                player.gamePA++;
                player.gameAB++;
                break;
            case 4:
                // homeruns
                game.pitcher.gameP_H++;
                game.pitcher.gameP_HR++;
                game.pitcher.gameP_PA++;

                if(game.manOnThird()) {
                    moveBaseRunnerFrom(3,1);
                    player.gameRBI++;
                    game.pitcher.gameP_R++;
                }
                if(game.manOnSecond()) {
                    moveBaseRunnerFrom(2,2);
                    game.pitcher.gameP_R++;
                }
                if(game.manOnFirst()) {
                    moveBaseRunnerFrom(1,3);
                    game.pitcher.gameP_R++;
                }
                player.gameH++;
                player.game1B++;
                player.gamePA++;
                player.gameAB++;
                player.gameR++;
                game.pitcher.gameP_R++;
                break;
            case 5:
                // walks
                game.pitcher.gameP_BB++;
                game.pitcher.gameP_PA++;

                if(game.manOnThird()) {
                    if(game.manOnSecond() && game.manOnFirst()) {
                        moveBaseRunnerFrom(3,1);
                        player.gameRBI++;
                        moveBaseRunnerFrom(2,1);
                        moveBaseRunnerFrom(1,1);
                        game.pitcher.gameP_R++;
                    }
                }
                if(game.manOnSecond()) {
                    if(game.manOnFirst()) {
                        moveBaseRunnerFrom(2,1);
                    }
                }
                if(game.manOnFirst()) {
                    moveBaseRunnerFrom(1,1);
                }

                moveBatter(1,player);

                player.gameBB++;
                player.gamePA++;
                player.gameAB++;
            case 7:
                // hbp
                game.pitcher.gameP_HBP += 1;
                game.pitcher.gameP_PA++;

                if(game.manOnThird()) {
                    if(game.manOnSecond() && game.manOnFirst()) {
                        moveBaseRunnerFrom(3,1);
                        player.gameRBI++;
                        moveBaseRunnerFrom(2,1);
                        moveBaseRunnerFrom(1,1);
                        game.pitcher.gameP_R++;
                    }
                }
                if(game.manOnSecond()) {
                    if(game.manOnFirst()) {
                        moveBaseRunnerFrom(2,1);
                    }
                }
                if(game.manOnFirst()) {
                    moveBaseRunnerFrom(1,1);
                }

                moveBatter(1,player);

                player.gameHBP++;
                player.gamePA++;
                break;
        }
    }

    private void moveBatter(int bases, Player player) {
        game.runner[bases] = player;
    }

    private void moveBaseRunnerFrom(int base, int numBases) {
        int runs = 0;

        if((base + numBases) == 4) {
            // move runner home
            game.runner[0] = game.runner[base];
            // clear runner at 3rd
            game.runner[base] = null;
            //  increment runs scored
            runs++;
            // increment runs scored for the player
            game.runner[0].gameR++;
        }
        else {
            game.runner[base+numBases] = game.runner[base];
        }
    }

    private void showResultText() {
        TextView tvResultText = (TextView) findViewById(R.id.tvResult);
        tvResultText.setText(game.resultText);
    }

    private void highlightOutcome() {
        boolean found = false;
        int iCount = 0;
        int foundResult = 0;
        ImageView ivHighlight = null;
        TextView tvHighlight = null;
        int[] ivOutcomeID = {R.id.ivResult_1b, R.id.ivResult_2b, R.id.ivResult_3b, R.id.ivResult_hr, R.id.ivResult_bb, R.id.ivResult_k, R.id.ivResult_hbp, R.id.ivResult_glove, R.id.ivResult_out};
        int[] tvOutcomeID = {R.id.tvResult_1b, R.id.tvResult_2b, R.id.tvResult_3b, R.id.tvResult_hr, R.id.tvResult_bb, R.id.tvResult_k, R.id.tvResult_hbp, R.id.tvResult_glove, R.id.tvResult_out};

        // get the batter's name
        String thisName = getPlayerName();

        // reset all outcomes
        for(int i=0; i < 9; i++) {
            ivHighlight = (ImageView) findViewById(ivOutcomeID[i]);
            ivHighlight.setBackgroundColor(Color.TRANSPARENT);

            tvHighlight = (TextView) findViewById(tvOutcomeID[i]);
            tvHighlight.setBackgroundColor(Color.TRANSPARENT);
        }

        while (found == false) {
            if(game.dieResult >= game.minOutcome[iCount] && game.dieResult <= game.maxOutcome[iCount]) {
                foundResult = iCount;
                found = true;
            }

            iCount += 1;
        }

        if(foundResult==0) {
            // single
            ivHighlight = (ImageView) findViewById(R.id.ivResult_1b);
            tvHighlight = (TextView) findViewById(R.id.tvResult_1b);

            // get random explanation of a single
            game.resultText = thisName + " singles to " + getHitDirection() + " field.";
            game.resultID = 1;
            nextBatter();
        }
        else if(foundResult==1) {
            // double
            ivHighlight = (ImageView) findViewById(R.id.ivResult_2b);
            tvHighlight = (TextView) findViewById(R.id.tvResult_2b);

            game.resultText = thisName + " doubles to " + getHitDirection() + " field.";
            game.resultID = 2;
            nextBatter();
        }
        else if(foundResult==2) {
            // triple
            ivHighlight = (ImageView) findViewById(R.id.ivResult_3b);
            tvHighlight = (TextView) findViewById(R.id.tvResult_3b);
            game.resultText = thisName + " triples to " + getHitDirection() + " field.";
            game.resultID = 3;
            nextBatter();
        }
        else if(foundResult==3) {
            // homerun
            ivHighlight = (ImageView) findViewById(R.id.ivResult_hr);
            tvHighlight = (TextView) findViewById(R.id.tvResult_hr);

            game.resultText = thisName + " hits a homerun to " + getHitDirection();
            game.resultID = 4;
            nextBatter();
        }
        else if(foundResult==4) {
            // walk
            ivHighlight = (ImageView) findViewById(R.id.ivResult_bb);
            tvHighlight = (TextView) findViewById(R.id.tvResult_bb);

            game.resultText = thisName + " draws a walk!";
            game.resultID = 5;
            nextBatter();
        }
        else if(foundResult==5) {
            // strikeout
            ivHighlight = (ImageView) findViewById(R.id.ivResult_k);
            tvHighlight = (TextView) findViewById(R.id.tvResult_k);

            game.resultID = 6;

            // swinging or looking?
            // league average is 25%, so that's what we'll use
            int newNum = game.rn.nextInt(100);

            if(newNum <=25 )
                game.resultText = thisName + " strikes out looking!";
            else
                game.resultText= thisName + " strikes out swinging!";

            game.addOuts();

            checkEndOfInning();
        }
        else if(foundResult==6) {
            // hbp
            ivHighlight = (ImageView) findViewById(R.id.ivResult_hbp);
            tvHighlight = (TextView) findViewById(R.id.tvResult_hbp);

            game.resultText = thisName + " is hit by the pitch!";
            game.resultID = 7;
            nextBatter();
        }
        else if(foundResult==7) {
            // glove
            ivHighlight = (ImageView) findViewById(R.id.ivResult_glove);
            tvHighlight = (TextView) findViewById(R.id.tvResult_glove);

            game.resultText = "Tough Play!";
            game.resultID = 8;

            // ***********CHANGE TO ANOTHER DIE ROLL TO SEE WHETHER FIELDER GETS THE BALL CLEANLY
            // *********** AND WHETHER OR NOT HE MAKES AN ERROR
            game.resultText = "Out!";
            game.resultID = 9;

            game.addOuts();

            checkEndOfInning();
        }
        else if(foundResult==8) {
            // out
            ivHighlight = (ImageView) findViewById(R.id.ivResult_out);
            tvHighlight = (TextView) findViewById(R.id.tvResult_out);

            game.resultID = 9;

            int fielder = getOutDirection(); // 1, 3, 4, 5, 6, 7, 8, 9

            int outType = getOutType(fielder); // gb, ld, flyball, popup

            getOutResult();

            game.addOuts();

            checkEndOfInning();
        }

        ivHighlight.setBackgroundColor(Color.RED);
        tvHighlight.setBackgroundColor(Color.RED);
    }

    private int getOutType(int fielder) {
        int dieRedResult = game.rollDie();
        int dieWhiteResult = game.rollDie();
        int dieBlueResult = game.rollDie();

        int total = (dieRedResult*100) + (dieWhiteResult*10) + dieBlueResult;

        if(fielder >= 1 && fielder <= 6) {
            if(total >= 1 && total <= 450) {
                // groundball
                return 0;
            }
            else if(total > 450 && total <= 683) {
                // linedrive (max outs)
                return 4;
            }
            else if(total > 683 && total <= 710) {
                // linedrive
                return 1;
            }
            else {
                // popup
                return 2;
            }
        }
        else {
            return 1;
        }
    }

    private void getOutResult() {
        // get the batter's name
        String thisName = getPlayerName();

        game.resultText = thisName + " hits the ball to " + getOutDirection();
    }

    private String getPlayerName() {
        String name;

        if(game.teamAtBat==0) {
            name = game.vBatter.name;
        }
        else
            name = game.hBatter.name;

        return name;
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

        game.pitcher = new Player();
        game.batter = new Player();
        game.vPitcher = new Player();
        game.hPitcher = new Player();
        game.hBatter = new Player();
        game.vBatter = new Player();
        game.vDefense = new Player[9];
        game.hDefense = new Player[9];

        buildVisTeam(vSeasonName, vTeamID);
        buildHomeTeam(hSeasonName, hTeamID);

        game.pitcher = game.hPitcher;

        // show team names on scoreboard
        TextView tvVTeamName = (TextView) findViewById(R.id.tvVisitorTeamName);
        TextView tvHTeamName = (TextView) findViewById(R.id.tvHomeTeamName);

        tvVTeamName.setText(vTeam.name);
        tvHTeamName.setText(hTeam.name);

        game.batter = vTeam.roster.get(0);
        game.onDeck = vTeam.roster.get(1);
        game.inTheHole = vTeam.roster.get(2);
    }

    private void buildVisTeam(String seasonName, int teamID) {
        String[] sVsl = {"vsl_1b_game", "vsl_2b_game", "vsl_3b_game", "vsl_hr_game", "vsl_bb_game", "vsl_so_game", "vsl_hbp_game"};
        String[] sVsr = {"vsr_1b_game", "vsr_2b_game", "vsr_3b_game", "vsr_hr_game", "vsr_bb_game", "vsr_so_game", "vsr_hbp_game"};
        String[] sprayChart = {"pull", "center", "oppo"};
        String[] ballSpeed = {"soft", "med", "hard"};

        myDB = openOrCreateDatabase(seasonName, MODE_PRIVATE, null);

        vTeam = new Team();
        vTeam.name = getTeamName(teamID);

        // *******
        // LINEUP
        // *******
        getVisLineups();

        Cursor cPlayer = null;

        boolean rosterComplete = false;

        cPlayer = myDB.query("players", null, "team='" + teamID + "'", null, null, null, null);

        while(!rosterComplete) {

            if(cPlayer.moveToNext()) {
                Player thisPlayer = new Player();

                int col1 = cPlayer.getColumnIndex("first_name");
                int col2 = cPlayer.getColumnIndex("last_name");

                thisPlayer.name = cPlayer.getString(col1) + " " + cPlayer.getString(col2);

                col1 = cPlayer.getColumnIndex("pos");
                thisPlayer.pos = cPlayer.getString(col1);

                col1 = cPlayer.getColumnIndex("_id");
                thisPlayer._id = cPlayer.getInt(col1);

                col1 = cPlayer.getColumnIndex("bats");
                thisPlayer.pBats = cPlayer.getString(col1);

                col1 = cPlayer.getColumnIndex("throws");
                thisPlayer.pThrows = cPlayer.getString(col1);

                for(int j = 0; j < 7; j++) {
                    col1 = cPlayer.getColumnIndex(sVsl[j]);
                    thisPlayer.pVsl[j] = cPlayer.getInt(col1);
                }

                for(int j = 0; j < 7; j++) {
                    col1 = cPlayer.getColumnIndex(sVsr[j]);
                    thisPlayer.pVsr[j] = cPlayer.getInt(col1);
                }

                col1 = cPlayer.getColumnIndex("sac_bunt");
                thisPlayer.sac_bunt = cPlayer.getInt(col1);

                for(int j = 0; j < 3; j++) {
                    col1 = cPlayer.getColumnIndex(sprayChart[j]);
                    thisPlayer.sprayChart[j] = cPlayer.getInt(col1);
                }

                for(int j = 0; j < 3; j++) {
                    col1 = cPlayer.getColumnIndex(ballSpeed[j]);
                    thisPlayer.ballSpeed[j] = cPlayer.getInt(col1);
                }

                col1 = cPlayer.getColumnIndex("baserunning");
                thisPlayer.baseRunning = cPlayer.getInt(col1);

                col1 = cPlayer.getColumnIndex("stealing");
                thisPlayer.stealing = cPlayer.getInt(col1);

                col1 = cPlayer.getColumnIndex("avoid_dp");
                thisPlayer.avoid_dp = cPlayer.getInt(col1);

                col1 = cPlayer.getColumnIndex("hold_rating");
                thisPlayer.hold_rating = cPlayer.getInt(col1);

                col1 = cPlayer.getColumnIndex("rsb");
                thisPlayer.rsb = cPlayer.getInt(col1);

                col1 = cPlayer.getColumnIndex("arm_rating");
                thisPlayer.arm_rating = cPlayer.getInt(col1);

                col1 = cPlayer.getColumnIndex("defense_rating");
                thisPlayer.defense_rating = cPlayer.getInt(col1);

                col1 = cPlayer.getColumnIndex("fld_range");
                thisPlayer.fld_range = cPlayer.getInt(col1);

                col1 = cPlayer.getColumnIndex("fld_error");
                thisPlayer.fld_error = cPlayer.getInt(col1);

                col1 = cPlayer.getColumnIndex("vsl_rating");
                thisPlayer.vsl_rating = cPlayer.getInt(col1);

                col1 = cPlayer.getColumnIndex("vsr_rating");
                thisPlayer.vsr_rating = cPlayer.getInt(col1);

                col1 = cPlayer.getColumnIndex("pb_rating");
                thisPlayer.passedBallRating = cPlayer.getInt(col1);

                col1 = cPlayer.getColumnIndex("speed_rating");
                thisPlayer.spd_rating = cPlayer.getInt(col1);

                col1 = cPlayer.getColumnIndex("con_rating");
                thisPlayer.con_rating = cPlayer.getInt(col1);

                col1 = cPlayer.getColumnIndex("power_rating");
                thisPlayer.pwr_rating = cPlayer.getInt(col1);

                col1 = cPlayer.getColumnIndex("pb_rating");
                thisPlayer.passedBallRating = cPlayer.getInt(col1);

                col1 = cPlayer.getColumnIndex("balk_rating");
                thisPlayer.balkRating = cPlayer.getInt(col1);

                col1 = cPlayer.getColumnIndex("wild_pitch_rating");
                thisPlayer.wildPitchRating = cPlayer.getInt(col1);

                // player value
                //col1 = cPlayer.getColumnIndex("value");
                //thisPlayer.value = cPlayer.getInt(col1);

                // special text

                // role

                // stamina

                vTeam.addPlayerToRoster(thisPlayer);
            }
            else {
                rosterComplete = true;
            }
        }

        cPlayer.close();

        // get the pitcher
        game.vPitcher = vTeam.roster.get(bundle.getInt("visStarter"));

        buildVisDefense();

        myDB.close();
    }

    private void buildVisDefense() {
        // get defense from bundle
        vDefense = bundle.getIntArray("visDefense");

        for(int i=0; i<9; i++) {
            game.vDefense[vDefense[i]] = vTeam.roster.get(vDefense[i]);
        }

        // pitcher's defense
        game.vDefense[0] = game.vPitcher;
    }

    private void buildHomeDefense() {
        // get defense from bundle
        hDefense = bundle.getIntArray("homeDefense");

        for(int i=0; i<9; i++) {
            game.hDefense[hDefense[i]] = hTeam.roster.get(hDefense[i]);
            Log.d(TAG, "hDefense: " + game.hDefense[hDefense[i]].name);
        }

        // pitcher's defense
        game.hDefense[0] = game.hPitcher;
    }

    private void buildHomeTeam(String seasonName, int teamID) {
        String[] sVsl = {"vsl_1b_game", "vsl_2b_game", "vsl_3b_game", "vsl_hr_game", "vsl_bb_game", "vsl_so_game", "vsl_hbp_game"};
        String[] sVsr = {"vsr_1b_game", "vsr_2b_game", "vsr_3b_game", "vsr_hr_game", "vsr_bb_game", "vsr_so_game", "vsr_hbp_game"};
        String[] sprayChart = {"pull", "center", "oppo"};
        String[] ballSpeed = {"soft", "med", "hard"};

        myDB = openOrCreateDatabase(seasonName, MODE_PRIVATE, null);

        hTeam = new Team();
        hTeam.name = getTeamName(teamID);

        // *******
        // LINEUP
        // *******
        getHomeLineups();

        Cursor cPlayer = null;

        boolean rosterComplete = false;

        cPlayer = myDB.query("players", null, "team='" + teamID + "'", null, null, null, null);

        while(!rosterComplete) {

            if(cPlayer.moveToNext()) {
                Player thisPlayer = new Player();

                int col1 = cPlayer.getColumnIndex("first_name");
                int col2 = cPlayer.getColumnIndex("last_name");

                thisPlayer.name = cPlayer.getString(col1) + " " + cPlayer.getString(col2);

                col1 = cPlayer.getColumnIndex("pos");
                thisPlayer.pos = cPlayer.getString(col1);

                col1 = cPlayer.getColumnIndex("_id");
                thisPlayer._id = cPlayer.getInt(col1);

                col1 = cPlayer.getColumnIndex("bats");
                thisPlayer.pBats = cPlayer.getString(col1);

                col1 = cPlayer.getColumnIndex("throws");
                thisPlayer.pThrows = cPlayer.getString(col1);

                for(int j = 0; j < 7; j++) {
                    col1 = cPlayer.getColumnIndex(sVsl[j]);
                    thisPlayer.pVsl[j] = cPlayer.getInt(col1);
                }

                for(int j = 0; j < 7; j++) {
                    col1 = cPlayer.getColumnIndex(sVsr[j]);
                    thisPlayer.pVsr[j] = cPlayer.getInt(col1);
                }

                col1 = cPlayer.getColumnIndex("sac_bunt");
                thisPlayer.sac_bunt = cPlayer.getInt(col1);

                for(int j = 0; j < 3; j++) {
                    col1 = cPlayer.getColumnIndex(sprayChart[j]);
                    thisPlayer.sprayChart[j] = cPlayer.getInt(col1);
                }

                for(int j = 0; j < 3; j++) {
                    col1 = cPlayer.getColumnIndex(ballSpeed[j]);
                    thisPlayer.ballSpeed[j] = cPlayer.getInt(col1);
                }

                col1 = cPlayer.getColumnIndex("baserunning");
                thisPlayer.baseRunning = cPlayer.getInt(col1);

                col1 = cPlayer.getColumnIndex("stealing");
                thisPlayer.stealing = cPlayer.getInt(col1);

                col1 = cPlayer.getColumnIndex("avoid_dp");
                thisPlayer.avoid_dp = cPlayer.getInt(col1);

                col1 = cPlayer.getColumnIndex("hold_rating");
                thisPlayer.hold_rating = cPlayer.getInt(col1);

                col1 = cPlayer.getColumnIndex("rsb");
                thisPlayer.rsb = cPlayer.getInt(col1);

                col1 = cPlayer.getColumnIndex("arm_rating");
                thisPlayer.arm_rating = cPlayer.getInt(col1);

                col1 = cPlayer.getColumnIndex("defense_rating");
                thisPlayer.defense_rating = cPlayer.getInt(col1);

                col1 = cPlayer.getColumnIndex("fld_range");
                thisPlayer.fld_range = cPlayer.getInt(col1);

                col1 = cPlayer.getColumnIndex("fld_error");
                thisPlayer.fld_error = cPlayer.getInt(col1);

                col1 = cPlayer.getColumnIndex("vsl_rating");
                thisPlayer.vsl_rating = cPlayer.getInt(col1);

                col1 = cPlayer.getColumnIndex("vsr_rating");
                thisPlayer.vsr_rating = cPlayer.getInt(col1);

                col1 = cPlayer.getColumnIndex("pb_rating");
                thisPlayer.passedBallRating = cPlayer.getInt(col1);

                col1 = cPlayer.getColumnIndex("speed_rating");
                thisPlayer.spd_rating = cPlayer.getInt(col1);

                col1 = cPlayer.getColumnIndex("con_rating");
                thisPlayer.con_rating = cPlayer.getInt(col1);

                col1 = cPlayer.getColumnIndex("power_rating");
                thisPlayer.pwr_rating = cPlayer.getInt(col1);

                col1 = cPlayer.getColumnIndex("pb_rating");
                thisPlayer.passedBallRating = cPlayer.getInt(col1);

                col1 = cPlayer.getColumnIndex("balk_rating");
                thisPlayer.balkRating = cPlayer.getInt(col1);

                col1 = cPlayer.getColumnIndex("wild_pitch_rating");
                thisPlayer.wildPitchRating = cPlayer.getInt(col1);

                // player value

                // special text

                // role

                // stamina

                hTeam.addPlayerToRoster(thisPlayer);
            }
            else {
                rosterComplete = true;
            }
        }

        cPlayer.close();

        // get the pitcher
        game.hPitcher = hTeam.roster.get(bundle.getInt("homeStarter"));

        buildHomeDefense();

        myDB.close();
    }

    private void getVisLineups() {
        vLineup = getIntent().getIntArrayExtra("visLineup");
        vDefense = getIntent().getIntArrayExtra("visDefense");
    }

    private void getHomeLineups() {
        hLineup = getIntent().getIntArrayExtra("homeLineup");
        hDefense = getIntent().getIntArrayExtra("homeDefense");
    }

    private void getVisBench() {
        //vTeam.benchID = getIntent().getIntArrayExtra("visBench");
    }

    private void getHomeBench() {
        //hTeam.benchID = getIntent().getIntArrayExtra("homeBench");
    }

    private void getVisBullpen() {
        //vTeam.bullpenID = getIntent().getIntArrayExtra("visBullpen");
    }

    private void getHomeBullpen() {
        //hTeam.bullpenID = getIntent().getIntArrayExtra("homeBullpen");
    }

    private void getVisStarters() {
        game.vPitcher = vTeam.roster.get(getIntent().getIntExtra("visStarter", 0));
    }

    private void getHomeStarters() {
        game.hPitcher = hTeam.roster.get(getIntent().getIntExtra("homeStarter", 0));
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
        updateNextBatters();
        updateResult(game.batter);
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
        if(game.batter.pBats.equals("r")) {
            if(game.pitcher.pThrows.equals("r")) {
                // base
                layers[0] = ContextCompat.getDrawable(this, R.drawable.card_vsr_as_right);

                // contact
                layers[2] = ContextCompat.getDrawable(this, cardContact[game.batter.vsr_rating]);
            }
            else {
                // base
                layers[0] = ContextCompat.getDrawable(this, R.drawable.card_vsl_as_right);
                // contact
                layers[2] = ContextCompat.getDrawable(this, cardContact[game.batter.vsl_rating]);
            }
        }
        else if(game.batter.pBats.equals("l") || game.batter.pBats.equals("s")) {
            // batter bats left
            if(game.pitcher.pThrows.equals("r")) {
                // base
                layers[0] = ContextCompat.getDrawable(this, R.drawable.card_vsr_as_left);

                // contact
                layers[2] = ContextCompat.getDrawable(this, cardContact[game.batter.vsr_rating]);
            }
            else {
                // base
                layers[0] = ContextCompat.getDrawable(this, R.drawable.card_vsl_as_left);
                // contact
                layers[2] = ContextCompat.getDrawable(this, cardContact[game.batter.vsl_rating]);
            }
        }

        // value
        layers[1] = ContextCompat.getDrawable(this, cardValue[0]);

        // defense
        layers[3] = ContextCompat.getDrawable(this, cardDefense[game.batter.defense_rating]);

        // position
        int posNum = game.convertPos(game.batter.pos);
        layers[4] = ContextCompat.getDrawable(this, cardPos[posNum]);

        LayerDrawable layerDrawable = new LayerDrawable(layers);
        ivCard.setImageDrawable(layerDrawable);

    }

    private void updateBaseRunners() {
        ImageView ivRunner_1 = (ImageView) findViewById(R.id.ivBaserunner_1);
        ImageView ivRunner_2 = (ImageView) findViewById(R.id.ivBaserunner_2);
        ImageView ivRunner_3 = (ImageView) findViewById(R.id.ivBaserunner_3);

        TextView tvRunnerSpeed_1 = (TextView) findViewById(R.id.tvBaserunnerSpeed_1);
        TextView tvRunnerSpeed_2 = (TextView) findViewById(R.id.tvBaserunnerSpeed_2);
        TextView tvRunnerSpeed_3 = (TextView) findViewById(R.id.tvBaserunnerSpeed_3);

        if(game.manOnFirst()) {
            // runner on 1st
            ivRunner_1.setVisibility(View.VISIBLE);
            tvRunnerSpeed_1.setVisibility(View.VISIBLE);
            tvRunnerSpeed_1.setText("" + game.runner[1].spd_rating);
        }
        else {
            ivRunner_1.setVisibility(View.INVISIBLE);
            tvRunnerSpeed_1.setVisibility(View.INVISIBLE);
        }

        if(game.manOnSecond()) {
            // runner on 2nd
            ivRunner_2.setVisibility(View.VISIBLE);
            tvRunnerSpeed_2.setVisibility(View.VISIBLE);
            tvRunnerSpeed_2.setText("" + game.runner[2].spd_rating);
        }
        else {
            ivRunner_2.setVisibility(View.INVISIBLE);
            tvRunnerSpeed_2.setVisibility(View.INVISIBLE);
        }

        if(game.manOnThird()) {
            // runner on 3rd
            ivRunner_3.setVisibility(View.VISIBLE);
            tvRunnerSpeed_3.setVisibility(View.VISIBLE);
            tvRunnerSpeed_3.setText("" + game.runner[3].spd_rating);
        }
        else {
            ivRunner_3.setVisibility(View.INVISIBLE);
            tvRunnerSpeed_3.setVisibility(View.INVISIBLE);
        }
    }

    private void updatePitcherCard() {
        int[] cardValue = {R.drawable.value_1, R.drawable.value_2, R.drawable.value_3, R.drawable.value_4, R.drawable.value_5, R.drawable.value_6, R.drawable.value_7, R.drawable.value_8, R.drawable.value_9, R.drawable.value_10};
        int[] cardDefense = {R.drawable.defense_1, R.drawable.defense_2, R.drawable.defense_3, R.drawable.defense_4, R.drawable.defense_5, R.drawable.defense_6, R.drawable.defense_7, R.drawable.defense_8, R.drawable.defense_9, R.drawable.defense_10};
        int[] cardContact = {R.drawable.contact_1, R.drawable.contact_2, R.drawable.contact_3, R.drawable.contact_4, R.drawable.contact_5, R.drawable.contact_6, R.drawable.contact_7, R.drawable.contact_8, R.drawable.contact_9, R.drawable.contact_10};
        int[] cardPos = {R.drawable.pos_sp, R.drawable.pos_rp, R.drawable.pos_c, R.drawable.pos_1b, R.drawable.pos_2b, R.drawable.pos_3b, R.drawable.pos_ss, R.drawable.pos_lf, R.drawable.pos_cf, R.drawable.pos_rf};

        ImageView ivCard = (ImageView) findViewById(R.id.ivPitcherCard);

        Drawable[] layers = new Drawable[5];

        // batter bats right or left? get correct bg image
        if(game.pitcher.pThrows.equals("r")) {
            if(game.batter.pBats.equals("r")) {
                // base
                layers[0] = ContextCompat.getDrawable(this, R.drawable.pitcher_card_vsr_as_right);

                // contact
                layers[2] = ContextCompat.getDrawable(this, cardContact[game.batter.vsr_rating]);
            }
            else {
                layers[0] = ContextCompat.getDrawable(this, R.drawable.pitcher_card_vsl_as_right);

                // contact
                layers[2] = ContextCompat.getDrawable(this, cardContact[game.batter.vsl_rating]);
            }
        }
        else {
            // pitcher throws left
            if(game.batter.pBats.equals("r") || game.batter.pBats.equals("s")) {
                // base
                layers[0] = ContextCompat.getDrawable(this, R.drawable.pitcher_card_vsr_as_left);

                // contact
                layers[2] = ContextCompat.getDrawable(this, cardContact[game.batter.vsr_rating]);
            }
            else {
                // base
                layers[0] = ContextCompat.getDrawable(this, R.drawable.pitcher_card_vsl_as_left);

                // contact
                layers[2] = ContextCompat.getDrawable(this, cardContact[game.batter.vsl_rating]);
            }
        }

        // value
        layers[1] = ContextCompat.getDrawable(this, cardValue[0]);

        // defense
        int def = game.pitcher.defense_rating;
        layers[3] = ContextCompat.getDrawable(this, cardDefense[def]);

        // position
        int posNum = game.convertPos(game.pitcher.pos);
        layers[4] = ContextCompat.getDrawable(this, cardPos[posNum]);

        LayerDrawable layerDrawable = new LayerDrawable(layers);
        ivCard.setImageDrawable(layerDrawable);

    }

    private void updateNextBatters() {
        TextView tvOnDeckBats = (TextView) findViewById(R.id.tvOnDeckBats);
        TextView tvOnDeckName = (TextView) findViewById(R.id.tvOnDeckPlayerName);
        TextView tvInTheHoleBats = (TextView) findViewById(R.id.tvInTheHoleBats);
        TextView tvInTheHoleName = (TextView) findViewById(R.id.tvInTheHolePlayerName);

        String sBats = "";
        String sName = "";

        if(game.teamAtBat==0) {
            if(game.vLineupBatter == 8) {
                // flip to top of lineup
                game.onDeck = vTeam.roster.get(vLineup[0]);
                game.inTheHole = vTeam.roster.get(vLineup[1]);
            }
            else if(game.vLineupBatter == 7) {
                game.onDeck = vTeam.roster.get(vLineup[8]);
                game.inTheHole = vTeam.roster.get(vLineup[0]);
            }
            else {
                game.onDeck = vTeam.roster.get(vLineup[game.vLineupBatter + 1]);
                game.inTheHole = vTeam.roster.get(vLineup[game.vLineupBatter + 2]);
            }
        }
        else {
            if(game.hLineupBatter == 8) {
                // flip to top of lineup
                game.onDeck = hTeam.roster.get(hLineup[0]);
                game.inTheHole = hTeam.roster.get(hLineup[1]);
            }
            else if(game.hLineupBatter == 7) {
                game.onDeck = hTeam.roster.get(hLineup[8]);
                game.inTheHole = hTeam.roster.get(hLineup[0]);
            }
            else {
                game.onDeck = hTeam.roster.get(hLineup[game.hLineupBatter + 1]);
                game.inTheHole = hTeam.roster.get(hLineup[game.hLineupBatter + 2]);
            }
        }

        tvOnDeckBats.setText(game.onDeck.pBats);
        tvOnDeckName.setText(game.onDeck.name);

        tvInTheHoleBats.setText(game.inTheHole.pBats);
        tvInTheHoleName.setText(game.inTheHole.name);
    }

    private void updateStamina() {
        ImageView ivStamina = (ImageView) findViewById(R.id.ivStamina);
        ClipDrawable drawable = (ClipDrawable) ivStamina.getBackground();

        int iStamina = 0; // progress
        int pStamina = 0; // pitcher
        int gStamina = 0; // game

        if(game.teamAtBat==0) {
            pStamina = game.hPitcher.stamina;
            gStamina = game.hStamina;
        }
        else {
            pStamina = game.vPitcher.stamina;
            gStamina = game.vStamina;
        }

        if(pStamina==0) {
            pStamina = 1;
        }

        iStamina = ((gStamina / pStamina) * 10000);

        iStamina = 5000;

        drawable.setLevel(iStamina);

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
            if (game.pitcher.pThrows.equals("r")) {
                if(game.batter.pBats.equals("r")) {
                    game.resultRange[i] = calculateNewRange(game.batter.pVsr[i], game.pitcher.pVsr[i], i);
                }
                else {
                    game.resultRange[i] = calculateNewRange(game.batter.pVsr[i], game.pitcher.pVsl[i], i);
                }
            }
            else {
                // pitcher throws left
                if(game.batter.pBats.equals("l")) {
                    game.resultRange[i] = calculateNewRange(game.batter.pVsl[i], game.pitcher.pVsl[i], i);
                }
                else {
                    game.resultRange[i] = calculateNewRange(game.batter.pVsl[i], game.hPitcher.pVsr[i], i);
                }
            }
        }
    }

    private int calculateNewRange(double b, double p, int avg) {
        double newValue;

        newValue = (((p*b)/game.resultAverages[avg])/(((p*b)/game.resultAverages[avg])+(((1000-p)*(1000-b))/(1000-game.resultAverages[avg]))))*1000;

        int newRange = (int) newValue;

        return newRange;
    }

    private int calculateHitRange(double b, double p, double avg) {
        double newValue;

        newValue = (((p*b)/avg)/(((p*b)/avg)+(((1000-p)*(1000-b))/(1000-avg))))*1000;

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

    private String getHitDirection() {

        // get LF, CF, RF ranges
        getHitDirectionRanges();

        // roll dice

        int dieRedResult = game.rollDie();
        int dieWhiteResult = game.rollDie();
        int dieBlueResult = game.rollDie();

        int total = (dieRedResult*100) + (dieWhiteResult*10) + dieBlueResult;

        if(total >= game.minHitDirection[0] && total <= game.maxHitDirection[0]) {
            // left field
            return "Left Field";
        }
        else if(total >= game.minHitDirection[1] && total <= game.maxHitDirection[1]) {
            // center field
            return "Center Field";
        }
        else {
            // right field
            return "Right Field";
        }

    }

    private int getOutDirection() {

        // left, middle, right
        getHitDirectionRanges();

        // roll dice
        int dieRedResult = game.rollDie();
        int dieWhiteResult = game.rollDie();
        int dieBlueResult = game.rollDie();

        int total = (dieRedResult*100) + (dieWhiteResult*10) + dieBlueResult;

        if(total <= 470) {
            // infield out
            dieRedResult = game.rollDie();
            dieWhiteResult = game.rollDie();
            dieBlueResult = game.rollDie();

            total = (dieRedResult*100) + (dieWhiteResult*10) + dieBlueResult;

            int thirdBaseMaxRange = (int)(game.maxHitDirection[0] *.25);
            int shortstopMaxRange = game.maxHitDirection[0] + ((game.maxHitDirection[1] - game.maxHitDirection[0])/2) - 35;
            int pitcherMaxRange = 70 + shortstopMaxRange;
            int secondBaseMaxRange = 1000 - (int)(game.maxHitDirection[1]*.25);

            if(total >= 1 && total <= thirdBaseMaxRange) {
                // 3rd baseman
                return 5;
            }
            else if(total > thirdBaseMaxRange && total <= shortstopMaxRange) {
                // shortstop
                return 6;
            }
            else if(total > shortstopMaxRange && total <= pitcherMaxRange) {
                // pitcher
                return 1;
            }
            else if(total > pitcherMaxRange && total <= secondBaseMaxRange) {
                // 2nd baseman
                return 4;
            }
            else if(total > (shortstopMaxRange + 70)) {
                // pitcher
                return 1;
            }
            else {
                // 1st baseman
                return 3;
            }
        }
        else {
            // outfield out
            dieRedResult = game.rollDie();
            dieWhiteResult = game.rollDie();
            dieBlueResult = game.rollDie();

            total = (dieRedResult*100) + (dieWhiteResult*10) + dieBlueResult;

            if(total >= 1 && total <= game.maxHitDirection[0]) {
                // left field
                return 7;
            }
            else if(total >= game.minHitDirection[1] && total <= game.maxHitDirection[1]) {
                // center field
                return 8;
            }
            else {
                // right field
                return 9;
            }
        }
    }

    private void getHitDirectionRanges() {
        int[] b = new int[3]; // batter
        int[] p = new int[3]; // pitcher
        int[] a = new int[3]; // average

        // batter
        if(game.batter.pBats.equals("r")) {
            b[0] = game.batter.sprayChart[0];
            b[1] = game.batter.sprayChart[1];
            b[2] = game.batter.sprayChart[2];

            p[0] = game.hPitcher.sprayChart[0];
            p[1] = game.hPitcher.sprayChart[1];
            p[2] = game.hPitcher.sprayChart[2];
                
            a[0] = 397;
            a[1] = 347;
            a[2] = 256;
        }
        else if(game.batter.pBats.equals("l")) {
            b[0] = game.batter.sprayChart[2];
            b[1] = game.batter.sprayChart[1];
            b[2] = game.batter.sprayChart[0];

            p[0] = game.hPitcher.sprayChart[2];
            p[1] = game.hPitcher.sprayChart[1];
            p[2] = game.hPitcher.sprayChart[0];

            a[0] = 256;
            a[1] = 347;
            a[2] = 397;
        }
        else if(game.pitcher.pThrows.equals("r")) {
            // switch hitter
            b[0] = game.batter.sprayChart[2];
            b[1] = game.batter.sprayChart[1];
            b[2] = game.batter.sprayChart[0];

            p[0] = game.hPitcher.sprayChart[2];
            p[1] = game.hPitcher.sprayChart[1];
            p[2] = game.hPitcher.sprayChart[0];

            a[0] = 256;
            a[1] = 347;
            a[2] = 397;
        }
        else {
            // throws left
            b[0] = game.batter.sprayChart[0];
            b[1] = game.batter.sprayChart[1];
            b[2] = game.batter.sprayChart[2];

            p[0] = game.hPitcher.sprayChart[0];
            p[1] = game.hPitcher.sprayChart[1];
            p[2] = game.hPitcher.sprayChart[2];

            a[0] = 397;
            a[1] = 347;
            a[2] = 256;
        }


        for(int i=0; i< 2; i++) {
            game.maxHitDirection[i] = calculateHitRange(b[i], p[i], a[i]);
        }

        game.minHitDirection[0] = 1;
        game.minHitDirection[1] = game.maxHitDirection[0] + 1;
        game.maxHitDirection[1] += game.maxHitDirection[0];
        game.minHitDirection[2] = game.maxHitDirection[1] + 1;
        game.maxHitDirection[2] = 1000;
    }

    private String getTeamName(int team) {
        String name = "Name not found";

        Cursor cTeam = myDB.query("teams", null, "_id='" + team + "'", null, null, null, null);

        if(cTeam.moveToFirst()) {
            int col = cTeam.getColumnIndex("team_name");
            name = cTeam.getString(col);
        }

        cTeam.close();

        return name;
    }
}
