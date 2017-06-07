package com.batterupbaseball;

public class Player {

    // max number of each range
    int[] pVsl = new int[7];
    int[] pVsr = new int[7];
    int[] sprayChart = new int[3]; // pull, center, oppo
    int[] ballSpeed = new int[3]; // soft, med, hard
    int[] running = new int[2]; // baserunning, stealing
    int[] defense = new int[4]; // arm_rating, defense_rating, fld_range, fld_error;

    String pBats, pThrows, name, pos;
    int sac_bunt, spd_rating, avoid_dp, hold_rating, rsb, vsl_rating, vsr_rating, con_rating, pwr_rating, pValue;
    String special_text;
    String role; // starter, bullpen, bench

    int _id = 0;

    // game stats
    double gameG, gamePA, gameAB, gameH, gameR, gameRBI, game1B, game2B, game3B, gameHR, gameBB, gameSO, gameHBP, gameSB, gameCS, gameGDP;
    double gameIP, gameGS, gameER, gameP_BB, gameP_SO, gameP_1B, gameP_2B, gameP_3B, gameP_HR, gameP_HBP;
    double gamePO, gameA, gameE, gameFLD_PCT;
    
    // season stats
    double G, PA, AB, H, R, RBI, B1, B2, B3, HR, BB, SO, HBP, SB, CS, GDP, AVG, SLG, OBP, OPS;
    double IP, GS, ER, P_BB, P_SO, P_1B, P_2B, P_3B, P_HR, P_HBP, P_H, W, L, S, BF, ERA;
    double PO, A, E;
    double fldPCT;
    
    public void Player() {
        
    }

    // setters
    public void setRole(String role) {
        this.role = role;
    }

    public void setBats(String bats) {
        this.pBats = bats;
    }
    public void setThrows(String pThrows) {
        this.pThrows = pThrows;
    }
    public void setName(String name) {
        this.name = name;
    }
    public void setPos(String pos) {
        this.pos = pos;
    }
    public void setSpd_rating(int num) {
        this.spd_rating = num;
    }
    public void setVslRatings(int num_1b, int num_2b, int num_3b, int num_hr, int num_bb, int num_so, int num_hbp) {
        this.pVsl[0] = num_1b;
        this.pVsl[1] = num_2b;
        this.pVsl[2] = num_3b;
        this.pVsl[3] = num_hr;
        this.pVsl[4] = num_bb;
        this.pVsl[5] = num_so;
        this.pVsl[6] = num_hbp;
    }
    public void setVsrRatings(int num_1b, int num_2b, int num_3b, int num_hr, int num_bb, int num_so, int num_hbp) {
        this.pVsr[0] = num_1b;
        this.pVsr[1] = num_2b;
        this.pVsr[2] = num_3b;
        this.pVsr[3] = num_hr;
        this.pVsr[4] = num_bb;
        this.pVsr[5] = num_so;
        this.pVsr[6] = num_hbp;
    }
    public void setSac_bunt(int num) {
        this.sac_bunt = num;
    }
    public void setSprayChart(int pull, int center, int oppo) {
        sprayChart[0] = pull;
        sprayChart[1] = center;
        sprayChart[2] = oppo;
    }
    public void setBallSpeed(int soft, int med, int hard) {
        ballSpeed[0] = soft;
        ballSpeed[1] = med;
        ballSpeed[2] = hard;
    }
    public void setRunning(int baserunning, int stealing) {
        running[0] = baserunning;
        running[1] = stealing;
    }
    public void setAvoid_dp(int num) {
        this.avoid_dp = num;
    }
    public void setHold_rating(int num) {
        this.hold_rating = num;
    }
    public void setRsb(int num) {
        this.rsb = num;
    }
    public void setDefense(int arm_rating, int defense_rating) {
        defense[0] = arm_rating;
        defense[1] = defense_rating;
    }
    public void setRatings(int vsl_rating, int vsr_rating) {
        this.vsl_rating = vsl_rating;
        this.vsr_rating = vsr_rating;
    }
    public void setConRating(int con_rating) {
        this.con_rating = con_rating;
    }
    public void setPwrRating(int pwr_rating) {
        this.pwr_rating = pwr_rating;
    }
    public void setSpecialText(String text) {
        this.special_text = text;
    }
    public void setValue(int value) {
        this.pValue = value;
    }

    // getters
    public int getSpd_rating() {
        return this.spd_rating;
    }
    public String getRole() {
        return this.role;
    }

    public String getBats() {
        return this.pBats;
    }
    public String getThrows() {
        return this.pThrows;
    }
    public String getName() {
        return this.name;
    }
    public String getPos() {
        return this.pos;
    }
    public int[] getVslRatings() {
        return this.pVsl;
    }
    public int[] getVsrRatings() {
        return this.pVsr;
    }
    public int getSac_bunt() {
        return this.sac_bunt;
    }
    public int[] getSprayChart() {
        return this.sprayChart;
    }
    public int[] getBallSpeed() {
        return this.ballSpeed;
    }
    public int[] getRunning() {
        return this.running;
    }
    public int getAvoid_dp() {
        return this.avoid_dp;
    }
    public int getHold_rating() {
        return this.hold_rating;
    }
    public int setRsb() {
        return this.rsb;
    }
    public int[] getDefense() {
        return defense;
    }
    public int getRatings(int rating) {
        if(rating==1)
            return vsl_rating;
        else
            return vsr_rating;
    }
    public int getConRating() {
        return con_rating;
    }
    public int getPwrRating() {
        return pwr_rating;
    }
    public String getSpecialText() {
        return this.special_text;
    }
    public int getValue() {
        return this.pValue;
    }

    public String toString() {
        return name+ "(" + pos + ")";
    }

    public boolean hasSpeed() {
        if(spd_rating > 5)
            return true;
        else
            return false;
    }
}
