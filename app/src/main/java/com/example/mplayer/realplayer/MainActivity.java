package com.example.mplayer.realplayer;

import android.app.Activity;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class MainActivity extends Activity implements MediaPlayer.OnPreparedListener {

    MediaPlayer myPlayer;
    ImageButton play_pause;
    ImageButton next;
    List<File> music = new ArrayList<File>();
    String songFileNames = "C:\\Users\\VasilVasiles\\AndroidStudioProjects\\REalPlayer\\app\\src\\main\\res\\raw";

    @Override
    public void onPrepared(MediaPlayer mp) {
        myPlayer.start();
    }

    private enum BPM {
        L1 (80), L2 (100), L3 (120), L4 (130), L5 (140), L6 (150), L7 (160), L8 (170), L9 (180), L10 (190);

        private Integer bpmRate;

        private BPM(Integer bpmRate) {
            this.bpmRate = bpmRate;
        }
    };

    private Map<BPM, List<File>> songsByBPM = new HashMap<BPM, List<File>>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        File[] songs =  new File(songFileNames).listFiles();
        for (File song: songs) {
            if (song.isFile()) {
                music.add(song);
            }
        }
        myPlayer = new MediaPlayer();


        play_pause = (ImageButton) findViewById(R.id.btnPlayPause);
        next = (ImageButton) findViewById(R.id.btnNextSong);



        play_pause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(myPlayer.isPlaying()){
                    myPlayer.pause();
                } else {
                    myPlayer.start();
                }
            }
        });

        play_pause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });


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
        myPlayer.reset();
        myPlayer.setDataSource(fis.getFD());
        myPlayer.setOnPreparedListener(this);
        myPlayer.prepareAsync();

    }
}
