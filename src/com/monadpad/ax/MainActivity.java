package com.monadpad.ax;

import android.app.Activity;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

/**
 * User: m
 * Date: 8/19/13
 * Time: 4:14 AM
 */
public class MainActivity extends Activity {


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN |
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_FULLSCREEN |
                        WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setVolumeControlStream(AudioManager.STREAM_MUSIC);

        setContentView(R.layout.main);

        findViewById(R.id.standaloneButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, FretsActivity.class);
                intent.putExtra("standalone", true);
                startActivity(intent);

            }
        });

        findViewById(R.id.bluetoothButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, SetupBluetoothActivity.class);
                startActivity(intent);

            }
        });

        findViewById(R.id.lessonButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, FretsActivity.class);
                intent.putExtra("lesson", "Smoke On The Water");
                startActivity(intent);
            }
        });

    }


}