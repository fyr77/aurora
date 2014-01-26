package ru.game.aurora.application;

import org.newdawn.slick.Music;
import org.newdawn.slick.MusicListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: User
 * Date: 26.01.14
 * Time: 17:35
 */
public class Playlist implements MusicListener {
    private static final Logger logger = LoggerFactory.getLogger(Playlist.class);

    private List<Music> music = new LinkedList<>();

    private Iterator<Music> currentMusicIter;

    private String id;

    public Playlist(String id, File musicDir) {
        this.id = id;
        logger.info("Loading music from {} for playlist {}", musicDir.getAbsolutePath(), id);
        for (File f : musicDir.listFiles()) {
            if (!f.getName().endsWith(".ogg")) {
                continue;
            }

            try {
                Music m = new Music(new FileInputStream(f), f.getName());
                m.addListener(this);
                music.add(m);
            } catch (Exception ex) {
                logger.error("Failed to load music from " + f.getAbsolutePath(), ex);
            }
        }

        if (music.isEmpty()) {
            logger.warn("No background music loaded");
        } else {
            currentMusicIter = music.iterator();
        }
    }

    public String getId() {
        return id;
    }

    public void play() {
        playNext();
    }

    private void playNext() {
        if (currentMusicIter.hasNext()) {
            Music m = currentMusicIter.next();
            m.play();
        } else {
            currentMusicIter = this.music.iterator();
            currentMusicIter.next().play();
        }
    }

    @Override
    public void musicEnded(Music music) {
        playNext();
    }

    @Override
    public void musicSwapped(Music music, Music music2) {

    }
}
