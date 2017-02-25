package com.monadpad.ax;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.*;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

public class FretsActivity extends Activity {

    private AudioDevice audioDevice;

    FretsView frets;

    final static int DIALOG_ASK_INSTRUMENT = 0;
    final static int DIALOG_SETUP_BLUETOOTH = 1;

    SharedPreferences prefs;

    private BluetoothFactory btf;

    private long connectedAt = 0;

    private boolean loadedOnce = false;

    private Uri background;
    private Bitmap bitmap;

    private boolean bluetooth = false;

    private final static int REQUEST_BACKGROUND = 123;

    private Device localDevice;

    SamplerPool pool;

    private Device deviceForInstrumentDialog;

    private BitarLooper looper;

    private List<PlaybackLoop> loops = new ArrayList<PlaybackLoop>();

    private List<Device> devices = new ArrayList<Device>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN |
                    WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_FULLSCREEN |
                        WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setVolumeControlStream(AudioManager.STREAM_MUSIC);

        setContentView(R.layout.frets);

        frets = (FretsView)findViewById(R.id.frets);


        prefs = PreferenceManager.
                getDefaultSharedPreferences(getApplicationContext());

        pool = new SamplerPool(this, 8, AudioManager.STREAM_MUSIC, 0);

        View button;
        button = findViewById(R.id.bt_button);
        button.setSoundEffectsEnabled(false);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                bluetoothButton();

            }
        });

        button = findViewById(R.id.record_button);
        button.setSoundEffectsEnabled(false);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                recordButton(localDevice);
                for (Device d : devices) {
                    recordButton(d);
                }

                frets.updateRecordingStatus();
            }
        });

        button = findViewById(R.id.instrument_button);
        button.setSoundEffectsEnabled(false);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deviceForInstrumentDialog = localDevice;
                showDialog(FretsActivity.DIALOG_ASK_INSTRUMENT);
            }
        });


        Intent intent = getIntent();
        if (intent.hasExtra("duration") && intent.hasExtra("started")) {
            looper = new BitarLooper(intent.getLongExtra("duration", 4000),
                    intent.getLongExtra("started", System.currentTimeMillis()));
        }
        else {
            looper = new BitarLooper();
        }


        localDevice = new Device(null);
        setLocalChannel("EGUITAR CHORDS");

        String background = prefs.getString("background", "");
        if (background.length() > 0) {
            setBackgroundUri(Uri.parse(background));
        }

        findViewById(R.id.omgdrums_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent drumsIntent = getPackageManager().
                        getLaunchIntentForPackage("com.monadpad.omgdrums");
                if (drumsIntent != null) {
                    //sketchatuneIntent.putExtra("duration", mJam.getDuration());
                    //sketchatuneIntent.putExtra("started", mJam.getStarted());
                    drumsIntent.putExtra("caller", "com.monadpad.ax");

                    startActivity(drumsIntent);
                }
                else {
                    //startActivity(new Intent(Main.this, GetDrawMusicActivity.class));
                }

            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();

        if (!loadedOnce) {
        }

        loadedOnce = true;
    }



    @Override
    public void onPause() {
        super.onPause();

        frets.onPause();
    }

    protected Dialog onCreateDialog(int dialog){

        switch (dialog){
            case DIALOG_ASK_INSTRUMENT:

                return askForInstrument();

            case DIALOG_SETUP_BLUETOOTH:

                return setupBluetoothDialog();

        }
        return null;
    }

    private Dialog askForInstrument () {

        final Dialog dl = new Dialog(this);
        dl.setTitle(getString(R.string.choose_an_instrument));
        dl.setContentView(R.layout.ask_for_instrument);


        dl.findViewById(R.id.cancelButton).setOnClickListener( new View.OnClickListener() {
            public void onClick(View v) {
                removeDialog(DIALOG_ASK_INSTRUMENT);
            }
        });

        dl.findViewById(R.id.aguitar_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                chooseInstrumentFromDialog("AGUITAR CHORDS");
            }
        });
        dl.findViewById(R.id.eguitar_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                chooseInstrumentFromDialog("EGUITAR");
            }
        });
        dl.findViewById(R.id.eguitarpower_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                chooseInstrumentFromDialog("EGUITAR CHORDS");
            }
        });
        dl.findViewById(R.id.bass_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                chooseInstrumentFromDialog("EBASS");
            }
        });
        dl.findViewById(R.id.drums_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                chooseInstrumentFromDialog("HHDRUMS");
            }
        });
        dl.findViewById(R.id.synth_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                chooseInstrumentFromDialog("SYNTH");
            }
        });



        return dl;
    }

    void chooseInstrumentFromDialog(String instrument) {
        removeDialog(DIALOG_ASK_INSTRUMENT);

        Device d = deviceForInstrumentDialog;
        d.getChannel().instrument = instrument;
        d.getChannel().setAudio(makeAudioDevice(instrument));

        if (d == localDevice) {
            frets.setChannel(d.getChannel());
            ((ImageButton)findViewById(R.id.instrument_button)).
                    setImageResource(getInstrumentImage(instrument));
        }
        else {
            d.getButton().setImageResource(getInstrumentImage(instrument));
            d.sendChangeInstrumentMessage(instrument);
        }

        deviceForInstrumentDialog = null;
    }

    private void setupBluetoothMode() {
        if (!BluetoothFactory.initialize(this))
            return;

        btf = new BluetoothFactory(getApplicationContext(), new BluetoothStatusCallback() {

            @Override
            public void onConnected(BluetoothFactory.ConnectedThread connection) {
                if (audioDevice != null)
                    audioDevice.finish();

                Channel channel = localDevice.getChannel();
                audioDevice = new BluetoothAudioDevice(FretsActivity.this,
                        channel.instrument, btf);

                channel.setAudio(audioDevice);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        frets.connectedMode();
                    }
                });

                connectedAt = System.currentTimeMillis();

            }

            @Override
            public void newStatus(final String status, final int deviceI) {

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (BluetoothFactory.STATUS_CONNECTED.equals(status)) {

                        }
                        else if (BluetoothFactory.STATUS_ACCEPTING_CONNECTIONS.equals(status)) {
                            frets.waitingMode();
                        }
                        else if (status.startsWith(BluetoothFactory.STATUS_CONNECTING_TO)) {
                            frets.connectingMode();
                        }
                        else if (BluetoothFactory.STATUS_IO_CONNECTED_THREAD.equals(status)) {
                            frets.cancelPlayback();
                            frets.standaloneMode();
                            if (connectedAt > 0 && System.currentTimeMillis() - connectedAt > 1000) {
                                //todo reset audiodevice? retry connection?
                            }
                        }
                        else {
                            Toast.makeText(FretsActivity.this,
                                    "Bluetooth Status: \n\n" + status, Toast.LENGTH_LONG).show();
                        }
                    }
                });

            }

            @Override
            public  void newData(final String data, int deviceI) {

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        String[] commandArray = data.split(":");

                        for (String command : commandArray) {
                            processData(command);
                        }

                    }
                });

            }

            void processData(String data) {
                if ("com.androidinstrument.STARTRECORDING".equals(data)) {
                    frets.startRecording();
                }

                if ("com.androidinstrument.STOPRECORDING".equals(data)) {
                    frets.stopRecording();
                }

                if ("com.androidinstrument.STOPPLAYBACK".equals(data)) {
                    frets.cancelPlayback();
                }

                if (data.startsWith(Device.ACTION_CHANGE_INSTRUMENT)) {
                    final String instrument = data.split(";")[1];

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            chooseInstrumentFromBluetooth(instrument);
                            frets.cancelPlayback();
                        }
                    });
                }

                if (data.equals(Device.ACTION_ARM_RECORDING)) {

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            localDevice.getChannel().armRecording();
                            frets.updateRecordingStatus();
                        }
                    });
                }
                if (data.equals(Device.ACTION_DISARM_RECORDING)) {

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            localDevice.getChannel().disarmRecording();
                            frets.updateRecordingStatus();
                        }
                    });
                }
                if (data.equals(Device.ACTION_DONE_RECORDING)) {

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            localDevice.getChannel().doneRecording();
                            frets.updateRecordingStatus();
                        }
                    });
                }


            }
        });


        btf.connect();

    }

    private void setupDeviceMixerMode() {
        if (BluetoothFactory.initialize(this)) {
            btf = new BluetoothFactory(this, btMixerCallback);
            btf.startAccepting();

            findViewById(R.id.devices_caption).setVisibility(View.VISIBLE);

        }

    }



    @Override
    public void onDestroy() {
        super.onDestroy();
        if (btf != null)
            btf.cleanUp();

        if (bitmap != null)
            bitmap.recycle();

        PlaybackLoop loop;
        while (loops.size() > 0) {
            loop = loops.get(0);
            loop.finish();
            loops.remove(loop);

        }

        if (audioDevice != null)
            audioDevice.finish();

    }


    public void onActivityResult(int request, int result, Intent data) {
        if (request == BluetoothFactory.REQUEST_ENABLE_BT) {
            if (result == RESULT_OK) {
                setupBluetoothMode();
            }
            else {
//                setupStandaloneMode();
            }
        }

        if (request == REQUEST_BACKGROUND) {
            if (result== RESULT_OK) {
                Uri targetUri = data.getData();
                setBackgroundUri(targetUri);
            }
            else {
                prefs.edit().putString("background", "").commit();
                frets.setBackground(null);
            }
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem){


        switch(menuItem.getItemId()) {
            case R.id.settings_menu:
                startActivity(new Intent(this, SynthPreferences.class));
                return true;
            case R.id.background_menu:
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), REQUEST_BACKGROUND);
                return true;

        }
        return true;
    }

    void setLocalChannel(String instrument) {

        audioDevice = makeAudioDevice(instrument);

        final Channel channel = new Channel(looper, instrument, audioDevice);

        frets.setChannel(channel);

        localDevice.setChannel(channel);

    }


    private void setBackgroundUri(Uri uri){
        background = uri;

        try {

            bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(uri));
            frets.setBackgroundDrawable(new BitmapDrawable(bitmap));
            prefs.edit().putString("background", uri.toString()).commit();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

    }

    void chooseInstrumentFromBluetooth(String instrument) {


        Log.d("MGH instrument from bluetooth", instrument);

        localDevice.getChannel().instrument = instrument;
        frets.setChannel(localDevice.getChannel());


    }

    int getInstrumentImage(String instrument) {
        if (instrument.equals("AGUITAR CHORDS"))
            return R.drawable.aguitar;
        if (instrument.equals("EGUITAR CHORDS"))
            return R.drawable.eguitar;
        if (instrument.equals("EGUITAR"))
            return R.drawable.eguitar;
        if (instrument.equals("EBASS"))
            return R.drawable.bass;
        if (instrument.equals("HHDRUMS"))
            return R.drawable.drum;
        if (instrument.equals("SYNTH"))
            return R.drawable.dialpad;


        return -1;
    }



    AudioDevice makeAudioDevice(String instrument) {
        AudioDevice audioDevice;
        if (instrument.equals("EGUITAR CHORDS")) {
            audioDevice = new Sampler(instrument,  pool).fillPoolWithPowerChords();
        }
        else if (instrument.equals("EGUITAR")) {
            audioDevice = new Sampler(instrument,  pool).fillPoolWithElectric();
        }
        else if (instrument.equals("EBASS")) {
            audioDevice = new Sampler(instrument,  pool).fillPoolWithBass();
        }
        else if (instrument.equals("AGUITAR CHORDS")) {
            audioDevice = new Sampler(instrument,  pool).fillPoolWithAcoustic();
        }
        else if (instrument.equals("HHDRUMS")) {
            audioDevice = new Sampler(instrument,  pool).fillPoolWithHipHopDrums();
        }
        else  {
            audioDevice = new DialpadAudioDevice(this, instrument);
        }
        return audioDevice;

    }

    private void newLoop(final PlaybackLoop loop) {

        loops.add(loop);

        Device device = loop.getDevice();
        final Channel channel = device.getChannel();
        final ImageButton newButton = new ImageButton(this);
        newButton.setImageResource(getInstrumentImage(channel.instrument));
        newButton.setSoundEffectsEnabled(false);
        newButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                channel.toggleMute();
            }
        });
        newButton.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {

                channel.finish();
                loops.remove(loop);
                ((LinearLayout)findViewById(R.id.loop_list)).removeView(newButton);

                if (loops.size() == 0) {
                    findViewById(R.id.loops_caption).setVisibility(View.GONE);
                    looper.clear();
                }

                return true;
            }
        });

        ((LinearLayout)findViewById(R.id.loop_list)).addView(newButton);

        if (loops.size() == 1) {
            findViewById(R.id.mixer_mode_button).setVisibility(View.VISIBLE);
            findViewById(R.id.loops_caption).setVisibility(View.VISIBLE);
        }

        if (channel == localDevice.getChannel()) {
            setLocalChannel(channel.instrument);
        }
        else {
            device.reloadChannel();
        }


    }

    Dialog setupBluetoothDialog() {
        final Dialog dl = new Dialog(this);
        //dl.setTitle(getString(R.string.setup_bt_text1));
        dl.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dl.setContentView(R.layout.setup_bluetooth);

        dl.findViewById(R.id.become_a_mixer_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                setupDeviceMixerMode();
                removeDialog(DIALOG_SETUP_BLUETOOTH);

            }
        });
        dl.findViewById(R.id.connect_to_mixer_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                setupBluetoothMode();
                removeDialog(DIALOG_SETUP_BLUETOOTH);

            }
        });


        return dl;
    }


    void bluetoothButton() {

        showDialog(DIALOG_SETUP_BLUETOOTH);

    }


    BluetoothStatusCallback btMixerCallback = new BluetoothStatusCallback() {

        @Override
        public void onConnected(final BluetoothFactory.ConnectedThread connection) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    newDevice(connection);
                }
            });

        }

        @Override
        public void newStatus(final String status, final int deviceI) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (BluetoothFactory.STATUS_ACCEPTING_CONNECTIONS.equals(status)) {
                        //findViewById(R.id.channelthing).setBackgroundColor(0xFFFFFF00);

                        Log.d("MGH bt connected", Integer.toString(deviceI));
                        return;
                    }
                    if (BluetoothFactory.STATUS_CONNECTED.equals(status)) {
                        //findViewById(R.id.channelthing).setBackgroundColor(0xFF0000FF);

                        Log.d("MGH bt connected", Integer.toString(deviceI));
                        return;
                    }
                    if (BluetoothFactory.STATUS_IO_CONNECTED_THREAD.equals(status)) {

                        return;
                    }
                    if (BluetoothFactory.STATUS_IO_CONNECT_THREAD.equals(status)) {

                        return;
                    }
                    if (status.startsWith(BluetoothFactory.STATUS_CONNECTING_TO)) {

                        return;
                    }

                    Toast.makeText(FretsActivity.this,
                            "Bluetooth Status: \n\n" + status, Toast.LENGTH_LONG).show();
                }
            });
        }

        @Override
        public void newData(final String data, final int deviceI) {

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    String[] commandArray = data.split(":");

                    for (String command : commandArray) {
                        processData(command, deviceI);
                    }

                }
            });

        }
        private void processData(String data, int deviceI) {
            String[] dataArray = data.split(";");
            Device device = devices.get(deviceI);

            onDataFromDevice(device);

            if (AudioDevice.ACTION_STARTCHANNEL.equals(dataArray[0])) {
                int id = Integer.parseInt(dataArray[1]);
                int x = Integer.parseInt(dataArray[2]);

                device.getChannel().startChannel(id, x);
            }
            if (AudioDevice.ACTION_SETCHANNEL.equals(dataArray[0])) {
                int id = Integer.parseInt(dataArray[1]);
                int x = Integer.parseInt(dataArray[2]);
                device.getChannel().setChannel(id, x);
            }
            if (AudioDevice.ACTION_STOPCHANNEL.equals(dataArray[0])) {
                int id = Integer.parseInt(dataArray[1]);
                device.getChannel().stopChannel(id);

            }

            if (AudioDevice.ACTION_CREATECHANNEL.equals(dataArray[0])) {
                //mixer.createChannel(deviceI, dataArray);
                device.setupChannel(looper, pool, dataArray);
                device.getButton().setImageResource(getInstrumentImage(device.getChannel().instrument));
            }

        }

    };


    void newDevice(BluetoothFactory.ConnectedThread connection) {
        final Device device = new Device(connection);
        devices.add(device);
        final ImageButton newButton = new ImageButton(this);
        newButton.setSoundEffectsEnabled(false);
        newButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                deviceForInstrumentDialog = device;
                showDialog(DIALOG_ASK_INSTRUMENT);

            }
        });
        ((LinearLayout)findViewById(R.id.devices_list)).addView(newButton);
        device.setButton(newButton);
    }

    void setupDeviceChannel(Device device, String[] dataArray) {

        String instrument = dataArray[1];
        AudioDevice ad = makeAudioDevice(instrument);
        final Channel channel = new Channel(looper, instrument, ad);
        device.getButton().setImageResource(getInstrumentImage(channel.instrument));

    }

    void recordButton(Device d) {
        Channel channel = d.getChannel();
        int status = channel.getStatus();
        if (status == Channel.STATUS_LIVE) {
            d.armRecording();
            //channel.armRecording();

        }
        else if (status == Channel.STATUS_RECORDING_ARMED) {
            d.disarmRecording();

        }
        else if (status == Channel.STATUS_RECORDING) {
            PlaybackLoop loop = d.doneRecording();
            newLoop(loop);
        }

    }

    @Override
    public void onNewIntent(Intent intent) {

        if (intent.hasExtra("duration") && intent.hasExtra("started")) {
            looper.start(intent.getLongExtra("duration", 4000),
                    intent.getLongExtra("started", System.currentTimeMillis()));
        }

    }


    public void onDataFromDevice(Device device) {

        // mute any loops from this device
        PlaybackLoop loop;
        for (int i = loops.size() - 1; i >= 0; i--) {
            loop = loops.get(i);

            if (loop.getDevice() == device) {

                Log.d("MGH", "muting channel");
                loop.getChannel().setMute(true);

//                loop.finish();
//                loops.remove(loop);
            }
        }


    }

}


