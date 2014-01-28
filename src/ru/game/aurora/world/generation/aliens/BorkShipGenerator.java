package ru.game.aurora.world.generation.aliens;

/**
 * Created with IntelliJ IDEA.
 * User: User
 * Date: 26.10.13
 * Time: 17:32
 * To change this template use File | Settings | File Templates.
 */
/**
 * Created with IntelliJ IDEA.
 * User: Egor.Smirnov
 * Date: 18.01.13
 * Time: 17:13
 */

import ru.game.aurora.application.CommonRandom;
import ru.game.aurora.dialog.Dialog;
import ru.game.aurora.npc.NPCShipFactory;
import ru.game.aurora.world.GameEventListener;
import ru.game.aurora.world.World;
import ru.game.aurora.world.space.NPCShip;
import ru.game.aurora.world.space.StarSystem;

/**
 * Creates given alien ship when player enters Xth starsystem for first time
 */
public class BorkShipGenerator extends GameEventListener {
    private static final long serialVersionUID = 1;

    private int count = 0;

    private NPCShipFactory factory;

    private int starshipsPerSystem;

    // optional dialog that will appear when player enters star system and notifies about presence of event starship
    private Dialog dialog;

    private double chance;


    public BorkShipGenerator(double chance, int count, Dialog dialog, NPCShipFactory factory, int starshipsPerSystem) {
        this.chance = chance;
        this.count = count;
        this.dialog = dialog;
        this.factory = factory;
        this.starshipsPerSystem = starshipsPerSystem;
        this.setGroups(GameEventListener.EventGroup.ENCOUNTER_SPAWN);
    }

    @Override
    public boolean onPlayerEnterStarSystem(World world, StarSystem ss) {
        if (ss.isQuestLocation()) {
            return false;
        }
        if (!ss.isVisited() && CommonRandom.getRandom().nextDouble() < chance && count-- > 0) {
            for (int i = 0; i < starshipsPerSystem; ++i) {
                NPCShip ship = factory.createShip(0);
                ss.setRandomEmptyPosition(ship);
                ss.getShips().add(ship);
            }
            if (dialog != null) {
                world.addOverlayWindow(dialog);
            }
            return true;
        }

        return false;
    }

    @Override
    public boolean isAlive() {
        return count > 0;
    }
}
