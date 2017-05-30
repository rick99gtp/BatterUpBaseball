package com.batterupbaseball;

public class Baserunners {
    int runnerOn1st, runnerOn2nd, runnerOn3rd;
    int speedOn1st, speedOn2nd, speedOn3rd;

    public void Baserunners() {
        this.runnerOn1st = 0;
        this.runnerOn2nd = 0;
        this.runnerOn3rd = 0;

        this.speedOn1st = 0;
        this.speedOn2nd = 0;
        this.speedOn3rd = 0;
    }

    // stealing rating
    public void setSpeedOn1st(int num) {
        this.speedOn1st = num;
    }
    public void setSpeedOn2nd(int num) {
        this.speedOn2nd = num;
    }
    public void setSpeedOn3rd(int num) {
        this.speedOn3rd = num;
    }

    public int getSpeedOn1st() {
        return this.speedOn1st;
    }
    public int getSpeedOn2nd() {
        return this.speedOn2nd;
    }
    public int getSpeedOn3rd() {
        return this.speedOn3rd;
    }

    // playerID of runners on base
    public void setRunnerOn1st(int num) {
        this.runnerOn1st = num;
    }
    public void setRunnerOn2nd(int num) {
        this.runnerOn2nd = num;
    }
    public void setRunnerOn3rd(int num) {
        this.runnerOn3rd = num;
    }

    public int getRunnerOn1st() {
        return this.runnerOn1st;
    }
    public int getRunnerOn2nd() {
        return this.runnerOn2nd;
    }
    public int getRunnerOn3rd() {
        return this.runnerOn3rd;
    }
}
