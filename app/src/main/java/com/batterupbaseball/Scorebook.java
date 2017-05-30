package com.batterupbaseball;

public class Scorebook {
    int vTeamRuns, hTeamRuns, vTeamHits, hTeamHits, vTeamErrors, hTeamErrors;

    public void Scorebook() {
        this.vTeamRuns = 0;
        this.hTeamRuns = 0;
        this.vTeamHits = 0;
        this.hTeamHits = 0;
        this.vTeamErrors = 0;
        this.hTeamErrors = 0;
    }

    public void setvRuns(int num) {
        this.vTeamRuns += num;
    }
    public int getvRuns() {
        return this.vTeamRuns;
    }
    public void sethRuns(int num) {
        this.hTeamRuns += num;
    }
    public int gethRuns() {
        return this.hTeamRuns;
    }
    public void setvHits() {
        this.vTeamHits += 1;
    }
    public int getvHits() {
        return this.vTeamHits;
    }
    public void sethHits() {
        this.hTeamHits += 1;
    }
    public int gethHits() {
        return this.hTeamHits;
    }
    public void setvErrors() {
        this.vTeamErrors += 1;
    }
    public int getvErrors() {
        return this.vTeamErrors;
    }
    public void sethErrors() {
        this.hTeamErrors += 1;
    }
    public int gethErrors() {
        return this.hTeamErrors;
    }
}
