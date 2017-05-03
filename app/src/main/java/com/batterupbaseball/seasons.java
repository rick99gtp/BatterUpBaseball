package com.batterupbaseball;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.view.View;

public class seasons extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.seasons);

        // Hide the status bar.
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);

        CardView newSeason = (CardView) findViewById(R.id.card_view_1);
        newSeason.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(seasons.this, new_season.class);
                startActivity(intent);
            }
        });

        CardView continueSeason = (CardView) findViewById(R.id.card_view_2);
        continueSeason.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(seasons.this, continue_season.class);
                startActivity(intent);
            }
        });
    }
}
