package com.batterupbaseball;

import java.util.ArrayList;

public class Team {
    String name;
    ArrayList<Player> lineup;
    int[] lineupID;
    int[] benchID;
    int[] bullpenID;
    int[] startersID;

    public Team() {
        lineup = new ArrayList<Player>();
    }

    public void addPlayerToLineup(Player aPlayer) {
        lineup.add(aPlayer);
    }

    public Player getPlayer(int _id) {
        Player target = lineup.get(0);
        for(Player p : lineup) {
            if(p._id == _id)
                target = p;
        }
        return target;
    }
}
