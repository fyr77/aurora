/**
 * Created with IntelliJ IDEA.
 * User: Egor.Smirnov
 * Date: 05.03.13
 * Time: 13:24
 */

package ru.game.aurora.world.generation.humanity;

import ru.game.aurora.application.CommonRandom;
import ru.game.aurora.application.GameLogger;
import ru.game.aurora.dialog.Dialog;
import ru.game.aurora.dialog.DialogListener;
import ru.game.aurora.npc.NPC;
import ru.game.aurora.npc.SingleShipEvent;
import ru.game.aurora.player.earth.PrivateMessage;
import ru.game.aurora.world.GameEventListener;
import ru.game.aurora.world.World;
import ru.game.aurora.world.space.NPCShip;

/**
 * At some random moment in game, in about 100 turns, spawns next ship of Aurora project - Enterprise.
 * Adds private messages from its captain.
 * Adds chance of meeting it in space.
 */
public class EnterpriseEncounterCreator extends GameEventListener
{
    private static final long serialVersionUID = 2077112120543067387L;

    private int turnCount;

    private boolean isAlive = true;

    public EnterpriseEncounterCreator()
    {
        turnCount = CommonRandom.getRandom().nextInt(50) + 50;
    }

    private static class EnterpriseDialogListener implements DialogListener
    {

        private static final long serialVersionUID = 8961118747394070541L;

        @Override
        public void onDialogEnded(World world, int returnCode) {
            if (returnCode == 1) {
                // repair
                final int maxHull = world.getPlayer().getShip().getMaxHull();
                if (world.getPlayer().getShip().getHull() == maxHull) {
                    GameLogger.getInstance().logMessage("Ship's hull is not damaged");
                } else {
                    GameLogger.getInstance().logMessage("Enterprise engineers helped our crew in fixing breaches in ship hull");
                    world.getPlayer().getShip().setHull(maxHull);
                }
            }
        }
    }

    @Override
    public void onTurnEnded(World world) {
        if (!isAlive) {
            return;
        }
        if (world.getTurnCount() < turnCount) {
            return;
        }

        // spawn private messages.
        world.getPlayer().getEarthState().getMessages().add(new PrivateMessage(
                "At last",
                "Hello! \n At last my time on Earth is over, they have finally finished building my ship and we are leaving in a couple of days. So don't think you will be ahead of your teacher anymore." +
                        " And guess how they named her? Enterprise, like in that old movie, heh. \n" +
                        " So now we are joining you in your search. Looking forward to meet you in outer space, boy. \n" +
                        " Derek McCartney, captain of UNS Enterprise, can't stop laughing from that name.",
                "message"
        ));

        world.getPlayer().getEarthState().getMessages().add(new PrivateMessage("Aurora project news: new ship launched"
        , "A new ship from the Aurora project has been successfully launched this friday. It will explore new sector of galaxy near the Orion star system, its main" +
                " objective is establishing diplomatic contacts with alien species in this region. \n " +
                " Ship is called UNS Enterprise, based on hundreds of thousands pleas, received from Star Trek fans. It is the third ship in this series, capable of carrying up to a " +
                "thousand crew members, scientists and explorers." +
                "\n It return is scheduled for autumn of a next year.", "news"));


        Dialog captainDialog = Dialog.loadFromFile("dialogs/encounters/enterprise.json");
        captainDialog.setListener(new EnterpriseDialogListener());

        NPCShip enterprise = new NPCShip(0, 0, "aurora", world.getRaces().get("Humanity"), new NPC(captainDialog), "UNS Enterprise");

        world.addListener(new SingleShipEvent(0.9, enterprise));

        isAlive = false;
    }

    @Override
    public boolean isAlive() {
        return isAlive;
    }
}
