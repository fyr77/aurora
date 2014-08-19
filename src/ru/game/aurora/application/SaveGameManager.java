/**
 * User: jedi-philosopher
 * Date: 21.01.13
 * Time: 21:53
 */
package ru.game.aurora.application;

import org.newdawn.slick.Image;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.game.aurora.gui.GUI;
import ru.game.aurora.util.EngineUtils;
import ru.game.aurora.world.World;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.Date;

public class SaveGameManager {
    private static final Logger logger = LoggerFactory.getLogger(SaveGameManager.class);

    private static final int SLOTS = 5;

    private static final int SCREEN_SIZE = 128;

    private static SaveGameSlot[] slots = new SaveGameSlot[SLOTS];

    private static SaveGameSlot autosaveSlot;

    public static final class SaveGameSlot implements Serializable
    {
        private static final long serialVersionUID = -136702861620738655L;

        public String fileName;

        public Date date;

        public byte[] screenshot;

        public byte[] saveData;

        public SaveGameSlot(String fileName) {
            this.fileName = fileName;
        }

        public boolean isLoaded()
        {
            return saveData != null;
        }

    }

    public static void init() throws IOException {
        File saveDir = new File("saves");
        if (!saveDir.exists()) {
            saveDir.mkdir();
        }

        for (int i = 0, slotsLength = slots.length; i < slotsLength; i++) {
            String fileName = "saves/save_" + i + ".bin";
            File f = new File(fileName);
            if (f.exists()) {
                ObjectInputStream ois = new ObjectInputStream(new FileInputStream(f));
                try {
                    slots[i] = (SaveGameSlot) ois.readObject();
                } catch (Exception e) {
                    logger.error("Failed to load save", e);
                }
                ois.close();
            } else {
                slots[i] = new SaveGameSlot(fileName);
            }
        }

        final String autosaveFileName = "saves/autosave.bin";
        File autosave = new File(autosaveFileName);
        if (autosave.exists()) {
            ObjectInputStream ois = new ObjectInputStream(new FileInputStream(autosave));
            try {
                autosaveSlot = (SaveGameSlot) ois.readObject();
            } catch (Exception e) {
                logger.error("Failed to load save", e);
            }
            ois.close();
        } else {
            autosaveSlot = new SaveGameSlot(autosaveFileName);
        }

    }

    public static void saveGame(SaveGameSlot slot, World world) {
        try {

            // take a screenshot
            GUI.getInstance().pushCurrentScreen();
            GUI.getInstance().getNifty().gotoScreen("empty_screen");
            Image screen = AuroraGame.takeScreenshot();
            GUI.getInstance().popAndSetScreen();

            Image scaled = screen.getScaledCopy(SCREEN_SIZE, SCREEN_SIZE);
            screen.destroy();
            BufferedImage bi = EngineUtils.convertToBufferedImage(scaled);
            scaled.destroy();

            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ImageIO.write(bi, "JPG", bos);
            slot.screenshot = bos.toByteArray();

            bos.reset();
            ObjectOutputStream oos = new ObjectOutputStream(bos);
            oos.writeObject(world);
            oos.close();

            slot.saveData = bos.toByteArray();
            slot.date = new Date();
            oos = new ObjectOutputStream(new FileOutputStream(slot.fileName));
            oos.writeObject(slot);
            oos.close();
        } catch (Exception ex) {
            logger.error("Failed to save game", ex);
        }
    }

    public static World loadGame(SaveGameSlot slot) {
        File f = new File(slot.fileName);
        if (!f.exists()) {
            return null;
        }
        try {
            ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(slot.saveData));
            World rz = (World) ois.readObject();
            ois.close();

            return rz;
        } catch (Exception e) {
            logger.error("Failed to load game", e);
        }
        return null;
    }

    public static SaveGameSlot[] getSlots() {
        return slots;
    }

    public static SaveGameSlot getAutosaveSlot() {
        return autosaveSlot;
    }
}
