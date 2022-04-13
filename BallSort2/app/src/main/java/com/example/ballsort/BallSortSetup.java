package com.example.ballsort;

import androidx.appcompat.app.AppCompatActivity;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.util.Log;

import android.os.Bundle;

public class BallSortSetup extends Activity {

    final static String[] NUM_OF_TRIALS = {"5", "10", "15"};
    final static String[] GESTURE_TYPE = {"Fling", "Tap"};
    Spinner spinNumTrials, spinGesType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.setup);


        spinNumTrials = (Spinner) findViewById(R.id.paramNoLaps);
        ArrayAdapter<CharSequence> adapter5 = new ArrayAdapter<CharSequence>(this, R.layout.spinnerstyle, NUM_OF_TRIALS);
        spinNumTrials.setAdapter(adapter5);
        spinNumTrials.setSelection(0);

        spinGesType = (Spinner) findViewById(R.id.paramBallTypeSpinner);
        ArrayAdapter<CharSequence> adapter2 = new ArrayAdapter<CharSequence>(this, R.layout.spinnerstyle, GESTURE_TYPE);
        spinGesType.setAdapter(adapter2);

    }

    public void clickOK(View view)
    {
        // get user's choices...
        String numtrials = (String) spinNumTrials.getSelectedItem();
        String gType = (String) spinGesType.getSelectedItem();


        int noTs;
        if(numtrials.equals("5"))
            noTs = 5;
        else if(numtrials.equals("10"))
            noTs = 10;
        else
            noTs = 15;


        // bundle up parameters to pass on to activity
        Bundle b = new Bundle();
        b.putString("gType", gType);
        b.putInt("noTs", noTs);


        // start experiment activity
        Intent i = new Intent(getApplicationContext(), BallSortActivity.class);
        i.putExtras(b);
        startActivity(i);

        // comment out (return to setup after clicking BACK in main activity
        finish();
    }

    public void clickExit(View view)
    {
        super.onDestroy(); // cleanup
        this.finish(); // terminate
    }

}
