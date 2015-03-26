/**
 * Created with IntelliJ IDEA.
 * User: Egor.Smirnov
 * Date: 05.03.13
 * Time: 13:24
 */

package ru.game.aurora.world.generation.humanity;

import ru.game.aurora.application.CommonRandom;
import ru.game.aurora.application.GameLogger;
import ru.game.aurora.application.Localization;
import ru.game.aurora.dialog.Dialog;
import ru.game.aurora.dialog.DialogListener;
import ru.game.aurora.npc.NPC;
import ru.game.aurora.npc.SingleShipEvent;
import ru.game.aurora.player.earth.PrivateMessage;
import ru.game.aurora.world.GameEventListener;
import ru.game.aurora.world.World;
import ru.game.aurora.world.space.NPCShip;

import java.util.Map;

/**
 * At some random moment in game, in about 100 turns, spawns next ship of Aurora project - Enterprise.
 * Adds private messages from its captain.
 * Adds chance of meeting it in space.
 */
public class EnterpriseEncounterCreator extends GameEventListener {
    private static final long serialVersionUID = 2077112120543067387L;

    private final int turnCount;

    private boolean isAlive = true;

    public EnterpriseEncounterCreator() {
        turnCount = CommonRandom.getRandom().nextInt(50) + 50;
        setGroups(EventGroup.ENCOUNTER_SPAWN);
    }

    private static class EnterpriseDialogListener implements DialogListener {

        private static final long serialVersionUID = 8961118747394070541L;

        @Override
        public void onDialogEnded(World world, Dialog dialog, int returnCode, Map<String, String> flags) {
            if (returnCode == 1) {
                // repair
                final int maxHull = world.getPlayer().getShip().getMaxHull();
                if (world.getPlayer().getShip().getHull() == maxHull) {
                    GameLogger.getInstance().logMessage(Localization.getText("gui", "logging.enterprise.hull_not_damaged"));
                } else {
                    GameLogger.getInstance().logMessage(Localization.getText("gui", "logging.enterprise.help"));
                    world.getPlayer().getShip().setHull(maxHull);
                }
            }
        }
    }

    @Override
    public boolean onTurnEnded(World world) {
        if (!isAlive) {
            return false;
        }
        if (world.getTurnCount() < turnCount) {
            return false;
        }

        // spawn private messages.
        world.getPlayer().getEarthState().getMessages().add(new PrivateMessage(
                world,
                "enterprise_1",
                "message"
        ));

        world.getPlayer().getEarthState().getMessages().add(new PrivateMessage(world, "news_sender", "enterprise_launch", "news"));


        Dialog captainDialog = Dialog.loadFromFile("dialogs/encounters/enterprise.json");
        captainDialog.addListener(new EnterpriseDialogListener());

        NPCShip enterprise = new NPCShip(0, 0, "aurora", world.getFactions().get("Humanity"), new NPC(captainDialog), "UNS Enterprise", 10);

        world.addListener(new SingleShipEvent(0.9, enterprise));

        isAlive = false;

        return true;
    }

    @Override
    public boolean isAlive() {
        return isAlive;
    }
}
