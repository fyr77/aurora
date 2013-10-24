/**
 * Created with IntelliJ IDEA.
 * User: Egor.Smirnov
 * Date: 18.01.13
 * Time: 17:13
 */
package ru.game.aurora.npc;

import ru.game.aurora.dialog.Dialog;
import ru.game.aurora.world.GameEventListener;
import ru.game.aurora.world.World;
import ru.game.aurora.world.space.NPCShip;
import ru.game.aurora.world.space.StarSystem;

/**
 * Creates given alien ship when player enters Xth starsystem for first time
 */
public class SingleShipFixedTime extends GameEventListener
{
    private static final long serialVersionUID = 6541906542086925960L;

    private int x;

    private int count = 0;

    private NPCShip ship;

    // optional dialog that will appear when player enters star system and notifies about presence of event starship
    private Dialog dialog;

    public SingleShipFixedTime(int x, NPCShip ship) {
        this.x = x;
        this.ship = ship;
    }

    public SingleShipFixedTime(int x, NPCShip ship, Dialog dialog) {
        this.x = x;
        this.ship = ship;
        this.dialog = dialog;
    }

    @Override
    public void onPlayerEnterStarSystem(World world, StarSystem ss) {
        if (ss.isQuestLocation()) {
            return;
        }
        if (!ss.isVisited() && ++count == x) {
            ss.setRandomEmptyPosition(ship);
            ss.getShips().add(ship);
            ship = null;
            if (dialog != null) {
                world.addOverlayWindow(dialog);
            }
        }

    }

    @Override
    public boolean isAlive() {
        return ship != null;
    }
}
