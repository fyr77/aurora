/**
 * User: jedi-philosopher
 * Date: 21.01.13
 * Time: 21:53
 */
package ru.game.aurora.application;

import ru.game.aurora.world.World;

import java.io.FileOutputStream;
import java.io.ObjectOutputStream;

public class SaveGameManager {
    private static final String SAVE_NAME = "save.bin";

    public void saveGame(World world) {
        try {
            ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(SAVE_NAME));
            oos.writeObject(world);
            oos.close();
        } catch (Exception ex) {
            System.err.println("Failed to save game");
            ex.printStackTrace();
        }
    }

    public World loadGame() {
        return null;
    }
}
