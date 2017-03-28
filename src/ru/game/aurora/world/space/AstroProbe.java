package ru.game.aurora.world.space;

import org.newdawn.slick.GameContainer;
import ru.game.aurora.application.Configuration;
import ru.game.aurora.application.GameLogger;
import ru.game.aurora.application.Localization;
import ru.game.aurora.npc.shipai.KeepOrbitAI;
import ru.game.aurora.player.Resources;
import ru.game.aurora.world.World;

/**
 * Astro probe flies around the starsystem and collects astro data
 */
public class AstroProbe extends NPCShip {
    private static final long serialVersionUID = 1L;

    private int lastCheckTurn;

    private int collectedAstroData = 0;

    private double cache = 0;

    public AstroProbe(World world) {
        super("drone", 0, 0);
        setAi(new KeepOrbitAI(false));
        lastCheckTurn = world.getDayCount();
    }

    @Override
    public void update(GameContainer container, World world) {
        super.update(container, world);

        if (!world.isUpdatedThisFrame()) {
            return;
        }

        StarSystem ss = world.getCurrentStarSystem();
        if (ss == null || ss.getAstronomyData() == 0) {
            return;
        }

        final int daysSinceLastCheck = world.getDayCount() - lastCheckTurn;
        lastCheckTurn = world.getDayCount();
        final double shouldHaveCollected = (daysSinceLastCheck * Configuration.getDoubleProperty("upgrades.astroprobe.collect_speed"));
        cache += Math.min(ss.getAstronomyData(), shouldHaveCollected);

        if (cache >= 1) {
            ss.setAstronomyData(ss.getAstronomyData() - (int) cache);
            collectedAstroData += (int) cache;
            cache -= (int) cache;
        }
    }

    @Override
    public boolean interact(World world) {
        isAlive = false;
        GameLogger.getInstance().logMessage(String.format(Localization.getText("gui", "space.astroprobe.collected"), collectedAstroData));
        world.getPlayer().getResearchState().addProcessedAstroData(collectedAstroData);
        world.getPlayer().changeResource(world, Resources.RU, Configuration.getIntProperty("upgrades.astroprobe.price") / 2);
        return true;
    }

    @Override
    public String getScanDescription(World world) {
        return String.format(Localization.getText("gui", "space.astroprobe.scan"), collectedAstroData, world.getCurrentStarSystem().getAstronomyData());
    }
}
