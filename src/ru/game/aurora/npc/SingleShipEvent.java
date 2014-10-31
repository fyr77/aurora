/**
 * Created with IntelliJ IDEA.
 * User: Egor.Smirnov
 * Date: 24.12.12
 * Time: 13:53
 */
package ru.game.aurora.npc;


import ru.game.aurora.application.CommonRandom;
import ru.game.aurora.dialog.Dialog;
import ru.game.aurora.world.GameEventListener;
import ru.game.aurora.world.GameObject;
import ru.game.aurora.world.World;
import ru.game.aurora.world.space.GalaxyMap;
import ru.game.aurora.world.space.StarSystem;

/**
 * Event when player has a fixed chance of meeting specific ship when entering arbitrary star system
 */
public class SingleShipEvent extends GameEventListener {
    private static final long serialVersionUID = 1L;

    protected final double chance;

    protected GameObject ship;

    protected Dialog starsystemEnterDialog = null;

    // if set, this event can happen only in star systems within this range from Solar system
    private int minRange = 0;
    private int maxRange = Integer.MAX_VALUE;

    public void setMinRange(int minRange) {
        this.minRange = minRange;
    }

    public void setMaxRange(int maxRange) {
        this.maxRange = maxRange;
    }


    public SingleShipEvent(double chance, GameObject ship) {
        this.chance = chance;
        this.ship = ship;
    }

    public SingleShipEvent(double chance, GameObject ship, Dialog starsystemEnterDialog) {
        this.chance = chance;
        this.ship = ship;
        this.starsystemEnterDialog = starsystemEnterDialog;
    }

    @Override
    public boolean onPlayerEnterStarSystem(World world, StarSystem ss) {
        // do not spawn in quest star systems
        if (ss.isQuestLocation()) {
            return false;
        }

        final double distFromSolarSystem = GalaxyMap.getDistance((StarSystem) world.getGlobalVariables().get("solar_system"), ss);
        if (distFromSolarSystem < minRange || distFromSolarSystem > maxRange) {
            return false;
        }

        if (CommonRandom.getRandom().nextDouble() < chance) {
            ss.setRandomEmptyPosition(ship);
            ss.getShips().add(ship);
            ship = null;
            if (starsystemEnterDialog != null) {
                world.addOverlayWindow(starsystemEnterDialog);
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean isAlive() {
        return ship != null;
    }
}
