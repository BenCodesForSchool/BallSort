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
import android.view.View;

import androidx.constraintlayout.solver.widgets.Rectangle;

import java.util.Locale;

public class BallPanel extends View {

    final static float DEGREES_TO_RADIANS = 0.0174532925f;

    // the ball diameter will be min(width, height) / this_value
    final static float BALL_DIAMETER_ADJUST_FACTOR = 30;

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

    Bitmap ball, decodedBallBitmap;
    int ballDiameter;

    float dT; // time since last sensor event (seconds)

    float width, height, pixelDensity;
    int labelTextSize, statsTextSize, gap, offset;

    RectF innerRectangle, outerRectangle, innerShadowRectangle, outerShadowRectangle, ballNow, upLeft, upMid, upRight, midLeft, midRight, bLeft, bMid, bRight;
    boolean touchFlag;
    Vibrator vib;
    int wallHits;

    float xBall, yBall; // top-left of the ball (for painting)
    float xBallCenter, yBallCenter; // center of the ball
    final static int MIDDLE_SQUARE_LENGTH = 0;
    final static int SQUARE_LENGTH = 0;




    // parameters from Setup dialog
    String gType;
    int noTs;

    float dBall; // the amount to move the ball (in pixels): dBall = dT * velocity
    float xCenter, yCenter; // the center of the screen
    long now, lastT;
    Paint bluePaint, greenPaint, orangePaint, purplePaint, yellowPaint, pinkPaint, cyanPaint,  redPaint;
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
        greenPaint.setColor(Color.GREEN);
        greenPaint.setStyle(Paint.Style.FILL);

        pinkPaint = new Paint();
        pinkPaint.setColor(0xffc0cb);
        pinkPaint.setStyle(Paint.Style.FILL);

        orangePaint = new Paint();
        orangePaint.setColor(0xff6600);
        orangePaint.setStyle(Paint.Style.FILL);

        purplePaint = new Paint();
        purplePaint.setColor(0x800080);
        purplePaint.setStyle(Paint.Style.FILL);

        yellowPaint = new Paint();
        yellowPaint.setColor(Color.YELLOW);
        yellowPaint.setStyle(Paint.Style.FILL);

        cyanPaint = new Paint();
        cyanPaint.setColor(Color.CYAN);
        cyanPaint.setStyle(Paint.Style.FILL);




        // NOTE: we'll create the actual bitmap in onWindowFocusChanged
        //decodedBallBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ball);

        lastT = System.nanoTime();
        this.setBackgroundColor(Color.LTGRAY);
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
        wallHits = 0;

        vib = (Vibrator)c.getSystemService(Context.VIBRATOR_SERVICE);

        xCenter = this.getWidth() / 2f;
        yCenter = this.getHeight() / 2f;

        int xcoord = ((int) xCenter);
        int ycoord = ((int) yCenter);


        Rectangle white = new Rectangle();
        white.setBounds(xcoord/2, ycoord/2, xcoord * 3/2, ycoord * 3/2);

    }

    protected void onDraw(Canvas canvas)
    {
        xCenter = this.getWidth() / 2f;
        yCenter = this.getHeight() / 2f;

        int xcoord = ((int) xCenter);
        int ycoord = ((int) yCenter);
        // check if view is ready for drawing
        if (updateY == null)
            return;

        // draw the paths
            // draw fills
        upLeft.left = (float) (0.00);
        upLeft.right = (float) (this.getWidth()/3.00);
        upLeft.bottom = (float) (this.getHeight() - (this.getHeight()/3.00));
        upLeft.top = (float)(this.getHeight());
        canvas.drawRect(upLeft, bluePaint);

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




            Paint white = new Paint();
            white.setColor(Color.WHITE);
            canvas.drawRoundRect(new RectF(xcoord/2, ycoord/2, xcoord * 3/2, ycoord * 3/2), 10, 10, white);

            // draw lines



        // draw the ball in its new location
        canvas.drawBitmap(ball, xBall, yBall, null);

    }
}
