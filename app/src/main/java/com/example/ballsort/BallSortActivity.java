package com.example.ballsort;

import android.app.Activity;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.util.Log;
import android.view.Surface;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.Nullable;

public class BallSortActivity extends Activity {

    final static String MYDEBUG = "MYDEBUG"; // for Log.i messages

    // parameters from the Setup dialog
    int noTs;
    String gType;
    BallPanel bP;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.i(MYDEBUG, "Got here! (BallSortActivity - onCreate)");

        requestWindowFeature(Window.FEATURE_NO_TITLE);  // no title bar
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.main);

        // get parameters selected by user from setup dialog
        Bundle b = getIntent().getExtras();
        noTs = b.getInt("noTs");
        gType = b.getString("gType");

        bP = (BallPanel)findViewById(R.id.ballpanel);
        bP.configure(noTs, gType);
        bP.onWindowFocusChanged(true);


    }

}
