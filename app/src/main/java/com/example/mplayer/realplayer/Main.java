package com.example.mplayer.realplayer;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Handler;

public class Main extends Activity implements MediaPlayer.OnPreparedListener, SensorEventListener {

    private static final String SCANNER = "SCANNER: ";

    private class TimerTaskSteps extends TimerTask {

        int ticks = 0;

        @Override
        public void run() {
            stepsIntervals.add(currentStepsCount);

            if (stepsIntervals.size() == 10) {
                stepsPerMinute = (Math.abs(stepsIntervals.peekLast() - stepsIntervals.peek()) * 6);
                stepsIntervals.poll();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        textStepsPerMinute.setText("Steps Per Minute: " + String.valueOf(stepsPerMinute));

                    }
                });
                Log.d("Steps Per Minute: ", String.valueOf(stepsPerMinute));
            }
        }
    }

    MediaPlayer myPlayer;
    ImageButton play_pause;
    ImageButton next;
    Button play;
    boolean isPlayerStarted = false;
    boolean started = false;
    boolean isClicked = false;

    private TextView textView;
    private TextView textStepsPerMinute;
    private TextView textSongName;
    int initialStepsCount;
    boolean isFirstStart;
    int stepsPerMinute;
    int currentStepsCount;
    LinkedList<Integer> stepsIntervals;
    private SensorManager mSensorManager;
    private Sensor mStepCounterSensor;
    private Sensor mStepDetectorSensor;
    private File[] mediaFiles;

    int current_song = 0;
    private Map<Integer, List<String>> songsByBPM = new HashMap<>();
    private List<String> mediaList = new ArrayList<>();


    @Override
    public void onPrepared(MediaPlayer mp) {
        myPlayer.start();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        textView = (TextView) findViewById(R.id.textview);
        textStepsPerMinute = (TextView) findViewById(R.id.textstepsperminute);
        textSongName = (TextView) findViewById(R.id.songName);
        isFirstStart = true;
        initializeSongsMap();
        String externalStoragePath = "/storage/sdcard0/GOTOWI";
        File targetDir = new File(externalStoragePath);
        Log.d(" externalStoragePath ::: ", targetDir.getAbsolutePath());
        mediaFiles = targetDir.listFiles();
        scanFiles(mediaFiles);

        //TODO: remove hardcore
        randomSongBpmRelation();
        // Initialize steps counter and detector sensors
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mStepCounterSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        //mStepDetectorSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);
        initialStepsCount = 0;
        Timer stepsTimer = new Timer();
        TimerTaskSteps task = new TimerTaskSteps();
        stepsTimer.schedule(task, 0, 1000);
        stepsIntervals = new LinkedList<Integer>();

        myPlayer = new MediaPlayer();

        play_pause = (ImageButton) findViewById(R.id.btnPlayPause);
        next = (ImageButton) findViewById(R.id.btnNextSong);

        final Main that = this;
        myPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            public void onCompletion(MediaPlayer player) {

                Random random_position = new Random();
                Integer rand = random_position.nextInt(mediaList.size());
                while (current_song == rand) {
                    rand = random_position.nextInt(mediaList.size());
                }
                current_song = rand;
                player.stop();
                try {
                    prepareNextSong(player);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        play_pause.setOnClickListener(new View.OnClickListener() {
            @Override

            public void onClick(View s) {
                if (!started) {
                    Random random_position = new Random();
                    Field[] fields = R.raw.class.getFields();
                    int position = random_position.nextInt(fields.length);
                    current_song = position;

                    swapButton();

                    changeSong(position);
                    started = true;
                } else {
                    swapButton();

                    if (myPlayer.isPlaying()) {
                        myPlayer.pause();
                    } else {
                        myPlayer.start();
                    }
                }
            }

        });
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!started) {
                    return;
                }
                Random random_position = new Random();
                Field[] fields = R.raw.class.getFields();
                int position = random_position.nextInt(fields.length);
                while (current_song == position) {
                    position = random_position.nextInt(fields.length);
                }
                current_song = position;
                play_pause.setImageResource(R.drawable.pausebutton);
                isClicked = true;
                changeSong(position);
            }
        });
    }

    private void randomSongBpmRelation() {
        int i = 0;
        for (BPM bpm : BPM.values()) {
            songsByBPM.get(bpm.getBpmRate()).add(mediaList.get(i++));
            if (bpm.equals(BPM.L10)) continue;
            songsByBPM.get(bpm.getBpmRate()).add(mediaList.get(i++));
        }
    }


    public void scanFiles(File[] scanFiles) {

        if (scanFiles != null) {
            for (File file : scanFiles) {

//                if (mediaList.size() > 4) {
//                    return;
//                }

                if (file.isDirectory()) {
                    // Log.d(" scaned File ::isDirectory: ",
                    // file.getAbsolutePath());
                    scanFiles(file.listFiles());

                } else {

                    addToMediaList(file);

                }

            }
        } else {

            Log.d(SCANNER,
                    " *************** No file  is available ***************");

        }
    }

    private void addToMediaList(File file) {

        if (file != null) {

            String path = file.getAbsolutePath();

            int index = path.lastIndexOf(".");

            String extn = path.substring(index + 1, path.length());

            if (extn.equalsIgnoreCase("mp4") || extn.equalsIgnoreCase("mp3")) {// ||

                Log.d(" scanned File ::: ", file.getAbsolutePath()
                        + "  file.getPath( )  " + file.getPath());// extn.equalsIgnoreCase("mp3"))
                // {
                Log.d(SCANNER, " ***** above file is added to list ");
                mediaList.add(path);


            }
        }
    }

    private void initializeSongsMap() {
        for (BPM bpm : BPM.values()) {
            songsByBPM.put(bpm.getBpmRate(), new ArrayList<String>());
        }
    }


    @Override
    public void onSensorChanged(SensorEvent event) {
        Sensor sensor = event.sensor;
        float[] values = event.values;
        int value = -1;

        if (values.length > 0) {
            value = (int) values[0];
            currentStepsCount = value;

            if (isFirstStart) {
                isFirstStart = false;
                initialStepsCount = value;
            }
        }

        if (sensor.getType() == Sensor.TYPE_STEP_COUNTER) {
            textView.setText("Step Counter Detected : " + (value - initialStepsCount));
        } else if (sensor.getType() == Sensor.TYPE_STEP_DETECTOR) {
            // For test only. Only allowed value is 1.0 i.e. for step taken
            //textView.setText("Step Detector Detected : " + value);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    protected void onResume() {

        super.onResume();
        mSensorManager.registerListener(this, mStepCounterSensor,
                SensorManager.SENSOR_DELAY_FASTEST);
        mSensorManager.registerListener(this, mStepDetectorSensor,
                SensorManager.SENSOR_DELAY_FASTEST);

    }

    protected void onStop() {
        super.onStop();
        mSensorManager.unregisterListener(this, mStepCounterSensor);
        mSensorManager.unregisterListener(this, mStepDetectorSensor);
    }

    private void swapButton() {
        if (isClicked) {
            play_pause.setImageResource(R.drawable.play_button);
        } else {
            play_pause.setImageResource(R.drawable.pausebutton);
        }
        isClicked = !isClicked;
    }

    private void changeSong(int position) {
        if (isPlayerStarted) {
            myPlayer.pause();
        }
        try {
            isPlayerStarted = true;
            prepareNextSong(myPlayer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void prepareNextSong(MediaPlayer myPlayer) throws IOException {
        myPlayer.reset();
        BPM songBPM = getSongBySpm();
        Random rand = new Random();
        Integer randomSong = rand.nextInt(songsByBPM.get(songBPM.getBpmRate()).size());
        String songURI = songsByBPM.get(songBPM.getBpmRate()).get(randomSong);
        myPlayer.setDataSource(songURI);
        StringBuilder sb = new StringBuilder(songURI);
        int lastIndex = sb.lastIndexOf("/");
        String songName = sb.substring(lastIndex + 1);
        textSongName.setText(songName);
        Log.d("SONG URI: ", songURI + " / " + songBPM.getBpmRate().toString());
        myPlayer.prepare();
        myPlayer.start();
    }

    private BPM getSongBySpm() {
        if (stepsPerMinute < BPM.L1.getBpmRate()) {
            return BPM.L1;
        } else if (stepsPerMinute < BPM.L2.getBpmRate()) {
            return BPM.L2;
        } else if (stepsPerMinute < BPM.L3.getBpmRate()) {
            return BPM.L3;
        } else if (stepsPerMinute < BPM.L4.getBpmRate()) {
            return BPM.L4;
        } else if (stepsPerMinute < BPM.L5.getBpmRate()) {
            return BPM.L5;
        } else if (stepsPerMinute < BPM.L6.getBpmRate()) {
            return BPM.L6;
        } else if (stepsPerMinute < BPM.L7.getBpmRate()) {
            return BPM.L7;
        } else if (stepsPerMinute < BPM.L8.getBpmRate()) {
            return BPM.L8;
        } else if (stepsPerMinute < BPM.L9.getBpmRate()) {
            return BPM.L9;
        } else {
            return BPM.L10;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (myPlayer != null) {
            myPlayer.release();
            myPlayer = null;
        }
    }

    private enum BPM {
        L1(80), L2(100), L3(120), L4(130), L5(140), L6(150), L7(160), L8(170), L9(180), L10(190);

        private Integer bpmRate;

        private BPM(Integer bpmRate) {
            this.bpmRate = bpmRate;
        }

        public Integer getBpmRate() {
            return bpmRate;
        }
    }
}
