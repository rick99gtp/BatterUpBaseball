package com.batterupbaseball;

import android.app.Activity;
import android.content.SharedPreferences;

public class Game {
    int[] visPlayerID, homePlayerID;
    Team vTeam, hTeam;
    Player[] vLineup, hLineup;
    Die die1, die2;

    public void Game(Team vTeam, Team hTeam) {
        vLineup = new Player[9];
        hLineup = new Player[9];
        setupNewDice();
    }

    private void setupNewDice() {
        die1 = new Die();
        die2 = new Die();
    }
}