package com.batterupbaseball;

import java.util.Random;

public class Game {
    Random rn = new Random();
    Player vPitcher, hPitcher;
    Player[] vDefense;
    Player[] hDefense;
    Player batter;
    Player pitcher;
    Player[] defense;
    Player onDeck;
    Player inTheHole;
    boolean defenseInfieldIn = false;
    int userTeam = 0;

    Die die1, die2, die3;
    int dieRedResult = 0;
    int dieWhiteResult = 0;
    int dieBlueResult = 0;

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

    int dieResult = 0;

    int[] minHitDirection = new int[3];
    int[] maxHitDirection = new int[3];

    Player[] runner = new Player[4]; // 0 is used to move the runner home, then add up runs, etc.

    int[] resultRange = new int[9];
    int resultID = 0; // 1=single, 2=double, 3=triple, 4=homerun, 5=walk, 6=strikeout, 7=hbp, 8=glove, 9=out
    int[] minOutcome = new int[9];
    int[] maxOutcome = new int[9];
    int[] resultAverages = new int[7];

    String vSeasonName = "";
    String hSeasonName = "";

    String resultText = "";

    int vLineupBatter = 0; // visitor batter in lineup
    int hLineupBatter = 0; // home batter in lineup

    public Game() {
        setupNewDice();
    }

    private void setupNewDice() {
        die1 = new Die();
        die2 = new Die();
        die3 = new Die();
    }

    public int rollDie() {
        int result;

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

    public void addOuts() {
        this.outs += 1;
    }

    public void clearTheBases() {
        for(int i=0; i < 4; i++)
            runner[i] = null;
    }

    public boolean manOnFirst() {
        if (runner[1] != null)
            return true;
        else
            return false;
    }
    public boolean manOnSecond() {
        if (runner[2] != null)
            return true;
        else
            return false;
    }
    public boolean manOnThird() {
        if (runner[3] != null)
            return true;
        else
            return false;
    }

    public boolean basesOccupied() {
        if(manOnFirst() || manOnSecond() || manOnThird())
            return true;

        return false;
    }

    public void clearBase(int base) {
        runner[base] = null;
    }

}