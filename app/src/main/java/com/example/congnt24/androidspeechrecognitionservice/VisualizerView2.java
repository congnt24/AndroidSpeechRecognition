package com.example.congnt24.androidspeechrecognitionservice;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

public class VisualizerView2 extends View {
    private Rect mRect = new Rect();
    private Paint mForePaint = new Paint();
    private float rmsdB;
    private float maxRmsdB = 10;
    private float startX, startY, stopX, stopY;

    public VisualizerView2(Context context) {
        super(context);
        init();
    }

    public VisualizerView2(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public VisualizerView2(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mForePaint.setStrokeWidth(2f);
        mForePaint.setAntiAlias(true);
        mForePaint.setColor(Color.rgb(0, 128, 255));
    }

    public void updateVisualizer(float rmsdB) {
        this.rmsdB = rmsdB;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (rmsdB < 0) {
            return;
        }

        mRect.set(0, 0, getWidth(), getHeight());

        float centerx = getWidth() / 2;
        float ratio = rmsdB / maxRmsdB;
        float unit = centerx / maxRmsdB;
        startY = getHeight() / 2;
        stopY = startY;
        startX = centerx - unit * rmsdB;
        stopX = centerx + unit * rmsdB;

        canvas.drawLine(startX, startY, stopX, stopY, mForePaint);
    }

}