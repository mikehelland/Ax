package com.monadpad.ax;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;

/**
 * User: m
 * Date: 2/20/13
 * Time: 9:23 PM
 */
public class FretsView extends View {


    int frets;
    int strings;
    int base;
    String scale;
    Paint paint;

    ArrayList<Touch> touches = new ArrayList<Touch>();

//    AudioDevice audioDevice = null;

    private Channel channel;

    boolean isSetup = false;

    float fretWidth;
    float stringHeight;


    FretsActivity fretsActivity;

    private boolean showWaiting = false;
    private Paint waitingPaint;
    private long startScroll = 0;
    private float waitingLength;
    private String waiting = "Waiting for connection by BITAR PICK";
    private RefreshThread refreshThread;

    private String instrument;


    private boolean isTouching = false;
    private RecordingArmedThread recordingArmedThread;

    private boolean onRed = false;

    private Paint recordingPaint;

    int[][] fretMap;


    public FretsView(Context context, AttributeSet attrs) {
        super(context, attrs);

        fretsActivity = (FretsActivity)context;

        paint = new Paint();
        paint.setARGB(255, 255, 255, 255);
        paint.setStyle(Paint.Style.FILL_AND_STROKE);
        paint.setStrokeWidth(4);
        paint.setShadowLayer(6, 0, 0, 0xFFFFFFFF);

        waitingPaint = new Paint();
        waitingPaint.setARGB(255, 0, 0, 0);
        waitingPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        waitingPaint.setStrokeWidth(4);
//        waitingPaint.setShadowLayer(6, 0, 0, 0x808080);
        waitingPaint.setTextSize(30.0f);
        waitingLength = waitingPaint.measureText(waiting);

        recordingPaint = new Paint();
        recordingPaint.setARGB(255, 255, 0, 0);
        recordingPaint.setStyle(Paint.Style.FILL);

        setBackgroundColor(0xFF000000);

    }

    public void setChannel(Channel channel) {

        this.channel = channel;

        instrument = channel.instrument;

        makeFretMap();

        frets = fretMap[0].length;
        strings = fretMap.length;
        isSetup = false;

        postInvalidate();
    }



    @Override
    public boolean onTouchEvent(MotionEvent event) {
        super.onTouchEvent(event);

        int action = event.getAction();

        boolean invalid = false;

        switch (action & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN : {

                isTouching = true;
                channel.cancelPlayback();

                Touch touch = makeTouch(event, -1);
                touch.onFret = (int)(touch.x / fretWidth);
                touch.onString = (int)(touch.y / stringHeight);
                touches.add(touch);

                touch.channelId = channel.startChannel(base + touch.fretMapping(fretMap));

                invalid = true;

                if (recordingArmedThread != null) {
                    recordingArmedThread.stop = true;
                    recordingArmedThread = null;
                    if (channel.getStatus() == Channel.STATUS_RECORDING) {
                        onRed = true;
                    }
                }
                break;
            }

            case MotionEvent.ACTION_UP : {

                if (isTouching) {
                    channel.stopChannel(touches.get(0).channelId);
                    touches.clear();
                    isTouching = false;
                }

                invalid = true;
                break;
            }

            case MotionEvent.ACTION_POINTER_DOWN : {
                final int index = (action & MotionEvent.ACTION_POINTER_INDEX_MASK)
                        >> MotionEvent.ACTION_POINTER_INDEX_SHIFT;

                Touch touch = makeTouch(event, index);
                touch.onFret = (int)(touch.x / fretWidth);
                touch.onString = (int)(touch.y / stringHeight);

                touches.add(touch);

                touch.channelId = channel.startChannel(base + touch.fretMapping(fretMap));

                invalid = true;
                break;
            }

            case MotionEvent.ACTION_POINTER_UP : {

                final int index = (action & MotionEvent.ACTION_POINTER_INDEX_MASK)
                        >> MotionEvent.ACTION_POINTER_INDEX_SHIFT;
                final int id = event.getPointerId(index);
                for (Touch touch : touches){
                    if (id == touch.id){
                        touches.remove(touch);

                        channel.stopChannel(touch.channelId);

                        break;
                    }
                }

                invalid = true;
                break;
            }

            case MotionEvent.ACTION_MOVE : {

                if (!isTouching)
                    break;

                int id;
                int lastFret;
                int lastString;
                for (int ip = 0; ip  < event.getPointerCount(); ip++) {
                    id = event.getPointerId(ip);
                    for (Touch touch : touches){
                        if (id == touch.id){

                            lastFret = touch.onFret;
                            lastString = touch.onString;

                            touch.x = event.getX(ip);
                            touch.y = event.getY(ip);
                            touch.onFret = (int)(touch.x / fretWidth);
                            touch.onString = (int)(touch.y / stringHeight);

                            if (lastFret != touch.onFret || lastString != touch.onString) {
                                channel.setChannel(touch.channelId,
                                        base + touch.fretMapping(fretMap));
                                invalid = true;

                            }

                            break;
                        }
                    }
                }
                break;
            }
        }
        if (invalid)
            invalidate();

        return true;
    }



    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (channel == null)
            return;

