/**
 * Created with IntelliJ IDEA.
 * User: Egor.Smirnov
 * Date: 18.01.13
 * Time: 16:24
 */
package ru.game.aurora.world.quest;


import ru.game.aurora.application.GameLogger;
import ru.game.aurora.npc.Dialog;
import ru.game.aurora.player.research.BaseResearchWithFixedProgress;
import ru.game.aurora.player.research.ResearchProjectDesc;
import ru.game.aurora.player.research.ResearchReport;
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

        ResearchProjectDesc researchProjectDesc = new BaseResearchWithFixedProgress("Probe data decoding", "Decoding data from probe storage. It can contain scientific data, as well as some information on its mothership location and progress", "probe_research", 50, 20);
        researchProjectDesc.setReport(new ResearchReport("probe", "After decoding data from probe storage we have learned some information about Aurora route." +
                " The last record was mentioning that they had searched stars nearby and found nothing interesting, and they were planning to make a long jump to sector" +
                " after [50, 10] and explore that part of space. \n" +
                " In addition probe storage contained some ship log messages and a backup copy of work done by Aurora science team. There is nothing really valuable for us there," +
                " but it will be usefull for researches on Earth."));

        world.getPlayer().getResearchState().getAvailableProjects().add(researchProjectDesc);

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
}
