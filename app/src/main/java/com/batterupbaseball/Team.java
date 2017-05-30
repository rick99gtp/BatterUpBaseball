package com.batterupbaseball;

/**
 * Created by Rick on 5/29/2017.
 */

public class Team {
    String Teamname;
    Player[] vPlayer = new Player[25];
    Player[] hPlayer = new Player[25];

    public void Team(String teamname) {
        this.Teamname = teamname;
        setupPlayers();
    }

    private void setupPlayers() {
        // allocate space and assign a Player object to the array
        for(int i=0; i < 25; i++) {
            Player thisPlayer = new Player();
            vPlayer[i] = thisPlayer;
            thisPlayer = new Player();
            hPlayer[i] = thisPlayer;
        }
    }
}
