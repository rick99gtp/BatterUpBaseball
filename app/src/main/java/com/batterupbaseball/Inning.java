package com.batterupbaseball;

public class Inning {
    int outs;
    Boolean topOrBottom; // true = top, false = bottom
    int inning;

    public void Inning() {
        this.outs = 0;
        this.topOrBottom = true;
        inning = 1;
    }

    public boolean getHalfInning() {
        return this.topOrBottom;
    }

    public void nextHalfInning() {
        if(this.topOrBottom) {
            this.topOrBottom = false;
        }
        else {
            this.topOrBottom = true;
            this.inning += 1;
        }
    }

    public int getOuts() {
        return this.outs;
    }

    public void setOuts() {
        this.outs += 1;
    }

    public int getInning() {
        return inning;
    }
}