/*
    public void setAddChannelAlpha(float alpha){

        findViewById(R.id.add_channel_button).setAlpha(alpha);

    }

    public void setInstrumentLeft(int paramInt){

        LinearLayout channel = (LinearLayout)channels.get(currentChannel).getView();
        ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams)channel.getLayoutParams();
        lp.leftMargin = paramInt;
        channel.setLayoutParams(lp);
    }

    public void setChannelAlpha(float alpha)  {
        View v;
        for (int i = 0; i < channels.size(); i++) {
            if (i != currentChannel) {
                //v = channels.get(i).getView();
                v.setAlpha(alpha);
                if (!v.isShown() && alpha > 0.0f) {
                    v.setVisibility(View.VISIBLE);
                }
                else if (alpha == 0.0f && v.isShown()) {
                    v.setVisibility(View.GONE);
                }
                Log.d("MGH shown:", v.isShown() ? "is shown": "is not shown");
            }
        }
    }

void addChannelButton() {

    addChannelButton.setVisibility(View.GONE);

    setLocalChannel("HHDRUMS");

    showAddButton();

}


void showAddButton() {
    addChannelButton.setAlpha(0.0f);
    addChannelButton.setVisibility(View.VISIBLE);
//                addChannelButton.setLeft(channelThingWidth * 2);
    ViewGroup.MarginLayoutParams layout = (ViewGroup.MarginLayoutParams)addChannelButton.getLayoutParams();
//        layout.leftMargin = channelThingWidth * (channels.size() + 1);
    addChannelButton.setLayoutParams(layout);

    ObjectAnimator anim = ObjectAnimator.ofFloat(FretsActivity.this,
            "addChannelAlpha", 0.0f, 1.0f);
    anim.setDuration(300);
    anim.start();

}

ObjectAnimator hideChannels() {

    ObjectAnimator anim = ObjectAnimator.ofFloat(FretsActivity.this,
            "channelAlpha", 1.0f, 0.0f);
    anim.setDuration(300);
    return anim;


}

ObjectAnimator showChannels() {

    ObjectAnimator anim = ObjectAnimator.ofFloat(FretsActivity.this,
            "channelAlpha", 0.0f, 1.0f);
    anim.setDuration(300);

    return anim;


}
*/
