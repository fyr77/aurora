/**
 * Created with IntelliJ IDEA.
 * User: Egor.Smirnov
 * Date: 05.03.13
 * Time: 13:24
 */

package ru.game.aurora.world.generation.humanity;

import ru.game.aurora.application.CommonRandom;
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
                "Hi, man! \n At last my time on Earth is over, they have finally finished building my ship and we are leaving in a couple of days." +
                        " And guess how they named her? Enterprise, like in that old movie, haha. \n" +
                        " So now we are joining you in your search. Looking forward to meet you in outer space, man. \n" +
                        " Derek McCartney, captain of UNS Enterprise, lol.",
                "message"
        ));

        world.getPlayer().getEarthState().getMessages().add(new PrivateMessage("Aurora project news: new ship launched"
        , "A new ship from the Aurora project has been successfully launched this friday. It will explore new sector of galaxy near the Orion star system, its main" +
                " objective is establishing diplomatic contacts with alien species in this region. \n " +
                " Ship is called UNS Enterprise, based on hundreds of thousands plees, received from Star Trek fans. It is the third ship in this series, capable of carrying up to a " +
                "thousand crewmembers, scientists and explorers." +
                "\n It return is scheduled for autumn of a next year.", "news"));


        NPCShip enterprise = new NPCShip(0, 0, "aurora", world.getRaces().get("Humanity"), null, "UNS Enterprise");

        world.addListener(new SingleShipEvent(0.1, enterprise));

        isAlive = false;
    }

    @Override
    public boolean isAlive() {
        return isAlive;
    }
}
