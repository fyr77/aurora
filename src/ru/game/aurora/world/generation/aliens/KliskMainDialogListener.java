/**
 * Created with IntelliJ IDEA.
 * User: Egor.Smirnov
 * Date: 14.03.13
 * Time: 12:58
 */
package ru.game.aurora.world.generation.aliens;

import ru.game.aurora.application.GameLogger;
import ru.game.aurora.dialog.DialogListener;
import ru.game.aurora.npc.AlienRace;
import ru.game.aurora.player.research.BaseResearchWithFixedProgress;
import ru.game.aurora.player.research.ResearchProjectDesc;
import ru.game.aurora.player.research.ResearchReport;
import ru.game.aurora.world.World;
import ru.game.aurora.world.space.NPCShip;
import ru.game.aurora.world.space.SpaceObject;
import ru.game.aurora.world.space.StarSystem;

import java.util.Iterator;

/**
 * Processes outcome of default dialog with Klisk race
 */
public class KliskMainDialogListener implements DialogListener
{
    private static final long serialVersionUID = -2351530187782245878L;

    private AlienRace kliskRace;

    public KliskMainDialogListener(AlienRace kliskRace) {
        this.kliskRace = kliskRace;
    }

    private void destroyRogueScout(World world)
    {
        StarSystem ss = (StarSystem) world.getGlobalVariables().get("rogues.damaged_scout_found");
        if (ss == null) {
            throw new IllegalStateException("Can not sell information about rogues scout to Klisk. 'rogues.damaged_scout_found' global variable not set");
        }

        for (Iterator<SpaceObject> iter = ss.getShips().iterator(); iter.hasNext();){
            SpaceObject so = iter.next();
            if (so instanceof NPCShip && so.getName().equals("Rogue scout")) {
                iter.remove();
                break;
            }
        }

        world.getPlayer().changeCredits(10);
        GameLogger.getInstance().logMessage("Received 10 credits");
        kliskRace.setRelationToPlayer(kliskRace.getRelationToPlayer() + 1);
        world.getGlobalVariables().put("rogues.damage_scout_result", "sold_to_klisk");

        world.getGlobalVariables().remove("rogues.damaged_scout_found");
    }

    private void sellTerraformerInformation(World world)
    {
        if (world.getPlayer().getCredits() < 1) {
            GameLogger.getInstance().logMessage("Not enough credits");
            return;
        }

        world.getPlayer().changeCredits(-1);

        ResearchReport report = new ResearchReport(
                "astronomy_research"
                , "First section of processed data tells about an ancient 'Builder' race. This was an advanced civilisation that has created" +
                " a lot of huge astroengineering structures in galaxy. There ary Dyson spheres, artificial planets and others, their coordinates are mentioned in data, so we" +
                " may visit them and examine them ourselves. \n " +
                " Builders have disappeared from this Galaxy centuries ago, leaving everything they have created. And among their creations there were some really dangerous toys. \n" +
                "     One of them is known as 'Obliterator'. It is hard to believe that such enormous spaceship really exists. \n" +
                "     Obliterator is some kind of automated terraforming ship. But it transforms not only planets, but star systems themselves, including stars, satellites and other celestial bodies, " +
                " making all systems look the same, according to a given template. This is how these 'twin systems' are born. [Remark from Godon 'Ha, I was right about their origin, captain!'] \n" +
                " Obliterator seems to have no general purpose, it just follows its program and travels across galaxy, transforming all systems it meets on its way. \n" +
                "     Its current coordinates are well-known, and we should probably go and look at it. Research project has been added for that purpose. \n" +
                "     Though most probably we won't receive any new information. Records state that Builders technology is completely invulnerable, so we won't be able to sneak inside, or to chop off a piece" +
                " for detailed study. All such attempts, done by other civilisations for past millenia, have failed."
        );

        ResearchProjectDesc research = new BaseResearchWithFixedProgress(
                "'Obliterator'",
                "Study of materials provided by Klisk race about origin of 'twin' starsystems. Contains huge text, image and video archives.",
                "probe_research",
                report,
                20,
                30
        );

        world.getPlayer().getResearchState().addNewAvailableProject(research);
    }

    @Override
    public void onDialogEnded(World world, int returnCode)
    {
        switch (returnCode) {
            case 100:
                // player has given location of a damaged rogue scout
                destroyRogueScout(world);
                break;
            case 200:
                sellTerraformerInformation(world);
                break;
        }
    }
}
