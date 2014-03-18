/**
 * Created with IntelliJ IDEA.
 * User: User
 * Date: 18.03.14
 * Time: 21:30
 */

package ru.game.aurora.application;

import org.newdawn.slick.Music;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.lang.ref.SoftReference;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

/**
 * Music takes a lot of space.
 * Load it only when needed.
 * When not plying - reference it via Soft Reference, so that it can be unloaded
 */
public class DeferredLoadingMusic {

    private Music strongRef = null;

    private SoftReference<Music> softReference = new SoftReference<Music>(null);

    private final File myFile;

    private Future<Music> loadingFuture = null;

    private final Playlist myPlaylist;

    private final static Logger logger = LoggerFactory.getLogger(DeferredLoadingMusic.class);

    public DeferredLoadingMusic(File file, Playlist myPlaylist) {
        myFile = file;
        this.myPlaylist = myPlaylist;
    }

    public Music getMusic() {
        strongRef = softReference.get();

        if (strongRef != null) {
            return strongRef;
        }

        try {
            strongRef = loadingFuture.get();
            softReference = new SoftReference<>(strongRef);
            loadingFuture = null;
        } catch (Exception e) {
            logger.error("Failed to load music from file " + myFile, e);
            return null;
        }
        return strongRef;
    }

    public void requestLoad() {
        strongRef = softReference.get();
        if (strongRef != null) {
            return;
        }

        loadingFuture = GlobalThreadPool.getExecutor().submit(new Callable<Music>() {
            @Override
            public Music call() throws Exception {
                try (FileInputStream fis = new FileInputStream(myFile)) {
                    Music rz = new Music(fis, myFile.getName());
                    rz.addListener(myPlaylist);
                    return rz;
                } catch (Exception e) {
                    logger.error("Failed to load music from file " + myFile, e);
                }
                return null;
            }
        });
    }

    public void release() {
        strongRef = null;
        loadingFuture = null;
    }

}
