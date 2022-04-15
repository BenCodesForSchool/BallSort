package com.example.ballsort;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Vibrator;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import androidx.constraintlayout.solver.widgets.Rectangle;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class BallPanel extends View {

    final static float DEGREES_TO_RADIANS = 0.0174532925f;

    // the ball diameter will be min(width, height) / this_value
    final static float BALL_DIAMETER_ADJUST_FACTOR = 30;
    final static String MYDEBUG = "MYDEBUG"; // for Log.i messages
    final static int DEFAULT_LABEL_TEXT_SIZE = 20; // tweak as necessary
    final static int DEFAULT_STATS_TEXT_SIZE = 10;
    final static int DEFAULT_GAP = 7; // between lines of text
    final static int DEFAULT_OFFSET = 10; // from bottom of display

    final static int MODE_NONE = 0;
    final static int PATH_TYPE_SQUARE = 1;
    final static int PATH_TYPE_CIRCLE = 2;

    final static float PATH_WIDTH_NARROW = 2f; // ... x ball diameter
    final static float PATH_WIDTH_MEDIUM = 4f; // ... x ball diameter
    final static float PATH_WIDTH_WIDE = 8f; // ... x ball diameter

    float radiusOuter, radiusInner;

    Bitmap ball, blueBallBitmap, redBallBitmap, greenBallBitmap, magentaBallBitmap, orangeBallBitmap, turquoiseBallBitmap, violetBallBitmap, yellowBallBitmap;
    int ballDiameter;

    float dT; // time since last sensor event (seconds)

    float width, height, pixelDensity;
    int labelTextSize, statsTextSize, gap, offset;

    RectF innerRectangle, outerRectangle, innerShadowRectangle, outerShadowRectangle, ballNow, upLeft, upMid, upRight, midLeft, midRight, bLeft, bMid, bRight, whiteRect;
    boolean touchFlag, oWFC, canDrawBall;
    Vibrator vib;
    int wallHits;

    float xBall, yBall, whiteLeft, whiteRight, whiteTop, whiteBottom; // top-left of the ball (for painting)
    float xBallCenter, yBallCenter; // center of the ball
    final static int MIDDLE_SQUARE_LENGTH = 0;
    final static int SQUARE_LENGTH = 0;




    // parameters from Setup dialog
    String gType;
    int noTs;

    float dBall; // the amount to move the ball (in pixels): dBall = dT * velocity
    float xCenter, yCenter; // the center of the screen
    long now, lastT;
    Paint bluePaint, greenPaint, orangePaint, magentaPaint, yellowPaint, violetPaint, turquoisePaint,  redPaint;
    float[] updateY;

    public BallPanel(Context contextArg)
    {
        super(contextArg);
        initialize(contextArg);
    }

    public BallPanel(Context contextArg, AttributeSet attrs)
    {
        super(contextArg, attrs);
        initialize(contextArg);
    }

    public BallPanel(Context contextArg, AttributeSet attrs, int defStyle)
    {
        super(contextArg, attrs, defStyle);
        initialize(contextArg);
    }

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
        Log.i(MYDEBUG, String.valueOf(this.getBackground()));
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
        wallHits = 0;

        vib = (Vibrator)c.getSystemService(Context.VIBRATOR_SERVICE);

        xCenter = this.getWidth() / 2f;
        yCenter = this.getHeight() / 2f;

        int xcoord = ((int) xCenter);
        int ycoord = ((int) yCenter);




    }

    public void onWindowFocusChanged(boolean hasFocus)
    {
        Log.i(MYDEBUG, "NGL we're changing window focus");
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
        upLeft.bottom = (float) (this.getHeight() - (this.getHeight()/3.00));
        upLeft.top = (float)(this.getHeight());

        upMid.left = (float)(this.getWidth()/3.00);
        upMid.bottom = (float)(this.getHeight() - (this.getHeight()/3.00));
        upMid.top = (float)(this.getHeight());
        upMid.right = (float) (this.getWidth() * 2.00/3.00);

        upRight.left = (float)(this.getWidth() * 2.00/3.00);
        upRight.bottom = (float)(this.getHeight() - (this.getHeight()/3.00));
        upRight.top = (float)(this.getHeight());
        upRight.right = (float) (this.getWidth());

        midLeft.left = (float) (0.00);
        midLeft.right = (float) (this.getWidth()/3.00);
        midLeft.bottom = (float) (this.getHeight()/3.00);
        midLeft.top = (float)(this.getHeight() - (this.getHeight()/3.00));

        midRight.left = (float)(this.getWidth() * 2.00/3.00);
        midRight.right = (float) (this.getWidth());
        midRight.bottom = (float) (this.getHeight()/3.00);
        midRight.top = (float)(this.getHeight() - (this.getHeight()/3.00));

        bLeft.left = (float) (0.00);
        bLeft.right = (float) (this.getWidth()/3.00);
        bLeft.bottom = (float) (0.00);
        bLeft.top = (float)(this.getHeight()/3.00);

        bMid.left = (float)(this.getWidth()/3.00);
        bMid.right = (float) (this.getWidth() * 2.00/3.00);
        bMid.bottom = (float) (0.00);
        bMid.top = (float)(this.getHeight()/3.00);

        bRight.left = (float)(this.getWidth() * 2.00/3.00);
        bRight.right = (float) (this.getWidth());
        bRight.bottom = (float) (0.00);
        bRight.top = (float)(this.getHeight()/3.00);

        whiteRect.left = (float)(this.getWidth()/3.00);
        whiteRect.right = (float) (this.getWidth() * 2.00/3.00);
        whiteRect.top = (float)(this.getHeight() - (this.getHeight()/3.00));
        whiteRect.bottom = (float)(this.getHeight()/3.00);


        whiteLeft = (float)(this.getWidth()/3.00);
        whiteRight = (float) (this.getWidth() * 2.00/3.00);
        whiteTop = (float)(this.getHeight() - (this.getHeight()/3.00));
        whiteBottom = (float)(this.getHeight()/3.00);
        if(oWFC == true)
        {
            ballDiameter = (int) (xCenter/ 15);
            Log.i(MYDEBUG, String.valueOf(xCenter));
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


        canvas.drawText("Random text", 100, -29, greenPaint);
        String cheight = String.valueOf(canvas.getHeight());
        String cwidth = String.valueOf(canvas.getWidth());
        Log.i(MYDEBUG, cheight);
        Log.i(MYDEBUG, cwidth);
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
        canvas.drawRect(upLeft, paintList.get(choice));
        paintList.remove(paintList.get(choice));

        choice = (int) Math.floor(Math.random() * 7);
        canvas.drawRect(midLeft, paintList.get(choice));
        paintList.remove(paintList.get(choice));
        Log.i(MYDEBUG, "It's drawing, apparently");

        choice = (int) Math.floor(Math.random() * 6);
        canvas.drawRect(bLeft, paintList.get(choice));
        paintList.remove(paintList.get(choice));

        choice = (int) Math.floor(Math.random() * 5);
        canvas.drawRect(bMid, paintList.get(choice));
        paintList.remove(paintList.get(choice));

        choice = (int) Math.floor(Math.random() * 4);
        canvas.drawRect(bRight, paintList.get(choice));
        paintList.remove(paintList.get(choice));

        choice = (int) Math.floor(Math.random() * 3);
        canvas.drawRect(midRight, paintList.get(choice));
        paintList.remove(paintList.get(choice));

        choice = (int) Math.floor(Math.random() * 2);
        canvas.drawRect(upRight, paintList.get(choice));
        paintList.remove(paintList.get(choice));

        choice = (int) Math.floor(Math.random() * 1);
        canvas.drawRect(upMid, paintList.get(choice));
        paintList.remove(paintList.get(choice));



        Paint white = new Paint();
        white.setColor(Color.WHITE);
        canvas.drawRoundRect(new RectF(whiteLeft, whiteTop, whiteRight, whiteBottom), 10, 10, white);
        if(canDrawBall == true)
        {
            canvas.drawBitmap(ball, xBall, yBall, null);

        }

            // draw lines



        // draw the ball in its new location
        //canvas.drawBitmap(ball, xBall, yBall, null);

    }

    public void configure(int numTs, String geeType)
    {
        noTs = numTs;

        gType = geeType;

    }
}
