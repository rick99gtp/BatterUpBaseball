package com.batterupbaseball;

public class teams {

    private String TeamName;
    private int battingRating;
    private int pitchingRating;
    private int fieldingRating;
    private int teamColor1;
    private int teamColor2;

    public teams(String TeamName, int battingRating, int pitchingRating, int fieldingRating, int teamColor1, int teamColor2) {
        super();
        this.battingRating = battingRating;
        this.TeamName = TeamName;
        this.pitchingRating = pitchingRating;
        this.fieldingRating = fieldingRating;
        this.teamColor1 = teamColor1;
        this.teamColor2 = teamColor2;
    }
    // getters and setters...
    public int getBattingRating() {
        return this.battingRating;
    }

    public int getPitchingRating() {
        return this.pitchingRating;
    }

    public int getFieldingRating() {
        return this.fieldingRating;
    }

    public String getTeamName() {
        return this.TeamName;
    }

    public int getTeamColor1() {
        return this.teamColor1;
    }

    public int getTeamColor2() {
        return this.teamColor2;
    }

    public void setTeamName(String teamName) {
        this.TeamName = teamName;
    }

    public void setBattingRating(int battingRating) {
        this.battingRating = battingRating;
    }

    public void setPitchingRating(int pitchingRating) {
        this.pitchingRating = pitchingRating;
    }

    public void setFieldingRating(int fieldingRating) {
        this.fieldingRating = fieldingRating;
    }

    public void setTeamColor1(int teamColor1) {
        this.teamColor1 = teamColor1;
    }

    public void setTeamColor2(int teamColor2) {
        this.teamColor2 = teamColor2;
    }
}