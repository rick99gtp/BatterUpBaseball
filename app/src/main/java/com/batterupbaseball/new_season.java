package com.batterupbaseball;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;

/**
 * Created by Rick on 4/30/2017.
 */

public class new_season extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_season);

        // Hide the status bar.
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);
    }
}
