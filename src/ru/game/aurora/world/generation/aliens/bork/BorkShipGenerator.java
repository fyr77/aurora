package ru.game.aurora.world.generation.aliens.bork;

/**
 * Created with IntelliJ IDEA.
 * User: User
 * Date: 26.10.13
 * Time: 17:32
 */


import ru.game.aurora.application.CommonRandom;
import ru.game.aurora.dialog.Dialog;
import ru.game.aurora.dialog.DialogListener;
import ru.game.aurora.npc.NPC;
import ru.game.aurora.npc.NPCShipFactory;
import ru.game.aurora.world.GameEventListener;
import ru.game.aurora.world.World;
import ru.game.aurora.world.space.NPCShip;
import ru.game.aurora.world.space.StarSystem;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Creates given alien ship when player enters Xth starsystem for first time
 */
public class BorkShipGenerator extends GameEventListener {
    private static final long serialVersionUID = 1;

    private int count = 0;

    private final NPCShipFactory factory;

    private final int starshipsPerSystem;

    // optional dialog that will appear when player enters star system and notifies about presence of event starship
    private final Dialog dialog;

    private final double chance;


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

        final List<NPCShip> generatedShips = new ArrayList<>(starshipsPerSystem);
        Dialog d = Dialog.loadFromFile("dialogs/bork/bork_default_aggressive.json");
        d.addListener(new DialogListener() {
            private static final long serialVersionUID = -9186250921578191503L;

            @Override
            public void onDialogEnded(World world, Dialog dialog, int returnCode, Map<String, String> flags) {
                for (NPCShip ship : generatedShips) {
                    ship.setHostile(true);

                }
            }
        });

        if (!ss.isVisited() && CommonRandom.getRandom().nextDouble() < chance && count-- > 0) {
            for (int i = 0; i < starshipsPerSystem; ++i) {
                NPCShip ship = factory.createShip(world, 0);
                ship.setRace(null); // so that destroying these pirates will not reduce relationship with bork
                ss.setRandomEmptyPosition(ship);
                ship.setCaptain(new NPC(d));
                ss.getShips().add(ship);
                generatedShips.add(ship);
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