        if (!isSetup) {
            fretWidth = getWidth() / frets;
            stringHeight = getHeight() / strings;

            isSetup = true;
        }

        int status = channel.getStatus();

        if (status == Channel.STATUS_RECORDING) {
            recordingPaint.setAlpha(200);
            canvas.drawRect(0, 0, getWidth(), getHeight(), recordingPaint);
        }
        if (status == Channel.STATUS_RECORDING_ARMED) {

            int millis = (int)(System.currentTimeMillis() % 2000);
            if (millis > 1000) millis = 1000 - millis;
            float percent = (float)millis / 1000.0f;

            recordingPaint.setAlpha((int)(255.0f * percent));
            canvas.drawRect(0, 0, getWidth(), getHeight(), recordingPaint);
        }


        float fstrings = (float)strings;
        float ffrets = (float)frets;
        for (int i = 1; i < strings; i++){
            canvas.drawLine(0, i / fstrings * getHeight(),
                    getWidth(), i / fstrings * getHeight(), paint);
        }

        for (int i = 1; i < frets; i++){
            canvas.drawLine(i / ffrets * getWidth(), 0,
                    i / ffrets * getWidth(), getHeight(), paint);
        }

        for (Touch touch : touches){
            canvas.drawRect(fretWidth * touch.onFret, stringHeight * touch.onString,
                    fretWidth * (touch.onFret + 1), stringHeight * (touch.onString + 1), paint);
        }

//        canvas.drawCircle(getWidth() * 0.3f, getHeight() * 0.25f, 20, paint);
//        canvas.drawCircle(getWidth() * 0.7f, getHeight() * 0.75f, 20, paint);
        canvas.drawCircle(getWidth() * 0.1f, getHeight() * 0.75f, 20, paint);
        canvas.drawCircle(getWidth() * 0.9f, getHeight() * 0.25f, 20, paint);

