package ru.game.aurora.world.space;

import org.newdawn.slick.GameContainer;
import ru.game.aurora.application.Configuration;
import ru.game.aurora.application.GameLogger;
import ru.game.aurora.application.Localization;
import ru.game.aurora.npc.shipai.KeepOrbitAI;
import ru.game.aurora.world.World;
import ru.game.aurora.world.generation.humanity.HumanityGenerator;

/**
 * Astro probe flies around the starsystem and collects astro data
 */
public class AstroProbe extends NPCShip {
    private static final long serialVersionUID = 1L;

    private int lastCheckTurn;

    private int collectedAstroData = 0;

    private double cache = 0;

    public AstroProbe(World world) {
        super(0, 0, "probe", world.getFactions().get(HumanityGenerator.NAME), null, "Astroprobe", 4);
        setAi(new KeepOrbitAI(false));
        lastCheckTurn = world.getTurnCount();
    }

    @Override
    public void update(GameContainer container, World world) {
        super.update(container, world);

        StarSystem ss = world.getCurrentStarSystem();
        if (ss == null || ss.getAstronomyData() == 0) {
            return;
        }

        final int daysSinceLastCheck = world.getTurnCount() - lastCheckTurn;
        final double shouldHaveCollected = (daysSinceLastCheck * Configuration.getDoubleProperty("upgrades.astroprobe.collect_speed"));
        cache += Math.min(ss.getAstronomyData(), shouldHaveCollected);

        if (cache >= 1) {
            ss.setAstronomyData(ss.getAstronomyData() - (int) cache);
            collectedAstroData += (int) cache;
            cache -= (int) cache;
        }
    }

    @Override
    public void interact(World world) {
        isAlive = false;
        GameLogger.getInstance().logMessage(String.format(Localization.getText("gui", "space.astroprobe.collected"), collectedAstroData));
        world.getPlayer().getResearchState().addProcessedAstroData(collectedAstroData);
    }

    @Override
    public String getScanDescription(World world) {
        return String.format(Localization.getText("gui", "space.astroprobe.scan"), collectedAstroData, world.getCurrentStarSystem().getAstronomyData());
    }
}
