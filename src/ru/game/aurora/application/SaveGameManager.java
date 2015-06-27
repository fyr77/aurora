/**
 * User: jedi-philosopher
 * Date: 21.01.13
 * Time: 21:53
 */
package ru.game.aurora.application;

import org.newdawn.slick.Image;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.game.aurora.util.EngineUtils;
import ru.game.aurora.world.GameEventListener;
import ru.game.aurora.world.World;
import ru.game.aurora.world.space.GalaxyMap;
import ru.game.aurora.world.space.StarSystem;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.Date;

public class SaveGameManager {
    private static final Logger logger = LoggerFactory.getLogger(SaveGameManager.class);

    private static final int SLOTS = 4;

    public static final int SCREEN_SIZE = 128;

    private static SaveGameSlot[] slots = new SaveGameSlot[SLOTS];

    private static SaveGameSlot autosaveSlot;

    public static class Autosaver extends GameEventListener {
        private static final long serialVersionUID = -5194873367259385934L;

        @Override
        public boolean onPlayerEnterStarSystem(World world, StarSystem ss) {
            SaveGameManager.saveGame(SaveGameManager.autosaveSlot, world);
            return false;
        }
    }

    public static final class SaveGameSlot implements Serializable
    {
        private static final long serialVersionUID = -136702861620738655L;

        public String fileName;

        public Date date;

        public String gameDate;

        public String gameLocation;

        public byte[] screenshot;

        public byte[] saveData;

        public boolean isAutosave = false;

        private transient Image screenshotImage;

        public SaveGameSlot(String fileName) {
            this.fileName = fileName;
        }

        public boolean isLoaded()
        {
            return saveData != null;
        }

        public Image getScreenshot() {
            if (screenshotImage == null) {
                if (screenshot == null) {
                    screenshotImage = ResourceManager.getInstance().getImage("no_image");
                } else {

                    try {
                        BufferedImage read = ImageIO.read(new ByteArrayInputStream(screenshot));
                        screenshotImage = EngineUtils.createImage(read);
                    } catch (Exception e) {
                        logger.error("Failed to read image", e);
                        screenshotImage = ResourceManager.getInstance().getImage("no_image");
                    }
                }
            }
            return screenshotImage;
        }
    }

    public static boolean hasSaves() {
        if (autosaveSlot.isLoaded()) {
            return true;
        }

        for (SaveGameSlot slot : slots) {
            if (slot.isLoaded()) {
                return true;
            }
        }

        return false;
    }

    public static void init() throws IOException {
        final File outDir = AuroraGame.getOutDir();
        File saveDir = new File(outDir, "saves");
        if (!saveDir.exists()) {
            saveDir.mkdir();
        }

        for (int i = 0, slotsLength = slots.length; i < slotsLength; i++) {
            String fileName = "saves/save_" + i + ".bin";
            File f = new File(outDir, fileName);
            try {
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
            } catch (Exception e) {
                logger.error("Save game file in slot " + i + " is corrupted and can not be loaded", e);
                slots[i] = new SaveGameSlot(fileName);
            }

        }

        final String autosaveFileName = "saves/autosave.bin";
        File autosave = new File(outDir, autosaveFileName);
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
            autosaveSlot.isAutosave = true;
        }

    }


    public static void saveGame(SaveGameSlot slot, World world) {
        try {

            // take a screenshot

            if (world.getCurrentStarSystem() != null) {
                slot.gameLocation = world.getCurrentStarSystem().getName();
            } else if (world.getCurrentRoom().getClass().equals(GalaxyMap.class)) {
                slot.gameLocation = Localization.getText("gui", "space.galaxy_map");
            }

            slot.gameDate = world.getCurrentDateString();
            Image screen = AuroraGame.takeScreenshot();

            BufferedImage bi = EngineUtils.convertToBufferedImage(screen);
            BufferedImage resizedImage = new BufferedImage(SCREEN_SIZE, SCREEN_SIZE, BufferedImage.TYPE_3BYTE_BGR);
            Graphics2D g = resizedImage.createGraphics();
            g.drawImage(bi, 0, 0, SCREEN_SIZE, SCREEN_SIZE, null);
            g.dispose();
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ImageIO.write(resizedImage, "JPG", bos);
            slot.screenshot = bos.toByteArray();
            slot.screenshotImage = null;
            // do not set slot.screenshotImage as it leads to strange bugs

            bos.reset();
            ObjectOutputStream oos = new ObjectOutputStream(bos);
            oos.writeObject(world);
            oos.close();

            slot.saveData = bos.toByteArray();
            slot.date = new Date();
            oos = new ObjectOutputStream(new FileOutputStream(new File(AuroraGame.getOutDir(), slot.fileName)));
            oos.writeObject(slot);
            oos.close();
        } catch (Exception ex) {
            logger.error("Failed to save game", ex);
        }
    }

    public static World loadGame(SaveGameSlot slot) {
        File f = new File(AuroraGame.getOutDir(), slot.fileName);
        if (!f.exists()) {
            return null;
        }
        try {
            ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(slot.saveData));
            World rz = (World) ois.readObject();
            ois.close();
            rz.checkCheats();
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
