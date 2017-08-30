package com.batterupbaseball;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.drawable.ClipDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import static android.content.ContentValues.TAG;

public class playball extends Activity implements View.OnClickListener {
    Game game;
    Bundle bundle;
    Team vTeam, hTeam;
    SQLiteDatabase myDB;
    String teamSelected;
    int[] vLineup;
    int[] hLineup;
    int[] vDefense = new int[9];
    int[] hDefense = new int[9];

        int runnerSelected;

    int[] ivBaserunner = {R.id.ivBaserunner_1, R.id.ivBaserunner_2, R.id.ivBaserunner_3};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.playball);

        final Context context = this;

        bundle = new Bundle();
        bundle = getIntent().getExtras();

        game = new Game();

        initGame();

        getResultAverages();

        updateScreen();

        RelativeLayout runnerOn1st = (RelativeLayout) findViewById(R.id.runnerOn1st);
        RelativeLayout runnerOn2nd = (RelativeLayout) findViewById(R.id.runnerOn2nd);
        RelativeLayout runnerOn3rd = (RelativeLayout) findViewById(R.id.runnerOn3rd);

        runnerOn1st.setOnClickListener(this);
        runnerOn2nd.setOnClickListener(this);
        runnerOn3rd.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {

        String stealText = "";

        if(game.teamAtBat == game.userTeam) {
            switch(v.getId()) {
                case R.id.runnerOn1st:
                    stealText = getString(R.string.steal_2nd);
                    runnerSelected = 1;
                    break;
                case R.id.runnerOn2nd:
                    stealText = getString(R.string.steal_3rd);
                    runnerSelected = 2;
                    break;
                case R.id.runnerOn3rd:
                    stealText = getString(R.string.steal_home);
                    runnerSelected = 3;
                    break;
            }

            final Dialog dialog = new Dialog(this);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setContentView(R.layout.base_stealing);

            TextView tvRunnerName = (TextView) dialog.findViewById(R.id.tvBaserunnerName);
            tvRunnerName.setText(game.runner[runnerSelected].name);

            final TextView tvStealBase = (TextView) dialog.findViewById(R.id.tvDialogSteal);
            tvStealBase.setText(stealText);

            final TextView tvPinchRunner = (TextView) dialog.findViewById(R.id.tvDialogPinchRunner);

            dialog.show();

            if(game.runnerStealing[runnerSelected-1])
                tvStealBase.setText(R.string.do_not_steal);
            else
                tvStealBase.setText(stealText + " " + game.runner[runnerSelected].stealing);

            tvStealBase.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ImageView thisBaseRunner = (ImageView) findViewById(ivBaserunner[runnerSelected-1]);

                    if(game.runnerStealing[runnerSelected-1]) {
                        if(runnerSelected == 1) {
                            // runner on 1st IS not stealing
                            game.runnerStealing[runnerSelected-1] = false;
                            thisBaseRunner.setImageResource(R.drawable.baserunner);
                        }
                        else if(runnerSelected == 2) {
                            // check to make sure the runner on 1st base isn't stealing
                            if(!game.runnerStealing[0]){
                                // runner on 1st IS not stealing
                                game.runnerStealing[runnerSelected-1] = false;
                                thisBaseRunner.setImageResource(R.drawable.baserunner);
                            }
                        }
                        else if(runnerSelected == 3) {
                            if(!game.runnerStealing[1]) {
                                // runner on 1st IS not stealing
                                game.runnerStealing[runnerSelected-1] = false;
                                thisBaseRunner.setImageResource(R.drawable.baserunner);
                            }
                        }

                    }
                    else {
                        game.runnerStealing[runnerSelected-1] = true;
                        // change image to runner stealing
                        thisBaseRunner = (ImageView) findViewById(ivBaserunner[runnerSelected-1]);
                        thisBaseRunner.setImageResource(R.drawable.baserunner_stealing);

                        if(runnerSelected == 1) {
                            if(game.manOnSecond()) {
                                ImageView secondBaseRunner = (ImageView) findViewById(R.id.ivBaserunner_2);
                                secondBaseRunner.setImageResource(R.drawable.baserunner_stealing);

                                game.runnerStealing[1] = true;

                                if(game.manOnThird()) {
                                    // bases loaded, everyone runs
                                    ImageView thirdBaseRunner = (ImageView) findViewById(R.id.ivBaserunner_3);
                                    thirdBaseRunner.setImageResource(R.drawable.baserunner_stealing);

                                    game.runnerStealing[2] = true;
                                }
                            }
                        }
                        else if(runnerSelected == 2) {
                            // check runner on 3rd
                            if(game.manOnThird()) {
                                // runner on 3rd, he runs too
                                ImageView thirdBaseRunner = (ImageView) findViewById(R.id.ivBaserunner_3);
                                thirdBaseRunner.setImageResource(R.drawable.baserunner_stealing);

                                game.runnerStealing[2] = true;
                            }
                        }
                    }

                    dialog.dismiss();
                }
            });

            tvPinchRunner.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // show pinch runner dialog
                    // dismiss this dialog
                    dialog.dismiss();
                }
            });
        }
    }

    public void getBatterResult(View view) {
        int balkRating, passedBallRating, wildPitchRating;

        if(game.basesOccupied()) {
            Log.d(TAG, "Bases Occupied");
            balkRating = game.pitcher.balkRating;
            wildPitchRating = game.pitcher.wildPitchRating;
            passedBallRating = game.defense[1].passedBallRating;

            // baserunners, check for wild pitch, passed ball, and balk
            roll_dice();

            if (game.dieResult <= 50) {
                // wild pitch
                Log.d(TAG, "Possibility of a wild pitch.");
                roll_dice();

                if(game.dieResult <= wildPitchRating) {
                    updateWildPitch();
                    Log.d(TAG, "WILD PITCH");
                }
                else {
                    Log.d(TAG, "No wild pitch, use the original result");
                    roll_dice();

                    highlightOutcome();
                }
            }
            else if (game.dieResult > 50 && game.dieResult <= 100) {
                // balk
                Log.d(TAG, "Possibility of a balk.");
                roll_dice();

                if(game.dieResult <= balkRating) {
                    updateBalk();
                    Log.d(TAG, "BALK");
                }
                else {
                    roll_dice();

                    highlightOutcome();
                }
            }
            else if(game.dieResult > 100 && game.dieResult <= 150) {
                // passed ball
                Log.d(TAG, "Possibility of a passed ball.");
                roll_dice();

                if(game.dieResult <= passedBallRating) {
                    updatePassedBall();
                    Log.d(TAG, "PASSED BALL");
                }
                else {
                    roll_dice();
                    show_dice_roll();

                    highlightOutcome();
                }
            }
            else {
                roll_dice();

                show_dice_roll();
                highlightOutcome();
            }

            showResultText();
        }
        else {
            roll_dice();
            show_dice_roll();

            Log.d(TAG, "Runner on 1st: " + game.manOnFirst());
            Log.d(TAG, "Runner on 2nd: " + game.manOnSecond());
            Log.d(TAG, "Runner on 3rd: " + game.manOnThird());

            highlightOutcome();
            showResultText();
        }

        updateResult(game.batter);
        updateScreen();
    }

    private void updateWildPitch() {
        game.resultText = game.pitcher.name + getString(R.string.throws_a_wild_pitch);
        game.pitcher.gameWP++;

        if(game.manOnThird()) {
            moveBaseRunnerFrom(3,1);
            game.resultText += " " + game.runner[0].name + getString(R.string.scores);
            game.pitcher.staminaRunsGivenUpThisInning += 1;

            if(game.pitcher.staminaRunsGivenUpThisInning > 1) {
                game.pitcher.staminaCurrent -= 1;
            }
            if(checkEndOfGame()) {
                TextView tvResultText = (TextView) findViewById(R.id.tvResult);
                tvResultText.setText("Game Over");
                Log.d(TAG, "GAME OVER");
            }
        }
        if(game.manOnSecond()) {
            moveBaseRunnerFrom(2,1);
            Log.d(TAG, "Runner on 2nd goes to 3rd.");
        }
        if(game.manOnFirst()) {
            moveBaseRunnerFrom(1,1);
            Log.d(TAG, "Runner on 1st goes to 2nd.");
        }

        updateScreen();
    }

    private void updateBalk() {
        game.resultText = game.pitcher.name + getString(R.string.is_called_for_a_balk);
        game.pitcher.gameBALK++;

        if(game.manOnThird()) {
            moveBaseRunnerFrom(3,1);
            game.resultText += " " + game.runner[0].name + getString(R.string.scores);
            game.pitcher.staminaRunsGivenUpThisInning += 1;

            if(game.pitcher.staminaRunsGivenUpThisInning > 1) {
                game.pitcher.staminaCurrent -= 1;
            }
            if(checkEndOfGame()) {
                TextView tvResultText = (TextView) findViewById(R.id.tvResult);
                tvResultText.setText("Game Over");
                Log.d(TAG, "GAME OVER");
            }
        }
        if(game.manOnSecond()) {
            moveBaseRunnerFrom(2,1);
            Log.d(TAG, "Runner on 2nd goes to 3rd.");

        }
        if(game.manOnFirst()) {
            moveBaseRunnerFrom(1,1);
            Log.d(TAG, "Runner on 1st goes to 2nd.");
        }

        updateScreen();
    }

    private void updatePassedBall() {
        game.resultText = "The ball gets passed " + game.defense[1].name + getString(R.string.for_a_passed_ball);
        game.defense[1].gamePASSEDBALL++;

        if(game.manOnThird()) {
            moveBaseRunnerFrom(3,1);
            game.resultText += " " + game.runner[0].name + getString(R.string.scores);
            game.pitcher.staminaRunsGivenUpThisInning += 1;

            if(game.pitcher.staminaRunsGivenUpThisInning > 1) {
                game.pitcher.staminaCurrent -= 1;
            }
            if(checkEndOfGame()) {
                TextView tvResultText = (TextView) findViewById(R.id.tvResult);
                tvResultText.setText("Game Over");
                Log.d(TAG, "GAME OVER");
            }
        }
        if(game.manOnSecond()) {
            moveBaseRunnerFrom(2,1);
            Log.d(TAG, "Runner on 2nd goes to 3rd.");
        }
        if(game.manOnFirst()) {
            moveBaseRunnerFrom(1,1);
            Log.d(TAG, "Runner on 1st goes to 2nd.");
        }

        updateScreen();
    }

    private void show_dice_roll() {
        int[] dieImageRed = {R.drawable.red_die_0, R.drawable.red_die_1, R.drawable.red_die_2, R.drawable.red_die_3, R.drawable.red_die_4, R.drawable.red_die_5, R.drawable.red_die_6, R.drawable.red_die_7, R.drawable.red_die_8, R.drawable.red_die_9};
        int[] dieImageWhite = {R.drawable.white_die_0, R.drawable.white_die_1, R.drawable.white_die_2, R.drawable.white_die_3, R.drawable.white_die_4, R.drawable.white_die_5, R.drawable.white_die_6, R.drawable.white_die_7, R.drawable.white_die_8, R.drawable.white_die_9};
        int[] dieImageBlue = {R.drawable.blue_die_0, R.drawable.blue_die_1, R.drawable.blue_die_2, R.drawable.blue_die_3, R.drawable.blue_die_4, R.drawable.blue_die_5, R.drawable.blue_die_6, R.drawable.blue_die_7, R.drawable.blue_die_8, R.drawable.blue_die_9};

        ImageView ivRedDie = (ImageView) findViewById(R.id.ivDie_1);
        ivRedDie.setImageResource(dieImageRed[game.dieRedResult]);

        ImageView ivWhiteDie = (ImageView) findViewById(R.id.ivDie_2);
        ivWhiteDie.setImageResource(dieImageWhite[game.dieWhiteResult]);

        ImageView ivBlueDie = (ImageView) findViewById(R.id.ivDie_3);
        ivBlueDie.setImageResource(dieImageBlue[game.dieBlueResult]);

    }

    private void roll_dice() {
        game.dieRedResult = game.rollDie();
        game.dieWhiteResult = game.rollDie();
        game.dieBlueResult = game.rollDie();

        game.dieResult = (game.dieRedResult*100) + (game.dieWhiteResult*10) + game.dieBlueResult;
    }

    private void checkEndOfInning() {
        if(game.outs == 3) {
            // check for end of game
            game.pitcher.staminaCurrent -= 1;
            game.pitcher.staminaRunsGivenUpThisInning = 0;

            if(checkEndOfGame()) {
                TextView tvResultText = (TextView) findViewById(R.id.tvResult);
                tvResultText.setText("Game Over");
                Log.d(TAG, "GAME OVER");
            }
            else {
                game.outs=0;
                game.clearTheBases();
                updateBaseRunners();
                nextHalfInning();
                updateScreen();
            }
        }
    }

    private void nextHalfInning() {
        game.teamAtBat ^= 1;

        if(game.teamAtBat==0) {
            game.inning++;
            game.pitcher = game.hPitcher;
        }
        else {
            game.pitcher = game.vPitcher;
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
            Log.d(TAG, "vLineup" + game.vLineupBatter);

            if(game.vLineupBatter > 8) {
                game.vLineupBatter = 0;
            }

            game.batter = vTeam.roster.get(game.vLineupBatter);
        }
        else {
            game.hLineupBatter++;

            if(game.hLineupBatter > 8) {
                game.hLineupBatter = 0;
            }

            game.batter = hTeam.roster.get(game.hLineupBatter);
        }
    }

    private void updateTeamHits() {
        if(game.teamAtBat==0) {
            game.vHits++;
        }
        else {
            game.hHits++;
        }
    }

    private void updateResult(Player player) {
        switch(game.resultID) {
            case 1:
                // singles
                updateTeamHits();
                game.pitcher.gameP_H++;
                game.pitcher.gameP_1B++;
                game.pitcher.gameP_PA++;
                game.pitcher.staminaCurrent -= 1;

                if(game.manOnThird()) {
                    moveBaseRunnerFrom(3,1);
                    player.gameRBI++;
                    game.pitcher.gameP_R++;
                    game.pitcher.staminaRunsGivenUpThisInning += 1;

                    if(game.pitcher.staminaRunsGivenUpThisInning > 1) {
                        game.pitcher.staminaCurrent -= 1;
                    }
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
                updateTeamHits();
                game.pitcher.gameP_H++;
                game.pitcher.gameP_2B++;
                game.pitcher.gameP_PA++;
                game.pitcher.staminaCurrent -= 1;

                if(game.manOnThird()) {
                    moveBaseRunnerFrom(3,1);
                    player.gameRBI++;
                    game.pitcher.staminaRunsGivenUpThisInning += 1;

                    if(game.pitcher.staminaRunsGivenUpThisInning > 1) {
                        game.pitcher.staminaCurrent -= 1;
                    }
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
                updateTeamHits();
                game.pitcher.gameP_H++;
                game.pitcher.gameP_3B++;
                game.pitcher.gameP_PA++;
                game.pitcher.staminaCurrent -= 1;

                if(game.manOnThird()) {
                    moveBaseRunnerFrom(3,1);
                    player.gameRBI++;
                    game.pitcher.gameP_R++;
                    game.pitcher.staminaRunsGivenUpThisInning += 1;

                    if(game.pitcher.staminaRunsGivenUpThisInning > 1) {
                        game.pitcher.staminaCurrent -= 1;
                    }
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
                updateTeamHits();
                // homeruns
                game.pitcher.gameP_H++;
                game.pitcher.gameP_HR++;
                game.pitcher.gameP_PA++;
                game.pitcher.staminaCurrent -= 1;

                if(game.manOnThird()) {
                    moveBaseRunnerFrom(3,1);
                    player.gameRBI++;
                    game.pitcher.gameP_R++;
                    game.pitcher.staminaRunsGivenUpThisInning += 1;

                    if(game.pitcher.staminaRunsGivenUpThisInning > 1) {
                        game.pitcher.staminaCurrent -= 1;
                    }
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
                updateScore(1); // for the batter
                break;
            case 5:
                // walks
                game.pitcher.gameP_BB++;
                Log.d(TAG, "Pitcher Walks Given Up: " + game.pitcher.gameP_BB);
                game.pitcher.gameP_PA++;
                Log.d(TAG, "Pitcher Plate Appearances: " + game.pitcher.gameP_PA);
                game.pitcher.staminaCurrent -= 1;

                if(game.manOnThird()) {
                    if(game.manOnSecond() && game.manOnFirst()) {
                        moveBaseRunnerFrom(3,1);
                        player.gameRBI++;
                        moveBaseRunnerFrom(2,1);
                        moveBaseRunnerFrom(1,1);
                        game.pitcher.gameP_R++;
                        game.pitcher.staminaRunsGivenUpThisInning += 1;

                        if(game.pitcher.staminaRunsGivenUpThisInning > 1) {
                            game.pitcher.staminaCurrent -= 1;
                        }
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
                break;
            case 7:
                // hbp
                game.pitcher.gameP_HBP += 1;
                Log.d(TAG, "Batters hit by pitch: " + game.pitcher.gameP_HBP);
                game.pitcher.gameP_PA++;
                Log.d(TAG, "Pitcher Plate Appearances: " + game.pitcher.gameP_PA);
                game.pitcher.staminaCurrent -= 1;

                if(game.manOnThird()) {
                    if(game.manOnSecond() && game.manOnFirst()) {
                        moveBaseRunnerFrom(3,1);
                        player.gameRBI++;
                        moveBaseRunnerFrom(2,1);
                        moveBaseRunnerFrom(1,1);
                        game.pitcher.gameP_R++;
                        game.pitcher.staminaRunsGivenUpThisInning += 1;

                        if(game.pitcher.staminaRunsGivenUpThisInning > 1) {
                            game.pitcher.staminaCurrent -= 1;
                        }
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

        updateBaseRunners();
        nextBatter();
    }

    private void moveBatter(int bases, Player player) {
        game.runner[bases] = player;
    }

    private void moveBaseRunnerFrom(int base, int numBases) {
        boolean runScored = false;

        if((base + numBases) == 4) {
            // move runner home
            game.runner[0] = game.runner[base];
            // clear runner at 3rd
            game.runner[base] = null;
            //  increment runs scored
            runScored = true;
            // increment runs scored for the player
            game.runner[0].gameR++;
        }
        else {
            game.runner[base+numBases] = game.runner[base];
            game.runner[base] = null;
        }

        if(runScored) {
            updateScore(1);
        }
    }

    private void updateScore(int runs) {
        if(game.teamAtBat==0) {
            game.vRuns += runs;
            game.vScoreByInning[game.inning-1] += runs;
        }
        else {
            game.hRuns += runs;
            game.hScoreByInning[game.inning-1] += runs;
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
        String thisName = game.batter.name;

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
            game.resultText = thisName + " singles to " + getHitDirection() + ".";
            game.resultID = 1;
        }
        else if(foundResult==1) {
            // double
            ivHighlight = (ImageView) findViewById(R.id.ivResult_2b);
            tvHighlight = (TextView) findViewById(R.id.tvResult_2b);

            game.resultText = thisName + " doubles to " + getHitDirection() + ".";
            game.resultID = 2;
        }
        else if(foundResult==2) {
            // triple
            ivHighlight = (ImageView) findViewById(R.id.ivResult_3b);
            tvHighlight = (TextView) findViewById(R.id.tvResult_3b);
            game.resultText = thisName + " triples to " + getHitDirection() + ".";
            game.resultID = 3;
        }
        else if(foundResult==3) {
            // homerun
            ivHighlight = (ImageView) findViewById(R.id.ivResult_hr);
            tvHighlight = (TextView) findViewById(R.id.tvResult_hr);

            game.resultText = thisName + " hits a homerun to " + getHitDirection();
            game.resultID = 4;
        }
        else if(foundResult==4) {
            // walk
            ivHighlight = (ImageView) findViewById(R.id.ivResult_bb);
            tvHighlight = (TextView) findViewById(R.id.tvResult_bb);

            game.resultText = thisName + " draws a walk!";
            game.resultID = 5;
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

            getOutResult(outType, fielder);

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

        Log.d(TAG, "fielder: " + fielder);
        Log.d(TAG, "total: " + total);

        if(fielder >= 0 && fielder <= 5) {
            if(total >= 1 && total <= 450) {
                // groundball
                roll_dice();

                // soft, med, or hard
                if(game.dieResult >= 1 && game.dieResult <= game.batter.ballSpeed[0]) {
                    // slow roller
                    Log.d(TAG, "slow roller");
                    return 0;
                }
                else if(game.dieResult > game.batter.ballSpeed[0] && game.dieResult <= (game.batter.ballSpeed[0] + game.batter.ballSpeed[1])) {
                    // normal groundball
                    Log.d(TAG, "normal groundball");
                    return 1;
                }
                else {
                    // hard grounder
                    Log.d(TAG, "hard grounder");
                    return 2;
                }
            }
            else if(total > 450 && total <= 600) {
                // line drive
                Log.d(TAG, "line drive");
                if(fielder == 0) {
                    return 1;
                }
                else {
                    return 3;
                }
            }
            else if(total > 600 && total <=610) {
                Log.d(TAG, "line drive");
                return 3;
            }
            else if(total >610 && total <= 700) {
                Log.d(TAG, "line drive");
                if(fielder == 0) {
                    return 1;
                }
                else {
                    return 3;
                }
            }
            else if(total > 700 && total <= 710) {
                // line drive (max)
                Log.d(TAG, "line drive/groudnball (max)");
                return 99;
            }
            else {
                // popup
                Log.d(TAG, "popup");

                if(fielder==0)
                    return 1;
                else
                    return 10;
            }
        }
        else {
            // ball to outfield
            roll_dice();

            if(game.dieResult >= 1 && game.dieResult <= game.batter.ballSpeed[0]) {
                // shallow fly ball
                Log.d(TAG, "shallow fly ball");
                return 4;
            }
            else if(game.dieResult > game.batter.ballSpeed[0] && game.dieResult <= (game.batter.ballSpeed[0] + game.batter.ballSpeed[1])) {
                // normal fly ball
                Log.d(TAG, "normal fly ball");
                return 5;
            }
            else {
                // deep fly ball
                Log.d(TAG, "deep fly ball");
                return 6;
            }
        }
    }

    private void getOutResult(int outType, int fielder) {
        String hitType = "";
        String additionalResultText = "";

        // get the batter's name
        String thisName = game.batter.name;

        switch(outType) {
            case 0:
                hitType = "slow roller"; // gb(c)
                // any runners on base advance while the batter is thrown out
                if(!game.basesOccupied()) {
                    additionalResultText = game.batter.name + " is thrown out at first.";
                    game.addOuts();
                }
                else if(game.manOnThird() && game.defenseInfieldIn) {
                    additionalResultText = game.batter.name + " is thrown out at first.";
                    game.addOuts();
                }
                else if(game.manOnFirst() && game.manOnThird()) {
                    additionalResultText = game.batter.name + " is thrown out at first.";
                    game.addOuts();

                    if(game.outs < 3) {
                        additionalResultText += game.runner[3].name + " is unable to score on the play.";
                        additionalResultText += game.runner[1].name + " advances to second on the play.";
                        moveBaseRunnerFrom(2,1);
                    }
                }
                else if(game.manOnSecond() && game.manOnThird()) {
                    additionalResultText = game.batter.name + " is thrown out at first.";
                    game.addOuts();

                    if(game.outs < 3) {
                        additionalResultText += game.runner[3] + " is unable to score on the play.";
                        additionalResultText += game.runner[2] + " is unable to advance to third on the play.";
                    }
                }
                else if(game.manOnThird() && game.manOnSecond() && game.manOnFirst()) {
                    additionalResultText = game.runner[3].name = " is thrown out at home!";
                    game.addOuts();

                    if(game.outs < 3) {
                        additionalResultText += game.runner[2].name = " advances to third.";
                        moveBaseRunnerFrom(2,1);
                        additionalResultText += game.runner[1].name = " advances to second.";
                        moveBaseRunnerFrom(1,1);
                        additionalResultText += game.batter.name = " is safe at first on the fielder's choice.";
                        moveBatter(1,game.batter);
                    }
                }
                else {
                    additionalResultText += thisName + " is thrown out at first.";
                    game.addOuts();
                    if(game.outs < 3) {
                        if(game.manOnThird()) {
                            additionalResultText += game.runner[3].name + " scores on the play!";
                            moveBaseRunnerFrom(3,1);
                            game.pitcher.staminaRunsGivenUpThisInning += 1;

                            if(game.pitcher.staminaRunsGivenUpThisInning > 1) {
                                game.pitcher.staminaCurrent -= 1;
                            }
                            if(checkEndOfGame()) {
                                TextView tvResultText = (TextView) findViewById(R.id.tvResult);
                                tvResultText.setText("Game Over");
                                Log.d(TAG, "GAME OVER");
                            }
                        }
                        if(game.manOnSecond()) {
                            additionalResultText += game.runner[2].name + " advances to third on the play";
                            moveBaseRunnerFrom(2,1);
                        }
                        if(game.manOnFirst()) {
                            additionalResultText += game.runner[1].name + " advances to second on the play";
                            moveBaseRunnerFrom(1,1);
                        }
                    }
                }
                break;
            case 1:
                hitType = "groundball"; // gb(b)

                if(game.defenseInfieldIn) {
                    if(game.manOnThird() && !game.manOnSecond() && !game.manOnFirst()) {
                        // man on third only - infield in
                        additionalResultText = game.runner[3].name + " is thrown out trying to score!";
                        game.addOuts();

                        if(game.outs < 3) {
                            additionalResultText += game.batter.name + " is safe at first on the fielder's choice.";
                            moveBatter(1,game.batter);
                        }
                    }
                    else if(game.manOnFirst() && !game.manOnSecond() && game.manOnThird()) {
                        // 1st and 3rd only - infield in
                        additionalResultText = game.runner[3].name + " is thrown out trying to score!";
                        game.addOuts();

                        if(game.outs < 3) {
                            additionalResultText += game.batter.name + " is safe at first on the fielder's choice.";
                            moveBatter(1,game.batter);
                            additionalResultText += game.runner[1].name + " advances to second on the play.";
                            moveBaseRunnerFrom(1,1);
                        }
                    }
                    else if(!game.manOnFirst() && game.manOnSecond() && game.manOnThird()) {
                        // 2nd and 3rd - infield in
                        additionalResultText = game.runner[3].name + " is thrown out trying to score!";
                        game.addOuts();

                        if(game.outs < 3) {
                            additionalResultText += game.batter.name + " is safe at first on the fielder's choice.";
                            moveBatter(1,game.batter);
                            additionalResultText += game.runner[2].name + " advances to third on the play.";
                            moveBaseRunnerFrom(2,1);
                        }
                    }
                    else if(game.manOnFirst() && game.manOnSecond() && game.manOnThird()) {
                        // bases loaded - infield in
                        additionalResultText = game.runner[3].name + " is thrown out trying to score!";
                        game.addOuts();

                        if(game.outs < 3) {
                            additionalResultText += game.batter.name + " is safe at first on the fielder's choice.";
                            moveBatter(1,game.batter);
                            additionalResultText += game.runner[2].name + " advances to third on the play.";
                            moveBaseRunnerFrom(2,1);
                            additionalResultText += game.runner[1].name + " advances to second on the play.";
                            moveBaseRunnerFrom(1,1);
                        }
                    }
                }
                else {
                    // infield back
                    if(!game.basesOccupied()) {
                        additionalResultText = thisName + " is thrown out at first.";
                        game.addOuts();
                    }
                    else if(game.manOnThird()) {
                        if(game.manOnSecond() && game.manOnFirst()) {
                            // bases loaded

                            if (game.outs == 2) {
                                additionalResultText = game.runner[1].name + " is thrown out at second.";
                                game.addOuts();
                            } else {
                                additionalResultText = game.runner[1].name + " is thrown out at second.";
                                additionalResultText += "All other runners advance one base on the fielder's choice.";
                                game.addOuts();

                                moveBaseRunnerFrom(3, 1);
                                moveBaseRunnerFrom(2, 1);
                                moveBatter(1, game.batter);
                            }
                        }
                        else if(game.manOnSecond()) {
                            if(fielder==3 || fielder==5) {
                                if(game.outs==2) {
                                    additionalResultText = game.batter.name + " is thrown out at first.";
                                    game.addOuts();
                                }
                                else {
                                    additionalResultText = game.batter.name + " is thrown out at first.";
                                    additionalResultText += game.runner[3].name + " scores from third!";
                                    additionalResultText += game.runner[2].name + " goes to third.";
                                    game.addOuts();

                                    moveBaseRunnerFrom(3,1);
                                    moveBaseRunnerFrom(2,1);
                                    game.pitcher.staminaRunsGivenUpThisInning += 1;

                                    if(game.pitcher.staminaRunsGivenUpThisInning > 1) {
                                        game.pitcher.staminaCurrent -= 1;
                                    }
                                    if(checkEndOfGame()) {
                                        TextView tvResultText = (TextView) findViewById(R.id.tvResult);
                                        tvResultText.setText("Game Over");
                                        Log.d(TAG, "GAME OVER");
                                    }
                                }
                            }
                            else {
                                additionalResultText = game.batter.name + " is thrown out at first.";
                                additionalResultText += "The runners are unable to advance.";
                                game.addOuts();
                            }
                        }
                        else if(game.manOnFirst()) {
                            if(game.outs==2) {
                                additionalResultText = game.runner[1].name + " is thrown out at second.";
                                game.addOuts();
                            }
                            else {
                                additionalResultText = game.runner[1].name + " is thrown out at second.";
                                additionalResultText += game.runner[3].name + " scores from third!";
                                game.addOuts();
                                moveBatter(1,game.batter);
                                if(checkEndOfGame()) {
                                    TextView tvResultText = (TextView) findViewById(R.id.tvResult);
                                    tvResultText.setText("Game Over");
                                    Log.d(TAG, "GAME OVER");
                                }
                            }
                        }
                        else {
                            if(fielder==3 || fielder==5) {
                                if(game.outs==2) {
                                    additionalResultText = game.batter.name + " is thrown out at first.";
                                    game.addOuts();
                                }
                                else {
                                    additionalResultText = game.batter.name + " is thrown out at first.";
                                    additionalResultText += game.runner[3].name + " scores from third!";

                                    game.addOuts();
                                    moveBaseRunnerFrom(3,1);
                                    game.pitcher.staminaRunsGivenUpThisInning += 1;

                                    if(game.pitcher.staminaRunsGivenUpThisInning > 1) {
                                        game.pitcher.staminaCurrent -= 1;
                                    }
                                    if(checkEndOfGame()) {
                                        TextView tvResultText = (TextView) findViewById(R.id.tvResult);
                                        tvResultText.setText("Game Over");
                                        Log.d(TAG, "GAME OVER");
                                    }
                                }
                            }
                            else {
                                additionalResultText = game.batter.name + " is thrown out at first.";
                                additionalResultText += "The runners are unable to advance.";
                                game.addOuts();
                            }
                        }
                    }
                    else if (game.manOnSecond()) {
                        if(game.manOnFirst()) {
                            if(game.outs==2) {
                                additionalResultText = game.runner[1].name + " is thrown out at second.";
                                additionalResultText += game.runner[2].name + " goes to third.";
                                game.addOuts();
                                moveBatter(1,game.batter);
                                moveBaseRunnerFrom(2,1);
                            }
                            else {
                                additionalResultText = game.runner[1].name + " is thrown out at second.";
                                game.addOuts();
                                additionalResultText += game.runner[2].name + " goes to third.";
                                moveBaseRunnerFrom(2,1);
                            }
                        }
                        else {
                            if(fielder==2 || fielder==3) {
                                if(game.outs==2) {
                                    additionalResultText = game.batter.name + " is thrown out at first.";
                                }
                                else {
                                    additionalResultText = game.batter.name + " is thrown out at first.";
                                    game.addOuts();
                                    additionalResultText += game.runner[2].name + " goes to third.";
                                    moveBaseRunnerFrom(2,1);
                                }

                            }
                            else {
                                additionalResultText = game.batter.name + " is thrown out at first.";
                                game.addOuts();
                                additionalResultText += game.runner[2].name + " is unable to advance to third.";
                            }
                        }
                    }
                    else if(game.manOnFirst()) {
                        if(game.outs==2) {
                            additionalResultText = game.runner[1].name + " is thrown out at second.";
                            game.addOuts();
                        }
                        else {
                            additionalResultText = game.runner[1].name + " is thrown out second.";
                            game.addOuts();
                            additionalResultText += game.batter.name + " is safe at first on the fielder's choice.";
                            moveBatter(1,game.batter);
                        }
                    }
                }
                break;
            case 2:
                hitType = "hard grounder"; // gb(a)
                if(game.defenseInfieldIn) {
                    if(game.manOnThird()) {
                        if(game.manOnFirst() && game.manOnSecond()) {
                            // bases loaded - infield in
                            if(game.outs == 2) {
                                additionalResultText = game.batter.name + " is thrown out at first.";
                                game.addOuts();
                            }
                            else {
                                additionalResultText = game.runner[3].name + " is thrown out at home!";
                                game.addOuts();
                                additionalResultText += game.batter.name + " is thrown out at first for a double play!";
                                game.addOuts();

                                if(game.outs < 3) {
                                    additionalResultText += game.runner[2].name + " advances to third on the play.";
                                    moveBaseRunnerFrom(2,1);
                                    additionalResultText += game.runner[1].name + " advances to second on the play.";
                                    moveBaseRunnerFrom(1,1);
                                }
                            }
                        }
                        else if(game.manOnSecond()) {
                            // 2nd and 3rd - infield in
                            additionalResultText = game.batter.name + " is thrown out at first.";
                            game.addOuts();
                        }
                        else if(game.manOnFirst()) {
                            // 1st and 3rd - infield in
                            additionalResultText = game.batter.name + " is thrown out at first.";
                            game.addOuts();

                            if(game.outs < 3) {
                                additionalResultText += game.runner[3].name + " is unable to score on the play.";
                                additionalResultText += game.runner[1].name + " advances to second on the play.";
                                moveBaseRunnerFrom(1,1);
                            }

                        }
                        else {
                            additionalResultText = game.batter.name + " is thrown out at first.";
                            game.addOuts();

                            if(game.outs < 3) {
                                additionalResultText += game.runner[3].name + " is unable to score on the play.";
                            }
                        }
                    }
                }
                else if(!game.basesOccupied()) {
                    // bases empty
                    additionalResultText = game.batter.name + " is thrown out at first.";
                    game.addOuts();
                }
                else if(game.manOnThird()) {
                    if(game.manOnSecond() && game.manOnFirst()) {
                        // bases loaded
                        if(game.outs==2) {
                            additionalResultText = game.batter.name + " is thrown out at first.";
                            game.addOuts();
                        }
                        else {
                            additionalResultText = game.batter.name + " grounds into a 6-4-3 double play!";
                            game.addOuts();
                            game.addOuts();

                            if(game.outs==2) {
                                additionalResultText += game.runner[3].name + " scores from third!";
                                moveBaseRunnerFrom(3,1);
                                game.pitcher.staminaRunsGivenUpThisInning += 1;

                                if(game.pitcher.staminaRunsGivenUpThisInning > 1) {
                                    game.pitcher.staminaCurrent -= 1;
                                }
                                additionalResultText += game.runner[2].name + " advances to third.";
                                moveBaseRunnerFrom(2,1);
                                game.clearBase(1);
                                if(checkEndOfGame()) {
                                    TextView tvResultText = (TextView) findViewById(R.id.tvResult);
                                    tvResultText.setText("Game Over");
                                    Log.d(TAG, "GAME OVER");
                                }
                            }
                        }
                    }
                    else if(game.manOnSecond()) {
                        if(fielder==3 || fielder==5) {
                            if(game.outs==2) {
                                additionalResultText = game.batter.name + " is thrown out at first.";
                                game.addOuts();
                            }
                            else {
                                additionalResultText = game.batter.name + " is thrown out at first.";
                                game.addOuts();
                                additionalResultText += game.runner[2].name + " advances to third on the play.";
                                moveBaseRunnerFrom(2,1);
                            }
                        }
                        else {
                            if(game.outs==2) {
                                additionalResultText = game.batter.name = " is thrown out at first.";
                                game.addOuts();
                            }
                            else {
                                additionalResultText = game.batter.name = " is thrown out at first.";
                                game.addOuts();
                                additionalResultText += game.runner[2].name = " is unable to go to third on the play.";
                            }
                        }
                    }
                    else if(game.manOnFirst()) {
                        if(game.outs==2) {
                            additionalResultText = game.batter.name + " is thrown out at first.";
                            game.addOuts();
                        }
                        else {
                            additionalResultText = game.batter.name + " grounds into a 6-4-3 double play!";
                            game.addOuts();
                            game.addOuts();

                            if(game.outs==2) {
                                additionalResultText += game.runner[3].name + " scores from third!";
                                moveBaseRunnerFrom(3,1);
                                game.clearBase(1);
                                game.pitcher.staminaRunsGivenUpThisInning += 1;

                                if(game.pitcher.staminaRunsGivenUpThisInning > 1) {
                                    game.pitcher.staminaCurrent -= 1;
                                }
                                if(checkEndOfGame()) {
                                    TextView tvResultText = (TextView) findViewById(R.id.tvResult);
                                    tvResultText.setText("Game Over");
                                    Log.d(TAG, "GAME OVER");
                                }
                            }
                        }
                    }
                    else {
                        // only a man on 3rd
                        if(fielder == 3 || fielder == 5) {
                            if(game.outs==2) {
                                additionalResultText = game.batter.name + " is thrown out at first.";
                                game.addOuts();
                            }
                            else {
                                additionalResultText = game.batter.name + " is thrown out at first.";
                                game.addOuts();
                                additionalResultText += game.runner[3].name + " scores from third!";
                                moveBaseRunnerFrom(3,1);
                                game.pitcher.staminaRunsGivenUpThisInning += 1;

                                if(game.pitcher.staminaRunsGivenUpThisInning > 1) {
                                    game.pitcher.staminaCurrent -= 1;
                                }
                                if(checkEndOfGame()) {
                                    TextView tvResultText = (TextView) findViewById(R.id.tvResult);
                                    tvResultText.setText("Game Over");
                                    Log.d(TAG, "GAME OVER");
                                }
                            }
                        }
                        else {
                            additionalResultText = game.batter.name + " is thrown out at first.";
                            game.addOuts();
                            additionalResultText += game.runner[3].name + " is unable to score on the play.";
                        }
                    }
                }
                else if(game.manOnSecond()) {
                    if(game.manOnFirst()) {
                        // men on 1st and 2nd
                        if(game.outs == 2){
                            additionalResultText = game.batter.name + " is thrown out at first.";
                            game.addOuts();
                        }
                        else {
                            additionalResultText = game.batter.name + " grounds into a 6-4-3 double play!";
                            game.addOuts();
                            game.addOuts();

                            if(game.outs==2) {
                                additionalResultText += game.runner[2].name + " advances to third on the play.";
                                moveBaseRunnerFrom(2,1);
                            }
                        }
                    }
                    else {
                        if(game.outs == 2) {
                            additionalResultText = game.batter.name + " is thrown out at first.";
                            game.addOuts();
                        }
                        else {
                            if(fielder == 2 || fielder == 3) {
                                additionalResultText = game.batter.name + " is thrown out at first.";
                                game.addOuts();
                                additionalResultText += game.runner[2].name + " advances to third on the play.";
                                moveBaseRunnerFrom(2,1);
                            }
                            else {
                                additionalResultText = game.batter.name + " is thrown out at first.";
                                game.addOuts();
                                additionalResultText += game.runner[2].name = " is unable to advance to third on the play.";
                            }
                        }
                    }
                }
                else if(game.manOnFirst()) {
                    if(game.outs==2) {
                        additionalResultText = game.batter.name + " is thrown out at first.";
                        game.addOuts();
                    }
                    else {
                        additionalResultText = game.batter.name + " grounds into a 6-4-3 double play!";
                        game.addOuts();
                        game.addOuts();
                    }
                }
                break;
            case 3:
                hitType = "line drive";
                game.addOuts();
                break;
            case 4:
                hitType = "shallow fly ball"; // fb(c)
                game.addOuts();

                if(game.outs < 3) {
                    if (game.basesOccupied()) {
                        additionalResultText = "The baserunners are unable to advance.";
                    }
                }
                break;
            case 5:
                hitType = "deep fly ball"; // fb(b)
                game.addOuts();
                if(game.outs < 3) {
                    if(game.manOnThird()) {
                        additionalResultText = game.runner[3].name + " tags from third and scores!";
                        moveBaseRunnerFrom(3,1);
                        game.pitcher.staminaRunsGivenUpThisInning += 1;

                        if(game.pitcher.staminaRunsGivenUpThisInning > 1) {
                            game.pitcher.staminaCurrent -= 1;
                        }
                        if(checkEndOfGame()) {
                            TextView tvResultText = (TextView) findViewById(R.id.tvResult);
                            tvResultText.setText("Game Over");
                            Log.d(TAG, "GAME OVER");
                        }
                    }
                    else if(game.basesOccupied()) {
                        additionalResultText = "The baserunner(s) are unable to advance.";
                    }
                }
                break;
            case 6:
                hitType = "deep fly ball to the warning track"; // fb(a)
                game.addOuts();
                if(game.outs < 3) {
                    if(game.manOnThird()) {
                        additionalResultText = game.runner[3].name + " tags from third and scores!";
                        moveBaseRunnerFrom(3,1);
                        game.pitcher.staminaRunsGivenUpThisInning += 1;

                        if(game.pitcher.staminaRunsGivenUpThisInning > 1) {
                            game.pitcher.staminaCurrent -= 1;
                        }
                        if(checkEndOfGame()) {
                            TextView tvResultText = (TextView) findViewById(R.id.tvResult);
                            tvResultText.setText("Game Over");
                            Log.d(TAG, "GAME OVER");
                        }
                    }
                    if(game.manOnSecond()) {
                        additionalResultText += game.runner[2].name + " tags from second and advances to third.";
                        moveBaseRunnerFrom(2,1);
                    }
                    if(game.manOnFirst()) {
                        additionalResultText += game.runner[1].name + " tags from first and advances to second.";
                        moveBaseRunnerFrom(1,1);
                    }
                }
                break;
            case 10:
                hitType = "popup";
                game.addOuts();
                break;
            case 99:
                if(!game.basesOccupied()) {
                    //lineout to fielder
                    hitType = "lineout";
                    game.addOuts();
                }
                else if(game.manOnThird()) {
                    if(game.manOnSecond() && game.manOnFirst()) {
                        // bases loaded
                        if(game.outs == 0) {
                            // possibility of a triple play
                            roll_dice();

                            if(game.dieResult <= 350) {

                                if(fielder==5) {
                                    // triple play groundout
                                    hitType = "hard line drive";
                                    additionalResultText = " He makes the catch.";
                                    additionalResultText += game.defense[5].name + " flips to second for the second out.";
                                    game.addOuts();
                                    game.addOuts();
                                    additionalResultText += "And on to first base for a TRIPLE PLAY!";
                                    game.addOuts();
                                }
                                else if(fielder==3) {
                                    hitType = "hard line drive";
                                    additionalResultText = game.defense[3].name + "He catches the ball and doubles off the runner on second base for the second out.";
                                    additionalResultText += "He throws to first for the TRIPLE PLAY!";
                                    game.addOuts();
                                    game.addOuts();
                                    game.addOuts();
                                }
                                else {
                                    // triple play groundout
                                    hitType = "hard groundball";
                                    additionalResultText = game.defense[4].name + " tags third for one out.";
                                    game.addOuts();
                                    additionalResultText += " He throws to second for the second out!";
                                    game.addOuts();
                                    additionalResultText += "And on to first base for a TRIPLE PLAY!";
                                    game.addOuts();
                                }
                            }
                            else {
                                // double play
                                hitType = "hard line drive";
                                additionalResultText = game.defense[fielder].name + " throws to second base for a double play";
                                game.addOuts();
                                game.addOuts();
                                game.clearBase(2);
                            }
                        }
                        else if(game.outs==1) {
                            // double play
                            hitType = "hard line drive";
                            additionalResultText = game.defense[fielder].name + " throws to second base for a double play";
                            game.addOuts();
                            game.addOuts();
                        }
                        else {
                            hitType = "hard line drive";
                            game.addOuts();
                        }
                    }
                    else if(game.manOnSecond()) {
                        if(game.outs < 2) {
                            // double play
                            hitType = "hard line drive";
                            additionalResultText = game.defense[fielder].name + " throws to third base for a double play";
                            game.addOuts();
                            game.addOuts();
                            game.clearBase(3);
                        }
                        else {
                            hitType = "hard line drive";
                            game.addOuts();
                        }
                    }
                    else if(game.manOnFirst()) {
                        if(game.outs < 2) {
                            // double play
                            hitType = "hard line drive";
                            additionalResultText = game.defense[fielder].name + " throws to third base for a double play";
                            game.addOuts();
                            game.addOuts();
                            game.clearBase(3);
                        }
                        else {
                            hitType = "hard line drive";
                            game.addOuts();
                        }
                    }
                    else {
                        // only a runner on third - double play
                        if(game.outs < 2) {
                            // double play
                            hitType = "hard line drive";
                            additionalResultText = game.defense[fielder].name + " throws to third base for a double play";
                            game.addOuts();
                            game.addOuts();
                            game.clearBase(3);
                        }
                        else {
                            hitType = "hard line drive";
                            game.addOuts();
                        }
                    }
                }
                else if(game.manOnSecond()) {
                    if (game.manOnFirst()) {
                        if (game.outs == 0) {
                            // possibility of a triple play
                            roll_dice();

                            if (game.dieResult <= 350) {

                                if (fielder == 5) {
                                    // triple play groundout
                                    hitType = "hard line drive";
                                    additionalResultText = game.defense[5].name + " catches the ball and flips to second for two outs.";
                                    game.addOuts();
                                    game.addOuts();
                                    additionalResultText += "And on to first base for a TRIPLE PLAY!";
                                    game.addOuts();
                                } else if (fielder == 3) {
                                    hitType = "hard line drive";
                                    additionalResultText = game.defense[3].name + "He catches the ball and doubles off the runner on second base for the second out.";
                                    additionalResultText = "He throws to first for the TRIPLE PLAY!";
                                    game.addOuts();
                                    game.addOuts();
                                    game.addOuts();
                                } else {
                                    // triple play groundout
                                    hitType = "hard groundball";
                                    additionalResultText = game.defense[4].name + " tags third for one out.";
                                    game.addOuts();
                                    additionalResultText += " He throws to second for the second out!";
                                    game.addOuts();
                                    additionalResultText += "And on to first base for a TRIPLE PLAY!";
                                    game.addOuts();
                                }
                            }
                            else {
                                // double play
                                hitType = "hard line drive";
                                additionalResultText = game.defense[fielder].name + " throws to second base for a double play";
                                game.addOuts();
                                game.addOuts();
                                game.clearBase(2);
                            }
                        }
                        else if (game.outs == 1) {
                            // double play
                            hitType = "hard line drive";
                            additionalResultText = game.defense[fielder].name + " throws to second base for a double play";
                            game.addOuts();
                            game.addOuts();
                        }
                        else {
                            hitType = "hard line drive";
                            game.addOuts();
                        }
                    }
                    else {
                        if(game.outs < 2) {
                            // double play
                            hitType = "hard line drive";
                            additionalResultText = game.defense[fielder].name + " throws to second base for a double play";
                            game.addOuts();
                            game.addOuts();
                        }
                        else {
                            hitType = "hard line drive";
                            game.addOuts();
                        }
                    }
                }
                else if(game.manOnFirst()) {
                    if(game.outs < 2) {
                        // double play
                        hitType = "hard line drive";
                        additionalResultText = game.defense[fielder].name + " throws to second base for a double play";
                        game.addOuts();
                        game.addOuts();
                    }
                    else {
                        hitType = "hard line drive";
                        game.addOuts();
                    }
                }

                break;
        }

        game.resultText = thisName + " hits a " + hitType + " to " + convertPosToString(fielder);
        game.resultText += ".  " + additionalResultText;

        showResultText();
        updateScreen();
    }

    private String convertPosToString(int pos) {
        switch(pos) {
            case 0:
                return "the pitcher.";
            case 1:
                return "the catcher.";
            case 2:
                return "the first baseman.";
            case 3:
                return "the second baseman.";
            case 4:
                return "the third baseman.";
            case 5:
                return "the shortstop.";
            case 6:
                return "the left fielder.";
            case 7:
                return "the center fielder.";
            case 8:
                return "the right fielder.";
            default:
                return "null";
        }
    }

    private void initGame() {

        String vSeasonName, hSeasonName;
        String current_year = getResources().getString(R.string.default_db_name);

        SharedPreferences prefs = getSharedPreferences("prefsFile", 0);
        int vTeamID = prefs.getInt("vTeamID", 1);
        int hTeamID = prefs.getInt("hTeamID", 2);
        teamSelected = prefs.getString("USER_TEAM", "V");

        if(teamSelected.equals("V")) {
            game.userTeam = 0;
            vSeasonName = prefs.getString("userSeasonFileName", current_year);
            hSeasonName = prefs.getString("oppSeasonFileName", current_year);
        }
        else {
            game.userTeam = 1;
            vSeasonName = prefs.getString("oppSeasonFileName", current_year);
            hSeasonName = prefs.getString("userSeasonFileName", current_year);
        }

        game.vSeasonName = vSeasonName;
        game.hSeasonName = hSeasonName;

        game.pitcher = new Player();
        game.batter = new Player();
        game.vPitcher = new Player();
        game.hPitcher = new Player();
        game.vDefense = new Player[9];
        game.hDefense = new Player[9];

        buildVisTeam(vSeasonName, vTeamID);
        buildHomeTeam(hSeasonName, hTeamID);

        game.pitcher = game.hPitcher;
        setupDefense();

        // show team names on scoreboard
        TextView tvVTeamName = (TextView) findViewById(R.id.tvVisitorTeamName);
        TextView tvHTeamName = (TextView) findViewById(R.id.tvHomeTeamName);

        tvVTeamName.setText(vTeam.name);
        tvHTeamName.setText(hTeam.name);

        game.batter = vTeam.roster.get(vLineup[0]);
        game.onDeck = vTeam.roster.get(vLineup[1]);
        game.inTheHole = vTeam.roster.get(vLineup[2]);

        updateBaseRunners();
    }

    private void setupDefense() {
        if(game.teamAtBat==0) {
            game.defense = game.hDefense;
        }
        else {
            game.defense = game.vDefense;
            Log.d(TAG, "" + game.defense[1].name);
        }
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
                col1 = cPlayer.getColumnIndex("stamina");
                thisPlayer.staminaMax = cPlayer.getInt(col1);
                thisPlayer.staminaCurrent = thisPlayer.staminaMax;

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
            game.vDefense[i] = vTeam.roster.get(vDefense[i]);
        }

        // pitcher's defense
        game.vDefense[0] = game.vPitcher;
    }

    private void buildHomeDefense() {
        // get defense from bundle
        hDefense = bundle.getIntArray("homeDefense");

        for(int i=0; i<9; i++) {
            game.hDefense[i] = hTeam.roster.get(hDefense[i]);
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
                col1 = cPlayer.getColumnIndex("stamina");
                thisPlayer.staminaMax = cPlayer.getInt(col1);
                thisPlayer.staminaCurrent = thisPlayer.staminaMax;

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
        updatePitcherCard();
        updateNextBatters();
        updateStamina();
        updatePossibleOutcomes();
        updatePitcherHoldRating();
        updateCatcherHoldRunners();
    }

    private void updateCatcherHoldRunners() {
        TextView tvCatcherHoldRunners = (TextView) findViewById(R.id.tvCatcherHoldRunners);
        tvCatcherHoldRunners.setText("Arm Rating: " + game.defense[1].rsb);
    }

    private void updatePitcherHoldRating() {
        TextView tvPitcherHoldRating = (TextView) findViewById(R.id.tvPitcherHoldRating);
        tvPitcherHoldRating.setText("Hold Runners: " + game.pitcher.hold_rating);
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
        layers[3] = ContextCompat.getDrawable(this, cardDefense[game.batter.defense_rating-1]);

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

        for(int i=0; i<3; i++) {
            game.runnerStealing[i] = false;
        }

        if(game.manOnFirst()) {
            // runner on 1st
            ivRunner_1.setVisibility(View.VISIBLE);
            tvRunnerSpeed_1.setVisibility(View.VISIBLE);
            tvRunnerSpeed_1.setText("" + game.runner[1].baseRunning + " / " + game.runner[1].stealing);
        }
        else {
            ivRunner_1.setVisibility(View.INVISIBLE);
            tvRunnerSpeed_1.setVisibility(View.INVISIBLE);
        }

        if(game.manOnSecond()) {
            // runner on 2nd
            ivRunner_2.setVisibility(View.VISIBLE);
            tvRunnerSpeed_2.setVisibility(View.VISIBLE);
            tvRunnerSpeed_2.setText("" + game.runner[2].baseRunning + " / " + game.runner[2].stealing);
        }
        else {
            ivRunner_2.setVisibility(View.INVISIBLE);
            tvRunnerSpeed_2.setVisibility(View.INVISIBLE);
        }

        if(game.manOnThird()) {
            // runner on 3rd
            ivRunner_3.setVisibility(View.VISIBLE);
            tvRunnerSpeed_3.setVisibility(View.VISIBLE);
            tvRunnerSpeed_3.setText("" + game.runner[3].baseRunning + " / " + game.runner[3].stealing);
        }
        else {
            ivRunner_3.setVisibility(View.INVISIBLE);
            tvRunnerSpeed_3.setVisibility(View.INVISIBLE);
        }

        // make sure all runners have stealing turned off
        ImageView ivRunner1 = (ImageView) findViewById(R.id.ivBaserunner_1);
        ImageView ivRunner2 = (ImageView) findViewById(R.id.ivBaserunner_2);
        ImageView ivRunner3 = (ImageView) findViewById(R.id.ivBaserunner_3);

        ivRunner1.setImageResource(R.drawable.baserunner);
        ivRunner2.setImageResource(R.drawable.baserunner);
        ivRunner3.setImageResource(R.drawable.baserunner);
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
        layers[3] = ContextCompat.getDrawable(this, cardDefense[def-1]);

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

        float iStamina = (((float)game.pitcher.staminaCurrent / game.pitcher.staminaMax)*10000); // 10000 = maximum, 0 = minimum

        drawable.setLevel((int)iStamina);

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
                return 4;
            }
            else if(total > thirdBaseMaxRange && total <= shortstopMaxRange) {
                // shortstop
                return 5;
            }
            else if(total > shortstopMaxRange && total <= pitcherMaxRange) {
                // pitcher
                return 0;
            }
            else if(total > pitcherMaxRange && total <= secondBaseMaxRange) {
                // 2nd baseman
                return 3;
            }
            else if(total > (shortstopMaxRange + 70)) {
                // pitcher
                return 0;
            }
            else {
                // 1st baseman
                return 2;
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
                return 6;
            }
            else if(total >= game.minHitDirection[1] && total <= game.maxHitDirection[1]) {
                // center field
                return 7;
            }
            else {
                // right field
                return 8;
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