        if (showWaiting)
            drawWaiting(canvas);

    }


    void drawWaiting(Canvas canvas) {
        if (startScroll == 0) {
            startScroll = System.currentTimeMillis();
        }

        float y = getHeight() / 2.0f - waitingPaint.getTextSize() / 2.0f;
        float x = getWidth() - ((System.currentTimeMillis() - startScroll) / 10)%(getWidth()+waitingLength);
        //float x = getWidth() / 2-.0f - waitingPaint.measureText(waiting) / 2;
        canvas.drawText(waiting, x, y, waitingPaint);
    }


    public void startRecording() {
        channel.startRecording();
        onRed = true;
        postInvalidate();
    }

    public void stopRecording() {

        channel.stopRecording();

        onRed = false;
        postInvalidate();


    }


    Touch makeTouch(MotionEvent event, int index) {
        Touch t;
        if (index == -1 ) {
            t = new Touch(event.getX(), event.getY(), event.getPointerId(0));
        }
        else {
            t = new Touch(event.getX(index), event.getY(index), event.getPointerId(index));
        }
        return t;
    }

    void makeFretMap() {

        if (instrument.equals("HHDRUMS")) {
            fretMap =   new int[][] {
                    new int[] {0, 1, 2, 3}
            };
        }
        else if (instrument.equals("AGUITAR CHORDS")) {
            fretMap =   new int[][] {
                    new int[] {4, 0, 5},
                    new int[] {1, 2, 3}
            };
        }
        else if (instrument.equals("EBASS") ||
                instrument.equals("EGUITAR CHORDS") ) {
            fretMap =   new int[][] {
                    new int[] {8, 9, 10, 11, 12},
                    new int[] {0, 3, 5, 6, 7}
            };

        }
        else if (instrument.equals("SYNTH") || instrument.equals("EGUITAR")) {

            scale = PreferenceManager.getDefaultSharedPreferences(getContext())
                    .getString("quantizer", "0,2,4,5,7,9,11");

            if (scale.equals("0,2,4,5,7,9,11")) {
                fretMap =   new int[][] {
                        new int[] {-1, 0, 5, 7, 9},
                        new int[] {4, 9, 11, 12, 14}
                };
            }
            else if (scale.equals("0,3,5,6,7,10")) {
                fretMap = new int[][] {
                        new int[] {-2, 0, 3, 5, 6},
                        new int[] {5, 7, 10, 12, 15}
                };
            }
            else {
                fretMap =   new int[][] {
                        new int[] {-1, 0, 5, 7, 9},
                        new int[] {4, 9, 11, 12, 14}
                };
            }
        }

        if (fretMap == null) {
            fretMap =   new int[][] {
                    new int[] {0}
            };
        }

    }

    public void onPause() {
        channel.cancelPlayback();
        if (refreshThread != null && refreshThread.isAlive()) {
            refreshThread.stop = true;
        }
    }



    void waitingMode() {
        refreshThread = new RefreshThread();
        refreshThread.start();
        showWaiting = true;
//        currentBackgroundColor = Color.YELLOW;

    }

    void connectingMode() {
        setBackgroundColor(Color.YELLOW);
        postInvalidate();

    }

    void standaloneMode() {
        setBackgroundColor(Color.BLACK);

        notWaitingMode();
        postInvalidate();
    }

    void connectedMode() {
        setBackgroundColor(Color.BLUE);

        notWaitingMode();
        postInvalidate();
    }

    private void notWaitingMode() {

        showWaiting = false;

        if (refreshThread != null && refreshThread.isAlive()) {
            refreshThread.stop = true;
        }
        refreshThread = null;


    }


    class RefreshThread extends Thread {
        boolean stop = false;

        @Override
        public void run() {
            while (!stop) {
                postInvalidate();
                try {
                    Thread.sleep(1000 / 60);
                } catch (InterruptedException e) {
                    break;
                }
            }
        }
    }

    class RecordingArmedThread extends Thread {
        boolean stop = false;

        @Override
        public void run() {
            long startedAt = System.currentTimeMillis();
            onRed = false;
            while (!stop) {
                startedAt = startedAt + 250;
                onRed = !onRed;
                postInvalidate();
                try {
                    Thread.sleep(1000 / 60);
                } catch (InterruptedException e) {
                    break;
                }
            }
//            onRed = false;
        }
    }

    public void updateRecordingStatus() {

        int status = channel.getStatus();

        if (status == Channel.STATUS_RECORDING_ARMED) {
            recordingArmedThread = new RecordingArmedThread();
            recordingArmedThread.start();
            return;
        }

        if (recordingArmedThread != null) {
            recordingArmedThread.stop = true;
            recordingArmedThread = null;
            onRed = false;
        }

        if (status == Channel.STATUS_LIVE) {
            onRed = false;
        }

        postInvalidate();
    }


    void cancelPlayback() {
        channel.cancelPlayback();
    }
}
