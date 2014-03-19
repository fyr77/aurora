package ru.game.aurora.application;

import org.newdawn.slick.Music;
import org.newdawn.slick.MusicListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: User
 * Date: 26.01.14
 * Time: 17:35
 */
public class Playlist implements MusicListener {
    private static final Logger logger = LoggerFactory.getLogger(Playlist.class);

    private List<DeferredLoadingMusic> music = new ArrayList<>();

    private int currentMusicIdx = 0;

    private String id;

    public Playlist(String id, File musicDir) {

        this.id = id;
        logger.info("Loading music from {} for playlist {}", musicDir.getAbsolutePath(), id);
        for (File f : musicDir.listFiles()) {
            if (!f.getName().endsWith(".ogg")) {
                continue;
            }

            music.add(new DeferredLoadingMusic(f, this));
        }

        if (music.isEmpty()) {
            logger.warn("No background music loaded");
        } else {
            music.get(0).requestLoad();
        }
    }

    public String getId() {
        return id;
    }

    public void play() {
        int idxToPlay = currentMusicIdx;
        int idxToLoad = currentMusicIdx + 1;
        if (idxToLoad == music.size()) {
            idxToLoad = 0;
        }

        music.get(idxToPlay).getMusic().play();
        music.get(idxToLoad).requestLoad();
    }


    @Override
    public void musicEnded(Music music) {
        this.music.get(currentMusicIdx).release();
        if (++currentMusicIdx == this.music.size()) {
            currentMusicIdx = 0;
        }
        play();
    }

    @Override
    public void musicSwapped(Music music, Music music2) {

    }
}
