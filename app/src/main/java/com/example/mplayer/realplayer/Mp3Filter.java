package com.example.mplayer.realplayer;

import java.io.File;
import java.io.FilenameFilter;

/**
 * Created by Milen on 25.4.2015 г..
 */
public class Mp3Filter implements FilenameFilter{

    @Override
    public boolean accept(File dir, String filename) {
        return (filename.endsWith(".mp3"));
    }
}
