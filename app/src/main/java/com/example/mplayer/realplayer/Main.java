package com.example.mplayer.realplayer;

import android.app.Activity;
import android.app.ListActivity;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;


public class Main extends Activity implements MediaPlayer.OnPreparedListener {

    MediaPlayer myPlayer;
    ImageButton play_pause;
    ImageButton next;
    boolean isPlayerStarted = false;
    boolean started = false;
    int current_song = 0;
    private Map<BPM, List<File>> songsByBPM = new HashMap<BPM, List<File>>();

    @Override
    public void onPrepared(MediaPlayer mp) {
        myPlayer.start();
    }

    ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        myPlayer = new MediaPlayer();

        play_pause = (ImageButton) findViewById(R.id.btnPlayPause);
        next = (ImageButton) findViewById(R.id.btnNextSong);

        final Main that = this;
        myPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            public void onCompletion(MediaPlayer player) {

                Random random_position = new Random();
                Field[] fields = R.raw.class.getFields();
                Integer rand = random_position.nextInt(fields.length);
                while(current_song == rand){
                    rand = random_position.nextInt(fields.length);
                }
                current_song = rand;
                player.stop();
                player.reset();
                try {
                    player.setDataSource(that,
                            Uri.parse("android.resource://com.example.mplayer.realplayer/" + fields[rand].getInt(Integer.class)));
                } catch (Exception e) {

                }
                try {
                    player.prepare();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                player.start();
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
                    changeSong(position);
                    started = true;
                } else {
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
                if(!started){
                    return;
                }
                Random random_position = new Random();
                Field[] fields = R.raw.class.getFields();
                int position = random_position.nextInt(fields.length);
                while (current_song == position) {
                    position = random_position.nextInt(fields.length);
                }
                current_song = position;
                changeSong(position);
            }
        });
    }

    private void changeSong(int position) {
        if (isPlayerStarted) {
            myPlayer.pause();
        }
        try {
            isPlayerStarted = true;
            Field[] fields = R.raw.class.getFields();
            //myPlayer = new MediaPlayer();
            myPlayer.reset();

            Uri uri = Uri.parse("android.resource://com.example.mplayer.realplayer/" + fields[position].getInt(Integer.class));
            myPlayer.setDataSource(this, uri);
            myPlayer.prepare();
            myPlayer.start();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
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

    public void Load(File song) throws IOException {

        FileInputStream fis = new FileInputStream(song);
        myPlayer.setDataSource(fis.getFD());
        myPlayer.setOnPreparedListener(this);
        myPlayer.prepareAsync();

    }

    @Override
    public void onDestroy() {
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
    }
}
