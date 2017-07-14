package com.batterupbaseball;

import android.app.Activity;
import android.content.SharedPreferences;

public class Game {
    int[] visPlayerID, homePlayerID;
    Team vTeam, hTeam;
    Player[] vLineup, hLineup;
    Die die1, die2;
    int inning = 1;
    int teamAtBat = 0;
    int vRuns = 0;
    int hRuns = 0;
    int vHits = 0;
    int hHits = 0;
    int vErrors = 0;
    int hErrors = 0;
    int outs = 0;
    int[] vScoreByInning = new int[10];
    int[] hScoreByInning = new int[10];

    public void Game() {
        setupNewDice();
    }

    private void setupNewDice() {
        die1 = new Die();
        die2 = new Die();
    }
}