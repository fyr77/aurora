/**
 * User: jedi-philosopher
 * Date: 21.01.13
 * Time: 21:53
 */
package ru.game.aurora.application;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.game.aurora.world.World;

import java.io.File;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class SaveGameManager {
    private static final Logger logger = LoggerFactory.getLogger(SaveGameManager.class);

    private static final String SAVE_NAME = "save.bin";

    public static void saveGame(World world) {
        try {
            ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(SAVE_NAME));
            oos.writeObject(world);
            oos.close();
        } catch (Exception ex) {
            logger.error("Failed to save game", ex);
        }
    }

    public static World loadGame() {
        File f = new File(SAVE_NAME);
        if (!f.exists()) {
            return null;
        }
        try {
            ObjectInputStream ois = new ObjectInputStream(new java.io.FileInputStream(f));
            World rz = (World) ois.readObject();
            ois.close();

            return rz;
        } catch (Exception e) {
            logger.error("Failed to load game", e);
        }
        return null;
    }

    public static boolean isSaveAvailable() {
        File f = new File(SAVE_NAME);
        return f.exists();
    }
}
