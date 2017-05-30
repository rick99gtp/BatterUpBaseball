package com.batterupbaseball;

public class Game {
    Team vTeam = new Team();
    Team hTeam = new Team();

    Inning inning = new Inning(); // tracks outs and inning
    Baserunners baserunners = new Baserunners();
    int[] maxRange = new int[9]; // always for current batter, sets the max range for each category (1b, 2b, 3b, hr, bb, etc.)
    Die die1 = new Die();
    Die die2 = new Die();

    public void Game() {
        setupTeams();
        setupInning();
        setupNewDice();
        setupBaseRunners();
    }

    private void setupTeams() {
        Team thisTeam = new Team();
        vTeam = thisTeam;
        thisTeam = new Team();
        hTeam = thisTeam;
    }
    private void setupBaseRunners() {
        Baserunners thisBaserunners = new Baserunners();
        baserunners = thisBaserunners;
    }

    private void setupNewDice() {
        Die die = new Die();
        die1 = die;
        die = new Die();
        die2 = die;
    }

    private void setupInning() {
        Inning thisInning = new Inning();
        inning = thisInning;
    }
}