package com.batterupbaseball;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;

/**
 * Created by Rick on 5/29/2017.
 */

public class playball extends Activity {
    Game game;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.playball);

        initGame();
    }

    private void initGame() {
        game = new Game();

        getVPlayers();
        getHPlayers();
    }

    private void getVPlayers() {

    }

    private void getHPlayers() {

    }
}
