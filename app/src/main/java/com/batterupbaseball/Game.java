package com.batterupbaseball;

import android.app.Activity;
import android.content.SharedPreferences;

import java.util.Random;

public class Game {
    Random rn = new Random();
    int[] visPlayerID, homePlayerID;
    Team vTeam, hTeam;
    Player[] vLineup, hLineup;
    Player vPitcher, hPitcher;
    Die die1, die2, die3;
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
    int vStamina = 0;
    int hStamina = 0;
    int dieResult = 0;

    int[] resultRange = new int[9];
    int[] resultID = new int[9]; // used to get whether it's a single, double, triple, homerun, walk, strikeout, hbp, glove, or out - some players don't have a triple, etc.
    int[] minOutcome = new int[9];
    int[] maxOutcome = new int[9];
    int[] resultAverages = new int[7];

    String vSeasonName = "";
    String hSeasonName = "";

    int vBatter = 0; // visitor batter in lineup
    int hBatter = 0; // home batter in lineup

    public void Game() {
        setupNewDice();
    }

    private void setupNewDice() {
        die1 = new Die();
        die2 = new Die();
        die3 = new Die();
    }

    public int rollDie() {
        int result = 0;

        result = rn.nextInt(10);

        return result;
    }

    public int convertPos(String pos) {
        int newPos = 0;

        switch(pos) {
            case "sp":
                newPos = 0;
                break;
            case "rp":
                newPos = 1;
                break;
            case "c":
                newPos = 2;
                break;
            case "1b":
                newPos = 3;
                break;
            case "2b":
                newPos = 4;
                break;
            case "3b":
                newPos = 5;
                break;
            case "ss":
                newPos = 6;
                break;
            case "lf":
                newPos = 7;
                break;
            case "cf":
                newPos = 8;
                break;
            case "rf":
                newPos = 9;
                break;
        }

        return newPos;
    }

    public void updateOuts() {
        this.outs += 1;
    }

}