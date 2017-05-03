package com.batterupbaseball;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.view.View;

import java.io.IOException;

public class startup_activity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.startup_activity);

        // Hide the status bar.
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);

        // create the newest players database
        final DataBaseHelper myDbHelper = new DataBaseHelper(this);

        try {
            myDbHelper.createDataBase();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // create the seasons_owned database
        final DataBaseHelperSeasonsOwned myDbHelperSeasonsOwned = new DataBaseHelperSeasonsOwned(this);

        try {
            myDbHelperSeasonsOwned.createDataBase();
        } catch (IOException e) {
            e.printStackTrace();
        }

        CardView season = (CardView) findViewById(R.id.card_view_1);
        season.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(startup_activity.this, seasons.class);
                startActivity(intent);
            }
        });

        CardView exhibition = (CardView) findViewById(R.id.card_view_2);
        exhibition.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(startup_activity.this, exhibition.class);
                startActivity(intent);
            }
        });

        CardView draft = (CardView) findViewById(R.id.card_view_3);
        draft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(startup_activity.this, draft.class);
                startActivity(intent);
            }
        });

        CardView store = (CardView) findViewById(R.id.card_view_4);
        store.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(startup_activity.this, store.class);
                startActivity(intent);
            }
        });
    }
}
