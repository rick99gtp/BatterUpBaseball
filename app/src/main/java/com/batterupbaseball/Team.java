package com.batterupbaseball;

import java.util.ArrayList;

public class Team {
    String name;
    ArrayList<Player> lineup;
    ArrayList<Player> bench;
    ArrayList<Player> bullpen;
    ArrayList<Player> starters;
    int[] lineupID;
    int[] benchID;
    int[] bullpenID;
    int[] startersID;

    public Team() {
        lineup = new ArrayList<Player>();
        bench = new ArrayList<Player>();
        bullpen = new ArrayList<Player>();
        starters = new ArrayList<Player>();
    }

    public void addPlayerToLineup(Player aPlayer) {
        lineup.add(aPlayer);
    }

    public void addPlayerToBench(Player aPlayer) {
        bench.add(aPlayer);
    }

    public void addPlayerToBullpen(Player aPlayer) {
        bullpen.add(aPlayer);
    }

    public void addPlayerToStarters(Player aPlayer) {
        starters.add(aPlayer);
    }

    public int getBenchCount() {
        return benchID.length;
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
