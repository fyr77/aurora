/**
 * Created with IntelliJ IDEA.
 * User: Egor.Smirnov
 * Date: 14.03.13
 * Time: 12:58
 */
package ru.game.aurora.world.generation.aliens;

import ru.game.aurora.application.GameLogger;
import ru.game.aurora.application.Localization;
import ru.game.aurora.dialog.Dialog;
import ru.game.aurora.dialog.DialogListener;
import ru.game.aurora.dialog.Reply;
import ru.game.aurora.dialog.Statement;
import ru.game.aurora.npc.AlienRace;
import ru.game.aurora.npc.NPC;
import ru.game.aurora.npc.NPCShipFactory;
import ru.game.aurora.npc.SingleStarsystemShipSpawner;
import ru.game.aurora.player.earth.PrivateMessage;
import ru.game.aurora.world.World;
import ru.game.aurora.world.space.NPCShip;
import ru.game.aurora.world.space.SpaceObject;
import ru.game.aurora.world.space.StarSystem;

import java.util.Iterator;

/**
 * Processes outcome of default dialog with Klisk race
 */
public class KliskMainDialogListener implements DialogListener {
    private static final long serialVersionUID = -2351530187782245878L;

    private AlienRace kliskRace;

    public KliskMainDialogListener(AlienRace kliskRace) {
        this.kliskRace = kliskRace;
    }

    private void destroyRogueScout(World world) {
        StarSystem ss = (StarSystem) world.getGlobalVariables().get("rogues.damaged_scout_found");
        if (ss == null) {
            throw new IllegalStateException("Can not sell information about rogues scout to Klisk. 'rogues.damaged_scout_found' global variable not set");
        }

        for (Iterator<SpaceObject> iter = ss.getShips().iterator(); iter.hasNext(); ) {
            SpaceObject so = iter.next();
            if (so instanceof NPCShip && so.getName().equals("Rogue scout")) {
                iter.remove();
                break;
            }
        }

        world.getPlayer().changeCredits(10);
        GameLogger.getInstance().logMessage(String.format(Localization.getText("gui", "logging.credits_received"), 10));
        kliskRace.setRelationToPlayer(kliskRace.getRelationToPlayer() + 1);
        world.getGlobalVariables().put("rogues.damage_scout_result", "sold_to_klisk");

        world.getGlobalVariables().remove("rogues.damaged_scout_found");
    }

    private void sellTerraformerInformation(World world) {
        if (world.getPlayer().getCredits() < 1) {
            GameLogger.getInstance().logMessage(Localization.getText("gui", "logging.not_enough_credits"));
            return;
        }

        world.getPlayer().changeCredits(-1);


        world.getGlobalVariables().put("quest.main.knows_about_obliterator", null);
        world.getPlayer().getResearchState().addNewAvailableProject(world.getResearchAndDevelopmentProjects().getResearchProjects().get("Obliterator"));
    }

    private void helpWithEvacuation(World world) {
        if (world.getPlayer().getCredits() < 10) {
            GameLogger.getInstance().logMessage(Localization.getText("gui", "logging.not_enough_credits"));
            return;
        }
        GameLogger.getInstance().logMessage(Localization.getText("gui", "logging.klisk_joined_evac"));
        world.getPlayer().getEarthState().getEvacuationState().changeEvacuationSpeed(10000 / 30);

        if (world.getGlobalVariables().containsKey("quest.main.klisk_evacuation_help")) {
            return;
        }

        world.getPlayer().getEarthState().getMessages().add(new PrivateMessage(
                "klisk_help_evac"
                , "news"
        ));

        // set custom dialog for those ships that can be met in solar system and help with evac
        final NPCShipFactory kliskDefaultFactory = world.getRaces().get("Klisk").getDefaultFactory();
        world.addListener(new SingleStarsystemShipSpawner(
                new NPCShipFactory() {

                    private static final long serialVersionUID = 1061545299484580242L;

                    @Override
                    public NPCShip createShip() {
                        NPCShip ship = kliskDefaultFactory.createShip();
                        ship.setCaptain(new NPC(new Dialog("klisk_evac_dialog", "klisk_dialog", new Statement(0, "", new Reply(0, -1, "end")))));
                        return ship;
                    }
                }
                , 0.1, world.getRaces().get("Humanity").getHomeworld()
        ));

    }

    @Override
    public void onDialogEnded(World world, int returnCode) {
        switch (returnCode) {
            case 100:
                // player has given location of a damaged rogue scout
                destroyRogueScout(world);
                break;
            case 200:
                sellTerraformerInformation(world);
                break;
            case 300:
                helpWithEvacuation(world);
                break;
        }
    }
}
