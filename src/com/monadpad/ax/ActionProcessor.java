package com.monadpad.ax;

import android.content.Context;
import android.content.Intent;

/**
 * User: m
 * Date: 7/1/13
 * Time: 1:46 PM
 */
public class ActionProcessor {

    public static String process(final Context ctx, final String action) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (action.length() > 0){

                    if (action.startsWith("com.androidinstrument")) {
                        Intent intent = new Intent();

                        // parameters for BPM
                        if (action.startsWith("com.androidinstrument.drum.SETBPM")) {
                            String[] params = action.split(";");
                            intent.putExtra("bpmval", Float.parseFloat(params[1]));
                            intent.setAction(params[0]);
                        }
                        else {

                            //send it raw
                            intent.setAction(action);
                        }
                        ctx.sendBroadcast(intent);
                    }
                    else if (action.startsWith("com.monadpad")) {
                        ctx.startActivity(new Intent(action).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                    }
                }
            }
        }).start();

        return action;

    }
}
