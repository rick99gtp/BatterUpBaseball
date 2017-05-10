package com.batterupbaseball;

public class season_class {
    private String season;

    public season_class(String season_name) {
        super();
        this.season = season_name;
    }

    public void setSeasonName(String season_name) {
        this.season = season_name;
    }

    public String getSeasonName() {
        return this.season;
    }
}
