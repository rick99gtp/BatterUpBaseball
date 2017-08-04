package com.batterupbaseball;

import android.util.Log;

import java.util.ArrayList;

public class Team {
    String name;
    int year;

    ArrayList<Player> roster;
    ArrayList<Player> bullpen;
    ArrayList<Player> starters;

    public Team() {
        roster = new ArrayList<Player>();
        bullpen = new ArrayList<Player>();
        starters = new ArrayList<Player>();
    }

    public void addPlayerToRoster(Player p) {
        roster.add(p);
        if(p.pos.equals("rp") || p.pos.equals("cl"))
            bullpen.add(p);
        else if(p.pos.equals("sp"))
            starters.add(p);
    }

    public Player getPlayer(int _id) {
        Player target = roster.get(0);
        for(Player p : roster) {
            if(p._id == _id)
                target = p;
        }
        return target;
    }
}
