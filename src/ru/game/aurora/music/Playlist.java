package ru.game.aurora.music;

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
public class Playlist implements MusicListener
{
    public static final String DEFAULT_PLAYLIST = "background";

    private static final Logger logger = LoggerFactory.getLogger(Playlist.class);

    private static Playlist currentPlaylist = null;

    private static Playlist nextPlaylist = null;

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
        if (currentPlaylist == this || currentPlaylist == null) {
            int idxToPlay = currentMusicIdx;
            int idxToLoad = currentMusicIdx + 1;
            if (idxToLoad == music.size()) {
                idxToLoad = 0;
            }

            music.get(idxToPlay).getMusic().play();
            music.get(idxToLoad).requestLoad();
            currentPlaylist = this;
        } else {
            currentPlaylist.getCurrentMusic().fade(1000, 0, true);
            nextPlaylist = this;
        }

    }


    @Override
    public void musicEnded(Music music) {
        this.music.get(currentMusicIdx).release();
        if (++currentMusicIdx == this.music.size()) {
            currentMusicIdx = 0;
        }
        if (nextPlaylist != null) {
            currentPlaylist = nextPlaylist;
            nextPlaylist.play();
        } else {
            play();
        }
    }

    @Override
    public void musicSwapped(Music music, Music music2) {

    }

    public boolean isPlaying()
    {
        return currentPlaylist == this;
    }

    public static Playlist getCurrentPlaylist() {
        return currentPlaylist;
    }

    public Music getCurrentMusic()
    {
        if (currentPlaylist != this) {
            return null;
        }

        return music.get(currentMusicIdx).getMusic();
    }
}
