package com.example.ballsort;

import static android.view.MotionEvent.INVALID_POINTER_ID;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.net.Uri;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.Process;
import android.os.Vibrator;
import android.provider.Settings;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.RequiresApi;
import androidx.constraintlayout.solver.widgets.Rectangle;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class BallPanel extends View {

    final static int TIMER_DELAY = 5;
    private final static String DATA_DIRECTORY = "/BallSortData/";
    final static String MYDEBUG = "MYDEBUG"; // for //Log.i messages
    private BufferedWriter sd3, sd4;
        long startTime, endTime;
    Bitmap ball, blueBallBitmap, redBallBitmap, greenBallBitmap, magentaBallBitmap, orangeBallBitmap, turquoiseBallBitmap, violetBallBitmap, yellowBallBitmap;
    String ballString;
    int ballDiameter;
    RectF[] squares = new RectF[9];
    float pixelDensity, veloX, veloY;

    int trials;
    float[] taptimes;
    float[] flingtimes;
    float[] tapSucs, flingSucs;
    int tapTrialsDone, flingTrialsDone, tapSuccesses, flingSuccesses;
    boolean flinging;
    boolean ballFlings;


    RectF innerRectangle, outerRectangle, innerShadowRectangle, outerShadowRectangle, ballNow, upLeft, upMid, upRight, midLeft, midRight, bLeft, bMid, bRight, whiteRect, target;
    boolean touchFlag, oWFC, canDrawBall, ballSelected, ballTapped, moving;
    Vibrator vib;
    int wallHits, activePointerId;;
    private float lastTouchX;
    private float lastTouchY;

    float xBall, yBall, whiteLeft, whiteRight, whiteTop, whiteBottom; // top-left of the ball (for painting)
    float xBallCenter, yBallCenter; // center of the ball
    private GestureDetector gestureDetector;
    private float flingVelocity;
    private float flingAngle;
    CountDownTimer flingTimer;
    private File f2;
    // parameters from Setup dialog
    String gType;
    int noTs;

    float xCenter, yCenter; // the center of the screen
    long now, lastT;
    Paint bluePaint, greenPaint, orangePaint, magentaPaint, yellowPaint, violetPaint, turquoisePaint,  redPaint, whitePaint, targetPaint;
    Paint[] masterPaints = new Paint[9];

    @RequiresApi(api = Build.VERSION_CODES.R)
    public BallPanel(Context contextArg)
    {
        super(contextArg);
        initialize(contextArg);
    }

    @RequiresApi(api = Build.VERSION_CODES.R)
    public BallPanel(Context contextArg, AttributeSet attrs)
    {
        super(contextArg, attrs);
        initialize(contextArg);
    }

    @RequiresApi(api = Build.VERSION_CODES.R)
    public BallPanel(Context contextArg, AttributeSet attrs, int defStyle)
    {
        super(contextArg, attrs, defStyle);
        initialize(contextArg);
    }

    @RequiresApi(api = Build.VERSION_CODES.R)
    private void initialize(Context c)
    {
        redPaint = new Paint();
        redPaint.setColor(Color.RED);
        redPaint.setStyle(Paint.Style.FILL);

        bluePaint = new Paint();
        bluePaint.setColor(Color.BLUE);
        bluePaint.setStyle(Paint.Style.FILL);

        greenPaint = new Paint();
        greenPaint.setColor(Color.parseColor("#90FF00"));
        greenPaint.setStyle(Paint.Style.FILL);

        violetPaint = new Paint();
        violetPaint.setColor(Color.parseColor("#622DD1"));
        violetPaint.setStyle(Paint.Style.FILL);

        orangePaint = new Paint();
        orangePaint.setColor(Color.parseColor("#FF9000"));
        orangePaint.setStyle(Paint.Style.FILL);

        magentaPaint = new Paint();
        magentaPaint.setColor(Color.parseColor("#900090"));
        magentaPaint.setStyle(Paint.Style.FILL);

        yellowPaint = new Paint();
        yellowPaint.setColor(Color.YELLOW);
        yellowPaint.setStyle(Paint.Style.FILL);

        turquoisePaint = new Paint();
        turquoisePaint.setColor(Color.parseColor("#009090"));
        turquoisePaint.setStyle(Paint.Style.FILL);

        whitePaint = new Paint();
        whitePaint.setColor(Color.WHITE);
        whitePaint.setStyle(Paint.Style.FILL);

        masterPaints[0] = redPaint;
        masterPaints[1] = bluePaint;
        masterPaints[2] = greenPaint;
        masterPaints[3] = orangePaint;
        masterPaints[4] = violetPaint;
        masterPaints[5] = magentaPaint;
        masterPaints[6] = yellowPaint;
        masterPaints[7] = turquoisePaint;
        masterPaints[8] = whitePaint;

        // NOTE: we'll create the actual bitmap in onWindowFocusChanged
        blueBallBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.blueball);
        greenBallBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.greenball);
        redBallBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.redball);
        magentaBallBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.magentaball);
        violetBallBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.violetball);
        orangeBallBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.orangeball);
        yellowBallBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.yellowball);
        turquoiseBallBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.turquoiseball);


        lastT = System.nanoTime();
        this.setBackgroundColor(Color.LTGRAY);
       // //Log.i(MYDEBUG, String.valueOf(this.getBackground()));
        touchFlag = false;
        outerRectangle = new RectF();
        innerRectangle = new RectF();
        innerShadowRectangle = new RectF();
        outerShadowRectangle = new RectF();
        upLeft = new RectF();
        upMid = new RectF();
        upRight = new RectF();
        midLeft = new RectF();
        midRight = new RectF();
        bLeft = new RectF();
        bRight = new RectF();
        bMid = new RectF();
        ballNow = new RectF();
        whiteRect = new RectF();
        target = new RectF();

        wallHits = 0;

        vib = (Vibrator)c.getSystemService(Context.VIBRATOR_SERVICE);

        xCenter = this.getWidth() / 2f;
        yCenter = this.getHeight() / 2f;
        gestureDetector = new GestureDetector(c, new MyGestureListener());
        flingTimer = new CountDownTimer(TIMER_DELAY, TIMER_DELAY)
        {
            public void onTick(long millisUntilFinished)
            {
                ////Log.i(MYDEBUG, "ticking");
            }

            @RequiresApi(api = Build.VERSION_CODES.Q)
            public void onFinish()
            {
                doFling();
            }
        };

        pixelDensity = c.getResources().getDisplayMetrics().density;


        String parent = String.valueOf(Environment.getExternalStorageDirectory());
        ////Log.i(MYDEBUG ,"HERE IT IS BUDDY " + parent);
        if (Environment.isExternalStorageManager()) {
            File dataDirectory = new File(Environment.getExternalStorageDirectory() +
                    DATA_DIRECTORY);
            ////Log.i(MYDEBUG, "HERE IS THE DATA DIRECTORY PRE: "+ dataDirectory);
            if (!dataDirectory.exists() && !dataDirectory.mkdirs())
            {
                ////Log.i(MYDEBUG, "does it exist? " + dataDirectory.exists() + "  can it be made? " + dataDirectory.mkdirs());
                Log.e(MYDEBUG, "ERROR --> FAILED TO CREATE DIRECTORY: " + DATA_DIRECTORY);
            }
            if(f2 == null)
            {
                f2 = new File(dataDirectory, "BallSortStuff" + ".sd2");
            }
            if(!f2.exists())
            {
                f2 = new File(dataDirectory, "BallSortStuff" + ".sd2");
            }
        } else {
            //request for the permission
            Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
            Uri uri = Uri.fromParts("package","com.example.ballsort", null);
            intent.setData(uri);
            this.getContext().startActivity(intent);
           // //Log.i(MYDEBUG, "Reached the end of initialize");
        }


        /**
         * The following do-loop creates data files for output and a string sd2Leader to write to the sd2
         * output files.  Both the filenames and the sd2Leader are constructed by combining the setup parameters
         * so that the filenames and sd2Leader are unique and also reveal the conditions used for the block of input.
         *
         * The block code begins "B01" and is incremented on each loop iteration until an available
         * filename is found.  The goal, of course, is to ensure data files are not inadvertently overwritten.
         */


    }
    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    public boolean onTouchEvent(MotionEvent me)
    {
        gestureDetector.onTouchEvent(me);
        final int action = me.getAction();
        switch (action & MotionEvent.ACTION_MASK) {
            // --------------------------
            case MotionEvent.ACTION_DOWN: {
                final float x = me.getX();
                final float y = me.getY();

                // determine if the initial touch point is inside the image
                float left = xBall;
                float top = yBall;
                float right = left + ballDiameter;
                float bottom = top + ballDiameter;
                RectF r = new RectF(left, top, right, bottom);
                boolean inside = r.contains(x, y);

                // only begin the gesture if the touch point is inside the image
                if (inside) {
                    /*
                     * imageSelected is used to ensure the gestures only have their effect if the
                     * initial ACTION_DOWN was inside the images. See ACTION_MOVE, onScaleBegin,
                     * onScale, and onFling. This flag is cleared on ACTION_UP.
                     */
                    ballSelected = true;
                    lastTouchX = x;
                    lastTouchY = y;
                    startTime = System.currentTimeMillis();

                    // save the ID of this pointer
                    activePointerId = me.getPointerId(0);
                } else if (!ballSelected && ballTapped)
                {
                    final float i = me.getX();
                    final float j = me.getY();

                    // determine if the initial touch point is inside the image
                    xBall = i;
                    yBall = j;
                    xBallCenter = xBall + ballDiameter / 2f;
                    yBallCenter = yBall + ballDiameter / 2f;
                    invalidate();

                }
                break;
            }

            // --------------------------
            case MotionEvent.ACTION_MOVE: {

                // ignore the move gesture if the initial ACTION_DOWN was outside the image
                if (!ballSelected)
                    break;
                if(flinging)
                {
                    moving = true;
                    if (activePointerId != INVALID_POINTER_ID) {
                        // find the index of the active pointer and fetch its position
                        final int pointerIndex = me.findPointerIndex(activePointerId);
                        final float x = me.getX(pointerIndex);
                        final float y = me.getY(pointerIndex);

                        // use this position to compute the 'delta' for the image
                        final float dx = x - lastTouchX;
                        final float dy = y - lastTouchY;
                        xBall += dx;
                        yBall += dy;
                        invalidate();

                        // the current pointer position becomes the last position
                        lastTouchX = x;
                        lastTouchY = y;
                    }
                }

                break;
            }

            // -------------------------
            case MotionEvent.ACTION_UP:
            {
                if(!flinging)
                {
                    if(ballTapped && !ballSelected)
                    {
                        endTime = System.currentTimeMillis();
                        if(target.contains(xBall, yBall))
                        {
                            tapSuccesses += 1;
                            tapSucs[tapTrialsDone] = 1;
                        }
                        float totaltime = endTime - startTime;
                        taptimes[tapTrialsDone] = totaltime/1000;
                        ////Log.i(MYDEBUG, "This much time: " + taptimes[tapTrialsDone]);
                        tapTrialsDone += 1;
                        if(tapTrialsDone == trials)
                        {

                            StringBuilder sd2Data = new StringBuilder(100);
                            sd2Data.append("Tap trial results:  \n");
                            for(int o = 0; o < trials; o++)
                            {
                                sd2Data.append(String.format(Locale.CANADA, "%.2f,", taptimes[o]));
                                sd2Data.append(String.format(Locale.CANADA, "%.2f,", tapSucs[o]));


                            }
                            sd2Data.append("\n");
                            // sd1.write(sd1Stuff.toString(), 0, sd1Stuff.length());
                            //sd1.flush();
                            if(f2 == null)
                            {
                                ////Log.i(MYDEBUG, "Jesus fucking Christ");
                            }
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                                if (Environment.isExternalStorageManager()) {
                                    File dataDirectory = new File(Environment.getExternalStorageDirectory() +
                                            DATA_DIRECTORY);
                                    //Log.i(MYDEBUG, "HERE IS THE DATA DIRECTORY TAP: "+ dataDirectory);
                                    if (!dataDirectory.exists() && !dataDirectory.mkdirs())
                                    {
                                        //Log.i(MYDEBUG, "does it exist? " + dataDirectory.exists() + "  can it be made? " + dataDirectory.mkdirs());
                                        Log.e(MYDEBUG, "ERROR --> FAILED TO CREATE DIRECTORY: " + DATA_DIRECTORY);
                                    }
                                    if(f2 == null)
                                    {
                                        f2 = new File(dataDirectory, "BallSortStuff" + ".sd2");
                                    }
                                    if(!f2.exists())
                                    {
                                        f2 = new File(dataDirectory, "BallSortStuff" + ".sd2");
                                    }



                                    try
                                    {

                                        sd3 = new BufferedWriter(new FileWriter(f2, true));
                                        sd3.write(sd2Data.toString(), 0, sd2Data.length());
                                        //Log.i(MYDEBUG, "It's been written");
                                        sd3.close();

                                    } catch (IOException e)
                                    {
                                        Log.e(MYDEBUG, "ERROR OPENING DATA FILES! e=" + e.toString());

                                    }
                                } else {
                                    //request for the permission
                                    Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                                    Uri uri = Uri.fromParts("package","com.example.ballsort", null);
                                    intent.setData(uri);
                                    this.getContext().startActivity(intent);
                                }
                            }


                            Intent i  = new Intent(this.getContext(), BallSortSetup.class);
                            this.getContext().startActivity(i);
                            android.os.Process.killProcess(android.os.Process.myPid());
                        }
                        //Log.i(MYDEBUG, "taptrialsdone = " + tapTrialsDone + " tapsuccesses " + tapSuccesses);
                        ballTapped = false;
                        onWindowFocusChanged(true);

                    }
                    if(ballSelected && !ballTapped)
                    {
                        ballTapped = true;
                    }
                }
                ballSelected = false;
                break;
            }
            case MotionEvent.ACTION_CANCEL: {
                activePointerId = INVALID_POINTER_ID;
                ballSelected = false;
                break;
            }
        }

        return true;
    }
    @RequiresApi(api = Build.VERSION_CODES.Q)
    private void doFling()
    {
        //flingVelocity = (float)Math.sqrt(veloX * veloX + veloY * veloY);
        //flingAngle = (float)Math.atan2(veloY, veloX);
        float FACTOR = 200f; // reduction in distance moved with each update
        float DIVISOR = 1.1f; // reduction in velocity with each update
        float THRESHOLD = .1f; // determines when the animation finishes
        if(xBall <= 0.00 || xBall + ballDiameter >= xCenter * 2)
        {
            //if(0)
            //Log.i(MYDEBUG, "IT'S OFF THE SIDE");
            veloX *= -1;
            flingVelocity = (float)Math.sqrt(veloX * veloX + veloY * veloY);
            flingAngle = (float)Math.atan2(veloY, veloX);
            //Log.i(MYDEBUG, "velox = " + veloX + " veloy =" + veloY);
            DIVISOR = 2.2f;
            //dx = (float)Math.cos(flingAngle) * (flingVelocity / FACTOR);
            //dy = (float)Math.sin(flingAngle) * (flingVelocity / FACTOR);
            //dx *= pixelDensity;
            //dy *= pixelDensity;
            // dx *= 2;
            //dy *= 2;

            //xBall += dx;
            //yBall += dy;

        }
        if (yBall >= yCenter * 2 || yBall - ballDiameter <= 0.00)
        {
            veloY *= -1;
            flingVelocity = (float)Math.sqrt(veloX * veloX + veloY * (.5) * veloY * 0.5);
            flingAngle = (float)Math.atan2(veloY, veloX);
            DIVISOR = 2.2f;
        }
        // fiddle with these constants, as necessary, to get good fling motion

        //Log.i(MYDEBUG, "xBall = "+ xBall);

        //Log.i(MYDEBUG, "velox = " + veloX + " veloy =" + veloY);
        float dx = (float)Math.cos(flingAngle) * (flingVelocity / FACTOR);
        float dy = (float)Math.sin(flingAngle) * (flingVelocity / FACTOR);


        // adjust as per each device's pixel density
        dx *= pixelDensity;
        dy *= pixelDensity;
       // dx *= 2;
        //dy *= 2;

        xBall += dx;
        yBall += dy;
       /* if(xBall <= 0.00 || xBall + ballDiameter >= xCenter * 2)
        {
            //if(0)
            //Log.i(MYDEBUG, "IT'S OFF THE SIDE");
            veloX *= -1;
            flingVelocity = (float)Math.sqrt(veloX * veloX + veloY * veloY);
            flingAngle = (float)Math.atan2(veloY, veloX);
            //Log.i(MYDEBUG, "velox = " + veloX + " veloy =" + veloY);
            dx = (float)Math.cos(flingAngle) * (flingVelocity / FACTOR);
            dy = (float)Math.sin(flingAngle) * (flingVelocity / FACTOR);
            dx *= pixelDensity;
            dy *= pixelDensity;
            // dx *= 2;
            //dy *= 2;

            xBall += dx;
            yBall += dy;

        }
        if (yBall >= yCenter * 2 || yBall - ballDiameter <= 0.00)
        {
            flingAngle = 180 - flingAngle;
        }*/
        flingVelocity /= DIVISOR;
        final float dz = (float)Math.sqrt(dx * dx + dy * dy);
        //Log.i(MYDEBUG, "dx equals" + dx + " dy equals" + dy);
        if (dz < THRESHOLD)
        {
            //Log.i(MYDEBUG, "Fling ending");

            flingTimer.cancel();
            ballFlings = false;
            moving = false;

            ////Log.i(MYDEBUG, "target.left = " + target.left + " target.right = " + target.right + " xBall = " + xBall + " yBall = " + yBall + " ballDiameter = " + ballDiameter + " target.top = " + target.top + " target.bottom = " + target.bottom);
            if(target.left < xBall && target.right > xBall + ballDiameter && target.top < yBall - ballDiameter && target.bottom > yBall)
            {
                //Log.i(MYDEBUG, "fling success!");
                flingSucs[flingTrialsDone] = 1;
                flingSuccesses += 1;
                //Log.i(MYDEBUG, "flingSuccesses = " + flingSuccesses);
            }
            flingTrialsDone += 1;
            if(flingTrialsDone == trials)
            {
                StringBuilder sd2Data = new StringBuilder(100);
                sd2Data.append("Fling trial results:  \n");
                for(int o = 0; o < trials; o++)
                {
                    sd2Data.append(String.format(Locale.CANADA, "%.2f,", flingtimes[o]));
                    sd2Data.append(String.format(Locale.CANADA, "%.2f,", flingSucs[o]));

                }
                sd2Data.append("\n");
                // sd1.write(sd1Stuff.toString(), 0, sd1Stuff.length());
                //sd1.flush();
                if(f2 == null)
                {
                    //Log.i(MYDEBUG, "Jesus fucking Christ");
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    if (Environment.isExternalStorageManager()) {
                        File dataDirectory = new File(Environment.getExternalStorageDirectory() +
                                DATA_DIRECTORY);
                        //Log.i(MYDEBUG, "HERE IS THE DATA DIRECTORY FLING: "+ dataDirectory);
                        if (!dataDirectory.exists() && !dataDirectory.mkdirs())
                        {
                            //Log.i(MYDEBUG, "does it exist? " + dataDirectory.exists() + "  can it be made? " + dataDirectory.mkdirs());
                            Log.e(MYDEBUG, "ERROR --> FAILED TO CREATE DIRECTORY: " + DATA_DIRECTORY);
                        }
                        if(f2 == null)
                        {
                            f2 = new File(dataDirectory, "BallSortStuff" + ".sd2");
                        }


                            // also make a comma-delimited leader that will begin each data line written to the sd2 file


                        try
                        {

                            sd4 = new BufferedWriter(new FileWriter(f2, true));
                            sd4.write(sd2Data.toString(), 0, sd2Data.length());
                            //Log.i(MYDEBUG, "It's been written");
                            sd4.flush();
                            sd4.close();

                        } catch (IOException e)
                        {
                            Log.e(MYDEBUG, "ERROR OPENING DATA FILES! e=" + e.toString());

                        }
                    } else {
                        //request for the permission
                        Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                        Uri uri = Uri.fromParts("package","com.example.ballsort", null);
                        intent.setData(uri);
                        this.getContext().startActivity(intent);
                    }
                }

                Intent i  = new Intent(this.getContext(), BallSortSetup.class);
                this.getContext().startActivity(i);
                android.os.Process.killProcess(android.os.Process.myPid());
            }
            onWindowFocusChanged(true);
            ////Log.i(MYDEBUG, "But is it getting here first?");
        }
        else
        {
            ////Log.i(MYDEBUG, "Fling beginning");
            flingTimer.start();
        }

        invalidate(); // apply the changes to xPosition and yPosition

    }

    private class MyGestureListener extends GestureDetector.SimpleOnGestureListener
    {
        @Override
        public boolean onSingleTapUp(MotionEvent me)
        {
            // no implementation (handle in onTouchEvent)
            return false;
        }

        /*
         * onFling - This method is executed when a fling or flick gesture is detected (that began
         * on the image). The goal is to determine the fling velocity and the fling direction
         * (angle). Then, the timer is started. The real work is done in doFling which is called
         * each time the CountDownTimer times out.
         */
        @Override
        public boolean onFling(MotionEvent me1, MotionEvent me2, float velocityX, float velocityY)
        {
            // ignore this fling gesture if the initial ACTION_DOWN was outside the image
            if (!ballSelected)
                return true;
            endTime = System.currentTimeMillis();
            float totalTime = endTime - startTime;
            flingtimes[flingTrialsDone] = totalTime/1000;
            //Log.i(MYDEBUG, "This much time: " + flingtimes[flingTrialsDone] + " this is how many trials: " + flingTrialsDone + " This is how big " + flingtimes.length);
            if(flinging)
            {
                ////Log.i(MYDEBUG, "Flinging");
                flingVelocity = (float)Math.sqrt(velocityX * velocityX + velocityY * velocityY);
                flingAngle = (float)Math.atan2(velocityY, velocityX);
                veloX = velocityX;
                veloY = velocityY;
                flingTimer.start();
                ballFlings = true;
                if(velocityX == 0 && velocityY == 0)
                {
                    ballFlings = false;
                }
            }

            return true;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    public void onWindowFocusChanged(boolean hasFocus)
    {
        ////Log.i(MYDEBUG, "NGL we're changing window focus");
        if (!hasFocus)
            return;
        xCenter = this.getWidth() / 2f;
        yCenter = this.getHeight() / 2f;



        xBall = xCenter;
        yBall = yCenter;
        xBallCenter = xBall + ballDiameter / 2f;
        yBallCenter = yBall + ballDiameter / 2f;
        // check if view is ready for drawing

        // draw the paths
        // draw fills
        upLeft.left = (float) (0.00);
        upLeft.right = (float) (this.getWidth()/3.00);
        upLeft.bottom = (float)(this.getHeight());
        upLeft.top = (float) (this.getHeight() - (this.getHeight()/3.00));


        upMid.left = (float)(this.getWidth()/3.00);
        upMid.bottom = (float)(this.getHeight());
        upMid.top = (float)(this.getHeight() - (this.getHeight()/3.00));
        upMid.right = (float) (this.getWidth() * 2.00/3.00);


        upRight.left = (float)(this.getWidth() * 2.00/3.00);
        upRight.bottom = (float)(this.getHeight());
        upRight.top = (float)(this.getHeight() - (this.getHeight()/3.00));
        upRight.right = (float) (this.getWidth());


        midLeft.left = (float) (0.00);
        midLeft.right = (float) (this.getWidth()/3.00);
        midLeft.bottom = (float)(this.getHeight() - (this.getHeight()/3.00));
        midLeft.top = (float) (this.getHeight()/3.00);


        midRight.left = (float)(this.getWidth() * 2.00/3.00);
        midRight.right = (float) (this.getWidth());
        midRight.bottom = (float)(this.getHeight() - (this.getHeight()/3.00));
        midRight.top = (float) (this.getHeight()/3.00);


        bLeft.left = (float) (0.00);
        bLeft.right = (float) (this.getWidth()/3.00);
        bLeft.bottom = (float)(this.getHeight()/3.00);
        bLeft.top = (float) (0.00);


        bMid.left = (float)(this.getWidth()/3.00);
        bMid.right = (float) (this.getWidth() * 2.00/3.00);
        bMid.bottom = (float)(this.getHeight()/3.00);
        bMid.top = (float) (0.00);


        bRight.left = (float)(this.getWidth() * 2.00/3.00);
        bRight.right = (float) (this.getWidth());
        bRight.bottom = (float)(this.getHeight()/3.00);
        bRight.top = (float) (0.00);


        whiteRect.left = (float)(this.getWidth()/3.00);
        whiteRect.right = (float) (this.getWidth() * 2.00/3.00);
        whiteRect.top = (float)(this.getHeight()/3.00);
        whiteRect.bottom = (float)(this.getHeight() - (this.getHeight()/3.00));



        whiteLeft = (float)(this.getWidth()/3.00);
        whiteRight = (float) (this.getWidth() * 2.00/3.00);
        whiteTop = (float)(this.getHeight()/3.00);
        whiteBottom = (float)(this.getHeight() - (this.getHeight()/3.00));
        if(oWFC == true)
        {
            ballDiameter = (int) (xCenter/ 15);
            //Log.i(MYDEBUG, String.valueOf(xCenter));
            List<Bitmap> ballList = new ArrayList<>();
            ballList.add(blueBallBitmap);
            ballList.add(greenBallBitmap);
            ballList.add(yellowBallBitmap);
            ballList.add(orangeBallBitmap);
            ballList.add(redBallBitmap);
            ballList.add(magentaBallBitmap);
            ballList.add(violetBallBitmap);
            ballList.add(turquoiseBallBitmap);
            int choice = (int) Math.floor(Math.random() * 8);
            if(ballList.get(choice) == blueBallBitmap)
            {
                ballString = "b";
            }
            if(ballList.get(choice) == greenBallBitmap)
            {
                ballString = "g";
            }
            if(ballList.get(choice) == yellowBallBitmap)
            {
                ballString = "y";
            }
            if(ballList.get(choice) == orangeBallBitmap)
            {
                ballString = "o";
            }
            if(ballList.get(choice) == redBallBitmap)
            {
                ballString = "r";
            }
            if(ballList.get(choice) == magentaBallBitmap)
            {
                ballString = "m";
            }
            if(ballList.get(choice) == violetBallBitmap)
            {
                ballString = "v";
            }
            if(ballList.get(choice) == turquoiseBallBitmap)
            {
                ballString = "t";
            }
            ball = Bitmap.createScaledBitmap(ballList.get(choice), ballDiameter, ballDiameter, true);
            canDrawBall = true;
        }

        oWFC = true;

        invalidate();
    }

    protected void onDraw(Canvas canvas)
    {
        if(oWFC == false)
            return;

        if(ballTapped == true)
        {
            ////Log.i(MYDEBUG, "Hey we're getting to this place");
            for (int j = 0; j < 8; j++)
            {
                canvas.drawRect(squares[j], masterPaints[j]);
            }
            canvas.drawRoundRect(new RectF(whiteLeft, whiteTop, whiteRight, whiteBottom), 10, 10, whitePaint);
            if(canDrawBall == true)
            {
                canvas.drawBitmap(ball, xBall, yBall, null);

            }
            return;
        }

        if(moving == true || ballFlings == true)
        {
            ////Log.i(MYDEBUG, "It's actually getting here");
            for (int j = 0; j < 8; j++)
            {
                canvas.drawRect(squares[j], masterPaints[j]);
            }
            canvas.drawRoundRect(new RectF(whiteLeft, whiteTop, whiteRight, whiteBottom), 10, 10, whitePaint);
            canvas.drawRect(target, targetPaint);
            if(canDrawBall == true)
            {
                canvas.drawBitmap(ball, xBall, yBall, null);
            }
            return;
        }

        //canvas.drawText("Random text", 100, -29, greenPaint);
        String cheight = String.valueOf(canvas.getHeight());
        String cwidth = String.valueOf(canvas.getWidth());
        //Log.i(MYDEBUG, cheight);
        //Log.i(MYDEBUG, cwidth);
        List<Paint> paintList = new ArrayList<>();
        paintList.add(bluePaint);
        paintList.add(greenPaint);
        paintList.add(orangePaint);
        paintList.add(magentaPaint);
        paintList.add(violetPaint);
        paintList.add(yellowPaint);
        paintList.add(turquoisePaint);
        paintList.add(redPaint);

        int choice = (int) Math.floor(Math.random() * 8);
        checkPaint(ballString, paintList.get(choice), upLeft, canvas);
        canvas.drawRect(upLeft, paintList.get(choice));
        for(int i = 0; i < 8; i++)
        {
            if(paintList.get(choice) == masterPaints[i])
            {
                squares[i] = upLeft;
            }
        }
        paintList.remove(paintList.get(choice));

        choice = (int) Math.floor(Math.random() * 7);
        checkPaint(ballString, paintList.get(choice), midLeft, canvas);
        canvas.drawRect(midLeft, paintList.get(choice));
        for(int i = 0; i < 8; i++)
        {
            if(paintList.get(choice) == masterPaints[i])
            {
                squares[i] = midLeft;
            }
        }
        paintList.remove(paintList.get(choice));


        choice = (int) Math.floor(Math.random() * 6);
        checkPaint(ballString, paintList.get(choice), bLeft, canvas);
        canvas.drawRect(bLeft, paintList.get(choice));
        for(int i = 0; i < 8; i++)
        {
            if(paintList.get(choice) == masterPaints[i])
            {
                squares[i] = bLeft;
            }
        }
        paintList.remove(paintList.get(choice));

        choice = (int) Math.floor(Math.random() * 5);
        checkPaint(ballString, paintList.get(choice), bMid, canvas);
        canvas.drawRect(bMid, paintList.get(choice));
        for(int i = 0; i < 8; i++)
        {
            if(paintList.get(choice) == masterPaints[i])
            {
                squares[i] = bMid;
            }
        }
        paintList.remove(paintList.get(choice));

        choice = (int) Math.floor(Math.random() * 4);
        checkPaint(ballString, paintList.get(choice), bRight, canvas);
        canvas.drawRect(bRight, paintList.get(choice));
        for(int i = 0; i < 8; i++)
        {
            if(paintList.get(choice) == masterPaints[i])
            {
                squares[i] = bRight;
            }
        }
        paintList.remove(paintList.get(choice));

        choice = (int) Math.floor(Math.random() * 3);
        checkPaint(ballString, paintList.get(choice), midRight, canvas);
        canvas.drawRect(midRight, paintList.get(choice));
        for(int i = 0; i < 8; i++)
        {
            if(paintList.get(choice) == masterPaints[i])
            {
                squares[i] = midRight;
            }
        }
        paintList.remove(paintList.get(choice));

        choice = (int) Math.floor(Math.random() * 2);
        checkPaint(ballString, paintList.get(choice), upRight, canvas);
        canvas.drawRect(upRight, paintList.get(choice));
        for(int i = 0; i < 8; i++)
        {
            if(paintList.get(choice) == masterPaints[i])
            {
                squares[i] = upRight;
            }
        }
        paintList.remove(paintList.get(choice));

        choice = (int) Math.floor(Math.random() * 1);
        checkPaint(ballString, paintList.get(choice), upMid, canvas);
        canvas.drawRect(upMid, paintList.get(choice));
        for(int i = 0; i < 8; i++)
        {
            if(paintList.get(choice) == masterPaints[i])
            {
                squares[i] = upMid;
            }
        }
        paintList.remove(paintList.get(choice));



        canvas.drawRoundRect(new RectF(whiteLeft, whiteTop, whiteRight, whiteBottom), 10, 10, whitePaint);
        if(canDrawBall == true)
        {
            canvas.drawBitmap(ball, xBall, yBall, null);

        }

            // draw lines



        // draw the ball in its new location
        //canvas.drawBitmap(ball, xBall, yBall, null);

    }
    public boolean checkPaint(String ballString, Paint paint, RectF r, Canvas canvas)
    {
        if(ballString == "b" && paint == bluePaint || ballString == "g" && paint == greenPaint || ballString == "r" && paint == redPaint || ballString == "o" & paint == orangePaint || ballString == "m" & paint == magentaPaint || ballString == "v" & paint == violetPaint || ballString == "t" & paint == turquoisePaint || ballString == "y" & paint == yellowPaint)
        {

            target.left = r.left;
            target.right = r.right;
            target.top = r.top;
            target.bottom = r.bottom;
            targetPaint = paint;
            canvas.drawRect(target, paint);
            ////Log.i(MYDEBUG, "Target. left = " + target.left + " target.right = " + target.right);
            return true;
        }
        return false;
    }

    public void configure(int numTs, String geeType)
    {
        noTs = numTs;

        trials = noTs;
        taptimes = new float[trials];
        flingtimes = new float[trials];
        tapSucs = new float[trials];
        flingSucs = new float[trials];

        gType = geeType;
        ////Log.i(MYDEBUG, "Here it is bruh" +  gType);
        if(gType.equals("Fling"))
        {
            flinging = true;
        }
        ////Log.i(MYDEBUG, "What is flinging? " + flinging);

    }
}
