/**
 * Created with IntelliJ IDEA.
 * User: Egor.Smirnov
 * Date: 18.01.13
 * Time: 16:24
 */
package ru.game.aurora.world.quest;


import ru.game.aurora.application.GameLogger;
import ru.game.aurora.dialog.Dialog;
import ru.game.aurora.world.World;
import ru.game.aurora.world.space.NPCShip;
import ru.game.aurora.world.space.StarSystem;

/**
 * Special quest probe. Appears in fourth visited system. Contains all astrodata for that system as well as records from Aurora
 * about their course and intentions.
 */
public class AuroraProbe extends NPCShip {
    private static final long serialVersionUID = -579871001131587178L;

    private Dialog dialog;

    private boolean contacted = false;

    public AuroraProbe(int x, int y) {
        super(x, y, "probe", null, null, "Probe-X513M");
        setAi(null);
        dialog = Dialog.loadFromFile(getClass().getClassLoader().getResourceAsStream("dialogs/quest/aurora_probe_1_found.json"));
    }

    @Override
    public boolean isHostile() {
        return false;
    }

    @Override
    public void onContact(World world) {
        world.addOverlayWindow(dialog);
        contacted = true;

        world.getPlayer().getResearchState().addNewAvailableProject(world.getResearchAndDevelopmentProjects().getResearchProjects().get("Probe data decoding"));

        StarSystem curSystem = (StarSystem) world.getCurrentRoom();
        int remainingAstroData = curSystem.getAstronomyData();
        GameLogger.getInstance().logMessage("Retrieved " + remainingAstroData + " new astro data from probe");
        curSystem.setAstronomyData(0);
        world.getPlayer().getResearchState().addProcessedAstroData(remainingAstroData);
    }

    @Override
    public boolean isAlive() {
        return !contacted;
    }

    @Override
    public String getScanDescription() {
        return "This is a probe #XNA-12 from Aurora vessel. According to telemetry data, it is functioning properly and is awaiting orders.";
    }
}
