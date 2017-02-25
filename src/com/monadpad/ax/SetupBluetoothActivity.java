package com.monadpad.ax;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.AudioManager;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

/**
 * User: m
 * Date: 8/19/13
 * Time: 4:14 AM
 */
public class SetupBluetoothActivity extends Activity {

    private boolean loadedOnce;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN |
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_FULLSCREEN |
                        WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setVolumeControlStream(AudioManager.STREAM_MUSIC);

        setContentView(R.layout.setup_bluetooth);

/*        findViewById(R.id.start_a_fretboard).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SetupBluetoothActivity.this, FretsActivity.class);
                intent.putExtra("bluetooth", true);
                startActivity(intent);

            }
        });
        findViewById(R.id.start_pick).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SetupBluetoothActivity.this, PickActivity.class);
                startActivity(intent);
            }
        });
 */
    }

    @Override
    public void onResume() {
        super.onResume();

        if (!loadedOnce) {

            loadedOnce = true;

            turnOnBlueTooth();

        }
    }

    void setStatus(boolean status) {
        TextView statusView = (TextView)findViewById(R.id.bt_status);
        TextView detailsView = (TextView)findViewById(R.id.bt_details);

        if (status) {
            statusView.setText(R.string.bluetooth_is_on);
            statusView.setTextColor(Color.GREEN);
            detailsView.setText(R.string.how_it_works);

        }
        else {
            statusView.setText(R.string.bluetooth_is_off);
            statusView.setTextColor(Color.RED);
            detailsView.setText(R.string.turn_on_bluetooth);
        }

    }

    BroadcastReceiver btStateReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null &&
                    BluetoothAdapter.STATE_ON == intent.getIntExtra(BluetoothAdapter.EXTRA_STATE,
                            BluetoothAdapter.ERROR)) {


                setStatus(true);

            }
        }
    };

    void turnOnBlueTooth() {
        if (BluetoothFactory.initialize(this)) {
            setStatus(true);
        }
        else {
            setStatus(false);
        }

    }

}