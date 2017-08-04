package com.batterupbaseball;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class exhibition_select_pitcher extends Activity implements View.OnClickListener {

    int[] llRow = {R.id.llRow1, R.id.llRow2, R.id.llRow3, R.id.llRow4, R.id.llRow5};
    int[] llThrows = {R.id.tvThrows1, R.id.tvThrows2, R.id.tvThrows3, R.id.tvThrows4, R.id.tvThrows5};
    int[] llName = {R.id.tvName1, R.id.tvName2, R.id.tvName3, R.id.tvName4, R.id.tvName5};
    int[] llVsLeft = {R.id.tvVsLeft1, R.id.tvVsLeft2, R.id.tvVsLeft3, R.id.tvVsLeft4, R.id.tvVsLeft5};
    int[] llVsRight = {R.id.tvVsRight1, R.id.tvVsRight2, R.id.tvVsRight3, R.id.tvVsRight4, R.id.tvVsRight5};
    LinearLayout[] llStarter = new LinearLayout[5];
    TextView[] tvThrows = new TextView[5];
    TextView[] tvName = new TextView[5];
    TextView[] tvVsLeft = new TextView[5];
    TextView[] tvVsRight = new TextView[5];

    int seasonID;
    String seasonFileName;
    String teamSelected; // V=Visitor, H=Home
    int teamID;
    SQLiteDatabase myDB = null;
    int[] starterID = new int[5];
    String[] playerName = new String[5];
    String[] playerThrows = new String[5];
    int[] playerDefense = new int[5];
    //int[] playerWins = new int[5];
    //int[] playerLosses = new int[5];
    //int[] playerPct = new int[5];
    int[] playerValueVsLeft = new int[5];
    int[] playerValueVsRight = new int[5];
    int[] playerContactVsLeft = new int[5];
    int[] playerContactVsRight = new int[5];
    int selectedStarter;
    boolean cardViewVsRight = true;
    String TAG = "com.batterupbaseball";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.exhibition_select_pitcher);

        // prepare the Views
        for(int i=0; i<5; i++) {
            llStarter[i] = (LinearLayout) findViewById(llRow[i]);
            llStarter[i].setOnClickListener(this);
            tvThrows[i] = (TextView) findViewById(llThrows[i]);
            tvName[i] = (TextView) findViewById(llName[i]);
            tvVsLeft[i] = (TextView) findViewById(llVsLeft[i]);
            tvVsRight[i] = (TextView) findViewById(llVsRight[i]);
        }

        TextView tvSelectPitcher = (TextView) findViewById(R.id.tvSelectPitcher);
        tvSelectPitcher.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(exhibition_select_pitcher.this, exhibition_select_lineup.class);
                startActivity(intent);
            }
        });

        ImageView ivCard = (ImageView) findViewById(R.id.ivCard);
        ivCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cardViewVsRight = !cardViewVsRight;
                getCardImage();
            }
        });

    }

    @Override
    public void onClick(View v) {
        resetColors();

        switch(v.getId()) {
            case R.id.llRow1:
                llStarter[0].setBackgroundColor(Color.RED);
                selectedStarter = 0;
                break;
            case R.id.llRow2:
                llStarter[1].setBackgroundColor(Color.RED);
                selectedStarter = 1;
                break;
            case R.id.llRow3:
                llStarter[2].setBackgroundColor(Color.RED);
                selectedStarter = 2;
                break;
            case R.id.llRow4:
                llStarter[3].setBackgroundColor(Color.RED);
                selectedStarter = 3;
                break;
            case R.id.llRow5:
                llStarter[4].setBackgroundColor(Color.RED);
                selectedStarter = 4;
                break;
            default:
                break;
        }

        SharedPreferences myPrefs = getSharedPreferences("prefsFile", 0);
        SharedPreferences.Editor editor = myPrefs.edit();
        editor.putInt("exhibition_user_starter", starterID[selectedStarter]);
        editor.apply();

        getCardImage();
    }

    private void resetColors() {
        for(int i=0; i<5; i++) {
            llStarter[i].setBackgroundColor(Color.TRANSPARENT);
        }
    }

    private void getSeasonFileName() {

        // open the season_owned database
        myDB = openOrCreateDatabase("seasons_owned.db", MODE_PRIVATE, null);

        Cursor seasonOwned = myDB.query("seasons", null, "_id='" + seasonID + "'", null, null, null, null);

        if(seasonOwned.moveToFirst()) {
            // database file name
            int colSeasonID = seasonOwned.getColumnIndex("seasonID");
            seasonFileName = seasonOwned.getString(colSeasonID);
        }

        // close the cursor
        seasonOwned.close();
        // close the database
        myDB.close();
    }

    private void getRotation() {
        String lastName, firstName;
        myDB = openOrCreateDatabase(seasonFileName, MODE_PRIVATE, null);

        // get player IDs
        Cursor cTeam = myDB.query("rotation", null, "team_id='" + teamID + "'", null, null, null, null);
        Log.d(TAG, "teamID: " + teamID);

        if(cTeam.moveToFirst()) {
            for (int i = 0; i < 5; i++) {
                int col = cTeam.getColumnIndex("sp" + (i + 1));
                starterID[i] = cTeam.getInt(col);
            }
        }

        cTeam.close();

        // get player names
        Cursor cPlayer = null;

        for(int i=0; i < 5; i++) {
            cPlayer = myDB.query("players", null, "_id='" + starterID[i] + "' AND team='" + teamID + "'", null, null, null, null);

            if(cPlayer.moveToFirst()) {
                int col = cPlayer.getColumnIndex("first_name");
                firstName = cPlayer.getString(col);

                col = cPlayer.getColumnIndex("last_name");
                lastName = cPlayer.getString(col);

                playerName[i] = firstName + " " + lastName;

                col = cPlayer.getColumnIndex("throws");
                playerThrows[i] = cPlayer.getString(col).toUpperCase();

                col = cPlayer.getColumnIndex("vsl_rating");
                playerValueVsLeft[i] = cPlayer.getInt(col);

                col = cPlayer.getColumnIndex("vsr_rating");
                playerValueVsRight[i] = cPlayer.getInt(col);

                col = cPlayer.getColumnIndex("defense_rating");
                playerDefense[i] = cPlayer.getInt(col);

                col = cPlayer.getColumnIndex("vsl_rating");
                playerContactVsLeft[i] = cPlayer.getInt(col);

                col = cPlayer.getColumnIndex("vsr_rating");
                playerContactVsRight[i] = cPlayer.getInt(col);
            }
        }

        cPlayer.close();

        myDB.close();
    }

    private void showRotation() {
        for(int i=0; i<5; i++) {
            tvThrows[i].setText(playerThrows[i]);
            tvName[i].setText(playerName[i]);
            tvVsLeft[i].setText("" + playerValueVsLeft[i]);
            tvVsRight[i].setText("" + playerValueVsRight[i]);
        }
    }

    private void highlightStarter() {
        // get season ID
        SharedPreferences myPrefs = getSharedPreferences("prefsFile", 0);
        selectedStarter = myPrefs.getInt("exhibition_selected_starter", 0);

        resetColors();

        llStarter[selectedStarter].setBackgroundColor(Color.RED);
    }

    private void getCardImage() {
        int[] cardValue = {R.drawable.value_1, R.drawable.value_2, R.drawable.value_3, R.drawable.value_4, R.drawable.value_5, R.drawable.value_6, R.drawable.value_7, R.drawable.value_8, R.drawable.value_9, R.drawable.value_10};
        int[] cardDefense = {R.drawable.defense_1, R.drawable.defense_2, R.drawable.defense_3, R.drawable.defense_4, R.drawable.defense_5, R.drawable.defense_6, R.drawable.defense_7, R.drawable.defense_8, R.drawable.defense_9, R.drawable.defense_10};
        int[] cardContact = {R.drawable.contact_1, R.drawable.contact_2, R.drawable.contact_3, R.drawable.contact_4, R.drawable.contact_5, R.drawable.contact_6, R.drawable.contact_7, R.drawable.contact_8, R.drawable.contact_9, R.drawable.contact_10};

        ImageView ivCard = (ImageView) findViewById(R.id.ivCard);

        Drawable[] layers = new Drawable[5];

        switch(playerThrows[selectedStarter]) {
            case "R":
                if(cardViewVsRight) {
                    // base
                    layers[0] = ContextCompat.getDrawable(this, R.drawable.pitcher_card_vsr_as_right);
                    // value
                    layers[1] = ContextCompat.getDrawable(this, cardValue[playerValueVsRight[selectedStarter]-1]);
                    // contact
                    layers[2] = ContextCompat.getDrawable(this, cardContact[playerContactVsRight[selectedStarter]-1]);
                }
                else {
                    // base
                    layers[0] = ContextCompat.getDrawable(this, R.drawable.pitcher_card_vsl_as_right);
                    // value
                    layers[1] = ContextCompat.getDrawable(this, cardValue[playerValueVsLeft[selectedStarter]-1]);
                    layers[2] = ContextCompat.getDrawable(this, cardContact[playerContactVsLeft[selectedStarter]-1]);
                }

                break;
            case "L":
                if(cardViewVsRight) {
                    // base
                    layers[0] = ContextCompat.getDrawable(this, R.drawable.pitcher_card_vsr_as_left);
                    // value
                    layers[1] = ContextCompat.getDrawable(this, cardValue[playerValueVsRight[selectedStarter]-1]);
                    layers[2] = ContextCompat.getDrawable(this, cardContact[playerContactVsRight[selectedStarter]-1]);
                }
                else {
                    // base
                    layers[0] = ContextCompat.getDrawable(this, R.drawable.pitcher_card_vsl_as_left);
                    // value
                    layers[1] = ContextCompat.getDrawable(this, cardValue[playerValueVsLeft[selectedStarter]-1]);
                    layers[2] = ContextCompat.getDrawable(this, cardContact[playerContactVsLeft[selectedStarter]-1]);
                }

                break;
            default:
                break;
        }

        // defense
        layers[3] = ContextCompat.getDrawable(this, cardDefense[playerDefense[selectedStarter]-1]);

        // position
        layers[4] = ContextCompat.getDrawable(this, R.drawable.pos_sp);

        LayerDrawable layerDrawable = new LayerDrawable(layers);
        ivCard.setImageDrawable(layerDrawable);

    }

    @Override
    protected void onResume() {
        super.onResume();

        SharedPreferences myPrefs = getSharedPreferences("prefsFile", 0);
        teamSelected = myPrefs.getString("USER_TEAM", "V");

        if(teamSelected.equals("V")) {
            seasonID = myPrefs.getInt("vSeasonID", 1);
            teamID = myPrefs.getInt("vTeamID", 1);
        }
        else {
            seasonID = myPrefs.getInt("hSeasonID", 1);
            teamID = myPrefs.getInt("hTeamID", 2);
        }

        // get the season file name
        getSeasonFileName();
        // get rotation
        getRotation();
        showRotation();

        // highlight starter selected
        highlightStarter();

        SharedPreferences.Editor editor = myPrefs.edit();
        editor.putInt("exhibition_user_starter", starterID[selectedStarter]);
        editor.apply();

        // get the card image and put in imageview
        getCardImage();
    }
}