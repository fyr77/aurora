package ru.game.aurora.modding;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.game.aurora.world.World;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Mod collection
 */
public class ModManager {
    private static final String MODS_DIR = "MODS";
    private static final Logger logger = LoggerFactory.getLogger(ModManager.class);
    private static ModManager instance;
    private List<Mod> mods = new ArrayList<>();

    public static void init() {
        instance = new ModManager();
        File modsRoot = new File(MODS_DIR);
        if (!modsRoot.exists() || !modsRoot.isDirectory()) {
            logger.info("Mods directory does not exist, skipping mods loading");
            return;
        }
        logger.info("Loading mods...");
        for (File f : modsRoot.listFiles()) {
            if (f.isDirectory()) {
                logger.info("Loading mod from " + f);
                final Mod e = new Mod(f);
                instance.mods.add(e);
                if (!e.isLoaded()) {
                    logger.warn("Mod at {} failed to load", f);
                }
            }
        }
        logger.info("Done loading mods, {} mods total ", instance.mods.size());
    }

    public static ModManager getInstance() {
        return instance;
    }

    public void onNewGameStarted(World world) {
        for (Mod m : mods) {
            if (m.isLoaded()) {
                try {
                    m.getModClass().onNewGameStarted(world);
                } catch (Exception e) {
                    logger.error("Mod " + m.getManifest().modName + " raised Exception in onNewGameStarted()", e);
                    m.unload();
                }
            }
        }
    }

    public void onGameLoaded(World world) {
        for (Mod m : mods) {
            if (m.isLoaded()) {
                try {
                    m.getModClass().onGameLoaded(world);
                } catch (Exception e) {
                    logger.error("Mod " + m.getManifest().modName + " raised Exception in onGameLoaded()", e);
                    m.unload();
                }
            }
        }
    }
}
