package com.example.mplayer.realplayer;

import android.app.ListActivity;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class Main extends ListActivity implements MediaPlayer.OnPreparedListener {

    MediaPlayer myPlayer;
    ImageButton play_pause;
    ImageButton next;
    boolean isStarted = false;
    List<String> songs = new ArrayList<String>();

    @Override
    public void onPrepared(MediaPlayer mp) {
        myPlayer.start();
    }

    private enum BPM {
        L1(80), L2(100), L3(120), L4(130), L5(140), L6(150), L7(160), L8(170), L9(180), L10(190);

        private Integer bpmRate;

        private BPM(Integer bpmRate) {
            this.bpmRate = bpmRate;
        }
    }

    ;

    private Map<BPM, List<File>> songsByBPM = new HashMap<BPM, List<File>>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        try {
            updatePlaylist();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        play_pause = (ImageButton) findViewById(R.id.btnPlayPause);
        next = (ImageButton) findViewById(R.id.btnNextSong);


        play_pause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });


    }

    @Override
    protected void onListItemClick(ListView list, View view, int position, long id) {
        if(isStarted){
            myPlayer.pause();
        }
        try {
            isStarted = true;
            Field[] fields = R.raw.class.getFields();
            myPlayer = new MediaPlayer();
            myPlayer.reset();
            /*String a1 = fields[0].getName();
            String a2 = fields[0].toString();*/
            Uri uri = Uri.parse("android.resource://com.example.mplayer.realplayer/" + fields[position].getInt(Integer.class));
            myPlayer.setDataSource(this, uri);
            myPlayer.prepare();
            myPlayer.start();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        play_pause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(myPlayer.isPlaying())
                {
                    myPlayer.pause();
                }
                else myPlayer.start();
//                if(myPlayer.isPlaying()){
//                    myPlayer.pause();
//                } else {
//                    try {
//                        Load(music.get(new Random().nextInt(music.size())));
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//                }
            }
        });
    }


    private void updatePlaylist() throws IllegalAccessException {

        Field[] fields = R.raw.class.getFields();

        for (Field field : fields) {
            songs.add(field.getName());
        }

        ArrayAdapter<String> songList = new ArrayAdapter<String>(this, R.layout.song_item, songs);
        setListAdapter(songList);
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
}
