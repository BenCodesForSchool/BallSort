package com.example.ballsort;

import android.app.Activity;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.util.Log;

import androidx.annotation.Nullable;

public class BallSortActivity extends Activity {

    final static String MYDEBUG = "MYDEBUG"; // for Log.i messages
    int n = 567890;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.i(MYDEBUG, "Got here! (BallSortActivity - onCreate)");

        setContentView(R.layout.setup);
    }
}
