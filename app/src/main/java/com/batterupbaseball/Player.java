package com.batterupbaseball;

public class Player {

    // max number of each range
    int[] pVsl = new int[7];
    int[] pVsr = new int[7];
    int[] sprayChart = new int[3]; // pull, center, oppo
    int[] ballSpeed = new int[3]; // soft, med, hard
    int baseRunning;
    int stealing;
    int arm_rating;
    int defense_rating;
    int fld_range;
    int fld_error;
    int value;

    String pBats, pThrows, name, pos;
    int sac_bunt, avoid_dp, vsl_rating, vsr_rating, con_rating, pwr_rating;
    double spd_rating, hold_rating, rsb;
    String special_text;
    String role; // starter, bullpen, bench
    int staminaMax;
    int staminaCurrent;
    int staminaRunsGivenUpThisInning; // for every run beyond the 1st, -1 to stamina
    int balkRating;
    int wildPitchRating;
    int passedBallRating;

    int _id = 0;

    // game stats
    double gameG, gamePA, gameAB, gameH, gameR, gameRBI, game1B, game2B, game3B, gameHR, gameBB, gameSO, gameHBP, gameSB, gameCS, gameGDP, gamePASSEDBALL;
    double gameIP, gameGS, gameER, gameP_R, gameP_BB, gameP_SO, gameP_1B, gameP_2B, gameP_3B, gameP_HR, gameP_HBP, gameP_PA, gameP_H, gameWP, gameBALK;
    double gamePO, gameA, gameE, gameFLD_PCT;
    
    // season stats
    double G, PA, AB, H, R, RBI, B1, B2, B3, HR, BB, SO, HBP, SB, CS, GDP, AVG, SLG, OBP, OPS;
    double IP, GS, ER, P_BB, P_SO, P_1B, P_2B, P_3B, P_HR, P_HBP, P_H, W, L, S, BF, ERA;
    double PO, A, E;
    double fldPCT;
    
    public Player() {
        
    }

    public boolean hasSpeed() {
        if(spd_rating > 5)
            return true;
        else
            return false;
    }

}
